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


package org.apache.geronimo.console.car;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.portlet.RenderRequest;

import org.apache.geronimo.system.plugin.PluginInstaller;
import org.apache.geronimo.system.plugin.PluginInstallerGBean;
import org.apache.geronimo.system.plugin.model.PluginListType;
import org.apache.geronimo.system.plugin.model.PluginType;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.geronimo.kernel.repository.Dependency;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractListHandler extends BaseImportExportHandler {
    public AbstractListHandler(String mode, String viewName) {
        super(mode, viewName);
    }

    protected void listPlugins(RenderRequest request, PluginInstaller pluginInstaller, PluginListType data, boolean validate) {
        List<PluginInfoBean> plugins = new ArrayList<PluginInfoBean>();

        for (PluginType metadata: data.getPlugin()) {

            // ignore plugins which have no artifacts defined
            if (metadata.getPluginArtifact().isEmpty()) {
                continue;
            }

            if (metadata.getCategory() == null) {
                metadata.setCategory("Unspecified");
            }

            for (PluginArtifactType artifact : metadata.getPluginArtifact()) {
                PluginInfoBean plugin = new PluginInfoBean();
                plugin.setPlugin(metadata);
                plugin.setPluginArtifact(artifact);

                if (validate) {
                    // determine if the plugin is installable
                    PluginType holder = PluginInstallerGBean.copy(metadata, artifact);
                    try {
                        plugin.setInstallable(pluginInstaller.validatePlugin(holder));
                    } catch (Exception e) {
                        plugin.setInstallable(false);
                    }
                    Dependency[] missingPrereqs = pluginInstaller.checkPrerequisites(holder);
                    if (missingPrereqs.length > 0) {
                        plugin.setInstallable(false);
                    }
                }
                plugins.add(plugin);
            }
        }

        // sort the plugin list based on the selected table column
        sortPlugins(plugins, request);

        // save everything in the request
        request.setAttribute("plugins", plugins);
    }
    
    protected void sortPlugins(List<PluginInfoBean> plugins, RenderRequest request) {
        // sort the plugin list based on the selected table column
        final String column = request.getParameter("column");
        Collections.sort(plugins, new Comparator<PluginInfoBean>() {
            public int compare(PluginInfoBean o1, PluginInfoBean o2) {
                if ("Category".equals(column)) {
                    String category1 = o1.getCategory();
                    String category2 = o2.getCategory();
                    if (category1.equals(category2)) {
                        return o1.getName().compareTo(o2.getName());
                    }
                    return category1.compareTo(category2);
                }
                else if ("Version".equals(column)) {
                    String version1 = o1.getPluginArtifact().getModuleId().getVersion();
                    String version2 = o2.getPluginArtifact().getModuleId().getVersion();
                    if (version1.equals(version2)) {
                        return o1.getName().compareTo(o2.getName());
                    }
                    return version1.compareTo(version2);
                }
                else if ("Installable".equals(column)) {
                    if (o1.isInstallable() == o2.isInstallable()) {
                        return o1.getName().compareTo(o2.getName());
                    }
                    return o1.isInstallable() ? -1 : 1 ;
                }
                else { // default sort column is Name
                    return o1.getName().compareTo(o2.getName());
                }
            }
        });
    }
}
