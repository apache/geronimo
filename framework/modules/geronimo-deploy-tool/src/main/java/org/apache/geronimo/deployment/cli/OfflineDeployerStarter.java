/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.deployment.cli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.LifecycleException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.PersistentConfigurationList;
import org.apache.geronimo.kernel.config.SwitchablePersistentConfigurationList;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.MissingDependencyException;

/**
 * @version $Rev: 476049 $ $Date: 2006-11-17 15:35:17 +1100 (Fri, 17 Nov 2006) $
 */
public class OfflineDeployerStarter {
    private static final Artifact OFFLINE_DEPLOYER_ARTIFACT = new Artifact("org.apache.geronimo.framework",
            "offline-deployer",
            (String) null,
            "car");
    
    private final Kernel kernel;
    private final AbstractName onlineDeployerConfigurationManagerName;
    private final ConfigurationManager onlineDeployerConfigurationManager;
    private final Set<AbstractName> onlineDeployerConfigStores;

    public OfflineDeployerStarter(Kernel kernel) throws GBeanNotFoundException {
        if (null == kernel) {
            throw new IllegalArgumentException("kernel is required");
        }
        this.kernel = kernel;
        
        onlineDeployerConfigurationManagerName = ConfigurationUtil.getConfigurationManagerName(kernel);
        onlineDeployerConfigurationManager = ConfigurationUtil.getConfigurationManager(kernel);
        onlineDeployerConfigStores = kernel.listGBeans(new AbstractNameQuery(ConfigurationStore.class.getName()));
    }
    
    public void start() throws DeploymentException {
        try {
            Artifact offlineDeployerArtifact = resolveOfflineDeployer();
            startOfflineConfiguration(offlineDeployerArtifact);
            startPersistentOfflineConfigurations(offlineDeployerArtifact);
            stopOfflineConfigurationManager();
            //stopOnlineConfigStores(); // If we stop the stores here, we are left with no stores to work with!!
            enablePersistentConfigurationTracking();
        } catch (Exception e) {
            throw new DeploymentException("Unexpected error. Cannot start offline-deployer", e);
        }
        onlineDeployerConfigurationManager.setOnline(false);
    }

    protected Artifact resolveOfflineDeployer() throws MissingDependencyException {
        ArtifactResolver artifactResolver = onlineDeployerConfigurationManager.getArtifactResolver();
        return artifactResolver.resolveInClassLoader(OFFLINE_DEPLOYER_ARTIFACT);
    }
    
    protected void enablePersistentConfigurationTracking() throws GBeanNotFoundException {
        SwitchablePersistentConfigurationList switchableList = (SwitchablePersistentConfigurationList) kernel.getGBean(SwitchablePersistentConfigurationList.class);
        switchableList.setOnline(true);
    }

    protected void stopOnlineConfigStores() throws GBeanNotFoundException {
        for (AbstractName configStoreName : onlineDeployerConfigStores) {
            kernel.stopGBean(configStoreName);
        }
    }

    protected void stopOfflineConfigurationManager() throws GBeanNotFoundException {
        Set names = kernel.listGBeans(new AbstractNameQuery(ConfigurationManager.class.getName()));
        for (Iterator iterator = names.iterator(); iterator.hasNext();) {
            AbstractName abstractName = (AbstractName) iterator.next();
            if (!onlineDeployerConfigurationManagerName.equals(abstractName)) {
                kernel.stopGBean(abstractName);
            }
        }
    }

    protected void startPersistentOfflineConfigurations(Artifact offlineDeployerArtifact) throws Exception {
        AbstractNameQuery query = new AbstractNameQuery(offlineDeployerArtifact, 
                Collections.EMPTY_MAP, 
                Collections.singleton(PersistentConfigurationList.class.getName()));
        Set configLists = kernel.listGBeans(query);

        List<Artifact> configs = new ArrayList<Artifact>();
        for (Iterator i = configLists.iterator(); i.hasNext();) {
            AbstractName configListName = (AbstractName) i.next();
            configs.addAll((List<Artifact>) kernel.invoke(configListName, "restore"));
        }
        
        for (Artifact config : configs) {
            onlineDeployerConfigurationManager.loadConfiguration(config);
            onlineDeployerConfigurationManager.startConfiguration(config);
        }
    }

    protected void startOfflineConfiguration(Artifact offlineDeployerArtifact) throws NoSuchConfigException, LifecycleException {
        onlineDeployerConfigurationManager.loadConfiguration(offlineDeployerArtifact);
        onlineDeployerConfigurationManager.startConfiguration(offlineDeployerArtifact);
    }

}
