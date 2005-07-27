/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.system.logging.log4j;

import junit.framework.TestCase;
import org.apache.log4j.Level;

/**
 * Tests the {@link XLevel} class.
 * @version $Rev$ $Date$
 */
public final class XLevelTest extends TestCase {
    private static final int TRACE_SYSLOG_EQ = 7;
    private static final String TRACE_STRING = "TRACE";
    private static final int TRACE_INT = Level.DEBUG_INT - 1;
    private static final int MOCK_INT = TRACE_INT - 99;

    private final static class MockLevel extends Level {
        MockLevel() {
            super(MOCK_INT, "MOCK", 99);
        }
    }

    private final static Level MOCK = new MockLevel();

    /**
     * Tests the constants declared in the {@link XLevel}.
     * The {@link XLevel#TRACE_INT} value is tested to be defined as {@link Level#DEBUG_INT}-1.
     * The {@link XLevel#TRACE} value is tested to be an instance of {@link XLevel},
     * have a name "TRACE" and syslog level equivalent of <code>7</code>.
     */
    public void testConstants() {
        assertEquals("XLevel.TRACE_INT is defined as Level.DEBUG_INT-1", XLevel.TRACE_INT, TRACE_INT);
        final Object o = XLevel.TRACE;
        assertTrue("XLevel.TRACE is an instance of XLevel", o instanceof XLevel);
        final XLevel traceLevel = (XLevel) o;
        assertEquals("XLevel.TRACE syslog equivalent is indeed " + TRACE_SYSLOG_EQ, traceLevel.getSyslogEquivalent(), TRACE_SYSLOG_EQ);
        assertEquals("XLevel.TRACE name is TRACE", traceLevel.toString(), TRACE_STRING);
        assertEquals("XLevel.TRACE int level is " + TRACE_INT, traceLevel.toInt(), TRACE_INT);
    }

    /**
     * Tests {@link XLevel#toLevel(java.lang.String, org.apache.log4j.Level) method.
     * Tests that default value is returned if null value is passed.
     * Tests that {@link XLevel#TRACE} is returned if the name is equal to "TRACE",
     * irrespective of case.
     * Tests that in all other cases the conversion is deferred to {@link Level#toLevel(java.lang.String, org.apache.log4j.Level)}.
     * Tests that if conversion fails the specified default value is returned.
     */
    public void testToLevelWithDefault() {
        assertSame("Default value is indeed returned if null name is passed", XLevel.toLevel(null, MOCK), MOCK);
        assertSame("XLevel.TRACE is returned if the name is equal to \"TRACE\" irrespecctive of case", XLevel.toLevel("trAce"), XLevel.TRACE);
        Level levelResults = Level.toLevel("MOCK", MOCK);
        Level xLevelResults = XLevel.toLevel("MOCK", MOCK);
        assertSame("In all other cases conversion is deferred to Level", xLevelResults, levelResults);
        assertSame("If conversion fails the default value is returned", xLevelResults, MOCK);
    }

    /**
     * Tests {@link XLevel#toLevel(java.lang.String) method.
     * Tests that this method simply delegates the work to
     * {@link XLevel#toLevel(java.lang.String, org.apache.log4j.Level) with default value
     * specified as {@link XLevel#TRACE}.
     */
    public void testToLevel() {
        assertSame("XLevel.TRACE is returned if null name is passed", XLevel.toLevel(null), XLevel.TRACE);
        assertSame("XLevel.TRACE is returned if the name is equal to \"TRACE\" irrespective of case", XLevel.toLevel("trAce"), XLevel.TRACE);
        Level levelResults = Level.toLevel("MOCK", XLevel.TRACE);
        Level xLevelResults = XLevel.toLevel("MOCK");
        assertSame("In all other cases conversion is deferred to Level", xLevelResults, levelResults);
        assertSame("If conversion fails XLevel.TRACE is returned", xLevelResults, XLevel.TRACE);
    }

    /**
     * Tests {@link XLevel#toLevel(int) method.
     * Tests that if level is equal to {@link XLevel#TRACE_INT} {@link XLevel#TRACE} is returned.
     * Tests that in other cases the conversion is deferred to {@link Level#toLevel(int)}
     * Tests that if conversion fails the {@link Level#DEBUG} is returned.
     */
    public void testToLevelInt() {
        assertSame("XLevel.TRACE is returned is " + TRACE_INT + "is passed", XLevel.toLevel(TRACE_INT), XLevel.TRACE);
        Level levelResults = Level.toLevel(MOCK_INT);
        Level xLevelResults = XLevel.toLevel(MOCK_INT);
        assertSame("In all other cases conversion is deferred to Level", xLevelResults, levelResults);
        assertSame("If conversion fails Level.DEBUG is returned", xLevelResults, Level.DEBUG);
    }

}
