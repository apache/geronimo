/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.geronimo.concurrent.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.geronimo.concurrent.ManagedContext;
import org.apache.geronimo.concurrent.ManagedContextHandler;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.lifecycle.LifecycleListener;

public class GeronimoManagedContext extends ManagedContext {

    private ModuleLifecycleListener moduleLifecyleListener;

    public GeronimoManagedContext(ManagedContextHandler contextHandler,
                                  Map<String, Object> capturedContext,
                                  AbstractName moduleName) {
        super(contextHandler, capturedContext);
        
        if (moduleName != null) {
            this.moduleLifecyleListener = ModuleLifecycleListener.getModuleLifecycleListener(moduleName);
        }
    }

    public boolean isValid() {
        return (this.moduleLifecyleListener == null) ? true : this.moduleLifecyleListener.isRunning();
    }
    
    private static class ModuleLifecycleListener implements LifecycleListener {

        private static Map<AbstractName, ModuleLifecycleListener> listeners = 
            new HashMap<AbstractName, ModuleLifecycleListener>();
        
        private final AbstractName moduleName;
        private boolean running;
        
        public ModuleLifecycleListener(AbstractName moduleName) {
            this.moduleName = moduleName;
            this.running = true;
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
        }

        public void unloaded(AbstractName arg0) { 
            this.running = false;
            removeModuleLifecycleListener(this.moduleName);
        }
        
        public static synchronized ModuleLifecycleListener getModuleLifecycleListener(AbstractName moduleName) {
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
