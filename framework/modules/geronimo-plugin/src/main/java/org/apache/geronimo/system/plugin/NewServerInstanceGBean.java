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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.basic.BasicKernel;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.PersistentConfigurationList;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.system.plugin.model.ArtifactType;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.geronimo.system.plugin.model.PluginListType;
import org.apache.geronimo.system.plugin.model.PluginType;
import org.osgi.framework.BundleContext;

/**
 * @version $Rev$ $Date$
 */
@GBean
public class NewServerInstanceGBean implements NewServerInstance {

    private final Set<String> defaultPlugins;
    private final Collection<PersistentConfigurationList> persistentConfigurationLists;
    private final PluginInstallerGBean oldPluginInstallerGBean;
    private final ConfigurationManager configManager;
    private final BundleContext bundleContext;

    public NewServerInstanceGBean(@ParamAttribute(name = "defaultPlugins")Map<String, String> defaultPlugins,
                                  @ParamReference(name = "PersistentConfigurationList", namingType="PersistentConfigurationList.PERSISTENT_CONFIGURATION_LIST")Collection<PersistentConfigurationList> persistentConfigurationLists,
                                  @ParamReference(name = "PluginInstallerGBean")PluginInstallerGBean pluginInstallerGBean,
                                  @ParamReference(name = "ConfigManager", namingType = "ConfigurationManager")ConfigurationManager configManager,
                                  @ParamSpecial(type = SpecialAttributeType.bundleContext)BundleContext bundleContext) {
        this.persistentConfigurationLists = persistentConfigurationLists;
        this.oldPluginInstallerGBean = pluginInstallerGBean;
        this.configManager = configManager;
        this.defaultPlugins = defaultPlugins == null ? Collections.<String>emptySet() : defaultPlugins.keySet();
        this.bundleContext = bundleContext;
    }

    public void newServerInstance(String serverName) throws Exception {
        try {
            PluginListType pluginList = new PluginListType();
            for (String artifactString : defaultPlugins) {
                Artifact artifact = Artifact.create(artifactString);
                PluginType plugin = getPlugin(artifact);
                pluginList.getPlugin().add(plugin);
            }
            Artifact query = Artifact.createPartial("///");
            for (PersistentConfigurationList persistentConfigurationList : persistentConfigurationLists) {
                for (Artifact installed : persistentConfigurationList.getListedConfigurations(query)) {
                    PluginType plugin = getPlugin(installed);
                    pluginList.getPlugin().add(plugin);
                }
            }

            Kernel kernel = new BasicKernel("assembly", bundleContext);

            try {
                PluginInstallerGBean pluginInstallerGBean = oldPluginInstallerGBean.pluginInstallerCopy(serverName, kernel);
                GeronimoSourceRepository localSourceRepository = new GeronimoSourceRepository(configManager.getRepositories(), configManager.getArtifactResolver());

                DownloadResults downloadPoller = new DownloadResults();
                pluginInstallerGBean.install(pluginList, localSourceRepository, true, null, null, downloadPoller);
            } finally {
                kernel.shutdown();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public PluginType getPlugin(Artifact sourceArtifact) {
        PluginType plugin = new PluginType();
        PluginArtifactType pluginArtifact = new PluginArtifactType();
        ArtifactType artifact = new ArtifactType();
        artifact.setGroupId(sourceArtifact.getGroupId());
        artifact.setArtifactId(sourceArtifact.getArtifactId());
        artifact.setVersion(sourceArtifact.getVersion().toString());
        artifact.setType(sourceArtifact.getType());
        pluginArtifact.setModuleId(artifact);
        plugin.getPluginArtifact().add(pluginArtifact);
        return plugin;
    }


}
