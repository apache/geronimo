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
package org.apache.geronimo.network.protocol.control;

import org.apache.geronimo.network.protocol.DownPacket;
import org.apache.geronimo.network.protocol.Protocol;
import org.apache.geronimo.network.protocol.ProtocolException;
import org.apache.geronimo.network.protocol.UpPacket;


/**
 * @version $Rev$ $Date$
 */
public abstract class State {

    private Protocol parent;

    protected State(Protocol parent) {
        this.parent = parent;
    }

    public Protocol getParent() {
        return parent;
    }

    public void setParent(Protocol parent) {
        this.parent = parent;
    }

    protected Protocol getUpProtocol() {
        return getParent().getUpProtocol();
    }

    protected Protocol getDownProtocol() {
        return getParent().getDownProtocol();
    }

    public abstract void sendUp(UpPacket packet) throws ProtocolException;

    public abstract void sendDown(DownPacket packet) throws ProtocolException;
}
