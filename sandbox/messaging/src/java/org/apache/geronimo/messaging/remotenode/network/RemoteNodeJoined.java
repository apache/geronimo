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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.messaging.Msg;
import org.apache.geronimo.messaging.MsgBody;
import org.apache.geronimo.messaging.NodeException;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;
import org.apache.geronimo.messaging.io.IOContext;
import org.apache.geronimo.messaging.remotenode.AbstractRemoteNode;
import org.apache.geronimo.messaging.remotenode.RemoteNodeConnection;
import org.apache.geronimo.network.protocol.Protocol;

/**
 * 
 * @version $Rev$ $Date$
 */
public class RemoteNodeJoined
    extends AbstractRemoteNode
{
    
    private static final Log log = LogFactory.getLog(RemoteNodeJoined.class);

    private final Protocol protocol;
    
    
    public RemoteNodeJoined(NodeInfo aLocalNode, IOContext anIOContext, 
        Protocol aProtocol) {
        super(aLocalNode, anIOContext);
        protocol = aProtocol;
    }
    
    public void join() throws NodeException {
        RemoteNodeConnection connection =
            new RemoteNodeJoinedConnection(ioContext, protocol);
        setConnection(connection);
        
        setMsgProducerOut(new JoinExecutor());
    }
    
    private class JoinExecutor implements MsgOutInterceptor {

        public void push(Msg aMsg) {
            MsgBody body = aMsg.getBody();
            remoteNodeInfo = (NodeInfo) body.getContent();
            
            if ( null != manager.findRemoteNode(remoteNodeInfo) ) {
                log.error(remoteNodeInfo + 
                    " tried to join twice this node; rejecting request.");
                Msg msg = aMsg.reply();
                msg.getBody().setContent(Boolean.FALSE);
                getMsgConsumerOut().push(msg);
                return;
            }
            Msg msg = aMsg.reply();
            msg.getBody().setContent(Boolean.TRUE);
            getMsgConsumerOut().push(msg);

            manager.registerRemoteNode(RemoteNodeJoined.this);
        }
        
    }

}
