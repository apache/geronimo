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

/**
 * Msg header.
 *
 * @version $Revision: 1.2 $ $Date: 2004/07/20 00:08:13 $
 */
public class MsgHeader
    implements Externalizable
{

    private static final Object[] headerConstants = {
        MsgHeaderConstants.BODY_TYPE,
        MsgHeaderConstants.CORRELATION_ID,
        MsgHeaderConstants.DEST_ENDPOINT,
        MsgHeaderConstants.DEST_NODE,
        MsgHeaderConstants.DEST_NODE_PATH,
        MsgHeaderConstants.DEST_NODES,
        MsgHeaderConstants.SRC_ENDPOINT,
        MsgHeaderConstants.SRC_NODE,
        MsgHeaderConstants.TOPOLOGY_VERSION};
    
    /**
     * Header maps.
     */
    private Object[] headers;
    
    public MsgHeader() {
        headers = new Object[headerConstants.length];
    }

    /**
     * Prototype.
     * 
     * @param aHeader Header to be duplicated.
     */
    public MsgHeader(MsgHeader aHeader) {
        headers = new Object[headerConstants.length];
        for (int i = 0; i < aHeader.headers.length; i++) {
            headers[i] = aHeader.headers[i];
        }
    }

    /**
     * Returns the header value and resets it. 
     * 
     * @param aKey Header key.
     * @return Header value.
     */
    public Object resetHeader(Object aKey) {
        int idx = getHeaderIndex(aKey);
        Object result = headers[idx];
        headers[idx] = null;
        return result;
    }
    
    /**
     * Adds an header.
     * 
     * @param aKey Header key. 
     * @param aValue Header value.
     */
    public void addHeader(Object aKey, Object aValue) {
        int idx = getHeaderIndex(aKey);
        headers[idx] = aValue;
    }
    
    /**
     * Gets a required header.
     *  
     * @param aKey Header key.
     * @return Header value.
     * @exception IllegalArgumentException Indicates that the specified header
     * does not exist.
     */
    public Object getHeader(Object aKey) {
        int idx = getHeaderIndex(aKey);
        Object value = headers[idx];
        if ( null == value ) {
            throw new IllegalArgumentException("Header {" + aKey +
                "} is not set.");
        }
        return value;
    }

    /**
     * Gets an optional header.
     *  
     * @param aKey Header key.
     * @return Header value. If the header is not defined, then null is\
     * returned.
     */
    public Object getOptionalHeader(Object aKey) {
        int idx = getHeaderIndex(aKey);
        return headers[idx];
    }

    /**
     * Gets the index of the header having the specified key.
     * 
     * @param aKey Header key.
     * @return header index.
     */
    private int getHeaderIndex(Object aKey) {
        for (int i = 0; i < headerConstants.length; i++) {
            if ( aKey == headerConstants[i] ) {
                return i;
            }
        }
        throw new IllegalArgumentException("Header {" + aKey +
            "} is not supported.");
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(headers);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        headers = (Object[]) in.readObject();        
    }

    public String toString() {
        return "MsgHeader:src=" + getOptionalHeader(MsgHeaderConstants.SRC_NODE)
            + ";dest=" + getOptionalHeader(MsgHeaderConstants.DEST_NODE)
            + ";dests=" + getOptionalHeader(MsgHeaderConstants.DEST_NODES)
            + ";type=" + getOptionalHeader(MsgHeaderConstants.BODY_TYPE)
            + ";ID=" + getOptionalHeader(MsgHeaderConstants.CORRELATION_ID);
    }
    
}
