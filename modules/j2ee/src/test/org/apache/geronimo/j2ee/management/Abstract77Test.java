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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.j2ee.management.impl.J2EEDomainImpl;
import org.apache.geronimo.j2ee.management.impl.J2EEServerImpl;
import org.apache.geronimo.j2ee.management.impl.JVMImpl;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.system.serverinfo.ServerInfo;

/**
 * @version $Rev$ $Date$
 */
public abstract class Abstract77Test extends TestCase {
    protected static final ObjectName SERVER_INFO_NAME = JMXUtil.getObjectName("geronimo.system:role=ServerInfo");

    protected static final String DOMAIN = "geronimo.test";
    protected static final ObjectName DOMAIN_NAME = JMXUtil.getObjectName(DOMAIN + ":j2eeType=J2EEDomain,name=" + DOMAIN);
    protected static final ObjectName SERVER_NAME = JMXUtil.getObjectName(DOMAIN + ":j2eeType=J2EEServer,name=Test");
    protected static final ObjectName JVM_NAME = JMXUtil.getObjectName(DOMAIN + ":j2eeType=JVM,J2EEServer=Test");

    protected Kernel kernel;
    protected MBeanServer mbServer;

    protected void setUp() throws Exception {
        super.setUp();
        kernel = new Kernel(DOMAIN);
        kernel.boot();
        GBeanMBean gbean;
        gbean = new GBeanMBean(ServerInfo.getGBeanInfo());
        gbean.setAttribute("baseDirectory", System.getProperty("java.io.tmpdir"));
        kernel.loadGBean(SERVER_INFO_NAME, gbean);

        gbean = new GBeanMBean(J2EEDomainImpl.GBEAN_INFO);
        kernel.loadGBean(DOMAIN_NAME, gbean);

        gbean = new GBeanMBean(J2EEServerImpl.GBEAN_INFO);
        gbean.setReferencePatterns("ServerInfo", Collections.singleton(SERVER_INFO_NAME));
        kernel.loadGBean(SERVER_NAME, gbean);


        kernel.loadGBean(JVM_NAME, new GBeanMBean(JVMImpl.GBEAN_INFO));
        kernel.startGBean(SERVER_INFO_NAME);
        kernel.startGBean(DOMAIN_NAME);
        kernel.startGBean(SERVER_NAME);
        kernel.startGBean(JVM_NAME);
        mbServer = kernel.getMBeanServer();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        kernel.stopGBean(JVM_NAME);
        kernel.stopGBean(SERVER_NAME);
        kernel.stopGBean(DOMAIN_NAME);
        kernel.stopGBean(SERVER_INFO_NAME);
        kernel.unloadGBean(JVM_NAME);
        kernel.unloadGBean(SERVER_NAME);
        kernel.unloadGBean(DOMAIN_NAME);
        kernel.unloadGBean(SERVER_INFO_NAME);
        kernel.shutdown();
        kernel = null;
    }

    protected void assertObjectNamesEqual(String[] expected, String[] test) throws Exception {
        Set expectedSet = new HashSet(expected.length);
        for (int i = 0; i < expected.length; i++) {
            expectedSet.add(new ObjectName(expected[i]));
        }
        Set testSet = new HashSet(test.length);
        for (int i = 0; i < test.length; i++) {
            testSet.add(new ObjectName(test[i]));
        }
        assertEquals(expectedSet, testSet);
    }
}
