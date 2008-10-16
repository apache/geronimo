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
import java.util.List;

import javax.portlet.PortletRequest;
import javax.portlet.PortletException;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.security.auth.login.FailedLoginException;

import org.apache.geronimo.console.MultiPageAbstractHandler;
import org.apache.geronimo.system.plugin.model.PluginListType;
import org.apache.geronimo.system.plugin.model.PluginType;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.geronimo.system.plugin.PluginInstaller;
import org.apache.geronimo.system.plugin.PluginInstallerGBean;
import org.apache.geronimo.kernel.config.NoSuchStoreException;

/**
 * The base class for all handlers for this portlet
 *
 * @version $Rev$ $Date$
 */
public abstract class BaseImportExportHandler extends MultiPageAbstractHandler {
    protected static final String CONFIG_LIST_SESSION_KEY = "console.plugins.ConfigurationList";
    protected static final String CONFIG_LIST_REPO_SESSION_KEY = "console.plugins.ConfigurationListRepo";
    protected static final String SERVER_CONFIG_LIST_SESSION_KEY = "console.plugins.ServerConfigurationList";
    protected static final String SERVER_APP_LIST_SESSION_KEY = "console.plugins.ServerApplicationList";
    public static final String DOWNLOAD_RESULTS_SESSION_KEY = "console.plugins.DownloadResults";
    protected static final String INDEX_MODE = "index";
    protected static final String ADD_REPO_MODE = "addRepository";
    protected static final String LIST_MODE = "list";
    protected static final String DOWNLOAD_MODE = "download";
    protected static final String VIEW_FOR_DOWNLOAD_MODE = "viewForDownload";
    protected static final String DOWNLOAD_STATUS_MODE = "downloadStatus";
    protected static final String RESULTS_MODE = "results";
    protected static final String CONFIGURE_EXPORT_MODE = "configure";
    protected static final String CONFIRM_EXPORT_MODE = "confirm";
    protected static final String UPDATE_REPOS_MODE = "updateList";
    protected static final String ASSEMBLY_CONFIRM_MODE = "assemblyConfirm";
    protected static final String LIST_SERVER_MODE = "listServer";
    protected static final String ASSEMBLY_VIEW_MODE = "assemblyView";
    protected static final String ASSEMBLY_NAME_MODE = "assemblyName";

    protected BaseImportExportHandler(String mode, String viewName) {
        super(mode, viewName);
    }

    protected PluginListType getRepoPluginList(PortletRequest request, PluginInstaller pluginInstaller, String repo) throws IOException, PortletException {
        PortletSession session = request.getPortletSession(true);
        PluginListType list = (PluginListType) session.getAttribute(CONFIG_LIST_SESSION_KEY);
        String listRepo = (String) session.getAttribute(CONFIG_LIST_REPO_SESSION_KEY);

        if (list == null || !repo.equals(listRepo)) {
            try {
                list = pluginInstaller.listPlugins(new URL(repo));
            } catch (FailedLoginException e) {
                throw new PortletException("Invalid login for repository '" + repo + "'", e);
            }
            session.setAttribute(CONFIG_LIST_SESSION_KEY, list);
            session.setAttribute(CONFIG_LIST_REPO_SESSION_KEY, repo);
        }
        return list;
    }

    protected PluginListType getServerPluginList(PortletRequest request, PluginInstaller pluginInstaller) throws PortletException {
        PluginListType data = (PluginListType) request.getPortletSession(true).getAttribute(SERVER_CONFIG_LIST_SESSION_KEY);
        if (data==null) {
            try {
                data = pluginInstaller.createPluginListForRepositories(null);
            } catch (NoSuchStoreException e) {
                throw new PortletException("Server in unknown state", e);
            }
        }
        return data;
    }
    
    protected List<String> getApplicationModuleLists(RenderRequest request) {
        List<String> applicationLists = (List<String>)request.getPortletSession(true).getAttribute(SERVER_APP_LIST_SESSION_KEY);
        if (applicationLists == null) {
            applicationLists = ManagementHelper.getManagementHelper(request).getApplicationModuleLists();
        }
        return applicationLists;
    }

    protected PluginListType getPluginsFromIds(String[] configIds, PluginListType list) throws PortletException {
        PluginListType installList = new PluginListType();
        for (String configId : configIds) {
            PluginType plugin = null;
            for (PluginType metadata : list.getPlugin()) {
                for (PluginArtifactType testInstance : metadata.getPluginArtifact()) {
                    if (PluginInstallerGBean.toArtifact(testInstance.getModuleId()).toString().equals(configId)) {
                        plugin = PluginInstallerGBean.copy(metadata, testInstance);
                        installList.getPlugin().add(plugin);
                        break;
                    }
                }
            }
            if (plugin == null) {
                throw new PortletException("No configuration found for '" + configId + "'");
            }
        }
        return installList;
    }

}
