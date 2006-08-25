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

package org.apache.geronimo.kernel.log;

import org.apache.commons.logging.Log;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * @version $Rev$ $Date$
 */
public class BootstrapLog4jLog implements Log {
    static {
        Logger root = Logger.getRootLogger();
        root.addAppender(new ConsoleAppender(new PatternLayout("%d{ABSOLUTE} %-5p [%c{1}] %m%n")));

        GeronimoLogging level = GeronimoLogging.getDefaultLevel();
        if (level == null) {
            root.setLevel(Level.ERROR);
        } else if (level == GeronimoLogging.TRACE) {
            root.setLevel(Level.DEBUG);
        } else if (level == GeronimoLogging.DEBUG) {
            root.setLevel(Level.DEBUG);
        } else if (level == GeronimoLogging.INFO) {
            root.setLevel(Level.INFO);
        } else if (level == GeronimoLogging.WARN) {
            root.setLevel(Level.WARN);
        } else if (level == GeronimoLogging.ERROR) {
            root.setLevel(Level.ERROR);
        } else if (level == GeronimoLogging.FATAL) {
            root.setLevel(Level.FATAL);
        }

    }

    private static final String FQCN = BootstrapLog4jLog.class.getName();
    private Logger logger;

    public BootstrapLog4jLog(String name) {
        logger = Logger.getLogger(name);
    }

    public boolean isTraceEnabled() {
        return logger.isEnabledFor(Level.DEBUG);
    }

    public void trace(Object message) {
        logger.log(FQCN, Level.DEBUG, message, null);
    }

    public void trace(Object message, Throwable throwable) {
        logger.log(FQCN, Level.DEBUG, message, throwable);
    }

    public boolean isDebugEnabled() {
        return logger.isEnabledFor(Level.DEBUG);
    }

    public void debug(Object message) {
        logger.log(FQCN, Level.DEBUG, message, null);
    }

    public void debug(Object message, Throwable throwable) {
        logger.log(FQCN, Level.DEBUG, message, throwable);
    }

    public boolean isInfoEnabled() {
        return logger.isEnabledFor(Level.INFO);
    }

    public void info(Object message) {
        logger.log(FQCN, Level.INFO, message, null);
    }

    public void info(Object message, Throwable throwable) {
        logger.log(FQCN, Level.INFO, message, throwable);
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
}
