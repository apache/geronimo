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


package org.apache.geronimo.jetty8.connector;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.system.threads.ThreadPool;

/**
 * @version $Rev$ $Date$
 */
public class JettyThreadPool implements org.eclipse.jetty.util.thread.ThreadPool {

    private static final Logger log = LoggerFactory.getLogger(JettyThreadPool.class);
    private final ThreadPool executor;
    private final String name;

    public JettyThreadPool(ThreadPool executor, String name) {
        this.executor = executor;
        this.name = name;
    }

    public boolean dispatch(Runnable runnable) {
        try {
            executor.execute(name, runnable);
            return true;
        } catch (RejectedExecutionException e) {
            log.warn("Unable to execute task", e);
            return false;
        } catch (InterruptedException e) {
            log.warn("Thread interrupted", e);
            return false;
        }
    }

    public void join() throws InterruptedException {
        //umm, this is doubtful
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public int getThreads() {
        return executor.getPoolSize();
    }

    public int getIdleThreads() {
        return executor.getMaximumPoolSize() - executor.getPoolSize();
    }

    public boolean isLowOnThreads() {
        return executor.getPoolSize() >= executor.getMaximumPoolSize();
    }
}
