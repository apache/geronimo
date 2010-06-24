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

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;

import org.apache.openejb.jee.HandlerChains;
import org.apache.openejb.jee.JaxbJavaee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class AnnotationHandlerChainBuilder
{
    private static final Logger log = LoggerFactory.getLogger(AnnotationHandlerChainBuilder.class);

    public AnnotationHandlerChainBuilder() {
    }

    /**
     * @param clz
     * @param existingHandlers
     * @return
     */
    public List<Handler> buildHandlerChainFromClass(Class<?> clz, List<Handler> existingHandlers) {
        log.debug("building handler chain");
        HandlerChainAnnotation hcAnn = findHandlerChainAnnotation(clz, true);
        List<Handler> chain = null;
        if (hcAnn == null) {
            log.debug("no HandlerChain annotation on " + clz);
            chain = new ArrayList<Handler>();
        } else {
            hcAnn.validate();

            try {
                URL handlerFileURL = clz.getResource(hcAnn.getFileName());
                InputStream in = handlerFileURL.openStream();
                HandlerChains handlerChainsType;
                try {
                    handlerChainsType = (HandlerChains) JaxbJavaee.unmarshal(HandlerChains.class, in);
                } finally {
                    in.close();
                }

                if (null == handlerChainsType || handlerChainsType.getHandlerChain().isEmpty()) {
                    throw new WebServiceException("Chain not specified");
                }

                chain = new ArrayList<Handler>();
                for (org.apache.openejb.jee.HandlerChain hc : handlerChainsType.getHandlerChain()) {
                    chain.addAll(buildHandlerChain(hc, clz.getClassLoader()));
                }

            } catch (Exception e) {
                throw new WebServiceException("Chain not specified", e);
            }
        }

        assert chain != null;
        if (existingHandlers != null) {
            chain.addAll(existingHandlers);
        }
        return sortHandlers(chain);
    }

    public List<Handler> buildHandlerChainFromClass(Class<?> clz) {
        return buildHandlerChainFromClass(clz, null);
    }

    private HandlerChainAnnotation findHandlerChainAnnotation(Class<?> clz, boolean searchSEI) {
        if (log.isDebugEnabled()) {
            log.debug("Checking for HandlerChain annotation on " + clz.getName());
        }
        HandlerChainAnnotation hcAnn = null;
        HandlerChain ann = clz.getAnnotation(HandlerChain.class);
        if (ann == null) {
            if (searchSEI) {
                /* HandlerChain annotation can be specified on the SEI
                 * but the implementation bean might not implement the SEI.
                 */
                WebService ws = clz.getAnnotation(WebService.class);
                if (ws != null
                    && ws.endpointInterface() != null
                    && ws.endpointInterface().trim().length() > 0) {
                    String seiClassName = ws.endpointInterface().trim();
                    Class seiClass = null;
                    try {
                        seiClass = clz.getClassLoader().loadClass(seiClassName);
                    } catch (ClassNotFoundException e) {
                        throw new WebServiceException("Failed to load SEI class: " + seiClassName, e);
                    }

                    // check SEI class and its interfaces for HandlerChain annotation
                    hcAnn = findHandlerChainAnnotation(seiClass, false);
                }
            }
            if (hcAnn == null) {
                // check interfaces for HandlerChain annotation
                for (Class<?> iface : clz.getInterfaces()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Checking for HandlerChain annotation on " + iface.getName());
                    }
                    ann = iface.getAnnotation(HandlerChain.class);
                    if (ann != null) {
                        hcAnn = new HandlerChainAnnotation(ann, iface);
                        break;
                    }
                }
            }
        } else {
            hcAnn = new HandlerChainAnnotation(ann, clz);
        }

        return hcAnn;
    }

    protected List<Handler> buildHandlerChain(org.apache.openejb.jee.HandlerChain hc, ClassLoader classLoader) {
        List<Handler> handlerChain = new ArrayList<Handler>();
        for (org.apache.openejb.jee.Handler ht : hc.getHandler()) {
            try {
                log.debug("loading handler :" + trimString(ht.getHandlerName()));

                Class<? extends Handler> handlerClass = Class.forName(
                        trimString(ht.getHandlerClass()), true, classLoader)
                        .asSubclass(Handler.class);

                Handler handler = handlerClass.newInstance();
                log.debug("adding handler to chain: " + handler);
                handlerChain.add(handler);
            } catch (Exception e) {
                throw new WebServiceException("Failed to instantiate handler", e);
            }
        }
        return handlerChain;
    }

    private String trimString(String str) {
        return str != null ? str.trim() : null;
    }

    /**
     * sorts the handlers into correct order. All of the logical handlers first
     * followed by the protocol handlers
     *
     * @param handlers
     * @return sorted list of handlers
     */
    public List<Handler> sortHandlers(List<Handler> handlers) {

        List<LogicalHandler> logicalHandlers = new ArrayList<LogicalHandler>();
        List<Handler> protocolHandlers = new ArrayList<Handler>();

        for (Handler handler : handlers) {
            if (handler instanceof LogicalHandler) {
                logicalHandlers.add((LogicalHandler) handler);
            } else {
                protocolHandlers.add(handler);
            }
        }

        List<Handler> sortedHandlers = new ArrayList<Handler>();
        sortedHandlers.addAll(logicalHandlers);
        sortedHandlers.addAll(protocolHandlers);
        return sortedHandlers;
    }

    private static class HandlerChainAnnotation {
        private final Class<?> declaringClass;
        private final HandlerChain ann;

        HandlerChainAnnotation(HandlerChain hc, Class<?> clz) {
            ann = hc;
            declaringClass = clz;
        }

        public Class<?> getDeclaringClass() {
            return declaringClass;
        }

        public String getFileName() {
            return ann.file();
        }

        public void validate() {
            if (null == ann.file() || "".equals(ann.file())) {
                throw new WebServiceException("@HandlerChain annotation does not contain a file name or url.");
            }
        }

        public String toString() {
            return "[" + declaringClass + "," + ann + "]";
        }
    }
}
