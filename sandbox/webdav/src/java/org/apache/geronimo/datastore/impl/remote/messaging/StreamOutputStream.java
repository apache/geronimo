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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * This is the counterpart of StreamInputStream.
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/25 13:36:15 $
 */
public class StreamOutputStream
    extends DataOutputStream
{

    private final StreamManager streamManager;
    
    public StreamOutputStream(OutputStream anOut, StreamManager aManager) {
        super(anOut);
        if ( null == aManager ) {
            throw new IllegalArgumentException("StreamManager is required.");
        }
        streamManager = aManager;
    }

    public void writeStream(InputStream aStream) throws IOException {
        Object id = streamManager.register(aStream);
        writeObject(id);
    }

    public void writeObject(Object anObject) throws IOException {
        CustomObjectOutputStream objOut = new CustomObjectOutputStream();
        objOut.writeObject(anObject);
        objOut.flush();
    }
    
    /**
     * Gets the StreamManager used to resolve InputStream identifiers.
     * 
     * @return StreamManager.
     */
    public StreamManager getStreamManager() {
        return streamManager;
    }
    
    public class CustomObjectOutputStream extends ObjectOutputStream {

        public CustomObjectOutputStream() throws IOException, SecurityException {
            super(StreamOutputStream.this);
        }

        public void writeStream(InputStream aStream) throws IOException {
            StreamOutputStream.this.writeStream(aStream);
        }
        
    }
    
}
