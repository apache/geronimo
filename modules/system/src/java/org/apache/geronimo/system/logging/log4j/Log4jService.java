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

import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.kernel.log.GeronimoLogFactory;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * A Log4j logging service.
 *
 * @version $Revision: 1.5 $ $Date: 2004/06/05 07:14:30 $
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
     * @param configURL The configuration URL.
     * @param refreshPeroid The refresh refreshPeroid (in seconds).
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
     * @param logger The logger to inspect.
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
     * @param logger The logger to change level
     * @param level The level to change the logger to.
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
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(Log4jService.class);

        infoFactory.addAttribute("ConfigurationURL", URL.class, true);
        infoFactory.addAttribute("RefreshPeriod", int.class, true);

        infoFactory.addReference("ServerInfo", ServerInfo.class);

        infoFactory.addOperation("reconfigure");
        infoFactory.addOperation("setLoggerLevel", new Class[]{String.class, String.class});
        infoFactory.addOperation("getLoggerLevel", new Class[]{String.class});

        infoFactory.setConstructor(new String[]{"ConfigurationURL", "RefreshPeriod", "ServerInfo"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
