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

import org.apache.geronimo.messaging.Msg;

/**
 * Inbound interceptor in charge of retrieving an header. 
 *
 * @version $Rev$ $Date$
 */
public class HeaderInInterceptor implements MsgInInterceptor
{

    /**
     * Header to be retrieved. 
     */
    private final Object header;
    
    /**
     * Inbound interceptor from which the Msgs are poped.
     */
    private final MsgInInterceptor prev;

    /**
     * Current header value.
     */
    private Object headerValue;

    /**
     * Creates an inbound Msgs interceptor popping messages from aPrev and
     * retrieving the header identified by aHeader. 
     * 
     * @param aPrev Inbound Msgs interceptor to pop from.
     * @param aHeader Identifier of the header to be retrieved.
     */
    public HeaderInInterceptor(MsgInInterceptor aPrev, Object aHeader) {
        if ( null == aPrev ) {
            throw new IllegalArgumentException("Interceptor is required.");
        } else if ( null == aHeader ) {
            throw new IllegalArgumentException("Header is required.");
        }
        prev = aPrev;
        header = aHeader;
    }

    /**
     * Pops a Msg from the previous inbound Msg interceptor and retrieves a
     * specific header.
     * 
     * @return Msg poped from the previous interceptor.
     */
    public Msg pop() {
        Msg msg = prev.pop();
        headerValue = msg.getHeader().getHeader(header);
        return msg;
    }

    /**
     * Gets the current header value.
     * 
     * @return Header value.
     */
    public Object getHeader() {
        return headerValue;
    }
    
}
