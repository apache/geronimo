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

package org.apache.geronimo.clustering.deployment;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.clustering.config.ClusterInfo;
import org.apache.geronimo.clustering.config.NodeInfo;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationAlreadyExistsException;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.WritableListableRepository;
import org.apache.geronimo.system.configuration.RepositoryConfigurationStore;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class MasterConfigurationStore implements ConfigurationStore {
    private static final Log log = LogFactory.getLog(MasterConfigurationStore.class);
    
    private final ConfigurationStore delegate;
    private final Environment defaultEnvironment;
    private final ClusterInfo clusterInfo;
    private final AbstractName clusterInfoName;
    private final ClusterConfigurationStoreClient storeDelegate;
    private final MasterConfigurationNameBuilder builder;
    
    public MasterConfigurationStore(Kernel kernel,
            String objectName,
            WritableListableRepository repository,
            Environment defaultEnvironment,
            ClusterInfo clusterInfo,
            ClusterConfigurationStoreClient storeDelegate) {
        if (null == kernel) {
            throw new IllegalArgumentException("kernel is required");
        } else if (null == objectName) {
            throw new IllegalArgumentException("objectName is required");
        } else if (null == repository) {
            throw new IllegalArgumentException("repository is required");
        } else if (null == defaultEnvironment) {
            throw new IllegalArgumentException("defaultEnvironment is required");
        } else if (null == clusterInfo) {
            throw new IllegalArgumentException("clusterInfo is required");
        } else if (null == storeDelegate) {
            throw new IllegalArgumentException("storeDelegate is required");
        }
        this.defaultEnvironment = defaultEnvironment;
        this.clusterInfo = clusterInfo;
        this.storeDelegate = storeDelegate;

        builder = newMasterConfigurationBuilder();
        clusterInfoName = kernel.getAbstractNameFor(clusterInfo);
        delegate = newConfigurationStore(kernel, objectName, repository);
    }

    public boolean containsConfiguration(Artifact configId) {
        if (!builder.isMasterConfigurationName(configId)) {
            return false;
        }
        return delegate.containsConfiguration(configId);
    }

    public File createNewConfigurationDir(Artifact configId) throws ConfigurationAlreadyExistsException {
        return delegate.createNewConfigurationDir(configId);
    }

    public void exportConfiguration(Artifact configId, OutputStream output) throws IOException, NoSuchConfigException {
        ensureArtifactForMasterConfiguration(configId);
        delegate.exportConfiguration(configId, output);
    }

    public AbstractName getAbstractName() {
        return delegate.getAbstractName();
    }

    public String getObjectName() {
        return delegate.getObjectName();
    }

    public void install(ConfigurationData configurationData) throws IOException, InvalidConfigException {
        storeDelegate.install(clusterInfo, configurationData);

        installRawConfiguration(configurationData);

        installMasterConfiguration(configurationData);
    }

    public boolean isInPlaceConfiguration(Artifact configId) throws NoSuchConfigException, IOException {
        ensureArtifactForMasterConfiguration(configId);
        return false;
    }

    public List<ConfigurationInfo> listConfigurations() {
        List<ConfigurationInfo> configurationInfos = delegate.listConfigurations();
        
        List<ConfigurationInfo> filteredConfigurationInfos = new ArrayList<ConfigurationInfo>();
        for (ConfigurationInfo configurationInfo : configurationInfos) {
            if (builder.isMasterConfigurationName(configurationInfo.getConfigID())) {
                filteredConfigurationInfos.add(configurationInfo);
            }
        }
        
        return filteredConfigurationInfos;
    }

    public ConfigurationData loadConfiguration(Artifact configId)
            throws NoSuchConfigException, IOException, InvalidConfigException {
        ensureArtifactForMasterConfiguration(configId);
        return delegate.loadConfiguration(configId);
    }

    public Set<URL> resolve(Artifact configId, String moduleName, String path)
            throws NoSuchConfigException, MalformedURLException {
        ensureArtifactForMasterConfiguration(configId);
        return delegate.resolve(configId, moduleName, path);
    }

    public void uninstall(Artifact configId) throws NoSuchConfigException, IOException {
        ensureArtifactForMasterConfiguration(configId);
        
        Artifact slaveConfigId = builder.buildSlaveConfigurationName(configId);
        storeDelegate.uninstall(clusterInfo, slaveConfigId);

        try {
            delegate.uninstall(slaveConfigId);
        } catch (Exception e) {
            log.warn("Exception when uninstalling [" + slaveConfigId + "]", e);
        }
        delegate.uninstall(configId);
    }

    protected void ensureArtifactForMasterConfiguration(Artifact configId) throws NoSuchConfigException {
        if (!builder.isMasterConfigurationName(configId)) {
            throw new NoSuchConfigException(configId);
        }
    }

    protected ConfigurationStore newConfigurationStore(Kernel kernel,
        String objectName,
        WritableListableRepository repository) {
        return new RepositoryConfigurationStore(kernel, objectName, repository);
    }

    protected MasterConfigurationNameBuilder newMasterConfigurationBuilder() {
        return new BasicMasterConfigurationNameBuilder();
    }

    protected void installMasterConfiguration(ConfigurationData configurationData)
            throws IOException, InvalidConfigException {
        ConfigurationData masterConfigurationData = buildMasterConfigurationData(configurationData);
        try {
            delegate.install(masterConfigurationData);
        } catch (Exception e) {
            storeDelegate.uninstall(clusterInfo, configurationData.getId());
            try {
                delegate.uninstall(configurationData.getId());
            } catch (NoSuchConfigException nestedE) {
            }
            if (e instanceof IOException) {
                throw (IOException) e;
            } else if (e instanceof InvalidConfigException) {
                throw (InvalidConfigException) e;
            }
            throw (IOException) new IOException("See nested").initCause(e);
        }
    }

    protected void installRawConfiguration(ConfigurationData configurationData)
            throws IOException, InvalidConfigException {
        try {
            delegate.install(configurationData);
        } catch (Exception e) {
            storeDelegate.uninstall(clusterInfo, configurationData.getId());
            if (e instanceof IOException) {
                throw (IOException) e;
            } else if (e instanceof InvalidConfigException) {
                throw (InvalidConfigException) e;
            }
            throw (IOException) new IOException("See nested").initCause(e);
        }
    }

    protected ConfigurationData buildMasterConfigurationData(ConfigurationData configurationData) {
        Environment environment = buildEnvironment(configurationData);

        Artifact configId = environment.getConfigId();
        
        List<GBeanData> gbeans = buildControllerGBeans(configId, configurationData);
        
        File configurationDir = delegate.createNewConfigurationDir(configId);
        
        return new ConfigurationData(ConfigurationModuleType.CAR,
            new LinkedHashSet(),
            gbeans,
            Collections.EMPTY_MAP,
            environment,
            configurationDir,
            null,
            configurationData.getNaming()); 
    }

    protected Environment buildEnvironment(ConfigurationData configurationData) {
        Environment environment = new Environment(defaultEnvironment);
        Artifact configId = builder.buildMasterConfigurationName(configurationData.getId());
        environment.setConfigId(configId);
        return environment;
    }

    protected List<GBeanData> buildControllerGBeans(Artifact configId, ConfigurationData configurationData) {
        List<GBeanData> gbeans = new ArrayList<GBeanData>();
        for (NodeInfo nodeInfo : clusterInfo.getNodeInfos()) {
            GBeanData gbean = buildControllerGBean(configId, configurationData, nodeInfo);
            gbeans.add(gbean);
        }
        return gbeans;
    }

    protected GBeanData buildControllerGBean(Artifact configId,
            ConfigurationData configurationData,
            NodeInfo nodeInfo) {
        AbstractName controllerName = buildControllerName(configId, configurationData, nodeInfo);
        
        GBeanData gbean = new GBeanData(controllerName, BasicClusterConfigurationController.GBEAN_INFO);
        gbean.setAttribute(BasicClusterConfigurationController.GBEAN_ATTR_ARTIFACT, configurationData.getId());
        gbean.setAttribute(BasicClusterConfigurationController.GBEAN_ATTR_IGNORE_START_CONF_FAIL_UPON_START,
            Boolean.TRUE);
        gbean.setAttribute(BasicClusterConfigurationController.GBEAN_ATTR_NODE_NAME, nodeInfo.getName());
        gbean.setAttribute(BasicClusterConfigurationController.GBEAN_ATTR_START_CONF_UPON_START, Boolean.TRUE);
        gbean.setReferencePattern(BasicClusterConfigurationController.GBEAN_REF_CLUSTER_INFO, clusterInfoName);
        return gbean;
    }

    protected AbstractName buildControllerName(Artifact configId,
            ConfigurationData configurationData, 
            NodeInfo nodeInfo) {
        return new AbstractName(configId, Collections.singletonMap("nodeName", nodeInfo.getName()));
    }

    public static final GBeanInfo GBEAN_INFO;

    public static final String GBEAN_J2EE_TYPE = "ConfigurationStore";
    public static final String GBEAN_ATTR_KERNEL = "kernel";
    public static final String GBEAN_ATTR_OBJECT_NAME = "objectName";
    public static final String GBEAN_ATTR_DEFAULT_ENV = "defaultEnvironment";
    public static final String GBEAN_REF_REPOSITORY = "Repository";
    public static final String GBEAN_REF_CLUSTER_INFO = "ClusterInfo";
    public static final String GBEAN_REF_CLUSTER_CONF_STORE_CLIENT = "ClusterConfigurationStoreClient";

    static {
        GBeanInfoBuilder builder = GBeanInfoBuilder.createStatic(MasterConfigurationStore.class, GBEAN_J2EE_TYPE);
        
        builder.addAttribute(GBEAN_ATTR_KERNEL, Kernel.class, false);
        builder.addAttribute(GBEAN_ATTR_OBJECT_NAME, String.class, false);
        builder.addAttribute(GBEAN_ATTR_DEFAULT_ENV, Environment.class, true, true);
        
        builder.addReference(GBEAN_REF_REPOSITORY, WritableListableRepository.class, "Repository");
        builder.addReference(GBEAN_REF_CLUSTER_INFO, ClusterInfo.class);
        builder.addReference(GBEAN_REF_CLUSTER_CONF_STORE_CLIENT, ClusterConfigurationStoreClient.class);
        
        builder.addInterface(ConfigurationStore.class);
        
        builder.setConstructor(new String[]{GBEAN_ATTR_KERNEL,
            GBEAN_ATTR_OBJECT_NAME,
            GBEAN_REF_REPOSITORY,
            GBEAN_ATTR_DEFAULT_ENV,
            GBEAN_REF_CLUSTER_INFO,
            GBEAN_REF_CLUSTER_CONF_STORE_CLIENT});
        
        GBEAN_INFO = builder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
