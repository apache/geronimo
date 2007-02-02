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
import javax.management.j2ee.statistics.JMSEndpointStats;
import javax.management.j2ee.statistics.TimeStatistic;

/**
 * Geronimo implementation of the JSR-77 JMSEndpointStats interface.
 * 
 * @version $Rev: 476049 $ $Date: 2006-11-16 20:35:17 -0800 (Thu, 16 Nov 2006) $
 */
public class JMSEndpointStatsImpl extends StatsImpl implements JMSEndpointStats {
    private final CountStatisticImpl messageCount;

    private final CountStatisticImpl pendingMessageCount;

    private final CountStatisticImpl expiredMessageCount;

    private final TimeStatisticImpl messageWaitTime;

    public JMSEndpointStatsImpl() {
        messageCount = new CountStatisticImpl("Message Count",
                StatisticImpl.UNIT_COUNT, "Number of messages sent or received");
        pendingMessageCount = new CountStatisticImpl("Pending Message Count",
                StatisticImpl.UNIT_COUNT, "Number of pending messages");
        expiredMessageCount = new CountStatisticImpl("Expired Message Count",
                StatisticImpl.UNIT_COUNT,
                "Number of messages that expired before delivery");
        messageWaitTime = new TimeStatisticImpl("Message Wait Time",
                StatisticImpl.UNIT_COUNT,
                "Time spent by a message before being delivered");

        addStat("MessageCount", messageCount);
        addStat("PendingMessageCount", pendingMessageCount);
        addStat("ExpiredMessageCount", expiredMessageCount);
        addStat("MessageWaitTime", messageWaitTime);
    }

    public CountStatistic getMessageCount() {
        return messageCount;
    }

    public CountStatistic getPendingMessageCount() {
        return pendingMessageCount;
    }

    public CountStatistic getExpiredMessageCount() {
        return messageCount;
    }

    public TimeStatistic getMessageWaitTime() {
        return messageWaitTime;
    }
}
