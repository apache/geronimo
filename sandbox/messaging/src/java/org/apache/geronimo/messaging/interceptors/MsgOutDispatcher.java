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

package org.apache.geronimo.messaging.interceptors;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.messaging.Msg;
import org.apache.geronimo.messaging.MsgHeader;

/**
 * Dispatches Msgs to Msg output based on their headers.
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:40 $
 */
public class MsgOutDispatcher
    implements MsgOutInterceptor
{

    private static final Log log = LogFactory.getLog(MsgOutDispatcher.class);
    
    /**
     * Header to MsgOutInterceptor map.
     */
    private final Map outputs;
    
    /**
     * MsgHeader key. 
     */
    private final Object headerKey;
    
    /**
     * Dispatches the received Msgs to the registered Msg outputs based on
     * the value of the header having the key aHeaderKey.
     * 
     * @param aHeaderKey Header key.
     */
    public MsgOutDispatcher(Object aHeaderKey) {
        if ( null == aHeaderKey ) {
            throw new IllegalArgumentException("Header key is required.");
        }
        headerKey = aHeaderKey;
        outputs = new HashMap();
    }

    /**
     * Registers a header value.
     * 
     * @param aHeader Header value.
     * @param anOut Msgs having the specified header value are dispatched to
     * this Msg output. 
     */
    public void register(Object aHeader, MsgOutInterceptor anOut) {
        log.debug("Register header value " + aHeader + "; output " + anOut);
        synchronized(outputs) {
            outputs.put(aHeader, anOut);
        }
    }

    /**
     * Unregisters a header value.
     * 
     * @param aHeader Header to be unregistered.
     */
    public void unregister(Object aHeader) {
        log.debug("Unregister header value " + aHeader);
        synchronized(outputs) {
            outputs.remove(aHeader);
        }
    }

    /**
     * Dispatches the Msg.
     * 
     * @param aMsg Msg to be dispatched.
     * @exception RuntimeException Indicates that no Msg output is registered
     * for this Msg.
     */
    public void push(Msg aMsg) {
        MsgHeader header = aMsg.getHeader();
        Object opaque;
        opaque = header.getHeader(headerKey);
        MsgOutInterceptor out;
        synchronized (outputs) {
            out = (MsgOutInterceptor) outputs.get(opaque);
        }
        if ( null == out ) {
            log.error("No Msg output for header {" + opaque + "}");
            throw new RuntimeException("No out for header {" + opaque + "}");
        }
        out.push(aMsg);
    }

}
