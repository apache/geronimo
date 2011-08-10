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
package org.apache.geronimo.monitoring.jmx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.j2ee.statistics.RangeStatistic;
import javax.management.j2ee.statistics.Stats;
import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.TimeStatistic;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;

import org.apache.geronimo.monitoring.MBeanHelper;
import org.apache.geronimo.monitoring.MonitorConstants;

import org.apache.geronimo.monitoring.jmx.snapshot.SnapshotThread;
import org.apache.geronimo.monitoring.snapshot.SnapshotConfigXMLBuilder;
import org.apache.geronimo.monitoring.snapshot.SnapshotDBHelper;

/**
 * This is the GBean that will be the bottleneck for the communication
 * between the management node and the data in the server node.
 */
public class MasterRemoteControlJMX implements GBeanLifecycle {
    private static final Logger log = LoggerFactory.getLogger(MasterRemoteControlJMX.class);

    // mbean server to talk to other components
    private static MBeanServer mbServer = null;
    
    // threads
    private static SnapshotThread snapshotThread = null;
    
    // datasources
    private DataSource activeDS;
    private DataSource archiveDS;

    private SnapshotDBHelper snapshotDBHelper;

    public MasterRemoteControlJMX() {
        // retrieve the mbean server
        ArrayList mbServerList = MBeanServerFactory.findMBeanServer(null);
        if(mbServerList.size() >= 1) {
            mbServer = (MBeanServer) mbServerList.get(0);
            for(int i = 0; i < mbServerList.size(); i++) {
                String domain = ((MBeanServer)mbServerList.get(i)).getDefaultDomain();
                if(domain.equals( MonitorConstants.GERONIMO_DEFAULT_DOMAIN )) {
                    mbServer = (MBeanServer)mbServerList.get(i);
                    break;
                }
            }
        }
        // ensure that the mbServer has something in it
        if(mbServer == null) {
            mbServer = MBeanServerFactory.createMBeanServer( MonitorConstants.GERONIMO_DEFAULT_DOMAIN );
        }
       
        // set up SnaphotDBHelper with the necessary data sources
        // Note: do not put this in the constructor...datasources are not injected by then
        try {
            InitialContext ic = new InitialContext();
            activeDS = (DataSource)ic.lookup("jca:/org.apache.geronimo.plugins.monitoring/agent-ds/JCAConnectionManager/jdbc/ActiveDS");
            archiveDS = (DataSource)ic.lookup("jca:/org.apache.geronimo.plugins.monitoring/agent-ds/JCAConnectionManager/jdbc/ArchiveDS");
        } catch(Exception e) {
            log.error(e.getMessage());
        }
        snapshotDBHelper = new SnapshotDBHelper(activeDS, archiveDS);
    }
    
    /**
     * Looks up the JSR-77 statistics associated with this object name.
     * 
     * @param objectName
     * @return HashMap
     * @throws Exception
     */
    public static HashMap getStats(String objectName) throws Exception {
        HashMap statsMap = new HashMap();
        Stats stats = (Stats)mbServer.getAttribute(new ObjectName(objectName), "stats");
        String[] sttsName = stats.getStatisticNames();
        Statistic[] stts = stats.getStatistics();
        for(int i = 0; i < sttsName.length; i++) {
            Statistic aStat = stats.getStatistic(sttsName[i]);
            if(aStat instanceof RangeStatistic) {
                Long current = new Long(((RangeStatistic)aStat).getCurrent());
                Long high = new Long(((RangeStatistic)aStat).getHighWaterMark());
                Long low = new Long(((RangeStatistic)aStat).getLowWaterMark());
                statsMap.put(stts[i].getName() + " Current", current);
                statsMap.put(stts[i].getName() + " Max", high);
                statsMap.put(stts[i].getName() + " Min", low);
            } else if(aStat instanceof CountStatistic) {
                Long current = new Long(((CountStatistic)aStat).getCount());
                statsMap.put(stts[i].getName(), current);
            } else if(aStat instanceof TimeStatistic) {
                Long current = new Long(((TimeStatistic)aStat).getCount());
                Long max = new Long(((TimeStatistic)aStat).getMaxTime());
                Long min = new Long(((TimeStatistic)aStat).getMinTime());
                Long total = new Long(((TimeStatistic)aStat).getTotalTime());
                statsMap.put(stts[i].getName() + " CurrentTime", current);
                statsMap.put(stts[i].getName() + " MaxTime", max);
                statsMap.put(stts[i].getName() + " MinTime", min);
                statsMap.put(stts[i].getName() + " TotalTime", total);
            } else {
                // this should never happen
                throw new Exception();
            }
        }
        return statsMap;
    }
    
    /**
     * Changes the objectName's attrName's value to attrValue
     * 
     * @param objectName
     * @param attrName
     * @param attrValue
     * @throws Exception
     */
    public void setAttribute(String objectName, String attrName, Object attrValue) throws Exception {
        Attribute attr = new Attribute(attrName, attrValue);
        mbServer.setAttribute(new ObjectName(objectName), attr);
    }
    
    /**
     * Stops the snapshot thread and save the started status
     */
    public boolean stopSnapshot() {
        setSnapshotStarted(false);
        return stopSnapshotThread();
    }
    
    /**
     * Stops the snapshot thread
     */
    private boolean stopSnapshotThread() {
        if(snapshotThread != null) {
            if(snapshotThread.getSnapshotDuration() != Long.MAX_VALUE) {
                saveDuration(snapshotThread.getSnapshotDuration());
                snapshotThread.setSnapshotDuration(Long.MAX_VALUE);
                log.info("Snapshot thread stopped.");
                return true;
            } else {
                return false;
            }
        } else {
            log.error("There is not a snapshot thread running. Stopping aborted.");
            return false;
        }
    }
    
    /**
     * Fetches the data stored from the snapshot thread and returns
     * it in a ArrayList with each element being a HashMap of the attribute
     * mapping to the statistic. All stats will be the average of 
     *          1 - n, n+1 - 2n, ..., cn+1 - c(n+1)
     *
     * Grabs 'numberOfSnapshots' snapshots. Grabs one snapshot per
     * 'everyNthsnapshot'
     * 
     * @param numberOfSnapshot
     * @param everyNthSnapshot
     * @return ArrayList
     */ 
    public ArrayList<HashMap<String, HashMap<String, Object>>> fetchSnapshotData(Integer numberOfSnapshot, Integer everyNthSnapshot) {
        return snapshotDBHelper.fetchData(numberOfSnapshot, everyNthSnapshot);
    }
    
    /**
     * Fetches the max amount for each statistic stored from the snapshot thread
     * and returns it in a HashMap
     * 
     * @param numberOfSnapshot
     * @return HashMap
     */
    public HashMap<String, HashMap<String, Long>> fetchMaxSnapshotData(Integer numberOfSnapshot) {
        return snapshotDBHelper.fetchMaxSnapshotData(numberOfSnapshot);
    }

    /**
     * Fetches the min amount for each statistic stored from the snapshot thread
     * and returns it in a HashMap
     * 
     * @param numberOfSnapshot
     * @return HashMap
     */
    public HashMap<String, HashMap<String, Long>> fetchMinSnapshotData(Integer numberOfSnapshot) {
        return snapshotDBHelper.fetchMinSnapshotData(numberOfSnapshot);
    }
    
    /**
     * Gets the elapsed time in milliseconds between each snapshot.
     * 
     * @return Long
     */
    public Long getSnapshotDuration() {
        try {
            return Long.parseLong(SnapshotConfigXMLBuilder.getAttributeValue( MonitorConstants.DURATION ));
        } catch(Exception e) {
            return new Long( MonitorConstants.DEFAULT_DURATION );
        }
    }
    
    /**
     * Sets the elapsed time in milliseconds between each snapshot.
     * 
     * @param snapshotDuration
     */
    public void setSnapshotDuration(Long snapshotDuration) {
        if(snapshotThread != null) {
            snapshotThread.setSnapshotDuration(snapshotDuration.longValue());
            saveDuration(snapshotThread.getSnapshotDuration());
        } else {
            log.warn("There is not a snapshot thread instantiated.");
        }
    }
    
    public void setSnapshotRetention(Integer retention) {
        saveRetention(retention.intValue());
    }
    
    public void setSnapshotStarted(Boolean started) {
        saveStarted(started.booleanValue());
    }
    
    /**
    * Begins the snapshot process given the time interval between snapshots
     *
     * Precondition:
     *          interval is given in milli seconds
     * 
     * @param interval
     */
    public boolean startSnapshot(Long interval) {
        setSnapshotStarted(true);
        // get the saved/default retention period
        String retentionStr = null;
        try {
            retentionStr = SnapshotConfigXMLBuilder.getAttributeValue( MonitorConstants.RETENTION );
        } catch(Exception e){
            // happens when there is not an instance of "retention" in the config
            // which is okay.
        }
        int retention;
        if(retentionStr == null) {
            retention = MonitorConstants.DEFAULT_RETENTION;
        } else {
            retention = Integer.parseInt(retentionStr);
        }
        return startSnapshot(interval, new Integer(retention));
    }
    
    /**
     * Begins the snapshot process given the time interval between snapshots
     *
     * Precondition:
     *          interval is given in milli seconds
     * 
     * @param interval
     */
    public boolean startSnapshot(Long interval, Integer retention) {
        if((snapshotThread == null || (snapshotThread != null && (snapshotThread.SnapshotStatus() == 0))) && interval.longValue() > 0) {
            saveDuration(interval.longValue());
            saveRetention(retention.intValue());
            snapshotThread = new SnapshotThread(interval.longValue(), mbServer);
            snapshotThread.start();
            log.info("Snapshot thread successfully created.");
            return true;
        } else {
            log.warn("There is already a snapshot thread running.");
            return false;
        }
    }
    
    public Long getSnapshotCount() {
        return snapshotDBHelper.getSnapshotCount();
    }
    
    /**
     * Fetches all mbean names that provide JSR-77 statistics
     * 
     * @return A set containing all mbean names of mbeans that provide
     * statistics
     */
    public Set<String> getStatisticsProviderMBeanNames() {
        return (Set<String>)MBeanHelper.getStatsProvidersMBeans( getAllMBeanNames() );
    }
    
    /**
     * Fetches all mbean names
     * 
     * @return A set containing all mbean names
     */
    public Set<String> getAllMBeanNames() {
        try {
            Set<ObjectName> names = (Set<ObjectName>)mbServer.queryNames(null, null);
            Set<String> strNames = new HashSet<String>();
            for(Iterator<ObjectName> it = names.iterator(); it.hasNext(); ) {
                strNames.add(it.next().getCanonicalName());
            }
            return strNames;
       } catch(Exception e) {
            log.error(e.getMessage(), e);
            return new HashSet<String>();
        }
    }
    
    public void doFail() {
        doStop();
    }

    /**
     * Executes when the GBean starts up. Also starts the snapshot thread.
     */
    public void doStart() {
        boolean started = false;
        try {
            started = Boolean.parseBoolean(
                    SnapshotConfigXMLBuilder.getAttributeValue(MonitorConstants.STARTED));
        } catch (Exception e) {
            log.warn("Failed to parse 'started', set to default value " + started, e);            
        }
        if (started) {
            long duration = MonitorConstants.DEFAULT_DURATION;
            try {
                duration = Long.parseLong(
                        SnapshotConfigXMLBuilder.getAttributeValue(MonitorConstants.DURATION));
            } catch (Exception e) {
                log.warn("Failed to parse 'duration', set to default value " + duration, e);
            }
            startSnapshot(duration);
        }
    }
    
    /**
     * Executes when the GBean stops. Also stops the snapshot thread.
     */
    public void doStop() {
        if(SnapshotStatus() == 1) {
            stopSnapshotThread();
        }
    }
    
    private void saveDuration(long duration) {
        SnapshotConfigXMLBuilder.saveDuration(duration);
    }
    
    private void saveRetention(int retention) {
        SnapshotConfigXMLBuilder.saveRetention(retention);
    }
    
    private void saveStarted(boolean started) {
        SnapshotConfigXMLBuilder.saveStarted(started);
    }
    
    /**
     * Adds a record of the mbean via its name to take snapshots of. As a result
     * the mbeanName will be written to snapshot-config.xml
     * 
     * @param mbeanName
     */
    public boolean addMBeanForSnapshot(String mbeanName) {
        return SnapshotConfigXMLBuilder.addMBeanName(mbeanName);
    }

    /**
     * Removes a record of the mbean via its name to take snapshots of. As a result
     * the mbeanName will be removed from snapshot-config.xml
     * 
     * @param mbeanName
     */
    public boolean removeMBeanForSnapshot(String mbeanName) {
        return SnapshotConfigXMLBuilder.removeMBeanName(mbeanName);
    }
    
    /**
     * @return A map: mbeanName --> ArrayList of statistic attributes for that mbean
     */
    public HashMap<String, ArrayList<String>> getAllSnapshotStatAttributes() {
        HashMap<String, ArrayList<String>> snapshotAttributes = new HashMap<String, ArrayList<String>>();
        Set<String> mbeans = getTrackedMBeans();
        // for each mbean name
        for(Iterator<String> it = mbeans.iterator(); it.hasNext(); ) {
            ArrayList<String> mbeanStatsList = new ArrayList<String>();
            String mbeanName = it.next();
            try {
                Stats stats = (Stats)mbServer.getAttribute(new ObjectName(mbeanName), "stats");
                String[] sttsName = stats.getStatisticNames();
                Statistic[] stts = stats.getStatistics();
                for(int i = 0; i < sttsName.length; i++) {
                    Statistic aStat = stats.getStatistic(sttsName[i]);
                    if(aStat instanceof RangeStatistic) {
                        mbeanStatsList.add(stts[i].getName() + " Current");
                        mbeanStatsList.add(stts[i].getName() + " Max");
                        mbeanStatsList.add(stts[i].getName() + " Min");
                    } else if(aStat instanceof CountStatistic) {
                        mbeanStatsList.add(stts[i].getName());
                    } else if(aStat instanceof TimeStatistic) {
                        mbeanStatsList.add(stts[i].getName() + " CurrentTime");
                        mbeanStatsList.add(stts[i].getName() + " MaxTime");
                        mbeanStatsList.add(stts[i].getName() + " MinTime");
                        mbeanStatsList.add(stts[i].getName() + " TotalTime");
                    } else {
                        // for the time being, only numbers should be returned
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            // save attributes to the returning list
            snapshotAttributes.put(mbeanName, mbeanStatsList);
        }
        return snapshotAttributes;
    }
    
    public Set<String> getTrackedMBeans() {
        ArrayList<String> mbeans = (ArrayList<String>)SnapshotConfigXMLBuilder.getMBeanNames();
        Set<String> set = new HashSet<String>();
        for(int i = 0; i < mbeans.size(); i++) {
            set.add(mbeans.get(i));
        }
        return set;
    }
    
    public Integer getSnapshotRetention() {
        try {
            return new Integer(SnapshotConfigXMLBuilder.getAttributeValue( MonitorConstants.RETENTION ));
        } catch(Exception e) {
            return new Integer(MonitorConstants.DEFAULT_RETENTION); // the default
        }
    }
    
    /**
     * @param name - object name of the mbean
     * @param operationName - method within the class
     * @param params - parameters for the method
     * @param signature - types for the parameters
     * @return Invokes the method of a class defined.
     */
    public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature) throws Exception {
        return mbServer.invoke(name, operationName, params, signature);
    }
    
    /**
     * @param mbeanName
     * @param statsName
     * @param numberOfSnapshots
     * @param everyNthSnapshot
     * @return HashMap which maps from a snapshot_time --> value of the mbean.statsName at that time
     */
    public TreeMap<Long, Long> getSpecificStatistics(   String mbeanName,
                                                        String statsName, 
                                                        Integer numberOfSnapshots, 
                                                        Integer everyNthSnapshot,
                                                        Boolean showArchived) {
        return snapshotDBHelper.getSpecificStatistics(mbeanName, statsName, numberOfSnapshots.intValue(), everyNthSnapshot.intValue(), showArchived);
    }
    
    /**
     * @return Returns true if snapshot is running.
     */
    public Integer SnapshotStatus() {
        // TODO: check if the snapshot thread is running 
        if(snapshotThread == null) {
            return 0;
        } else {
            return snapshotThread.SnapshotStatus();
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("MasterRemoteControlJMX", MasterRemoteControlJMX.class);
        infoFactory.addOperation("getStats", new Class[] {String.class}, "HashMap");
        infoFactory.addOperation("setAttribute", new Class[] {String.class, String.class, Object.class}, "void");
        infoFactory.addOperation("startSnapshot", new Class[] {Long.class}, "Boolean");
        infoFactory.addOperation("startSnapshot", new Class[] {Long.class, Integer.class}, "Boolean");
        infoFactory.addOperation("stopSnapshot", new Class[] {}, "Boolean");
        infoFactory.addOperation("fetchSnapshotData", new Class[] {Integer.class, Integer.class}, "ArrayList");
        infoFactory.addOperation("fetchMaxSnapshotData", new Class[] {Integer.class}, "HashMap");
        infoFactory.addOperation("fetchMinSnapshotData", new Class[] {Integer.class}, "HashMap");
        infoFactory.addOperation("getSnapshotDuration", new Class[] {}, "Long");
        infoFactory.addOperation("getSnapshotCount", new Class[] {}, "Long");
        infoFactory.addOperation("setSnapshotDuration", new Class[] {Long.class}, "void");
        infoFactory.addOperation("getStatisticsProviderMBeanNames", new Class[] {}, "Set");
        infoFactory.addOperation("getAllMBeanNames", new Class[] {}, "Set");
        infoFactory.addOperation("getAllSnapshotStatAttributes", new Class[] {}, "HashMap");
        infoFactory.addOperation("addMBeanForSnapshot", new Class[] {String.class}, "void");
        infoFactory.addOperation("removeMBeanForSnapshot", new Class[] {String.class}, "void");
        infoFactory.addOperation("getSnapshotRetention", new Class[] {}, "Integer");
        infoFactory.addOperation("setSnapshotRetention", new Class[] {Integer.class}, "void");
        infoFactory.addOperation("SnapshotStatus", new Class[] {}, "Integer");
        infoFactory.addOperation("getSpecificStatistics", new Class[] {String.class, String.class, Integer.class, Integer.class, Boolean.class}, "TreeMap");
        infoFactory.addOperation("getTrackedMBeans", new Class[] {}, "Set");
        infoFactory.setConstructor(new String[] {});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
