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

import java.lang.reflect.Constructor;
import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkEvent;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkListener;

import junit.framework.TestCase;

/**
 * Timing is crucial for this test case, which focuses on the synchronization
 * specificities of the doWork, startWork and scheduleWork.
 *
 * @version $Rev$ $Date$
 */
public class PooledWorkManagerTest extends TestCase {

    private GeronimoWorkManager workManager;

    protected void setUp() throws Exception {
        workManager = new GeronimoWorkManager(1, null);
        workManager.doStart();
    }

    public void testDoWork() throws Exception {
        int nbThreads = 2;
        AbstractDummyWork threads[] =
            helperTest(DummyDoWork.class, nbThreads, 500, 600);
        int nbStopped = 0;
        int nbTimeout = 0;
        for (int i = 0; i < threads.length; i++) {
            if ( null != threads[i].listener.completedEvent ) {
                nbStopped++;
            } else if ( null != threads[i].listener.rejectedEvent ) {
                assertTrue("Should be a time out exception.",
                    threads[i].listener.rejectedEvent.getException().
                    getErrorCode() == WorkException.START_TIMED_OUT);
                nbTimeout++;
            } else {
                fail("WORK_COMPLETED or WORK_REJECTED expected");
            }
        }
        assertEquals("Wrong number of works in the WORK_COMPLETED state",
            1, nbStopped);
        assertEquals("Wrong number of works in the START_TIMED_OUT state",
            1, nbTimeout);
    }

    public void testStartWork() throws Exception {
        AbstractDummyWork threads[] =
            helperTest(DummyStartWork.class, 2, 10000, 100);
        int nbStopped = 0;
        int nbStarted = 0;
        for (int i = 0; i < threads.length; i++) {
            if ( null != threads[i].listener.completedEvent ) {
                nbStopped++;
            } else if ( null != threads[i].listener.startedEvent ) {
                nbStarted++;
            } else {
                fail("WORK_COMPLETED or WORK_STARTED expected");
            }
        }
        assertEquals("At least one work should be in the WORK_COMPLETED state.",
            1, nbStopped);
        assertEquals("At least one work should be in the WORK_STARTED state.",
            1, nbStarted);
    }

    public void testScheduleWork() throws Exception {
        AbstractDummyWork threads[] =
            helperTest(DummyScheduleWork.class, 3, 10000, 100);
        int nbAccepted = 0;
        int nbStarted = 0;
        for (int i = 0; i < threads.length; i++) {
            if ( null != threads[i].listener.acceptedEvent ) {
                nbAccepted++;
            } else if ( null != threads[i].listener.startedEvent ) {
                nbStarted++;
            } else {
                fail("WORK_ACCEPTED or WORK_STARTED expected");
            }
        }
        assertTrue("At least one work should be in the WORK_ACCEPTED state.",
            nbAccepted > 0);
    }

    public void testLifecycle() throws Exception {
        testDoWork();
        workManager.doStop();
        workManager.doStart();
        testDoWork();
    }

    private AbstractDummyWork[] helperTest(Class aWork, int nbThreads,
        int aTimeOut, int aTempo)
        throws Exception {
        Constructor constructor = aWork.getConstructor(
            new Class[]{PooledWorkManagerTest.class, String.class,
                int.class, int.class});
        AbstractDummyWork rarThreads[] = new AbstractDummyWork[nbThreads];
        for (int i = 0; i < nbThreads; i++) {
            rarThreads[i] = (AbstractDummyWork)
                constructor.newInstance(
                    new Object[]{this, "Work" + i,
                        new Integer(aTimeOut), new Integer(aTempo)});
        }
        for (int i = 0; i < nbThreads; i++) {
            rarThreads[i].start();
        }
        for (int i = 0; i < nbThreads; i++) {
            rarThreads[i].join();
        }
        return rarThreads;
    }

    public abstract class AbstractDummyWork extends Thread {
        public final DummyWorkListener listener;
        protected final  String name;
        private final int timeout;
        private final int tempo;
        public AbstractDummyWork(String aName, int aTimeOut, int aTempo) {
            listener = new DummyWorkListener();
            timeout = aTimeOut;
            tempo = aTempo;
            name = aName;
        }
        public void run() {
            try {
                perform(new DummyWork(name, tempo), timeout, null, listener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        protected abstract void perform(Work work,
                long startTimeout,
                ExecutionContext execContext,
                WorkListener workListener) throws Exception;
    }

    public class DummyDoWork extends AbstractDummyWork {
        public DummyDoWork(String aName, int aTimeOut, int aTempo) {
            super(aName, aTimeOut, aTempo);
        }

        protected void perform(Work work,
                long startTimeout,
                ExecutionContext execContext,
                WorkListener workListener) throws Exception {
            workManager.doWork(work, startTimeout, execContext, workListener);
        }
    }

    public class DummyStartWork extends AbstractDummyWork {
        public DummyStartWork(String aName, int aTimeOut, int aTempo) {
            super(aName, aTimeOut, aTempo);
        }

        protected void perform(Work work,
                long startTimeout,
                ExecutionContext execContext,
                WorkListener workListener) throws Exception {
            workManager.startWork(work, startTimeout, execContext, workListener);
        }
    }

    public class DummyScheduleWork extends AbstractDummyWork {
        public DummyScheduleWork(String aName, int aTimeOut, int aTempo) {
            super(aName, aTimeOut, aTempo);
        }

        protected void perform(Work work,
                long startTimeout,
                ExecutionContext execContext,
                WorkListener workListener) throws Exception {
            workManager.scheduleWork(work, startTimeout, execContext, workListener);
        }
    }

    public static class DummyWork implements Work {
        private final String name;
        private final int tempo;

        public DummyWork(String aName, int aTempo) {
            name = aName;
            tempo = aTempo;
        }

        public void release() {
        }

        public void run() {
            try {
                Thread.sleep(tempo);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public String toString() {
            return name;
        }
    }

    public static class DummyWorkListener implements WorkListener {
        public WorkEvent acceptedEvent;
        public WorkEvent rejectedEvent;
        public WorkEvent startedEvent;
        public WorkEvent completedEvent;

        public void workAccepted(WorkEvent e) {
            acceptedEvent = e;
            System.out.println("accepted" + e);
        }

        public void workRejected(WorkEvent e) {
            rejectedEvent = e;
            System.out.println("rejected" + e);
        }

        public void workStarted(WorkEvent e) {
            startedEvent = e;
            System.out.println("started" + e);
        }

        public void workCompleted(WorkEvent e) {
            completedEvent = e;
            System.out.println("completed" + e);
        }
    }
}
