/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.console.car;

import java.io.Serializable;
import java.util.List;

import org.apache.geronimo.system.plugin.model.ArtifactType;
import org.apache.geronimo.system.plugin.model.ConfigXmlContentType;
import org.apache.geronimo.system.plugin.model.CopyFileType;
import org.apache.geronimo.system.plugin.model.DependencyType;
import org.apache.geronimo.system.plugin.model.HashType;
import org.apache.geronimo.system.plugin.model.LicenseType;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.geronimo.system.plugin.model.PluginType;
import org.apache.geronimo.system.plugin.model.PrerequisiteType;
import org.apache.geronimo.system.plugin.model.PropertyType;

/**
 * Bean class that holds information about a plugin
 * 
 * @version $Rev$ $Date$
 */
public class PluginInfoBean implements Serializable {
    protected ArtifactType moduleId;
    protected HashType hash;
    protected List<String> geronimoVersion;
    protected List<String> jvmVersion;
    protected List<PrerequisiteType> prerequisite;
    protected List<DependencyType> dependency;
    protected List<ArtifactType> obsoletes;
    protected List<String> sourceRepository;
    protected List<CopyFileType> copyFile;
    protected List<ConfigXmlContentType> configXmlContent;
    protected List<PropertyType> artifactAlias;
    protected List<PropertyType> configSubstitution;
    protected String name;
    protected String category;
    protected String description;
    protected String url;
    protected String author;
    protected List<LicenseType> license;
    protected PluginType plugin;
    protected PluginArtifactType pluginArtifact;
    protected boolean installable = true;
    protected String validationMessage;
    protected boolean isSystemPlugin = true;
    protected boolean isPluginGroup = false;

    public List<PropertyType> getArtifactAlias() {
        return artifactAlias;
    }

    public void setArtifactAlias(List<PropertyType> artifactAlias) {
        this.artifactAlias = artifactAlias;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<PropertyType> getConfigSubstitution() {
        return configSubstitution;
    }

    public void setConfigSubstitution(List<PropertyType> configSubstitution) {
        this.configSubstitution = configSubstitution;
    }

    public List<ConfigXmlContentType> getConfigXmlContent() {
        return configXmlContent;
    }

    public void setConfigXmlContent(List<ConfigXmlContentType> configXmlContent) {
        this.configXmlContent = configXmlContent;
    }

    public List<CopyFileType> getCopyFile() {
        return copyFile;
    }

    public void setCopyFile(List<CopyFileType> copyFile) {
        this.copyFile = copyFile;
    }

    public List<DependencyType> getDependency() {
        return dependency;
    }

    public void setDependency(List<DependencyType> dependency) {
        this.dependency = dependency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getGeronimoVersion() {
        return geronimoVersion;
    }

    public void setGeronimoVersion(List<String> geronimoVersion) {
        this.geronimoVersion = geronimoVersion;
    }

    public HashType getHash() {
        return hash;
    }

    public void setHash(HashType hash) {
        this.hash = hash;
    }

    public List<String> getJvmVersion() {
        return jvmVersion;
    }

    public void setJvmVersion(List<String> jvmVersion) {
        this.jvmVersion = jvmVersion;
    }

    public List<LicenseType> getLicense() {
        return license;
    }

    public void setLicense(List<LicenseType> license) {
        this.license = license;
    }

    public ArtifactType getModuleId() {
        return moduleId;
    }

    public void setModuleId(ArtifactType moduleId) {
        this.moduleId = moduleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ArtifactType> getObsoletes() {
        return obsoletes;
    }

    public void setObsoletes(List<ArtifactType> obsoletes) {
        this.obsoletes = obsoletes;
    }

    public List<PrerequisiteType> getPrerequisite() {
        return prerequisite;
    }

    public void setPrerequisite(List<PrerequisiteType> prerequisite) {
        this.prerequisite = prerequisite;
    }

    public List<String> getSourceRepository() {
        return sourceRepository;
    }

    public void setSourceRepository(List<String> sourceRepository) {
        this.sourceRepository = sourceRepository;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public PluginType getPlugin() {
        return plugin;
    }

    public void setPlugin(PluginType plugin) {
        this.plugin = plugin;
        // copy the fields
        author = plugin.getAuthor();
        category = plugin.getCategory();
        description = plugin.getDescription();
        license = plugin.getLicense();
        name = plugin.getName();
        url = plugin.getUrl();
    }

    public PluginArtifactType getPluginArtifact() {
        return pluginArtifact;
    }

    public void setPluginArtifact(PluginArtifactType pluginArtifact) {
        this.pluginArtifact = pluginArtifact;
        // copy the fields
        artifactAlias = pluginArtifact.getArtifactAlias();
        configSubstitution = pluginArtifact.getConfigSubstitution();
        configXmlContent = pluginArtifact.getConfigXmlContent();
        copyFile = pluginArtifact.getCopyFile();
        dependency = pluginArtifact.getDependency();
        geronimoVersion = pluginArtifact.getGeronimoVersion();
        hash = pluginArtifact.getHash();
        jvmVersion = pluginArtifact.getJvmVersion();
        moduleId = pluginArtifact.getModuleId();
        obsoletes = pluginArtifact.getObsoletes();
        prerequisite = pluginArtifact.getPrerequisite();
        sourceRepository = pluginArtifact.getSourceRepository();
    }

    public boolean isInstallable() {
        return this.installable;
    }

    public void setInstallable(boolean installable) {
        this.installable = installable;
    }

    public String getValidationMessage() {
        return this.validationMessage;
    }

    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
    }
    
    public boolean getIsSystemPlugin() {
        return this.isSystemPlugin;
    }
    
    public void setIsSystemPlugin(boolean isSystemPlugin) {
        this.isSystemPlugin = isSystemPlugin;
    }
    
    public boolean getIsPluginGroup() {
        return this.isPluginGroup;
    }
    
    public void setIsPluginGroup(boolean isPluginGroup) {
        this.isPluginGroup = isPluginGroup;
    }
}