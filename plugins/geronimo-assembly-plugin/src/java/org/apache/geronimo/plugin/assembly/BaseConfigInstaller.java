/**
 *
 * Copyright 2005 The Apache Software Foundation
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
import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;

import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.repository.FileWriteMonitor;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.system.repository.FileSystemRepository;
import org.apache.geronimo.gbean.GBeanData;

/**
 * @version $Rev$ $Date$
 */
public class BaseConfigInstaller {
    /**
     * root file of the targetConfigStore and TargetRepository.  Typically $GERONIMO_HOME of the
     * geronimo server being assembled
     */
    protected File targetRoot;
    /**
     * location of the target config store relative to targetRoot.  Typically "config-store"
     */
    protected String targetConfigStore;
    /**
     * location of the target repository relative to targetRoot.  Typically "repository"
     */
    protected String targetRepository;
    /**
     * location of the configuration to be installed relative to the sourceRepository
     */
    private String artifact;
    /**
     * location of the source repository for the dependencies
     */
    private File sourceRepository;
    protected URI sourceRepositoryURI;

    public File getTargetRoot() {
        return targetRoot;
    }

    public void setTargetRoot(File targetRoot) {
        this.targetRoot = targetRoot;
    }

    public String getTargetConfigStore() {
        return targetConfigStore;
    }

    public void setTargetConfigStore(String targetConfigStore) {
        this.targetConfigStore = targetConfigStore;
    }

    public String getTargetRepository() {
        return targetRepository;
    }

    public void setTargetRepository(String targetRepository) {
        this.targetRepository = targetRepository;
    }

    public String getArtifact() {
        return artifact;
    }

    public void setArtifact(String artifact) {
        this.artifact = artifact;
    }

    public File getSourceRepository() {
        return sourceRepository;
    }

    public void setSourceRepository(File sourceRepository) {
        this.sourceRepository = sourceRepository;
        sourceRepositoryURI = sourceRepository.toURI();
    }

    public URI getSourceRepositoryURI() {
        return sourceRepositoryURI;
    }

    public void setSourceRepositoryURI(URI sourceRepositoryURI) {
        this.sourceRepositoryURI = sourceRepositoryURI;
    }

    protected void execute(InstallAdapter installAdapter, Repository sourceRepo, FileSystemRepository targetRepo) throws IOException, InvalidConfigException {
        URI configId = URI.create(artifact);
        execute(configId, installAdapter, sourceRepo,  targetRepo);
    }

    protected void execute(URI configId, InstallAdapter installAdapter, Repository sourceRepo, FileSystemRepository targetRepo) throws IOException, InvalidConfigException {
        if (installAdapter.containsConfiguration(configId)) {
            System.out.println("Configuration " + configId + " already present in configuration store");
            return;
        }
        GBeanData config = installAdapter.install(sourceRepo, configId);
        List dependencies = (List) config.getAttribute("dependencies");
        System.out.println("Installed configuration " + configId);

        FileWriteMonitor monitor = new StartFileWriteMonitor();

        for (Iterator iterator = dependencies.iterator(); iterator.hasNext();) {
            URI dependency = (URI) iterator.next();
            if (!sourceRepo.hasURI(dependency)) {
                throw new RuntimeException("Dependency: " + dependency + " not found in local maven repo: for configuration: " + artifact);
            }
            if (!targetRepo.hasURI(dependency)) {
                URL sourceURL = sourceRepo.getURL(dependency);
                InputStream in = sourceURL.openStream();
                targetRepo.copyToRepository(in, dependency, monitor);
            }
        }
        URI[] parentId = (URI[]) config.getAttribute("parentId");
        if (parentId != null) {
            for (int i = 0; i < parentId.length; i++) {
                URI parent = parentId[i];
                execute(parent, installAdapter, sourceRepo, targetRepo);
            }
        }
    }

    protected interface InstallAdapter {

        GBeanData install(Repository sourceRepo, URI configId) throws IOException, InvalidConfigException;

        boolean containsConfiguration(URI configID);
    }

    protected static class StartFileWriteMonitor implements FileWriteMonitor {
        public void writeStarted(String fileDescription) {
            System.out.println("Copying " + fileDescription);
        }

        public void writeProgress(int bytes) {

        }

        public void writeComplete(int bytes) {

        }
    }

    protected class InnerRepository implements Repository {

        public boolean hasURI(URI uri) {
            uri = resolve(uri);
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
            uri = resolve(uri);
            return uri.toURL();
        }

        /**
         * todo if the uri has a scheme, don't dissect it.
         *
         * @param uri
         * @return
         */
        private URI resolve(final URI uri) {
            String[] bits = uri.toString().split("/");
            StringBuffer buf = new StringBuffer(bits[0]).append('/');
            String type = bits.length >= 4 ? bits[3] : "jar";
            buf.append(type).append('s').append('/').append(bits[1]).append('-').append(bits[2]).append('.').append(type);
            return sourceRepositoryURI.resolve(buf.toString());
        }
    }
}
