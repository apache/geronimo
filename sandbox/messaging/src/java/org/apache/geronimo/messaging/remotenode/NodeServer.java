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

/**
 * A NodeServer listens for remote nodes and delegates to a
 * RemoteNodeManager their management. 
 * 
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:42 $
 */
public interface NodeServer
{

    /**
     * Start the server.
     * 
     * @throws IOException Indicates an I/O problem.
     * @throws CommunicationException If a communication can not be established.
     */
    public void start() throws IOException, CommunicationException;

    /**
     * Stop the server.
     * 
     * @throws IOException Indicates an I/O problem.
     * @throws CommunicationException If a communication can not be established.
     */
    public void stop() throws IOException, CommunicationException;

    /**
     * Sets the RemoteNodeManager in charge of managing the remote node, which
     * have join this server.
     * <BR>
     * A NodeServer must notify this RemoteNodeManager when a new connection
     * abstracting a remote note has joined it.
     * 
     * @param aManager Manager of RemoteNode.
     */
    public void setRemoteNodeManager(RemoteNodeManager aManager);
    
}