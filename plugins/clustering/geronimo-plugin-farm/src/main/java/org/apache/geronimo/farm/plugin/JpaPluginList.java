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
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQuery;

import org.apache.geronimo.system.plugin.model.PluginListType;
import org.apache.geronimo.system.plugin.model.PluginType;

/**
 * @version $Rev$ $Date$
 */
@Entity(name = "pluginlist")
@NamedQuery(name = "pluginListByName", query = "SELECT a FROM pluginlist a WHERE a.name = :name")
public class JpaPluginList {
    @Id
    private String name;
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.EAGER)
    private List<JpaPluginInstance> plugins = new ArrayList<JpaPluginInstance>();
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.EAGER)
    @JoinTable(name = "pluginlist_cluster",
            joinColumns = {@JoinColumn(name = "pluginlist_name")},
            inverseJoinColumns = {@JoinColumn(name = "cluster_name")}
    )
    private List<JpaClusterInfo> clusters = new ArrayList<JpaClusterInfo>();
    private String defaultPluginRepository;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<JpaPluginInstance> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<JpaPluginInstance> plugins) {
        this.plugins = plugins;
    }

    public List<JpaClusterInfo> getClusters() {
        return clusters;
    }

    public void setClusters(List<JpaClusterInfo> clusters) {
        this.clusters = clusters;
    }

    public String getDefaultPluginRepository() {
        return defaultPluginRepository;
    }

    public void setDefaultPluginRepository(String defaultPluginRepository) {
        this.defaultPluginRepository = defaultPluginRepository;
    }

    public PluginListType getPluginList() {
        PluginListType pluginList = new PluginListType();
        List<PluginType> plugins = pluginList.getPlugin();
        for (JpaPluginInstance pluginInstance : this.plugins) {
            PluginType plugin = pluginInstance.getPlugin();
            plugins.add(plugin);
        }
        if (defaultPluginRepository != null) {
            pluginList.getDefaultRepository().add(defaultPluginRepository);
        }
        return pluginList;
    }
}
