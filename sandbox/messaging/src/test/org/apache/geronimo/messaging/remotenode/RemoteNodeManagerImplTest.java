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

package org.apache.geronimo.messaging.remotenode;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.geronimo.messaging.Msg;
import org.apache.geronimo.messaging.MsgHeader;
import org.apache.geronimo.messaging.MsgHeaderConstants;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.NodeTopology;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;
import org.apache.geronimo.messaging.io.IOContext;
import org.apache.geronimo.pool.ClockPool;

/**
 *
 * @version $Rev$ $Date$
 */
public class RemoteNodeManagerImplTest extends TestCase
{
    
    private RemoteNodeManager manager;
    private ClockPool cp;
    
    protected void setUp() throws Exception {
        InetAddress address = InetAddress.getLocalHost();
        NodeInfo nodeInfo1 = new NodeInfo("Node1", address, 8081);
        IOContext ioContext = new IOContext();
        MockMessagingTransportFactory factory = new MockMessagingTransportFactory();
        factory.setUpFactoryServer(new MockNodeServer());

        cp = new ClockPool();
        cp.setPoolName("CP");

        manager = new RemoteNodeManagerImpl(nodeInfo1, ioContext, cp, factory);
        
        NodeTopology topology = new NodeTopology() {
            public int getVersion() {
                return 0;
            }
            public Set getNeighbours(NodeInfo aRoot) {
                return new HashSet();
            }
            public NodeInfo[] getPath(NodeInfo aSource, NodeInfo aTarget) {
                return null;
            }
            public int getIDOfNode(NodeInfo aNodeInfo) {
                throw new UnsupportedOperationException("getVersion");
            }
            public NodeInfo getNodeById(int anId) {
                throw new UnsupportedOperationException("getVersion");
            }
            public Set getNodes() {
                throw new UnsupportedOperationException("getVersion");
            }
        };
        manager.prepareTopology(topology);
        manager.commitTopology();
    }
    
    public void testRegisterRemoteNode() throws Exception {
        InetAddress address = InetAddress.getLocalHost();
        NodeInfo nodeInfo = new NodeInfo("Node1", address, 8081);
        MockRemoteNode remoteNode1 = new MockRemoteNode();
        remoteNode1.setNodeInfo(nodeInfo);
        
        manager.registerRemoteNode(remoteNode1);
        RemoteNode remoteNode2 = manager.findRemoteNode(nodeInfo);
        
        assertEquals(remoteNode1, remoteNode2);
    }

    public void testAddListener() throws Exception {
        InetAddress address = InetAddress.getLocalHost();
        NodeInfo nodeInfo = new NodeInfo("Node1", address, 8081);
        MockRemoteNode remoteNode = new MockRemoteNode();
        remoteNode.setNodeInfo(nodeInfo);

        DummyListener listener = new DummyListener();

        manager.addListener(listener);
        manager.registerRemoteNode(remoteNode);
        
        assertNotNull(listener.event);
        assertTrue(listener.event.isAddEvent());
        assertEquals(remoteNode, listener.event.getRemoteNode());

        manager.unregisterRemoteNode(remoteNode);
        assertNotNull(listener.event);
        assertTrue(listener.event.isRemoveEvent());
        assertEquals(remoteNode, listener.event.getRemoteNode());
    }
    
    private TestGetMsgOutInfo newGetMsgOutInfo() throws Exception {
        final TestGetMsgOutInfo info = new TestGetMsgOutInfo();

        InetAddress address = InetAddress.getLocalHost();
        info.srcNode = new NodeInfo("SrcNode1", address, 8081);
        info.node1 = new NodeInfo("Node1", address, 8081);
        info.node2 = new NodeInfo("Node2", address, 8081);
        
        MockRemoteNode remoteNode1 = new MockRemoteNode();
        remoteNode1.setNodeInfo(info.node1);
        manager.registerRemoteNode(remoteNode1);
        
        MockRemoteNode remoteNode2 = new MockRemoteNode();
        remoteNode2.setNodeInfo(info.node2);
        manager.registerRemoteNode(remoteNode2);

        info.topology = new NodeTopology() {
            public Set getNeighbours(NodeInfo aRoot) {
                Set result = new HashSet();
                result.add(info.node1);
                result.add(info.node2);
                return result;
            }
            public NodeInfo[] getPath(NodeInfo aSource, NodeInfo aTarget) {
                if ( aSource.equals(info.srcNode) && 
                    aTarget.equals(info.node1) ) {
                    return new NodeInfo[] {info.node1};
                } else if ( aSource.equals(info.srcNode) && 
                    aTarget.equals(info.node2) ) {
                    return new NodeInfo[] {info.node2};
                }
                return null;
            }
            public int getIDOfNode(NodeInfo aNodeInfo) {
                throw new UnsupportedOperationException("getIDOfNode");
            }
            public NodeInfo getNodeById(int anId) {
                throw new UnsupportedOperationException("getNodeById");
            }
            public Set getNodes() {
                throw new UnsupportedOperationException("getNodes");
            }
            public int getVersion() {
                return 1;
            }
        };
        // Test that Msg are successfully routed within the context of
        // a prepared topology.
        manager.prepareTopology(info.topology);
        
        info.remoteNode1 = remoteNode1;
        info.remoteNode2 = remoteNode2;
        return info;
    }

    /**
     * Test that Msg are successfully routed within the context of a prepared 
     * topology.
     */
    public void testPreparedGetMsgOut() throws Exception {
        TestGetMsgOutInfo info = newGetMsgOutInfo();

        MsgOutInterceptor out = manager.getMsgConsumerOut();
        Msg msg = new Msg();
        MsgHeader header = msg.getHeader();
        Integer id = new Integer(1234);
        header.addHeader(MsgHeaderConstants.CORRELATION_ID, id);
        header.addHeader(MsgHeaderConstants.SRC_NODE, info.srcNode);
        header.addHeader(MsgHeaderConstants.DEST_NODES, info.node1);
        header.addHeader(MsgHeaderConstants.TOPOLOGY_VERSION,
            new Integer(info.topology.getVersion()));
        out.push(msg);
        
        List receivedMsgs = info.remoteNode1.getPushedMsg();
        assertEquals(1, receivedMsgs.size());
        msg = (Msg) receivedMsgs.get(0);
        assertEquals(id, msg.getHeader().getHeader(MsgHeaderConstants.CORRELATION_ID));
        receivedMsgs.clear();
        
        receivedMsgs = info.remoteNode2.getPushedMsg();
        assertEquals(0, receivedMsgs.size());
        
        msg = new Msg();
        header = msg.getHeader();
        header.addHeader(MsgHeaderConstants.CORRELATION_ID, id);
        header.addHeader(MsgHeaderConstants.SRC_NODE, info.srcNode);
        header.addHeader(MsgHeaderConstants.DEST_NODES, info.node2);
        header.addHeader(MsgHeaderConstants.TOPOLOGY_VERSION,
            new Integer(info.topology.getVersion()));
        out.push(msg);
        
        receivedMsgs = info.remoteNode2.getPushedMsg();
        assertEquals(1, receivedMsgs.size());
        msg = (Msg) receivedMsgs.get(0);
        assertEquals(id, msg.getHeader().getHeader(MsgHeaderConstants.CORRELATION_ID));
        receivedMsgs.clear();
        
        receivedMsgs = info.remoteNode1.getPushedMsg();
        assertEquals(0, receivedMsgs.size());
    }
    
    /**
     * Test that Msg are successfully routed within the context of a committed 
     * topology.
     */
    public void testCommittedGetMsgOut() throws Exception {
        TestGetMsgOutInfo info = newGetMsgOutInfo();
        
        manager.commitTopology();

        MsgOutInterceptor out = manager.getMsgConsumerOut();
        Msg msg = new Msg();
        MsgHeader header = msg.getHeader();
        Integer id = new Integer(1234);
        header.addHeader(MsgHeaderConstants.CORRELATION_ID, id);
        header.addHeader(MsgHeaderConstants.SRC_NODE, info.srcNode);
        header.addHeader(MsgHeaderConstants.DEST_NODES, info.node1);
        out.push(msg);
        
        List receivedMsgs = info.remoteNode1.getPushedMsg();
        assertEquals(1, receivedMsgs.size());
        msg = (Msg) receivedMsgs.get(0);
        assertEquals(id, msg.getHeader().getHeader(MsgHeaderConstants.CORRELATION_ID));
        receivedMsgs.clear();
        
        receivedMsgs = info.remoteNode2.getPushedMsg();
        assertEquals(0, receivedMsgs.size());
        
        msg = new Msg();
        header = msg.getHeader();
        header.addHeader(MsgHeaderConstants.CORRELATION_ID, id);
        header.addHeader(MsgHeaderConstants.SRC_NODE, info.srcNode);
        header.addHeader(MsgHeaderConstants.DEST_NODES, info.node2);
        out.push(msg);
        
        receivedMsgs = info.remoteNode2.getPushedMsg();
        assertEquals(1, receivedMsgs.size());
        msg = (Msg) receivedMsgs.get(0);
        assertEquals(id, msg.getHeader().getHeader(MsgHeaderConstants.CORRELATION_ID));
        receivedMsgs.clear();
        
        receivedMsgs = info.remoteNode1.getPushedMsg();
        assertEquals(0, receivedMsgs.size());
    }
    
    private class DummyListener implements RemoteNodeEventListener {
        private RemoteNodeEvent event;
        public void fireRemoteNodeEvent(RemoteNodeEvent anEvent) {
            event = anEvent;
        }
    }
    
    private class TestGetMsgOutInfo {
        private NodeTopology topology;
        private MockRemoteNode remoteNode1;
        private MockRemoteNode remoteNode2;
        private NodeInfo srcNode;
        private NodeInfo node1;
        private NodeInfo node2;
    }
    
}
