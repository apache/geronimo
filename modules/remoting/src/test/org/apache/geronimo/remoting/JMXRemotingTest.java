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

package org.apache.geronimo.remoting;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import java.net.URI;
import java.util.Collections;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.MBeanProxyFactory;
import org.apache.geronimo.remoting.MarshalledObject;
import org.apache.geronimo.remoting.jmx.RemoteMBeanServerFactory;
import org.apache.geronimo.remoting.transport.TransportLoader;
import org.apache.geronimo.remoting.transport.BytesMarshalledObject;


/**
 * @version $Revision: 1.5 $ $Date: 2004/03/10 09:59:20 $
 */

public class JMXRemotingTest extends TestCase {
    private Kernel kernel;
    ObjectName subsystemRouter;
    ObjectName asyncTransport;
    ObjectName jmxRouter;
    ObjectName serverStub;
    URI connectURI;
    MBeanServer remoteProxy;

    public void setUp() throws Exception {
        kernel = new Kernel("test.kernel", "simple.geronimo.test");
        kernel.boot();

        GBeanMBean gbean;

        // Create all the parts
        gbean = new GBeanMBean("org.apache.geronimo.remoting.router.SubsystemRouter");
        subsystemRouter = new ObjectName("geronimo.remoting:router=SubsystemRouter");
        kernel.loadGBean(subsystemRouter, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.remoting.transport.TransportLoader");
        gbean.setAttribute("BindURI", new URI("async://0.0.0.0:0"));
        gbean.setReferencePatterns("Router", Collections.singleton(subsystemRouter));
        asyncTransport = new ObjectName("geronimo.remoting:transport=async");
        kernel.loadGBean(asyncTransport, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.remoting.router.JMXRouter");
        gbean.setReferencePatterns("SubsystemRouter", Collections.singleton(subsystemRouter));
        jmxRouter = new ObjectName("geronimo.remoting:router=JMXRouter");
        kernel.loadGBean(jmxRouter, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.remoting.jmx.MBeanServerStub");
        gbean.setReferencePatterns("Router", Collections.singleton(jmxRouter));
        serverStub = new ObjectName("geronimo.remoting:target=MBeanServerStub");
        kernel.loadGBean(serverStub, gbean);

        kernel.startGBean(subsystemRouter);
        kernel.startGBean(asyncTransport);
        kernel.startGBean(jmxRouter);
        kernel.startGBean(serverStub);

        TransportLoader bean = (TransportLoader) MBeanProxyFactory.getProxy(TransportLoader.class, kernel.getMBeanServer(), asyncTransport);
        connectURI = bean.getClientConnectURI();

        // simulate remote copy of handle
        MarshalledObject mo = new BytesMarshalledObject(RemoteMBeanServerFactory.create(connectURI.getHost(), connectURI.getPort()));
        remoteProxy = (MBeanServer)mo.get();
    }

    protected void tearDown() throws Exception {
        kernel.stopGBean(serverStub);
        kernel.stopGBean(jmxRouter);
        kernel.stopGBean(asyncTransport);
        kernel.stopGBean(subsystemRouter);

        kernel.unloadGBean(subsystemRouter);
        kernel.unloadGBean(asyncTransport);
        kernel.unloadGBean(jmxRouter);
        kernel.unloadGBean(serverStub);

        kernel.shutdown();
    }

    public void testSomething() throws Exception {
        assertEquals("Should be able to obtain default domain", "simple.geronimo.test", remoteProxy.getDefaultDomain());
        assertTrue("There should be a couple of beans", remoteProxy.getMBeanCount().intValue() > 0);
    }
}
