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

package org.apache.geronimo.messaging.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;


/**
 * Allows to read an InputStream from the underlying InputStream. More
 * accurately, it allows retrieving the InputStream identifier encoded in the
 * underlying InputStream. This identifier is then passed to the StreamManager,
 * which returns the InputStream having the provided identifier.
 * 
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:41 $
 */
public class StreamInputStream
    extends ObjectInputStream
{

    /**
     * ClassDescriptors cache.
     */
    private final List classDescCache;
    
    /**
     * StreamManager to be used to retrieve InputStream encoded in the
     * underlying InputStream.
     */
    private final StreamManager streamManager;

    /**
     * Used to resolveObject.
     */
    private final ReplacerResolver resolver;
    
    /**
     * Creates an InputStream operating on the provided InputStream and using
     * the provided StreamManager to retrieve encoded InputStream.
     * 
     * @param anIn InpuStream to be read from.
     * @param aManager StreamManager.
     * @param aResolver Used to resolveObject.
     * @throws IOException If an I/O error has occured.
     */
    public StreamInputStream(InputStream anIn, StreamManager aManager,
        ReplacerResolver aResolver)
        throws IOException {
        super(anIn);
        enableResolveObject(null != aResolver);
        if ( null == aManager ) {
            throw new IllegalArgumentException("StreamManager is required.");
        }
        streamManager = aManager;
        resolver = aResolver;
        
        classDescCache = new ArrayList();
    }

    /**
     * Reads the InputStream identifier encoded in the underlying InputStream
     * and returns the InputStream having this identifier and registered by
     * the StreamManager.
     * 
     * @return InputStream encoded in the underlying InputStream.
     * @throws IOException May indicates that the StreamManager does not know
     * about the encoded identifer.
     */
    public InputStream readStream() throws IOException {
        Object id;
        try {
            id = readObject();
        } catch (ClassNotFoundException e) {
            IOException ex = new IOException();
            ex.initCause(e);
            throw ex;
        }
        InputStream returned = streamManager.retrieve(id);
        return returned;
    }

    /**
     * It is critical to read nothing during the creation of ObjectInputStream.
     * <BR>
     * Indeed, StreamInputStream and StreamOutputStream are used by
     * StreamInInterceptor and StreamOutInterceptor respectively. It should be
     * possible to instantiate these two objects at the same time, for instance
     * to wrap the InputStream and OutputStream of a Socket.
     * <BR>
     * If ObjectInputStream reads a StreamHeader during its creation, then it
     * is not possible to create these two classes at the same time.
     */
    protected void readStreamHeader()
        throws IOException, StreamCorruptedException {}
    
    /**
     * Counterpart of StreamOutputStream.writeClassDescriptor.
     */
    protected ObjectStreamClass readClassDescriptor()
        throws IOException, ClassNotFoundException {
        ObjectStreamClass streamClass;
        int type = read();
        if ( StreamOutputStream.NOT_CACHED == type ) {
            int id = readInt();
            streamClass = super.readClassDescriptor();
            classDescCache.add(id, streamClass);
        } else {
            int id = readInt();
            try {
                streamClass = (ObjectStreamClass) classDescCache.get(id);
            } catch (IndexOutOfBoundsException e) {
                throw new StreamCorruptedException(
                        "No ObjectStreamClass has the identifier {" + id + "}");
            }
        }
        return streamClass;
    }
    
    protected Object resolveObject(Object obj) throws IOException {
        return resolver.resolveObject(obj);
    }
    
}
