/**
 *
 * Copyright 2004 The Apache Software Foundation
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

import java.lang.reflect.Constructor;
import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkEvent;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkListener;
import javax.resource.spi.work.WorkManager;

import org.apache.geronimo.kernel.jmx.JMXKernel;

import junit.framework.TestCase;

/**
 * Timing is crucial for this test case, which focuses on the synchronization
 * specificities of the doWork, startWork and scheduleWork.
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:57:13 $
 */
public class PooledWorkManagerTest extends TestCase {

    private JMXKernel m_kernel;
    private GeronimoWorkManager m_workManager;
    private static final int m_nbMin = 1;
    private static final int m_nbMax = 1;
    private static final int m_timeout = 300;
    private static final int m_tempo = 200;

    protected void setUp() throws Exception {
        m_workManager = new GeronimoWorkManager(1, 1, null);
    }

    public void testDoWork() throws Exception {
        int nbThreads = 3;
        AbstractDummyWork threads[] = helperTest(DummyDoWork.class, nbThreads);
        int nbStopped = 0;
        int nbTimeout = 0;
        for (int i = 0; i < threads.length; i++) {
            if (threads[i].m_listener.m_event.getType() ==
                    WorkEvent.WORK_COMPLETED) {
                nbStopped++;
            } else if (threads[i].m_listener.m_event.getType() ==
                    WorkEvent.WORK_REJECTED) {
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
                "expected " + (nbThreads - 1) + "; retrieved " + nbStopped,
                (nbThreads - 1) == nbStopped);
        assertTrue("Wrong number of works in the START_TIMED_OUT state: " +
                "expected 1; retrieved " + nbTimeout, 1 == nbTimeout);
    }

    public void testStartWork() throws Exception {
        AbstractDummyWork threads[] = helperTest(DummyStartWork.class, 2);
        int nbStopped = 0;
        int nbStarted = 0;
        for (int i = 0; i < threads.length; i++) {
            if (threads[i].m_listener.m_event.getType() ==
                    WorkEvent.WORK_COMPLETED) {
                nbStopped++;
            } else if (threads[i].m_listener.m_event.getType() ==
                    WorkEvent.WORK_STARTED) {
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
            if (threads[i].m_listener.m_event.getType() ==
                    WorkEvent.WORK_ACCEPTED) {
                nbAccepted++;
            } else if (threads[i].m_listener.m_event.getType() ==
                    WorkEvent.WORK_STARTED) {
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
                    new Object[]{m_workManager, "Work" + i});
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

        public DummyWork(String aName) {
            m_name = aName;
        }

        public void release() {
        }

        public void run() {
            try {
                Thread.sleep(m_tempo);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public String toString() {
            return m_name;
        }
    }

    public static class DummyWorkListener implements WorkListener {
        public WorkEvent m_event;

        public void workAccepted(WorkEvent e) {
            m_event = e;
        }

        public void workRejected(WorkEvent e) {
            m_event = e;
        }

        public void workStarted(WorkEvent e) {
            m_event = e;
        }

        public void workCompleted(WorkEvent e) {
            m_event = e;
        }
    }
}
