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
package org.apache.geronimo.concurrent;

import java.util.HashMap;
import java.util.Map;

import org.apache.geronimo.concurrent.spi.ManagedContextBuilder;

public abstract class ManagedContext {

    protected ManagedContextHandler contextHandler;
    protected Map<String, Object> context;

    public ManagedContext(ManagedContextHandler handler,
                          Map<String, Object> context) {
        if (handler == null || context == null) {
            throw new NullPointerException("executor or listener is null");
        }
        this.contextHandler = handler;
        this.context = context;
    }
    
    public static ManagedContext captureContext(ManagedContextHandler contextHandler) {
        ManagedContextBuilder builder = ManagedContextBuilder.getManagedContextBuilder();
        return builder.buildManagedContext(contextHandler);
    }
    
    /**
     * Checks if the context is still valid. The context becomes invalid
     * when the component that was used to create this context is stopped or 
     * was undeployed. 
     * 
     * @return true if the context's component is running. False, if the context's components
     *         is not running.
     */
    public abstract boolean isValid();
   
    public Map<String, Object> set() {
        Map<String, Object> threadContext = new HashMap<String, Object>();
        set(threadContext);
        return threadContext;
    }
      
    public void set(Map<String, Object> threadContext) {
        if (!isValid()) {
           throw new IllegalStateException("Context is not valid: context's component is not running");
        }
        threadContext.putAll(this.context);
        this.contextHandler.setContext(threadContext);
    }
    
    public void unset(Map<String, Object> threadContext) {
        this.contextHandler.unsetContext(threadContext);
    }

}
