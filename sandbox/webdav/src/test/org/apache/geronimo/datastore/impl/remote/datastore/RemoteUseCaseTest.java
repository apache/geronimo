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

package org.apache.geronimo.datastore.impl.remote.datastore;

import java.io.File;
import java.net.InetAddress;
import java.util.Collections;

import javax.management.ObjectName;

import org.apache.geronimo.datastore.GFileManager;
import org.apache.geronimo.datastore.Util;
import org.apache.geronimo.datastore.impl.LockManager;
import org.apache.geronimo.datastore.impl.local.AbstractUseCaseTest;
import org.apache.geronimo.datastore.impl.local.LocalGFileManager;
import org.apache.geronimo.datastore.impl.remote.messaging.Node;
import org.apache.geronimo.datastore.impl.remote.messaging.NodeImpl;
import org.apache.geronimo.datastore.impl.remote.messaging.NodeInfo;
import org.apache.geronimo.datastore.impl.remote.messaging.Topology;
import org.apache.geronimo.datastore.impl.remote.messaging.Topology.NodePath;
import org.apache.geronimo.datastore.impl.remote.messaging.Topology.PathWeight;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;

/**
 * This is a remote use-case.
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/24 11:42:57 $
 */
public class RemoteUseCaseTest extends AbstractUseCaseTest {

    private Kernel kernel1;
    private ObjectName node1Name;
    private ObjectName delegateName;
    private ObjectName proxyName;
    
    private Kernel kernel2;
    private ObjectName node2Name;
    private ObjectName clientName;
    
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
    
    /**
     * In this set-up one initializes two nodes, namely Node1 and Node2. A
     * local GFileManager is mounted by Node1. A client GFileManager is mounted
     * by Node2. Node2 joins Node1.
     */
    protected void setUp() throws Exception {
        File root = new File(System.getProperty("java.io.tmpdir"),
            "GFileManager");
        Util.recursiveDelete(root);
        root.mkdir();
        
        LockManager lockManager = new LockManager();

        InetAddress address = InetAddress.getLocalHost();
        NodeInfo node1Info = new NodeInfo("Node1", address, 8080);
        NodeInfo node2Info = new NodeInfo("Node2", address, 8082);
        

        // Set-up the first ServerNode.
        kernel1 = new Kernel("test.kernel1", "test");
        kernel1.boot();

        node1Name = new ObjectName("geronimo.test:role=node1");
        GBeanMBean node1GB = new GBeanMBean(NodeImpl.GBEAN_INFO);
        node1GB.setAttribute("NodeInfo", node1Info);
        delegateName = new ObjectName("geronimo.test:role=delegate");
        GBeanMBean delegateGB = new GBeanMBean(LocalGFileManager.GBEAN_INFO);
        delegateGB.setAttribute("Name", "FileSystem1");
        delegateGB.setAttribute("Root", root);
        delegateGB.setAttribute("LockManager", lockManager);
        proxyName = new ObjectName("geronimo.test:role=proxy");
        GBeanMBean proxyGB = new GBeanMBean(GFileManagerProxy.GBEAN_INFO);
        proxyGB.setReferencePatterns("Delegate", Collections.singleton(delegateName));
        proxyGB.setReferencePatterns("Node",
            Collections.singleton(node1Name));
        loadAndStart(kernel1, delegateName, delegateGB);
        loadAndStart(kernel1, proxyName, proxyGB);
        loadAndStart(kernel1, node1Name, node1GB);
        
        // Set-up the second ServerNode.
        kernel2 = new Kernel("test.kernel2", "test");
        kernel2.boot();
        
        node2Name = new ObjectName("geronimo.test:role=node2");
        GBeanMBean node2GB = new GBeanMBean(NodeImpl.GBEAN_INFO);
        node2GB.setAttribute("NodeInfo", node2Info);
        clientName = new ObjectName("geronimo.test:role=client");
        GBeanMBean clientGB = new GBeanMBean(GFileManagerClient.GBEAN_INFO);
        clientGB.setAttribute("Name", "FileSystem1");
        clientGB.setAttribute("HostingNode", node1Info);
        clientGB.setReferencePatterns("Node",
            Collections.singleton(node2Name));
        loadAndStart(kernel2, clientName, clientGB);
        loadAndStart(kernel2, node2Name, node2GB);
        fileManager = (GFileManager) clientGB.getTarget();
        
        Node node = (Node) node2GB.getTarget();
        // The second ServerNode joins the first one.
        node.join(node1Info);
        
        Topology topology = new Topology();
        PathWeight weight = new PathWeight(10);
        NodePath path = new NodePath(node1Info, node2Info, weight, weight);
        topology.addPath(path);

        kernel1.setAttribute(node1Name, "Topology", topology);
        kernel2.setAttribute(node2Name, "Topology", topology);
    }
    
}
