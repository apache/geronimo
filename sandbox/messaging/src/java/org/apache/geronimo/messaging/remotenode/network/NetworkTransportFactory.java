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

import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.io.IOContext;
import org.apache.geronimo.messaging.remotenode.MessagingTransportFactory;
import org.apache.geronimo.messaging.remotenode.NodeServer;
import org.apache.geronimo.messaging.remotenode.RemoteNode;
import org.apache.geronimo.messaging.remotenode.RemoteNodeConnection;
import org.apache.geronimo.network.SelectorManager;
import org.apache.geronimo.system.ClockPool;

/**
 * MessagingTransportFactory using Geronimo network as the transport layer.
 * 
 * @version $Revision: 1.2 $ $Date: 2004/06/10 23:12:25 $
 */
public class NetworkTransportFactory
    implements GBeanLifecycle, MessagingTransportFactory
{
    
    private final SelectorManager sm;
    private final ClockPool cp;
    
    public NetworkTransportFactory(SelectorManager aSelectorManager,
        ClockPool aClockPool) {
        if ( null == aSelectorManager ) {
            throw new IllegalArgumentException("SelectorManager is required.");
        }
        sm = aSelectorManager;
        cp = aClockPool;
    }
    
    public void doStart() throws WaitingException, Exception {
    }

    public void doStop() throws WaitingException, Exception {
    }

    public void doFail() {
    }
    
    public NodeServer factoryServer(NodeInfo aNodeInfo, IOContext anIOContext) {
        return new NodeServerImpl(aNodeInfo, anIOContext, sm, cp);
    }
    
    public RemoteNode factoryNode(NodeInfo aNodeInfo, IOContext anIOContext) {
        return new RemoteNodeJoiner(aNodeInfo, anIOContext, this);
    }

    public RemoteNodeConnection factoryNodeConnection(
        NodeInfo aNodeInfo, IOContext anIOContext) {
        return new RemoteNodeJoinerConnection(aNodeInfo, anIOContext, sm);
    }
    
}
