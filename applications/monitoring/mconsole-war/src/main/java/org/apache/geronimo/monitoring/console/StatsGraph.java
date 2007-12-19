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

    public StatsGraph(
                        Integer graph_id, 
                        String graphName,
                        String description,
                        String xAxisLabel,
                        String yAxisLabel,
                        char data1operation,
                        ArrayList<Object> dataSet1, 
                        String operation, 
                        char data2operation,
                        ArrayList<Object> dataSet2,
                        ArrayList<Object> snapshotTimes,
                        int snapshotDuration,
                        int timeFrame, 
                        String hexColor,
                        float warninglevel1,    
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

        DivDefine = "#" + DivName + "\n" + "{\n" + "margin: 0px;\n"
                + "width: 670px;\n" + "height: 240px;\n" + "}";

        DivImplement = "<div id=\"" + DivName + "\"></div>" + "\n";

        GraphJS = "var " + "graph" + graph_id + "Data = \n" + "[\n";
        if (data1operation == 'D' && data2operation == 'D') {
            for (int i = 1; i < dataSet1.size(); i++) {
                if (((Long) dataSet1.get(i) - (Long) dataSet1.get(i - 1)) < 0) {
                    dataSet1.set(i - 1, dataSet1.get(i));
                    GraphJS = GraphJS
                            + " { index: "
                            + (i)
                            + ", value: Math.round(("
                            + ((Long) dataSet1.get(i) - (Long) dataSet1
                                    .get(i - 1));
                    // ensure there is not a division by 0
                    GraphJS += appendOperation(operation, (Long) dataSet2.get(i) - (Long) dataSet2.get(i - 1));
                    GraphJS += ")*10)/10 },\n";
                }
            }
        }
        if (data1operation == 'D' && data2operation != 'D')
            for (int i = 1; i < dataSet1.size(); i++) {
                if (((Long) dataSet1.get(i) - (Long) dataSet1.get(i - 1)) < 0)
                    dataSet1.set(i - 1, dataSet1.get(i));
                GraphJS = GraphJS + " { index: " + (i)
                        + ", value: Math.round(("
                        + ((Long) dataSet1.get(i) - (Long) dataSet1.get(i - 1));
                // ensure there is not a division by 0
                GraphJS += appendOperation(operation, (Long) dataSet2.get(i));
                GraphJS += ")*10)/10 },\n";
            }
        if (data1operation != 'D' && data2operation == 'D')
            for (int i = 1; i < dataSet1.size(); i++) {
                if (((Long) dataSet1.get(i) - (Long) dataSet1.get(i - 1)) < 0)
                    dataSet1.set(i - 1, dataSet1.get(i));
                GraphJS = GraphJS + " { index: " + (i)
                        + ", value: Math.round((" + (dataSet1.get(i));
                // ensure there is not a division by 0
                GraphJS += appendOperation(operation, (Long) dataSet2.get(i) - (Long) dataSet2.get(i - 1));
                GraphJS += ")*10)/10 },\n";
            }
        if (data1operation != 'D' && data2operation != 'D')
            for (int i = 1; i < dataSet1.size(); i++) {
                if (((Long) dataSet1.get(i) - (Long) dataSet1.get(i - 1)) < 0) {
                    dataSet1.set(i - 1, dataSet1.get(i));
                }
                GraphJS = GraphJS + " { index: " + (i)
                        + ", value: Math.round((" + (dataSet1.get(i));
                // ensure there is not a division by 0
                GraphJS += appendOperation(operation, (Long) dataSet2.get(i));
                GraphJS += ")*10)/10 },\n";
            }

        GraphJS = GraphJS + "];\n";

        GraphJS = GraphJS + "var " + "graph" + graph_id
                + "Store = new dojo.collections.Store();\n";
        GraphJS = GraphJS + "graph" + graph_id + "Store.setData(" + "graph"
                + graph_id + "Data);\n";
        GraphJS = GraphJS + "graph" + graph_id + "Max = 0;\n" + "graph"
                + graph_id + "Min = 0;\n" + "graph" + graph_id + "Avg = 0;\n"
                + "for (var i = 0; i<" + "graph" + graph_id
                + "Data.length; i++)\n" + "{\n" + "graph" + graph_id
                + "Max = Math.max(" + "graph" + graph_id + "Max," + "graph"
                + graph_id + "Data[i].value);\n" + "graph" + graph_id
                + "Min = Math.min(" + "graph" + graph_id + "Min," + "graph"
                + graph_id + "Data[i].value);\n" + "graph" + graph_id
                + "Avg = (" + "graph" + graph_id + "Avg + " + "graph"
                + graph_id + "Data[i].value);\n" + "}\n" + "graph" + graph_id
                + "Avg = Math.round(" + "graph" + graph_id + "Avg/" + "graph"
                + graph_id + "Data.length*10)/10;\n" + "if (" + "graph"
                + graph_id + "Max == 0)\n" + "graph" + graph_id + "Max = 1;\n";

        // Setup the data series
        GraphJS = GraphJS + "var " + "graph" + graph_id
                + "Series = new dojo.charting.Series({\n";
        GraphJS = GraphJS + "dataSource: " + "graph" + graph_id + "Store,\n";
        GraphJS = GraphJS + "bindings: { x: \"index\", y: \"value\" },\n";
        GraphJS = GraphJS + "label: \"" + "graph" + graph_id + "\"\n";
        GraphJS = GraphJS + "});\n";

        // Define the x-axis
        GraphJS = GraphJS + "var " + "graph" + graph_id
                + "xAxis = new dojo.charting.Axis(); \n";

        // Set the upper and lower data range valuesprettyName
        GraphJS = GraphJS + "graph" + graph_id + "xAxis.range = { lower: "
                + "graph" + graph_id + "Data[0].index, upper: " + "graph"
                + graph_id + "Data[" + "graph" + graph_id
                + "Data.length-1].index };\n";

        GraphJS = GraphJS + "graph" + graph_id + "xAxis.origin = \"" + "graph"
                + graph_id + "Max\";\n";
        GraphJS = GraphJS + "graph" + graph_id + "xAxis.showTicks = true;\n";
        GraphJS = GraphJS + "graph" + graph_id + "xAxis.label = \""
                + xAxisLabel + "\";\n";

        // Setup the x tick marks on the chart
        GraphJS = GraphJS + "graph" + graph_id + "xAxis.labels = [ \n";
        // timeFrame = ((int) ((Long)snapshotTimes.get(0) - (Long)snapshotTimes
        // .get(snapshotTimes.size() - 1)) / 60000);
        Format formatter = new SimpleDateFormat("HH:mm");
        if ((timeFrame / 1440) > 7)
            formatter = new SimpleDateFormat("M/d");
        else {
            if ((timeFrame / 60) > 24)
                formatter = new SimpleDateFormat("E a");
            else {
                // if (timeFrame > 60)
                // formatter = new SimpleDateFormat("HH:mm");
                // else
                formatter = new SimpleDateFormat("HH:mm");
            }
        }

        for (int i = 1; i < dataSet1.size(); i++) {
            Date date = new Date((Long) snapshotTimes.get(i));
            // System.out.println("StatsGraph Says: Time object "+i+" is
            // "+snapshotTimes.get(i));
            // System.out.println("StatsGraph Says: Time object "+i+" is
            // "+formatter.format(date));
            GraphJS = GraphJS + "{ label: '" + formatter.format(date)
                    + "', value: " + (i) + " }, \n";
        }
        GraphJS = GraphJS + "];\n";
        // Define the y-axis
        GraphJS = GraphJS + "var " + "graph" + graph_id
                + "yAxis = new dojo.charting.Axis();\n";
        GraphJS = GraphJS + "graph" + graph_id + "yAxis.range = { lower: "
                + "graph" + graph_id + "Min, upper: " + "graph" + graph_id
                + "Max+(0.1*" + "graph" + graph_id + "Max)};\n";
        GraphJS = GraphJS + "graph" + graph_id + "yAxis.showLines = true;\n";
        GraphJS = GraphJS + "graph" + graph_id + "yAxis.showTicks = true;\n";
        GraphJS = GraphJS + "graph" + graph_id + "yAxis.label = \""
                + yAxisLabel + "\";\n";

        // Setup the y tick marks on the chart
        GraphJS = GraphJS + "graph" + graph_id + "yAxis.labels = [ \n";
        GraphJS = GraphJS + "{ label: \"min - \"+" + "graph" + graph_id
                + "Min, value: " + "graph" + graph_id + "Min },\n";
        GraphJS = GraphJS + "{ label: \"avg - \"+" + "graph" + graph_id
                + "Avg, value: " + "graph" + graph_id + "Avg },\n";
        GraphJS = GraphJS + "{ label: \"max - \"+" + "graph" + graph_id
                + "Max, value: " + "graph" + graph_id + "Max },\n";
        GraphJS = GraphJS + "{ label: Math.round((" + "graph" + graph_id
                + "Max+(0.1*" + "graph" + graph_id + "Max))), value: "
                + "graph" + graph_id + "Max+(0.1*" + "graph" + graph_id
                + "Max) },\n";
        GraphJS = GraphJS + "];  \n";

        // Create the actual graph with the x and y axes defined above
        GraphJS = GraphJS + "var " + "graph" + graph_id
                + "chartPlotArea = new dojo.charting.PlotArea();\n";
        GraphJS = GraphJS + "var " + "graph" + graph_id
                + "chartPlot = new dojo.charting.Plot(" + "graph" + graph_id
                + "xAxis, " + "graph" + graph_id + "yAxis);\n";
        GraphJS = GraphJS + "graph" + graph_id
                + "chartPlotArea.initializePlot(" + "graph" + graph_id
                + "chartPlot);\n";
        // graphOutput.add(graphName+"xAxis.initializeLabels();");
        // graphOutput.add(graphName+"xAxis.renderLabels("+graphName+"chartPlotArea,
        // "+graphName+"chartPlot, '200', '10', 'LABEL');");

        // Add the time series to the graph. The plotter will be a curved
        // area graph.
        // Other available plotters are:
        // Bar, HorizontalBar, Gantt, StackedArea, StackedCurvedArea,
        // HighLow, HighLowClose, HighLowOpenClose, Bubble,
        // DataBar, Line, CurvedLine, Area, CurvedArea, Scatter

        GraphJS = GraphJS + "graph" + graph_id + "chartPlot.addSeries({ \n";
        GraphJS = GraphJS + "data: " + "graph" + graph_id + "Series,\n";
        GraphJS = GraphJS + "plotter: dojo.charting.Plotters.CurvedArea\n";
        GraphJS = GraphJS + "});\n";

        // Define the plot area

        GraphJS = GraphJS + "graph" + graph_id
                + "chartPlotArea.size = { width: 650, height: 200 };\n";
        GraphJS = GraphJS
                + "graph"
                + graph_id
                + "chartPlotArea.padding = { top: 10, right: 20, bottom: 30, left: 80 };\n";

        // Add the plot to the area
        GraphJS = GraphJS + "graph" + graph_id + "chartPlotArea.plots.push("
                + "graph" + graph_id + "chartPlot);\n";
        // Simply use the next available color when plotting the time series
        // plot
        GraphJS = GraphJS + "graph" + graph_id + "Series.color = '#" + hexColor
                + "';\n";

        // Create the actual chart "canvas"
        GraphJS = GraphJS + "var " + "graph" + graph_id
                + "chart = new dojo.charting.Chart(null, \"" + "graph"
                + graph_id
                + "\", \"This is the example chart description\");\n";

        // Add the plot area at an offset of 10 pixels from the top left
        GraphJS = GraphJS + "graph" + graph_id + "chart.addPlotArea({ x: "
                + dataSet1.size() + ", y: " + dataSet1.size() + ", plotArea: "
                + "graph" + graph_id + "chartPlotArea });\n";

        // Setup the chart to be added to the DOM on load
        GraphJS = GraphJS + "dojo.addOnLoad(function()\n{\n" + "graph"
                + graph_id + "chart.node = dojo.byId(\"" + DivName + "\");";

        GraphJS = GraphJS + "graph" + graph_id + "chart.render();\n});\n";

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

        DivDefine = "#" + DivName + "\n" + "{\n" + "margin: 0px;\n"
                + "width: 670px;\n" + "height: 240px;\n" + "}";

        DivImplement = "<div id=\"" + DivName + "\"></div>" + "\n";

        GraphJS = "var " + "graph" + graph_id + "Data = \n" + "[\n";
        if (data1operation == 'D')
            for (int i = 1; i < dataSet1.size(); i++) {
                if (((Long) dataSet1.get(i) - (Long) dataSet1.get(i - 1)) < 0)
                    dataSet1.set(i - 1, dataSet1.get(i));
                GraphJS = GraphJS + " { index: " + (i)
                        + ", value: Math.round(("
                        + ((Long) dataSet1.get(i) - (Long) dataSet1.get(i - 1))
                        + operation + ")*10)/10 },\n";
            }
        if (data1operation != 'D')
            for (int i = 1; i < dataSet1.size(); i++) {
                if (((Long) dataSet1.get(i) - (Long) dataSet1.get(i - 1)) < 0)
                    dataSet1.set(i - 1, dataSet1.get(i));
                GraphJS = GraphJS + " { index: " + (i)
                        + ", value: Math.round((" + (dataSet1.get(i))
                        + operation + ")*10)/10 },\n";
            }

        GraphJS = GraphJS + "];\n";

        GraphJS = GraphJS + "var " + "graph" + graph_id
                + "Store = new dojo.collections.Store();\n";
        GraphJS = GraphJS + "graph" + graph_id + "Store.setData(" + "graph"
                + graph_id + "Data);\n";
        GraphJS = GraphJS + "graph" + graph_id + "Max = 0;\n" + "graph"
                + graph_id + "Min = 0;\n" + "graph" + graph_id + "Avg = 0;\n"
                + "for (var i = 0; i<" + "graph" + graph_id
                + "Data.length; i++)\n" + "{\n" + "graph" + graph_id
                + "Max = Math.max(" + "graph" + graph_id + "Max," + "graph"
                + graph_id + "Data[i].value);\n" + "graph" + graph_id
                + "Min = Math.min(" + "graph" + graph_id + "Min," + "graph"
                + graph_id + "Data[i].value);\n" + "graph" + graph_id
                + "Avg = (" + "graph" + graph_id + "Avg + " + "graph"
                + graph_id + "Data[i].value);\n" + "}\n" + "graph" + graph_id
                + "Avg = Math.round(" + "graph" + graph_id + "Avg/" + "graph"
                + graph_id + "Data.length*10)/10;\n" + "if (" + "graph"
                + graph_id + "Max == 0)\n" + "graph" + graph_id + "Max = 1;\n";

        // Setup the data series
        GraphJS = GraphJS + "var " + "graph" + graph_id
                + "Series = new dojo.charting.Series({\n";
        GraphJS = GraphJS + "dataSource: " + "graph" + graph_id + "Store,\n";
        GraphJS = GraphJS + "bindings: { x: \"index\", y: \"value\" },\n";
        GraphJS = GraphJS + "label: \"" + "graph" + graph_id + "\"\n";
        GraphJS = GraphJS + "});\n";

        // Define the x-axis
        GraphJS = GraphJS + "var " + "graph" + graph_id
                + "xAxis = new dojo.charting.Axis(); \n";

        // Set the upper and lower data range valuesprettyName
        GraphJS = GraphJS + "graph" + graph_id + "xAxis.range = { lower: "
                + "graph" + graph_id + "Data[0].index, upper: " + "graph"
                + graph_id + "Data[" + "graph" + graph_id
                + "Data.length-1].index };\n";

        GraphJS = GraphJS + "graph" + graph_id + "xAxis.origin = \"" + "graph"
                + graph_id + "Max\";\n";
        GraphJS = GraphJS + "graph" + graph_id + "xAxis.showTicks = true;\n";
        GraphJS = GraphJS + "graph" + graph_id + "xAxis.label = \""
                + xAxisLabel + "\";\n";

        // Setup the x tick marks on the chart
        GraphJS = GraphJS + "graph" + graph_id + "xAxis.labels = [ \n";
        // timeFrame = ((int) ((Long)snapshotTimes.get(0) - (Long)snapshotTimes
        // .get(snapshotTimes.size() - 1)) / 60000);
        Format formatter = new SimpleDateFormat("HH:mm");
        if ((timeFrame / 1440) > 7)
            formatter = new SimpleDateFormat("M/d");
        else {
            if ((timeFrame / 60) > 24)
                formatter = new SimpleDateFormat("E a");
            else {
                // if (timeFrame > 60)
                // formatter = new SimpleDateFormat("HH:mm");
                // else
                formatter = new SimpleDateFormat("HH:mm");
            }
        }

        for (int i = 1; i < dataSet1.size(); i++) {
            Date date = new Date((Long) snapshotTimes.get(i));
            // System.out.println("StatsGraph Says: Time object "+i+" is
            // "+snapshotTimes.get(i));
            // System.out.println("StatsGraph Says: Time object "+i+" is
            // "+formatter.format(date));
            GraphJS = GraphJS + "{ label: '" + formatter.format(date)
                    + "', value: " + (i) + " }, \n";
        }
        GraphJS = GraphJS + "];\n";
        // Define the y-axis
        GraphJS = GraphJS + "var " + "graph" + graph_id
                + "yAxis = new dojo.charting.Axis();\n";
        GraphJS = GraphJS + "graph" + graph_id + "yAxis.range = { lower: "
                + "graph" + graph_id + "Min, upper: " + "graph" + graph_id
                + "Max+(0.1*" + "graph" + graph_id + "Max)};\n";
        GraphJS = GraphJS + "graph" + graph_id + "yAxis.showLines = true;\n";
        GraphJS = GraphJS + "graph" + graph_id + "yAxis.showTicks = true;\n";
        GraphJS = GraphJS + "graph" + graph_id + "yAxis.label = \""
                + yAxisLabel + "\";\n";

        // Setup the y tick marks on the chart
        GraphJS = GraphJS + "graph" + graph_id + "yAxis.labels = [ \n";
        GraphJS = GraphJS + "{ label: \"min - \"+" + "graph" + graph_id
                + "Min, value: " + "graph" + graph_id + "Min },\n";
        GraphJS = GraphJS + "{ label: \"avg - \"+" + "graph" + graph_id
                + "Avg, value: " + "graph" + graph_id + "Avg },\n";
        GraphJS = GraphJS + "{ label: \"max - \"+" + "graph" + graph_id
                + "Max, value: " + "graph" + graph_id + "Max },\n";
        GraphJS = GraphJS + "{ label: Math.round((" + "graph" + graph_id
                + "Max+(0.1*" + "graph" + graph_id + "Max))), value: "
                + "graph" + graph_id + "Max+(0.1*" + "graph" + graph_id
                + "Max) },\n";
        GraphJS = GraphJS + "];  \n";

        // Create the actual graph with the x and y axes defined above
        GraphJS = GraphJS + "var " + "graph" + graph_id
                + "chartPlotArea = new dojo.charting.PlotArea();\n";
        GraphJS = GraphJS + "var " + "graph" + graph_id
                + "chartPlot = new dojo.charting.Plot(" + "graph" + graph_id
                + "xAxis, " + "graph" + graph_id + "yAxis);\n";
        GraphJS = GraphJS + "graph" + graph_id
                + "chartPlotArea.initializePlot(" + "graph" + graph_id
                + "chartPlot);\n";
        // graphOutput.add(graphName+"xAxis.initializeLabels();");
        // graphOutput.add(graphName+"xAxis.renderLabels("+graphName+"chartPlotArea,
        // "+graphName+"chartPlot, '200', '10', 'LABEL');");

        // Add the time series to the graph. The plotter will be a curved
        // area graph.
        // Other available plotters are:
        // Bar, HorizontalBar, Gantt, StackedArea, StackedCurvedArea,
        // HighLow, HighLowClose, HighLowOpenClose, Bubble,
        // DataBar, Line, CurvedLine, Area, CurvedArea, Scatter

        GraphJS = GraphJS + "graph" + graph_id + "chartPlot.addSeries({ \n";
        GraphJS = GraphJS + "data: " + "graph" + graph_id + "Series,\n";
        GraphJS = GraphJS + "plotter: dojo.charting.Plotters.CurvedArea\n";
        GraphJS = GraphJS + "});\n";

        // Define the plot area

        GraphJS = GraphJS + "graph" + graph_id
                + "chartPlotArea.size = { width: 650, height: 200 };\n";
        GraphJS = GraphJS
                + "graph"
                + graph_id
                + "chartPlotArea.padding = { top: 10, right: 20, bottom: 30, left: 80 };\n";

        // Add the plot to the area
        GraphJS = GraphJS + "graph" + graph_id + "chartPlotArea.plots.push("
                + "graph" + graph_id + "chartPlot);\n";
        // Simply use the next available color when plotting the time series
        // plot
        GraphJS = GraphJS + "graph" + graph_id + "Series.color = '#" + hexColor
                + "';\n";

        // Create the actual chart "canvas"
        GraphJS = GraphJS + "var " + "graph" + graph_id
                + "chart = new dojo.charting.Chart(null, \"" + "graph"
                + graph_id
                + "\", \"This is the example chart description\");\n";

        // Add the plot area at an offset of 10 pixels from the top left
        GraphJS = GraphJS + "graph" + graph_id + "chart.addPlotArea({ x: "
                + dataSet1.size() + ", y: " + dataSet1.size() + ", plotArea: "
                + "graph" + graph_id + "chartPlotArea });\n";

        // Setup the chart to be added to the DOM on load
        GraphJS = GraphJS + "dojo.addOnLoad(function()\n{\n" + "graph"
                + graph_id + "chart.node = dojo.byId(\"" + DivName + "\");";

        GraphJS = GraphJS + "graph" + graph_id + "chart.render();\n});\n";

    }

    private String appendOperation(String operation, Long number) {
        String retval = "";
        // ensure there is not a division by 0
        if (operation.equals("/") && number == 0) {
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
