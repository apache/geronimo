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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.WriteableRepository;
import org.apache.geronimo.system.repository.Maven1Repository;
import org.apache.geronimo.system.repository.Maven2Repository;

/**
 * JellyBean that installs configuration artifacts into a repository based ConfigurationStore,  It also copies all
 * configuration dependencies into the repository
 *
 * @version $Rev$ $Date$
 */
public class RepoConfigInstaller extends BaseConfigInstaller {

    public void execute() throws Exception {
        Repository sourceRepo = new Maven1Repository(getSourceRepository());
        Maven2Repository targetRepo = new Maven2Repository(new File(targetRoot, targetRepository));
        InstallAdapter installAdapter = new CopyConfigStore(targetRepo);

        execute(installAdapter, sourceRepo, targetRepo);
   }

    private static class CopyConfigStore implements InstallAdapter {
        private final WriteableRepository targetRepo;

        public CopyConfigStore(WriteableRepository targetRepo) {
            this.targetRepo = targetRepo;
        }

        public GBeanData install(Repository sourceRepo, Artifact configId) throws IOException, InvalidConfigException {
            if (!targetRepo.contains(configId)) {
                File sourceFile = sourceRepo.getLocation(configId);
                InputStream in = new FileInputStream(sourceFile);
                try {
                    targetRepo.copyToRepository(in, configId, new StartFileWriteMonitor());
                } finally {
                    in.close();
                }
            }

            File targetFile = targetRepo.getLocation(configId);
            GBeanData config = new GBeanData();
            URL baseURL = new URL("jar:" + targetFile.toString() + "!/");
            InputStream jis = null;
            try {
                URL stateURL = new URL(baseURL, "META-INF/config.ser");
                jis = stateURL.openStream();
                ObjectInputStream ois = new ObjectInputStream(jis);
                config.readExternal(ois);
            } catch (ClassNotFoundException e) {
                throw new InvalidConfigException("Unable to load class from config: " + configId, e);
            } finally {
                if (jis != null) {
                    jis.close();
                }
            }
            return config;
        }

        public boolean containsConfiguration(Artifact configID) {
            return targetRepo.contains(configID);
        }
    }

}
