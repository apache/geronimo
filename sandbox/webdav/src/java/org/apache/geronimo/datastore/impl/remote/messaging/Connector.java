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


/**
 * This is the interface used between a ServerNode or ServantNode and a 
 * component, whishing to enter into a bidirectional communication with remote
 * components.
 * <BR>
 * See ServerNode for more details.
 *
 * @version $Revision: 1.2 $ $Date: 2004/03/11 15:36:14 $
 */
public interface Connector
{
 
    /**
     * Gets the name of this Connector. This name is used by a ServerNode
     * to expose this Connector to other components. 
     * 
     * @return Name of this Connector.
     */
    public String getName();

    /**
     * Sets the ServerNode context of this Connector.
     */
    public void setContext(ServerNodeContext aContext);

    /**
     * When a ServerNode receives a Msg to be delivered to a Connector, it
     * invokes this method on it.
     * <BR>
     * This method MUST be thread-safe as a Thread could be dispatched to
     * perform the invocation.
     * 
     * @param anIn Msg to be processed by the Connector.
     */
    public void deliver(Msg aMsg);
    
}
