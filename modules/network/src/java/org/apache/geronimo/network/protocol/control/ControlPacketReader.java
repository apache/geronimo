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

import org.apache.geronimo.network.protocol.util.ByteKeyUpPacketReader;


/**
 * @version $Revision: 1.2 $ $Date: 2004/03/10 09:59:14 $
 */
public class ControlPacketReader extends ByteKeyUpPacketReader {

    private static ControlPacketReader ourInstance = new ControlPacketReader();

    public static ControlPacketReader getInstance() {
        return ourInstance;
    }

    private ControlPacketReader() {
        register(AbstractControlProtocol.PASSTHROUGH, new PassthroughUpPacket());
        register(AbstractControlProtocol.BOOT_REQUEST, new BootRequestUpPacket());
        register(AbstractControlProtocol.BOOT_RESPONSE, new BootResponseUpPacket());
        register(AbstractControlProtocol.BOOT_SUCCESS, new BootSuccessUpPacket());
        register(AbstractControlProtocol.SHUTDOWN_REQ, new ShutdownRequestUpPacket());
        register(AbstractControlProtocol.SHUTDOWN_ACK, new ShutdownAcknowledgeUpPacket());
        register(AbstractControlProtocol.NOBOOT, new NoBootUpPacket());
    }
}
