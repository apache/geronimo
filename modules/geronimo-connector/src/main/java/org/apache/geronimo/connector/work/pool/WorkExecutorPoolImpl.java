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

package org.apache.geronimo.connector.work.pool;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Based class for WorkExecutorPool. Sub-classes define the synchronization
 * policy (should the call block until the end of the work; or when it starts
 * et cetera).
 *
 * @version $Rev$ $Date$
 */
public class WorkExecutorPoolImpl implements WorkExecutorPool {

    /**
     * A timed out pooled executor.
     */
    private ThreadPoolExecutor pooledExecutor;
    private static Log log = LogFactory.getLog(WorkExecutorPoolImpl.class);

    /**
     * Creates a pool with the specified minimum and maximum sizes. The Channel
     * used to enqueue the submitted Work instances is queueless synchronous
     * one.
     *
     * @param maxSize Maximum size of the work executor pool.
     */
    public WorkExecutorPoolImpl(int maxSize) {
        pooledExecutor = new ThreadPoolExecutor(1, maxSize, 60, TimeUnit.SECONDS, new LinkedBlockingQueue());
        /*
        FIXME: How to do this with concurrent.util ?
        pooledExecutor.waitWhenBlocked();
        */
    }
    
    /**
     * Execute the specified Work.
     *
     * @param work Work to be executed.
     */
    public void execute(Runnable work) {
        if(pooledExecutor.getPoolSize() == pooledExecutor.getMaximumPoolSize()) {
            log.warn("Maximum Pool size has been exceeded.  Current Pool Size = "+pooledExecutor.getMaximumPoolSize());
        }

        pooledExecutor.execute(work);
    }

    /**
     * Gets the size of this pool.
     */
    public int getPoolSize() {
        return pooledExecutor.getPoolSize();
    }

    /**
     * Gets the maximum size of this pool.
     */
    public int getMaximumPoolSize() {
        return pooledExecutor.getMaximumPoolSize();
    }

    /**
     * Sets the maximum size of this pool.
     * @param maxSize New maximum size of this pool.
     */
    public void setMaximumPoolSize(int maxSize) {
        pooledExecutor.setMaximumPoolSize(maxSize);
    }

    public WorkExecutorPool start() {
        throw new IllegalStateException("This pooled executor is already started");
    }

    /**
     * Stops this pool. Prior to stop this pool, all the enqueued Work instances
     * are processed. This is an orderly shutdown.
     */
    public WorkExecutorPool stop() {
        int maxSize = getMaximumPoolSize();
        pooledExecutor.shutdown();
        return new NullWorkExecutorPool(maxSize);
    }

}
