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

package org.apache.geronimo.security;

import java.io.Serializable;


/**
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:58:08 $
 */
public class SubjectId implements Serializable {
    private final Long subjectId;
    private final byte[] hash;
    private transient int hashCode;
    private transient String name;

    public SubjectId(Long subjectId, byte[] hash) {
        this.subjectId = subjectId;
        this.hash = hash;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public byte[] getHash() {
        return hash;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof SubjectId)) return false;

        SubjectId another = (SubjectId) obj;
        if (!another.subjectId.equals(subjectId)) return false;
        for (int i = 0; i < hash.length; i++) {
            if (another.hash[i] != hash[i]) return false;
        }
        return true;
    }

    public String toString() {
        if (name == null) {
            StringBuffer buffer = new StringBuffer();
            buffer.append('[');
            buffer.append(subjectId);
            buffer.append(":0x");
            for (int i = 0; i < hash.length; i++) {
                buffer.append(HEXCHAR[(hash[i]>>>4)&0x0F]);
                buffer.append(HEXCHAR[(hash[i]    )&0x0F]);
            }
            buffer.append(']');
            name = buffer.toString();
        }
        return name;
    }

    /**
     * Returns a hashcode for this LoginModuleId.
     *
     * @return a hashcode for this LoginModuleId.
     */
    public int hashCode() {
        if (hashCode == 0) {
            for (int i = 0; i < hash.length; i++) {
                hashCode ^= hash[i];
            }
            hashCode ^= subjectId.hashCode();
        }
        return hashCode;
    }

    private static final char[] HEXCHAR = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };
}
