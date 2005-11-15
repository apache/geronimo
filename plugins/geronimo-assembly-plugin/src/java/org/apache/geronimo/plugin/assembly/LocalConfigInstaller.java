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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.repository.FileWriteMonitor;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.system.configuration.LocalConfigStore;
import org.apache.geronimo.system.repository.FileSystemRepository;

/**
 * JellyBean that installs configuration artifacts into a LocalConfigurationStore
 *
 * @version $Rev: 156292 $ $Date: 2005-03-05 18:48:02 -0800 (Sat, 05 Mar 2005) $
 */
public class LocalConfigInstaller {
    private File root;
    private String configStore;
    private String repository;
    private File artifact;
    private File mavenRepoLocal;
    private URI mavenRepoLocalURI;

    public File getRoot() {
        return root;
    }

    public void setRoot(File root) {
        this.root = root;
    }

    public String getConfigStore() {
        return configStore;
    }

    public void setConfigStore(String configStore) {
        this.configStore = configStore;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public File getArtifact() {
        return artifact;
    }

    public void setArtifact(File artifact) {
        this.artifact = artifact;
    }

    public File getMavenRepoLocal() {
        return mavenRepoLocal;
    }

    public void setMavenRepoLocal(File mavenRepoLocal) {
        this.mavenRepoLocal = mavenRepoLocal;
        mavenRepoLocalURI = mavenRepoLocal.toURI();
    }

    public void execute() throws Exception {
        LocalConfigStore store = new LocalConfigStore(new File(root, configStore));
        store.doStart();
        GBeanData config;
        try {
            config = store.install2(artifact.toURL());
            System.out.println("Installed configuration " + config.getAttribute("id"));
        } finally{
            store.doStop();
        }
        URI rootURI = root.toURI().resolve(repository);
        Repository sourceRepo = new Repository() {

            public boolean hasURI(URI uri) {
                uri = mavenRepoLocalURI.resolve(uri);
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
                uri = mavenRepoLocalURI.resolve(uri);
                return uri.toURL();
            }
        };

        FileSystemRepository targetRepo = new FileSystemRepository(rootURI, null);
        targetRepo.doStart();
        List dependencies = (List) config.getAttribute("dependencies");
        FileWriteMonitor monitor = new FileWriteMonitor() {

            public void writeStarted(String fileDescription) {
                System.out.println("Copying " + fileDescription);
            }

            public void writeProgress(int bytes) {

            }

            public void writeComplete(int bytes) {

            }
        };

        for (Iterator iterator = dependencies.iterator(); iterator.hasNext();) {
            URI dependency = (URI) iterator.next();
            if (!sourceRepo.hasURI(dependency)) {
                throw new RuntimeException("Dependency: " + dependency + " not found in local maven repo: for configuration: " + config.getAttribute("id"));
            }
            if (!targetRepo.hasURI(dependency)) {
                URL sourceURL = sourceRepo.getURL(dependency);
                InputStream in = sourceURL.openStream();
                targetRepo.copyToRepository(in, dependency, monitor);
            }
        }
    }
}
