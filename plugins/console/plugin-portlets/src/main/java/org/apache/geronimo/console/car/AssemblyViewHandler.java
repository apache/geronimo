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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.system.plugin.DownloadResults;
import org.apache.geronimo.system.plugin.PluginInstaller;
import org.apache.geronimo.system.plugin.ServerArchiver;
import org.apache.geronimo.system.plugin.model.PluginListType;
import org.apache.geronimo.system.plugin.model.PluginType;

/**
 * Handler for the screen that shows you plugin details before you go on and
 * install it.
 *
 * @version $Rev$ $Date$
 */
public class AssemblyViewHandler extends BaseImportExportHandler {

    public AssemblyViewHandler() {
        super(ASSEMBLY_VIEW_MODE, "/WEB-INF/view/car/viewForDownload.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String[] pluginIds = request.getParameterValues("plugin");
        response.setRenderParameter("pluginIds", pluginIds);

        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        PluginInstaller pluginInstaller = ManagementHelper.getManagementHelper(request).getPluginInstaller();

        String[] configIds = request.getParameterValues("pluginIds");

        PluginListType list = getServerPluginList(request, pluginInstaller);
        PluginListType installList = getPluginsFromIds(configIds, list);
        List<PluginInfoBean> plugins = new ArrayList<PluginInfoBean>();
        for (PluginType pluginType: installList.getPlugin()) {
            PluginInfoBean infoBean = new PluginInfoBean();
            infoBean.setPlugin(pluginType);
            infoBean.setPluginArtifact(pluginType.getPluginArtifact().get(0));
            plugins.add(infoBean);
        }

        request.setAttribute("plugins", plugins);
        request.setAttribute("allInstallable", true);
        request.setAttribute("mode", ASSEMBLY_VIEW_MODE + "-after");
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String relativeServerPath = "var/temp/assembly";
        String group = "test";
        String artifact = "testserver";
        String version = "1.0";
        String format = "tar.gz";
        
        PluginInstaller pluginInstaller = ManagementHelper.getManagementHelper(request).getPluginInstaller();
        ServerArchiver archiver = ManagementHelper.getManagementHelper(request).getArchiver();
        String[] configIds = request.getParameterValues("configId");

        PluginListType list = getServerPluginList(request, pluginInstaller);
        PluginListType installList = getPluginsFromIds(configIds, list);

        try {
            DownloadResults downloadResults = pluginInstaller.installPluginList("repository", relativeServerPath, installList);
            archiver.archive(relativeServerPath, "var/temp", new Artifact(group, artifact, version, format));
        } catch (Exception e) {
            throw new PortletException("Could not assemble server", e);
        }
        return INDEX_MODE;
    }

}