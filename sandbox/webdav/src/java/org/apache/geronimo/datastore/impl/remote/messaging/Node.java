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

package org.apache.geronimo.datastore.impl.remote.messaging;

import java.io.IOException;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/03/24 11:37:05 $
 */
public interface Node {
    
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
    public void setTopology(Topology aTopology);
    
    /**
     * Joins the node uniquely identified on the network by aNodeInfo.
     * 
     * @param aNodeInfo NodeInfo of a remote node to join.
     * @throws IOException Indicates that an I/O error has occured.
     * @throws CommunicationException Indicates that the node can not be
     * registered by the remote node identified by aNodeInfo.
     */
    public void join(NodeInfo aNodeInfo)
        throws IOException, CommunicationException;
    
    /**
     * Leaves the node uniquely identified on the network by aNodeInfo.
     * 
     * @param aNodeInfo NodeInfo of the remote node to leave.
     * @throws IOException Indicates that an I/O error has occured.
     * @throws CommunicationException Indicates that the node has not leaved
     * successfully the remote node.
     */
    public void leave(NodeInfo aNodeInfo)
        throws IOException, CommunicationException;
    
    /**
     * Gets the StreamManager of this node.
     * 
     * @return StreamManager used by this server to resolve/encode InputStreams.
     */
    public StreamManager getStreamManager();
    
    /**
     * Registers a new Connector.
     * 
     * @param aConnector Connector to be registered.
     */
    public void addConnector(Connector aConnector);
    
    /**
     * Unregisters the Connector.
     * 
     * @param aConnector Connector to be deregistered.
     */
    public void removeConnector(Connector aConnector);
    
}