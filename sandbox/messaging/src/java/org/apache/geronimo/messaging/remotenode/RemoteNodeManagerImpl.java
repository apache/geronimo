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

package org.apache.geronimo.messaging.remotenode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.messaging.CommunicationException;
import org.apache.geronimo.messaging.Msg;
import org.apache.geronimo.messaging.MsgHeader;
import org.apache.geronimo.messaging.MsgHeaderConstants;
import org.apache.geronimo.messaging.NodeException;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.NodeTopology;
import org.apache.geronimo.messaging.RequestSender;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;
import org.apache.geronimo.messaging.io.IOContext;

/**
 * RemoteNode implementation.
 *
 * @version $Revision: 1.2 $ $Date: 2004/05/27 14:27:32 $
 */
public class RemoteNodeManagerImpl
    implements RemoteNodeManager
{

    private static final Log log = LogFactory.getLog(RemoteNodeManagerImpl.class);
    
    private final MessagingTransportFactory factory;
    private final IOContext ioContext;
    private final Collection listeners;
    private final Map remoteNodes;
    private final RemoteNodeRouter router;
    private final NodeServer server;
    private final NodeInfo nodeInfo;
    
    /**
     * Node topology to be used to derive the most appropriate path to reach
     * a node.
     */
    private NodeTopology topology;
    
    public RemoteNodeManagerImpl(NodeInfo aNodeInfo, IOContext anIOContext,
        MessagingTransportFactory aFactory) {
        if ( null == aNodeInfo ) {
            throw new IllegalArgumentException("NodeInfo is required.");
        } else if ( null == anIOContext ) {
            throw new IllegalArgumentException("IOContext is required.");
        } else if ( null == aFactory ) {
            throw new IllegalArgumentException("Factory is required.");
        }
        nodeInfo = aNodeInfo;
        ioContext = anIOContext;
        factory = aFactory;
        
        listeners = new ArrayList();
        remoteNodes = new HashMap();
        router = new RemoteNodeRouter();
        server = aFactory.factoryServer(aNodeInfo, anIOContext);
        server.setRemoteNodeManager(this);
    }

    public void start() throws NodeException {
        try {
            server.start();
        } catch (IOException e) {
            throw new NodeException("Can not start server.", e);
        } catch (CommunicationException e) {
            throw new NodeException("Can not start server.", e);
        }
    }
    
    public void stop() throws NodeException {
        synchronized(remoteNodes) {
            for (Iterator iter = remoteNodes.values().iterator(); iter.hasNext();) {
                RemoteNode node = (RemoteNode) iter.next();
                try {
                    node.leave();
                } catch (IOException e) {
                    log.error(e);
                } catch (CommunicationException e) {
                    log.error(e);
                } finally {
                    node.setMsgProducerOut(null);
                    iter.remove();
                }
            }
        }
        try {
            server.stop();
        } catch (IOException e) {
            throw new NodeException("Can not stop NodeServer.", e);
        } catch (CommunicationException e) {
            throw new NodeException("Can not stop NodeServer.", e);
        }
    }

    public void setTopology(NodeTopology aTopology) {
       topology = aTopology; 
    }
    
    public void addListener(RemoteNodeEventListener aListener) {
        synchronized(listeners) {
            listeners.add(aListener);
        }
    }

    public void removeListener(RemoteNodeEventListener aListener) {
        synchronized(listeners) {
            listeners.remove(aListener);
        }
    }
    
    public void leaveRemoteNode(NodeInfo aNodeInfo)
        throws NodeException {
        synchronized(remoteNodes) {
            RemoteNode remoteNode = findRemoteNode(aNodeInfo);
            if ( null == remoteNode ) {
                return;
            }
            try {
                remoteNode.leave();
            } catch (IOException e) {
                throw new NodeException("Can not leave " + aNodeInfo, e);
            } catch (CommunicationException e) {
                throw new NodeException("Can not leave " + aNodeInfo, e);
            } finally {
                unregisterRemoteNode(remoteNode);
            }
        }
    }
    
    public RemoteNode findOrJoinRemoteNode(NodeInfo aNodeInfo)
        throws NodeException {
        RemoteNode remoteNode;
        synchronized(remoteNodes) {
            remoteNode = findRemoteNode(aNodeInfo);
            if ( null != remoteNode ) {
                return remoteNode;
            }
            remoteNode = factory.factoryNode(aNodeInfo, ioContext);
            try {
                remoteNode.connect();
            } catch (IOException e) {
                throw new NodeException("Can not reach " + aNodeInfo, e);
            } catch (CommunicationException e) {
                throw new NodeException("Can not reach " + aNodeInfo, e);
            }
            Msg msg = new Msg();
            MsgHeader header = msg.getHeader();
            header.addHeader(MsgHeaderConstants.SRC_NODE, nodeInfo);
            header.addHeader(MsgHeaderConstants.DEST_NODE, aNodeInfo);
            
            // Only set to comply with a valid request. 
            header.addHeader(MsgHeaderConstants.DEST_NODES, aNodeInfo);
            header.addHeader(MsgHeaderConstants.SRC_ENDPOINT, "");
            header.addHeader(MsgHeaderConstants.CORRELATION_ID,
                new RequestSender.RequestID(new Integer(0)));
            
            msg.getBody().setContent(nodeInfo);
            remoteNode.getMsgConsumerOut().push(msg);
            
            registerRemoteNode(remoteNode);
        }
        return remoteNode;
    }
    
    public RemoteNode findRemoteNode(NodeInfo aNodeInfo) {
        synchronized(remoteNodes) {
            return (RemoteNode) remoteNodes.get(aNodeInfo);
        }
    }

    public void registerRemoteNode(RemoteNode aRemoteNode) {
        synchronized(remoteNodes) {
            remoteNodes.put(aRemoteNode.getNodeInfo(), aRemoteNode);
        }
        notifyListeners(
            new RemoteNodeEvent(aRemoteNode, RemoteNodeEvent.NODE_ADDED));
    }

    public void unregisterRemoteNode(RemoteNode aRemoteNode) {
        synchronized(remoteNodes) {
            remoteNodes.remove(aRemoteNode.getNodeInfo());
        }
        notifyListeners(
            new RemoteNodeEvent(aRemoteNode, RemoteNodeEvent.NODE_REMOVED));
    }
    
    public Collection listRemoteNodes() {
        return Collections.unmodifiableCollection(remoteNodes.values());
    }

    public MsgOutInterceptor getMsgConsumerOut() {
        return router;
    }

    private void notifyListeners(RemoteNodeEvent anEvent) {
        Collection tmpListeners;
        synchronized(listeners) {
            tmpListeners = new ArrayList(listeners);
        }
        for (Iterator iter = tmpListeners.iterator(); iter.hasNext();) {
            RemoteNodeEventListener listener =
                (RemoteNodeEventListener) iter.next();
            listener.fireRemoteNodeEvent(anEvent);
        }
    }
    
    /**
     * Msg consumer.
     */
    private class RemoteNodeRouter implements MsgOutInterceptor {

        public void push(Msg aMsg) {
            if ( null == topology ) {
                throw new RuntimeException("No topology is set.");
            }
            
            MsgHeader header = aMsg.getHeader();
            Object destNode = header.getHeader(MsgHeaderConstants.DEST_NODES);
            if (destNode instanceof NodeInfo) {
                destNode = new NodeInfo[] {(NodeInfo) destNode };
            }
            MsgOutInterceptor out;
            NodeInfo[] dests = (NodeInfo[]) destNode;
            for (int i = 0; i < dests.length; i++) {
                NodeInfo target = dests[i];
                Msg msg2 = new Msg(aMsg);
                MsgHeader header2 = msg2.getHeader();
                // A path is defined if this Msg is routed via the node
                // owning this instance.
                NodeInfo[] path =
                    (NodeInfo[]) header2.getOptionalHeader(
                        MsgHeaderConstants.DEST_NODE_PATH);
                if ( null != path ) {
                    // A path has already been computed. Gets the next hop.
                    target = path[0];
                    // Pops the next hop from the path.
                    header2.addHeader(
                        MsgHeaderConstants.DEST_NODE_PATH,
                        NodeInfo.pop(path));
                    RemoteNode remoteNode = findRemoteNode(target);
                    out = remoteNode.getMsgConsumerOut();
                } else {
                    // A path has not already been computed. Computes one.
                    NodeInfo src = (NodeInfo)
                        header2.getHeader(MsgHeaderConstants.SRC_NODE);
                    path = topology.getPath(src, target);
                    if ( null == path ) {
                        throw new RuntimeException("{" + target +
                            "} is not reachable by {" + src + "}");
                    }
                    NodeInfo tmpNode = path[0];
                    RemoteNode remoteNode = findRemoteNode(tmpNode);
                    if ( null == remoteNode ) {
                        throw new RuntimeException("{" + target +
                            "} is not reachable by {" + src + "}");
                    }
                    out = remoteNode.getMsgConsumerOut();
                    
                    NodeInfo[] newPath = NodeInfo.pop(path);
                    // Inserts the computed path and the new dests.
                    header2.addHeader(MsgHeaderConstants.DEST_NODE_PATH, newPath);
                    header2.addHeader(MsgHeaderConstants.DEST_NODES, target);
                }
                out.push(msg2);
            }
        }
        
    }
    
}
