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

import java.io.IOException;

import org.apache.geronimo.messaging.CommunicationException;
import org.apache.geronimo.messaging.MsgConsProd;
import org.apache.geronimo.messaging.NodeInfo;

/**
 * Provides a local representation of a remote Node.
 * 
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:42 $
 */
public interface RemoteNode
    extends MsgConsProd
{

    /**
	 * Gets the NodeInfo of this remote node.
	 * 
	 * @return NodeInfo.
	 */
    public NodeInfo getNodeInfo();

    /**
     * Connects to the remote node.
     * 
     * @throws IOException Indicates an I/O problem.
     * @throws CommunicationException If a communication can not be established.
     */
    public void connect() throws IOException, CommunicationException;

    /**
     * Leaves the remote node.
     * 
     * @throws IOException Indicates an I/O problem.
     * @throws CommunicationException If a communication can not be established.
     */
    public void leave() throws IOException, CommunicationException;

    /**
     * Adds a connection.
     * 
     * @param aConnection Connection to be added to the RemoteNode.
     */
    public void addConnection(RemoteNodeConnection aConnection);
    
    /**
     * Removes a connection.
     * 
     * @param aConnection Connection to be removed.
     */
    public void removeConnection(RemoteNodeConnection aConnection);
    
}