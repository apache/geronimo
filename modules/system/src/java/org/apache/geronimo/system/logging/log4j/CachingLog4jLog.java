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

package org.apache.geronimo.system.logging.log4j;

import org.apache.commons.logging.Log;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * This log wrapper caches the trace, debug and info enabled flags.  The flags are updated
 * by a single timer task for all logs.
 *
 * @version $Rev$ $Date$
 */
public final class CachingLog4jLog implements Log {
    private final String FQCN = getClass().getName();
    private Logger logger;
    private boolean traceEnabled;
    private boolean debugEnabled;
    private boolean infoEnabled;

    public CachingLog4jLog(String name) {
        logger = Logger.getLogger(name);
        updateLevelInfo();
    }

    public CachingLog4jLog(Logger logger) {
        this.logger = logger;
        updateLevelInfo();
    }

    public boolean isTraceEnabled() {
        return traceEnabled;
    }

    public void trace(Object message) {
        if (traceEnabled) {
            logger.log(FQCN, XLevel.TRACE, message, null);
        }
    }

    public void trace(Object message, Throwable throwable) {
        if (traceEnabled) {
            logger.log(FQCN, XLevel.TRACE, message, throwable);
        }
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void debug(Object message) {
        if (debugEnabled) {
            logger.log(FQCN, Level.DEBUG, message, null);
        }
    }

    public void debug(Object message, Throwable throwable) {
        if (debugEnabled) {
            logger.log(FQCN, Level.DEBUG, message, throwable);
        }
    }

    public boolean isInfoEnabled() {
        return infoEnabled;
    }

    public void info(Object message) {
        if (infoEnabled) {
            logger.log(FQCN, Level.INFO, message, null);
        }
    }

    public void info(Object message, Throwable throwable) {
        if (infoEnabled) {
            logger.log(FQCN, Level.INFO, message, throwable);
        }
    }

    public boolean isWarnEnabled() {
        return logger.isEnabledFor(Level.WARN);
    }

    public void warn(Object message) {
        logger.log(FQCN, Level.WARN, message, null);
    }

    public void warn(Object message, Throwable throwable) {
        logger.log(FQCN, Level.WARN, message, throwable);
    }

    public boolean isErrorEnabled() {
        return logger.isEnabledFor(Level.ERROR);
    }

    public void error(Object message) {
        logger.log(FQCN, Level.ERROR, message, null);
    }

    public void error(Object message, Throwable throwable) {
        logger.log(FQCN, Level.ERROR, message, throwable);
    }

    public boolean isFatalEnabled() {
        return logger.isEnabledFor(Level.FATAL);
    }

    public void fatal(Object message) {
        logger.log(FQCN, Level.FATAL, message, null);
    }

    public void fatal(Object message, Throwable throwable) {
        logger.log(FQCN, Level.FATAL, message, throwable);
    }

    public void updateLevelInfo() {
        // This method is proposely not synchronized.
        // The setting of a boolean is atomic so we don't have to worry about inconsistent state.
        // Normally we would have to worry about an out of date cache running threads (SMP boxes),
        // but this cache is not time critical (so don't worry about it).
        traceEnabled = logger.isEnabledFor(XLevel.TRACE);
        debugEnabled = logger.isDebugEnabled();
        infoEnabled = logger.isInfoEnabled();
    }
}
