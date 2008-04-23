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
import java.util.ArrayList;
import java.util.Date;

public class StatsGraph {
    private String GraphName;
    private String DivName;
    private String Description;
    private String DivDefine;
    private String DivImplement;
    private String XAxisLabel;
    private String YAxisLabel;
    private int SnapshotDuration;
    private int TimeFrame;
    private int PointCount;
    private String HexColor;
    private String GraphJS;

    public StatsGraph(Integer graph_id, String graphName, String description,
            String xAxisLabel, String yAxisLabel, char data1operation,
            ArrayList<Object> dataSet1, String operation, char data2operation,
            ArrayList<Object> dataSet2, ArrayList<Object> snapshotTimes,
            int snapshotDuration, int timeFrame, String hexColor,
            float warninglevel1, float warninglevel2) {

        DivName = "graph" + graph_id + "Container";
        GraphName = graphName;
        Description = description;
        XAxisLabel = xAxisLabel;
        YAxisLabel = yAxisLabel;
        SnapshotDuration = snapshotDuration;
        TimeFrame = timeFrame;
        PointCount = dataSet1.size();
        HexColor = hexColor;

        DivImplement = "<div id=\"" + DivName
                + "\" style=\"height: 220px;\"></div><br><div id='" + DivName
                + "Sub' style='text-align: center;'>" + yAxisLabel + " vs. "
                + xAxisLabel + "</div>" + "\n";

        GraphJS = "var " + "graph" + graph_id
                + " = new dojox.charting.Chart2D(\"" + DivName + "\");\n"
                + "graph" + graph_id
                + ".addPlot(\"default\", {type: \"Areas\"});\n" + "graph"
                + graph_id + ".setTheme(dojox.charting.themes.PlotKit.blue);\n";

        // Setup the x tick marks on the chart
        Format formatter = new SimpleDateFormat("HH:mm");
        if ((timeFrame / 1440) > 7)
            formatter = new SimpleDateFormat("M/d");
        else {
            if ((timeFrame / 60) > 24)
                formatter = new SimpleDateFormat("E a");
            else {
                formatter = new SimpleDateFormat("HH:mm");
            }
        }
        GraphJS += "graph" + graph_id + ".addAxis(\"x\", {labels: [";
        for (int i = 1; i < dataSet1.size(); i++) {
            Date date = new Date((Long) snapshotTimes.get(i));
            GraphJS += "{value: " + (i) + ", text: '" + formatter.format(date);
            if ((i+1) != dataSet1.size())
                GraphJS += "' }, \n";
            else
            	GraphJS += "' } \n";
        }
        GraphJS += "]});\n";
        GraphJS += "graph" + graph_id + ".addAxis(\"y\", {vertical: true});\n";

        GraphJS += "graph" + graph_id + ".addSeries(\"Series" + graph_id
                + "\", [";
        if (data1operation == 'D' && data2operation == 'D') {
            for (int i = 1; i < dataSet1.size(); i++) {
                if (((Long) dataSet1.get(i) - (Long) dataSet1.get(i - 1)) < 0)
                    dataSet1.set(i - 1, dataSet1.get(i));
                GraphJS = GraphJS
                        + ((Long) dataSet1.get(i) - (Long) dataSet1.get(i - 1));
                // ensure there is not a division by 0
                GraphJS += appendOperation(operation, (Long) dataSet2.get(i)
                        - (Long) dataSet2.get(i - 1));
                if ((i+1) != dataSet1.size())
                    GraphJS += ",";
            }
        }
        if (data1operation == 'D' && data2operation != 'D') {
            for (int i = 1; i < dataSet1.size(); i++) {
                GraphJS = GraphJS
                        + ((Long) dataSet1.get(i) - (Long) dataSet1.get(i - 1));
                // ensure there is not a division by 0
                GraphJS += appendOperation(operation, (Long) dataSet2.get(i));
                if ((i+1) != dataSet1.size())
                    GraphJS += ",";
            }
        }
        if (data1operation != 'D' && data2operation == 'D') {
            for (int i = 1; i < dataSet1.size(); i++) {
                GraphJS = GraphJS + dataSet1.get(i);
                // ensure there is not a division by 0
                GraphJS += appendOperation(operation, (Long) dataSet2.get(i)
                        - (Long) dataSet2.get(i - 1));
                if ((i+1) != dataSet1.size())
                    GraphJS += ",";
            }
        }
        if (data1operation != 'D' && data2operation != 'D') {
            for (int i = 1; i < dataSet1.size(); i++) {
                GraphJS = GraphJS + dataSet1.get(i);
                // ensure there is not a division by 0
                GraphJS += appendOperation(operation, (Long) dataSet2.get(i));
                if ((i+1) != dataSet1.size())
                    GraphJS += ",";
            }
        }

        GraphJS = GraphJS + "]);\n";

        GraphJS = GraphJS + "graph" + graph_id + ".render();\n";

    }

    public StatsGraph(Integer graph_id, String graphName, String description,
            String xAxisLabel, String yAxisLabel, char data1operation,
            ArrayList<Object> dataSet1, String operation,
            ArrayList<Object> snapshotTimes, int snapshotDuration,
            int timeFrame, String hexColor, float warninglevel1,
            float warninglevel2) {

        DivName = "graph" + graph_id + "Container";
        GraphName = graphName;
        Description = description;
        XAxisLabel = xAxisLabel;
        YAxisLabel = yAxisLabel;
        SnapshotDuration = snapshotDuration;
        TimeFrame = timeFrame;
        PointCount = dataSet1.size();
        HexColor = hexColor;

        DivImplement = "<div id=\"" + DivName
                + "\" style=\"height: 220px;\"></div><br><div id='" + DivName
                + "Sub' style='text-align: center;'>" + yAxisLabel + " vs. "
                + xAxisLabel + "</div>" + "\n";

        GraphJS = "var " + "graph" + graph_id
                + " = new dojox.charting.Chart2D(\"" + DivName + "\");\n"
                + "graph" + graph_id
                + ".addPlot(\"default\", {type: \"Areas\"});\n" + "graph"
                + graph_id + ".setTheme(dojox.charting.themes.PlotKit.blue);\n";

        // Setup the x tick marks on the chart
        Format formatter = new SimpleDateFormat("HH:mm");
        if ((timeFrame / 1440) > 7)
            formatter = new SimpleDateFormat("M/d");
        else {
            if ((timeFrame / 60) > 24)
                formatter = new SimpleDateFormat("E a");
            else {
                formatter = new SimpleDateFormat("HH:mm");
            }
        }
        GraphJS += "graph" + graph_id + ".addAxis(\"x\", {labels: [";
        for (int i = 1; i < dataSet1.size(); i++) {
            Date date = new Date((Long) snapshotTimes.get(i));
            GraphJS += "{value: " + (i) + ", text: '" + formatter.format(date);
            if ((i+1) != dataSet1.size())
                GraphJS += "' }, \n";
            else
            	GraphJS += "' } \n";
        }
        GraphJS += "]});\n";
        GraphJS += "graph" + graph_id + ".addAxis(\"y\", {vertical: true});\n";

        GraphJS += "graph" + graph_id + ".addSeries(\"Series" + graph_id
                + "\", [";
        if (data1operation == 'D')
            for (int i = 1; i < dataSet1.size(); i++) {
                GraphJS = GraphJS
                        + ((Long) dataSet1.get(i) - (Long) dataSet1.get(i - 1)) + operation;
                if ((i+1) != dataSet1.size())
                    GraphJS += ",\n";
            }
        if (data1operation != 'D')
            for (int i = 1; i < dataSet1.size(); i++) {
                GraphJS = GraphJS + dataSet1.get(i) + operation;
                if ((i+1) != dataSet1.size())
                    GraphJS += ",\n";
            }

        GraphJS = GraphJS + "]);\n";

        GraphJS = GraphJS + "graph" + graph_id + ".render();\n";
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

    public String getDiv() {
        return DivDefine;
    }

    public String getDivImplement() {
        return DivImplement;
    }

    public String getDivName() {
        return DivName;
    }

    public String getXAxis() {
        return XAxisLabel;
    }

    public String getYAxis() {
        return YAxisLabel;
    }

    public String getName() {
        return GraphName;
    }

    public String getDescription() {
        return Description;
    }

    public int getSnapshotDuration() {
        return SnapshotDuration;
    }

    public int getTimeFrame() {
        return TimeFrame;
    }

    public int getPointCount() {
        return PointCount;
    }

    public String getColor() {
        return HexColor;
    }
}
