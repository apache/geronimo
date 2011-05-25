/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.jaxws.handler;

import org.apache.geronimo.jaxws.info.HandlerChainInfo;
import org.apache.geronimo.jaxws.info.HandlerChainsInfo;
import org.apache.geronimo.jaxws.info.HandlerInfo;
import org.apache.openejb.jee.Handler;
import org.apache.openejb.jee.HandlerChain;
import org.apache.openejb.jee.HandlerChains;

/**
 * @version $Rev$ $Date$
 */
public class HandlerChainsInfoBuilder {

    public HandlerChainsInfo build(HandlerChains handlerChains) {
        HandlerChainsInfo handlerChainsInfo = new HandlerChainsInfo();
        for (HandlerChain handlerChain : handlerChains.getHandlerChain()) {
            HandlerChainInfo handlerChainInfo = new HandlerChainInfo();
            if (handlerChain.getServiceNamePattern() != null) {
                handlerChainInfo.serviceNamePattern = handlerChain.getServiceNamePattern();
            } else if (handlerChain.getPortNamePattern() != null) {
                handlerChainInfo.portNamePattern = handlerChain.getPortNamePattern();
            } else if (handlerChain.getProtocolBindings() != null) {
                handlerChainInfo.protocolBindings.addAll(handlerChain.getProtocolBindings());
            }
            for (Handler handler : handlerChain.getHandler()) {
                HandlerInfo handlerInfo = new HandlerInfo();
                handlerInfo.handlerName = handler.getHandlerName();
                handlerInfo.handlerClass = handler.getHandlerClass();
                if (handler.getSoapRole() != null) {
                    handlerInfo.soapRoles.addAll(handler.getSoapRole());
                }
                handlerChainInfo.handlers.add(handlerInfo);
            }
            handlerChainsInfo.handleChains.add(handlerChainInfo);
        }
        return handlerChainsInfo;
    }
}
