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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.PortInfo;

import org.apache.geronimo.jaxws.JAXWSUtils;
import org.apache.geronimo.jaxws.info.HandlerChainInfo;
import org.apache.geronimo.jaxws.info.HandlerInfo;
import org.apache.xbean.osgi.bundle.util.BundleClassLoader;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoHandlerChainBuilder {

    private static final Logger logger = LoggerFactory.getLogger(GeronimoHandlerChainBuilder.class);
    private Bundle bundle = null;
    private PortInfo portInfo;

    public GeronimoHandlerChainBuilder(Bundle bundle,
                                       PortInfo portInfo) {
        this.bundle = bundle;
        this.portInfo = portInfo;
    }

    public Bundle getHandlerBundle() {
        return bundle;
    }

    protected List<Handler> buildHandlerChain(HandlerChainInfo hc,
                                              Bundle bundle) {
        if (matchServiceName(portInfo, hc)
                && matchPortName(portInfo, hc)
                && matchBinding(portInfo, hc)) {
            return buildHandlerChain(hc, new BundleClassLoader(bundle));
        } else {
            return Collections.emptyList();
        }
    }

    protected List<Handler> buildHandlerChain(HandlerChainInfo handlerChainInfo, ClassLoader classLoader) {
        List<Handler> handlerChain = new ArrayList<Handler>(handlerChainInfo.handlers.size());
        for (HandlerInfo handlerInfo : handlerChainInfo.handlers) {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("loading handler :" + trimString(handlerInfo.handlerName));
                }
                Class<? extends Handler> handlerClass = Class.forName(
                        trimString(handlerInfo.handlerClass), true, classLoader)
                        .asSubclass(Handler.class);
                Handler handler = handlerClass.newInstance();
                if (logger.isDebugEnabled()) {
                    logger.debug("adding handler to chain: " + handler);
                }
                handlerChain.add(handler);
            } catch (Exception e) {
                throw new WebServiceException("Failed to instantiate handler", e);
            }
        }
        return handlerChain;
    }
    
    private boolean matchServiceName(PortInfo info, HandlerChainInfo hc) {
        if (hc.serviceNamePattern != null) {
            QName serviceName = (info == null) ? null : info.getServiceName();
            return match(serviceName, hc.serviceNamePattern);
        } else {
            // handler matches since no service-name-pattern
            return true;
        }
    }

    private boolean matchPortName(PortInfo info, HandlerChainInfo hc) {
        if (hc.portNamePattern != null) {
            QName portName = (info == null) ? null : info.getPortName();
            return match(portName, hc.portNamePattern);
        } else {
            // handler maches no port-name-pattern
            return true;
        }
    }

    private boolean matchBinding(PortInfo info, HandlerChainInfo hc) {
        return match((info == null ? null : info.getBindingID()), hc.protocolBindings);
    }

    private boolean match(String binding, List<String> bindings) {
        if (binding == null) {
            return (bindings == null || bindings.isEmpty());
        } else {
            if (bindings == null || bindings.isEmpty()) {
                return true;
            } else {
                String actualBindingURI = JAXWSUtils.getBindingURI(binding);
                for (String bindingToken : bindings) {
                    String bindingURI = JAXWSUtils.getBindingURI(bindingToken);
                    if (actualBindingURI.equals(bindingURI)) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public List<Handler> buildHandlerChainFromConfiguration(HandlerChainInfo handlerChainInfo) {
        if (handlerChainInfo == null) {
            return null;
        }
        return sortHandlers(buildHandlerChain(handlerChainInfo, getHandlerBundle()));
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
    
    /*
     * Performs basic localName matching, namespaces are not checked!
     */
    private boolean match(QName name, QName namePattern) {
        if (name == null) {
            return (namePattern == null || namePattern.getLocalPart().equals("*"));
        } else {
            if (namePattern == null) {
                return true;
            } else {
                String localNamePattern = namePattern.getLocalPart();
                // check namespace
                if (!namePattern.getNamespaceURI().equals(name.getNamespaceURI())) {
                    return false;
                }

                // check local name
                localNamePattern = localNamePattern.trim();
                if (localNamePattern.contains("*")) {
                    //wildcard pattern matching
                    Pattern pattern = Pattern.compile(localNamePattern.replace("*", "(\\w|\\.|-|_)*"));
                    Matcher matcher = pattern.matcher(name.getLocalPart());
                    return matcher.matches();
                } else {
                    return localNamePattern.equals(name.getLocalPart());
                }
            }
        }
    }

}
