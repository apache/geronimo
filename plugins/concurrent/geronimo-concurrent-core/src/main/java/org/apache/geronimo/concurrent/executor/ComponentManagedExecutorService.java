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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.util.concurrent.ManagedTaskListener;
import javax.util.concurrent.ManagedThreadFactory;

import org.apache.geronimo.concurrent.ManagedContext;
import org.apache.geronimo.concurrent.ManagedContextHandler;
import org.apache.geronimo.concurrent.thread.ManagedThreadFactoryUtils;

/**
 * Component-managed implementation of {@link ManagedExecutorService}. 
 * In component-managed implementation {@link ManagedTaskListener} callbacks
 * must execute within the context of the thread that created the executor. 
 * <BR>
 * Some key methods are overridden to set the right context on the thread submitting
 * the tasks. 
 */
public class ComponentManagedExecutorService 
    extends AbstractManagedExecutorService {

    private ManagedContext managedContext;

    public ComponentManagedExecutorService(int corePoolSize,
                                           int maximumPoolSize,
                                           long keepAliveTime,
                                           TimeUnit unit,
                                           BlockingQueue<Runnable> workQueue,
                                           ManagedThreadFactory threadFactory,
                                           ManagedContextHandler contextHandler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, 
              workQueue, threadFactory);
        
        // save context now
        this.managedContext = ManagedContext.captureContext(contextHandler);
        
        setThreadFactory(ManagedThreadFactoryUtils.createEmbeddedThreadFactory(threadFactory, 
                                                                               this.managedContext));        
    }
                
    protected ManagedContext getManagedContext() {
        return this.managedContext;
    }
            
    protected void executeTask(ManagedFutureTask<?> task) { 
        task.setSetContextOnRun(false);
        super.executeTask(task);
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
    
    public <T> Future<T> submit(Runnable task, T result, ManagedTaskListener listener) {
        if (task == null) {
            throw new NullPointerException();
        }
        
        Map<String, Object> threadContext = this.managedContext.set();
        try {
            return super.submit(task, result, listener);
        } finally {
            this.managedContext.unset(threadContext);
        }
    }
        
    public <T> Future<T> submit(Callable<T> task, ManagedTaskListener listener) {
        if (task == null) {
            throw new NullPointerException();
        }

        Map<String, Object> threadContext = this.managedContext.set();
        try {
            return super.submit(task, listener);
        } finally {
            this.managedContext.unset(threadContext); 
        }
    }
        
}
