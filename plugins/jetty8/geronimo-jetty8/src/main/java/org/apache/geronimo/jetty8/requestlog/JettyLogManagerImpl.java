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
package org.apache.geronimo.jetty8.requestlog;

import java.io.File;
import java.io.FilenameFilter;
import java.io.RandomAccessFile;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.system.serverinfo.ServerInfo;

/**
 * Jetty implementation of the WebAccessLog management interface.
 *
 * @version $Rev$ $Date$
 */
public class JettyLogManagerImpl implements JettyLogManager {
    private static final Logger log = LoggerFactory.getLogger(JettyLogManagerImpl.class);

    // Pattern that matches the date in the logfile name
    private final static Pattern FILENAME_DATE_PATTERN = Pattern.compile("[-_ /.](((19|20)\\d\\d)[-_ /.](0[1-9]|1[012])[-_ /.](0[1-9]|[12][0-9]|3[01]))");
    private final static int GROUP_FILENAME_FULL_DATE = 1;
    private final static int GROUP_FILENAME_YEAR  = 2;
    private final static int GROUP_FILENAME_MONTH = 4;
    private final static int GROUP_FILENAME_DAY   = 5;
    // NOTE:  The file separators are specified here rather than using something like File.separator because
    //        they are hard coded in config plans and sometimes in java code itself rather than being dependent
    //        upon the OS.  This should be fixed someday, but for now we will manually check for either format.
    private final static String FILE_SEPARATOR_UNIX_STYLE = "/";
    private final static String FILE_SEPARATOR_WIN_STYLE = "\\";

    // Pattern that matches a single line  (used to calculate line numbers)
    private final static Pattern FULL_LINE_PATTERN = Pattern.compile("^.*", Pattern.MULTILINE);
    private final static Pattern ACCESS_LOG_PATTERN = Pattern.compile("(\\S*) (\\S*) (\\S*) \\[(.*)\\] \\\"(\\S*) (\\S*).*?\\\" (\\S*) (\\S*).*");
    private final static int GROUP_HOST = 1;
    private final static int GROUP_USER = 3;
    private final static int GROUP_DATE = 4;
    private final static int GROUP_METHOD = 5;
    private final static int GROUP_URI = 6;
    private final static int GROUP_RESPONSE_CODE = 7;
    private final static int GROUP_RESPONSE_LENGTH = 8;
    private final static String ACCESS_LOG_DATE_FORMAT = "dd/MMM/yyyy:HH:mm:ss ZZZZ";
    private final static String LOG_FILE_NAME_FORMAT = "yyyy_MM_dd";
    private final Collection<JettyRequestLog> logGbeans;
    private final ServerInfo serverInfo;  

    public JettyLogManagerImpl(ServerInfo serverInfo, Collection<JettyRequestLog> logGbeans) {
        this.serverInfo = serverInfo;
        this.logGbeans = logGbeans;
    }

    /**
     * Gets the name of all logs used by this system.  Typically there
     * is only one, but specialized cases may use more.
     *
     * @return An array of all log names
     *
     */
    public String[] getLogNames() {
        List<String> logNames = new ArrayList<String>();
        for (JettyRequestLog jettyLog : logGbeans) {
            if (jettyLog.getFilename() != null) {
                logNames.add(jettyLog.getFilename());
            }
        }
        return logNames.toArray(new String[logNames.size()]);
    }

    /**
     * Gets the names of all log files for this log name.  
     *
     * @param logName The name of the log for which to return the specific file names.
     *
     * @return An array of log file names
     *
     */
    public String[] getLogFileNames(String logName) {
        List<String> names = new ArrayList<String>();

        // Find all the files for this logName
        File[] logFiles = getLogFiles(logName);

        if (logFiles !=null) {
            for (File logFile : logFiles) {
                names.add(logFile.getName());
            }
        }
        return names.toArray(new String[names.size()]);
    }

    /**
     * Gets the name of all log files used by this log.  Typically there
     * is only one, but specialized cases may use more.
     *
     * @param logName The name of the log for which to return the specific files.
     *
     * @return An array of all log file names
     *
     */
    private File[] getLogFiles(String logName) {
        File[] logFiles = null;

        try {
            String fileNamePattern = logName;
            if (fileNamePattern.indexOf(FILE_SEPARATOR_UNIX_STYLE) > -1) {
                fileNamePattern = fileNamePattern.substring(fileNamePattern.lastIndexOf(FILE_SEPARATOR_UNIX_STYLE) + 1);
            } else if (fileNamePattern.indexOf(FILE_SEPARATOR_WIN_STYLE) > -1) {
                fileNamePattern = fileNamePattern.substring(fileNamePattern.lastIndexOf(FILE_SEPARATOR_WIN_STYLE) + 1);
            }

            String logFile = serverInfo.resolveServerPath(logName);

            File parent = new File(logFile).getParentFile();

            if (parent != null) {
                logFiles = parent.listFiles(new PatternFilenameFilter(fileNamePattern));
            }
        } catch (Exception e) {
            log.error("Exception attempting to locate Jetty log files", e);
            logFiles = new File[0];
        }
        return logFiles;
    }

    /**
     * Searches the log for records matching the specified parameters.  The
     * maximum results returned will be the lesser of 1000 and the
     * provided maxResults argument.
     *
     * @see #MAX_SEARCH_RESULTS
     */
    public SearchResults getMatchingItems(String logName, String host, String user, String method, String uri, Date startDate,
                                          Date endDate, Integer skipResults, Integer maxResults) {

        // Clean up the arguments so we know what we've really got
        if(host != null && host.equals("")) host = null;
        if(user != null && user.equals("")) user = null;
        if(method != null && method.equals("")) method = null;
        if(uri != null && uri.equals("")) uri = null;

        long start = startDate == null ? 0 : startDate.getTime();
        long end = endDate == null ? 0 : endDate.getTime();

        List<LogMessage> list = new LinkedList<LogMessage>();
        boolean capped = false;
        int lineCount = 0;
        int fileCount;

        // Find all the files for this logName
        File[] logFiles = getLogFiles(logName);

        if (logFiles !=null) {
            for (File logFile : logFiles) {
                fileCount = 0;
                try {
                    // Obtain the date for the current log file
                    String fileName = logFile.getName();
                    Matcher fileDate = FILENAME_DATE_PATTERN.matcher(fileName);
                    fileDate.find();
                    SimpleDateFormat simpleFileDate = new SimpleDateFormat(LOG_FILE_NAME_FORMAT);
                    long logFileTime = simpleFileDate.parse(fileDate.group(GROUP_FILENAME_FULL_DATE)).getTime();

                    // Check if the dates are null (ignore) or fall within the search range
                    if ((start == 0 && end == 0)
                            || (start > 0 && start <= logFileTime && end > 0 && end >= logFileTime)) {

                        // It's in the range, so process the file
                        RandomAccessFile raf = new RandomAccessFile(logFile, "r");
                        FileChannel fc = raf.getChannel();
                        MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                        CharBuffer cb = Charset.forName("US-ASCII").decode(bb); //todo: does Jetty use a different charset on a foreign PC?
                        Matcher lines = FULL_LINE_PATTERN.matcher(cb);
                        Matcher target = ACCESS_LOG_PATTERN.matcher("");
                        SimpleDateFormat format = (start == 0 && end == 0) ? null : new SimpleDateFormat(ACCESS_LOG_DATE_FORMAT);
                        int max = maxResults == null ? MAX_SEARCH_RESULTS : Math.min(maxResults.intValue(), MAX_SEARCH_RESULTS);

                        while (lines.find()) {
                            ++lineCount;
                            ++fileCount;
                            if (capped) {
                                continue;
                            }
                            CharSequence line = cb.subSequence(lines.start(), lines.end());
                            target.reset(line);
                            if (target.find()) {
                                if (host != null && !host.equals(target.group(GROUP_HOST))) {
                                    continue;
                                }
                                if (user != null && !user.equals(target.group(GROUP_USER))) {
                                    continue;
                                }
                                if (method != null && !method.equals(target.group(GROUP_METHOD))) {
                                    continue;
                                }
                                if (uri != null && !target.group(GROUP_URI).startsWith(uri)) {
                                    continue;
                                }
                                if (format != null) {
                                    try {
                                        long entry = format.parse(target.group(GROUP_DATE)).getTime();
                                        if (start > entry) {
                                            continue;
                                        }
                                        if (end > 0 && end < entry) {
                                            continue;
                                        }
                                    } catch (ParseException e) {
                                        // can't read the date, guess this record counts.
                                    }
                                }
                                if (skipResults != null && skipResults > lineCount) {
                                    continue;
                                }
                                if (list.size() > max) {
                                    capped = true;
                                    continue;
                                }
                                list.add(new LogMessage(fileCount, line.toString()));
                            }
                        }
                        fc.close();
                        raf.close();
                    }
                } catch (Exception e) {
                    log.error("Unexpected error processing logs", e);
                }
            }
        }
        return new SearchResults(lineCount, list.toArray(new LogMessage[list.size()]), capped);
    }


    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("Jetty Log Manager", JettyLogManagerImpl.class);
        infoFactory.addReference("LogGBeans", JettyRequestLog.class);
        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");
        infoFactory.addInterface(JettyLogManager.class);

        infoFactory.setConstructor(new String[]{"ServerInfo","LogGBeans"});  
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    /*
     * Static inner class implementation of java.io.Filename. This will help us
     * filter for only the files that we are interested in.
     */
    static class PatternFilenameFilter implements FilenameFilter {
        Pattern pattern;
        //todo: put this pattern in a GBean parameter?
        PatternFilenameFilter(String fileNamePattern) {
            fileNamePattern = fileNamePattern.replaceAll("yyyy", "\\\\d{4}");
            fileNamePattern = fileNamePattern.replaceAll("yy", "\\\\d{2}");
            fileNamePattern = fileNamePattern.replaceAll("mm", "\\\\d{2}");
            fileNamePattern = fileNamePattern.replaceAll("dd", "\\\\d{2}");
            this.pattern = Pattern.compile(fileNamePattern);
        }

        public boolean accept(File file, String fileName) {
            return pattern.matcher(fileName).matches();
        }
    }
}
