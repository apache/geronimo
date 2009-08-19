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

package org.apache.geronimo.cxf.client;

import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.geronimo.jaxws.client.EndpointInfo;
import org.apache.geronimo.jaxws.client.PortMethodInterceptor;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CXFPortMethodInterceptor extends PortMethodInterceptor {
    
    private static final Logger LOG = LoggerFactory.getLogger(CXFPortMethodInterceptor.class);

    private static final String IN_PREFIX = "wss4j.in.";
    private static final String OUT_PREFIX = "wss4j.out.";
    
    private static final String[] ACTIONS = { WSHandlerConstants.USERNAME_TOKEN,
                                              WSHandlerConstants.SIGNATURE,
                                              WSHandlerConstants.ENCRYPT };
    
    public CXFPortMethodInterceptor(Map<Object, EndpointInfo> seiInfoMap) {
        super(seiInfoMap);
    }

    @Override
    protected void setProperties(BindingProvider proxy, EndpointInfo info, Map<String, Object> props) {
        if (info == null) {
            return;
        } 
        
        Map<String, Object> wss4jInProps = new HashMap<String, Object>();
        Map<String, Object> wss4jOutProps = new HashMap<String, Object>();
        Map<String, Object> otherProps = new HashMap<String, Object>();
        
        for (Map.Entry<String, Object> entry : props.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key.startsWith(IN_PREFIX)) {
                key = key.substring(IN_PREFIX.length());
                wss4jInProps.put(key, value);
            } else if (key.startsWith(OUT_PREFIX)) {
                key = key.substring(OUT_PREFIX.length());
                wss4jOutProps.put(key, value);
            } else {
                otherProps.put(key, value);
            }
        }
        
        super.setProperties(proxy, info, otherProps);
                
        if (proxy instanceof Dispatch) {
            if (!wss4jInProps.isEmpty() || !wss4jOutProps.isEmpty()) {
                LOG.warn("wss4j properties are not supported for Dispatch clients");
            }
            return;
        }
        
        Client client = ClientProxy.getClient(proxy);
        Endpoint cxfEndpoint = client.getEndpoint();
                
        if (!wss4jOutProps.isEmpty()) {
            // pass the security properties to the WSS4J out interceptor
            updateSecurityProperties(wss4jOutProps);
            WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(wss4jOutProps);
            cxfEndpoint.getOutInterceptors().add(wssOut);
        }
        
        if (!wss4jInProps.isEmpty()) {
            // pass the security properties to the WSS4J in interceptor
            WSS4JInInterceptor wssIn = new WSS4JInInterceptor(wss4jInProps);
            cxfEndpoint.getInInterceptors().add(wssIn);
        }
               
    }

    private static void updateSecurityProperties(Map<String, Object> properties) {
        String action = (String) properties.get(WSHandlerConstants.ACTION);
        if (containsValue(action, ACTIONS) && 
            !properties.containsKey(WSHandlerConstants.PW_CALLBACK_CLASS)) {
            CXFPasswordHandler handler = new CXFPasswordHandler();
            handler.addPassword( (String) properties.get("user"),
                                 (String) properties.get("password") );
            handler.addPassword( (String) properties.get("signatureUser"),
                                 (String) properties.get("signaturePassword") );
            handler.addPassword( (String) properties.get("encryptionUser"),
                                 (String) properties.get("encryptionPassword") );
            properties.put(WSHandlerConstants.PW_CALLBACK_REF, handler);
        }
    }
    
    private static boolean containsValue(String property, String[] values) {
        if (property != null) {
            String[] entries = property.split(" ");
            for (String value : values) {
                for (String entry : entries) {
                    if (value.equals(entry)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
