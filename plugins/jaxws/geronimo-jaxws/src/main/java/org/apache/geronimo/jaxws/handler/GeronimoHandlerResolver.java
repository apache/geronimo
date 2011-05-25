/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.jaxws.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;

import org.apache.geronimo.jaxws.annotations.AnnotationException;
import org.apache.geronimo.jaxws.annotations.AnnotationProcessor;
import org.apache.geronimo.jaxws.info.HandlerChainInfo;
import org.apache.geronimo.jaxws.info.HandlerChainsInfo;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoHandlerResolver implements HandlerResolver {

    private HandlerChainsInfo handlerChainsInfo;

    private Bundle bundle;

    private Class serviceClass;

    private AnnotationProcessor annotationProcessor;

    public GeronimoHandlerResolver(Bundle bundle,
                                   Class serviceClass,
                                   HandlerChainsInfo handlerChainsInfo,
                                   AnnotationProcessor annotationProcessor) {
        this.bundle = bundle;
        this.serviceClass = serviceClass;
        this.handlerChainsInfo = handlerChainsInfo;
        this.annotationProcessor = annotationProcessor;
    }

    public List<Handler> getHandlerChain(javax.xml.ws.handler.PortInfo portInfo) {

        GeronimoHandlerChainBuilder builder =
                new GeronimoHandlerChainBuilder(bundle, portInfo);

        if (this.handlerChainsInfo == null) {
            /*handlers = builder.buildHandlerChainFromClass(this.serviceClass);*/
            /* Since we run here, the HandlerChain from the class annotation should have been considered in the WebServiceRefBuilder (client side)
             * or WebServiceFinder (server side)
             * */
            AnnotationHandlerChainFinder annotationHandlerChainFinder = new AnnotationHandlerChainFinder();
            handlerChainsInfo = annotationHandlerChainFinder.buildHandlerChainFromClass(serviceClass);
        }

        if (handlerChainsInfo == null || handlerChainsInfo.handleChains.size() == 0) {
            return Collections.<Handler> emptyList();
        }

        List<Handler> handlers = new ArrayList<Handler>();
        for (HandlerChainInfo handlerChain : handlerChainsInfo.handleChains) {
            handlers.addAll(builder.buildHandlerChainFromConfiguration(handlerChain));
        }
        handlers = builder.sortHandlers(handlers);
        if (this.annotationProcessor != null) {
            try {
                for (Handler handler : handlers) {
                    this.annotationProcessor.processAnnotations(handler);
                    this.annotationProcessor.invokePostConstruct(handler);
                }
            } catch (AnnotationException e) {
                throw new WebServiceException("Handler annotation failed", e);
            }
        }

        return handlers;
    }

}
