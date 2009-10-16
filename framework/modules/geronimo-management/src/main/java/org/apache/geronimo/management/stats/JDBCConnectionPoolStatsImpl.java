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

import javax.management.j2ee.statistics.BoundedRangeStatistic;
import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.JDBCConnectionPoolStats;
import javax.management.j2ee.statistics.RangeStatistic;

/**
 * Geronimo implementation of the JSR-77 JDBCConnectionPoolStats interface.
 * 
 * @version $Rev: 476049 $ $Date: 2006-11-16 20:35:17 -0800 (Thu, 16 Nov 2006) $
 */
public class JDBCConnectionPoolStatsImpl extends JDBCConnectionStatsImpl
        implements JDBCConnectionPoolStats {
    private final CountStatisticImpl closeCount;

    private final CountStatisticImpl createCount;

    private final BoundedRangeStatisticImpl freePoolSize;

    private final BoundedRangeStatisticImpl poolSize;

    private final RangeStatisticImpl waitingThreadCount;

    public JDBCConnectionPoolStatsImpl() {
        closeCount = new CountStatisticImpl("Close Count",
                StatisticImpl.UNIT_COUNT, "Number of connections closed");
        createCount = new CountStatisticImpl("Create Count",
                StatisticImpl.UNIT_COUNT, "Number of connections created");
        freePoolSize = new BoundedRangeStatisticImpl("Free Pool Size",
                StatisticImpl.UNIT_COUNT,
                "Number of free connections in the pool");
        poolSize = new BoundedRangeStatisticImpl("Pool Size",
                StatisticImpl.UNIT_COUNT, "Size of the connection pool");
        waitingThreadCount = new RangeStatisticImpl("Waiting Thread Count",
                StatisticImpl.UNIT_COUNT,
                "Number of threads waiting for a connection");

        addStat("CloseCount", closeCount);
        addStat("CreateCount", createCount);
        addStat("FreePoolSize", freePoolSize);
        addStat("PoolSize", poolSize);
        addStat("WaitingThreadCount", waitingThreadCount);
    }

    public CountStatistic getCloseCount() {
        return closeCount;
    }

    public CountStatistic getCreateCount() {
        return createCount;
    }

    public BoundedRangeStatistic getFreePoolSize() {
        return freePoolSize;
    }

    public BoundedRangeStatistic getPoolSize() {
        return poolSize;
    }

    public RangeStatistic getWaitingThreadCount() {
        return waitingThreadCount;
    }
}
