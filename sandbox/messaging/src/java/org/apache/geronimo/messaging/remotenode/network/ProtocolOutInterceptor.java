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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.messaging.CommunicationException;
import org.apache.geronimo.messaging.Msg;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;
import org.apache.geronimo.messaging.io.PushSynchronization;
import org.apache.geronimo.messaging.io.PushSynchronizationAdaptor;
import org.apache.geronimo.messaging.io.ReplacerResolver;
import org.apache.geronimo.messaging.io.StreamManager;
import org.apache.geronimo.messaging.io.StreamOutputStream;
import org.apache.geronimo.network.protocol.PlainDownPacket;
import org.apache.geronimo.network.protocol.Protocol;
import org.apache.geronimo.network.protocol.ProtocolException;

/**
 *
 * @version $Rev$ $Date$
 */
public class ProtocolOutInterceptor
    implements MsgOutInterceptor
{

    /**
     * Null PushSerialization;
     */
    private static final PushSynchronization NULL_PUSH_SYNC =
        new PushSynchronizationAdaptor();
    
    private static final Log log = LogFactory.getLog(ProtocolOutInterceptor.class);
    
    /**
     * Protocol to write to.
     */
    private final Protocol protocol;
    
    /**
     * Used to serialize the Msgs to be pushed.
     */
    private final StreamOutputStream streamOutputStream;
    
    /**
     * Memory buffer used to serialize the Msgs prior to send them down.
     */
    private final ByteArrayOutputStream memOut;

    /**
     * Push synchronization to be applied by this instance.
     */
    private final PushSynchronization pushSynchronization;
    
    /**
     * Pushes Msgs to a Protocol. Msgs are written by a StreamOutputStream
     * using the provided StreamManager to encode InputStream in the
     * raw OutputStream.
     * 
     * @param aProtocol Protocol to write to.
     * @param aManager Used to encode InputStream.
     * @param aSerialization PushSynchronization to be applied when pushing
     * Msgs.
     * @param aResolver Used to replace Objects to be written to anOut.
     */
    public ProtocolOutInterceptor(Protocol aProtocol, StreamManager aManager,
        PushSynchronization aSerialization, ReplacerResolver aResolver)
        throws IOException {
        if ( null == aProtocol ) {
            throw new IllegalArgumentException("Protocol is required.");
        } else if ( null == aManager ) {
            throw new IllegalArgumentException("StreamManager is required.");
        }
        protocol = aProtocol;
        memOut = new ByteArrayOutputStream(2048);
        streamOutputStream = new StreamOutputStream(memOut, aManager, aResolver);
        if ( null == aSerialization ) {
            pushSynchronization = NULL_PUSH_SYNC; 
        } else {
            pushSynchronization = aSerialization;
        }
    }
    
    public void push(Msg aMsg) {
        byte[] marshalled;
        try {
            synchronized(streamOutputStream) {
                Object opaque =
                    pushSynchronization.beforePush(streamOutputStream, aMsg);
                streamOutputStream.writeObject(aMsg);
                pushSynchronization.afterPush(streamOutputStream, aMsg, opaque);
                streamOutputStream.reset();
                streamOutputStream.flush();
                marshalled = memOut.toByteArray();
                memOut.reset();
            }
        } catch (IOException e) {
            log.error(e);
            throw new CommunicationException(e);
        }
        PlainDownPacket downPacket = new PlainDownPacket();
        ByteBuffer buffer = ByteBuffer.allocate(marshalled.length);
        buffer.put(marshalled);
        buffer.flip();
        downPacket.setBuffers(Collections.singleton(buffer));
        synchronized(protocol) {
            try {
                protocol.sendDown(downPacket);
            } catch (ProtocolException e) {
                log.error(e);
                throw new CommunicationException(e);
            }
        }
    }

}
