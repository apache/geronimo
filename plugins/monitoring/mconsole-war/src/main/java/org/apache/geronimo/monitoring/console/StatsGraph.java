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

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.geronimo.monitoring.console.data.Graph;

public class StatsGraph {

    private Graph graph;

    private String GraphName;
    private int SnapshotDuration;
    private int PointCount;
    private String GraphJS;

    public StatsGraph(Graph graph,
                      String graphName,
                      List<Long> dataSet1,
                      List<Long> dataSet2,
                      List<Long> snapshotTimes,
                      int snapshotDuration
    ) {
        this.graph = graph;

        GraphName = graphName;
        SnapshotDuration = snapshotDuration;
        PointCount = dataSet1.size();

        String dataDisplay = displayData(graph.getData1operation(), dataSet1, graph.getOperation(), graph.getData2operation(), dataSet2);

        this.GraphJS = buildJavaScript(dataSet1, snapshotTimes, dataDisplay);
    }

    private String buildJavaScript(List<Long> dataSet1, List<Long> snapshotTimes, String dataDisplay) {
        String GraphJS = "var " + "graph" + graph.getId()
                + " = new dojox.charting.Chart2D(\"" + getDivName() + "\");\n"
                + "graph" + graph.getId()
                + ".addPlot(\"default\", {type: \"Areas\", tension:3});\n" + "graph"
                + graph.getId() + ".setTheme(dojox.charting.themes.PlotKit.blue);\n";

        // Setup the x tick marks on the chart
        Format formatter;
        if ((graph.getTimeFrame() / 1440) > 7)
            formatter = new SimpleDateFormat("M/d");
        else {
            if ((graph.getTimeFrame() / 60) > 24)
                formatter = new SimpleDateFormat("E a");
            else {
                formatter = new SimpleDateFormat("HH:mm");
            }
        }
        GraphJS += "graph" + graph.getId() + ".addAxis(\"x\", {labels: [";
        for (int i = 1; i < dataSet1.size(); i++) {
            Date date = new Date(snapshotTimes.get(i));
            GraphJS += "{value: " + (i) + ", text: '" + formatter.format(date);
            if ((i+1) != dataSet1.size())
                GraphJS += "' }, \n";
            else
            	GraphJS += "' } \n";
        }
        GraphJS += "]});\n";
        GraphJS += "graph" + graph.getId() + ".addAxis(\"y\", {vertical: true});\n";

        GraphJS += "graph" + graph.getId() + ".addSeries(\"Series" + graph.getId()
                + "\", [";
        GraphJS = GraphJS + dataDisplay;

        GraphJS = GraphJS + "]);\n";

        GraphJS = GraphJS + "graph" + graph.getId() + ".render();\n";
        return GraphJS;
    }

    private String displayData(char data1operation, List<Long> dataSet1, String operation, char data2operation, List<Long> dataSet2) {
        String graphJS = "";
        if (data1operation == 'D' && data2operation == 'D') {
            for (int i = 1; i < dataSet1.size(); i++) {
                if ((dataSet1.get(i) - dataSet1.get(i - 1)) < 0)
                    dataSet1.set(i - 1, dataSet1.get(i));
                graphJS = graphJS
                        + (dataSet1.get(i) - dataSet1.get(i - 1));
                // ensure there is not a division by 0
                graphJS += appendOperation(operation, dataSet2.get(i)
                        - dataSet2.get(i - 1));
                if ((i+1) != dataSet1.size())
                    graphJS += ",";
            }
        }
        if (data1operation == 'D' && data2operation != 'D') {
            for (int i = 1; i < dataSet1.size(); i++) {
                graphJS = graphJS
                        + (dataSet1.get(i) - dataSet1.get(i - 1));
                // ensure there is not a division by 0
                graphJS += appendOperation(operation, dataSet2.get(i));
                if ((i+1) != dataSet1.size())
                    graphJS += ",\n";
            }
        }
        if (data1operation != 'D' && data2operation == 'D') {
            for (int i = 1; i < dataSet1.size(); i++) {
                graphJS = graphJS + dataSet1.get(i);
                // ensure there is not a division by 0
                graphJS += appendOperation(operation, dataSet2.get(i)
                        - dataSet2.get(i - 1));
                if ((i+1) != dataSet1.size())
                    graphJS += ",\n";
            }
        }
        if (data1operation != 'D' && data2operation != 'D') {
            for (int i = 1; i < dataSet1.size(); i++) {
                graphJS = graphJS + dataSet1.get(i);
                // ensure there is not a division by 0
                graphJS += appendOperation(operation, dataSet2.get(i));
                if ((i+1) != dataSet1.size())
                    graphJS += ",";
            }
        }
        return graphJS;
    }

    public StatsGraph(Graph graph,
                      String graphName,
                      List<Long> dataSet1,
                      List<Long> snapshotTimes, int snapshotDuration
    ) {

        this.graph = graph;

        GraphName = graphName;
        SnapshotDuration = snapshotDuration;
        PointCount = dataSet1.size();
        String dataDisplay = displayData(graph.getData1operation(), dataSet1, graph.getOperation());

        this.GraphJS = buildJavaScript(dataSet1, snapshotTimes, dataDisplay);
    }

    private String displayData(char data1operation, List<Long> dataSet1, String operation) {
        String graphJS = "";
        if (data1operation == 'D')
            for (int i = 1; i < dataSet1.size(); i++) {
                graphJS = graphJS
                        + (dataSet1.get(i) - dataSet1.get(i - 1)) + operation;
                if ((i+1) != dataSet1.size())
                    graphJS += ",\n";
            }
        if (data1operation != 'D')
            for (int i = 1; i < dataSet1.size(); i++) {
                graphJS = graphJS + dataSet1.get(i) + operation;
                if ((i+1) != dataSet1.size())
                    graphJS += ",\n";
            }
        return graphJS;
    }

    private String appendOperation(String operation, Long number) {
        String retval = "";
        // ensure there is not a division by 0
        if (operation.endsWith("/") && number == 0) {
            retval += "*0";
        } else {
            retval += operation + number;
        }
        return retval;
    }

    public StatsGraph() {

    }

    public void redraw() {

    }

    public String getJS() {
        return GraphJS;
    }

    public String getDivImplement() {
        return "<div id=\"" + getDivName()
                + "\" style=\"height: 220px;\"></div><br><div id='" + getDivName()
                + "Sub' style='text-align: center;'>" + graph.getYlabel() + " vs. "
                + graph.getXlabel() + "</div>" + "\n";
    }

    public String getDivName() {
        return "graph" + graph.getId() + "Container";
    }

    public String getXAxis() {
        return graph.getXlabel();
    }

    public String getYAxis() {
        return graph.getYlabel();
    }

    public String getName() {
        return GraphName;
    }

    public String getDescription() {
        return graph.getDescription();
    }

    public int getSnapshotDuration() {
        return SnapshotDuration;
    }

    public int getTimeFrame() {
        return graph.getTimeFrame();
    }

    public int getPointCount() {
        return PointCount;
    }

    public String getColor() {
        return graph.getColor();
    }
}
