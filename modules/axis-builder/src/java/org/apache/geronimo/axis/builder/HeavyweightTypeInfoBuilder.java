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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.axis.description.AttributeDesc;
import org.apache.axis.description.ElementDesc;
import org.apache.axis.description.FieldDesc;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.description.ParameterDesc;
import org.apache.axis.encoding.ser.ArrayDeserializerFactory;
import org.apache.axis.encoding.ser.ArraySerializerFactory;
import org.apache.axis.encoding.ser.Base64DeserializerFactory;
import org.apache.axis.encoding.ser.Base64SerializerFactory;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.apache.axis.encoding.ser.CalendarDeserializerFactory;
import org.apache.axis.encoding.ser.CalendarSerializerFactory;
import org.apache.axis.encoding.ser.DateDeserializerFactory;
import org.apache.axis.encoding.ser.DateSerializerFactory;
import org.apache.axis.encoding.ser.HexDeserializerFactory;
import org.apache.axis.encoding.ser.HexSerializerFactory;
import org.apache.axis.encoding.ser.QNameDeserializerFactory;
import org.apache.axis.encoding.ser.QNameSerializerFactory;
import org.apache.axis.encoding.ser.SimpleDeserializerFactory;
import org.apache.axis.encoding.ser.SimpleListDeserializerFactory;
import org.apache.axis.encoding.ser.SimpleListSerializerFactory;
import org.apache.axis.encoding.ser.SimpleSerializerFactory;
import org.apache.axis.encoding.ser.TimeDeserializerFactory;
import org.apache.axis.encoding.ser.TimeSerializerFactory;
import org.apache.geronimo.axis.client.TypeInfo;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.geronimo.xbeans.j2ee.JavaWsdlMappingType;
import org.apache.geronimo.xbeans.j2ee.JavaXmlTypeMappingType;
import org.apache.geronimo.xbeans.j2ee.VariableMappingType;
import org.apache.xmlbeans.SchemaLocalAttribute;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaType;

/**
 * @version $Rev:  $ $Date:  $
 */
public class HeavyweightTypeInfoBuilder implements TypeInfoBuilder {
    private static final String SOAP_ENCODING_NS = "http://schemas.xmlsoap.org/soap/encoding/";
    private static final String XMLSchema_NS = "http://www.w3.org/2001/XMLSchema";
    
    private final ClassLoader cl;
    private final Map schemaTypeKeyToSchemaTypeMap;
    private final Set wrapperElementQNames;
    private final Collection operations;
    private final boolean hasEncoded;

    public HeavyweightTypeInfoBuilder(ClassLoader cl, Map schemaTypeKeyToSchemaTypeMap, Set wrapperElementQNames, Collection operations, boolean hasEncoded) {
        this.cl = cl;
        this.schemaTypeKeyToSchemaTypeMap = schemaTypeKeyToSchemaTypeMap;
        this.wrapperElementQNames = wrapperElementQNames;
        this.operations = operations;
        this.hasEncoded = hasEncoded;
    }

    public List buildTypeInfo(JavaWsdlMappingType mapping) throws DeploymentException {
        List typeInfoList = new ArrayList();

        Set mappedTypeQNames = new HashSet();
        
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

            SchemaType schemaType = (SchemaType) schemaTypeKeyToSchemaTypeMap.get(key);
            if (schemaType == null) {
                throw new DeploymentException("Schema type key " + key + " not found in analyzed schema: " + schemaTypeKeyToSchemaTypeMap);
            }
            mappedTypeQNames.add(key.getqName());

            String className = javaXmlTypeMapping.getJavaType().getStringValue().trim();
            Class clazz = null;
            try {
                clazz = ClassLoading.loadClass(className, cl);
            } catch (ClassNotFoundException e2) {
                throw new DeploymentException("Could not load java type", e2);
            }

            TypeInfo.UpdatableTypeInfo internalTypeInfo = new TypeInfo.UpdatableTypeInfo();
            defineSerializerPair(internalTypeInfo, schemaType, clazz);

            populateInternalTypeInfo(clazz, key, schemaType, javaXmlTypeMapping, internalTypeInfo);

            typeInfoList.add(internalTypeInfo.buildTypeInfo());
        }
        
        Map qNameToKey = new HashMap();
        for (Iterator iter = schemaTypeKeyToSchemaTypeMap.keySet().iterator(); iter.hasNext();) {
            SchemaTypeKey key = (SchemaTypeKey) iter.next();
            qNameToKey.put(key.getqName(), key);
        }
        
        for (Iterator iter = operations.iterator(); iter.hasNext();) {
            OperationDesc operationDesc = (OperationDesc) iter.next();
            ArrayList parameters = new ArrayList(operationDesc.getParameters());
            if (null != operationDesc.getReturnParamDesc().getTypeQName()) {
                parameters.add(operationDesc.getReturnParamDesc());
            }
            for (Iterator iterator = parameters.iterator(); iterator.hasNext();) {
                ParameterDesc parameterDesc = (ParameterDesc) iterator.next();
                QName typeQName = parameterDesc.getTypeQName();
                if (mappedTypeQNames.contains(typeQName)) {
                    continue;
                } else if (typeQName.getNamespaceURI().equals(XMLSchema_NS)) {
                    continue;
                }
                
                SchemaTypeKey key = (SchemaTypeKey) qNameToKey.get(typeQName);
                if (null == key) {
                    continue;
//                    throw new DeploymentException("Type Qname " + typeQName + " defined by operation " + 
//                            operationDesc + " has not been found in schema: " + schemaTypeKeyToSchemaTypeMap);
                }
                SchemaType schemaType = (SchemaType) schemaTypeKeyToSchemaTypeMap.get(key);
                mappedTypeQNames.add(key.getqName());
	            
                Class clazz = parameterDesc.getJavaType();
                TypeInfo.UpdatableTypeInfo internalTypeInfo = new TypeInfo.UpdatableTypeInfo();
                setTypeQName(internalTypeInfo, key);
                defineSerializerPair(internalTypeInfo, schemaType, clazz);
                internalTypeInfo.setFields(new FieldDesc[0]);

                typeInfoList.add(internalTypeInfo.buildTypeInfo());
            }
        }

        return typeInfoList;
    }

    private void defineSerializerPair(TypeInfo.UpdatableTypeInfo internalTypeInfo, SchemaType schemaType, Class clazz) {
        Class serializerFactoryClass = null;
        Class deserializerFactoryClass = null;
        if (null == schemaType.getContentModel()) {
            QName typeQName;
            if (SchemaType.SIMPLE_CONTENT == schemaType.getContentType()) {
                typeQName = schemaType.getBaseType().getName();
            } else {
                typeQName = schemaType.getName();
            }
            FactoryPair pair = (FactoryPair) qnamesToFactoryPair.get(typeQName);
            if (null != pair) {
                serializerFactoryClass = pair.serializerFactoryClass;
                deserializerFactoryClass = pair.deserializerFactoryClass;
            }
            // TODO: shall we fail there?
        }

        if (null == serializerFactoryClass) {
            serializerFactoryClass = BeanSerializerFactory.class;
            deserializerFactoryClass = BeanDeserializerFactory.class;
        }
        
        if (clazz.isArray()) {
            if (schemaType.isSimpleType() && SchemaType.LIST == schemaType.getSimpleVariety()) {
                serializerFactoryClass = SimpleListSerializerFactory.class;
                deserializerFactoryClass = SimpleListDeserializerFactory.class;
            } else {
                serializerFactoryClass = ArraySerializerFactory.class;
                deserializerFactoryClass = ArrayDeserializerFactory.class;
            }
        } else if (clazz == List.class) {
            serializerFactoryClass = SimpleListSerializerFactory.class;
            deserializerFactoryClass = SimpleListDeserializerFactory.class;
        }
        
        internalTypeInfo.setClazz(clazz);
        internalTypeInfo.setSerializerClass(serializerFactoryClass);
        internalTypeInfo.setDeserializerClass(deserializerFactoryClass);
    }

    private void setTypeQName(TypeInfo.UpdatableTypeInfo typeInfo, SchemaTypeKey key) {
        //figure out the name axis expects to look up under.
        QName axisKey = key.getElementQName();
        if (axisKey == null) {
            axisKey = key.getqName();
        }
        typeInfo.setQName(axisKey);
    }
    
    private void populateInternalTypeInfo(Class javaClass, SchemaTypeKey key, SchemaType schemaType, JavaXmlTypeMappingType javaXmlTypeMapping, TypeInfo.UpdatableTypeInfo typeInfo) throws DeploymentException {
        String ns = key.getqName().getNamespaceURI();
        typeInfo.setCanSearchParents(schemaType.getDerivationType() == SchemaType.DT_RESTRICTION);

        setTypeQName(typeInfo, key);

        Map paramNameToType = new HashMap();
        if (null == schemaType.getContentModel()) {
            ;
        } else if (SchemaParticle.SEQUENCE == schemaType.getContentModel().getParticleType()
                || SchemaParticle.ALL == schemaType.getContentModel().getParticleType()) {
            SchemaParticle[] properties = schemaType.getContentModel().getParticleChildren();
            for (int i = 0; i < properties.length; i++) {
                SchemaParticle parameter = properties[i];
                paramNameToType.put(parameter.getName(), parameter);
            }
        } else if (SchemaParticle.ELEMENT == schemaType.getContentModel().getParticleType()) {
            SchemaParticle parameter = schemaType.getContentModel();
            paramNameToType.put(parameter.getName(), parameter);
        } else {
            throw new DeploymentException("Only element, sequence, and all particle types are supported." +
                    " SchemaType name =" + schemaType.getName());
        }

        Map attNameToType = new HashMap();
        if (null != schemaType.getAttributeModel()) {
            SchemaLocalAttribute[] attributes = schemaType.getAttributeModel().getAttributes();
            for (int i = 0; i < attributes.length; i++) {
                SchemaLocalAttribute attribute = attributes[i];
                Object old = attNameToType.put(attribute.getName().getLocalPart(), attribute);
                if (old != null) {
                    throw new DeploymentException("Complain to your expert group member, spec does not support attributes with the same local name and differing namespaces: original: " + old + ", duplicate local name: " + attribute);
                }
            }
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
                String attributeLocalName = variableMapping.getXmlAttributeName().getStringValue().trim();
                QName xmlName = new QName("", attributeLocalName);
                attributeDesc.setXmlName(xmlName);

                SchemaLocalAttribute attribute = (SchemaLocalAttribute) attNameToType.get(attributeLocalName);
                if (null == attribute) {
                    throw new DeploymentException("attribute " + xmlName + " not found in schema " + schemaType.getName());
                }
                attributeDesc.setXmlType(attribute.getType().getName());

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
                SchemaParticle particle = (SchemaParticle) paramNameToType.get(xmlName);
                if (null == particle) {
                    xmlName = new QName(ns, variableMapping.getXmlElementName().getStringValue().trim());
                    particle = (SchemaParticle) paramNameToType.get(xmlName);
                    if (null == particle) {
                        throw new DeploymentException("element " + xmlName + " not found in schema " + schemaType.getName());
                    }
                } else if (SchemaParticle.ELEMENT != particle.getParticleType()) {
                    throw new DeploymentException(xmlName + " is not an element in schema " + schemaType.getName());
                }
                elementDesc.setNillable(particle.isNillable() || hasEncoded);
                elementDesc.setXmlName(xmlName);
                if (null != particle.getType().getName()) {
                    elementDesc.setXmlType(particle.getType().getName());
                } else {
                    QName anonymousName;
                    if (key.isAnonymous()) {
                        anonymousName = new QName(key.getqName().getNamespaceURI(), key.getqName().getLocalPart() +
                                ">" + particle.getName().getLocalPart());
                    } else {
                        anonymousName = new QName(key.getqName().getNamespaceURI(),
                                ">" + key.getqName().getLocalPart() + ">" + particle.getName().getLocalPart());
                    }
                    elementDesc.setXmlType(anonymousName);
                }

                if (javaType.isArray()) {
                    elementDesc.setMinOccurs(particle.getIntMinOccurs());
                    elementDesc.setMaxOccurs(particle.getIntMaxOccurs());
                    //TODO axis seems to have the wrong name for this property based on how it is used
                    elementDesc.setMaxOccursUnbounded(particle.getIntMaxOccurs() > 1);
                }

                fields[i] = elementDesc;
            }
        }
    }
    
    private static final Map qnamesToFactoryPair = new HashMap();

    static {
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "string"),
                new FactoryPair(SimpleSerializerFactory.class, SimpleDeserializerFactory.class));
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "integer"),
                new FactoryPair(SimpleSerializerFactory.class, SimpleDeserializerFactory.class));
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "int"),
                new FactoryPair(SimpleSerializerFactory.class, SimpleDeserializerFactory.class));
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "long"),
                new FactoryPair(SimpleSerializerFactory.class, SimpleDeserializerFactory.class));
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "short"),
                new FactoryPair(SimpleSerializerFactory.class, SimpleDeserializerFactory.class));
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "decimal"), 
                new FactoryPair(SimpleSerializerFactory.class, SimpleDeserializerFactory.class));
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "float"), 
                new FactoryPair(SimpleSerializerFactory.class, SimpleDeserializerFactory.class));
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "double"), 
                new FactoryPair(SimpleSerializerFactory.class, SimpleDeserializerFactory.class));
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "boolean"), 
                new FactoryPair(SimpleSerializerFactory.class, SimpleDeserializerFactory.class));
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "byte"), 
                new FactoryPair(SimpleSerializerFactory.class, SimpleDeserializerFactory.class));
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "unsignedInt"), 
                new FactoryPair(SimpleSerializerFactory.class, SimpleDeserializerFactory.class));
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "unsignedShort"), 
                new FactoryPair(SimpleSerializerFactory.class, SimpleDeserializerFactory.class));
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "unsignedByte"), 
                new FactoryPair(SimpleSerializerFactory.class, SimpleDeserializerFactory.class));
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "unsignedLong"), 
                new FactoryPair(SimpleSerializerFactory.class, SimpleDeserializerFactory.class));
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "QName"), 
                new FactoryPair(QNameSerializerFactory.class, QNameDeserializerFactory.class));
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "dateTime"), 
                new FactoryPair(CalendarSerializerFactory.class, CalendarDeserializerFactory.class));
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "date"), 
                new FactoryPair(DateSerializerFactory.class, DateDeserializerFactory.class));
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "time"), 
                new FactoryPair(TimeSerializerFactory.class, TimeDeserializerFactory.class));
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "anyURI"), 
                new FactoryPair(SimpleSerializerFactory.class, SimpleDeserializerFactory.class));
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "base64Binary"), 
                new FactoryPair(Base64SerializerFactory.class, Base64DeserializerFactory.class));
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "hexBinary"), 
                new FactoryPair(HexSerializerFactory.class, HexDeserializerFactory.class));
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "anySimpleType"), 
                new FactoryPair(SimpleSerializerFactory.class, SimpleDeserializerFactory.class));
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "nonPositiveInteger"), 
                new FactoryPair(SimpleSerializerFactory.class, SimpleDeserializerFactory.class));
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "nonNegativeInteger"), 
                new FactoryPair(SimpleSerializerFactory.class, SimpleDeserializerFactory.class));
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "positiveInteger"), 
                new FactoryPair(SimpleSerializerFactory.class, SimpleDeserializerFactory.class));
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "negativeInteger"), 
                new FactoryPair(SimpleSerializerFactory.class, SimpleDeserializerFactory.class));
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "ID"), 
                new FactoryPair(SimpleSerializerFactory.class, SimpleDeserializerFactory.class));
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "NCName"), 
                new FactoryPair(SimpleSerializerFactory.class, SimpleDeserializerFactory.class));
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "language"), 
                new FactoryPair(SimpleSerializerFactory.class, SimpleDeserializerFactory.class));
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "normalizedString"), 
                new FactoryPair(SimpleSerializerFactory.class, SimpleDeserializerFactory.class));
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "token"), 
                new FactoryPair(SimpleSerializerFactory.class, SimpleDeserializerFactory.class));
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "Name"), 
                new FactoryPair(SimpleSerializerFactory.class, SimpleDeserializerFactory.class));
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "NMTOKEN"), 
                new FactoryPair(SimpleSerializerFactory.class, SimpleDeserializerFactory.class));
        qnamesToFactoryPair.put(new QName("http://www.w3.org/2001/XMLSchema", "NMTOKENS"), 
                new FactoryPair(SimpleSerializerFactory.class, SimpleDeserializerFactory.class));
    }
    
    private static class FactoryPair {
        private final Class serializerFactoryClass;
        private final Class deserializerFactoryClass;
        
        private FactoryPair(Class serializerFactoryClass, Class deserializerFactoryClass) {
            this.serializerFactoryClass = serializerFactoryClass;
            this.deserializerFactoryClass = deserializerFactoryClass;
        }
    }
}
