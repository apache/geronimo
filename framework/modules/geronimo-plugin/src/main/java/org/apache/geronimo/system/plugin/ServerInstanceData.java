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


package org.apache.geronimo.system.plugin;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.system.configuration.LocalAttributeManager;
import org.apache.geronimo.system.configuration.LocalPluginAttributeStore;
import org.apache.geronimo.system.configuration.PluginAttributeStore;
import org.apache.geronimo.system.resolver.ExplicitDefaultArtifactResolver;
import org.apache.geronimo.system.serverinfo.ServerInfo;

/**
 * @version $Rev$ $Date$
 */
public class ServerInstanceData {

    private String name;
    private String attributeManagerFrom;
    private String configFile = "var/config/config.xml";
    private String configSubstitutionsFile = "var/config/config-substitutions.properties";
    private String configSubstitutionsPrefix = "org.apache.geronimo.config.substitution.";
    private String artifactAliasesFile = "var/config/artifact_aliases.properties";

    public ServerInstanceData() {
    }

    public ServerInstanceData(ServerInstanceData toCopy) {
        this.name = toCopy.getName();
        this.attributeManagerFrom = toCopy.getAttributeManagerFrom();
        this.configFile = toCopy.getConfigFile();
        this.configSubstitutionsFile = toCopy.getConfigSubstitutionsFile();
        this.configSubstitutionsPrefix = toCopy.getConfigSubstitutionsPrefix();
        this.artifactAliasesFile = toCopy.getArtifactAliasesFile();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getAttributeManagerFrom() {
        return attributeManagerFrom;
    }

    public void setAttributeManagerFrom(String attributeManagerFrom) {
        this.attributeManagerFrom = attributeManagerFrom;
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public String getConfigSubstitutionsFile() {
        return configSubstitutionsFile;
    }

    public void setConfigSubstitutionsFile(String configSubstitutionsFile) {
        this.configSubstitutionsFile = configSubstitutionsFile;
    }

    public String getConfigSubstitutionsPrefix() {
        return configSubstitutionsPrefix;
    }

    public void setConfigSubstitutionsPrefix(String configSubstitutionsPrefix) {
        this.configSubstitutionsPrefix = configSubstitutionsPrefix;
    }

    public String getArtifactAliasesFile() {
        return artifactAliasesFile;
    }

    public void setArtifactAliasesFile(String artifactAliasesFile) {
        this.artifactAliasesFile = artifactAliasesFile;
    }

    public ServerInstance getServerInstance(ArtifactManager artifactManager, ListableRepository targetRepo, ServerInfo serverInfo, Map<String, org.apache.geronimo.system.plugin.ServerInstance> serverInstances, boolean live) throws IOException {
        ExplicitDefaultArtifactResolver geronimoArtifactResolver = new ExplicitDefaultArtifactResolver(
                getArtifactAliasesFile(),
                artifactManager,
                Collections.singleton(targetRepo),
                serverInfo);
        //TODO osgi LocalPluginAttributeStore?
        PluginAttributeStore attributeStore;
        if (attributeManagerFrom == null) {
            LocalAttributeManager attributeStorex = new LocalAttributeManager();
            attributeStorex.setServerInfo(serverInfo);
            Map<String, Object> config = new HashMap<String, Object>();
            config.put(LocalAttributeManager.CONFIG_FILE_KEY, getConfigFile());
            config.put(LocalAttributeManager.CONFIG_SUBSTITUTIONS_FILE_KEY, getConfigSubstitutionsFile());
            config.put(LocalAttributeManager.PREFIX_KEY, getConfigSubstitutionsPrefix());
            config.put(LocalAttributeManager.READ_ONLY_KEY, false);
            attributeStorex.activate(config);
            attributeStorex.load();
            attributeStore = attributeStorex;
        } else {
            ServerInstance shared = serverInstances.get(attributeManagerFrom);
            if (shared == null) {
                throw new IllegalArgumentException("Incorrect configuration: no server instance named '" + attributeManagerFrom + "' defined before being shared from '" + name + "'");
            }
            attributeStore = shared.getAttributeStore();
        }
        return new ServerInstance(name, attributeStore, geronimoArtifactResolver);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("ServerInstanceData:\n");
        buf.append("  Name: ").append(getName()).append("\n");
        buf.append("  AttributeManagerFrom: ").append(getAttributeManagerFrom()).append("\n");
        buf.append("  ConfigFile: ").append(getConfigFile()).append("\n");
        buf.append("  ConfigSubstitutionsFile: ").append(getConfigSubstitutionsFile()).append("\n");
        buf.append("  ConfigSubstitutionsPrefix: ").append(getConfigSubstitutionsPrefix()).append("\n");
        buf.append("  ArtifactAliasesFile: ").append(getArtifactAliasesFile()).append("\n");
        return buf.toString();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(ServerInstanceData.class, "ServerInstanceData");
        infoFactory.addAttribute("name", String.class, true, true);
        infoFactory.addAttribute("attributeManagerFrom", String.class, true, true);
        infoFactory.addAttribute("configFile", String.class, true, true);
        infoFactory.addAttribute("configSubstitutionsFile", String.class, true, true);
        infoFactory.addAttribute("configSubstitutionsPrefix", String.class, true, true);
        infoFactory.addAttribute("artifactAliasesFile", String.class, true, true);

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
