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

/**
 * Msg header keys.
 * <BR>
 * Only these keys are supported by MsgHeaders.
 *
 * @version $Revision: 1.2 $ $Date: 2004/07/20 00:08:13 $
 */
public interface MsgHeaderConstants
{

    /**
     * Source node.
     * <BR>
     * The value for this key MUST be a NodeInfo instance.
     */
    public static final Object SRC_NODE = "SrcNode";
    
    /**
     * Destination node.
     * <BR>
     * The value for this key MUST be a NodeInfo instance.
     */
    public static final Object DEST_NODE = "DestNode";
    
    /**
     * Destination nodes.
     * <BR>
     * The value for this key MUST be a NodeInfo or a NodeInfo[] instance.
     */
    public static final Object DEST_NODES = "DestNodes";
    
    /**
     * Path to be traversed to reach DEST_NODES.
     * <BR>
     * The value for this key MUST be a NodeInfo[] instance.
     */
    public static final Object DEST_NODE_PATH = "DestNodePath";
    
    /**
     * Source EndPoint.
     * <BR>
     * The value for this key MUST be an EndPoint identifier..
     */
    public static final Object SRC_ENDPOINT = "SrcEndPoint";
    
    /**
     * Destination EndPoint.
     * <BR>
     * The value for this key MUST be an EndPoint identifier.
     */
    public static final Object DEST_ENDPOINT = "DestEndPoint";

    /**
     * Msg identifier.
     */
    public static final Object CORRELATION_ID = "CorrelationID";

    /**
     * Body type.
     * <BR>
     * The value for this key MUST be a MsgBody.Type.
     */
    public static final Object BODY_TYPE = "BodyType";
    
    /**
     * Topology version in which the Msg is to be sent/received.
     * <BR>
     * It is used to send Msgs in a prepared (not yet committed) topology.
     */
    public static final Object TOPOLOGY_VERSION = "TopologyVersion";
    
}
