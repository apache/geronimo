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
package org.apache.geronimo.common.log.log4j;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import org.apache.commons.logging.Log;

/**
 * This log wrapper caches the trace, debug and info enabled flags.  The flags are updated
 * by a single timer task for all logs.  The error, warn and fatal levels are always enabled.
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/27 10:08:45 $
 */
public final class CachingLog4jLog
    implements Log
{
    // @todo this need to be moved to a log manager MBean, but this works as a proof of concept
    private static final Set logs = new HashSet();
    private static final Timer timer = new Timer();
    private static final TimerTask task = new TimerTask() {
        public void run() {
            HashSet logSnapshot;
            synchronized (logs) {
                logSnapshot = new HashSet(logs);
            }

            for (Iterator i = logSnapshot.iterator(); i.hasNext();) {
                WeakReference weakReference = (WeakReference) i.next();
                CachingLog4jLog log = (CachingLog4jLog) weakReference.get();
                if (log == null) {
                    synchronized (logs) {
                        logs.remove(log);
                    }
                } else {
                    log.updateLevelInfo();
                }
            }

        }
    };

    static {
        timer.schedule(task, 3 * 60 * 1000, 3 * 60 * 1000);
    }

    private static void addLog(CachingLog4jLog log) {
        synchronized (logs) {
            logs.add(new WeakReference(log));
        }
    }

    private static final String FQCN = CachingLog4jLog.class.getName();
    private final Logger logger;
    private boolean traceEnabled;
    private boolean debugEnabled;
    private boolean infoEnabled;

    public CachingLog4jLog(String name) {
        logger = Logger.getLogger(name);
        updateLevelInfo();
        addLog(this);
    }

    public CachingLog4jLog(Logger logger) {
        this.logger = logger;
        updateLevelInfo();
        addLog(this);
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
            logger.log(FQCN, Priority.DEBUG, message, null);
        }
    }

    public void debug(Object message, Throwable throwable) {
        if (debugEnabled) {
            logger.log(FQCN, Priority.DEBUG, message, throwable);
        }
    }

    public boolean isInfoEnabled() {
        return infoEnabled;
    }

    public void info(Object message) {
        if (infoEnabled) {
            logger.log(FQCN, Priority.INFO, message, null);
        }
    }

    public void info(Object message, Throwable throwable) {
        if (infoEnabled) {
            logger.log(FQCN, Priority.INFO, message, throwable);
        }
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

    private void updateLevelInfo() {
        // This method is proposely not synchronized.
        // The setting of a boolean is atomic so we don't have to worry about inconsistent state.
        // Normally we would have to worry about an out of date cache running threads (SMP boxes),
        // but this cache is not time critical (so don't worry about it).
        traceEnabled = logger.isEnabledFor(XLevel.TRACE);
        debugEnabled = logger.isDebugEnabled();
        infoEnabled = logger.isInfoEnabled();
    }
}
