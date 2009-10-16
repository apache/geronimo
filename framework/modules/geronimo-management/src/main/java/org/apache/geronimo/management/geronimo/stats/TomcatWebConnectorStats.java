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
package org.apache.geronimo.management.geronimo.stats;

import javax.management.j2ee.statistics.Stats;
import javax.management.j2ee.statistics.RangeStatistic;
import javax.management.j2ee.statistics.TimeStatistic;
import javax.management.j2ee.statistics.CountStatistic;

/**
 * Statistics exposed by a Tomcat web connector (http, https)
 * 
 * @version $Rev$ $Date$
 */
public interface TomcatWebConnectorStats extends WebConnectorStats {

    /**
     * Gets the Time statistics (count, total, Max, Min) for requests (includes
     * figures across all requests since statistics gathering started)
     */
    TimeStatistic getRequestTime();

    /**
     * Gets the number of errors that have been returned since statistics
     * gathering started.
     */
    CountStatistic getErrorCount();

    /**
     * Gets the number of requests being processed concurrently (as well as the
     * min and max since statistics gathering started).
     */
    RangeStatistic getActiveRequestCount();
    
    /**
     * Gets the number of threads currently available (as well as min and max 
     * since statistics gathering started.
     * current - The number of threads currently in the pool (currentThreadCount)
     *         - the number of threads currently serving requests (currentThreadBusy)
     *  HiMark    - The maximum number of unused threads that will be allowed to exist 
     *              until the thread pool starts stopping the unnecessary threads(maxSpareThread)
     * UpperBound - The max number of threads created by the connector (maxThreads)
     * LowerBound - The number of threads created by the connector in the begining (minSpareThread)
     */        
    // This could be a container statistics
    RangeStatistic getSpareThreadCount();
}
