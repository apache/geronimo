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

import java.lang.reflect.Constructor;

import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkEvent;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkListener;
import javax.resource.spi.work.WorkManager;

import junit.framework.TestCase;

import org.apache.geronimo.kernel.jmx.JMXKernel;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.service.GeronimoMBeanContext;
import org.apache.geronimo.connector.work.pool.ScheduleWorkExecutorPool;
import org.apache.geronimo.connector.work.pool.StartWorkExecutorPool;
import org.apache.geronimo.connector.work.pool.SyncWorkExecutorPool;

/**
 * Timing is crucial for this test case, which focuses on the synchronization
 * specificities of the doWork, startWork and scheduleWork.
 *  
 * @version $Revision: 1.2 $ $Date: 2003/11/16 23:12:07 $
 */
public class PooledWorkManagerTest extends TestCase
{

    private JMXKernel m_kernel;
    private GeronimoWorkManager m_workManager;
    private static final int m_nbMin = 1;
    private static final int m_nbMax = 1;
    private static final int m_timeout = 300;
    private static final int m_tempo = 200;

    public PooledWorkManagerTest() throws Exception {
        super("WorkManager");
        initMinimalisticServer();
        m_workManager = new GeronimoWorkManager();

        // We are mocking the GeronimoMBeanContext
        m_workManager.setMBeanContext(new GeronimoMBeanContext(null, null, null){
            public int getState() throws Exception {
                return State.RUNNING_INDEX;
            }
        });
        
        SyncWorkExecutorPool syncWorkExecutorPool = new SyncWorkExecutorPool(1, 1);
        syncWorkExecutorPool.setGeronimoWorkManager(m_workManager);
        
        StartWorkExecutorPool startWorkExecutorPool = new StartWorkExecutorPool(1, 1);
        startWorkExecutorPool.setGeronimoWorkManager(m_workManager);
        
        ScheduleWorkExecutorPool scheduleWorkExecutorPool = new ScheduleWorkExecutorPool(1, 1);
        scheduleWorkExecutorPool.setGeronimoWorkManager(m_workManager);
    }

    public void initMinimalisticServer() throws Exception {
    }

    public void testDoWork() throws Exception {
        int nbThreads = 3;
        AbstractDummyWork threads[] = helperTest(DummyDoWork.class, nbThreads);
        int nbStopped = 0;
        int nbTimeout = 0;
        for (int i = 0; i < threads.length; i++) {
            if ( threads[i].m_listener.m_event.getType() ==
                WorkEvent.WORK_COMPLETED ) {
                nbStopped++;
            } else if ( threads[i].m_listener.m_event.getType() ==
                WorkEvent.WORK_REJECTED ) {
                assertTrue("Should be a time out exception.",
                    threads[i].m_listener.m_event.getException().
                    getErrorCode() == WorkException.START_TIMED_OUT);
                nbTimeout++;
            } else {
                assertTrue("Works should be either in the WORK_COMPLETED or " +
                    "WORK_REJECTED state", false);
            }
        }
        assertTrue("Wrong number of works in the WORK_COMPLETED state: " +
            "expected " + (nbThreads - 1 ) + "; retrieved " + nbStopped,
            (nbThreads - 1 ) == nbStopped);
        assertTrue("Wrong number of works in the START_TIMED_OUT state: " +
            "expected 1; retrieved " + nbTimeout, 1 == nbTimeout);
    }
    
    public void testStartWork() throws Exception {
        AbstractDummyWork threads[] = helperTest(DummyStartWork.class, 2);
        int nbStopped = 0;
        int nbStarted = 0;
        for (int i = 0; i < threads.length; i++) {
            if ( threads[i].m_listener.m_event.getType() ==
                WorkEvent.WORK_COMPLETED ) {
                nbStopped++;
            } else if ( threads[i].m_listener.m_event.getType() ==
                WorkEvent.WORK_STARTED ) {
                nbStarted++;
            } else {
                assertTrue("Works should be either in the WORK_COMPLETED or " +
                    "WORK_STARTED state", false);
            }
        }
        assertTrue("At least one work should be in the WORK_COMPLETED state.",
            nbStopped == 1);
        assertTrue("At least one work should be in the WORK_STARTED state.",
            nbStarted == 1);
    }

    public void testScheduleWork() throws Exception {
        AbstractDummyWork threads[] = helperTest(DummyScheduleWork.class, 3);
        int nbAccepted = 0;
        int nbStarted = 0;
        for (int i = 0; i < threads.length; i++) {
            if ( threads[i].m_listener.m_event.getType() ==
                WorkEvent.WORK_ACCEPTED ) {
                nbAccepted++;
            } else if ( threads[i].m_listener.m_event.getType() ==
                WorkEvent.WORK_STARTED ) {
                nbStarted++;
            } else {
                assertTrue("Works should be eithe in the WORK_ACCEPTED or" +
                    "  WORK_STARTED state.", false);
            }
        }
        assertTrue("At least one work should be in the WORK_ACCEPTED state.",
            nbAccepted > 0);
    }
    
    private AbstractDummyWork[] helperTest(Class aWork, int nbThreads)
        throws Exception {
        Constructor constructor = aWork.getConstructor(
            new Class[]{WorkManager.class, String.class}); 
        AbstractDummyWork rarThreads[] =
            new AbstractDummyWork[nbThreads];
        for (int i = 0; i < nbThreads; i++) {
            rarThreads[i] = (AbstractDummyWork) constructor.newInstance(
                new Object[] {m_workManager, "Work" + i});
            rarThreads[i].start();
        }
        for (int i = 0; i < nbThreads; i++) {
            rarThreads[i].join();
        }
        return rarThreads;
    }
    
    public static abstract class AbstractDummyWork extends Thread {
        public DummyWorkListener m_listener;
        protected WorkManager m_workManager;
        protected String m_name;
        public AbstractDummyWork(WorkManager aWorkManager, String aName) {
            m_workManager = aWorkManager;
            m_listener = new DummyWorkListener();
            m_name = aName;
        }
        public void run() {
            try {
                perform(new DummyWork(m_name), m_timeout, null, m_listener);                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        protected abstract void perform(Work work,
            long startTimeout,
            ExecutionContext execContext,
            WorkListener workListener) throws Exception;
    }

    public static class DummyDoWork extends AbstractDummyWork {
        public DummyDoWork(WorkManager aWorkManager, String aName) {
            super(aWorkManager, aName);
        }
        protected void perform(Work work,
            long startTimeout,
            ExecutionContext execContext,
            WorkListener workListener) throws Exception {
            m_workManager.doWork(work, startTimeout, execContext, workListener);
        }
    }
    
    public static class DummyStartWork extends AbstractDummyWork {
        public DummyStartWork(WorkManager aWorkManager, String aName) {
            super(aWorkManager, aName);
        }
        protected void perform(Work work,
            long startTimeout,
            ExecutionContext execContext,
            WorkListener workListener) throws Exception {
            m_workManager.startWork(work, startTimeout, execContext, workListener);
        }
    }
    
    public static class DummyScheduleWork extends AbstractDummyWork {
        public DummyScheduleWork(WorkManager aWorkManager, String aName) {
            super(aWorkManager, aName);
        }
        protected void perform(Work work,
            long startTimeout,
            ExecutionContext execContext,
            WorkListener workListener) throws Exception {
            m_workManager.scheduleWork(work, startTimeout, execContext, workListener);
        }
    }
    
    public static class DummyWork implements Work {
        private String m_name;
        public DummyWork(String aName) {m_name = aName;}
        public void release() {}
        public void run() {
            try {
                Thread.sleep(m_tempo);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        public String toString() {return m_name;}
    }
    
    public static class DummyWorkListener implements WorkListener {
        public WorkEvent m_event;
        public void workAccepted(WorkEvent e) {m_event = e;}
        public void workRejected(WorkEvent e) {m_event = e;}
        public void workStarted(WorkEvent e) {m_event = e;}
        public void workCompleted(WorkEvent e) {m_event = e;}
    }
}
