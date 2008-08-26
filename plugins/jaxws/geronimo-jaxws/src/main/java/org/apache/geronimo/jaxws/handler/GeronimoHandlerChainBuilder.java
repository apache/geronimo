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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.jaxws.JAXWSUtils;
import org.apache.geronimo.xbeans.javaee.HandlerChainType;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.PortInfo;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoHandlerChainBuilder extends AnnotationHandlerChainBuilder {
    private static final Logger log = LoggerFactory.getLogger(GeronimoHandlerChainBuilder.class);

    private ClassLoader classLoader = null;
    private javax.xml.ws.handler.PortInfo portInfo;

    public GeronimoHandlerChainBuilder(ClassLoader classloader,
                                       PortInfo portInfo) {
        this.classLoader = classloader;
        this.portInfo = portInfo;
    }

    public ClassLoader getHandlerClassLoader() {
        return this.classLoader;
    }

    protected List<Handler> buildHandlerChain(HandlerChainType hc,
                                              ClassLoader classLoader) {
        if (matchServiceName(portInfo, hc.getServiceNamePattern())
                && matchPortName(portInfo, hc.getPortNamePattern())
                && matchBinding(portInfo, hc.getProtocolBindings())) {
            return super.buildHandlerChain(hc, classLoader);
        } else {
            return Collections.emptyList();
        }
    }

    private boolean matchServiceName(PortInfo info, String namePattern) {
        return match((info == null ? null : info.getServiceName()), namePattern);
    }

    private boolean matchPortName(PortInfo info, String namePattern) {
        return match((info == null ? null : info.getPortName()), namePattern);
    }

    private boolean matchBinding(PortInfo info, List bindings) {
        return match((info == null ? null : info.getBindingID()), bindings);
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
        return sortHandlers(buildHandlerChain(hc, getHandlerClassLoader()));
    }

    /*
     * Performs basic localName matching, namespaces are not checked!
     */
    private boolean match(QName name, String namePattern) {
        if (name == null) {
            return (namePattern == null || namePattern.equals("*"));
        } else {
            if (namePattern == null) {
                return true;
            } else {
                String localNamePattern;

                // get the local name from pattern
                int pos = namePattern.indexOf(':');
                localNamePattern = (pos == -1) ? namePattern : namePattern
                        .substring(pos + 1);
                localNamePattern = localNamePattern.trim();

                if (localNamePattern.equals("*")) {
                    // matches anything
                    return true;
                } else if (localNamePattern.endsWith("*")) {
                    // match start
                    localNamePattern = localNamePattern.substring(0,
                            localNamePattern.length() - 1);
                    return name.getLocalPart().startsWith(localNamePattern);
                } else {
                    // match exact
                    return name.getLocalPart().equals(localNamePattern);
                }
            }
        }
    }

}
