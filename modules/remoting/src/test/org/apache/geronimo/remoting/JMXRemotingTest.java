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

import java.net.URI;
import java.util.Collections;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.remoting.jmx.MBeanServerStub;
import org.apache.geronimo.remoting.jmx.RemoteMBeanServerFactory;
import org.apache.geronimo.remoting.router.JMXRouter;
import org.apache.geronimo.remoting.router.SubsystemRouter;
import org.apache.geronimo.remoting.transport.BytesMarshalledObject;
import org.apache.geronimo.remoting.transport.TransportLoader;


/**
 * @version $Rev$ $Date$
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
        kernel = new Kernel("simple.geronimo.test");
        kernel.boot();

        GBeanData gbean;

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        // Create all the parts
        subsystemRouter = new ObjectName("geronimo.remoting:router=SubsystemRouter");
        gbean = new GBeanData(subsystemRouter, SubsystemRouter.GBEAN_INFO);
        kernel.loadGBean(gbean, cl);

        asyncTransport = new ObjectName("geronimo.remoting:transport=async");
        gbean = new GBeanData(asyncTransport, TransportLoader.GBEAN_INFO);
        gbean.setAttribute("bindURI", new URI("async://0.0.0.0:0"));
        gbean.setReferencePatterns("Router", Collections.singleton(subsystemRouter));
        kernel.loadGBean(gbean, cl);

        jmxRouter = new ObjectName("geronimo.remoting:router=JMXRouter");
        gbean = new GBeanData(jmxRouter, JMXRouter.GBEAN_INFO);
        gbean.setReferencePatterns("SubsystemRouter", Collections.singleton(subsystemRouter));
        kernel.loadGBean(gbean, cl);

        serverStub = new ObjectName("geronimo.remoting:target=MBeanServerStub");
        gbean = new GBeanData(serverStub, MBeanServerStub.GBEAN_INFO);
        gbean.setReferencePatterns("Router", Collections.singleton(jmxRouter));
        kernel.loadGBean(gbean, cl);

        kernel.startGBean(subsystemRouter);
        kernel.startGBean(asyncTransport);
        kernel.startGBean(jmxRouter);
        kernel.startGBean(serverStub);

        connectURI = (URI) kernel.getAttribute(asyncTransport, "clientConnectURI"); 

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
