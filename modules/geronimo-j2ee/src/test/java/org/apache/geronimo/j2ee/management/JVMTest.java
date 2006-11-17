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

package org.apache.geronimo.j2ee.management;

import java.net.InetAddress;
import javax.management.j2ee.statistics.JVMStats;
import org.apache.geronimo.management.JVM;

/**
 * @version $Rev$ $Date$
 */
public class JVMTest extends Abstract77Test {
    private JVM jvm;
    private org.apache.geronimo.management.geronimo.JVM jvmEx;
    private String node;
    private Runtime runtime;

    public void testStandardInterface() {
        assertEquals(JVM_DATA.getAbstractName().getObjectName().getCanonicalName(), jvm.getObjectName());
        assertEquals(System.getProperty("java.version"), jvm.getJavaVersion());
        assertEquals(System.getProperty("java.vendor"), jvm.getJavaVendor());
        assertEquals(node, jvm.getNode());
    }

    public void testStandardAttributes() throws Exception {
        assertEquals(JVM_DATA.getAbstractName().getObjectName().getCanonicalName(), kernel.getAttribute(JVM_DATA.getAbstractName(), "objectName"));
        assertEquals(System.getProperty("java.version"), kernel.getAttribute(JVM_DATA.getAbstractName(), "javaVersion"));
        assertEquals(System.getProperty("java.vendor"), kernel.getAttribute(JVM_DATA.getAbstractName(), "javaVendor"));
        assertEquals(node, kernel.getAttribute(JVM_DATA.getAbstractName(), "node"));
    }

    public void testGeronimoInterface() {
        assertEquals(runtime.availableProcessors(), jvmEx.getAvailableProcessors());

    }

    public void testGeronimoAttributes() throws Exception {
        assertEquals(new Integer(runtime.availableProcessors()), kernel.getAttribute(JVM_DATA.getAbstractName(), "availableProcessors"));
    }

    public void testStatistics() throws Exception {
        assertEquals(Boolean.TRUE, kernel.getAttribute(JVM_DATA.getAbstractName(), "statisticsProvider"));
        JVMStats stats = (JVMStats) kernel.getAttribute(JVM_DATA.getAbstractName(), "stats");
        assertNotNull(stats.getHeapSize());
        assertTrue(stats.getHeapSize().getCurrent() > 0);
        assertNotNull(stats.getHeapSize().getDescription());
        assertTrue(stats.getHeapSize().getHighWaterMark() > 0);
        assertTrue(stats.getHeapSize().getLastSampleTime() > 0);
        assertTrue(stats.getHeapSize().getLowerBound() == 0);
        assertTrue(stats.getHeapSize().getLowWaterMark() > 0);
        assertNotNull(stats.getHeapSize().getName());
        assertTrue(stats.getHeapSize().getStartTime() > 0);
        assertNotNull(stats.getHeapSize().getUnit());
        assertTrue(stats.getHeapSize().getUpperBound() > 0);
        assertNotNull(stats.getUpTime());
        assertTrue(stats.getUpTime().getCount() > 0);
        assertNotNull(stats.getUpTime().getDescription());
        assertTrue(stats.getUpTime().getLastSampleTime() > 0);
        assertNotNull(stats.getUpTime().getName());
        assertTrue(stats.getUpTime().getStartTime() > 0);
        assertNotNull(stats.getUpTime().getUnit());
    }

    protected void setUp() throws Exception {
        super.setUp();
        jvm = (JVM) kernel.getGBean(JVM_DATA.getAbstractName());
        jvmEx = (org.apache.geronimo.management.geronimo.JVM) kernel.getGBean(JVM_DATA.getAbstractName());
        node = InetAddress.getLocalHost().toString();
        runtime = Runtime.getRuntime();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
