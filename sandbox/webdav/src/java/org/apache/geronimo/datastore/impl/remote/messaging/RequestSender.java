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

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.FutureResult;
import EDU.oswego.cs.dl.util.concurrent.TimeoutException;

/**
 * Request Msgs sender.
 *
 * @version $Revision: 1.2 $ $Date: 2004/03/03 13:10:07 $
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
    
    public RequestSender() {
        responses = new HashMap();
    }

    /**
     * Sends a synchronous request Msg.
     * 
     * @param anOpaque Request to be sent.
     * @param anOut Transport bus.
     * @return Request result.
     */
    public Object sendSyncRequest(Object anOpaque, MsgOutInterceptor anOut) {
        Msg msg = new Msg();
        
        MsgHeader header = msg.getHeader();
        Object id = createID();
        header.addHeader(MsgHeaderConstants.CORRELATION_ID, id);
        
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
     * @return Request identifier.
     */
    private Object createID() {
        Integer id;
        synchronized (responses) {
            id = new Integer(requestIDSeq++);
            responses.put(id, new FutureResult());
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
        FutureResult result;
        synchronized(responses) {
            result = (FutureResult) responses.get(anID);
        }
        Exception ex;
        try {
            // TODO swap comment. Only used during debugging. 
            CommandResult returned = (CommandResult) result.get();
            // CommandResult returned = (CommandResult) result.timedGet(aWaitTime);
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
        FutureResult result;
        synchronized(responses) {
            result = (FutureResult) responses.get(anID);
        }
        result.set(aResult);
    }
    
}
