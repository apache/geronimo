/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.pool;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class ThreadPoolTimeoutTest extends TestCase {

    private ThreadPool threadPool;

    private SleepRunnable firstRunnable;

    private SleepRunnable secondRunnable;

    public void setUp() throws Exception {
        threadPool = new ThreadPool(1, 1, "abc", Long.MAX_VALUE, ThreadPoolTimeoutTest.class.getClassLoader(), "abc:pool=pool");
        threadPool.doStart();
        firstRunnable = new SleepRunnable(2000);
        secondRunnable = new SleepRunnable(0);
    }

    public void testFailWaitWhenBlocked() {

        threadPool.setWaitWhenBlocked(true);
        // occupy the only thread in the pool
        threadPool.execute(firstRunnable);

        try {
            // timeout is less than sleep time of firstRunnable
            threadPool.execute(secondRunnable, 1, TimeUnit.SECONDS);
            fail();
        } catch (RejectedExecutionException e) {
            // expected behavior
            assertFalse(secondRunnable.started);
        }
    }

    public void testFailAbortWhenBlocked() {

        threadPool.setWaitWhenBlocked(false);

        // occupy the only thread in the pool
        threadPool.execute(firstRunnable);

        try {
            // timeout is less than sleep time of firstRunnable
            threadPool.execute(secondRunnable, 1, TimeUnit.SECONDS);
            fail();
        } catch (RejectedExecutionException e) {
            // expected behavior
            assertFalse(secondRunnable.started);
        }
    }

    public void testSuccessWaitWhenBlocked() {

        threadPool.setWaitWhenBlocked(true);

        // occupy the only thread in the pool
        threadPool.execute(firstRunnable);

        try {
            // timeout is more than sleep time of firstRunnable, so secondRunnable will get executed
            threadPool.execute(secondRunnable, 3, TimeUnit.SECONDS);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            assertTrue(secondRunnable.started);
        } catch (RejectedExecutionException e) {
            fail();
        }
    }

    public void testSuccessAbortWhenBlocked() {

        threadPool.setWaitWhenBlocked(false);

        // occupy the only thread in the pool
        threadPool.execute(firstRunnable);

        try {
            // timeout is more than sleep time of firstRunnable, so secondRunnable will get executed
            threadPool.execute(secondRunnable, 3, TimeUnit.SECONDS);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            assertTrue(secondRunnable.started);
        } catch (RejectedExecutionException e) {
            fail();
        }
    }

    class SleepRunnable implements Runnable {

        private long time;

        boolean started;

        boolean ended;

        SleepRunnable(long t) {
            this.time = t;
            started = false;
            ended = false;
        }

        public void run() {
            try {
                started = true;
                Thread.sleep(time);
                ended = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void tearDown() throws Exception {
        Thread.sleep(3000);
        threadPool.doStop();
    }
}
