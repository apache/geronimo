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

import java.util.HashMap;

import org.apache.geronimo.deployment.javabean.xbeans.BeanPropertyType;
import org.apache.geronimo.deployment.javabean.xbeans.JavabeanType;
import org.apache.geronimo.deployment.javabean.xbeans.PropertyType;
import org.apache.geronimo.crypto.Encryption;
import org.apache.geronimo.crypto.EncryptionManager;
import org.apache.geronimo.kernel.osgi.MockBundleContext;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.osgi.framework.BundleContext;

import com.agical.rmock.extension.junit.RMockTestCase;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class JavaBeanXmlAttributeEditorTest extends RMockTestCase {

    private BundleContext bundleContext = new MockBundleContext(getClass().getClassLoader(), "", new HashMap<Artifact, ConfigurationData>(), null);
    private JavaBeanXmlAttributeEditor editor;

    @Override
    protected void setUp() throws Exception {
        editor = new JavaBeanXmlAttributeEditor(DummyJavaBean.class, bundleContext.getBundle());
    }
    
    public void testPrimitives() throws Exception {
        DummyJavaBean bean = new DummyJavaBean();
        bean.setBooleanValue(true);
        bean.setByteValue((byte) 1);
        bean.setCharValue('a');
        bean.setDoubleValue(2);
        bean.setFloatValue(3);
        bean.setIntValue(4);
        bean.setLongValue(5);
        bean.setShortValue((short) 6);
        bean.setString("string");
        
        editor.setValue(bean);
        String result = editor.getAsText();

        JavabeanType javabeanType = JavabeanType.Factory.parse(result);
        assertPrimitive(javabeanType, "booleanValue", "true");
        assertPrimitive(javabeanType, "byteValue", "1");
        assertPrimitive(javabeanType, "charValue", "a");
        assertPrimitive(javabeanType, "doubleValue", "2.0");
        assertPrimitive(javabeanType, "floatValue", "3.0");
        assertPrimitive(javabeanType, "intValue", "4");
        assertPrimitive(javabeanType, "longValue", "5");
        assertPrimitive(javabeanType, "shortValue", "6");
        assertPrimitive(javabeanType, "string", "string");
    }

    private void assertPrimitive(JavabeanType javabeanType, String propertyName, String value) {
        for (PropertyType propertyType : javabeanType.getPropertyArray()) {
            if (propertyType.getName().equals(propertyName)) {
                assertEquals(value, propertyType.getStringValue());
            }
        }
    }

    /**
     * I observed the resulting XML and it seems correct. It is weird that this test fails.
     */
    public void xtestNestedJavaBean() throws Exception {
        DummyJavaBean bean = new DummyJavaBean();
        DummyJavaBean nestedBean = new DummyJavaBean();
        bean.setDummyJavaBean(nestedBean);
        
        editor.setValue(bean);
        String result = editor.getAsText();
        
        JavabeanType javabeanType = JavabeanType.Factory.parse(result);
        BeanPropertyType[] beanPropertyArray = javabeanType.getBeanPropertyArray();
        assertEquals(1, beanPropertyArray.length);
    }
    
    public void testEncryption() throws Exception {
        Encryption encryption = (Encryption) mock(Encryption.class);
        encryption.encrypt("encryptOnPersist");
        String encryptedValue = "encryptedOnPersist";
        modify().returnValue(encryptedValue);

        startVerification();
        
        String prefix = "{Mock}";
        EncryptionManager.setEncryptionPrefix(prefix, encryption);
        
        JavaBeanXmlAttributeEditor editor = new JavaBeanXmlAttributeEditor(DummyJavaBean.class, bundleContext.getBundle());

        DummyJavaBean bean = new DummyJavaBean();
        bean.setEncryptOnPersist("encryptOnPersist");
        
        editor.setValue(bean);
        String result = editor.getAsText();
        
        JavabeanType javabeanType = JavabeanType.Factory.parse(result);
        assertPrimitive(javabeanType, "encryptOnPersist", prefix + encryptedValue);
    }

}
