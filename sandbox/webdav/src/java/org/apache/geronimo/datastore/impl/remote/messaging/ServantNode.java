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
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.WaitingException;

/**
 * Provides a remote connectivity to a ServerNode.
 * <BR>
 * See ServerNode for more details.
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/25 13:36:15 $
 */
public class ServantNode
    implements GBean
{

    private static final Log log = LogFactory.getLog(ServantNode.class);
    
    /**
     * Address of the server used by this servant.
     */
    private final InetAddress address;
    
    /**
     * Port of the server used by this servant.
     */
    private final int port;
    
    /**
     * Map of registered Connectors. The key is the name of the Connector and 
     * the value is the Connector itself.
     */
    private final Map connectors;
    
    /**
     * StreamManager used to register/retrieve distributed InputStream.
     */
    private final StreamManager streamManager;
    
    /**
     * Name of this servant.
     */
    private final String name;
    
    /**
     * Inbound MsgQueue. It is a buffer between Connectors and the network.
     * This buffer insulates Connectors from IOException related to a direct
     * connection to the network.
     */
    private final MsgQueue queueIn;
    
    /**
     * Outbound MsgQueue.
     */
    private final MsgQueue queueOut;

    /**
     * Logical connection between this servant and its server.
     */
    private ConnectionWrapper connection;

    /**
     * In charge of dispatching the inbound Msgs seating in the Inbound queue.
     */
    private final HeaderReactor reactor;
    
    /**
     * In charge of handling all the Processor execution.
     */
    private final Processors processors;
    
    private GBeanContext context;
    

    /**
     * Creates a servant node.
     * 
     * @param aName Name of the servant.
     * @param aCollOfConnectors Collection of Connector instances.
     * @param anAddess Address of the server to be used by this servant.
     * @param aPort Port of the server to be used by this servant.
     * @param aMaxRequest Maximum number of concurrent requests, which can be
     * processed by this servant.
     */
    public ServantNode(String aName, Collection aCollOfConnectors,
        InetAddress anAddess, int aPort, int aMaxRequest) {
        if ( null == aName ) {
            throw new IllegalArgumentException("Name is required.");
        } else if ( null == anAddess ) {
            throw new IllegalArgumentException("Address is required.");
        } else if ( 0 == aPort ) {
            throw new IllegalArgumentException("Port is required.");
        }
        
        name = aName;
        address = anAddess;
        port = aPort;

        // One needs at least 3 processors for the Inbound and Outbound queues.
        processors = new Processors(aName, 3, aMaxRequest + 3);
        
        streamManager = new StreamManagerImpl(name);
        
        queueIn = new MsgQueue(aName + " Inbound");
        queueOut = new MsgQueue(aName + " Outbound");
        
        // Inbound Msgs are processed by the reactor, which invokes the
        // relevant Connector based on the header of the inbound Msg.
        HeaderInInterceptor in =
            new HeaderInInterceptor(
                new QueueInInterceptor(queueIn),
                    MsgHeaderConstants.DEST_CONNECTOR);
        reactor = new HeaderReactor(in, processors);
        
        // The StreamManager writes to the Outbound queue of the servant.
        reactor.register(StreamManager.NAME, streamManager);
        streamManager.setOutput(
            new HeaderOutInterceptor(
                MsgHeaderConstants.SRC_CONNECTOR,
                StreamManager.NAME,
                new QueueOutInterceptor(queueOut)));
        
        connectors = new HashMap();
        // Registers the Connectors.
        for (Iterator iter = aCollOfConnectors.iterator(); iter.hasNext();) {
            Connector connector = (Connector) iter.next();
            addConnector(connector);
        }
    }

    /**
     * Registers a new Connector.
     * 
     * @param aConnector Connector to be registered.
     */
    public void addConnector(Connector aConnector) {
        String cName = aConnector.getName();
        synchronized (connectors) {
            connectors.put(cName, aConnector);
            reactor.register(cName, aConnector);
            // Connectors write to the outgoing queue of the servant.
            aConnector.setOutput(
                new HeaderOutInterceptor(
                    MsgHeaderConstants.SRC_CONNECTOR,
                    cName,
                    new QueueOutInterceptor(queueOut)));
        }
    }

    /**
     * Drops the specified Connector.
     * 
     * @param aConnector Connector to be unregistered.
     */
    public void removeConnector(Connector aConnector) {
        String cName = aConnector.getName();
        synchronized (connectors) {
            connectors.remove(cName);
            aConnector.setOutput(null);
            reactor.unregister(cName);
        }
    }
    
    public void setGBeanContext(GBeanContext aContext) {
        context = aContext;
    }

    public void doStart() throws WaitingException, Exception {
        initConnection();
        
        // Push the Msgs coming from the sockets to the incoming queue.
        fillQueueIn();
        // Push the outgoing queue blocks to the sockets.
        emptyQueueOut();
        
        // Dispatches the Msgs of the incoming queue to the Connectors.
        dispathQueueIn();
    }

    public void doStop() throws WaitingException, Exception {
        connection.close();
    }

    public void doFail() {
        connection.close();
    }

    /**
     * Initializes the connections between this servant and its server. By now,
     * only one connection is created, yet this method should create a logical
     * connection using N physical connections.
     * 
     * @throws IOException Indicates a communication issue.
     */
    private void initConnection()
        throws IOException {
        Socket socket = new Socket(address, port);
        
        connection = new ConnectionWrapper(socket.getInputStream(),
            socket.getOutputStream());
        
        // Try to register this servant to the server: one provides for this 
        // prototype the name of the servant.
        Msg msg = new Msg();
        MsgBody body = msg.getBody();
        body.setContent(name);
        connection.out.push(msg);
        
        msg = connection.in.pop();
        // In case of successful registration, the server returns true.
        Boolean success = (Boolean) msg.getBody().getContent();
        if ( !success.booleanValue() ) {
            log.error("Can not register Servant {" + name + "}");
            // Otherwise, one needs to fail this service.
            context.fail();
            return;
        }

    }
    
    /**
     * Fills the inbound Msg queue. It pops Msg from the logical connection.
     */
    private void fillQueueIn() {
        QueueOutInterceptor out = new QueueOutInterceptor(queueIn);
        MsgCopier copier = new MsgCopier(
            connection.in, out, connection.listener);
        processors.execute(copier);
    }
    
    /**
     * Dispatches Msg seating in the inbound queue to the relevant Connector.
     */
    private void dispathQueueIn() {
        processors.execute(reactor);
    }
    
    /**
     * Pushes Msg seating in the outbound Msg queue to the relevant server.
     */
    private void emptyQueueOut() {
        QueueInInterceptor in = new QueueInInterceptor(queueOut);
        MsgCopier copier =
            new MsgCopier(in, connection.out, connection.listener);
        processors.execute(copier);
    }

    /**
     * Logical connection to the server. By now, there is only one physical
     * connection, however it should contain more.
     */
    private class ConnectionWrapper {
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
         * Creates a connection using the specified input and output streams. 
         * 
         * @param anIn InputStream to read from.
         * @param anOut OutputStream to write to.
         */
        private ConnectionWrapper(InputStream anIn, OutputStream anOut) {
            if ( null == anIn ) {
                throw new IllegalArgumentException("InputStream is required.");
            } else if ( null == anOut ) {
                throw new IllegalArgumentException("OutputStream is required.");
            }
            rawIn = anIn;
            in = new StreamInInterceptor(anIn, streamManager);
            rawOut = anOut;
            // One adds the name of this node on exit.
            out =
                new HeaderOutInterceptor(
                    MsgHeaderConstants.SRC_NODE,
                    name,
                    new StreamOutInterceptor(anOut, streamManager));
            // One fails the whole GBean if connection fails.
            listener = new MsgCopier.NullCopierListener() {
                public void onFailure() {
                    context.fail();
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
        }
    }
    
}
