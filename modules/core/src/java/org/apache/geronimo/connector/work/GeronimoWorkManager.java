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

import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkListener;
import javax.resource.spi.work.WorkManager;

import org.apache.geronimo.connector.work.pool.ScheduleWorkExecutorPool;
import org.apache.geronimo.connector.work.pool.StartWorkExecutorPool;
import org.apache.geronimo.connector.work.pool.SyncWorkExecutorPool;
import org.apache.geronimo.connector.work.pool.WorkExecutorPool;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.kernel.service.GeronimoAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.gbean.GConstructorInfo;

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
 * @version $Revision: 1.6 $ $Date: 2004/01/20 06:13:38 $
 */
public class GeronimoWorkManager implements WorkManager {

    private final static int DEFAULT_MIN_POOL_SIZE = 0;
    private final static int DEFAULT_MAX_POOL_SIZE = 10;

    private static final GBeanInfo GBEAN_INFO;

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

    /**
     * Create a WorkManager.
     */
    public GeronimoWorkManager() {
        this(DEFAULT_MIN_POOL_SIZE, DEFAULT_MAX_POOL_SIZE);
    }

    public GeronimoWorkManager(int minSize, int maxSize) {
        this(minSize, maxSize, minSize, maxSize, minSize, maxSize);
    }

    public GeronimoWorkManager(int syncMinSize, int syncMaxSize, int startMinSize, int startMaxSize, int schedMinSize, int schedMaxSize) {
        syncWorkExecutorPool = new SyncWorkExecutorPool(syncMinSize, syncMaxSize);
        startWorkExecutorPool = new StartWorkExecutorPool(startMinSize, startMaxSize);
        scheduledWorkExecutorPool = new ScheduleWorkExecutorPool(schedMinSize, schedMaxSize);
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
                new WorkerContext(work, startTimeout, execContext, workListener);
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
                new WorkerContext(work, startTimeout, execContext, workListener);
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
                new WorkerContext(work, startTimeout, execContext, workListener);
        workWrapper.setThreadPriority(Thread.currentThread().getPriority());
        scheduledWorkExecutorPool.executeWork(workWrapper);
    }

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(GeronimoWorkManager.class.getName());
        infoFactory.addAttribute(new GAttributeInfo("SyncThreadCount", true));
        infoFactory.addAttribute(new GAttributeInfo("SyncMinimumPoolSize", true));
        infoFactory.addAttribute(new GAttributeInfo("SyncMaximumPoolSize", true));
        infoFactory.addAttribute(new GAttributeInfo("StartThreadCount", true));
        infoFactory.addAttribute(new GAttributeInfo("StartMinimumPoolSize", true));
        infoFactory.addAttribute(new GAttributeInfo("StartMaximumPoolSize", true));
        infoFactory.addAttribute(new GAttributeInfo("ScheduledThreadCount", true));
        infoFactory.addAttribute(new GAttributeInfo("ScheduledMinimumPoolSize", true));
        infoFactory.addAttribute(new GAttributeInfo("ScheduledMaximumPoolSize", true));
        infoFactory.addOperation(new GOperationInfo("doWork", new String[]{Work.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("doWork", new String[]{Work.class.getName(), Long.TYPE.getName(), ExecutionContext.class.getName(), WorkListener.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("startWork", new String[]{Work.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("startWork", new String[]{Work.class.getName(), Long.TYPE.getName(), ExecutionContext.class.getName(), WorkListener.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("scheduleWork", new String[]{Work.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("scheduleWork", new String[]{Work.class.getName(), Long.TYPE.getName(), ExecutionContext.class.getName(), WorkListener.class.getName()}));
        infoFactory.setConstructor(new GConstructorInfo(
                new String[] {"SyncMinimumPoolSize", "SyncMaximumPoolSize", "StartMinimumPoolSize", "StartMaximumPoolSize", "ScheduledMinimumPoolSize", "ScheduledMaximumPoolSize"},
                new Class[] {Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE}));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    /**
     * Provides the GeronimoMBean description for this class
     * @return
     */
    public static GeronimoMBeanInfo getGeronimoMBeanInfo() throws Exception {

        GeronimoMBeanInfo rc = new GeronimoMBeanInfo();
        rc.setTargetClass(GeronimoWorkManager.class);
        rc.addOperationsDeclaredIn(WorkManager.class);
        rc.addAttributeInfo(new GeronimoAttributeInfo("SyncThreadCount", true, false, "Actual size of sync thread pool"));
        rc.addAttributeInfo(new GeronimoAttributeInfo("SyncMinimumPoolSize", true, true, "Minimum size of sync thread pool"));
        rc.addAttributeInfo(new GeronimoAttributeInfo("SyncMaximumPoolSize", true, true, "Maximum size of sync thread pool"));
        rc.addAttributeInfo(new GeronimoAttributeInfo("StartThreadCount", true, false, "Actual size of sync thread pool"));
        rc.addAttributeInfo(new GeronimoAttributeInfo("StartMinimumPoolSize", true, true, "Minimum size of sync thread pool"));
        rc.addAttributeInfo(new GeronimoAttributeInfo("StartMaximumPoolSize", true, true, "Maximum size of sync thread pool"));
        rc.addAttributeInfo(new GeronimoAttributeInfo("ScheduledThreadCount", true, false, "Actual size of sync thread pool"));
        rc.addAttributeInfo(new GeronimoAttributeInfo("ScheduledMinimumPoolSize", true, true, "Minimum size of sync thread pool"));
        rc.addAttributeInfo(new GeronimoAttributeInfo("ScheduledMaximumPoolSize", true, true, "Maximum size of sync thread pool"));
        return rc;

    }

}
