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

import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.messaging.io.ReplacerResolver;
import org.apache.geronimo.messaging.proxy.EndPointProxyInfo;

/**
 * Abstract a node in a clustered deployment.
 * <BR>
 * A Node knows how to join, leave other nodes. It also registers EndPoints
 * and provide them a mean to exchange Msgs with other EndPoints.
 * <BR>
 * The following diagram shows how Nodes and EndPoints are combined together:
 * <PRE>
 * EndPoint -- MTO -- Node -- MTM -- Node -- OTM -- EndPoint
 * </PRE>
 *
 * @version $Revision: 1.5 $ $Date: 2004/07/17 03:52:34 $
 */
public interface Node extends GBeanLifecycle
{
    
    /**
     * Gets the NodeInfo of this node.
     * 
     * @return NodeInfo.
     */
    public NodeInfo getNodeInfo();
    
    /**
     * Sets the node topology in which this instance is operating.
     * <BR>
     * When the topology is set, this node tries to "apply" it: it creates
     * physical connections with all of its neighbours as defined by the
     * specified topology and drops the physical connections no more
     * required by the topology change. It should also cascade the topology
     * change to all of its neighbours. These latter should then applied it
     * locally.
     * 
     * @param aTopology Topology of the nodes constituting the network layout.
     * 
     * @exception NodeException Indicates that the topology can not be set.
     */
    public void setTopology(NodeTopology aTopology) throws NodeException;

    /**
     * Gets the node topology in which this instance is operating.
     * 
     * @return Node topology.
     */
    public NodeTopology getTopology();
    
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

    /**
     * Creates a proxy for the EndPoint defined by anInfo.
     * 
     * @param anInfo EndPoint meta-data.
     * @return A proxy for the EndPoint defined by anInfo. This proxy implements
     * all the EndPoint interfaces plus the EndPointProxy interface.
     */
    public Object factoryEndPointProxy(EndPointProxyInfo anInfo);
    
    /**
     * Releases the resources of the specified EndPoint proxy.
     * <BR>
     * From this point, the proxy can no more be used.
     * <BR>An IllegalStateException should be thrown when a method is invoked
     * on a released proxy.
     * 
     * @param aProxy EndPoint proxy.
     * @exception IllegalArgumentException Indicates that the provided instance
     * is not a proxy.
     */
    public void releaseEndPointProxy(Object aProxy);
    
}
