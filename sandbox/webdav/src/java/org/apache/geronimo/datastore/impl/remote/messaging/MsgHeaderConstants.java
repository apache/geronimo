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
 * Msg header keys.
 *
 * @version $Revision: 1.2 $ $Date: 2004/03/11 15:36:14 $
 */
public interface MsgHeaderConstants
{

    /**
     * Source node.
     */
    public static final Object SRC_NODE = "SrcNode";
    
    /**
     * Destination node.
     */
    public static final Object DEST_NODE = "DestNode";
    
    /**
     * Destination nodes.
     */
    public static final Object DEST_NODES = "DestNodes";
    
    /**
     * Path - list of nodes - to be traversed to reach DEST_NODE.
     */
    public static final Object DEST_NODE_PATH = "DestNodePath";
    
    /**
     * Source Connector.
     */
    public static final Object SRC_CONNECTOR = "ConnectorName";
    
    /**
     * Destination Connector.
     */
    public static final Object DEST_CONNECTOR = "DestConnectorName";

    /**
     * Msg identifier.
     */
    public static final Object CORRELATION_ID = "CorrelationID";

    /**
     * Body type.
     */
    public static final Object BODY_TYPE = "BodyType";
    
}
