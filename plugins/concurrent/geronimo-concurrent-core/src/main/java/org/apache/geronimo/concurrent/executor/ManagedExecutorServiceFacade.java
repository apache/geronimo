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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.util.concurrent.ManagedExecutorService;
import javax.util.concurrent.ManagedTaskListener;

/**
 * A facade class for ManagedExecutorService. If facade is configured as serverManaged
 * all lifecycle operations of this class will throw {@link #IllegalArgumentException}. 
 */
public class ManagedExecutorServiceFacade 
    implements ManagedExecutorService {
    
    protected ManagedExecutorService executor;
    protected boolean serverManaged;

    public ManagedExecutorServiceFacade(ManagedExecutorService executor, 
                                        boolean serverManaged) {
        if (executor == null) {
            throw new NullPointerException();
        }
        this.executor = executor;
        this.serverManaged = serverManaged;
    }
    
    public <T> List<Future<T>> invokeAll(Collection<Callable<T>> tasks, 
                                         ManagedTaskListener listener)
        throws InterruptedException {
        return this.executor.invokeAll(tasks, listener);
    }

    public <T> List<Future<T>> invokeAll(Collection<Callable<T>> tasks,
                                         long timeout,
                                         TimeUnit unit,
                                         ManagedTaskListener listener) 
        throws InterruptedException {
        return this.executor.invokeAll(tasks, timeout, unit, listener);
    }

    public <T> T invokeAny(Collection<Callable<T>> tasks, 
                           ManagedTaskListener listener)
        throws InterruptedException, 
               ExecutionException {
        return this.executor.invokeAny(tasks, listener);
    }

    public <T> T invokeAny(Collection<Callable<T>> tasks,
                           long timeout,
                           TimeUnit unit,
                           ManagedTaskListener listener) 
        throws InterruptedException,
               ExecutionException, 
               TimeoutException {
        return this.executor.invokeAny(tasks, timeout, unit, listener);
    }

    public Future<?> submit(Runnable command, ManagedTaskListener listener) {
        return this.executor.submit(command, listener);
    }

    public <T> Future<T> submit(Callable<T> callable, ManagedTaskListener listener) {
        return this.executor.submit(callable, listener);
    }

    public <T> Future<T> submit(Runnable command, T result, ManagedTaskListener listener) {
        return this.executor.submit(command, result, listener);
    }

    public <T> List<Future<T>> invokeAll(/*replace*/Collection<Callable<T>> tasks) 
        throws InterruptedException {
        return this.executor.invokeAll(tasks);
    }

    public <T> List<Future<T>> invokeAll(/*replace*/Collection<Callable<T>> tasks, 
                                         long timeout, 
                                         TimeUnit unit)
        throws InterruptedException {
        return this.executor.invokeAll(tasks, timeout, unit);
    }

    public <T> T invokeAny(/*replace*/Collection<Callable<T>> tasks) 
        throws InterruptedException,
               ExecutionException {
        return this.executor.invokeAny(tasks);
    }

    public <T> T invokeAny(/*replace*/Collection<Callable<T>> tasks, 
                           long timeout, 
                           TimeUnit unit)
        throws InterruptedException, 
               ExecutionException, 
               TimeoutException {
        return this.executor.invokeAny(tasks, timeout, unit);
    }

    public <T> Future<T> submit(Callable<T> task) {
        return this.executor.submit(task);
    }

    public Future<?> submit(Runnable task) {
        return this.executor.submit(task);
    }

    public <T> Future<T> submit(Runnable task, T result) {
        return this.executor.submit(task, result);
    }

    public void execute(Runnable command) {
        this.executor.execute(command);
    }
        
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        if (this.serverManaged) {
            throw new IllegalStateException();
        } else {
            return this.executor.awaitTermination(timeout, unit);
        }
    }
    
    public boolean isShutdown() {
        if (this.serverManaged) {
            throw new IllegalStateException();
        } else {
            return this.executor.isShutdown();
        }
    }

    public boolean isTerminated() {
        if (this.serverManaged) {
            throw new IllegalStateException();
        } else {
            return this.executor.isTerminated();
        }
    }

    public void shutdown() {
        if (this.serverManaged) {
            throw new IllegalStateException();
        } else {
            this.executor.shutdown();
        }
    }

    public List<Runnable> shutdownNow() {
        if (this.serverManaged) {
            throw new IllegalStateException();
        } else {
            return this.executor.shutdownNow();
        }
    }
   
}
