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

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.system.configuration.LocalAttributeManager;
import org.apache.geronimo.system.resolver.ExplicitDefaultArtifactResolver;
import org.apache.geronimo.system.serverinfo.ServerInfo;

/**
 * @version $Rev:$ $Date:$
 */
public class ServerInstance {

    private String name;
    private String attributeManagerFrom;
    private String configFile = "var/config/config.xml";
    private String configSubstitutionsFileName = "var/config/config-substitutions.properties";
    private String configSubstitutionsPrefix = "org.apache.geronimo.config.substitution.";
    private String artifactAliasesFileName = "var/config/artifact_aliases.properties";


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
        return configSubstitutionsFileName;
    }

    public void setConfigSubstitutionsFile(String configSubstitutionsFileName) {
        this.configSubstitutionsFileName = configSubstitutionsFileName;
    }

    public String getConfigSubstitutionsPrefix() {
        return configSubstitutionsPrefix;
    }

    public void setConfigSubstitutionsPrefix(String configSubstitutionsPrefix) {
        this.configSubstitutionsPrefix = configSubstitutionsPrefix;
    }

    public String getArtifactAliasesFile() {
        return artifactAliasesFileName;
    }

    public void setArtifactAliasesFile(String artifactAliasesFileName) {
        this.artifactAliasesFileName = artifactAliasesFileName;
    }

    public org.apache.geronimo.system.plugin.ServerInstance getServerInstance(ArtifactManager artifactManager, ListableRepository targetRepo, ServerInfo serverInfo, Map<String, org.apache.geronimo.system.plugin.ServerInstance> serverInstances) throws IOException {
        ExplicitDefaultArtifactResolver geronimoArtifactResolver = new ExplicitDefaultArtifactResolver(
                artifactAliasesFileName,
                artifactManager,
                Collections.singleton(targetRepo),
                serverInfo);
        LocalAttributeManager attributeStore;
        if (attributeManagerFrom == null) {
            attributeStore = new LocalAttributeManager(configFile,
                    configSubstitutionsFileName,
                    configSubstitutionsPrefix,
                    false,
                    serverInfo);
            attributeStore.load();
        } else {
            org.apache.geronimo.system.plugin.ServerInstance shared = serverInstances.get(attributeManagerFrom);
            if (shared == null) {
                throw new IllegalArgumentException("Incorrect configuration: no server instance named '" + attributeManagerFrom + "' defined before being shared from '" + name + "'");
            }
            attributeStore = (LocalAttributeManager) shared.getAttributeStore();
        }
        return new org.apache.geronimo.system.plugin.ServerInstance(name, attributeStore, geronimoArtifactResolver);
    }
}
