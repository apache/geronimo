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

package org.apache.geronimo.remoting.transport.async;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.StreamCorruptedException;

import org.apache.geronimo.remoting.transport.BytesMsg;
import org.apache.geronimo.remoting.transport.Msg;

/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:58:04 $
 */
public class AsyncMsg extends BytesMsg {

    public static final byte DATAGRAM_TYPE = 0;
    public static final byte REQUEST_TYPE = 1;
    public static final byte RESPONE_TYPE = 2;

    byte type = REQUEST_TYPE;
    int requestId;
    String to;

    
    /**
     * @param transportContext
     */
    public AsyncMsg() {
        super(Registry.transportContext);
    }

    
    /**
     * @see org.apache.geronimo.remoting.transport.BytesMsg#createMsg()
     */
    public Msg createMsg() {
        return new AsyncMsg();
    }

    /**
     * @see org.apache.geronimo.remoting.transport.BytesMsg#writeExternal(java.io.DataOutput)
     */
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeByte(type);
        switch (type) {
            case DATAGRAM_TYPE :
                out.writeUTF(to);
                break;
            case REQUEST_TYPE :
                out.writeInt(requestId);
                out.writeUTF(to);
                break;
            case RESPONE_TYPE :
                out.writeInt(requestId);
                break;
            default :
                throw new StreamCorruptedException("Unknow type: " + type);
        }
    }

    /**
     * @see org.apache.geronimo.remoting.transport.BytesMsg#readExternal(java.io.DataInput)
     */
    public void readExternal(DataInput in) throws IOException {
        super.readExternal(in);
        type = in.readByte();
        requestId = 0;
        switch (type) {
            case DATAGRAM_TYPE :
                to = in.readUTF();
                break;
            case REQUEST_TYPE :
                requestId = in.readInt();
                to = in.readUTF();
                break;
            case RESPONE_TYPE :
                requestId = in.readInt();
                break;
            default :
                throw new StreamCorruptedException("Unknow type: " + type);
        }
    }    
}
