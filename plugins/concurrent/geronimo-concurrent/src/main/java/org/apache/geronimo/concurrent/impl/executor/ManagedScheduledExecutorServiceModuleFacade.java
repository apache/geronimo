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

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.util.concurrent.ManagedScheduledExecutorService;
import javax.util.concurrent.ManagedTaskListener;
import javax.util.concurrent.Trigger;

import org.apache.geronimo.gbean.AbstractName;

public class ManagedScheduledExecutorServiceModuleFacade 
    extends ManagedExecutorServiceModuleFacade 
    implements ManagedScheduledExecutorService {
    
    public ManagedScheduledExecutorServiceModuleFacade(ManagedScheduledExecutorService executor,
                                                       AbstractName moduleID) {
        super(executor, moduleID);
    }

    private ManagedScheduledExecutorService getExecutor() {
        return (ManagedScheduledExecutorService)this.executor;
    }
    
    public ScheduledFuture<?> schedule(Runnable command, 
                                       Trigger trigger, 
                                       ManagedTaskListener listener) {
        Object rs = before();
        try {
            return getExecutor().schedule(command, trigger, listener);
        } finally {
            after(rs);
        }
    }

    public <V> ScheduledFuture<V> schedule(Callable<V> callable, 
                                           Trigger trigger, 
                                           ManagedTaskListener listener) {
        Object rs = before();
        try {
            return getExecutor().schedule(callable, trigger, listener);
        } finally {
            after(rs);
        }
    }

    public ScheduledFuture<?> schedule(Runnable command,
                                       long initialDelay,
                                       TimeUnit unit,
                                       ManagedTaskListener listener) {
        Object rs = before();
        try {
            return getExecutor().schedule(command, initialDelay, unit, listener);
        } finally {
            after(rs);
        }
    }

    public <V> ScheduledFuture<V> schedule(Callable<V> callable,
                                           long initialDelay,
                                           TimeUnit unit,
                                           ManagedTaskListener listener) {
        Object rs = before();
        try {
            return getExecutor().schedule(callable, initialDelay, unit, listener);
        } finally {
            after(rs);
        }
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                  long initialDelay,
                                                  long period,
                                                  TimeUnit unit,
                                                  ManagedTaskListener listener) {
        Object rs = before();
        try {
            return getExecutor().scheduleAtFixedRate(command, initialDelay, period, unit, listener);
        } finally {
            after(rs);
        }
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
                                                     long initialDelay,
                                                     long delay,
                                                     TimeUnit unit,
                                                     ManagedTaskListener listener) {
        Object rs = before();
        try {
            return getExecutor().scheduleWithFixedDelay(command, initialDelay, delay, unit, listener);
        } finally {
            after(rs);
        }
    }

    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        Object rs = before();
        try {
            return getExecutor().schedule(command, delay, unit);
        } finally {
            after(rs);
        }
    }

    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        Object rs = before();
        try {
            return getExecutor().schedule(callable, delay, unit);
        } finally {
            after(rs);
        }
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                  long initialDelay,
                                                  long period,
                                                  TimeUnit unit) {
        Object rs = before();
        try {
            return getExecutor().scheduleAtFixedRate(command, initialDelay, period, unit);
        } finally {
            after(rs);
        }
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
                                                     long initialDelay,
                                                     long delay,
                                                     TimeUnit unit) {
        Object rs = before();
        try {
            return getExecutor().scheduleWithFixedDelay(command, initialDelay, delay, unit);
        } finally {
            after(rs);
        }
    }
          
}
