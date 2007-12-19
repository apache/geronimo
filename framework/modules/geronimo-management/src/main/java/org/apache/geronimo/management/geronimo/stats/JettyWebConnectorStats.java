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

package org.apache.geronimo.management.geronimo.stats;

import org.apache.geronimo.management.geronimo.stats.WebConnectorStats;

import javax.management.j2ee.statistics.RangeStatistic;
import javax.management.j2ee.statistics.TimeStatistic;
import javax.management.j2ee.statistics.CountStatistic;

public interface JettyWebConnectorStats extends WebConnectorStats {
    
    /**
     * Gets the number of request count since statistics gathering started.
     */
    CountStatistic getRequestCount();

    /**
     * Gets the count, min, max, and total connection duration time since 
     * statistics gathering started.
     */
    TimeStatistic getConnectionsDuration();
    
    /**
     * Gets the min, max, current number of connection requests since statistics gathering started.
     */
    RangeStatistic getConnectionsRequest();
}
