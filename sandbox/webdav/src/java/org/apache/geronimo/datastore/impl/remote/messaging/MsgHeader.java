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

/**
 * Msg header.
 *
 * @version $Revision: 1.2 $ $Date: 2004/03/11 15:36:14 $
 */
public class MsgHeader
    implements Externalizable
{

    private static final Object[] headerConstants = {
        MsgHeaderConstants.BODY_TYPE,
        MsgHeaderConstants.CORRELATION_ID,
        MsgHeaderConstants.DEST_CONNECTOR,
        MsgHeaderConstants.DEST_NODE,
        MsgHeaderConstants.DEST_NODE_PATH,
        MsgHeaderConstants.DEST_NODES,
        MsgHeaderConstants.SRC_CONNECTOR,
        MsgHeaderConstants.SRC_NODE};
    
    /**
     * Header maps.
     */
    private Object[] headers;
    
    public MsgHeader() {
        headers = new Object[headerConstants.length];
    }

    /**
     * Prototype.
     * <BR>
     * TODO This prototype is broken.
     */
    public MsgHeader(MsgHeader aHeader) {
        headers = new Object[headerConstants.length];
        for (int i = 0; i < aHeader.headers.length; i++) {
            headers[i] = aHeader.headers[i];
        }
    }
    
    /**
     * Adds an header.
     * 
     * @param aKey Header key. 
     * @param aValue Header value.
     */
    public void addHeader(Object aKey, Object aValue) {
        for (int i = 0; i < headerConstants.length; i++) {
            if ( aKey == headerConstants[i] ) {
                headers[i] = aValue;
                return;
            }
        }
        throw new IllegalArgumentException("Header {" + aKey +
            "} is not supported.");
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
        for (int i = 0; i < headerConstants.length; i++) {
            if ( aKey == headerConstants[i] ) {
                Object value = headers[i];
                if ( null == value ) {
                    throw new IllegalArgumentException("Header {" + aKey +
                        "} is not set.");
                }
                return value;
            }
        }
        throw new IllegalArgumentException("Header {" + aKey +
            "} is not supported.");
    }

    /**
     * Gets an optional header.
     *  
     * @param aKey Header key.
     * @return Header value.
     */
    public Object getOptionalHeader(Object aKey) {
        Object value;
        for (int i = 0; i < headerConstants.length; i++) {
            if ( aKey == headerConstants[i] ) {
                return headers[i];
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
    
}
