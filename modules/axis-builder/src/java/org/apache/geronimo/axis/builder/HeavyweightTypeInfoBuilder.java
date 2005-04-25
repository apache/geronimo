/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.axis.description.AttributeDesc;
import org.apache.axis.description.ElementDesc;
import org.apache.axis.description.FieldDesc;
import org.apache.axis.encoding.ser.ArrayDeserializerFactory;
import org.apache.axis.encoding.ser.ArraySerializerFactory;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.apache.axis.encoding.ser.SimpleListDeserializerFactory;
import org.apache.axis.encoding.ser.SimpleListSerializerFactory;
import org.apache.geronimo.axis.client.TypeInfo;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.geronimo.xbeans.j2ee.JavaWsdlMappingType;
import org.apache.geronimo.xbeans.j2ee.JavaXmlTypeMappingType;
import org.apache.geronimo.xbeans.j2ee.VariableMappingType;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaType;

/**
 * @version $Rev:  $ $Date:  $
 */
public class HeavyweightTypeInfoBuilder implements TypeInfoBuilder {
    private static final String SOAP_ENCODING_NS = "http://schemas.xmlsoap.org/soap/encoding/";
    private final ClassLoader cl;
    private final Map schemaTypeKeyToSchemaTypeMap;
    private final Set wrapperElementQNames;
    private final boolean hasEncoded;

    public HeavyweightTypeInfoBuilder(ClassLoader cl, Map schemaTypeKeyToSchemaTypeMap, Set wrapperElementQNames, boolean hasEncoded) {
        this.cl = cl;
        this.schemaTypeKeyToSchemaTypeMap = schemaTypeKeyToSchemaTypeMap;
        this.wrapperElementQNames = wrapperElementQNames;
        this.hasEncoded = hasEncoded;
    }

    public List buildTypeInfo(JavaWsdlMappingType mapping) throws DeploymentException {
        List typeInfoList = new ArrayList();

        JavaXmlTypeMappingType[] javaXmlTypeMappings = mapping.getJavaXmlTypeMappingArray();
        for (int j = 0; j < javaXmlTypeMappings.length; j++) {
            JavaXmlTypeMappingType javaXmlTypeMapping = javaXmlTypeMappings[j];

            SchemaTypeKey key;
            boolean isElement = javaXmlTypeMapping.getQnameScope().getStringValue().equals("element");
            boolean isSimpleType = javaXmlTypeMapping.getQnameScope().getStringValue().equals("simpleType");
            if (javaXmlTypeMapping.isSetRootTypeQname()) {
                QName typeQName = javaXmlTypeMapping.getRootTypeQname().getQNameValue();
                key = new SchemaTypeKey(typeQName, isElement, isSimpleType, false, null);

                // Skip the wrapper elements.
                if (wrapperElementQNames.contains(typeQName)) {
                    continue;
                }
            } else if (javaXmlTypeMapping.isSetAnonymousTypeQname()) {
                String anonTypeQNameString = javaXmlTypeMapping.getAnonymousTypeQname().getStringValue();
                int pos = anonTypeQNameString.lastIndexOf(":");
                if (pos == -1) {
                    throw new DeploymentException("anon QName is invalid, no final ':' " + anonTypeQNameString);
                }

                //this appears to be ignored...
                QName typeQName = new QName(anonTypeQNameString.substring(0, pos), anonTypeQNameString.substring(pos + 1));
                key = new SchemaTypeKey(typeQName, isElement, isSimpleType, true, null);

                // Skip the wrapper elements.
                if (wrapperElementQNames.contains(new QName(anonTypeQNameString.substring(0, pos), anonTypeQNameString.substring(pos + 2)))) {
                    continue;
                }
            } else {
                throw new DeploymentException("either root type qname or anonymous type qname must be set");
            }

            //default settings
            Class serializerFactoryClass = BeanSerializerFactory.class;
            Class deserializerFactoryClass = BeanDeserializerFactory.class;

            String className = javaXmlTypeMapping.getJavaType().getStringValue().trim();

            Class clazz = null;
            try {
                clazz = ClassLoading.loadClass(className, cl);
            } catch (ClassNotFoundException e2) {
                throw new DeploymentException("Could not load java type", e2);
            }

            if (clazz.isArray()) {
                serializerFactoryClass = ArraySerializerFactory.class;
                deserializerFactoryClass = ArrayDeserializerFactory.class;
            } else if (clazz == List.class) {
                serializerFactoryClass = SimpleListSerializerFactory.class;
                deserializerFactoryClass = SimpleListDeserializerFactory.class;
            }

            TypeInfo.UpdatableTypeInfo internalTypeInfo = new TypeInfo.UpdatableTypeInfo();
            internalTypeInfo.setClazz(clazz);

            internalTypeInfo.setSerializerClass(serializerFactoryClass);
            internalTypeInfo.setDeserializerClass(deserializerFactoryClass);

            populateInternalTypeInfo(clazz, key, javaXmlTypeMapping, internalTypeInfo);

            typeInfoList.add(internalTypeInfo.buildTypeInfo());
        }

        return typeInfoList;
    }

    private void populateInternalTypeInfo(Class javaClass, SchemaTypeKey key, JavaXmlTypeMappingType javaXmlTypeMapping, TypeInfo.UpdatableTypeInfo typeInfo) throws DeploymentException {
        SchemaType schemaType = (SchemaType) schemaTypeKeyToSchemaTypeMap.get(key);
        if (schemaType == null) {
            throw new DeploymentException("Schema type key " + key + " not found in analyzed schema: " + schemaTypeKeyToSchemaTypeMap);
        }
        String ns = key.getqName().getNamespaceURI();
        typeInfo.setCanSearchParents(schemaType.getDerivationType() == SchemaType.DT_RESTRICTION);

        //figure out the name axis expects to look up under.
        QName axisKey = key.getElementQName();
        if (axisKey == null) {
            axisKey = key.getqName();
        }
        typeInfo.setQName(axisKey);

        Map nameToType = new HashMap();
        if (null  == schemaType.getContentModel()) {
            ;
        } else if (SchemaParticle.SEQUENCE == schemaType.getContentModel().getParticleType()
                || SchemaParticle.ALL == schemaType.getContentModel().getParticleType()) {
            SchemaParticle[] properties = schemaType.getContentModel().getParticleChildren();
            for (int i = 0; i < properties.length; i++) {
                SchemaParticle parameter = properties[i];
                nameToType.put(parameter.getName(), parameter);
            }
        } else if (SchemaParticle.ELEMENT == schemaType.getContentModel().getParticleType()) {
            SchemaParticle parameter = schemaType.getContentModel();
            nameToType.put(parameter.getName(), parameter);
        } else {
            throw new DeploymentException("Only element, sequence, and all particle types are supported." +
                    " SchemaType name =" + schemaType.getName());
        }

        VariableMappingType[] variableMappings = javaXmlTypeMapping.getVariableMappingArray();
        FieldDesc[] fields = new FieldDesc[variableMappings.length];
        typeInfo.setFields(fields);

        PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[0];
        try {
            propertyDescriptors = Introspector.getBeanInfo(javaClass).getPropertyDescriptors();
        } catch (IntrospectionException e) {
            throw new DeploymentException("Class " + javaClass + " is not a valid javabean", e);
        }
        Map properties = new HashMap();
        for (int i = 0; i < propertyDescriptors.length; i++) {
            PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
            properties.put(propertyDescriptor.getName(), propertyDescriptor.getPropertyType());
        }
        for (int i = 0; i < variableMappings.length; i++) {
            VariableMappingType variableMapping = variableMappings[i];
            String fieldName = variableMapping.getJavaVariableName().getStringValue().trim();

            if (variableMapping.isSetXmlAttributeName()) {
                AttributeDesc attributeDesc = new AttributeDesc();
                attributeDesc.setFieldName(fieldName);
                Class javaType = (Class) properties.get(fieldName);
                if (javaType == null) {
                    throw new DeploymentException("field name " + fieldName + " not found in " + properties);
                }
                QName xmlName = new QName("", variableMapping.getXmlAttributeName().getStringValue().trim());
                attributeDesc.setXmlName(xmlName);
                // TODO retrieve the type of the attribute.
                QName xmlType = schemaType.getName();
                attributeDesc.setXmlType(xmlType);
                fields[i] = attributeDesc;
            } else {
                ElementDesc elementDesc = new ElementDesc();
                elementDesc.setFieldName(fieldName);
                Class javaType = (Class) properties.get(fieldName);
                if (javaType == null) {
                    //see if it is a public field
                    try {
                        Field field = javaClass.getField(fieldName);
                        javaType = field.getType();
                    } catch (NoSuchFieldException e) {
                        throw new DeploymentException("field name " + fieldName + " not found in " + properties);
                    }
                }
                QName xmlName = new QName("", variableMapping.getXmlElementName().getStringValue().trim());
                SchemaParticle particle = (SchemaParticle) nameToType.get(xmlName);
                if (null == particle) {
                    xmlName = new QName(ns, variableMapping.getXmlElementName().getStringValue().trim());
                    particle = (SchemaParticle) nameToType.get(xmlName);
                    if (null == particle) {
                        throw new DeploymentException("element " + xmlName + " not found in schema " + schemaType.getName());
                    }
                } else if (SchemaParticle.ELEMENT != particle.getParticleType()) {
                    throw new DeploymentException(xmlName + " is not an element in schema " + schemaType.getName());
                }
                elementDesc.setNillable(particle.isNillable() || hasEncoded);
                elementDesc.setXmlName(xmlName);
                elementDesc.setXmlType(particle.getType().getName());

                if (javaType.isArray()) {
                    elementDesc.setMinOccurs(particle.getIntMinOccurs());
                    elementDesc.setMaxOccurs(particle.getIntMaxOccurs());
                }

                fields[i] = elementDesc;
            }
        }
    }
}
