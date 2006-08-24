/*
 *  Copyright 2005 The Apache Software Foundation
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

package org.apache.geronimo.plugin.car;

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
import org.apache.geronimo.system.repository.Maven2Repository;
import org.apache.geronimo.system.configuration.RepositoryConfigurationStore;
import org.apache.geronimo.system.resolver.ExplicitDefaultArtifactResolver;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;

import java.util.Iterator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Collections;

/**
 * Installs CAR files into a target repository to support assembly.
 *
 * @goal installConfig
 * 
 * @version $Rev$ $Date$
 */
public class InstallConfigMojo
    extends AbstractCarMojo
{
    /**
     * Root file of the TargetRepository.
     *
     * @parameter expression="${project.build.directory}"
     */
    private String targetRoot = null;

    /**
     * The location of the target repository relative to targetRoot.
     * 
     * @parameter default-value="archive-tmp/repository"
     * @required
     */
    private String targetRepository = null;

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

        File targetRepoFile = new File(targetRoot, targetRepository);
        if (!targetRepoFile.exists()) {
            targetRepoFile.mkdirs();
        }
        
        targetRepo = new Maven2Repository(targetRepoFile);
        targetStore = new RepositoryConfigurationStore(targetRepo);

        Artifact configId;
        ArtifactManager artifactManager = new DefaultArtifactManager();
        geronimoArtifactResolver = new ExplicitDefaultArtifactResolver(
            explicitResolutionProperties.getPath(),
            artifactManager,
            Collections.singleton(sourceRepo),
            null);

        if ( artifact != null ) {
            configId = Artifact.create(artifact);
            execute(configId);
        }
        else {
            Iterator itr = getDependencies().iterator();
            while (itr.hasNext()) {
                org.apache.maven.artifact.Artifact mavenArtifact = (org.apache.maven.artifact.Artifact) itr.next();
                if ("car".equals(mavenArtifact.getType())) {
                    configId = new Artifact(mavenArtifact.getGroupId(), mavenArtifact.getArtifactId(), mavenArtifact.getVersion(), "car");
                    execute(configId);
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

    private void execute(final Artifact configArtifact) throws Exception {
        assert configArtifact != null;

        LinkedHashSet dependencies;

        StartFileWriteMonitor monitor = new StartFileWriteMonitor(log);

        // does this configuration exist?
        if (!sourceRepo.contains(configArtifact)) {
            throw new NoSuchConfigException(configArtifact);
        }

        // is this config already installed?
        if (targetStore.containsConfiguration(configArtifact)) {
            log.info("Configuration already present in configuration store: " + configArtifact);
            return;
        }

        if (sourceStore.containsConfiguration(configArtifact)) {
            // Copy the configuration into the target configuration store
            if (!targetStore.containsConfiguration(configArtifact)) {
                File sourceFile = sourceRepo.getLocation(configArtifact);
                InputStream in = new BufferedInputStream(new FileInputStream(sourceFile));
                try {
                    targetStore.install(in, (int)sourceFile.length(), configArtifact, monitor);
                }
                finally {
                    in.close();
                }
            }

            // Determine the dependencies of this configuration
            try {
                ConfigurationData configurationData = targetStore.loadConfiguration(configArtifact);
                Environment environment = configurationData.getEnvironment();
                dependencies = new LinkedHashSet();
                for (Iterator iterator = environment.getDependencies().iterator(); iterator.hasNext();) {
                    Dependency dependency = (Dependency) iterator.next();
                    dependencies.add(dependency.getArtifact());
                }

                log.info("Installed configuration: " + configArtifact);
            }
            catch (IOException e) {
                throw new InvalidConfigException("Unable to load configuration: " + configArtifact, e);
            }
            catch (NoSuchConfigException e) {
                throw new InvalidConfigException("Unable to load configuration: " + configArtifact, e);
            }
        }
        else {
            if (!sourceRepo.contains(configArtifact)) {
                throw new RuntimeException("Dependency: " + configArtifact + " not found in local maven repo: for configuration: " + this.artifact);
            }

            // Copy the artifact into the target repo
            if (!targetRepo.contains(configArtifact)) {
                File sourceFile = sourceRepo.getLocation(configArtifact);
                InputStream in = new BufferedInputStream(new FileInputStream(sourceFile));
                try {
                    targetRepo.copyToRepository(in, (int)sourceFile.length(), configArtifact, monitor);
                }
                finally {
                    in.close();
                }
            }

            // Determine the dependencies of this artifact
            dependencies = sourceRepo.getDependencies(configArtifact);
        }
        
        dependencies = geronimoArtifactResolver.resolveInClassLoader(dependencies);
        for (Iterator iterator = dependencies.iterator(); iterator.hasNext();) {
            Artifact a = (Artifact)iterator.next();
            execute(a);
        }
    }

    private static class StartFileWriteMonitor
        implements FileWriteMonitor
    {
        private Log log;

        public StartFileWriteMonitor(final Log log) {
            this.log = log;
        }

        public void writeStarted(String fileDescription, int fileSize) {
            log.info("Copying: " + fileDescription);
        }

        public void writeProgress(int bytes) {
            // ???
        }

        public void writeComplete(int bytes) {
            // ???
        }
    }
}
