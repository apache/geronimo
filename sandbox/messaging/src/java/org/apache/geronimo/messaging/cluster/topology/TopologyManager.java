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

package org.apache.geronimo.messaging.cluster.topology;

import java.io.Serializable;
import java.util.Set;

import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.NodeTopology;

/**
 * Component in charge of defining the topology of a set of nodes.
 * <BR>
 * For instance, nodes could be organized in ring, mesh, hypercube,
 * torus et cetera.
 * <BR>
 * A TopologyManager is not required to be thread-safe.
 * <BR>
 * Implementation node: a TopologyManager is a Serializable as it is a GBean
 * persistent attribute.
 *
 * @version $Rev$ $Date$
 */
public interface TopologyManager extends Serializable
{

    /**
     * Gets the nodes registered by this manager. 
     * 
     * @return Set<NodeInfo>
     */
    public Set getNodes();
    
    /**
     * Adds a node.
     * 
     * @param aNode Node.
     */
    public void addNode(NodeInfo aNode);

    /**
     * Removes a node.
     * 
     * @param aNode Node.
     */
    public void removeNode(NodeInfo aNode);

    /**
     * Builds a node topology.
     * 
     * @return Node topology.
     */
    public NodeTopology factoryTopology();
    
}
