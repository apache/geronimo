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
import java.util.Map;

import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.system.configuration.LocalPluginAttributeStore;
import org.apache.geronimo.system.resolver.LocalAliasedArtifactResolver;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;

/**
 * @version $Rev$ $Date$
 */
public class ReferenceServerInstanceData extends ServerInstanceData {

    private final LocalPluginAttributeStore attributeStore;
    private final LocalAliasedArtifactResolver artifactResolver;


    public ReferenceServerInstanceData() {
        attributeStore = null;
        artifactResolver = null;
    }

    public ReferenceServerInstanceData(LocalPluginAttributeStore attributeStore, LocalAliasedArtifactResolver artifactResolver) {
        this.attributeStore = attributeStore;
        this.artifactResolver = artifactResolver;
    }

    @Override
    public String getConfigFile() {
        return attributeStore.getConfigFile();
    }

    @Override
    public String getConfigSubstitutionsFile() {
        return attributeStore.getConfigSubstitutionsFile();
    }

    @Override
    public String getConfigSubstitutionsPrefix() {
        return attributeStore.getConfigSubstitutionsPrefix();
    }

    @Override
    public String getArtifactAliasesFile() {
        return artifactResolver.getArtifactAliasesFile();
    }

    @Override
    public ServerInstance getServerInstance(ArtifactManager artifactManager, ListableRepository targetRepo, ServerInfo serverInfo, Map<String, ServerInstance> serverInstances, boolean live) throws IOException {
        if (live) {
            return new ServerInstance(getName(), attributeStore, artifactResolver);
        } else {
            return super.getServerInstance(artifactManager, targetRepo, serverInfo, serverInstances, live);
        }
    }

    @Override
     public String toString() {
         StringBuilder buf = new StringBuilder();
         buf.append("ReferenceServerInstanceData:\n");
         buf.append("  Name: ").append(getName()).append("\n");
         buf.append("  ConfigFile: ").append(getConfigFile()).append("\n");
         buf.append("  ConfigSubstitutionsFile: ").append(getConfigSubstitutionsFile()).append("\n");
         buf.append("  ConfigSubstitutionsPrefix: ").append(getConfigSubstitutionsPrefix()).append("\n");
         buf.append("  ArtifactAliasesFile: ").append(getArtifactAliasesFile()).append("\n");
         return buf.toString();
     }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(ReferenceServerInstanceData.class, "ServerInstanceData");
        infoFactory.addAttribute("name", String.class, true, true);
        infoFactory.addReference("AttributeStore", LocalPluginAttributeStore.class, "AttributeStore");
        infoFactory.addReference("ArtifactResolver", LocalAliasedArtifactResolver.class, "ArtifactResolver");

        infoFactory.setConstructor(new String[] {"AttributeStore", "ArtifactResolver"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
