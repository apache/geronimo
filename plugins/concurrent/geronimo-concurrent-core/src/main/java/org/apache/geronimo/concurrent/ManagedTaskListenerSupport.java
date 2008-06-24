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
package org.apache.geronimo.concurrent;

import java.util.concurrent.Future;

import javax.util.concurrent.ManagedExecutorService;
import javax.util.concurrent.ManagedTaskListener;

public class ManagedTaskListenerSupport {

    private ManagedExecutorService executor;
    private ManagedTaskListener listener;

    public ManagedTaskListenerSupport(ManagedExecutorService executor,
                                      ManagedTaskListener listener) { 
        if (executor == null || listener == null) {
            throw new NullPointerException("executor or listener is null");
        }
        this.executor = executor;
        this.listener = listener;
    }
    
    public void taskAborted(Future<?> future, Throwable exception) {
        this.listener.taskAborted(future, this.executor, exception);       
    }

    public void taskDone(Future<?> future, Throwable exception) {
        this.listener.taskDone(future, this.executor, exception);     
    }

    public void taskStarting(Future<?> future) {
        this.listener.taskStarting(future, this.executor); 
    }

    public void taskSubmitted(Future<?> future) {
        this.listener.taskSubmitted(future, this.executor);         
    }

}
