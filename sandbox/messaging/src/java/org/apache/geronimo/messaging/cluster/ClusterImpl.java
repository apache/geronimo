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

package org.apache.geronimo.messaging.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.messaging.Node;
import org.apache.geronimo.messaging.NodeException;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.NodeTopology;
import org.apache.geronimo.messaging.cluster.topology.TopologyManager;

/**
 * Cluster implementation.
 * <BR>
 * It manages the nodes at the cluster level and triggers dynamic 
 * reconfigurations of the underlying node topology when members are added or 
 * removed.
 *  
 * @version $Rev$ $Date$
 */
public class ClusterImpl
    implements Cluster, GBeanLifecycle
{
    
    /**
     * Cluster meta-data.
     */
    private final ClusterInfo clusterInfo;

    /**
     * Node which is owning this cluster view.
     */
    private final Node node;
    
    /**
     * To reconfigure the node topology.
     */
    private final TopologyManager topologyManager;
    
    /**
     * Collection<ClusterEventListener> Cluster event listeners.
     */
    private final Collection listeners;

    /**
     * Creates a cluster view mounted by the specified node.
     * 
     * @param aClusterInfo Cluster meta-data.
     * @param aNode Node which is mounting this cluster view.
     * @param aTopologyManager Use to reconfigure the node topology when
     * members joined or leaved the cluster. 
     */
    public ClusterImpl(ClusterInfo aClusterInfo, Node aNode,
        TopologyManager aTopologyManager) {
        if ( null == aClusterInfo ) {
            throw new IllegalArgumentException("ClusterInfo is required");
        } else if ( null == aNode ) {
            throw new IllegalArgumentException("Node is required");
        } else if ( null == aTopologyManager ) {
            throw new IllegalArgumentException("Topology manager is required");
        }
        clusterInfo = aClusterInfo;
        node = aNode;
        topologyManager = aTopologyManager;

        listeners = new ArrayList();
    }

    public void doStart() throws WaitingException, Exception {
        topologyManager.addNode(node.getNodeInfo());
    }

    public void doStop() throws WaitingException, Exception {
        topologyManager.removeNode(node.getNodeInfo());
    }

    public void doFail() {
        topologyManager.removeNode(node.getNodeInfo());
    }

    public ClusterInfo getClusterInfo() {
        return clusterInfo;
    }
    
    public Set getMembers() {
        return node.getTopology().getNodes();
    }

    public void addMember(NodeInfo aNode) throws NodeException {
        NodeTopology nodeTopology;
        synchronized(topologyManager) {
            Set nodes = topologyManager.getNodes();
            if ( nodes.contains(aNode) ) {
                return;
            }
            topologyManager.addNode(aNode);
            nodeTopology = topologyManager.factoryTopology();
            try {
                node.setTopology(nodeTopology);
            } catch (NodeException e) {
                // If the topology can not be applied, then one removes it
                // from the topologyManager.
                topologyManager.removeNode(aNode);
                throw e;
            }
        }
        fireClusterMemberEvent(
            new ClusterEvent(this, aNode, ClusterEvent.MEMBER_ADDED));
    }

    public void removeMember(NodeInfo aNode) throws NodeException {
        NodeTopology nodeTopology;
        synchronized(topologyManager) {
            Set nodes = topologyManager.getNodes();
            if ( !nodes.contains(aNode) ) {
                return;
            }
            topologyManager.removeNode(aNode);
            nodeTopology = topologyManager.factoryTopology();
            try {
                node.setTopology(nodeTopology);
            } catch (NodeException e) {
                throw e;
            }
        }
        fireClusterMemberEvent(
            new ClusterEvent(this, aNode, ClusterEvent.MEMBER_REMOVED));
    }

    public void addListener(ClusterEventListener aListener) {
        synchronized(listeners) {
            listeners.add(aListener);
        }
    }

    public void removeListener(ClusterEventListener aListener) {
        synchronized(listeners) {
            listeners.remove(aListener);
        }
    }

    private void fireClusterMemberEvent(ClusterEvent anEvent) {
        ClusterEventListener[] tmpListeners;
        synchronized(listeners) {
            tmpListeners = (ClusterEventListener[])
                listeners.toArray(new ClusterEventListener[0]);
        }
        for (int i = 0; i < tmpListeners.length;) {
            tmpListeners[i].fireClusterMemberEvent(anEvent);
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory factory = new GBeanInfoFactory(ClusterImpl.class);
        factory.setConstructor(new String[] {"clusterInfo", "Node", 
            "topologyManager"});
        factory.addInterface(Cluster.class, new String[] {"clusterInfo"});
        factory.addAttribute("topologyManager", TopologyManager.class, true);
        factory.addReference("Node", Node.class);
        GBEAN_INFO = factory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
    
}
