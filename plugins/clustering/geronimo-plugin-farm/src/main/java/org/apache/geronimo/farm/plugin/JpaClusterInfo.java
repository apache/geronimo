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

package org.apache.geronimo.farm.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

import org.apache.geronimo.farm.config.ClusterInfo;
import org.apache.geronimo.farm.config.NodeInfo;

/**
 * @version $Rev:$ $Date:$
 */
@Entity(name = "cluster")
@NamedQuery(name = "clusterByName", query = "SELECT a FROM cluster a WHERE a.name = :name")

public class JpaClusterInfo implements ClusterInfo {

    @Id
    private String name;
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE},
            fetch = FetchType.EAGER,
            mappedBy = "cluster")
    private List<JpaNodeInfo> nodes = new ArrayList<JpaNodeInfo>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            mappedBy = "clusters",
            fetch = FetchType.EAGER)
    private List<JpaPluginList> pluginLists = new ArrayList<JpaPluginList>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<NodeInfo> getNodeInfos() {
        return new ArrayList<NodeInfo>(nodes);
    }

    public List<JpaPluginList> getPluginLists() {
        return pluginLists;
    }

    public List<JpaNodeInfo> getJpaNodeInfos() {
        return nodes;
    }
}
