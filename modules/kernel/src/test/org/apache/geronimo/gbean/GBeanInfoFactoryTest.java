/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.gbean;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import junit.framework.TestCase;

/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:57:50 $
 */
public class GBeanInfoFactoryTest extends TestCase {

    /*
     * void GBeanInfoFactory(String)
     */
    public void testGBeanInfoFactoryString() {
        assertNotNull(new GBeanInfoFactory(""));
        try {
            new GBeanInfoFactory((String) null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {}

        final String gbeanName = "gbeanName";
        GBeanInfoFactory gbeanInfoFactory = new GBeanInfoFactory(gbeanName);
        assertEquals(gbeanName, gbeanInfoFactory.getBeanInfo().getName());
        assertEquals(gbeanName, gbeanInfoFactory.getBeanInfo().getClassName());
    }

    /*
     * void GBeanInfoFactory(Class)
     */
    public void testGBeanInfoFactoryClass() {
        assertNotNull(new GBeanInfoFactory(String.class));
        try {
            new GBeanInfoFactory((Class) null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {}

        final Class className = String.class;
        GBeanInfoFactory gbeanInfoFactory = new GBeanInfoFactory(className);
        assertEquals(className.getName(), gbeanInfoFactory.getBeanInfo().getName());
        assertEquals(className.getName(), gbeanInfoFactory.getBeanInfo().getClassName());
    }

    /*
     * test for void GBeanInfoFactory(Class, String)
     */
    public void testGBeanInfoFactoryClassString() {
        try {
            new GBeanInfoFactory((Class) null, null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {}
    }

    /*
     * test for void GBeanInfoFactory(Class, GBeanInfo)
     */
    public void testGBeanInfoFactoryClassGBeanInfo() {
        try {
            new GBeanInfoFactory((Class) null, (GBeanInfo) null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {}

        final Class className = String.class;
        GBeanInfoFactory gbeanInfoFactory = new GBeanInfoFactory(className, MockGBean.getGBeanInfo());
        assertEquals(className.getName(), gbeanInfoFactory.getBeanInfo().getName());
        assertEquals(className.getName(), gbeanInfoFactory.getBeanInfo().getClassName());
        assertTrue(gbeanInfoFactory.getBeanInfo().getAttributes().isEmpty());
        assertTrue(gbeanInfoFactory.getBeanInfo().getOperations().isEmpty());
    }

    /*
     * void GBeanInfoFactory(String, GBeanInfo)
     */
    public void testGBeanInfoFactoryStringGBeanInfo() {
        try {
            new GBeanInfoFactory((String) null, (GBeanInfo) null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {}
        GBeanInfoFactory gbeanInfoFactory = new GBeanInfoFactory(String.class.getName(),
                GBeanInfo.getGBeanInfo(GBeanInfoTest.MockGBean.class.getName(),
                                       GBeanInfoTest.MockGBean.class.getClassLoader()));
        assertNotNull(gbeanInfoFactory);
    }

    /*
     * Class to test for void addInterface(Class)
     */
    public void testAddInterfaceClass() {
        GBeanInfoFactory gbeanInfoFactory;

        gbeanInfoFactory = new GBeanInfoFactory("");
        gbeanInfoFactory.addInterface(Serializable.class);
        assertTrue(gbeanInfoFactory.getBeanInfo().getAttributes().size() == 0);
        assertTrue(gbeanInfoFactory.getBeanInfo().getOperations().size() == 0);

        gbeanInfoFactory = new GBeanInfoFactory("");
        gbeanInfoFactory.addInterface(GBean.class);
        GBeanInfo gbeanInfo = gbeanInfoFactory.getBeanInfo();
        assertTrue(gbeanInfo.getAttributes().size() == 1);
        GAttributeInfo gattrInfo = (GAttributeInfo) gbeanInfo.getAttributes().iterator().next();
        assertEquals("GBeanContext", gattrInfo.getName());
        assertEquals("setGBeanContext", gattrInfo.getSetterName());
        assertNull(gattrInfo.getGetterName());
        assertTrue(gbeanInfo.getOperations().size() == 3);

        gbeanInfoFactory = new GBeanInfoFactory("");
        gbeanInfoFactory.addInterface(SetterOnlyInterface.class);
        gbeanInfo = gbeanInfoFactory.getBeanInfo();
        assertEquals(1, gbeanInfo.getAttributes().size());
        gattrInfo = (GAttributeInfo) gbeanInfo.getAttributes().iterator().next();
        assertEquals("Int", gattrInfo.getName());
        assertEquals("setInt", gattrInfo.getSetterName());
        assertNull(gattrInfo.getGetterName());

        Set opsSet = gbeanInfo.getOperations();
        assertEquals(0, opsSet.size());

        gbeanInfoFactory.addInterface(GetterOnlyInterface.class);
        gbeanInfo = gbeanInfoFactory.getBeanInfo();
        opsSet = gbeanInfo.getOperations();
        assertEquals(0, opsSet.size());
        assertEquals(1, gbeanInfo.getAttributes().size());
        gattrInfo = (GAttributeInfo) gbeanInfo.getAttributes().iterator().next();
        assertEquals("Int", gattrInfo.getName());
        assertEquals("getInt", gattrInfo.getGetterName());
        assertEquals("setInt", gattrInfo.getSetterName());
    }

    private static interface SetterOnlyInterface {

        public void setInt(int i);
    }

    private static interface GetterOnlyInterface {

        public int getInt();
    }

    final static GNotificationInfo notificationInfo = new GNotificationInfo("notification", Collections.singleton(null));

    final static GReferenceInfo refInfo = new GReferenceInfo("reference", String.class);

    public static final class MockGBean {

        public static final GBeanInfo GBEAN_INFO;

        static {
            GBeanInfoFactory infoFactory = new GBeanInfoFactory(MockGBean.class);
            infoFactory.setConstructor(new GConstructorInfo(new String[] { String.class.getName(),
                    Integer.class.getName()}, new Class[] { String.class, Integer.class}));
            infoFactory.addNotification(notificationInfo);
            infoFactory.addReference(refInfo);
            GBEAN_INFO = infoFactory.getBeanInfo();
        }

        public static GBeanInfo getGBeanInfo() {
            return GBEAN_INFO;
        }
    }

}
