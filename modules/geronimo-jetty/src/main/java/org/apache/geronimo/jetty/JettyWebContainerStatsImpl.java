package org.apache.geronimo.jetty;

import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.RangeStatistic;
import javax.management.j2ee.statistics.TimeStatistic;

import org.apache.geronimo.management.stats.CountStatisticImpl;
import org.apache.geronimo.management.stats.RangeStatisticImpl;
import org.apache.geronimo.management.stats.StatisticImpl;
import org.apache.geronimo.management.stats.StatsImpl;
import org.apache.geronimo.management.stats.TimeStatisticImpl;

/**
 * Jetty implementation of the Geronimo stats interface WebContainerStats
 *
 * @version $Revision: 1.0$
 */
public class JettyWebContainerStatsImpl extends StatsImpl implements JettyWebContainerStats {
    private CountStatisticImpl totalConnectionCount;
    private RangeStatisticImpl openConnectionCount;
    private RangeStatisticImpl connectionRequestCount;
    private TimeStatisticImpl connectionDuration;
    private CountStatisticImpl totalErrorCount;
    private CountStatisticImpl totalRequestCount;
    private RangeStatisticImpl activeRequestCount;
    private TimeStatisticImpl requestDuration;
    private boolean statsOn=false;

    public JettyWebContainerStatsImpl() {
        totalConnectionCount = new CountStatisticImpl("Total Connections", StatisticImpl.UNIT_COUNT,
                "The total number of connections since last reset");
        openConnectionCount = new RangeStatisticImpl("Open Connections", StatisticImpl.UNIT_COUNT,
                "The number of connections open at present");
        connectionRequestCount = new RangeStatisticImpl("Connection Request Count", StatisticImpl.UNIT_COUNT,
                "The number of requests handled by a particular connection");
        connectionDuration = new TimeStatisticImpl("Connection Duration", StatisticImpl.UNIT_TIME_MILLISECOND,
                "The legnth of time that individual connections have been open");
        totalErrorCount = new CountStatisticImpl("Error Count", StatisticImpl.UNIT_COUNT,
                "The number of reponses that were errors since statistics gathering started");
        totalRequestCount = new CountStatisticImpl("Request Count", StatisticImpl.UNIT_COUNT,
                "The number of requests that were handled since statistics gathering started");
        activeRequestCount = new RangeStatisticImpl("Active Request Count", StatisticImpl.UNIT_COUNT,
                "The number of requests being processed concurrently");
        requestDuration = new TimeStatisticImpl("Request Duration", StatisticImpl.UNIT_TIME_MILLISECOND,
                "The length of time that it's taken to handle individual requests");

        addStat("TotalConnectionCount", totalConnectionCount);
        addStat("OpenConnectionCount", openConnectionCount);
        addStat("ConnectionRequestCount", connectionRequestCount);
        addStat("ConnectionDuration", connectionDuration);
        addStat("TotalErrorCount", totalErrorCount);
        addStat("TotalRequestCount", totalRequestCount);
        addStat("ActiveRequestCount", activeRequestCount);
        addStat("RequestDuration", requestDuration);
    }

    public CountStatistic getTotalConnectionCount() {
        return totalConnectionCount;
    }

    public RangeStatistic getOpenConnectionCount() {
        return openConnectionCount;
    }

    public RangeStatistic getConnectionRequestCount() {
        return connectionRequestCount;
    }

    public TimeStatistic getConnectionDuration() {
        return connectionDuration;
    }

    public CountStatistic getTotalErrorCount() {
        return totalErrorCount;
    }

    public CountStatistic getTotalRequestCount() {
        return totalRequestCount;
    }

    public RangeStatistic getActiveRequestCount() {
        return activeRequestCount;
    }

    public TimeStatistic getRequestDuration() {
        return requestDuration;
    }

    public boolean isStatsOn() {
        return statsOn;
    }

    public void setStatsOn(boolean on) {
        statsOn = on;
    }

    public CountStatisticImpl getTotalConnectionCountImpl() {
        return totalConnectionCount;
    }

    public RangeStatisticImpl getOpenConnectionCountImpl() {
        return openConnectionCount;
    }

    public RangeStatisticImpl getConnectionRequestCountImpl() {
        return connectionRequestCount;
    }

    public TimeStatisticImpl getConnectionDurationImpl() {
        return connectionDuration;
    }

    public CountStatisticImpl getTotalErrorCountImpl() {
        return totalErrorCount;
    }

    public CountStatisticImpl getTotalRequestCountImpl() {
        return totalRequestCount;
    }

    public RangeStatisticImpl getActiveRequestCountImpl() {
        return activeRequestCount;
    }

    public TimeStatisticImpl getRequestDurationImpl() {
        return requestDuration;
    }
}
