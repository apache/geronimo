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

package org.apache.geronimo.messaging;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;

import EDU.oswego.cs.dl.util.concurrent.FutureResult;
import EDU.oswego.cs.dl.util.concurrent.TimeoutException;

/**
 * Request Msgs sender.
 *
 * @version $Revision: 1.2 $ $Date: 2004/05/20 13:37:11 $
 */
public class RequestSender
{

    private static final Log log = LogFactory.getLog(RequestSender.class);
    
    /**
     * Number of milliseconds to wait for a response.
     */
    private static final long WAIT_RESPONSE = 5000;
    
    /**
     * Used to generate request identifiers.
     */
    private static volatile int seqID = 0;
    
    /**
     * Request id to FuturResult map.
     */
    private final Map responses;

    
    /**
     * Creates a request sender.
     * <BR>
     * A request sender adds automatically to the sent messages the required
     * information to find in a Topology the node, which has emitted the
     * request.
     */
    public RequestSender() {
        responses = new HashMap();
    }

    /**
     * Sends a synchronous request Msg to the specified node.
     * 
     * @param anOpaque Request to be sent.
     * @param anOut Transport bus.
     * @param aTargetID Target EndPoint identifier.
     * @param aTargetNode Target node.
     * @return Request result.
     */
    public Object sendSyncRequest(Object anOpaque, MsgOutInterceptor anOut,
        Object aTargetID, NodeInfo aTargetNode) {
        return sendSyncRequest(anOpaque, anOut, aTargetID,
            new NodeInfo[] {aTargetNode});
    }

    /**
     * Sends a synchronous request Msg to the specified nodes.
     * 
     * @param anOpaque Request to be sent.
     * @param anOut Transport bus.
     * @param aTargetID Target EndPoint identifier.
     * @param aTargetNodes Target nodes.
     * @return Request result.
     */
    public Object sendSyncRequest(Object anOpaque, MsgOutInterceptor anOut,
        Object aTargetID, NodeInfo[] aTargetNodes) {
        if ( null == anOut ) {
            throw new IllegalArgumentException("Msg out is not required");
        }
        Msg msg = new Msg();
        
        MsgHeader header = msg.getHeader();
        RequestID id = createID(aTargetNodes);
        header.addHeader(MsgHeaderConstants.CORRELATION_ID, id);
        header.addHeader(MsgHeaderConstants.DEST_NODES, aTargetNodes);
        header.addHeader(MsgHeaderConstants.BODY_TYPE, MsgBody.Type.REQUEST);
        header.addHeader(MsgHeaderConstants.DEST_ENDPOINT, aTargetID);
        
        MsgBody body = msg.getBody();
        body.setContent(anOpaque);
        
        anOut.push(msg);

        Result result = waitResponse(id, WAIT_RESPONSE);
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
    private RequestID createID(NodeInfo[] aTargetNodes) {
        FutureResult[] results = new FutureResult[aTargetNodes.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = new FutureResult();
        }
        RequestID id = new RequestID(new Integer(seqID++));
        responses.put(id, results);
        return id;
    }
    
    /**
     * Waits for the response of the request anID.
     * 
     * @param anID Request identifier.
     * @param aWaitTime number of milliseconds to wait for a response.
     * @return Result of the request.
     */
    private Result waitResponse(RequestID anID, long aWaitTime) {
        FutureResult[] results = (FutureResult[]) responses.get(anID);
        Exception ex;
        try {
            Result returned = null;
            for (int i = 0; i < results.length; i++) {
                returned = (Result) results[i].timedGet(aWaitTime);
            }
            responses.remove(anID);
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
    public void setResponse(Object anID, Result aResult) {
        if ( false == anID instanceof RequestID ) {
            throw new IllegalArgumentException("ID is of the wrong type.");
        }
        FutureResult[] results = (FutureResult[]) responses.get(anID);
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
    public static class RequestID implements Externalizable {
        protected Integer id;
        /**
         * Required for Externalization.
         */
        public RequestID() {}
        public RequestID(Integer anID) {
            id = anID;
        }
        public int getID() {
            return id.intValue();
        }
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(id.intValue());
        }
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            id = new Integer(in.readInt());
        }
        public int hashCode() {
            return id.hashCode();
        }
        public boolean equals(Object obj) {
            if ( false == obj instanceof RequestID ) {
                return false;
            }
            RequestID otherID = (RequestID) obj;
            return id.equals(otherID.id);
        }
        public String toString() {
            return "ID=" + id;
        }
    }
    
}
