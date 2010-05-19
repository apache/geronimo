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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

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
        String configId = request.getParameter("configId");
        String[] pluginIds = request.getParameterValues("plugin");
        if (configId != null) {
            pluginIds = new String[]{configId};
        }
        String targetPath = request.getParameter("targetPath");
        String groupId = request.getParameter("groupId");
        String artifactId = request.getParameter("artifactId");
        String version = request.getParameter("version");
        String format = request.getParameter("format");

        response.setRenderParameter("clickedConfigId", isEmpty(configId) ? "" : configId);
        response.setRenderParameter("pluginIds", pluginIds);
        response.setRenderParameter("targetPath", isEmpty(targetPath) ? "var/temp/" : targetPath);
        if(!isEmpty(groupId)) response.setRenderParameter("groupId", groupId);
        if(!isEmpty(artifactId)) response.setRenderParameter("artifactId", artifactId);
        response.setRenderParameter("version", isEmpty(version) ? "1.0" : version);
        if(!isEmpty(format)) response.setRenderParameter("format", format);
        
        response.setWindowState(WindowState.MAXIMIZED);
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        PluginInstaller pluginInstaller = ManagementHelper.getManagementHelper(request).getPluginInstaller();

        String clickedConfigId = request.getParameter("clickedConfigId");
        String[] configIds = request.getParameterValues("pluginIds");
        String targetPath = request.getParameter("targetPath");
        String groupId = request.getParameter("groupId");
        String artifactId = request.getParameter("artifactId");
        String version = request.getParameter("version");
        String format = request.getParameter("format");

        PluginListType list = getServerPluginList(request, pluginInstaller);
        PluginListType installList = getPluginsFromIds(configIds, list);
        List<PluginInfoBean> plugins = new ArrayList<PluginInfoBean>();
        for (PluginType pluginType: installList.getPlugin()) {
            PluginInfoBean infoBean = new PluginInfoBean();
            infoBean.setPlugin(pluginType);
            infoBean.setPluginArtifact(pluginType.getPluginArtifact().get(0));
            plugins.add(infoBean);
        }

        request.setAttribute("clickedConfigId", clickedConfigId);
        request.setAttribute("plugins", plugins);
        request.setAttribute("targetPath", targetPath);
        request.setAttribute("groupId", groupId);
        request.setAttribute("artifactId", artifactId);
        request.setAttribute("version", version);
        request.setAttribute("format", format);
        
        PortletSession assemblysession = request.getPortletSession(true);
        assemblysession.setAttribute("plugins", plugins);

        request.setAttribute("allInstallable", true);
        request.setAttribute("mode", ASSEMBLY_VIEW_MODE + "-after");
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String targetPath = request.getParameter("targetPath");
        String groupId = request.getParameter("groupId");
        String artifactId = request.getParameter("artifactId");
        String version = request.getParameter("version");
        String format = request.getParameter("format");
        

        PluginInstaller pluginInstaller = ManagementHelper.getManagementHelper(request).getPluginInstaller();
        ServerArchiver archiver = ManagementHelper.getManagementHelper(request).getArchiver();
        String[] configIds = request.getParameterValues("configId");

        PluginListType list = getServerPluginList(request, pluginInstaller);
        PluginListType installList = getPluginsFromIds(configIds, list);
        
        
        // if the targetPath doesn't end with "/" or "\", add one.
        if (targetPath.lastIndexOf("/") != targetPath.length()-1){
            if (targetPath.lastIndexOf("\\") != targetPath.length()-1){
                targetPath = targetPath + "/";
            }
        }
        
        String targetBasePath = targetPath + groupId +"/";
        String targetAssemblyPath = targetBasePath + artifactId+"-"+version;
        String targetArchivePath = targetBasePath;
        
        try {
            DownloadResults downloadResults = pluginInstaller.installPluginList("repository", targetAssemblyPath, installList);
            archiver.archive(targetAssemblyPath, targetArchivePath, new Artifact(groupId, artifactId, version, format));
        } catch (Exception e) {
            throw new PortletException("Could not assemble server", e);
        }
        
        response.setRenderParameter("assemblyFileLocation", targetBasePath);
        
        return ASSEMBLY_CONFIRM_MODE+BEFORE_ACTION;
    }

}