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

import javax.security.jacc.PolicyContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.concurrent.ManagedContextHandler;
import org.apache.geronimo.security.Callers;
import org.apache.geronimo.security.ContextManager;

/*
 * Security context handler.
 */
public class SecurityContextHandler implements ManagedContextHandler {

    private final static Log LOG = LogFactory.getLog(SecurityContextHandler.class);
    
    private final static String OLD_CALLERS = 
        SecurityContextHandler.class.getName() + ".oldCallers";
    
    private final static String OLD_CONTEXT_ID = 
        SecurityContextHandler.class.getName() + ".oldContextId";
    
    private final static String NEW_CALLERS = 
        SecurityContextHandler.class.getName() + ".newCallers";
    
    private final static String NEW_CONTEXT_ID = 
        SecurityContextHandler.class.getName() + ".newContextId";
            
    public void saveContext(Map<String, Object> context) {
        LOG.debug("saveContext");
        
        context.put(NEW_CONTEXT_ID, 
                    PolicyContext.getContextID());
        context.put(NEW_CALLERS, 
                    ContextManager.getCallers());
    }

    public void setContext(Map<String, Object> threadContext) {
        LOG.debug("setContext");
        
        // save existing security info
        threadContext.put(OLD_CONTEXT_ID, 
                          PolicyContext.getContextID());
        threadContext.put(OLD_CALLERS, 
                          ContextManager.getCallers());
    
        // set new security info
        String contextId = (String)threadContext.get(NEW_CONTEXT_ID);
        Callers callers = (Callers)threadContext.get(NEW_CALLERS);
        
        PolicyContext.setContextID(contextId);
        ContextManager.popCallers(callers); // works like setCallers()
    }

    public void unsetContext(Map<String, Object> threadContext) {
        LOG.debug("unsetContext");
        
        // restore old security info
        String contextId = (String)threadContext.get(OLD_CONTEXT_ID);
        Callers callers = (Callers)threadContext.get(OLD_CALLERS);
        
        PolicyContext.setContextID(contextId);
        ContextManager.popCallers(callers); // works like setCallers()
    }
  
}
