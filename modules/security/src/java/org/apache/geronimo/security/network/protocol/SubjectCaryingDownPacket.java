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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.geronimo.network.protocol.util.ByteKeyDownPacket;
import org.apache.geronimo.network.protocol.util.PacketUtil;
import org.apache.geronimo.security.SubjectId;


/**
 * @version $Revision: 1.1 $ $Date: 2004/03/10 02:15:50 $
 */
class SubjectCaryingDownPacket extends ByteKeyDownPacket implements SubjectCarryingPackets {

    private SubjectId subjectId;
    private Collection buffers;

    public SubjectCaryingDownPacket() {
        super(SUBJECT);
    }

    public SubjectId getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(SubjectId subjectId) {
        this.subjectId = subjectId;
    }

    protected Collection getChildBuffers() {
        ArrayList list = new ArrayList(2 + buffers.size());

        list.add(PacketUtil.putLong(subjectId.getSubjectId()).flip());
        list.add(PacketUtil.putByteArray(subjectId.getHash()).flip());

        list.addAll(buffers);

        return list;
    }

    public void setBuffers(Collection buffers) {
        this.buffers = buffers;
    }

}
