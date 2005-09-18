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
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Set;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;
import java.nio.channels.FileChannel;
import java.nio.MappedByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.log.GeronimoLogFactory;
import org.apache.geronimo.kernel.log.GeronimoLogging;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.system.logging.SystemLog;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.FileAppender;

/**
 * A Log4j logging service.
 *
 * @version $Rev$ $Date$
 */
public class Log4jService implements GBeanLifecycle, SystemLog {
    // A substitution variable in the file path in the config file
    private final static Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{.*?\\}");
    // Next 6 are patterns that identify log messages in our default format
    private final static Pattern DEFAULT_ANY_START = Pattern.compile("^\\d\\d\\:\\d\\d\\:\\d\\d\\,\\d\\d\\d (TRACE|DEBUG|INFO|WARN|ERROR|FATAL) .*");
    private final static Pattern DEFAULT_FATAL_START = Pattern.compile("^\\d\\d\\:\\d\\d\\:\\d\\d\\,\\d\\d\\d FATAL .*");
    private final static Pattern DEFAULT_ERROR_START = Pattern.compile("^\\d\\d\\:\\d\\d\\:\\d\\d\\,\\d\\d\\d (ERROR|FATAL) .*");
    private final static Pattern DEFAULT_WARN_START = Pattern.compile("^\\d\\d\\:\\d\\d\\:\\d\\d\\,\\d\\d\\d (WARN|ERROR|FATAL) .*");
    private final static Pattern DEFAULT_INFO_START = Pattern.compile("^\\d\\d\\:\\d\\d\\:\\d\\d\\,\\d\\d\\d (INFO|WARN|ERROR|FATAL) .*");
    private final static Pattern DEFAULT_DEBUG_START = Pattern.compile("^\\d\\d\\:\\d\\d\\:\\d\\d\\,\\d\\d\\d (DEBUG|INFO|WARN|ERROR|FATAL) .*");
    // Next 6 are patterns that identify log messages if the user changed the format -- but we assume the log level is in there somewhere
    private final static Pattern UNKNOWN_ANY_START = Pattern.compile("(TRACE|DEBUG|INFO|WARN|ERROR|FATAL)");
    private final static Pattern UNKNOWN_FATAL_START = Pattern.compile("FATAL");
    private final static Pattern UNKNOWN_ERROR_START = Pattern.compile("(ERROR|FATAL)");
    private final static Pattern UNKNOWN_WARN_START = Pattern.compile("(WARN|ERROR|FATAL)");
    private final static Pattern UNKNOWN_INFO_START = Pattern.compile("(INFO|WARN|ERROR|FATAL)");
    private final static Pattern UNKNOWN_DEBUG_START = Pattern.compile("(DEBUG|INFO|WARN|ERROR|FATAL)");
    // Pattern that matches a single line  (used to calculate line numbers and check for follow-on stack traces)
    private final static Pattern FULL_LINE_PATTERN = Pattern.compile("^.*", Pattern.MULTILINE);


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
     * @param refreshPeriod The refresh refreshPeriod (in seconds).
     */
    public Log4jService(final String configurationFile, final int refreshPeriod, ServerInfo serverInfo) {
        this.refreshPeriod = refreshPeriod;
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

        String currentLevel = this.getRootLoggerLevel();

        // ensure that the level has really been changed
        if (!currentLevel.equals(level)) {
            LogManager.getRootLogger().setLevel(XLevel.toLevel(level));
        }
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
    public synchronized int getRefreshPeriodSeconds() {
        return refreshPeriod;
    }

    /**
     * Set the refresh period.
     *
     * @param period the refresh period (in seconds)
     * @throws IllegalArgumentException if refresh period is <= 0
     */
    public synchronized void setRefreshPeriodSeconds(final int period) {
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
    public synchronized String getConfigFileName() {
        return configurationFile;
    }

    /**
     * Set the logging configuration URL.
     *
     * @param configurationFile the logging configuration file
     */
    public synchronized void setConfigFileName(final String configurationFile) {
        if (configurationFile == null) {
            throw new IllegalArgumentException("configurationFile is null");
        }

        // ensure that the file name has really been updated
        if (!this.configurationFile.equals(configurationFile)) {
            this.configurationFile = configurationFile;
            lastChanged = -1;
        }
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

    public synchronized String[] getLogFileNames() {
        List list = new ArrayList();
        for (Enumeration e = Logger.getRootLogger().getAllAppenders(); e.hasMoreElements();) {
            Object appender = e.nextElement();
            if (appender instanceof FileAppender) {
                list.add(((FileAppender) appender).getFile());
            }
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    private static SearchResults searchFile(File file, String targetLevel, Pattern textSearch, Integer start, Integer stop, int max, boolean stacks) {
        List list = new LinkedList();
        boolean capped = false;
        int lineCount = 0;
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            FileChannel fc = raf.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            CharBuffer cb = Charset.forName("US-ASCII").decode(bb); //todo: does Log4J use a different charset on a foreign PC?
            Matcher target = null;
            Matcher any = null;
            Matcher lines = FULL_LINE_PATTERN.matcher(cb);
            Matcher text = textSearch == null ? null : textSearch.matcher("");
            boolean hit = false;
            max = Math.min(max, MAX_SEARCH_RESULTS);
            while(lines.find()) {
                ++lineCount;
                if(target == null) {
                    if(DEFAULT_ANY_START.matcher(cb.subSequence(lines.start(), lines.end())).find()) {
                        target = getDefaultPatternForLevel(targetLevel).matcher("");
                        any = DEFAULT_ANY_START.matcher("");
                    } else {
                        target = getUnknownPatternForLevel(targetLevel).matcher("");
                        any = UNKNOWN_ANY_START.matcher("");
                    }
                }
                if(start != null && start.intValue() > lineCount) {
                    continue;
                }
                if(stop != null && stop.intValue() < lineCount) {
                    continue;
                }
                CharSequence line = cb.subSequence(lines.start(), lines.end());
                target.reset(line);
                if(target.find()) {
                    if(text != null) {
                        text.reset(line);
                        if(!text.find()) {
                            hit = false;
                            continue;
                        }
                    }
                    list.add(new LogMessage(lineCount,line.toString()));
                    if(list.size() > max) {
                        list.remove(0);
                        capped = true;
                    }
                    hit = true;
                } else if(stacks && hit) {
                    any.reset(line);
                    if(!any.find()) {
                        list.add(new LogMessage(lineCount,line.toString()));
                        if(list.size() > max) {
                            list.remove(0);
                            capped = true;
                        }
                    } else {
                        hit = false;
                    }
                }
            }
            fc.close();
            raf.close();
        } catch (Exception e) {}
        return new SearchResults(lineCount, (LogMessage[]) list.toArray(new LogMessage[list.size()]), capped);
    }

    private static String substituteSystemProps(String source) {
        StringBuffer buf = new StringBuffer();
        int last = 0;
        Matcher m = VARIABLE_PATTERN.matcher(source);
        while(m.find()) {
            buf.append(source.substring(last, m.start()));
            String prop = source.substring(m.start()+2, m.end()-1);
            buf.append(System.getProperty(prop));
            last = m.end();
        }
        buf.append(source.substring(last));
        return buf.toString();
    }

    private static Pattern getDefaultPatternForLevel(String targetLevel) {
        if(targetLevel.equals("FATAL")) {
            return DEFAULT_FATAL_START;
        } else if(targetLevel.equals("ERROR")) {
            return DEFAULT_ERROR_START;
        } else if(targetLevel.equals("WARN")) {
            return DEFAULT_WARN_START;
        } else if(targetLevel.equals("INFO")) {
            return DEFAULT_INFO_START;
        } else if(targetLevel.equals("DEBUG")) {
            return DEFAULT_DEBUG_START;
        } else {
            return DEFAULT_ANY_START;
        }
    }

    private static Pattern getUnknownPatternForLevel(String targetLevel) {
        if(targetLevel.equals("FATAL")) {
            return UNKNOWN_FATAL_START;
        } else if(targetLevel.equals("ERROR")) {
            return UNKNOWN_ERROR_START;
        } else if(targetLevel.equals("WARN")) {
            return UNKNOWN_WARN_START;
        } else if(targetLevel.equals("INFO")) {
            return UNKNOWN_INFO_START;
        } else if(targetLevel.equals("DEBUG")) {
            return UNKNOWN_DEBUG_START;
        } else {
            return UNKNOWN_ANY_START;
        }
    }

    public SearchResults getMatchingItems(String logFile, Integer firstLine, Integer lastLine, String minLevel, String text, int maxResults, boolean includeStackTraces) {
        // Ensure the file argument is really a log file!
        if(logFile == null) {
            throw new IllegalArgumentException("Must specify a log file");
        }
        String[] files = getLogFileNames();
        boolean found = false;
        for (int i = 0; i < files.length; i++) {
            if(files[i].equals(logFile)) {
                found = true;
                break;
            }
        }
        if(!found) {
            throw new IllegalArgumentException("Not a log file!");
        }
        // Check for valid log level
        if(minLevel == null) {
            minLevel = "TRACE";
        } else if(!minLevel.equals("FATAL") && !minLevel.equals("ERROR") && !minLevel.equals("WARN") &&
                !minLevel.equals("INFO") && !minLevel.equals("DEBUG") && !minLevel.equals("TRACE")) {
            throw new IllegalArgumentException("Not a valid log level");
        }
        // Check that the text pattern is valid
        Pattern textPattern = null;
        try {
            textPattern = text == null || text.equals("") ? null : Pattern.compile(text);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Bad regular expression '"+text+"'");
        }
        // Make sure we can find the log file
        File log = new File(substituteSystemProps(logFile));
        if(!log.exists()) {
            throw new IllegalArgumentException("Log file "+log.getAbsolutePath()+" does not exist");
        }
        // Run the search
        return searchFile(log, minLevel, textPattern, firstLine, lastLine, maxResults, includeStackTraces);
    }

    /**
     * Force the logging system to reconfigure.
     */
    public void reconfigure() {
        File file = resolveConfigurationFile();
        if (file == null) {
            return;
        }
        
        // Record the default console log level
        System.setProperty("org.apache.geronimo.log.ConsoleLogLevel", GeronimoLogging.getConsoleLogLevel().toString());

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
        if (logFactory instanceof GeronimoLogFactory) {
            synchronized (this) {
                timer = new Timer(true);

                // Periodically check the configuration file
                schedule();

                // Make sure the root Logger has loaded
                LogManager.getRootLogger();

                reconfigure();

                File file = resolveConfigurationFile();
                if (file != null) {
                    lastChanged = file.lastModified();
                }
            }

            // Change all of the loggers over to use log4j
            GeronimoLogFactory geronimoLogFactory = (GeronimoLogFactory) logFactory;
            synchronized (geronimoLogFactory) {
                if (!(geronimoLogFactory.getLogFactory() instanceof CachingLog4jLogFactory)) {
                    geronimoLogFactory.setLogFactory(new CachingLog4jLogFactory());
                }
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
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(Log4jService.class, "SystemLog");

        infoFactory.addAttribute("configFileName", String.class, true);
        infoFactory.addAttribute("refreshPeriodSeconds", int.class, true);
        infoFactory.addAttribute("configuration", String.class, false);
        infoFactory.addAttribute("rootLoggerLevel", String.class, false);

        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");

        infoFactory.addOperation("reconfigure");
        infoFactory.addOperation("setLoggerLevel", new Class[]{String.class, String.class});
        infoFactory.addOperation("getLoggerLevel", new Class[]{String.class});
        infoFactory.addOperation("getLoggerEffectiveLevel", new Class[]{String.class});
        infoFactory.addInterface(SystemLog.class);

        infoFactory.setConstructor(new String[]{"configFileName", "refreshPeriodSeconds", "ServerInfo"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
