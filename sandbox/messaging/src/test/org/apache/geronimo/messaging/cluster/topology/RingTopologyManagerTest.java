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
 * @version $Rev$ $Date$
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
        NodeInfo node4 = new NodeInfo("node4", address, 1234);
        manager.addNode(node4);
        NodeInfo node5 = new NodeInfo("node5", address, 1234);
        manager.addNode(node5);
        
        NodeTopology topology = manager.factoryTopology();

        NodeInfo[] path = topology.getPath(node2, node3);
        assertEquals(1, path.length);
        assertEquals(node3, path[0]);

        path = topology.getPath(node2, node4);
        assertEquals(2, path.length);
        assertEquals(node3, path[0]);
        assertEquals(node4, path[1]);

        path = topology.getPath(node2, node5);
        assertEquals(2, path.length);
        assertEquals(node1, path[0]);
        assertEquals(node5, path[1]);

        path = topology.getPath(node2, node1);
        assertEquals(1, path.length);
        assertEquals(node1, path[0]);
    }

}
