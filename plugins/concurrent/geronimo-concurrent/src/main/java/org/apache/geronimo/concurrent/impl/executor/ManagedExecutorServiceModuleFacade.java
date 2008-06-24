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
package org.apache.geronimo.concurrent.impl.executor;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.util.concurrent.ManagedExecutorService;
import javax.util.concurrent.ManagedTaskListener;

import org.apache.geronimo.concurrent.executor.ManagedExecutorServiceFacade;
import org.apache.geronimo.concurrent.impl.ModuleContext;
import org.apache.geronimo.gbean.AbstractName;

public class ManagedExecutorServiceModuleFacade extends ManagedExecutorServiceFacade {
            
    private AbstractName moduleID;

    public ManagedExecutorServiceModuleFacade(ManagedExecutorService executor,
                                              AbstractName moduleID) {
        super(executor, true);
        this.moduleID = moduleID;
    }
    
    protected Object before() {
        return ModuleContext.setCurrentModule(this.moduleID);
    }
    
    protected void after(Object obj) {
        ModuleContext.setCurrentModule((AbstractName)obj);
    }
        
    public <T> List<Future<T>> invokeAll(Collection<Callable<T>> tasks, 
                                         ManagedTaskListener listener)
        throws InterruptedException {
        Object rs = before();
        try {
            return this.executor.invokeAll(tasks, listener);
        } finally {
            after(rs);
        }
    }

    public <T> List<Future<T>> invokeAll(Collection<Callable<T>> tasks,
                                         long timeout,
                                         TimeUnit unit,
                                         ManagedTaskListener listener) 
        throws InterruptedException {
        Object rs = before();
        try {
            return this.executor.invokeAll(tasks, timeout, unit, listener);
        } finally {
            after(rs);
        }
    }

    public <T> T invokeAny(Collection<Callable<T>> tasks, 
                           ManagedTaskListener listener)
        throws InterruptedException, 
               ExecutionException {
        Object rs = before();
        try {
            return this.executor.invokeAny(tasks, listener);
        } finally {
            after(rs);
        }
    }

    public <T> T invokeAny(Collection<Callable<T>> tasks,
                           long timeout,
                           TimeUnit unit,
                           ManagedTaskListener listener) 
        throws InterruptedException,
               ExecutionException, 
               TimeoutException {
        Object rs = before();
        try {
            return this.executor.invokeAny(tasks, timeout, unit, listener);
        } finally {
            after(rs);
        }           
    }

    public Future<?> submit(Runnable command, ManagedTaskListener listener) {
        Object rs = before();
        try {            
            return this.executor.submit(command, listener);
        } finally {
            after(rs);
        }      
    }

    public <T> Future<T> submit(Callable<T> callable, ManagedTaskListener listener) {
        Object rs = before();
        try {
            return this.executor.submit(callable, listener);
        } finally {
            after(rs);
        } 
    }

    public <T> Future<T> submit(Runnable command, T result, ManagedTaskListener listener) {            
        Object rs = before();
        try {
            return this.executor.submit(command, result, listener);
        } finally {
            after(rs);
        } 
    }

    public <T> List<Future<T>> invokeAll(Collection<Callable<T>> tasks) 
        throws InterruptedException {
        Object rs = before();
        try {
            return this.executor.invokeAll(tasks);
        } finally {
            after(rs);
        } 
    }

    public <T> List<Future<T>> invokeAll(Collection<Callable<T>> tasks, 
                                         long timeout, 
                                         TimeUnit unit)
        throws InterruptedException {        
        Object rs = before();
        try {
            return this.executor.invokeAll(tasks, timeout, unit);
        } finally {
            after(rs);
        } 
    }

    public <T> T invokeAny(Collection<Callable<T>> tasks) 
        throws InterruptedException,
               ExecutionException {
        Object rs = before();
        try {
            return this.executor.invokeAny(tasks);
        } finally {
            after(rs);
        } 
    }

    public <T> T invokeAny(Collection<Callable<T>> tasks, 
                           long timeout, 
                           TimeUnit unit)
        throws InterruptedException, 
               ExecutionException, 
               TimeoutException {
        Object rs = before();
        try {
            return this.executor.invokeAny(tasks, timeout, unit);
        } finally {
            after(rs);
        } 
    }

    public <T> Future<T> submit(Callable<T> task) {
        Object rs = before();
        try {
            return this.executor.submit(task);
        } finally {
            after(rs);
        } 
    }

    public Future<?> submit(Runnable task) {
        Object rs = before();
        try {
            return this.executor.submit(task);
        } finally {
            after(rs);
        } 
    }

    public <T> Future<T> submit(Runnable task, T result) {
        Object rs = before();
        try {
            return this.executor.submit(task, result);
        } finally {
            after(rs);
        } 
    }

    public void execute(Runnable command) {
        Object rs = before();
        try {
            this.executor.execute(command);
        } finally {
            after(rs);
        } 
    }
           
}
