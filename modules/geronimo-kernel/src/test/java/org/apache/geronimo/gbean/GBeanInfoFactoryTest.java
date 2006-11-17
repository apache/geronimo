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

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class GBeanInfoFactoryTest extends TestCase {
    /*
     * void GBeanInfoBuilder(Class)
     */
    public void testGBeanInfoFactoryClass() {
        assertNotNull(new GBeanInfoBuilder(String.class));
        try {
            new GBeanInfoBuilder(null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }

        final Class className = String.class;
        GBeanInfoBuilder gbeanInfoFactory = new GBeanInfoBuilder(className);
        assertEquals(className.getName(), gbeanInfoFactory.getBeanInfo().getName());
        assertEquals(className.getName(), gbeanInfoFactory.getBeanInfo().getClassName());
    }

    /*
     * test for void GBeanInfoBuilder(Class, String)
     */
    public void testGBeanInfoFactoryClassString() {
        try {
            new GBeanInfoBuilder((Class) null, (String) null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    /*
     * test for void GBeanInfoBuilder(Class, GBeanInfo)
     */
    public void testGBeanInfoFactoryClassGBeanInfo() {
        try {
            new GBeanInfoBuilder((Class) null, (GBeanInfo) null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }

        GBeanInfoBuilder gbeanInfoFactory = new GBeanInfoBuilder(MockGBean.class, MockGBean.getGBeanInfo());
        assertEquals(MockGBean.class.getName(), gbeanInfoFactory.getBeanInfo().getName());
        assertEquals(MockGBean.class.getName(), gbeanInfoFactory.getBeanInfo().getClassName());
        assertEquals(5, gbeanInfoFactory.getBeanInfo().getAttributes().size());
        assertEquals(3, gbeanInfoFactory.getBeanInfo().getOperations().size());
    }

    /*
     * Class to test for void addInterface(Class)
     */
    public void testAddInterfaceClass() {
        GBeanInfoBuilder gbeanInfoFactory;

        gbeanInfoFactory = new GBeanInfoBuilder(MockGBean.class);
        gbeanInfoFactory.addInterface(Serializable.class);
        assertEquals(3, gbeanInfoFactory.getBeanInfo().getAttributes().size());
        assertEquals(3, gbeanInfoFactory.getBeanInfo().getOperations().size());

        gbeanInfoFactory = new GBeanInfoBuilder(MockGBean.class);
        gbeanInfoFactory.addInterface(GBeanLifecycle.class);
        GBeanInfo gbeanInfo = gbeanInfoFactory.getBeanInfo();
        assertEquals(3, gbeanInfoFactory.getBeanInfo().getAttributes().size());
        assertEquals(3, gbeanInfoFactory.getBeanInfo().getOperations().size());

        gbeanInfoFactory = new GBeanInfoBuilder(MockGBean.class);
        gbeanInfoFactory.addInterface(SetterOnlyInterface.class);
        gbeanInfo = gbeanInfoFactory.getBeanInfo();
        assertEquals(3, gbeanInfo.getAttributes().size());
        GAttributeInfo gattrInfo = gbeanInfo.getAttribute("int");
        assertEquals("int", gattrInfo.getName());
        assertEquals("setInt", gattrInfo.getSetterName());
        assertEquals("getInt", gattrInfo.getGetterName());

        Set opsSet = gbeanInfo.getOperations();
        assertEquals(3, opsSet.size());
    }

    private static interface SetterOnlyInterface {
        public void setInt(int i);
    }

    private static interface GetterOnlyInterface {
        public int getInt();
    }

    final static GNotificationInfo notificationInfo = new GNotificationInfo("notification", Collections.singleton(null));

    public static final class MockGBean implements GBeanLifecycle, SetterOnlyInterface, GetterOnlyInterface {

        public static final GBeanInfo GBEAN_INFO;

        static {
            GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(MockGBean.class);
            infoFactory.setConstructor(new String[]{"foo", "bar"});
            infoFactory.addAttribute("foo", String.class, false);
            infoFactory.addAttribute("bar", String.class, false);
            infoFactory.addReference("reference", String.class, null);
            GBEAN_INFO = infoFactory.getBeanInfo();
        }

        public static GBeanInfo getGBeanInfo() {
            return GBEAN_INFO;
        }

        public MockGBean() {
        }
        
        public MockGBean(String foo, String bar) {
        }

        public void setReference(String reference) {
        }

        public void setInt(int i) {
        }

        public int getInt() {
            return 0;
        }

        public void doStart() {
        }

        public void doStop() {
        }

        public void doFail() {
        }
    }

}
