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

package org.apache.geronimo.mavenplugins.geronimo.module;

import java.io.File;
import java.io.IOException;

import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressObject;

import org.apache.geronimo.deployment.plugin.GeronimoDeploymentManager;
import org.apache.geronimo.system.plugin.DownloadResults;
import org.apache.geronimo.system.plugin.PluginInstallerGBean;
import org.apache.geronimo.system.plugin.model.ArtifactType;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.geronimo.system.plugin.model.PluginListType;
import org.apache.geronimo.system.plugin.model.PluginType;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Deploy modules (and optionally starting them) to a Geronimo server.
 *
 * @version $Rev$ $Date$
 * @goal install-plugin
 */
public class InstallPluginMojo
        extends ModuleMojoSupport {
    /**
     * Flag to indicate if modules should be started after they have been distributed to the server.
     *
     * @parameter default-value="true"
     * @optional
     */
    private boolean startModules = false;

    /**
     * maven artifact
     *
     * @parameter expression="${project.artifact}"
     * @readonly
     * @required
     */
    private Artifact projectArtifact;

    /**
     * maven artifact
     *
     * @parameter
     */
    private String moduleId;

    /**
     * local repository location
     *
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    private ArtifactRepository artifactRepository;

    /**
     * Default geronimo repository matching the geronimo version to install to
     *
     * @parameter
     */
    private String defaultGeronimoRepository;

    @Override
    public void execute() throws MojoExecutionException {
        if (moduleId == null) {
            moduleId = projectArtifact.getGroupId() + "/" + projectArtifact.getArtifactId() + "/" + projectArtifact.getVersion() + "/" + projectArtifact.getType();
            getLog().info("using project artifact");
        }
        getLog().info("moduleId: " + moduleId);
        getLog().info("artifactRepository: " + artifactRepository);

        org.apache.geronimo.kernel.repository.Artifact geronimoArtifact = org.apache.geronimo.kernel.repository.Artifact.create(moduleId);

        PluginListType pluginListType = new PluginListType();
        String defaultRepository = artifactRepository.getBasedir();
        if (defaultGeronimoRepository != null) {
            pluginListType.getDefaultRepository().add(defaultGeronimoRepository);
            defaultRepository = defaultGeronimoRepository;
        }
        pluginListType.getDefaultRepository().add(artifactRepository.getBasedir());
        PluginType pluginType = new PluginType();
        pluginListType.getPlugin().add(pluginType);
        PluginArtifactType instance = new PluginArtifactType();
        pluginType.getPluginArtifact().add(instance);
        ArtifactType artifactType = PluginInstallerGBean.toArtifactType(geronimoArtifact);
        instance.setModuleId(artifactType);

        GeronimoDeploymentManager mgr = getDeploymentManager2();
        Object key = mgr.startInstall(pluginListType, defaultRepository, false, null, null);
//        Object key = mgr.startInstall(artifactFile, defaultRepository, false, null, null);
        waitTillDone(mgr, key);

        if (startModules) {
            getLog().info("Starting modules...");
            Target[] allTargets = mgr.getTargets();
            TargetModuleID[] allModules;
            try {
                allModules = mgr.getAvailableModules(null, allTargets);
            } catch (TargetException e) {
                throw new MojoExecutionException("Unable to load module list from server", e);
            }
            TargetModuleID id = null;
            for (TargetModuleID test : allModules) {
                if (geronimoArtifact.matches(org.apache.geronimo.kernel.repository.Artifact.create(test.getModuleID()))) {
                    id = test;
                    break;
                }
            }
            if (id == null) {
                throw new MojoExecutionException("Just installed plugin cannot be located");
            }
            TargetModuleID[] ids = new TargetModuleID[]{id};

            ProgressObject progress = getDeploymentManager2().start(ids);
            DeploymentStatus status = null;
            try {
                status = waitFor(progress);
            } catch (InterruptedException e) {
                throw new MojoExecutionException("Interrupted waiting for start completion", e);
            }

            if (status.isFailed()) {
                throw new MojoExecutionException("Failed to start modules: " + status.getMessage());
            }

            getLog().info("Started module(s):");
            logModules(ids, "    ");
        }
    }

    private GeronimoDeploymentManager getDeploymentManager2() throws MojoExecutionException {
        try {
            return (GeronimoDeploymentManager) getDeploymentManager();
        } catch (IOException e) {
            throw new MojoExecutionException("could not communicate with server", e);
        } catch (DeploymentManagerCreationException e) {
            throw new MojoExecutionException("Could not create deployment manager", e);
        }
    }

    static DownloadResults waitTillDone(GeronimoDeploymentManager mgr, Object key) throws MojoExecutionException {
        while (true) {
            DownloadResults results = mgr.checkOnInstall(key);
            if (results.isFinished()) {
                if (results.isFailed()) {
                    throw new MojoExecutionException("Failed to install plugin");
                }
                return results;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new MojoExecutionException("Failed to install plugin, interrupted");
            }
        }
    }


    protected String getFullClassName() {
        return this.getClass().getName();
    }
}