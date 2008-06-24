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
package org.apache.geronimo.concurrent.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.concurrent.ManagedContextHandler;

public class ContextHandlerUtils {
        
    private final static Log LOG = LogFactory.getLog(ContextHandlerUtils.class);
    
    public static List<ManagedContextHandler> loadHandlers(ClassLoader classLoader,
                                                           String[] handlerClasses) {
        List<ManagedContextHandler> handlers = new ArrayList<ManagedContextHandler>();
        if (handlerClasses != null) {
            for (String handlerClass : handlerClasses) {
                try {
                    handlers.add(loadHandler(classLoader, handlerClass.trim()));
                } catch (Exception e) {
                    LOG.warn("Failed to load context handler class " + handlerClass, e);
                }
            }
        }
        return handlers;
    }
    
    public static ManagedContextHandler loadHandler(ClassLoader classLoader, 
                                                    String className)
        throws Exception {
        Class clazz = classLoader.loadClass(className);
        if (!ManagedContextHandler.class.isAssignableFrom(clazz)) {
            throw new Exception("Class " + className + " is not a ManagedContextHandler class");
        }
        return (ManagedContextHandler) clazz.newInstance();
    }
               
}
