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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.geronimo.messaging.CommunicationException;
import org.apache.geronimo.messaging.MsgHeaderConstants;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.interceptors.HeaderOutInterceptor;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;
import org.apache.geronimo.messaging.io.IOContext;
import org.apache.geronimo.messaging.remotenode.RemoteNode;
import org.apache.geronimo.messaging.remotenode.RemoteNodeConnection;

/**
 * Abstract implememtation for the RemoteNode contracts.
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:42 $
 */
public abstract class AbstractRemoteNode
    implements RemoteNode
{

    /**
     * Remote node meta-data.
     */
    protected final NodeInfo nodeInfo;

    protected final IOContext ioContext;
    
    /**
     * Connections opened to this remote node.
     */
    private final Set connections;
    
    /**
     * Incoming Msgs (coming from remote nodes) are pushed to this output.
     */
    protected MsgOutInterceptor out; 
    
    public AbstractRemoteNode(NodeInfo aNodeInfo, IOContext anIOContext) {
        if ( null == aNodeInfo ) {
            throw new IllegalArgumentException("NodeInfo is required.");
        } else if ( null == anIOContext ) {
            throw new IllegalArgumentException("IOContext is required.");
        }
        nodeInfo = aNodeInfo;
        ioContext = anIOContext;
        
        connections = new HashSet();
    }
    
    public void setMsgProducerOut(MsgOutInterceptor aMsgOut) {
        synchronized(connections) {
            for (Iterator iter = connections.iterator(); iter.hasNext();) {
                RemoteNodeConnection conn = (RemoteNodeConnection) iter.next();
                conn.setMsgProducerOut(aMsgOut);
            }
        }
        out = aMsgOut;
    }

    public MsgOutInterceptor getMsgConsumerOut() {
        RemoteNodeConnection connection;
        synchronized(connections) {
            if ( connections.isEmpty() ) {
                throw new IllegalStateException("No opened connections.");
            }
            connection = (RemoteNodeConnection) connections.iterator().next();
        }
        return
            new HeaderOutInterceptor(
                MsgHeaderConstants.DEST_NODE,
                nodeInfo,
				connection.getMsgConsumerOut());
    }
    
    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }

    public void addConnection(RemoteNodeConnection aConnection) {
        aConnection.setMsgProducerOut(out);
        synchronized(connections) {
            connections.add(aConnection);
        }
    }
    
    public void removeConnection(RemoteNodeConnection aConnection) {
        synchronized(connections) {
            connections.remove(aConnection);
        }
        aConnection.setMsgProducerOut(null);
    }
    
    public void leave() throws IOException, CommunicationException {
        synchronized(connections) {
            for (Iterator iter = connections.iterator(); iter.hasNext();) {
                RemoteNodeConnection conn = (RemoteNodeConnection) iter.next();
                conn.close();
                iter.remove();
                conn.setMsgProducerOut(null);
            }
        }
    }
    
}
