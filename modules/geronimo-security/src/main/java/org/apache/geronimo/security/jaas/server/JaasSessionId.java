/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.security.jaas.server;

import java.io.Serializable;


/**
 * @version $Rev$ $Date$
 */
public class JaasSessionId implements Serializable {
    private final long sessionId;
    private final byte[] hash;
    private transient int hashCode;
    private transient String name;

    public JaasSessionId(long sessionId, byte[] hash) {
        this.sessionId = sessionId;
        this.hash = hash;
    }

    public long getSessionId() {
        return sessionId;
    }

    public byte[] getHash() {
        return hash;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof JaasSessionId)) return false;

        JaasSessionId another = (JaasSessionId) obj;
        if (another.sessionId != sessionId) return false;
        for (int i = 0; i < hash.length; i++) {
            if (another.hash[i] != hash[i]) return false;
        }
        return true;
    }

    public String toString() {
        if (name == null) {
            StringBuffer buffer = new StringBuffer();
            buffer.append('[');
            buffer.append(sessionId);
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
            hashCode ^= (int)(sessionId ^ (sessionId >>> 32));
        }
        return hashCode;
    }

    private static final char[] HEXCHAR = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };
}
