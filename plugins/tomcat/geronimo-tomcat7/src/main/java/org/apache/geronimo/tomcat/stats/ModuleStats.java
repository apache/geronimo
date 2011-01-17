/**
 *
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

package org.apache.geronimo.tomcat.stats;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.j2ee.statistics.Stats;

import org.apache.catalina.core.StandardContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.geronimo.management.stats.WebModuleStatsImpl;

/**
 * Query MBeanServer and provide jsr77 Stats for module, i.e. a webapp
 *
 * @version $Revision$ $Date$
 */

public class ModuleStats {

    private static final Logger log = LoggerFactory.getLogger(ModuleStats.class);

    private MBeanServer mBeanServer = null;

    private ObjectName mgrName;

    private WebModuleStatsImpl stats = new WebModuleStatsImpl();

    public ModuleStats(StandardContext context) {
        assert context != null;
        // Retrieve the MBean server
        mBeanServer = Registry.getRegistry(null, null).getMBeanServer();

        try {
            // org.apache.commons.modeler.BaseModelMBean@Geronimo:type=Manager,path=/,host=localhost
            mgrName = new ObjectName("*:type=Manager,*");
        } catch (Exception ex) {
            log.error("Error - " + ex.toString());
        }
        // Query Session Managers
        for (ObjectInstance oi : mBeanServer.queryMBeans(mgrName, null)) {
            ObjectName objectName = oi.getObjectName();
            if (objectName.getKeyProperty("context").indexOf(context.getPath()) > -1) {
                mgrName = objectName;
                break;

            }
        }

//      initialize static values
        stats.setProcessingTime(context.getProcessingTime());
        stats.setStartupTime(context.getStartupTime());
        stats.setTldScanTime(context.getTldScanTime());
    }

    public Stats getStats() {
        // Initialize startTime for all statistics
        stats.setStartTime();
        // get transient statistics
        updateStats(stats);
        return stats;
    }

    public Stats updateStats() {
        // get transient statistics
        updateStats(stats);
        return stats;
    }

    /*
     * return updated value of all trainsient statistics
     *
     */
    private void updateStats(WebModuleStatsImpl stats) {
        stats.setLastSampleTime();

        // transient data
        try {
            int maxActive = ((Integer) (mBeanServer.getAttribute(mgrName,
                    "maxActive"))).intValue();
            int sessionMaxAliveTime = ((Integer) (mBeanServer.getAttribute(
                    mgrName, "sessionMaxAliveTime"))).intValue();
            int sessionAverageAliveTime = ((Integer) (mBeanServer.getAttribute(
                    mgrName, "sessionAverageAliveTime"))).intValue();
            int activeSessions = ((Integer) (mBeanServer.getAttribute(mgrName,
                    "activeSessions"))).intValue();
            int rejectedSessions = ((Integer) (mBeanServer.getAttribute(
                    mgrName, "rejectedSessions"))).intValue();
            long expiredSessions = ((Long) (mBeanServer.getAttribute(mgrName,
                    "expiredSessions"))).longValue();
            long sessionCounter = ((Long) (mBeanServer.getAttribute(mgrName,
                    "sessionCounter"))).longValue();

            stats.setSessionAliveTime(maxActive, -1, sessionMaxAliveTime,
                    sessionAverageAliveTime * maxActive);
            stats.setRejectedSessionCount(rejectedSessions);
            stats.setExpiredSessionCount((int)expiredSessions);
            stats.setActiveSessionCount(activeSessions);
            stats.setSessionCount((int)sessionCounter);

        } catch (Exception ex) {
            log.error("Error getting attribute " + mgrName + " " + ex.toString());
        }

    }

}
