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

package org.apache.geronimo.network.protocol.control.commands;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.geronimo.network.protocol.PacketField;
import org.apache.geronimo.network.protocol.PacketFieldFactory;
import org.apache.geronimo.network.protocol.ProtocolException;


/**
 * @version $Rev$ $Date$
 */
public class MenuItemPacketFieldReader {

    private static MenuItemPacketFieldReader ourInstance = new MenuItemPacketFieldReader();
    protected Map factories = new HashMap();

    public static MenuItemPacketFieldReader getInstance() {
        return ourInstance;
    }

    public void register(byte key, PacketFieldFactory factory) {
        factories.put(new Byte(key), factory);
    }

    /**
     * We need to read from the same buffer so, we don't slice
     *
     * @param buffer the buffer to be read to obtain the field
     * @return the field in for from of a packet
     * @throws ProtocolException
     */
    public PacketField read(ByteBuffer buffer) throws ProtocolException {
        Byte key = new Byte(buffer.get());
        PacketFieldFactory factory = (PacketFieldFactory) factories.get(key);

        if (factory == null) throw new ProtocolException("No factory registered for " + key);

        return factory.create(buffer);
    }

    private MenuItemPacketFieldReader() {
        register(MenuItem.CREATE, new CreateInstanceMenuItem());
        register(MenuItem.ATTRIBUTE, new SetAttributeMenuItem());
        register(MenuItem.REFERENCE, new SetReferenceMenuItem());
    }

}
