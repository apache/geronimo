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

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.messaging.NodeException;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.io.IOContext;
import org.apache.geronimo.messaging.remotenode.NodeServer;
import org.apache.geronimo.messaging.remotenode.RemoteNodeManager;
import org.apache.geronimo.network.SelectorManager;
import org.apache.geronimo.network.protocol.AcceptableProtocol;
import org.apache.geronimo.network.protocol.ProtocolException;
import org.apache.geronimo.network.protocol.ProtocolFactory;
import org.apache.geronimo.network.protocol.ServerSocketAcceptor;
import org.apache.geronimo.network.protocol.ProtocolFactory.AcceptedCallBack;
import org.apache.geronimo.pool.ClockPool;

/**
 * NodeServer implementation.
 *
 * @version $Revision: 1.6 $ $Date: 2004/07/20 00:15:05 $
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

    public void start() throws NodeException {
        if ( null == manager ) {
            throw new IllegalStateException("Manager is not set.");
        }
        log.debug("Starting NodeServer.");
        CallbackSocketProtocol spt = new CallbackSocketProtocol();
        // TODO configurable.
        spt.setTimeout(1000);
        spt.setSelectorManager(selectorManager);

        ProtocolFactory pf = new ProtocolFactory();
        pf.setClockPool(clockPool);
        // TODO configurable.
        pf.setMaxAge(Long.MAX_VALUE);
        pf.setMaxInactivity(1 * 60 * 60 * 1000);
        pf.setReclaimPeriod(500);
        pf.setTemplate(spt);
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
            throw new NodeException("Can not start server", e);
        }
    }

    public void stop() {
        log.info("Stopping NodeServer.");
        try {
            serverSocketAcceptor.drain();
        } catch (Exception e) {
            log.error("Error stopping NodeServer", e);
        }
    }

    public void setRemoteNodeManager(RemoteNodeManager aManager) {
        manager = aManager;
    }
    
    public void accepted(AcceptableProtocol aProtocol)
        throws ProtocolException {
        RemoteNodeJoined remoteNode =
            new RemoteNodeJoined(nodeInfo, ioContext, aProtocol);
        remoteNode.setManager(manager);
        try {
            remoteNode.join();
        } catch (NodeException e) {
            log.error("Can not join node", e);
        }
    }
    
}