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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.geronimo.monitoring.MasterRemoteControlRemote;
import org.apache.geronimo.monitoring.console.util.DBManager;

import org.apache.geronimo.crypto.EncryptionManager;

public class MRCConnector {

    private static final String PATH = "geronimo:ServiceModule=org.apache.geronimo.plugins.monitoring/agent-car-jmx/2.2-SNAPSHOT/car,J2EEServer=geronimo,name=MasterRemoteControlJMX,j2eeType=GBean";
    private static MBeanServerConnection mbServerConn;
    private MasterRemoteControlRemote mrc = null;
    private int Protocol = 0;

    MRCConnector() {

    }

    /**
     * @param ip -
     *            IP address of mrc-server to connect to
     * @param userName -
     *            Username for JMX connection to the host
     * @param password -
     *            Password for JMX connection to the host
     * @throws Exception -
     *             If the connection to mrc-server fails
     */
    public MRCConnector(String ip, String userName, String password, int port,
            int protocol) throws Exception {
        // decrypt the password
        password = (String) EncryptionManager.decrypt(password);
        Protocol = protocol;

        if (Protocol == 1) {

            try {
                Properties props = new Properties();
                props
                        .setProperty(Context.INITIAL_CONTEXT_FACTORY,
                                "org.apache.openejb.client.RemoteInitialContextFactory");
                props.setProperty(Context.PROVIDER_URL, "ejbd://" + ip + ":"
                        + port);
                props.setProperty(Context.SECURITY_PRINCIPAL, userName);
                props.setProperty(Context.SECURITY_CREDENTIALS, password);
                props.setProperty("openejb.authentication.realmName",
                        "geronimo-admin");
                Context ic = new InitialContext(props);
                mrc = (MasterRemoteControlRemote) ic
                        .lookup("ejb/mgmt/MRCRemote");
                mrc.setUpMEJB(userName, password);
            } catch (Exception e) {
                throw e;
            }

        } else {
            try {
                JMXServiceURL serviceURL = new JMXServiceURL(
                        "service:jmx:rmi:///jndi/rmi://" + ip + ":" + port
                                + "/JMXConnector");
                Hashtable<String, Object> env = new Hashtable<String, Object>();
                String[] credentials = new String[2];
                credentials[0] = userName;
                credentials[1] = password;
                env.put(JMXConnector.CREDENTIALS, credentials);
                JMXConnector connector = JMXConnectorFactory.connect(
                        serviceURL, env);
                mbServerConn = connector.getMBeanServerConnection();
            } catch (Exception e) {
                throw e;
            }

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
     *             If the connection to the MRC-Server fails
     */
    public Long getSnapshotDuration() throws Exception {
        if (Protocol == 1) {

            return mrc.getSnapshotDuration();

        } else {

            return (Long) mbServerConn.invoke(new ObjectName(PATH),
                    "getSnapshotDuration", new Object[] {}, new String[] {});
        }
    }

    /**
     * @return - Returns an ArrayList of String objects containing a listing of
     *         all statistics values being collected
     * @throws Exception -
     *             If the connection to the MRC-Server fails
     */
    @SuppressWarnings("unchecked")
    public HashMap<String, ArrayList<String>> getDataNameList()
            throws Exception {

        HashMap<String, ArrayList<String>> DataNameList = new HashMap<String, ArrayList<String>>();

        if (Protocol == 1) {

            try {
                DataNameList = mrc.getAllSnapshotStatAttributes();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            try {
                DataNameList = (HashMap<String, ArrayList<String>>) mbServerConn
                        .invoke(new ObjectName(PATH),
                                "getAllSnapshotStatAttributes",
                                new Object[] {}, new String[] {});
            } catch (Exception e) {
                e.printStackTrace();
            }
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
     *            Number of snapshots to request from the server
     * @param skipCount -
     *            Every nth snapshot. A value of 1 will be every 1. A value of 2
     *            will be every other.
     * @return - Returns an ArrayList of Map objects.
     * @throws Exception -
     *             If the connection to the MRC-Server fails
     */
    @SuppressWarnings("unchecked")
    public ArrayList<HashMap<String, HashMap<String, Object>>> getSnapshots(
            int snapCount, int skipCount) throws Exception {
        ArrayList<HashMap<String, HashMap<String, Object>>> snapshotList = null;
        if (Protocol == 1) {

            snapshotList = mrc.fetchSnapshotData(snapCount, skipCount);

        } else {
            snapshotList = (ArrayList<HashMap<String, HashMap<String, Object>>>) mbServerConn
                    .invoke(new ObjectName(PATH), "fetchSnapshotData",
                            new Object[] { snapCount, skipCount },
                            new String[] { "java.lang.Integer",
                                    "java.lang.Integer" });
        }
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
        TreeMap<Long, Long> snapshotList = null;
        if (Protocol == 1) {

            snapshotList = mrc.getSpecificStatistics(mbeanName, statsName,
                    snapCount, skipCount, showArchive);

        } else {
            snapshotList = (TreeMap<Long, Long>) mbServerConn.invoke(
                    new ObjectName(PATH), "getSpecificStatistics",
                    new Object[] { mbeanName, statsName, snapCount, skipCount,
                            showArchive }, new String[] { "java.lang.String",
                            "java.lang.String", "java.lang.Integer",
                            "java.lang.Integer", "java.lang.Boolean" });

        }
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
            snapshotList.put((timeFix - (getSnapshotDuration() * skipCount)),
                    new Long(0));
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
            while ((((tempLong1 / 1000) / 60) - ((tempLong2 / 1000) / 60) > (((getSnapshotDuration() / 1000) / 60) * skipCount))
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
        ArrayList<HashMap<String, HashMap<String, Object>>> snapshotList = null;
        if (Protocol == 1) {

            snapshotList = mrc.fetchSnapshotData(snapCount, skipCount);

        } else {
            snapshotList = (ArrayList<HashMap<String, HashMap<String, Object>>>) mbServerConn
                    .invoke(new ObjectName(PATH), "fetchSnapshotData",
                            new Object[] { snapCount, skipCount },
                            new String[] { "java.lang.Integer",
                                    "java.lang.Integer" });
        }
        // Check if snapshotList is empty
        if (snapshotList.size() == 0) {
            return null;
        } else
            return snapshotList.get(0);
    }

    /**
     * @return - Returns a boolean indicating successful stop
     * @throws Exception -
     *             If the connection to the MRC-Server fails
     */
    public boolean stopSnapshotThread() throws Exception {
        if (Protocol == 1) {

            return mrc.stopSnapshot();

        } else {
            return (Boolean) mbServerConn.invoke(new ObjectName(PATH),
                    "stopSnapshot", new Object[] {}, new String[] {});
        }
    }

    /**
     * @return - Returns a boolean indicating successful stop
     * @throws Exception -
     *             If the connection to the MRC-Server fails
     */
    public boolean startSnapshotThread(long time) throws Exception {
        if (Protocol == 1) {

            return mrc.startSnapshot(time);

        } else {
            return (Boolean) mbServerConn.invoke(new ObjectName(PATH),
                    "startSnapshot", new Object[] { time },
                    new String[] { "java.lang.Long" });
        }
    }

    public int isSnapshotRunning() {
        Integer running = 0;
        if (Protocol == 1) {

            try {
                if (mrc.isSnapshotRunning())
                    running = 1;
            } catch (Exception e) {
                return 0;
            }

        } else {
            try {
                running = (Integer) mbServerConn.invoke(new ObjectName(PATH),
                        "SnapshotStatus", new Object[] {}, new String[] {});
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
        return running;
    }

    @SuppressWarnings("unchecked")
    public Set<String> getAllMbeanNames() throws Exception {
        if (Protocol == 1) {

            return mrc.getAllMBeanNames();

        } else {
            return (Set<String>) mbServerConn.invoke(new ObjectName(PATH),
                    "getAllMBeanNames", new Object[] {}, new String[] {});
        }
    }

    @SuppressWarnings("unchecked")
    public Set<String> getStatisticsProviderBeanNames() throws Exception {
        if (Protocol == 1) {

            return mrc.getStatisticsProviderMBeanNames();

        } else {
            return (Set<String>) mbServerConn.invoke(new ObjectName(PATH),
                    "getStatisticsProviderMBeanNames", new Object[] {},
                    new String[] {});
        }
    }

    @SuppressWarnings("unchecked")
    public HashMap<String, ArrayList<String>> getAllSnapshotStatAttributes()
            throws Exception {
        if (Protocol == 1) {

            return mrc.getAllSnapshotStatAttributes();

        } else {
            return (HashMap<String, ArrayList<String>>) mbServerConn.invoke(
                    new ObjectName(PATH), "getAllSnapshotStatAttributes",
                    new Object[] {}, new String[] {});
        }
    }

    @SuppressWarnings("unchecked")
    public Set<String> getTrackedBeans() throws Exception {
        if (Protocol == 1) {

            return mrc.getTrackedMBeans();

        } else {
            return (Set<String>) mbServerConn.invoke(new ObjectName(PATH),
                    "getTrackedMBeans", new Object[] {}, new String[] {});
        }
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
        if (Protocol == 1) {

            mrc.removeMBeanForSnapshot(MBean);

        } else {
            mbServerConn
                    .invoke(new ObjectName(PATH), "removeMBeanForSnapshot",
                            new Object[] { MBean },
                            new String[] { "java.lang.String" });

        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public boolean startTrackingMbean(String MBean) throws Exception {
        if (Protocol == 1) {

            mrc.addMBeanForSnapshot(MBean);

        } else {
            mbServerConn
                    .invoke(new ObjectName(PATH), "addMBeanForSnapshot",
                            new Object[] { MBean },
                            new String[] { "java.lang.String" });
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public HashMap<String, Long> getStats(String MBean) throws Exception {
        if (Protocol == 1) {

            return mrc.getStats(MBean);

        } else {
            return (HashMap<String, Long>) mbServerConn.invoke(new ObjectName(
                    PATH), "getStats", new Object[] { MBean },
                    new String[] { "java.lang.String" });
        }
    }

    public void setSnapshotDuration(long duration) {
        if (Protocol == 1) {

            mrc.setSnapshotDuration(new Long(duration));

        } else {
            try {

                mbServerConn.invoke(new ObjectName(PATH),
                        "setSnapshotDuration",
                        new Object[] { new Long(duration) },
                        new String[] { "java.lang.Long" });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public int getSnapshotRetention() {
        if (Protocol == 1) {

            return Integer.parseInt(mrc.getSnapshotRetention());

        } else {
            try {
                return (Integer) mbServerConn.invoke(new ObjectName(PATH),
                        "getSnapshotRetention", new Object[] {},
                        new String[] {});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public void setSnapshotRetention(int duration) {
        if (Protocol == 1) {

            mrc.setSnapshotRetention(duration);

        } else {
            try {
                mbServerConn.invoke(new ObjectName(PATH),
                        "setSnapshotRetention", new Object[] { duration },
                        new String[] { "java.lang.Integer" });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
