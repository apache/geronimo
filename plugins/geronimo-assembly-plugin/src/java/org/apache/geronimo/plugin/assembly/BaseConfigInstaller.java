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

import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.FileWriteMonitor;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.WritableListableRepository;
import org.apache.geronimo.system.repository.Maven1Repository;
import org.apache.geronimo.system.repository.Maven2Repository;
import org.apache.geronimo.system.configuration.RepositoryConfigurationStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * @version $Rev: 384686 $ $Date$
 */
public class BaseConfigInstaller {
    public static final FileWriteMonitor LOG_COPY_START = new StartFileWriteMonitor();

    /**
     * root file of the targetConfigStore and TargetRepository.  Typically $GERONIMO_HOME of the
     * geronimo server being assembled
     */
    private File targetRoot;

    /**
     * location of the target config store relative to targetRoot.  Typically "config-store"
     */
    private String targetConfigStore;

    /**
     * location of the target repository relative to targetRoot.  Typically "repository"
     */
    private String targetRepository;

    /**
     * location of the configuration to be installed relative to the sourceRepository
     */
    private String artifact;

    /**
     * location of the source repository for the dependencies
     */
    private File sourceRepository;

    private ArtifactResolver artifactResolver;

    private WritableListableRepository targetRepo;
    private RepositoryConfigurationStore targetStore;

    private WritableListableRepository sourceRepo;
    private RepositoryConfigurationStore sourceStore;

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
    }

    public void execute() throws Exception {
        ArtifactManager artifactManager = new DefaultArtifactManager();
        artifactResolver = new DefaultArtifactResolver(artifactManager, sourceRepo);

        sourceRepo = new Maven1Repository(getSourceRepository());
        sourceStore = new RepositoryConfigurationStore(sourceRepo);

        targetRepo = new Maven2Repository(new File(targetRoot, targetRepository));
        targetStore = new RepositoryConfigurationStore(targetRepo);

        Artifact configId = Artifact.create(artifact);

        // does this configuration exist?
        if (!sourceRepo.contains(configId)) {
            throw new NoSuchConfigException(configId);
        }

        // is this config already installed?
        if (targetStore.containsConfiguration(configId)) {
            System.out.println("Configuration " + configId + " already present in configuration store");
            return;
        }
        execute(configId);
    }

    private void execute(Artifact configId) throws IOException, InvalidConfigException, MissingDependencyException {
        LinkedHashSet dependencies;
        if (sourceStore.containsConfiguration(configId)) {
            // Copy the configuration into the target configuration store
            if (!targetStore.containsConfiguration(configId)) {
                File sourceFile = sourceRepo.getLocation(configId);
                InputStream in = new FileInputStream(sourceFile);
                try {
                    targetStore.install(in, configId, LOG_COPY_START);
                } finally {
                    in.close();
                }
            }

            // Determine the dependencies of this configuration
            try {
                ConfigurationData configurationData = targetStore.loadConfiguration(configId);
                Environment environment = configurationData.getEnvironment();
                dependencies = new LinkedHashSet();
                for (Iterator iterator = environment.getDependencies().iterator(); iterator.hasNext();) {
                    Dependency dependency = (Dependency) iterator.next();
                    dependencies.add(dependency.getArtifact());
                }

                System.out.println("Installed configuration " + configId);
            } catch (IOException e) {
                throw new InvalidConfigException("Unable to load configuration: " + configId, e);
            } catch (NoSuchConfigException e) {
                throw new InvalidConfigException("Unable to load configuration: " + configId, e);
            }
        } else {
            if (!sourceRepo.contains(configId)) {
                throw new RuntimeException("Dependency: " + configId + " not found in local maven repo: for configuration: " + artifact);
            }

            // Copy the artifact into the target repo
            if (!targetRepo.contains(configId)) {
                File sourceFile = sourceRepo.getLocation(configId);
                InputStream in = new FileInputStream(sourceFile);
                try {
                    targetRepo.copyToRepository(in, configId, LOG_COPY_START);
                } finally {
                    in.close();
                }
            }

            // Determine the dependencies of this artifact
            dependencies = sourceRepo.getDependencies(configId);
        }
        dependencies = artifactResolver.resolve(dependencies);
        for (Iterator iterator = dependencies.iterator(); iterator.hasNext();) {
            Artifact artifact = (Artifact) iterator.next();
            execute(artifact);
        }
    }

    private static class StartFileWriteMonitor implements FileWriteMonitor {
        public void writeStarted(String fileDescription) {
            System.out.println("Copying " + fileDescription);
        }

        public void writeProgress(int bytes) {

        }

        public void writeComplete(int bytes) {

        }
    }
}
