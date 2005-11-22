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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

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
        Repository sourceRepo = new Repository() {

            public boolean hasURI(URI uri) {
                uri = sourceRepositoryURI.resolve(uri);
                if ("file".equals(uri.getScheme())) {
                    return new File(uri).canRead();
                } else {
                    try {
                        uri.toURL().openStream().close();
                        return true;
                    } catch (IOException e) {
                        return false;
                    }
                }
            }

            public URL getURL(URI uri) throws MalformedURLException {
                uri = sourceRepositoryURI.resolve(uri);
                return uri.toURL();
            }
        };
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

        public List install(Repository sourceRepo, String artifactPath) throws IOException, InvalidConfigException {
            URI destination = URI.create(artifactPath);
            URL sourceURL = sourceRepo.getURL(destination);
            InputStream in = sourceURL.openStream();
            try {
                if (!targetRepo.hasURI(destination)) {
                    targetRepo.copyToRepository(in, destination, new StartFileWriteMonitor());
                }
            } finally {
                in.close();
            }
            URL targetURL = targetRepo.getURL(destination);
            GBeanData config = new GBeanData();
            URL baseURL = new URL("jar:" + targetURL.toString() + "!/");
            InputStream jis = null;
            try {
                URL stateURL = new URL(baseURL, "META-INF/config.ser");
                jis = stateURL.openStream();
                ObjectInputStream ois = new ObjectInputStream(jis);
                config.readExternal(ois);
            } catch (ClassNotFoundException e) {
                throw new InvalidConfigException("Unable to load class from config: " + destination, e);
            } finally {
                if (jis != null) {
                    jis.close();
                }
            }
            List dependencies = (List) config.getAttribute("dependencies");
            return dependencies;
        }
    }

}
