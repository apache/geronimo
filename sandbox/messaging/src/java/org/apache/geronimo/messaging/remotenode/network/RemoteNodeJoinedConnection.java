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

package org.apache.geronimo.messaging.remotenode.network;

import org.apache.geronimo.messaging.io.IOContext;
import org.apache.geronimo.network.protocol.Protocol;
import org.apache.geronimo.network.protocol.ProtocolException;

/**
 *
 * @version $Revision: 1.2 $ $Date: 2004/07/20 00:15:05 $
 */
public class RemoteNodeJoinedConnection
    extends AbstractRemoteNodeConnection
{

    public RemoteNodeJoinedConnection(IOContext anIOContext,
        Protocol aProtocol) {
        super(anIOContext);
        if ( null == aProtocol ) {
            throw new IllegalArgumentException("Protocol is required.");
        }
        protocol = aProtocol;
    }

    protected Protocol newProtocol() throws ProtocolException {
        return protocol;
    }
    
}
