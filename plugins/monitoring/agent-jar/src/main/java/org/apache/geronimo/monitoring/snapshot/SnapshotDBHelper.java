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
package org.apache.geronimo.monitoring.snapshot;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.monitoring.MonitorConstants;

public class SnapshotDBHelper {
    private static final Logger log = LoggerFactory.getLogger(SnapshotDBHelper.class);
    // Connection object used for DB interaction
    private static Connection conn = null;
    // Data Sources
    private static DataSource activeDS = null;
    private static DataSource archiveDS = null;
    
    public SnapshotDBHelper() {
        
    }
    
    public SnapshotDBHelper(DataSource activeDS, DataSource archiveDS) {
        SnapshotDBHelper.activeDS = activeDS;
        SnapshotDBHelper.archiveDS = archiveDS;
    }
    
    /**
     * @return A map: mbeanName --> ArrayList of statistic attributes for that mbean
     */
    public HashMap<String, ArrayList<String>> getAllSnapshotStatAttributes() {
        openActiveConnection();
        HashMap<String, ArrayList<String>> retval = new HashMap<String, ArrayList<String>>();
        try {
            Statement stmt = conn.createStatement();
            String query = "SELECT DISTINCT mbeanName, statsNameList FROM MBeans";
            ResultSet rs = stmt.executeQuery(query);
            // add each mbean/statsValue combination to retval
            while(rs.next()) {
                String mbeanName = rs.getString( MonitorConstants.MBEANNAME );
                String statsNameStr = rs.getString( MonitorConstants.STATSNAMELIST );
                String[] statsNameList = statsNameStr.split(",");
                ArrayList<String> mbeanAttributeList = new ArrayList<String>();
                // copy from String[] to ArrayList<String>
                for(int i = 0; i < statsNameList.length; i++) {
                    mbeanAttributeList.add(statsNameList[i]);
                }
                retval.put(mbeanName, mbeanAttributeList);
            }
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            closeConnection();
        }
        return retval;
    }
    
    /**
     * 
     * @return The number of snapshots present in the active database
     */
    public Long getSnapshotCount() {
        long retval = 0;
        try {
            openActiveConnection();
            Statement stmt = conn.createStatement();
            String query = "SELECT COUNT(DISTINCT snapshot_time) FROM Statistics";
            ResultSet rs = stmt.executeQuery(query);
            rs.next();
            retval = rs.getLong(1);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            closeConnection();
        }
        return new Long(retval);
    }
    
    /**
     * @param numberOfSnapshots - the number of latest snapshots to look at
     * @return A hashmap which maps an mbean --> a hashmap with an attribute name and its value . All values will be the max.
     */
    public HashMap<String, HashMap<String, Long>> fetchMaxSnapshotData(Integer numberOfSnapshots) {
        return fetchMaxOrMinSnapshotData(numberOfSnapshots, true);
    }
    /**
     * @param numberOfSnapshots - the number of latest snapshots to look at
     * @return A hashmap which maps an mbean --> a hashmap with an attribute name and its value . All values will be the min.
     */    
    public HashMap<String, HashMap<String, Long>> fetchMinSnapshotData(Integer numberOfSnapshots) {
        return fetchMaxOrMinSnapshotData(numberOfSnapshots, false);
    }
    
    /**
     * @param numberOfSnapshots - the number of latest snapshots to look at.
     * @param isMax - true if the result should be all maximum values. otherwise, false.
     * @return A hashmap which maps an mbean --> a hashmap with an attribute name and its value . All values will be the min
     * or max, depending on the isMax parameter.
     */
    private HashMap<String, HashMap<String, Long>> fetchMaxOrMinSnapshotData(Integer numberOfSnapshots, boolean isMax) {
        openActiveConnection();
        ResultSet snapshotTimeTable = fetchSnapshotTimesFromDB();
        HashMap<String, HashMap<String, Long>> stats = new HashMap<String, HashMap<String, Long>>();
        try {
            // for each snapshot in the table
            while(snapshotTimeTable.next()) {
                Long snapshotTime = snapshotTimeTable.getLong( MonitorConstants.SNAPSHOT_TIME );
                // retrieve the snapshot information by time
                ResultSet snapshotData = fetchSnapshotDataFromDB(snapshotTime);
                // for each statistic, perform a relaxation
                while(snapshotData.next()) {
                    String mbean = snapshotData.getString( MonitorConstants.MBEANNAME );
                    // get map associated with mbean
                    HashMap<String, Long> mbeanMap = stats.get(mbean);
                    if(mbeanMap == null) {
                        mbeanMap = new HashMap<String, Long>();
                    }
                    String[] statsNameList = snapshotData.getString( MonitorConstants.STATSNAMELIST ).split(",");
                    String[] statsValueList = snapshotData.getString( MonitorConstants.STATSVALUELIST ).split(",");
                    assert(statsNameList.length == statsValueList.length);
                    // for each statname/statsvalue combo in an mbean
                    for(int i = 0 ; i < statsNameList.length; i++) {
                        String statsName = statsNameList[i];
                        Long maxStatsValue = mbeanMap.get(statsName);
                        // give maxStatsValue some value if there isn't one
                        if(maxStatsValue == null) {
                            if(isMax) {
                                maxStatsValue = new Long(0);
                            } else {
                                maxStatsValue = Long.MAX_VALUE;
                            }
                        }
                        // relax
                        if(isMax) {
                            maxStatsValue = new Long(Math.max(Long.parseLong(statsValueList[i]), maxStatsValue.longValue()));
                        } else {
                            maxStatsValue = new Long(Math.min(Long.parseLong(statsValueList[i]), maxStatsValue.longValue()));
                        }
                        // save name/value back into mbeanMap
                        mbeanMap.put(statsName, maxStatsValue);
                    }
                    // save mbeanMap back into stats
                    stats.put(mbean, mbeanMap);
                }
                
                // compute the remaining snapshots left to look at
                numberOfSnapshots--;
                // discontinue once we have looked at numberOfSnapshots snapshots
                if(numberOfSnapshots == 0) {
                    break;
                }
            }
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            closeConnection();
        }
        return stats;
    }
    
    /**
     * @param ds
     * @param aggregateStats
     * @return Returns a boolean if the snapshot statistics were successfully added
     * to the DB.
     */
    public boolean addSnapshotToDB(HashMap<String, HashMap<String, Long>> aggregateStats) {
        boolean success = true;
        // get the current time from 1970
        String currTime = "";
        currTime += (new Date()).getTime();
        try {
            // for each mbean
            for(Iterator itt = aggregateStats.keySet().iterator(); itt.hasNext(); ) {
                String mbean = (String)itt.next();
                // prepare the statsNameList and statsValueList beforehand
                String statsNameList = "", statsValueList = "";
                for(Iterator<String> it = aggregateStats.get(mbean).keySet().iterator(); it.hasNext(); ) {
                    String statsName = it.next();
                    Long statsValue = aggregateStats.get(mbean).get(statsName);
                    if(statsNameList.length() == 0 || statsValueList.length() == 0) {
                        // do not add a comma because this is the first occurrence
                        statsValueList += statsValue.toString();
                        statsNameList += statsName;
                    } else {
                        // add a comma
                        statsValueList += "," + statsValue.toString();
                        statsNameList += "," + statsName;
                    }
                }
                
                // start talking to DB
                openActiveConnection();
                Statement stmt = conn.createStatement();
                HashMap stats = aggregateStats.get(mbean);
                //--------Ensure MBeans are in place
                int mbeanId = getMBeanId(mbean); 
                if(mbeanId != -1) {
                    // mbean already exists in the db
                } else {
                    // doesn't exist in the db so add it
                    // add mbean record to the db
                    stmt.executeUpdate("INSERT INTO MBeans (mbeanName, statsNameList) VALUES ("+ surroundWithQuotes(mbean) + "," + surroundWithQuotes(statsNameList) + ")");
                    mbeanId = getMBeanId(mbean);
                }
                
                // insert the statistics into Statistics table
                stmt.executeUpdate( prepareInsertSnapshotStatement(currTime, statsValueList, mbeanId) );
                closeConnection();
            }
        } catch(Exception  e){
            log.error(e.getMessage(), e);
            success = false;
        } finally {
            closeConnection();
        }
        
        // go through the archiving process
        try {
            int retentionDays = Integer.parseInt(SnapshotConfigXMLBuilder.getAttributeValue("retention"));
            long retentionMillis = (long)(retentionDays) * 86400000; // convert from days to milliseconds
            archiveSnapshots( Long.parseLong(currTime) - retentionMillis );
        } catch(Exception e) {
            log.warn("Cannot archive snapshots because attribute 'retention' is not present in snapshot-config.xml.");
        }
        return success;
    }
    
    /**
     * Moves records from the ActiveDB to the ArchiveDB. The records that are moved
     * are those whose snapshot_times exceed the retention period
     * @param cutOffTime - in milliseconds
     */
    private void archiveSnapshots(long cutOffTime) {
        // for each successful update of Snapshots/Statistics table
        // increment or decrement these counters to ensure that nothing is being 
        // lost in between. If these counters are non-zero, some records have been
        // lost.
        int snapshotsOver = 0;
        int statisticsOver = 0;
        try {
            openActiveConnection();
            ResultSet overDueSnapshotTimes = getOverDueSnapshotTimes(cutOffTime);
            ArrayList<Long> overdueTimes = new ArrayList<Long>();
            // save overdue times into an array list for later usage
            while(overDueSnapshotTimes.next()) {
                overdueTimes.add(overDueSnapshotTimes.getLong( MonitorConstants.SNAPSHOT_TIME ));
            }
            closeConnection();
            // for each overdue snapshot time
            // -transfer all records associated with that snaphot_time to ArchiveDB
            for(int i = 0; i < overdueTimes.size(); i++) {
                long snapshotTime = overdueTimes.get(i);
                openActiveConnection();
                ResultSet rsSnapshotData = fetchSnapshotDataFromDB(new Long(snapshotTime));
                HashMap<String, HashMap<String, Long>> snapshotData = new HashMap<String,HashMap<String, Long>>();
                while(rsSnapshotData.next()) {
                    // extract values from sql table
                    String mbeanName = rsSnapshotData.getString( MonitorConstants.MBEANNAME );
                    String statsNameList = rsSnapshotData.getString( MonitorConstants.STATSNAMELIST );
                    String statsValueList = rsSnapshotData.getString( MonitorConstants.STATSVALUELIST );
                    Long snapshot_time = rsSnapshotData.getLong( MonitorConstants.SNAPSHOT_TIME );
                    // get a connection to the archive db too
                    Connection archiveConn = archiveDS.getConnection();
                    Statement archiveStmt = archiveConn.createStatement();
                    //--------Ensure MBeans are in place
                    int mbeanId = getMBeanIdFromArchive(mbeanName); 
                    if(mbeanId != -1) {
                        // mbean already exists in the db
                    } else {
                        // doesn't exist in the db so add it
                        // add mbean record to the db
                        archiveStmt.executeUpdate("INSERT INTO MBeans (mbeanName, statsNameList) VALUES ("+ surroundWithQuotes(mbeanName) + ", " + surroundWithQuotes(statsNameList) + ")");
                        mbeanId = getMBeanIdFromArchive(mbeanName);
                    }
                    // ensure Statistics table has record of mbeanId, snapshotId, statsValue, statsName
                    String updateStr = prepareInsertSnapshotStatement(snapshot_time + "", statsValueList, mbeanId);
                    statisticsOver += archiveStmt.executeUpdate( updateStr );
                    // close connection to archiveDB
                    archiveConn.close();
                }
                closeConnection();
            }
            // for each snapshot time, remove all instances that is associated with in 
            // in the active DB
            for(int i = 0; i < overdueTimes.size(); i++) {
                long snapshotTime = overdueTimes.get(i);
                openActiveConnection();
                Statement stmt = conn.createStatement();
                // remove from Statistics table
                String statisticsUpdate = "DELETE FROM Statistics WHERE snapshot_time=" + snapshotTime;
                statisticsOver -= stmt.executeUpdate(statisticsUpdate);
                closeConnection();
            }
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            closeConnection();
        }
        
        // ensure that the transferring was good
        if(snapshotsOver != 0) {
            log.warn("Transferred snapshots was completed, but some things were lost.");
        }
        if(statisticsOver != 0) {
            log.warn("Transferred statistics was completed, but some things were lost.");
        }
    }
    
    /**
     * @param cutOffTime
     * @return An SQL table contain a column of all the times that did not make the cutOffTime.
     */
    private ResultSet getOverDueSnapshotTimes(long cutOffTime) {
        try {
            Statement stmt = conn.createStatement();
            String query = "SELECT DISTINCT snapshot_time FROM Statistics WHERE snapshot_time < " + cutOffTime;
            return stmt.executeQuery(query);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * @param mbean
     * @return The mbean id of the mbean from table ArchiveDB.MBean. Returns -1 if record does not exist.
     */
    private int getMBeanIdFromArchive(String mbean) throws Exception {
        int retval = -1;
        Connection archiveConn = archiveDS.getConnection();
        Statement stmt = archiveConn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT id FROM MBeans WHERE mbeanName=" + surroundWithQuotes(mbean));
        if(rs.next()) {
            retval = rs.getInt("id");
        }
        stmt.close();
        archiveConn.close();
        return retval;
    }
    
    /**
     * @param mbean
     * @return The mbean id of the mbean from table ActiveDB.MBean. Returns -1 if record does not exist.
     */
    private int getMBeanId(String mbean) throws Exception {
        int retval = -1;
        Connection conn = activeDS.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT id FROM MBeans WHERE mbeanName=" + surroundWithQuotes(mbean));
        if(rs.next()) {
            retval = rs.getInt("id");
        }
        stmt.close();
        conn.close();
        return retval;
    }
    
    /**
     * @param snapshot_time
     * @param statsValueList
     * @param mbeanId
     * @return Returns an SQL insert statement for one statistic given the correct information.
     */
    public String prepareInsertSnapshotStatement(String snapshot_time, String statsValueList, int mbeanId) {
        String retval = "INSERT INTO Statistics (snapshot_time, statsValueList, mbeanId) VALUES (";
        retval += snapshot_time;
        retval += ",";
        retval += surroundWithQuotes(statsValueList);
        retval += ",";
        retval += mbeanId;
        retval += ")";
        return retval;
    }

    /**
     * @param s
     * @return A String with ' at the beginning and end
     */
    private String surroundWithQuotes(String s) {
        return "'" + s.trim() + "'";
    }
    
    /**
     * Fetches the data stored from the snapshot thread and returns
     * it in a ArrayList with each element being a HashMap of the attribute
     * mapping to the statistic. Grabs 'numberOfSnapshots' snapshots. Grabs 
     * one snapshot per 'everyNthsnapshot'
     * 
     * @param numberOfSnapshot
     * @param everyNthSnapshot
     * @return ArrayList
     */ 
    public ArrayList<HashMap<String, HashMap<String, Object>>> fetchData(Integer numberOfSnapshot, 
                                                                                Integer everyNthSnapshot) {
        ArrayList<HashMap<String, HashMap<String, Object>>> stats = new ArrayList<HashMap<String, HashMap<String, Object>>>();
        openActiveConnection();
        // get all records in the database grouped and ordered by time
        ResultSet table = fetchSnapshotTimesFromDB();
        // iterate through the table and finds the times (which uniquely IDs a snapshot)
        // that are wanted and queries the rest of the DB using the time as the condition
        // (i.e. the ones that are in the c*n-th snapshot where c <= numberOfSnapshot
        // and n == everyNthSnapshot)
        int nthSnapshot = 0;
        try {
            while(table.next()) {
                Long snapshotTime = table.getLong( MonitorConstants.SNAPSHOT_TIME );
                // grab 0*nth, 1*nth, 2*nth snapshot up to min(numberOfSnapshot*everyNthSnapshot, size of the table)
                if(nthSnapshot % everyNthSnapshot == 0) {
                    HashMap<String, HashMap<String, Object>> snapshotData = packageSnapshotData(snapshotTime);
                    stats.add( 0, snapshotData );
                    numberOfSnapshot--;
                }
                nthSnapshot++;
                // no more snapshots needs to be looked at, we have successfully acquired our goal
                if(numberOfSnapshot == 0) {
                    break;
                }
            }
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            closeConnection();
        }
        return stats;
    }
    
    /**
     * @param snapshotTime
     * @return A hashmap in the form <String1, HashMap> where String1 is the mbean name
     * and HashMap is a map containing a <String2, Object> where String2 is an attribute name
     * and Object is the value. Additionally, if String is "times" then it maps to a HashMap
     * containing snapshot_time and snapshot_date information.
     */
    private HashMap<String, HashMap<String, Object>> packageSnapshotData(Long snapshotTime) {
        HashMap<String, HashMap<String, Object>> snapshotPkg = new HashMap<String, HashMap<String, Object>>();
        openActiveConnection();
        ResultSet snapshotData = fetchSnapshotDataFromDB(snapshotTime);
        try {
            // for each record save it somewhere in the snapshotPkg
            while(snapshotData.next()) {
                String currMBean = snapshotData.getString( MonitorConstants.MBEANNAME );
                // get the information for the mbean defined by currMBean
                HashMap<String, Object> mbeanInfo = snapshotPkg.get(currMBean);
                if(mbeanInfo == null) {
                    mbeanInfo = new HashMap<String, Object>();
                }
                // get statistics from resultset
                String statsValueStr = snapshotData.getString( MonitorConstants.STATSVALUELIST );
                String statsNameStr = snapshotData.getString( MonitorConstants.STATSNAMELIST );
                String[] statsValueList = statsValueStr.split(",");
                String[] statsNameList = statsNameStr.split(",");
                assert(statsValueList.length == statsNameList.length);
                // for each statsValue/statsName, save it
                for(int i = 0 ; i < statsValueList.length; i++) {
                    long statValue = Long.parseLong(statsValueList[i]);
                    mbeanInfo.put(statsNameList[i], new Long(statValue));
                }
                // save the hashmap into the snapshotpkg
                snapshotPkg.put(currMBean, mbeanInfo);
            }
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            closeConnection();
        }
        // add the time and date
        HashMap<String, Object> timeMap = new HashMap<String, Object>();
        timeMap.put( MonitorConstants.SNAPSHOT_TIME, snapshotTime);
        snapshotPkg.put("times", timeMap);
        
        return snapshotPkg;
    }
    
    /**
     * @param snapshotTime
     * @return Returns a ResultSet with all statistic information that matches the snapshot_time.
     */
    private ResultSet fetchSnapshotDataFromDB(Long snapshotTime) {
        String query = "SELECT S.statsValueList AS statsValueList, M.statsNameList AS statsNameList, S.snapshot_time AS snapshot_time, M.mbeanName AS mbeanName FROM Statistics S, MBeans M WHERE S.snapshot_time=" + snapshotTime;
        query += " AND S.mbeanId=M.id";
        ResultSet retval = null;
        try {
            if(conn.isClosed()) {
                openActiveConnection();
            }
            Statement stmt = conn.createStatement();
            retval = stmt.executeQuery(query);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }
        return retval;
    }

    /**
     * @return Returns a ResultSet with one column (snapshot_time) sorted in descending order
     */
    private ResultSet fetchSnapshotTimesFromDB() {
        String query = "SELECT DISTINCT snapshot_time FROM Statistics ORDER BY snapshot_time DESC";
        ResultSet retval = null;
        try {
            if(conn.isClosed()) {
                openActiveConnection();
            }
            Statement stmt = conn.createStatement();
            retval = stmt.executeQuery(query);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }
        return retval;
    }
    
    /**
     * Opens the global connection to activeDB
     */
    private void openActiveConnection() {
        try {
            conn = activeDS.getConnection();
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }
    }
    
    /**
     * Opens the global connection to archiveDB
     */
    private void openArchiveConnection() {
        try {
            conn = archiveDS.getConnection();
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }
    }
    
    /**
     * Closes the global connection to a DB
     */
    private void closeConnection() {
        if(conn != null) {
            try {
                conn.close();
            } catch(Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
    
    /**
     * @param mbeanName
     * @param statsName
     * @param numberOfSnapshots
     * @param everyNthSnapshot
     * @return HashMap which maps from a snapshot_time --> value of the mbean.statsName at that time
     */
    public TreeMap<Long, Long> getSpecificStatistics(    String mbeanName, 
                                                                String statsName, 
                                                                int numberOfSnapshots, 
                                                                int everyNthSnapshot, 
                                                                boolean showArchived) {
        openActiveConnection();
        TreeMap<Long, Long> stats = new TreeMap<Long, Long>();
        int nthSnapshot = 0;
        // attempt to get as many snapshots from the active db as possible
        try {
            Statement stmt = conn.createStatement();
            int mbeanId = getMBeanId(mbeanName);
            if(mbeanId == -1) {
                log.error(mbeanName + " does not exist in the database.");
            } else {
                String query = "SELECT DISTINCT snapshot_time, statsValueList, statsNameList FROM Statistics, MBeans M WHERE mbeanId=" + mbeanId + " AND mbeanId=M.id ORDER BY snapshot_time DESC";
                ResultSet rs = stmt.executeQuery(query);
                // iterate through the table paying attention to those at everyNthSnapshot-th position
                while(rs.next()) {
                    // every nth snapshot I save the information into my returning hashmap
                    if(nthSnapshot % everyNthSnapshot == 0) {
                        String[] statsValueList = rs.getString( MonitorConstants.STATSVALUELIST ).split(",");
                        String[] statsNameList = rs.getString( MonitorConstants.STATSNAMELIST ).split(",");
                        assert(statsValueList.length == statsNameList.length);
                        Long statsValue = null;
                        for(int i = 0 ; i < statsNameList.length; i++) {
                            if(statsNameList[i].equals(statsName)) {
                                long value = Long.parseLong(statsValueList[i]);
                                statsValue = new Long(value);
                            }
                        }
                        // exit function after error
                        if(statsValue == null) {
                            log.warn("Statistics name '" + statsName + "' does not exist");
                            return stats;
                        } else {
                            stats.put(rs.getLong( MonitorConstants.SNAPSHOT_TIME ), statsValue);
                            numberOfSnapshots--;
                        }
                    }
                    // update counter
                    nthSnapshot++;
                    // enough data, end this thing
                    if(numberOfSnapshots == 0) {
                        break;
                    }
                }
            }
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            closeConnection();
        }

        nthSnapshot = 0;

        // attempt to get the remaining snapshots requested from the archive DB
        // iff the showArchive flag is set
        if(showArchived && numberOfSnapshots != 0) {
            try {
                openArchiveConnection();    // connection to the Archive DB
                Statement stmt = conn.createStatement();
                int mbeanId = getMBeanId(mbeanName);
                if(mbeanId == -1) {
                    log.error(mbeanName + " does not exist in the database.");
                } else {
                    String query = "SELECT DISTINCT snapshot_time, statsValueList, statsNameList FROM Statistics, MBeans M WHERE mbeanId=" + mbeanId + " AND mbeanId=M.id ORDER BY snapshot_time DESC";
                    ResultSet rs = stmt.executeQuery(query);
                    // iterate through the table paying attention to those at everyNthSnapshot-th position
                    while(rs.next()) {
                        // every nth snapshot I save the information into my returning hashmap
                        if(nthSnapshot % everyNthSnapshot == 0) {
                            String[] statsValueList = rs.getString( MonitorConstants.STATSVALUELIST ).split(",");
                            String[] statsNameList = rs.getString( MonitorConstants.STATSNAMELIST ).split(",");
                            assert(statsValueList.length == statsNameList.length);
                            Long statsValue = null;
                            for(int i = 0 ; i < statsNameList.length; i++) {
                                if(statsNameList[i].equals(statsName)) {
                                    long value = Long.parseLong(statsValueList[i]);
                                    statsValue = new Long(value);
                                }
                            }
                            // exit function after error
                            if(statsValue == null) {
                                log.warn("Statistics name '" + statsName + "' does not exist");
                                return stats;
                            } else {
                                stats.put(rs.getLong( MonitorConstants.SNAPSHOT_TIME ), statsValue);
                                numberOfSnapshots--;
                            }
                        }
                        // update counter
                        nthSnapshot++;
                        // enough data, end this thing
                        if(numberOfSnapshots == 0) {
                            break;
                        }
                    }
                }
            } catch(Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                closeConnection();
            }
        }
        return stats;
    }
    
    /**
     * Sets the necessary data sources for this helper to talk to the db
     * @param activeDS
     * @param archiveDS
     */
    public void setDataSources(DataSource active, DataSource archive) {
        activeDS = active;
        archiveDS = archive;
    }
}
