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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.messaging.CommunicationException;
import org.apache.geronimo.messaging.Msg;
import org.apache.geronimo.messaging.MsgHeader;
import org.apache.geronimo.messaging.MsgHeaderConstants;
import org.apache.geronimo.messaging.NodeException;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.NodeTopology;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;
import org.apache.geronimo.messaging.io.IOContext;
import org.apache.geronimo.pool.ClockPool;

/**
 * RemoteNode implementation.
 *
 * @version $Revision: 1.8 $ $Date: 2004/07/20 00:15:06 $
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
    private NodeServer server;
    private final NodeInfo nodeInfo;
    private final RemoteNodeMonitor remoteNodeMonitor;
    
    /**
     * Node topology to be used to derive the most appropriate path to reach
     * a node.
     */
    private NodeTopology topology;
    
    /**
     * Topology being prepared.
     */
    private NodeTopology preparedTopology;
    
    public RemoteNodeManagerImpl(NodeInfo aNodeInfo, IOContext anIOContext,
        ClockPool aClockPool, MessagingTransportFactory aFactory) {
        if ( null == aNodeInfo ) {
            throw new IllegalArgumentException("NodeInfo is required.");
        } else if ( null == anIOContext ) {
            throw new IllegalArgumentException("IOContext is required.");
        } else if ( null == aClockPool ) {
            throw new IllegalArgumentException("Clock Pool is required.");
        } else if ( null == aFactory ) {
            throw new IllegalArgumentException("Factory is required.");
        }
        nodeInfo = aNodeInfo;
        ioContext = anIOContext;
        factory = aFactory;
        
        listeners = new ArrayList();
        remoteNodes = new HashMap();
        router = new RemoteNodeRouter();
        remoteNodeMonitor = new RemoteNodeMonitor(this, aClockPool);
    }

    public void start() throws NodeException {
        log.info("Starting RemoteNodeManager for node {" + nodeInfo + "}");
        server = factory.factoryServer(nodeInfo, ioContext);
        server.setRemoteNodeManager(this);
        server.start();
        remoteNodeMonitor.start();
    }
    
    public void stop() throws NodeException {
        log.info("Stopping RemoteNodeManager for node {" + nodeInfo + "}");
        remoteNodeMonitor.stop();
        server.stop();
        Collection nodes;
        synchronized(remoteNodes) {
            nodes = new ArrayList(remoteNodes.values());
        }
        for (Iterator iter = nodes.iterator(); iter.hasNext();) {
            RemoteNode node = (RemoteNode) iter.next();
            node.leave();
            node.setMsgProducerOut(null);
        }
    }

    public void prepareTopology(NodeTopology aTopology) throws NodeException {
        Set oldNeighbours;
        if ( null == topology ) {
            oldNeighbours = Collections.EMPTY_SET;
        } else {
            oldNeighbours = topology.getNeighbours(nodeInfo);
        }
        // Computes the new neighbours
        Set newNeighbours = aTopology.getNeighbours(nodeInfo);
        newNeighbours.removeAll(oldNeighbours);

        // Makes sure that one does not drop them during the reconfiguration.
        remoteNodeMonitor.unscheduleNodeDeletion(newNeighbours);

        Exception exception = null;
        // Joins all the new neighbours
        for (Iterator iter = newNeighbours.iterator(); iter.hasNext();) {
            NodeInfo node = (NodeInfo) iter.next();
            try {
                findOrJoinRemoteNode(node);
            } catch (NodeException e) {
                exception = e;
                break;
            } catch (CommunicationException e) {
                exception = e;
                break;
            }
        }
        // One new neighbour has not been joined successfully. Rolls-back.
        if ( null != exception ) {
            for (Iterator iter = newNeighbours.iterator(); iter.hasNext();) {
                NodeInfo node = (NodeInfo) iter.next();
                leaveRemoteNode(node);
            }
            throw new NodeException("Can not apply topology.", exception);
        }
        preparedTopology = aTopology;
    }
    
    public void commitTopology() {
        Set oldNeighbours;
        if ( null == topology ) {
            oldNeighbours = Collections.EMPTY_SET;
        } else {
            oldNeighbours = topology.getNeighbours(nodeInfo);
        }
        // Computes the old neighbours
        Set newNeighbours = preparedTopology.getNeighbours(nodeInfo);
        oldNeighbours.removeAll(newNeighbours);

        // Schedules the deletion of the old neighbours.
        remoteNodeMonitor.scheduleNodeDeletion(oldNeighbours);
        
        topology = preparedTopology;
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
    
    public void leaveRemoteNode(NodeInfo aNodeInfo) {
        synchronized(remoteNodes) {
            RemoteNode remoteNode = (RemoteNode) remoteNodes.get(aNodeInfo);
            if ( null == remoteNode ) {
                return;
            }
            remoteNode.leave();
            unregisterRemoteNode(remoteNode);
        }
    }
    
    public RemoteNode findOrJoinRemoteNode(NodeInfo aNodeInfo)
        throws NodeException {
        RemoteNode remoteNode;
        synchronized(remoteNodes) {
            remoteNode = (RemoteNode) remoteNodes.get(aNodeInfo);
            if ( null != remoteNode ) {
                return remoteNode;
            }
            remoteNode =
                factory.factoryRemoteNode(nodeInfo, aNodeInfo, ioContext);
            remoteNode.setManager(this);
            remoteNode.join();
        }
        return remoteNode;
    }
    
    public RemoteNode findRemoteNode(NodeInfo aNodeInfo) {
        synchronized(remoteNodes) {
            return (RemoteNode) remoteNodes.get(aNodeInfo);
        }
    }

    public void registerRemoteNode(RemoteNode aRemoteNode) {
        log.info("Node {" + aRemoteNode.getNodeInfo() + "} has joined.");
        synchronized(remoteNodes) {
            remoteNodes.put(aRemoteNode.getNodeInfo(), aRemoteNode);
        }
        notifyListeners(
            new RemoteNodeEvent(aRemoteNode, RemoteNodeEvent.NODE_ADDED));
    }

    public void unregisterRemoteNode(RemoteNode aRemoteNode) {
        log.info("Node {" + aRemoteNode.getNodeInfo() + "} has left.");
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
                    if ( null == remoteNode ) {
                        throw new CommunicationException(target +
                            " has failed during a topology reconfiguration.");
                    }
                    out = remoteNode.getMsgConsumerOut();
                } else {
                    // A path has not already been computed. Computes one.
                    NodeInfo src = (NodeInfo)
                        header2.getHeader(MsgHeaderConstants.SRC_NODE);
                    NodeTopology topo = markTopology(header2);
                    path = topo.getPath(src, target);
                    if (null == path) {
                        throw new CommunicationException("{" + target
                            + "} is not reachable by {" + src + 
                            "} in the topology " + topo);
                    }
                    RemoteNode remoteNode = findRemoteNode(path[0]);
                    if ( null == remoteNode ) {
                        throw new CommunicationException(path[0] +
                            " has failed during a topology reconfiguration.");
                    }
                    out = remoteNode.getMsgConsumerOut();
                    
                    // Inserts the computed path and the new dests.
                    header2.addHeader(MsgHeaderConstants.DEST_NODE_PATH, NodeInfo.pop(path));
                    header2.addHeader(MsgHeaderConstants.DEST_NODES, target);
                }
                out.push(msg2);
            }
        }
        
        /**
         * If the topology version is not set, then the Msg is sent in the
         * current topology.
         * <BR>
         * If it is set, then one checks that the associated topology is
         * still defined. It must be either the currently installed or the
         * one being prepared.
         */
        private NodeTopology markTopology(MsgHeader aHeader) {
            NodeTopology topo = topology;
            Integer version = (Integer)
                aHeader.getOptionalHeader(MsgHeaderConstants.TOPOLOGY_VERSION);
            if ( null == version ) {
                aHeader.addHeader(MsgHeaderConstants.TOPOLOGY_VERSION,
                    new Integer(topo.getVersion()));
            } else if ( version.intValue() == preparedTopology.getVersion() ) {
                topo = preparedTopology;
            } else if ( version.intValue() != topo.getVersion() ) {
                throw new CommunicationException("Topology version " +
                    version + " too old.");
            }
            return topo;
        }
        
    }
    
}
