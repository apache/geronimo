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

package org.apache.geronimo.messaging.jmx;

import javax.management.MBeanServer;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.LazyLoader;

import org.apache.geronimo.kernel.KernelMBean;
import org.apache.geronimo.messaging.AbstractEndPoint;
import org.apache.geronimo.messaging.Node;
import org.apache.geronimo.messaging.reference.Referenceable;

/**
 * MBeanServerEndPoint implementation.
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/24 12:12:47 $
 */
public class MBeanServerEndPointImpl
    extends AbstractEndPoint
    implements MBeanServerEndPoint
{

    /**
     * MBeanServer exposed to other EndPoints. This MBeanServer is also a
     * Referenceable in order to be passed around to EndPoints registered by
     * remote Nodes.
     */
    private final MBeanServer server;
    
    /**
     * Creates an EndPoint providing an access to the MBeanServer used by
     * the specified KernelMBean.
     * 
     * @param aNode Hosting Node.
     * @param anID EndPoint identifier.
     * @param aKernel Kernel whose MBeanServer is to be exposed.
     */
    public MBeanServerEndPointImpl(Node aNode, Object anID,
        final KernelMBean aKernel) {
        super(aNode, anID);
        if ( null == aKernel ) {
            throw new IllegalArgumentException("Kernel is required.");
        }
        // Injects the Referenceable interface.
        Enhancer enhancer = new Enhancer();
        enhancer.setInterfaces(
                new Class[] {MBeanServer.class, Referenceable.class});
        enhancer.setCallbackType(LazyLoader.class);
        enhancer.setCallback(new LazyLoader() {
            public Object loadObject() throws Exception {
                return aKernel.getMBeanServer();
            }
        });
        // Gets rid of the Factory interface.
        // Implementation note: when the proxy was implementing it, the
        // ReferenceableInfo was defining two interfaces MBeanServer and
        // Factory. Upon deserialization these two interfaces were MBeanServer
        // and MBeanServer.
        enhancer.setUseFactory(false);
        server = (MBeanServer) enhancer.create();
    }

    public MBeanServer getMBeanServer() {
        return server;
    }
    
}
