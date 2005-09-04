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
package org.apache.geronimo.console.jsr77;

import javax.servlet.http.HttpSession;
import javax.management.j2ee.statistics.JVMStats;
import javax.management.j2ee.statistics.BoundedRangeStatistic;
import uk.ltd.getahead.dwr.ExecutionContext;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.console.util.ManagementHelper;
import org.apache.geronimo.management.J2EEDomain;
import org.apache.geronimo.management.StatisticsProvider;
import org.apache.geronimo.management.geronimo.J2EEServer;
import org.apache.geronimo.management.geronimo.JVM;

/**
 * Looks up JSR-77 statistics in response to AJAX calls from portlets.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class Jsr77Lookup {
    public DynamicServerInfo getJavaVMStatistics() {
        HttpSession session = ExecutionContext.get().getSession();
        ManagementHelper helper = PortletManager.getManagementHelper(session);
        J2EEDomain[] domains = helper.getDomains();
        J2EEServer[] servers = helper.getServers(domains[0]);
        JVM[] jvms = helper.getJavaVMs(servers[0]);
        long elapsed = System.currentTimeMillis() - jvms[0].getKernelBootTime().getTime();
        if(jvms[0].isStatisticsProvider()) {
            JVMStats stats = (JVMStats) ((StatisticsProvider)jvms[0]).getStats();
            BoundedRangeStatistic heap = stats.getHeapSize();
            return new DynamicServerInfo(heap.getCurrent(), heap.getHighWaterMark(), heap.getUpperBound(), elapsed);
        } else {
            return new DynamicServerInfo(elapsed);
        }
    }
}
