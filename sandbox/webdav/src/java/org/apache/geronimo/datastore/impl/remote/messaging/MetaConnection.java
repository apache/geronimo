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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is a connection of connections.
 *
 * @version $Revision: 1.6 $ $Date: 2004/03/24 11:37:05 $
 */
public class MetaConnection
{

    private static final Log log = LogFactory.getLog(MetaConnection.class);
    
    /**
     * Node owning this connection.
     */
    private final NodeImpl node;
    
    /**
     * NodeInfo to Connection map.
     */
    private final Map connections;
    
    /**
     * Connections opened by MetaConnection.
     */
    private final Collection openedConnections;
    
    /**
     * Node topology to be used to derive the most appropriate path to reach
     * a node.
     */
    private Topology topology;
    
    /**
     * Logical compression and decompression applied on Msgs pushed and poped 
     * by this meta-connection.
     */
    private final LogicalCompression logicalComp;
    
    /**
     * Creates a meta-connection for the specified node.
     * 
     * @param aNode Node.
     */
    public MetaConnection(NodeImpl aNode) {
        if ( null == aNode ) {
            throw new IllegalArgumentException("Node is required.");
        }
        node = aNode;
        connections = new HashMap();
        logicalComp = new LogicalCompression();
        openedConnections = new ArrayList();
    }
    
    /**
     * Sets the node topology to be used in order to resolve the nodes to be
     * traversed to reach a specific node.  
     * 
     * @param aTopology Node topology.
     */
    public void setTopology(Topology aTopology) {
        topology = aTopology;
    }
    
    /**
     * Gets the Msg output to be used to communicate with the node aNodeName.
     * <BR>
     * This node MUST be a node directly connected to the node owning this
     * meta-connection.
     * 
     * @param aNodeName Node name.
     * @return Msg output.
     * @throws CommunicationException Indicates that the node aNodeName is not
     * registered by this connection.
     */
    public MsgOutInterceptor getRawOutForNode(NodeInfo aNode)
        throws CommunicationException {
        Connection connection;
        synchronized(connections) {
            connection = (Connection) connections.get(aNode);
        }
        if ( null == connection ) {
            throw new CommunicationException("{" + aNode +
                "} is not reachable by {" + node.getNodeInfo() + "}");
        }
        return connection.out;
    }
    
    /**
     * Gets the Msg output to be used to communicate with the node aNodeName.
     * <BR>
     * The specified node can be anywhere in the current node topology.
     * Moreover, the returned Msg output automatically adds the information
     * required to reach the node aNode.
     * 
     * @param aNodeName Node name.
     * @return Msg output.
     * @throws CommunicationException Indicates that the node aNodeName is not
     * registered by this connection.
     */
    public MsgOutInterceptor getOutForNode(NodeInfo aNode)
        throws CommunicationException {
        NodeInfo[] path = topology.getPath(node.getNodeInfo(), aNode);
        if ( null == path ) {
            throw new CommunicationException("{" + aNode +
                "} is not reachable by {" + node.getNodeInfo() + "}");
        }
        NodeInfo tmpNode = path[0];
        Connection connection;
        synchronized(connections) {
            connection = (Connection) connections.get(tmpNode);
        }
        if ( null == connection ) {
            throw new CommunicationException("{" + aNode +
                "} is not reachable by {" + node.getNodeInfo() + "}");
        }
        NodeInfo[] newPath = NodeInfo.pop(path);
        MsgOutInterceptor out =
            new HeaderOutInterceptor(
                MsgHeaderConstants.SRC_NODE,
                node.getNodeInfo(),
                new HeaderOutInterceptor(
                    MsgHeaderConstants.DEST_NODE,
                    aNode,
                    new HeaderOutInterceptor(
                        MsgHeaderConstants.DEST_NODES,
                        aNode,
                        connection.out)));
        if ( null != newPath ) {
            out =
                new HeaderOutInterceptor(
                    MsgHeaderConstants.DEST_NODE_PATH,
                    newPath,
                    out);
        }
        return out; 
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
        connection.nodeInfo = otherNodeInfo;
        synchronized(connections) {
            connections.put(otherNodeInfo, connection);
        }
        body.setContent(Boolean.TRUE);
        connection.out.push(msg);
        popConnection(connection);
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
        synchronized (openedConnections) {
            openedConnections.add(connection);
        }
        popConnection(connection);
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
        if ( !isRegistered(aNodeInfo) ) {
            throw new IllegalArgumentException("{" + aNodeInfo +
                "} is not registered by {" + node + "}");
        }
        
        Connection connection;
        synchronized (connections) {
            connection = (Connection) connections.remove(aNodeInfo);
        }
        synchronized (openedConnections) {
            openedConnections.remove(connection);
        }
        connection.close();
    }

    /**
     * Stops the connections. 
     */
    public void stop() {
        // TODO Remote nodes which have join the node owning this
        // meta-connection need to be notified in order to close on their side
        // the connection.
        synchronized(openedConnections) {
            for (Iterator iter = openedConnections.iterator(); iter.hasNext();) {
                Connection connection = (Connection) iter.next();
                connection.close();
                iter.remove();
            }
        }
    }
    
    /**
     * Pops the input stream of the connection and fills in the inbound
     * Msg queue with Msgs sent to this node. Otherwise, fills in the
     * outbound queue with Msgs sent to other nodes, which are proxied by this
     * node.
     * 
     * @param aConnection Connection to be polled.
     */
    private void popConnection(Connection aConnection) {
        final QueueOutInterceptor inboundOut =
        new QueueOutInterceptor(node.queueIn);
        final QueueOutInterceptor outboundOut =
        new QueueOutInterceptor(node.queueOut);
        MsgOutInterceptor out = new MsgOutInterceptor() {
            public void push(Msg aMsg) {
                MsgHeader header = aMsg.getHeader();
                if ( node.getNodeInfo().equals(
                    header.getHeader(MsgHeaderConstants.DEST_NODE)) ) {
                    inboundOut.push(aMsg);
                } else {
                    outboundOut.push(aMsg);
                }
            }
        };
        MsgCopier copier = new MsgCopier(
            aConnection.in, out, aConnection.listener);
        node.processors.execute(copier);
    }

    /**
     * Logical compression of the Msgs pushed and poped by this meta-connection.
     * <BR>
     * Its goal is to remove from Msgs to be serialized the information, shared
     * by all the nodes. For instance, as a Topology is shared by all the nodes
     * one can replace the NodeInfo instances contained by Msgs by their
     * corresponding identifier. 
     *
     * @version $Revision: 1.6 $ $Date: 2004/03/24 11:37:05 $
     */
    private class LogicalCompression implements
        StreamInInterceptor.PopSynchronization,
        StreamOutInterceptor.PushSynchronization {
        
        /**
         * No logical compression.
         */
        private final static byte NULL = 0x00;
        
        /**
         * Compression based on the Topology shared knowledge.
         */
        private final static byte TOPOLOGY = 0x01;
        
        private final static byte REQUEST = 0x01;
        private final static byte RESPONSE = 0x02;
        
        public Object beforePop(StreamInputStream anIn)
            throws IOException {
            byte type = anIn.readByte(); 
            if ( type == NULL ) {
                return null;
            }
            if ( null == topology ) {
                throw new IllegalArgumentException("No topology is defined.");
            }
            Object[] result = new Object[4];
            int id = anIn.readInt();
            NodeInfo nodeInfo = topology.getNodeById(id);
            result[0] = nodeInfo;
            id = anIn.readInt();
            nodeInfo = topology.getNodeById(id);
            result[1] = nodeInfo;
            int bodyType = anIn.read();
            if ( REQUEST == bodyType ) {
                result[2] = MsgBody.Type.REQUEST;
            } else {
                result[2] = MsgBody.Type.RESPONSE;
            }
            int reqID = anIn.readInt();
            result[3] = new RequestSender.RequestID(reqID);
            return result;
        }
        public void afterPop(StreamInputStream anIn, Msg aMsg, Object anOpaque)
            throws IOException {
            if ( null == anOpaque ) {
                return;
            }
            Object[] prePop = (Object[]) anOpaque;
            MsgHeader header = aMsg.getHeader();
            header.addHeader(MsgHeaderConstants.SRC_NODE, prePop[0]);
            header.addHeader(MsgHeaderConstants.DEST_NODE, prePop[1]);
            header.addHeader(MsgHeaderConstants.DEST_NODES, prePop[1]);
            header.addHeader(MsgHeaderConstants.BODY_TYPE, prePop[2]);
            header.addHeader(MsgHeaderConstants.CORRELATION_ID, prePop[3]);
        }
        
        public Object beforePush(StreamOutputStream anOut, Msg aMsg)
            throws IOException {
            if ( null == topology ) {
                anOut.writeByte(NULL);
                return null;
            }
            anOut.writeByte(TOPOLOGY);
            MsgHeader header = aMsg.getHeader();
            NodeInfo info =
                (NodeInfo) header.resetHeader(MsgHeaderConstants.SRC_NODE);
            int id = topology.getIDOfNode(info);
            anOut.writeInt(id);
            info =
                (NodeInfo) header.resetHeader(MsgHeaderConstants.DEST_NODE);
            id = topology.getIDOfNode(info);
            // When pushing a Msg on the network, DEST_NODES equals to
            // DEST_NODE.
            header.resetHeader(MsgHeaderConstants.DEST_NODES);
            anOut.writeInt(id);
            MsgBody.Type type  = (MsgBody.Type)
                header.resetHeader(MsgHeaderConstants.BODY_TYPE);
            if ( type == MsgBody.Type.REQUEST ) {
                anOut.write(REQUEST);
            } else {
                anOut.write(RESPONSE);
            }
            RequestSender.RequestID reqID  = (RequestSender.RequestID)
                header.resetHeader(MsgHeaderConstants.CORRELATION_ID);
            anOut.writeInt(reqID.getID());
            return null;
        }
        public void afterPush(StreamOutputStream anOut, Msg aMsg,
            Object anOpaque) throws IOException {
        }
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
         * NodeInfo of the client.
         */
        private NodeInfo nodeInfo;
        
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
            in = new StreamInInterceptor(rawIn, node.getStreamManager(),
                logicalComp);
            rawOut = anOut;
            out = new StreamOutInterceptor(rawOut, node.getStreamManager(),
                logicalComp);
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
            nodeInfo  = aNodeInfo;
            Socket socket =
                new Socket(aNodeInfo.getAddress(), aNodeInfo.getPort());
            
            rawIn = socket.getInputStream();
            in = new StreamInInterceptor(rawIn, node.getStreamManager(),
                logicalComp);
            rawOut = socket.getOutputStream();
            out = new StreamOutInterceptor(rawOut, node.getStreamManager(),
                logicalComp);
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
            synchronized(connections) {
                connections.remove(nodeInfo);
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
