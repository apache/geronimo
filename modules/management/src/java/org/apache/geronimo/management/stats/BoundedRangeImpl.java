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

import javax.management.j2ee.statistics.BoundedRangeStatistic;

/**
 * @version $Rev$ $Date$
 */
public class BoundedRangeImpl extends RangeStatisticImpl implements BoundedRangeStatistic {
    private long upperBound;
    private long lowerBound;

    public BoundedRangeImpl(String name, String unit, String description) {
        super(name, unit, description);
    }

    public BoundedRangeImpl(String name, String unit, String description, long currentValue, long lowerBound, long upperBound) {
        super(name, unit, description, currentValue);
        this.upperBound = upperBound;
        this.lowerBound = lowerBound;
    }

    public long getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(long upperBound) {
        this.upperBound = upperBound;
    }

    public long getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(long lowerBound) {
        this.lowerBound = lowerBound;
    }

    public void setBounds(long lower, long upper) {
        upperBound = upper;
        lowerBound = lower;
    }

    public void setCurrent(long current) {
        super.setCurrent(current);
        if(current < lowerBound) {
            lowerBound = current;
        }
        if(current > upperBound) {
            upperBound = current;
        }
    }
}
