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

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Collections;
import java.util.List;

import javax.management.ObjectName;

import junit.framework.TestCase;

import org.apache.geronimo.datastore.impl.remote.messaging.Topology.NodePath;
import org.apache.geronimo.datastore.impl.remote.messaging.Topology.PathWeight;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/03/24 11:37:06 $
 */
public class NodeTest
    extends TestCase
{

    private Kernel kernel1;
    private ObjectName node1Name;
    private ObjectName dummy1Node1Name;
    private DummyConnector dummy1Node1;
    private ObjectName dummy11Node1Name;
    private DummyConnector dummy11Node1;
    
    private Kernel kernel2;
    private ObjectName node2Name;
    private ObjectName dummy1Node2Name;
    private DummyConnector dummy1Node2;
    private ObjectName dummy11Node2Name;
    private DummyConnector dummy11Node2;
    
    private Kernel kernel3;
    private ObjectName node3Name;

    private Kernel kernel4;
    private ObjectName node4Name;
    private ObjectName dummy1Node4Name;
    private DummyConnector dummy1Node4;
    
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
        NodeInfo nodeInfo1 = new NodeInfo("Node1", address, 8081);
        NodeInfo nodeInfo2 = new NodeInfo("Node2", address, 8082);
        NodeInfo nodeInfo3 = new NodeInfo("Node3", address, 8083);
        NodeInfo nodeInfo4 = new NodeInfo("Node4", address, 8084);

        // Set-up the first ServerNode.
        kernel1 = new Kernel("test.kernel1", "test");
        kernel1.boot();

        node1Name = new ObjectName("geronimo.test:role=node1");
        GBeanMBean node1 = new GBeanMBean(NodeImpl.GBEAN_INFO);
        node1.setAttribute("NodeInfo", nodeInfo1);
        dummy1Node1Name = new ObjectName("geronimo.test:name=dummy1");
        GBeanMBean dummy1Node1GB = new GBeanMBean(DummyConnector.GBEAN_INFO);
        dummy1Node1GB.setReferencePatterns("Node",
            Collections.singleton(node1Name));
        dummy1Node1GB.setAttribute("Name", "dummy1");
        dummy1Node1GB.setAttribute("TargetNodes",
            new NodeInfo[] {nodeInfo2, nodeInfo4});
        dummy11Node1Name = new ObjectName("geronimo.test:name=dummy11");
        GBeanMBean dummy11Node1GB = new GBeanMBean(DummyConnector.GBEAN_INFO);
        dummy11Node1GB.setReferencePatterns("Node",
            Collections.singleton(node1Name));
        dummy11Node1GB.setAttribute("Name", "dummy11");
        dummy11Node1GB.setAttribute("TargetNodes", new NodeInfo[] {nodeInfo2});
        loadAndStart(kernel1, node1Name, node1);
        loadAndStart(kernel1, dummy1Node1Name, dummy1Node1GB);
        loadAndStart(kernel1, dummy11Node1Name, dummy11Node1GB);
        dummy1Node1 = (DummyConnector) dummy1Node1GB.getTarget();
        dummy11Node1 = (DummyConnector) dummy11Node1GB.getTarget();
        
        // Set-up the second ServerNode.
        kernel2 = new Kernel("test.kernel2", "test");
        kernel2.boot();

        node2Name = new ObjectName("geronimo.test:role=node2");
        GBeanMBean node2 = new GBeanMBean(NodeImpl.GBEAN_INFO);
        node2.setAttribute("NodeInfo", nodeInfo2);
        dummy1Node2Name = new ObjectName("geronimo.test:name=dummy1");
        GBeanMBean dummy1Node2GB = new GBeanMBean(DummyConnector.GBEAN_INFO);
        dummy1Node2GB.setReferencePatterns("Node",
            Collections.singleton(node2Name));
        dummy1Node2GB.setAttribute("Name", "dummy1");
        dummy1Node2GB.setAttribute("TargetNodes",
                new NodeInfo[] {nodeInfo1, nodeInfo4});
        dummy11Node2Name = new ObjectName("geronimo.test:name=dummy11");
        GBeanMBean dummy11Node2GB = new GBeanMBean(DummyConnector.GBEAN_INFO);
        dummy11Node2GB.setReferencePatterns("Node",
            Collections.singleton(node2Name));
        dummy11Node2GB.setAttribute("Name", "dummy11");
        dummy11Node2GB.setAttribute("TargetNodes", new NodeInfo[] {nodeInfo1});
        loadAndStart(kernel2, node2Name, node2);
        loadAndStart(kernel2, dummy1Node2Name, dummy1Node2GB);
        loadAndStart(kernel2, dummy11Node2Name, dummy11Node2GB);
        dummy1Node2 = (DummyConnector) dummy1Node2GB.getTarget();
        dummy11Node2 = (DummyConnector) dummy11Node2GB.getTarget();

        Node node = (Node) node2.getTarget();
        // The second ServerNode joins the first one.
        node.join(nodeInfo1);

        // Set-up the third ServerNode.
        kernel3 = new Kernel("test.kernel3", "test");
        kernel3.boot();
        
        node3Name = new ObjectName("geronimo.test:role=node3");
        GBeanMBean node3 = new GBeanMBean(NodeImpl.GBEAN_INFO);
        node3.setAttribute("NodeInfo", nodeInfo3);
        loadAndStart(kernel3, node3Name, node3);

        node = (NodeImpl) node3.getTarget();
        // The third ServerNode joins the second one.
        node.join(nodeInfo2);

        // Set-up the fourth ServerNode.
        kernel4 = new Kernel("test.kernel4", "test");
        kernel4.boot();
        
        node4Name = new ObjectName("geronimo.test:role=node4");
        GBeanMBean node4 = new GBeanMBean(NodeImpl.GBEAN_INFO);
        dummy1Node4Name = new ObjectName("geronimo.test:name=dummy1");
        GBeanMBean dummy1Node4GB = new GBeanMBean(DummyConnector.GBEAN_INFO);
        dummy1Node4GB.setReferencePatterns("Node",
            Collections.singleton(node4Name));
        dummy1Node4GB.setAttribute("Name", "dummy1");
        dummy1Node4GB.setAttribute("TargetNodes",
                new NodeInfo[] {nodeInfo1, nodeInfo2});
        node4.setAttribute("NodeInfo", nodeInfo4);
        loadAndStart(kernel4, node4Name, node4);
        loadAndStart(kernel4, dummy1Node4Name, dummy1Node4GB);
        dummy1Node4 = (DummyConnector) dummy1Node4GB.getTarget();

        node = (NodeImpl) node4.getTarget();
        // The fourth ServerNode joins the third one.
        node.join(nodeInfo3);

        // Sets the topology.
        Topology topology = new Topology();
        PathWeight weight = new PathWeight(10);
        NodePath path = new NodePath(nodeInfo1, nodeInfo2, weight, weight);
        topology.addPath(path);
        path = new NodePath(nodeInfo2, nodeInfo3, weight, weight);
        topology.addPath(path);
        path = new NodePath(nodeInfo3, nodeInfo4, weight, weight);
        topology.addPath(path);
        
        kernel1.setAttribute(node1Name, "Topology", topology);
        kernel2.setAttribute(node2Name, "Topology", topology);
        kernel3.setAttribute(node3Name, "Topology", topology);
        kernel4.setAttribute(node4Name, "Topology", topology);
    }

    protected void tearDown() throws Exception {
        unloadAndStop(kernel1, node1Name);
        unloadAndStop(kernel1, dummy1Node1Name);
        unloadAndStop(kernel1, dummy11Node1Name);
        kernel1.shutdown();
        
        unloadAndStop(kernel2, node2Name);
        unloadAndStop(kernel2, dummy1Node2Name);
        unloadAndStop(kernel2, dummy11Node2Name);
        kernel2.shutdown();
        
        unloadAndStop(kernel3, node3Name);
        kernel3.shutdown();
        
        unloadAndStop(kernel4, node4Name);
        unloadAndStop(kernel4, dummy1Node4Name);
        kernel4.shutdown();
    }
    
    public void testMulticast() throws Exception {
        dummy1Node1.sendRawObject("Test1");
        List list = dummy1Node2.getReceived();
        assertEquals(1, list.size());
        assertEquals("Test1", list.remove(0));
        list = dummy1Node4.getReceived();
        assertEquals(1, list.size());
        assertEquals("Test1", list.remove(0));
    }

    public static void main(String[] args) throws Exception {
        NodeTest test = new NodeTest();
        test.setUp();
        while ( true ) {
            test.testSendRawPerformance();
        }
    }
    
    public void testSendRawPerformance() throws Exception {
        List list = dummy11Node2.getReceived();
        int iter = 1000;
        long start = System.currentTimeMillis();
        for(int i = 0; i < iter; i++) {
            dummy11Node1.sendRawObject(null);
            assertEquals(1, list.size());
            list.remove(0);
        }
        long end = System.currentTimeMillis();
        System.out.println("#calls={" + iter + "}; Time={" + (end-start) + "}");
        // TODO update when compression is implemented.
        assertTrue((end - start) < 3000);
    }
    
    public void testInputStreamPerformance() throws Exception {
        long nbBytes = 1024 * 1024;
        InputStream in = new DummyInputStream(nbBytes);
        long baseLine = timeRead(in);
        
        in = new DummyInputStream(nbBytes);
        dummy11Node1.sendRawObject(in);
        List list = dummy11Node2.getReceived();
        assertEquals(1, list.size());
        in = (InputStream) list.remove(0);
        long time = timeRead(in);
        System.out.println("#bytes={" + nbBytes +
            "}; Baseline={" + baseLine + "}; Time={" + time + "}");
        // TODO update when compression is implemented.
        assertTrue(baseLine * 100 > time);
    }

    private long timeRead(InputStream anIn)
        throws Exception {
        int read;
        long start = System.currentTimeMillis();
        while ( -1 != (read = anIn.read() ) ) {}
        return System.currentTimeMillis() - start;
    }
    
    private static class DummyInputStream extends InputStream {
        private final long size;
        private long curPos = 0;
        private DummyInputStream(long aSize) {
            size = aSize;
        }
        public int read() throws IOException {
            if ( curPos++ < size ) {
                return 1;
            }
            return -1;
        }
    }
    
}
