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

package org.apache.geronimo.monitoring.ejb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.rmi.RemoteException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.ejb.CreateException;
import javax.ejb.EJB;
import javax.management.Attribute;
import javax.management.ObjectName;
import javax.management.j2ee.Management;
import javax.management.j2ee.ManagementHome;
import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.RangeStatistic;
import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.Stats;
import javax.management.j2ee.statistics.TimeStatistic;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.geronimo.monitoring.MonitorConstants;
import org.apache.geronimo.monitoring.MBeanHelper;
import org.apache.geronimo.monitoring.snapshot.SnapshotConfigXMLBuilder;
import org.apache.geronimo.monitoring.snapshot.SnapshotDBHelper;
import org.apache.geronimo.monitoring.ejb.snapshot.SnapshotProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a Stateless Session Bean that will be the bottleneck for the communication
 * between the management node and the data in the server node.
 */
@Stateless(name="ejb/mgmt/MRC")
@Remote(MasterRemoteControlRemote.class)
@Local(MasterRemoteControlLocal.class)
@PermitAll
public class MasterRemoteControl {
    private static final Logger log = LoggerFactory.getLogger(MasterRemoteControl.class);
    
    @EJB(name = "ejb/mgmt/MEJB")
    private ManagementHome mejbHome;
    
    // inject Data Sources
    @Resource(name="jdbc/ActiveDS") private DataSource activeDS;
    @Resource(name="jdbc/ArchiveDS") private DataSource archiveDS;
    
    // inject a TimerService
    @Resource private TimerService timer;
    
    private SnapshotDBHelper snapshotDBHelper;

    public MasterRemoteControl() {
        
    }

    @PostConstruct
    private void init() {
        // set up SnaphotDBHelper with the necessary data sources
        // Note: do not put this in the constructor...datasources are not injected by then
        snapshotDBHelper = new SnapshotDBHelper(activeDS, archiveDS);
    }

    /**
     * Looks up the JSR-77 statistics associated with this object name.
     * 
     * @param objectName
     * @return HashMap
     * @throws Exception
     */
    @RolesAllowed("mejbuser")
    public HashMap<String, Long> getStats(String objectName) throws Exception {
        HashMap<String, Long> statsMap = new HashMap<String, Long>();
        Stats stats = (Stats)getMEJB().getAttribute(new ObjectName(objectName), "stats");
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
                // for the time being, only numbers should be returned
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
    @RolesAllowed("mejbadmin")
    public void setAttribute(String objectName, String attrName, Object attrValue) throws Exception {
        Attribute attr = new Attribute(attrName, attrValue);
        getMEJB().setAttribute(new ObjectName(objectName), attr);
    }
    
    // This method is called by the EJB container upon Timer expiration.
    @Timeout
    @RolesAllowed("mejbuser")
    public void handleTimeout(Timer theTimer) {
        SnapshotProcessor.takeSnapshot(this);
        
        // get the duration of theTimer
        long duration = Long.parseLong((String)theTimer.getInfo());
        // if the duration is different than the one in the snapshot-config.xml
        // we need to get rid of this timer and start a new one with the 
        // correct duration.
        if(duration != getSnapshotDuration().longValue()) {
            Collection<Timer> timers = timer.getTimers();
            for(Iterator<Timer> it = timers.iterator(); it.hasNext(); ) {
                // cancel all timers
                it.next().cancel();
            }
            // start a new one
            long newDuration = getSnapshotDuration().longValue();
            timer.createTimer(newDuration, newDuration, "" + newDuration);
        }
    }
    
    /**
     * Begins the snapshot process given the time interval between snapshots
     *
     * Precondition:
     *          interval is given in milli seconds
     * 
     * @param interval
     */
    @RolesAllowed("mejbuser")
    public boolean startSnapshot(Long interval) {
        // get the saved/default retention period
        String retentionStr = null;
        try {
            retentionStr = SnapshotConfigXMLBuilder.getAttributeValue("retention");
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
        return startSnapshot(interval, retention);
    }
    
    @RolesAllowed("mejbuser")
    public boolean startSnapshot(Long interval, int retention) {
        Collection<Timer> timers = timer.getTimers();
        if(timers.size() == 0) {
            saveDuration(interval.longValue());
            saveRetention(retention);
            timer.createTimer(0, interval.longValue(), "" + interval.longValue());
            log.info("Created timer successfully.");
            return true;
        } else {
            log.warn("There is already a snapshot timer running...");
            return false;
        }
    }
    
    /**
     * Stops the snapshot thread
     */
    @RolesAllowed("mejbuser")
    public boolean stopSnapshot() {
        Collection<Timer> timers = timer.getTimers();
        // stop all timers
        boolean cancelled = false;
        for(Iterator<Timer> it = timers.iterator(); it.hasNext(); ) {
            Timer t = it.next();
            t.cancel();
            cancelled = true;
            log.info("Stopped snapshot timer...");
        }
        return cancelled;
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
    @RolesAllowed("mejbuser")
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
    @RolesAllowed("mejbuser")
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
    @RolesAllowed("mejbuser")
    public HashMap<String, HashMap<String, Long>> fetchMinSnapshotData(Integer numberOfSnapshot) {
        return snapshotDBHelper.fetchMinSnapshotData(numberOfSnapshot);
    }
    
    /**
     * Gets the elapsed time in milliseconds between each snapshot.
     * 
     * @return Long
     */
    @RolesAllowed("mejbuser")
    public Long getSnapshotDuration() {
        // return what is stored in the snapshot-config.xml or default value
        try {
            String returnedDuration = SnapshotConfigXMLBuilder.getAttributeValue( MonitorConstants.DURATION );
            return Long.parseLong( returnedDuration );
        } catch(Exception e) {
            return MonitorConstants.DEFAULT_DURATION; // the default
        }
    }
    
    /**
     * Sets the elapsed time in milliseconds between each snapshot.
     * The duration will be read in each time the handleTimeout()
     * is called. So the change will be seen when the next
     * handleTimeout() is called.
     * 
     * @param snapshotDuration
     */
    @RolesAllowed("mejbuser")
    public void setSnapshotDuration(Long snapshotDuration) {
        saveDuration(snapshotDuration);
    }
    
    @RolesAllowed("mejbuser")
    public void setSnapshotRetention(int retention) {
        saveRetention(retention);
    }
    
    @RolesAllowed("mejbuser")
    public String getSnapshotRetention() {
        try {
            return SnapshotConfigXMLBuilder.getAttributeValue( MonitorConstants.RETENTION );
        } catch(Exception e) {
            return "" + MonitorConstants.DEFAULT_RETENTION; // the default
        }
    }
    
    @RolesAllowed("mejbuser")
    public Long getSnapshotCount() {
        return snapshotDBHelper.getSnapshotCount();
    }
    
    /**
     * Fetches all mbean names that provide JSR-77 statistics
     * 
     * @return A set containing all mbean names of mbeans that provide
     * statistics
     */
    @RolesAllowed("mejbuser")
    public Set<String> getStatisticsProviderMBeanNames() {
        return MBeanHelper.getStatsProvidersMBeans( getAllMBeanNames() );
    }
    
    /**
     * Fetches all mbean names
     * 
     * @return A set containing all mbean names
     */
    @RolesAllowed("mejbuser")
    public Set<String> getAllMBeanNames() {
        try {
            Set<ObjectName> names = (Set<ObjectName>)getMEJB().queryNames(null, null);
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
    
    private void saveDuration(long duration) {
        SnapshotConfigXMLBuilder.saveDuration(duration);
    }
    
    private void saveRetention(int retention) {
        SnapshotConfigXMLBuilder.saveRetention(retention);
    }
    
    /**
     * Adds a record of the mbean via its name to take snapshots of. As a result
     * the mbeanName will be written to snapshot-config.xml
     * 
     * @param mbeanName
     */
    @RolesAllowed("mejbuser")
    public boolean addMBeanForSnapshot(String mbeanName) {
        return SnapshotConfigXMLBuilder.addMBeanName(mbeanName);
    }

    /**
     * Removes a record of the mbean via its name to take snapshots of. As a result
     * the mbeanName will be removed from snapshot-config.xml
     * 
     * @param mbeanName
     */
    @RolesAllowed("mejbuser")
    public boolean removeMBeanForSnapshot(String mbeanName) {
        return SnapshotConfigXMLBuilder.removeMBeanName(mbeanName);
    }
    
    /**
     * @return A map: mbeanName --> ArrayList of statistic attributes for that mbean
     */
    @RolesAllowed("mejbuser")
    public HashMap<String, ArrayList<String>> getAllSnapshotStatAttributes() {
        HashMap<String, ArrayList<String>> snapshotAttributes = new HashMap<String, ArrayList<String>>();
        Set<String> mbeans = getTrackedMBeans();
        // for each mbean name
        for(Iterator<String> it = mbeans.iterator(); it.hasNext(); ) {
            ArrayList<String> mbeanStatsList = new ArrayList<String>();
            String mbeanName = it.next();
            try {
                Stats stats = (Stats)getMEJB().getAttribute(new ObjectName(mbeanName), "stats");
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

    /**
     * @return Returns true if snapshot is running.
     */
    @RolesAllowed("mejbuser")
    public boolean isSnapshotRunning() {
        Collection<Timer> timers = timer.getTimers();
        // if there are timers there is something running to collect snapshots
        if(timers.size() > 0) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * @param name - object name of the mbean
     * @param operationName - method within the class
     * @param params - parameters for the method
     * @param signature - types for the parameters
     * @return Invokes the method of a class defined.
     */
    @RolesAllowed("mejbadmin")
    public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature) throws Exception {
        return getMEJB().invoke(name, operationName, params, signature);
    }
    
    /**
     * @param mbeanName
     * @param statsName
     * @param numberOfSnapshots
     * @param everyNthSnapshot
     * @return HashMap which maps from a snapshot_time --> value of the mbean.statsName at that time
     */
    @RolesAllowed("mejbuser")
    public TreeMap<Long, Long> getSpecificStatistics(   String mbeanName,
                                                        String statsName, 
                                                        int numberOfSnapshots, 
                                                        int everyNthSnapshot,
                                                        boolean showArchived) {
        return snapshotDBHelper.getSpecificStatistics(mbeanName, statsName, numberOfSnapshots, everyNthSnapshot, showArchived);
    }
    
    /**
     * @return A set of all mbeans being tracked from the db
     */
    @RolesAllowed("mejbuser")
    public Set<String> getTrackedMBeans() {
        ArrayList<String> mbeans = SnapshotConfigXMLBuilder.getMBeanNames();
        Set<String> set = new HashSet<String>();
        for(int i = 0; i < mbeans.size(); i++) {
            set.add(mbeans.get(i));
        }
        return set;
    }

    private Management getMEJB() throws NamingException, RemoteException, CreateException {
        return mejbHome.create();
    }
}
