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

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collections;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.basic.BasicKernel;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.system.configuration.RepositoryConfigurationStore;
import org.apache.geronimo.system.plugin.DownloadResults;
import org.apache.geronimo.system.plugin.PluginInstallerGBean;
import org.apache.geronimo.system.plugin.SourceRepository;
import org.apache.geronimo.system.plugin.PluginRepositoryList;
import org.apache.geronimo.system.plugin.PluginRepositoryDownloader;
import org.apache.geronimo.system.plugin.model.ArtifactType;
import org.apache.geronimo.system.plugin.model.AttributesType;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.geronimo.system.plugin.model.PluginListType;
import org.apache.geronimo.system.plugin.model.PluginType;
import org.apache.geronimo.system.resolver.AliasedArtifactResolver;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

/**
 * Installs Geronimo module CAR files into a target repository to support assembly.
 *
 * @version $Rev$ $Date$
 * @goal install-modules
 */
public class InstallModulesMojo extends AbstractCarMojo {
    /**
     * The location of the server repository.
     *
     * @parameter expression="${project.build.directory}/assembly"
     * @required
     */
    private File targetServerDirectory = null;

    /**
     * The location of the target repository.
     *
     * @parameter expression="system"
     * @required
     */
    private String targetRepositoryPath = null;

    /**
     * The location of the target config files.
     *
     * @parameter expression="var/config"
     * @required
     */
    private String targetConfigPath = null;

    /**
     * ServerInstance specific in plugin configuration, to specify where config.xml and properties updates go.
     * @parameter
     */
    private List<ServerInstance> servers;

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
     * The location of the target config files.
     *
     * @parameter expression="var/config/installed-plugins.properties"
     * @required
     */
    private String installedPluginsList;

    /**
     * The Geronimo repository artifact resolver.
     * <p/>
     * <p/>
     * Using a custom name here to prevent problems that happen when Plexus
     * injects the Maven resolver into the base-class.
     * </p>
     */
    private AliasedArtifactResolver geronimoArtifactResolver;

    private RepositoryConfigurationStore sourceStore;


    /**
     * @parameter expression="${project.build.directory}/classes/var/config/overrides"
     * @required
     */
    private File overridesDir;

    /**
     * @parameter
     */
    private List<Override> overrides;

    /**
     * Set of artifacts which have already been installed, so we can skip any processing.
     */
    private Set installedArtifacts = new HashSet();


    public void execute() throws MojoExecutionException, MojoFailureException {

        getDependencies(project, false);
        Maven2RepositoryAdapter.ArtifactLookup lookup = new ArtifactLookupImpl();
        SourceRepository sourceRepo = new Maven2RepositoryAdapter(dependencyArtifacts, lookup);
        PluginListType pluginList = new PluginListType();
        String localRepo = sourceRepository.getUrl();
        if ("file".equals(sourceRepository.getProtocol())) {
            File localRepoDir = new File(sourceRepository.getBasedir());
            localRepo = localRepoDir.toURI().toString();
        }
        pluginList.getDefaultRepository().add(localRepo);
        for (org.apache.maven.model.Repository repository: (List<org.apache.maven.model.Repository>)project.getRepositories()) {
            pluginList.getDefaultRepository().add(repository.getUrl());
        }

        if (artifact != null) {
            pluginList.getPlugin().add(toPluginType(Artifact.create(artifact)));
        } else {
            addDependencies(pluginList);
        }
        DownloadResults downloadPoller = new DownloadResults();
        String targetServerPath = targetServerDirectory.getAbsolutePath();

        PluginRepositoryList pluginRepoList = new PluginRepositoryDownloader(Collections.singletonMap(localRepo, (String[]) null), true);
        try {
            PluginInstallerGBean installer = new PluginInstallerGBean(targetRepositoryPath, targetServerPath, installedPluginsList, servers, pluginRepoList, null, null);
            installer.install(pluginList, sourceRepo, true, null, null, downloadPoller);
            if (overrides != null) {
                for (Override override: this.overrides) {
                    AttributesType attributes = override.getOverrides(overridesDir);
                    installer.mergeOverrides(override.getServer(), attributes);
                }
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Could not use plugin installer bean", e);
        }
        getLog().info("Installed plugins: ");
        for (Artifact artifact: downloadPoller.getInstalledConfigIDs()) {
            getLog().info("    " + artifact);
        }
        getLog().info("Installed dependencies: ");
        for (Artifact artifact: downloadPoller.getDependenciesInstalled()) {
            getLog().info("    " + artifact);
        }
        if (downloadPoller.isFailed()) {
            throw new MojoExecutionException("Could not download all dependencies", downloadPoller.getFailure());
        }
        cleanup();
    }


    private PluginType toPluginType(Artifact artifact) {
        PluginType plugin = new PluginType();
        PluginArtifactType instance = new PluginArtifactType();
        ArtifactType artifactType = PluginInstallerGBean.toArtifactType(artifact);
        instance.setModuleId(artifactType);
        plugin.getPluginArtifact().add(instance);
        return plugin;
    }

    /**
     * Retrieves all artifact dependencies.
     *
     * @param pluginList PluginListType to add dependencies to as PluginType instances.
     */
    protected void addDependencies(PluginListType pluginList) {

        org.apache.maven.artifact.Artifact artifact = project.getArtifact();
        if (artifact != null && ("car".equals(artifact.getType()) || "jar".equals(artifact.getType())) && artifact.getFile() != null) {
            pluginList.getPlugin().add(toPluginType(mavenToGeronimoArtifact(artifact)));
        }

        List<org.apache.maven.model.Dependency> projectArtifacts = project.getModel().getDependencies();
        if (projectArtifacts != null) {
            for (org.apache.maven.model.Dependency dependency: projectArtifacts) {
                if (dependency.getScope() == null || "compile".equals(dependency.getScope())) {
                    pluginList.getPlugin().add(toPluginType(mavenToGeronimoArtifact(dependency)));
                }
            }
        }

    }

}
