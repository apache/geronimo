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
import javax.management.j2ee.statistics.EJBStats;

/**
 * Geronimo implementation of the JSR-77 EJBStats interface.
 * 
 * @version $Rev: 476049 $ $Date: 2006-11-16 20:35:17 -0800 (Thu, 16 Nov 2006) $
 */
public class EJBStatsImpl extends StatsImpl implements EJBStats {
    private final CountStatisticImpl createCount;

    private final CountStatisticImpl removeCount;

    public EJBStatsImpl() {
        createCount = new CountStatisticImpl("Bean Create Count",
                StatisticImpl.UNIT_COUNT,
                "Number of times the beans create method was called", 0);
        removeCount = new CountStatisticImpl("Bean Remove Count",
                StatisticImpl.UNIT_COUNT,
                "Number of times the beans remove method was called", 0);
        addStat("CreateCount", createCount);
        addStat("RemoveCount", removeCount);
    }

    public CountStatistic getCreateCount() {
        return createCount;
    }

    public CountStatistic getRemoveCount() {
        return removeCount;
    }
}
