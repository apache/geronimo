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

import java.io.IOException;

import org.apache.geronimo.messaging.CommunicationException;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;
import org.apache.geronimo.messaging.io.IOContext;
import org.apache.geronimo.messaging.io.PopSynchronization;
import org.apache.geronimo.messaging.io.PushSynchronization;
import org.apache.geronimo.messaging.io.ReplacerResolver;
import org.apache.geronimo.messaging.io.StreamManager;
import org.apache.geronimo.messaging.remotenode.RemoteNodeConnection;
import org.apache.geronimo.network.protocol.Protocol;
import org.apache.geronimo.network.protocol.ProtocolException;

/**
 * Abstract implememtation for the RemoteNodeConnection contracts.
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:42 $
 */
public abstract class AbstractRemoteNodeConnection
    implements RemoteNodeConnection
{

    private final IOContext ioContext;
    protected Protocol protocol;
    private MsgOutInterceptor msgOut;
    private ProtocolInDispatcher inDispatcher;

    public AbstractRemoteNodeConnection(IOContext anIOContext) {
        if ( null == anIOContext ) {
            throw new IllegalArgumentException("IOContext is required.");
        }
        ioContext = anIOContext;
    }
    
    public void setMsgProducerOut(MsgOutInterceptor aMsgOut) {
        if ( null == aMsgOut ) {
            return;
        } if ( null == inDispatcher ) {
            throw new IllegalStateException("Connection is not opened.");
        }
        inDispatcher.setMsgProducerOut(aMsgOut);
    }

    public MsgOutInterceptor getMsgConsumerOut() {
        if ( null == msgOut ) {
            throw new IllegalStateException("Connection is not opened.");
        }
        return msgOut;
    }
    
    public void open() throws IOException, CommunicationException {
        try {
            protocol = newProtocol();
        } catch (ProtocolException e) {
            IOException exception = new IOException("Can not create Protocol.");
            exception.initCause(e);
            throw exception;
        }
        StreamManager streamManager = ioContext.getStreamManager();
        ReplacerResolver replacerResolver = ioContext.getReplacerResolver();
        PushSynchronization pushSynchronization = ioContext.getPushSynchronization();
        PopSynchronization popSynchronization = ioContext.getPopSynchronization();
        msgOut = new ProtocolOutInterceptor(protocol,
            streamManager, pushSynchronization, replacerResolver);
        inDispatcher = new ProtocolInDispatcher(protocol,
            streamManager, popSynchronization, replacerResolver);
    }

    protected abstract Protocol newProtocol() throws ProtocolException;
    
    public void close() throws IOException, CommunicationException {
        msgOut = null;
        inDispatcher = null;
        try {
            protocol.drain();
        } catch (ProtocolException e) {
            IOException exception = new IOException("Can not drain Protocol");
            exception.initCause(e);
            throw exception;
        }
    }

}
