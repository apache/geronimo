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

import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

import org.apache.geronimo.messaging.BaseEndPoint;
import org.apache.geronimo.messaging.EndPoint;
import org.apache.geronimo.messaging.Node;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.Request;

/**
 * StreamManager implementation.
 *
 * @version $Revision: 1.4 $ $Date: 2004/06/10 23:12:25 $
 */
public class StreamManagerImpl
    extends BaseEndPoint
    implements EndPoint, StreamManager
{

    /**
     * Used to signal an end of input stream.
     */
    public static final byte[] NULL_READ = new byte[0];
    
    /**
     * Read block size.
     */
    private static final int READ_SIZE = 2048;

    /**
     * Null input stream identifier.
     */
    private static final ID NULL_INPUT_STREAM = new ID(null);

    /**
     * Node owning this manager.
     */
    protected final NodeInfo owningNode; 
    
    /**
     * identifier to input stream map.
     */
    private final Map inputStreams;

    private final ReplacerResolver replacerResolver;
    
    /**
     * Creates a manager owned by the specified node.
     * 
     * @param aNode Node containing this instance.
     */
    public StreamManagerImpl(Node aNode) {
        super(aNode, StreamManager.NAME);
        if ( null == aNode ) {
            throw new IllegalArgumentException("Node is required.");
        }
        owningNode = aNode.getNodeInfo();
        inputStreams = new HashMap();
        
        replacerResolver = new InputStreamReplacerResolver();
    }
    
    public void start() {
        replacerResolver.online();
        node.getReplacerResolver().append(replacerResolver);
    }
    
    public void stop() {
        replacerResolver.offline();
    }
    
    public Object register(InputStream anIn) {
        if ( null == anIn ) {
            return NULL_INPUT_STREAM;
        }
        Object streamID = new ID(owningNode);
        synchronized(inputStreams) {
            inputStreams.put(streamID, anIn);
        }
        return streamID;
    }

    public InputStream retrieve(Object anId) throws IOException {
        return new ProxyInputStream(anId);
    }

    public byte[] retrieveLocalNext(Object anID)
        throws IOException {
        InputStream in;
        synchronized(inputStreams) {
            in = (InputStream) inputStreams.get(anID);
        }
        if ( null == in ) {
            return NULL_READ;
        }
        byte[] buffer = new byte[READ_SIZE];
        synchronized (in) {
            int nbRead = in.read(buffer);
            if (-1 == nbRead) {
                in.close();
                return NULL_READ;
            }
            if (nbRead < buffer.length) {
                byte[] buffer2 = new byte[nbRead];
                System.arraycopy(buffer, 0, buffer2, 0, nbRead);
                buffer = buffer2;
                synchronized(inputStreams) {
                    inputStreams.remove(anID);
                }
            }
        }
        return buffer;
    }

    protected byte[] retrieveNext(Object anID) throws IOException {
        ID streamID = (ID) anID;
        byte[] result = (byte[])
            sender.sendSyncRequest(
                new Request("retrieveLocalNext", new Class[] {Object.class},
                    new Object[] {anID}),
                out, StreamManager.NAME, streamID.hostingNode); 
        return result;
    }

    /**
     * InputStream returned when a GInputStream is deserialized. This 
     * InputStream calls back its StreamManager when its internal buffer is
     * empty. 
     *
     * @version $Revision: 1.4 $ $Date: 2004/06/10 23:12:25 $
     */
    private class ProxyInputStream extends InputStream {
        /**
         * InputStream identifier proxied by this instance.
         */
        private final Object streamID;
        
        /**
         * Internal buffer.
         */
        private byte[] buffer;
        
        /**
         * Current position in the internal buffer.
         */
        private int pos;
        
        /**
         * Creates a proxy for the InputStream tracked by the ID anID.
         * 
         * @param anID InputStream identifier.
         * @throws IOException Indicates than an I/O error has occured.
         */
        private ProxyInputStream(Object anID) throws IOException {
            if ( null == anID ) {
                throw new IllegalArgumentException("ID is required.");
            }
            streamID = anID;
            buffer = new byte[0];
            pos = 0;
        }
        
        public int read() throws IOException {
            if ( 0 == buffer.length || pos > (buffer.length - 1) ) {
                buffer = retrieveNext(streamID);
                if ( 0 == buffer.length ) {
                    return -1;
                }
                pos = 0;
            }
            return buffer[pos++];
        }
    }
    
    /**
     * This is the object, which is written to an OutputStream during
     * serialization of a GInputStream. This identifier contains the location
     * of the StreamManager, which has registered the InputStream wrapped by
     * a GInputStream. This information is used to locate the relevant
     * node when a StreamManager on another node needs to pull the content of 
     * an InputStream.
     */
    protected static class ID implements Externalizable {
        private static volatile int idSeq = 0;
        private int sequence;
        private NodeInfo hostingNode;
        /**
         * Required for Externalization.
         */
        public ID() {}
        private ID(NodeInfo aNode) {
            sequence = ++idSeq;
            hostingNode = aNode;
        }
        public int hashCode() {
            return hostingNode.hashCode() * new Integer(sequence).hashCode();
        }
        public boolean equals(Object obj) {
            if ( false == obj instanceof ID ) {
                return false;
            }
            ID id = (ID) obj;
            return id.sequence == sequence && hostingNode.equals(id.hostingNode) ;
        }
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(sequence);
            out.writeObject(hostingNode);
        }
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            sequence = in.readInt();
            hostingNode = (NodeInfo) in.readObject();
        }
    }
    
}
