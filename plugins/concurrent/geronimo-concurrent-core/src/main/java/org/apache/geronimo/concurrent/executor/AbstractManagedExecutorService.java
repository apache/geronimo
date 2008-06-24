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
 * This class extends java.util.concurrent.ThreadPoolExecutor and borrows some code from 
 * java.util.concurrent.AbstractExecutorService class in Apache Harmony.
 */
package org.apache.geronimo.concurrent.executor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.util.concurrent.ManagedExecutorService;
import javax.util.concurrent.ManagedTaskListener;

import org.apache.geronimo.concurrent.ManagedContext;
import org.apache.geronimo.concurrent.ManagedTaskListenerSupport;
import org.apache.geronimo.concurrent.harmony.ThreadPoolExecutor;
import org.apache.geronimo.concurrent.thread.ManagedRunnable;

public abstract class AbstractManagedExecutorService 
    extends ThreadPoolExecutor 
    implements ManagedExecutorService {
       
    public AbstractManagedExecutorService(int corePoolSize,
                                          int maximumPoolSize,
                                          long keepAliveTime,
                                          TimeUnit unit,
                                          BlockingQueue<Runnable> workQueue,
                                          ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }
              
    protected abstract ManagedContext getManagedContext();
    
    // invokeAny() functions
    
    /**
     * the main mechanics of invokeAny.
     */    
    private <T> T doInvokeAny(Collection<Callable<T>> tasks,
                              boolean timed, 
                              long nanos,
                              ManagedTaskListener listener)
        throws InterruptedException, 
               ExecutionException, 
               TimeoutException {
        if (tasks == null) {
            throw new NullPointerException();
        }
        int ntasks = tasks.size();
        if (ntasks == 0) {
            throw new IllegalArgumentException();
        }
        List<Future<T>> futures= new ArrayList<Future<T>>(ntasks); 
        ManagedTaskListenerSupport listenerSupport = getManagedTaskListenerSupport(listener);
        ManagedContext managedContext = getManagedContext();
        ManagedExecutorCompletionService<T> ecs = 
            new ManagedExecutorCompletionService<T>(this, managedContext, listenerSupport);
        
        // For efficiency, especially in executors with limited
        // parallelism, check to see if previously submitted tasks are
        // done before submitting more of them. This interleaving
        // plus the exception mechanics account for messiness of main
        // loop.

        try {
            // Record exceptions so that if we fail to obtain any
            // result, we can throw the last exception we got.
            ExecutionException ee = null;
            long lastTime = (timed)? System.nanoTime() : 0;
            Iterator<Callable<T>> it = tasks.iterator();

            // Start one task for sure; the rest incrementally
            futures.add(ecs.submit(it.next()));
            --ntasks;
            int active = 1;

            for (;;) {
                Future<T> f = ecs.poll(); 
                if (f == null) {
                    if (ntasks > 0) {
                        --ntasks;
                        futures.add(ecs.submit(it.next()));
                        ++active;
                    } else if (active == 0) { 
                        break;
                    } else if (timed) {
                        f = ecs.poll(nanos, TimeUnit.NANOSECONDS);
                        if (f == null) {
                            throw new TimeoutException();
                        }
                        long now = System.nanoTime();
                        nanos -= now - lastTime;
                        lastTime = now;
                    } else { 
                        f = ecs.take();
                    }
                }
                if (f != null) {
                    --active;
                    try {
                        return f.get();
                    } catch(InterruptedException ie) {
                        throw ie;
                    } catch(ExecutionException eex) {
                        ee = eex;
                    } catch(RuntimeException rex) {
                        ee = new ExecutionException(rex);
                    }
                }
            }    

            if (ee == null) {
                ee = new ExecutionException(null);
            }
            throw ee;

        } finally {
            for (Future<T> f : futures)  {
                f.cancel(true);
            }
        }
    }
        
    public <T> T invokeAny(Collection<Callable<T>> tasks) 
        throws InterruptedException,
               ExecutionException {
        return invokeAny(tasks, null);
    }       
    
    public <T> T invokeAny(Collection<Callable<T>> tasks, 
                           ManagedTaskListener listener)
        throws InterruptedException, 
               ExecutionException {
        try {
            return doInvokeAny(tasks, false, 0, listener);
        } catch (TimeoutException cannotHappen) {
            assert false;
            return null;
        }
    }
  
    public <T> T invokeAny(Collection<Callable<T>> tasks, long timeout, TimeUnit unit)
        throws InterruptedException, 
               ExecutionException, 
               TimeoutException {
        return invokeAny(tasks, timeout, unit, null);
    }
    
    public <T> T invokeAny(Collection<Callable<T>> tasks,
                           long timeout,
                           TimeUnit unit,
                           ManagedTaskListener listener) 
        throws InterruptedException,
               ExecutionException, 
               TimeoutException {
        return doInvokeAny(tasks, true, unit.toNanos(timeout), listener);
    }
  
    // invokeAll() functions
        
    public <T> List<Future<T>> invokeAll(Collection<Callable<T>> tasks) 
        throws InterruptedException {
        return invokeAll(tasks, null);
    }
    
    public <T> List<Future<T>> invokeAll(Collection<Callable<T>> tasks,
                                         ManagedTaskListener listener) 
        throws InterruptedException {
        if (tasks == null) {
            throw new NullPointerException();
        }
        ManagedTaskListenerSupport listenerSupport = getManagedTaskListenerSupport(listener);
        ManagedContext managedContext = getManagedContext();
        List<Future<T>> futures = new ArrayList<Future<T>>(tasks.size());
        boolean done = false;
        try {
            for (Callable<T> task : tasks) {
                ManagedFutureTask<T> future = createManagedTask(task, managedContext, listenerSupport);
                futures.add(future);
                executeTask(future);
            }
            for (Future<T> future : futures) {
                if (!future.isDone()) {
                    try { 
                        future.get(); 
                    } catch(CancellationException ignore) {
                    } catch(ExecutionException ignore) {
                    }
                }
            }
            done = true;
            return futures;
        } finally {
            if (!done) {
                for (Future<T> future : futures) {
                    future.cancel(true);
                }
            }
        }
    }

    public <T> List<Future<T>> invokeAll(Collection<Callable<T>> tasks, 
                                         long timeout, 
                                         TimeUnit unit)
        throws InterruptedException {
        return invokeAll(tasks, timeout, unit, null);
    }
    
    public <T> List<Future<T>> invokeAll(Collection<Callable<T>> tasks,
                                         long timeout,
                                         TimeUnit unit,
                                         ManagedTaskListener listener) 
        throws InterruptedException {
        if (tasks == null || unit == null) {
            throw new NullPointerException();
        }
        ManagedTaskListenerSupport listenerSupport = getManagedTaskListenerSupport(listener);
        ManagedContext managedContext = getManagedContext();
        long nanos = unit.toNanos(timeout);
        List<Future<T>> futures = new ArrayList<Future<T>>(tasks.size());
        boolean done = false;
        try {
            for (Callable<T> task : tasks)  {
                futures.add(createManagedTask(task, managedContext, listenerSupport));
            }
            long lastTime = System.nanoTime();

            // Interleave time checks and calls to execute in case
            // executor doesn't have any/much parallelism.
            for (Future<T> future : futures) {
                executeTask((ManagedFutureTask<T>)future);
                long now = System.nanoTime();
                nanos -= now - lastTime;
                lastTime = now;
                if (nanos <= 0) {
                    return futures;
                }
            }

            for (Future<T> future : futures) {
                if (!future.isDone()) {
                    if (nanos <= 0) 
                        return futures; 
                    try { 
                        future.get(nanos, TimeUnit.NANOSECONDS); 
                    } catch(CancellationException ignore) {
                    } catch(ExecutionException ignore) {
                    } catch(TimeoutException toe) {
                        return futures;
                    }
                    long now = System.nanoTime();
                    nanos -= now - lastTime;
                    lastTime = now;
                }
            }
            done = true;
            return futures;
        } finally {
            if (!done) {
                for (Future<T> f : futures) { 
                    f.cancel(true);
                }
            }
        }    
    }
    
    // execute and submit functions
    
    public void execute(Runnable task) {
        if (task == null) {
            throw new NullPointerException();
        }
        ManagedRunnable managedRunnable = new ManagedRunnable(task, getManagedContext());
        super.execute(managedRunnable);
    }
    
    public <T> Future<T> submit(Callable<T> task) {
        return submit(task, null);
    }

    public Future<?> submit(Runnable task) {
        return submit(task, null, null);
    }

    public <T> Future<T> submit(Runnable task, T result) {
        return submit(task, result, null);
    }
    
    public Future<?> submit(Runnable task, ManagedTaskListener listener) {
        return submit(task, null, listener);
    }

    public <T> Future<T> submit(Runnable task, T result, ManagedTaskListener listener) {
        if (task == null) {
            throw new NullPointerException();
        }
        
        ManagedTaskListenerSupport listenerSupport = getManagedTaskListenerSupport(listener);
        ManagedContext managedContext = getManagedContext();
        ManagedFutureTask<T> managedFuture = new ManagedFutureTask<T>(task, 
                                                                      result,
                                                                      managedContext,
                                                                      listenerSupport);       
        executeTask(managedFuture);
        
        return managedFuture;
    }
        
    public <T> Future<T> submit(Callable<T> task, ManagedTaskListener listener) {
        if (task == null) {
            throw new NullPointerException();
        }
        
        ManagedTaskListenerSupport listenerSupport = getManagedTaskListenerSupport(listener);
        ManagedContext managedContext = getManagedContext();
        ManagedFutureTask<T> managedFuture = new ManagedFutureTask<T>(task, 
                                                                      managedContext,
                                                                      listenerSupport);             
        executeTask(managedFuture);
        
        return managedFuture;
    }
        
    private <T> ManagedFutureTask<T> createManagedTask(Callable<T> task, 
                                                       ManagedContext managedContext,
                                                       ManagedTaskListenerSupport listenerSupport) {
        ManagedFutureTask<T> managedFuture = 
            new ManagedFutureTask<T>(task, managedContext, listenerSupport);
        return managedFuture;
    }
    
    protected ManagedTaskListenerSupport getManagedTaskListenerSupport(ManagedTaskListener listener) {
        return (listener == null) ? null : new ManagedTaskListenerSupport(this, listener);
    }
        
    /* 
     * This is called by invokeAll/invokeAny/submit functions.     
     */
    protected void executeTask(ManagedFutureTask<?> task) {        
        ManagedTaskListenerSupport listenerSupport = task.getManagedTaskListenerSupport();
        if (listenerSupport != null) {
            listenerSupport.taskSubmitted(task);
        }
        try {
            super.execute(task);
        } catch (RejectedExecutionException exception) {
            if (listenerSupport != null) {
                listenerSupport.taskDone(task, exception);
            }
            throw exception;
        }
    }
     
}
