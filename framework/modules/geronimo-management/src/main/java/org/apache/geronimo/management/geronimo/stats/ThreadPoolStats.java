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

import javax.management.j2ee.statistics.BoundedRangeStatistic;
import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.Stats;

/**
 * Statistics expected for a Thread Pool implementation
 *
 * @version $Rev$ $Date$
 */
public interface ThreadPoolStats extends Stats {
    /**
     * Gets the min and max pool size, as well as the most, least, and current
     * number of threads in use.
     */
    public BoundedRangeStatistic getThreadsInUse();

    /**
     * Gets a list of the known consumers of threads from this pool.  This may
     * not be all consumers, because they won't necessarily identify themselves,
     * but it's a start when tracking down what's using threads in a pool.
     */
    public String[] getThreadConsumers();

    /**
     * For each consumer listed by getThreadConsumers(), this can be used to
     * find out how many threads that consumer is consuming at the time these
     * statistics were generated.
     *
     * @param consumer The consumer you're inquiring about
     */
    public CountStatistic getCountForConsumer(String consumer);
}
