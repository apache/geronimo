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
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.management.State;

/**
 * @version $Revision: 1.9 $ $Date: 2004/06/04 22:31:56 $
 */
public class GBeanTest extends TestCase {
    private ObjectName name;
    private ObjectName name2;
    private Kernel kernel;

    public void testLoad() throws Exception {
        ClassLoader cl = getClass().getClassLoader();
        ClassLoader myCl = new URLClassLoader(new URL[0], cl);
        GBeanMBean gbean = new GBeanMBean(MockGBean.getGBeanInfo(), myCl);
        gbean.setAttribute("Name", "Test");
        gbean.setAttribute("FinalInt", new Integer(123));
        kernel.loadGBean(name, gbean);
        kernel.startGBean(name);
        assertEquals(new Integer(State.RUNNING_INDEX), kernel.getMBeanServer().getAttribute(name, "state"));
        assertEquals("Hello", kernel.getMBeanServer().invoke(name, "doSomething", new Object[]{"Hello"}, new String[]{String.class.getName()}));

        assertEquals(name.getCanonicalName(), kernel.getAttribute(name, "objectName"));
        assertEquals(name.getCanonicalName(), kernel.getAttribute(name, "actualObjectName"));

        assertSame(myCl, kernel.getAttribute(name, "actualClassLoader"));

        // the normal classLoader attribute has been changed in the MockGBean implementation to
        // return ClassLoader.getSystemClassLoader()
        assertSame(ClassLoader.getSystemClassLoader(), kernel.getAttribute(name, "classLoader"));

        GBeanContext gbeanContext = (GBeanContext) kernel.getAttribute(name, "gbeanContext");
        assertNotNull(gbeanContext);
        assertEquals(State.RUNNING_INDEX, gbeanContext.getState());

        assertNotSame(kernel, kernel.getAttribute(name, "kernel"));
        assertSame(kernel, kernel.getAttribute(name, "actualKernel"));

        kernel.stopGBean(name);
        kernel.unloadGBean(name);
    }

    public void testEndpoint() throws Exception {
        GBeanMBean gbean1 = new GBeanMBean(MockGBean.getGBeanInfo());
        gbean1.setAttribute("FinalInt", new Integer(123));
        kernel.loadGBean(name, gbean1);
        kernel.startGBean(name);

        GBeanMBean gbean2 = new GBeanMBean(MockGBean.getGBeanInfo());
        gbean2.setAttribute("FinalInt", new Integer(123));
        gbean2.setReferencePatterns("MockEndpoint", Collections.singleton(name));
        kernel.loadGBean(name2, gbean2);
        kernel.startGBean(name2);

        assertEquals("endpointCheck", kernel.getMBeanServer().invoke(name2, "checkEndpoint", null, null));
    }

    protected void setUp() throws Exception {
        name = new ObjectName("test:name=MyMockGBean");
        name2 = new ObjectName("test:name=MyMockGBean2");
        kernel = new Kernel("test.kernel", "test");
        kernel.boot();
    }

    protected void tearDown() throws Exception {
        kernel.shutdown();
    }
}
