/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.tomcat;

import java.util.concurrent.TimeUnit;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.geronimo.pool.GeronimoExecutor;

/**
 * @version $Rev$ $Date$
 */
public class TomcatExecutorWrapper implements org.apache.catalina.Executor{
    private final GeronimoExecutor executor;
    private final LifecycleSupport lifecycle = new LifecycleSupport(this);

    public TomcatExecutorWrapper(GeronimoExecutor executor) {
        this.executor = executor;
    }

    public String getName() {
        return executor.getName();
    }

    @Override
    public void execute(Runnable runnable) {
        executor.execute(runnable);
    }

    public void addLifecycleListener(LifecycleListener listener) {
        lifecycle.addLifecycleListener(listener);
    }

    public LifecycleListener[] findLifecycleListeners() {
        return lifecycle.findLifecycleListeners();
    }

    public void removeLifecycleListener(LifecycleListener listener) {
        lifecycle.removeLifecycleListener(listener);
    }

    public void start() throws LifecycleException {
        lifecycle.fireLifecycleEvent(BEFORE_START_EVENT, null);
        lifecycle.fireLifecycleEvent(START_EVENT, null);
        lifecycle.fireLifecycleEvent(AFTER_START_EVENT, null);
    }

    public void stop() throws LifecycleException {
        lifecycle.fireLifecycleEvent(BEFORE_STOP_EVENT, null);
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        lifecycle.fireLifecycleEvent(AFTER_STOP_EVENT, null);
    }
 
    @Override
    public void execute(Runnable runnable, long timeout, TimeUnit unit) {
        // FIXME Figure out how to implement it
    }
}
