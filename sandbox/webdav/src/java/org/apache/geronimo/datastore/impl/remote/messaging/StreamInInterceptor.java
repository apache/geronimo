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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Allows popping Msgs from an InputStream.
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/16 14:48:58 $
 */
public class StreamInInterceptor
    implements MsgInInterceptor
{

    /**
     * Buffer size to be applied on top of the InputStream when reading.
     */
    private static final int BUFFER_SIZE = 2048; 
    
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
     * InputStream to pop Msgs from.
     */
    private final InputStream in;
    
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
     * using the provided StreamManager to resolve InputStream encoded in the
     * raw InputStream.
     * 
     * @param anIn InputStream to read from.
     * @param aManager Used to resolve encoded InputStream.
     * @param aSynchronization PopSynchronization applied by this instance.
     */
    public StreamInInterceptor(InputStream anIn, StreamManager aManager,
        PopSynchronization aSynchronization)
        throws IOException {
        if ( null == anIn ) {
            throw new IllegalArgumentException("InputStream is required.");
        } else if ( null == aManager ) {
            throw new IllegalArgumentException("StreamManager is required.");
        }
        rawIn = anIn;
        in = new BufferedInputStream(anIn, BUFFER_SIZE);
        streamInputStream = new StreamInputStream(in, aManager);
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

    /**
     * Allows an implementation to be notified when a Msg is about to be
     * popped.
     */
    public interface PopSynchronization {
        /**
         * Notifies the implementation that a Msg is being popped.
         * <BR>
         * This method is called before the actual pop of the Msg.
         * 
         * @param anIn Used to read information from the input stream before
         * the Msg itself. 
         * @return Opaque object which is passed by to this instance via
         * afterPop. It can be used to pass information between a beforePop
         * and a afterPop call.
         * @throws IOException Indicates that an I/O error has occured.
         */
        public Object beforePop(StreamInputStream anIn)
            throws IOException ;
        
        /**
         * Notifies the implementation that a Msg has been popped.
         * 
         * @param anIn Used to read information from the input stream after
         * the Msg itself. 
         * @param aMsg Msg which has just been popped.
         * @param anOpaque Value returned by beforePop.
         * @throws IOException Indicates that an I/O error has occured.
         */
        public void afterPop(StreamInputStream anIn, Msg aMsg, Object anOpaque)
            throws IOException;
    }
    
    /**
     * PopSynchronizarion adaptor.
     */
    public static class PopSynchronizationAdaptor implements PopSynchronization {
        public Object beforePop(StreamInputStream anIn)
            throws IOException {
            return null;
        }
        public void afterPop(StreamInputStream anIn, Msg aMsg,
            Object anOpaque) throws IOException {}
    }
    
}
