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
import javax.management.j2ee.statistics.RangeStatistic;
import javax.management.j2ee.statistics.TimeStatistic;

import org.apache.geronimo.management.stats.StatsImpl;
import org.apache.geronimo.management.stats.StatisticImpl;
import org.apache.geronimo.management.stats.CountStatisticImpl;
import org.apache.geronimo.management.stats.RangeStatisticImpl;
import org.apache.geronimo.management.stats.BoundedRangeStatisticImpl;
import org.apache.geronimo.management.stats.TimeStatisticImpl;

/**
 * Geronimo implementation of the JSR-77 style WebConnectorStats interface. This
 * is not required by JSR-77, but provides useful statistics. This will be
 * discovered by mejb using 'stats' attribute.
 * 
 * @version $Revision$ $Date$
 */

public class TomcatWebConnectorStatsImpl extends StatsImpl implements TomcatWebConnectorStats {
    private TimeStatisticImpl requestTime; // total, max, count

    private CountStatisticImpl activeRequestCount;

    private CountStatisticImpl errorCount;

    private CountStatisticImpl bytesSentCount;

    private CountStatisticImpl bytesReceivedCount;
    
    // these come from ThreadPool
    private RangeStatisticImpl openConnectionCount; 
    
    private CountStatisticImpl busyThreadCount;
    
    // TODO - change the name to BoundedRangeStatisticsImpl
    private BoundedRangeStatisticImpl busyThreads;
    // TODO - spareThreads metrics = current - busy, maxSpareThreads, minSpareThreads 

    public TomcatWebConnectorStatsImpl() {
        requestTime = new TimeStatisticImpl("Request Time", StatisticImpl.UNIT_TIME_MILLISECOND,
                "The time to process all requests");
        activeRequestCount = new CountStatisticImpl("Active Request Count", StatisticImpl.UNIT_COUNT,
                "currently active requests ", 0);
        errorCount = new CountStatisticImpl("Error Count", StatisticImpl.UNIT_COUNT,
                "The numbet of Errors during the observed period", 0);
        bytesSentCount = new CountStatisticImpl("Bytes Sent", StatisticImpl.UNIT_COUNT,
                "The number of bytes sent during the observerd period", 0);
        bytesReceivedCount = new CountStatisticImpl("Bytes Received", StatisticImpl.UNIT_COUNT,
                "The number of bytes received during the observerd period", 0);
        openConnectionCount = new RangeStatisticImpl("" + "Open Connections", StatisticImpl.UNIT_COUNT,
                "Range for connections opened during the observed period", 0); // all 0's
        busyThreads = new BoundedRangeStatisticImpl("Busy Threads", StatisticImpl.UNIT_COUNT,
                "BoundedRange for Threads currently busy serving requests", 0, 0, 0);
        addStat("RequestTime", requestTime); // better name
        addStat("activeRequestCount", activeRequestCount);
        addStat("errorCount", errorCount);
        addStat("bytesSent", bytesSentCount);
        addStat("bytesReceived", bytesReceivedCount);
        addStat("openConnectionCount", openConnectionCount);
        addStat("busyThreads", busyThreads);
    }

    public RangeStatistic getActiveRequestCount() {
        // TODO 
        return null;
    }

    public TimeStatistic getRequestTime() {
        return requestTime;
    }

    public CountStatistic getErrorCount() {
        return errorCount;
    }

    public CountStatistic getBytesSentCount() {
        return bytesSentCount;
    }

    public CountStatistic getBytesReceivedCount() {
        return bytesReceivedCount;
    }
   
    public RangeStatistic getOpenConnectionCount() {
        return openConnectionCount;
    }
    
    // TODO - Move this to container statistics
    public RangeStatistic getSpareThreadCount() {
        return null;
    }
    
    /**
     * These setters are used by native implementation
     */
    public void setBytesReceivedCount(long bytesReceived) {
        this.bytesReceivedCount.setCount(bytesReceived);
    }

    public void setBytesSentCount(long bytesSent) {
        this.bytesSentCount.setCount(bytesSent);
    }

    public void setActiveRequestCount(int activeRequestCount) {
        this.activeRequestCount.setCount(activeRequestCount);
    }

    public void setErrorCount(int errorCount) {
        this.errorCount.setCount(errorCount);
    }

    public void setRequestTime(int count, long minTime, long maxTime, long totalTime) {
        this.requestTime.setCount(count);
        this.requestTime.setMinTime(minTime);
        this.requestTime.setMaxTime(maxTime);
        this.requestTime.setTotalTime(totalTime);
    }
 
    public void setOpenConnection(long current, long highMark, long lowMark) {
        openConnectionCount.setCurrent(current);
        openConnectionCount.setHighWaterMark(highMark);
        openConnectionCount.setLowWaterMark(lowMark);
    }
    
    public void setBusyThreads(long current, long highWaterMark, long lowWaterMark,
            long upperBound, long lowerBound) {
        busyThreads.setCurrent(current);
        busyThreads.setHighWaterMark(highWaterMark);
        busyThreads.setLowWaterMark(lowWaterMark); //0?
        busyThreads.setLowerBound(lowerBound); //0?
        busyThreads.setUpperBound(upperBound);  // always maxThreads
    }
    
    /**
     * Used to access the native implementation in order to call setters
     * TODO implement these if needed by console
     */
    public RangeStatisticImpl getActiveRequestCountImpl() {
        return null;
    }

    public TimeStatisticImpl getRequestDurationImpl() {
        return null;
    }

    public CountStatisticImpl getTotalErrorCountImpl() {
        return null;
    }

    public CountStatistic getTotalRequestCountImpl() {
        return null;
    }

}
