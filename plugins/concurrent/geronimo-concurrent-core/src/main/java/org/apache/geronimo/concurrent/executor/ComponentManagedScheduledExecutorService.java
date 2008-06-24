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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.util.concurrent.ManagedScheduledExecutorService;
import javax.util.concurrent.ManagedTaskListener;
import javax.util.concurrent.ManagedThreadFactory;
import javax.util.concurrent.Trigger;

import org.apache.geronimo.concurrent.ManagedContext;
import org.apache.geronimo.concurrent.ManagedContextHandler;
import org.apache.geronimo.concurrent.thread.ManagedThreadFactoryUtils;

/**
 * Component-managed implementation of {@link ManagedScheduledExecutorService}.
 * In component-managed implementation {@link ManagedTaskListener} callbacks
 * must execute within the context of the thread that created the executor. <BR>
 * Some key methods are overridden to set the right context on the thread
 * submitting the tasks.
 */
public class ComponentManagedScheduledExecutorService 
    extends AbstractManagedScheduledExecutorService {

    private ManagedContext managedContext;

    public ComponentManagedScheduledExecutorService(int corePoolSize,
                                                    ManagedThreadFactory threadFactory,
                                                    ManagedContextHandler contextHandler) {
        super(corePoolSize, threadFactory);
        
        // save context now
        this.managedContext = ManagedContext.captureContext(contextHandler);
        
        setThreadFactory(ManagedThreadFactoryUtils.createEmbeddedThreadFactory(threadFactory, 
                                                                               this.managedContext));        
    }
  
    protected ManagedContext getManagedContext() {
        return this.managedContext;
    }
    
    protected void preExecute(ManagedFutureTask<?> task) {
        task.setSetContextOnRun(false);
        super.preExecute(task);
    }
    
    /*
     * TODO: some of these functions could be optimized a bit. When listener == null
     *       the context does not have to be set on the current thread since 
     *       there is no listener methods to call.
     */
    
    public <T> T invokeAny(Collection<Callable<T>> tasks, 
                           ManagedTaskListener listener)
        throws InterruptedException, 
               ExecutionException {
        if (tasks == null) {
            throw new NullPointerException();
        }
        
        Map<String, Object> threadContext = this.managedContext.set();
        try {
            return super.invokeAny(tasks, listener);
        } finally {
            this.managedContext.unset(threadContext);
        }
    }
    
    public <T> T invokeAny(Collection<Callable<T>> tasks,
                           long timeout,
                           TimeUnit unit,
                           ManagedTaskListener listener) 
        throws InterruptedException,
               ExecutionException, 
               TimeoutException {
        if (tasks == null) {
            throw new NullPointerException();
        }
        
        Map<String, Object> threadContext = this.managedContext.set();
        try {
            return super.invokeAny(tasks, timeout, unit, listener);
        } finally {
            this.managedContext.unset(threadContext);
        }
    }
    public <T> List<Future<T>> invokeAll(Collection<Callable<T>> tasks, 
                                         ManagedTaskListener listener)
            throws InterruptedException {
        if (tasks == null) {
            throw new NullPointerException();
        }
        
        Map<String, Object> threadContext = this.managedContext.set();
        try {
            return super.invokeAll(tasks, listener);
        } finally {
            this.managedContext.unset(threadContext);
        }
    }

    public <T> List<Future<T>> invokeAll(Collection<Callable<T>> tasks,
                                         long timeout,
                                         TimeUnit unit,
                                         ManagedTaskListener listener) throws InterruptedException {
        if (tasks == null || unit == null) {
            throw new NullPointerException();
        }
        
        Map<String, Object> threadContext = this.managedContext.set();
        try {
            return super.invokeAll(tasks, timeout, unit, listener);
        } finally {
            this.managedContext.unset(threadContext);
        }
    }
    
    public <V> ScheduledFuture<V> schedule(Callable<V> callable,
                                           long delay,
                                           TimeUnit unit,
                                           ManagedTaskListener listener) {
        if (callable == null || unit == null) {
            throw new NullPointerException();
        }

        Map<String, Object> threadContext = this.managedContext.set();
        try {
            return super.schedule(callable, delay, unit, listener);
        } finally {
            this.managedContext.unset(threadContext);
        }
    }

    public <V> ScheduledFuture<V> schedule(Callable<V> callable,
                                           Trigger trigger,
                                           ManagedTaskListener listener) {
        if (callable == null || trigger == null) {
            throw new NullPointerException();
        }

        Map<String, Object> threadContext = this.managedContext.set();
        try {
            return super.schedule(callable, trigger, listener);
        } finally {
            this.managedContext.unset(threadContext);
        }
    }
          
    protected <T> ScheduledFuture<T> schedule(Runnable command,
                                              T result,
                                              long delay,
                                              TimeUnit unit,
                                              ManagedTaskListener listener) {
        if (command == null || unit == null) {
            throw new NullPointerException();
        }

        Map<String, Object> threadContext = this.managedContext.set();
        try {
            return super.schedule(command, result, delay, unit, listener);
        } finally {
            this.managedContext.unset(threadContext);
        }
    }

    public ScheduledFuture<?> schedule(Runnable command,
                                       Trigger trigger,
                                       ManagedTaskListener listener) {
        if (command == null || trigger == null) {
            throw new NullPointerException();
        }

        Map<String, Object> threadContext = this.managedContext.set();
        try {
            return super.schedule(command, trigger, listener);
        } finally {
            this.managedContext.unset(threadContext);
        }
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                  long initialDelay,
                                                  long period,
                                                  TimeUnit unit,
                                                  ManagedTaskListener listener) {
        if (command == null || unit == null) {
            throw new NullPointerException();
        }
        if (period <= 0) {
            throw new IllegalArgumentException();
        }

        Map<String, Object> threadContext = this.managedContext.set();
        try {
            return super.scheduleAtFixedRate(command, initialDelay, period, unit, listener);
        } finally {
            this.managedContext.unset(threadContext);
        }
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
                                                     long initialDelay,
                                                     long delay,
                                                     TimeUnit unit,
                                                     ManagedTaskListener listener) {
        if (command == null || unit == null) {
            throw new NullPointerException();
        }
        if (delay <= 0) {
            throw new IllegalArgumentException();
        }

        Map<String, Object> threadContext = this.managedContext.set();
        try {
            return super.scheduleAtFixedRate(command, initialDelay, delay, unit, listener);
        } finally {
            this.managedContext.unset(threadContext);
        }
    }

}
