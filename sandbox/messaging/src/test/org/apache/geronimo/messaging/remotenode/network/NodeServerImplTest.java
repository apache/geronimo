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

package org.apache.geronimo.messaging.remotenode.network;

import java.net.InetAddress;

import junit.framework.TestCase;

import org.apache.geronimo.messaging.Msg;
import org.apache.geronimo.messaging.MsgBody;
import org.apache.geronimo.messaging.MsgHeader;
import org.apache.geronimo.messaging.MsgHeaderConstants;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;
import org.apache.geronimo.messaging.io.MockStreamManager;
import org.apache.geronimo.messaging.io.IOContext;
import org.apache.geronimo.messaging.remotenode.MessagingTransportFactory;
import org.apache.geronimo.messaging.remotenode.RemoteNodeManager;
import org.apache.geronimo.messaging.remotenode.RemoteNodeManagerImpl;
import org.apache.geronimo.messaging.remotenode.network.NodeServerImpl;
import org.apache.geronimo.messaging.remotenode.network.NetworkTransportFactory;
import org.apache.geronimo.network.SelectorManager;
import org.apache.geronimo.system.ClockPool;
import org.apache.geronimo.system.ThreadPool;

/**
 *
 * @version $Revision: 1.2 $ $Date: 2004/05/27 15:41:14 $
 */
public class NodeServerImplTest extends TestCase {

    private ThreadPool tp;
    private ClockPool cp;
    private SelectorManager sm;
    private Object tempo;
    
    protected void setUp() throws Exception {
        tempo = new Object();
        
        tp = new ThreadPool();
        tp.setKeepAliveTime(1 * 1000);
        tp.setMinimumPoolSize(5);
        tp.setMaximumPoolSize(25);
        tp.setPoolName("TP");
        tp.doStart();

        cp = new ClockPool();
        cp.setPoolName("CP");
        cp.doStart();

        sm = new SelectorManager();
        sm.setThreadPool(tp);
        sm.setThreadName("SM");
        sm.setTimeout(500);
        sm.doStart();
    }
    
    protected void tearDown() throws Exception {
        sm.doStop();
        cp.doStop();
        tp.doStop();
    }
    
    public void testHandshake() throws Exception {
        InetAddress address = InetAddress.getLocalHost();
        NodeInfo nodeInfo1 = new NodeInfo("Node1", address, 8081);
        
        IOContext ioContext = new IOContext();
        ioContext.setPopSynchronization(null);
        ioContext.setPushSynchronization(null);
        ioContext.setReplacerResolver(null);
        ioContext.setStreamManager(new MockStreamManager());

        MessagingTransportFactory factory =
            new NetworkTransportFactory(sm, cp);
        
        RemoteNodeManager manager =
            new RemoteNodeManagerImpl(nodeInfo1, ioContext, factory);
        
        NodeServerImpl serverImpl =
            new NodeServerImpl(nodeInfo1, ioContext, sm, cp);
        serverImpl.setRemoteNodeManager(manager);
        serverImpl.start();

        DummyDispatcher dispatcher = new DummyDispatcher();
        RemoteNodeJoiner remoteNode =
            new RemoteNodeJoiner(nodeInfo1, ioContext, factory);
        remoteNode.setMsgProducerOut(dispatcher);
        remoteNode.connect();
        
        NodeInfo nodeInfo2 = new NodeInfo("Node2", address, 8082);
        Msg msg = new Msg();
        MsgHeader header = msg.getHeader();
        header.addHeader(MsgHeaderConstants.CORRELATION_ID, "");
        header.addHeader(MsgHeaderConstants.DEST_ENDPOINT, "");
        header.addHeader(MsgHeaderConstants.DEST_NODE, "");
        header.addHeader(MsgHeaderConstants.DEST_NODES, "");
        header.addHeader(MsgHeaderConstants.SRC_ENDPOINT, "");
        header.addHeader(MsgHeaderConstants.SRC_NODE, "");
        header.addHeader(MsgHeaderConstants.BODY_TYPE, MsgBody.Type.RESPONSE);
        msg.getBody().setContent(nodeInfo2);
        remoteNode.getMsgConsumerOut().push(msg);
        
        synchronized(tempo) {
            tempo.wait(1000);
        }
        assertNotNull(dispatcher.msg);
        assertEquals(Boolean.TRUE, dispatcher.msg.getBody().getContent());
        assertNotNull(manager.findRemoteNode(nodeInfo2));
    }

    private class DummyDispatcher implements MsgOutInterceptor {
        private Msg msg;
        public void push(Msg aMsg) {
            msg = aMsg;
            synchronized(tempo) {
                tempo.notify();
            }
        }
    }
    
}
