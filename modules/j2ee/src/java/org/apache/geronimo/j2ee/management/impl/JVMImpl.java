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

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.management.geronimo.JVM;

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

    public JVMImpl(String objectName) {
        this.objectName = objectName;
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

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(JVMImpl.class, NameFactory.JVM);

        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addAttribute("javaVersion", String.class, false);
        infoFactory.addAttribute("javaVendor", String.class, false);
        infoFactory.addAttribute("node", String.class, false);
        infoFactory.addAttribute("freeMemory", Long.TYPE, false);
        infoFactory.addAttribute("maxMemory", Long.TYPE, false);
        infoFactory.addAttribute("totalMemory", Long.TYPE, false);
        infoFactory.addAttribute("availableProcessors", Integer.TYPE, false);
        infoFactory.addInterface(JVM.class);
        infoFactory.setConstructor(new String[] {"objectName"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
