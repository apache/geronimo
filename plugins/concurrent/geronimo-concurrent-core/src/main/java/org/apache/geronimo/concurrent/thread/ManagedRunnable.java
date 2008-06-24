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
package org.apache.geronimo.concurrent.thread;

import java.util.Map;

import org.apache.geronimo.concurrent.ManagedContext;
import org.apache.geronimo.concurrent.ManagedTask;
import org.apache.geronimo.concurrent.ManagedTaskUtils;

public class ManagedRunnable implements Runnable, ManagedTask {

    private Runnable target;
    private ManagedContext managedContext;
    private boolean associateTaskWithThread;

    private Thread runner;
    
    public ManagedRunnable(Runnable target, 
                           ManagedContext managedContext) {  
        this(target, managedContext, true);
    }
    
    public ManagedRunnable(Runnable target, 
                           ManagedContext managedContext,
                           boolean associateTaskWithThread) {        
        this.target = target;
        this.managedContext = managedContext;
        this.associateTaskWithThread = associateTaskWithThread;
    }

    public void run() {
        if (this.target == null) {
            return;
        }
        
        // set ee context
        Map<String, Object> threadContext = this.managedContext.set();
        try {
            if (this.associateTaskWithThread) {
                runTasked();
            } else {
                runBasic();
            }
        } finally {            
            // restore ee context
            this.managedContext.unset(threadContext);              
        }
    }
    
    private void runBasic() {
        this.target.run();
    }
    
    private void runTasked() {
        Thread thread = Thread.currentThread();
        if (!(thread instanceof ManagedThread)) {
            throw new IllegalStateException("Expected ManagedThread thread");
        }
        
        // associate task with thread
        ManagedThread managedThread = (ManagedThread)thread;
        managedThread.startTask(this);
        try {
            this.runner = managedThread;
            this.target.run();
        } finally {
            this.runner = null;
            // de-associate task with thread
            managedThread.endTask();
        }        
    }

    public boolean cancel() {
        Thread thread = this.runner;
        if (thread != null) {
            thread.interrupt();
            return true;
        } else {
            return false;
        }
    }

    public String getIdentityDescription(String locale) {
        return ManagedTaskUtils.getTaskDescription(this.target, locale);
    }

    public String getIdentityName() {
        return ManagedTaskUtils.getTaskName(this.target);
    }
}
