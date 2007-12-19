/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
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

import org.apache.geronimo.management.stats.CountStatisticImpl;
import org.apache.geronimo.management.stats.RangeStatisticImpl;
import org.apache.geronimo.management.stats.StatisticImpl;
import org.apache.geronimo.management.stats.TimeStatisticImpl;
import org.apache.geronimo.management.stats.StatsImpl;
import org.apache.geronimo.management.stats.WebConnectorStatsImpl;

import javax.management.j2ee.statistics.RangeStatistic;
import javax.management.j2ee.statistics.TimeStatistic;
import javax.management.j2ee.statistics.CountStatistic;

/**
 * Jetty Web Connector class for JSR-77 stats.
 */
public class JettyWebConnectorStatsImpl extends WebConnectorStatsImpl implements JettyWebConnectorStats {
    private CountStatisticImpl requestCount;
    private TimeStatisticImpl connectionsDuration;
    private RangeStatisticImpl connectionsRequest;
    
    public JettyWebConnectorStatsImpl() {
        requestCount = new CountStatisticImpl("Request Count", StatisticImpl.UNIT_COUNT,
                "Total number of requests made to server", 0);
        connectionsDuration = new TimeStatisticImpl("Connections Duration", StatisticImpl.UNIT_TIME_MILLISECOND,
                "Duration of a connection");
        connectionsRequest = new RangeStatisticImpl("Connections Request", StatisticImpl.UNIT_COUNT,
                "Range for connections requested during the observed period", 0);       // all 0's
        
        addStat("RequestCount", requestCount);
        addStat("ConnectionsDuration", connectionsDuration);
        addStat("ConnectionsRequest", connectionsRequest);
    }
    
    /**
     * Gets the number of request count since statistics gathering started.
     */
    public CountStatistic getRequestCount() {
        return requestCount;
    }

    /**
     * Gets the avg, min, max, and total connection duration time since 
     * statistics gathering started.
     */
    public TimeStatistic getConnectionsDuration() {
        return connectionsDuration;
    }
    
    /**
     * Gets the min, max, current number of connection requests since statistics gathering started.
     */
    public RangeStatistic getConnectionsRequest() {
        return connectionsRequest;
    }
    
    /**
     * Gets the number of request count since statistics gathering started.
     */
    public CountStatisticImpl getRequestCountImpl() {
        return requestCount;
    }

    /**
     * Gets the count, min, max, and total connection duration time since 
     * statistics gathering started. The avg is total/count
     */
    public TimeStatisticImpl getConnectionsDurationImpl() {
        return connectionsDuration;
    }
    
    /**
     * Gets the min, max, current number of connection requests since statistics gathering started.
     */
    public RangeStatisticImpl getConnectionsRequestImpl() {
        return connectionsRequest;
    }
}
