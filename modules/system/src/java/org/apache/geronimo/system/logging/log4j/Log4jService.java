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
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.log.GeronimoLogFactory;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * A Log4j logging service.
 *
 * @version $Rev$ $Date$
 */
public class Log4jService implements GBeanLifecycle {
    /**
     * The URL to the configuration file.
     */
    private String configurationFile;

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
     * The level used for the root logger
     */
    private Level rootLoggerLevel;

    /**
     * Is this service running?
     */
    private boolean running = false;

    /**
     * Construct a <code>Log4jService</code>.
     *
     * @param configurationFile The log4j configuration file.
     * @param refreshPeroid The refresh refreshPeroid (in seconds).
     */
    public Log4jService(final String rootLoggerLevel, final String configurationFile, final int refreshPeroid, ServerInfo serverInfo) {
        LogFactory logFactory = LogFactory.getFactory();
        if (!(logFactory instanceof GeronimoLogFactory)) {
            throw new IllegalStateException("Commons log factory: " + logFactory + " is not a GeronimoLogFactory");
        }
        this.rootLoggerLevel = XLevel.toLevel(rootLoggerLevel);
        this.refreshPeriod = refreshPeroid;
        this.configurationFile = configurationFile;
        this.serverInfo = serverInfo;
    }

    /**
     * Gets the level of the root logger.
     */
    public synchronized String getRootLoggerLevel() {
        if (rootLoggerLevel != null) {
            return rootLoggerLevel.toString();
        }

        return null;
    }

    /**
     * Sets the level of the root logger.
     *
     * @param level The level to change the logger to.
     */
    public synchronized void setRootLoggerLevel(final String level) {
        if (level == null) {
            rootLoggerLevel = null;
        } else {
            rootLoggerLevel = XLevel.toLevel(level);
            if (running) {
                Logger.getRootLogger().setLevel(rootLoggerLevel);
            }
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
    public synchronized String getConfigurationFile() {
        return configurationFile;
    }

    /**
     * Set the logging configuration URL.
     *
     * @param configurationFile the logging configuration file
     */
    public synchronized void setConfigurationFile(final String configurationFile) {
        if (this.configurationFile == null) {
            throw new IllegalArgumentException("configurationFile is null");
        }

        this.configurationFile = configurationFile;
    }

    /**
     * Force the logging system to reconfigure.
     */
    public void reconfigure() {
        URL url = resolveURL();
        if (url == null) {
            return;
        }
        URLConfigurator.configure(url);
    }

    private synchronized void schedule() {
        if (timer != null) {
            // kill the old monitor
            if (monitor != null) {
                monitor.cancel();
            }

            // start the new one
            monitor = new URLMonitorTask();
            TimerTask task = monitor;
            timer.schedule(monitor, 1000 * refreshPeriod, 1000 * refreshPeriod);
            task.run();
        }
    }

    public void doStart() {
        synchronized (this) {
            // Peroidally check the configuration file
            schedule();

            // Make sure the root Logger has loaded
            Logger.getRootLogger();

            // set the root logger level
            if (rootLoggerLevel != null) {
                Logger.getRootLogger().setLevel(rootLoggerLevel);
            }

            reconfigure();
        }

        // Change all of the loggers over to use log4j
        GeronimoLogFactory logFactory = (GeronimoLogFactory) LogFactory.getFactory();
        synchronized (logFactory) {
            if (!(logFactory.getLogFactory() instanceof CachingLog4jLogFactory)) {
                logFactory.setLogFactory(new CachingLog4jLogFactory());
            }
        }

        synchronized (this) {
            running = true;
        }
    }

    public synchronized void doStop() {
        running = false;
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
            return serverInfo.resolve(URI.create(configurationFile)).toURL();
        } catch (Exception e) {
            return null;
        }
    }

    private class URLMonitorTask extends TimerTask {
        public void run() {
            try {
                long lastModified;
                synchronized (this) {
                    if (running == false) {
                        return;
                    }

                    URL url = resolveURL();
                    if (url == null) {
                        return;
                    }
                    URLConnection connection = url.openConnection();
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

        infoFactory.addAttribute("rootLoggerLevel", String.class, true);
        infoFactory.addAttribute("configurationFile", String.class, true);
        infoFactory.addAttribute("refreshPeriod", int.class, true);

        infoFactory.addReference("ServerInfo", ServerInfo.class);

        infoFactory.addOperation("reconfigure");
        infoFactory.addOperation("setLoggerLevel", new Class[]{String.class, String.class});
        infoFactory.addOperation("getLoggerLevel", new Class[]{String.class});

        infoFactory.setConstructor(new String[]{"rootLoggerLevel", "configurationFile", "refreshPeriod", "ServerInfo"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
