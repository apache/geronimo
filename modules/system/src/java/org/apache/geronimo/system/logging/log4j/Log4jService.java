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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Set;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.log.GeronimoLogFactory;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
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
    private final ServerInfo serverInfo;

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
     * Is this service running?
     */
    private boolean running = false;

    /**
     * Construct a <code>Log4jService</code>.
     *
     * @param configurationFile The log4j configuration file.
     * @param refreshPeroid The refresh refreshPeroid (in seconds).
     */
    public Log4jService(final String configurationFile, final int refreshPeroid, ServerInfo serverInfo) {
        this.refreshPeriod = refreshPeroid;
        this.configurationFile = configurationFile;
        this.serverInfo = serverInfo;
    }

    /**
     * Gets the level of the root logger.
     */
    public synchronized String getRootLoggerLevel() {
        Level level = LogManager.getRootLogger().getLevel();

        if (level != null) {
            return level.toString();
        }

        return null;
    }

    /**
     * Sets the level of the root logger.
     *
     * @param level The level to change the logger to.
     */
    public synchronized void setRootLoggerLevel(final String level) {
        LogManager.getRootLogger().setLevel(XLevel.toLevel(level));
    }

    /**
     * Gets the level of the logger of the give name.
     *
     * @param logger The logger to inspect.
     */
    public String getLoggerEffectiveLevel(final String logger) {
        if (logger == null) {
            throw new IllegalArgumentException("logger is null");
        }

        Level level = LogManager.getLogger(logger).getEffectiveLevel();

        if (level != null) {
            return level.toString();
        }

        return null;
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

        Level level = LogManager.getLogger(logger).getLevel();

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
     * Get the content of logging configuration file.
     *
     * @return the content of logging configuration file
     */
    public synchronized String getConfiguration() {
        File file = resolveConfigurationFile();
        if (file == null || !file.canRead()) {
            return null;
        }
        Reader in = null;
        try {
            StringBuffer configuration = new StringBuffer();
            in = new InputStreamReader(new FileInputStream(file));
            char[] buffer = new char[4096];
            for (int size = in.read(buffer); size >= 0; size = in.read(buffer)) {
                configuration.append(buffer, 0, size);
            }
            return configuration.toString();
        } catch (IOException e) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Overwrites the content of logging configuration file.
     *
     * @param configuration the new content of logging configuration file
     */
    public synchronized void setConfiguration(final String configuration) throws IOException {
        if (configuration == null || configuration.length() == 0) {
            throw new IllegalArgumentException("configuration is null or an empty string");
        }

        File file = resolveConfigurationFile();
        if (file == null) {
            throw new IllegalStateException("Configuration file is null");
        }

        // make parent directory if necessary
        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                throw new IllegalStateException("Could not create parent directory of log configuration file: " + file.getParent());
            }
        }

        // verify that the file is writable or does not exist
        if (file.exists() && !file.canWrite()) {
            throw new IllegalStateException("Configuration file is not writable: " + file.getAbsolutePath());
        }

        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(configuration.getBytes());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
    }

    /**
     * Force the logging system to reconfigure.
     */
    public void reconfigure() {
        File file = resolveConfigurationFile();
        if (file == null) {
            return;
        }
        try {
            URLConfigurator.configure(file.toURL());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // refresh the level info for every log
        GeronimoLogFactory logFactory = (GeronimoLogFactory) LogFactory.getFactory();
        Set instances = logFactory.getInstances();
        for (Iterator iterator = instances.iterator(); iterator.hasNext();) {
            Object log = iterator.next();
            if (log instanceof CachingLog4jLog) {
                ((CachingLog4jLog)log).updateLevelInfo();
            }
        }
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
        LogFactory logFactory = LogFactory.getFactory();
        if (!(logFactory instanceof GeronimoLogFactory)) {
            throw new IllegalStateException("Commons log factory: " + logFactory + " is not a GeronimoLogFactory");
        }

        synchronized (this) {
            timer = new Timer(true);

            // Peroidally check the configuration file
            schedule();

            // Make sure the root Logger has loaded
            LogManager.getRootLogger();

            reconfigure();
        }

        // Change all of the loggers over to use log4j
        GeronimoLogFactory geronimoLogFactory = (GeronimoLogFactory) logFactory;
        synchronized (geronimoLogFactory) {
            if (!(geronimoLogFactory.getLogFactory() instanceof CachingLog4jLogFactory)) {
                geronimoLogFactory.setLogFactory(new CachingLog4jLogFactory());
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

    private synchronized File resolveConfigurationFile() {
        try {
            return serverInfo.resolve(configurationFile);
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

                    File file = resolveConfigurationFile();
                    if (file == null) {
                        return;
                    }

                    lastModified = file.lastModified();
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
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(Log4jService.class);

        infoFactory.addAttribute("configurationFile", String.class, true);
        infoFactory.addAttribute("refreshPeriod", int.class, true);
        infoFactory.addAttribute("configuration", String.class, false);
        infoFactory.addAttribute("rootLoggerLevel", String.class, false);

        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");

        infoFactory.addOperation("reconfigure");
        infoFactory.addOperation("setLoggerLevel", new Class[]{String.class, String.class});
        infoFactory.addOperation("getLoggerLevel", new Class[]{String.class});
        infoFactory.addOperation("getLoggerEffectiveLevel", new Class[]{String.class});

        infoFactory.setConstructor(new String[]{"configurationFile", "refreshPeriod", "ServerInfo"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
