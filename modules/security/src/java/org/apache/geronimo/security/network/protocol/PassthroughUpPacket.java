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

package org.apache.geronimo.security.network.protocol;

import java.nio.ByteBuffer;

import org.apache.geronimo.network.protocol.PacketFactory;
import org.apache.geronimo.network.protocol.ProtocolException;
import org.apache.geronimo.network.protocol.UpPacket;
import org.apache.geronimo.network.protocol.util.ByteKeyUpPacket;


/**
 * @version $Rev$ $Date$
 */
public class PassthroughUpPacket extends ByteKeyUpPacket implements PacketFactory, SubjectCarryingPackets {

    public PassthroughUpPacket() {
        super(PASSTHROUGH);
    }

    public UpPacket create(ByteBuffer buffer) throws ProtocolException {
        return new PassthroughUpPacket();
    }

}
