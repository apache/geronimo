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

package org.apache.geronimo.messaging.proxy;

import java.net.InetAddress;
import java.util.List;

import junit.framework.TestCase;

import org.apache.geronimo.messaging.EndPointUtil;
import org.apache.geronimo.messaging.MockEndPoint;
import org.apache.geronimo.messaging.MockEndPointImpl;
import org.apache.geronimo.messaging.MockNode;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.Request;

/**
 *
 * @version $Revision: 1.2 $ $Date: 2004/05/24 12:03:34 $
 */
public class EndPointProxyFactoryTest
    extends TestCase
{

    private NodeInfo[] targets;
    private Object proxy;
    private EndPointProxyFactory factory;
    
    protected void setUp() throws Exception {
        InetAddress address = InetAddress.getLocalHost();
        targets = new NodeInfo[] {new NodeInfo("dummy", address, 8081)};
        
        factory = new EndPointProxyFactoryImpl(new MockNode(), "Factory");
        
        EndPointProxyInfo proxyInfo =
            new EndPointProxyInfo("", new Class[] {MockEndPoint.class},
                targets);
        proxy = factory.factory(proxyInfo);
    }
    
    public void testTypes() throws Exception {
        assertTrue(proxy instanceof MockEndPoint);
        assertTrue(proxy instanceof EndPointProxy);
    }

    public void testInvoke() throws Exception {
        MockEndPoint actual = new MockEndPointImpl(new MockNode(), "", targets);
        
        EndPointUtil.interConnect(actual, factory);
        
        MockEndPoint endPoint = (MockEndPoint) proxy;
        Object opaque = new Object();
        endPoint.sendRawObject(opaque);
        List received = actual.getReceived();
        assertEquals(1, received.size());
        assertEquals(opaque, ((Request)received.get(0)).getParameters()[0]);
    }

    public void testRelease() throws Exception {
        factory.releaseProxy(proxy);
        try {
            ((MockEndPoint) proxy).sendRawObject(null);
            fail("Proxy has been released. Can not use anymore.");
        } catch (IllegalStateException e1) {
        }
        
        try {
            factory.releaseProxy(new Object());
            fail("Not a proxy.");
        } catch (IllegalArgumentException e) {
        }
    }
    
}
