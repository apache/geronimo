/**
 *
 * Copyright 2006 The Apache Software Foundation
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
package org.apache.geronimo.commonj.timers;

import java.util.Date;

import commonj.timers.Timer;
import commonj.timers.TimerListener;
import commonj.timers.TimerManager;

import junit.framework.TestCase;

public class TimerManagerTest extends TestCase {

    private static final long HEDGE = 100L;

    private long delay = 1000L;

    private int counter;

    private long scheduleTime;

    private long expirationTime;

    private TimerManager timerManager = null;

    private long period = 500L;

    protected void setUp() throws Exception {
        super.setUp();

        timerManager = new TimerManagerImpl();
    }

    protected void tearDown() throws Exception {
        super.tearDown();

        timerManager.stop();
    }

    /*
     * Test method for
     * 'org.apache.geronimo.commonj.timers.TimerManagerImpl.TimerManagerImpl()'
     */
    public void testTimerManagerImpl() {
        assertFalse(timerManager.isSuspending());
        assertFalse(timerManager.isSuspended());
        assertFalse(timerManager.isStopping());
        assertFalse(timerManager.isStopped());
    }

    /*
     * Test method for
     * 'org.apache.geronimo.commonj.timers.TimerManagerImpl.suspend()'
     */
    public void testSuspend() throws Exception {
        suspendTest(1);
    }

    public void testSuspend_N() throws Exception {
        suspendTest(10);
    }

    private void suspendTest(int timerCount) throws Exception {
        counter = 0;
        scheduleTime = System.currentTimeMillis();
        expirationTime = 0;

        for (int i = 0; i < timerCount; i++) {
            timerManager.schedule(new TimerListener() {
                public void timerExpired(Timer timer) {
                    counter++;
                    expirationTime = System.currentTimeMillis();
                }
            }, new Date(System.currentTimeMillis() + delay));
        }

        timerManager.suspend();

        assertTrue(timerManager.isSuspending());
        assertTrue(timerManager.isSuspended());

        Thread.sleep(delay + HEDGE);

        assertTrue(counter == 0);
        timerManager.resume();
        Thread.sleep(delay + HEDGE);

        assertTrue(counter == timerCount);
    }

    /*
     * Test method for
     * 'org.apache.geronimo.commonj.timers.TimerManagerImpl.isSuspending()'
     */
    public void testIsSuspending() throws Exception {
        counter = 0;
        scheduleTime = System.currentTimeMillis();
        expirationTime = 0;

        timerManager.schedule(new TimerListener() {
            public void timerExpired(Timer timer) {
                counter++;
                expirationTime = System.currentTimeMillis();
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    throw new RuntimeException(ie);
                }
                counter++;
            }
        }, new Date(System.currentTimeMillis()));

        Thread.sleep(HEDGE);

        timerManager.waitForSuspend(HEDGE);

        assertTrue(timerManager.isSuspending());
        assertTrue(counter == 1);
        assertFalse(timerManager.isSuspended());

        Thread.sleep(delay + HEDGE);

        assertTrue(counter == 2);
        assertTrue(timerManager.isSuspended());

        timerManager.resume();

        assertTrue(counter == 2);
        long measuredDelay = expirationTime - scheduleTime;
        assertTrue(measuredDelay < HEDGE);
        assertFalse(timerManager.isSuspending());
        assertFalse(timerManager.isSuspended());
    }

    public void testIsSuspending_N() throws Exception {
        counter = 0;
        scheduleTime = System.currentTimeMillis();
        expirationTime = 0;

        for (int i = 0; i < 10; i++) {
            timerManager.schedule(new TimerListener() {
                public void timerExpired(Timer timer) {
                    counter++;
                    expirationTime = System.currentTimeMillis();
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        throw new RuntimeException(ie);
                    }
                    counter++;
                }
            }, new Date(System.currentTimeMillis()));
        }

        Thread.sleep(HEDGE);

        timerManager.waitForSuspend(HEDGE);

        assertTrue(timerManager.isSuspending());
        assertTrue(counter == 10);

        Thread.sleep(delay + HEDGE);

        assertTrue(counter == 20);
        assertTrue(timerManager.isSuspended());

        timerManager.resume();

        assertTrue(counter == 20);
        // long measuredDelay = expirationTime - scheduleTime;
        // assertTrue(measuredDelay <HEDGE);
        assertFalse(timerManager.isSuspending());
        assertFalse(timerManager.isSuspended());
    }

    /*
     * Test method for
     * 'org.apache.geronimo.commonj.timers.TimerManagerImpl.isSuspended()'
     */
    public void testIsSuspended() {
        assertFalse(timerManager.isSuspended());
        timerManager.suspend();
        assertTrue(timerManager.isSuspended());
        timerManager.resume();
        assertFalse(timerManager.isSuspended());
    }

    /*
     * Test method for
     * 'org.apache.geronimo.commonj.timers.TimerManagerImpl.stop()'
     */
    public void testStop() throws Exception {
        stopTest(1);
    }

    public void testStop_N() throws Exception {
        stopTest(10);
    }

    private void stopTest(int timerCount) throws Exception {
        counter = 0;
        scheduleTime = System.currentTimeMillis();
        expirationTime = 0;

        for (int i = 0; i < timerCount; i++) {
            timerManager.schedule(new TimerListener() {
                public void timerExpired(Timer timer) {
                    counter++;
                    expirationTime = System.currentTimeMillis();
                }
            }, new Date(System.currentTimeMillis() + delay));
        }

        timerManager.stop();

        assertTrue(timerManager.isStopped());
        assertTrue(timerManager.isStopping());
        assertFalse(timerManager.isSuspending());
        assertFalse(timerManager.isSuspended());

        Thread.sleep(delay + HEDGE);

        assertTrue(counter == 0);

        boolean passed = false;
        try {
            timerManager.resume();
        } catch (IllegalStateException ise) {
            passed = true;
        }
        assertTrue(passed);

        passed = false;
        try {
            timerManager.schedule(new TimerListener() {
                public void timerExpired(Timer timer) {
                }
            }, new Date());
        } catch (IllegalStateException ise) {
            passed = true;
        }
        assertTrue(passed);

        passed = false;
        try {
            timerManager.schedule(new TimerListener() {
                public void timerExpired(Timer timer) {
                }
            }, HEDGE);
        } catch (IllegalStateException ise) {
            passed = true;
        }
        assertTrue(passed);

        passed = false;
        try {
            timerManager.schedule(new TimerListener() {
                public void timerExpired(Timer timer) {
                }
            }, new Date(), delay);
        } catch (IllegalStateException ise) {
            passed = true;
        }
        assertTrue(passed);

        passed = false;
        try {
            timerManager.schedule(new TimerListener() {
                public void timerExpired(Timer timer) {
                }
            }, HEDGE, delay);
        } catch (IllegalStateException ise) {
            passed = true;
        }
        assertTrue(passed);

        passed = false;
        try {
            timerManager.scheduleAtFixedRate(new TimerListener() {
                public void timerExpired(Timer timer) {
                }
            }, new Date(), delay);
        } catch (IllegalStateException ise) {
            passed = true;
        }
        assertTrue(passed);

        passed = false;
        try {
            timerManager.scheduleAtFixedRate(new TimerListener() {
                public void timerExpired(Timer timer) {
                }
            }, HEDGE, delay);
        } catch (IllegalStateException ise) {
            passed = true;
        }
        assertTrue(passed);

        passed = false;
        try {
            timerManager.suspend();
        } catch (IllegalStateException ise) {
            passed = true;
        }
        assertTrue(passed);

        passed = false;
        try {
            timerManager.waitForSuspend(0);
        } catch (IllegalStateException ise) {
            passed = true;
        }
        assertTrue(passed);

        passed = true;
        try {
            timerManager.stop();
        } catch (IllegalStateException ise) {
            passed = false;
        }
        assertTrue(passed);

        passed = true;
        try {
            timerManager.waitForStop(0);
        } catch (IllegalStateException ise) {
            passed = false;
        }
        assertTrue(passed);
        timerManager = new TimerManagerImpl();
    }

    /*
     * Test method for
     * 'org.apache.geronimo.commonj.timers.TimerManagerImpl.waitForStop(long)'
     */
    public void testWaitForStop() throws Exception {
        waitForStopTest(1);
    }

    public void waitForStopTest(int timerCount) throws Exception {
        initializeTest();

        for (int i = 0; i < timerCount; i++) {
            timerManager.schedule(new TimerListener() {
                public void timerExpired(Timer timer) {
                    counter++;
                    expirationTime = System.currentTimeMillis();
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        throw new RuntimeException(ie);
                    }
                    counter++;
                }
            }, new Date(System.currentTimeMillis()));
        }

        Thread.sleep(HEDGE);

        timerManager.waitForStop(HEDGE);

        assertTrue(timerManager.isStopping());
        assertTrue(counter == timerCount);

        Thread.sleep(delay + HEDGE);

        assertTrue(counter == 2*timerCount);
        assertTrue(timerManager.isStopped());

        timerManager = new TimerManagerImpl();
    }

    /*
     * Test method for
     * 'org.apache.geronimo.commonj.timers.TimerManagerImpl.schedule(TimerListener,
     * Date)'
     */
    public void testScheduleTimerListenerDate() throws Exception {
        initializeTest();

        timerManager.schedule(new TimerListener() {
            public void timerExpired(Timer timer) {
                counter++;
                expirationTime = System.currentTimeMillis();
            }
        }, new Date(System.currentTimeMillis() + delay));

        Thread.sleep(HEDGE);
        assertTrue(counter == 0);

        Thread.sleep(delay);
        assertTrue(counter == 1);

        long measuredDelay = expirationTime - scheduleTime;
        assertTrue(measuredDelay > 0L);
        assertTrue(Math.abs(measuredDelay - delay) < HEDGE);
    }

    /*
     * Test method for
     * 'org.apache.geronimo.commonj.timers.TimerManagerImpl.schedule(TimerListener,
     * long)'
     */
    public void testScheduleTimerListenerLong() throws Exception {
        initializeTest();

        timerManager.schedule(new TimerListener() {
            public void timerExpired(Timer timer) {
                counter++;
                expirationTime = System.currentTimeMillis();
            }
        }, delay);

        Thread.sleep(HEDGE);
        assertTrue(counter == 0);

        Thread.sleep(delay);
        assertTrue(counter == 1);

        long measuredDelay = expirationTime - scheduleTime;
        assertTrue(measuredDelay > 0L);
        assertTrue(Math.abs(measuredDelay - delay) < HEDGE);
    }

    public void testScheduleTimerListenerLong_N() throws Exception {
        initializeTest();

        for (int i = 0; i < 10; i++)
            timerManager.schedule(new TimerListener() {
                public void timerExpired(Timer timer) {
                    counter++;
                }
            }, delay);

        Thread.sleep(HEDGE);
        assertTrue(counter == 0);

        Thread.sleep(delay + HEDGE);
        assertTrue(counter == 10);
    }

    /*
     * Test method for
     * 
     * 'org.apache.geronimo.commonj.timers.TimerManagerImpl.schedule(TimerListener,
     * Date, long)'
     */
    public void testScheduleTimerListenerDateLong() throws Exception {
        initializeTest();

        Timer timer = timerManager.schedule(new TimerListener() {
            public void timerExpired(Timer timer) {
                counter++;
                expirationTime = System.currentTimeMillis();
            }
        }, new Date(System.currentTimeMillis() + delay), period);

        Thread.sleep(HEDGE);
        assertTrue(counter == 0);

        Thread.sleep(delay);
        assertTrue(counter == 1);

        long measuredDelay = expirationTime - scheduleTime;
        assertTrue(measuredDelay > 0L);
        assertTrue(Math.abs(measuredDelay - delay) < HEDGE);

        Thread.sleep(period + HEDGE);
        assertTrue(counter == 2);

        timer.cancel();
        Thread.sleep(period + HEDGE);
        assertTrue(counter == 2);
    }

    /*
     * Test method for
     * 
     * 'org.apache.geronimo.commonj.timers.TimerManagerImpl.schedule(TimerListener,
     * long, long)'
     */
    public void testScheduleTimerListenerLongLong() throws Exception {
        initializeTest();

        Timer timer = timerManager.schedule(new TimerListener() {
            public void timerExpired(Timer timer) {
                counter++;
                expirationTime = System.currentTimeMillis();
            }
        }, delay, period);

        Thread.sleep(HEDGE);
        assertTrue(counter == 0);

        Thread.sleep(delay);
        assertTrue(counter == 1);

        long measuredDelay = expirationTime - scheduleTime;
        assertTrue(measuredDelay > 0L);
        assertTrue(Math.abs(measuredDelay - delay) < HEDGE);

        Thread.sleep(period + HEDGE);
        assertTrue(counter == 2);

        timer.cancel();
        Thread.sleep(period + HEDGE);
        assertTrue(counter == 2);
    }

    /*
     * Test method for
     * 
     * 'org.apache.geronimo.commonj.timers.TimerManagerImpl.scheduleAtFixedRate(TimerListener,
     * Date, long)'
     */
    public void testScheduleAtFixedRateTimerListenerDateLong() throws Exception {
        initializeTest();

        Timer timer = timerManager.scheduleAtFixedRate(new TimerListener() {
            public void timerExpired(Timer timer) {
                counter++;
                expirationTime = System.currentTimeMillis();
            }
        }, new Date(System.currentTimeMillis() + delay), period);

        Thread.sleep(HEDGE);
        assertTrue(counter == 0);

        Thread.sleep(delay);
        assertTrue(counter == 1);

        long measuredDelay = expirationTime - scheduleTime;
        assertTrue(measuredDelay > 0L);
        assertTrue(Math.abs(measuredDelay - delay) < HEDGE);

        Thread.sleep(period + HEDGE);
        assertTrue(counter == 2);

        timer.cancel();
        Thread.sleep(period + HEDGE);
        assertTrue(counter == 2);
    }

    /*
     * Test method for
     * 
     * 'org.apache.geronimo.commonj.timers.TimerManagerImpl.scheduleAtFixedRate(TimerListener,
     * long, long)'
     */
    public void testScheduleAtFixedRateTimerListenerLongLong() throws Exception {
        initializeTest();

        Timer timer = timerManager.scheduleAtFixedRate(new TimerListener() {
            public void timerExpired(Timer timer) {
                counter++;
                expirationTime = System.currentTimeMillis();
            }
        }, delay, period);

        Thread.sleep(HEDGE);
        assertTrue(counter == 0);

        Thread.sleep(delay);
        assertTrue(counter == 1);

        long measuredDelay = expirationTime - scheduleTime;
        assertTrue(measuredDelay > 0L);
        assertTrue(Math.abs(measuredDelay - delay) < HEDGE);

        Thread.sleep(period + HEDGE);
        assertTrue(counter == 2);

        timer.cancel();
        Thread.sleep(period + HEDGE);
        assertTrue(counter == 2);
    }

    private void initializeTest() {
        counter = 0;
        scheduleTime = System.currentTimeMillis();
        expirationTime = 0;
    }
}
