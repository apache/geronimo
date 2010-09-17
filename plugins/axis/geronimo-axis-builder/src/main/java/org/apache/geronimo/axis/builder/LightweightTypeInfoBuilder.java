/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.axis.builder;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.axis.description.ElementDesc;
import org.apache.axis.description.FieldDesc;
import org.apache.axis.encoding.ser.ArrayDeserializerFactory;
import org.apache.axis.encoding.ser.ArraySerializerFactory;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.apache.geronimo.axis.client.TypeInfo;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.geronimo.webservices.builder.SchemaTypeKey;
import org.apache.geronimo.webservices.builder.WSDescriptorParser;
import org.apache.openejb.jee.JavaWsdlMapping;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaType;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class LightweightTypeInfoBuilder implements TypeInfoBuilder {
    private final Bundle cl;
    private final Map schemaTypeKeyToSchemaTypeMap;
    private final Set wrapperElementQNames;

    public LightweightTypeInfoBuilder(Bundle bundle, Map schemaTypeKeyToSchemaTypeMap, Set wrapperElementQNames) {
        this.cl = bundle;
        this.schemaTypeKeyToSchemaTypeMap = schemaTypeKeyToSchemaTypeMap;
        this.wrapperElementQNames = wrapperElementQNames;
    }

    public List buildTypeInfo(JavaWsdlMapping mapping) throws DeploymentException {
        List typeInfoList = new ArrayList();

        for (Iterator iterator = schemaTypeKeyToSchemaTypeMap.keySet().iterator(); iterator.hasNext();) {
            SchemaTypeKey key = (SchemaTypeKey) iterator.next();
            if (!key.isElement() && !key.isAnonymous()) {
                //default settings
                QName typeQName = key.getqName();
                String namespace = typeQName.getNamespaceURI();
                String packageName = WSDescriptorParser.getPackageFromNamespace(namespace, mapping);
                String classShortName = typeQName.getLocalPart();
                String className = packageName + "." + classShortName;

                Class clazz = null;
                try {
                    clazz = ClassLoading.loadClass(className, cl);
                } catch (ClassNotFoundException e) {
                    throw new DeploymentException("Could not load java type", e);
                }

                Class serializerFactoryClass = BeanSerializerFactory.class;
                Class deserializerFactoryClass = BeanDeserializerFactory.class;

                if (clazz.isArray()) {
                    serializerFactoryClass = ArraySerializerFactory.class;
                    deserializerFactoryClass = ArrayDeserializerFactory.class;
                }

                TypeInfo.UpdatableTypeInfo internalTypeInfo = new TypeInfo.UpdatableTypeInfo();
                internalTypeInfo.setClazz(clazz);
                internalTypeInfo.setQName(typeQName);
                internalTypeInfo.setSerializerClass(serializerFactoryClass);
                internalTypeInfo.setDeserializerClass(deserializerFactoryClass);

                populateInternalTypeInfo(clazz, typeQName, key, internalTypeInfo);

                typeInfoList.add(internalTypeInfo.buildTypeInfo());
            }
        }

        return typeInfoList;
    }

    private void populateInternalTypeInfo(Class javaClass, QName typeQName, SchemaTypeKey key, TypeInfo.UpdatableTypeInfo typeInfo) throws DeploymentException {
        SchemaType schemaType = (SchemaType) schemaTypeKeyToSchemaTypeMap.get(key);
        if (schemaType == null) {
            throw new DeploymentException("Schema type key " + key + " not found in analyzed schema: " + schemaTypeKeyToSchemaTypeMap);
        }
        typeInfo.setCanSearchParents(schemaType.getDerivationType() == SchemaType.DT_RESTRICTION);

        Map nameToType = new HashMap();
        if (null  == schemaType.getContentModel()) {

        } else if (SchemaParticle.SEQUENCE == schemaType.getContentModel().getParticleType()
                || SchemaParticle.ALL == schemaType.getContentModel().getParticleType()) {
            SchemaParticle[] properties = schemaType.getContentModel().getParticleChildren();
            for (int i = 0; i < properties.length; i++) {
                SchemaParticle parameter = properties[i];
//                if (SchemaParticle.ELEMENT != parameter.getType().getContentModel().getParticleType()) {
//                    throw new DeploymentException(parameter.getName() + " is not an element in schema " + schemaType.getName());
//                }
                nameToType.put(parameter.getName(), parameter);
            }
        } else if (SchemaParticle.ELEMENT == schemaType.getContentModel().getParticleType()) {
            SchemaParticle parameter = schemaType.getContentModel();
            nameToType.put(parameter.getName(), parameter);
        } else {
            throw new DeploymentException("Only all, choice and sequence particle types are supported." +
                    " SchemaType name =" + schemaType.getName());
        }

        PropertyDescriptor[] descriptors;
        try {
            descriptors = Introspector.getBeanInfo(javaClass).getPropertyDescriptors();
        } catch (IntrospectionException e) {
            throw new DeploymentException("Class " + javaClass + " is not a valid javabean", e);
        }
        Map nameToClass = new HashMap();
        for (int i = 0; i < descriptors.length; i++) {
            nameToClass.put(descriptors[i].getName(), descriptors[i].getPropertyType());
        }

        int idx = 0;
        FieldDesc[] fields = new FieldDesc[nameToType.size()];
        typeInfo.setFields(fields);
        for (Iterator iter = nameToType.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            QName fieldQName = (QName) entry.getKey();
            String fieldName = fieldQName.getLocalPart();
            SchemaParticle particle = (SchemaParticle) entry.getValue();

            ElementDesc elementDesc = new ElementDesc();
            elementDesc.setFieldName(fieldName);

            Class javaType = (Class) nameToClass.get(fieldName);
            if (null == javaType) {
                throw new DeploymentException("Field " + fieldName + " is not defined by class " + javaClass.getName());
            }
            elementDesc.setNillable(particle.isNillable());
            elementDesc.setXmlName(fieldQName);
            elementDesc.setXmlType(particle.getType().getName());

            if (javaType.isArray()) {
                elementDesc.setMinOccurs(particle.getIntMinOccurs());
                elementDesc.setMaxOccurs(particle.getIntMaxOccurs());
                //TODO axis seems to have the wrong name for this property based on how it is used
                elementDesc.setMaxOccursUnbounded(particle.getIntMaxOccurs() > 1);
            }

            fields[idx++] = elementDesc;
        }
    }
}
