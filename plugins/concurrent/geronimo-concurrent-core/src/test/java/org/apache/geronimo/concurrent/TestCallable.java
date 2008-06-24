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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class TestCallable implements Callable<Object> {
    private List list = new ArrayList();
    private long delay;
    private boolean fail;
    
    public TestCallable() {
        this(0, false);
    }
    
    public TestCallable(long delay) {
        this(delay, false);
    }
    
    public TestCallable(long delay, boolean fail) {
        this.delay = delay;
        this.fail = fail;
    }
    
    public List getList() {
        return this.list;
    }
                  
    public Object call() throws Exception {
        System.out.println(Thread.currentThread());
        if (this.delay > 0) {
            Thread.sleep(this.delay);
        }
        list.add(TestContextHandler.getCurrentObject());
        if (this.fail) {
            throw new IllegalStateException("Abort");
        }
        return this;
    } 
} 