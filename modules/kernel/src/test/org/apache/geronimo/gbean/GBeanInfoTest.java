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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

/**
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:50 $
 */
public class GBeanInfoTest extends TestCase {

    public void testGetGBeanInfo() {
        // 1. Test GBean that exists
        GBeanInfo gbeanInfo = GBeanInfo.getGBeanInfo(MockGBean.class.getName(),
                MockGBean.class.getClassLoader());
        assertNotNull(gbeanInfo);

        // 2. Test GBean that doesn't exist
        try {
            GBeanInfo.getGBeanInfo("ClassThatDoesNotExist", this.getClass()
                    .getClassLoader());
            fail("InvalidConfigurationException expected");
        } catch (InvalidConfigurationException expected) {
        }

        // 3. Test GBean that exist, but doesn't declare a getGBeanInfo()
        // method
        try {
            GBeanInfo.getGBeanInfo(String.class.getName(), this.getClass()
                    .getClassLoader());
            fail("InvalidConfigurationException expected");
        } catch (InvalidConfigurationException expected) {
        }
    }

    /*
     * void GBeanInfo(String, Set, GConstructorInfo, Set, Set, Set)
     */
    public void testGBeanInfoStringSetGConstructorInfoSetSetSet() {
        GBeanInfo gbeanInfo = new GBeanInfo(null, null, null, null, null, null);
        assertNotNull(gbeanInfo);
    }

    /*
     * void GBeanInfo(String, String, Set, GConstructorInfo, Set, Set, Set)
     */
    public void testGBeanInfoStringStringSetGConstructorInfoSetSetSet() {
        GBeanInfo gbeanInfo = new GBeanInfo(null, null, null, null, null, null,
                null);
        assertNotNull(gbeanInfo);
    }

    public void testGetName() {
        assertEquals(MockGBean.class.getName(), gbeanInfo.getName());
    }

    public void testGetClassName() {
        assertEquals(MockGBean.class.getName(), gbeanInfo.getClassName());
    }

    public void testGetAttributeSet() {
        Set attrSet = gbeanInfo.getAttributes();
        assertEquals(2, attrSet.size());
        assertTrue(attrSet.contains(persistentAttrInfo));
        assertTrue(attrSet.contains(nonPersistentAttrInfo));
    }

    public void testGetPersistentAttributes() {
        List attrList = gbeanInfo.getPersistentAttributes();
        assertEquals(1, attrList.size());
        assertEquals(persistentAttrInfo, attrList.get(0));
    }

    public void testGetConstructor() {
        GConstructorInfo gctorInfo = gbeanInfo.getConstructor();
        List attrNameList = gctorInfo.getAttributeNames();
        assertEquals(2, attrNameList.size());
        assertTrue(attrNameList.contains(String.class.getName()));
        assertTrue(attrNameList.contains(Integer.class.getName()));
        Map attrTypeMap = gctorInfo.getAttributeTypeMap();
        assertTrue(attrTypeMap.containsValue(String.class));
        assertTrue(attrTypeMap.containsValue(Integer.class));
        assertEquals(String.class, attrTypeMap.get(String.class.getName()));
        assertEquals(Integer.class, attrTypeMap.get(Integer.class.getName()));
    }

    public void testGetOperationsSet() {
        Set gbeanOpSet = gbeanInfo.getOperations();
        assertEquals(1, gbeanOpSet.size());
        assertTrue(gbeanOpSet.contains(opInfo));
    }

    public void testGetNotificationsSet() {
        Set gbeanNotificationSet = gbeanInfo.getNotifications();
        assertEquals(1, gbeanNotificationSet.size());
        assertTrue(gbeanNotificationSet.contains(notificationInfo));
    }

    public void testGetReferencesSet() {
        Set gbeanRefSet = gbeanInfo.getReferences();
        assertEquals(1, gbeanRefSet.size());
        assertTrue(gbeanRefSet.contains(refInfo));
    }

    public void testToString() {
        assertNotNull(gbeanInfo.toString());
        assertEquals(gbeanInfo.toString(), MockGBean.getGBeanInfo().toString());
    }

    GBeanInfo gbeanInfo;

    final static String nonPersistentAttrName = "nonPersistentAttribute";

    final static GAttributeInfo nonPersistentAttrInfo = new GAttributeInfo(
            nonPersistentAttrName, false);

    final static String persistentAttrName = "persistentAttribute";

    final static GAttributeInfo persistentAttrInfo = new GAttributeInfo(
            persistentAttrName, true);

    final static GOperationInfo opInfo = new GOperationInfo("operation");

    final static GNotificationInfo notificationInfo = new GNotificationInfo(
            "notification", Collections.singleton(null));

    final static GReferenceInfo refInfo = new GReferenceInfo("reference",
            String.class);

    public void setUp() {
        gbeanInfo = MockGBean.getGBeanInfo();
    }

    protected void tearDown() throws Exception {
        gbeanInfo = null;
    }

    public static final class MockGBean {

        public static final GBeanInfo GBEAN_INFO;

        static {
            GBeanInfoFactory infoFactory = new GBeanInfoFactory(MockGBean.class);
            infoFactory.setConstructor(new GConstructorInfo(new String[] {
                    String.class.getName(), Integer.class.getName()},
                    new Class[] { String.class, Integer.class}));
            infoFactory.addAttribute(nonPersistentAttrInfo);
            infoFactory.addAttribute(persistentAttrInfo);
            infoFactory.addOperation(opInfo);
            infoFactory.addNotification(notificationInfo);
            infoFactory.addReference(refInfo);
            GBEAN_INFO = infoFactory.getBeanInfo();
        }

        public static GBeanInfo getGBeanInfo() {
            return GBEAN_INFO;
        }
    }
}
