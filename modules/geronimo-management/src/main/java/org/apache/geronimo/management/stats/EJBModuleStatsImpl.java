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

/**
 * EJB module statistics implementation
 */
public class EJBModuleStatsImpl extends StatsImpl implements EJBModuleStats {
    private CountStatisticImpl entityBeanCount;
    private CountStatisticImpl statelessSessionBeanCount;
    private CountStatisticImpl statefulSessionBeanCount;
    private CountStatisticImpl messageDrivenBeanCount;
    private CountStatisticImpl totalBeanCount;

    /**
     * Default constructor which constructs the different EJB type statistic
     */
    public EJBModuleStatsImpl() {
        entityBeanCount = new CountStatisticImpl("Entity Bean Count",
                StatisticImpl.UNIT_COUNT,
                "The number of entity beans defined in this EJB Module");
        statelessSessionBeanCount = new CountStatisticImpl(
                "Stateless Session Bean Count", StatisticImpl.UNIT_COUNT,
                "The number of stateless session beans defined in this EJB Module");
        statefulSessionBeanCount = new CountStatisticImpl(
                "Stateful Session Bean Count", StatisticImpl.UNIT_COUNT,
                "The number of stateful session beans defined in this EJB Module");
        messageDrivenBeanCount = new CountStatisticImpl(
                "Message Driven Bean Count", StatisticImpl.UNIT_COUNT,
                "The number of message driven beans defined in this EJB Module");
        totalBeanCount = new CountStatisticImpl("Total Bean Count",
                StatisticImpl.UNIT_COUNT,
                "The total number of beans defined in this EJB Module");

        addStat("EntityBeanCount", entityBeanCount);
        addStat("StatelessSessionBeanCount", statelessSessionBeanCount);
        addStat("StatefulSessionBeanCount", statefulSessionBeanCount);
        addStat("MessageDrivenBeanCount", messageDrivenBeanCount);
        addStat("TotalBeanCount", totalBeanCount);
    }

    /**
     * Get the entity bean count
     */
    public CountStatistic getEntityBeanCount() {
        return entityBeanCount;
    }

    /**
     * Get the stateless session bean count
     */
    public CountStatistic getStatelessSessionBeanCount() {
        return statelessSessionBeanCount;
    }

    /**
     * Get the statefull session bean count
     */
    public CountStatistic getStatefulSessionBeanCount() {
        return statefulSessionBeanCount;
    }

    /**
     * Get the message driven bean count
     */
    public CountStatistic getMessageDrivenBeanCount() {
        return messageDrivenBeanCount;
    }

    /**
     * Get the total bean count
     */
    public CountStatistic getTotalBeanCount() {
        return totalBeanCount;
    }

    /**
     * Get the entity bean count
     */
    public CountStatisticImpl getEntityBeanCountImpl() {
        return entityBeanCount;
    }

    /**
     * Get the stateless session bean count
     */
    public CountStatisticImpl getStatelessSessionBeanCountImpl() {
        return statelessSessionBeanCount;
    }

    /**
     * Get the statefull session bean count
     */
    public CountStatisticImpl getStatefulSessionBeanCountImpl() {
        return statefulSessionBeanCount;
    }

    /**
     * Get the message driven bean count
     */
    public CountStatisticImpl getMessageDrivenBeanCountImpl() {
        return messageDrivenBeanCount;
    }

    /**
     * Get the total bean count
     */
    public CountStatisticImpl getTotalBeanCountImpl() {
        return totalBeanCount;
    }
}
