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

/**
 * Counterpart of HeaderInInterceptor.
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/25 13:36:15 $
 */
public class HeaderOutInterceptor
    implements MsgOutInterceptor
{

    /**
     * Key of the header to insert.
     */
    private final Object key;
    
    /**
     * Value of the header to insert.
     */
    private final Object value;
    
    /**
     * Next interceptor.
     */
    private final MsgOutInterceptor next;

    /**
     * Creates an outbound interceptor, which adds a header having the key
     * aKey and the value aValue. 
     * 
     * @param aKey Header key.
     * @param aValue Header value.
     * @param aNext Interceptor towards which the Msgs are pushed.
     */
    public HeaderOutInterceptor(Object aKey, Object aValue,
        MsgOutInterceptor aNext) {
        if ( null == aKey ) {
            throw new IllegalArgumentException("Key is required.");
        } else if ( null == aValue ) {
            throw new IllegalArgumentException("Label is required.");
        } else if ( null == aNext ) {
            throw new IllegalArgumentException("Next interceptor is required.");
        }
        key = aKey;
        value = aValue;
        next = aNext;
    }
    
    /**
     * Adds an header to the Msg and pushes it to the next interceptor.
     * 
     * @param aMsg Msg to process.
     */
    public void push(Msg aMsg) {
        MsgHeader header = aMsg.getHeader();
        header.addHeader(key, value);
        next.push(aMsg);
    }

}
