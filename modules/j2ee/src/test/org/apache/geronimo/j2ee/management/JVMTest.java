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

package org.apache.geronimo.j2ee.management;

import java.net.InetAddress;
import javax.management.ObjectName;
import javax.management.MBeanServer;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.jmx.MBeanProxyFactory;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.j2ee.management.impl.JVMImpl;

import junit.framework.TestCase;

/**
 * 
 * 
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:58:52 $
 */
public class JVMTest extends Abstract77Test {
    private JVM jvm;
    private org.apache.geronimo.j2ee.management.geronimo.JVM jvmEx;
    private String node;
    private Runtime runtime;

    public void testStandardInterface() {
        assertEquals(JVM_NAME.toString(), jvm.getobjectName());
        assertEquals(System.getProperty("java.version"), jvm.getjavaVersion());
        assertEquals(System.getProperty("java.vendor"), jvm.getjavaVendor());
        assertEquals(node, jvm.getnode());
    }

    public void testStandardAttributes() throws Exception {
        assertEquals(JVM_NAME.toString(), mbServer.getAttribute(JVM_NAME, "objectName"));
        assertEquals(System.getProperty("java.version"), mbServer.getAttribute(JVM_NAME, "javaVersion"));
        assertEquals(System.getProperty("java.vendor"), mbServer.getAttribute(JVM_NAME, "javaVendor"));
        assertEquals(node, mbServer.getAttribute(JVM_NAME, "node"));
    }

    public void testGeronimoInterface() {
        assertEquals(runtime.availableProcessors(), jvmEx.getavailableProcessors());

        // I'm going to leave these in but I am not sure the results are deterministic
//        assertEquals(runtime.freeMemory(), jvmEx.getfreeMemory());
        assertEquals(runtime.maxMemory(), jvmEx.getmaxMemory());
        assertEquals(runtime.totalMemory(), jvmEx.gettotalMemory());

    }

    public void testGeronimoAttributes() throws Exception {
        assertEquals(new Integer(runtime.availableProcessors()), mbServer.getAttribute(JVM_NAME, "availableProcessors"));

        // I'm going to leave these in but I am not sure the results are deterministic
//        assertEquals(new Long(runtime.freeMemory()), mbServer.getAttribute(JVM_NAME, "freeMemory"));
        assertEquals(new Long(runtime.maxMemory()), mbServer.getAttribute(JVM_NAME, "maxMemory"));
        assertEquals(new Long(runtime.totalMemory()), mbServer.getAttribute(JVM_NAME, "totalMemory"));

    }

    protected void setUp() throws Exception {
        super.setUp();
        jvm = (JVM) MBeanProxyFactory.getProxy(JVM.class, mbServer, JVM_NAME);
        jvmEx = (org.apache.geronimo.j2ee.management.geronimo.JVM) MBeanProxyFactory.getProxy(org.apache.geronimo.j2ee.management.geronimo.JVM.class, mbServer, JVM_NAME);
        node = InetAddress.getLocalHost().toString();
        runtime = Runtime.getRuntime();
    }
}
