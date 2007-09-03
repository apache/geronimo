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
import java.util.Collections;
import java.util.List;

import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.system.plugin.PluginInstallerGBean;
import org.apache.geronimo.system.plugin.model.ArtifactType;
import org.apache.geronimo.system.plugin.model.LicenseType;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.geronimo.system.plugin.model.PluginType;
import org.apache.maven.model.License;

/**
 * Add dependencies to a plan and process with velocity
 *
 * @goal prepare-metadata
 *
 * @version $Rev$ $Date$
 */
public class PluginMetadataGeneratorMojo
        extends AbstractCarMojo
{

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
    private List<Prerequisite> prerequisites = Collections.emptyList();

    /**
     * @parameter
     */
    private List<Dependency> dependencies = Collections.emptyList();

    /**
     * @parameter
     */
    private List<ModuleId> obsoletes = Collections.emptyList();

    /**
     * @parameter
     */
    private List<String> sourceRepositories = Collections.emptyList();

    /**
     * @parameter
     */
    private List<CopyFile> copyFiles = Collections.emptyList();
    /**
     * @parameter
     */
    private List<Gbean> gbeans;

    protected void doExecute() throws Exception {

        PluginType metadata = new PluginType();
        metadata.setName(project.getName());
        metadata.setAuthor(project.getOrganization().getName());
        metadata.setUrl(project.getOrganization().getUrl());
        metadata.setDescription(project.getDescription());
        metadata.setCategory(category);

        for (Object licenseObj: project.getLicenses()) {
            License license = (License) licenseObj;
            LicenseType licenseType = new LicenseType();
            licenseType.setValue(license.getName());
            licenseType.setOsiApproved(osiApproved);
            metadata.getLicense().add(licenseType);
        }

        PluginArtifactType instance = new PluginArtifactType();
        metadata.getPluginArtifact().add(instance);

        ArtifactType artifactType = new ArtifactType();
        artifactType.setGroupId(project.getGroupId());
        artifactType.setArtifactId(project.getArtifactId());
        artifactType.setVersion(project.getVersion());
        artifactType.setType(project.getArtifact().getType());
        instance.setModuleId(artifactType);

        instance.getGeronimoVersion().addAll(geronimoVersions);
        instance.getJvmVersion().addAll(jvmVersions);
        for (Prerequisite prerequisite: prerequisites) {
            instance.getPrerequisite().add(prerequisite.toPrerequisiteType());
        }
        for (Dependency dependency: dependencies) {
            instance.getDependency().add(dependency.toDependencyType());
        }
        for (ModuleId obsolete: obsoletes) {
            instance.getObsoletes().add(obsolete.toArtifactType());
        }

        instance.getSourceRepository().addAll(sourceRepositories);
        for (CopyFile copyFile: copyFiles) {
            instance.getCopyFile().add(copyFile.toCopyFileType());
        }
        if (gbeans != null && gbeans.size() > 0) {
            PluginArtifactType.ConfigXmlContent configXmlContent = new PluginArtifactType.ConfigXmlContent();
            for (Gbean gbean: gbeans) {
                configXmlContent.getGbean().add(gbean.toGBeanType());
            }
            instance.setConfigXmlContent(configXmlContent);
        }

        targetDir.mkdirs();
        FileOutputStream out = new FileOutputStream(targetFile);
        try {
            PluginInstallerGBean.writePluginMetadata(metadata, out);
        } finally {
            out.close();
        }
        getProject().getResources().add(targetFile);
    }

}
