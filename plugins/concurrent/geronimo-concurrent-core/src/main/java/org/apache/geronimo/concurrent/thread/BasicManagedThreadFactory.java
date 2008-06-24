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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BasicManagedThreadFactory extends AbstractManagedThreadFactory {
   
    private final static Log LOG = LogFactory.getLog(BasicManagedThreadFactory.class);
          
    protected List<ManagedThread> threads = 
        Collections.synchronizedList(new ArrayList<ManagedThread>());
    protected boolean stopped = false;
                             
    @Override
    public Thread newThread(Runnable runnable) {
        ManagedThread thread = (ManagedThread)super.newThread(runnable);
                
        this.threads.add(thread);
        
        LOG.debug("Added thread: " + thread);
                
        return thread;
    }
    
    @Override
    public void threadStopped(Thread thread) {        
        this.threads.remove(thread);
        
        LOG.debug("Removed thread: " + thread);
    }
    
    protected void updateStatus() {
        List<ManagedThread> threadList = getThreadList();
        for (ManagedThread thread : threadList) {
            thread.updateState();
        }
    }
    
    protected List<ManagedThread> getThreadList() {
        synchronized(this.threads) {
            return new ArrayList<ManagedThread>(this.threads);
        }
    }
    
    public boolean isRunning() {
        return !this.stopped;
    }
    
    public void shutdown() {
        this.stopped = true;
        synchronized(this.threads) {
            for (Thread thread : this.threads) {
                thread.interrupt();               
            }
        }
    }
        
}
