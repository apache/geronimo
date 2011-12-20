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
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.geronimo.farm.config.ClusterInfo;
import org.apache.geronimo.farm.config.NodeInfo;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @version $Rev:$ $Date:$
 */
@GBean(j2eeType=MasterConfigurationStore.GBEAN_J2EE_TYPE)
public class MasterConfigurationStore implements ConfigurationStore {
    private static final Logger log = LoggerFactory.getLogger(MasterConfigurationStore.class);
    
    private final ConfigurationStore delegate;
    private final Environment defaultEnvironment;
    private final ClusterInfo clusterInfo;
    private final AbstractName clusterInfoName;
    private final ClusterConfigurationStoreClient storeDelegate;
    private final ConfigurationNameBuilder configNameBuilder;
    
    
    public MasterConfigurationStore(@ParamSpecial(type=SpecialAttributeType.kernel) Kernel kernel,
            @ParamSpecial(type=SpecialAttributeType.objectName) String objectName,
            @ParamSpecial(type=SpecialAttributeType.abstractName) AbstractName abstractName,
            @ParamReference(name=GBEAN_REF_REPOSITORY, namingType="Repository") WritableListableRepository repository,
            @ParamAttribute(name=GBEAN_ATTR_DEFAULT_ENV) Environment defaultEnvironment,
            @ParamReference(name=GBEAN_REF_CLUSTER_INFO) ClusterInfo clusterInfo,
            @ParamReference(name=GBEAN_REF_CLUSTER_CONF_STORE_CLIENT) ClusterConfigurationStoreClient storeDelegate) {
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

        configNameBuilder = newMasterConfigurationNameBuilder();
        clusterInfoName = kernel.getAbstractNameFor(clusterInfo);
        delegate = newConfigurationStore(kernel, objectName, abstractName, repository);
    }

    public boolean containsConfiguration(Artifact configId) {
        if (!configNameBuilder.isMasterConfigurationName(configId)) {
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

        installSlaveConfiguration(configurationData);

        Environment environment = configurationData.getEnvironment();
        Artifact slaveConfigId = environment.getConfigId();
        Artifact masterConfigId = configNameBuilder.buildMasterConfigurationName(slaveConfigId);
        environment.setConfigId(masterConfigId);
        installMasterConfiguration(configurationData, slaveConfigId);
    }

    public boolean isInPlaceConfiguration(Artifact configId) throws NoSuchConfigException, IOException {
        ensureArtifactForMasterConfiguration(configId);
        return false;
    }

    public List<ConfigurationInfo> listConfigurations() {
        List<ConfigurationInfo> configurationInfos = delegate.listConfigurations();
        
        List<ConfigurationInfo> filteredConfigurationInfos = new ArrayList<ConfigurationInfo>();
        for (ConfigurationInfo configurationInfo : configurationInfos) {
            if (configNameBuilder.isMasterConfigurationName(configurationInfo.getConfigID())) {
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
        
        Artifact slaveConfigId = configNameBuilder.buildSlaveConfigurationName(configId);
        storeDelegate.uninstall(clusterInfo, slaveConfigId);

        try {
            delegate.uninstall(slaveConfigId);
        } catch (Exception e) {
            log.warn("Exception when uninstalling [" + slaveConfigId + "]", e);
        }
        delegate.uninstall(configId);
    }

    protected void ensureArtifactForMasterConfiguration(Artifact configId) throws NoSuchConfigException {
        if (!configNameBuilder.isMasterConfigurationName(configId)) {
            throw new NoSuchConfigException(configId);
        }
    }

    protected ConfigurationStore newConfigurationStore(Kernel kernel,
        String objectName,
        AbstractName abstractName,
        WritableListableRepository repository) {
        //TODO OSGI is anything missing?
        return new RepositoryConfigurationStore(repository);
    }

    protected ConfigurationNameBuilder newMasterConfigurationNameBuilder() {
        return new BasicConfigurationNameBuilder();
    }

    protected void installMasterConfiguration(ConfigurationData configurationData, Artifact slaveConfigId)
            throws IOException, InvalidConfigException {
        ConfigurationData masterConfigurationData = buildMasterConfigurationData(configurationData, slaveConfigId);
        try {
            delegate.install(masterConfigurationData);
        } catch (Exception e) {
            storeDelegate.uninstall(clusterInfo, slaveConfigId);
            try {
                delegate.uninstall(slaveConfigId);
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

    protected void installSlaveConfiguration(ConfigurationData configurationData)
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

    protected ConfigurationData buildMasterConfigurationData(ConfigurationData configurationData,
        Artifact slaveConfigId) {
        Environment environment = buildEnvironment(configurationData);

        Artifact configId = environment.getConfigId();
        
        List<GBeanData> gbeans = buildControllerGBeans(configId, slaveConfigId);
        
        File configurationDir = delegate.createNewConfigurationDir(configId);
        
        return new ConfigurationData(ConfigurationModuleType.CAR,
            gbeans,
            Collections.EMPTY_MAP,
            environment,
            configurationDir,
            null,
            configurationData.getNaming()); 
    }

    protected Environment buildEnvironment(ConfigurationData configurationData) {
        Environment environment = new Environment(defaultEnvironment);
        environment.setConfigId(configurationData.getId());
        return environment;
    }

    protected List<GBeanData> buildControllerGBeans(Artifact configId, Artifact slaveConfigId) {
        List<GBeanData> gbeans = new ArrayList<GBeanData>();
        for (NodeInfo nodeInfo : clusterInfo.getNodeInfos()) {
            GBeanData gbean = buildControllerGBean(configId, nodeInfo, slaveConfigId);
            gbeans.add(gbean);
        }
        return gbeans;
    }

    protected GBeanData buildControllerGBean(Artifact configId, NodeInfo nodeInfo, Artifact slaveConfigId) {
        AbstractName controllerName = buildControllerName(configId, nodeInfo);
        
        GBeanData gbean = new GBeanData(controllerName, BasicClusterConfigurationController.class);
        gbean.setAttribute(BasicClusterConfigurationController.GBEAN_ATTR_ARTIFACT, slaveConfigId);
        gbean.setAttribute(BasicClusterConfigurationController.GBEAN_ATTR_IGNORE_START_CONF_FAIL_UPON_START,
            Boolean.TRUE);
        gbean.setAttribute(BasicClusterConfigurationController.GBEAN_ATTR_NODE_NAME, nodeInfo.getName());
        gbean.setAttribute(BasicClusterConfigurationController.GBEAN_ATTR_START_CONF_UPON_START, Boolean.TRUE);
        gbean.setReferencePattern(BasicClusterConfigurationController.GBEAN_REF_CLUSTER_INFO, clusterInfoName);
        return gbean;
    }

    protected AbstractName buildControllerName(Artifact configId,
            NodeInfo nodeInfo) {
        return new AbstractName(configId, Collections.singletonMap("nodeName", nodeInfo.getName()));
    }

    public static final String GBEAN_J2EE_TYPE = "ConfigurationStore";
    public static final String GBEAN_ATTR_DEFAULT_ENV = "defaultEnvironment";
    public static final String GBEAN_REF_REPOSITORY = "Repository";
    public static final String GBEAN_REF_CLUSTER_INFO = "ClusterInfo";
    public static final String GBEAN_REF_CLUSTER_CONF_STORE_CLIENT = "ClusterConfigurationStoreClient";
}
