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

package org.apache.geronimo.datastore.impl.remote.replication;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.management.ObjectName;

import junit.framework.TestCase;

import org.apache.geronimo.datastore.impl.remote.messaging.Node;
import org.apache.geronimo.datastore.impl.remote.messaging.NodeInfo;
import org.apache.geronimo.datastore.impl.remote.messaging.NodeImpl;
import org.apache.geronimo.datastore.impl.remote.messaging.Topology;
import org.apache.geronimo.datastore.impl.remote.messaging.Topology.NodePath;
import org.apache.geronimo.datastore.impl.remote.messaging.Topology.PathWeight;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;

/**
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/24 11:37:06 $
 */
public class ReplicationTest extends TestCase {
    
    private Kernel kernel1;
    private ObjectName node1Name;
    private ObjectName repNode1Name;
    private ReplicationMember repNode1; 
    
    private Kernel kernel2;
    private ObjectName node2Name;
    private ObjectName repNode2Name;
    private ReplicationMember repNode2; 
    
    private void loadAndStart(Kernel kernel, ObjectName name, GBeanMBean instance)
        throws Exception {
        kernel.loadGBean(name, instance);
        kernel.startGBean(name);
    }
    
    private void unloadAndStop(Kernel kernel, ObjectName name)
        throws Exception {
        kernel.stopGBean(name);
        kernel.unloadGBean(name);
    }

    protected void setUp() throws Exception {
        InetAddress address = InetAddress.getLocalHost();
        NodeInfo primaryNode = new NodeInfo("Primary", address, 8080);
        NodeInfo secondaryNode = new NodeInfo("Secondary", address, 8082);
        

        // Set-up the first ServerNode.
        kernel1 = new Kernel("test.kernel1", "test");
        kernel1.boot();

        node1Name = new ObjectName("geronimo.test:role=node1");
        GBeanMBean node1GB = new GBeanMBean(NodeImpl.GBEAN_INFO);
        node1GB.setAttribute("NodeInfo", primaryNode);
        repNode1Name = new ObjectName("geronimo.test:role=replication");
        GBeanMBean repNode1GB = new GBeanMBean(ReplicationMemberImpl.GBEAN_INFO);
        repNode1GB.setReferencePatterns("Node",
            Collections.singleton(node1Name));
        repNode1GB.setAttribute("Name", "Replication");
        repNode1GB.setAttribute("TargetNodes", new NodeInfo[] {secondaryNode});
        loadAndStart(kernel1, repNode1Name, repNode1GB);
        loadAndStart(kernel1, node1Name, node1GB);
        repNode1 = (ReplicationMember) repNode1GB.getTarget();
        
        // Set-up the second ServerNode.
        kernel2 = new Kernel("test.kernel2", "test");
        kernel2.boot();
        
        node2Name = new ObjectName("geronimo.test:role=node2");
        GBeanMBean node2GB = new GBeanMBean(NodeImpl.GBEAN_INFO);
        node2GB.setAttribute("NodeInfo", secondaryNode);
        repNode2Name = new ObjectName("geronimo.test:role=replication");
        GBeanMBean repNode2GB = new GBeanMBean(ReplicationMemberImpl.GBEAN_INFO);
        repNode2GB.setReferencePatterns("Node",
            Collections.singleton(node2Name));
        repNode2GB.setAttribute("Name", "Replication");
        repNode2GB.setAttribute("TargetNodes", new NodeInfo[] {primaryNode});
        loadAndStart(kernel2, repNode2Name, repNode2GB);
        loadAndStart(kernel2, node2Name, node2GB);
        repNode2 = (ReplicationMember) repNode2GB.getTarget();
        
        Node node = (Node) node2GB.getTarget();
        // The second ServerNode joins the first one.
        node.join(primaryNode);
        
        // Sets the topology.
        Topology topology = new Topology();
        PathWeight weight = new PathWeight(10);
        NodePath path = new NodePath(primaryNode, secondaryNode, weight, weight);
        topology.addPath(path);

        kernel1.setAttribute(node1Name, "Topology", topology);
        kernel2.setAttribute(node2Name, "Topology", topology);
    }

    protected void tearDown() throws Exception {
        unloadAndStop(kernel1, repNode1Name);
        unloadAndStop(kernel1, node1Name);
        kernel1.shutdown();

        unloadAndStop(kernel2, repNode2Name);
        unloadAndStop(kernel2, node2Name);
        kernel2.shutdown();
    }
    
    public void testUseCase() throws Exception {
        SimpleReplicatedMap replicant1 = new SimpleReplicatedMap();
        replicant1.put("test1", "value1");
        repNode1.registerReplicantCapable(replicant1);
        Object id = replicant1.getID();
        SimpleReplicatedMap replicant2 = (SimpleReplicatedMap)
            repNode2.retrieveReplicantCapable(id);
        assertNotNull("Not been registered", replicant2);
        assertEquals("value1", replicant2.get("test1"));
        replicant1.put("test2", "value2");
        assertEquals("value2", replicant2.get("test2"));
        replicant1.remove("test1");
        assertNull(replicant2.get("test1"));
        Map tmp = new HashMap();
        tmp.put("test3", "value3");
        replicant1.putAll(tmp);
        assertEquals("value3", replicant2.get("test3"));
        replicant2.remove("test3");
        assertNull(replicant1.get("test3"));
    }
    
}
