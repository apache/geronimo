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
 * @version $Revision: 1.1 $ $Date: 2004/02/22 05:19:10 $
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
