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

package org.apache.geronimo.axis2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.xbeans.javaee.HandlerChainType;
import org.apache.geronimo.xbeans.javaee.HandlerChainsType;
import org.apache.geronimo.xbeans.javaee.PortComponentHandlerType;
import org.apache.geronimo.xbeans.javaee.HandlerChainsDocument;

import javax.jws.HandlerChain;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class AnnotationHandlerChainBuilder {

    private static final Log log = LogFactory.getLog(AnnotationHandlerChainBuilder.class);

    public AnnotationHandlerChainBuilder() {
    }

    /**
     * @param clz
     * @param existingHandlers
     * @return
     */
    public List<Handler> buildHandlerChainFromClass(Class<?> clz, List<Handler> existingHandlers) {
        log.debug("building handler chain");
        HandlerChainAnnotation hcAnn = findHandlerChainAnnotation(clz);
        List<Handler> chain = null;
        if (hcAnn == null) {
            log.debug("no HandlerChain annotation on " + clz);
            chain = new ArrayList<Handler>();
        } else {
            hcAnn.validate();

            HandlerChainType hc = null;
            try {
                URL handlerFileURL = clz.getResource(hcAnn.getFileName());
                HandlerChainsType handlerChainsType = HandlerChainsDocument.Factory.parse(handlerFileURL).getHandlerChains();

                if (null == handlerChainsType || handlerChainsType.getHandlerChainArray() == null) {
                    throw new WebServiceException("Chain not specified");
                }
                //We expect only one HandlerChainType here
                hc = handlerChainsType.getHandlerChainArray()[0];
            } catch (Exception e) {
                e.printStackTrace();
                throw new WebServiceException("Chain not specified", e);
            }

            chain = buildHandlerChain(hc, clz.getClassLoader());
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

    private HandlerChainAnnotation findHandlerChainAnnotation(Class<?> clz) {

        HandlerChain ann = clz.getAnnotation(HandlerChain.class);
        Class<?> declaringClass = clz;

        if (ann == null) {
            for (Class<?> iface : clz.getInterfaces()) {
                if (log.isDebugEnabled()) {
                    log.debug("checking for HandlerChain annotation on " + iface.getName());
                }
                ann = iface.getAnnotation(HandlerChain.class);
                if (ann != null) {
                    declaringClass = iface;
                    break;
                }
            }
        }
        if (ann != null) {
            return new HandlerChainAnnotation(ann, declaringClass);
        } else {
            return null;
        }
    }

    protected List<Handler> buildHandlerChain(HandlerChainType hc, ClassLoader classLoader) {
        List<Handler> handlerChain = new ArrayList<Handler>();
        for (PortComponentHandlerType ht : hc.getHandlerArray()) {
            try {
                log.debug("loading handler :" + trimString(ht.getHandlerName().getStringValue()));

                Class<? extends Handler> handlerClass = Class.forName(
                        trimString(ht.getHandlerClass()
                                .getStringValue()), true, classLoader)
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
