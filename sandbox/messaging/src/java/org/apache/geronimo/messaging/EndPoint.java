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


/**
 * EndPoints are registered by Nodes and abstract end-points of inter-node
 * communication channels.
 * <BR>
 * When an EndPoint is mounted by a Node, this latter provides it a mean
 * to push Msgs to other Nodes via the MsgProducer interface.
 * <BR>
 * Conversely, an EndPoint provides to this Node a mean to push Msgs to it via
 * the MsgConsumer interface.
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:41 $
 */
public interface EndPoint
    extends MsgConsProd, GBean
{
 
    /**
     * Gets the identifier of this EndPoint. It is used by a Node to route
     * incoming Msgs to this instance. 
     * 
     * @return EndPoint identifier.
     */
    public Object getID();
    
}
