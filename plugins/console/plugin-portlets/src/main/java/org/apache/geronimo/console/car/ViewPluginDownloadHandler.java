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
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.system.plugin.DownloadResults;
import org.apache.geronimo.system.plugin.PluginInstaller;
import org.apache.geronimo.system.plugin.PluginInstallerGBean;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.geronimo.system.plugin.model.PluginListType;
import org.apache.geronimo.system.plugin.model.PluginType;

/**
 * Handler for the screen that shows you plugin details before you go on and
 * install it.
 *
 * @version $Rev$ $Date$
 */
public class ViewPluginDownloadHandler extends BaseImportExportHandler {

    public ViewPluginDownloadHandler() {
        super(VIEW_FOR_DOWNLOAD_MODE, "/WEB-INF/view/car/viewForDownload.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String configId = request.getParameter("configId");
        String[] pluginIds = request.getParameterValues("plugin");
        if (configId != null) {
            pluginIds = new String[]{configId};
        }
        String repo = request.getParameter("repository");
        String user = request.getParameter("repo-user");
        String pass = request.getParameter("repo-pass");
        response.setRenderParameter("pluginIds", pluginIds);
        response.setRenderParameter("repository", repo);
        if (!isEmpty(user)) response.setRenderParameter("repo-user", user);
        if (!isEmpty(pass)) response.setRenderParameter("repo-pass", pass);

        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        PluginInstaller pluginInstaller = ManagementHelper.getManagementHelper(request).getPluginInstaller();

        String[] configIds = request.getParameterValues("pluginIds");
        String repo = request.getParameter("repository");
        String user = request.getParameter("repo-user");
        String pass = request.getParameter("repo-pass");

        PluginListType list = getRepoPluginList(request, pluginInstaller, repo);
        PluginListType installList = getPluginsFromIds(configIds, list);
        List<PluginInfoBean> plugins = new ArrayList<PluginInfoBean>();
        for (PluginType pluginType: installList.getPlugin()) {
            PluginInfoBean infoBean = new PluginInfoBean();
            infoBean.setPlugin(pluginType);
            infoBean.setPluginArtifact(pluginType.getPluginArtifact().get(0));
            plugins.add(infoBean);
        }

        boolean allInstallable = true;
        // see if the plugin is installable.  if not then provide the details
        String validationOk = "All requirements for this plugin have been met.";
        for (PluginInfoBean plugin : plugins) {
            StringBuilder validationNotOk = new StringBuilder();
            PluginType holder = PluginInstallerGBean.copy(plugin.getPlugin(), plugin.getPluginArtifact());
            try {
                plugin.setInstallable(pluginInstaller.validatePlugin(holder));
            } catch (Exception e) {
                plugin.setInstallable(false);
                validationNotOk.append(e.getMessage());
                validationNotOk.append("<BR>\n");
            }
            Dependency[] missingPrereqs = pluginInstaller.checkPrerequisites(holder);
            if (missingPrereqs.length > 0) {
                plugin.setInstallable(false);
                for (Dependency dep : missingPrereqs) {
                    validationNotOk.append(" Missing prerequisite ");
                    validationNotOk.append(dep.getArtifact().toString());
                    validationNotOk.append("<BR>\n");
                }
            }
            if (plugin.isInstallable()) {
                plugin.setValidationMessage(validationOk);
            } else {
                plugin.setValidationMessage(validationNotOk.toString());
                allInstallable = false;
            }
        }
        request.setAttribute("plugins", plugins);
        request.setAttribute("repository", repo);
        request.setAttribute("repouser", user);
        request.setAttribute("repopass", pass);
        request.setAttribute("allInstallable", allInstallable);
        request.setAttribute("mode", "viewForDownload-after");
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        PluginInstaller pluginInstaller = ManagementHelper.getManagementHelper(request).getPluginInstaller();
        String repo = request.getParameter("repository");
        String user = request.getParameter("repo-user");
        String pass = request.getParameter("repo-pass");
        String[] configIds = request.getParameterValues("configId");

        PluginListType list = getRepoPluginList(request, pluginInstaller, repo);
        PluginListType installList = getPluginsFromIds(configIds, list);

        Object downloadKey = pluginInstaller.startInstall(installList, repo, false, user, pass);
        DownloadResults results = pluginInstaller.checkOnInstall(downloadKey);
        request.getPortletSession(true).setAttribute(DOWNLOAD_RESULTS_SESSION_KEY, results);
        
        response.setRenderParameter("configIds", configIds);
        response.setRenderParameter("repository", repo);
        response.setRenderParameter("downloadKey", downloadKey.toString());

        if (!isEmpty(user)) response.setRenderParameter("repo-user", user);
        if (!isEmpty(pass)) response.setRenderParameter("repo-pass", pass);
        return DOWNLOAD_STATUS_MODE + BEFORE_ACTION;
    }

}
