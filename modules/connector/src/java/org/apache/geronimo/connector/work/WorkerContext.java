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
import javax.resource.spi.work.WorkAdapter;
import javax.resource.spi.work.WorkCompletedException;
import javax.resource.spi.work.WorkEvent;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkListener;
import javax.resource.spi.work.WorkRejectedException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.Latch;

/**
 * Work wrapper providing an execution context to a Work instance.
 *
 * @version $Revision: 1.1 $ $Date: 2004/01/23 05:56:11 $
 */
public class WorkerContext implements Work {

    private Log log = LogFactory.getLog(WorkerContext.class);

    /**
     * Null WorkListener used as the default WorkListener.
     */
    private static final WorkListener NULL_WORK_LISTENER = new WorkAdapter();

    /**
     * Priority of the thread, which will execute this work.
     */
    private int threadPriority;

    /**
     * Actual work to be executed.
     */
    private Work adaptee;

    /**
     * Indicates if this work has been accepted.
     */
    private boolean isAccepted;

    /**
     * System.currentTimeMillis() when the wrapped Work has been accepted.
     */
    private long acceptedTime;

    /**
     * Number of times that the execution of this work has been tried.
     */
    private int nbRetry;

    /**
     * Time duration (in milliseconds) within which the execution of the Work
     * instance must start.
     */
    private long startTimeOut;

    /**
     * Execution context of the actual work to be executed.
     */
    private ExecutionContext executionContext;

    /**
     * Listener to be notified during the life-cycle of the work treatment.
     */
    private WorkListener workListener = NULL_WORK_LISTENER;

    /**
     * Work exception, if any.
     */
    private WorkException workException;

    /**
     * A latch, which is released when the work is started.
     */
    private Latch startLatch = new Latch();

    /**
     * A latch, which is released when the work is completed.
     */
    private Latch endLatch = new Latch();

    /**
     * Create a WorkWrapper.
     *
     * @param aWork Work to be wrapped.
     */
    public WorkerContext(Work aWork) {
        adaptee = aWork;
    }

    /**
     * Create a WorkWrapper with the specified execution context.
     *
     * @param aWork Work to be wrapped.
     * @param aStartTimeout a time duration (in milliseconds) within which the
     * execution of the Work instance must start.
     * @param execContext an object containing the execution context with which
     * the submitted Work instance must be executed.
     * @param workListener an object which would be notified when the various
     * Work processing events (work accepted, work rejected, work started,
     * work completed) occur.
     */
    public WorkerContext(Work aWork, long aStartTimeout,
            ExecutionContext execContext,
            WorkListener workListener) {
        adaptee = aWork;
        startTimeOut = aStartTimeout;
        executionContext = execContext;
        if (null != workListener) {
            this.workListener = workListener;
        }
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.work.Work#release()
     */
    public void release() {
        adaptee.release();
    }

    /**
     * Defines the thread priority level of the thread, which will be dispatched
     * to process this work. This priority level must be the same one for a
     * given resource adapter.
     *
     * @param aPriority Priority of the thread to be used to process the wrapped
     * Work instance.
     */
    public void setThreadPriority(int aPriority) {
        threadPriority = aPriority;
    }

    /**
     * Gets the priority level of the thread, which will be dispatched
     * to process this work. This priority level must be the same one for a
     * given resource adapter.
     *
     * @return The priority level of the thread to be dispatched to
     * process the wrapped Work instance.
     */
    public int getThreadPriority() {
        return threadPriority;
    }

    /**
     * Call-back method used by a Work executor in order to notify this
     * instance that the wrapped Work instance has been accepted.
     *
     * @param anObject Object on which the event initially occurred. It should
     * be the work executor.
     */
    public synchronized void workAccepted(Object anObject) {
        isAccepted = true;
        acceptedTime = System.currentTimeMillis();
        workListener.workAccepted(new WorkEvent(anObject,
                WorkEvent.WORK_ACCEPTED, adaptee, null));
    }

    /**
     * System.currentTimeMillis() when the Work has been accepted. This method
     * can be used to compute the duration of a work.
     *
     * @return When the work has been accepted.
     */
    public synchronized long getAcceptedTime() {
        return acceptedTime;
    }

    /**
     * Gets the time duration (in milliseconds) within which the execution of
     * the Work instance must start.
     *
     * @return Time out duration.
     */
    public long getStartTimeout() {
        return startTimeOut;
    }

    /**
     * Used by a Work executor in order to know if this work, which should be
     * accepted but not started has timed out. This method MUST be called prior
     * to retry the execution of a Work.
     *
     * @return true if the Work has timed out and false otherwise.
     */
    public synchronized boolean isTimedOut() {
        assert !isAccepted: "The work is not accepted.";
        // A value of 0 means that the work never times out.
        if (0 == startTimeOut) {
            return false;
        }
        boolean isTimeout =
                System.currentTimeMillis() > acceptedTime + startTimeOut;
        if (log.isDebugEnabled()) {
            log.debug(
                    this
                    + " accepted at "
                    + acceptedTime
                    + (isTimeout ? " has timed out." : " has not timed out. ")
                    + nbRetry
                    + " retries have been performed.");
        }
        if (isTimeout) {
            workException = new WorkRejectedException(this + " has timed out.",
                    WorkException.START_TIMED_OUT);
            workListener.workRejected(
                    new WorkEvent(
                            this,
                            WorkEvent.WORK_REJECTED,
                            adaptee,
                            workException));
            return true;
        }
        nbRetry++;
        return isTimeout;
    }

    /**
     * Gets the WorkException, if any, thrown during the execution.
     *
     * @return WorkException, if any.
     */
    public synchronized WorkException getWorkException() {
        return workException;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        if (isTimedOut()) {
            // In case of a time out, one releases the start and end latches
            // to prevent a dead-lock.
            startLatch.release();
            endLatch.release();
            return;
        }
        // Implementation note: the work listener is notified prior to release
        // the start lock. This behavior is intentional and seems to be the
        // more conservative.
        workListener.workStarted(
                new WorkEvent(this, WorkEvent.WORK_STARTED, adaptee, null));
        startLatch.release();
        try {
            adaptee.run();
            workListener.workCompleted(
                    new WorkEvent(this, WorkEvent.WORK_COMPLETED, adaptee, null));
        } catch (Throwable e) {
            workException = new WorkCompletedException(e);
            workListener.workRejected(
                    new WorkEvent(this, WorkEvent.WORK_REJECTED, adaptee,
                            workException));
        } finally {
            endLatch.release();
        }
    }

    /**
     * Provides a latch, which can be used to wait the start of a work
     * execution.
     *
     * @return Latch that a caller can acquire to wait for the start of a
     * work execution.
     */
    public synchronized Latch provideStartLatch() {
        return startLatch;
    }

    /**
     * Provides a latch, which can be used to wait the end of a work
     * execution.
     *
     * @return Latch that a caller can acquire to wait for the end of a
     * work execution.
     */
    public synchronized Latch provideEndLatch() {
        return endLatch;
    }

    public String toString() {
        return "Work :" + adaptee;
    }

}
