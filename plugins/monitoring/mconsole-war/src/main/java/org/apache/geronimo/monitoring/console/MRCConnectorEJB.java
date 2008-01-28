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
package org.apache.geronimo.monitoring.console;

import java.sql.Connection;
import java.sql.Statement;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.geronimo.monitoring.MasterRemoteControlRemote;
import org.apache.geronimo.monitoring.console.util.DBManager;

import org.apache.geronimo.util.EncryptionManager;

public class MRCConnectorEJB {

    private MasterRemoteControlRemote mrc = null;

    MRCConnectorEJB() {

    }

    /**
     * @param ip -
     *                IP address of mrc-server to connect to
     * @param userName -
     *                Username for JMX connection to the host
     * @param password -
     *                Password for JMX connection to the host
     * @throws Exception -
     *                 If the connection to mrc-server fails
     */
    public MRCConnectorEJB(String ip, String userName, String password, int port)
            throws Exception {
        // decrypt the password
        password = (String) EncryptionManager.decrypt(password);
        try {
            Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.openejb.client.RemoteInitialContextFactory");
            props
                    .setProperty(Context.PROVIDER_URL, "ejbd://" + ip + ":"
                            + port);
            props.setProperty(Context.SECURITY_PRINCIPAL, userName);
            props.setProperty(Context.SECURITY_CREDENTIALS, password);
            props.setProperty("openejb.authentication.realmName",
                    "geronimo-admin");
            Context ic = new InitialContext(props);
            mrc = (MasterRemoteControlRemote) ic.lookup("ejb/mgmt/MRCRemote");
            mrc.setUpMEJB(userName, password);
        } catch (Exception e) {
            throw e;
        }
        // when the code has reach this point, a connection was successfully
        // established
        // so we need to update the last_seen attribute for the server
        Format formatter = null;
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String currentTime = formatter.format(date);

        Connection conn = DBManager.createConnection();
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("UPDATE SERVERS SET LAST_SEEN = '" + currentTime
                    + "' WHERE IP='" + ip + "'");
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {

            }
        }
    }

    /**
     * @return - Returns an Long representing the current snapshot duration set
     *         on the server side
     * @throws Exception -
     *                 If the connection to the MRC-Server fails
     */
    public Long getSnapshotDuration() throws Exception {
        return mrc.getSnapshotDuration();
    }

    /**
     * @return - Returns an ArrayList of String objects containing a listing of
     *         all statistics values being collected
     * @throws Exception -
     *                 If the connection to the MRC-Server fails
     */
    @SuppressWarnings("unchecked")
    public HashMap<String, ArrayList<String>> getDataNameList()
            throws Exception {

        HashMap<String, ArrayList<String>> DataNameList = new HashMap<String, ArrayList<String>>();

        try {
            DataNameList = mrc.getAllSnapshotStatAttributes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Strip out snapshot_date and snapshot_time, we know these exist
        for (Iterator<String> it = DataNameList.keySet().iterator(); it
                .hasNext();) {
            String mbeanName = it.next();
            DataNameList.get(mbeanName).remove("snapshot_date");
            DataNameList.get(mbeanName).remove("snapshot_time");
        }
        return DataNameList;
    }

    /**
     * @param snapCount -
     *                Number of snapshots to request from the server
     * @param skipCount -
     *                Every nth snapshot. A value of 1 will be every 1. A value
     *                of 2 will be every other.
     * @return - Returns an ArrayList of Map objects.
     * @throws Exception -
     *                 If the connection to the MRC-Server fails
     */
    @SuppressWarnings("unchecked")
    public ArrayList<HashMap<String, HashMap<String, Object>>> getSnapshots(
            int snapCount, int skipCount) throws Exception {
        ArrayList<HashMap<String, HashMap<String, Object>>> snapshotList = mrc
                .fetchSnapshotData(snapCount, skipCount);
        // Check if snapshotList is empty
        if (snapshotList.size() == 0) {
            return snapshotList;
        }
        /*
         * If there are not enough snapshots available to fill the requested
         * number, insert some with values of 0 and the proper times.
         */
        while (snapshotList.size() < snapCount) {
            // Temporary, always is first element (oldest)
            HashMap<String, HashMap<String, Object>> mapTimeFix = snapshotList
                    .get(0);

            // Temporary map, used to generate blank data to be added to
            // the
            // list at position 0
            HashMap<String, HashMap<String, Object>> tempMap = new HashMap<String, HashMap<String, Object>>();

            // Temporary submap, used to store 0 elements to be added to
            // the
            // tempmap
            HashMap<String, Object> subMap = new HashMap<String, Object>();

            // Calculate appropriate time, add it to the submap, then
            // add
            // that to the tempMap
            subMap.put("snapshot_time", ((Long) mapTimeFix.get("times").get(
                    "snapshot_time") - (getSnapshotDuration() * skipCount)));
            Format formatter = null;
            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date((Long) subMap.get("snapshot_time"));
            subMap.put("snapshot_date", formatter.format(date));

            // Add the submap back to the tempmap
            tempMap.put("times", new HashMap<String, Object>(subMap));

            // Clear out the subMap for use again
            subMap.clear();

            // Run through the mbeans

            // Run through the mbeans
            for (Iterator<String> it = mapTimeFix.keySet().iterator(); it
                    .hasNext();) {
                // get the mbean name
                String mbeanName = it.next();
                HashMap<String, Object> stats = null;
                // Verify that it's not times

                if (mbeanName.equals(new String("times"))) {

                } else {
                    stats = mapTimeFix.get(mbeanName);
                    // Run through the stats elements for the particular
                    // mbean
                    for (Iterator<String> itt = stats.keySet().iterator(); itt
                            .hasNext();) {
                        String key = itt.next();
                        // Place faux data into the submap
                        subMap.put(key, new Long(0));
                    }
                    // Add the submap to the tempmap, and clear it
                    tempMap.put(mbeanName, new HashMap<String, Object>(subMap));
                }
            }
            snapshotList.add(0, new HashMap<String, HashMap<String, Object>>(
                    tempMap));
        }

        /*
         * This is where we will be inserting data to fill 'gaps' in the
         * snapshots The initial for-loop will travel from the most recent
         * snapshot to the oldest, checking that the snapshot_time along the way
         * all align with what they should be
         */
        for (int i = snapshotList.size() - 1; i > 0; i--) {
            if (i > 0) {
                HashMap<String, HashMap<String, Object>> mapTimeFix = snapshotList
                        .get(i);
                HashMap<String, HashMap<String, Object>> mapTimeFix2 = snapshotList
                        .get(i - 1);
                // here is where we will in missing data
                while (((((Long) mapTimeFix.get("times").get("snapshot_time") / 1000) / 60)
                        - (((Long) mapTimeFix2.get("times")
                                .get("snapshot_time") / 1000) / 60) > (((getSnapshotDuration() / 1000) / 60) * skipCount))) {
                    HashMap<String, HashMap<String, Object>> tempMap = new HashMap<String, HashMap<String, Object>>();
                    HashMap<String, Object> subMap = new HashMap<String, Object>();

                    for (Iterator<String> it = mapTimeFix.keySet().iterator(); it
                            .hasNext();) {
                        // get the mbean name
                        String mbeanName = it.next();
                        HashMap<String, Object> stats = null;
                        // Verify that it's not times
                        if (!mbeanName.equals("times")) {
                            stats = mapTimeFix.get(mbeanName);
                            // Run through the stats elements for the
                            // particular
                            // mbean
                            for (Iterator<String> itt = stats.keySet()
                                    .iterator(); itt.hasNext();) {
                                String key = itt.next();
                                // Place faux data into the submap
                                subMap.put(key, new Long(0));
                            }
                            // Add the submap to the tempmap, and clear it
                            tempMap.put(mbeanName, new HashMap<String, Object>(
                                    subMap));
                            subMap.clear();
                        }
                    }

                    subMap.put("snapshot_time", new Long((Long) mapTimeFix.get(
                            "times").get("snapshot_time")
                            - (getSnapshotDuration() * skipCount)));
                    Format formatter = null;
                    formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = new Date((Long) subMap.get("snapshot_time"));
                    subMap.put("snapshot_date", formatter.format(date));
                    tempMap.put("times", new HashMap<String, Object>(subMap));
                    subMap.clear();
                    snapshotList.add(i,
                            new HashMap<String, HashMap<String, Object>>(
                                    tempMap));
                    snapshotList.remove(0);
                    mapTimeFix = tempMap;
                    mapTimeFix2 = snapshotList.get(i - 1);
                }
            }
        }
        return snapshotList;
    }

    @SuppressWarnings("unchecked")
    public TreeMap<Long, Long> getSpecificStatistics(String mbeanName,
            String statsName, int snapCount, int skipCount, boolean showArchive)
            throws Exception {
        TreeMap<Long, Long> snapshotList = mrc.getSpecificStatistics(mbeanName,
                statsName, snapCount, skipCount, showArchive);
        // Check if snapshotList is empty
        if (snapshotList.size() == 0) {
            return snapshotList;
        }
        /*
         * If there are not enough snapshots available to fill the requested
         * number, insert some with values of 0 and the proper times.
         */
        while (snapshotList.size() < snapCount) {
            // Temporary, always is first element (oldest)
            Long timeFix = snapshotList.firstKey();

            // Calculate appropriate time, add it to the submap, then
            // add
            // that to the tempMap
            snapshotList.put((timeFix - (getSnapshotDuration() * skipCount)), new Long(0));
        }

        /*
         * This is where we will be inserting data to fill 'gaps' in the
         * snapshots The initial for-loop will travel from the most recent
         * snapshot to the oldest, checking that the snapshot_time along the way
         * all align with what they should be
         */
        Set tempSet = snapshotList.keySet();
        ArrayList<Long> tempArray = new ArrayList(tempSet);

        for (int i = tempArray.size() - 1; i > 0; i--) {
            Long tempLong1 = tempArray.get(i);
            Long tempLong2 = tempArray.get(i - 1);
            // here is where we will in missing data
            while ((((tempLong1 / 1000) / 60) - ((tempLong2 / 1000) / 60) > 
                    (((getSnapshotDuration() / 1000) / 60) * skipCount))
                    && i > 0) {
                tempLong1 = tempLong1 - (getSnapshotDuration() * skipCount);
                snapshotList.remove(tempArray.get(0));
                snapshotList.put(tempLong1, new Long(0));
                tempArray.remove(0);
                i--;
            }
        }
        return snapshotList;
    }

    @SuppressWarnings("unchecked")
    public HashMap<String, HashMap<String, Object>> getLatestSnapshots()
            throws Exception {
        int snapCount = 1;
        int skipCount = 1;
        ArrayList<HashMap<String, HashMap<String, Object>>> snapshotList = mrc
                .fetchSnapshotData(snapCount, skipCount);
        // Check if snapshotList is empty
        if (snapshotList.size() == 0) {
            return null;
        } else
            return snapshotList.get(0);
    }

    /**
     * @return - Returns a boolean indicating successful stop
     * @throws Exception -
     *                 If the connection to the MRC-Server fails
     */
    public boolean stopSnapshotThread() throws Exception {
        return mrc.stopSnapshot();
    }

    /**
     * @return - Returns a boolean indicating successful stop
     * @throws Exception -
     *                 If the connection to the MRC-Server fails
     */
    public boolean startSnapshotThread(long time) throws Exception {
        return mrc.startSnapshot(time);
    }

    public boolean isSnapshotRunning() {
        boolean running = false;
        try {
            running = mrc.isSnapshotRunning();
        } catch (Exception e) {
            return false;
        }
        return running;
    }

    @SuppressWarnings("unchecked")
    public Set<String> getAllMbeanNames() throws Exception {
        return mrc.getAllMBeanNames();
    }

    @SuppressWarnings("unchecked")
    public Set<String> getStatisticsProviderBeanNames() throws Exception {
        return mrc.getStatisticsProviderMBeanNames();
    }

    @SuppressWarnings("unchecked")
    public HashMap<String, ArrayList<String>> getAllSnapshotStatAttributes()
            throws Exception {
        return mrc.getAllSnapshotStatAttributes();
    }

    @SuppressWarnings("unchecked")
    public Set<String> getTrackedBeans() throws Exception {
        return mrc.getTrackedMBeans();
    }

    @SuppressWarnings("unchecked")
    public Set<String> getStatAttributesOnMBean(String mBean) throws Exception {
        HashMap<String, ArrayList<String>> allStatAttributes = getAllSnapshotStatAttributes();
        ArrayList<String> tempArrayList = allStatAttributes.get(mBean);
        Set<String> tempSet = new TreeSet<String>();
        Iterator it = tempArrayList.iterator();
        while (it.hasNext()) {
            tempSet.add(it.next().toString());
        }
        return tempSet;
    }

    @SuppressWarnings("unchecked")
    public Set<String> getTrackedBeansPretty() throws Exception {
        Set trackedBeans = getTrackedBeans();
        Set prettybeans = new TreeSet();
        Iterator it = trackedBeans.iterator();
        while (it.hasNext()) {
            String[] temparray1 = it.next().toString().split("name=");
            String[] temparray2 = temparray1[1].split(",");
            String[] temparray3 = temparray2[0].split("/");
            String mbeanName = null;
            if (temparray3.length > 1)
                mbeanName = temparray3[1];
            else
                mbeanName = temparray2[0];
            prettybeans.add(mbeanName);
        }
        return prettybeans;
    }

    @SuppressWarnings("unchecked")
    public TreeMap<String, String> getTrackedBeansMap() throws Exception {
        Set trackedBeans = getTrackedBeans();
        TreeMap<String, String> beanMap = new TreeMap<String, String>();
        Iterator it = trackedBeans.iterator();
        while (it.hasNext()) {
            String mbeanName = it.next().toString();
            String[] temparray1 = mbeanName.split("name=");
            String[] temparray2 = temparray1[1].split(",");
            String[] temparray3 = temparray2[0].split("/");
            String mbeanNamePretty = null;
            if (temparray3.length > 1)
                mbeanNamePretty = temparray3[1];
            else
                mbeanNamePretty = temparray2[0];
            beanMap.put(mbeanNamePretty, mbeanName);
        }
        return beanMap;
    }

    @SuppressWarnings("unchecked")
    public Set<String> getStatisticsProviderBeanNamesPretty() throws Exception {
        Set availableBeans = getStatisticsProviderBeanNames();
        Set prettybeans = new TreeSet();
        Iterator it = availableBeans.iterator();
        while (it.hasNext()) {
            String[] temparray1 = it.next().toString().split("name=");
            String[] temparray2 = temparray1[1].split(",");
            String[] temparray3 = temparray2[0].split("/");
            String mbeanName = null;
            if (temparray3.length > 1)
                mbeanName = temparray3[1];
            else
                mbeanName = temparray2[0];
            prettybeans.add(mbeanName);
        }
        return prettybeans;
    }

    @SuppressWarnings("unchecked")
    public TreeMap<String, String> getStatisticsProviderBeanNamesMap()
            throws Exception {
        Set availableBeans = getStatisticsProviderBeanNames();
        TreeMap<String, String> beanMap = new TreeMap<String, String>();
        Iterator it = availableBeans.iterator();
        while (it.hasNext()) {
            String mbeanName = it.next().toString();
            String[] temparray1 = mbeanName.split("name=");
            String[] temparray2 = temparray1[1].split(",");
            String[] temparray3 = temparray2[0].split("/");
            String mbeanNamePretty = null;
            if (temparray3.length > 1)
                mbeanNamePretty = temparray3[1];
            else
                mbeanNamePretty = temparray2[0];
            beanMap.put(mbeanNamePretty, mbeanName);
        }
        return beanMap;
    }

    @SuppressWarnings("unchecked")
    public Set<String> getFreeStatisticsProviderBeanNamesPretty()
            throws Exception {
        Set<String> availableBeans = getStatisticsProviderBeanNamesPretty();
        Set<String> usedBeans = getTrackedBeansPretty();
        Set freeBeans = new TreeSet();
        Iterator it = availableBeans.iterator();
        while (it.hasNext()) {
            String mbeanName = it.next().toString();
            if (!usedBeans.contains(mbeanName))
                freeBeans.add(mbeanName);
        }
        return freeBeans;
    }

    @SuppressWarnings("unchecked")
    public Set<String> getFreeStatisticsProviderBeanNames() throws Exception {
        Set<String> availableBeans = getStatisticsProviderBeanNames();
        Set<String> usedBeans = getTrackedBeansPretty();
        Set freeBeans = new TreeSet();
        Iterator it = availableBeans.iterator();
        while (it.hasNext()) {
            String mbeanName = it.next().toString();
            String[] temparray1 = mbeanName.split("name=");
            String[] temparray2 = temparray1[1].split(",");
            String[] temparray3 = temparray2[0].split("/");
            String mbeanNamePretty = null;
            if (temparray3.length > 1)
                mbeanNamePretty = temparray3[1];
            else
                mbeanNamePretty = temparray2[0];
            if (!usedBeans.contains(mbeanNamePretty))
                freeBeans.add(mbeanName);
        }
        return freeBeans;
    }

    @SuppressWarnings("unchecked")
    public TreeMap<String, String> getFreeStatisticsProviderBeanNamesMap()
            throws Exception {
        Set<String> availableBeans = getStatisticsProviderBeanNames();
        Set<String> usedBeans = getTrackedBeansPretty();
        TreeMap<String, String> beanMap = new TreeMap<String, String>();
        Iterator it = availableBeans.iterator();
        while (it.hasNext()) {
            String mbeanName = it.next().toString();
            String[] temparray1 = mbeanName.split("name=");
            String[] temparray2 = temparray1[1].split(",");
            String[] temparray3 = temparray2[0].split("/");
            String mbeanNamePretty = null;
            if (temparray3.length > 1)
                mbeanNamePretty = temparray3[1];
            else
                mbeanNamePretty = temparray2[0];
            if (!usedBeans.contains(mbeanNamePretty))
                beanMap.put(mbeanNamePretty, mbeanName);
        }
        return beanMap;
    }

    @SuppressWarnings("unchecked")
    public boolean stopTrackingMbean(String MBean) throws Exception {
        mrc.removeMBeanForSnapshot(MBean);
        return true;
    }

    @SuppressWarnings("unchecked")
    public boolean startTrackingMbean(String MBean) throws Exception {
        mrc.addMBeanForSnapshot(MBean);
        return true;
    }

    @SuppressWarnings("unchecked")
    public HashMap<String, Long> getStats(String MBean) throws Exception {
        return mrc.getStats(MBean);
    }

    public void setSnapshotDuration(long duration) {
        mrc.setSnapshotDuration(new Long(duration));
    }

    public long getSnapshotRetention() {
        return Long.parseLong(mrc.getSnapshotRetention());
    }

    public void setSnapshotRetention(int duration) {
        mrc.setSnapshotRetention(duration);
    }
}
