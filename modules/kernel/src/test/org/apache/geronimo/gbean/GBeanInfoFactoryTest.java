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

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import junit.framework.TestCase;

/**
 * @version $Revision: 1.2 $ $Date: 2004/02/24 22:36:01 $
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
