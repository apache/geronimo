/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.remoting.transport.async;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.StreamCorruptedException;

import org.apache.geronimo.remoting.transport.BytesMsg;
import org.apache.geronimo.remoting.transport.Msg;

/**
 * @version $Revision: 1.2 $ $Date: 2003/11/23 10:56:35 $
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
