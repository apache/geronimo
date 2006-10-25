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

package org.apache.geronimo.mavenplugins.car;

import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.FileWriteMonitor;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.WritableListableRepository;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.system.repository.Maven2Repository;
import org.apache.geronimo.system.configuration.RepositoryConfigurationStore;
import org.apache.geronimo.system.resolver.ExplicitDefaultArtifactResolver;

import org.apache.maven.artifact.repository.ArtifactRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;

import java.util.Iterator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.jar.JarFile;

import org.codehaus.plexus.util.FileUtils;

/**
 * Installs Geronimo module CAR files into a target repository to support assembly.
 *
 * @goal install-modules
 * 
 * @version $Rev$ $Date$
 */
public class InstallModulesMojo
    extends AbstractCarMojo
{
    /**
     * The location of the target repository.
     * 
     * @parameter expression="${project.build.directory}/repository"
     * @required
     */
    private File targetRepositoryDirectory = null;

    /**
     * Configuration to be installed specified as groupId/artifactId/version/type
     * if none specified, plugin will install all dependencies of type "car"
     *
     * @parameter
     * @optional
     */
    private String artifact = null;

    /**
     * Location of the source repository for the dependencies
     *
     * @parameter expression="${localRepository}"
     * @required
     */
    private ArtifactRepository sourceRepository = null;
    
    /**
     * The location where the properties mapping will be generated.
     *
     * @parameter expression="${project.build.directory}/explicit-versions.properties"
     * @required
     */
    private File explicitResolutionProperties = null;

    /**
     * The Geronimo repository artifact resolver.
     *
     * <p>
     * Using a custom name here to prevent problems that happen when Plexus
     * injects the Maven resolver into the base-class.
     * </p>
     */
    private ArtifactResolver geronimoArtifactResolver;

    private WritableListableRepository targetRepo;

    private RepositoryConfigurationStore targetStore;

    private WritableListableRepository sourceRepo;

    private RepositoryConfigurationStore sourceStore;

    protected void doExecute() throws Exception {
        generateExplicitVersionProperties(explicitResolutionProperties);
        
        sourceRepo = new Maven2Repository(new File(sourceRepository.getBasedir()));
        sourceStore = new RepositoryConfigurationStore(sourceRepo);

        FileUtils.forceMkdir(targetRepositoryDirectory);
        
        targetRepo = new Maven2Repository(targetRepositoryDirectory);
        targetStore = new RepositoryConfigurationStore(targetRepo);

        ArtifactManager artifactManager = new DefaultArtifactManager();
        geronimoArtifactResolver = new ExplicitDefaultArtifactResolver(
            explicitResolutionProperties.getPath(),
            artifactManager,
            Collections.singleton(sourceRepo),
            null);

        if (artifact != null) {
            install(Artifact.create(artifact));
        }
        else {
            Iterator itr = getDependencies().iterator();
            while (itr.hasNext()) {
                org.apache.maven.artifact.Artifact mavenArtifact = (org.apache.maven.artifact.Artifact) itr.next();
                if ("car".equals(mavenArtifact.getType())) {
                    install(new Artifact(mavenArtifact.getGroupId(), mavenArtifact.getArtifactId(), mavenArtifact.getVersion(), "car"));
                }
            }
        }
    }

    /**
     * Retrieves all artifact dependencies.
     *
     * @return A HashSet of artifacts
     */
    protected Set getDependencies() {
        Set dependenciesSet = new HashSet();

        if (project.getArtifact() != null && project.getArtifact().getFile() != null) {
            dependenciesSet.add(project.getArtifact());
        }

        Set projectArtifacts = project.getArtifacts();
        if (projectArtifacts != null) {
            dependenciesSet.addAll(projectArtifacts);
        }

        return dependenciesSet;
    }

    /**
     * Check if a module has changed by comparing the checksum in the source and target repos.
     *
     * @param module    The module to inspect
     * @return          Returns true if the module has changed
     *
     * @throws IOException      Failed to load checksum
     */
    private boolean hasModuleChanged(final Artifact module) throws IOException {
        assert module != null;

        String sourceChecksum = loadChecksum(sourceRepo, module);
        String targetChecksum = loadChecksum(targetRepo, module);

        return !sourceChecksum.equals(targetChecksum);
    }

    /**
     * Load the <tt>config.ser</tt> checksum for the given artifact.
     * 
     * @param repo      The repository to resolve the artifacts location.
     * @param artifact  The artifact to retrieve a checksum for
     * @return          Thr artifacts checksum
     *
     * @throws IOException  Failed to load checksums
     */
    private String loadChecksum(final Repository repo, final Artifact artifact) throws IOException {
        assert repo != null;
        assert artifact != null;

        File file = repo.getLocation(artifact);
        BufferedReader reader;

        if (file.isDirectory()) {
            File serFile = new File(file, "META-INF/config.ser.sha1");
            reader = new BufferedReader(new FileReader(serFile));
        }
        else {
            JarFile jarFile = new JarFile(file);
            ZipEntry entry = jarFile.getEntry("META-INF/config.ser.sha1");
            reader = new BufferedReader(new InputStreamReader(jarFile.getInputStream(entry)));
        }

        String checksum = reader.readLine();
        reader.close();

        return checksum;
    }

    private void install(final Artifact module) throws Exception {
        assert module != null;

        log.debug("Installing: " + module);
        
        LinkedHashSet dependencies;

        FileWriteMonitor monitor = new FileWriteMonitor() {
            public void writeStarted(String fileDescription, int fileSize) {
                log.debug("Copying: " + fileDescription);
            }

            public void writeProgress(int bytes) {
                // empty
            }

            public void writeComplete(int bytes) {
                // empty
            }
        };

        // does this configuration exist?
        if (!sourceRepo.contains(module)) {
            throw new NoSuchConfigException(module);
        }
        
        // is this config already installed?
        if (targetStore.containsConfiguration(module)) {
            if (hasModuleChanged(module)) {
                log.debug("Old module exists in target store; uninstalling: " + module);
                targetStore.uninstall(module);
            }
            else {
                log.debug("Same module exists in target store; skipping: " + module);
                return;
            }
        }
        
        if (sourceStore.containsConfiguration(module)) {
            // Copy the configuration into the target configuration store
            if (!targetStore.containsConfiguration(module)) {
                File sourceFile = sourceRepo.getLocation(module);
                InputStream in = new BufferedInputStream(new FileInputStream(sourceFile));
                try {
                    targetStore.install(in, (int)sourceFile.length(), module, monitor);
                }
                finally {
                    in.close();
                }
            }

            // Determine the dependencies of this configuration
            try {
                ConfigurationData configurationData = targetStore.loadConfiguration(module);
                Environment environment = configurationData.getEnvironment();
                dependencies = new LinkedHashSet();
                for (Iterator iterator = environment.getDependencies().iterator(); iterator.hasNext();) {
                    Dependency dependency = (Dependency) iterator.next();
                    dependencies.add(dependency.getArtifact());
                }

                log.info("Installed module: " + module);
            }
            catch (IOException e) {
                throw new InvalidConfigException("Unable to load module: " + module, e);
            }
            catch (NoSuchConfigException e) {
                throw new InvalidConfigException("Unable to load module: " + module, e);
            }
        }
        else {
            if (!sourceRepo.contains(module)) {
                throw new RuntimeException("Dependency not found in local maven repo: " + module + "; for module: " + artifact);
            }

            // Copy the artifact into the target repo
            if (!targetRepo.contains(module)) {
                File sourceFile = sourceRepo.getLocation(module);
                InputStream input = new BufferedInputStream(new FileInputStream(sourceFile));
                try {
                    targetRepo.copyToRepository(input, (int)sourceFile.length(), module, monitor);
                }
                finally {
                    input.close();
                }
            }

            // Determine the dependencies of this artifact
            dependencies = sourceRepo.getDependencies(module);
        }
        
        dependencies = geronimoArtifactResolver.resolveInClassLoader(dependencies);
        for (Iterator iterator = dependencies.iterator(); iterator.hasNext();) {
            Artifact a = (Artifact)iterator.next();
            install(a);
        }
    }
}
