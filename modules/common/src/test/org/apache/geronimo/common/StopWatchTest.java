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

package org.apache.geronimo.common;

import junit.framework.TestCase;


/**
 *
 * @version $Revision: 1.5 $ $Date: 2004/02/25 09:57:04 $
 */
public class StopWatchTest extends TestCase {
    private static long TIME = 100L;
    private static long EPSILON = TIME /10;

    public void testConstructor() {
        StopWatch sw = new StopWatch();
        assertFalse(sw.isRunning());
    }

    public void testConstructorRunning() {
        StopWatch sw = new StopWatch(true);
        assertTrue(sw.isRunning());

        sw = new StopWatch(false);
        assertFalse(sw.isRunning());
    }

    public void testStartStop() {
        StopWatch sw = new StopWatch();

        sw.start();
        assertTrue(sw.isRunning());
        try {
            Thread.sleep(TIME);
        } catch (Exception ex) {
        }
        sw.stop();
        assertFalse(sw.isRunning());

        assertEquals(1, sw.getLapCount());
        assertTrue("Expected more than one second, not " + sw.getLapTime(), sw.getLapTime() >= TIME - EPSILON);
        assertTrue(sw.getTime() >= TIME - EPSILON);
        assertEquals(sw.getLapTime(), sw.getAverageLapTime());
    }

    public void testTwoStarts() {
        StopWatch sw = new StopWatch();

        sw.start();
        assertTrue(sw.isRunning());
        sw.start();
        assertTrue(sw.isRunning());
        try {
            Thread.sleep(TIME);
        } catch (Exception ex) {
        }
        sw.stop();
        assertFalse(sw.isRunning());

        assertEquals(1, sw.getLapCount());
        assertTrue("Expected more than one second, not " + sw.getLapTime(), sw.getLapTime() >= TIME - EPSILON);
        assertTrue(sw.getTime() >= TIME - EPSILON);
        assertEquals(sw.getLapTime(), sw.getAverageLapTime());
    }

    public void testTwoStops() {
        StopWatch sw = new StopWatch();

        sw.start();
        assertTrue(sw.isRunning());
        try {
            Thread.sleep(TIME);
        } catch (Exception ex) {
        }
        sw.stop();
        assertFalse(sw.isRunning());
        sw.stop();
        assertFalse(sw.isRunning());

        assertEquals(1, sw.getLapCount());
        assertTrue("Expected more than one second, not " + sw.getLapTime(), sw.getLapTime() >= TIME - EPSILON);
        assertTrue(sw.getTime() >= TIME - EPSILON);
        assertEquals(sw.getLapTime(), sw.getAverageLapTime());
    }

    public void testTwoLaps() {
        StopWatch sw = new StopWatch();

        sw.start();
        try {
            Thread.sleep(TIME);
        } catch (Exception ex) {
        }
        sw.stop();

        assertEquals(1, sw.getLapCount());
        assertTrue("Expected more than one second, not " + sw.getLapTime(), sw.getLapTime() >= TIME - EPSILON);
        assertTrue(sw.getTime() >= TIME - EPSILON);
        assertEquals(sw.getLapTime(), sw.getAverageLapTime());

        sw.start();
        try {
            Thread.sleep(TIME);
        } catch (Exception ex) {
        }
        sw.stop();

        assertEquals(2, sw.getLapCount());
        assertTrue("Expected  more than one second, not " + sw.getLapTime(), sw.getLapTime() >= TIME - EPSILON);
        assertTrue("Expected more than two seconds, not " + sw.getTime(), sw.getTime() >= 2 * (TIME - EPSILON));
        assertEquals(sw.getTime() / 2, sw.getAverageLapTime());
    }

    public void testReset() {
        StopWatch sw = new StopWatch();

        sw.start();
        try {
            Thread.sleep(TIME);
        } catch (Exception ex) {
        }
        sw.stop();

        assertEquals(1, sw.getLapCount());
        assertTrue(sw.getLapTime() >= TIME - EPSILON);
        assertTrue(sw.getTime() >= TIME - EPSILON);
        assertEquals(sw.getLapTime(), sw.getAverageLapTime());

        sw.reset();

        assertEquals(0, sw.getLapCount());
        assertEquals(0L, sw.getLapTime());
        assertEquals(0L, sw.getTime());
    }

    public void testToDuration() {
        StopWatch sw = new StopWatch();

        sw.start();
        try {
            Thread.sleep(TIME);
        } catch (Exception ex) {
        }
        sw.stop();

        Duration d = sw.toDuration();
        assertTrue(d.compareTo(1000) >= 0 - EPSILON);
    }
}
