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

package org.apache.geronimo.tomcat.stats;

import java.util.Iterator;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.j2ee.statistics.Stats;
import org.apache.geronimo.management.geronimo.stats.TomcatWebConnectorStatsImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.tomcat.util.modeler.Registry;

/**
 * This will query MBeanServer and provide jsr77 Stats for connectors.
 *
 * @version $Revision$ $Date$
 */
public class ConnectorStats {
    private static final Logger log = LoggerFactory.getLogger(ConnectorStats.class);
    protected MBeanServer mBeanServer = null;

    protected Registry registry;

    private ObjectName grpName;

    private ObjectName tpName;

    private TomcatWebConnectorStatsImpl stats = new TomcatWebConnectorStatsImpl();

    public ConnectorStats() {
        // Retrieve the MBean server
        registry = Registry.getRegistry(null, null);
        mBeanServer = Registry.getRegistry(null, null).getMBeanServer();
        try {
            grpName = new ObjectName("*:type=GlobalRequestProcessor,*");
            tpName = new ObjectName("*:type=ThreadPool,*");
        } catch (Exception ex) {
            log.error("Error - " + ex.toString());
        }
    }

    public Stats getStats(String port) {
        stats.setStartTime();
        updateStats(stats, port);
        return stats;

    }

    public Stats updateStats(String port) {
        updateStats(stats, port);
        return stats;

    }

    private void updateStats(TomcatWebConnectorStatsImpl stats, String port) {
        Iterator<ObjectInstance> iterator;
        Set<ObjectInstance> set;
        ObjectName objectName;
        try {
            // Query Thread Pools
            set = mBeanServer.queryMBeans(tpName, null);
            iterator = set.iterator();
            while (iterator.hasNext()) {
                ObjectInstance oi = iterator.next();
                objectName = oi.getObjectName();
                if (objectName.getKeyProperty("name").indexOf(port) > -1) {
                    tpName = objectName;
                    break;
                }
            }
            // Query Global Request Processors
            set = mBeanServer.queryMBeans(grpName, null);
            iterator = set.iterator();
            while (iterator.hasNext()) {
                ObjectInstance oi = iterator.next();
                objectName = oi.getObjectName();
                if (objectName.getKeyProperty("name").indexOf(port) > -1) {
                    grpName = objectName;
                    break;
                }
            }
            stats.setLastSampleTime();
            // Any http connector !
            long maxTime = ((Long) (mBeanServer.getAttribute(grpName, "maxTime"))).longValue();
            long processingTime = ((Long) (mBeanServer.getAttribute(grpName, "processingTime"))).longValue();
            int requestCount = ((Integer) (mBeanServer.getAttribute(grpName, "requestCount"))).intValue();
            int errorCount = ((Integer) (mBeanServer.getAttribute(grpName, "errorCount"))).intValue();
            long bytesReceived = ((Long) (mBeanServer.getAttribute(grpName, "bytesReceived"))).longValue();
            long bytesSent = ((Long) (mBeanServer.getAttribute(grpName, "bytesSent"))).longValue();
            // Tomcat does not keep min Time, using 0 as Undefined value
            stats.setRequestTime(requestCount, 0, maxTime, processingTime);
            stats.setErrorCount(errorCount);
            stats.setBytesSentCount(bytesSent);
            stats.setBytesReceivedCount(bytesReceived);
            long openConnections = 0;
            // TODO find these
            //long openConnections = ((Long) (mBeanServer.getAttribute(grpName, "countOpenConnections"))).longValue();
            long maxOpenConnections = 0;
            //long maxOpenConnections = ((Long) (mBeanServer.getAttribute(grpName, "maxOpenConnections"))).longValue();
            stats.setOpenConnection(openConnections, maxOpenConnections, 0);
            // ThreadPool
            int currentThreadsBusy = ((Integer) (mBeanServer.getAttribute(tpName, "currentThreadsBusy"))).intValue();
            //stats.setActiveRequestCount(currentThreadsBusy); ??
            int currentThreadCount = ((Integer) (mBeanServer.getAttribute(tpName, "currentThreadCount"))).intValue();
            // these are available from "*:type=Connector,*"
            //int minSpareThreads = ((Integer) (mBeanServer.getAttribute(tpName, "minSpareThreads"))).intValue();
            //int maxSpareThreads = ((Integer) (mBeanServer.getAttribute(tpName, "maxSpareThreads"))).intValue();
            int maxThreads = ((Integer) (mBeanServer.getAttribute(tpName, "maxThreads"))).intValue();
            // keepAliveCount is also available
            stats.setBusyThreads(currentThreadsBusy, currentThreadCount, 0, maxThreads, 0);
            // TODO - spareThreads (current-busy, maxSpareThread, minSpareThreads)
        } catch (Exception ex) {
            log.error("Error getting attribute " + grpName + " " + ex.toString());
        }

    }
}
