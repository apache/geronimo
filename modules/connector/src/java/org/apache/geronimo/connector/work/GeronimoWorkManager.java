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
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkListener;
import javax.resource.spi.work.WorkManager;

import org.apache.geronimo.connector.work.pool.ScheduleWorkExecutorPool;
import org.apache.geronimo.connector.work.pool.StartWorkExecutorPool;
import org.apache.geronimo.connector.work.pool.SyncWorkExecutorPool;
import org.apache.geronimo.connector.work.pool.WorkExecutorPool;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.transaction.XAWork;

/**
 * WorkManager implementation which uses under the cover three WorkExecutorPool
 * - one for each synchronization policy - in order to dispatch the submitted
 * Work instances.
 * <P>
 * A WorkManager is a component of the JCA specifications, which allows a
 * Resource Adapter to submit tasks to an Application Server for execution.
 *
 * TODO There needs to be better lifecycle support.  The individual pools can be stopped now, but
 * not restarted AFAIK.
 *
 * @version $Revision: 1.5 $ $Date: 2004/03/10 09:58:33 $
 */
public class GeronimoWorkManager implements WorkManager {

    private final static int DEFAULT_MIN_POOL_SIZE = 0;
    private final static int DEFAULT_MAX_POOL_SIZE = 10;

    /**
     * Pool of threads used by this WorkManager in order to process
     * the Work instances submitted via the doWork methods.
     */
    private final WorkExecutorPool syncWorkExecutorPool;

    /**
     * Pool of threads used by this WorkManager in order to process
     * the Work instances submitted via the startWork methods.
     */
    private final WorkExecutorPool startWorkExecutorPool;

    /**
     * Pool of threads used by this WorkManager in order to process
     * the Work instances submitted via the scheduleWork methods.
     */
    private final WorkExecutorPool scheduledWorkExecutorPool;

    private final XAWork xaWork;

    /**
     * Create a WorkManager.
     */
    public GeronimoWorkManager() {
        this(DEFAULT_MIN_POOL_SIZE, DEFAULT_MAX_POOL_SIZE, null);
    }

    public GeronimoWorkManager(int minSize, int maxSize, XAWork xaWork) {
        this(minSize, maxSize, minSize, maxSize, minSize, maxSize, xaWork);
    }

    public GeronimoWorkManager(int syncMinSize, int syncMaxSize, int startMinSize, int startMaxSize, int schedMinSize, int schedMaxSize, XAWork xaWork) {
        syncWorkExecutorPool = new SyncWorkExecutorPool(syncMinSize, syncMaxSize);
        startWorkExecutorPool = new StartWorkExecutorPool(startMinSize, startMaxSize);
        scheduledWorkExecutorPool = new ScheduleWorkExecutorPool(schedMinSize, schedMaxSize);
        this.xaWork = xaWork;
    }

    public int getSyncThreadCount() {
        return syncWorkExecutorPool.getPoolSize();
    }

    public int getSyncMinimumPoolSize() {
        return syncWorkExecutorPool.getMinimumPoolSize();
    }

    public int getSyncMaximumPoolSize() {
        return syncWorkExecutorPool.getMaximumPoolSize();
    }

    public void setSyncMinimumPoolSize(int minSize) {
        syncWorkExecutorPool.setMinimumPoolSize(minSize);
    }

    public void setSyncMaximumPoolSize(int maxSize) {
        syncWorkExecutorPool.setMaximumPoolSize(maxSize);
    }

    public int getStartThreadCount() {
        return startWorkExecutorPool.getPoolSize();
    }

    public int getStartMinimumPoolSize() {
        return startWorkExecutorPool.getMinimumPoolSize();
    }

    public int getStartMaximumPoolSize() {
        return startWorkExecutorPool.getMaximumPoolSize();
    }

    public void setStartMinimumPoolSize(int minSize) {
        startWorkExecutorPool.setMinimumPoolSize(minSize);
    }

    public void setStartMaximumPoolSize(int maxSize) {
        startWorkExecutorPool.setMaximumPoolSize(maxSize);
    }

    public int getScheduledThreadCount() {
        return scheduledWorkExecutorPool.getPoolSize();
    }

    public int getScheduledMinimumPoolSize() {
        return scheduledWorkExecutorPool.getMinimumPoolSize();
    }

    public int getScheduledMaximumPoolSize() {
        return scheduledWorkExecutorPool.getMaximumPoolSize();
    }

    public void setScheduledMinimumPoolSize(int minSize) {
        scheduledWorkExecutorPool.setMinimumPoolSize(minSize);
    }

    public void setScheduledMaximumPoolSize(int maxSize) {
        scheduledWorkExecutorPool.setMaximumPoolSize(maxSize);
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.work.WorkManager#doWork(javax.resource.spi.work.Work)
     */
    public void doWork(Work work) throws WorkException {
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
        WorkerContext workWrapper =
                new WorkerContext(work, startTimeout, execContext, xaWork, workListener);
        workWrapper.setThreadPriority(Thread.currentThread().getPriority());
        syncWorkExecutorPool.executeWork(workWrapper);
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.work.WorkManager#startWork(javax.resource.spi.work.Work)
     */
    public long startWork(Work work) throws WorkException {
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
        WorkerContext workWrapper =
                new WorkerContext(work, startTimeout, execContext, xaWork, workListener);
        workWrapper.setThreadPriority(Thread.currentThread().getPriority());
        startWorkExecutorPool.executeWork(workWrapper);
        return System.currentTimeMillis() - workWrapper.getAcceptedTime();
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.work.WorkManager#scheduleWork(javax.resource.spi.work.Work)
     */
    public void scheduleWork(Work work) throws WorkException {
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
        WorkerContext workWrapper =
                new WorkerContext(work, startTimeout, execContext, xaWork, workListener);
        workWrapper.setThreadPriority(Thread.currentThread().getPriority());
        scheduledWorkExecutorPool.executeWork(workWrapper);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(GeronimoWorkManager.class.getName());
        infoFactory.addInterface(WorkManager.class, new String[]{"SyncMinimumPoolSize", "SyncMaximumPoolSize", "StartMinimumPoolSize", "StartMaximumPoolSize", "ScheduledMinimumPoolSize", "ScheduledMaximumPoolSize"});
        infoFactory.addReference("XAWork", XAWork.class);
        infoFactory.setConstructor(new GConstructorInfo(
                new String[]{"SyncMinimumPoolSize", "SyncMaximumPoolSize", "StartMinimumPoolSize", "StartMaximumPoolSize", "ScheduledMinimumPoolSize", "ScheduledMaximumPoolSize", "XAWork"},
                new Class[]{Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, XAWork.class}));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
