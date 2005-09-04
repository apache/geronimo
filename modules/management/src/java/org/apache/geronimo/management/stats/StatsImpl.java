/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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
import java.util.Map;
import java.util.HashMap;
import javax.management.j2ee.statistics.Stats;
import javax.management.j2ee.statistics.Statistic;

/**
 * Geronimo implementation of the JSR-77 Stats interface.  Dynamically tracks
 * available statistics for its subclasses, to make it easy to iterate
 * available statistics without knowing exactly what kind of class you're
 * looking at.  Not sure when you'd want to do that, but hey.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class StatsImpl implements Stats, Serializable {
    private final Map stats = new HashMap();

    public StatsImpl() {
    }

    protected void addStat(String name, Statistic value) {
        stats.put(name, value);
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
