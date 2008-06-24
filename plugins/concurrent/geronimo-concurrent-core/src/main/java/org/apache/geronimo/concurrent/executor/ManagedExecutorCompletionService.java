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

/*
 * This class is based on and borrows code from java.util.concurrent.ExecutorCompletionService 
 * class in Apache Harmony.
 */
package org.apache.geronimo.concurrent.executor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.geronimo.concurrent.ManagedContext;
import org.apache.geronimo.concurrent.ManagedTaskListenerSupport;

public class ManagedExecutorCompletionService<V> {
    private final AbstractManagedExecutorService executor;
    private final BlockingQueue<Future<V>> completionQueue;
    
    private ManagedContext managedContext;
    private ManagedTaskListenerSupport listenerSupport;
    
    /**
     * FutureTask extension to enqueue upon completion
     */
    private class QueueingManagedFuture extends ManagedFutureTask<V> {
        QueueingManagedFuture(Callable<V> callable, 
                              ManagedContext managedContext,
                              ManagedTaskListenerSupport listenerSupport) { 
            super(callable, managedContext, listenerSupport);
        }
        protected void done() { completionQueue.add(this); }
    }

    public ManagedExecutorCompletionService(AbstractManagedExecutorService executor,
                                            ManagedContext managedContext,
                                            ManagedTaskListenerSupport listenerSupport) {
        this(executor, new LinkedBlockingQueue<Future<V>>(), managedContext, listenerSupport);
    }

    public ManagedExecutorCompletionService(AbstractManagedExecutorService executor,
                                            BlockingQueue<Future<V>> completionQueue,
                                            ManagedContext managedContext,
                                            ManagedTaskListenerSupport listenerSupport) {
        if (executor == null || completionQueue == null) {
            throw new NullPointerException();
        }
        this.executor = executor;
        this.completionQueue = completionQueue;
        this.managedContext = managedContext;
        this.listenerSupport = listenerSupport;
    }

    public Future<V> submit(Callable<V> task) {
        if (task == null) {
            throw new NullPointerException();
        }
        
        QueueingManagedFuture managedFuture = 
            new QueueingManagedFuture(task, this.managedContext, this.listenerSupport);        
        this.executor.executeTask(managedFuture);
        
        return managedFuture;
    }

    public Future<V> take() throws InterruptedException {
        return completionQueue.take();
    }

    public Future<V> poll() {
        return completionQueue.poll();
    }

    public Future<V> poll(long timeout, TimeUnit unit) throws InterruptedException {
        return completionQueue.poll(timeout, unit);
    }
    
}

