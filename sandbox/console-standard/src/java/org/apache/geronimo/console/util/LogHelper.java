/**
 *
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.
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

package org.apache.geronimo.console.util;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Iterator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.util.Enumeration;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

public class LogHelper extends KernelHelper {

    private static ObjectName loggerObjName;

    private static final String LOGGER_LOG_LEVEL_ATTR = "rootLoggerLevel";

    private static final String LOGGER_CONFIG_FILE_ATTR = "configurationFile";

    private static final String LOGGER_REFRESH_PERIOD_ATTR = "refreshPeriod";

    private static final String LOGGER_CONFIG_ATTR = "configuration";

    private static final String LOGGER_RECONFIG_FUNC = "reconfigure";

    private static int lineCount = 0;

    private static ArrayList logs = new ArrayList();

    private static boolean started;

    public static String getLogLevel() {
        return get(loggerObjName, LOGGER_LOG_LEVEL_ATTR).toString();
    }

    public static void setLogLevel(String newLevel) {
        set(loggerObjName, LOGGER_LOG_LEVEL_ATTR, newLevel);
    }

    public static String getConfigFile() {
        return (String) get(loggerObjName, LOGGER_CONFIG_FILE_ATTR);
    }

    public static void setConfigFile(String newFile) {
        set(loggerObjName, LOGGER_CONFIG_FILE_ATTR, newFile);
    }

    public static Integer getRefreshPeriod() {
        Object ref = get(loggerObjName, LOGGER_REFRESH_PERIOD_ATTR);
        return (ref != null ? new Integer(ref.toString()) : null);
    }

    public static void setRefreshPeriod(int refreshPeriod) {
        set(loggerObjName, LOGGER_REFRESH_PERIOD_ATTR, new Integer(
                refreshPeriod));
    }

    public static String getConfiguration() {
        return (String) get(loggerObjName, LOGGER_CONFIG_ATTR);
    }

    public static void setConfiguration(String newConfig) {
        set(loggerObjName, LOGGER_CONFIG_ATTR, newConfig);
    }

    public static void reconfigure() throws Exception {
        invoke(loggerObjName, LOGGER_RECONFIG_FUNC);
    }

    public static void refresh() throws IOException {
        logs.clear();
        lineCount = 0;
        started = false;
        BufferedReader in = new BufferedReader(getFileReader());
        if (in != null) {
            Stack holder = new Stack();
            for (String line = in.readLine(); line != null; line = in
                    .readLine()) {
                if (line.indexOf("DEBUG") > -1 || line.indexOf("INFO") > -1
                        || line.indexOf("WARN") > -1
                        || line.indexOf("ERROR") > -1
                        || line.indexOf("FATAL") > -1
                        || line.indexOf("TRACE") > -1) {
                    holder.push(line);
                    lineCount++;
                } else {
                    String top = (String) holder.pop();
                    holder.push(top + "\n" + line);
                }
            }
            logs.addAll(holder);
        }
        started = true;
    }

    public static ArrayList getLogs() throws IOException {
        if (!started) {
            refresh();
        }
        return logs;
    }

    public static List searchLogs(int startLine, int endLine, String logLevel,
            String searchString) throws IOException {
        return filterLogs(getLogs().subList(startLine - 1, endLine), logLevel,
                searchString);
    }

    public static List searchLogs(int startLine, int endLine)
            throws IOException {
        return searchLogs(startLine, endLine, null, null);
    }

    public static int getLineCount() throws IOException {
        if (!started) {
            refresh();
        }
        return lineCount;
    }

    private static ArrayList filterLogs(List listToFilter, String logLevel,
            String searchString) {
        ArrayList ret = new ArrayList();
        for (Iterator i = listToFilter.iterator(); i.hasNext();) {
            String msg = i.next().toString();
            boolean passed = true;
            passed = (logLevel == null || logLevel.trim().length() < 1 || msg
                    .indexOf(logLevel) > -1)
                    && (searchString == null
                            || searchString.trim().length() < 1 || msg
                            .toLowerCase().indexOf(searchString.toLowerCase()) > -1);
            if (passed) {
                ret.add(msg);
            }
        }
        return ret;
    }

    private static InputStreamReader getFileReader() {
        Stack files = new Stack();
        for (Enumeration e = Logger.getRootLogger().getAllAppenders(); e
                .hasMoreElements();) {
            Object o = e.nextElement();
            if (o instanceof FileAppender) {
                File logFile = new File(((FileAppender) o).getFile());
                if (logFile.isFile() && logFile.canRead()) {
                    try {
                        FileInputStream in = new FileInputStream(logFile);
                        files.push(in);
                    } catch (IOException ignore) {
                        /*
                         * Someone probably made a mistake in configuring the
                         * loggappender. Keep on processing the others.
                         */
                    }
                }
            }
        }
        return new InputStreamReader(new SequenceInputStream(files.elements()));
    }

    static {
        try {
            loggerObjName = new ObjectName(
                    ObjectNameConstants.ROOT_LOGGER_OBJECT_NAME);
        } catch (MalformedObjectNameException e) {

        }
    }

}
