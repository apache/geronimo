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

import java.net.InetSocketAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.io.IOContext;
import org.apache.geronimo.messaging.remotenode.RemoteNodeConnection;
import org.apache.geronimo.network.SelectorManager;
import org.apache.geronimo.network.protocol.BufferProtocol;
import org.apache.geronimo.network.protocol.Protocol;
import org.apache.geronimo.network.protocol.ProtocolException;
import org.apache.geronimo.network.protocol.SocketProtocol;

/**
 * 
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:42 $
 */
public class RemoteNodeJoinerConnection
    extends AbstractRemoteNodeConnection
    implements RemoteNodeConnection
{

    private static final Log log = LogFactory.getLog(RemoteNodeJoinerConnection.class);
    
    /**
     * NodeInfo the remote node.
     */
    private final NodeInfo nodeInfo;
    
    private final SelectorManager selectorManager;
    
    public RemoteNodeJoinerConnection(NodeInfo aNodeInfo,
        IOContext anIOContext,
        SelectorManager aSelectorManager) {
        super(anIOContext);
        if ( null == aNodeInfo ) {
            throw new IllegalArgumentException("NodeInfo is required.");
        } else if ( null == aSelectorManager ) {
            throw new IllegalArgumentException("SelectorManager is required.");
        }
        nodeInfo = aNodeInfo;
        selectorManager = aSelectorManager;
    }

    protected Protocol newProtocol() throws ProtocolException {
        String hostName = nodeInfo.getAddress().getHostName();
        int port = nodeInfo.getPort();
        
        SocketProtocol socketProtocol = new SocketProtocol();
        // TODO configurable.
        socketProtocol.setTimeout(10 * 1000);
        socketProtocol.setInterface(new InetSocketAddress(hostName, 0));
        socketProtocol.setAddress(new InetSocketAddress(hostName, port));
        socketProtocol.setSelectorManager(selectorManager);
        
        BufferProtocol buffpt = new BufferProtocol();
        buffpt.setThreadPool(selectorManager.getThreadPool());
        
        socketProtocol.setUpProtocol(buffpt);
        buffpt.setDownProtocol(socketProtocol);

        socketProtocol.setup();
        buffpt.setup();
        
        return buffpt;
    }

}
