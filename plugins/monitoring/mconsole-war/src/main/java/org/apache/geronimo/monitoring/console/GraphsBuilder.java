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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.geronimo.monitoring.console.data.Graph;
import org.apache.geronimo.monitoring.console.data.Node;

public class GraphsBuilder {
    private static final long ONE_MINUTE_MS = 60000;

    // constructor
    public GraphsBuilder() {
    }

    public StatsGraph getStatsGraph(Graph graph) throws Exception {
        StatsGraph statsGraph = null;
        Node node = graph.getNode();
        if (node != null) {
            String ip = node.getHost();
            String username = node.getUserName();
            String password = node.getPassword();
            int port = node.getPort();
            String protocol = node.getProtocol();
            MRCConnector mrc = new MRCConnector(ip, username, password, port, protocol);
            Long snapshotDurationMS = mrc.getSnapshotDuration();

            List<Long> dataList1 = new ArrayList<Long>();
            List<Long> dataList2 = new ArrayList<Long>();

            boolean hasSecondSeries = (graph.getDataName2() != null) && !graph.getDataName2().equals("time")
                    && !graph.getDataName2().equals("null") && !graph.getDataName2().equals("");

            int snapCount;
            int timeFrame = graph.getTimeFrame();
            if ((timeFrame / 1440 >= 30)) {
                snapCount = 17;
            } else {
                if ((timeFrame / 1440) == 7) {
                    snapCount = 16;
                } else if ((int) ((timeFrame / (snapshotDurationMS / ONE_MINUTE_MS))) <= 12)
                    snapCount = (int) ((timeFrame / (snapshotDurationMS / ONE_MINUTE_MS)));
                else
                    snapCount = 12;
            }

            List<Long> snapshot_time = new ArrayList<Long>();

            String prettyTimeFrame;
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

            int skipCount = (int) ((timeFrame / (snapshotDurationMS / ONE_MINUTE_MS))) / (snapCount);

            snapCount = snapCount + 2;
            TreeMap<Long, Long> snapshotList1 = mrc.getSpecificStatistics(graph.getMBeanName(), graph.getDataName1(), snapCount, skipCount, graph.isShowArchive());
            TreeMap<Long, Long> snapshotList2 = new TreeMap<Long, Long>();
            if (hasSecondSeries) {
                snapshotList2 = mrc.getSpecificStatistics(graph.getMBeanName(), graph.getDataName2(), snapCount, skipCount, graph.isShowArchive());
            }
            mrc.dispose();
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
                    snapshotList1.put((timeFix - (snapshotDurationMS * skipCount)), (long) 0);
                }
            }
            if (snapshotList2.size() == 0) {
                snapshotList2.put(System.currentTimeMillis(), (long) 0);
                while (snapshotList2.size() < snapCount) {
                    // Temporary, always is first element (oldest)
                    Long timeFix = snapshotList2.firstKey();
                    snapshotList2.put((timeFix - (snapshotDurationMS * skipCount)), (long) 0);
                }
            }

            for (Long current : snapshotList1.keySet()) {
                snapshot_time.add(current);
                dataList1.add(snapshotList1.get(current));
                if (hasSecondSeries) {
                    dataList2.add(snapshotList2.get(current));
                }
            }

            String prettyGraphName = ip + " - " + graph.getXlabel() + " - " + prettyTimeFrame;
            int snapshotDurationSeconds = (int) (snapshotDurationMS / 1000);
            if (hasSecondSeries) {
                statsGraph = new StatsGraph(graph,
                        prettyGraphName,
                        dataList1,
                        dataList2,
                        snapshot_time,
                        snapshotDurationSeconds
                );
            } else {
                statsGraph = new StatsGraph(graph,
                        prettyGraphName,
                        dataList1,
                        snapshot_time,
                        snapshotDurationSeconds
                );
            }
        }
        return statsGraph;
    }
}
