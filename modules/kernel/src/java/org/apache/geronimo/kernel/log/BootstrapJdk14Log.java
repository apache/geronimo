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

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.Handler;
import java.util.logging.ConsoleHandler;

import org.apache.commons.logging.Log;

/**
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:59:02 $
 */
public class BootstrapJdk14Log implements Log {
    static {
        Logger root = Logger.getLogger("");

        GeronimoLogging geronimoLevel = GeronimoLogging.getDefaultLevel();
        Level javaLevel;
        if (geronimoLevel == GeronimoLogging.TRACE) {
            javaLevel = Level.FINEST;
        } else if (geronimoLevel == GeronimoLogging.DEBUG) {
            javaLevel = Level.FINE;
        } else if (geronimoLevel == GeronimoLogging.INFO) {
            javaLevel = Level.INFO;
        } else if (geronimoLevel == GeronimoLogging.WARN) {
            javaLevel = Level.WARNING;
        } else {
            javaLevel = Level.SEVERE;
        }

        // set the root level
        root.setLevel(javaLevel);

        // set the console handler level (if present)
        Handler[] handlers = root.getHandlers();
        for (int index = 0; index < handlers.length; index++) {
            if (handlers[index] instanceof ConsoleHandler) {
                handlers[index].setLevel(javaLevel);
            }
        }
    }

    private Logger logger = null;

    public BootstrapJdk14Log(String name) {
        logger = Logger.getLogger(name);
    }

    private void log(Level level, String messge, Throwable throwable) {
        if (logger.isLoggable(level)) {
            // need to determine if caller class name and method
            StackTraceElement locations[] = new Throwable().getStackTrace();

            // Caller will be the forth element
            String cname = "unknown";
            String method = "unknown";
            if (locations != null && locations.length > 3) {
                StackTraceElement caller = locations[3];
                cname = caller.getClassName();
                method = caller.getMethodName();
            }
            if (throwable == null) {
                logger.logp(level, cname, method, messge);
            } else {
                logger.logp(level, cname, method, messge, throwable);
            }
        }
    }

    public void debug(Object message) {
        log(Level.FINE, String.valueOf(message), null);
    }

    public void debug(Object message, Throwable exception) {
        log(Level.FINE, String.valueOf(message), exception);
    }

    public void error(Object message) {
        log(Level.SEVERE, String.valueOf(message), null);
    }

    public void error(Object message, Throwable exception) {
        log(Level.SEVERE, String.valueOf(message), exception);
    }

    public void fatal(Object message) {
        log(Level.SEVERE, String.valueOf(message), null);
    }

    public void fatal(Object message, Throwable exception) {
        log(Level.SEVERE, String.valueOf(message), exception);
    }

    public Logger getLogger() {
        return this.logger;
    }

    public void info(Object message) {
        log(Level.INFO, String.valueOf(message), null);
    }

    public void info(Object message, Throwable exception) {
        log(Level.INFO, String.valueOf(message), exception);
    }

    public boolean isDebugEnabled() {
        return (logger.isLoggable(Level.FINE));
    }

    public boolean isErrorEnabled() {
        return (logger.isLoggable(Level.SEVERE));
    }

    public boolean isFatalEnabled() {
        return (logger.isLoggable(Level.SEVERE));
    }

    public boolean isInfoEnabled() {
        return (logger.isLoggable(Level.INFO));
    }

    public boolean isTraceEnabled() {
        return (logger.isLoggable(Level.FINEST));
    }

    public boolean isWarnEnabled() {
        return (logger.isLoggable(Level.WARNING));
    }

    public void trace(Object message) {
        log(Level.FINEST, String.valueOf(message), null);
    }

    public void trace(Object message, Throwable exception) {
        log(Level.FINEST, String.valueOf(message), exception);
    }

    public void warn(Object message) {
        log(Level.WARNING, String.valueOf(message), null);
    }

    public void warn(Object message, Throwable exception) {
        log(Level.WARNING, String.valueOf(message), exception);
    }
}
