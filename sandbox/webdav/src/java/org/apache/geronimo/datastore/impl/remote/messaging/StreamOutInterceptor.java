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
 * @version $Revision: 1.2 $ $Date: 2004/03/11 15:36:14 $
 */
public class StreamOutInterceptor
    implements MsgOutInterceptor
{

    /**
     * Buffer size to be applied on top of the OutputStream when writting.
     */
    private static final int BUFFER_SIZE = 2048; 
    
    
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
     * Pushes Msgs to an OutputStream. Msgs are written by a StreamOutputStream
     * using the provided StreamManager to encode InputStream in the
     * raw OutputStream.
     * 
     * @param anOut OutputStream to write to.
     * @param aManager Used to encode InputStream.
     */
    public StreamOutInterceptor(OutputStream anOut, StreamManager aManager) {
        if ( null == anOut ) {
            throw new IllegalArgumentException("OutputStream is required.");
        } else if ( null == aManager ) {
            throw new IllegalArgumentException("StreamManager is required.");
        }
        out = new BufferedOutputStream(anOut, BUFFER_SIZE);
        memOut = new ByteArrayOutputStream();
        streamOutputStream = new StreamOutputStream(memOut, aManager);
    }
    
    public void push(Msg aMessage) {
        try {
            streamOutputStream.writeObject(aMessage);
            streamOutputStream.flush();
        } catch (IOException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
        synchronized(out) {
            try {
                out.write(memOut.toByteArray());
                out.flush();
            } catch (IOException e) {
                log.error(e);
                throw new RuntimeException(e);
            }
        }
        memOut.reset();
    }

}
