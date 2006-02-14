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

import java.io.File;
import java.io.IOException;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.system.configuration.LocalConfigStore;
import org.apache.geronimo.system.repository.Maven1Repository;
import org.apache.geronimo.system.repository.Maven2Repository;

/**
 * JellyBean that installs configuration artifacts into a LocalConfigurationStore,  It also copies all
 * configuration dependencies into the repository
 *
 * @version $Rev$ $Date$
 */
public class LocalConfigInstaller extends BaseConfigInstaller {

    public void execute() throws Exception {
        final LocalConfigStore store = new LocalConfigStore(new File(targetRoot, targetConfigStore));
        store.doStart();
        InstallAdapter installAdapter = new InstallAdapter() {

            public GBeanData install(Repository sourceRepo, Artifact configId) throws IOException, InvalidConfigException {
                File artifact = sourceRepo.getLocation(configId);
                GBeanData config = store.install2(artifact.toURL());
                return config;
            }

            public boolean containsConfiguration(Artifact configID) {
                return store.containsConfiguration(configID);
            }
        };
        Repository sourceRepo = new Maven1Repository(getSourceRepository());
        Maven2Repository targetRepo = new Maven2Repository(new File(targetRoot, targetRepository));

        try {
            execute(installAdapter, sourceRepo, targetRepo);
        } finally {
            store.doStop();
        }
    }
}
