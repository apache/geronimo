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
package org.apache.geronimo.concurrent.impl.handlers;

import java.util.Map;

import javax.naming.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.concurrent.ManagedContextHandler;
import org.apache.geronimo.naming.java.RootContext;

/**
 * Geronimo naming context handler. 
 */
public class NamingContextHandler implements ManagedContextHandler {

    private final static Log LOG = LogFactory.getLog(NamingContextHandler.class);
    
    private final static String OLD_CONTEXT = 
        NamingContextHandler.class.getName() + ".oldContext";
    
    private final static String NEW_CONTEXT = 
        NamingContextHandler.class.getName() + ".newContext";
        
    public void saveContext(Map<String, Object> context) {
        LOG.debug("saveContext");
        
        Context componentContext = RootContext.getComponentContext();
        if (!UserTransactionContext.hasUserTransaction(componentContext)) {
            componentContext = new UserTransactionContext(componentContext);
            LOG.debug("java:comp/UserTransaction not found. Using UserTransactionContext");
        }
        context.put(NEW_CONTEXT, componentContext);
    }

    public void setContext(Map<String, Object> threadContext) {
        LOG.debug("setContext");
        
        // save existing context
        threadContext.put(OLD_CONTEXT,
                          RootContext.getComponentContext());
        
        // set new context
        Context context = (Context)threadContext.get(NEW_CONTEXT);
        RootContext.setComponentContext(context);
    }

    public void unsetContext(Map<String, Object> threadContext) {
        LOG.debug("unsetContext");
        
        // restore old context
        Context context = (Context)threadContext.get(OLD_CONTEXT);
        RootContext.setComponentContext(context);       
    }
  
}
