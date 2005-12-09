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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URI;
import java.net.URL;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.system.repository.FileSystemRepository;

/**
 * JellyBean that installs configuration artifacts into a repository based ConfigurationStore,  It also copies all
 * configuration dependencies into the repository
 *
 * @version $Rev: 156292 $ $Date: 2005-03-05 18:48:02 -0800 (Sat, 05 Mar 2005) $
 */
public class RepoConfigInstaller extends BaseConfigInstaller {

    public void execute() throws Exception {
        Repository sourceRepo = new InnerRepository();
        URI rootURI = targetRoot.toURI().resolve(targetRepository);
        FileSystemRepository targetRepo = new FileSystemRepository(rootURI, null);
        InstallAdapter installAdapter = new CopyConfigStore(targetRepo);
        targetRepo.doStart();

        try {
            execute(installAdapter, sourceRepo, targetRepo);
        } finally {
            targetRepo.doStop();
        }

    }

    private static class CopyConfigStore implements InstallAdapter {

        private final FileSystemRepository targetRepo;

        public CopyConfigStore(FileSystemRepository targetRepo) {
            this.targetRepo = targetRepo;
        }

        public GBeanData install(Repository sourceRepo, URI configId) throws IOException, InvalidConfigException {
            URL sourceURL = sourceRepo.getURL(configId);
            InputStream in = sourceURL.openStream();
            try {
                if (!targetRepo.hasURI(configId)) {
                    targetRepo.copyToRepository(in, configId, new StartFileWriteMonitor());
                }
            } finally {
                in.close();
            }
            URL targetURL = targetRepo.getURL(configId);
            GBeanData config = new GBeanData();
            URL baseURL = new URL("jar:" + targetURL.toString() + "!/");
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
    }

}
