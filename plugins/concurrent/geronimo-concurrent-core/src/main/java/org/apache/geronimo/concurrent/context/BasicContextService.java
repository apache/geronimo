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
package org.apache.geronimo.concurrent.context;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import javax.util.concurrent.ContextService;

import org.apache.geronimo.concurrent.ManagedContextHandler;

/**
 * Base implementation of ContextService using Java Dynamic Proxy.
 */
public class BasicContextService implements ContextService {

    protected ManagedContextHandler contextHandler;
    
    public BasicContextService(ManagedContextHandler contextHandler) {
        if (contextHandler == null) {
            throw new NullPointerException("contextHandler is required");
        }
        this.contextHandler = contextHandler;
    }
    
    public Object createContextObject(Object obj, Class<?>[] interfaces) {
        return createContextObject(obj, interfaces, new HashMap<String, String>());
    }

    public Object createContextObject(Object obj, Class<?>[] interfaces, Map<String, String> props) {
        checkInterfaces(obj, interfaces);
        ManagedProxy invHandler = new ManagedProxy(obj, this.contextHandler);
        invHandler.setProperties(props);
        return Proxy.newProxyInstance(obj.getClass().getClassLoader(), 
                                      interfaces, 
                                      invHandler);
    }
    
    public Map<String, String> getProperties(Object obj) {
        ManagedProxy proxy = getContextProxy(obj);
        return proxy.getProperties();
    }

    public void setProperties(Object obj, Map<String, String> props) {
        ManagedProxy proxy = getContextProxy(obj);
        proxy.setProperties(props);
    }
    
    private void checkInterfaces(Object obj, Class<?>[] interfaces) {
        if (interfaces == null || interfaces.length == 0) {
            throw new IllegalArgumentException("Must specify proxy interfaces");
        }
        for (Class clazz : interfaces) {
            if (clazz == null) {
                throw new IllegalArgumentException("Null proxy interface");
            }
            if (!clazz.isAssignableFrom(obj.getClass())) {
                throw new IllegalArgumentException("Instance must implement " + clazz.getName() + " interface");
            }
        }
    }
    
    private ManagedProxy getContextProxy(Object obj) {
        InvocationHandler invHandler = Proxy.getInvocationHandler(obj);
        if (invHandler instanceof ManagedProxy) {
            return (ManagedProxy)invHandler;
        } else {
            throw new IllegalArgumentException("Invalid proxy");
        }
    }
   
}
