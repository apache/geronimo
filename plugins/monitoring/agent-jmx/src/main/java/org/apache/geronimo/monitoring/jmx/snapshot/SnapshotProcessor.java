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
package org.apache.geronimo.monitoring.jmx.snapshot;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.apache.geronimo.monitoring.MBeanHelper;
import org.apache.geronimo.monitoring.snapshot.SnapshotConfigXMLBuilder;
import org.apache.geronimo.monitoring.snapshot.SnapshotDBHelper;
import org.apache.geronimo.monitoring.jmx.MasterRemoteControlJMX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread that is in charge of executing every x milliseconds. Upon each
 * iteration, a snapshot of the server's information is recorded.
 */
public class SnapshotProcessor {
    
    private static final Logger log = LoggerFactory.getLogger(SnapshotProcessor.class);
    
    /**
     * Collects JSR-77 statistics for all mbeans that have been chosen to 
     * be monitored and stores it in a DB. Will also, archive snapshots
     * if they have passed their retention period.
     * @param username
     * @param password
     */
    public static void takeSnapshot(/*String username, String password*/) {
        // ensure that there is a 'monitoring' directory
        SnapshotConfigXMLBuilder.ensureMonitorDir();
        // get any saved mbean names from snapshot-config.xml
        ArrayList<String> mbeanNames = SnapshotConfigXMLBuilder.getMBeanNames();
        // get a handle on the mrc
        MasterRemoteControlJMX mrc = getMRC(/*username, password*/);
        // in the case where nothing is present, grab a set of default mbeans
        if(mbeanNames.size() <= 0) {
            mbeanNames = getDefaultMBeanList(mrc);
        }
        // turn on all stats in the list
        setStatsOn(mbeanNames, mrc);
        try {
            // take a snapshot
            log.info("======SNAPSHOT======");
            // instantiate map <mbean name, stats for mbean>
            HashMap<String, HashMap<String, Long>> aggregateStats = new HashMap<String, HashMap<String, Long>>();
            // for each mbean name in the list, get its stats
            for(int i = 0; i < mbeanNames.size(); i++) {
                String mbeanName = mbeanNames.get(i);
                HashMap<String, Long> stats = (HashMap<String, Long>)mrc.getStats(mbeanName);
                aggregateStats.put(mbeanName, stats);
            }
            
            // store the data in a DB
            (new SnapshotDBHelper()).addSnapshotToDB(aggregateStats);
            
            for(Iterator itt = aggregateStats.keySet().iterator(); itt.hasNext(); ) {
                String mbean = (String)itt.next();
                HashMap<String, Long> stats = aggregateStats.get(mbean);
                log.info(mbean);
                for(Iterator it = stats.keySet().iterator(); it.hasNext(); ) {
                    String key = (String)it.next();
                    Long value = (Long)stats.get(key);
                    log.info(key + ": " + value);
                }
            }
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }
    }
    
    /**
     * Turns all statistics on for each mbean in the list.
     * 
     * @param mbeanList
     */
    private static void setStatsOn(ArrayList<String> mbeanList, MasterRemoteControlJMX mrc) {
        // for each mbean name in the list
        for(int i = 0; i < mbeanList.size(); i++) {
            // turn the statistics collection on
            String methodName = "setStatsOn";
            Object[] params = new Object[] { Boolean.TRUE };
            String[] signatures = new String[] { "boolean" };
            try {
                ObjectName objName = new ObjectName(mbeanList.get(i));
                mrc.invoke(objName, methodName, params, signatures);
                log.info("Stats for " + mbeanList.get(i) + " was turned on.");
            } catch (UndeclaredThrowableException e) {
                // HACK : this will happen for components that always collect statistics
                // and do not have StatsOn method.
            } catch(ReflectionException e) {
                // HACK : this will happen for components that do not have setStatsOn()
            } catch(Exception e) { 
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * @return A list of all default mbeans; namely, all connector or container mbean names
     * Prereq: in order to be a connector or container mbean the name must contain "Connector"/"Container" 
     * and "Tomcat"/"Jetty" or JVM.
     */
    private static ArrayList<String> getDefaultMBeanList(MasterRemoteControlJMX mrc) {
        Set<String> mbeans = MBeanHelper.getStatsProvidersMBeans( mrc.getAllMBeanNames() );
        ArrayList<String> retval = new ArrayList<String>();
        for(Iterator it = mbeans.iterator(); it.hasNext(); ) {
            String name = (String)it.next();
            if(((name.contains("Connector") || name.contains("Container")) && (name.contains("Jetty") || name.contains("Tomcat")))
                                || name.contains("JVM")) {
                // this is a connector or JVM, so add to the list
                retval.add(name);
                // update the snapshot-config.xml to include these
                SnapshotConfigXMLBuilder.addMBeanName(name);
            }
        }
        return retval;
    }
    
    /**
     * @return An instance of a MRC. 
     */
    public static MasterRemoteControlJMX getMRC(/*String username, String password*/) {
        return new MasterRemoteControlJMX();
    }
}
