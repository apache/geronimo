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
 * Connector -- MTO -- ServerNode -- MTO -- ServerNode -- OTM -- Connector
 *
 * Connector communicates with each other by sending Msgs.
 *
 * @version $Revision: 1.2 $ $Date: 2004/03/01 13:16:35 $
 */
public class ServerNode
    extends ThreadedServer
    implements GBean
{
    
    private static final Log log = LogFactory.getLog(ServerNode.class);

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
    
    private GBeanContext context;

    /**
     * Creates a server.
     * 
     * @param aName Name of this server.
     * @param aCollOfConnectors Collection of Connectors to be registered by 
     * this server.
     * @param anAddress Listening address of this server.
     * @param aPort Listening port of this server.
     * @param aMaxRequest Maximum number of concurrent requests, which can be
     * processed by this server.
     */
    public ServerNode(NodeInfo aNodeInfo, Collection aCollOfConnectors,
        int aMaxRequest) {
        super(aNodeInfo.getAddress(), aNodeInfo.getPort());
        
        nodeInfo = aNodeInfo;
        metaConnection = new MetaConnection(this);
        
        // No socket timeout.
        setMaxIdleTimeMs(0);
        
        streamManager = new StreamManagerImpl(getName());
        
        processors = new ServerProcessors(this);
        
        queueIn = new MsgQueue(getName() + " Inbound");
        queueOut = new MsgQueue(getName() + " Outbound");
        
        connections = new HashMap();
        
        // The incoming messages are dispatched to the clients.
        inReactor = new HeaderReactor(
            new HeaderInInterceptor(
                new QueueInInterceptor(queueIn),
                MsgHeaderConstants.DEST_CONNECTOR),
                processors.getProcessors());
        
        inReactor.register(StreamManager.NAME, streamManager);
        // The stream manager writes to the output queue of the server.
        streamManager.setOutput(
            new HeaderOutInterceptor(
                MsgHeaderConstants.SRC_CONNECTOR,
                StreamManager.NAME,
                new QueueOutInterceptor(queueOut)
                ));
        
        connectors = new HashMap();
        // Registers the Connectors.
        for (Iterator iter = aCollOfConnectors.iterator();
            iter.hasNext();) {
            Connector connector = (Connector) iter.next();
            addConnector(connector);
        }
    }

    /**
     * Gets the name of this node.
     */
    public String getName() {
        return nodeInfo.getName();
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
     * 
     * @param aServantName Node name.
     * @return Output to be used to communicate with the specified node.
     */
    public MsgOutInterceptor getOutForNode(String aNodeName)
        throws CommunicationException {
        return metaConnection.getOutForNode(aNodeName);
    }
    
    /**
     * Registers a new Connector.
     * 
     * @param aConnector Connector to be registered.
     */
    public void addConnector(Connector aConnector) {
        String pName = aConnector.getName();
        // Connectors write to the outbound Msg queue.
        aConnector.setOutput(
            new HeaderOutInterceptor(
                MsgHeaderConstants.SRC_CONNECTOR,
                pName,
                new QueueOutInterceptor(queueOut)
                ));
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
    public void removeConnector(Connector aConnector) {
        String pName = aConnector.getName();
        aConnector.setOutput(null);
        inReactor.unregister(pName);
        synchronized (connectors) {
            connectors.remove(pName);
        }
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
    
    public void setGBeanContext(GBeanContext aContext) {
        context = aContext;
    }

    public void doStart() throws WaitingException, Exception {
        // Start the thread pool.
        start();
        
        processors.start();
    }

    public void doStop() throws WaitingException, Exception {
        stop();
        
        processors.stop();
    }

    public void doFail() {
        try {
            stop();
        } catch (InterruptedException e) {
            log.error("Exception when stopping server", e);
        }

        processors.stop();
    }
    
    public String toString() {
        return "Node {" + nodeInfo + "}";
    }

}