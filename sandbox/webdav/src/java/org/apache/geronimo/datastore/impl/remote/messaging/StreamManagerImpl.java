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
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.datastore.impl.remote.datastore.CommandResult;

/**
 * StreamManager implementation.
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/25 13:36:15 $
 */
public class StreamManagerImpl
    implements Connector, StreamManager
{

    private static final Log log = LogFactory.getLog(StreamManagerImpl.class);
    
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
    private static final ID NULL_INPUT_STREAM = new ID("NULL");
    
    /**
     * Name of this manager.
     */
    protected final String name; 
    
    /**
     * Number of milliseconds to wait for a response.
     */
    private static final long WAIT_RESPONSE = 100;
    
    /**
     * identifier to input stream map.
     */
    private final Map inputStreams;
    
    /**
     * To send requests.
     */
    protected RequestSender sender;
    
    /**
     * Used to communicate with remote StreamManagers.
     */
    protected MsgOutInterceptor out;
    
    /**
     * Creates a manager having the specified name.
     * 
     * @param aName Manager name.
     */
    public StreamManagerImpl(String aName) {
        if ( null == aName ) {
            throw new IllegalArgumentException("Name is required.");
        }
        name = aName;
        inputStreams = new HashMap();
        sender = new RequestSender();
    }
    
    public String getName() {
        return name;
    }
    
    public void setOutput(MsgOutInterceptor anOut) {
        out = anOut;
    }
    
    public Object register(InputStream anIn) {
        if ( null == anIn ) {
            return NULL_INPUT_STREAM;
        }
        Object id = new ID(name);
        synchronized(inputStreams) {
            inputStreams.put(id, anIn);
        }
        return id;
    }

    public InputStream retrieve(Object anId) throws IOException {
        return new ProxyInputStream(anId);
    }

    /**
     * Retrieves the local InputStream having the specified identifier; reads
     * READ_SIZE bytes and returns them.
     * 
     * @param anID Identifier of the InputStream to read from.
     * @return Bytes read. If the end of stream is reached, then NULL_READ is
     * returned.
     * @throws IOException Indicates that an I/O error has occured.
     */
    public byte[] retrieveLocalNext(Object anID)
        throws IOException {
        InputStream in;
        synchronized(inputStreams) {
            in = (InputStream) inputStreams.get(anID);
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
            }
        }
        return buffer;
    }

    /**
     * Same as retrieveLocalNext, yet for an InputStream tracked by a remote
     * StreamManager.
     */
    protected byte[] retrieveNext(Object anID) throws IOException {
        ID id = (ID) anID;
        MsgOutInterceptor reqOut =
            new HeaderOutInterceptor(
                MsgHeaderConstants.BODY_TYPE,
                MsgBody.Type.REQUEST,
                new HeaderOutInterceptor(
                    MsgHeaderConstants.DEST_NODE,
                    id.managerName,
                    new HeaderOutInterceptor(
                        MsgHeaderConstants.DEST_CONNECTOR,
                        StreamManager.NAME,
                        out)));
        byte[] result = (byte[])
            sender.sendSyncRequest(new CommandWithStreamManager(anID), reqOut); 
        return result;
        
    }
    
    public void deliver(Msg aMsg) {
        MsgHeader header = aMsg.getHeader();
        MsgBody.Type bodyType =
            (MsgBody.Type) header.getHeader(MsgHeaderConstants.BODY_TYPE);
        if ( bodyType.equals(MsgBody.Type.REQUEST) ) {
            handleRequest(aMsg);
        } else if ( bodyType.equals(MsgBody.Type.RESPONSE) ) {
            handleResponse(aMsg);
        }
    }
    
    /**
     * Handles a request Msg.
     * 
     * @param aMsg Request Msg to be handled.
     */
    protected void handleRequest(Msg aMsg) {
        MsgBody body = aMsg.getBody();
        MsgHeader header = aMsg.getHeader();
        Object sourceNode = header.getHeader(MsgHeaderConstants.SRC_NODE);
        Object id = header.getHeader(MsgHeaderConstants.CORRELATION_ID);
        CommandWithStreamManager command;
        String gateway;
        command = (CommandWithStreamManager) body.getContent();
        command.setStreamManager(this);
        CommandResult result = command.execute();
        Msg msg = new Msg();
        body = msg.getBody();
        body.setContent(result);
        MsgOutInterceptor reqOut =
            new HeaderOutInterceptor(
                MsgHeaderConstants.CORRELATION_ID,
                id,
                new HeaderOutInterceptor(
                    MsgHeaderConstants.BODY_TYPE,
                    MsgBody.Type.RESPONSE,
                    new HeaderOutInterceptor(
                        MsgHeaderConstants.DEST_NODE,
                        sourceNode,
                        new HeaderOutInterceptor(
                            MsgHeaderConstants.DEST_CONNECTOR,
                            StreamManager.NAME,
                            out))));
        reqOut.push(msg);
    }

    /**
     * Handles a response Msg.
     * 
     * @param aMsg Response to be handled.
     */
    protected void handleResponse(Msg aMsg) {
        MsgBody body = aMsg.getBody();
        MsgHeader header = aMsg.getHeader();
        CommandResult result;
        result = (CommandResult) body.getContent();
        sender.setResponse(
            (Integer) header.getHeader(MsgHeaderConstants.CORRELATION_ID),
            result);
    }
    
    /**
     * InputStream returned when a GInputStream is deserialized. This 
     * InputStream calls back its StreamManager when its internal buffer is
     * empty. 
     *
     * @version $Revision: 1.1 $ $Date: 2004/02/25 13:36:15 $
     */
    private class ProxyInputStream extends InputStream {
        /**
         * InputStream identifier proxied by this instance.
         */
        private final Object id;
        
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
            id = anID;
            buffer = new byte[0];
            pos = 0;
        }
        
        public int read() throws IOException {
            if ( 0 == buffer.length || pos > (buffer.length - 1) ) {
                buffer = retrieveNext(id);
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
    protected static class ID implements Serializable {
        private static volatile int idSeq = 0;
        protected int sequence;
        protected String managerName;
        private ID(String aName) {
            sequence = ++idSeq;
            managerName = aName;
        }
        public int hashCode() {
            return managerName.hashCode() + new Integer(sequence).hashCode();
        }
        public boolean equals(Object obj) {
            if ( !(obj instanceof ID) ) {
                return false;
            }
            ID id = (ID) obj;
            return id.sequence == sequence &&
            managerName.equals(id.managerName) ;
        }
    }

    public static class CommandWithStreamManager
        implements Serializable {

        private final Object id;
        private byte[] content;
        private StreamManager streamManager;

        public CommandWithStreamManager(Object anId) {
            id = anId;
        }
        
        public void setStreamManager(StreamManager aManager) {
            streamManager = aManager;
        }
        
        public CommandResult execute() {
            try {
                content = streamManager.retrieveLocalNext(id);
            } catch (IOException e) {
                return new CommandResult(false, e);
            }
            return new CommandResult(true, content);
        }
        
    }
    
}
