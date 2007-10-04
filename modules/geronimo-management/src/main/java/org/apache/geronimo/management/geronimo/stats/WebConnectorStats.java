/**
 *
 * Copyright 2006 The Apache Software Foundation
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

package org.apache.geronimo.management.geronimo.stats;

import javax.management.j2ee.statistics.RangeStatistic;
import javax.management.j2ee.statistics.Stats;

/**
 * Statistics exposed by a Tomcat web connector (http, https)
 * 
 * @version $Rev$ $Date$
 */
public interface WebConnectorStats extends Stats {

    // TODO - check if some other stats can be merged
    /**
     * Gets the number of connections currently open (as well as the min and max
     * since statistics gathering started).
     */
    RangeStatistic getOpenConnectionCount();
}
