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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ManagedContextHandlerChain implements ManagedContextHandler {

    private final static Log LOG = LogFactory.getLog(ManagedContextHandlerChain.class);
    
    private final static String INVOKED_HANDLERS = 
        ManagedContextHandlerChain.class.getName() + ".invokedHandlers";
    
    private List<ManagedContextHandler> chain = new ArrayList<ManagedContextHandler>();
        
    public ManagedContextHandlerChain() {        
    }
    
    public ManagedContextHandlerChain(Collection<ManagedContextHandler> handlers) {
        if (handlers != null) {
            this.chain.addAll(handlers);
        }
    }
    
    public void addManagedContextHandler(ManagedContextHandler handler) {
        this.chain.add(handler);
    }
    
    public void removeManagedContextHandler(ManagedContextHandler handler) {
        this.chain.remove(handler);
    }
    
    public void saveContext(Map<String, Object> context) {
        // call in proper order
        ListIterator<ManagedContextHandler> iter = this.chain.listIterator();
        while (iter.hasNext()) {
            ManagedContextHandler handler = iter.next();         
            handler.saveContext(context);
        }         
    }

    public void setContext(Map<String, Object> threadContext) {        
        // keep track of invoked handlers
        List<ManagedContextHandler> invokedHandlers = new ArrayList<ManagedContextHandler>();
        threadContext.put(INVOKED_HANDLERS, invokedHandlers);
        
        // call in proper order
        ListIterator<ManagedContextHandler> iter = this.chain.listIterator();
        while (iter.hasNext()) {
            ManagedContextHandler handler = iter.next();
            try {
                handler.setContext(threadContext);
            } finally {
                invokedHandlers.add(handler);
            }
        }  
    }

    public void unsetContext(Map<String, Object> threadContext) {
        List<ManagedContextHandler> invokedHandlers = 
            (List<ManagedContextHandler>)threadContext.get(INVOKED_HANDLERS);
        // call in reverse order
        ListIterator<ManagedContextHandler> iter = 
            invokedHandlers.listIterator(invokedHandlers.size());
        while (iter.hasPrevious()) {
            ManagedContextHandler handler = iter.previous();
            try {
                handler.unsetContext(threadContext);
            } catch (RuntimeException e) {
                LOG.error("Error occurred in unsetContext function: " + e.getMessage(), e);
            }
        } 
        
        invokedHandlers.clear();
    }
    
}
