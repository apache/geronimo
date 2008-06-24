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

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.concurrent.ManagedTask;

public class ManagedThread extends Thread {

    private final static Log LOG = LogFactory.getLog(ManagedThread.class);
    
    public enum TaskState {
        RUNNING, HUNG, CANCELLED, RELEASED, DONE
    }
    
    // thread-specific properties
    protected ThreadLifecycleListener threadLifecycleListener;            
    protected long hungTaskThreshold; 
    
    // task-specific properties
    protected String taskIdentityDescription;    
    protected long startTime;
    protected ManagedTask task; 
    protected TaskState state;
       
    public ManagedThread(Runnable runnable, 
                         ThreadLifecycleListener threadLifecycleListener) {
        super(runnable);
        setThreadLifecycleListener(threadLifecycleListener);
    }
    
    public ManagedThread(ThreadGroup group,
                         Runnable runnable, 
                         String name,
                         ThreadLifecycleListener threadLifecycleListener) {
        super(group, runnable, name);
        setThreadLifecycleListener(threadLifecycleListener);
    }
    
    public void setThreadLifecycleListener(ThreadLifecycleListener threadLifecycleListener) {
        this.threadLifecycleListener = threadLifecycleListener;
    }
    
    /**
     * This function is assumed to be called on the same thread that is executing
     * the task and after the right context is associated with the thread.
     */
    public void startTask(ManagedTask task) {        
        if (Thread.currentThread() != this) {
            throw new IllegalStateException("startTask called from invalid thread");
        }             
        if (this.task != null) {
            throw new IllegalStateException("Another task is already associated with this thread");
        }
        
        this.task = task;    
        
        setStartState();
        
        this.startTime = System.currentTimeMillis();       
        this.taskIdentityDescription = 
            getTaskIdentityDescription(getThreadLocale().toString());
                                              
        LOG.debug("Start task: " + this.taskIdentityDescription);
    }
    
    /**
     * This function is assumed to be called on the same thread as when 
     * {@link #startTask(Object)} was first called.
     */
    public void endTask() {
        if (Thread.currentThread() != this) {
            throw new IllegalStateException("endTask called from invalid thread");
        }
        
        LOG.debug("End task: " + this.taskIdentityDescription);

        setEndState();

        this.task = null;        
        this.startTime = 0;
        this.taskIdentityDescription = null;
    }
         
    public void start() {
        super.start();
        this.threadLifecycleListener.threadStarted(this);
    }
    
    public void run() {
        try {
            super.run();
        } finally {
            this.threadLifecycleListener.threadStopped(this);
        }
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
        
    protected Locale getThreadLocale() {
        return Locale.getDefault();
    }
                
    public long getThreadID() {
        return getId();
    }
    
    public String getThreadName() {
        return getName();
    }
    
    public synchronized long getTaskRunTime() {
        return (isTaskRunning()) ? getRunTime() : 0;
    }
        
    protected long getRunTime() {
        return System.currentTimeMillis() - this.startTime;
    }
    
    protected boolean isHung() {
        if (this.hungTaskThreshold > 0) {
            return (getRunTime() > this.hungTaskThreshold);
        } else {
            return false;
        }
    }
    
    protected synchronized void setState(TaskState newState) {
        this.state = newState;        
    }
    
    private synchronized void setStartState() {
        setState(TaskState.RUNNING);
    }
    
    private synchronized void setEndState() {
        if (this.state == TaskState.RUNNING) {
            setState(TaskState.DONE);
        } else if (this.state == TaskState.CANCELLED ||
                   this.state == TaskState.HUNG) {
            setState(TaskState.RELEASED);
        }
    }
    
    public synchronized boolean isTaskRunning() {
        return (this.state == TaskState.RUNNING || 
                this.state == TaskState.HUNG ||
                this.state == TaskState.CANCELLED);
    }
    
    public synchronized boolean isTaskHung() {
        return (this.state == TaskState.HUNG);
    }
    
    public synchronized boolean isTaskCancelled() {
        return (this.state == TaskState.CANCELLED);
    }
    
    /*
     * Should be called periodically to check if the task is hung
     */
    public synchronized void updateState() {
        if (this.state == TaskState.RUNNING) {
            if (isHung()) {
                setState(TaskState.HUNG);
            }
        }
    }
    
    /**
     * Cancel hung task.    
     */
    public synchronized boolean cancelTask() {
        if (this.state == TaskState.HUNG) {
            boolean result = this.task.cancel();
            setState(TaskState.CANCELLED);
            return result; 
        } else {
            return false;
        }
    }
    
    public String getTaskIdentityDescription() {
        return (isTaskRunning()) ? this.taskIdentityDescription : null;
    }
    
    public String getTaskIdentityDescription(String localeStr) {
        return (isTaskRunning()) ? this.task.getIdentityDescription(localeStr) : null;            
    }
    
    public String getTaskIdentityName() {
        return (isTaskRunning()) ? this.task.getIdentityName() : null;
    }
           
}
