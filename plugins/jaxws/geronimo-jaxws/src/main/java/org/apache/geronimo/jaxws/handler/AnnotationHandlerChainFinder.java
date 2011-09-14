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

import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.WebServiceException;

import org.apache.geronimo.jaxws.info.HandlerChainsInfo;
import org.apache.geronimo.kernel.util.IOUtils;
import org.apache.openejb.jee.HandlerChains;
import org.apache.openejb.jee.JaxbJavaee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class AnnotationHandlerChainFinder
{
    private static final Logger log = LoggerFactory.getLogger(AnnotationHandlerChainFinder.class);

    private HandlerChainsInfoBuilder handlerChainsInfoBuilder = new HandlerChainsInfoBuilder();

    /**
     * @param clz
     * @param existingHandlers
     * @return
     */
    public HandlerChainsInfo buildHandlerChainFromClass(Class<?> clz, HandlerChainsInfo existingHandlerChainsInfo) {
        if (log.isDebugEnabled()) {
            log.debug("building handler chain on class " + clz.getName());
        }
        HandlerChainsInfo handlerChainsInfo = null;
        HandlerChainAnnotation hcAnn = findHandlerChainAnnotation(clz, true);
        if (hcAnn == null) {
            if (log.isDebugEnabled()) {
                log.debug("no HandlerChain annotation on " + clz);
            }
            handlerChainsInfo = new HandlerChainsInfo();
        } else {
            hcAnn.validate();

            try {
                URL handlerFileURL = clz.getResource(hcAnn.getFileName());
                HandlerChains handlerChainsType;
                InputStream in = null;
                try {
                    if (handlerFileURL == null) {
                        handlerFileURL = new URL(hcAnn.getFileName());
                    }
                    in = handlerFileURL.openStream();
                    handlerChainsType = (HandlerChains) JaxbJavaee.unmarshalHandlerChains(HandlerChains.class, in);
                } catch (Exception e) {
                    throw new WebServiceException("Could not read the chain info from " + hcAnn.getFileName(), e);
                } finally {
                    IOUtils.close(in);
                }

                if (null == handlerChainsType || handlerChainsType.getHandlerChain().isEmpty()) {
                    throw new WebServiceException("Chain not specified for class " + clz.getName());
                }

                handlerChainsInfo = handlerChainsInfoBuilder.build(handlerChainsType);

            } catch (WebServiceException e) {
                throw e;
            } catch (Exception e) {
                throw new WebServiceException("Chain not specified", e);
            }
        }

        if (existingHandlerChainsInfo != null) {
            handlerChainsInfo.handleChains.addAll(existingHandlerChainsInfo.handleChains);
        }
        return handlerChainsInfo;
    }

    public HandlerChainsInfo buildHandlerChainFromClass(Class<?> clz) {
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
            if (null == ann.file() || ann.file().isEmpty()) {
                throw new WebServiceException("@HandlerChain annotation does not contain a file name or url.");
            }
        }

        public String toString() {
            return "[" + declaringClass + "," + ann + "]";
        }
    }
}
