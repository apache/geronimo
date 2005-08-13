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

package org.apache.geronimo.kernel;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import javax.management.ObjectName;

import junit.framework.TestCase;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.proxy.ProxyManager;
import org.apache.geronimo.kernel.basic.BasicProxyManager;

/**
 * @version $Rev$ $Date$
 */
public class GBeanTest extends TestCase {
    private ObjectName name;
    private ObjectName name2;
    private Kernel kernel;

    public void testLoad() throws Exception {
        ClassLoader cl = getClass().getClassLoader();
        ClassLoader myCl = new URLClassLoader(new URL[0], cl);
        GBeanData gbean = new GBeanData(name, MockGBean.getGBeanInfo());
        gbean.setAttribute("name", "Test");
        gbean.setAttribute("finalInt", new Integer(123));
        kernel.loadGBean(gbean, myCl);
        kernel.startGBean(name);
        assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(name));
        assertEquals("Hello", kernel.invoke(name, "doSomething", new Object[]{"Hello"}, new String[]{String.class.getName()}));

        assertEquals(name.getCanonicalName(), kernel.getAttribute(name, "objectName"));
        assertEquals(name.getCanonicalName(), kernel.getAttribute(name, "actualObjectName"));

        assertSame(myCl, kernel.getAttribute(name, "actualClassLoader"));

        // the MockGBean implemmentation of getConfigurationClassLoader will throw an exception, but since the GBean architecture
        // handles this directly the implementation method will never be called
        kernel.getAttribute(name, "classLoader");

        assertSame(kernel, kernel.getAttribute(name, "kernel"));
        assertSame(kernel, kernel.getAttribute(name, "actualKernel"));

        kernel.stopGBean(name);
        kernel.unloadGBean(name);
    }

    public void testEndpoint() throws Exception {
        ClassLoader cl = MockGBean.class.getClassLoader();
        GBeanData gbean1 = new GBeanData(name, MockGBean.getGBeanInfo());
        gbean1.setAttribute("finalInt", new Integer(123));
        kernel.loadGBean(gbean1, cl);
        kernel.startGBean(name);

        GBeanData gbean2 = new GBeanData(name2, MockGBean.getGBeanInfo());
        gbean2.setAttribute("finalInt", new Integer(123));
        gbean2.setReferencePatterns("MockEndpoint", Collections.singleton(name));
        kernel.loadGBean(gbean2, cl);
        kernel.startGBean(name2);

        assertEquals("endpointCheck", kernel.invoke(name2, "checkEndpoint", null, null));
    }

    public void testProxiesInterfaces() throws Exception {
        ClassLoader cl = getClass().getClassLoader();
        ClassLoader myCl = new URLClassLoader(new URL[0], cl);
        GBeanData gbean = new GBeanData(name, MockGBean.getGBeanInfo());
        gbean.setAttribute("name", "Test");
        gbean.setAttribute("finalInt", new Integer(123));
        kernel.loadGBean(gbean, myCl);
        kernel.startGBean(name);
        ProxyManager mgr = kernel.getProxyManager();

        Object test = mgr.createProxy(name, myCl);
        assertTrue(test instanceof MockEndpoint);
        assertTrue(test instanceof MockParentInterface1);
        assertTrue(test instanceof MockParentInterface2);
        assertTrue(test instanceof MockChildInterface1);
        assertTrue(test instanceof MockChildInterface2);
        assertFalse(test instanceof Comparable);
        ((MockEndpoint)test).doNothing();
        assertEquals("Foo", ((MockEndpoint)test).echo("Foo"));
        ((MockParentInterface1)test).setValue("Foo");
        assertEquals("Foo", ((MockParentInterface1)test).getValue());
        ((MockParentInterface1)test).setMutableInt(6);
        assertEquals(6, ((MockParentInterface1)test).getMutableInt());
        ((MockParentInterface2)test).doNothing();
        assertEquals("Foo", ((MockParentInterface2)test).echo("Foo"));
        ((MockParentInterface2)test).setValue("Foo");
        assertEquals("Foo", ((MockParentInterface2)test).getValue());
        ((MockChildInterface1)test).getFinalInt();
        ((MockChildInterface2)test).doNothing();
        assertEquals("Foo", ((MockChildInterface2)test).doSomething("Foo"));

        test = mgr.createProxy(name, MockEndpoint.class);
        assertTrue(test instanceof MockEndpoint);
        assertFalse(test instanceof MockParentInterface1);
        assertFalse(test instanceof MockParentInterface2);
        assertFalse(test instanceof MockChildInterface1);
        assertFalse(test instanceof MockChildInterface2);
        assertFalse(test instanceof Comparable);

        test = mgr.createProxy(name, MockEndpoint.class, new Class[]{MockParentInterface2.class, MockChildInterface2.class});
        assertTrue(test instanceof MockEndpoint);
        assertTrue(test instanceof MockParentInterface1);
        assertTrue(test instanceof MockParentInterface2);
        assertTrue(test instanceof MockChildInterface1);
        assertTrue(test instanceof MockChildInterface2);
        assertFalse(test instanceof Comparable);

        test = mgr.createProxy(name, MockEndpoint.class, new Class[]{MockParentInterface1.class, MockChildInterface1.class});
        assertTrue(test instanceof MockEndpoint);
        assertTrue(test instanceof MockParentInterface1);
        assertFalse(test instanceof MockParentInterface2);
        assertTrue(test instanceof MockChildInterface1);
        assertFalse(test instanceof MockChildInterface2);
        assertFalse(test instanceof Comparable);

        test = mgr.createProxy(name, MockEndpoint.class, new Class[]{MockParentInterface1.class, MockChildInterface1.class, Comparable.class});
        assertTrue(test instanceof MockEndpoint);
        assertTrue(test instanceof MockParentInterface1);
        assertFalse(test instanceof MockParentInterface2);
        assertTrue(test instanceof MockChildInterface1);
        assertFalse(test instanceof MockChildInterface2);
        assertFalse(test instanceof Comparable);

        test = mgr.createProxy(name, null, new Class[]{MockParentInterface1.class, MockChildInterface1.class, Comparable.class});
        assertFalse(test instanceof MockEndpoint);
        assertTrue(test instanceof MockParentInterface1);
        assertFalse(test instanceof MockParentInterface2);
        assertTrue(test instanceof MockChildInterface1);
        assertFalse(test instanceof MockChildInterface2);
        assertFalse(test instanceof Comparable);

        test = mgr.createProxy(name, MockEndpoint.class, new Class[]{Comparable.class});
        assertTrue(test instanceof MockEndpoint);
        assertFalse(test instanceof MockParentInterface1);
        assertFalse(test instanceof MockParentInterface2);
        assertFalse(test instanceof MockChildInterface1);
        assertFalse(test instanceof MockChildInterface2);
        assertFalse(test instanceof Comparable);

        test = mgr.createProxy(name, null, new Class[]{Comparable.class}); // no implementable interface
        assertNull(test);

        try {
            test = mgr.createProxy(name, null, new Class[0]); // no interface
            fail();
        }catch(IllegalArgumentException e) {}

        try {
            test = mgr.createProxy(name, null, null); // no interface
            fail();
        }catch(IllegalArgumentException e) {}

        test = mgr.createProxy(name, MockGBean.class, null); // class not interface
        test = mgr.createProxy(name, MockGBean.class, new Class[]{MockEndpoint.class}); // class and interface

        if(mgr instanceof BasicProxyManager) {
            try { // two classes
                test = ((BasicProxyManager)mgr).createProxyFactory(new Class[]{MockGBean.class, Object.class}).createProxy(name);
                fail();
            }catch(IllegalArgumentException e) {}
        }
    }

    protected void setUp() throws Exception {
        name = new ObjectName("test:name=MyMockGBean");
        name2 = new ObjectName("test:name=MyMockGBean2");
        kernel = KernelFactory.newInstance().createKernel("test");
        kernel.boot();
    }

    protected void tearDown() throws Exception {
        kernel.shutdown();
    }
}
