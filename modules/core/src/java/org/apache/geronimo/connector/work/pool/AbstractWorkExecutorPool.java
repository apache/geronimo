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

import org.apache.geronimo.kernel.service.GeronimoAttributeInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.connector.work.WorkerContext;

import EDU.oswego.cs.dl.util.concurrent.Channel;

/**
 * Based class for WorkExecutorPool. A sub-class defines the synchronization
 * policy (should the call block until the end of the work; or when it starts
 * et cetera).
 *
 * @jmx:mbean
 *      extends="org.apache.geronimo.kernel.management.StateManageable,org.apache.geronimo.kernel.management.ManagedObject"
 *   
 * @version $Revision: 1.1 $ $Date: 2003/11/16 22:42:20 $
 */
public abstract class AbstractWorkExecutorPool implements WorkExecutorPool {

    /**
     * A timed out pooled executor.
     */
    private TimedOutPooledExecutor m_pooledExecutor;

    /**
     * Creates a pool with the specified minimum and maximum sizes.
     * 
     * @param aMinSize Minimum size of the work executor pool.
     * @param aMaxSize Maximum size of the work executor pool.
     * @param aRetryDuration Duration (in milliseconds) to wait prior to retry 
     * the execution of a Work.
     */
    public AbstractWorkExecutorPool(int aMinSize, int aMaxSize) {
        m_pooledExecutor = new TimedOutPooledExecutor();
        m_pooledExecutor.setMinimumPoolSize(aMinSize);
        m_pooledExecutor.setMaximumPoolSize(aMaxSize);
        m_pooledExecutor.waitWhenBlocked();
    }

    /**
     * Creates a pool with the specified minimum and maximum sizes.
     * 
     * @param Queue to be used on top of the pool.
     * @param aMinSize Minimum size of the work executor pool.
     * @param aMaxSize Maximum size of the work executor pool.
     * @param aRetryDuration Duration (in milliseconds) to wait prior to retry 
     * the execution of a Work.
     */
    public AbstractWorkExecutorPool(Channel aChannel, int aMinSize, int aMaxSize) {
        m_pooledExecutor = new TimedOutPooledExecutor(aChannel);
        m_pooledExecutor.setMinimumPoolSize(aMinSize);
        m_pooledExecutor.setMaximumPoolSize(aMaxSize);
        m_pooledExecutor.waitWhenBlocked();
    }

    /**
     * Delegates the work execution to the pooled executor.
     * 
     * @see EDU.oswego.cs.dl.util.concurrent.PooledExecutor#execute(java.lang.Runnable)
     */
    protected void execute(WorkerContext aWork) throws InterruptedException {
        m_pooledExecutor.execute(aWork);
    }

    /**
     * @see org.apache.geronimo.workmanagement.WorkExecutorPool#execute(org.apache.geronimo.workmanagement.WorkWrapper)
     */
    public void executeWork(WorkerContext aWork) throws WorkException {
        aWork.workAccepted(this);
        try {
            doExecute(aWork);
            WorkException exception = aWork.getWorkException();
            if (null != exception) {
                throw exception;
            }
        } catch (InterruptedException e) {
            WorkCompletedException wcj = new WorkCompletedException("The execution has been interrupted.", e);
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
     * @see org.apache.geronimo.work.WorkExecutorPool#getPoolSize()
     */
    public int getPoolSize() {
        return m_pooledExecutor.getPoolSize();
    }

    /**
     * @see org.apache.geronimo.work.WorkExecutorPool#getMinimumPoolSize()
     */
    public int getMinimumPoolSize() {
        return m_pooledExecutor.getMinimumPoolSize();
    }

    /**
     * @see org.apache.geronimo.work.WorkExecutorPool#setMinimumPoolSize(int)
     */
    public void setMinimumPoolSize(int aSize) {
        m_pooledExecutor.setMinimumPoolSize(aSize);
    }

    /**
     * @see org.apache.geronimo.work.WorkExecutorPool#getMaximumPoolSize()
     */
    public int getMaximumPoolSize() {
        return m_pooledExecutor.getMaximumPoolSize();
    }

    /**
     * @see org.apache.geronimo.work.WorkExecutorPool#setMaximumPoolSize(int)
     */
    public void setMaximumPoolSize(int aSize) {
        m_pooledExecutor.setMaximumPoolSize(aSize);
    }

    /**
     * This method must be implemented by sub-classes in order to provide a
     * synchronization policy.
     * 
     * @param aWork Work to be executed.
     * 
     * @throws WorkException Indicates the work has failed.
     * @throws InterruptedException Indicates that the thread in charge of the 
     * execution of the specified work dies. 
     */
    protected abstract void doExecute(WorkerContext aWork) throws WorkException, InterruptedException;

    /* (non-Javadoc)
     * @see org.apache.geronimo.connector.WorkExecutorPool#stop()
     */
    public void doStop() {
        m_pooledExecutor.shutdownAfterProcessingCurrentlyQueuedTasks();
    }

}
