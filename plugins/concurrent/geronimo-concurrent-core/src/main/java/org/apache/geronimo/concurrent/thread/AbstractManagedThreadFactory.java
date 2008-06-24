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

import java.util.concurrent.atomic.AtomicInteger;

import javax.util.concurrent.ManagedThreadFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractManagedThreadFactory 
    implements ManagedThreadFactory, ThreadLifecycleListener {

    private final static Log LOG = LogFactory.getLog(AbstractManagedThreadFactory.class);
    
    private static final AtomicInteger groupNumber = new AtomicInteger(1);
        
    private final AtomicInteger threadNumber = new AtomicInteger(1);    
    private int threadPriority = Thread.NORM_PRIORITY;   
    private boolean daemonThread = false;
    private long hungTaskThreshold = 0;
    protected ThreadGroup threadGroup;       
                
    public AbstractManagedThreadFactory() { 
        setThreadGroup(null);
    }
    
    public ThreadGroup getThreadGroup() {
        return this.threadGroup;
    }
    
    public void setThreadGroup(String groupName) {
        this.threadGroup = new ThreadGroup(getTheadGroupName(groupName));       
    }
    
    private static String getTheadGroupName(String groupName) {
        if (groupName == null) {
            return "ManagedThread-g" + groupNumber.getAndIncrement();
        } else {
            return groupName;
        }
    }
    
    public int getThreadPriority() {
        return this.threadPriority;
    }

    public void setThreadPriority(int threadPriority) {
        if (threadPriority < Thread.MIN_PRIORITY ||
            threadPriority > Thread.MAX_PRIORITY) {
            throw new IllegalArgumentException("Invalid thread priority: " + threadPriority);
        }
        this.threadPriority = threadPriority;
    }

    public boolean isDaemonThread() {
        return this.daemonThread;
    }

    public void setDaemonThread(boolean daemonThread) {
        this.daemonThread = daemonThread;
    }
        
    public long getHungTaskThreshold() {
        return this.hungTaskThreshold;
    }

    public void setHungTaskThreshold(long hungTaskThreshold) {
        if (hungTaskThreshold < 0) {
            throw new IllegalArgumentException("Threshold must be zero or greater");
        }
        this.hungTaskThreshold = hungTaskThreshold;
    }
        
    public Thread newThread(Runnable runnable) {
        if (!isRunning()) {
            throw new IllegalArgumentException(
                  "Component that created this thread factory is no longer running");
        }
        
        String name = this.threadGroup.getName() + "-t" + this.threadNumber.getAndIncrement();        
        ManagedThread thread = createThread(this.threadGroup, runnable, name);  
        thread.setPriority(this.threadPriority);
        thread.setDaemon(this.daemonThread);
        thread.setHungTaskThreshold(this.hungTaskThreshold);
                        
        LOG.debug("Created thread: " + thread);
        
        return thread;
    }

    protected ManagedThread createThread(ThreadGroup group, Runnable runnable, String name) {
        return new ManagedThread(group, runnable, name, this);
    }
    
    public void threadStarted(Thread thread) {        
    }
    
    public void threadStopped(Thread thread) {        
    }
        
    public abstract boolean isRunning();
            
    public abstract void shutdown(); 
        
}
