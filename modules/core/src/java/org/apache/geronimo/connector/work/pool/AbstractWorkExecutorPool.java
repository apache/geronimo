/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */

package org.apache.geronimo.connector.work.pool;

import javax.resource.spi.work.WorkCompletedException;
import javax.resource.spi.work.WorkException;

import EDU.oswego.cs.dl.util.concurrent.Channel;
import org.apache.geronimo.connector.work.WorkerContext;
import org.apache.geronimo.kernel.service.GeronimoAttributeInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;

/**
 * Based class for WorkExecutorPool. Sub-classes define the synchronization
 * policy (should the call block until the end of the work; or when it starts
 * et cetera).
 *
 * @jmx:mbean
 *      extends="org.apache.geronimo.kernel.management.StateManageable,org.apache.geronimo.kernel.management.ManagedObject"
 *
 * @version $Revision: 1.3 $ $Date: 2003/11/26 02:15:32 $
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
            if ( null != exception ) {
                throw exception;
            }
        } catch (InterruptedException e) {
            WorkCompletedException wcj = new WorkCompletedException(
                "The execution has been interrupted.", e);
            wcj.setErrorCode(WorkException.INTERNAL);
            throw wcj;
        }
    }

    public static GeronimoMBeanInfo getGeronimoMBeanInfo() throws Exception {
        GeronimoMBeanInfo rc = new GeronimoMBeanInfo();
        rc.setTargetClass(AbstractWorkExecutorPool.class);
        rc.addAttributeInfo(new GeronimoAttributeInfo("PoolSize", true, false));
        rc.addAttributeInfo(new GeronimoAttributeInfo("MinimumPoolSize", true, true));
        rc.addAttributeInfo(new GeronimoAttributeInfo("MaximumPoolSize", true, true));
        return rc;
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
