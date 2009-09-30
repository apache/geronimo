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
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

public class GraphsBuilder {

    private String ip = new String();

    private int timeFrame;

    private int snapCount;

    private MRCConnector mrc = null;

    private Connection con;

    // constructor
    public GraphsBuilder(Connection con) {
        // TODO: Database pull stuff may go here... based on server ID...\
        this.con = con;
    }

    public StatsGraph buildOneDB(int graph_id) throws Exception {
        StatsGraph graph = null;
        Statement graphStmt = null;
        Statement serverStmt = null;
        Statement updateStmt = null;
        try {
            graphStmt = con.createStatement();            
            ResultSet rs = graphStmt.executeQuery("SELECT * from graphs WHERE enabled=1 AND graph_id=" + graph_id);
            if (rs.next()) {
                String mBeanName = rs.getString("mbean");
                String dataName1 = rs.getString("dataname1");
                String dataName2 = rs.getString("dataname2");
                String graphName1 = rs.getString("dataname1");
                String graphName2 = rs.getString("dataname2");
                timeFrame = rs.getInt("timeframe");
                String server_id = rs.getString("server_id");
                String xlabel = rs.getString("xlabel");
                String ylabel = rs.getString("ylabel");
                String data1operation = rs.getString("data1operation");
                String data2operation = rs.getString("data2operation");
                String operation = rs.getString("operation");
                String color = rs.getString("color");
                float warninglevel1 = rs.getFloat("warninglevel1");
                String description = rs.getString("description");
                boolean showArchive = rs.getInt("archive") == 1 ? true : false;
                serverStmt = con.createStatement();
                ResultSet rsServer = serverStmt.executeQuery("SELECT * from servers WHERE enabled=1 AND server_id=" + server_id);
                if (rsServer.next()) {
                    ip = rsServer.getString("ip");
                    String username = rsServer.getString("username");
                    String password = rsServer.getString("password");
                    int port = rsServer.getInt("port");
                    int protocol = rsServer.getInt("protocol");
                    rsServer.close();
                    mrc = new MRCConnector(ip, username, password, port, protocol);
                    HashMap<String, ArrayList<Object>> DataList = new HashMap<String, ArrayList<Object>>();
                    DataList.put(graphName1, new ArrayList<Object>());
                    if ((dataName2 != null) && !dataName2.equals("time") && !dataName2.equals("null") && !dataName2.equals("")) {
                        DataList.put(graphName2, new ArrayList<Object>());
                    }
                    if ((timeFrame / 1440 >= 30))
                        snapCount = 17;
                    else {
                        if ((timeFrame / 1440) == 7) {
                            snapCount = 16;
                        } else if ((int) ((timeFrame / (mrc.getSnapshotDuration() / 60000))) <= 12)
                            snapCount = (int) ((timeFrame / (mrc.getSnapshotDuration() / 60000)));
                        else
                            snapCount = 12;
                    }
                    ArrayList<Object> snapshot_time = new ArrayList<Object>();
                    ArrayList<Object> PrettyTime = new ArrayList<Object>();
                    String prettyTimeFrame = new String();
                    DecimalFormat fmt = new DecimalFormat("0.##");
                    if (timeFrame / 60 > 24) {
                        prettyTimeFrame = fmt.format((float) (timeFrame / 1440)) + " day";
                    } else {
                        if (timeFrame > 60) {
                            prettyTimeFrame = fmt.format((float) timeFrame / 60) + " hour";
                        } else {
                            prettyTimeFrame = fmt.format(timeFrame) + " minute";
                        }
                    }
                    int skipCount = (int) ((timeFrame / (mrc.getSnapshotDuration() / 60000))) / (snapCount);
                    snapCount = snapCount + 2;
                    TreeMap<Long, Long> snapshotList1 = mrc.getSpecificStatistics(mBeanName, dataName1, snapCount, skipCount, showArchive);
                    TreeMap<Long, Long> snapshotList2 = new TreeMap<Long, Long>();
                    if ((dataName2 != null) && !dataName2.equals("time") && !dataName2.equals("null") && !dataName2.equals("")) {
                        snapshotList2 = mrc.getSpecificStatistics(mBeanName, dataName2, snapCount, skipCount, showArchive);
                    }
                    // Check if snapshotList is empty
                    if (snapshotList1.size() == 0) {
                        snapshotList1.put(System.currentTimeMillis(), new Long(0));
                        /*
                         * If there are not enough snapshots available to fill the
                         * requested number, insert some with values of 0 and the
                         * proper times.
                         */
                        while (snapshotList1.size() < snapCount) {
                            // Temporary, always is first element (oldest)
                            Long timeFix = snapshotList1.firstKey();
                            // Calculate appropriate time, add it to the submap,
                            // then
                            // add
                            // that to the tempMap
                            snapshotList1.put((timeFix - (mrc.getSnapshotDuration() * skipCount)), new Long(0));
                        }
                    }
                    if (snapshotList2.size() == 0) {
                        snapshotList2.put(System.currentTimeMillis(), new Long(0));
                        while (snapshotList2.size() < snapCount) {
                            // Temporary, always is first element (oldest)
                            Long timeFix = snapshotList2.firstKey();
                            snapshotList2.put((timeFix - (mrc.getSnapshotDuration() * skipCount)), new Long(0));
                        }
                    }
                    for (Iterator<Long> it = snapshotList1.keySet().iterator(); it.hasNext();) {
                        Long current = it.next();
                        snapshot_time.add(current);
                        ArrayList<Object> ArrayListTemp = DataList.get(graphName1);
                        ArrayListTemp.add(snapshotList1.get(current));
                        DataList.put(graphName1, ArrayListTemp);
                        if ((dataName2 != null) && !dataName2.equals("time") && !dataName2.equals("null") && !dataName2.equals("")) {
                            ArrayList<Object> ArrayListTemp2 = DataList.get(graphName2);
                            ArrayListTemp2.add(snapshotList2.get(current));
                            DataList.put(graphName2, ArrayListTemp2);
                        }
                        PrettyTime.add((Long) current / 1000);
                    }
                    if (dataName2.equals("time")) {
                        graph = (new StatsGraph(graph_id, ip + " - " + xlabel + " - " + prettyTimeFrame, description, xlabel, ylabel, data1operation.charAt(0), DataList.get(graphName1), operation, data2operation.charAt(0), PrettyTime, snapshot_time, (int) (mrc.getSnapshotDuration() / 1000), timeFrame, color, warninglevel1, warninglevel1));
                    } else if (!dataName2.equals("time") && (dataName2 != null) && !dataName2.equals("null") && !dataName2.equals("")) {
                        graph = new StatsGraph(graph_id, ip + " - " + xlabel + " - " + prettyTimeFrame, description, xlabel, ylabel, data1operation.charAt(0), DataList.get(graphName1), operation, data2operation.charAt(0), DataList.get(graphName2), snapshot_time, (int) (mrc.getSnapshotDuration() / 1000), timeFrame, color, warninglevel1,
                                warninglevel1);
                    } else if (dataName2 == null || dataName2.equals("null") || dataName2.equals("")) {
                        graph = new StatsGraph(graph_id, ip + " - " + xlabel + " - " + prettyTimeFrame, description, xlabel, ylabel, data1operation.charAt(0), DataList.get(graphName1), operation, snapshot_time, (int) (mrc.getSnapshotDuration() / 1000), timeFrame, color, warninglevel1, warninglevel1);
                    } else {
                        graph = new StatsGraph();
                    }
                }
            }
            // check to see if graph was successfully populated
            if (graph != null) {
                // get the current date
                Format formatter = null;
                formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(System.currentTimeMillis());
                String currentTime = formatter.format(date);
                // the graph was successfully operated on,
                // so update the last_seen attribute 
                updateStmt = con.createStatement();
                updateStmt.executeUpdate("UPDATE GRAPHS SET LAST_SEEN='" + currentTime + "' WHERE GRAPH_ID=" + graph_id);                
            }
            return graph;
        } finally {
            if (updateStmt != null)
                try {
                    updateStmt.close();
                } catch (Exception e) {
                }
            if (serverStmt != null)
                try {
                    serverStmt.close();
                } catch (Exception e) {
                }
            if (graphStmt != null)
                try {
                    graphStmt.close();
                } catch (Exception e) {
                }
            if (mrc != null)
                try {
                    mrc.dispose();
                } catch (Exception e) {
                }
        }
    }
}
