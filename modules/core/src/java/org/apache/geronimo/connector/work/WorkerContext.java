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
 * @version $Revision: 1.1 $ $Date: 2003/11/16 22:42:20 $
 */
public class WorkerContext implements Work
{

    private Log m_log = LogFactory.getLog(WorkerContext.class);

    /**
     * Null WorkListener used as the default WorkListener. 
     */    
    private static final WorkListener NULL_WORK_LISTENER = new WorkAdapter();

    /**
     * Priority of the thread which will execute this work.
     */
    private int m_threadPriority;

    /**
     * Actual work to be executed.
     */        
    private Work m_adaptee;

    /**
     * Indicates if this work has been accepted.
     */
    private boolean m_isAccepted;

    /**
     * System.currentTimeMillis() when the Work has been accepted.
     */
    private long m_acceptedTime;

    /**
     * Number of times that the execution of this work has been tried.
     */
    private int m_nbRetry;

    /**
     * time duration (in milliseconds) within which the execution of the Work 
     * instance must start.
     */
    private long m_startTimeOut;
    
    /**
     * execution context of the actual work to be executed.
     */
    private ExecutionContext m_executionContext;
    
    /**
     * Listener to be notified during the life-cycle of the work treatment. 
     */
    private WorkListener m_workListener = NULL_WORK_LISTENER;
    
    /**
     * Work exception if any.
     */
    private WorkException m_workException;

    /**
     * A latch which is released when the work is started.
     */
    private Latch m_startLatch = new Latch();
    
    /**
     * A latch which is released when the work is completed.
     */
    private Latch m_endLatch = new Latch();

    /**
     * Create a WorkWrapper.
     * 
     * @param aWork Work wrapped by this instance.
     */        
    public WorkerContext(Work aWork) {
        m_adaptee = aWork;
    }
        
    /**
     * Create a WorkWrapper with the specified execution context.
     * 
     * @param aWork Work wrapped by this instance.
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
        m_adaptee = aWork;
        m_startTimeOut = aStartTimeout;
        m_executionContext = execContext;
        if ( null != workListener ) {
            m_workListener = workListener;     
        }
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.work.Work#release()
     */
    public void release() {
        m_adaptee.release();            
    }

    /**
     * Defines the thread priority level of the thread which will be dispatched
     * to process this work. This priority level must be the same one for a
     * given resource adapter. 
     */
    public void setThreadPriority(int aPriority) {
        m_threadPriority = aPriority;
    }

    /**
     * Gets the thread priority level of the thread which will be dispatched
     * to process this work. This priority level must be the same one for a
     * given resource adapter. 
     */
    public int getThreadPriority() {
        return m_threadPriority;
    }

    /**
     * Used by a Work executor in order to notify this work that it has been
     * accepted.
     * 
     * @param anObject Object on which the event initially occurred. It should
     * be the work executor.
     */
    public synchronized void workAccepted(Object anObject) {
        m_isAccepted = true;
        m_acceptedTime = System.currentTimeMillis();
        m_workListener.workAccepted(new WorkEvent(anObject,
            WorkEvent.WORK_ACCEPTED, m_adaptee, null));
    }

    /**
     * System.currentTimeMillis() when the Work has been accepted.
     *
     * @return when the work has ben accepted.
     */
    public synchronized long getAcceptedTime() {
        return m_acceptedTime;
    }

    /**
     * Gets the time duration (in milliseconds) within which the execution of 
     * the Work instance must start.
     * 
     * @return time out duration.
     */
    public long getStartTimeout() {
        return m_startTimeOut;
    }

    /**
     * Used by a Work executor in order to know if this work, which should be
     * accepted but not started has timed out. This method MUST be called prior
     * to retry the execution of a Work.
     * 
     * @return true if the work has timed out and false otherwise.
     */
    public synchronized boolean isTimedOut() {
        assert !m_isAccepted: "The work is not accepted.";
        // A value of 0 means that the work never times out.
        if ( 0 == m_startTimeOut ) {
           return false;
        }
        boolean isTimeout =
            System.currentTimeMillis() > m_acceptedTime + m_startTimeOut;
        if ( m_log.isDebugEnabled() ) {
            m_log.debug(this + " accepted at " + m_acceptedTime + 
            (isTimeout? " has timed out.":" has not timed out. ") +
            m_nbRetry + " retries have been performed.");        
        }
        if ( isTimeout ) {
            m_workException = new WorkRejectedException("Time out.",
                WorkException.START_TIMED_OUT);
            m_workListener.workRejected(
                new WorkEvent(this, WorkEvent.WORK_REJECTED, m_adaptee,
                m_workException));
            return true; 
        }
        m_nbRetry++;
        return isTimeout;
    }

    /**
     * Gets the WorkException, if any, thrown during this work execution.
     * 
     * @return WorkException, if any.
     */
    public synchronized WorkException getWorkException() {
        return m_workException;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        if ( isTimedOut() ) {
            // In case of a time out, one releases the start and end latches
            // to prevent a dead-lock.
            m_startLatch.release();
            m_endLatch.release();
            return;
        }
        // Implementation note: the work listener is notified prior to release
        // the start lock. This behavior is intentional and seems to be the
        // more conservative.
        m_workListener.workStarted(
            new WorkEvent(this, WorkEvent.WORK_STARTED, m_adaptee, null));
        m_startLatch.release();
        try {
            m_adaptee.run();
            m_workListener.workCompleted(
                new WorkEvent(this, WorkEvent.WORK_COMPLETED, m_adaptee, null));
        } catch (Throwable e) {
            m_workException = new WorkCompletedException(e);
            m_workListener.workRejected(
                new WorkEvent(this, WorkEvent.WORK_REJECTED, m_adaptee,
                m_workException));
        } finally  {
            m_endLatch.release();
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
        return m_startLatch;
    }

    /**
     * Provides a latch, which can be used to wait the end of a work 
     * execution.
     * 
     * @return Latch that a caller can acquire to wait for the end of a 
     * work execution.
     */
    public synchronized Latch provideEndLatch() {
        return m_endLatch;
    }

    public String toString() {
        return "Work :" + m_adaptee;  
    }
    
}
