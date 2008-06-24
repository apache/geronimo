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
package org.apache.geronimo.concurrent.executor;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.concurrent.ManagedContext;
import org.apache.geronimo.concurrent.ManagedTask;
import org.apache.geronimo.concurrent.ManagedTaskListenerSupport;
import org.apache.geronimo.concurrent.ManagedTaskUtils;
import org.apache.geronimo.concurrent.harmony.FutureTask;
import org.apache.geronimo.concurrent.thread.ManagedThread;

public class ManagedFutureTask<V> extends FutureTask<V> implements ManagedTask {

    protected final static Log LOG = LogFactory.getLog(ManagedFutureTask.class);
    
    protected ManagedContext managedContext;
    protected ManagedTaskListenerSupport listenerSupport;
    
    private Object task;
    private boolean associateTaskWithThread;
    protected boolean setContextOnRun = true;
        
    public ManagedFutureTask(Callable<V> callable, 
                             ManagedContext managedContext,
                             ManagedTaskListenerSupport listener) {                             
        super(callable);
        this.task = callable;
        this.managedContext = managedContext;
        this.listenerSupport = listener;
        // always associate the task with the thread
        this.associateTaskWithThread = true;
        init();
    }
    
    public ManagedFutureTask(Runnable runnable, V result,
                             ManagedContext managedContext,
                             ManagedTaskListenerSupport listener) {                             
        super(runnable, result);
        this.task = runnable;
        this.managedContext = managedContext;
        this.listenerSupport = listener;
        // if the runnable is ManagedFutureTask, do not associate it with the thread
        // in this instance since the runnable will already do that 
        this.associateTaskWithThread = !(runnable instanceof ManagedFutureTask);
        init();
    }
    
    private void init() {
        if (this.listenerSupport != null && this.managedContext == null) {
            throw new IllegalArgumentException("ManagedContext must be non-null if listener is specifed");
        }
    }
    
    boolean isSetContextOnRun() {
        return this.setContextOnRun;
    }

    /**
     * Sets if the context should be applied when the {@link #run() method is executed.
     * By default and in the server-managed executors the context is applied. 
     * In component-managed executors the context is not applied as the right context is
     * already set by the thread.
     */
    void setSetContextOnRun(boolean setContextOnRun) {
        this.setContextOnRun = setContextOnRun;
    }

    public ManagedTaskListenerSupport getManagedTaskListenerSupport() {
        return this.listenerSupport;
    }
    
    public ManagedContext getManagedContext() {
        return this.managedContext;
    }
    
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {        
        boolean result = super.cancel(mayInterruptIfRunning);
        if (result && this.listenerSupport != null) {
            // always set the context
            Map<String, Object> threadContext = this.managedContext.set();
            try {
                this.listenerSupport.taskAborted(this, new CancellationException());
            } finally {
                this.managedContext.unset(threadContext);
            }
        }
        return result;        
    }

    @Override
    public void run() {
        if (this.setContextOnRun && this.managedContext != null) {
            Map<String, Object> threadContext = null;
            try {
                threadContext = this.managedContext.set();
            } catch (RuntimeException e) {
                LOG.warn("Failed to apply context to thread for task " + this + ": " + 
                         e.getMessage() + ". Cancelling task");        
                cancelFutureTask(false);
                return;
            }
            try {
                super.run();
            } finally {
                this.managedContext.unset(threadContext);
            }
        } else {
            super.run();
        }
    }
    
    protected boolean cancelFutureTask(boolean mayInterruptIfRunning) {
        return super.cancel(mayInterruptIfRunning);
    }
    
    @Override
    protected void taskStart() {
        if (this.listenerSupport != null) {
            this.listenerSupport.taskStarting(this);
        }
        
        if (this.associateTaskWithThread) {
            // associate task with the thread
            Thread thread = Thread.currentThread();
            if (thread instanceof ManagedThread) {
                ManagedThread managedThread = (ManagedThread)thread;
                managedThread.startTask(this);
            } else {
                LOG.warn("taskStart was not called on ManagedThread: " + thread);
            }
        }
    }
    
    @Override
    protected void taskDone(Throwable exception) {
        if (this.listenerSupport != null) {
            this.listenerSupport.taskDone(this, exception);
        }
        
        if (this.associateTaskWithThread) {
            // de-associate task with the thread
            Thread thread = Thread.currentThread();
            if (thread instanceof ManagedThread) {
                ManagedThread managedThread = (ManagedThread)thread;
                managedThread.endTask();
            }
        }
    }

    public boolean cancel() {
        return cancel(true);
    }

    public String getIdentityDescription(String locale) {
        return ManagedTaskUtils.getTaskDescription(this.task, locale);
    }

    public String getIdentityName() {
        return ManagedTaskUtils.getTaskName(this.task);
    }
    
}
