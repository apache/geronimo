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
import javax.management.j2ee.statistics.Statistic;

/**
 * Implementation of the JSR-77 Statistic interface (JSR77.6.4)
 *
 * @version $Rev$ $Date$
 */
public class StatisticImpl implements Statistic, Serializable {
    // Defined in JSR77.6.4.1.2
    public final static String UNIT_TIME_HOUR = "HOUR";
    public final static String UNIT_TIME_MINUTE = "MINUTE";
    public final static String UNIT_TIME_SECOND = "SECOND";
    public final static String UNIT_TIME_MILLISECOND = "MILLISECOND";
    public final static String UNIT_TIME_MICROSECOND = "MICROSECOND";
    public final static String UNIT_TIME_NANOSECOND = "NANOSECOND";
    // Units that are not defined in JSR-77
    public final static String UNIT_MEMORY_BYTES = "BYTE";
    public final static String UNIT_MEMORY_KILOBYTES = "KILOBYTE";
    public final static String UNIT_MEMORY_MEGABYTES = "MEGABYTE";
    public final static String UNIT_MEMORY_GIGABYTES = "GIGABYTE";
    // need a better name for this
    public final static String UNIT_COUNT = "UNITCOUNT";

    private String name;
    private String unit;
    private String description;
    private long startTime;
    private long lastSampleTime;

    public StatisticImpl(String name, String unit, String description) {
        this.name = name;
        this.unit = unit;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public String getDescription() {
        return description;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime() {
        this.startTime = System.currentTimeMillis();
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getLastSampleTime() {
        return lastSampleTime;
    }

    public void setLastSampleTime(long lastSampleTime) {
        this.lastSampleTime = lastSampleTime;
    }

    public void setLastSampleTime() {
        this.lastSampleTime = System.currentTimeMillis();
    }
}
