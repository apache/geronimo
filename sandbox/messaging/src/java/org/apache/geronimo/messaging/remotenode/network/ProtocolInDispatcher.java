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
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.messaging.Msg;
import org.apache.geronimo.messaging.MsgProducer;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;
import org.apache.geronimo.messaging.io.PopSynchronization;
import org.apache.geronimo.messaging.io.PopSynchronizationAdaptor;
import org.apache.geronimo.messaging.io.ReplacerResolver;
import org.apache.geronimo.messaging.io.StreamInputStream;
import org.apache.geronimo.messaging.io.StreamManager;
import org.apache.geronimo.network.protocol.AbstractProtocol;
import org.apache.geronimo.network.protocol.DownPacket;
import org.apache.geronimo.network.protocol.Protocol;
import org.apache.geronimo.network.protocol.ProtocolException;
import org.apache.geronimo.network.protocol.UpPacket;

/**
 *
 * @version $Rev$ $Date$
 */
public class ProtocolInDispatcher
    implements MsgProducer
{
    
    private static final Log log = LogFactory.getLog(ProtocolInDispatcher.class);

    /**
     * Null PopSynchronizarion;
     */
    private static final PopSynchronization NULL_POP_SYNC =
        new PopSynchronizationAdaptor();
    
    /**
     * Protocol to pop Msgs from.
     */
    private final Protocol protocol;
    
    /**
     * Used to deserialize Msgs.
     */
    private final StreamInputStream streamInputStream;
    
    /**
     * Pop synchronization to be applied by this instance.
     */
    private final PopSynchronization popSynchronization;
    
    private MsgOutInterceptor out;
    
    private final RefillableInputStream in;
    
    /**
     * Pops Msgs from an InputStream. Msgs are read by a StreamInputStream
     * using the provided StreamManager to resolve InputStream encoded in the
     * raw InputStream.
     * 
     * @param aProtocol InputStream to read from.
     * @param aManager Used to resolve encoded InputStream.
     * @param aSynchronization PopSynchronization applied by this instance.
     * @param aResolver Used to resolve Objects read from anIn.
     */
    public ProtocolInDispatcher(Protocol aProtocol, StreamManager aManager,
        PopSynchronization aSynchronization, ReplacerResolver aResolver)
        throws IOException {
        if ( null == aProtocol ) {
            throw new IllegalArgumentException("Protocol is required.");
        } else if ( null == aManager ) {
            throw new IllegalArgumentException("StreamManager is required.");
        }
        aProtocol.setUpProtocol(new InboundMsgNotifier());
        protocol = aProtocol;
        in = new RefillableInputStream();
        streamInputStream = new StreamInputStream(in, aManager, aResolver);
        if ( null == aSynchronization ) {
            popSynchronization = NULL_POP_SYNC;
        } else {
            popSynchronization = aSynchronization;
        }
    }
    
    private void dispatch() throws ProtocolException {
        Msg msg;
        try {
            synchronized (in) {
                Object opaque = popSynchronization.beforePop(streamInputStream);
                msg = (Msg) streamInputStream.readObject();
                popSynchronization.afterPop(streamInputStream, msg, opaque);
            }
        } catch (ClassNotFoundException e) {
            throw new ProtocolException(e);
        } catch (IOException e) {
            throw new ProtocolException(e);
        }
        out.push(msg);
    }

    public void setMsgProducerOut(MsgOutInterceptor aMsgOut) {
        out = aMsgOut;
    }

    private class RefillableInputStream extends InputStream {
        private int pos = 0;
        private byte[] buffer;
        public int read() throws IOException {
            return buffer[pos++];
        }
        public void refill(byte[] aNewBuffer) {
            if ( null == buffer ) {
                buffer = aNewBuffer;
                return;
            }
            int remaining = buffer.length - pos;
            int newSize = remaining + aNewBuffer.length;
            byte[] newBuffer = new byte[newSize];
            System.arraycopy(buffer, pos, newBuffer, 0, remaining);
            System.arraycopy(aNewBuffer, 0, newBuffer, remaining, aNewBuffer.length);
            buffer = newBuffer;
            pos = 0;
        }
    }
    
    private class InboundMsgNotifier extends AbstractProtocol {

        public void setup() throws ProtocolException {
        }

        public void drain() throws ProtocolException {
        }

        public void teardown() throws ProtocolException {
        }

        public void sendUp(UpPacket packet) throws ProtocolException {
            ByteBuffer byteBuffer = packet.getBuffer();
            byte[] buffer = new byte[byteBuffer.remaining()];
            byteBuffer.get(buffer);
            synchronized (in) {
                in.refill(buffer);
            }
            dispatch();
        }

        public void sendDown(DownPacket packet) throws ProtocolException {
        }
        
    }
    
}
