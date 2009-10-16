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

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class ThreadPoolTest extends TestCase {
    private final Object lock = new Object();
    private boolean ready;
    private ThreadPool threadPool;

    public void testPoolLimit() throws Exception {
        // grab the only thread in the pool
        ready = false;
        threadPool.execute(new Runnable() {
            public void run() {
                synchronized(lock) {
                    ready = true;
                    lock.notifyAll();
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        });

        // wait for up to 5 seconds for the thread above to start running
        synchronized(lock) {
            if (!ready) {
                lock.wait(5000);
            }
        }
        assertTrue(ready);

        // try to schedule another task
        try {
            threadPool.execute(new Runnable(){
                public void run() {
                }
            });
            fail("Should not have been able to schedule second task");
        } catch (RuntimeException e) {
            // expected
        }
    }

    public void setUp() throws Exception {
        threadPool = new ThreadPool(1, 1, "foo", Long.MAX_VALUE, ThreadPoolTest.class.getClassLoader(), "foo:bar=baz");
        threadPool.doStart();
    }

    public void tearDown() throws Exception {
        threadPool.doStop();
        synchronized(lock) {
            lock.notifyAll();
        }
    }
}
