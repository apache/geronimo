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
import java.net.InetAddress;
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
 * A ServantNode is the counterpart of a ServerNode: it allows to access a
 * ServerNode remotely.
 * <BR>
 * The following diagram shows how ServerNode, ServantNode and Connector are
 * combined together:
 * 
 * Connector -- MTO -- ServantNode -- MTO -- ServerNode -- OTM -- Connector
 *
 * Connector communicates with each other by sending Msgs.
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/25 13:36:15 $
 */
public class ServerNode
    extends ThreadedServer
    implements GBean
{
    
    private static final Log log = LogFactory.getLog(ServerNode.class);

    /**
     * Server name.
     */
    private final String name;
    
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
    private final ServerProcessors processors;
    
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
    public ServerNode(String aName, Collection aCollOfConnectors,
        InetAddress anAddress, int aPort, int aMaxRequest) {
        super(anAddress, aPort);
        if ( null == aName ) {
            throw new IllegalArgumentException("Name is required.");
        }
        
        name = aName;
        
        // No socket timeout.
        setMaxIdleTimeMs(0);
        
        streamManager = new StreamManagerImpl(name);
        
        processors = new ServerProcessors(this);
        
        queueIn = new MsgQueue(aName + " Inbound");
        queueOut = new MsgQueue(aName + " Outbound");
        
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
     * Gets the StreamManager of this server.
     * 
     * @return StreamManager used by this server to resolve/encode InputStreams.
     */
    public StreamManager getStreamManager() {
        return streamManager;
    }

    /**
     * Gets the Output to be used to communicate with the specified servant.
     * 
     * @param aServantName Servant name.
     * @return Output to be used to communicate with the specified servant.
     */
    public MsgOutInterceptor getOutForServant(Object aServantName) {
        ConnectionWrapper connection; 
        synchronized (connections) {
            connection = (ConnectionWrapper) connections.get(aServantName);
        }
        return connection.out;
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
        ConnectionWrapper connection = initConnection(anIn, anOut);
        
        // Wait until the end of the connection.
        Object releaser = connection.endReleaser;
        synchronized (releaser) {
            try {
                releaser.wait();
            } catch (InterruptedException e) {
                log.error(e);
            }
        }
        
        removeConnection(connection);
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

    /**
     * Initializes a connection. Checks that a connection with the same name
     * is not already registered by the server. If a connection with the same
     * name exists, then the server refuses the connection and exits. Otherwise,
     * a connection is registered with the provided name.
     * 
     * @param anIn Raw input of the connection.
     * @param anOut Raw output of the connection.
     * @return Connection.
     */
    private ConnectionWrapper initConnection(
        InputStream anIn, OutputStream anOut) {
        ConnectionWrapper connection = new ConnectionWrapper(anIn, anOut);
        
        Msg msg = connection.in.pop();
        MsgBody body = msg.getBody();
        String cName = (String) body.getContent();
        
        msg = new Msg();
        body = msg.getBody();
        synchronized (connections) {
            if ( connections.containsKey(cName) ) {
                body.setContent(Boolean.FALSE);
                connection.out.push(msg);
                throw new RuntimeException(cName + " already registered");
            }
            connection.nodeName = cName;
            addConnection(connection);
        }
        body.setContent(Boolean.TRUE);
        connection.out.push(msg);
        return connection;
    }

    /**
     * Releases a connection.
     * 
     * @param aConnection Connection to be released.
     */
    private void removeConnection(ConnectionWrapper aConnection) {
        synchronized(connections) {
            connections.remove(aConnection.nodeName);
        }
        processors.stopConnection(aConnection);
        aConnection.close();
    }

    /**
     * Registers a connection.
     * 
     * @param aConnection Connection to be registered.
     */
    private void addConnection(ConnectionWrapper aConnection) {
        synchronized(connections) {
            connections.put(aConnection.nodeName, aConnection);
            processors.startConnection(aConnection);
        }
    }
    
    class ConnectionWrapper {
        final MsgInInterceptor in;
        final InputStream rawIn;
        final MsgOutInterceptor out;
        final OutputStream rawOut;
        String nodeName;
        final Object endReleaser;
        private ConnectionWrapper(InputStream anIn, OutputStream anOut) {
            rawIn = anIn;
            in = new StreamInInterceptor(rawIn, streamManager);
            rawOut = anOut;
            out =
                new HeaderOutInterceptor(
                    MsgHeaderConstants.SRC_NODE,
                    name,
                    new StreamOutInterceptor(anOut, streamManager));
            endReleaser = new Object();
        }
        
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
        }
    }

}