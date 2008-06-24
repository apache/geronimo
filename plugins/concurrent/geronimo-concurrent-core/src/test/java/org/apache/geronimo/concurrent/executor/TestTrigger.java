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
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import javax.util.concurrent.Trigger;

import org.apache.geronimo.concurrent.TestContextHandler;

public class TestTrigger implements Trigger {
    
    enum Callbacks {
        GET_NEXT_RUN_TIME, SKIP_RUN
    };
    
    public static class CallbackInfo {
        Callbacks callback;
        Future future;
        Object data;

        public CallbackInfo(Callbacks callback,
                            Future future,
                            Object data) {
            this.callback = callback;
            this.future = future;
            this.data = data;
        }

        public Callbacks getCallback() {
            return callback;
        }
        
        public Future getFuture() {
            return this.future;
        }
        
        public Object getData() {
            return this.data;
        }
        
        public String toString() {
            StringBuffer buf = new StringBuffer();
            buf.append(callback.toString());
            if (future != null) {
                buf.append(" ");
                buf.append(future.toString());
            }
            if (data != null) {
                buf.append(" ");
                buf.append(data.toString());
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
            return true;
        }
    }
    
    private List<CallbackInfo> callbacks = 
        Collections.synchronizedList(new ArrayList<CallbackInfo>());

    private int run;
    private int skip;
    private int done;
    
    public TestTrigger() {
        this(-1, -1);
    }
    
    public TestTrigger(int skip, int done) {
        this.skip = skip;
        this.done = done;
    }
    
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
                  
    public Date getNextRunTime(Future<?> future, Date arg1, Date arg2, Date arg3, Date arg4) {
        Object data = TestContextHandler.getCurrentObject();
        callbacks.add(new CallbackInfo(Callbacks.GET_NEXT_RUN_TIME, future, data));
        run++; 
        
        if (run == done) {
            return null;
        } else {                  
            return new Date(System.currentTimeMillis() + 1000 * 5);
        }
    }

    public boolean skipRun(Future<?> future, Date arg1) {
        Object data = TestContextHandler.getCurrentObject();
        callbacks.add(new CallbackInfo(Callbacks.SKIP_RUN, future, data));
        
        return (run == skip) ? true : false; 
    } 
} 
