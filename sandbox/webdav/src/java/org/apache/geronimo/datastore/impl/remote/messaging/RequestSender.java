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

package org.apache.geronimo.datastore.impl.remote.messaging;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.FutureResult;
import EDU.oswego.cs.dl.util.concurrent.TimeoutException;

/**
 * Request Msgs sender.
 * <BR>
 * It is provided by a ServerNode to its Connectors in order to send Msgs to
 * remote Connectors.
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/11 15:36:14 $
 */
public class RequestSender {

    private static final Log log = LogFactory.getLog(RequestSender.class);
    
    /**
     * Number of milliseconds to wait for a response.
     */
    private static final long WAIT_RESPONSE = 100;
    
    /**
     * Used to generate request identifiers.
     */
    private volatile int requestIDSeq = 0;
    
    /**
     * Request id to FuturResult map.
     */
    private final Map responses;

    /**
     * Node using this sender.
     */
    private final NodeInfo srcNode;
    
    /**
     * Creates a request sender for the node aSrcNode.
     * <BR>
     * A request sender adds automatically to the sent messages the required
     * information to find in a Topology the node, which has emitted the
     * request.
     * 
     * @param aSrcNode Node which is emitting the request.
     */
    public RequestSender(NodeInfo aSrcNode) {
        if ( null == aSrcNode ) {
            throw new IllegalArgumentException("Node is required.");
        }
        srcNode = aSrcNode;
        responses = new HashMap();
    }

    /**
     * Sends a synchronous request Msg to the specified node.
     * 
     * @param anOpaque Request to be sent.
     * @param anOut Transport bus.
     * @param aTargetNodes Target node.
     * @return Request result.
     */
    public Object sendSyncRequest(Object anOpaque, MsgOutInterceptor anOut,
        NodeInfo aTargetNodes) {
        return sendSyncRequest(anOpaque, anOut, new NodeInfo[] {aTargetNodes});
    }
        
    /**
     * Sends a synchronous request Msg to the specified nodes.
     * 
     * @param anOpaque Request to be sent.
     * @param anOut Transport bus.
     * @param aTargetNodes Nodes to which the request is to be sent.
     * @return Request result.
     */
    public Object sendSyncRequest(Object anOpaque, MsgOutInterceptor anOut,
        NodeInfo[] aTargetNodes) {
        Msg msg = new Msg();
        
        MsgHeader header = msg.getHeader();
        Object id = createID(aTargetNodes);
        header.addHeader(MsgHeaderConstants.CORRELATION_ID, id);
        header.addHeader(MsgHeaderConstants.SRC_NODE, srcNode);
        header.addHeader(MsgHeaderConstants.DEST_NODES, aTargetNodes);
        
        MsgBody body = msg.getBody();
        body.setContent(anOpaque);
        
        anOut.push(msg);

        CommandResult result = waitResponse(id, WAIT_RESPONSE);
        if ( !result.isSuccess() ) {
            throw new RuntimeException(result.getException());
        }
        return result.getResult();
    }
    
    /**
     * Creates a slot for a new request/response and returns a request
     * identifier for this slot.
     * 
     * @param aTargetNodes Nodes to which the request is to be sent.
     * @return Request identifier.
     */
    private Object createID(NodeInfo[] aTargetNodes) {
        RequestID id;
        synchronized (responses) {
            id = new RequestID(requestIDSeq++);
            FutureResult[] results = new FutureResult[aTargetNodes.length];
            for (int i = 0; i < results.length; i++) {
                results[i] = new FutureResult();
            }
            responses.put(id, results);
        }
        return id;
    }
    
    /**
     * Waits for the response of the request anID.
     * 
     * @param anID Request identifier.
     * @param aWaitTime number of milliseconds to wait for a response.
     * @return Result of the request.
     */
    private CommandResult waitResponse(Object anID, long aWaitTime) {
        FutureResult[] results;
        synchronized(responses) {
            results = (FutureResult[]) responses.get(anID);
        }
        Exception ex;
        try {
            CommandResult returned = null;
            for (int i = 0; i < results.length; i++) {
                // TODO swap comment. Only used during debugging.
                returned = (CommandResult) results[i].get();
                // CommandResult returned = (CommandResult) result.timedGet(aWaitTime);
            }
            synchronized(responses) {
                responses.remove(anID);
            }
            return returned;
        } catch (TimeoutException e) {
            log.error(e);
            ex = e;
        } catch (InterruptedException e) {
            log.error(e);
            ex = e;
        } catch (InvocationTargetException e) {
            log.error(e);
            ex = e;
        }
        throw new RuntimeException(ex);
    }
    
    /**
     * Sets the result of the request anID. 
     * 
     * @param anID Request id.
     * @param aResult Response
     */
    public void setResponse(Object anID, CommandResult aResult) {
        FutureResult[] results;
        synchronized(responses) {
            results = (FutureResult[]) responses.get(anID);
        }
        for (int i = 0; i < results.length; i++) {
            FutureResult result = results[i];
            if ( null == result.peek() ) {
                result.set(aResult);
                break;
            }
        }
    }
    
    /**
     * Request identifier.
     */
    private static class RequestID implements Externalizable {
        private int id;
        /**
         * Required for Externalization.
         */
        public RequestID() {}
        public RequestID(int anId) {
            id = anId;
        }
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(id);
        }
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            id = in.readInt();
        }
        public boolean equals(Object obj) {
            if ( false == obj instanceof RequestID ) {
                return false;
            }
            return id == ((RequestID)obj).id;
        }
        public int hashCode() {
            return id;
        }
    }
    
}
