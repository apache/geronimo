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

import java.io.Serializable;
import java.util.Set;

/**
 * Abstracts the topology of a set of nodes.
 * <BR>
 * This is a Serializable as it is exchanged between nodes.
 *
 * @version $Rev$ $Date$
 */
public interface NodeTopology extends Serializable
{

    /**
     * Gets the version of this topology.
     * <BR>
     * 0 is a reserved value and must not be used.
     * 
     * @return version number.
     */
    public int getVersion();
    
    /**
     * Gets the neighbours of the specified node. They are the nodes directly
     * reachable from aRoot. 
     * 
     * @param aRoot Node.
     * @return Set<NodeInfo> Neighbours. An empty Set must be returned if
     * the specified node is not registered by this topology.
     */
    public Set getNeighbours(NodeInfo aRoot);
    
    /**
     * Gets a path between aSource and aTarget. 
     * 
     * @param aSource Source node.
     * @param aTarget Target node.
     * @return Nodes to be traversed to reach aTarget from aSource. null is
     * returned if these two nodes are not connected.
     */
    public NodeInfo[] getPath(NodeInfo aSource, NodeInfo aTarget);

    /**
     * Gets the identifier of the provided node in the specified topology.
     * 
     * @param aNodeInfo Node whose identifier is to be returned.
     * @return Node identifier.
     */
    public int getIDOfNode(NodeInfo aNodeInfo);

    /**
     * Gets the NodeInfo having the specified identifier.
     * 
     * @param anId Node identifier.
     * @return NodeInfo having this identifier.
     */
    public NodeInfo getNodeById(int anId);
    
    /**
     * Gets the NodeInfo registered by this topology.
     * 
     * @return Set<NodeInfo> of NodeInfo.
     */
    public Set getNodes();

}