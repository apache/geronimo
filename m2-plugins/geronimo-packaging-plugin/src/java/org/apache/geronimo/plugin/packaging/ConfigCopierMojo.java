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
import java.util.Iterator;
import java.util.List;

import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.WritableListableRepository;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

//
// TODO: Rename to InstallMojo
//

/**
 * Copies all the configurations out of source config store into target config store.
 *
 * @goal install
 *
 * @version $Rev$ $Date$
 */
public class ConfigCopierMojo extends AbstractPackagingMojo {

    private static final Class[] REPO_ARGS = { File.class };

    private static final Class[] STORE_ARGS = { WritableListableRepository.class };

    /**
     * @parameter expression="org.apache.geronimo.system.repository.Maven2Repository"
     */
    private String sourceRepositoryClass;

    /**
     * @parameter expression="org.apache.geronimo.system.repository.Maven2Repository"
     */
    private String targetRepositoryClass;

    /**
     * @parameter expression="org.apache.geronimo.system.configuration.RepositoryConfigurationStore"
     */
    private String sourceConfigurationStoreClass;

    /**
     * @parameter expression="org.apache.geronimo.plugin.packaging.MavenConfigStore"
     */
    private String targetConfigurationStoreClass;

    /**
     * @parameter expression="${project.build.directory}/repository"
     */
    private File sourceRepositoryLocation;

    /**
     * @parameter expression="${settings.LocalRepository}"
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
