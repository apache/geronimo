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
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;


/**
 * This is the counterpart of StreamInputStream.
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:41 $
 */
public class StreamOutputStream
    extends ObjectOutputStream
{

    /**
     * First byte written by writeClassDescriptor to indicate that the
     * ObjectStreamClass to be written is new.
     * <BR>
     * StreamOutputStream writes just after this byte an int, which will be
     * used subsequently to send the same ObjectStreamClass (having the same
     * SUID).
     * <BR>
     * After this identifier, the actual ObjectStreamClass is written.
     */
    public static final byte NOT_CACHED = 0x01;
    
    /**
     * First byte written by writeClassDescriptor to indicate that the
     * ObjectStreamClass to be written has already been provided.
     * <BR>
     * StreamOutputStream writes just after this byte an int, which is the 
     * identifier of the ObjectStreamClass being written.\
     * <BR>
     * See NOT_CACHED for more details.
     */
    public static final byte CACHED = 0x02;

    /**
     * Used to generate identifiers for cached ObjectStreamClasses.
     */
    private int seqID;
    
    private final StreamManager streamManager;

    /**
     * Used by replaceObject.
     */
    private final ReplacerResolver resolver;
    
    /**
     * ClassDescriptors to Integer map.
     */
    private final Map classDescCache;
    
    public StreamOutputStream(OutputStream anOut, StreamManager aManager,
        ReplacerResolver aResolver)
        throws IOException {
        super(anOut);
        enableReplaceObject(null != aResolver);
        if ( null == aManager ) {
            throw new IllegalArgumentException("StreamManager is required.");
        }
        streamManager = aManager;
        classDescCache = new HashMap();
        resolver = aResolver;
    }

    public void writeStream(InputStream aStream) throws IOException {
        Object id = streamManager.register(aStream);
        writeObject(id);
    }

    /**
     * Gets the StreamManager used to resolve InputStream identifiers.
     * 
     * @return StreamManager.
     */
    public StreamManager getStreamManager() {
        return streamManager;
    }

    /**
     * It is critical to avoid to write 
     */
    protected void writeStreamHeader() throws IOException {}

    /**
     * ObjectStreamClasses are not systematically written to the stream.
     * Instead, the very first time that a given ObjectStreamClass needs to be
     * written, this implementation assigns it an identifier.
     * <BR>
     * This latter will be written for all the remaining requests.
     */
    protected void writeClassDescriptor(ObjectStreamClass desc)
        throws IOException {
        Long descKey = new Long(desc.getSerialVersionUID()); 
        Integer id = (Integer) classDescCache.get(descKey);
        if ( null == id ) {
            id = new Integer(seqID++);
            classDescCache.put(descKey, id);
            write(NOT_CACHED);
            writeInt(id.intValue());
            super.writeClassDescriptor(desc);
        } else {
            write(CACHED);
            writeInt(id.intValue());
        }
    }
    
    protected Object replaceObject(Object obj) throws IOException {
        return resolver.replaceObject(obj);
    }
    
}
