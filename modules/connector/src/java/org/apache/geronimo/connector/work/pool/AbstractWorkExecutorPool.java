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

import org.apache.geronimo.connector.work.WorkerContext;

import EDU.oswego.cs.dl.util.concurrent.Channel;

/**
 * Based class for WorkExecutorPool. Sub-classes define the synchronization
 * policy (should the call block until the end of the work; or when it starts
 * et cetera).
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:58:33 $
 */
public abstract class AbstractWorkExecutorPool implements WorkExecutorPool {

    /**
     * A timed out pooled executor.
     */
    private TimedOutPooledExecutor pooledExecutor;

    /**
     * Creates a pool with the specified minimum and maximum sizes. The Channel
     * used to enqueue the submitted Work instances is queueless synchronous
     * one.
     *
     * @param minSize Minimum size of the work executor pool.
     * @param maxSize Maximum size of the work executor pool.
     */
    public AbstractWorkExecutorPool(int minSize, int maxSize) {
        pooledExecutor = new TimedOutPooledExecutor();
        pooledExecutor.setMinimumPoolSize(minSize);
        pooledExecutor.setMaximumPoolSize(maxSize);
        pooledExecutor.waitWhenBlocked();
    }

    /**
     * Creates a pool with the specified minimum and maximum sizes and using the
     * specified Channel to enqueue the submitted Work instances.
     *
     * @param channel Queue to be used as the queueing facility of this pool.
     * @param minSize Minimum size of the work executor pool.
     * @param maxSize Maximum size of the work executor pool.
     */
    public AbstractWorkExecutorPool(
            Channel channel,
            int minSize, int maxSize) {
        pooledExecutor = new TimedOutPooledExecutor(channel);
        pooledExecutor.setMinimumPoolSize(minSize);
        pooledExecutor.setMaximumPoolSize(maxSize);
        pooledExecutor.waitWhenBlocked();
    }

    /**
     * Delegates the work execution to the pooled executor.
     *
     * @param work Work to be executed.
     */
    protected void execute(WorkerContext work) throws InterruptedException {
        pooledExecutor.execute(work);
    }

    /**
     * Execute the specified Work.
     *
     * @param work Work to be executed.
     *
     * @exception WorkException Indicates that the Work execution has been
     * unsuccessful.
     */
    public void executeWork(WorkerContext work) throws WorkException {
        work.workAccepted(this);
        try {
            doExecute(work);
            WorkException exception = work.getWorkException();
            if (null != exception) {
                throw exception;
            }
        } catch (InterruptedException e) {
            WorkCompletedException wcj = new WorkCompletedException(
                    "The execution has been interrupted.", e);
            wcj.setErrorCode(WorkException.INTERNAL);
            throw wcj;
        }
    }

    /**
     * Gets the size of this pool.
     */
    public int getPoolSize() {
        return pooledExecutor.getPoolSize();
    }

    /**
     * Gets the minimu size of this pool.
     */
    public int getMinimumPoolSize() {
        return pooledExecutor.getMinimumPoolSize();
    }

    /**
     * Sets the minimum size of this pool.
     * @param minSize New minimum size of the pool.
     */
    public void setMinimumPoolSize(int minSize) {
        pooledExecutor.setMinimumPoolSize(minSize);
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

    /**
     * This method must be implemented by sub-classes in order to provide the
     * relevant synchronization policy. It is called by the executeWork template
     * method.
     *
     * @param work Work to be executed.
     *
     * @throws WorkException Indicates that the work has failed.
     * @throws InterruptedException Indicates that the thread in charge of the
     * execution of the specified work has been interrupted.
     */
    protected abstract void doExecute(WorkerContext work)
            throws WorkException, InterruptedException;

    /**
     * Stops this pool. Prior to stop this pool, all the enqueued Work instances
     * are processed. This is an orderly shutdown.
     */
    public void doStop() {
        pooledExecutor.shutdownAfterProcessingCurrentlyQueuedTasks();
    }

}
