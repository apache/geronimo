/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.core.log;

import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/11 21:04:56 $
 */
public class Log4jLog implements Log {
    private String FQCN = getClass().getName();
    private Logger logger;

    public Log4jLog() {
    }

    public Log4jLog(String name) {
        logger = Logger.getLogger(name);
    }

    public Log4jLog(Logger logger) {
        this.logger = logger;
    }

    public boolean isTraceEnabled() {
        return logger.isEnabledFor(XLevel.TRACE);
    }

    public void trace(Object message) {
        logger.log(FQCN, XLevel.TRACE, message, null);
    }

    public void trace(Object message, Throwable throwable) {
        logger.log(FQCN, XLevel.TRACE, message, throwable);
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public void debug(Object message) {
        logger.log(FQCN, Priority.DEBUG, message, null);
    }

    public void debug(Object message, Throwable throwable) {
        logger.log(FQCN, Priority.DEBUG, message, throwable);
    }

    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    public void info(Object message) {
        logger.log(FQCN, Priority.INFO, message, null);
    }

    public void info(Object message, Throwable throwable) {
        logger.log(FQCN, Priority.INFO, message, throwable);
    }

    public boolean isWarnEnabled() {
        return true;
    }

    public void warn(Object message) {
        logger.log(FQCN, Priority.WARN, message, null);
    }

    public void warn(Object message, Throwable throwable) {
        logger.log(FQCN, Priority.WARN, message, throwable);
    }

    public boolean isErrorEnabled() {
        return true;
    }

    public void error(Object message) {
        logger.log(FQCN, Priority.ERROR, message, null);
    }

    public void error(Object message, Throwable throwable) {
        logger.log(FQCN, Priority.ERROR, message, throwable);
    }

    public boolean isFatalEnabled() {
        return true;
    }

    public void fatal(Object message) {
        logger.log(FQCN, Priority.FATAL, message, null);
    }

    public void fatal(Object message, Throwable throwable) {
        logger.log(FQCN, Priority.FATAL, message, throwable);
    }
}
