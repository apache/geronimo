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

import java.lang.reflect.InvocationTargetException;

import org.apache.geronimo.messaging.Msg;
import org.apache.geronimo.messaging.MsgBody;
import org.apache.geronimo.messaging.MsgHeader;
import org.apache.geronimo.messaging.MsgHeaderConstants;
import org.apache.geronimo.messaging.NodeException;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.RequestSender;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;
import org.apache.geronimo.messaging.io.IOContext;
import org.apache.geronimo.messaging.remotenode.AbstractRemoteNode;
import org.apache.geronimo.messaging.remotenode.RemoteNodeConnection;
import org.apache.geronimo.network.SelectorManager;

import EDU.oswego.cs.dl.util.concurrent.FutureResult;
import EDU.oswego.cs.dl.util.concurrent.TimeoutException;

/**
 * 
 * @version $Revision: 1.4 $ $Date: 2004/07/20 00:15:05 $
 */
public class RemoteNodeJoiner
    extends AbstractRemoteNode
{

    private final SelectorManager sm;
    
    public RemoteNodeJoiner(NodeInfo aLocalNodeInfo, NodeInfo aRemoteNodeInfo, 
        IOContext anIOContext, SelectorManager aSelectorManager) {
        super(aLocalNodeInfo, aRemoteNodeInfo, anIOContext);
        if ( null == aSelectorManager ) {
            throw new IllegalArgumentException("SelectorManager is required");
        }
        sm = aSelectorManager;
    }

    public void join() throws NodeException {
        RemoteNodeConnection connection = 
            new RemoteNodeJoinerConnection(remoteNodeInfo, ioContext, sm);
        setConnection(connection);

        Msg msg = new Msg();
        MsgHeader header = msg.getHeader();
        header.addHeader(MsgHeaderConstants.SRC_NODE, localNodeInfo);
        header.addHeader(MsgHeaderConstants.DEST_NODE, remoteNodeInfo);
        
        // Only set to comply with a valid request. 
        header.addHeader(MsgHeaderConstants.DEST_NODES, remoteNodeInfo);
        header.addHeader(MsgHeaderConstants.SRC_ENDPOINT, "");
        header.addHeader(MsgHeaderConstants.CORRELATION_ID,
            new RequestSender.RequestID((byte) 0));
        header.addHeader(MsgHeaderConstants.BODY_TYPE, MsgBody.Type.REQUEST);
        header.addHeader(MsgHeaderConstants.TOPOLOGY_VERSION, new Integer(0));
        
        msg.getBody().setContent(localNodeInfo);

        final FutureResult result = new FutureResult();
        setMsgProducerOut(new MsgOutInterceptor() {
            public void push(Msg aMsg) {
                result.set(aMsg);
            }
        });
        getMsgConsumerOut().push(msg);
        Msg reply;
        try {
            // waits 3 seconds for a reply.
            reply = (Msg) result.get();
            reply = (Msg) result.timedGet(3000);
        } catch (TimeoutException e) {
            throw new NodeException("Join request submitted by " +
                localNodeInfo + " to " + remoteNodeInfo + " has timed out.");
        } catch (InterruptedException e) {
            throw new NodeException(e);
        } catch (InvocationTargetException e) {
            throw new NodeException(e);
        }
        Boolean isOK = (Boolean) reply.getBody().getContent();
        if ( Boolean.FALSE == isOK ) {
            throw new NodeException(remoteNodeInfo + " has refused the " +
                "join request submitted by " + localNodeInfo);
        }
        manager.registerRemoteNode(this);
    }
    
}
