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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Msg header.
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/25 13:36:15 $
 */
public class MsgHeader
    implements Serializable
{

    /**
     * Header maps.
     */
    private final Map headers;
    
    public MsgHeader() {
        headers = new HashMap();
    }

    /**
     * Prototype.
     * <BR>
     * TODO This prototype is broken.
     */
    public MsgHeader(MsgHeader aHeader) {
        headers = new HashMap(aHeader.headers);
    }
    
    /**
     * Adds an header.
     * 
     * @param aKey Header key. 
     * @param aValue Header value.
     */
    public void addHeader(Object aKey, Object aValue) {
        synchronized(headers) {
            headers.put(aKey, aValue);
        }
    }

    /**
     * Gets a header.
     *  
     * @param aKey Header key.
     * @return Header value.
     * @exception IllegalArgumentException Indicates that the specified header
     * does not exist.
     */
    public Object getHeader(Object aKey) {
        Object value;
        synchronized(headers) {
            value = headers.get(aKey);
        }
        if ( null == value ) {
            throw new IllegalArgumentException("Header {" + aKey +
                "} does not exits.");
        }
        return value;
    }

}
