/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.jetty.requestlog;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.MappedByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * Jetty implementation of the WebAccessLog management interface.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class JettyLogManagerImpl implements JettyLogManager {
    private final static Log log = LogFactory.getLog(JettyLogManagerImpl.class);
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
    private Collection logGbeans;

    public JettyLogManagerImpl(Collection logGbeans) {
        this.logGbeans = logGbeans;
    }

    public String[] getLogFileNames() {
        List files = new ArrayList();
        for (Iterator it = logGbeans.iterator(); it.hasNext();) {
            JettyRequestLog log = (JettyRequestLog) it.next();
            if(log.getFilename() != null) {
                files.add(log.getFilename());
            }
        }
        return (String[]) files.toArray(new String[files.size()]);
    }

    public SearchResults getMatchingItems(String logFile, String host, String user, String method, String uri,
                                          Date startDate, Date endDate, Integer skipResults, Integer maxResults) {
        File log = null;
        for (Iterator it = logGbeans.iterator(); it.hasNext();) {
            JettyRequestLog logger = (JettyRequestLog) it.next();
            if(logger.getFilename() != null && logger.getFilename().equals(logFile)) {
                log = new File(logger.getAbsoluteFilePath());
                break;
            }
        }
        if(log == null) {
            throw new IllegalArgumentException("Unknown log file '"+logFile+"'");
        }

        return search(log, host, user, method, uri, startDate, endDate, skipResults, maxResults);
    }

    private SearchResults search(File file, String host, String user, String method, String uri, Date startDate,
                                 Date endDate, Integer skipResults, Integer maxResults) {
        // Clean up the arguments so we know what we've really got
        if(host != null && host.equals("")) host = null;
        if(user != null && user.equals("")) user = null;
        if(method != null && method.equals("")) method = null;
        if(uri != null && uri.equals("")) uri = null;
        // Do the search
        List list = new LinkedList();
        boolean capped = false;
        int lineCount = 0;
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            FileChannel fc = raf.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            CharBuffer cb = Charset.forName("US-ASCII").decode(bb); //todo: does Jetty use a different charset on a foreign PC?
            Matcher lines = FULL_LINE_PATTERN.matcher(cb);
            Matcher target = ACCESS_LOG_PATTERN.matcher("");
            long start = startDate == null ? 0 : startDate.getTime();
            long end = endDate == null ? 0 : endDate.getTime();
            SimpleDateFormat format = (start == 0 && end == 0) ? null : new SimpleDateFormat(ACCESS_LOG_DATE_FORMAT);
            int max = maxResults == null ? MAX_SEARCH_RESULTS : Math.min(maxResults.intValue(), MAX_SEARCH_RESULTS);
log.warn("CRITERIA: "+file.getAbsolutePath()+" "+host+" "+user+" "+method+" "+uri+" "+start+" "+end+" "+skipResults+" "+max);
            while(lines.find()) {
                ++lineCount;
                if(capped) {
                    continue;
                }
                CharSequence line = cb.subSequence(lines.start(), lines.end());
                target.reset(line);
                if(target.find()) {
                    if(host != null && !host.equals(target.group(GROUP_HOST))) {
                        continue;
                    }
                    if(user != null && !user.equals(target.group(GROUP_USER))) {
                        continue;
                    }
                    if(method != null && !method.equals(target.group(GROUP_METHOD))) {
                        continue;
                    }
                    if(uri != null && !target.group(GROUP_URI).startsWith(uri)) {
                        continue;
                    }
                    if(format != null) {
                        try {
                            long entry = format.parse(target.group(GROUP_DATE)).getTime();
                            if(start > entry) {
                                continue;
                            }
                            if(end > 0 && end < entry) {
                                continue;
                            }
                        } catch (ParseException e) {
                            // can't read the date, guess this record counts.
                        }
                    }
                    if(skipResults != null && skipResults.intValue() > lineCount) {
                        continue;
                    }
                    if(list.size() > max) {
                        capped = true;
                        continue;
                    }
                    list.add(new LogMessage(lineCount,line.toString()));
                }
            }
            fc.close();
            raf.close();
        } catch (Exception e) {
            log.error("Unexpected error processing logs", e);
        }
        return new SearchResults(lineCount, (LogMessage[]) list.toArray(new LogMessage[list.size()]), capped);
    }


    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("Jetty Log Manager", JettyLogManagerImpl.class);
        infoFactory.addReference("LogGBeans", JettyRequestLog.class);
        infoFactory.addInterface(JettyLogManager.class);

        infoFactory.setConstructor(new String[]{"LogGBeans"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
/*
    public static void main(String[] args) {
        String jetty = "127.0.0.1 - - [07/Sep/2005:19:54:41 +0000] \"GET /console/ HTTP/1.1\" 302 0 \"-\" \"Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.7.10) Gecko/20050715 Firefox/1.0.6 SUSE/1.0.6-4.1\" -";
        String tomcat = "127.0.0.1 - - [07/Sep/2005:15:51:18 -0500] \"GET /console/portal/server/server_info HTTP/1.1\" 200 11708";

        SimpleDateFormat format = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss ZZZZ");
        try {
            Pattern p = Pattern.compile("(\\S*) (\\S*) (\\S*) \\[(.*)\\] \\\"(\\S*) (\\S*).*?\\\" (\\S*) (\\S*).*");
            Matcher m = p.matcher(jetty);
            if(m.matches()) {
                System.out.println("Group 1: "+m.group(1)); // client
                System.out.println("Group 2: "+m.group(2)); // ?? server host?
                System.out.println("Group 3: "+m.group(3)); // username
                System.out.println("Group 4: "+format.parse(m.group(4))); // date
                System.out.println("Group 5: "+m.group(5)); // method
                System.out.println("Group 5: "+m.group(6)); // URI
                System.out.println("Group 6: "+m.group(7)); // response code
                System.out.println("Group 7: "+m.group(8)); // response length
            } else {
                System.out.println("No match");
            }
            m = p.matcher(tomcat);
            if(m.matches()) {
                System.out.println("Group 1: "+m.group(1));
                System.out.println("Group 2: "+m.group(2));
                System.out.println("Group 3: "+m.group(3));
                System.out.println("Group 4: "+format.parse(m.group(4)));
                System.out.println("Group 5: "+m.group(5)); // method
                System.out.println("Group 5: "+m.group(6)); // URI
                System.out.println("Group 6: "+m.group(7)); // response code
                System.out.println("Group 7: "+m.group(8)); // response length
            } else {
                System.out.println("No match");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
*/
}
