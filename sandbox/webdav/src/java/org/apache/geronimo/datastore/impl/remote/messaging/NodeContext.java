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
 * Context provided by a Node to its Connectors when they are registered.
 *
 * @version $Revision: 1.1 $ $Date: 2004/03/24 11:37:05 $
 */
public class NodeContext {

    private final MsgOutInterceptor out;
    private final RequestSender sender;
    
    public NodeContext(MsgOutInterceptor anOut, RequestSender aSender) {
        out = anOut;
        sender = aSender;
    }
    
    /**
     * Gets the Msg outbound interceptor to be used to contact remote
     * Connectors.
     */
    public MsgOutInterceptor getOutput() {
        return out;
    }

    /**
     * Gets the RequestSender to be used to send requests to remote Connectors.
     */
    public RequestSender getRequestSender() {
        return sender;
    }
    
}
