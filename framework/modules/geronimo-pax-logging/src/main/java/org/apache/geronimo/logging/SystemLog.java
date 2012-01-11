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
package org.apache.geronimo.logging;

import java.io.Serializable;

/**
 * @version $Rev$ $Date$
 */
public interface SystemLog {
    /**
     * The most search lines that will ever be returned, no matter what you
     * ask for.  This is to conserve memory and transfer bandwidth.
     */
    int MAX_SEARCH_RESULTS = 1000;
    
    /**
     * Gets the name of the file that configures the log system
     */
//    String getConfigFileName();
    
    /**
     * Sets the name of the file that the log system should configure itself from.
     */
//    void setConfigFileName(String fileName);
    
    /**
     * Gets the name of the log level used for the root logger.
     */
    String getRootLoggerLevel();
    
    /**
     * Sets the name of the log level used for the root logger.
     */
    void setRootLoggerLevel(String level);
    
    /**
     * Indicates how often the log system should check to see if its
     * configuration file has been updated.
     */
//    int getRefreshPeriodSeconds();
    
    /**
     * Sets how often the log system should check to see if its
     * configuration file has been updated.
     */
//    void setRefreshPeriodSeconds(int seconds);
    
    /**
     * Gets the name of all log files used by this log system.  Typically there
     * is only one, but specialized cases may use more.
     */
    String[] getLogFileNames();
    
    /**
     * Searches the log for records matching the specified parameters.  The
     * maximum results returned will be the lesser of 1000 and the
     * provided maxResults argument.
     *
     * @see #MAX_SEARCH_RESULTS
     */
    SearchResults getMatchingItems(String logFile, Integer firstLine, Integer lastLine, String minLevel,
                                   String regex, int maxResults, boolean includeStackTraces);

    public static class LogMessage implements Serializable {
        private final int lineNumber;
        private final String lineContent;

        public LogMessage(int lineNumber, String lineContent) {
            this.lineNumber = lineNumber;
            this.lineContent = lineContent;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public String getLineContent() {
            return lineContent;
        }
    }

    public static class SearchResults implements Serializable {
        private final int lineCount; // total lines in file
        private final LogMessage[] results;
        private final boolean capped;

        public SearchResults(int lineCount, LogMessage[] results, boolean capped) {
            this.lineCount = lineCount;
            this.results = results;
            this.capped = capped;
        }

        public int getLineCount() {
            return lineCount;
        }

        public LogMessage[] getResults() {
            return results;
        }

        public boolean isCapped() {
            return capped;
        }
    }
}
