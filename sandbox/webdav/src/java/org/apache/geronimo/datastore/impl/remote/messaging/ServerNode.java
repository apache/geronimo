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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.WaitingException;
import org.mortbay.util.ThreadedServer;

/**
 * It allows a remote connectivity to a set of Connectors.
 * <BR>
 * It is the only component dealing directly with raw connections: it directly
 * accesses the InputStream and OutputStream of the registered connections. It
 * insulates the other components from connectivity issues.
 * <BR>
 * It is also in charge of dispatching the incoming Msgs to the registered
 * Connectors.
 * <BR>
 * The following diagram shows how ServantNode and Connectors are combined
 * together:
 * 
 * Connector -- MTO -- ServerNode -- MTM -- ServerNode -- OTM -- Connector
 *
 * Connector communicates with each other by sending Msgs.
 *
 * @version $Revision: 1.4 $ $Date: 2004/03/11 15:36:14 $
 */
public class ServerNode
    implements GBean
{

    private static final Log log = LogFactory.getLog(ServerNode.class);

    private static final ServerNodeContext NULL_CONTEXT =
        new ServerNodeContext(null, null);
    
    /**
     * Node meta-data.
     */
    private final NodeInfo nodeInfo;
    
    /**
     * Connectors registered by this server.
     */
    private final Map connectors;
    
    /**
     * StreamManager to register/retrieve distributed InputStream.
     */
    private final StreamManager streamManager;
    
    /**
     * Server listening for connections to be made.
     */
    private final InternalServer server;
    
    /**
     * Inbound Msg queue. This queue is filled by Msgs coming directly
     * from the network connections.
     */
    final MsgQueue queueIn;
    
    /**
     * Inbound Msgs reactor. it is between the inbound Msg queue and 
     * the Connectors.
     */
    final HeaderReactor inReactor;
    
    /**
     * Outbound Msgs queue. This queue is a staging repository for Nsgs
     * to be sent over the network.
     */
    final MsgQueue queueOut;
    
    /**
     * Connections to this server. The key is the name of the ServantNode and 
     * the value is a ConnectionWrapper.
     */
    final Map connections;
    
    /**
     * Processors of this server.
     */
    final ServerProcessors processors;
    
    /**
     * MetaConnection to other nodes.
     */
    final MetaConnection metaConnection;

    private final RequestSender sender;
    
    private GBeanContext context;

    /**
     * Creates a server.
     * 
     * @param aNodeInfo NodeInfo identifying uniquely this node on the network.
     * @param aCollOfConnectors Collection of Connectors to be registered by 
     * this server.
     */
    public ServerNode(NodeInfo aNodeInfo, Collection aCollOfConnectors) {
        if ( null == aNodeInfo ) {
            throw new IllegalArgumentException("NodeInfo is required.");
        } else if ( null == aCollOfConnectors ) {
            throw new IllegalArgumentException("Connectors is required.");
        }
        nodeInfo = aNodeInfo;
        sender = new RequestSender(nodeInfo);
        server = new InternalServer();
        
        metaConnection = new MetaConnection(this);
        
        streamManager = new StreamManagerImpl(nodeInfo);
        
        processors = new ServerProcessors(this);
        
        queueIn = new MsgQueue(nodeInfo.getName() + " Inbound");
        queueOut = new MsgQueue(nodeInfo.getName() + " Outbound");
        
        connections = new HashMap();
        
        // The incoming messages are dispatched to the clients.
        inReactor = new HeaderReactor(
            new HeaderInInterceptor(
                new QueueInInterceptor(queueIn),
                MsgHeaderConstants.DEST_CONNECTOR),
                processors.getProcessors());
        
        inReactor.register(StreamManager.NAME, streamManager);
        // The stream manager writes to the output queue of the server.
        ServerNodeContext nodeContext = new ServerNodeContext(
            new HeaderOutInterceptor(
                MsgHeaderConstants.SRC_CONNECTOR,
                StreamManager.NAME,
                new QueueOutInterceptor(queueOut)),
            sender
            );
        streamManager.setContext(nodeContext);
                
        connectors = new HashMap();
        // Registers the Connectors.
        for (Iterator iter = aCollOfConnectors.iterator();
            iter.hasNext();) {
            Connector connector = (Connector) iter.next();
            addConnector(connector);
        }
    }

    /**
     * Gets the NodeInfo of this node.
     * 
     * @return NodeInfo.
     */
    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }
    
    /**
     * Sets the node topology in which this instance is operating. 
     * 
     * @param aTopology Topology of the nodes constituting the network layout.
     */
    public void setTopology(Topology aTopology) {
        metaConnection.setTopology(aTopology);
    }
    
    /**
     * Joins the node uniquely identified on the network by aNodeInfo.
     * 
     * @param aNodeInfo NodeInfo of a remote node to join.
     * @throws IOException Indicates that an I/O error has occured.
     * @throws CommunicationException Indicates that the node can not be
     * registered by the remote node identified by aNodeInfo.
     */
    public void join(NodeInfo aNodeInfo)
        throws IOException, CommunicationException {
        metaConnection.join(aNodeInfo);
    }
    
    /**
     * Leaves the node uniquely identified on the network by aNodeInfo.
     * 
     * @param aNodeInfo NodeInfo of the remote node to leave.
     * @throws IOException Indicates that an I/O error has occured.
     * @throws CommunicationException Indicates that the node has not leaved
     * successfully the remote node.
     */
    public void leave(NodeInfo aNodeInfo)
        throws IOException, CommunicationException {
        metaConnection.leave(aNodeInfo);
    }
    
    /**
     * Gets the StreamManager of this server.
     * 
     * @return StreamManager used by this server to resolve/encode InputStreams.
     */
    public StreamManager getStreamManager() {
        return streamManager;
    }

    /**
     * Gets the Output to be used to communicate with the specified node.
     * <BR>
     * aNode must be a node directly connected to this instance.
     * 
     * @param aNode Node.
     * @return Output to be used to communicate with the specified node.
     * @throws CommunicationException
     */
    public MsgOutInterceptor getRawOutForNode(NodeInfo aNode)
        throws CommunicationException {
        return metaConnection.getRawOutForNode(aNode);
    }
        
    /**
     * Gets the Output to be used to communicate with the specified node.
     * <BR>
     * aNode can be a node anywhere in the topology.
     * 
     * @param aNode Node.
     * @return Output to be used to communicate with the specified node.
     */
    protected MsgOutInterceptor getOutForNode(NodeInfo aNode)
        throws CommunicationException {
        return metaConnection.getOutForNode(aNode);
    }
    
    /**
     * Registers a new Connector.
     * 
     * @param aConnector Connector to be registered.
     */
    private void addConnector(Connector aConnector) {
        String pName = aConnector.getName();
        // Connectors write to the outbound Msg queue.
        ServerNodeContext nodeContext = new ServerNodeContext(
            new HeaderOutInterceptor(
                MsgHeaderConstants.SRC_CONNECTOR,
                pName,
                new QueueOutInterceptor(queueOut)),
                sender);
        aConnector.setContext(nodeContext);
        inReactor.register(pName, aConnector);
        synchronized (connectors) {
            connectors.put(pName, aConnector);
        }
    }
    
    /**
     * Unregisters the Connector.
     * 
     * @param aConnector Connector to be deregistered.
     */
    private void removeConnector(Connector aConnector) {
        String pName = aConnector.getName();
        aConnector.setContext(NULL_CONTEXT);
        inReactor.unregister(pName);
        synchronized (connectors) {
            connectors.remove(pName);
        }
    }
    
    public void setGBeanContext(GBeanContext aContext) {
        context = aContext;
    }

    public void doStart() throws WaitingException, Exception {
        server.start();
        processors.start();
    }

    public void doStop() throws WaitingException, Exception {
        server.stop();
        processors.stop();
    }

    public void doFail() {
        server.stop();
        processors.stop();
    }
    
    public String toString() {
        return "Node {" + nodeInfo + "}";
    }

    /**
     * Socket server listening for connections to be made to this node.
     */
    private class InternalServer extends ThreadedServer {
        
        public InternalServer() {
            super(nodeInfo.getAddress(), nodeInfo.getPort());
            // No socket timeout.
            setMaxIdleTimeMs(0);
        }
        
        /**
         * Handles a new connection.
         */
        protected void handleConnection(InputStream anIn,OutputStream anOut) {
            try {
                metaConnection.joined(anIn, anOut);
            } catch (IOException e) {
                log.error(e);
            } catch (CommunicationException e) {
                log.error(e);
            }
        }
        
        public void start() {
            try {
                super.start();
            } catch (Exception e) {
                log.error(e);
                context.fail();
            }
        }
        
        public void stop() {
            try {
                super.stop();
            } catch (InterruptedException e) {
                log.error(e);
                context.fail();
            }
        }
        
        public void fail() {
            try {
                super.stop();
            } catch (InterruptedException e) {
                log.error(e);
            }
        }
    }
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory factory = new GBeanInfoFactory(ServerNode.class);
        factory.setConstructor(
            new String[] {"NodeInfo", "Connectors"},
            new Class[] {NodeInfo.class, Collection.class});
        factory.addAttribute("NodeInfo", true);
        factory.addReference("Connectors", Connector.class);
        factory.addAttribute("StreamManager", false);
        factory.addOperation("join", new Class[]{NodeInfo.class});
        factory.addOperation("leave", new Class[]{NodeInfo.class});
        GBEAN_INFO = factory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
    
}