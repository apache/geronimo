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
package org.apache.geronimo.network.protocol.totem.singlering;

import org.apache.geronimo.network.protocol.DownPacket;
import org.apache.geronimo.network.protocol.Protocol;
import org.apache.geronimo.network.protocol.ProtocolException;
import org.apache.geronimo.network.protocol.UpPacket;


/**
 * @version $Revision: 1.1 $ $Date: 2004/03/10 02:14:29 $
 */
public class SingleTotemRingProtocol implements Protocol {

    public void doStart() throws ProtocolException {
    }

    public void doStop() throws ProtocolException {
    }

    public void sendUp(UpPacket packet) throws ProtocolException {
    }

    public void sendDown(DownPacket packet) throws ProtocolException {
    }

    public Protocol getUp() {
        return null;
    }

    public void setUp(Protocol up) {
    }

    public Protocol getDown() {
        return null;
    }

    public void setDown(Protocol down) {
    }

    public void clearLinks() {
    }

    public Protocol cloneProtocol() throws CloneNotSupportedException {
        return (Protocol) super.clone();
    }
}
