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

package org.apache.geronimo.messaging.cluster.topology;

import java.net.InetAddress;
import java.util.Set;

import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.NodeTopology;

import junit.framework.TestCase;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/06/10 23:12:24 $
 */
public class RingTopologyManagerTest extends TestCase
{

    public void testGetNeighbours1() throws Exception {
        InetAddress address = InetAddress.getLocalHost();

        RingTopologyManager manager = new RingTopologyManager();
        
        NodeInfo node1 = new NodeInfo("node1", address, 1234);
        manager.addNode(node1);
        
        NodeTopology topology = manager.factoryTopology();
        Set neighbours = topology.getNeighbours(node1);
        assertEquals(0, neighbours.size());
    }

    public void testGetNeighbours2() throws Exception {
        InetAddress address = InetAddress.getLocalHost();

        RingTopologyManager manager = new RingTopologyManager();
        
        NodeInfo node1 = new NodeInfo("node1", address, 1234);
        manager.addNode(node1);
        NodeInfo node2 = new NodeInfo("node2", address, 1234);
        manager.addNode(node2);
        
        NodeTopology topology = manager.factoryTopology();
        Set neighbours = topology.getNeighbours(node1);
        assertEquals(1, neighbours.size());
        assertTrue(neighbours.contains(node2));
        neighbours = topology.getNeighbours(node2);
        assertEquals(1, neighbours.size());
        assertTrue(neighbours.contains(node1));
    }

    public void testGetNeighbours3() throws Exception {
        InetAddress address = InetAddress.getLocalHost();

        RingTopologyManager manager = new RingTopologyManager();
        
        NodeInfo node1 = new NodeInfo("node1", address, 1234);
        manager.addNode(node1);
        NodeInfo node2 = new NodeInfo("node2", address, 1234);
        manager.addNode(node2);
        NodeInfo node3 = new NodeInfo("node3", address, 1234);
        manager.addNode(node3);
        
        NodeTopology topology = manager.factoryTopology();
        Set neighbours = topology.getNeighbours(node1);
        assertEquals(2, neighbours.size());
        assertTrue(neighbours.contains(node2));
        assertTrue(neighbours.contains(node3));
        neighbours = topology.getNeighbours(node2);
        assertEquals(2, neighbours.size());
        assertTrue(neighbours.contains(node1));
        assertTrue(neighbours.contains(node3));
        neighbours = topology.getNeighbours(node3);
        assertEquals(2, neighbours.size());
        assertTrue(neighbours.contains(node1));
        assertTrue(neighbours.contains(node2));
    }

    public void testGetPath() throws Exception {
        InetAddress address = InetAddress.getLocalHost();

        RingTopologyManager manager = new RingTopologyManager();
        
        NodeInfo node1 = new NodeInfo("node1", address, 1234);
        manager.addNode(node1);
        NodeInfo node2 = new NodeInfo("node2", address, 1234);
        manager.addNode(node2);
        NodeInfo node3 = new NodeInfo("node3", address, 1234);
        manager.addNode(node3);
        
        NodeTopology topology = manager.factoryTopology();
        NodeInfo[] path = topology.getPath(node1, node3);
        assertEquals(2, path.length);
        assertEquals(node2, path[0]);
        assertEquals(node3, path[1]);

        path = topology.getPath(node3, node1);
        assertEquals(2, path.length);
        assertEquals(node2, path[0]);
        assertEquals(node1, path[1]);
    }
    
}
