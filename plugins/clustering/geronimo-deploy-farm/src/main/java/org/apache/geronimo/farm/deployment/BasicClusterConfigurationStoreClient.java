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

package org.apache.geronimo.farm.deployment;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.deployment.plugin.remote.FileUploadClient;
import org.apache.geronimo.deployment.plugin.remote.FileUploadProgress;
import org.apache.geronimo.deployment.plugin.remote.FileUploadServletClient;
import org.apache.geronimo.farm.config.ClusterInfo;
import org.apache.geronimo.farm.config.ExtendedJMXConnectorInfo;
import org.apache.geronimo.farm.config.NodeInfo;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class BasicClusterConfigurationStoreClient implements ClusterConfigurationStoreClient {
    private static final Logger log = LoggerFactory.getLogger(BasicClusterConfigurationStoreClient.class);

    private static final String[] METHOD_SIGNATURE_INSTALL =
        new String[] {ConfigurationData.class.getName(), File.class.getName()};
    private static final String[] METHOD_SIGNATURE_UNINSTALL = new String[] {Artifact.class.getName()};

    private final AbstractNameQuery clusterConfigurationStoreNameQuery;
    private final DirectoryPackager packager;
    private final FileUploadClient fileUploadClient;

    public BasicClusterConfigurationStoreClient(@ParamAttribute(name=GBEAN_ATTR_CLUSTER_CONF_STORE_NAME_QUERY) AbstractNameQuery clusterConfigurationStoreNameQuery) {
        if (null == clusterConfigurationStoreNameQuery) {
            throw new IllegalArgumentException("clusterConfigurationStoreNameQuery is required");
        }
        this.clusterConfigurationStoreNameQuery = clusterConfigurationStoreNameQuery;
        
        packager = newDirectoryPackager();
        fileUploadClient = newFileUploadClient();
    }

    public void install(ClusterInfo clusterInfo, ConfigurationData configurationData)
            throws IOException, InvalidConfigException {
        Collection<NodeInfo> nodeInfos = clusterInfo.getNodeInfos();

        Collection<NodeInfo> installedToNodeInfos = new ArrayList<NodeInfo>();
        for (NodeInfo nodeInfo : nodeInfos) {
            try {
                install(nodeInfo, configurationData);
                installedToNodeInfos.add(nodeInfo);
            } catch (Exception e) {
                uninstall(clusterInfo, configurationData.getId(), installedToNodeInfos);
                if (e instanceof IOException) {
                    throw (IOException) e;
                } else if (e instanceof InvalidConfigException) {
                    throw (InvalidConfigException) e;
                }
                throw (IOException) new IOException("See nested").initCause(e);
            }
        }
    }

    public void uninstall(ClusterInfo clusterInfo, Artifact configId) {
        uninstall(clusterInfo, configId, clusterInfo.getNodeInfos());
    }

    protected void uninstall(ClusterInfo clusterInfo, Artifact configId, Collection<NodeInfo> installedToNodeInfos) {
        for (NodeInfo nodeInfo : installedToNodeInfos) {
            try {
                uninstall(nodeInfo, configId);
            } catch (Exception e) {
                log.info("Ignoring error while uninstalling [" + configId + "]from [" + nodeInfo + "]", e);
            }
        }
    }
    
    protected void install(NodeInfo nodeInfo, ConfigurationData configurationData) throws IOException {
        Kernel kernel = nodeInfo.newKernel();

        AbstractName clusterConfigurationStoreName = searchClusterConfigurationStore(kernel);

        File configurationDataFile = uploadConfiguration(kernel, nodeInfo, configurationData);

        boolean inVMCall = nodeInfo.getConnectorInfo().isLocal();
        File oldConfigurationDir = null;
        if (inVMCall) {
            oldConfigurationDir = configurationData.getConfigurationDir();
        }
        Object[] params = new Object[] {configurationData, configurationDataFile};
        try {
            kernel.invoke(clusterConfigurationStoreName, "install", params, METHOD_SIGNATURE_INSTALL);
        } catch (Exception e) {
            throw (IOException) new IOException("See nested").initCause(e);
        } finally {
            if (inVMCall) {
                configurationData.setConfigurationDir(oldConfigurationDir);
            }
        }
    }

    protected void uninstall(NodeInfo nodeInfo, Artifact configId) throws IOException {
        Kernel kernel = nodeInfo.newKernel();
        try {
        	ConfigurationManager configurationManager = (ConfigurationManager) kernel.getGBean(ConfigurationManager.class);
            configurationManager.stopConfiguration(configId);
            configurationManager.unloadConfiguration(configId);
            configurationManager.uninstallConfiguration(configId);
        } catch (Exception e) {
            throw (IOException) new IOException("See nested").initCause(e);
        }
    }
    
    protected File uploadConfiguration(Kernel kernel, NodeInfo nodeInfo, ConfigurationData configurationData) throws IOException {
        File packedConfigurationDir = packager.pack(configurationData.getConfigurationDir());

        if (nodeInfo.getConnectorInfo().isLocal()) {
            return packedConfigurationDir;
        }
        
        URL remoteDeployUploadURL = fileUploadClient.getRemoteDeployUploadURL(kernel);

        ConfigurationUploadProgress configurationUploadProgress = new ConfigurationUploadProgress(configurationData);
        File[] configurationDataFiles = new File[] {packedConfigurationDir};
        ExtendedJMXConnectorInfo connectorInfo = nodeInfo.getConnectorInfo();
        fileUploadClient.uploadFilesToServer(remoteDeployUploadURL, 
            connectorInfo.getUsername(),
            connectorInfo.getPassword(),
            configurationDataFiles,
            configurationUploadProgress);

        if (configurationUploadProgress.failure) {
            if (null != configurationUploadProgress.exception) {
                throw (IOException) new IOException("See nested").initCause(configurationUploadProgress.exception);
            }
            throw new IOException(configurationUploadProgress.failureMessage);
        }
        
        return configurationDataFiles[0];
    }

    protected DirectoryPackager newDirectoryPackager() {
        return new ZipDirectoryPackager();
    }

    protected FileUploadClient newFileUploadClient() {
        return new FileUploadServletClient();
    }

    protected AbstractName searchClusterConfigurationStore(Kernel kernel) throws IOException {
        Set<AbstractName> clusterConfigurationStoreNames = kernel.listGBeans(clusterConfigurationStoreNameQuery);
        if (1 != clusterConfigurationStoreNames.size()) {
            throw new IOException("Cannot locate remote store. Found [" + clusterConfigurationStoreNames + "]");
        }
        return clusterConfigurationStoreNames.iterator().next();
    }

    protected class ConfigurationUploadProgress implements FileUploadProgress {
        private final ConfigurationData configurationData;
        private boolean failure;
        private Exception exception;
        private String failureMessage;

        public ConfigurationUploadProgress(ConfigurationData configurationData) {
            this.configurationData = configurationData;
        }

        public void fail(String message) {
            failure = true;
            failureMessage = "Upload of configuration [" + configurationData.getId() + "] - [" + message + "]";
            log.error("Upload of configuration [" + configurationData.getId() + "] - [" + message + "]");
        }

        public void fail(Exception exception) {
            failure = true;
            this.exception = exception;
            log.error("Upload of configuration [" + configurationData.getId() + "]", exception);
        }

        public void updateStatus(String message) {
            log.info("Upload of configuration [" + configurationData.getId() + "] - [" + message + "]");
        }
    }

    public static final String GBEAN_ATTR_CLUSTER_CONF_STORE_NAME_QUERY = "clusterConfigurationStoreNameQuery";
}
