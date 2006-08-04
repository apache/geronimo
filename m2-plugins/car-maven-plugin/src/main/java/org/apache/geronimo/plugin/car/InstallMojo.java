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

package org.apache.geronimo.plugin.car;

import java.io.File;
import java.util.Iterator;

import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.WritableListableRepository;

/**
 * Copies all the configurations out of source config store into target config store.
 *
 * @goal install
 *
 * @version $Rev$ $Date$
 */
public class InstallMojo extends AbstractCarMojo {

    private static final Class[] REPO_ARGS = { File.class };

    private static final Class[] STORE_ARGS = { WritableListableRepository.class };

    /**
     * @parameter default-value="org.apache.geronimo.system.repository.Maven2Repository"
     * @required
     */
    private String sourceRepositoryClass;

    /**
     * @parameter default-value="org.apache.geronimo.system.repository.Maven2Repository"
     * @required
     */
    private String targetRepositoryClass;

    /**
     * @parameter default-value="org.apache.geronimo.system.configuration.RepositoryConfigurationStore"
     * @required
     */
    private String sourceConfigurationStoreClass;

    /**
     * @parameter default-value="org.apache.geronimo.plugin.car.MavenConfigStore"
     * @required
     */
    private String targetConfigurationStoreClass;

    /**
     * @parameter expression="${project.build.directory}/repository"
     * @required
     */
    private File sourceRepositoryLocation;

    /**
     * @parameter expression="${settings.LocalRepository}"
     * @required
     */
    private File targetRepositoryLocation;

    protected void doExecute() throws Exception {
        // copy artifact(s) to maven repository

        ClassLoader cl = this.getClass().getClassLoader();

        Class sourceRepoClass = cl.loadClass(sourceRepositoryClass);
        WritableListableRepository sourceRepository = (WritableListableRepository)
                sourceRepoClass.getDeclaredConstructor(REPO_ARGS).newInstance(new Object[]{ sourceRepositoryLocation });

        Class sourceConfigStoreClass = cl.loadClass(sourceConfigurationStoreClass);
        ConfigurationStore sourceConfigStore = (ConfigurationStore)
                sourceConfigStoreClass.getDeclaredConstructor(STORE_ARGS).newInstance(new Object[]{ sourceRepository });

        Class targetRepoClass = cl.loadClass(targetRepositoryClass);
        WritableListableRepository targetRepository = (WritableListableRepository)
                targetRepoClass.getDeclaredConstructor(REPO_ARGS).newInstance(new Object[]{ targetRepositoryLocation });
        Class targetConfigStoreClass = cl.loadClass(targetConfigurationStoreClass);

        ConfigurationStore targetConfigStore = (ConfigurationStore)
                targetConfigStoreClass.getDeclaredConstructor(STORE_ARGS).newInstance(new Object[]{ targetRepository });

        Iterator iterator = sourceConfigStore.listConfigurations().iterator();
        while (iterator.hasNext()) {
            ConfigurationInfo configInfo = (ConfigurationInfo) iterator.next();
            Artifact configId = configInfo.getConfigID();
            ConfigurationData configData = sourceConfigStore.loadConfiguration(configId);

            log.info("Copying artifact: " + configId);

            if (targetConfigStore.containsConfiguration(configId)) {
                targetConfigStore.uninstall(configId);
            }

            targetConfigStore.install(configData);
        }
    }
}
