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

package org.apache.geronimo.core.logging.log4j;

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.NullArgumentException;
import org.apache.geronimo.common.log.log4j.URLConfigurator;
import org.apache.geronimo.common.propertyeditor.PropertyEditors;
import org.apache.geronimo.common.propertyeditor.TextPropertyEditorSupport;
import org.apache.geronimo.core.logging.AbstractLoggingService;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.kernel.log.XLevel;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * A Log4j logging service.
 *
 *
 * @version $Revision: 1.5 $ $Date: 2004/01/22 04:24:57 $
 */
public class Log4jService extends AbstractLoggingService {

    private static final GBeanInfo GBEAN_INFO;

    private static final Log log = LogFactory.getLog(Log4jService.class);

    /**
     * Construct a <code>Log4jService</code>.
     *
     * @param url       The configuration URL.
     * @param period    The refresh period (in seconds).
     *
     */
    public Log4jService(final URL url, final int period) {
        super(url, period);
    }

    /**
     * Construct a <code>Log4jService</code>.
     *
     * @param url   The configuration URL.
     *
     */
    public Log4jService(final URL url) {
        super(url);
    }

    /**
     * Force the logging system to configure from the given URL.
     *
     * @param url   The URL to configure from.
     */
    public void configure(final URL url) {
        if (url == null) {
            throw new NullArgumentException("url");
        }

        URLConfigurator.configure(url);
    }


    ///////////////////////////////////////////////////////////////////////////
    //                    Log4j Level Accessors & Mutators                   //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * A property editor for Log4j Levels.
     */
    public static class LevelEditor
        extends TextPropertyEditorSupport {
        public Object getValue() {
            return XLevel.toLevel(getAsText().trim());
        }
    }

    /**
     * A property editor for Log4j Loggers.
     */
    public static class LoggerEditor
        extends TextPropertyEditorSupport {
        public Object getValue() {
            return Logger.getLogger(getAsText().trim());
        }
    }

    /** Install property editors for Logger and Level. */
    static {
        PropertyEditors.registerEditor(Logger.class, LoggerEditor.class);
        PropertyEditors.registerEditor(Level.class, LevelEditor.class);
    }

    /**
     * Sets the level for a logger of the give name.
     *
     * @param logger    The logger to change level
     * @param level     The level to change the logger to.
     *
     */
    public void setLoggerLevel(final Logger logger, final Level level) {
        if (logger == null) {
            throw new NullArgumentException("logger");
        }
        if (level == null) {
            throw new NullArgumentException("level");
        }

        logger.setLevel(level);
        log.info("Changed logger '" + logger.getName() + "' level: " + level);
    }

    /**
     * Gets the level of the logger of the give name.
     *
     * @param logger    The logger to inspect.
     *
     */
    public String getLoggerLevel(final Logger logger) {
        if (logger == null) {
            throw new NullArgumentException("logger");
        }

        Level level = logger.getLevel();

        if (level != null) {
            return level.toString();
        }

        return null;
    }


    //GBean
    public void doStart() {
        super.doStart();

        // Make sure the root Logger has loaded
        Logger.getRootLogger();
    }

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(Log4jService.class.getName(), AbstractLoggingService.getGBeanInfo());
        infoFactory.addOperation(new GOperationInfo("setLoggerLevel", new String[] {Logger.class.getName(), Level.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("getLoggerLevel", new String[] {Logger.class.getName()}));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
