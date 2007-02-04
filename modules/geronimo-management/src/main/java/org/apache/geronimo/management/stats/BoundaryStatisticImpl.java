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

import javax.management.j2ee.statistics.BoundaryStatistic;

/**
 * @version $Rev$ $Date$
 */
public class BoundaryStatisticImpl extends StatisticImpl implements BoundaryStatistic {
    private long upperBound;
    private long lowerBound;

    public BoundaryStatisticImpl(String name, String unit, String description) {
        super(name, unit, description);
    }

    public BoundaryStatisticImpl(String name, String unit, String description, long lower, long upper) {
        super(name, unit, description);
        setBounds(lower, upper);
    }

    public long getUpperBound() {
        return upperBound;
    }

    public long getLowerBound() {
        return lowerBound;
    }

    public void setBounds(long value) {
        if(value < lowerBound) {
            lowerBound = value;
        }
        if(value > upperBound) {
            upperBound = value;
        }
    }

    public void setBounds(long lower, long upper) {
        upperBound = upper;
        lowerBound = lower;
    }

    public void setUpperBound(long upperBound) {
        this.upperBound = upperBound;
    }

    public void setLowerBound(long lowerBound) {
        this.lowerBound = lowerBound;
    }
    
    public String toString() {
        return(getName() + " in " + getUnit() + " -- lowerBound: " + lowerBound + ", upperBound: " + upperBound);
    }
}
