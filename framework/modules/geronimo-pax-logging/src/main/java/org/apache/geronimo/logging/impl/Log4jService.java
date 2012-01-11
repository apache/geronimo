/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.logging.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

//import org.apache.felix.scr.annotations.Component;
//import org.apache.felix.scr.annotations.Service;
import org.apache.geronimo.logging.SystemLog;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * A Log4j logging service.
 *
 * @version $Rev$ $Date$
 */

//@Component(immediate = true)
//@Service
public class Log4jService implements SystemLog {
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

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Log4jService.class);

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
            LogManager.getRootLogger().setLevel(Level.toLevel(level));
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

        log.info("Setting logger level: logger=" + logger + ", level=" + level);
        Logger.getLogger(logger).setLevel(Level.toLevel(level));
    }

//    /**
//     * Get the refresh period.
//     *
//     * @return the refresh period (in seconds)
//     */
//    public synchronized int getRefreshPeriodSeconds() {
//        return refreshPeriod;
//    }
//
//    /**
//     * Set the refresh period.
//     *
//     * @param period the refresh period (in seconds)
//     * @throws IllegalArgumentException if refresh period is < 5
//     */
//    public synchronized void setRefreshPeriodSeconds(final int period) {
//        if (period < 5) {
//            throw new IllegalArgumentException("Refresh period must be at least 5 seconds");
//        }
//
//        if (this.refreshPeriod != period) {
//            this.refreshPeriod = period;
//            schedule();
//        }
//    }

//    /**
//     * Get the logging configuration URL.
//     *
//     * @return the logging configuration URL
//     */
//    public synchronized String getConfigFileName() {
//        return configurationFile;
//    }
//
//    /**
//     * Set the logging configuration URL.
//     *
//     * @param configurationFile the logging configuration file
//     */
//    public synchronized void setConfigFileName(final String configurationFile) {
//        if (configurationFile == null) {
//            throw new IllegalArgumentException("configurationFile is null");
//        }
//
//        log.debug("Using configuration file: {}", configurationFile);
//
//        // ensure that the file name has really been updated
//        if (!this.configurationFile.equals(configurationFile)) {
//            this.configurationFile = configurationFile;
//            lastChanged = -1;
//            reconfigure();
//        }
//    }

//    /**
//     * Get the content of logging configuration file.
//     *
//     * @return the content of logging configuration file
//     */
//    public synchronized String getConfiguration() {
//        File file = resolveConfigurationFile();
//        if (file == null || !file.canRead()) {
//            return null;
//        }
//        Reader in = null;
//        try {
//            StringBuilder configuration = new StringBuilder();
//            in = new InputStreamReader(new FileInputStream(file));
//            char[] buffer = new char[4096];
//            for (int size = in.read(buffer); size >= 0; size = in.read(buffer)) {
//                configuration.append(buffer, 0, size);
//            }
//            return configuration.toString();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (in != null) {
//                try {
//                    in.close();
//                } catch (IOException e1) {
//                    e1.printStackTrace();
//                }
//            }
//        }
//        return null;
//    }
//
//    /**
//     * Overwrites the content of logging configuration file.
//     *
//     * @param configuration the new content of logging configuration file
//     */
//    public synchronized void setConfiguration(final String configuration) throws IOException {
//        if (configuration == null || configuration.length() == 0) {
//            throw new IllegalArgumentException("configuration is null or an empty string");
//        }
//
//        File file = resolveConfigurationFile();
//        if (file == null) {
//            throw new IllegalStateException("Configuration file is null");
//        }
//
//        // make parent directory if necessary
//        if (!file.getParentFile().exists()) {
//            if (!file.getParentFile().mkdirs()) {
//                throw new IllegalStateException("Could not create parent directory of log configuration file: " + file.getParent());
//            }
//        }
//
//        // verify that the file is writable or does not exist
//        if (file.exists() && !file.canWrite()) {
//            throw new IllegalStateException("Configuration file is not writable: " + file.getAbsolutePath());
//        }
//
//        OutputStream out = null;
//        try {
//            out = new FileOutputStream(file);
//            out.write(configuration.getBytes());
//            log.info("Updated configuration file: {}", file);
//        } finally {
//            if (out != null) {
//                try {
//                    out.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

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
        FileInputStream logInputStream = null;
        try {
            logInputStream = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(logInputStream, System.getProperty("file.encoding")));
            Matcher target = null;
            Matcher any = null;
            Matcher text = textSearch == null ? null : textSearch.matcher("");
            boolean hit = false;
            max = Math.min(max, MAX_SEARCH_RESULTS);
            String line;
            while ((line = reader.readLine()) != null) {
                ++lineCount;
                if(target == null) {
                    if(DEFAULT_ANY_START.matcher(line).find()) {
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
        } catch (Exception e) {
            // TODO: improve exception handling
        } finally {
            if (logInputStream != null) {
                try {
                    logInputStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return new SearchResults(lineCount, (LogMessage[]) list.toArray(new LogMessage[list.size()]), capped);
    }

    private static String substituteSystemProps(String source) {
        StringBuilder buf = new StringBuilder();
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
            throw new IllegalArgumentException("Bad regular expression '"+text+"'", e);
        }
        // Make sure we can find the log file
        File file = new File(substituteSystemProps(logFile));
        if(!file.exists()) {
            throw new IllegalArgumentException("Log file "+file.getAbsolutePath()+" does not exist");
        }
        // Run the search
        return searchFile(file, minLevel, textPattern, firstLine, lastLine, maxResults, includeStackTraces);
    }

}
