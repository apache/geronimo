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

import javax.management.j2ee.statistics.RangeStatistic;

import org.apache.geronimo.management.geronimo.stats.WebConnectorStats;

/**
 * Geronimo implementation of the JSR-77 style WebConnectorStats interface. This
 * is not required by JSR-77, but provides useful statistics. This will be
 * discovered by mejb using 'stats' attribute.
 * 
 * @version $Revision$ $Date$
 */

public class WebConnectorStatsImpl extends StatsImpl implements WebConnectorStats { 
    // these come from ThreadPool
    private RangeStatisticImpl openConnectionCount; 
    
    public WebConnectorStatsImpl() {
        openConnectionCount = new RangeStatisticImpl("" + "Open Connections", StatisticImpl.UNIT_COUNT,
                "Range for connections opened during the observed period", 0); // all 0's
        addStat("OpenConnectionCount", openConnectionCount);
    }

   
    public RangeStatistic getOpenConnectionCount() {
        return openConnectionCount;
    }
 
    public void setOpenConnection(long current, long highMark, long lowMark) {
        openConnectionCount.setCurrent(current);
        openConnectionCount.setHighWaterMark(highMark);
        openConnectionCount.setLowWaterMark(lowMark);
    }
    
    /**
     * Used to access the native implementation in order to call setters
     * TODO implement these if needed by console
     */
    public RangeStatisticImpl getOpenConnectionCountImpl() {
        return openConnectionCount;
    }
}
