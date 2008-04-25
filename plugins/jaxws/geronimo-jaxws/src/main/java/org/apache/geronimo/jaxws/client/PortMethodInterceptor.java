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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class PortMethodInterceptor implements MethodInterceptor {

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    
    private Map<Object, EndpointInfo> seiInfoMap;

    public PortMethodInterceptor(Map<Object, EndpointInfo> seiInfoMap) {
        this.seiInfoMap = seiInfoMap;
    }

    public Object intercept(Object target, Method method, Object[] arguments, MethodProxy methodProxy) throws Throwable {
        Object proxy = methodProxy.invokeSuper(target, arguments);       
        
        if (method.getName().equals("getPort")) {     
            // it's a generic getPort() method
            if (arguments.length == 1) {
                // getPort(Class) called - use SEI annotation
                setProperties((BindingProvider)proxy, JAXWSUtils.getPortType((Class)arguments[0]));
            } else if (arguments.length == 2) {
                // getPort(QName, Class) called
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
            setProperties((BindingProvider)proxy, ((QName)arguments[0]).getLocalPart());
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
    
    private void setProperties(BindingProvider proxy, EndpointInfo info) {
        if (info == null) {
            return;
        }       
        
        // set mtom
        boolean enableMTOM = info.isMTOMEnabled();
        if (enableMTOM && proxy.getBinding() instanceof SOAPBinding) {
            ((SOAPBinding)proxy.getBinding()).setMTOMEnabled(enableMTOM);
            LOG.debug("Set mtom property: " + enableMTOM);
        }
      
        // set address
        URL location = info.getLocation();
        if (location != null) {
            proxy.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, location.toString());
            LOG.debug("Set address property: " + location);
        }
        
        // set credentials
        String credentialsName = info.getCredentialsName();
        if (credentialsName != null) {
            Subject subject = ContextManager.getNextCaller();
            if (subject == null) {
                throw new IllegalStateException("Subject missing but authentication turned on");
            } else {
                Set creds = subject.getPrivateCredentials(NamedUsernamePasswordCredential.class);
                boolean found = false;
                
                for (Iterator iterator = creds.iterator(); iterator.hasNext();) {
                    NamedUsernamePasswordCredential namedUsernamePasswordCredential = (NamedUsernamePasswordCredential) iterator.next();
                    if (credentialsName.equals(namedUsernamePasswordCredential.getName())) {
                        proxy.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, 
                                                      namedUsernamePasswordCredential.getUsername());
                        proxy.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, 
                                                      new String(namedUsernamePasswordCredential.getPassword()));
                        LOG.debug("Set username/password property: " + credentialsName);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new IllegalStateException("no NamedUsernamePasswordCredential found for name " + credentialsName);
                }
            }
        }
    }
}
