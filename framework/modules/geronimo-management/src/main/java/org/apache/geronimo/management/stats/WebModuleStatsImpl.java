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

package org.apache.geronimo.management.stats;

import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.TimeStatistic;

import org.apache.geronimo.management.geronimo.stats.WebModuleStats;

/**
 * Geronimo implementation of the JSR-77 style WebModule interface. This
 * is not required by JSR-77, but provides useful statistics for a web app
 *
 * @version $Revison$ $Date$
 */
public class WebModuleStatsImpl extends StatsImpl implements WebModuleStats {
    // Static data

    private CountStatisticImpl processingTime;

    private CountStatisticImpl startupTime;

    private CountStatisticImpl tldScanTime;

    // transient data
    private TimeStatisticImpl sessionAliveTime; // total, max, count

    private CountStatisticImpl sessionCount;

    private CountStatisticImpl activeSessionCount;

    private CountStatisticImpl rejectedSessionCount;

    private CountStatisticImpl expiredSessionCount;

    public WebModuleStatsImpl() {
        processingTime = new CountStatisticImpl("Processing Time", StatisticImpl.UNIT_TIME_MILLISECOND,
                "Time to process all the requests ", 0);
        startupTime = new CountStatisticImpl("Startup Time", StatisticImpl.UNIT_TIME_MILLISECOND,
                "Time to start this application", 0);
        tldScanTime = new CountStatisticImpl("TLD Scan Time", StatisticImpl.UNIT_TIME_MILLISECOND,
                "Time to scan TLDs ", 0);
        sessionAliveTime = new TimeStatisticImpl("Session Alive Time", StatisticImpl.UNIT_TIME_SECOND,
                "The time for session");
        sessionCount = new CountStatisticImpl("Session Count", StatisticImpl.UNIT_COUNT,
                "total number of sessions created ", 0);
        activeSessionCount = new CountStatisticImpl("Active Session Count", StatisticImpl.UNIT_COUNT,
                "currently active sessions ", 0);
        rejectedSessionCount = new CountStatisticImpl("Rejected Session Count", StatisticImpl.UNIT_COUNT,
                "rejected sessions ", 0);
        expiredSessionCount = new CountStatisticImpl("Expired Session Count", StatisticImpl.UNIT_COUNT,
                "expired sessions ", 0);
        addStat("processingTime", processingTime);
        addStat("startupTime", startupTime);
        addStat("tldScanTime", tldScanTime);
        addStat("sessionAliveTime", sessionAliveTime); // better name
        addStat("sessionCount", sessionCount);
        addStat("activeSessionCount", activeSessionCount);
        addStat("rejectedSessionCount", rejectedSessionCount);
        addStat("expiredSessionCount", expiredSessionCount);
    }

    public CountStatistic getProcessingTime() {
        return processingTime;
    }

    public CountStatistic getStartupTime() {
        return startupTime;
    }

    public CountStatistic getTldScanTime() {
        return tldScanTime;
    }

    public TimeStatistic getSessionAliveTime() {
        return sessionAliveTime;
    }

    public CountStatistic getSessionCount() {
        return sessionCount;
    }

    public CountStatistic getActiveSessionCount() {
        return activeSessionCount;
    }

    public CountStatistic getExpiredSessionCount() {
        return expiredSessionCount;
    }

    public CountStatistic getRejectedSessionCount() {
        return rejectedSessionCount;
    }

    /**
     * These setters are used by native implementation
     */

    public void setProcessingTime(long processingTime) {
        this.processingTime.setCount(processingTime);
    }

    // This is static data, need not be refreshed
    public void setStartupTime(long startupTime) {
        this.startupTime.setCount(startupTime);
    }

    public void setTldScanTime(long tldScanTime) {
        this.tldScanTime.setCount(tldScanTime);
    }

    public void setSessionAliveTime(int count, long minTime, long maxTime, long totalTime) {
        this.sessionAliveTime.setCount(count);
        this.sessionAliveTime.setMinTime(minTime);
        this.sessionAliveTime.setMaxTime(maxTime);
        this.sessionAliveTime.setTotalTime(totalTime);
    }

    public void setSessionCount(long sessionCount) {
        this.sessionCount.setCount(sessionCount);
    }

    public void setActiveSessionCount(int activeSessionCount) {
        this.activeSessionCount.setCount(activeSessionCount);
    }

    public void setExpiredSessionCount(long expiredSessionCount) {
        this.expiredSessionCount.setCount(expiredSessionCount);
    }

    public void setRejectedSessionCount(int rejectedSessionCount) {
        this.rejectedSessionCount.setCount(rejectedSessionCount);
    }

    // Is this needed ?
    public void setLastSampleTime(long time) {
        sessionAliveTime.setLastSampleTime(time);
        activeSessionCount.setLastSampleTime(time);
        expiredSessionCount.setLastSampleTime(time);
        rejectedSessionCount.setLastSampleTime(time);
    }

}
