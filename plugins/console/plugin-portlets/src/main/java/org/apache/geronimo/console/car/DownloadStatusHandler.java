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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;



import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;


import org.apache.geronimo.console.car.ManagementHelper;
import org.apache.geronimo.system.plugin.PluginInstallerGBean;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.system.plugin.DownloadResults;
import org.apache.geronimo.system.plugin.PluginInstaller;

/**
 * Handler for the initial download screen.
 *
 * @version $Rev$ $Date$
 */
public class DownloadStatusHandler extends BaseImportExportHandler {
    public DownloadStatusHandler() {
        super(DOWNLOAD_STATUS_MODE, "/WEB-INF/view/car/downloadStatus.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        return getMode();
        
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        String[] configId = request.getParameterValues("configIds");
        String repo = request.getParameter("repository");
        String user = request.getParameter("repo-user");
        String pass = request.getParameter("repo-pass");
        String downloadKey = request.getParameter("downloadKey");
        
        request.setAttribute("repository", repo);
        request.setAttribute("repouser", user);
        request.setAttribute("repopass", pass);
        request.setAttribute("downloadKey", downloadKey);
        request.setAttribute("configIds", configId);
        
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        PluginInstallerGBean pluginInstaller = (PluginInstallerGBean) ManagementHelper.getManagementHelper(request).getPluginInstaller();
        String[] configId = request.getParameterValues("configId");
        String repo = request.getParameter("repository");
        String user = request.getParameter("repo-user");
        String pass = request.getParameter("repo-pass");
        int downloadKey = Integer.parseInt(request.getParameter("download-key"));
        DownloadResults results = pluginInstaller.checkOnInstall(downloadKey, true);

        List<InstallResults> dependencies = new ArrayList<InstallResults>();
        if (results != null) {
            if(results.isFailed()) {
                //TODO is this an appropriate way to explain failure?
                throw new PortletException("Unable to install configuration", results.getFailure());
            }
            for (Artifact uri: results.getDependenciesInstalled()) {
                dependencies.add(new InstallResults(uri.toString(), "installed"));
            }
            for (Artifact uri: results.getDependenciesPresent()) {
                dependencies.add(new InstallResults(uri.toString(), "already present"));
            }
        }
        request.getPortletSession(true).setAttribute("car.install.results", dependencies);
        response.setRenderParameter("configId", configId);
        response.setRenderParameter("repository", repo);
        if(!isEmpty(user)) response.setRenderParameter("repo-user", user);
        if(!isEmpty(pass)) response.setRenderParameter("repo-pass", pass);
        return RESULTS_MODE+BEFORE_ACTION;
    }

    public static class InstallResults implements Serializable {
        private static final long serialVersionUID = -3745382506085182610L;
        private String name;
        private String action;

        public InstallResults(String name, String action) {
            this.name = name;
            this.action = action;
        }

        public String getName() {
            return name;
        }

        public String getAction() {
            return action;
        }
    }
}
