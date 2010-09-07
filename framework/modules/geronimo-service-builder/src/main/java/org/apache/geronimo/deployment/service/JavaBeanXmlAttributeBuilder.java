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
package org.apache.geronimo.deployment.service;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.lang.reflect.Method;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.common.propertyeditor.PropertyEditors;
import org.apache.geronimo.deployment.javabean.xbeans.JavabeanType;
import org.apache.geronimo.deployment.javabean.xbeans.PropertyType;
import org.apache.geronimo.deployment.javabean.xbeans.BeanPropertyType;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.crypto.EncryptionManager;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class JavaBeanXmlAttributeBuilder implements XmlAttributeBuilder {

    private static final String NAMESPACE = "http://geronimo.apache.org/xml/ns/deployment/javabean-1.0";

    public String getNamespace() {
        return NAMESPACE;
    }

    public Object getValue(XmlObject xmlObject, XmlObject enclosing, String type, Bundle bundle) throws DeploymentException {
        JavabeanType javabean = (JavabeanType) xmlObject.copy().changeType(JavabeanType.type);
        return internalGetValue(javabean, type, bundle);
    }

    private Object internalGetValue(JavabeanType javabean, String type, Bundle bundle) throws DeploymentException {
        String className = type;
        if (javabean.isSetClass1()) {
            className = javabean.getClass1();
        }
        Class clazz = null;
        try {
            clazz = bundle.loadClass(className);
            if (!type.equals(className) && !bundle.loadClass(type).isAssignableFrom(clazz)) {
                throw new DeploymentException("javabean class " + className + " is not of the expected type " + type);
            }
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Could not load alleged javabean class " + className, e);
        }
        Object instance = null;
        try {
            instance = clazz.newInstance();
        } catch (Exception e) {
            throw new DeploymentException("Could not create java bean instance", e);
        }
        
        PropertyDescriptor[] propertyDescriptors;
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            propertyDescriptors = beanInfo.getPropertyDescriptors();
        } catch (IntrospectionException e) {
            throw new DeploymentException("Could not analyze java bean class", e);
        }

        PropertyType[] properties = javabean.getPropertyArray();
        for (int i = 0; i < properties.length; i++) {
            PropertyType property = properties[i];
            String propertyName = Introspector.decapitalize(property.getName());
            String propertyString = property.getStringValue().trim();
            for (int j = 0; j < propertyDescriptors.length; j++) {
                PropertyDescriptor propertyDescriptor = propertyDescriptors[j];
                if (propertyName.equals(propertyDescriptor.getName())) {
                    Method writeMethod = propertyDescriptor.getWriteMethod();
                    if (writeMethod.isAnnotationPresent(EncryptOnPersist.class)) {
                        propertyString = (String) EncryptionManager.decrypt(propertyString);
                    }

                    String protertyType = propertyDescriptor.getPropertyType().getName();
                    
                    PropertyEditor propertyEditor = null;
                    try {
                        propertyEditor = PropertyEditors.findEditor(protertyType, bundle);
                    } catch (ClassNotFoundException e) {
                        throw new DeploymentException("Could not load editor for type " + protertyType, e);
                    }
                    if (propertyEditor == null) {
                        throw new DeploymentException("Unable to find PropertyEditor for " + protertyType);
                    }
                    propertyEditor.setAsText(propertyString);
                    Object value = propertyEditor.getValue();
                    
                    try {
                        writeMethod.invoke(instance, new Object[] {value});
                    } catch (Exception e) {
                        throw new DeploymentException("Could not set property value for property named " + propertyName, e);
                    }
                    break;
                }
            }
        }

        BeanPropertyType[] beanProperties = javabean.getBeanPropertyArray();
        for (int i = 0; i < beanProperties.length; i++) {
            BeanPropertyType beanProperty = beanProperties[i];
            String propertyName = Introspector.decapitalize(beanProperty.getName().trim());
            JavabeanType innerBean = beanProperty.getJavabean();
            for (int j = 0; j < propertyDescriptors.length; j++) {
                PropertyDescriptor propertyDescriptor = propertyDescriptors[j];
                if (propertyName.equals(propertyDescriptor.getName())) {
                    String propertyType = propertyDescriptor.getPropertyType().getName();
                    Object value = getValue(innerBean, beanProperty, propertyType, bundle);
                    Method m = propertyDescriptor.getWriteMethod();
                    try {
                        m.invoke(instance, new Object[] {value});
                    } catch (Exception e) {
                        throw new DeploymentException("Could not set property value for property named " + propertyName, e);
                    }
                    break;
                }
            }
        }
        return instance;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(JavaBeanXmlAttributeBuilder.class, "XmlAttributeBuilder");
        infoBuilder.addInterface(XmlAttributeBuilder.class);
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
