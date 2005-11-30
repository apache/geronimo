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

package org.apache.geronimo.console.logmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.geronimo.console.util.KernelHelper;
import org.apache.geronimo.console.util.ObjectNameConstants;

public class WebAccessLogHelper extends KernelHelper {

    private static final String LOG_FILE_ATTR = "filename";

    private static Map logs = new TreeMap();

    private static boolean loaded = false;

    private static int lines = 0;

    private static Map loadLogs() throws IOException {
        if (!loaded) {
            refresh();
        }
        return logs;
    }

    public static ArrayList getLogsByDate(Date logDate) throws IOException {
        // Since the two date arg version is inclusive we just call that one
        // passing in the logDate as start and end dates.
        return filterLogsByDate(WebAccessLogCriteria.getGlobalMatcher(),
                logDate, logDate);
    }

    public static int getLineCount() throws IOException {
        if (!loaded) {
            refresh();
        }
        return lines;
    }

    public static ArrayList getLogs() throws IOException {
        return filterLogs(WebAccessLogCriteria.getGlobalMatcher());
    }

    public static ArrayList filterLogs(WebAccessLogCriteria criteria)
            throws IOException {
        Map log = loadLogs();
        ArrayList ret = new ArrayList();
        for (Iterator i = log.keySet().iterator(); i.hasNext();) {
            Date date = (Date) i.next();
            ArrayList msgs = (ArrayList) log.get(date);
            for (Iterator j = msgs.iterator(); j.hasNext();) {
                WebAccessLogCriteria obj = (WebAccessLogCriteria) j.next();
                if (obj.matches(criteria)) {
                    ret.add(obj);
                }
            }
        }
        return ret;
    }

    public static ArrayList filterLogsByDate(WebAccessLogCriteria criteria,
            Date startDate, Date endDate) throws IOException {
        Map log = loadLogs();
        ArrayList ret = new ArrayList();
        for (Iterator i = log.keySet().iterator(); i.hasNext();) {
            Date date = (Date) i.next();
            ArrayList msgs = (ArrayList) log.get(date);
            for (Iterator j = msgs.iterator(); j.hasNext();) {
                WebAccessLogCriteria obj = (WebAccessLogCriteria) j.next();
                if (obj.matches(criteria)
                        && isBetween(date, startDate, endDate)) {
                    ret.add(obj);
                }
            }
        }
        return ret;

    }

    public static ArrayList searchLogs(String requestHost, String authUser,
            String requestMethod, String requestedURI) throws IOException {
        return searchLogs(requestHost, authUser, requestMethod, requestedURI,
                null, null);
    }

    public static ArrayList searchLogs(String requestHost, String authUser,
            String requestMethod, String requestedURI, Date startDate,
            Date endDate) throws IOException {
        if (requestHost != null && requestHost.trim().length() == 0) {
            requestHost = null;
        }
        if (authUser != null && authUser.trim().length() == 0) {
            authUser = null;
        }
        if (requestMethod != null && requestMethod.trim().length() == 0) {
            requestMethod = null;
        }
        if (requestedURI != null && requestedURI.trim().length() == 0) {
            requestedURI = null;
        }
        WebAccessLogCriteria criteria = new WebAccessLogCriteria("",
                requestHost, authUser, requestMethod, requestedURI);
        if (startDate == null || endDate == null) {
            return filterLogs(criteria);
        } else {
            return filterLogsByDate(criteria, startDate, endDate);
        }
    }

    public static File[] getFiles() {
        String fileNamePattern = get(ObjectNameConstants.REQUEST_LOGGER_OBJECT_NAME, LOG_FILE_ATTR).toString();
        if (fileNamePattern.indexOf("/") > -1) {
            fileNamePattern = fileNamePattern.substring(fileNamePattern
                    .lastIndexOf("/") + 1);
        } else if (fileNamePattern.indexOf("\\") > -1) {
            fileNamePattern = fileNamePattern.substring(fileNamePattern
                    .lastIndexOf("\\") + 1);
        }

        Object[] arg = { get(ObjectNameConstants.REQUEST_LOGGER_OBJECT_NAME, LOG_FILE_ATTR).toString() };
        String[] parms = { String.class.getName() };
        try {
            String logFile = (String) invoke(ObjectNameConstants.SERVER_INFO_OBJECT_NAME, "resolvePath",
                    arg, parms);
            File f = new File(logFile).getParentFile();
            return (f != null ? f.listFiles(new PatternFilenameFilter(
                    fileNamePattern)) : new File[0]);
        } catch (Exception e) {
            return new File[0];
        }
    }

    public static void refresh() throws IOException {
        loaded = false;
        lines = 0;
        logs.clear();
        File[] logFiles = getFiles();
        for (int i = 0; i < logFiles.length; i++) {
            BufferedReader in = new BufferedReader(new FileReader(logFiles[i]));
            for (String line = in.readLine(); line != null; line = in
                    .readLine()) {
                try {
                    StringTokenizer split = new StringTokenizer(line, " ");
                    String remoteHost = split.nextToken().trim();
                    // Ignore host
                    split.nextToken();
                    String user = split.nextToken("[").trim();
                    String dateTime = split.nextToken(" ").substring(1).trim();
                    //ignore GMT offset
                    split.nextToken();
                    String reqMethod = split.nextToken().substring(1).trim();
                    String reqURI = split.nextToken().trim();
                    // Get date time.
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new SimpleDateFormat(
                            "dd/MMM/yyyy:HH:mm:ss").parse(dateTime));
                    // remove time so we can filter by date later.
                    calendar.clear(Calendar.MILLISECOND);
                    calendar.clear(Calendar.MINUTE);
                    calendar.clear(Calendar.SECOND);
                    // Weird java bug. calling calendar.clear(Calendar.HOUR)
                    // does not clear the hour but this works.
                    calendar.clear(calendar.HOUR_OF_DAY);
                    calendar.clear(calendar.HOUR);
                    ArrayList msgs;
                    if (logs.containsKey(calendar.getTime())) {
                        msgs = (ArrayList) logs.get(calendar.getTime());
                    } else {
                        msgs = new ArrayList();
                    }
                    WebAccessLogCriteria criteria = new WebAccessLogCriteria(
                            line, remoteHost, user, reqMethod, reqURI);
                    msgs.add(0, criteria);
                    logs.put(calendar.getTime(), msgs);
                    lines++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        loaded = true;
    }

    /**
     * Check if a date is between two dates (inclusive).
     *
     * @return true if date is between(inclusive) startDate and endDate.
     */
    private static boolean isBetween(Date date, Date startDate, Date endDate) {
        return (date.equals(startDate) || date.after(startDate))
                && (date.equals(endDate) || date.before(endDate));
    }


    /*
     * Static inner class implementation of java.io.Filename. This will help us
     * filter for only the files that we are interested in.
     */
    static class PatternFilenameFilter implements FilenameFilter {
        Pattern pattern;

        PatternFilenameFilter(String fileNamePattern) {
            fileNamePattern = fileNamePattern.replaceAll("yyyy", "\\\\d{4}");
            fileNamePattern = fileNamePattern.replaceAll("yy", "\\\\d{2}");
            fileNamePattern = fileNamePattern.replaceAll("mm", "\\\\d{2}");
            fileNamePattern = fileNamePattern.replaceAll("dd", "\\\\d{2}")
                    + ".*";
            this.pattern = Pattern.compile(fileNamePattern);
        }

        public boolean accept(File file, String fileName) {
            return pattern.matcher(fileName).matches();
        }
    }

}
