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


import org.apache.geronimo.messaging.MsgConsProd;
import org.apache.geronimo.messaging.NodeException;
import org.apache.geronimo.messaging.NodeInfo;

/**
 * Provides a local representation of a remote Node.
 * 
 * @version $Revision: 1.3 $ $Date: 2004/07/20 00:15:06 $
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
     * Sets the manager of this remote node.
     * 
     * @param aManager Manager.
     */
    public void setManager(RemoteNodeManager aManager);
    
    /**
     * Leaves the remote node.
     */
    public void leave();

    /**
     * Joins the remote node.
     * 
     * @exception NodeException Indicates that the remote node can not be 
     * joined.
     */
    public void join() throws NodeException;
    
}