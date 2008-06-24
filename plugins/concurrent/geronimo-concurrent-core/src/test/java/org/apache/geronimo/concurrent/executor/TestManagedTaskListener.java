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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.util.concurrent.ManagedExecutorService;
import javax.util.concurrent.ManagedTaskListener;

import org.apache.geronimo.concurrent.TestContextHandler;

public class TestManagedTaskListener implements ManagedTaskListener {

    enum Callbacks {
        SUBMITTED, STARTING, DONE, ABORTED
    };

    public static class CallbackInfo {
        Callbacks callback;
        Future future;
        ManagedExecutorService executor;
        Throwable exception;
        Object data;

        public CallbackInfo(Callbacks callback,
                            Future future,
                            ManagedExecutorService executor,
                            Throwable exception,
                            Object data) {
            this.callback = callback;
            this.future = future;
            this.executor = executor;
            this.exception = exception;
            this.data = data;
        }

        public Callbacks getCallback() {
            return callback;
        }
        
        public Future getFuture() {
            return this.future;
        }

        public ManagedExecutorService getManagedExecutorService() {
            return executor;
        }
        
        public Object getData() {
            return this.data;
        }
        
        public Throwable getException() {
            return this.exception;
        }
        
        public String toString() {
            StringBuffer buf = new StringBuffer();
            buf.append(callback.toString());
            if (future != null) {
                buf.append(" ");
                buf.append(future.toString());
            }
            if (executor != null) {
                buf.append(" ");
                buf.append(executor.toString());
            }
            if (data != null) {
                buf.append(" ");
                buf.append(data.toString());
            }
            if (exception != null) {
                buf.append(" ");
                buf.append(exception.toString());
            }
            return buf.toString();
        }

        public boolean equals(Object o) {
            if (!(o instanceof CallbackInfo)) {
                return false;
            }
            CallbackInfo other = (CallbackInfo) o;
            if (this.callback != other.callback) {
                return false;
            }
            if (this.future != other.future) {
                return false;
            }
            if (this.executor != other.executor) {
                return false;
            }
            if ((this.exception == null && other.exception != null) || this.exception != null
                && other.exception == null) {
                return false;
            }
            return true;
        }
    }

    private List<CallbackInfo> callbacks = Collections
            .synchronizedList(new ArrayList<CallbackInfo>());

    private Semaphore semaphore = new Semaphore(0);
    
    public List<CallbackInfo> getCallbacks() {
        return this.callbacks;
    }

    public List<CallbackInfo> getCallbacks(Future future) {
        List<CallbackInfo> futureCallbacks = new ArrayList<CallbackInfo>();
        synchronized (this.callbacks) {
            for (CallbackInfo info : this.callbacks) {
                if (info.getFuture() == future) {
                    futureCallbacks.add(info);
                }
            }
        }
        return futureCallbacks;
    }

    public void taskAborted(Future<?> arg0, ManagedExecutorService arg1, Throwable arg2) {
        Object data = TestContextHandler.getCurrentObject();
        callbacks.add(new CallbackInfo(Callbacks.ABORTED, arg0, arg1, arg2, data));
    }

    public void taskDone(Future<?> arg0, ManagedExecutorService arg1, Throwable arg2) {
        Object data = TestContextHandler.getCurrentObject();
        callbacks.add(new CallbackInfo(Callbacks.DONE, arg0, arg1, arg2, data)); 
        semaphore.release();
    }

    public void taskStarting(Future<?> arg0, ManagedExecutorService arg1) {
        Object data = TestContextHandler.getCurrentObject();
        callbacks.add(new CallbackInfo(Callbacks.STARTING, arg0, arg1, null, data));
    }

    public void taskSubmitted(Future<?> arg0, ManagedExecutorService arg1) {
        Object data = TestContextHandler.getCurrentObject();
        callbacks.add(new CallbackInfo(Callbacks.SUBMITTED, arg0, arg1, null, data));
    }
    
    public boolean waitForDone(int timeout) throws InterruptedException {
        return semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS);
    }
    
    public boolean waitForDone(int permits, int timeout) throws InterruptedException {
        for (int i = 0; i < permits; i++) {
            if (!semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS)) {
                return false;
            }
        }
        return true;
    }
}
