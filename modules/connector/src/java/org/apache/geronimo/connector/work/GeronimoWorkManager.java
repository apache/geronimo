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

package org.apache.geronimo.connector.work;

import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkCompletedException;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkListener;
import javax.resource.spi.work.WorkManager;
import javax.resource.spi.XATerminator;

import EDU.oswego.cs.dl.util.concurrent.Executor;
import org.apache.geronimo.connector.work.pool.NullWorkExecutorPool;
import org.apache.geronimo.connector.work.pool.ScheduleWorkExecutor;
import org.apache.geronimo.connector.work.pool.StartWorkExecutor;
import org.apache.geronimo.connector.work.pool.SyncWorkExecutor;
import org.apache.geronimo.connector.work.pool.WorkExecutor;
import org.apache.geronimo.connector.work.pool.WorkExecutorPool;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

/**
 * WorkManager implementation which uses under the cover three WorkExecutorPool
 * - one for each synchronization policy - in order to dispatch the submitted
 * Work instances.
 * <P>
 * A WorkManager is a component of the JCA specifications, which allows a
 * Resource Adapter to submit tasks to an Application Server for execution.
 *
 * @version $Rev$ $Date$
 */
public class GeronimoWorkManager implements WorkManager, GBeanLifecycle {

    private final static int DEFAULT_POOL_SIZE = 10;

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

    private final TransactionContextManager transactionContextManager;

    private final WorkExecutor scheduleWorkExecutor = new ScheduleWorkExecutor();
    private final WorkExecutor startWorkExecutor = new StartWorkExecutor();
    private final WorkExecutor syncWorkExecutor = new SyncWorkExecutor();

    /**
     * Create a WorkManager.
     */
    public GeronimoWorkManager() {
        this(DEFAULT_POOL_SIZE, null);
    }

    public GeronimoWorkManager(int size, TransactionContextManager transactionContextManager) {
        this(size, size, size, transactionContextManager);
    }

    public GeronimoWorkManager(int syncSize, int startSize, int schedSize, TransactionContextManager transactionContextManager) {
        syncWorkExecutorPool = new NullWorkExecutorPool(syncSize);
        startWorkExecutorPool = new NullWorkExecutorPool(startSize);
        scheduledWorkExecutorPool = new NullWorkExecutorPool(schedSize);
        this.transactionContextManager = transactionContextManager;
    }

    public void doStart() throws WaitingException, Exception {
        syncWorkExecutorPool = syncWorkExecutorPool.start();
        startWorkExecutorPool = startWorkExecutorPool.start();
        scheduledWorkExecutorPool = scheduledWorkExecutorPool.start();
    }

    public void doStop() throws WaitingException, Exception {
        syncWorkExecutorPool = syncWorkExecutorPool.stop();
        startWorkExecutorPool = startWorkExecutorPool.stop();
        scheduledWorkExecutorPool = scheduledWorkExecutorPool.stop();
    }

    public void doFail() {
        try {
            doStop();
        } catch (Exception e) {
            //TODO what to do?
        }
    }

    public XATerminator getXATerminator() {
        return transactionContextManager;
    }

    public int getSyncThreadCount() {
        return syncWorkExecutorPool.getPoolSize();
    }

    public int getSyncMaximumPoolSize() {
        return syncWorkExecutorPool.getMaximumPoolSize();
    }

    public void setSyncMaximumPoolSize(int maxSize) {
        syncWorkExecutorPool.setMaximumPoolSize(maxSize);
    }

    public int getStartThreadCount() {
        return startWorkExecutorPool.getPoolSize();
    }

    public int getStartMaximumPoolSize() {
        return startWorkExecutorPool.getMaximumPoolSize();
    }

    public void setStartMaximumPoolSize(int maxSize) {
        startWorkExecutorPool.setMaximumPoolSize(maxSize);
    }

    public int getScheduledThreadCount() {
        return scheduledWorkExecutorPool.getPoolSize();
    }

    public int getScheduledMaximumPoolSize() {
        return scheduledWorkExecutorPool.getMaximumPoolSize();
    }

    public void setScheduledMaximumPoolSize(int maxSize) {
        scheduledWorkExecutorPool.setMaximumPoolSize(maxSize);
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.work.WorkManager#doWork(javax.resource.spi.work.Work)
     */
    public void doWork(Work work) throws WorkException {
        executeWork(new WorkerContext(work, transactionContextManager), syncWorkExecutor, syncWorkExecutorPool);
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
        WorkerContext workWrapper =
                new WorkerContext(work, startTimeout, execContext, transactionContextManager, workListener);
        workWrapper.setThreadPriority(Thread.currentThread().getPriority());
        executeWork(workWrapper, syncWorkExecutor, syncWorkExecutorPool);
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.work.WorkManager#startWork(javax.resource.spi.work.Work)
     */
    public long startWork(Work work) throws WorkException {
        WorkerContext workWrapper = new WorkerContext(work, transactionContextManager);
        workWrapper.setThreadPriority(Thread.currentThread().getPriority());
        executeWork(workWrapper, startWorkExecutor, startWorkExecutorPool);
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
        WorkerContext workWrapper =
                new WorkerContext(work, startTimeout, execContext, transactionContextManager, workListener);
        workWrapper.setThreadPriority(Thread.currentThread().getPriority());
        executeWork(workWrapper, startWorkExecutor, startWorkExecutorPool);
        return System.currentTimeMillis() - workWrapper.getAcceptedTime();
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.work.WorkManager#scheduleWork(javax.resource.spi.work.Work)
     */
    public void scheduleWork(Work work) throws WorkException {
        WorkerContext workWrapper = new WorkerContext(work, transactionContextManager);
        workWrapper.setThreadPriority(Thread.currentThread().getPriority());
        executeWork(workWrapper, scheduleWorkExecutor, scheduledWorkExecutorPool);
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
        WorkerContext workWrapper =
                new WorkerContext(work, startTimeout, execContext, transactionContextManager, workListener);
        workWrapper.setThreadPriority(Thread.currentThread().getPriority());
        executeWork(workWrapper, scheduleWorkExecutor, scheduledWorkExecutorPool);
    }

    /**
     * Execute the specified Work.
     *
     * @param work Work to be executed.
     *
     * @exception WorkException Indicates that the Work execution has been
     * unsuccessful.
     */
    private void executeWork(WorkerContext work, WorkExecutor workExecutor, Executor pooledExecutor) throws WorkException {
        work.workAccepted(this);
        try {
            workExecutor.doExecute(work, pooledExecutor);
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

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(GeronimoWorkManager.class, NameFactory.JCA_WORK_MANAGER);
        infoFactory.addInterface(WorkManager.class);

        infoFactory.addAttribute("syncMaximumPoolSize", Integer.TYPE, true);
        infoFactory.addAttribute("startMaximumPoolSize", Integer.TYPE, true);
        infoFactory.addAttribute("scheduledMaximumPoolSize", Integer.TYPE, true);

        infoFactory.addOperation("getXATerminator");

        infoFactory.addReference("TransactionContextManager", TransactionContextManager.class);

        infoFactory.setConstructor(new String[]{
            "syncMaximumPoolSize",
            "startMaximumPoolSize",
            "scheduledMaximumPoolSize",
            "TransactionContextManager"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }


}
