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

package org.apache.geronimo.messaging.reference;

import java.net.InetAddress;

import junit.framework.TestCase;

import org.apache.geronimo.messaging.EndPointUtil;
import org.apache.geronimo.messaging.MockNode;
import org.apache.geronimo.messaging.NodeInfo;

/**
 *
 * @version $Revision: 1.2 $ $Date: 2004/05/24 12:03:34 $
 */
public class ReferenceManagerImplTest extends TestCase
{

    public void testInvokeOn() throws Exception {
        InetAddress address = InetAddress.getLocalHost();
        NodeInfo nodeInfo1 = new NodeInfo("Node1", address, 8081);
        MockNode node1 = new MockNode();
        node1.setNodeInfo(nodeInfo1);
        
        ReferenceableManager manager1 = new ReferenceableManagerImpl(node1, "A");
        ReferenceableManager manager2 = new ReferenceableManagerImpl(new MockNode(), "A");

        EndPointUtil.interConnect(manager1, manager2);
        
        MockReferenceableImpl reference1 = new MockReferenceableImpl();
        ReferenceableInfo info = manager1.register(reference1);
        
        MockReferenceable reference2 = (MockReferenceable) manager2.factoryProxy(info);
        
        String expected = "TEST";
        reference2.testMe(expected);
        
        assertEquals(expected, reference1.getParam());
    }
    
    public void testLocalReference() throws Exception {
        InetAddress address = InetAddress.getLocalHost();
        NodeInfo nodeInfo = new NodeInfo("Node", address, 8081);
        MockNode node = new MockNode();
        node.setNodeInfo(nodeInfo);
        
        ReferenceableManager manager = new ReferenceableManagerImpl(node, "A");
        MockReferenceableImpl reference = new MockReferenceableImpl();

        ReferenceableInfo info = manager.register(reference);
        Object opaque = manager.factoryProxy(info);
        assertTrue(reference == opaque);
    }
}
