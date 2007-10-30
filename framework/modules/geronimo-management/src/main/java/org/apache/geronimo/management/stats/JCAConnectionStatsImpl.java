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

import javax.management.j2ee.statistics.JCAConnectionStats;
import javax.management.j2ee.statistics.TimeStatistic;

/**
 * Geronimo implementation of the JSR-77 JCAConnectionStats interface.
 * 
 * @version $Rev: 476049 $ $Date: 2006-11-16 20:35:17 -0800 (Thu, 16 Nov 2006) $
 */
public class JCAConnectionStatsImpl extends StatsImpl implements
        JCAConnectionStats {
    private String connectionFactory;

    private String managedConnectionFactory;

    private final TimeStatisticImpl waitTime;

    private final TimeStatisticImpl useTime;

    public JCAConnectionStatsImpl() {
        waitTime = new TimeStatisticImpl("Passive Count",
                StatisticImpl.UNIT_TIME_MILLISECOND,
                "Time spent waiting for a connection to be available");
        useTime = new TimeStatisticImpl("Passive Count",
                StatisticImpl.UNIT_TIME_MILLISECOND,
                "Time spent using a connection");
        addStat("WaitTime", waitTime);
        addStat("UseTime", useTime);
    }

    public String getConnectionFactory() {
        return connectionFactory;
    }

    public String getManagedConnectionFactory() {
        return managedConnectionFactory;
    }

    public TimeStatistic getWaitTime() {
        return waitTime;
    }

    public TimeStatistic getUseTime() {
        return useTime;
    }
}
