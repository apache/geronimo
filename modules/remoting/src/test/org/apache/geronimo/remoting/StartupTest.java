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
 * @version $Revision: 1.3 $ $Date: 2004/01/31 20:20:44 $
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
        kernel = new Kernel("test");
        kernel.boot();
    }

    protected void tearDown() throws Exception {
        kernel.shutdown();
    }
}
