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

import java.io.IOException;
import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.messaging.CommunicationException;
import org.apache.geronimo.messaging.Msg;
import org.apache.geronimo.messaging.MsgBody;
import org.apache.geronimo.messaging.MsgHeader;
import org.apache.geronimo.messaging.MsgHeaderConstants;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;
import org.apache.geronimo.messaging.io.IOContext;
import org.apache.geronimo.messaging.remotenode.NodeServer;
import org.apache.geronimo.messaging.remotenode.RemoteNode;
import org.apache.geronimo.messaging.remotenode.RemoteNodeConnection;
import org.apache.geronimo.messaging.remotenode.RemoteNodeManager;
import org.apache.geronimo.network.SelectorManager;
import org.apache.geronimo.network.protocol.AcceptableProtocol;
import org.apache.geronimo.network.protocol.AcceptableProtocolStack;
import org.apache.geronimo.network.protocol.BufferProtocol;
import org.apache.geronimo.network.protocol.ProtocolException;
import org.apache.geronimo.network.protocol.ProtocolFactory;
import org.apache.geronimo.network.protocol.ServerSocketAcceptor;
import org.apache.geronimo.network.protocol.SocketProtocol;
import org.apache.geronimo.network.protocol.ProtocolFactory.AcceptedCallBack;
import org.apache.geronimo.system.ClockPool;

/**
 * NodeServer implementation.
 *
 * @version $Revision: 1.2 $ $Date: 2004/05/27 14:27:32 $
 */
public class NodeServerImpl
    implements NodeServer, AcceptedCallBack
{

    private static final Log log = LogFactory.getLog(NodeServerImpl.class);

    private final NodeInfo nodeInfo;
    
    private final SelectorManager selectorManager;
    
    private final ClockPool clockPool;
    
    private final ServerSocketAcceptor serverSocketAcceptor;
    
    private final IOContext ioContext;
    
    private RemoteNodeManager manager;
    
    public NodeServerImpl(NodeInfo aNodeInfo,
        IOContext anIOContext,
        SelectorManager aSelectorManager, ClockPool aClockPool) {
        if ( null == aNodeInfo ) {
            throw new IllegalArgumentException("NodeInfo is required.");
        } else if ( null == anIOContext ) {
            throw new IllegalArgumentException("IOContex is required.");
        } else if ( null == aSelectorManager ) {
            throw new IllegalArgumentException("SelectorManager is required.");
        } else if ( null == aClockPool ) {
            throw new IllegalArgumentException("ClockPool is required.");
        }
        nodeInfo = aNodeInfo;
        ioContext = anIOContext;
        selectorManager = aSelectorManager;
        clockPool = aClockPool;
        
        serverSocketAcceptor = new ServerSocketAcceptor();
        serverSocketAcceptor.setSelectorManager(selectorManager);
    }

    public void start() throws IOException, CommunicationException {
        log.info("Starting NodeServer.");
        AcceptableProtocolStack stack = new AcceptableProtocolStack();
        
        SocketProtocol spt = new SocketProtocol();
        // TODO configurable.
        spt.setTimeout(10 * 1000);
        spt.setSelectorManager(selectorManager);
        stack.push(spt);

        BufferProtocol buffpt = new BufferProtocol();
        buffpt.setThreadPool(selectorManager.getThreadPool());
        stack.push(buffpt);
        
        ProtocolFactory pf = new ProtocolFactory();
        pf.setClockPool(clockPool);
        // TODO configurable.
        pf.setMaxAge(Long.MAX_VALUE);
        pf.setMaxInactivity(1 * 60 * 60 * 1000);
        pf.setReclaimPeriod(10 * 1000);
        pf.setTemplate(stack);
        pf.setAcceptedCallBack(this);

        serverSocketAcceptor.setAcceptorListener(pf);
        serverSocketAcceptor.setReuseAddress(true);
        
        try {
            URI bindURI = new URI("async",
                null,
                nodeInfo.getAddress().getHostName(),
                nodeInfo.getPort(),
            	"", null, null);
            serverSocketAcceptor.setUri(bindURI);
            serverSocketAcceptor.startup();
        } catch (Exception e) {
            IOException exception = new IOException("Can not start.");
            exception.initCause(e);
            throw exception;
        }
    }

    public void stop() throws IOException, CommunicationException {
        log.info("Stopping NodeServer.");
        try {
            serverSocketAcceptor.drain();
        } catch (Exception e) {
            IOException exception = new IOException("Can not stop.");
            exception.initCause(e);
            throw exception;
        }
    }

    public void setRemoteNodeManager(RemoteNodeManager aManager) {
        manager = aManager;
    }
    
    public void accepted(AcceptableProtocol aProtocol)
        throws ProtocolException {
        new RemoteNodeInitializer(aProtocol);
    }
    
    private class RemoteNodeInitializer implements MsgOutInterceptor {
        private final RemoteNodeConnection connection;
        private RemoteNodeInitializer(AcceptableProtocol aProtocol)
            throws ProtocolException {
            connection =
                new RemoteNodeJoinedConnection(ioContext, aProtocol);
            try {
                connection.open();
            } catch (IOException e) {
                throw new ProtocolException(e);
            } catch (CommunicationException e) {
                throw new ProtocolException(e);
            }
            connection.setMsgProducerOut(this);
        }
        
        public void push(Msg aMsg) {
            MsgBody body = aMsg.getBody();
            NodeInfo otherNodeInfo = (NodeInfo) body.getContent();
            
            RemoteNode remoteNode = manager.findRemoteNode(otherNodeInfo);
            if ( null == remoteNode ) {
                remoteNode = new RemoteNodeJoined(otherNodeInfo, ioContext);
                manager.registerRemoteNode(remoteNode);
            }
            remoteNode.addConnection(connection);
            
            Msg msg = aMsg.reply();
            msg.getBody().setContent(Boolean.TRUE);
            connection.getMsgConsumerOut().push(msg);
        }

    }
    
}