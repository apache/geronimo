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

/**
 * @version $Rev$ $Date$
 */
public class JVMTest extends Abstract77Test {
    private JVM jvm;
    private org.apache.geronimo.j2ee.management.geronimo.JVM jvmEx;
    private String node;
    private Runtime runtime;

    public void testStandardInterface() {
        assertEquals(JVM_NAME.getCanonicalName(), jvm.getObjectName());
        assertEquals(System.getProperty("java.version"), jvm.getJavaVersion());
        assertEquals(System.getProperty("java.vendor"), jvm.getJavaVendor());
        assertEquals(node, jvm.getNode());
    }

    public void testStandardAttributes() throws Exception {
        assertEquals(JVM_NAME.getCanonicalName(), kernel.getAttribute(JVM_NAME, "objectName"));
        assertEquals(System.getProperty("java.version"), kernel.getAttribute(JVM_NAME, "javaVersion"));
        assertEquals(System.getProperty("java.vendor"), kernel.getAttribute(JVM_NAME, "javaVendor"));
        assertEquals(node, kernel.getAttribute(JVM_NAME, "node"));
    }

    public void testGeronimoInterface() {
        assertEquals(runtime.availableProcessors(), jvmEx.getAvailableProcessors());

        // I'm going to leave these in but I am not sure the results are deterministic
//        assertEquals(runtime.freeMemory(), jvmEx.getFreeMemory());
        assertEquals(runtime.maxMemory(), jvmEx.getMaxMemory());
        assertEquals(runtime.totalMemory(), jvmEx.getTotalMemory());

    }

    public void testGeronimoAttributes() throws Exception {
        assertEquals(new Integer(runtime.availableProcessors()), kernel.getAttribute(JVM_NAME, "availableProcessors"));

        // I'm going to leave these in but I am not sure the results are deterministic
//        assertEquals(new Long(runtime.freeMemory()), kernel.getAttribute(JVM_NAME, "freeMemory"));
        assertEquals(new Long(runtime.maxMemory()), kernel.getAttribute(JVM_NAME, "maxMemory"));
        assertEquals(new Long(runtime.totalMemory()), kernel.getAttribute(JVM_NAME, "totalMemory"));

    }

    protected void setUp() throws Exception {
        super.setUp();
        jvm = (JVM) kernel.getProxyManager().createProxy(JVM_NAME, JVM.class);
        jvmEx = (org.apache.geronimo.j2ee.management.geronimo.JVM) kernel.getProxyManager().createProxy(JVM_NAME, org.apache.geronimo.j2ee.management.geronimo.JVM.class);
        node = InetAddress.getLocalHost().toString();
        runtime = Runtime.getRuntime();
    }

    protected void tearDown() throws Exception {
        kernel.getProxyManager().destroyProxy(jvm);
        kernel.getProxyManager().destroyProxy(jvmEx);
        super.tearDown();
    }
}
