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
package org.apache.geronimo.kernel.log;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.Handler;
import java.util.logging.ConsoleHandler;

import org.apache.commons.logging.Log;

/**
 * @version $Revision: 1.1 $ $Date: 2004/02/13 07:22:22 $
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
