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

import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * An Externalizable InputStream Wrapper.
 * <BR>
 * The Externalization MUST be performed by the 
 * StreamOutputStream/StreamInputStream pair.
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/25 13:36:15 $
 */
public class GInputStream
    extends InputStream
    implements Externalizable
{

    /**
     * Wrapped InputStream.
     */
    private InputStream content;

    /**
     * Requires for Externalization. Do not use.
     */
    public GInputStream() {}
    
    /**
     * Wraps the specified InputStream.
     * 
     * @param anIn InputStream to be wrapped.
     */
    public GInputStream(InputStream anIn) {
        content = anIn;
    }
    
    /**
     * Gets the wrapped InputStream
     * 
     * @return Wrapped InputStream.
     */
    public InputStream getRawInputStream() {
        return content; 
    }

    public int read() throws IOException {
        return content.read();
    }

    /**
     * Serializes the wrapped InputStream. The serialization MUST be performed
     * by a StreamOutputStream, which knows how to encode a stream into an
     * OutputStream.
     */
    public void writeExternal(ObjectOutput anOut) throws IOException {
        if ( !(anOut instanceof StreamOutputStream.CustomObjectOutputStream) ) {
            throw new IOException("Must be serialized by a StreamOutputStream.");
        }
        StreamOutputStream.CustomObjectOutputStream objOut =
            (StreamOutputStream.CustomObjectOutputStream) anOut;
        objOut.writeStream(content);
        objOut.flush();
    }

    public void readExternal(ObjectInput anIn) throws IOException, ClassNotFoundException {
        if ( !(anIn instanceof StreamInputStream.CustomObjectInputStream) ) {
            throw new IOException("Must be deserialized by a StreamInputStream.");
        }
        StreamInputStream.CustomObjectInputStream objIn =
            (StreamInputStream.CustomObjectInputStream) anIn;
        content = objIn.readInputStream();
    }

}
