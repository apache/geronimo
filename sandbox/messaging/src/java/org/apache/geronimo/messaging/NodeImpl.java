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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.messaging.interceptors.ThrowableTrapOutInterceptor;
import org.apache.geronimo.messaging.interceptors.HeaderOutInterceptor;
import org.apache.geronimo.messaging.interceptors.MsgOutDispatcher;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;
import org.apache.geronimo.messaging.io.IOContext;
import org.apache.geronimo.messaging.io.NullReplacerResolver;
import org.apache.geronimo.messaging.io.ReplacerResolver;
import org.apache.geronimo.messaging.io.StreamManager;
import org.apache.geronimo.messaging.io.StreamManagerImpl;
import org.apache.geronimo.messaging.reference.ReferenceableManager;
import org.apache.geronimo.messaging.reference.ReferenceableManagerImpl;
import org.apache.geronimo.messaging.remotenode.LogicalCompression;
import org.apache.geronimo.messaging.remotenode.MessagingTransportFactory;
import org.apache.geronimo.messaging.remotenode.RemoteNode;
import org.apache.geronimo.messaging.remotenode.RemoteNodeEvent;
import org.apache.geronimo.messaging.remotenode.RemoteNodeEventListener;
import org.apache.geronimo.messaging.remotenode.RemoteNodeManager;
import org.apache.geronimo.messaging.remotenode.RemoteNodeManagerImpl;
import org.apache.geronimo.system.ThreadPool;

/**
 * Node implementation.
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:41 $
 */
public class NodeImpl
    implements Node, GBean
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
     * Creates a Node.
     * 
     * @param aNodeInfo Node meta-data.
     * @param aThreadPool Pool of threads.
     * @param aFactory Transport layer factory.
     */
    public NodeImpl(NodeInfo aNodeInfo, ThreadPool aThreadPool,
        MessagingTransportFactory aFactory) {
        if ( null == aNodeInfo ) {
            throw new IllegalArgumentException("NodeInfo is required.");
        } else if ( null == aThreadPool ) {
            throw new IllegalArgumentException("Pool is required.");
        } else if ( null == aFactory ) {
            throw new IllegalArgumentException("Factory is required.");
        }
        nodeInfo = aNodeInfo;
        threadPool = aThreadPool; 
        
        replacerResolver = new NullReplacerResolver();
        streamManager = newStreamManager();
        referenceableManager = newReferenceableManager();
        
        compression = new LogicalCompression();
        IOContext ioContext = new IOContext();
        ioContext.setPopSynchronization(compression);
        ioContext.setPushSynchronization(compression);
        ioContext.setReplacerResolver(replacerResolver);
        ioContext.setStreamManager(streamManager);
        
        nodeManager = new RemoteNodeManagerImpl(aNodeInfo, ioContext, aFactory);
        nodeManager.addListener(new RemoteNodeTracker());
        
        // The incoming messages are dispatched to the EndPoints.
        inDispatcher = new MsgOutDispatcher(MsgHeaderConstants.DEST_ENDPOINT);
        inDispatcher.register(StreamManager.NAME, streamManager.getMsgConsumerOut());

        MsgOutInterceptor out = newOutboundMsgProviderOut();
        streamManager.setMsgProducerOut(out);
    }

    public ReplacerResolver getReplacerResolver() {
        return replacerResolver;
    }
    
    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }
    
    public void setTopology(NodeTopology aTopology) {
        compression.setTopology(aTopology);
        nodeManager.setTopology(aTopology);
    }
    
    public void join(NodeInfo aNodeInfo) throws NodeException {
        nodeManager.findOrJoinRemoteNode(aNodeInfo);
    }
    
    public void leave(NodeInfo aNodeInfo) throws NodeException {
        nodeManager.leaveRemoteNode(aNodeInfo);
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
    
    public void setGBeanContext(GBeanContext aContext) {
    }

    public void doStart() throws WaitingException, Exception {
        referenceableManager.doStart();
        streamManager.doStart();
        nodeManager.start();
        
    }

    public void doStop() throws WaitingException, Exception {
        nodeManager.stop();
        streamManager.doStop();
        referenceableManager.doStop();
    }

    public void doFail() {
        try {
            nodeManager.stop();
        } catch (NodeException e) {
            log.error("Can not stop node manager.", e);
        }
        try {
            streamManager.doStop();
        } catch (Exception e) {
            log.error("Can not stop stream manager.", e);
        }
        try {
            referenceableManager.doStop();
        } catch (Exception e) {
            log.error("Can not stop referenceable manager.", e);
        }
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
                throw new RuntimeException(e);
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
     */
    private class ExceptionTracker
        implements ThrowableTrapOutInterceptor.ThrowableTrapHandler {

        public void push(Msg aMsg, Throwable aThrowable) {
            log.error("Can not deliver " + aMsg, aThrowable);
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
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory factory = new GBeanInfoFactory(NodeImpl.class);
        factory.setConstructor(
            new String[] {"NodeInfo", "MessagingTransportFactory"},
            new Class[] {NodeInfo.class, MessagingTransportFactory.class});
        factory.addAttribute("NodeInfo", true);
        factory.addAttribute("MessagingTransportFactory", true);
        factory.addAttribute("Topology", true);
        factory.addOperation("join", new Class[]{NodeInfo.class});
        factory.addOperation("leave", new Class[]{NodeInfo.class});
        factory.addOperation("addEndPoint", new Class[]{EndPoint.class});
        factory.addOperation("removeEndPoint", new Class[]{EndPoint.class});
        GBEAN_INFO = factory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}