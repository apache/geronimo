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

package org.apache.geronimo.messaging;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.geronimo.messaging.remotenode.MessagingTransportFactory;
import org.apache.geronimo.messaging.remotenode.network.NetworkTransportFactory;
import org.apache.geronimo.network.SelectorManager;
import org.apache.geronimo.system.ClockPool;
import org.apache.geronimo.system.ThreadPool;

/**
 *
 * @version $Revision: 1.4 $ $Date: 2004/07/05 07:03:50 $
 */
public class NodeImplTest
    extends TestCase
{

    private ProtocolContext ctx1;
    private Node node1;
    private MockEndPoint endPoint11;
    private MockEndPoint endPoint12;
    
    private ProtocolContext ctx2;
    private Node node2;
    private MockEndPoint endPoint21;
    private MockEndPoint endPoint22;
    
    private ProtocolContext ctx3;
    private Node node3;
    
    private ProtocolContext ctx4;
    private Node node4;
    private MockEndPoint endPoint41;
    
    protected void setUp() throws Exception {
        InetAddress address = InetAddress.getLocalHost();
        final NodeInfo nodeInfo1 = new NodeInfo("Node1", address, 8081);
        final NodeInfo nodeInfo2 = new NodeInfo("Node2", address, 8082);
        final NodeInfo nodeInfo3 = new NodeInfo("Node3", address, 8083);
        final NodeInfo nodeInfo4 = new NodeInfo("Node4", address, 8084);

        // Set-up the first Node.
        ctx1 = new ProtocolContext();
        ctx1.init("Node1");
        ctx1.start();
        node1 = new NodeImpl(nodeInfo1, ctx1.tp, ctx1.cp, ctx1.factoryTransport());
        endPoint11 = new MockEndPointImpl(node1, "dummy1",
            new NodeInfo[] {nodeInfo2, nodeInfo4});
        endPoint12 = new MockEndPointImpl(node1, "dummy11",
            new NodeInfo[] {nodeInfo2}); 
        node1.doStart();
        endPoint11.doStart();
        endPoint12.doStart();
        
        // Set-up the second Node.
        ctx2 = new ProtocolContext();
        ctx2.init("Node2");
        ctx2.start();
        node2 = new NodeImpl(nodeInfo2, ctx2.tp, ctx2.cp, ctx2.factoryTransport());
        endPoint21 = new MockEndPointImpl(node2, "dummy1",
            new NodeInfo[] {nodeInfo1, nodeInfo4}); 
        endPoint22 = new MockEndPointImpl(node2, "dummy11",
            new NodeInfo[] {nodeInfo1}); 
        node2.doStart();
        endPoint21.doStart();
        endPoint22.doStart();

        // Set-up the third Node.
        ctx3 = new ProtocolContext();
        ctx3.init("Node3");
        ctx3.start();
        node3 = new NodeImpl(nodeInfo3, ctx3.tp, ctx3.cp, ctx3.factoryTransport());
        node3.doStart();
        
        // Set-up the fourth Node.
        ctx4 = new ProtocolContext();
        ctx4.init("Node4");
        ctx4.start();
        node4 = new NodeImpl(nodeInfo4, ctx4.tp, ctx4.cp, ctx4.factoryTransport());
        endPoint41 = new MockEndPointImpl(node4, "dummy1",
            new NodeInfo[] {nodeInfo1, nodeInfo2}); 
        node4.doStart();
        endPoint41.doStart();
        
        // Sets the topology.
        NodeTopology topology =
            new MockTopology(nodeInfo1, nodeInfo2, nodeInfo3, nodeInfo4);
        node1.setTopology(topology);
    }

    protected void tearDown() throws Exception {
        endPoint41.doStop();
        node4.doStop();
        ctx4.stop();
        
        node3.doStop();
        ctx3.stop();
        
        endPoint22.doStop();
        endPoint21.doStop();
        node2.doStop();
        ctx2.stop();
        
        endPoint11.doStop();
        endPoint21.doStop();
        node1.doStop();
        ctx1.stop();
    }
    
    public void testMulticast() throws Exception {
        endPoint11.sendRawObject("Test1");
        List list = endPoint21.getReceived();
        assertEquals(1, list.size());
        assertEquals("Test1", list.remove(0));
        list = endPoint41.getReceived();
        assertEquals(1, list.size());
        assertEquals("Test1", list.remove(0));
    }

    public static void main(String[] args) throws Exception {
        NodeImplTest test = new NodeImplTest();
        test.setUp();
        test.testSendRawPerformance();
    }
    
    public void testSendRawPerformance() throws Exception {
        List list = endPoint12.getReceived();
        int iter = 10;
        long start = System.currentTimeMillis();
        for(int i = 0; i < iter; i++) {
            endPoint22.sendRawObject(null);
            assertEquals(1, list.size());
            list.remove(0);
        }
        long end = System.currentTimeMillis();
        System.out.println("#calls={" + iter + "}; Time={" + (end-start) + "}");
    }
    
    public void testInputStreamPerformance() throws Exception {
        long nbBytes = 1024 * 10 + 10;
        InputStream in = new DummyInputStream(nbBytes);
        long baseLine = timeRead(in);
        
        in = new DummyInputStream(nbBytes);
        endPoint12.sendRawObject(in);
        List list = endPoint22.getReceived();
        assertEquals(1, list.size());
        in = (InputStream) list.remove(0);
        long time = timeRead(in);
        System.out.println("#bytes={" + nbBytes +
            "}; Baseline={" + baseLine + "}; Time={" + time + "}");
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

    private static class  ProtocolContext {
        private ThreadPool tp;
        private ClockPool cp;
        private SelectorManager sm;
        public MessagingTransportFactory factoryTransport() {
            return new NetworkTransportFactory(sm, cp);
        }
        public void init(String aName) throws Exception {
            tp = new ThreadPool();
            tp.setKeepAliveTime(1 * 1000);
            tp.setMinimumPoolSize(5);
            tp.setMaximumPoolSize(25);
            tp.setPoolName("TP " + aName);

            cp = new ClockPool();
            cp.setPoolName("CP " + aName);

            sm = new SelectorManager();
            sm.setThreadPool(tp);
            sm.setThreadName("SM " + aName);
            sm.setTimeout(500);
        }
        public void start() throws Exception {
            tp.doStart();
            cp.doStart();
            sm.doStart();
        }
        public void stop() throws Exception {
            sm.doStop();
            cp.doStop();
            tp.doStop();
        }
    }
 
    private static class MockTopology implements NodeTopology {

        private final NodeInfo nodeInfo1;
        private final NodeInfo nodeInfo2;
        private final NodeInfo nodeInfo3;
        private final NodeInfo nodeInfo4;

        private MockTopology(NodeInfo aNodeInfo1,
            NodeInfo aNodeInfo2,
            NodeInfo aNodeInfo3,
            NodeInfo aNodeInfo4) {
            nodeInfo1 = aNodeInfo1;
            nodeInfo2 = aNodeInfo2;
            nodeInfo3 = aNodeInfo3;
            nodeInfo4 = aNodeInfo4;
        }
        
        public Set getNeighbours(NodeInfo aRoot) {
            Set result = new HashSet();
            if ( aRoot.equals(nodeInfo1) ) {
                result.add(nodeInfo2);
            } else if ( aRoot.equals(nodeInfo2) ) {
                result.add(nodeInfo3);
            } else if ( aRoot.equals(nodeInfo3) ) {
                result.add(nodeInfo4);
            } else if ( aRoot.equals(nodeInfo4) ) {
            } else {
                throw new IllegalArgumentException("Not expected");
            }
            return result;
        }
        public NodeInfo[] getPath(NodeInfo aSource, NodeInfo aTarget) {
            if ( aSource.equals(nodeInfo1) && aTarget.equals(nodeInfo2) ) {
                return new NodeInfo[] {nodeInfo2};
            } else if ( aSource.equals(nodeInfo2) && aTarget.equals(nodeInfo1) ) {
                return new NodeInfo[] {nodeInfo1};
            } else if ( aSource.equals(nodeInfo2) && aTarget.equals(nodeInfo3) ) {
                return new NodeInfo[] {nodeInfo3};
            } else if ( aSource.equals(nodeInfo3) && aTarget.equals(nodeInfo2) ) {
                return new NodeInfo[] {nodeInfo2};
            } else if ( aSource.equals(nodeInfo3) && aTarget.equals(nodeInfo4) ) {
                return new NodeInfo[] {nodeInfo4};
            } else if ( aSource.equals(nodeInfo4) && aTarget.equals(nodeInfo3) ) {
                return new NodeInfo[] {nodeInfo3};
            } else if ( aSource.equals(nodeInfo1) && aTarget.equals(nodeInfo4) ) {
                return new NodeInfo[] {nodeInfo2, nodeInfo3, nodeInfo4};
            } else if ( aSource.equals(nodeInfo4) && aTarget.equals(nodeInfo1) ) {
                return new NodeInfo[] {nodeInfo3, nodeInfo2, nodeInfo1};
            }
            throw new IllegalArgumentException("Not expected");
        }
        public int getIDOfNode(NodeInfo aNodeInfo) {
            if ( aNodeInfo.equals(nodeInfo1) ) {
                return 1;
            } else if ( aNodeInfo.equals(nodeInfo2) ) {
                return 2;
            } else if ( aNodeInfo.equals(nodeInfo3) ) {
                return 3;
            } else if ( aNodeInfo.equals(nodeInfo4) ) {
                return 4;
            }
            throw new IllegalArgumentException("Not expected");
        }
        public NodeInfo getNodeById(int anId) {
            switch (anId) {
                case 1:
                    return nodeInfo1;
                case 2:
                    return nodeInfo2;
                case 3:
                    return nodeInfo3;
                case 4:
                    return nodeInfo4;
                default:
                    throw new IllegalArgumentException("Not expected");
            }
        }
        public Set getNodes() {
            throw new IllegalArgumentException("Not expected");
        }
        public int getVersion() {
            return 0;
        }
    }
    
}
