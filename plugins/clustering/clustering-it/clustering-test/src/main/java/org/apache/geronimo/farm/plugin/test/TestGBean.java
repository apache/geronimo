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


package org.apache.geronimo.farm.plugin.test;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.apache.geronimo.farm.plugin.FarmGBean;
import org.apache.geronimo.farm.plugin.JpaClusterInfo;
import org.apache.geronimo.farm.plugin.JpaNodeInfo;
import org.apache.geronimo.farm.plugin.JpaPluginInstance;
import org.apache.geronimo.farm.plugin.JpaPluginList;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.persistence.PersistenceUnitGBean;
import org.apache.geronimo.system.plugin.DownloadResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
@GBean
public class TestGBean {
    private static Logger log = LoggerFactory.getLogger(TestGBean.class);

    private EntityManagerFactory emf;
    private FarmGBean farmGBean;
    private static final String CLUSTER1 = "cluster1";
    private static final String CLUSTER2 = "cluster2";
    private static final String PLUGIN_LIST1 = "pluginList1";
    private static final String NODE1 = "node1";

    public TestGBean(@ParamReference(name="PersistenceUnit", namingType= NameFactory.PERSISTENCE_UNIT)PersistenceUnitGBean persistenceUnitGBean,
                    @ParamReference(name="FarmGBean") FarmGBean farmGBean) throws Exception {
        this.emf = persistenceUnitGBean.getEntityManagerFactory();
        this.farmGBean = farmGBean;
        testAddPlugin();
//        testAddNode1();
//        testAddExistingPluginListToNewCluster();
//        Thread.sleep(1000 * 60 * 2);
    }

    public void testAddPlugin() throws Exception {
        String clusterName = CLUSTER1;
        String pluginListName = PLUGIN_LIST1;
        int clusterSize = 1;
        JpaPluginInstance pluginInstance = new JpaPluginInstance("org.apache.geronimo.plugins.it/customer-jetty/2.2/car");
        farmGBean.addPluginToCluster(clusterName, pluginListName, pluginInstance);

        checkCluster(clusterName, pluginListName, clusterSize);
    }

    public void testAddNode1() throws Exception {
        String clusterName = CLUSTER1;
        String pluginListName = PLUGIN_LIST1;
        int clusterSize = 1;

        checkCluster(clusterName, pluginListName, clusterSize);
        checkCluster(clusterName, pluginListName, clusterSize);

        JpaNodeInfo nodeInfo = new JpaNodeInfo();
        nodeInfo.setName(NODE1);
        int serverId = 1;
//        int serverId = 0;
        nodeInfo.setConnectorInfo("system", "manager", "rmi", "localhost", 1099 + 10 * serverId, "JMXConnector", false);
        Map<String, DownloadResults> results = farmGBean.addNode(CLUSTER1, nodeInfo);
        if (results.size() != 1) {
            throw new IllegalStateException("wrong number of nodes installed to" + results.size());
        }
        DownloadResults downloadResults = results.get(PLUGIN_LIST1);
        if (downloadResults.isFailed()) {
            throw new IllegalStateException("failed to install on node1", downloadResults.getFailure());
        }
    }

    public void testAddExistingPluginListToNewCluster() throws Exception {
        String clusterName = CLUSTER2;
        String pluginListName = PLUGIN_LIST1;
        int clusterSize = 2;
        farmGBean.addPluginList(clusterName, pluginListName);

        checkCluster(clusterName, pluginListName, clusterSize);
    }

    private void checkCluster(String clusterName, String pluginListName, int clusterSize) {
        FarmGBean.JpaContext context = new FarmGBean.JpaContext(emf);
        JpaClusterInfo cluster = context.getClusterInfo(clusterName);
        log.info("retrieved " + cluster.getName());
        log.info("cluster pluginlist count: " + cluster.getPluginLists().size());
        JpaPluginList pluginList = cluster.getPluginLists().iterator().next();
        if (!pluginList.getName().equals(pluginListName)) {
            throw new IllegalStateException("Wrong name: " + pluginList.getName());
        }
        List<JpaClusterInfo> clusters = pluginList.getClusters();
        if (clusters.size() != clusterSize) {
            throw new IllegalStateException("Wrong size: " + clusters.size());
        }
        context.close();
    }

}
