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

import java.util.Date;

/**
 * A web container access log manager.
 *
 * @version $Rev$ $Date$
 */
public interface WebAccessLog extends Log {
    /**
     * Gets the name of all logs used by this system.  Typically there
     * is only one, but specialized cases may use more.
     *
     * @return An array of all log names
     *
     */
    String[] getLogNames();

    /**
     * Gets the name of all log files used by this log.  Typically there
     * is only one, but specialized cases may use more.
     *
     * @param log The name of the log for which to return the specific file names. 
     *
     * @return An array of all log file names
     *
     */
    String[] getLogFileNames(String log);

    /**
     * Searches the log for records matching the specified parameters.  The
     * maximum results returned will be the lesser of 1000 and the
     * provided maxResults argument.
     *
     * @see #MAX_SEARCH_RESULTS
     */
    SearchResults getMatchingItems(String logName, String host, String user, String method,
                                   String uri, Date startDate, Date endDate,
                                   Integer skipResults, Integer maxResults);

}
