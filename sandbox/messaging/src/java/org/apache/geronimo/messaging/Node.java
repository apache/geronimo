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

import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.messaging.io.ReplacerResolver;

/**
 * Abstract a node in a clustered deployment.
 * <BR>
 * A Node knows how to join, leave other nodes. It also registers EndPoints
 * and provide them a mean to exchange Msgs with other EndPoints.
 * <BR>
 * The following diagram shows how Node and EndPoints are combined together:
 * <PRE>
 * EndPoint -- MTO -- Node -- MTM -- Node -- OTM -- EndPoint
 * </PRE>
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:41 $
 */
public interface Node extends GBean
{
    
    /**
     * Gets the NodeInfo of this node.
     * 
     * @return NodeInfo.
     */
    public NodeInfo getNodeInfo();
    
    /**
     * Sets the node topology in which this instance is operating. 
     * 
     * @param aTopology Topology of the nodes constituting the network layout.
     */
    public void setTopology(NodeTopology aTopology);
    
    /**
     * Joins the specified node.
     * 
     * @param aNodeInfo NodeInfo of the node to join.
     * @exception NodeException Indicates that the remote node can not be
     * joined.
     */
    public void join(NodeInfo aNodeInfo) throws NodeException;
    
    /**
     * Leaves the specified node.
     * 
     * @param aNodeInfo NodeInfo of the node to leave.
     * @exception NodeException Indicates that the remote node has not been
     * leaved successfully.
     */
    public void leave(NodeInfo aNodeInfo) throws NodeException;
    
    /**
     * Gets the root ReplacerResolver used by this node in order to replace and
     * resolve instances to be sent and received by remote nodes.
     * 
     * @return Root ReplacerResolver.
     */
    public ReplacerResolver getReplacerResolver();

    /**
     * Registers a new EndPoint.
     * 
     * @param anEndPoint EndPoint to be registered.
     */
    public void addEndPoint(EndPoint anEndPoint);
    
    /**
     * Unregisters the EndPoint.
     * 
     * @param anEndPoint EndPoint to be deregistered.
     */
    public void removeEndPoint(EndPoint anEndPoint);
    
}
