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

/**
 * When a Node is created, it registers itself as an EndPoint defining the
 * following contracts.
 * <BR>
 * Implementation note: this class should have been a nested class of NodeImpl.
 * Though, I do not know how to achieve that: EndPointProxyFactory can not
 * create a proxy for static nested interfaces. I think that this is a CGLIB
 * limitation.
 *
 * @version $Revision :$ $Date$
 */
public interface NodeEndPointView extends EndPoint
{

    /**
     * EndPoint ID. 
     */
    public static final Object NODE_ID = "Node";

    /**
     * Prepares the specified topology.
     * <BR>
     * The node must validate (join) all of its neighbours defined by the
     * specified topology.
     * 
     * @param aTopology Topology to be prepared.
     * @exception NodeException If the node can not join all of its neighbours.
     */
    public void prepareTopology(NodeTopology aTopology) throws NodeException;
    
    /**
     * Commits the topology previously prepared.
     * 
     * @param aTopology Topology to be committed.
     */
    public void commitTopology();
    
}