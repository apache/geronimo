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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.PortInfo;

import org.apache.geronimo.jaxws.JAXWSUtils;
import org.apache.geronimo.xbeans.javaee.HandlerChainType;
import org.apache.xbean.osgi.bundle.util.BundleClassLoader;
import org.apache.xmlbeans.XmlCursor;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoHandlerChainBuilder extends AnnotationHandlerChainBuilder {
    private static final Logger log = LoggerFactory.getLogger(GeronimoHandlerChainBuilder.class);

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

    protected List<Handler> buildHandlerChain(HandlerChainType hc,
                                              Bundle bundle) {
        if (matchServiceName(portInfo, hc)
                && matchPortName(portInfo, hc)
                && matchBinding(portInfo, hc)) {
            return super.buildHandlerChain(hc, new BundleClassLoader(bundle));
        } else {
            return Collections.emptyList();
        }
    }

    private boolean matchServiceName(PortInfo info, HandlerChainType hc) {
        if (hc.isSetServiceNamePattern()) {
            QName serviceName = (info == null) ? null : info.getServiceName();
            return match(hc.xgetServiceNamePattern().newCursor(), serviceName, hc.getServiceNamePattern());
        } else {
            // handler matches since no service-name-pattern
            return true;
        }
    }

    private boolean matchPortName(PortInfo info, HandlerChainType hc) {
        if (hc.isSetPortNamePattern()) {
            QName portName = (info == null) ? null : info.getPortName();
            return match(hc.xgetPortNamePattern().newCursor(), portName, hc.getPortNamePattern());
        } else {
            // handler maches no port-name-pattern
            return true;
        }
    }

    private boolean matchBinding(PortInfo info, HandlerChainType hc) {
        return match((info == null ? null : info.getBindingID()), hc.getProtocolBindings());
    }

    private boolean match(String binding, List bindings) {
        if (binding == null) {
            return (bindings == null || bindings.isEmpty());
        } else {
            if (bindings == null || bindings.isEmpty()) {
                return true;
            } else {
                String actualBindingURI = JAXWSUtils.getBindingURI(binding);
                Iterator iter = bindings.iterator();
                while (iter.hasNext()) {
                    String bindingToken = (String) iter.next();
                    String bindingURI = JAXWSUtils.getBindingURI(bindingToken);
                    if (actualBindingURI.equals(bindingURI)) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public List<Handler> buildHandlerChainFromConfiguration(HandlerChainType hc) {
        if (null == hc) {
            return null;
        }
        return sortHandlers(buildHandlerChain(hc, getHandlerBundle()));
    }

    /*
     * Performs basic localName matching, namespaces are not checked!
     */
    private boolean match(XmlCursor node, QName name, String namePattern) {
        if (name == null) {
            return (namePattern == null || namePattern.equals("*"));
        } else {
            if (namePattern == null) {
                return true;
            } else {
                String localNamePattern;

                // get the local name from pattern
                int pos = namePattern.indexOf(':');
                if (pos == -1) {
                    localNamePattern = namePattern;
                } else {
                    localNamePattern = namePattern.substring(pos + 1);

                    String prefix = namePattern.substring(0, pos);
                    String namespace = node.namespaceForPrefix(prefix.trim());
                    if (namespace == null) {
                        namespace = prefix;
                    }

                    // check namespace
                    if (!namespace.equals(name.getNamespaceURI())) {
                        return false;
                    }
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
