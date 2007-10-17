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
import javax.management.j2ee.statistics.JTAStats;

/**
 * Geronimo implementation of the JSR-77 JTAStats interface.
 * 
 * @version $Rev: 476049 $ $Date: 2006-11-16 20:35:17 -0800 (Thu, 16 Nov 2006) $
 */
public class JTAStatsImpl extends StatsImpl implements JTAStats {
    private final CountStatisticImpl activeCount;

    private final CountStatisticImpl committedCount;

    private final CountStatisticImpl rolledbackCount;

    public JTAStatsImpl() {
        activeCount = new CountStatisticImpl("Active Count",
                StatisticImpl.UNIT_COUNT, "Number of active transactions");
        committedCount = new CountStatisticImpl("Committed Count",
                StatisticImpl.UNIT_COUNT, "Number of committed transactions");
        rolledbackCount = new CountStatisticImpl("Rolledback Count",
                StatisticImpl.UNIT_COUNT, "Number of rolled-back transactions");
        addStat("ActiveCount", activeCount);
        addStat("CommittedCount", committedCount);
        addStat("RolledbackCount", rolledbackCount);
    }

    public CountStatistic getActiveCount() {
        return activeCount;
    }

    public CountStatistic getCommittedCount() {
        return committedCount;
    }

    public CountStatistic getRolledbackCount() {
        return rolledbackCount;
    }

    public CountStatisticImpl getActiveCountImpl() {
        return activeCount;
    }

    public CountStatisticImpl getCommittedCountImpl() {
        return committedCount;
    }

    public CountStatisticImpl getRolledbackCountImpl() {
        return rolledbackCount;
    }
}
