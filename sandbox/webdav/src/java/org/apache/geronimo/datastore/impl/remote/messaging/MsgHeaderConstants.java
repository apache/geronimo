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
 * Msh header keys.
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/25 13:36:15 $
 */
public interface MsgHeaderConstants
{

    /**
     * Source node.
     */
    public static final String SRC_NODE = "SrcNodeName";

    /**
     * Destination nodes.
     */
    public static final String DEST_NODE = "DestNodeName";

    /**
     * Source Connector.
     */
    public static final String SRC_CONNECTOR = "ConnectorName";
    
    /**
     * Destination Connectors.
     */
    public static final String DEST_CONNECTOR = "DestConnectorName";

    /**
     * Msg identifier.
     */
    public static final String CORRELATION_ID = "CorrelationID";

    /**
     * Body type.
     */
    public static final String BODY_TYPE = "BodyType";
    
}
