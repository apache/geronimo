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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.messaging.MsgHeaderConstants;
import org.apache.geronimo.messaging.NodeException;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.interceptors.HeaderOutInterceptor;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;
import org.apache.geronimo.messaging.io.IOContext;
import org.apache.geronimo.messaging.remotenode.RemoteNodeConnection.LifecycleListener;

/**
 * Abstract implememtation for the RemoteNode contracts.
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractRemoteNode
    implements RemoteNode
{

    private static final Log log = LogFactory.getLog(AbstractRemoteNode.class);

    /**
     * Local node meta-data.
     */
    protected final NodeInfo localNodeInfo;
    
    /**
     * Manager of this remote node.
     */
    protected RemoteNodeManager manager;
    
    /**
     * Remote node meta-data.
     */
    protected NodeInfo remoteNodeInfo;

    protected final IOContext ioContext;

    /**
     * Connection opened to this remote node.
     */
    private RemoteNodeConnection connection;
    
    /**
     * Incoming Msgs (coming from remote nodes) are pushed to this output.
     */
    protected MsgOutInterceptor producerOut; 
    

    public AbstractRemoteNode(NodeInfo aLocalNode, IOContext anIOContext) {
        if ( null == aLocalNode ) {
            throw new IllegalArgumentException("Local NodeInfo is required.");
        } else if ( null == anIOContext ) {
            throw new IllegalArgumentException("IOContext is required.");
        }
        localNodeInfo = aLocalNode;
        ioContext = anIOContext;
    }
    
    public AbstractRemoteNode(NodeInfo aLocalNodeInfo, NodeInfo aRemoteNodeInfo,
        IOContext anIOContext) {
        this(aLocalNodeInfo, anIOContext);
        if ( null == aRemoteNodeInfo ) {
            throw new IllegalArgumentException("Remote NodeInfo is required.");
        }
        remoteNodeInfo = aRemoteNodeInfo;
    }
    
    public void setManager(RemoteNodeManager aManager) {
        manager = aManager;
    }
    
    public void setMsgProducerOut(MsgOutInterceptor aMsgOut) {
        producerOut = aMsgOut;
        if ( null == connection ) {
            return;
        }
        connection.setMsgProducerOut(aMsgOut);
    }

    public MsgOutInterceptor getMsgConsumerOut() {
        return new HeaderOutInterceptor(
            MsgHeaderConstants.DEST_NODE,
            remoteNodeInfo,
            connection.getMsgConsumerOut());
    }
    
    public NodeInfo getNodeInfo() {
        return remoteNodeInfo;
    }

    protected void setConnection(RemoteNodeConnection aConnection)
        throws NodeException {
        if ( null != connection && null != aConnection ) {
            throw new IllegalArgumentException("Connection already defined.");
        } else if ( null != connection ) {
            connection.close();
            connection = null;
            return;
        }
        connection = aConnection;
        connection.open();
        connection.setMsgProducerOut(producerOut);
        connection.setLifecycleListener(new LifecycleListener() {
            public void onClose() {
                manager.unregisterRemoteNode(AbstractRemoteNode.this);
            }
        });
    }
    
    public void leave() {
        connection.close();
        connection.setMsgProducerOut(null);
        connection = null;
    }
    
}
