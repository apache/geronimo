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
import java.net.URISyntaxException;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.system.repository.FileSystemRepository;

/**
 * JellyBean that installs configuration artifacts into a repository based ConfigurationStore,  It also copies all
 * configuration dependencies into the repository
 *
 * @version $Rev$ $Date$
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

        public GBeanData install(Repository sourceRepo, Artifact configId) throws IOException, InvalidConfigException {
            URI sourceURI;
            try {
                sourceURI = configId.toURI();
            } catch (URISyntaxException e) {
                throw new InvalidConfigException(e);
            }
            URL sourceURL = sourceRepo.getURL(sourceURI);
            InputStream in = sourceURL.openStream();
            try {
                if (!targetRepo.hasURI(sourceURI)) {
                    targetRepo.copyToRepository(in, sourceURI, new StartFileWriteMonitor());
                }
            } finally {
                in.close();
            }
            URL targetURL = targetRepo.getURL(sourceURI);
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

        public boolean containsConfiguration(Artifact configID) {
            try {
                return targetRepo.hasURI(configID.toURI());
            } catch (URISyntaxException e) {
                throw (IllegalArgumentException)new IllegalArgumentException("bad artifact").initCause(e);
            }
        }
    }

}
