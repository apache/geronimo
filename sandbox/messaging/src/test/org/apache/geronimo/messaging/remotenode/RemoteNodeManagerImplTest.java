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
import java.util.List;

import junit.framework.TestCase;

import org.apache.geronimo.messaging.Msg;
import org.apache.geronimo.messaging.MsgHeader;
import org.apache.geronimo.messaging.MsgHeaderConstants;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.NodeTopology;
import org.apache.geronimo.messaging.NodeTopology.NodePath;
import org.apache.geronimo.messaging.NodeTopology.PathWeight;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;
import org.apache.geronimo.messaging.io.IOContext;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:43 $
 */
public class RemoteNodeManagerImplTest extends TestCase
{
    
    private RemoteNodeManager manager;
    
    protected void setUp() throws Exception {
        InetAddress address = InetAddress.getLocalHost();
        NodeInfo nodeInfo1 = new NodeInfo("Node1", address, 8081);
        IOContext ioContext = new IOContext();
        MockMessagingTransportFactory factory = new MockMessagingTransportFactory();
        factory.setUpFactoryServer(new MockNodeServer());
        
        manager = new RemoteNodeManagerImpl(nodeInfo1, ioContext, factory);
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
    
    public void testGetMsgOut() throws Exception {
        InetAddress address = InetAddress.getLocalHost();
        NodeInfo srcNodeInfo = new NodeInfo("SrcNode1", address, 8081);
        NodeInfo nodeInfo1 = new NodeInfo("Node1", address, 8081);
        NodeInfo nodeInfo2 = new NodeInfo("Node2", address, 8081);
        NodePath path = new NodePath(srcNodeInfo, nodeInfo1,
            new PathWeight(10), new PathWeight(10));
        NodeTopology topology = new NodeTopology();
        topology.addPath(path);
        path = new NodePath(srcNodeInfo, nodeInfo2,
            new PathWeight(10), new PathWeight(10));
        topology.addPath(path);
        
        manager.setTopology(topology);
        
        MockRemoteNode remoteNode1 = new MockRemoteNode();
        remoteNode1.setNodeInfo(nodeInfo1);
        manager.registerRemoteNode(remoteNode1);
        
        MockRemoteNode remoteNode2 = new MockRemoteNode();
        remoteNode2.setNodeInfo(nodeInfo2);
        manager.registerRemoteNode(remoteNode2);

        MsgOutInterceptor out = manager.getMsgConsumerOut();
        Msg msg = new Msg();
        MsgHeader header = msg.getHeader();
        Integer id = new Integer(1234);
        header.addHeader(MsgHeaderConstants.CORRELATION_ID, id);
        header.addHeader(MsgHeaderConstants.SRC_NODE, srcNodeInfo);
        header.addHeader(MsgHeaderConstants.DEST_NODES, nodeInfo1);
        out.push(msg);
        
        List receivedMsgs = remoteNode1.getPushedMsg();
        assertEquals(1, receivedMsgs.size());
        msg = (Msg) receivedMsgs.get(0);
        assertEquals(id, msg.getHeader().getHeader(MsgHeaderConstants.CORRELATION_ID));
        receivedMsgs.clear();
        
        receivedMsgs = remoteNode2.getPushedMsg();
        assertEquals(0, receivedMsgs.size());
        
        msg = new Msg();
        header = msg.getHeader();
        header.addHeader(MsgHeaderConstants.CORRELATION_ID, id);
        header.addHeader(MsgHeaderConstants.SRC_NODE, srcNodeInfo);
        header.addHeader(MsgHeaderConstants.DEST_NODES, nodeInfo2);
        out.push(msg);
        
        receivedMsgs = remoteNode2.getPushedMsg();
        assertEquals(1, receivedMsgs.size());
        msg = (Msg) receivedMsgs.get(0);
        assertEquals(id, msg.getHeader().getHeader(MsgHeaderConstants.CORRELATION_ID));
        receivedMsgs.clear();
        
        receivedMsgs = remoteNode1.getPushedMsg();
        assertEquals(0, receivedMsgs.size());
    }
    
    private class DummyListener implements RemoteNodeEventListener {
        private RemoteNodeEvent event;
        public void fireRemoteNodeEvent(RemoteNodeEvent anEvent) {
            event = anEvent;
        }
    }
    
}
