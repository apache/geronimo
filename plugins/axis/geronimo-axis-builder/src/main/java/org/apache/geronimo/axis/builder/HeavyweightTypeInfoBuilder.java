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
import javax.xml.rpc.encoding.DeserializerFactory;
import javax.xml.rpc.encoding.SerializerFactory;

import org.apache.axis.description.AttributeDesc;
import org.apache.axis.description.ElementDesc;
import org.apache.axis.description.FieldDesc;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.description.ParameterDesc;
import org.apache.axis.encoding.DefaultJAXRPC11TypeMappingImpl;
import org.apache.axis.encoding.DefaultSOAPEncodingTypeMappingImpl;
import org.apache.axis.encoding.TypeMappingImpl;
import org.apache.axis.encoding.XMLType;
import org.apache.axis.encoding.ser.ArrayDeserializerFactory;
import org.apache.axis.encoding.ser.ArraySerializerFactory;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.apache.axis.encoding.ser.EnumDeserializerFactory;
import org.apache.axis.encoding.ser.EnumSerializerFactory;
import org.apache.axis.encoding.ser.SimpleListDeserializerFactory;
import org.apache.axis.encoding.ser.SimpleListSerializerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.axis.client.ArrayTypeInfo;
import org.apache.geronimo.axis.client.TypeInfo;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.geronimo.webservices.builder.SchemaTypeKey;
import org.apache.openejb.jee.JavaWsdlMapping;
import org.apache.openejb.jee.JavaXmlTypeMapping;
import org.apache.openejb.jee.VariableMapping;
import org.apache.xmlbeans.SchemaLocalAttribute;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.soap.SOAPArrayType;
import org.apache.xmlbeans.soap.SchemaWSDLArrayType;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class HeavyweightTypeInfoBuilder implements TypeInfoBuilder {
    private static final String SOAP_ENCODING_NS = "http://schemas.xmlsoap.org/soap/encoding/";
    private static final String XML_SCHEMA_NS = "http://www.w3.org/2001/XMLSchema";
    
    private static final Logger log = LoggerFactory.getLogger(HeavyweightTypeInfoBuilder.class);

    private final Bundle bundle;
    private final Map schemaTypeKeyToSchemaTypeMap;
    private final Set wrapperElementQNames;
    private final Collection operations;
    private final boolean hasEncoded;

    public HeavyweightTypeInfoBuilder(Bundle bundle, Map schemaTypeKeyToSchemaTypeMap, Set wrapperElementQNames, Collection operations, boolean hasEncoded) {
        this.bundle = bundle;
        this.schemaTypeKeyToSchemaTypeMap = schemaTypeKeyToSchemaTypeMap;
        this.wrapperElementQNames = wrapperElementQNames;
        this.operations = operations;
        this.hasEncoded = hasEncoded;
    }

    public List buildTypeInfo(JavaWsdlMapping mapping) throws DeploymentException {
        List typeInfoList = new ArrayList();

        Set mappedTypeQNames = new HashSet();

        List<JavaXmlTypeMapping> javaXmlTypeMappings = mapping.getJavaXmlTypeMapping();
        for (JavaXmlTypeMapping javaXmlTypeMapping: javaXmlTypeMappings) {
            SchemaTypeKey key;
            boolean isElement = javaXmlTypeMapping.getQNameScope().equals("element");
            boolean isSimpleType = javaXmlTypeMapping.getQNameScope().equals("simpleType");
            QName typeQName = javaXmlTypeMapping.getRootTypeQname();
            if (typeQName != null) {
                key = new SchemaTypeKey(typeQName, isElement, isSimpleType, false, null);

                // Skip the wrapper elements.
                if (wrapperElementQNames.contains(typeQName)) {
                    continue;
                }
            } else if (javaXmlTypeMapping.getAnonymousTypeQname() != null) {
                String anonTypeQNameString = javaXmlTypeMapping.getAnonymousTypeQname();
                int pos = anonTypeQNameString.lastIndexOf(":");
                if (pos == -1) {
                    throw new DeploymentException("anon QName is invalid, no final ':' " + anonTypeQNameString);
                }

                //this appears to be ignored...
                QName qname = new QName(anonTypeQNameString.substring(0, pos), anonTypeQNameString.substring(pos + 1));
                key = new SchemaTypeKey(qname, isElement, isSimpleType, true, null);

                // Skip the wrapper elements.
                if (wrapperElementQNames.contains(new QName(anonTypeQNameString.substring(0, pos), anonTypeQNameString.substring(pos + 2)))) {
                    continue;
                }
            } else {
                throw new DeploymentException("either root type qname or anonymous type qname must be set");
            }

            SchemaType schemaType = (SchemaType) schemaTypeKeyToSchemaTypeMap.get(key);
            if (schemaType == null) {
                // if it is a built-in type, then one assumes a redundant mapping. 
                if (null != TypeMappingLookup.getFactoryPair(key.getqName())) {
                    continue;
                }
//              throw new DeploymentException("Schema type key " + key + " not found in analyzed schema: " + schemaTypeKeyToSchemaTypeMap);
                log.warn("Schema type key " + key + " not found in analyzed schema: " + schemaTypeKeyToSchemaTypeMap);
                continue;
            }
            mappedTypeQNames.add(key.getqName());

            String className = javaXmlTypeMapping.getJavaType();
            Class clazz = null;
            try {
                clazz = ClassLoading.loadClass(className, bundle);
            } catch (ClassNotFoundException e2) {
                throw new DeploymentException("Could not load java type", e2);
            }

            TypeInfo.UpdatableTypeInfo internalTypeInfo = defineSerializerPair(schemaType, clazz);

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
            ParameterDesc returnParameterDesc = operationDesc.getReturnParamDesc();
            if (null != returnParameterDesc.getTypeQName() &&
                    false == returnParameterDesc.getTypeQName().equals(XMLType.AXIS_VOID)) {
                parameters.add(returnParameterDesc);
            }
            for (Iterator iterator = parameters.iterator(); iterator.hasNext();) {
                ParameterDesc parameterDesc = (ParameterDesc) iterator.next();
                QName typeQName = parameterDesc.getTypeQName();
                if (null == typeQName) {
                    continue;
                } else if (mappedTypeQNames.contains(typeQName)) {
                    continue;
                } else if (typeQName.getNamespaceURI().equals(XML_SCHEMA_NS) ||
                        typeQName.getNamespaceURI().equals(SOAP_ENCODING_NS)) {
                    continue;
                }

                SchemaTypeKey key = (SchemaTypeKey) qNameToKey.get(typeQName);
                if (null == key) {
                    log.warn("Type QName [" + typeQName + "] defined by operation [" +
                            operationDesc + "] has not been found in schema: " + schemaTypeKeyToSchemaTypeMap);
                    continue;
                }
                SchemaType schemaType = (SchemaType) schemaTypeKeyToSchemaTypeMap.get(key);
                mappedTypeQNames.add(key.getqName());

                if (false == schemaType.isSimpleType()) {
                    if (false == parameterDesc.getJavaType().isArray()) {
                        if (false == mappedTypeQNames.contains(schemaType.getName())) {
                            // TODO: this lookup is not enough: the jaxrpc mapping file may define an anonymous
                            // mapping.
                            log.warn("Operation [" + operationDesc + "] uses XML type [" + schemaType +
                                    "], whose mapping is not declared by the jaxrpc mapping file.\n Continuing deployment; " +
                                    "yet, the deployment is not-portable.");
                        }
                        continue;
                    }
                }

                Class clazz = parameterDesc.getJavaType();
                TypeInfo.UpdatableTypeInfo internalTypeInfo =  defineSerializerPair(schemaType, clazz);
                setTypeQName(internalTypeInfo, key);
                internalTypeInfo.setFields(new FieldDesc[0]);

                typeInfoList.add(internalTypeInfo.buildTypeInfo());
            }
        }

        return typeInfoList;
    }

    private TypeInfo.UpdatableTypeInfo defineSerializerPair(SchemaType schemaType, Class clazz)
            throws DeploymentException {
        TypeInfo.UpdatableTypeInfo internalTypeInfo = new TypeInfo.UpdatableTypeInfo();
        Class serializerFactoryClass = null;
        Class deserializerFactoryClass = null;
        if (schemaType.isSimpleType()) {
            if (SchemaType.ATOMIC == schemaType.getSimpleVariety()) {
                if (clazz.isArray()) {
                    internalTypeInfo = new ArrayTypeInfo.UpdatableArrayTypeInfo();
                    serializerFactoryClass = ArraySerializerFactory.class;
                    deserializerFactoryClass = ArrayDeserializerFactory.class;
                    //TODO set componentType, componentQName
                } else if (null != schemaType.getEnumerationValues()) {
                    serializerFactoryClass = EnumSerializerFactory.class;
                    deserializerFactoryClass = EnumDeserializerFactory.class;
                } else {
                    QName typeQName = schemaType.getPrimitiveType().getName();
                    FactoryPair pair = (FactoryPair) TypeMappingLookup.getFactoryPair(typeQName);
                    if (null == pair) {
                        throw new DeploymentException("Primitive type [" + typeQName + "] is not registered.");
                    }
                    serializerFactoryClass = pair.serializerFactoryClass;
                    deserializerFactoryClass = pair.deserializerFactoryClass;
                }
            } else if (SchemaType.LIST == schemaType.getSimpleVariety()) {
                serializerFactoryClass = SimpleListSerializerFactory.class;
                deserializerFactoryClass = SimpleListDeserializerFactory.class;
            } else {
                throw new DeploymentException("Schema type [" + schemaType + "] is invalid.");
            }
        } else {
            if (clazz.isArray()) {
                internalTypeInfo = new ArrayTypeInfo.UpdatableArrayTypeInfo();
                serializerFactoryClass = ArraySerializerFactory.class;
                deserializerFactoryClass = ArrayDeserializerFactory.class;
                QName componentType = null;
                //First, handle case that looks like this:
//                <complexType name="ArrayOfstring">
//                    <complexContent>
//                        <restriction base="soapenc:Array">
//                            <attribute ref="soapenc:arrayType" wsdl:arrayType="xsd:string[]"/>
//                        </restriction>
//                    </complexContent>
//                </complexType>
                SchemaLocalAttribute arrayTypeAttribute =  schemaType.getAttributeModel().getAttribute(new QName(SOAP_ENCODING_NS, "arrayType"));
                if (arrayTypeAttribute != null) {
                    SchemaWSDLArrayType wsdlArrayType = (SchemaWSDLArrayType) arrayTypeAttribute;
                    SOAPArrayType soapArrayType = wsdlArrayType.getWSDLArrayType();
                    if (soapArrayType != null) {
                        componentType = soapArrayType.getQName();
                        log.debug("extracted componentType " + componentType + " from schemaType " + schemaType);
                    } else {
                        log.info("no SOAPArrayType for component from schemaType " + schemaType);
                    }
                } else {
                    log.warn("No soap array info for schematype: " + schemaType);
                }
                if (componentType == null) {
                    //If that didn't work, try to handle case like this:
//                    <complexType name="ArrayOfstring1">
//                        <complexContent>
//                            <restriction base="soapenc:Array">
//                                <sequence>
//                                    <element name="string1" type="xsd:string" minOccurs="0" maxOccurs="unbounded"/>
//                                </sequence>
//                            </restriction>
//                        </complexContent>
//                    </complexType>
                    //todo consider if we should check for maxOccurs > 1
                    if (schemaType.getBaseType().getName().equals(new QName(SOAP_ENCODING_NS, "Array"))) {
                        SchemaProperty[] properties = schemaType.getDerivedProperties();
                        if (properties.length != 1) {
                            throw new DeploymentException("more than one element inside array definition: " + schemaType);
                        }
                        componentType = properties[0].getType().getName();
                        log.debug("determined component type from element type");
                    }

                }

                ((ArrayTypeInfo.UpdatableArrayTypeInfo)internalTypeInfo).setComponentType(componentType);
                //If we understand the axis comments correctly, componentQName is never set for j2ee ws.
            } else {
                QName typeQName;
                if (SchemaType.SIMPLE_CONTENT == schemaType.getContentType()) {
                    typeQName = schemaType.getBaseType().getName();
                } else if (SchemaType.EMPTY_CONTENT == schemaType.getContentType() ||
                        SchemaType.ELEMENT_CONTENT == schemaType.getContentType() ||
                        SchemaType.MIXED_CONTENT == schemaType.getContentType()) {
                    typeQName = schemaType.getName();
                } else {
                    throw new DeploymentException("Schema type [" + schemaType + "] is invalid.");
                }
                FactoryPair pair = (FactoryPair) TypeMappingLookup.getFactoryPair(typeQName);
                if (null != pair) {
                    serializerFactoryClass = pair.serializerFactoryClass;
                    deserializerFactoryClass = pair.deserializerFactoryClass;
                } else {
                    serializerFactoryClass = BeanSerializerFactory.class;
                    deserializerFactoryClass = BeanDeserializerFactory.class;
                }
            }
        }

        internalTypeInfo.setClazz(clazz);
        internalTypeInfo.setSerializerClass(serializerFactoryClass);
        internalTypeInfo.setDeserializerClass(deserializerFactoryClass);
        return internalTypeInfo;
    }

    private void setTypeQName(TypeInfo.UpdatableTypeInfo typeInfo, SchemaTypeKey key) {
        //figure out the name axis expects to look up under.
        QName axisKey = key.getElementQName();
        if (axisKey == null) {
            axisKey = key.getqName();
        }
        typeInfo.setQName(axisKey);
    }

    private void populateInternalTypeInfo(Class javaClass, SchemaTypeKey key, SchemaType schemaType, JavaXmlTypeMapping javaXmlTypeMapping, TypeInfo.UpdatableTypeInfo typeInfo) throws DeploymentException {
        String ns = key.getqName().getNamespaceURI();
        typeInfo.setCanSearchParents(schemaType.getDerivationType() == SchemaType.DT_RESTRICTION);

        setTypeQName(typeInfo, key);

        Map paramNameToType = new HashMap();
        if (null == schemaType.getContentModel()) {

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
        
        List<VariableMapping> variableMappings = javaXmlTypeMapping.getVariableMapping();

        // short-circuit the processing of arrays as they should not define variable-mapping elements. 
        if (javaClass.isArray()) {
            if (!variableMappings.isEmpty()) {
                // for portability reason we simply warn and not fail.
                log.warn("Ignoring variable-mapping defined for class " + javaClass + " which is an array.");
            }
            typeInfo.setFields(new FieldDesc[0]);
            return;
        }

        FieldDesc[] fields = new FieldDesc[variableMappings.size()];
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
        int i = 0; 
        for (VariableMapping variableMapping: variableMappings) {
            String fieldName = variableMapping.getJavaVariableName();

            if (variableMapping.getXmlAttributeName() != null) {
                AttributeDesc attributeDesc = new AttributeDesc();
                attributeDesc.setFieldName(fieldName);
                Class javaType = (Class) properties.get(fieldName);
                if (javaType == null) {
                    throw new DeploymentException("field name " + fieldName + " not found in " + properties);
                }
                String attributeLocalName = variableMapping.getXmlAttributeName();
                QName xmlName = new QName("", attributeLocalName);
                attributeDesc.setXmlName(xmlName);

                SchemaLocalAttribute attribute = (SchemaLocalAttribute) attNameToType.get(attributeLocalName);
                if (null == attribute) {
                    throw new DeploymentException("attribute " + xmlName + " not found in schema " + schemaType.getName());
                }
                attributeDesc.setXmlType(attribute.getType().getName());

                fields[i++] = attributeDesc;
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
                        throw new DeploymentException("field name " + fieldName + " not found in " + properties, e);
                    }
                }
                QName xmlName = new QName("", variableMapping.getXmlElementName());
                SchemaParticle particle = (SchemaParticle) paramNameToType.get(xmlName);
                if (null == particle) {
                    xmlName = new QName(ns, variableMapping.getXmlElementName());
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

                fields[i++] = elementDesc;
            }
        }
    }

    private static class TypeMappingLookup {
        private static final TypeMappingImpl SOAP_TM = DefaultSOAPEncodingTypeMappingImpl.getSingleton();
        private static final TypeMappingImpl JAXRPC_TM = DefaultJAXRPC11TypeMappingImpl.getSingleton();

        public static FactoryPair getFactoryPair(QName xmlType) {
            Class clazz = SOAP_TM.getClassForQName(xmlType, null, null);
            SerializerFactory sf;
            DeserializerFactory df;
            if (null != clazz) {
                sf = SOAP_TM.getSerializer(clazz, xmlType);
                df = SOAP_TM.getDeserializer(clazz, xmlType, null);
            } else {
                clazz = JAXRPC_TM.getClassForQName(xmlType, null, null);
                if (null == clazz) {
                    return null;
                }
                sf = JAXRPC_TM.getSerializer(clazz, xmlType);
                df = JAXRPC_TM.getDeserializer(clazz, xmlType, null);
            }
            return new FactoryPair(sf.getClass(), df.getClass());
        }
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
