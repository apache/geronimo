/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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

import javax.resource.spi.work.WorkCompletedException;
import javax.resource.spi.work.WorkException;

import EDU.oswego.cs.dl.util.concurrent.Channel;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import EDU.oswego.cs.dl.util.concurrent.LinkedQueue;
import org.apache.geronimo.connector.work.WorkerContext;

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
    private PooledExecutor pooledExecutor;

    /**
     * Creates a pool with the specified minimum and maximum sizes. The Channel
     * used to enqueue the submitted Work instances is queueless synchronous
     * one.
     *
     * @param maxSize Maximum size of the work executor pool.
     */
    public WorkExecutorPoolImpl(int maxSize) {
        pooledExecutor = new PooledExecutor(new LinkedQueue(), maxSize);
        pooledExecutor.setMinimumPoolSize(maxSize);
        pooledExecutor.waitWhenBlocked();
    }

    /**
     * Creates a pool with the specified minimum and maximum sizes and using the
     * specified Channel to enqueue the submitted Work instances.
     *
     * @param channel Queue to be used as the queueing facility of this pool.
     * @param maxSize Maximum size of the work executor pool.
     */
    public WorkExecutorPoolImpl(
            Channel channel,
            int maxSize) {
        pooledExecutor = new PooledExecutor(channel, maxSize);
        pooledExecutor.setMinimumPoolSize(maxSize);
        pooledExecutor.waitWhenBlocked();
    }

    /**
     * Execute the specified Work.
     *
     * @param work Work to be executed.
     *
     * @exception InterruptedException Indicates that the Work execution has been
     * unsuccessful.
     */
    public void execute(Runnable work) throws InterruptedException {
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
        pooledExecutor.shutdownAfterProcessingCurrentlyQueuedTasks();
        return new NullWorkExecutorPool(maxSize);
    }

}
