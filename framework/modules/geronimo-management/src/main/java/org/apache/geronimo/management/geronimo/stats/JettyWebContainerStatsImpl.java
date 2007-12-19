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
    private RangeStatisticImpl activeRequestCount;
    private TimeStatisticImpl requestDuration;
    private CountStatisticImpl response1xx;
    private CountStatisticImpl response2xx;
    private CountStatisticImpl response3xx;
    private CountStatisticImpl response4xx;
    private CountStatisticImpl response5xx;
    private CountStatisticImpl statsOnMs;               // time elapsed since the stats collection

    public JettyWebContainerStatsImpl() {
        activeRequestCount = new RangeStatisticImpl("Active Request Count", StatisticImpl.UNIT_COUNT,
                "The number of requests being processed concurrently");
        requestDuration = new TimeStatisticImpl("Request Duration", StatisticImpl.UNIT_TIME_MILLISECOND,
                "The length of time that it's taken to handle individual requests");
        response1xx = new CountStatisticImpl("Response 1xx", StatisticImpl.UNIT_COUNT,
                "The number of 1xx responses");
        response2xx = new CountStatisticImpl("Response 2xx", StatisticImpl.UNIT_COUNT,
                "The number of 2xx responses");
        response3xx = new CountStatisticImpl("Response 3xx", StatisticImpl.UNIT_COUNT,
                "The number of 3xx responses");
        response4xx = new CountStatisticImpl("Response 4xx", StatisticImpl.UNIT_COUNT,
                "The number of 4xx responses");
        response5xx = new CountStatisticImpl("Response 5xx", StatisticImpl.UNIT_COUNT,
                "The number of 5xx responses");
        statsOnMs = new CountStatisticImpl("Stats Duration", StatisticImpl.UNIT_TIME_MILLISECOND,
                "The length of time that statistics have been collected.");

        addStat("ActiveRequestCount", activeRequestCount);
        addStat("RequestDuration", requestDuration);
        addStat("Responses1xx", response1xx);
        addStat("Responses2xx", response2xx);
        addStat("Responses3xx", response3xx);
        addStat("Responses4xx", response4xx);
        addStat("Responses5xx", response5xx);
        addStat("StatsDuration", statsOnMs); // TODO - remove this
    }

/**
 * Public methods to return the interfaces for statistics.
 * These are used by the objects (such as the web console) that
 * retrieve the stats for presentation purposes.
 */

    public RangeStatistic getActiveRequestCount() {
        return activeRequestCount;
    }

    public TimeStatistic getRequestDuration() {
        return requestDuration;
    }

    /**
     * @return Gets the number of 1xx status returned by this
     * context since last call of stats reset.
     */
    public CountStatistic getResponses1xx() {
        return response1xx;
    }

    /**
     * @return Gets the number of 2xx status returned by this
     * context since last call of stats reset.
     */
    public CountStatistic getResponses2xx() {
        return response2xx;
    }
    
    /**
     * @return Gets the number of 3xx status returned by this
     * context since last call of stats reset.
     */
    public CountStatistic getResponses3xx() {
        return response3xx;
    }
    
    /**
     * @return Gets the number of 4xx status returned by this
     * context since last call of stats reset.
     */
    public CountStatistic getResponses4xx() {
        return response4xx;
    }
    
    /**
     * @return Gets the number of 5xx status returned by this
     * context since last call of stats reset.
     */
    public CountStatistic getResponses5xx() {
        return response5xx;
    }
    
    /**
     * @return Time in millis since statistics collection was started.
     */
    public CountStatistic getStatsOnMs() {
        return statsOnMs;
    }

/**
 * Public methods to return the implementations for statistics.
 * These are used by the JettyContainerImpl to set the values.
 */

    public RangeStatisticImpl getActiveRequestCountImpl() {
        return activeRequestCount;
    }

    public TimeStatisticImpl getRequestDurationImpl() {
        return requestDuration;
    }

    /**
     * @return Gets the number of 1xx status returned by this
     * context since last call of stats reset.
     */
    public CountStatisticImpl getResponses1xxImpl() {
        return response1xx;
    }

    /**
     * @return Gets the number of 2xx status returned by this
     * context since last call of stats reset.
     */
    public CountStatisticImpl getResponses2xxImpl() {
        return response2xx;
    }
    
    /**
     * @return Gets the number of 3xx status returned by this
     * context since last call of stats reset.
     */
    public CountStatisticImpl getResponses3xxImpl() {
        return response3xx;
    }
    
    /**
     * @return Gets the number of 4xx status returned by this
     * context since last call of stats reset.
     */
    public CountStatisticImpl getResponses4xxImpl() {
        return response4xx;
    }
    
    /**
     * @return Gets the number of 5xx status returned by this
     * context since last call of stats reset.
     */
    public CountStatisticImpl getResponses5xxImpl() {
        return response5xx;
    }
    
    /**
     * @return Time in millis since statistics collection was started.
     */
    public CountStatisticImpl getStatsOnMsImpl() {
        return statsOnMs;
    }
}
