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

import java.util.Set;

/**
 * When a Node is created, it registers itself as an EndPoint defining the
 * following contracts.
 * <BR>
 * Implementation note: this class should have been a nested class of NodeImpl.
 * Though, I do not know how to achieve that: EndPointProxyFactory can not
 * create a proxy for static nested interfaces. I think that this is a CGLIB
 * limitation.
 *
 * @version $Revision :$ $Date: 2004/06/10 23:12:24 $
 */
public interface NodeEndPointView extends EndPoint
{

    /**
     * EndPoint ID. 
     */
    public static final Object NODE_ID = "Node";

    /**
     * Cascades the specified topology to all the neighbours of the underlying
     * Node. The topology change should not be cascaded to the Nodes contained
     * by aSetOfProcessed. 
     * 
     * @param aTopology Topology to be cascaded to all the neighbours of the
     * underlying Node and then applied on the underlying node. 
     * @param aSetOfProcessed Set<NodeInfo> Nodes which have already received
     * the topology change request.
     */
    public void cascadeTopology(NodeTopology aTopology, Set aSetOfProcessed);
    
}