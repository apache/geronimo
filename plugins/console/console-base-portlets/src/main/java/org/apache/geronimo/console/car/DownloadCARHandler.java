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
import java.net.URL;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.security.auth.login.FailedLoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.ajax.ProgressInfo;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.system.plugin.DownloadResults;
import org.apache.geronimo.system.plugin.PluginInstaller;
import org.apache.geronimo.system.plugin.PluginInstallerGBean;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.geronimo.system.plugin.model.PluginListType;
import org.apache.geronimo.system.plugin.model.PluginType;

/**
 * Handler for the initial download screen.
 *
 * @version $Rev$ $Date$
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
        if (!isEmpty(user)) response.setRenderParameter("repo-user", user);
        if (!isEmpty(pass)) response.setRenderParameter("repo-pass", pass);

        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        String configId = request.getParameter("configId");
        String repo = request.getParameter("repository");
        String user = request.getParameter("repo-user");
        String pass = request.getParameter("repo-pass");
        PluginType config = null;
        try {
            PluginListType list = (PluginListType) request.getPortletSession(true).getAttribute(CONFIG_LIST_SESSION_KEY);
            if (list == null) {
                list = ManagementHelper.getManagementHelper(request).getPluginInstaller().listPlugins(new URL(repo), user, pass);
                request.getPortletSession(true).setAttribute(CONFIG_LIST_SESSION_KEY, list);
            }
            for (PluginType metadata : list.getPlugin()) {
                for (PluginArtifactType instance : metadata.getPluginArtifact())
                    if (PluginInstallerGBean.toArtifact(instance.getModuleId()).toString().equals(configId)) {
                        config = metadata;
                        request.setAttribute("dependencies", instance.getDependency());
                        break;
                    }
            }
        } catch (FailedLoginException e) {
            throw new PortletException("Invalid login for Maven repository '" + repo + "'", e);
        }
        if (config == null) {
            throw new PortletException("No configuration found for '" + configId + "'");
        }
        request.setAttribute("configId", configId);
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

            PluginType plugin = null;
            PluginArtifactType instance = null;
            try {
                PluginListType list = (PluginListType) request.getPortletSession(true).getAttribute(CONFIG_LIST_SESSION_KEY);
                if(list == null) {
                    list = ManagementHelper.getManagementHelper(request).getPluginInstaller().listPlugins(new URL(repo), user, pass);
                    request.getPortletSession(true).setAttribute(CONFIG_LIST_SESSION_KEY, list);
                }
                for (PluginType metadata: list.getPlugin()) {
                    for (PluginArtifactType testInstance: metadata.getPluginArtifact()) {
                    if(PluginInstallerGBean.toArtifact(testInstance.getModuleId()).toString().equals(configId)) {
                        plugin = metadata;
                        instance = testInstance;
                        break;
                    }
                    }
                }
            } catch (FailedLoginException e) {
                throw new PortletException("Invalid login for Maven repository '"+repo+"'", e);
            }
            if (plugin == null) {
                throw new PortletException("No configuration found for '" + configId + "'");
            }
            PluginType copy = PluginInstallerGBean.copy(plugin, instance);
            PluginListType installList = new PluginListType();
            installList.getPlugin().add(copy);

            PluginInstaller configInstaller = ManagementHelper.getManagementHelper(request).getPluginInstaller();
            Object downloadKey = configInstaller.startInstall(installList, repo, false, user, pass);
            DownloadResults results = configInstaller.checkOnInstall(downloadKey);
            request.getPortletSession(true).setAttribute(DOWNLOAD_RESULTS_SESSION_KEY, results);
            response.setRenderParameter("configId", configId);
            response.setRenderParameter("repository", repo);
            response.setRenderParameter("downloadKey", downloadKey.toString());

            if(!isEmpty(user)) response.setRenderParameter("repo-user", user);
            if(!isEmpty(pass)) response.setRenderParameter("repo-pass", pass);
        }
        return DOWNLOAD_STATUS_MODE+BEFORE_ACTION;
    }
        

    public static class Installer implements Runnable {
        private PluginInstaller configInstaller;
        private Object downloadKey;
        private ProgressInfo progressInfo;
        private PortletSession session;

        public Installer(PluginInstaller configInstaller, Object downloadKey, ProgressInfo progressInfo, PortletSession session) {
            this.configInstaller = configInstaller;
            this.downloadKey = downloadKey;
            this.progressInfo = progressInfo;
            this.session = session;
        }

        public void run() {
            DownloadResults results;

            while (true) {
                results = configInstaller.checkOnInstall(downloadKey);
                progressInfo.setMainMessage(results.getCurrentMessage());
                progressInfo.setProgressPercent(results.getCurrentFilePercent());
                progressInfo.setFinished(results.isFinished());
                log.debug(progressInfo.getMainMessage());
                if (results.isFinished()) {
                    log.debug("Installation finished");
                    session.setAttribute(DOWNLOAD_RESULTS_SESSION_KEY, results);
                    break;
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        log.error("Download monitor thread interrupted", e);
                    }
                }
            }
        }
    }
}