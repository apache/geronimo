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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.messaging.Msg;
import org.apache.geronimo.messaging.io.PopSynchronization;
import org.apache.geronimo.messaging.io.PopSynchronizationAdaptor;
import org.apache.geronimo.messaging.io.ReplacerResolver;
import org.apache.geronimo.messaging.io.StreamInputStream;
import org.apache.geronimo.messaging.io.StreamManager;

/**
 * Allows popping Msgs from an InputStream.
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:40 $
 */
public class StreamInInterceptor
    implements MsgInInterceptor
{

    private static final Log log = LogFactory.getLog(StreamInInterceptor.class);

    /**
     * Null PopSynchronizarion;
     */
    private static final PopSynchronization NULL_POP_SYNC =
        new PopSynchronizationAdaptor();
    
    /**
     * Raw InputStream to pop Msgs from.
     */
    private final InputStream rawIn;
    
    /**
     * Used to read in Msg mode from the raw InputStream.
     */
    private final StreamInputStream streamInputStream;
    
    /**
     * Pop synchronization to be applied by this instance.
     */
    private final PopSynchronization popSynchronization;
    
    /**
     * Pops Msgs from an InputStream. Msgs are read by a StreamInputStream
     * using the provided StreamManager to resolve InputStreams encoded in the
     * raw InputStream.
     * 
     * @param anIn InputStream to read from.
     * @param aManager Used to resolve encoded InputStreams.
     * @param aSynchronization PopSynchronization applied by this instance.
     * @param aResolver Used to resolve Objects read from anIn.
     * @exception IOException Indicates an I/O error.
     */
    public StreamInInterceptor(InputStream anIn, StreamManager aManager,
        PopSynchronization aSynchronization, ReplacerResolver aResolver)
        throws IOException {
        if ( null == anIn ) {
            throw new IllegalArgumentException("InputStream is required.");
        } else if ( null == aManager ) {
            throw new IllegalArgumentException("StreamManager is required.");
        }
        rawIn = anIn;
        streamInputStream = new StreamInputStream(anIn, aManager, aResolver);
        if ( null == aSynchronization ) {
            popSynchronization = NULL_POP_SYNC;
        } else {
            popSynchronization = aSynchronization;
        }
    }
    
    public Msg pop() {
        Msg msg;
        try {
            synchronized (rawIn) {
                Object opaque = popSynchronization.beforePop(streamInputStream);
                msg = (Msg) streamInputStream.readObject();
                popSynchronization.afterPop(streamInputStream, msg, opaque);
            }
        } catch (ClassNotFoundException e) {
            log.error(e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
        return msg;
    }
    
}
