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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.geronimo.farm.config.NodeInfo;
import org.apache.geronimo.farm.discovery.DiscoveryAgent;
import org.apache.geronimo.farm.discovery.DiscoveryListener;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.system.plugin.DownloadResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
@GBean
public class FarmDiscoveryListener implements DiscoveryListener {

    private static final Logger log = LoggerFactory.getLogger(FarmDiscoveryListener.class);
    private DiscoveryAgent discoveryAgent;
    private NodeListener nodeListener;
    private String defaultClusterName;
    private String discoveryType;
    private String userName;
    private String password;


    public FarmDiscoveryListener(@ParamAttribute(name = "defaultClusterName")String defaultClusterName,
                                 @ParamAttribute(name = "discoveryType")String discoveryType,
                                 @ParamAttribute(name = "userName")String userName,
                                 @ParamAttribute(name = "password")String password,
                                 @ParamReference(name = "DiscoveryAgent")DiscoveryAgent discoveryAgent,
                                 @ParamReference(name = "NodeListener")NodeListener nodeListener) {
        discoveryAgent.setDiscoveryListener(this);
        this.discoveryAgent = discoveryAgent;
        this.nodeListener = nodeListener;
        this.defaultClusterName = defaultClusterName;
        this.discoveryType = discoveryType;
        this.userName = userName;
        this.password = password;
    }

    /**
     * Starts the GBean.  This informs the GBean that it is about to transition to the running state.
     *
     * @throws Exception if the target failed to start; this will cause a transition to the failed state
     */
    public void doStart() throws Exception {
    }

    /**
     * Stops the target.  This informs the GBean that it is about to transition to the stopped state.
     *
     * @throws Exception if the target failed to stop; this will cause a transition to the failed state
     */
    public void doStop() throws Exception {
        doFail();
    }

    /**
     * Fails the GBean.  This informs the GBean that it is about to transition to the failed state.
     */
    public void doFail() {
        discoveryAgent.setDiscoveryListener(null);
    }

    public void serviceAdded(URI service) {
        NodeInfo nodeInfo = toNodeInfo(service);
        if (nodeInfo != null) {
            String clusterName = toClusterName(service);
            Map<String, DownloadResults> results = nodeListener.addNode(clusterName, nodeInfo);
            log(nodeInfo, results);
        }
    }

    public void serviceRemoved(URI service) {
        NodeInfo nodeInfo = toNodeInfo(service);
        if (nodeInfo != null) {
            String clusterName = toClusterName(service);
            nodeListener.removeNode(clusterName, nodeInfo.getName());
        }
    }

    NodeInfo toNodeInfo(URI service) {
        String typeScheme = service.getScheme();
        if (!discoveryType.equals(typeScheme)) {
            return null;
        }
        try {
            URI jmxService = new URI(service.getSchemeSpecificPart());
            JpaNodeInfo nodeInfo = new JpaNodeInfo();
            String host = jmxService.getHost();
            int port = jmxService.getPort();
            String urlPath = jmxService.getPath().substring(1);
            //TODO not sure about this one
            String protocol = jmxService.getScheme();
            String name = getValue("node", jmxService.getQuery(), host + ":" + port);
            nodeInfo.setName(name);
            nodeInfo.setConnectorInfo(userName, password, protocol, host, port, urlPath, false);
            return nodeInfo;
        } catch (URISyntaxException e) {
            //TODO log error
            return null;
        }
    }

    String toClusterName(URI service) {
        String typeScheme = service.getScheme();
        if (!discoveryType.equals(typeScheme)) {
            return null;
        }
        try {
            URI jmxService = new URI(service.getSchemeSpecificPart());
            return getValue("cluster", jmxService.getQuery(), defaultClusterName);
        } catch (URISyntaxException e) {
            //TODO log error
            return null;
        }
    }

    private String getValue(String key, String query, String defaultValue) {
        String[] bits = query.split("[&=]");
        if (bits.length % 2 != 0) {
            throw new IllegalArgumentException("Can't parse query string: " + query);
        }
        for (int i = 0; i < bits.length; i = i + 2) {
            if (key.equals(bits[i])) {
                return bits[i + 1];
            }
        }
        return defaultValue;
    }

    private void log(NodeInfo nodeInfo, Map<String, DownloadResults> results) {
        log.info("installing to node: " + nodeInfo.getName());
        for (Map.Entry<String, DownloadResults> entry: results.entrySet()) {
            log.info("installation results for plugin list: " + entry.getKey());
            DownloadResults downloadResults = entry.getValue();
            log.info("installed: " + downloadResults.getInstalledConfigIDs());
            if (downloadResults.isFailed()) {
                log.info("failure: ", downloadResults.getFailure());
            }
        }

    }

}
