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

package org.apache.geronimo.messaging.remotenode.admin;

import java.lang.reflect.InvocationTargetException;

import org.apache.geronimo.messaging.CommunicationException;
import org.apache.geronimo.messaging.Msg;
import org.apache.geronimo.messaging.MsgHeader;
import org.apache.geronimo.messaging.MsgHeaderConstants;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.RequestSender;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;
import org.apache.geronimo.messaging.remotenode.RemoteNodeConnection;

import EDU.oswego.cs.dl.util.concurrent.FutureResult;
import EDU.oswego.cs.dl.util.concurrent.TimeoutException;

/**
 * Join request.
 *
 * @version $Revision: 1.1 $ $Date: 2004/06/03 14:39:44 $
 */
public class JoinRequest
    implements RemoteNodeConnectionCommand
{

    /**
     * Node which initiates the join command.
     */
    private final NodeInfo joiner;
    
    /**
     * Target of the join command.
     */
    private final NodeInfo joined;
    
    /**
     * @param aJoiner Node which initiates the join command.
     * @param aJoined Target of the join command.
     */
    public JoinRequest(NodeInfo aJoiner, NodeInfo aJoined) {
        if ( null == aJoiner ) {
            throw new IllegalArgumentException("Joiner is required");
        } else if ( null == aJoined ) {
            throw new IllegalArgumentException("Joined is required");
        }
        joiner = aJoiner;
        joined = aJoined;
    }
    
	public void execute(RemoteNodeConnection aConnection) {
        Msg msg = new Msg();
        MsgHeader header = msg.getHeader();
        header.addHeader(MsgHeaderConstants.SRC_NODE, joiner);
        header.addHeader(MsgHeaderConstants.DEST_NODE, joined);
        
        // Only set to comply with a valid request. 
        header.addHeader(MsgHeaderConstants.DEST_NODES, joined);
        header.addHeader(MsgHeaderConstants.SRC_ENDPOINT, "");
        header.addHeader(MsgHeaderConstants.CORRELATION_ID,
            new RequestSender.RequestID(new Integer(0)));

        msg.getBody().setContent(joiner);

        final FutureResult result = new FutureResult();
        aConnection.setMsgProducerOut(new MsgOutInterceptor() {
			public void push(Msg aMsg) {
                result.set(aMsg);
			}
        });
        aConnection.getMsgConsumerOut().push(msg);
        Msg reply;
        try {
			// waits 3 seconds for a reply.
            reply = (Msg) result.get();
//			reply = (Msg) result.timedGet(3000);
        } catch (TimeoutException e) {
            throw new CommunicationException("Join request submitted by " +
                joiner + " to " + joined + " has timed out.");
		} catch (InterruptedException e) {
            throw new CommunicationException(e);
		} catch (InvocationTargetException e) {
            throw new CommunicationException(e);
		}
        Boolean isOK = (Boolean) reply.getBody().getContent();
        if ( Boolean.FALSE == isOK ) {
            throw new CommunicationException(joined + " has refused the " +
                "join request submitted by " + joiner);
        }
    }
    
}
