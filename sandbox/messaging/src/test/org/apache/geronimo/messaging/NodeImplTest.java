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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.geronimo.messaging.remotenode.MessagingTransportFactory;
import org.apache.geronimo.messaging.remotenode.network.NetworkTransportFactory;
import org.apache.geronimo.network.SelectorManager;
import org.apache.geronimo.pool.ClockPool;
import org.apache.geronimo.pool.ThreadPool;

/**
 *
 * @version $Revision: 1.7 $ $Date: 2004/07/20 00:26:03 $
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
        node3.setTopology(topology);
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
        test.tearDown();
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
            tp.setPoolSize(10);
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

        private final NodeInfo[] nodesInfo;
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
            nodesInfo = new NodeInfo[] {
                nodeInfo1, nodeInfo2, nodeInfo3, nodeInfo4};
        }
        
        public Set getNeighbours(NodeInfo aRoot) {
            Set result = new HashSet();
            if ( aRoot.equals(nodesInfo[0]) ) {
                result.add(nodesInfo[1]);
                return result;
            } else if ( aRoot.equals(nodesInfo[nodesInfo.length - 1]) ) {
                result.add(nodesInfo[nodesInfo.length - 2]);
                return result;
            }
            for (int i = 1; i < nodesInfo.length - 1; i++) {
                if ( nodesInfo[i].equals(aRoot) ) {
                    result.add(nodesInfo[i - 1]);
                    result.add(nodesInfo[i + 1]);
                    return result;
                }
            }
            throw new IllegalArgumentException("Not expected");
        }
        public NodeInfo[] getPath(NodeInfo aSource, NodeInfo aTarget) {
            boolean isInside = false;
            List result = new ArrayList();
            for (int i = 0; i < nodesInfo.length ; i++) {
                NodeInfo curNode = nodesInfo[i];
                if ( curNode.equals(aSource) ) {
                    if ( isInside ) {
                        Collections.reverse(result);
                        return (NodeInfo[]) result.toArray(new NodeInfo[0]);
                    }
                    isInside = true;
                    continue;
                } else if ( curNode.equals(aTarget) ) {
                    if ( isInside ) {
                        result.add(curNode);
                        return (NodeInfo[]) result.toArray(new NodeInfo[0]);
                    }
                    isInside = true;
                    result.add(curNode);
                    continue;
                }
                if ( isInside ) {
                    result.add(nodesInfo[i]);
                }
            }
            throw new IllegalArgumentException("Not expected");
        }
        public int getIDOfNode(NodeInfo aNodeInfo) {
            for (int i = 0; i < nodesInfo.length; i++) {
                if ( nodesInfo[i].equals(aNodeInfo) ) {
                    return i + 1;
                }
            }
            throw new IllegalArgumentException("Not expected");
        }
        public NodeInfo getNodeById(int anId) {
            if ( nodesInfo.length < anId  ) {
                throw new IllegalArgumentException("Not expected");
            }
            return nodesInfo[anId - 1];
        }
        public Set getNodes() {
            throw new IllegalArgumentException("Not expected");
        }
        public int getVersion() {
            return 1;
        }
    }
    
}
