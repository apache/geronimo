/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
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

    /**
     * Set of artifacts which have already been installed, so we can skip any processing.
     */
    private Set installedArtifacts = new HashSet();

    protected void doExecute() throws Exception {
        generateExplicitVersionProperties(explicitResolutionProperties);

        //
        // TODO: Check if we need to use the Maven2RepositoryAdapter here or not...
        //

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
            Iterator iter = getDependencies().iterator();
            while (iter.hasNext()) {

                Artifact artifact = mavenToGeronimoArtifact((org.apache.maven.artifact.Artifact) iter.next());
                if (isModuleArtifact(artifact)) {
                    install(artifact);
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

        org.apache.maven.artifact.Artifact artifact = project.getArtifact();
        if (artifact != null && artifact.getFile() != null) {
            dependenciesSet.add(artifact);
        }

        Set projectArtifacts = project.getArtifacts();
        if (projectArtifacts != null) {
            dependenciesSet.addAll(projectArtifacts);
        }

        return dependenciesSet;
    }

    /**
     * Install the given artifact into the target Geronimo repository.
     *
     * @param artifact      The artifact to be installed; must not be null
     *
     * @throws Exception    Failed to install artifact
     */
    private void install(final Artifact artifact) throws Exception {
        assert artifact != null;

        if (installedArtifacts.contains(artifact)) {
            log.debug("Skipping artifact; already installed: " + artifact);
        }
        else {
            // The artifact must exist in the source repository
            if (!sourceRepo.contains(artifact)) {
                throw new Exception("Missing artifact in source repository: " + artifact);
            }

            if (isModuleArtifact(artifact)) {
                installModule(artifact);
            }
            else {
                installDependency(artifact);
            }
        }
    }

    /**
     * Install a Geornimo module artifact.
     *
     * @param artifact      The Geronimo module artifact to be installed; must not be null, must be a module
     *
     * @throws Exception    Failed to insall Geronimo module artifact
     */
    private void installModule(final Artifact artifact) throws Exception {
        assert artifact != null;
        assert isModuleArtifact(artifact);

        boolean install = true;

        // The source store must contain the module artifact
        if (!sourceStore.containsConfiguration(artifact)) {
            throw new Exception("Missing module artifact in source repository: " + artifact);
        }

        // If the target store already contains the module, check if we need to reinstall it
        if (targetStore.containsConfiguration(artifact)) {
            if (hasModuleChanged(artifact)) {
                log.debug("Old module exists in target store; uninstalling: " + artifact);
                targetStore.uninstall(artifact);
            }
            else {
                log.debug("Same module exists in target store; skipping: " + artifact);
                install = false;
            }
        }

        // Copy the configuration into the target configuration store
        if (install) {
            log.info("Installing module: " + artifact);

            File file = sourceRepo.getLocation(artifact);
            InputStream input = new BufferedInputStream(new FileInputStream(file));

            try {
                FileWriteMonitor monitor = new FileWriteMonitor() {
                    public void writeStarted(final String file, final int bytes) {
                        log.debug("Installing module: " + file + " (" + bytes + " bytes)");
                    }

                    public void writeProgress(int bytes) {
                        // empty
                    }

                    public void writeComplete(int bytes) {
                        // empty
                    }
                };

                targetStore.install(input, (int)file.length(), artifact, monitor);

                installedArtifacts.add(artifact);
            }
            finally {
                input.close();
            }
        }

        // Install all dependencies of this module
        installModuleDependencies(artifact);
    }

    /**
     * Install all of the dependencies of the given Geronimo module artifact.
     *
     * @param artifact      The Geronimo module artifact to be installed; must not be null, must be a module
     *
     * @throws Exception    Failed to install Geronimo module dependencies
     */
    private void installModuleDependencies(final Artifact artifact) throws Exception {
        assert artifact != null;
        assert isModuleArtifact(artifact);

        log.debug("Installing module dependencies for artifact: " + artifact);

        try {
            ConfigurationData config = targetStore.loadConfiguration(artifact);
            Environment env = config.getEnvironment();
            LinkedHashSet deps = new LinkedHashSet();

            Iterator iter = env.getDependencies().iterator();
            while (iter.hasNext()) {
                Dependency dep = (Dependency) iter.next();
                deps.add(dep.getArtifact());
            }

            installDependencies(deps);
        }
        catch (IOException e) {
            throw new InvalidConfigException("Unable to load module: " + artifact, e);
        }
        catch (NoSuchConfigException e) {
            throw new InvalidConfigException("Unable to load module: " + artifact, e);
        }
    }

    /**
     * Install a dependency artifact into the Geronimo repository.
     *
     * @param artifact      The artifact to be installed; must not be null, or a module artifact
     *
     * @throws Exception    Failed to install artifact dependencies
     */
    private void installDependency(final Artifact artifact) throws Exception {
        assert artifact != null;
        assert !isModuleArtifact(artifact);

        boolean install = true;

        // If the dep already exists, then check if we need to reinstall it
        if (targetRepo.contains(artifact)) {
            if (hasDependencyChanged(artifact)) {
                File file = targetRepo.getLocation(artifact);
                log.debug("Old dependency exists in target repo; deleting: " + file);
                FileUtils.forceDelete(file);
            }
            else {
                log.debug("Same dependency exists in target repo; skipping: " + artifact);
                install = false;
            }
        }

        if (install) {
            log.info("Installing dependency: " + artifact);

            // Copy the artifact into the target repo
            File file = sourceRepo.getLocation(artifact);
            InputStream input = new BufferedInputStream(new FileInputStream(file));
            try {
                FileWriteMonitor monitor = new FileWriteMonitor() {
                    public void writeStarted(final String file, final int bytes) {
                        log.debug("Copying dependency: " + file + " (" + bytes + " bytes)");
                    }

                    public void writeProgress(int bytes) {
                        // empty
                    }

                    public void writeComplete(int bytes) {
                        // empty
                    }
                };
                
                targetRepo.copyToRepository(input, (int)file.length(), artifact, monitor);

                installedArtifacts.add(artifact);
            }
            finally {
                input.close();
            }
        }

        // Install all dependencies of this artifact
        installDependencies(sourceRepo.getDependencies(artifact));
    }

    /**
     * Install a set of dependency artifacts into the Geronimo repository.
     *
     * @param dependencies  The set of artifacts to be installed; must not be null.
     *
     * @throws Exception    Failed to install artifacts
     */
    private void installDependencies(final Set/*<Artifact>*/ dependencies) throws Exception {
        assert dependencies != null;

        Set resolved = geronimoArtifactResolver.resolveInClassLoader(dependencies);
        Iterator iter = resolved.iterator();

        while (iter.hasNext()) {
            Artifact a = (Artifact)iter.next();
            install(a);
        }
    }

    /**
     * Check if a module has changed by comparing the checksum in the source and target repos.
     *
     * @param module    The module to inspect; must not be null.
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
     * @param repo      The repository to resolve the artifacts location; must not be null.
     * @param artifact  The artifact to retrieve a checksum for; must not be null.
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

    /**
     * Check if a dependency has changed by checking the file size and last modified for source and target.
     *
     * @param artifact  The artifact to check; must not be null
     * @return          True if the dependency has changed
     */
    private boolean hasDependencyChanged(final Artifact artifact) {
        assert artifact != null;

        File source = sourceRepo.getLocation(artifact);
        File target = targetRepo.getLocation(artifact);

        return (source.length() != target.length()) ||
               (source.lastModified() > target.lastModified());
    }
}
