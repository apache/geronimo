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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.system.plugin.PluginInstaller;
import org.apache.geronimo.system.plugin.PluginInstallerGBean;
import org.apache.geronimo.system.plugin.model.ArtifactType;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.geronimo.system.plugin.model.PluginListType;
import org.apache.geronimo.system.plugin.model.PluginType;

/**
 * Handler for the import export list screen.
 *
 * @version $Rev$ $Date$
 */
public class AssemblyListHandler extends AbstractListHandler {

    public AssemblyListHandler() {
        super(LIST_SERVER_MODE, "/WEB-INF/view/car/assemblylist.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String column = (String) request.getAttribute("column");
        String relativeServerPath = request.getParameter("relativeServerPath");
        String groupId = request.getParameter("groupId");
        String artifactId = request.getParameter("artifactId");
        String version = request.getParameter("version");
        String format = request.getParameter("format");
        String type = request.getParameter("type");

        if(!isEmpty(column)) response.setRenderParameter("column", column);
        response.setRenderParameter("relativeServerPath", isEmpty(relativeServerPath) ? "var/temp/assembly" : relativeServerPath);
        if(!isEmpty(groupId)) response.setRenderParameter("groupId", groupId);
        if(!isEmpty(artifactId)) response.setRenderParameter("artifactId", artifactId);
        response.setRenderParameter("version", isEmpty(version) ? "1.0" : version);
        if(!isEmpty(format)) response.setRenderParameter("format", format);
        if(!isEmpty(type)) response.setRenderParameter("type", type);
        
        response.setWindowState(WindowState.MAXIMIZED);
        
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        String column = request.getParameter("column");
        String relativeServerPath = request.getParameter("relativeServerPath");
        String groupId = request.getParameter("groupId");
        String artifactId = request.getParameter("artifactId");
        String version = request.getParameter("version");
        String format = request.getParameter("format");
        String type = request.getParameter("type");
        if(!loadFromServer(request)) {
            //todo: loading failed -- do something!
        }
            
        request.setAttribute("column", column);
        request.setAttribute("relativeServerPath", relativeServerPath);
        request.setAttribute("groupId", groupId);
        request.setAttribute("artifactId", artifactId);
        request.setAttribute("version", version);
        request.setAttribute("format", format);
        request.setAttribute("type", type);
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        return getMode()+BEFORE_ACTION;
    }

    private boolean loadFromServer(RenderRequest request) throws IOException, PortletException {

        PluginInstaller pluginInstaller = ManagementHelper.getManagementHelper(request).getPluginInstaller();

        // try to reuse the catalog data if it was already downloaded
        PluginListType data = getServerPluginList(request, pluginInstaller);
        //try to reuse the server application list
        List<String> appList = getApplicationModuleLists(request);
        
        if(data == null || data.getPlugin() == null) {
            return false;
        }

        listPlugins(request, pluginInstaller, data, appList);
        
        // don't cache plugin list or application module list - see GERONIMO-4306
        //request.getPortletSession(true).setAttribute(SERVER_CONFIG_LIST_SESSION_KEY, data);
        //request.getPortletSession(true).setAttribute(SERVER_APP_LIST_SESSION_KEY, appList);
        
        return true;
    }
    
    private void listPlugins(RenderRequest request, PluginInstaller pluginInstaller, PluginListType data, List<String> appList) {
        List<PluginInfoBean> sysPlugins = new ArrayList<PluginInfoBean>();
        List<PluginInfoBean> appPlugins = new ArrayList<PluginInfoBean>();
        List<PluginInfoBean> groupPlugins = new ArrayList<PluginInfoBean>();
        
        for (PluginType metadata: data.getPlugin()) {

            // ignore plugins which have no artifacts defined
            if (metadata.getPluginArtifact().isEmpty()) {
                continue;
            }

            if (metadata.getCategory() == null) {
                metadata.setCategory("Unspecified");
            }

            for (PluginArtifactType pluginArtifact : metadata.getPluginArtifact()) {
                PluginInfoBean plugin = new PluginInfoBean();
                plugin.setPlugin(metadata);
                plugin.setPluginArtifact(pluginArtifact);
                
                if (metadata.isPluginGroup() != null && metadata.isPluginGroup()) {
                    plugin.setIsPluginGroup(true);
                }
                
                //determine if the plugin is a system plugin or application plugin
                ArtifactType artifact = pluginArtifact.getModuleId();
                String configId = artifact.getGroupId() + "/" + artifact.getArtifactId() + "/" 
                                  + artifact.getVersion() + "/" + artifact.getType();
                for (String app : appList) {
                    if (app.equals(configId)) {
                        plugin.setIsSystemPlugin(false);
                        appPlugins.add(plugin);
                    } 
                }
                
                if (metadata.isPluginGroup() != null && metadata.isPluginGroup()) {
                    groupPlugins.add(plugin);
                }
                
                if (plugin.getIsSystemPlugin()) {
                    sysPlugins.add(plugin);
                }
            }
        }
        
        // sort the plugin list based on the selected table column
        sortPlugins(appPlugins, request);
        sortPlugins(sysPlugins, request);
        sortPlugins(groupPlugins, request);

        // save everything in the request
        request.setAttribute("appPlugins", appPlugins);
        request.setAttribute("sysPlugins", sysPlugins);
        request.setAttribute("groupPlugins", groupPlugins);
    }
}