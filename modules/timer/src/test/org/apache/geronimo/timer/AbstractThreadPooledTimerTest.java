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

package org.apache.geronimo.timer;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;
import junit.framework.TestCase;
import org.apache.geronimo.pool.ThreadPool;
import org.apache.geronimo.transaction.context.TransactionContext;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.timer.vm.VMWorkerPersistence;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public abstract class AbstractThreadPooledTimerTest extends TestCase {

    private static final int COUNT = 20; //should run with much higher counts, but fails sometimes on slow hardware.
    private static final long DELAY = 1000;
    private static final long SLOP = 200;

    private static final String key = "testThreadPooledTimer";
    private Object userId = null;
    private ThreadPool threadPool;
    private ThreadPooledTimer timer;

    private SynchronizedInt counter = new SynchronizedInt(0);
    protected TransactionContextManager transactionContextManager;
    protected ExecutorTaskFactory executableWorkFactory;
    protected UserTaskFactory userTaskFactory;

    private Object userKey = "test user info";

    protected void setUp() throws Exception {
        userTaskFactory = new MockUserTaskFactory();
        threadPool = new ThreadPool(30, "TestPool", 10000, this.getClass().getClassLoader());
        WorkerPersistence workerPersistence = new VMWorkerPersistence();
        timer = new ThreadPooledTimer(executableWorkFactory, workerPersistence, threadPool, transactionContextManager);
        timer.doStart();

        counter.set(0);
        transactionContextManager.setContext(null);
    }

    protected void tearDown() throws Exception {
        timer.doStop();
        threadPool.doStop();
    }

    public void testTasks() throws Exception {
        for (long i = 0; i < COUNT; i++) {
            timer.schedule(userTaskFactory, key, userId, userKey, i);
        }
        Thread.sleep(COUNT + SLOP);
        assertEquals(COUNT, counter.get());
    }

    public void testCancel() throws Exception {
        WorkInfo[] workInfos = new WorkInfo[COUNT];
        for (long i = 0; i < COUNT; i++) {
            workInfos[(int) i] = timer.schedule(userTaskFactory, key, userId, userKey, DELAY);
        }
        for (int i = 0; i < workInfos.length; i++) {
            workInfos[i].getExecutorFeedingTimerTask().cancel();
        }
        Thread.sleep(SLOP + DELAY);
        assertEquals(0, counter.get());
    }

    public void testPersistence() throws Exception {
        for (long i = 0; i < COUNT; i++) {
            timer.schedule(userTaskFactory, key, userId, userKey, DELAY);
        }
        timer.doStop();
        assertEquals(0, counter.get());

        timer.doStart();
        timer.playback(key, userTaskFactory);
        Thread.sleep(2 * SLOP + DELAY);
        assertEquals(COUNT, counter.get());
    }

    public void testTasksInUnspecifiedTxContext() throws Exception {
        transactionContextManager.newUnspecifiedTransactionContext();
        try {
            testTasks();
        } finally {
            transactionContextManager.setContext(null);
        }
    }

    public void testCancelInUnspecifiedTxContext() throws Exception {
        transactionContextManager.newUnspecifiedTransactionContext();
        try {
            testCancel();
        } finally {
            transactionContextManager.setContext(null);
        }
    }

    public void testPersistenceInUnspecifiedTxContext() throws Exception {
        transactionContextManager.newUnspecifiedTransactionContext();
        try {
            testPersistence();
        } finally {
            transactionContextManager.setContext(null);
        }
    }

    public void testTasksInTransaction() throws Exception {
        TransactionContext transactionContext = transactionContextManager.newContainerTransactionContext();
        for (long i = 0; i < COUNT; i++) {
            timer.schedule(userTaskFactory, key, userId, userKey, i);
        }
        Thread.sleep(COUNT + SLOP);
        assertEquals(0, counter.get());
        transactionContext.commit();
        Thread.sleep(COUNT + SLOP);
        assertEquals(COUNT, counter.get());
    }

    public void testCancelInCommittedTransaction() throws Exception {
        Thread.sleep(SLOP + DELAY);
        WorkInfo[] workInfos = new WorkInfo[COUNT];
        for (long i = 0; i < COUNT; i++) {
            workInfos[(int) i] = timer.scheduleAtFixedRate(key, userTaskFactory, userId, userKey, DELAY, DELAY);
        }
        Thread.sleep(SLOP + DELAY);
        assertEquals(COUNT, counter.get());
        TransactionContext transactionContext = transactionContextManager.newContainerTransactionContext();
        for (int i = 0; i < workInfos.length; i++) {
            workInfos[i].getExecutorFeedingTimerTask().cancel();
        }
        Thread.sleep(SLOP + DELAY);
        assertEquals(COUNT, counter.get());
        transactionContext.commit();
        Thread.sleep(SLOP + DELAY);
        assertEquals(COUNT, counter.get());
    }

    public void testCancelInRolledBackTransaction() throws Exception {
        Thread.sleep(SLOP + DELAY);
        WorkInfo[] workInfos = new WorkInfo[COUNT];
        for (long i = 0; i < COUNT; i++) {
            workInfos[(int) i] = timer.scheduleAtFixedRate(key, userTaskFactory, userId, userKey, DELAY, DELAY);
        }
        Thread.sleep(SLOP + DELAY);
        assertEquals(COUNT, counter.get());
        TransactionContext transactionContext = transactionContextManager.newContainerTransactionContext();
        for (int i = 0; i < workInfos.length; i++) {
            workInfos[i].getExecutorFeedingTimerTask().cancel();
        }
        Thread.sleep(SLOP + DELAY);
        assertEquals(COUNT, counter.get());
        transactionContext.rollback();
        Thread.sleep(SLOP + DELAY);
        // Catches up with two periods.
        assertEquals(3 * COUNT, counter.get());
    }

    public void testRepeatCountFromPersisted() throws Exception {
        assert DELAY > 2 * SLOP;
        timer.scheduleAtFixedRate(key, userTaskFactory, userId, userKey, 0L, DELAY);
        Thread.sleep(4 * DELAY + SLOP);
        timer.doStop();
        assertEquals(5, counter.get());

        timer.doStart();
        timer.playback(key, userTaskFactory);
        Thread.sleep(5 * DELAY + SLOP);
        assertEquals(2 * 5, counter.get());

    }

    private class MockUserTaskFactory implements UserTaskFactory {
        public Runnable newTask(long id) {
            return new Runnable() {
                public void run() {
                    counter.increment();
                }

            };
        }

    }

}
