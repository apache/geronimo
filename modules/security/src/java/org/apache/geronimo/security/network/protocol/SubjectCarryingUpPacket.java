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
import org.apache.geronimo.network.protocol.util.PacketUtil;
import org.apache.geronimo.security.SubjectId;


/**
 * @version $Revision: 1.1 $ $Date: 2004/03/10 02:15:50 $
 */
class SubjectCarryingUpPacket extends ByteKeyUpPacket implements PacketFactory, SubjectCarryingPackets {

    private SubjectId subjectId;

    public SubjectCarryingUpPacket() {
        super(SUBJECT);
    }

    public SubjectId getSubjectId() {
        return subjectId;
    }

    public UpPacket create(ByteBuffer buffer) throws ProtocolException {
        Long id = PacketUtil.getLong(buffer);
        byte[] hash = PacketUtil.getByteArray(buffer);

        SubjectCarryingUpPacket packet = new SubjectCarryingUpPacket();
        packet.setBuffer(buffer);
        packet.subjectId = new SubjectId(id, hash);

        return packet;
    }

}
