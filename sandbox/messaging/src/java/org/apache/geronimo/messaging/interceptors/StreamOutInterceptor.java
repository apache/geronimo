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

package org.apache.geronimo.messaging.interceptors;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.messaging.Msg;
import org.apache.geronimo.messaging.io.PushSynchronization;
import org.apache.geronimo.messaging.io.PushSynchronizationAdaptor;
import org.apache.geronimo.messaging.io.ReplacerResolver;
import org.apache.geronimo.messaging.io.StreamManager;
import org.apache.geronimo.messaging.io.StreamOutputStream;

/**
 * Counterpart of StreamInInterceptor. It allows to push Msgs to an
 * OutputStream.
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:40 $
 */
public class StreamOutInterceptor
    implements MsgOutInterceptor
{

    private static final Log log = LogFactory.getLog(StreamOutInterceptor.class);
    
    /**
     * Null PushSerialization;
     */
    private static final PushSynchronization NULL_PUSH_SYNC =
        new PushSynchronizationAdaptor();
    
    /**
     * OutputStream to write to.
     */
    private final OutputStream out;
    
    /**
     * Used to serialize the Msgs to be pushed.
     */
    private final StreamOutputStream streamOutputStream;
    
    /**
     * Memory buffer used to serialize the Msgs prior to write them to the
     * actual OutputStream.
     */
    private final ByteArrayOutputStream memOut;

    /**
     * Push synchronization to be applied by this instance.
     */
    private final PushSynchronization pushSynchronization;
    
    /**
     * Pushes Msgs to an OutputStream. Msgs are written by a StreamOutputStream
     * using the provided StreamManager to encode InputStream in the
     * raw OutputStream.
     * 
     * @param anOut OutputStream to write to.
     * @param aManager Used to encode InputStream.
     * @param aSerialization PushSynchronization to be applied when pushing
     * Msgs.
     * @param aResolver Used to replace Objects to be written to anOut.
     * @exception IOException Indicates an I/O error.
     */
    public StreamOutInterceptor(OutputStream anOut, StreamManager aManager,
        PushSynchronization aSerialization, ReplacerResolver aResolver)
        throws IOException {
        if ( null == anOut ) {
            throw new IllegalArgumentException("OutputStream is required.");
        } else if ( null == aManager ) {
            throw new IllegalArgumentException("StreamManager is required.");
        }
        out = anOut;
        memOut = new ByteArrayOutputStream(2048);
        streamOutputStream = new StreamOutputStream(memOut, aManager, aResolver);
        if ( null == aSerialization ) {
            pushSynchronization = NULL_PUSH_SYNC; 
        } else {
            pushSynchronization = aSerialization;
        }
    }
    
    public void push(Msg aMessage) {
        try {
            Object opaque =
                pushSynchronization.beforePush(streamOutputStream, aMessage);
            streamOutputStream.writeObject(aMessage);
            pushSynchronization.afterPush(streamOutputStream, aMessage, opaque);
            streamOutputStream.reset();
            streamOutputStream.flush();
        } catch (IOException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
        synchronized(out) {
            try {
                memOut.writeTo(out);
                out.flush();
            } catch (IOException e) {
                log.error(e);
                throw new RuntimeException(e);
            }
        }
        memOut.reset();
    }
    
}
