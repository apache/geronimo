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

package org.apache.geronimo.network.protocol;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;


/**
 * @version $Rev$ $Date$
 */
public class UpPacket {

    private ByteBuffer buffer;
    private Map metadata = new HashMap();

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public Object getMetadata(Object key) {
        return metadata.get(key);
    }

    public Object setMetadata(Object key, Object value) {
        return metadata.put(key, value);
    }
}
