/**
 *
 * Copyright 2004 The Apache Software Foundation
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

import java.util.Set;
import java.util.Collections;
import java.util.HashSet;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.j2ee.management.impl.DomainImpl;
import org.apache.geronimo.j2ee.management.impl.JVMImpl;
import org.apache.geronimo.j2ee.management.impl.ServerImpl;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.system.serverinfo.ServerInfo;

import junit.framework.TestCase;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:41 $
 */
public abstract class Abstract77Test extends TestCase {
    protected static final ObjectName SERVER_INFO_NAME = JMXUtil.getObjectName("geronimo.system:role=ServerInfo");

    protected static final String DOMAIN = "geronimo.test";
    protected static final ObjectName DOMAIN_NAME = JMXUtil.getObjectName(DOMAIN + ":type=J2EEDomain,name=" + DOMAIN);
    protected static final ObjectName SERVER_NAME = JMXUtil.getObjectName(DOMAIN + ":type=J2EEServer,name=Test");
    protected static final ObjectName JVM_NAME = JMXUtil.getObjectName(DOMAIN + ":type=JVM,J2EEServer=Test");

    private static final Set SERVER_PATTERN = Collections.singleton(JMXUtil.getObjectName(DOMAIN+":type=J2EEServer,*"));
    private static final Set JVM_PATTERN = Collections.singleton(JMXUtil.getObjectName(DOMAIN+":type=JVM,*"));

    protected Kernel kernel;
    protected MBeanServer mbServer;

    protected void setUp() throws Exception {
        super.setUp();
        kernel = new Kernel(DOMAIN);
        kernel.boot();
        GBeanMBean gbean;
        gbean = new GBeanMBean(ServerInfo.getGBeanInfo());
        gbean.setAttribute("BaseDirectory", System.getProperty("java.io.tmpdir"));
        kernel.loadGBean(SERVER_INFO_NAME, gbean);

        gbean = new GBeanMBean(DomainImpl.GBEAN_INFO);
        gbean.setReferencePatterns("Servers", SERVER_PATTERN);
        kernel.loadGBean(DOMAIN_NAME, gbean);

        gbean = new GBeanMBean(ServerImpl.GBEAN_INFO);
        Set objects = new HashSet();
        objects.add(JMXUtil.getObjectName(DOMAIN + ":type=J2EEApplication,J2EEServer=Test,*"));
        objects.add(JMXUtil.getObjectName(DOMAIN + ":type=AppClientModule,J2EEServer=Test,*"));
        objects.add(JMXUtil.getObjectName(DOMAIN + ":type=EJBModule,J2EEServer=Test,*"));
        objects.add(JMXUtil.getObjectName(DOMAIN + ":type=WebModule,J2EEServer=Test,*"));
        objects.add(JMXUtil.getObjectName(DOMAIN + ":type=ResourceAdapterModule,J2EEServer=Test,*"));
        gbean.setReferencePatterns("DeployedObjects", objects);
        objects = new HashSet();
        objects.add(JMXUtil.getObjectName(DOMAIN + ":type=JCAResource,J2EEServer=Test,*"));
        objects.add(JMXUtil.getObjectName(DOMAIN + ":type=JavaMailResource,J2EEServer=Test,*"));
        objects.add(JMXUtil.getObjectName(DOMAIN + ":type=JDBCResource,J2EEServer=Test,*"));
        objects.add(JMXUtil.getObjectName(DOMAIN + ":type=JMSResource,J2EEServer=Test,*"));
        objects.add(JMXUtil.getObjectName(DOMAIN + ":type=JNDIResource,J2EEServer=Test,*"));
        objects.add(JMXUtil.getObjectName(DOMAIN + ":type=JTAResource,J2EEServer=Test,*"));
        objects.add(JMXUtil.getObjectName(DOMAIN + ":type=RMI_IIOPResource,J2EEServer=Test,*"));
        objects.add(JMXUtil.getObjectName(DOMAIN + ":type=URLResource,J2EEServer=Test,*"));
        gbean.setReferencePatterns("Resources", objects);
        gbean.setReferencePatterns("JVMs", JVM_PATTERN);
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
}
