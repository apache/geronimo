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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.geronimo.farm.config.NodeInfo;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.LifecycleException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.persistence.PersistenceUnitGBean;
import org.apache.geronimo.system.plugin.DownloadResults;
import org.apache.geronimo.system.plugin.PluginInstaller;
import org.apache.geronimo.system.plugin.model.PluginListType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * //need interface for operations
 * // need gshell commands
 *
 * @version $Rev$ $Date$
 */
@GBean
public class FarmGBean implements NodeListener, org.apache.geronimo.system.plugin.Farm {
    private static final Logger log = LoggerFactory.getLogger(FarmGBean.class);
    private String defaultRepository;
    private EntityManagerFactory emf;

    public FarmGBean(String defaultPluginRepository, EntityManagerFactory emf) {
        this.defaultRepository = defaultPluginRepository;
        this.emf = emf;
    }

    public FarmGBean(@ParamAttribute(name = "defaultPluginRepository")String defaultPluginRepository,
                     @ParamReference(name = "PersistenceUnit", namingType = NameFactory.PERSISTENCE_UNIT)PersistenceUnitGBean persistenceUnitGBean) {
        this.defaultRepository = defaultPluginRepository;
        this.emf = persistenceUnitGBean.getEntityManagerFactory();
    }


    public Map<String, DownloadResults> addNode(String clusterName, NodeInfo nodeInfo) {
        log.info("Node "  + nodeInfo.getName() + " added to cluster " + clusterName);
        JpaClusterInfo cluster;
        JpaNodeInfo jpaNodeInfo;
        synchronized (this) {
            JpaContext clusterContext = new JpaContext(emf);
            cluster = clusterContext.getClusterInfo(clusterName);
            jpaNodeInfo = clusterContext.getNodeInfo(cluster, nodeInfo);
            clusterContext.close();
        }
        Map<String, DownloadResults> installedPluginLists = new HashMap<String, DownloadResults>();
        for (JpaPluginList pluginList : cluster.getPluginLists()) {
            DownloadResults downloadResults = installToNode(pluginList, jpaNodeInfo);
            installedPluginLists.put(pluginList.getName(), downloadResults);
        }
        return installedPluginLists;
    }

    public void removeNode(String clusterName, String nodeName) {
        log.info("Node "  + nodeName + " renoved from cluster " + clusterName);
        synchronized (this) {
            JpaContext clusterContext = new JpaContext(emf);
            JpaClusterInfo cluster = clusterContext.getClusterInfo(clusterName);
            for (JpaNodeInfo nodeInfo: cluster.getJpaNodeInfos()) {
                if (nodeName.equals(nodeInfo.getName())) {
                    cluster.getJpaNodeInfos().remove(nodeInfo);
                    break;
                }
            }
            clusterContext.close();
        }
    }

    public Map<String, DownloadResults> addPluginList(String clusterName, JpaPluginList pluginList) {
        JpaClusterInfo cluster;
        synchronized (this) {
            JpaContext clusterContext = new JpaContext(emf);
            cluster = clusterContext.getClusterInfo(clusterName);
            cluster.getPluginLists().add(pluginList);
            clusterContext.close();
        }
        return installToCluster(pluginList, cluster);
    }

    public Map<String, DownloadResults> addPluginList(String clusterName, String pluginListName) {
        JpaClusterInfo cluster;
        JpaPluginList pluginList;
        synchronized (this) {
            JpaContext clusterContext = new JpaContext(emf);
            cluster = clusterContext.getClusterInfo(clusterName);
            pluginList = clusterContext.getPluginList(pluginListName, defaultRepository);
            cluster.getPluginLists().add(pluginList);
            clusterContext.close();
        }
        return installToCluster(pluginList, cluster);
    }

    public Map<String, DownloadResults> addPlugin(String pluginListName, String artifactURI) {
        return addPlugin(pluginListName, new JpaPluginInstance(artifactURI));
    }

    public Map<String, DownloadResults> addPlugin(String pluginListName, JpaPluginInstance pluginInstance) {
        JpaPluginList pluginList;
        synchronized (this) {
            JpaContext pluginListContext = new JpaContext(emf);
            pluginList = pluginListContext.getPluginList(pluginListName, defaultRepository);
            pluginList.getPlugins().add(pluginInstance);
            pluginListContext.close();
        }
        return installToClusters(pluginList);
    }

    public Map<String, DownloadResults> removePluginFromPluginList(String pluginListName, String artifactURI) {
        return removePluginFromPluginList(pluginListName, new JpaPluginInstance(artifactURI));
    }

    public Map<String, DownloadResults> removePluginFromPluginList(String pluginListName, JpaPluginInstance pluginInstance) {
        JpaPluginList pluginList;
        synchronized (this) {
            JpaContext pluginListContext = new JpaContext(emf);
            pluginList = pluginListContext.getPluginList(pluginListName, defaultRepository);
            pluginList.getPlugins().remove(pluginInstance);
            pluginListContext.close();
        }
        return removeFromClusters(pluginList, pluginInstance);
    }

    public Map<String, DownloadResults> removePluginListFromCluster(String clusterName, String pluginListName) {
        JpaClusterInfo cluster;
        JpaPluginList pluginList;
        synchronized (this) {
            JpaContext clusterContext = new JpaContext(emf);
            cluster = clusterContext.getClusterInfo(clusterName);
            pluginList = clusterContext.getPluginList(pluginListName, defaultRepository);
            cluster.getPluginLists().remove(pluginList);
            clusterContext.close();
        }
        return removeFromCluster(pluginList.getPlugins(), cluster);
    }

    public Map<String, DownloadResults> addPluginToCluster(String clusterName, String pluginListName, String artifactURI) {
        return addPluginToCluster(clusterName, pluginListName, new JpaPluginInstance(artifactURI));
    }

    public Map<String, DownloadResults> addPluginToCluster(String clusterName, String pluginListName, JpaPluginInstance pluginInstance) {
        JpaPluginList pluginList;
        synchronized (this) {
            JpaContext clusterContext = new JpaContext(emf);
            JpaClusterInfo cluster = clusterContext.getClusterInfo(clusterName);
            pluginList = getPluginList(cluster, pluginListName);
            pluginList.getPlugins().add(pluginInstance);
            clusterContext.close();
        }
        return installToClusters(pluginList);
    }

    private Map<String, DownloadResults> installToClusters(JpaPluginList pluginList) {
        Map<String, DownloadResults> results = new HashMap<String, DownloadResults>();
        for (JpaClusterInfo cluster : pluginList.getClusters()) {
            results.putAll(installToCluster(pluginList, cluster));
        }
        return results;
    }

    private Map<String, DownloadResults> installToCluster(JpaPluginList pluginList, JpaClusterInfo cluster) {
        Map<String, DownloadResults> installedNodes = new HashMap<String, DownloadResults>();
        for (JpaNodeInfo jpaNodeInfo : cluster.getJpaNodeInfos()) {
            DownloadResults downloadResults = installToNode(pluginList, jpaNodeInfo);
            installedNodes.put(jpaNodeInfo.getName(), downloadResults);
        }
        return installedNodes;
    }

    private DownloadResults installToNode(JpaPluginList jpaPluginList, JpaNodeInfo jpaNodeInfo) {
        try {
            PluginInstaller pluginInstaller = jpaNodeInfo.getPluginInstaller();
            PluginListType pluginList = jpaPluginList.getPluginList();
            //TODO parameterize restrictToDefaultRepository
            return pluginInstaller.install(pluginList, defaultRepository, false, null, null);
        } catch (IOException e) {
            DownloadResults downloadResults = new DownloadResults();
            downloadResults.setFailure(e);
            return downloadResults;
        }
    }

    private Map<String, DownloadResults> removeFromClusters(JpaPluginList clusterPluginList, JpaPluginInstance pluginInstance) {
        List<JpaPluginInstance> pluginList = pluginInstance == null? clusterPluginList.getPlugins(): Collections.singletonList(pluginInstance);
        Map<String, DownloadResults> results = new HashMap<String, DownloadResults>();
        for (JpaClusterInfo cluster : clusterPluginList.getClusters()) {
            results.putAll(removeFromCluster(pluginList, cluster));
        }
        return results;
    }

    private Map<String, DownloadResults> removeFromCluster(List<JpaPluginInstance> pluginList, JpaClusterInfo cluster) {
        Map<String, DownloadResults> installedNodes = new HashMap<String, DownloadResults>();
        for (JpaNodeInfo jpaNodeInfo : cluster.getJpaNodeInfos()) {
            DownloadResults downloadResults = removeFromNode(pluginList, jpaNodeInfo);
            installedNodes.put(jpaNodeInfo.getName(), downloadResults);
        }
        return installedNodes;
    }

    private DownloadResults removeFromNode(List<JpaPluginInstance> pluginList, JpaNodeInfo jpaNodeInfo) {
        DownloadResults downloadResults = new DownloadResults();
        try {
            ConfigurationManager configurationManager = jpaNodeInfo.getConfigurationManager();
            for (JpaPluginInstance jpaPluginInstance: pluginList) {
                Artifact artifact = jpaPluginInstance.toArtifact();
                configurationManager.uninstallConfiguration(artifact);
                downloadResults.addRemovedConfigID(artifact);
            }
        } catch (IOException e) {
            downloadResults.setFailure(e);
        } catch (NoSuchConfigException e) {
            downloadResults.setFailure(e);
        } catch (LifecycleException e) {
            downloadResults.setFailure(e);
        }
        return downloadResults;
    }


    private JpaPluginList getPluginList(JpaClusterInfo cluster, String pluginListName) {
        for (JpaPluginList pluginList : cluster.getPluginLists()) {
            if (pluginList.getName().equals(pluginListName)) {
                return pluginList;
            }
        }
        JpaPluginList pluginList = new JpaPluginList();
        pluginList.setName(pluginListName);
        pluginList.setDefaultPluginRepository(defaultRepository);
        cluster.getPluginLists().add(pluginList);
        return pluginList;
    }


    public static class JpaContext {
        private EntityManager em;
        private EntityTransaction entityTransaction;

        public JpaContext(EntityManagerFactory emf) {
            em = emf.createEntityManager();
            entityTransaction = em.getTransaction();
            entityTransaction.begin();
        }

        public JpaClusterInfo getClusterInfo(String clusterName) {
            Query query = em.createNamedQuery("clusterByName");
            query.setParameter("name", clusterName);
            try {
                return (JpaClusterInfo) query.getSingleResult();
            } catch (NoResultException e) {
                JpaClusterInfo clusterInfo = new JpaClusterInfo();
                clusterInfo.setName(clusterName);
                em.persist(clusterInfo);
                em.flush();
                return clusterInfo;
            }
        }

        public JpaNodeInfo getNodeInfo(JpaClusterInfo cluster, NodeInfo nodeInfo) {
            Query query = em.createNamedQuery("nodeByName");
            query.setParameter("name", nodeInfo.getName());
            JpaNodeInfo jpaNodeInfo;
            try {
                jpaNodeInfo = (JpaNodeInfo) query.getSingleResult();
            } catch (NoResultException e) {
                 jpaNodeInfo = new JpaNodeInfo(nodeInfo);
                em.persist(jpaNodeInfo);
            }
            if (jpaNodeInfo.getCluster() == null)  {
                jpaNodeInfo.setCluster(cluster);
            } else if (!jpaNodeInfo.getCluster().getName().equals(cluster.getName())){
                throw new IllegalStateException("cannot move node to another cluster");
            }
            em.flush();
            return jpaNodeInfo;
        }

        public JpaPluginList getPluginList(String name, String defaultRepository) {
            Query query = em.createNamedQuery("pluginListByName");
            query.setParameter("name", name);
            try {
                return (JpaPluginList) query.getSingleResult();
            } catch (NoResultException e) {
                JpaPluginList pluginList = new JpaPluginList();
                pluginList.setName(name);
                pluginList.setDefaultPluginRepository(defaultRepository);
                em.persist(pluginList);
                em.flush();
                return pluginList;
            }
        }

        public void close() {
            entityTransaction.commit();
            em.close();
        }
    }

}
