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
 * Message.
 * <BR>
 * Messages are exchanges between ServerNodes in a bi-directional
 * direction to wrap various information.
 * <BR>
 * Msgs are Serializable as they are sent across the wire.
 *
 * @version $Revision: 1.2 $ $Date: 2004/03/11 15:36:14 $
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
     */
    public Msg(Msg aPrototype) {
        header = new MsgHeader(aPrototype.header);
        body = new MsgBody(aPrototype.body);
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
    
}
