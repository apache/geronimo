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

import org.apache.geronimo.messaging.GBeanBaseEndPoint;
import org.apache.geronimo.messaging.Node;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.NodeTopology;
import org.apache.geronimo.messaging.cluster.topology.TopologyManager;

/**
 * Cluster implementation.
 * <BR>
 * It is an EndPoint, which manages the nodes at the cluster level and triggers
 * dynamic reconfigurations of the underlying node topology when members
 * are added or removed.
 *  
 * @version $Revision: 1.1 $ $Date: 2004/06/10 23:12:25 $
 */
public class ClusterImpl
    extends GBeanBaseEndPoint
    implements Cluster
{

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
     * @param aNode Node which is mounting this cluster view.
     * @param anID Cluster identifier.
     * @param aTopologyManager Use to reconfigure the node topology when
     * members joined or leaved the cluster. 
     */
    public ClusterImpl(Node aNode, Object anID,
        TopologyManager aTopologyManager) {
        super(aNode, anID);
        if ( null == aTopologyManager ) {
            throw new IllegalArgumentException("Topology manager is required");
        }
        
        topologyManager = aTopologyManager;
        topologyManager.addNode(node.getNodeInfo());

        listeners = new ArrayList();
    }
    
    public Object getClusterID() {
        return getID();
    }

    public Set getMembers() {
        return node.getTopology().getNodes();
    }

    public void addMember(NodeInfo aNode) {
        topologyManager.addNode(aNode);
        NodeTopology nodeTopology = topologyManager.factoryTopology();
        node.setTopology(nodeTopology);
        fireClusterMemberEvent(
            new ClusterEvent(this, aNode, ClusterEvent.MEMBER_ADDED));
    }

    public void removeMember(NodeInfo aNode) {
        topologyManager.removeNode(aNode);
        NodeTopology nodeTopology = topologyManager.factoryTopology();
        node.setTopology(nodeTopology);
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
    
}
