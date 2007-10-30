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

import javax.management.j2ee.statistics.RangeStatistic;

/**
 * @version $Rev$ $Date$
 */
public class RangeStatisticImpl extends StatisticImpl implements RangeStatistic {
    private long highWaterMark;
    private long lowWaterMark;
    private long current;

    public RangeStatisticImpl(String name, String unit, String description) {
        super(name, unit, description);
    }

    public RangeStatisticImpl(String name, String unit, String description, long currentValue) {
        super(name, unit, description);
        highWaterMark = lowWaterMark = current = currentValue;
    }

    public long getHighWaterMark() {
        return highWaterMark;
    }

    public long getLowWaterMark() {
        return lowWaterMark;
    }

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
        if(current < lowWaterMark) {
            lowWaterMark = current;
        }
        if(current > highWaterMark) {
            highWaterMark = current;
        }
    }

    public void setHighWaterMark(long highWaterMark) {
        this.highWaterMark = highWaterMark;
    }

    public void setLowWaterMark(long lowWaterMark) {
        this.lowWaterMark = lowWaterMark;
    }
    
    public String toString() {
        return(getName() + " in " + getUnit() + " -- current: " + current + ", highWaterMark: " + highWaterMark + 
                ", lowWaterMark: " + lowWaterMark);
    }
}
