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

package org.apache.geronimo.system.logging.log4j;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

/**
 *
 *
 * @version $Rev$ $Date$
 */
public class NamedNDCFilter extends Filter {
    private NamedNDC namedNDC;
    private String name;
    private String value;
    private boolean acceptOnMatch = true;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
        namedNDC = NamedNDC.getNamedNDC(name);
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public boolean getAcceptOnMatch() {
        return acceptOnMatch;
    }

    public void setAcceptOnMatch(final boolean acceptOnMatch) {
        this.acceptOnMatch = acceptOnMatch;
    }

    public int decide(LoggingEvent event) {
        if (value == null) {
            return Filter.NEUTRAL;
        }

        Object ndcValue = namedNDC.get();
        if (ndcValue == null) {
            return Filter.NEUTRAL;
        }

        if (value.equals(ndcValue.toString())) {
            if (acceptOnMatch) {
                return Filter.ACCEPT;
            } else {
                return Filter.DENY;
            }
        }
        return Filter.NEUTRAL;
    }
}
