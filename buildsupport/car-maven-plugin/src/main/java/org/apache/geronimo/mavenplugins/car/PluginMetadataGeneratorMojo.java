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

import org.apache.geronimo.system.plugin.PluginXmlUtil;
import org.apache.geronimo.system.plugin.model.ArtifactType;
import org.apache.geronimo.system.plugin.model.DependencyType;
import org.apache.geronimo.system.plugin.model.LicenseType;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.geronimo.system.plugin.model.PluginType;
import org.apache.maven.model.License;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.Resource;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Generate a geronimo-plugin.xml file based on configuration in the pom and car-maven-plugin configuration.
 *
 * @version $Rev$ $Date$
 * @goal prepare-metadata
 */
public class PluginMetadataGeneratorMojo
        extends AbstractCarMojo {


    /**
     * Directory for generated plugin metadata file.
     *
     * @parameter expression="${project.build.directory}/resources/"
     * @required
     */
    protected File targetDir = null;

    /**
     * Name of generated plugin metadata file.
     *
     * @parameter default-value="META-INF/geronimo-plugin.xml"
     * @required
     */
    protected String pluginMetadataFileName = null;

    /**
     * Full path of generated plugin metadata file.
     *
     * @ parameter expression="${project.build.directory}/resources/META-INF/geronimo-plugin.xml"
     * @required
     */
//    protected File targetFile = null;

    /**
     * Whether licenses (copied from maven licence elements) are OSI approved.
     *
     * @parameter default-value="false"
     */
    private boolean osiApproved;

    /**
     * Category of the geronimo plugin.
     *
     * @parameter
     */
    private String category;

    /**
     * Dependencies explicitly listed in the car-maven-plugin configuration
     *
     * @parameter
     */
    private List<Dependency> dependencies = Collections.emptyList();

    /**
     * Configuration of use of maven dependencies.  If missing or if value element is false, use the explicit list in the car-maven-plugin configuration.
     * If present and true, use the maven dependencies in the current pom file of scope null, runtime, or compile.  In addition, the version of the maven
     * dependency can be included or not depending on the includeVersion element.
     *
     * @parameter
     */
    private UseMavenDependencies useMavenDependencies;

    /**
     * Shared configuration from parent that we merge since maven is incompetent at it.  This is a plugin-artifactType element without moduleId or dependencies.
     * Do not attempt to include more than one of these in the parent poms since maven will not merge them correctly.
     *
     * @parameter
     */
    private PlexusConfiguration commonInstance;

    /**
     * Configuration for this instance itself.  This is a plugin-artifactType element without moduleId or dependencies. Do not include more than one of these in the parent poms
     * since maven will not merge them correctly.  Actually we have to fish this out of the model since maven mangles the xml for us.
     *
     */
//    private PlexusConfiguration instance;

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
        Plugin plugin = (Plugin) project.getModel().getBuild().getPluginsAsMap().get("org.apache.geronimo.buildsupport:car-maven-plugin");
        if (plugin == null) {
            throw new Error("Unable to resolve car plugin");
        }
        
        Xpp3Dom dom;
        if (plugin.getExecutions().isEmpty()) {
            dom = (Xpp3Dom) plugin.getConfiguration();
        } else {
            if (plugin.getExecutions().size() > 1) {
                throw new IllegalStateException("Cannot determine correct configuration for PluginMetadataGeneratorMojo: " + plugin.getExecutionsAsMap().keySet());
            }
            dom = (Xpp3Dom) ((PluginExecution)plugin.getExecutions().get(0)).getConfiguration();
        }
        Xpp3Dom instanceDom = dom.getChild("instance");

        if (instanceDom == null || instanceDom.getChild("plugin-artifact") == null) {
            instance = new PluginArtifactType();
        } else {
            String instanceString = instanceDom.getChild("plugin-artifact").toString();
            instance = PluginXmlUtil.loadPluginArtifactMetadata(new StringReader(instanceString.replace("#{", "${")));
        }
        if (this.commonInstance != null && this.commonInstance.getChild("plugin-artifact") != null) {
            PluginArtifactType commonInstance = PluginXmlUtil.loadPluginArtifactMetadata(new StringReader(this.commonInstance.getChild("plugin-artifact").toString().replace("#{", "${")));
            //merge
            if (instance.getArtifactAlias().isEmpty()) {
                instance.getArtifactAlias().addAll(commonInstance.getArtifactAlias());
            }
            if (instance.getConfigSubstitution().isEmpty()) {
                instance.getConfigSubstitution().addAll(commonInstance.getConfigSubstitution());
            }
            //it doesn't make sense to copy a "generic" config.xml content into a specific module.
//            if ((instance.getConfigXmlContent() == null || instance.getConfigXmlContent().getGbean().isEmpty())
//                    && (commonInstance.getConfigXmlContent() != null && !commonInstance.getConfigXmlContent().getGbean().isEmpty())) {
//                instance.setConfigXmlContent(new ConfigXmlContentType());
//                instance.getConfigXmlContent().getGbean().addAll(commonInstance.getConfigXmlContent().getGbean());
//            }
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
        ArtifactType existingArtifact = instance.getModuleId();
        if (existingArtifact != null && existingArtifact.getType() != null) {
            artifactType.setType(existingArtifact.getType());
        } else {
            artifactType.setType(project.getArtifact().getType());
        }
        instance.setModuleId(artifactType);
        addDependencies(instance);
        targetDir.mkdirs();
        File targetFile = new File(targetDir.toURI().resolve(pluginMetadataFileName));
        targetFile.getParentFile().mkdirs();
        FileOutputStream out = new FileOutputStream(targetFile);
        try {
            PluginXmlUtil.writePluginMetadata(metadata, out);
        } finally {
            out.close();
        }
        Resource resource = new Resource();
        resource.setDirectory(targetDir.getPath());
        resource.addInclude(pluginMetadataFileName);
        getProject().getResources().add(resource);
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
