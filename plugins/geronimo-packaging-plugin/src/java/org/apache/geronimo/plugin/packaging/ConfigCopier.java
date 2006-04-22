/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.plugin.packaging;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.WritableListableRepository;

/**
 * Copies all the configurations out of source config store into target config store.
 *
 * @version $Rev:$ $Date:$
 */
public class ConfigCopier {

    private static final Class[] REPO_ARGS = new Class[] {File.class};
    private static final Class[] STORE_ARGS = new Class[] {WritableListableRepository.class};


    private String sourceRepositoryClass;
    private String targetRepositoryClass;
    private String sourceConfigurationStoreClass;
    private String targetConfigurationStoreClass;
    private File sourceRepositoryLocation;
    private File targetRepositoryLocation;


    public String getSourceRepositoryClass() {
        return sourceRepositoryClass;
    }

    public void setSourceRepositoryClass(String sourceRepositoryClass) {
        this.sourceRepositoryClass = sourceRepositoryClass;
    }

    public String getTargetRepositoryClass() {
        return targetRepositoryClass;
    }

    public void setTargetRepositoryClass(String targetRepositoryClass) {
        this.targetRepositoryClass = targetRepositoryClass;
    }

    public String getSourceConfigurationStoreClass() {
        return sourceConfigurationStoreClass;
    }

    public void setSourceConfigurationStoreClass(String sourceConfigurationStoreClass) {
        this.sourceConfigurationStoreClass = sourceConfigurationStoreClass;
    }

    public String getTargetConfigurationStoreClass() {
        return targetConfigurationStoreClass;
    }

    public void setTargetConfigurationStoreClass(String targetConfigurationStoreClass) {
        this.targetConfigurationStoreClass = targetConfigurationStoreClass;
    }

    public File getSourceRepositoryLocation() {
        return sourceRepositoryLocation;
    }

    public void setSourceRepositoryLocation(File sourceRepositoryLocation) {
        this.sourceRepositoryLocation = sourceRepositoryLocation;
    }

    public File getTargetRepositoryLocation() {
        return targetRepositoryLocation;
    }

    public void setTargetRepositoryLocation(File targetRepositoryLocation) {
        this.targetRepositoryLocation = targetRepositoryLocation;
    }

    public void execute() throws Exception {
        try {
            ClassLoader cl = this.getClass().getClassLoader();

            Class sourceRepoClass = cl.loadClass(sourceRepositoryClass);
            WritableListableRepository sourceRepository = (WritableListableRepository) sourceRepoClass.getDeclaredConstructor(REPO_ARGS).newInstance(new Object[] {sourceRepositoryLocation});
            Class sourceConfigStoreClass = cl.loadClass(sourceConfigurationStoreClass);
            ConfigurationStore sourceConfigStore = (ConfigurationStore) sourceConfigStoreClass.getDeclaredConstructor(STORE_ARGS).newInstance(new Object[] {sourceRepository});

            Class targetRepoClass = cl.loadClass(targetRepositoryClass);
            WritableListableRepository targetRepository = (WritableListableRepository) targetRepoClass.getDeclaredConstructor(REPO_ARGS).newInstance(new Object[] {targetRepositoryLocation});
            Class targetConfigStoreClass = cl.loadClass(targetConfigurationStoreClass);
            ConfigurationStore targetConfigStore = (ConfigurationStore) targetConfigStoreClass.getDeclaredConstructor(STORE_ARGS).newInstance(new Object[] {targetRepository});

            List configs = sourceConfigStore.listConfigurations();
            for (Iterator iterator = configs.iterator(); iterator.hasNext();) {
                ConfigurationInfo configInfo = (ConfigurationInfo) iterator.next();
                Artifact configId = configInfo.getConfigID();
                ConfigurationData configData = sourceConfigStore.loadConfiguration(configId);
                if (targetConfigStore.containsConfiguration(configId)) {
                    targetConfigStore.uninstall(configId);
                }
                targetConfigStore.install(configData);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
