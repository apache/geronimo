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

import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.Stats;
import javax.management.j2ee.statistics.TimeStatistic;

/**
 * @version $Rev$ $Date$
 * 
 */
public interface WebModuleStats extends Stats {
    // -----------------------
    // static data
    // -----------------------

    /**
     * @return The cumulative processing times of requests by all servlets in
     *         this Context
     */
    public CountStatistic getProcessingTime();

    /**
     * @return The time this context was started.
     */
    // public CountStatistic getStartTime();
    
    /**
     * @return The time (in milliseconds) it took to start this context.
     */
    public CountStatistic getStartupTime();

    /**
     * Scan the TLD contents of all tag library descriptor files (including
     * those inside the jars that are 'accesible' to this webapp) and register
     * any application event listeners found there.
     * 
     * @return
     */
    public CountStatistic getTldScanTime();

    // -----------------------
    // Transient data
    // -----------------------

    /**
     * Gets the time (in seconds) that an expired session had been alive.
     * (count, max, total)
     * 
     * @return Time (count, max, total) that an expired session had been alive.
     */
    public TimeStatistic getSessionAliveTime();

    /**
     * Returns the total number of sessions created by this manager
     * 
     * @return Total number of sessions created by this manager
     */
    public CountStatistic getSessionCount();

    /**
     * Gets the number of currently active sessions.
     * 
     * @return Number of currently active sessions
     */
    public CountStatistic getActiveSessionCount();

    /**
     * Gets the number of sessions that have expired.
     * 
     * @return Number of sessions that have expired
     */
    public CountStatistic getExpiredSessionCount();

    /**
     * Gets the number of sessions that were not created because the maximum
     * number of active sessions was reached.
     * 
     * @return Number of rejected sessions
     */
    public CountStatistic getRejectedSessionCount();

}
