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
 * A message.
 * <BR>
 * Msgs are Externalizable as they are sent across the wire.
 *
 * @version $Revision: 1.3 $ $Date: 2004/07/20 00:08:14 $
 */
public class Msg
    implements Externalizable
{

    /**
     * Header.
     */
    private MsgHeader header;
    
    /**
     * Body.
     */
    private MsgBody body;

    /**
     * Creates a new Msg.
     */
    public Msg() {
        header = new MsgHeader();
        body = new MsgBody();
    }

    /**
     * Prototype.
     * 
     * @param aPrototype Msg to be duplicated.
     */
    public Msg(Msg aPrototype) {
        header = new MsgHeader(aPrototype.header);
        body = new MsgBody(aPrototype.body);
    }

    /**
     * Builds a response Msg from this Msg.
     * 
     * @return Response Msg for this Msg.
     */
    public Msg reply() {
        Msg msg = new Msg();
        MsgHeader newHeader = msg.getHeader();
        newHeader.addHeader(MsgHeaderConstants.CORRELATION_ID,
            header.getHeader(MsgHeaderConstants.CORRELATION_ID));
        newHeader.addHeader(MsgHeaderConstants.DEST_ENDPOINT,
            header.getHeader(MsgHeaderConstants.SRC_ENDPOINT));
        newHeader.addHeader(MsgHeaderConstants.DEST_NODE,
            header.getHeader(MsgHeaderConstants.SRC_NODE));
        newHeader.addHeader(MsgHeaderConstants.DEST_NODES,
            header.getHeader(MsgHeaderConstants.SRC_NODE));
        newHeader.addHeader(MsgHeaderConstants.SRC_NODE,
            header.getHeader(MsgHeaderConstants.DEST_NODES));
        newHeader.addHeader(MsgHeaderConstants.BODY_TYPE,
            MsgBody.Type.RESPONSE);
        newHeader.addHeader(MsgHeaderConstants.TOPOLOGY_VERSION,
            header.getHeader(MsgHeaderConstants.TOPOLOGY_VERSION));
        return msg;
    }
    
    /**
     * Gets the header.
     * 
     * @return Header.
     */
    public MsgHeader getHeader() {
        return header;
    }
    
    /**
     * Gets the body
     * 
     * @return Body.
     */
    public MsgBody getBody() {
        return body;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(header);
        out.writeObject(body);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        header = (MsgHeader) in.readObject();
        body = (MsgBody) in.readObject();
    }

    public String toString() {
        return "Msg:" + header + ";" + body;
    }
    
}
