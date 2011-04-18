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
package org.apache.geronimo.jaxws.client;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.jaxws.JAXWSUtils;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.jaas.NamedUsernamePasswordCredential;

import javax.security.auth.Subject;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.soap.SOAPBinding;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map;
import java.util.Set;

public class PortMethodInterceptor implements MethodInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(PortMethodInterceptor.class);

    private Map<Object, EndpointInfo> seiInfoMap;

    public PortMethodInterceptor(Map<Object, EndpointInfo> seiInfoMap) {
        this.seiInfoMap = seiInfoMap;
    }

    public Object intercept(Object target, Method method, Object[] arguments, MethodProxy methodProxy) throws Throwable {
        Object proxy = methodProxy.invokeSuper(target, arguments);

        if (method.getName().equals("getPort")) {
            // it's a generic getPort() method
            Class<?> paramType = method.getParameterTypes()[0];
            if (paramType.equals(Class.class)) {
                // getPort(Class) or getPort(Class, WebServiceFeatures) called - use SEI annotation
                setProperties((BindingProvider)proxy, JAXWSUtils.getPortType((Class)arguments[0]));
            } else if (paramType.equals(QName.class)) {
                // getPort(QName, Class) or getPort(QName, Class, WebServiceFeatures) called
                if (arguments[0] == null) {
                    // port qname not specified - use SEI annotation
                    setProperties((BindingProvider)proxy, JAXWSUtils.getPortType((Class)arguments[1]));
                } else {
                    // port qname specified
                    setProperties((BindingProvider)proxy, ((QName)arguments[0]).getLocalPart());
                }
            }
        } else if (method.getName().startsWith("get")) {
            // it's a generated get<PortName>() method
            WebEndpoint endpoint = method.getAnnotation(WebEndpoint.class);
            setProperties((BindingProvider)proxy, endpoint.name());
        } else if (method.getName().equals("createDispatch")) {
            // it's one of createDispatch() methods
            Class<?> paramType = method.getParameterTypes()[0];
            if (paramType.equals(QName.class)) {
                // one of creatDispatch(QName, ....) methods is called
                setProperties((BindingProvider)proxy, ((QName)arguments[0]).getLocalPart());
            }
        }

        return proxy;
    }

    private void setProperties(BindingProvider proxy, QName portType) {
        if (portType == null) {
            return;
        }
        EndpointInfo info = this.seiInfoMap.get(portType);
        setProperties(proxy, info);
    }

    private void setProperties(BindingProvider proxy, String portName) {
        if (portName == null) {
            return;
        }
        EndpointInfo info = this.seiInfoMap.get(portName);
        setProperties(proxy, info);
    }

    protected void setProperties(BindingProvider proxy, EndpointInfo info) {
        if (info == null) {
            return;
        }
        setProperties(proxy, info, info.getProperties());
    }

    protected void setProperties(BindingProvider proxy, EndpointInfo info, Map<String, Object> properties) {
        if (info == null) {
            return;
        }

        // set mtom
        boolean enableMTOM = info.isMTOMEnabled();
        if (enableMTOM && proxy.getBinding() instanceof SOAPBinding) {
            ((SOAPBinding)proxy.getBinding()).setMTOMEnabled(enableMTOM);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Set mtom property: " + enableMTOM);
            }
        }

        // set address
        URL location = info.getLocation();
        if (location != null) {
            proxy.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, location.toString());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Set address property: " + location);
            }
        }

        // set credentials
        String credentialsName = info.getCredentialsName();
        if (credentialsName != null) {
            NamedUsernamePasswordCredential namedUsernamePasswordCredential = findCredential(credentialsName);
            proxy.getRequestContext().put(BindingProvider.USERNAME_PROPERTY,
                                          namedUsernamePasswordCredential.getUsername());
            proxy.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY,
                                         new String(namedUsernamePasswordCredential.getPassword()));
        }

        // set user-specified properties
        if (properties != null) {
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                proxy.getRequestContext().put(entry.getKey(), entry.getValue());
            }
        }
    }

    protected NamedUsernamePasswordCredential findCredential(String credentialsName) {
        Subject subject = ContextManager.getNextCaller();
        if (subject == null) {
            throw new IllegalStateException("Subject missing but authentication turned on");
        } else {
            Set<NamedUsernamePasswordCredential> creds = subject.getPrivateCredentials(NamedUsernamePasswordCredential.class);
            for (NamedUsernamePasswordCredential namedUsernamePasswordCredential : creds) {
                if (credentialsName.equals(namedUsernamePasswordCredential.getName())) {
                    return namedUsernamePasswordCredential;
                }
            }
            throw new IllegalStateException("No NamedUsernamePasswordCredential found for name " + credentialsName);
        }
    }
}
