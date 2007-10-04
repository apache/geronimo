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
 * looking at.
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

    /**
     * Set the startTime for all statistics to System.currentTimeMillis()
     */
    public void setStartTime() {
        long now = System.currentTimeMillis();
        for (StatisticImpl item : stats.values()) {
            item.setStartTime(now);
        }            
    }
    
    /**
     * Set the startTime for all statistics to the given value
     * @param time
     */
    public void setStartTime(long time) {
        for (StatisticImpl item : stats.values()) {
            item.setStartTime(time);
        }            
    }
    
    /**
     * Set the lastSampleTime for all statistics to System.currentTimeMillis()
     */
    public void setLastSampleTime() {
        long now = System.currentTimeMillis();
        for (StatisticImpl item : stats.values()) {
            item.setLastSampleTime(now);
        }            
    }

    /* 
     * Gets a Statistic by name
     * @see javax.management.j2ee.statistics.Stats#getStatistic(java.lang.String)
     */
    public Statistic getStatistic(String statisticName) {
        return (Statistic)stats.get(statisticName);
    }

    /*
     * Returns a list of names of statistics
     * @see javax.management.j2ee.statistics.Stats#getStatisticNames()
     */
    public String[] getStatisticNames() {
        return (String[]) stats.keySet().toArray(new String[stats.size()]);
    }

    /* 
     * Returns a list of all the Statistic objects supported by this Stats object
     * @see javax.management.j2ee.statistics.Stats#getStatistics()
     */
    public Statistic[] getStatistics() {
        String[] names = getStatisticNames();
        Statistic[] result = new Statistic[names.length];
        for (int i = 0; i < names.length; i++) {
            result[i] = (Statistic) stats.get(names[i]);
        }
        return result;
    }
}
