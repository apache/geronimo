/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.messaging.remotenode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.messaging.NodeException;
import org.apache.geronimo.messaging.NodeInfo;
import org.apache.geronimo.pool.ClockPool;

import EDU.oswego.cs.dl.util.concurrent.ClockDaemon;

/**
 * RemoteNode monitor.
 *
 * @version $Revision: 1.2 $ $Date: 2004/07/08 05:13:29 $
 */
public class RemoteNodeMonitor
{

    private static final Log log = LogFactory.getLog(RemoteNodeMonitor.class); 
    
    /**
     * Number of milliseconds between two scans.
     */
    public static final long MONITOR_PERIOD = 2000l;

    /**
     * Nodes which have been idle for more than this number of milliseconds
     * are left.
     */
    public static final long IDLE_TIME = 10000l;

    /**
     * Manager whose RemoteNodes are monitored.
     */
    private final RemoteNodeManager manager;
    
    /**
     * To perform recurrent operations.
     */
    private final ClockPool clockPool;
    
    /**
     * Nodes to be removed associated with a timestamp.
     */
    private final Map toBeRemoved;
    
    /**
     * Ticket allocated by ClockDaemon when this instance has been started.
     */
    private Object clockTicket;
    
    /**
     * Creates a monitor for the RemoteNodes managed by aManager.
     * 
     * @param aManager Manager whose RemoteNodes are to be monitored.
     * @param aClockPool To schedule recurrent tasks.
     */
    public RemoteNodeMonitor(RemoteNodeManager aManager, ClockPool aClockPool) {
        if ( null == aManager ) {
            throw new IllegalArgumentException("Manager is required");
        } else if ( null == aClockPool ) {
            throw new IllegalArgumentException("Clock Pool is required");
        }
        manager = aManager;
        clockPool = aClockPool;
        
        toBeRemoved = new HashMap();
    }
    
    /**
     * Start the monitoring.
     */
    public void start() {
        clockTicket = clockPool.getClockDaemon().executePeriodically(
            MONITOR_PERIOD, new MonitorTask(), true);
    }

    /**
     * Stops the monitoring.
     */
    public void stop() {
        ClockDaemon.cancel(clockTicket);
    }

    /**
     * Schedules the deletion of the specified nodes.
     * 
     * @param aNodes Set<NodeInfo>
     */
    public void scheduleNodeDeletion(Set aNodes) {
        synchronized(toBeRemoved) {
            for (Iterator iter = aNodes.iterator(); iter.hasNext();) {
                NodeInfo node = (NodeInfo) iter.next();
                toBeRemoved.put(node, new Long(System.currentTimeMillis()));
            }
        }
    }

    /**
     * Unschedules the deletion of the specified nodes.
     * 
     * @param aNodes Set<NodeInfo>
     */
    public void unscheduleNodeDeletion(Set aNodes) {
        synchronized(toBeRemoved) {
            for (Iterator iter = aNodes.iterator(); iter.hasNext();) {
                NodeInfo node = (NodeInfo) iter.next();
                toBeRemoved.remove(node);
            }
        }
    }

    /**
     * Executes the monitoring activities.
     */
    private void monitor() {
        synchronized(toBeRemoved) {
            for (Iterator iter = toBeRemoved.entrySet().iterator();
                iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                NodeInfo node = (NodeInfo) entry.getKey();
                Long lastActivity = (Long)entry.getValue();
                if ( lastActivity.longValue() >
                    System.currentTimeMillis() + IDLE_TIME ) {
                    try {
                        manager.leaveRemoteNode(node);
                    } catch (NodeException e) {
                        log.error("Can not leave " + node, e);
                    }
                }
                iter.remove();
            }
        }
    }
    
    private class MonitorTask implements Runnable {
        public void run() {
            monitor();
        }
    }
    
}