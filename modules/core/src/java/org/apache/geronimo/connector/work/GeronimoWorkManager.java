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

package org.apache.geronimo.connector.work;
import javax.management.MBeanOperationInfo;
import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkListener;
import javax.resource.spi.work.WorkManager;
import javax.resource.spi.work.WorkRejectedException;

import org.apache.geronimo.common.Classes;
import org.apache.geronimo.connector.work.pool.WorkExecutorPool;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.service.GeronimoMBeanContext;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanTarget;
import org.apache.geronimo.kernel.service.GeronimoOperationInfo;
import org.apache.geronimo.kernel.service.GeronimoParameterInfo;

/**
 * WorkManager implementation which uses under the cover three WorkExecutorPool
 * - one for each synchronization policy - in order to dispatch the submitted 
 * Work instances. 
 * <P>
 * A WorkManager is a component of the JCA specifications, which allows a
 * Resource Adapter to submit tasks to an Application Server for execution.  
 * 
* @version $Revision: 1.3 $ $Date: 2003/11/17 00:46:09 $
 */
public class GeronimoWorkManager implements WorkManager, GeronimoMBeanTarget {

    /**
     * Pool of threads used by this WorkManager in order to process
     * the Work instances submitted via the doWork methods. 
     */
    private WorkExecutorPool syncWorkExecutorPool; 

    /**
     * Pool of threads used by this WorkManager in order to process
     * the Work instances submitted via the startWork methods. 
     */
    private WorkExecutorPool startWorkExecutorPool;
     
    /**
     * Pool of threads used by this WorkManager in order to process
     * the Work instances submitted via the scheduleWork methods.
     */
    private WorkExecutorPool scheduledWorkExecutorPool;
    private GeronimoMBeanContext geronimoMBeanContext;
     
    /**
     * Create a WorkManager. 
     */
    public GeronimoWorkManager() {
    }

    /**
     * Set the executor in charge of the processing of synchronous works.
     * @param anExecutorPool An executor.
     */
    public void setSyncExecutor(WorkExecutorPool anExecutorPool) {
        syncWorkExecutorPool = anExecutorPool;
    }

    /**
     * Sets the executor in charge of the processing of synchronous until start
     * works.
     * @param anExecutorPool An executor.
     */
    public void setStartExecutor(WorkExecutorPool anExecutorPool) {
        startWorkExecutorPool = anExecutorPool;
    }
    
    /**
     * Set the executor in charge of the processing of asynchronous works.
     * @param anExecutorPool An executor.
     */
    public void setAsyncExecutor(WorkExecutorPool anExecutorPool) {
        scheduledWorkExecutorPool = anExecutorPool;
    }
    
    /* (non-Javadoc)
     * @see javax.resource.spi.work.WorkManager#doWork(javax.resource.spi.work.Work)
     */
    public void doWork(Work work) throws WorkException {
        checkStateBeforeAccept(syncWorkExecutorPool, "synchronous");
        syncWorkExecutorPool.executeWork(new WorkerContext(work));
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.work.WorkManager#doWork(javax.resource.spi.work.Work, long, javax.resource.spi.work.ExecutionContext, javax.resource.spi.work.WorkListener)
     */
    public void doWork(
        Work work,
        long startTimeout,
        ExecutionContext execContext,
        WorkListener workListener)
        throws WorkException {
        checkStateBeforeAccept(syncWorkExecutorPool, "synchronous");
        WorkerContext workWrapper =
            new WorkerContext(work, startTimeout, execContext, workListener);
        workWrapper.setThreadPriority(Thread.currentThread().getPriority());
        syncWorkExecutorPool.executeWork(workWrapper);
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.work.WorkManager#startWork(javax.resource.spi.work.Work)
     */
    public long startWork(Work work) throws WorkException {
        checkStateBeforeAccept(startWorkExecutorPool,
            "synchronous until start");
        WorkerContext workWrapper = new WorkerContext(work);
        workWrapper.setThreadPriority(Thread.currentThread().getPriority());
        startWorkExecutorPool.executeWork(workWrapper);
        return System.currentTimeMillis() - workWrapper.getAcceptedTime();
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.work.WorkManager#startWork(javax.resource.spi.work.Work, long, javax.resource.spi.work.ExecutionContext, javax.resource.spi.work.WorkListener)
     */
    public long startWork(
        Work work,
        long startTimeout,
        ExecutionContext execContext,
        WorkListener workListener)
        throws WorkException {
        checkStateBeforeAccept(startWorkExecutorPool,
            "synchronous until start");
        WorkerContext workWrapper =
            new WorkerContext(work, startTimeout, execContext, workListener);
        workWrapper.setThreadPriority(Thread.currentThread().getPriority());
        startWorkExecutorPool.executeWork(workWrapper);
        return System.currentTimeMillis() - workWrapper.getAcceptedTime();
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.work.WorkManager#scheduleWork(javax.resource.spi.work.Work)
     */
    public void scheduleWork(Work work) throws WorkException {
        checkStateBeforeAccept(scheduledWorkExecutorPool, "asynchronous");
        WorkerContext workWrapper = new WorkerContext(work);
        workWrapper.setThreadPriority(Thread.currentThread().getPriority());
        scheduledWorkExecutorPool.executeWork(workWrapper);
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.work.WorkManager#scheduleWork(javax.resource.spi.work.Work, long, javax.resource.spi.work.ExecutionContext, javax.resource.spi.work.WorkListener)
     */
    public void scheduleWork(
        Work work,
        long startTimeout,
        ExecutionContext execContext,
        WorkListener workListener)
        throws WorkException {
        checkStateBeforeAccept(scheduledWorkExecutorPool, "asynchronous");
        WorkerContext workWrapper =
            new WorkerContext(work, startTimeout, execContext, workListener);
        workWrapper.setThreadPriority(Thread.currentThread().getPriority());
        scheduledWorkExecutorPool.executeWork(workWrapper);
    }

    /**
     * This helper method MUST be called prior to accept a Work instance. It
     * ensures that the state of this WorkManager is running and that the
     * provided work executor is defined.
     *
     * @param aPool Work executor, which will accept the Work instance.
     * @param aType "Label" of this work executor. It is only used to
     * create an more accurate message when the provided Work executor is not
     * defined (null). 
     *  
     * @throws WorkRejectedException Indicates that this WorkManager is not
     * running and hence that a work can not be accepted.
     */
    private void checkStateBeforeAccept(WorkExecutorPool aPool,
        String aType) throws WorkRejectedException {
        if ( !(State.RUNNING_INDEX == getState()) ) {
            throw new WorkRejectedException(getClass() + " is not running.",
                WorkException.INTERNAL);
        } else if ( null == aPool ) {
            throw new WorkRejectedException(getClass() + " is partially" +
                " running. Its " + aType + " work facilities are unmounted.",
                WorkException.INTERNAL);
        }
    }


    public int getState() throws WorkRejectedException {
        try {
            return geronimoMBeanContext.getState();
        } catch (Exception e) {
            throw new WorkRejectedException("WorkManager is not ready.", WorkException.INTERNAL);
        }
    }
    
    /**
     * @see org.apache.geronimo.kernel.service.GeronimoMBeanTarget#setMBeanContext(org.apache.geronimo.kernel.service.GeronimoMBeanContext)
     */
    public void setMBeanContext(GeronimoMBeanContext geronimoMBeanContext) {
        this.geronimoMBeanContext = geronimoMBeanContext;
    }

    /**
     * @see org.apache.geronimo.kernel.service.GeronimoMBeanTarget#canStart()
     */
    public boolean canStart() {
        return true;
    }

    /**
     * @see org.apache.geronimo.kernel.service.GeronimoMBeanTarget#doStart()
     */
    public void doStart() {
    }

    /**
     * @see org.apache.geronimo.kernel.service.GeronimoMBeanTarget#canStop()
     */
    public boolean canStop() {
        return true;
    }

    /**
     * @see org.apache.geronimo.kernel.service.GeronimoMBeanTarget#doStop()
     */
    public void doStop() {
    }

    /**
     * @see org.apache.geronimo.kernel.service.GeronimoMBeanTarget#doFail()
     */
    public void doFail() {
    }

    
    /**
     * Provides the GeronimoMBean description for this class
     * @return
     */
    public static GeronimoMBeanInfo getGeronimoMBeanInfo() throws Exception {

        // TODO: add descriptions to all operations.
        GeronimoMBeanInfo rc = new GeronimoMBeanInfo();
        rc.setTargetClass(GeronimoWorkManager.class);
        rc.addOperationFor( Classes.getMethod(GeronimoWorkManager.class, "setSyncExecutor") ); 
        rc.addOperationFor( Classes.getMethod(GeronimoWorkManager.class, "setStartExecutor") ); 
        rc.addOperationFor( Classes.getMethod(GeronimoWorkManager.class, "setAsyncExecutor") ); 
        return rc;

    }
    
}
