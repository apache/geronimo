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
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.messaging.cluster;

import java.util.Set;

import org.apache.geronimo.messaging.EndPoint;
import org.apache.geronimo.messaging.NodeInfo;

/**
 * Cluster of Nodes.
 *
 * @version $Revision: 1.1 $ $Date: 2004/06/10 23:12:25 $
 */
public interface Cluster
    extends EndPoint
{

    /**
     * Gets the cluster identifier.
     * 
     * @return Identifier.
     */
    public Object getClusterID();

    /**
     * Gets the Nodes of this cluster.
     * 
     * @return Set<NodeInfo> nodes.
     */
    public Set getMembers();

    /**
     * Adds a Node to this cluster.
     * <BR>
     * When a Node is added to a cluster, this latter performs a dynamic 
     * reconfiguration of the node topology.
     * <BR>
     * When the topology has been applied, it notifies the registered
     * cluster event listeners of the addition. 
     * 
     * @param aNode Node to be added to this cluster. 
     */
    public void addMember(NodeInfo aNode);

    /**
     * Removes a Node from this cluster.
     * <BR>
     * See {@link addMember} for more details on the operations of this method. 
     * 
     * @param aNode
     */
    public void removeMember(NodeInfo aNode);

    /**
     * Adds a listener to this cluster.
     * 
     * @param aListener Listener to be registered.
     */
    public void addListener(ClusterEventListener aListener);
    
    /**
     * Removes the specified listener.
     * 
     * @param aListener Listener to be removed.
     */
    public void removeListener(ClusterEventListener aListener);
    
}
