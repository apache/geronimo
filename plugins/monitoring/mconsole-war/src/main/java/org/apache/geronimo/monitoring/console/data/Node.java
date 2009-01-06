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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Column;

/**
 * @version $Rev$ $Date$
 */
@Entity(name = "node")

@NamedQueries(
  {
    @NamedQuery(name = "allNodes", query = "SELECT a FROM node a"),
    @NamedQuery(name = "nodeByName", query = "SELECT n FROM node n WHERE n.name = :name")
  }
)

public class Node {
    @Id
    private String name;
    private String userName;
    @Column(length = 1024)
    private String password;
    private String protocol;
    private String host;
    private int port = -1;
    private String urlPath;
    private boolean local;

    private boolean enabled = true;

//    @ManyToOne(fetch= FetchType.EAGER, cascade={CascadeType.PERSIST})
//    @JoinColumn(
//        name="cluster", referencedColumnName="name"
//    )
//    private JpaClusterInfo cluster;
//
//
//
//    public JpaClusterInfo getCluster() {
//        return cluster;
//    }
//
//    public void setCluster(JpaClusterInfo cluster) {
//        this.cluster = cluster;
//    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUrlPath() {
        return urlPath;
    }

    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
