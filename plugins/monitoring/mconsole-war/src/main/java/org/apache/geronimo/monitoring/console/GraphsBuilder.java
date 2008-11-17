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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;

import org.apache.geronimo.monitoring.console.util.DBManager;
import org.apache.geronimo.monitoring.console.data.Graph;

public class GraphsBuilder {

    // constructor
    public GraphsBuilder() {
    }

    public StatsGraph buildOneDB(int graph_id) throws Exception {
        Connection con = (new DBManager()).getConnection();
        StatsGraph statsGraph = null;
        PreparedStatement pStmt;
        ResultSet rsServer;
        pStmt = con
                .prepareStatement("SELECT * from graphs WHERE enabled=1 AND graph_id="
                        + graph_id);
        ResultSet rs = pStmt.executeQuery();
        if (rs.next()) {
            String mBeanName = rs.getString("mbean");
            String dataName1 = rs.getString("dataname1");
            String dataName2 = rs.getString("dataname2");
            String graphName1 = rs.getString("dataname1");
            String graphName2 = rs.getString("dataname2");
            int timeFrame = rs.getInt("timeframe");
            String server_id = rs.getString("server_id");
            String xlabel = rs.getString("xlabel");
            String ylabel = rs.getString("ylabel");
            String data1operation = rs.getString("data1operation");
            String data2operation = rs.getString("data2operation");
            String operation = rs.getString("operation");
            String color = rs.getString("color");
            float warninglevel1 = rs.getFloat("warninglevel1");
            String description = rs.getString("description");
            boolean showArchive = rs.getInt("archive") == 1;

            Graph graph = new Graph();
            graph.setId(graph_id);
            graph.setDescription(description);
            graph.setXlabel(xlabel);
            graph.setYlabel(ylabel);
            graph.setData1operation(data1operation.charAt(0));
            graph.setOperation(operation);
            graph.setData2operation(data2operation.charAt(0));
            graph.setTimeFrame(timeFrame);
            graph.setColor(color);
            graph.setWarninglevel1(warninglevel1);

            pStmt = con
                    .prepareStatement("SELECT * from servers WHERE enabled=1 AND server_id="
                            + server_id);
            rsServer = pStmt.executeQuery();
            if (rsServer.next()) {
                String ip = rsServer.getString("ip");
                String username = rsServer.getString("username");
                String password = rsServer.getString("password");
                int port = rsServer.getInt("port");
                int protocol = rsServer.getInt("protocol");
                // close the connection before calling the MRCConnector because
                // it opens another
                // connection to the db to update the SERVERS.last_seen
                // attribute
                    con.close();
                MRCConnector mrc = new MRCConnector(ip, username, password, port, protocol);
                HashMap<String, ArrayList<Long>> dataList = new HashMap<String, ArrayList<Long>>();

                dataList.put(graphName1, new ArrayList<Long>());
                if ((dataName2 != null) && !dataName2.equals("time")
                        && !dataName2.equals("null") && !dataName2.equals("")) {
                    dataList.put(graphName2, new ArrayList<Long>());
                }
                int snapCount;
                if ((timeFrame / 1440 >= 30))
                    snapCount = 17;
                else {
                    if ((timeFrame / 1440) == 7) {
                        snapCount = 16;
                    } else if ((int) ((timeFrame / (mrc.getSnapshotDuration() / 60000))) <= 12)
                        snapCount = (int) ((timeFrame / (mrc
                                .getSnapshotDuration() / 60000)));
                    else
                        snapCount = 12;
                }

                ArrayList<Long> snapshot_time = new ArrayList<Long>();

                ArrayList<Long> prettyTime = new ArrayList<Long>();

                String prettyTimeFrame;
                DecimalFormat fmt = new DecimalFormat("0.##");
                if (timeFrame / 60 > 24) {
                    prettyTimeFrame = fmt.format((float) (timeFrame / 1440))
                            + " day";
                } else {
                    if (timeFrame > 60) {
                        prettyTimeFrame = fmt.format((float) timeFrame / 60)
                                + " hour";
                    } else {
                        prettyTimeFrame = fmt.format(timeFrame) + " minute";
                    }
                }

                int skipCount = (int) ((timeFrame / (mrc.getSnapshotDuration() / 60000)))
                        / (snapCount);

                snapCount = snapCount + 2;
                TreeMap<Long, Long> snapshotList1 = mrc
                        .getSpecificStatistics(mBeanName, dataName1, snapCount,
                                skipCount, showArchive);
                TreeMap<Long, Long> snapshotList2 = new TreeMap<Long, Long>();
                if ((dataName2 != null) && !dataName2.equals("time")
                        && !dataName2.equals("null") && !dataName2.equals("")) {
                    snapshotList2 = mrc.getSpecificStatistics(mBeanName,
                            dataName2, snapCount, skipCount, showArchive);
                }
                // Check if snapshotList is empty
                if (snapshotList1.size() == 0) {
                    snapshotList1.put(System.currentTimeMillis(), (long) 0);
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
                        snapshotList1
                                .put(
                                        (timeFix - (mrc.getSnapshotDuration() * skipCount)),
                                        (long) 0);
                    }
                }
                if (snapshotList2.size() == 0) {
                    snapshotList2.put(System.currentTimeMillis(), (long) 0);
                    while (snapshotList2.size() < snapCount) {
                        // Temporary, always is first element (oldest)
                        Long timeFix = snapshotList2.firstKey();
                        snapshotList2
                                .put(
                                        (timeFix - (mrc.getSnapshotDuration() * skipCount)),
                                        (long) 0);
                    }
                }

                for (Long current : snapshotList1.keySet()) {
                    snapshot_time.add(current);
                    ArrayList<Long> ArrayListTemp = dataList.get(graphName1);
                    ArrayListTemp.add(snapshotList1.get(current));
                    if ((dataName2 != null) && !dataName2.equals("time")
                            && !dataName2.equals("null")
                            && !dataName2.equals("")) {
                        ArrayList<Long> ArrayListTemp2 = dataList.get(graphName2);
                        ArrayListTemp2.add(snapshotList2.get(current));
                    }
                    prettyTime.add(current / 1000);
                }

                if (dataName2.equals("time")) {
                    statsGraph = new StatsGraph(graph,
                            ip + " - " + xlabel + " - " + prettyTimeFrame,
                            dataList.get(graphName1),
                            prettyTime,
                            snapshot_time,
                            (int) (mrc.getSnapshotDuration() / 1000)
                    );
                } else if (!dataName2.equals("time") && (dataName2 != null)
                        && !dataName2.equals("null") && !dataName2.equals("")) {
                    statsGraph = new StatsGraph(graph,
                            ip + " - " + xlabel + " - " + prettyTimeFrame,
                            dataList.get(graphName1),
                            dataList.get(graphName2),
                            snapshot_time,
                            (int) (mrc.getSnapshotDuration() / 1000)
                    );
                } else if (dataName2 == null || dataName2.equals("null")
                        || dataName2.equals("")) {
                    statsGraph = new StatsGraph(graph,
                            ip + " - " + xlabel + " - " + prettyTimeFrame,
                            dataList.get(graphName1),
                            snapshot_time,
                            (int) (mrc.getSnapshotDuration() / 1000)
                    );
                } else {
                    System.out.println("Using Null call.");
                    statsGraph = new StatsGraph();
                }
            }
        }

        // check to see if graph was successfully populated
        if (statsGraph != null) {
            // get the current date
            Format formatter;
            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date(System.currentTimeMillis());
            String currentTime = formatter.format(date);
            // the graph was successfully operated on,
            // so update the last_seen attribute
            DBManager dbManager = new DBManager();
            Connection conn = dbManager.getConnection();
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("UPDATE GRAPHS SET LAST_SEEN='" + currentTime
                    + "' WHERE GRAPH_ID=" + graph_id);
            conn.close();
        }
        return statsGraph;
    }
}
