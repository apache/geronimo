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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.Stats;

/**
 * Geronimo implementation of the JSR-77 Stats interface.  Dynamically tracks
 * available statistics for its subclasses, to make it easy to iterate
 * available statistics without knowing exactly what kind of class you're
 * looking at.  Not sure when you'd want to do that, but hey.
 *
 * @version $Rev$ $Date$
 */
public class StatsImpl implements Stats, Serializable {
    private final Map <String,StatisticImpl> stats = new HashMap<String,StatisticImpl>();

    public StatsImpl() {
    }

    protected void addStat(String name, StatisticImpl value) {
        stats.put(name, value);
    }

    /**
     * Used when the available statistics are dynamic (e.g. depend on the
     * current clients of the service, etc.).
     * 
     * @param name The statistic to remove
     */
    protected void removeStat(String name) {
        stats.remove(name);
    }

    public void setStartTime() {
        long now = System.currentTimeMillis();
        for (StatisticImpl item : stats.values()) {
            item.setStartTime(now);
        }            
    }
    
    public void setLastSampleTime() {
        long now = System.currentTimeMillis();
        for (StatisticImpl item : stats.values()) {
            item.setLastSampleTime(now);
        }            
    }

    public Statistic getStatistic(String statisticName) {
        return (Statistic)stats.get(statisticName);
    }

    public String[] getStatisticNames() {
        return (String[]) stats.keySet().toArray(new String[stats.size()]);
    }

    public Statistic[] getStatistics() {
        String[] names = getStatisticNames();
        Statistic[] result = new Statistic[names.length];
        for (int i = 0; i < names.length; i++) {
            result[i] = (Statistic) stats.get(names[i]);
        }
        return result;
    }
}
