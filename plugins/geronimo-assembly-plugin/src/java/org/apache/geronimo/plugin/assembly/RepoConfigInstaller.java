/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.plugin.assembly;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.WritableListableRepository;
import org.apache.geronimo.system.repository.Maven1Repository;
import org.apache.geronimo.system.repository.Maven2Repository;
import org.apache.geronimo.system.configuration.RepositoryConfigurationStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * JellyBean that installs configuration artifacts into a repository based ConfigurationStore,  It also copies all
 * configuration dependencies into the repository
 *
 * @version $Rev$ $Date$
 */
public class RepoConfigInstaller extends BaseConfigInstaller {
    public void execute() throws Exception {
        ListableRepository sourceRepo = new Maven1Repository(getSourceRepository());

        WritableListableRepository targetRepo = new Maven2Repository(new File(targetRoot, targetRepository));
        RepositoryConfigurationStore configurationStore = new RepositoryConfigurationStore(targetRepo);
        InstallAdapter installAdapter = new CopyConfigStore(configurationStore);

        execute(installAdapter, sourceRepo, targetRepo);
   }

    private static class CopyConfigStore implements InstallAdapter {
        private final RepositoryConfigurationStore configurationStore;

        public CopyConfigStore(RepositoryConfigurationStore configurationStore) {
            this.configurationStore = configurationStore;
        }

        public GBeanData install(Repository sourceRepo, Artifact configId) throws IOException, InvalidConfigException {
            if (!configurationStore.containsConfiguration(configId)) {
                File sourceFile = sourceRepo.getLocation(configId);
                InputStream in = new FileInputStream(sourceFile);
                try {
                    configurationStore.install(in, configId, new StartFileWriteMonitor());
                } finally {
                    in.close();
                }
            }

            try {
                GBeanData config = configurationStore.loadConfiguration(configId);
                return config;
            } catch (IOException e) {
                throw new InvalidConfigException("Unable to load configuration: " + configId, e);
            } catch (NoSuchConfigException e) {
                throw new InvalidConfigException("Unable to load configuration: " + configId, e);
            }
        }

        public boolean containsConfiguration(Artifact configID) {
            return configurationStore.containsConfiguration(configID);
        }
    }
}
