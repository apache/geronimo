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

/**
 * Connection to a remote node.
 * 
 * @version $Revision: 1.2 $ $Date: 2004/07/20 00:15:06 $
 */
public interface RemoteNodeConnection
    extends MsgConsProd
{
    
    /**
     * Opens the connection.
     * 
     * @throws NodeException Indicates that the connection can not be opened.
     */
    public void open() throws NodeException;
    
    /**
     * Closes the connection.
     */
    public void close();
    
    /**
     * Sets the listener to be notified when the connection is closed.
     * 
     * @param aListener Listener.
     */
    public void setLifecycleListener(LifecycleListener aListener);
    
    /**
     * Callback interface to be notified when the connection is closed.
     */
    public interface LifecycleListener {

        public void onClose();
        
    }
    
}