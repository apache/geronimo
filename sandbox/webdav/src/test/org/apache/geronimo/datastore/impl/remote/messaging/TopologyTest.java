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

package org.apache.geronimo.datastore.impl.remote.messaging;

import java.net.InetAddress;

import org.apache.geronimo.datastore.impl.remote.messaging.Topology.NodePath;
import org.apache.geronimo.datastore.impl.remote.messaging.Topology.PathWeight;

import junit.framework.TestCase;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/03/11 15:36:13 $
 */
public class TopologyTest extends TestCase {

    public void testGetPath() throws Exception {
        PathWeight weight30 = new PathWeight(30);
        PathWeight weight10 = new PathWeight(10);
        
        Topology topology = new Topology();
        InetAddress address = InetAddress.getLocalHost();
        NodeInfo node1 = new NodeInfo("node1", address, 8080);
        NodeInfo node2 = new NodeInfo("node2", address, 8080);
        NodePath path1 = new NodePath(node1, node2, weight10, weight10);
        topology.addPath(path1);
        NodeInfo[] path = topology.getPath(node1, node2);
        assertEquals(1, path.length);
        
        NodeInfo node3 = new NodeInfo("node3", address, 8080);
        NodePath path2 = new NodePath(node2, node3, weight10, weight10);
        topology.addPath(path2);
        path = topology.getPath(node1, node3);
        assertEquals(2, path.length);
        
        NodeInfo node4 = new NodeInfo("node4", address, 8080);
        NodePath path3 = new NodePath(node3, node4, weight10, weight10);
        topology.addPath(path3);
        path = topology.getPath(node1, node4);
        assertEquals(3, path.length);
        
        topology.removePath(path3);
        path = topology.getPath(node1, node4);
        assertNull(path);
        
        path3 = new NodePath(node3, node1, weight30, weight10);
        topology.addPath(path3);
        path = topology.getPath(node1, node3);
        assertEquals(1, path.length);
        path = topology.getPath(node3, node1);
        assertEquals(2, path.length);
    }
    
}
