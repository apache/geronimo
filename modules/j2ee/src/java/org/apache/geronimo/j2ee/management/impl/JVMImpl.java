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

package org.apache.geronimo.j2ee.management.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Properties;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.management.geronimo.JVM;
import org.apache.geronimo.kernel.Kernel;

/**
 *
 *
 * @version $Rev$ $Date$
 */
public class JVMImpl implements JVM {
    public static final String JAVA_VERSION = System.getProperty("java.version");
    public static final String JAVA_VENDOR = System.getProperty("java.vendor");
    public static final String NODE;
    private static final Runtime runtime = Runtime.getRuntime();

    static {
        String node;
        try {
            node = InetAddress.getLocalHost().toString();
        } catch (UnknownHostException e) {
            node = null;
        }
        NODE = node;
    }

    private final String objectName;
    private final Kernel kernel;

    public JVMImpl(String objectName, Kernel kernel) {
        this.objectName = objectName;
        this.kernel = kernel;
    }

    public String getObjectName() {
        return objectName;
    }

    public boolean isStateManageable() {
        return true;
    }

    public boolean isStatisticsProvider() {
        return false;
    }

    public boolean isEventProvider() {
        return true;
    }

    /**
     * The version of the JVMImpl we are running on.
     * This is the value of java.version system property
     * @see "JSR77.3.4.1.1"
     * @return the JVMImpl version
     */
    public String getJavaVersion() {
        return JAVA_VERSION;
    }

    /**
     * The vendor of the JVMImpl we are running on.
     * This is the value of java.vendor system property
     * @see "JSR77.3.4.1.2"
     * @return the JVMImpl version
     */
    public String getJavaVendor() {
        return JAVA_VENDOR;
    }

    /**
     * The node we are running on.
     * This is the fully qualified host name returned for InetAddress.getLocalHost.toString();
     * we return null if there is no network
     * @see "JSR77.3.4.1.3"
     * @return the node we are running on
     */
    public String getNode() {
        return NODE;
    }

    public long getFreeMemory() {
        return runtime.freeMemory();
    }

    public long getMaxMemory() {
        return runtime.maxMemory();
    }

    public long getTotalMemory() {
        return runtime.totalMemory();
    }

    public int getAvailableProcessors() {
        return runtime.availableProcessors();
    }

    public Date getKernelBootTime() {
        return kernel.getBootTime();
    }

    public Properties getSystemProperties() {
        return System.getProperties();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(JVMImpl.class, NameFactory.JVM);

//        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addInterface(JVM.class);
        infoFactory.setConstructor(new String[] {"objectName", "kernel"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
