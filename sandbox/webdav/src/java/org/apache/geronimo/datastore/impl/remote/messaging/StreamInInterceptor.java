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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Allows popping Msg from an InputStream.
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/25 13:36:15 $
 */
public class StreamInInterceptor
    implements MsgInInterceptor
{

    private static final Log log = LogFactory.getLog(StreamInInterceptor.class);
    
    /**
     * InputStream to pop Msg from.
     */
    private final InputStream in;
    
    /**
     * Used to read in Msg mode from the raw InputStream.
     */
    private final StreamInputStream streamInputStream;
    
    /**
     * Pops Msgs from an InputStream. Msgs are read by a StreamInputStream
     * using the provided StreamManager to resolve InputStream encoded in the
     * raw InputStream.
     * 
     * @param anIn InputStream to read from.
     * @param aManager Used to resolve encoded InputStream.
     */
    public StreamInInterceptor(InputStream anIn, StreamManager aManager) {
        if ( null == anIn ) {
            throw new IllegalArgumentException("InputStream is required.");
        } else if ( null == aManager ) {
            throw new IllegalArgumentException("StreamManager is required.");
        }
        in = anIn;
        streamInputStream = new StreamInputStream(in, aManager);
    }
    
    public Msg pop() {
        Msg msg;
        try {
            msg = (Msg) streamInputStream.readObject();
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
