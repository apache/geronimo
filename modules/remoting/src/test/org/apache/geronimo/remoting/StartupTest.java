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

package org.apache.geronimo.remoting;

import java.net.URI;
import java.util.Collections;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.management.State;

import junit.framework.TestCase;

/**
 * 
 * 
 * @version $Revision: 1.5 $ $Date: 2004/02/25 09:58:04 $
 */
public class StartupTest  extends TestCase {
    private Kernel kernel;

    public void testLoad() throws Exception {
        GBeanMBean gbean;

        // Create all the parts
        gbean = new GBeanMBean("org.apache.geronimo.remoting.router.SubsystemRouter");
        ObjectName subsystemRouter = new ObjectName("geronimo.remoting:router=SubsystemRouter");
        kernel.loadGBean(subsystemRouter, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.remoting.transport.TransportLoader");
        gbean.setAttribute("BindURI", new URI("async://0.0.0.0:3434"));
        gbean.setReferencePatterns("Router", Collections.singleton(subsystemRouter));
        ObjectName asyncTransport = new ObjectName("geronimo.remoting:transport=async");
        kernel.loadGBean(asyncTransport, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.remoting.router.JMXRouter");
        gbean.setReferencePatterns("SubsystemRouter", Collections.singleton(subsystemRouter));
        ObjectName jmxRouter = new ObjectName("geronimo.remoting:router=JMXRouter");
        kernel.loadGBean(jmxRouter, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.remoting.router.InterceptorRegistryRouter");
        gbean.setReferencePatterns("SubsystemRouter", Collections.singleton(subsystemRouter));
        ObjectName registeryRouter = new ObjectName("geronimo.remoting:router=InterceptorRegistryRouter");
        kernel.loadGBean(registeryRouter, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.remoting.jmx.MBeanServerStub");
        gbean.setReferencePatterns("Router", Collections.singleton(jmxRouter));
        ObjectName serverStub = new ObjectName("geronimo.remoting:target=MBeanServerStub");
        kernel.loadGBean(serverStub, gbean);

        // Start all the parts
        kernel.startGBean(subsystemRouter);
        kernel.startGBean(asyncTransport);
        kernel.startGBean(jmxRouter);
        kernel.startGBean(registeryRouter);
        kernel.startGBean(serverStub);

        // They should all be started now
        assertEquals(new Integer(State.RUNNING_INDEX), kernel.getMBeanServer().getAttribute(subsystemRouter, "state"));
        assertEquals(new Integer(State.RUNNING_INDEX), kernel.getMBeanServer().getAttribute(asyncTransport, "state"));
        assertEquals(new Integer(State.RUNNING_INDEX), kernel.getMBeanServer().getAttribute(jmxRouter, "state"));
        assertEquals(new Integer(State.RUNNING_INDEX), kernel.getMBeanServer().getAttribute(registeryRouter, "state"));
        assertEquals(new Integer(State.RUNNING_INDEX), kernel.getMBeanServer().getAttribute(serverStub, "state"));

        kernel.stopGBean(subsystemRouter);
        kernel.stopGBean(asyncTransport);
        kernel.stopGBean(jmxRouter);
        kernel.stopGBean(registeryRouter);
        kernel.stopGBean(serverStub);

        kernel.unloadGBean(subsystemRouter);
        kernel.unloadGBean(asyncTransport);
        kernel.unloadGBean(jmxRouter);
        kernel.unloadGBean(registeryRouter);
        kernel.unloadGBean(serverStub);
    }

    protected void setUp() throws Exception {
        kernel = new Kernel("test.kernel", "test");
        kernel.boot();
    }

    protected void tearDown() throws Exception {
        kernel.shutdown();
    }
}
