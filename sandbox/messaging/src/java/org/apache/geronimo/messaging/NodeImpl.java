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

package org.apache.geronimo.messaging;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.messaging.interceptors.HeaderOutInterceptor;
import org.apache.geronimo.messaging.interceptors.MsgOutDispatcher;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;
import org.apache.geronimo.messaging.interceptors.ThrowableTrapOutInterceptor;
import org.apache.geronimo.messaging.io.IOContext;
import org.apache.geronimo.messaging.io.ReplacerResolver;
import org.apache.geronimo.messaging.io.StreamManager;
import org.apache.geronimo.messaging.io.StreamManagerImpl;
import org.apache.geronimo.messaging.proxy.EndPointProxyFactory;
import org.apache.geronimo.messaging.proxy.EndPointProxyFactoryImpl;
import org.apache.geronimo.messaging.proxy.EndPointProxyInfo;
import org.apache.geronimo.messaging.reference.ReferenceableManager;
import org.apache.geronimo.messaging.reference.ReferenceableManagerImpl;
import org.apache.geronimo.messaging.remotenode.LogicalCompression;
import org.apache.geronimo.messaging.remotenode.MessagingTransportFactory;
import org.apache.geronimo.messaging.remotenode.RemoteNode;
import org.apache.geronimo.messaging.remotenode.RemoteNodeEvent;
import org.apache.geronimo.messaging.remotenode.RemoteNodeEventListener;
import org.apache.geronimo.messaging.remotenode.RemoteNodeManager;
import org.apache.geronimo.messaging.remotenode.RemoteNodeManagerImpl;
import org.apache.geronimo.system.ClockPool;
import org.apache.geronimo.system.ThreadPool;

/**
 * Node implementation.
 *
 * @version $Revision: 1.6 $ $Date: 2004/07/05 07:03:50 $
 */
public class NodeImpl
    implements Node, GBeanLifecycle
{

    private static final Log log = LogFactory.getLog(NodeImpl.class);

    /**
     * Node meta-data.
     */
    private final NodeInfo nodeInfo;
    
    /**
     * StreamManager to register/retrieve distributed InputStreams.
     */
    private final StreamManager streamManager;

    /**
     * ReferenceableManager tracking the marshalled Referenceables and
     * resolving the ReferenceInfo when unmarshalled.
     */
    private final ReferenceableManager referenceableManager;
    
    /**
     * Used to create/release EndPoint proxies.
     */
    private final EndPointProxyFactory endPointProxyFactory;
    
    /**
     * Used to replace or resolve objects during the Serialization of
     * instances sent to remote nodes. 
     */
    private final ReplacerResolver replacerResolver;
    
    /**
     * Inbound Msgs dispatcher.
     */
    private final MsgOutDispatcher inDispatcher;
    
    /**
     * RemoteNode manager.
     */
    private final RemoteNodeManager nodeManager;

    /**
     * LogicalCompression to be applied to Msgs sent to remote Nodes.
     */
    private final LogicalCompression compression;
    
    /**
     * Used to dispatch async requests.
     */
    private final ThreadPool threadPool;

    /**
     * Used to execute periodical tasks.
     */
    private final ClockPool clockPool;

    /**
     * NodeTopology within which this node is operating.
     */
    private NodeTopology nodeTopology;
    
    /**
     * To serialize the topology changes.
     */
    private final Object topologyMonitor;
    
    /**
     * Creates a Node.
     * 
     * @param aNodeInfo Node meta-data.
     * @param aThreadPool Pool of threads.
     * @param aClockPool To execute period tasks.
     * @param aFactory Transport layer factory.
     */
    public NodeImpl(NodeInfo aNodeInfo, ThreadPool aThreadPool,
        ClockPool aClockPool, MessagingTransportFactory aFactory) {
        if ( null == aNodeInfo ) {
            throw new IllegalArgumentException("NodeInfo is required.");
        } else if ( null == aThreadPool ) {
            throw new IllegalArgumentException("Thread Pool is required.");
        } else if ( null == aClockPool ) {
            throw new IllegalArgumentException("Clock Pool is required.");
        } else if ( null == aFactory ) {
            throw new IllegalArgumentException("Factory is required.");
        }
        nodeInfo = aNodeInfo;
        threadPool = aThreadPool; 
        clockPool = aClockPool;
        
        replacerResolver = new MsgReplacerResolver();
        topologyMonitor = new Object();

        streamManager = newStreamManager();
        referenceableManager = newReferenceableManager();
        endPointProxyFactory = newEndPointProxyFactory();
        
        compression = new LogicalCompression();
        IOContext ioContext = new IOContext();
        ioContext.setPopSynchronization(compression);
        ioContext.setPushSynchronization(compression);
        ioContext.setReplacerResolver(replacerResolver);
        ioContext.setStreamManager(streamManager);
        
        nodeManager = new RemoteNodeManagerImpl(aNodeInfo, ioContext, 
            aClockPool, aFactory);
        nodeManager.addListener(new RemoteNodeTracker());
        
        // The incoming messages are dispatched to the EndPoints based on
        // their destination endpoint header.
        inDispatcher = new MsgOutDispatcher(MsgHeaderConstants.DEST_ENDPOINT);

        addEndPoint(endPointProxyFactory);
        addEndPoint(referenceableManager);
        addEndPoint(streamManager);
        addEndPoint(new NodeEndPointViewImpl());
    }

    public ReplacerResolver getReplacerResolver() {
        return replacerResolver;
    }
    
    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }
    
    public void setTopology(NodeTopology aTopology) {
        synchronized(topologyMonitor) {
            cascadeTopology(aTopology, Collections.EMPTY_SET);
        }
    }
    
    private void cascadeTopology(NodeTopology aTopology, Set aSetOfProcessed) {
        // Registers a future topology here. This way neighbours can start to
        // send Msgs compressed with the new topology.
        compression.registerFutureTopology(aTopology);
        
        // Applies the new topology.
        nodeManager.setTopology(aTopology);

        // Computes the neighbours which have not yet received the topology
        // reconfiguration.
        Set neighbours = new HashSet(aTopology.getNeighbours(nodeInfo));
        neighbours.removeAll(aSetOfProcessed);
        
        // Computes the nodes which have already received the topology
        // reconfiguration.
        Set processed = new HashSet(aSetOfProcessed);
        processed.add(nodeInfo);
        processed.addAll(neighbours);

        NodeInfo[] targets = (NodeInfo[]) neighbours.toArray(new NodeInfo[0]);
        // No more nodes to process.
        if ( 0 == targets.length ) {
            return;
        }
      
        // Acquires a proxy on the NodeEndPointViews of all the neighbours,
        // which have not yet been processed.
        EndPointProxyInfo proxyInfo =
            new EndPointProxyInfo(NodeEndPointView.NODE_ID,
                new Class[] {NodeEndPointView.class}, targets);
        NodeEndPointView topologyEndPoint = (NodeEndPointView)
            endPointProxyFactory.factory(proxyInfo);
        try {
            // Cascades the new topology to all of them.
            topologyEndPoint.cascadeTopology(aTopology, processed);
        } finally {
            endPointProxyFactory.releaseProxy(topologyEndPoint);
        }
        
        compression.registerTopology(aTopology);
        nodeTopology = aTopology;
    }
    
    public NodeTopology getTopology() {
        return nodeTopology;
    }
    
    public void addEndPoint(EndPoint anEndPoint) {
        Object id = anEndPoint.getID();
        MsgOutInterceptor out = newOutboundMsgProviderOut();
        anEndPoint.setMsgProducerOut(out);
        inDispatcher.register(id, anEndPoint.getMsgConsumerOut());
    }
    
    public void removeEndPoint(EndPoint anEndPoint) {
        Object id = anEndPoint.getID();
        anEndPoint.setMsgProducerOut(null);
        inDispatcher.unregister(id);
    }
    
    public Object factoryEndPointProxy(EndPointProxyInfo anInfo) {
        return endPointProxyFactory.factory(anInfo);
    }
    
    public void releaseEndPointProxy(Object aProxy) {
        endPointProxyFactory.releaseProxy(aProxy);
    }
    
    public void doStart() throws WaitingException, Exception {
        endPointProxyFactory.start();
        referenceableManager.start();
        streamManager.start();
        nodeManager.start();
        NodeTopology topology = new NodeTopology() {
            public Set getNeighbours(NodeInfo aRoot) {
                return Collections.EMPTY_SET;
            }
            public NodeInfo[] getPath(NodeInfo aSource, NodeInfo aTarget) {
                throw new UnsupportedOperationException();
            }
            public int getIDOfNode(NodeInfo aNodeInfo) {
                throw new UnsupportedOperationException();
            }
            public NodeInfo getNodeById(int anId) {
                throw new UnsupportedOperationException();
            }
            public Set getNodes() {
                Set result = new HashSet();
                result.add(nodeInfo);
                return result;
            }
            public int getVersion() {
                return 0;
            }
            public void setVersion(int aVersion) {
                throw new UnsupportedOperationException();
            }
        };
        setTopology(topology);
    }

    public void doStop() throws WaitingException, Exception {
        nodeManager.stop();
        streamManager.stop();
        referenceableManager.stop();
        endPointProxyFactory.stop();
    }

    public void doFail() {
        try {
            nodeManager.stop();
        } catch (NodeException e) {
            log.error("Can not stop node manager.", e);
        }
        streamManager.stop();
        referenceableManager.stop();
        endPointProxyFactory.stop();
    }
    
    public String toString() {
        return "Node: " + nodeInfo + "";
    }

    /**
     * Returns a StreamManager implementation.
     * 
     * @return StreamManager.
     */
    protected StreamManager newStreamManager() {
        return new StreamManagerImpl(this); 
    }

    /**
     * Returns a ReferenceableManager inplementation.
     * 
     * @return ReferenceableManager
     */
    protected ReferenceableManager newReferenceableManager() {
        return new ReferenceableManagerImpl(this, "ReferenceableManager");
    }

    /**
     * Returns an EndPointProxyFactory implementation.
     * 
     * @return EndPointProxyFactory
     */
    protected EndPointProxyFactory newEndPointProxyFactory() {
        return new EndPointProxyFactoryImpl(this, "EndPointProxyFactory");
    }
    
    /**
     * Creates a MsgConsumer input for outbound Msgs (Msgs pushed to
     * the RemoteNodeManager).  
     * 
     * @return Output to be used to push Msgs to the RemoteNodeManager. 
     */
    private MsgOutInterceptor newOutboundMsgProviderOut() {
        return
            new HeaderOutInterceptor(
                MsgHeaderConstants.SRC_NODE,
                nodeInfo,
                nodeManager.getMsgConsumerOut());
    }
    
    /**
     * Creates a MsgProducer output for incoming Msgs (Msgs produced by
     * the RemoteNodeManager).  
     * 
     * @return Output to be used as a MsgProducer output when a RemoteNode
     * join this Node. 
     */
    private MsgOutInterceptor newInboundMsgProducerOut() {
        return
            new ThreadedDispatcher(
                new ThrowableTrapOutInterceptor(new MsgForwarder(),
                    new ExceptionTracker()));
    }

    private class ThreadedDispatcher implements MsgOutInterceptor {
        private MsgOutInterceptor out;
        private ThreadedDispatcher(MsgOutInterceptor anOut) {
            out = anOut;
        }
        public void push(final Msg aMsg) {
            try {
                threadPool.getWorkManager().execute(new Runnable() {
                    public void run() {
                        out.push(aMsg);
                    }
                });
            } catch (InterruptedException e) {
                log.error("Async Msg dispatcher failure.", e);
                throw new CommunicationException(e);
            }
        }
    }
    
    /**
     * Handles the notifications sent by RemoteNodeManager.   
     */
    private class RemoteNodeTracker implements RemoteNodeEventListener {

        public void fireRemoteNodeEvent(RemoteNodeEvent anEvent) {
            RemoteNode node = anEvent.getRemoteNode(); 
            if ( anEvent.isAddEvent() ) {
                // When a RemoteNode is added, registers ourself as the Msg
                // consumer.
                node.setMsgProducerOut(newInboundMsgProducerOut());
            } else {
                // Reset as we are not more a Msg consumer.  
                node.setMsgProducerOut(null);
            }
        }
        
    }

    /**
     * Tracks the Msgs which are not successfully delivered/processed.
     * <BR>
     * Sends them back to their source in case of an exception during their
     * processing.
     */
    private class ExceptionTracker
        implements ThrowableTrapOutInterceptor.ThrowableTrapHandler {

        public void push(Msg aMsg, Throwable aThrowable) {
            log.error("Can not deliver " + aMsg, aThrowable);
            // Send the Msg back to the caller and provide the exception.
            Msg msg = aMsg.reply();
            msg.getBody().setContent(new Result(false, aThrowable));
            nodeManager.getMsgConsumerOut().push(aMsg);
        }
        
    }

    /**
     * Forwards the incoming Msgs (coming from the RemoteNodeManager) to the
     * relevant EndPoint or Node. 
     */
    private class MsgForwarder implements MsgOutInterceptor {

        public void push(Msg aMsg) {
            MsgHeader header = aMsg.getHeader();
            NodeInfo[] path = (NodeInfo[])
                header.getOptionalHeader(MsgHeaderConstants.DEST_NODE_PATH);
            if ( null == path ) {
                // The path is null when the Msg reaches its destination node.
                // Tries to dispatch it to the relevant EndPoint.
                inDispatcher.push(aMsg);
            } else {
                // This Node is a "router". Pushes the Msg back to the node
                // manager, which will transfer it to the next Node.
                nodeManager.getMsgConsumerOut().push(aMsg);
            }
        }
        
    }

    private class NodeEndPointViewImpl extends BaseEndPoint
        implements NodeEndPointView {
        private NodeEndPointViewImpl() {
            super(NodeImpl.this, NODE_ID);
        }
        public void cascadeTopology(NodeTopology aTopology, Set aSetOfProcessed) {
            NodeImpl.this.cascadeTopology(aTopology, aSetOfProcessed);
        }
    }
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory factory = new GBeanInfoFactory(NodeImpl.class);
        factory.setConstructor(new String[] {"NodeInfo", "MessagingTransportFactory"});
        factory.addAttribute("NodeInfo", NodeInfo.class, true);
        factory.addAttribute("MessagingTransportFactory", MessagingTransportFactory.class, true);
        factory.addAttribute("Topology", NodeTopology.class, true);
        factory.addOperation("addEndPoint", new Class[]{EndPoint.class});
        factory.addOperation("removeEndPoint", new Class[]{EndPoint.class});
        GBEAN_INFO = factory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}