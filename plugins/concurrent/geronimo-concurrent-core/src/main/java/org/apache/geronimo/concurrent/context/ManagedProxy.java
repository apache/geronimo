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

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.concurrent.ManagedContext;
import org.apache.geronimo.concurrent.ManagedContextHandler;

public class ManagedProxy implements InvocationHandler, Serializable {

    private final static Log LOG = LogFactory.getLog(ManagedProxy.class);
    
    private static final Method HASHCODE_METHOD;
    private static final Method EQUALS_METHOD;
    private static final Method TOSTRING_METHOD;
    
    static {
        try {
            HASHCODE_METHOD = Object.class.getMethod("hashCode", null);
            EQUALS_METHOD =
                Object.class.getMethod("equals", new Class[] { Object.class });
            TOSTRING_METHOD = Object.class.getMethod("toString", null);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }
        
    private Object obj;
    private Map<String, String> props;
    private ManagedContext managedContext;
           
    public ManagedProxy(Object obj, ManagedContextHandler contextHandler) {
        if (obj == null || contextHandler == null) {
            throw new IllegalArgumentException("object or contextHandler is null");
        }
        this.obj = obj;
        
        // save context now
        this.managedContext = ManagedContext.captureContext(contextHandler);
    }
    
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class declaringClass = method.getDeclaringClass();
               
        if (declaringClass == Object.class) {
            if (method.equals(HASHCODE_METHOD) ||
                method.equals(EQUALS_METHOD) ||
                method.equals(TOSTRING_METHOD)) {
                // make call without context
                return invokeMethod(proxy, method, args);
            } else {
                throw new InternalError("Unexpected Object method: " + method);
            }
        } else {
            // make call with context                                      
            return invokeContextMethod(proxy, method, args);            
        }
    }

    protected Object invokeContextMethod(Object proxy, Method method, Object[] args) throws Throwable {
        LOG.debug("Calling managed method " + method);
                
        Map<String, Object> threadContext = new HashMap<String, Object>();
        threadContext.putAll(this.props);
        
        this.managedContext.set(threadContext);
        try {
            return method.invoke(this.obj, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } finally {
            this.managedContext.unset(threadContext);
        }
    }
            
    protected Object invokeMethod(Object proxy, Method method, Object[] args) throws Throwable {
        LOG.debug("Calling non-managed method " + method);
        
        try {
            return method.invoke(this.obj, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }
    
    public void setProperties(Map<String, String> props) {
        this.props = props;
    }
    
    public Map<String, String> getProperties() {
        return this.props;
    }
  
}
