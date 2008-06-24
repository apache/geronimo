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
package org.apache.geronimo.concurrent.impl.thread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.geronimo.concurrent.ManagedContext;
import org.apache.geronimo.concurrent.ManagedContextHandler;
import org.apache.geronimo.concurrent.thread.ManagedRunnable;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.lifecycle.LifecycleListener;

public class StandaloneManagedThreadFactory extends TrackingManagedThreadFactory {
  
    private ManagedContextHandler contextHandler;
    private ModuleLifecycleListener moduleLifecyleListener;  
    private ManagedContext managedContext;
   
    public StandaloneManagedThreadFactory(GeronimoManagedThreadFactory threadFactory,
                                          ManagedContextHandler contextHandler, 
                                          AbstractName moduleName) {
        super(threadFactory);
        this.contextHandler = contextHandler;
        this.moduleLifecyleListener = ModuleLifecycleListener.getModuleLifecycleListener(moduleName);
        this.moduleLifecyleListener.addThreadFactory(this);
        
        // capture context
        this.managedContext = ManagedContext.captureContext(this.contextHandler);
    }
    
    @Override
    public Thread newThread(Runnable runnable) {
        if (!isRunning()) {
            throw new IllegalArgumentException(
                "Component that created this thread factory is no longer running");
        }
                
        Runnable managedRunnable = new ManagedRunnable(runnable, this.managedContext, true);
        Thread thread = super.newThread(managedRunnable);
        return thread;
    }

    protected boolean isRunning() {
        return this.moduleLifecyleListener.isRunning();
    }
          
    protected void shutdown() {
        interruptThreads();
    }
        
    private static class ModuleLifecycleListener implements LifecycleListener {

        private static final Map<AbstractName, ModuleLifecycleListener> listeners = 
            new HashMap<AbstractName, ModuleLifecycleListener>();
        
        private final AbstractName moduleName;
        private boolean running;
        private final List<StandaloneManagedThreadFactory> threadFactories = 
            new ArrayList<StandaloneManagedThreadFactory>();
        
        public ModuleLifecycleListener(AbstractName moduleName) {
            this.moduleName = moduleName;
            this.running = true;
        }
                
        public synchronized void addThreadFactory(StandaloneManagedThreadFactory threadFactory) {
            if (this.running) {
                this.threadFactories.add(threadFactory);
            }
        }
        
        public synchronized void shutdown() {
            for (StandaloneManagedThreadFactory threadFactory : this.threadFactories) {
                try {
                    threadFactory.shutdown();
                } catch (Exception e) {
                    // ignore
                }
            }
            this.threadFactories.clear();
        }
        
        public boolean isRunning() {
            return this.running;
        }
        
        public AbstractName getModuleName() {
            return this.moduleName;
        }
        
        public void failed(AbstractName arg0) {
            this.running = false;
        }

        public void loaded(AbstractName arg0) {
            this.running = false;
        }

        public void running(AbstractName arg0) {
            this.running = true;
        }

        public void starting(AbstractName arg0) {
            this.running = false;
        }

        public void stopped(AbstractName arg0) {
            this.running = false;            
        }

        public void stopping(AbstractName arg0) {
            this.running = false;
            shutdown();
        }

        public void unloaded(AbstractName arg0) { 
            this.running = false;
            shutdown();
            removeModuleLifecycleListener(this.moduleName);
        }
        
        private static synchronized ModuleLifecycleListener getModuleLifecycleListener(AbstractName moduleName) {
            ModuleLifecycleListener listener = listeners.get(moduleName);
            if (listener == null) {
                listener = new ModuleLifecycleListener(moduleName);
                
                // register listener with Kernel
                AbstractNameQuery query = new AbstractNameQuery(moduleName);
                Kernel kernel = KernelRegistry.getSingleKernel();
                kernel.getLifecycleMonitor().addLifecycleListener(listener, query);
                
                listeners.put(moduleName, listener);
            }                       
            return listener;
        }
                
        private static synchronized void removeModuleLifecycleListener(AbstractName moduleName) {
            listeners.remove(moduleName);
        }
        
    }
        
}
