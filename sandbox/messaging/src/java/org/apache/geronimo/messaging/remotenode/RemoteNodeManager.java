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

import java.util.Collection;

import org.apache.geronimo.messaging.MsgConsumer;
import org.apache.geronimo.messaging.NodeException;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.NodeTopology;

/**
 * RemoteNode manager.
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/10 23:12:25 $
 */
public interface RemoteNodeManager
    extends MsgConsumer
{

    /**
     * Starts the manager.
     * 
     * @throws NodeException
     */
    public void start() throws NodeException;
    
    /**
     * Stops the manager.
     * 
     * @throws NodeException
     */
    public void stop() throws NodeException;
    
    /**
     * Sets the Topology to be used to derive the path between two nodes.
     * <BR>
     * When the topology is set, the manager tries to "apply" it: it creates
     * physical connections with all of its neighbours as defined by the
     * specified topology and drops the physical connections no more
     * required by the topology change.
     * 
     * @param aTopology Topology.
     */
    public void setTopology(NodeTopology aTopology);
    
    /**
     * Adds a listener for RemoteNode event.
     * 
     * @param aListener Listener.
     */
    public void addListener(RemoteNodeEventListener aListener);

    /**
     * Removes a listener for RemoteNode event.
     * 
     * @param aListener Listener.
     */
    public void removeListener(RemoteNodeEventListener aListener);

    /**
     * Leaves a remote node. 
     * 
     * @param aNodeInfo Meta-data of the node to be left.
     * @throws NodeException
     */
    public void leaveRemoteNode(NodeInfo aNodeInfo) throws NodeException;
    
    /**
     * Finds or joins a remote node.
     * <BR>
     * If the remote node aNodeInfo is not already registered by this manager,
     * then it is joined by this manager.
     * 
     * @param aNodeInfo Remote node.
     * @return RemoteNode.
     * @throws NodeException
     */
    public RemoteNode findOrJoinRemoteNode(NodeInfo aNodeInfo)
        throws NodeException;
    
    /**
     * Finds a remote node.
     * <BR>
     * If the remote node aNodeInfo is not registered, then no attempt is done
     * to join it.
     * 
     * @param aNodeInfo Remote node.
     * @return
     */
    public RemoteNode findRemoteNode(NodeInfo aNodeInfo);
    
    /**
     * Registers a remote node.
     * 
     * @param aRemoteNode Remote node to be registered.
     */
    public void registerRemoteNode(RemoteNode aRemoteNode);
    
    /**
     * Unregistered a remote node.
     * 
     * @param aRemoteNode Remote node to be unregistered.
     */
    public void unregisterRemoteNode(RemoteNode aRemoteNode);

    /**
     * Gets the registered remote nodes.
     * 
     * @return Collection of RemoteNodes.
     */
    public Collection listRemoteNodes();
    
}