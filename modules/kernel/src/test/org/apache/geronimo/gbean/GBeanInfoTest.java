/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.gbean;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

/**
 * @version $Revision: 1.1 $ $Date: 2004/02/24 18:41:45 $
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
