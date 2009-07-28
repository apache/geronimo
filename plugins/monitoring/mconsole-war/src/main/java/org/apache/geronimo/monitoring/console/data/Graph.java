/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.monitoring.console.data;

import java.util.List;
import java.util.ArrayList;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.GeneratedValue;

/**
 * @version $Rev$ $Date$
 */
@Entity(name = "graph")
@NamedQueries(
 { 
    @NamedQuery(name = "allGraphs", query = "SELECT a FROM graph a"), 
    @NamedQuery(name = "graphById", query = "SELECT g FROM graph g WHERE g.id = :id"),
    @NamedQuery(name = "graphsByNode", query = "SELECT g FROM graph g WHERE g.node.name = :name")
 }
 )
public class Graph {

    @Id
    @GeneratedValue
    private int id;
//    private String name;
//    private String description;
//    private int timeFrame;
//    private String divDefine;
//    private String divImplement;
//    private String xAxisLabel;
//    private String yAxisLabel;
//    private int snapshotDuration;
//    private int pointCount;
//    private String hexColor;
//    private boolean enabled = true;
//    private boolean archive = false;
//
//    private int server_id;
//    private String mbeanName;
//    private char data1operation;

    private String mBeanName;
    private String dataName1;
    private String dataName2;
    private String graphName1;
    private String graphName2;
    private int timeFrame;
    private String xlabel;
    private String ylabel;
    private char data1operation = 'A';
    private char data2operation = 'A';
    private String operation;
    private String color;
    private float warninglevel1;
    private String description;
    private boolean showArchive;

//    private String server_id;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.EAGER)
    @JoinTable(name = "view_graph",
            joinColumns = {@JoinColumn(name = "graph_id")},
            inverseJoinColumns = {@JoinColumn(name = "view_id")}
    )
    private List<View> views = new ArrayList<View>();

    @ManyToOne(fetch= FetchType.EAGER, cascade={CascadeType.PERSIST})
    @JoinColumn(
        name="node", referencedColumnName="name"
    )
    private Node node;

    /**
     * + "graph_id    INTEGER PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1),"
     * + "enabled     SMALLINT NOT NULL DEFAULT 1,"
     * + "server_id   INTEGER NOT NULL DEFAULT 0,"
     * + "name        VARCHAR(128) UNIQUE NOT NULL,"
     * + "description LONG VARCHAR DEFAULT NULL,"
     * + "timeframe   INTEGER NOT NULL DEFAULT 60,"
     * + "mbean       VARCHAR(512) NOT NULL,"
     * + "data1operation  CHAR DEFAULT NULL,"
     * + "dataname1   VARCHAR(128) NOT NULL,"
     * + "operation   VARCHAR(128) DEFAULT NULL,"
     * + "data2operation  CHAR DEFAULT NULL,"
     * + "dataname2   VARCHAR(128) DEFAULT NULL,"
     * + "xlabel      VARCHAR(128) DEFAULT NULL,"
     * + "ylabel      VARCHAR(128) DEFAULT NULL,"
     * + "warninglevel1   FLOAT DEFAULT NULL,"
     * + "warninglevel2   FLOAT DEFAULT NULL,"
     * + "color       VARCHAR(6) NOT NULL DEFAULT '1176c2',"
     * + "last_js     LONG VARCHAR DEFAULT NULL,"
     * + "added       TIMESTAMP NOT NULL,"
     * + "modified    TIMESTAMP NOT NULL,"
     * + "archive     SMALLINT NOT NULL DEFAULT 0,"
     * + "last_seen   TIMESTAMP NOT NULL" + ")");
     */

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMBeanName() {
        return mBeanName;
    }

    public void setMBeanName(String mBeanName) {
        this.mBeanName = mBeanName;
    }

    public String getDataName1() {
        return dataName1;
    }

    public void setDataName1(String dataName1) {
        this.dataName1 = dataName1;
    }

    public String getDataName2() {
        return dataName2;
    }

    public void setDataName2(String dataName2) {
        this.dataName2 = dataName2;
    }

    public String getGraphName1() {
        return graphName1;
    }

    public void setGraphName1(String graphName1) {
        this.graphName1 = graphName1;
    }

    public String getGraphName2() {
        return graphName2;
    }

    public void setGraphName2(String graphName2) {
        this.graphName2 = graphName2;
    }

    public int getTimeFrame() {
        return timeFrame;
    }

    public void setTimeFrame(int timeFrame) {
        this.timeFrame = timeFrame;
    }

//    public String getServer_id() {
//        return server_id;
//    }
//
//    public void setServer_id(String server_id) {
//        this.server_id = server_id;
//    }

    public String getXlabel() {
        return xlabel;
    }

    public void setXlabel(String xlabel) {
        this.xlabel = xlabel;
    }

    public String getYlabel() {
        return ylabel;
    }

    public void setYlabel(String ylabel) {
        this.ylabel = ylabel;
    }

    public char getData1operation() {
        return data1operation;
    }

    public void setData1operation(char data1operation) {
        this.data1operation = data1operation;
    }

    public char getData2operation() {
        return data2operation;
    }

    public void setData2operation(char data2operation) {
        this.data2operation = data2operation;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public float getWarninglevel1() {
        return warninglevel1;
    }

    public void setWarninglevel1(float warninglevel1) {
        this.warninglevel1 = warninglevel1;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isShowArchive() {
        return showArchive;
    }

    public void setShowArchive(boolean showArchive) {
        this.showArchive = showArchive;
    }

    public String getIdString() {
        return "" + id;
    }

    public List<View> getViews() {
        return views;
    }

    public void setViews(List<View> views) {
        this.views = views;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }
}
