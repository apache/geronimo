/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.deployment.service;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.Method;

import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.common.propertyeditor.PropertyEditorException;
import org.apache.geronimo.deployment.javabean.xbeans.BeanPropertyType;
import org.apache.geronimo.deployment.javabean.xbeans.JavabeanDocument;
import org.apache.geronimo.deployment.javabean.xbeans.JavabeanType;
import org.apache.geronimo.deployment.javabean.xbeans.PropertyType;
import org.apache.geronimo.crypto.EncryptionManager;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.osgi.framework.Bundle;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class JavaBeanXmlAttributeEditor extends PropertyEditorSupport {
    private static final QName QNAME = JavabeanDocument.type.getDocumentElementName();
    private static final Class[] PRIMITIVES_CLASSES = new Class[] {String.class,
        Boolean.class,
        Character.class,
        Byte.class,
        Short.class,
        Integer.class,
        Long.class,
        Float.class,
        Double.class
    };

    private final Class javaBeanClazz;
    private final XmlAttributeBuilder xmlAttributeBuilder;
    private final Bundle bundle;

    public JavaBeanXmlAttributeEditor(Class clazz, Bundle bundle) {
        if (null == clazz) {
            throw new IllegalArgumentException("clazz is required");
        }
        this.javaBeanClazz = clazz;
        this.bundle = bundle;
        
        xmlAttributeBuilder = newXmlAttributeBuilder();
    }

    protected JavaBeanXmlAttributeBuilder newXmlAttributeBuilder() {
        return new JavaBeanXmlAttributeBuilder();
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        try {
            JavabeanDocument document = JavabeanDocument.Factory.parse(text);
            JavabeanType javaBeanType = document.getJavabean();

            for (PropertyType propertyType : javaBeanType.getPropertyArray()) {
                if (propertyType.getName().endsWith("Password") || propertyType.getName().endsWith("password")) {
                    String decryptedValue = (String) EncryptionManager.decrypt(propertyType.getStringValue());
                    propertyType.setStringValue(decryptedValue);
                }
            }
            Object javabean = xmlAttributeBuilder.getValue(javaBeanType,
                    document, javaBeanClazz.getName(),
                bundle);
            
            setValue(javabean);
        } catch (XmlException e) {
            throw new PropertyEditorException(e);
        } catch (DeploymentException e) {
            throw new PropertyEditorException(e);
        }
    }

    @Override
    public String getAsText() {
        JavabeanType javabeanType = getJavabeanType(getValue());
        
        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setSaveSyntheticDocumentElement(QNAME);
        xmlOptions.setSavePrettyPrint();
        return javabeanType.xmlText(xmlOptions);
    }

    protected JavabeanType getJavabeanType(Object javaBean) {
        JavabeanType javabeanType = JavabeanType.Factory.newInstance();

        javabeanType.setClass1(javaBean.getClass().getName());
        
        PropertyDescriptor[] propertyDescriptors;
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(javaBean.getClass());
            propertyDescriptors = beanInfo.getPropertyDescriptors();
        } catch (IntrospectionException e) {
            throw new IllegalStateException("See nested", e);
        }

        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            handle(javaBean, propertyDescriptor, javabeanType);
        }
        
        return javabeanType;
    }
    
    protected void handle(Object javaBean, PropertyDescriptor propertyDescriptor, JavabeanType javabeanType) {
        Method readMethod = propertyDescriptor.getReadMethod();
        if (null == readMethod) {
            return;
        } else if (readMethod.isAnnotationPresent(DoNotPersist.class) || readMethod.getName().equals("getClass")) {
            return;
        }
        
        Object value;
        try {
            value = readMethod.invoke(javaBean, (Object[])null);
        } catch (Exception e) {
            throw new IllegalStateException("See nested", e);
        }
        if (null == value) {
            return;
        }
        
        if (isPrimitive(value)) {
            PropertyType propertyType = javabeanType.addNewProperty();
            propertyType.setName(propertyDescriptor.getName());
            
            String valueAsString = value.toString();
            if (readMethod.isAnnotationPresent(EncryptOnPersist.class)) {
                valueAsString = EncryptionManager.encrypt(valueAsString);
            }
            
            propertyType.setStringValue(valueAsString);
        } else {
            JavabeanType nestedJavabeanType = getJavabeanType(value);
            
            BeanPropertyType propertyType = javabeanType.addNewBeanProperty();
            propertyType.setName(propertyDescriptor.getName());
            propertyType.setJavabean(nestedJavabeanType);
        }
    }
    
    protected boolean isPrimitive(Object propertyValue) {
        Class valueClass = propertyValue.getClass();
        for (Class primitiveClass : PRIMITIVES_CLASSES) {
            if (valueClass.equals(primitiveClass)) {
                return true;
            }
        }
        return false;
    }
    
}
