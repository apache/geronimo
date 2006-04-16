/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.security.auth.login.FailedLoginException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.system.configuration.ConfigurationMetadata;
import org.apache.geronimo.system.configuration.DownloadResults;
import org.apache.geronimo.system.configuration.ConfigurationList;

/**
 * Handler for the initial download screen.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class DownloadCARHandler extends BaseImportExportHandler {
    private final static Log log = LogFactory.getLog(DownloadCARHandler.class);

    public DownloadCARHandler() {
        super(DOWNLOAD_MODE, "/WEB-INF/view/car/download.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String configId = request.getParameter("configId");
        String repo = request.getParameter("repository");
        String user = request.getParameter("repo-user");
        String pass = request.getParameter("repo-pass");
        response.setRenderParameter("configId", configId);
        response.setRenderParameter("repository", repo);
        if(!isEmpty(user)) response.setRenderParameter("repo-user", user);
        if(!isEmpty(pass)) response.setRenderParameter("repo-pass", pass);

        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        String configId = request.getParameter("configId");
        String repo = request.getParameter("repository");
        String user = request.getParameter("repo-user");
        String pass = request.getParameter("repo-pass");
        ConfigurationMetadata config = null;
        try {
            ConfigurationList list = (ConfigurationList) request.getPortletSession(true).getAttribute(CONFIG_LIST_SESSION_KEY);
            if(list == null) {
                list = PortletManager.getCurrentServer(request).getConfigurationInstaller().listConfigurations(new URL(repo), user, pass);
                request.getPortletSession(true).setAttribute(CONFIG_LIST_SESSION_KEY, list);
            }
            for (int i = 0; i < list.getConfigurations().length; i++) {
                ConfigurationMetadata metadata = list.getConfigurations()[i];
                if(metadata.getConfigId().toString().equals(configId)) {
                    config = metadata;
                    break;
                }
            }
        } catch (FailedLoginException e) {
            throw new PortletException("Invalid login for Maven repository '"+repo+"'", e);
        }
        if(config == null) {
            throw new PortletException("No configuration found for '"+configId+"'");
        }
        request.setAttribute("configId", configId);
        request.setAttribute("dependencies", config.getDependencies());
        request.setAttribute("repository", repo);
        request.setAttribute("repouser", user);
        request.setAttribute("repopass", pass);
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String repo = request.getParameter("repository");
        String user = request.getParameter("repo-user");
        String pass = request.getParameter("repo-pass");
        boolean proceed = Boolean.valueOf(request.getParameter("proceed")).booleanValue();
        if(proceed) {
            String configId = request.getParameter("configId");

            ConfigurationList installList;
            try {
                ConfigurationList list = (ConfigurationList) request.getPortletSession(true).getAttribute(CONFIG_LIST_SESSION_KEY);
                if(list == null) {
                    list = PortletManager.getCurrentServer(request).getConfigurationInstaller().listConfigurations(new URL(repo), user, pass);
                    request.getPortletSession(true).setAttribute(CONFIG_LIST_SESSION_KEY, list);
                }
                installList = ConfigurationList.createInstallList(list, Artifact.create(configId));
            } catch (FailedLoginException e) {
                throw new PortletException("Invalid login for Maven repository '"+repo+"'", e);
            }
            if(installList == null) {
                throw new PortletException("No configuration found for '"+configId+"'");
            }

            DownloadResults results;
            try {
                results = PortletManager.getCurrentServer(request).getConfigurationInstaller().install(installList, user, pass);
            } catch (FailedLoginException e) {
                throw new PortletException("Invalid login for Maven repository '"+repo+"'", e);
            } catch (MissingDependencyException e) {
                throw new PortletException(e.getMessage(), e);
            }
            List dependencies = new ArrayList();
            for (int i = 0; i < results.getDependenciesInstalled().length; i++) {
                Artifact uri = results.getDependenciesInstalled()[i];
                dependencies.add(new InstallResults(uri.toString(), "installed"));
            }
            for (int i = 0; i < results.getDependenciesPresent().length; i++) {
                Artifact uri = results.getDependenciesPresent()[i];
                dependencies.add(new InstallResults(uri.toString(), "already present"));
            }
            request.getPortletSession(true).setAttribute("car.install.results", dependencies);
            response.setRenderParameter("configId", configId);
            response.setRenderParameter("repository", repo);
            if(!isEmpty(user)) response.setRenderParameter("repo-user", user);
            if(!isEmpty(pass)) response.setRenderParameter("repo-pass", pass);
        }
        return RESULTS_MODE+BEFORE_ACTION;
    }

    public static class InstallResults implements Serializable {
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
