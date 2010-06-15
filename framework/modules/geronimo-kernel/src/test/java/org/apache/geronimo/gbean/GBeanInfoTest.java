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

package org.apache.geronimo.gbean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Set;

import org.apache.geronimo.gbean.annotation.AnnotationGBeanInfoBuilder;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.OsgiService;
import org.apache.geronimo.testsupport.TestSupport;
import org.apache.geronimo.kernel.osgi.MockBundleContext;

/**
 * @version $Rev$ $Date$
 */
public class GBeanInfoTest extends TestSupport {
    private static final String CONSTRUCTOR_ARG_0 = "ConstructorArg_0";
    private static final String CONSTRUCTOR_ARG_1 = "ConstructorArg_1";

    public void testGetGBeanInfo() {
        // 1. Test GBean that exists
        GBeanInfo gbeanInfo = GBeanInfo.getGBeanInfo(MockGBean.class.getName(), getBundleContext().getBundle());
        assertNotNull(gbeanInfo);

        // 2. Test GBean that doesn't exist
        try {
            GBeanInfo.getGBeanInfo("ClassThatDoesNotExist", getBundleContext().getBundle());
            fail("InvalidConfigurationException expected");
        } catch (InvalidConfigurationException expected) {
        }
    }

    public void testGetName() {
        assertEquals(MockGBean.class.getName(), gbeanInfo.getName());
    }

    public void testGetClassName() {
        assertEquals(MockGBean.class.getName(), gbeanInfo.getClassName());
    }

    public void testGetAttributeSet() {
        Set attrSet = gbeanInfo.getAttributes();
        assertEquals(6, attrSet.size());
        assertTrue(attrSet.contains(persistentAttrInfo));
        assertTrue(attrSet.contains(nonPersistentAttrInfo));
    }

    public void testGetPersistentAttributes() {
        List attrList = gbeanInfo.getPersistentAttributes();
        assertEquals(3, attrList.size());
    }

    public void testGetConstructor() {
        GConstructorInfo gctorInfo = gbeanInfo.getConstructor();
        List attrNameList = gctorInfo.getAttributeNames();
        assertEquals(2, attrNameList.size());
        assertEquals(CONSTRUCTOR_ARG_0, attrNameList.get(0));
        assertEquals(CONSTRUCTOR_ARG_1, attrNameList.get(1));
    }

    public void testGetOperationsSet() {
        Set gbeanOpSet = gbeanInfo.getOperations();
        assertEquals(3, gbeanOpSet.size());
        assertTrue(gbeanOpSet.contains(opInfo));
    }

    public void testGetReferencesSet() {
        Set gbeanRefSet = gbeanInfo.getReferences();
        assertEquals(1, gbeanRefSet.size());
        GReferenceInfo newRefInfo = (GReferenceInfo) gbeanRefSet.iterator().next();
        assertEquals(refInfo.getName(), newRefInfo.getName());
    }

    public void testToString() {
        assertNotNull(gbeanInfo.toString());
        assertEquals(gbeanInfo.toString(), MockGBean.getGBeanInfo().toString());
    }

//    public void testBackwardCompatibility() throws Exception {
//        FileInputStream fis = new FileInputStream(resolveFile("src/test/data/gbeaninfo/SERIALIZATION_-6198804067155550221.ser"));
//        ObjectInputStream is = new ObjectInputStream(fis);
//        GBeanInfo beanInfo = (GBeanInfo) is.readObject();
//        assertEquals(GBeanInfo.PRIORITY_NORMAL, beanInfo.getPriority());
//    }

    public void testCurrentSerialization() throws Exception {
        GBeanInfo beanInfo = MockGBean.GBEAN_INFO;

        ByteArrayOutputStream memOut = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(memOut);
        os.writeObject(beanInfo);

        ByteArrayInputStream memIn = new ByteArrayInputStream(memOut.toByteArray());
        ObjectInputStream is = new ObjectInputStream(memIn);
        beanInfo = (GBeanInfo) is.readObject();
        assertEquals(GBeanInfo.PRIORITY_CLASSLOADER, beanInfo.getPriority());
    }
    
    public void testCurrentSerializationAnnotation() throws Exception {
        GBeanInfo beanInfo = new AnnotationGBeanInfoBuilder(AnnotationGBean.class).buildGBeanInfo();

        ByteArrayOutputStream memOut = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(memOut);
        os.writeObject(beanInfo);

        ByteArrayInputStream memIn = new ByteArrayInputStream(memOut.toByteArray());
        ObjectInputStream is = new ObjectInputStream(memIn);
        beanInfo = (GBeanInfo) is.readObject();
        assertEquals(GBeanInfo.PRIORITY_NORMAL, beanInfo.getPriority());
        assertEquals(true, beanInfo.isOsgiService());
    }

    GBeanInfo gbeanInfo;

    final static String nonPersistentAttrName = "nonPersistentAttribute";

    final static GAttributeInfo nonPersistentAttrInfo = new GAttributeInfo(nonPersistentAttrName, String.class.getName(), false, false, "getFoo", "setFoo");

    final static String persistentAttrName = "persistentAttribute";

    final static GAttributeInfo persistentAttrInfo = new GAttributeInfo(persistentAttrName, String.class.getName(), true, false, "getFoo", "setFoo");

    final static GOperationInfo opInfo = new GOperationInfo("operation", "java.lang.Object");

    final static GReferenceInfo refInfo = new GReferenceInfo("reference", String.class.getName(), String.class.getName(), "setReference", "Fooifier");

    public void setUp() throws Exception {
        super.setUp();
        bundleContext = new MockBundleContext(getClass().getClassLoader(), BASEDIR.getAbsolutePath(), null, null);
        gbeanInfo = MockGBean.getGBeanInfo();
    }

    protected void tearDown() throws Exception {
        gbeanInfo = null;
        super.tearDown();
    }

    public static final class MockGBean {
        public static final GBeanInfo GBEAN_INFO;

        static {
            GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(MockGBean.class);

            infoFactory.addAttribute(nonPersistentAttrInfo);
            infoFactory.addAttribute(persistentAttrInfo);

            infoFactory.addOperation(opInfo);
            infoFactory.addReference(refInfo);

            infoFactory.addAttribute(CONSTRUCTOR_ARG_0, String.class, true);
            infoFactory.addAttribute(CONSTRUCTOR_ARG_1, String.class, true);
            infoFactory.setPriority(GBeanInfo.PRIORITY_CLASSLOADER);
            infoFactory.setConstructor(new String[]{CONSTRUCTOR_ARG_0, CONSTRUCTOR_ARG_1});


            GBEAN_INFO = infoFactory.getBeanInfo();
        }

        public static GBeanInfo getGBeanInfo() {
            return GBEAN_INFO;
        }

        public MockGBean(String ConstructorArg_0, String ConstructorArg_1) {
        }

        public void setReference(String reference) {
        }
        
        public void addSomething(String something){            
        }
        
        public String removeSomething(String something){
           return null; 
        }

    }
    @GBean(j2eeType = "Foo")
    @OsgiService
    public static class AnnotationGBean {

    }
}
