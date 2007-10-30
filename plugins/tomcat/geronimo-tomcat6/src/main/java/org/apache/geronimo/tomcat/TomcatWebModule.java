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
package org.apache.geronimo.tomcat;

import javax.management.j2ee.statistics.Stats;

import org.apache.catalina.Context;
import org.apache.geronimo.management.geronimo.WebModule;

/**
 * @version $Revision$ $Date$
 * 
 */
public interface TomcatWebModule extends WebModule {

    public Context getContext();

    public Stats getStats();

    /**
     * @return The cumulative processing times of requests by all servlets in
     *         this Context
     */
    public long getProcessingTime();

    /**
     * @return The time this context was started.
     */
    public long getStartTime();

    /**
     * @return The time (in milliseconds) it took to start this context.
     */
    public long getStartupTime();

    public long getTldScanTime();

}
