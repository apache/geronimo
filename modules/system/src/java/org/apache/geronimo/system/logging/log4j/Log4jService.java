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

package org.apache.geronimo.system.logging.log4j;

import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.kernel.log.GeronimoLogFactory;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * A Log4j logging service.
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/12 18:12:52 $
 */
public class Log4jService implements GBean {
    /**
     * The URL to the configuration file.
     */
    private URL configURL;

    /**
     * The time (in seconds) between checking for new config.
     */
    private int refreshPeriod;

    /**
     * The properties service
     */
    private ServerInfo serverInfo;

    /**
     * The URL watch timer (in daemon mode).
     */
    private Timer timer = new Timer(true);

    /**
     * A monitor to check when the config URL changes.
     */
    private TimerTask monitor;

    /**
     * Last time the file was changed.
     */
    private long lastChanged = -1;

    /**
     * Construct a <code>Log4jService</code>.
     *
     * @param configURL       The configuration URL.
     * @param refreshPeroid    The refresh refreshPeroid (in seconds).
     *
     */
    public Log4jService(final URL configURL, final int refreshPeroid, ServerInfo serverInfo) {
        setRefreshPeriod(refreshPeroid);
        setConfigurationURL(configURL);
        this.serverInfo = serverInfo;
        LogFactory logFactory = LogFactory.getFactory();
        if (!(logFactory instanceof GeronimoLogFactory)) {
            throw new IllegalStateException("Commons log factory is not a GeronimoLogFactory");
        }
    }

    /**
     * Gets the level of the logger of the give name.
     *
     * @param logger    The logger to inspect.
     *
     */
    public String getLoggerLevel(final String logger) {
        if (logger == null) {
            throw new IllegalArgumentException("logger is null");
        }

        Level level = Logger.getLogger(logger).getLevel();

        if (level != null) {
            return level.toString();
        }

        return null;
    }

    /**
     * Sets the level for a logger of the give name.
     *
     * @param logger    The logger to change level
     * @param level     The level to change the logger to.
     *
     */
    public void setLoggerLevel(final String logger, final String level) {
        if (logger == null) {
            throw new IllegalArgumentException("logger is null");
        }
        if (level == null) {
            throw new IllegalArgumentException("level is null");
        }

        Logger.getLogger(logger).setLevel(XLevel.toLevel(level));
    }

    /**
     * Get the refresh period.
     *
     * @return the refresh period (in seconds)
     */
    public synchronized int getRefreshPeriod() {
        return refreshPeriod;
    }

    /**
     * Set the refresh period.
     *
     * @param period the refresh period (in seconds)
     * @throws IllegalArgumentException if refresh period is <= 0
     */
    public synchronized void setRefreshPeriod(final int period) {
        if (period < 1) {
            throw new IllegalArgumentException("Refresh period must be > 0");
        }

        if (this.refreshPeriod != period) {
            this.refreshPeriod = period;
            schedule();
        }
    }

    /**
     * Get the logging configuration URL.
     *
     * @return the logging configuration URL
     */
    public synchronized URL getConfigurationURL() {
        return configURL;
    }

    /**
     * Set the logging configuration URL.
     *
     * @param url the logging configuration URL
     */
    public synchronized void setConfigurationURL(final URL url) {
        if (url == null) {
            throw new IllegalArgumentException("url is null");
        }

        this.configURL = url;
    }

    /**
     * Force the logging system to reconfigure.
     */
    public void reconfigure() {
        URL url;
        synchronized (this) {
            url = configURL;
        }
        URLConfigurator.configure(url);
    }

    private void schedule() {
        if (timer != null) {
            TimerTask task;
            synchronized (this) {
                // kill the old monitor
                if (monitor != null) {
                    monitor.cancel();
                }

                // start the new one
                monitor = new URLMonitorTask();
                task = monitor;
                timer.schedule(monitor, 1000 * refreshPeriod, 1000 * refreshPeriod);
            }
            task.run();
        }
    }

    public void setGBeanContext(GBeanContext context) {
    }

    public void doStart() {
        // Peroidally check the configuration file
        schedule();

        // Make sure the root Logger has loaded
        Logger.getRootLogger();

        // Change all of the loggers over to use log4j
        GeronimoLogFactory logFactory = (GeronimoLogFactory) LogFactory.getFactory();
        synchronized (logFactory) {
            if (!(logFactory.getLogFactory() instanceof CachingLog4jLogFactory)) {
                logFactory.setLogFactory(new CachingLog4jLogFactory());
            }
        }
    }

    public synchronized void doStop() {
        if (monitor != null) {
            monitor.cancel();
            monitor = null;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void doFail() {
        doStop();
    }

    private synchronized URL resolveURL() {
        try {
            URI configURI = null;
            URI baseURI = new URI(serverInfo.getBaseDirectory());
            return baseURI.resolve(configURI).toURL();
        } catch (Exception e) {
            return null;
        }
    }

    private class URLMonitorTask extends TimerTask {
        public void run() {
            try {
                long lastModified;
                synchronized (this) {
                    URLConnection connection = resolveURL().openConnection();
                    lastModified = connection.getLastModified();
                }

                if (lastChanged < lastModified) {
                    lastChanged = lastModified;
                    reconfigure();
                }
            } catch (Exception e) {
            }
        }
    }

    private static class CachingLog4jLogFactory extends LogFactory {
        public Log getInstance(Class clazz) throws LogConfigurationException {
            return getInstance(clazz.getName());
        }

        public Log getInstance(String name) throws LogConfigurationException {
            return new CachingLog4jLog(name);
        }

        public Object getAttribute(String name) {
            return null;
        }

        public String[] getAttributeNames() {
            return new String[0];
        }

        public void release() {
        }

        public void removeAttribute(String name) {
        }

        public void setAttribute(String name, Object value) {
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(Log4jService.class.getName());
        infoFactory.setConstructor(new GConstructorInfo(
                new String[]{"ConfigurationURL", "RefreshPeriod", "ServerInfo"},
                new Class[]{URL.class, int.class, ServerInfo.class}));
        infoFactory.addAttribute(new GAttributeInfo("ConfigurationURL", true));
        infoFactory.addAttribute(new GAttributeInfo("RefreshPeriod", true));
        infoFactory.addReference(new GReferenceInfo("ServerInfo", ServerInfo.class.getName()));
        infoFactory.addOperation(new GOperationInfo("reconfigure"));
        infoFactory.addOperation(new GOperationInfo("setLoggerLevel", new String[]{String.class.getName(), String.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("getLoggerLevel", new String[]{String.class.getName()}));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
