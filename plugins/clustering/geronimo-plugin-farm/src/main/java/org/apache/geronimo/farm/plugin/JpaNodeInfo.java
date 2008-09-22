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

import java.io.IOException;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.persistence.NamedQuery;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToOne;
import javax.persistence.FetchType;
import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;
import javax.management.remote.JMXConnector;

import org.apache.geronimo.farm.config.ExtendedJMXConnectorInfo;
import org.apache.geronimo.farm.config.NodeInfo;
import org.apache.geronimo.farm.config.BasicExtendedJMXConnectorInfo;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.system.plugin.PluginInstaller;

/**
 * @version $Rev:$ $Date:$
 */
@Entity(name="node")
@NamedQuery(name = "nodeByName", query = "SELECT a FROM node a WHERE a.name = :name")
public class JpaNodeInfo implements NodeInfo {
    @Id
    private String name;
    private String userName;
    private String password;
    private String protocol;
    private String host;
    private int port = -1;
    private String urlPath;
    private boolean local;

    @ManyToOne(fetch= FetchType.EAGER, cascade={CascadeType.PERSIST})
    @JoinColumn(
        name="cluster", referencedColumnName="name"
    )
    private JpaClusterInfo cluster;

    @Transient
    private Kernel kernel;
    @Transient
    private PluginInstaller pluginInstaller;
    @Transient
    private JMXConnector jmxConnector;
    @Transient
    private ConfigurationManager configurationManager;

    public JpaNodeInfo() {
    }

    public JpaNodeInfo(NodeInfo nodeInfo) {
        this.name = nodeInfo.getName();
        ExtendedJMXConnectorInfo connectorInfo = nodeInfo.getConnectorInfo();
        userName = connectorInfo.getUsername();
        password = connectorInfo.getPassword();
        protocol = connectorInfo.getProtocol();
        host = connectorInfo.getHost();
        port = connectorInfo.getPort();
        urlPath = connectorInfo.getUrlPath();
        local = connectorInfo.isLocal();
    }

    public JpaClusterInfo getCluster() {
        return cluster;
    }

    public void setCluster(JpaClusterInfo cluster) {
        this.cluster = cluster;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ExtendedJMXConnectorInfo getConnectorInfo() {
        return new BasicExtendedJMXConnectorInfo(userName,  password,  protocol,  host,  port,  urlPath, local);
    }

    public void setConnectorInfo(String userName, String password, String protocol, String host, int port, String urlPath, boolean local) {
        this.userName = userName;
        this.password = password;
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.urlPath = urlPath;
        this.local = local;
    }

    public Kernel newKernel() throws IOException {
        if (kernel == null) {
            if (local) {
                kernel = KernelRegistry.getSingleKernel();
            } else {
                ExtendedJMXConnectorInfo connectorInfo = getConnectorInfo();
                jmxConnector = connectorInfo.connect();
                return connectorInfo.newKernel(jmxConnector);
            }
        }
        return kernel;
    }

    public synchronized PluginInstaller getPluginInstaller() throws IOException {
        if (pluginInstaller != null) {
            return pluginInstaller;
        }
        Kernel kernel = newKernel();
        Set<AbstractName> set = kernel.listGBeans(new AbstractNameQuery(PluginInstaller.class.getName()));
        for (AbstractName name : set) {
            pluginInstaller = kernel.getProxyManager().createProxy(name, PluginInstaller.class);
            return pluginInstaller;
        }
        throw new IllegalStateException("No plugin installer found");
    }

    public synchronized ConfigurationManager getConfigurationManager() throws IOException {
        if (configurationManager != null) {
            return configurationManager;
        }
        Kernel kernel = newKernel();
        Set<AbstractName> set = kernel.listGBeans(new AbstractNameQuery(ConfigurationManager.class.getName()));
        for (AbstractName name : set) {
            configurationManager = kernel.getProxyManager().createProxy(name, ConfigurationManager.class);
            return configurationManager;
        }
        throw new IllegalStateException("No plugin installer found");
    }

    public synchronized void disconnect() throws IOException {
        if (kernel != null) {
            if (pluginInstaller != null) {
                kernel.getProxyManager().destroyProxy(pluginInstaller);
                pluginInstaller = null;
            }
            if (!local) {
                jmxConnector.close();
                jmxConnector = null;
            }
            kernel = null;
        }
    }

}
