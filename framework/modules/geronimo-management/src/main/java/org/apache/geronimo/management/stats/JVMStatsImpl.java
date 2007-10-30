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
import javax.management.j2ee.statistics.JVMStats;

/**
 * Geronimo implementation of the JSR-77 JVMStats interface.
 *
 * @version $Rev$ $Date$
 */
public class JVMStatsImpl extends StatsImpl implements JVMStats {
    private final CountStatisticImpl upTime;
    private final BoundedRangeStatisticImpl heapSize;

    public JVMStatsImpl() {
        upTime = new CountStatisticImpl("JVM Up Time", StatisticImpl.UNIT_TIME_MILLISECOND,
                "The length of time that the JVM has been running", 0);
        heapSize = new BoundedRangeStatisticImpl("JVM Heap Size", StatisticImpl.UNIT_MEMORY_BYTES,
                "The memory usage of the JVM");
        addStat("UpTime", upTime);
        addStat("HeapSize", heapSize);
    }

    public CountStatistic getUpTime() {
        return upTime;
    }

    public BoundedRangeStatistic getHeapSize() {
        return heapSize;
    }

    /**
     * Used to access the native implementation in order to call setters
     */
    public CountStatisticImpl getUpTimeImpl() {
        return upTime;
    }

    /**
     * Used to access the native implementation in order to call setters
     */
    public BoundedRangeStatisticImpl getHeapSizeImpl() {
        return heapSize;
    }
}
