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

import org.apache.geronimo.messaging.interceptors.HeaderOutInterceptor;
import org.apache.geronimo.messaging.interceptors.MsgOutInterceptor;

/**
 *
 * @version $Revision: 1.2 $ $Date: 2004/07/20 00:26:03 $
 */
public class EndPointUtil
{

    public static void interConnect(EndPoint anEP1, EndPoint anEP2) {
        MsgOutInterceptor out =
            new HeaderOutInterceptor(
                MsgHeaderConstants.SRC_NODE, "",
                new HeaderOutInterceptor(
                    MsgHeaderConstants.SRC_ENDPOINT, "",
                    new HeaderOutInterceptor(
                        MsgHeaderConstants.TOPOLOGY_VERSION,
                        new Integer(1),
                        anEP1.getMsgConsumerOut())));
        anEP2.setMsgProducerOut(out);
        out =
            new HeaderOutInterceptor(
                MsgHeaderConstants.SRC_NODE, "",
                new HeaderOutInterceptor(
                    MsgHeaderConstants.SRC_ENDPOINT, "",
                    new HeaderOutInterceptor(
                        MsgHeaderConstants.TOPOLOGY_VERSION,
                        new Integer(1),
                        anEP2.getMsgConsumerOut())));
        anEP1.setMsgProducerOut(out);
    }
    
}
