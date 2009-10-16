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
package org.apache.geronimo.management.geronimo;

import java.io.Serializable;

/**
 * A general log manager.
 *
 * @version $Rev$ $Date$
 */
public interface Log {
    /**
     * The most search lines that will ever be returned, no matter what you
     * ask for.  This is to conserve memory and transfer bandwidth.
     */
    public final static int MAX_SEARCH_RESULTS = 1000;
    
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
        private final int lineCount; // total lines in log file
        private final LogMessage[] results;
        private final boolean capped; // whether there were more matched than are returned here

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
