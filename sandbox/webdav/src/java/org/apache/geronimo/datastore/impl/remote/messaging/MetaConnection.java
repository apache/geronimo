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
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is a connection of connections.
 *
 * @version $Revision: 1.2 $ $Date: 2004/03/03 13:10:07 $
 */
public class MetaConnection
{

    private static final Log log = LogFactory.getLog(MetaConnection.class);
    
    /**
     * Node owning this connection.
     */
    private ServerNode node;
    
    /**
     * NodeInfo to Connection map.
     */
    private Map connections;
    
    /**
     * Creates a meta-connection for the specified node.
     * 
     * @param aNode Node.
     */
    public MetaConnection(ServerNode aNode) {
        if ( null == aNode ) {
            throw new IllegalArgumentException("Node is required.");
        }
        node = aNode;
        connections = new HashMap();
    }
    
    /**
     * Gets the Msg output to be used to communicate with the node aNodeName.
     * 
     * @param aNodeName Node name.
     * @return Msg output.
     * @throws CommunicationException Indicates that the node aNodeName is not
     * registered by this connection.
     */
    public MsgOutInterceptor getOutForNode(String aNodeName)
        throws CommunicationException {
        Map tmpConnections;
        synchronized(connections) {
            tmpConnections = new HashMap(connections);
        }
        Connection connection = null;
        for (Iterator iter = tmpConnections.entrySet().iterator();
            iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            NodeInfo nodeInfo = (NodeInfo) entry.getKey();
            if ( nodeInfo.getName().equals(aNodeName) ) {
                connection = (Connection) entry.getValue();
                break;
            }
        }
        
        if ( null == connection ) {
            throw new CommunicationException("Node {" + aNodeName +
                "} is not know by {" + node.getNodeInfo().getName() + "}");
        }
        
        return connection.out;
    }
    
    /**
     * Tests if the specified NodeInfo is already registered by this
     * meta-connection.
     * 
     * @param aNodeInfo NodeInfo defining a Node.
     * @return true if the node info is already registered.
     */
    public boolean isRegistered(NodeInfo aNodeInfo) {
        synchronized(connections) {
            return connections.containsKey(aNodeInfo);
        }
    }
    
    /**
     * Creates a new connection on top of the provided input and output streams
     * and waits for the end or failure of this connection prior to return.
     * <BR>
     * These streams should have been provided by a remote node, which is
     * joining the node owning this meta-connection.
     * <BR>
     * This method reads the NodeInfo of the remote node and tries to register
     * it with this node.  
     * 
     * @param anIn InputStream opened by a remote node on the node owning this
     * meta-connection.
     * @param anOut OutputStream.
     * @throws IOException Indicates that an I/O error has occured.
     * @throws CommunicationException Indicates that the NodeInfo provided by
     * the remote node conflicts with the current NodeInfo registrations.
     */
    public void joined(InputStream anIn, OutputStream anOut)
        throws IOException, CommunicationException {
        Connection connection = new Connection(anIn, anOut);

        // Try to register the connected node with this node.
        Msg msg = connection.in.pop();
        MsgBody body = msg.getBody();
        NodeInfo otherNodeInfo = (NodeInfo) body.getContent();
        
        msg = new Msg();
        body = msg.getBody();
        if ( isRegistered(otherNodeInfo) ) {
            body.setContent(Boolean.FALSE);
            connection.out.push(msg);
            throw new CommunicationException(
                otherNodeInfo + " already registered");
        }
        synchronized(connections) {
            connections.put(otherNodeInfo, connection);
        }
        body.setContent(Boolean.TRUE);
        connection.out.push(msg);
        // Pops the input stream of the connection and fills in the inbound
        // Msg queue.
        QueueOutInterceptor out = new QueueOutInterceptor(node.queueIn);
        MsgCopier copier = new MsgCopier(
            connection.in, out, connection.listener);
        node.processors.execute(copier);
        connection.waitForEnd();
    }
    
    /**
     * Creates a new connection to the node uniquely identified on the network
     * by the provided NodeInfo.
     * 
     * @param aNodeInfo NodeInfo of a node.
     * @throws IOException Indicates that an I/O error has occured.
     * @throws CommunicationException Indicates that the node owning this
     * meta-connection can not be registered by the remote node identified by
     * aNodeInfo.
     */
    public void join(NodeInfo aNodeInfo)
        throws IOException, CommunicationException {
        if ( isRegistered(aNodeInfo) ) {
            throw new IllegalArgumentException("{" + aNodeInfo +
                "} is already registered by {" + node + "}");
        }
        
        Connection connection = new Connection(aNodeInfo);
        
        // Try to register this node with the other one.
        Msg msg = new Msg();
        MsgBody body = msg.getBody();
        body.setContent(node.getNodeInfo());
        connection.out.push(msg);
        
        msg = connection.in.pop();
        // In case of successful registration, the server returns true.
        Boolean success = (Boolean) msg.getBody().getContent();
        if ( !success.booleanValue() ) {
            throw new CommunicationException("Can not register Node {" +
                node.getNodeInfo() + "} with {" + aNodeInfo + "}");
        }
        synchronized (connections) {
            connections.put(aNodeInfo, connection);
        }
        // Pops the input stream of the connection and fills in the inbound
        // Msg queue.
        QueueOutInterceptor out = new QueueOutInterceptor(node.queueIn);
        MsgCopier copier = new MsgCopier(
            connection.in, out, connection.listener);
        node.processors.execute(copier);
    }
    
    /**
     * Closes the connection to the node identified by aNodeInfo.
     * 
     * @param aNodeInfo NodeInfo of a remote node.
     * @throws IOException Indicates that an I/O error has occured.
     * @throws CommunicationException Indicates that the node owning this 
     * meta-connection has not leaved successfully the remote node. 
     */
    public void leave(NodeInfo aNodeInfo)
        throws IOException, CommunicationException {
        if ( isRegistered(aNodeInfo) ) {
            throw new IllegalArgumentException("{" + aNodeInfo +
                "} is already registered by {" + node + "}");
        }
        
        Connection connection;
        synchronized (connections) {
            connection = (Connection) connections.remove(aNodeInfo);
        }
        connection.close();
    }

    /**
     * Logical connection.
     */
    private class Connection {
        /**
         * Allows reading from the connection in Msg mode.
         */
        private final MsgInInterceptor in;
        /**
         * Raw InputStream.
         */
        private final InputStream rawIn;
        /**
         * Allows writing to the connection in Msg mode.
         */
        private final MsgOutInterceptor out;
        /**
         * Raw OutputStream.
         */
        private final OutputStream rawOut;
        /**
         * Receives notification when the copier poping and pushing Msgs to
         * the raw InputStream and OutputStream fails.
         */
        private final MsgCopier.CopierListener listener;
        /**
         * Monitor used to wait the end of this connection.
         */
        private final Object endReleaser = new Object();
        
        /**
         * Creates a connection wrapping the provided input and output streams.
         *  
         * @param anIn InputStream of the connection.
         * @param anOut OutputStream of the connection.
         * @exception IOException Indicates that an I/O error has occured.
         */
        private Connection(InputStream anIn, OutputStream anOut)
            throws IOException {
            if ( null == anIn ) {
                throw new IllegalArgumentException("InputStream is required.");
            } else if ( null == anOut ) {
                throw new IllegalArgumentException("OutputStream is required.");
            }
            
            rawIn = anIn;
            in = new StreamInInterceptor(rawIn, node.getStreamManager());
            rawOut = anOut;
            // One adds the name of this node on exit.
            out =
                new HeaderOutInterceptor(
                    MsgHeaderConstants.SRC_NODE,
                    node.getNodeInfo().getName(),
                    new StreamOutInterceptor(rawOut, node.getStreamManager()));
            listener = new MsgCopier.NullCopierListener() {
                public void onFailure() {
                    close();
                }
            };
        }
        /**
         * Creates a connection to the node defined by aNodeInfo. 
         * 
         * @param aNodeInfo NodeInfo of a node.
         * @exception IOException Indicates that an I/O error has occured.
         */
        private Connection(NodeInfo aNodeInfo)
            throws IOException {
            if ( null == aNodeInfo ) {
                throw new IllegalArgumentException("NodeInfo is required.");
            }
            Socket socket =
                new Socket(aNodeInfo.getAddress(), aNodeInfo.getPort());
            
            rawIn = socket.getInputStream();
            in = new StreamInInterceptor(rawIn, node.getStreamManager());
            rawOut = socket.getOutputStream();
            // One adds the name of this node on exit.
            out =
                new HeaderOutInterceptor(
                    MsgHeaderConstants.SRC_NODE,
                    node.getNodeInfo().getName(),
                    new StreamOutInterceptor(rawOut, node.getStreamManager()));
            listener = new MsgCopier.NullCopierListener() {
                public void onFailure() {
                    close();
                }
            };
        }

        /**
         * Close the logical connection.
         */
        private void close() {
            try {
                rawIn.close();
            } catch (IOException e) {
                log.error("Can not close input", e);
            }
            try {
                rawOut.close();
            } catch (IOException e) {
                log.error("Can not close output", e);
            }
            synchronized(endReleaser) {
                endReleaser.notify();
            }
        }
        
        /**
         * Waits until the end of this connection.
         */
        private void waitForEnd() {
            synchronized(endReleaser) {
                try {
                    endReleaser.wait();
                } catch (InterruptedException e) {
                    log.error(e);
                }
            }
        }
    }
    
}
