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

import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.messaging.io.IOContext;


/**
 * Transport layer factory.
 * 
 * @version $Revision: 1.3 $ $Date: 2004/07/20 00:15:06 $
 */
public interface MessagingTransportFactory
{

    /**
     * Creates a NodeServer for the specified node.
     * 
     * @param aNodeInfo Node meta-data. 
     * @param anIOContext Used to retrieve the IOContext of the node.
     * @return NodeServer.
     */
    public NodeServer factoryServer(
        NodeInfo aNodeInfo, IOContext anIOContext);
    
    /**
     * Creates a RemoteNode providing a local view of the remote node aNodeInfo.
     * 
     * @param aLocalNodeInfo Local node meta-data.
     * @param aRemoteNodeInfo Remote node meta-data.
     * @param anIOContext Used to retrieve the IOContext to be used to
     * communicate with the remote node.
     * @return RemoteNode.
     */
    public RemoteNode factoryRemoteNode(NodeInfo aLocalNodeInfo, 
        NodeInfo aRemoteNodeInfo, IOContext anIOContext);

}
