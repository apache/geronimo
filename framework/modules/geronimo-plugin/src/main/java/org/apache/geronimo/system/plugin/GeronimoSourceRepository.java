/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.system.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.security.auth.login.FailedLoginException;

import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.FileWriteMonitor;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.WritableListableRepository;
import org.apache.geronimo.kernel.repository.WriteableRepository;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.system.configuration.RepositoryConfigurationStore;
import org.apache.geronimo.system.plugin.model.PluginListType;
import org.apache.geronimo.system.plugin.model.PluginType;
import org.apache.geronimo.system.plugin.model.PluginXmlUtil;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoSourceRepository implements SourceRepository {

    private final Collection<? extends Repository> repos;
    private final ArtifactResolver artifactResolver;

    public GeronimoSourceRepository(Collection<? extends Repository> repos, ArtifactResolver artifactResolver) {
        this.repos = repos;
        this.artifactResolver = artifactResolver;
    }

    public PluginListType getPluginList() {
        Map<PluginType, PluginType> pluginMap = new HashMap<PluginType, PluginType>();
        for (Repository listableRepository : repos) {
            if (listableRepository instanceof ListableRepository) {
                SortedSet<Artifact> artifacts = ((ListableRepository) listableRepository).list();
                for (Artifact artifact : artifacts) {
                    File location = listableRepository.getLocation(artifact);
                    PluginType data = extractPluginMetadata(location);
                    if (data != null) {
                        PluginType key = PluginInstallerGBean.toKey(data);
                        PluginType existing = pluginMap.get(key);
                        if (existing == null) {
                            pluginMap.put(key, data);
                        } else {
                            existing.getPluginArtifact().addAll(data.getPluginArtifact());
                        }
                    }
                }
            }
        }
        PluginListType pluginList = new PluginListType();
        pluginList.getPlugin().addAll(pluginMap.values());
        return pluginList;
    }

    public OpenResult open(Artifact artifact, FileWriteMonitor monitor) throws IOException, FailedLoginException {
        try {
            artifact = artifactResolver.resolveInClassLoader(artifact);
        } catch (MissingDependencyException e) {
            return null;
        }
        for (Repository repo: repos) {
            if (repo.contains(artifact)) {
                File location = repo.getLocation(artifact);
                if (location.isFile()) {
                    return new LocalOpenResult(artifact, location);
                }
                if (location.isDirectory()) {
                    return new ZipOpenResult(artifact, repo);
                }
            }
        }
        return null;
    }

    PluginType extractPluginMetadata(Artifact artifact) {
        for (Repository repo: repos) {
            if (repo.contains(artifact)) {
                File location = repo.getLocation(artifact);
                return extractPluginMetadata(location);
            }
        }
        return null;
    }

    static PluginType extractPluginMetadata(File dir) {
        try {
            if (dir.isDirectory()) {
                File meta = new File(dir, "META-INF");
                if (!meta.isDirectory() || !meta.canRead()) {
                    return null;
                }
                File xml = new File(meta, "geronimo-plugin.xml");
                if (!xml.isFile() || !xml.canRead() || xml.length() == 0) {
                        return null;
                }
                InputStream in = new FileInputStream(xml);
                try {
                    return PluginXmlUtil.loadPluginMetadata(in);
                } finally {
                    in.close();
                }
            } else {
                if (!dir.isFile() || !dir.canRead()) {
                    throw new IllegalStateException("Cannot read configuration " + dir.getAbsolutePath());
                }
                JarFile jar = new JarFile(dir);
                try {
                    ZipEntry entry = jar.getEntry("META-INF/geronimo-plugin.xml");
                    if (entry == null) {
                            return null;
                    }
                    InputStream in = jar.getInputStream(entry);
                    try {
                        return PluginXmlUtil.loadPluginMetadata(in);
                    } finally {
                        in.close();
                    }
                } finally {
                    jar.close();
                }
            }
        } catch (Exception e) {
            //ignore
        }
        return null;
    }

    public String toString() {
        return getClass().getName();
    }

    private static class RepoWrapper implements WritableListableRepository {

        private final Repository repo;

        private RepoWrapper(Repository repo) {
            this.repo = repo;
        }

        public void copyToRepository(File source, Artifact destination, FileWriteMonitor monitor) throws IOException {
        }

        public void copyToRepository(InputStream source, int size, Artifact destination, FileWriteMonitor monitor) throws IOException {
        }

        public boolean contains(Artifact artifact) {
            return repo.contains(artifact);
        }

        public File getLocation(Artifact artifact) {
            return repo.getLocation(artifact);
        }

        public SortedSet<Artifact> list() {
            return null;
        }

        public SortedSet<Artifact> list(Artifact query) {
            return null;
        }
    }

    private class ZipOpenResult implements OpenResult {
        private final Artifact artifact;
        private final Repository repo;
        File location;

        private ZipOpenResult(Artifact artifact, Repository repo) {
            this.artifact = artifact;
            this.repo = repo;
        }

        public Artifact getArtifact() {
            return artifact;
        }

        public File getFile() throws IOException {
            location = File.createTempFile("geronimo-plugin-download-", ".tmp");
            OutputStream output = new FileOutputStream(location);
            WritableListableRepository writableRepo = new RepoWrapper(repo);
            ConfigurationStore store = new RepositoryConfigurationStore(writableRepo);
            try {
                store.exportConfiguration(artifact, output);
            } catch (NoSuchConfigException e) {
                throw (IOException)new IOException("Could not locate artefact " + artifact).initCause(e);
            }

            return location;
        }

        public void install(WriteableRepository repo, FileWriteMonitor monitor) throws IOException {
            File file = getFile();
            repo.copyToRepository(file, artifact, monitor);
            if (!file.delete()) {
//                log.warn("Unable to delete temporary download file " + tempFile.getAbsolutePath());
                file.deleteOnExit();
            }
        }

        public void close() {
        }
    }
}
