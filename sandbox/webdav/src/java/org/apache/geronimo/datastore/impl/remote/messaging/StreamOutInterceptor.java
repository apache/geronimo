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

package org.apache.geronimo.datastore.impl.remote.messaging;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Counterpart of StreamInInterceptor. It allows to push Msg to an OutputStream.
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/16 14:48:58 $
 */
public class StreamOutInterceptor
    implements MsgOutInterceptor
{

    /**
     * Buffer size to be applied on top of the OutputStream when writting.
     */
    private static final int BUFFER_SIZE = 2048; 

    /**
     * Null PushSerialization;
     */
    private static final PushSynchronization NULL_PUSH_SYNC =
        new PushSynchronizationAdaptor();
    
    private static final Log log = LogFactory.getLog(StreamOutInterceptor.class);
    
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
     */
    public StreamOutInterceptor(OutputStream anOut, StreamManager aManager,
        PushSynchronization aSerialization)
        throws IOException {
        if ( null == anOut ) {
            throw new IllegalArgumentException("OutputStream is required.");
        } else if ( null == aManager ) {
            throw new IllegalArgumentException("StreamManager is required.");
        }
        out = new BufferedOutputStream(anOut, BUFFER_SIZE);
        memOut = new ByteArrayOutputStream(BUFFER_SIZE);
        streamOutputStream = new StreamOutputStream(memOut, aManager);
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
    
    /**
     * Allows an implementation to be notified when a Msg is about to be
     * pushed.
     */
    public interface PushSynchronization {
        /**
         * Notifies the implementation that a Msg is being pushed.
         * <BR>
         * This method is called before the actual push of the Msg.
         * 
         * @param anOut Used to write information before the Msg itself.
         * @param aMsg Msg being pushed. 
         * @return Opaque object which is passed by to this instance via
         * afterPush. It can be used to pass information between a beforePush
         * and a afterPush call.
         * @throws IOException Indicates that an I/O error has occured.
         */
        public Object beforePush(StreamOutputStream anOut, Msg aMsg)
            throws IOException;
        /**
         * Notifies the implementation that a Msg has been pushed.
         * 
         * @param anOut Used to write information after the Msg itself.
         * @param aMsg Msg which has just been pushed.
         * @param anOpaque Value returned by beforePush.
         * @throws IOException Indicates that an I/O error has occured.
         */
        public void afterPush(StreamOutputStream anOut, Msg aMsg,
            Object anOpaque)
            throws IOException;
    }
    
    /**
     * PushSynchronization adaptor.
     */
    public static class PushSynchronizationAdaptor
        implements PushSynchronization {
        public Object beforePush(StreamOutputStream anOut, Msg aMsg)
            throws IOException {
            return null;
        }
        public void afterPush(StreamOutputStream anOut, Msg aMsg,
            Object anOpaque) throws IOException {}
    }
    
}
