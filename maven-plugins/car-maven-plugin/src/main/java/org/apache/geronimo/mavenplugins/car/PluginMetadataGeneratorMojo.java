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

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;

import org.apache.geronimo.system.plugin.PluginInstallerGBean;
import org.apache.geronimo.system.plugin.model.ArtifactType;
import org.apache.geronimo.system.plugin.model.DependencyType;
import org.apache.geronimo.system.plugin.model.LicenseType;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.geronimo.system.plugin.model.PluginType;
import org.apache.maven.model.License;
import org.codehaus.plexus.configuration.PlexusConfiguration;

/**
 * Add dependencies to a plan and process with velocity
 *
 * @version $Rev$ $Date$
 * @goal prepare-metadata
 */
public class PluginMetadataGeneratorMojo
        extends AbstractCarMojo {


    /**
     * @parameter expression="${project.build.directory}/resources/META-INF"
     * @required
     */
    protected File targetDir = null;

    /**
     * @parameter default-value="geronimo-plugin.xml"
     * @required
     */
    protected String pluginMetadataFileName = null;

    /**
     * @parameter expression="${project.build.directory}/resources/META-INF/geronimo-plugin.xml"
     * @required
     */
    protected File targetFile = null;

    /**
     * @parameter default-value="false"
     */
    private boolean osiApproved;

    /**
     * @parameter
     */
    private List<String> geronimoVersions = Collections.emptyList();

    /**
     * @parameter
     */
    private List<String> jvmVersions = Collections.emptyList();

    /**
     * @parameter
     */
    private String category;

    /**
     * @parameter
     */
    private List<Dependency> dependencies = Collections.emptyList();

    /**
     * @parameter
     */
    private UseMavenDependencies useMavenDependencies;

    /**
     * shared configuration from parent that we merge since maven is incompetent at it.
     *
     * @parameter
     */
    private PlexusConfiguration commonInstance;

    /**
     * configuration for this instance itself
     *
     * @parameter
     */
    private PlexusConfiguration instance;

    protected void doExecute() throws Exception {

        PluginType metadata = new PluginType();
        metadata.setName(project.getName());
        metadata.setAuthor(project.getOrganization().getName());
        metadata.setUrl(project.getOrganization().getUrl());
        metadata.setDescription(project.getDescription());
        metadata.setCategory(category);

        for (Object licenseObj : project.getLicenses()) {
            License license = (License) licenseObj;
            LicenseType licenseType = new LicenseType();
            licenseType.setValue(license.getName());
            licenseType.setOsiApproved(osiApproved);
            metadata.getLicense().add(licenseType);
        }

        PluginArtifactType instance;
        if (this.instance == null || this.instance.getChild("plugin-artifact") == null) {
            instance = new PluginArtifactType();
        } else {
            instance = PluginInstallerGBean.loadPluginArtifactMetadata(new StringReader(this.instance.getChild("plugin-artifact").toString().replace("#{", "${")));
        }
        if (this.commonInstance != null && this.commonInstance.getChild("plugin-artifact") != null) {
            PluginArtifactType commonInstance = PluginInstallerGBean.loadPluginArtifactMetadata(new StringReader(this.commonInstance.getChild("plugin-artifact").toString().replace("#{", "${")));
            //merge
            if (instance.getArtifactAlias().isEmpty()) {
                instance.getArtifactAlias().addAll(commonInstance.getArtifactAlias());
            }
            if (instance.getConfigSubstitution().isEmpty()) {
                instance.getConfigSubstitution().addAll(commonInstance.getConfigSubstitution());
            }
            if ((instance.getConfigXmlContent() == null || instance.getConfigXmlContent().getGbean().isEmpty())
                    && (commonInstance.getConfigXmlContent() != null && !commonInstance.getConfigXmlContent().getGbean().isEmpty())) {
                instance.setConfigXmlContent(new PluginArtifactType.ConfigXmlContent());
                instance.getConfigXmlContent().getGbean().addAll(commonInstance.getConfigXmlContent().getGbean());
            }
            if (instance.getCopyFile().isEmpty()) {
                instance.getCopyFile().addAll(commonInstance.getCopyFile());
            }
            if (instance.getDependency().isEmpty()) {
                instance.getDependency().addAll(commonInstance.getDependency());
            }
            if (instance.getGeronimoVersion().isEmpty()) {
                instance.getGeronimoVersion().addAll(commonInstance.getGeronimoVersion());
            }
            if (instance.getJvmVersion().isEmpty()) {
                instance.getJvmVersion().addAll(commonInstance.getJvmVersion());
            }
            if (instance.getObsoletes().isEmpty()) {
                instance.getObsoletes().addAll(commonInstance.getObsoletes());
            }
            if (instance.getPrerequisite().isEmpty()) {
                instance.getPrerequisite().addAll(commonInstance.getPrerequisite());
            }
            if (instance.getSourceRepository().isEmpty()) {
                instance.getSourceRepository().addAll(commonInstance.getSourceRepository());
            }
        }
        metadata.getPluginArtifact().add(instance);

        ArtifactType artifactType = new ArtifactType();
        artifactType.setGroupId(project.getGroupId());
        artifactType.setArtifactId(project.getArtifactId());
        artifactType.setVersion(project.getVersion());
        artifactType.setType(project.getArtifact().getType());
        instance.setModuleId(artifactType);
        addDependencies(instance);
        targetDir.mkdirs();
        FileOutputStream out = new FileOutputStream(targetFile);
        try {
            PluginInstallerGBean.writePluginMetadata(metadata, out);
        } finally {
            out.close();
        }
        getProject().getResources().add(targetFile);
    }

    private void addDependencies(PluginArtifactType instance) {
        if (useMavenDependencies == null || !useMavenDependencies.isValue()) {
            for (Dependency dependency : dependencies) {
                instance.getDependency().add(dependency.toDependencyType());
            }
        } else {
            List<org.apache.maven.model.Dependency> includedDependencies = project.getOriginalModel().getDependencies();
            List<org.apache.maven.model.Dependency> artifacts = project.getDependencies();
            for (org.apache.maven.model.Dependency dependency : includedDependencies) {
                dependency = resolveDependency(dependency, artifacts);
                if (includeDependency(dependency)) {
                    DependencyType gdep = toGeronimoDependency(dependency, useMavenDependencies.isIncludeVersion());
                    instance.getDependency().add(gdep);
                }
            }

        }
    }

    private static DependencyType toGeronimoDependency(final org.apache.maven.model.Dependency dependency, boolean includeVersion) {
        DependencyType dependencyType = new DependencyType();
        dependencyType.setGroupId(dependency.getGroupId());
        dependencyType.setArtifactId(dependency.getArtifactId());
        if (includeVersion) {
            dependencyType.setVersion(dependency.getVersion());
        }
        dependencyType.setType(dependency.getType());
        dependencyType.setStart(true);
        return dependencyType;
    }
}
