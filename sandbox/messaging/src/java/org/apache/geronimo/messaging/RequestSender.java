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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;

import EDU.oswego.cs.dl.util.concurrent.FutureResult;
import EDU.oswego.cs.dl.util.concurrent.TimeoutException;

/**
 * Request Msgs sender.
 *
 * @version $Rev$ $Date$
 */
public class RequestSender
{

    private static final Log log = LogFactory.getLog(RequestSender.class);
    
    /**
     * Number of milliseconds to wait for a response.
     */
    private static final long WAIT_RESPONSE = 2000;

    /**
     * Maximum number of requests that this instance can performed concurrently.
     */
    private static final int MAX_CONCURRENT_REQUEST = 255;
    
    /**
     * Memory barrier for seqID counter.
     */
    private final Object seqMemBarrier = new Object(); 
    
    /**
     * Used to generate request identifiers.
     */
    private int seqID = 0;
    
    /**
     * Request id to FuturResult[].
     */
    private final Object[] responses;

    
    /**
     * Creates a request sender.
     * <BR>
     * A request sender adds automatically to the sent messages the required
     * information to find in a Topology the node, which has emitted the
     * request.
     */
    public RequestSender() {
        responses = new Object[MAX_CONCURRENT_REQUEST + 1];
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
        IDTOFutureResult futurResult = createID(aTargetNodes);
        header.addHeader(MsgHeaderConstants.CORRELATION_ID, futurResult.id);
        header.addHeader(MsgHeaderConstants.DEST_NODES, aTargetNodes);
        header.addHeader(MsgHeaderConstants.BODY_TYPE, MsgBody.Type.REQUEST);
        header.addHeader(MsgHeaderConstants.DEST_ENDPOINT, aTargetID);
        
        MsgBody body = msg.getBody();
        body.setContent(anOpaque);
        
        anOut.push(msg);

        Result result = waitResponse(futurResult.futurResults, WAIT_RESPONSE);
        if ( !result.isSuccess() ) {
            throw new CommunicationException(result.getThrowable());
        }
        return result.getResult();
    }
    
    /**
     * Creates a slot for a new request/response and returns a request
     * identifier for this slot.
     * 
     * @param aTargetNodes Nodes to which the request is to be sent.
     * @return Request identifier and FutureResults.
     */
    private IDTOFutureResult createID(NodeInfo[] aTargetNodes) {
        FutureResult[] results = new FutureResult[aTargetNodes.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = new FutureResult();
        }
        int idAsInt;
        synchronized (seqMemBarrier) {
            // Implementation note: it is unlikely to have more than
            // MAX_CONCURRENT_REQUEST Threads sending requests concurrently;
            // This implementation assumes this unlikelihood. 
            if ( MAX_CONCURRENT_REQUEST == ++seqID ) seqID = 1;
            responses[seqID] = results;
            idAsInt = seqID;
        }
        RequestID id = new RequestID((byte)idAsInt);
        IDTOFutureResult result = new IDTOFutureResult();
        result.id = id;
        result.futurResults = results;
        return result;
    }
    
    /**
     * Waits for the response of the request anID.
     * 
     * @param anID Request identifier.
     * @param aWaitTime number of milliseconds to wait for a response.
     * @return Result of the request.
     */
    private Result waitResponse(FutureResult[] aResults, long aWaitTime) {
        Exception ex;
        try {
            Result returned = null;
            for (int i = 0; i < aResults.length; i++) {
                returned = (Result) aResults[i].timedGet(aWaitTime);
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
        throw new CommunicationException(ex);
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
        RequestID id = (RequestID) anID;
        int index = id.id <= 0 ? id.id & 127 + 128 : id.id;
        FutureResult[] results;
        results = (FutureResult[]) responses[index];
        if ( null == results ) {
            log.error("Invalid request ID {" + anID + "}");
            return;
        }
        synchronized (results) {
            for (int i = 0; i < results.length; i++) {
                FutureResult result = results[i];
                if ( null == result.peek() ) {
                    result.set(aResult);
                    break;
                }
            }
        }
    }
    
    private static class IDTOFutureResult {
        private RequestID id;
        private FutureResult[] futurResults;
    }
    
    /**
     * Request identifier.
     */
    public static class RequestID implements Externalizable {
        protected byte id;
        /**
         * Required for Externalization.
         */
        public RequestID() {}
        public RequestID(byte anID) {
            id = anID;
        }
        public byte getID() {
            return id;
        }
        public void writeExternal(ObjectOutput out) throws IOException {
            out.write(id);
        }
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            id = (byte) in.read();
        }
        public int hashCode() {
            return id;
        }
        public boolean equals(Object obj) {
            if ( false == obj instanceof RequestID ) {
                return false;
            }
            RequestID otherID = (RequestID) obj;
            return id == otherID.id;
        }
        public String toString() {
            return "ID=" + id;
        }
    }
    
}
