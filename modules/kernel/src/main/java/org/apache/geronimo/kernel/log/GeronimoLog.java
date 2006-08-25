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

/**
 * This log wrapper caches the trace, debug and info enabled flags.  The flags are updated
 * by a single timer task for all logs.
 *
 * @version $Rev$ $Date$
 */
public final class GeronimoLog implements Log {
    private final String name;
    private Log log;

    public GeronimoLog(String name, Log log) {
        this.name = name;
        this.log = log;
    }

    public String getName() {
        return name;
    }

    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    public void trace(Object message) {
        log.trace(message);
    }

    public void trace(Object message, Throwable throwable) {
        log.trace(message, throwable);
    }

    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    public void debug(Object message) {
        log.debug(message);
    }

    public void debug(Object message, Throwable throwable) {
        log.debug(message, throwable);
    }

    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    public void info(Object message) {
        if(!name.startsWith("/")) { //todo: temporary fix to work around Jetty logging issue
            log.info(message);
        }
    }

    public void info(Object message, Throwable throwable) {
        log.info(message, throwable);
    }

    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    public void warn(Object message) {
        log.warn(message);
    }

    public void warn(Object message, Throwable throwable) {
        log.warn(message, throwable);
    }

    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    public void error(Object message) {
        log.error(message);
    }

    public void error(Object message, Throwable throwable) {
        log.error(message, throwable);
    }

    public boolean isFatalEnabled() {
        return log.isFatalEnabled();
    }

    public void fatal(Object message) {
        log.fatal(message);
    }

    public void fatal(Object message, Throwable throwable) {
        log.fatal(message, throwable);
    }
}
