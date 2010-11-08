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
package org.apache.geronimo.derby;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * ReplaceMe
 *
 * @version $Rev$ $Date$
 */
public class DerbyLogGBean implements DerbyLog {

    private final DerbySystem derby;
    private File logFile = null;

    public DerbyLogGBean(DerbySystem derby) {
        this.derby = derby;
    }

    public SearchResults searchLog(Integer startLine, Integer endLine, Integer max, String text) {
        // Get log file
        if(logFile == null) {
            logFile = new File(derby.getDerbyHome(), "derby.log");
            if(!logFile.canRead()) {
                throw new IllegalStateException("Cannot read Derby log file at '"+logFile.getAbsolutePath()+"'");
            }
        }
        // Check that the text pattern is valid
        Pattern textPattern;
        try {
            textPattern = text == null || text.equals("") ? null : Pattern.compile(text);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Bad regular expression '"+text+"'");
        }
        return searchFile(logFile, textPattern, startLine, endLine,
                max == null ? MAX_SEARCH_RESULTS : Math.min(MAX_SEARCH_RESULTS, max.intValue()));
    }

    private static SearchResults searchFile(File file, Pattern textSearch, Integer start, Integer stop, int max) {
        List list = new LinkedList();
        boolean capped = false;
        int lineCount = 0;
        FileInputStream logInputStream = null;
        try {
            logInputStream = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(logInputStream, "US-ASCII"));
            Matcher text = textSearch == null ? null : textSearch.matcher("");
            max = Math.min(max, MAX_SEARCH_RESULTS);
            String line;
            while ((line = reader.readLine()) != null) {
                ++lineCount;
                if(start != null && start.intValue() > lineCount) {
                    continue;
                }
                if(stop != null && stop.intValue() < lineCount) {
                    continue;
                }
                if(text != null) {
                    text.reset(line);
                    if(!text.find()) {
                        continue;
                    }
                }
                list.add(new LogMessage(lineCount,line.toString()));
                if(list.size() > max) {
                    list.remove(0);
                    capped = true;
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
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("Derby Log", DerbyLogGBean.class);

        infoFactory.addReference("DerbySystem", DerbySystem.class, "GBean");
        infoFactory.addInterface(DerbyLog.class);
        infoFactory.setConstructor(new String[]{"DerbySystem"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
