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
import java.util.Collections;
import java.util.List;

import javax.util.concurrent.ManagedThreadFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.concurrent.thread.ManagedThread;
import org.apache.geronimo.concurrent.thread.ThreadLifecycleListener;

public class TrackingManagedThreadFactory implements ManagedThreadFactory, ThreadLifecycleListener {

    private final static Log LOG = LogFactory.getLog(TrackingManagedThreadFactory.class);
    
    protected GeronimoManagedThreadFactory threadFactory;      
    protected List<ManagedThread> threads = 
        Collections.synchronizedList(new ArrayList<ManagedThread>());
    
    public TrackingManagedThreadFactory(GeronimoManagedThreadFactory threadFactory) {       
        this.threadFactory = threadFactory;       
        if (this.threadFactory == null) {
            throw new NullPointerException("threadFactory is null");
        }
    }
    
    public Thread newThread(Runnable runnable) {                
        ManagedThread thread = (ManagedThread)this.threadFactory.newThread(runnable);
        
        // set listener so that this class gets notifications of thread lifecycle events
        thread.setThreadLifecycleListener(this);
        
        this.threads.add(thread);        
        
        LOG.debug("Thread created: " + thread);
        
        return thread;
    }
    
    public void threadStarted(Thread thread) {       
        this.threadFactory.threadStarted(thread);
        
        LOG.debug("Thread started: " + thread);
    }
    
    public void threadStopped(Thread thread) {
        this.threads.remove(thread);
        this.threadFactory.threadStopped(thread);
        
        LOG.debug("Thread stopped: " + thread);
    }
    
    protected List<ManagedThread> getThreadList() {
        synchronized(this.threads) {
            return new ArrayList<ManagedThread>(this.threads);
        }
    }
    
    protected void interruptThreads() {
        synchronized(this.threads) {
            for (Thread thread : this.threads) {
                thread.interrupt();               
            }
        }
    }
                   
}
