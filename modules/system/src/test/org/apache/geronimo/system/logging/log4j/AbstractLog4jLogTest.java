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
import org.apache.log4j.Priority;
import org.apache.log4j.Level;
import org.apache.commons.logging.Log;

/**
 * @version $Rev$ $Date$
 */
public class AbstractLog4jLogTest extends TestCase {
    protected final static Object MESSAGE = new Object();
    protected final static Throwable THROWABLE = new Throwable();
    protected MockLogger mockLogger;
    protected Log log;
    protected String logFQCN;

    protected void doTestDelegatedToLog(Priority priority, Object object, Throwable throwable) {
        assertTrue("Method delegates to Logger.log",
                mockLogger.logCalled
                & mockLogger.calledWithString == logFQCN
                & mockLogger.calledWithPriority == priority
                & mockLogger.calledWithObject == object
                & mockLogger.calledWithThrowable == throwable);
    }

    /**
     * Tests {@link org.apache.commons.logging.Log#isWarnEnabled()} method.
     * Tests that it delegates to {@link org.apache.log4j.Category#isEnabledFor(Priority)}
     * with {@link org.apache.log4j.Level#WARN} as an argument.
     */
    public void testIsWarnEnabled() {
        log.isWarnEnabled();
        assertTrue("Method delegates to Logger.isEnabledFor",
                mockLogger.isEnabledForCalled
                & mockLogger.calledWithPriority==Level.WARN);
    }

    /**
     * Tests {@link org.apache.commons.logging.Log#warn(Object) method.
     * Tests that it delegates to
     * {@link org.apache.log4j.Category#log(String, Priority, Object, Throwable)}
     * with {@link org.apache.log4j.Level#WARN} level, specified message and <code>null</code> throwable.
     */
    public void testWarnNull() {
        log.warn(MESSAGE);
        doTestDelegatedToLog(Level.WARN, MESSAGE, null);
    }

    /**
     * Tests {@link org.apache.commons.logging.Log#warn(Object,java.lang.Throwable throwable) method.
     * Tests that it delegates to
     * {@link org.apache.log4j.Category#log(String, Priority, Object, Throwable)}
     * with {@link org.apache.log4j.Level#WARN} level, specified message and throwable.
     */
    public void testWarnThrowable() {
        log.warn(MESSAGE, THROWABLE);
        doTestDelegatedToLog(Level.WARN, MESSAGE, THROWABLE);
    }

    /**
     * Tests {@link org.apache.commons.logging.Log#isErrorEnabled()} method.
     * Tests that it delegates to {@link org.apache.log4j.Category#isEnabledFor(Priority)}
     * with {@link org.apache.log4j.Level#ERROR} as an argument.
     */
    public void testIsErrorEnabled() {
        log.isErrorEnabled();
        assertTrue("Method delegates to Logger.isEnabledFor",
                mockLogger.isEnabledForCalled
                & mockLogger.calledWithPriority==Level.ERROR);
    }

    /**
     * Tests {@link org.apache.commons.logging.Log#error(Object) method.
     * Tests that it delegates to
     * {@link org.apache.log4j.Category#log(String, Priority, Object, Throwable)}
     * with {@link org.apache.log4j.Level#ERROR} level, specified message and <code>null</code> throwable.
     */
    public void testErrorNull() {
        log.error(MESSAGE);
        doTestDelegatedToLog(Level.ERROR, MESSAGE, null);
    }

    /**
     * Tests {@link org.apache.commons.logging.Log#error(Object,java.lang.Throwable throwable) method.
     * Tests that it delegates to
     * {@link org.apache.log4j.Category#log(String, Priority, Object, Throwable)}
     * with {@link org.apache.log4j.Level#ERROR} level, specified message and throwable.
     */
    public void testErrorThrowable() {
        log.error(MESSAGE, THROWABLE);
        doTestDelegatedToLog(Level.ERROR, MESSAGE, THROWABLE);
    }

    /**
     * Tests {@link org.apache.commons.logging.Log#isFatalEnabled()} method.
     * Tests that it delegates to {@link org.apache.log4j.Category#isEnabledFor(Priority)}
     * with {@link org.apache.log4j.Level#FATAL} as an argument.
     */
    public void testIsFatalEnabled() {
        log.isFatalEnabled();
        assertTrue("Method delegates to Logger.isEnabledFor",
                mockLogger.isEnabledForCalled
                & mockLogger.calledWithPriority==Level.FATAL);
    }

    /**
     * Tests {@link org.apache.commons.logging.Log#fatal(Object) method.
     * Tests that it delegates to
     * {@link org.apache.log4j.Category#log(String, Priority, Object, Throwable)}
     * with {@link org.apache.log4j.Level#FATAL} level, specified message and <code>null</code> throwable.
     */
    public void testFatalNull() {
        log.fatal(MESSAGE);
        doTestDelegatedToLog(Level.FATAL, MESSAGE, null);
    }

    /**
     * Tests {@link org.apache.commons.logging.Log#fatal(Object,java.lang.Throwable throwable) method.
     * Tests that it delegates to
     * {@link org.apache.log4j.Category#log(String, Priority, Object, Throwable)}
     * with {@link org.apache.log4j.Level#FATAL} level, specified message and throwable.
     */
    public void testFatalThrowable() {
        log.fatal(MESSAGE, THROWABLE);
        doTestDelegatedToLog(Level.FATAL, MESSAGE, THROWABLE);
    }
}
