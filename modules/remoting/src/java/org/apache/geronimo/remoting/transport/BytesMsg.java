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

package org.apache.geronimo.remoting.transport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.geronimo.remoting.MarshalledObject;
import org.apache.geronimo.remoting.TransportContext;

/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:58:03 $
 */
public class BytesMsg implements Msg {

    transient TransportContext transportContext;
    ArrayList objectStack = new ArrayList(5);

    
    /**
     * @param transportContext
     */
    public BytesMsg(TransportContext transportContext) {
        this.transportContext = transportContext;
    }
    
    /**
     * @see org.apache.geronimo.remoting.transport.Msg#pushMarshaledObject(org.apache.geronimo.remoting.MarshalledObject)
     */
    public void pushMarshaledObject(MarshalledObject mo) throws IOException {
        objectStack.add((BytesMarshalledObject) mo);
    }

    /**
     * @see org.apache.geronimo.remoting.transport.Msg#popMarshaledObject()
     */
    public MarshalledObject popMarshaledObject() throws IOException {
        if (objectStack.size() == 0)
            throw new ArrayIndexOutOfBoundsException("Object stack is empty.");
        return (MarshalledObject) objectStack.remove(objectStack.size() - 1);
    }
    
    public int getStackSize() {
        return objectStack.size();
    }

    /**
     * @see org.apache.geronimo.remoting.transport.Msg#createMsg()
     */
    public Msg createMsg() {
        return new BytesMsg(transportContext);
    }

    public void writeExternal(DataOutput out) throws IOException {
        out.writeByte(objectStack.size());
        for (int i = 0; i < objectStack.size(); i++) {
            BytesMarshalledObject mo = (BytesMarshalledObject) objectStack.get(i);
            byte[] bs = mo.getBytes();
            out.writeInt(bs.length);
            out.write(bs);
        }
    }

    public void readExternal(DataInput in) throws IOException {
        objectStack.clear();
        int s = in.readByte();
        for (int i = 0; i < s; i++) {
            int l = in.readInt();
            byte t[] = new byte[l];
            in.readFully(t);
            BytesMarshalledObject mo = new BytesMarshalledObject(transportContext);
            mo.setBytes(t);
            objectStack.add(mo);
        }
    }
    
    public byte[] getAsBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(baos);
        writeExternal(os);
        os.close();
        return baos.toByteArray();
    }
    
    public void setFromBytes(byte data[]) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream is = new DataInputStream(bais);
        readExternal(is);
    }
}