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

/**
 * Message.
 * <BR>
 * Messages are exchanges between ServantNode and ServerNode in a bi-directional
 * direction to wrap various information.
 * <BR>
 * Msg are Serializable are they are send across the wire.
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/25 13:36:15 $
 */
public class Msg
    implements Serializable
{

    /**
     * Header.
     */
    private final MsgHeader header;
    
    /**
     * Body.
     */
    private final MsgBody body;

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
    
}
