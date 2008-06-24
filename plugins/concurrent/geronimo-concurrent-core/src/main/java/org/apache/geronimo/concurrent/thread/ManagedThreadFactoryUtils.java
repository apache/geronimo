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
package org.apache.geronimo.concurrent.thread;

import javax.util.concurrent.ManagedThreadFactory;

import org.apache.geronimo.concurrent.ManagedContext;
import org.apache.geronimo.concurrent.ManagedContextHandler;

public class ManagedThreadFactoryUtils {
    
    /**
     * Creates ThreadFactory to be used within component-managed executor service.
     */
    public static ManagedThreadFactory createEmbeddedThreadFactory(ManagedThreadFactory factory, 
                                                                   ManagedContext managedContext) {
        // apply context to the thread only
        return new GenericThreadFactory(factory, managedContext, false);
    }
            
    public static ManagedThreadFactory createStandaloneThreadFactory(ManagedThreadFactory factory, 
                                                                     ManagedContextHandler contextHandler) {
        ManagedContext managedContext = ManagedContext.captureContext(contextHandler);
        // apply context to the thread AND set the right task info on the thread 
        return new GenericThreadFactory(factory, managedContext, true);
    }
        
    private static class GenericThreadFactory implements ManagedThreadFactory {

        private ManagedThreadFactory factory;  
        private ManagedContext managedContext; 
        private boolean associateTask;
        
        public GenericThreadFactory(ManagedThreadFactory factory, 
                                    ManagedContext managedContext,
                                    boolean associateTask) {
            this.factory = factory;       
            this.managedContext = managedContext;
            this.associateTask = associateTask;
        }
        
        public Thread newThread(Runnable runnable) {                
            runnable = new ManagedRunnable(runnable, this.managedContext, this.associateTask);
            return this.factory.newThread(runnable);
        }
               
    }
            
}
