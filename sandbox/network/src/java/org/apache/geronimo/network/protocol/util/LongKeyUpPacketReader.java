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

package org.apache.geronimo.network.protocol.util;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.geronimo.network.protocol.PacketFactory;
import org.apache.geronimo.network.protocol.PacketReader;
import org.apache.geronimo.network.protocol.ProtocolException;
import org.apache.geronimo.network.protocol.UpPacket;


/**
 * @version $Rev$ $Date$
 */
public class LongKeyUpPacketReader implements PacketReader {

    static protected Map factories = new HashMap();

    public static void register(Long key, PacketFactory factory) {
        factories.put(key, factory);
    }

    public UpPacket read(ByteBuffer buffer) throws ProtocolException {
        Long key = new Long(buffer.getLong());
        PacketFactory factory = (PacketFactory) factories.get(key);

        if (factory == null) throw new ProtocolException("No factory registered for " + key);

        return factory.create(buffer.slice());
    }

}
