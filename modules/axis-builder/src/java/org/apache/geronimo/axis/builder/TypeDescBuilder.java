/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

import java.beans.PropertyDescriptor;
import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Field;
import javax.xml.namespace.QName;

import org.apache.axis.description.TypeDesc;
import org.apache.axis.description.FieldDesc;
import org.apache.axis.description.AttributeDesc;
import org.apache.axis.description.ElementDesc;
import org.apache.geronimo.xbeans.j2ee.JavaXmlTypeMappingType;
import org.apache.geronimo.xbeans.j2ee.VariableMappingType;
import org.apache.geronimo.common.DeploymentException;
import org.apache.xmlbeans.SchemaType;

public class TypeDescBuilder {
    public static TypeDesc getTypeDescriptor(Class javaClass, QName typeQName, JavaXmlTypeMappingType javaXmlTypeMapping, SchemaType schemaType) throws DeploymentException {
        boolean isRestriction = schemaType.getDerivationType() == SchemaType.DT_RESTRICTION;
        TypeDesc typeDesc = new TypeDesc(javaClass, !isRestriction);
        //TODO typeQName may be a 'anonymous" QName like construct.  Is this what axis expects?
        typeDesc.setXmlType(typeQName);
        VariableMappingType[] variableMappings = javaXmlTypeMapping.getVariableMappingArray();
        FieldDesc[] fields = new FieldDesc[variableMappings.length];

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
                //setting attribute name sets the xmlName with "" namespace, so don't do it
//                attributeDesc.setAttributeName(fieldName);
                attributeDesc.setFieldName(fieldName);
                Class javaType = (Class) properties.get(fieldName);
                if (javaType == null) {
                    throw new DeploymentException("field name " + fieldName + " not found in " + properties);
                }
                attributeDesc.setJavaType(javaType);
                //TODO correct namespace???
                String namespace = "";
                QName xmlName = new QName(namespace, variableMapping.getXmlAttributeName().getStringValue().trim());
                attributeDesc.setXmlName(xmlName);
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
                elementDesc.setJavaType(javaType);
                //TODO correct namespace???
                String namespace = "";
                QName xmlName = new QName(namespace, variableMapping.getXmlElementName().getStringValue().trim());
                elementDesc.setXmlName(xmlName);
                QName xmlType = schemaType.getName();
                elementDesc.setXmlType(xmlType);
                //TODO figure out how to find these:
//                if (javaType.isArray()) {
//                    elementDesc.setArrayType(null);
//                    elementDesc.setMinOccurs(0);
//                    elementDesc.setMaxOccurs(0);
//                }
                //TODO I have no evidence this is what nillable is supposed to mean, but it's more plausible than constant true or false.
                elementDesc.setNillable(!javaType.isPrimitive());
                fields[i] = elementDesc;
            }
        }
        typeDesc.setFields(fields);
        return typeDesc;
    }
}
