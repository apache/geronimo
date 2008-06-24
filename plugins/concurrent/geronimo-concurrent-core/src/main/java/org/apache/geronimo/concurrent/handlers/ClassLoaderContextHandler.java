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
package org.apache.geronimo.concurrent.handlers;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.concurrent.ManagedContextHandler;

/*
 * Managed context handler that for thread classloader.
 */
public class ClassLoaderContextHandler implements ManagedContextHandler {

    private final static Log LOG = LogFactory.getLog(ClassLoaderContextHandler.class);
    
    private final static String OLD_CLASSLOADER = 
        ClassLoaderContextHandler.class.getName() + ".oldClassloader";
    
    private final static String NEW_CLASSLOADER = 
        ClassLoaderContextHandler.class.getName() + ".newClassloader";
        
    public void saveContext(Map<String, Object> context) {
        LOG.debug("saveContext");
        
        Thread thread = Thread.currentThread();
        context.put(NEW_CLASSLOADER, 
                    thread.getContextClassLoader());                    
    }

    public void setContext(Map<String, Object> threadContext) {
        LOG.debug("setContext");
        
        Thread thread = Thread.currentThread();
        
        // save existing classloader
        threadContext.put(OLD_CLASSLOADER, 
                          thread.getContextClassLoader());
        
        // set new classloader
        ClassLoader classLoader = (ClassLoader)threadContext.get(NEW_CLASSLOADER);
        thread.setContextClassLoader(classLoader);
    }

    public void unsetContext(Map<String, Object> threadContext) {
        LOG.debug("unsetContext");
        
        Thread thread = Thread.currentThread();
        
        // restore old classloader
        ClassLoader classLoader = (ClassLoader)threadContext.get(OLD_CLASSLOADER);
        thread.setContextClassLoader(classLoader);;       
    }
  
}
