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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.security.auth.login.FailedLoginException;

import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.system.plugin.PluginInstaller;
import org.apache.geronimo.system.plugin.PluginInstallerGBean;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.geronimo.system.plugin.model.PluginListType;
import org.apache.geronimo.system.plugin.model.PluginType;

/**
 * Handler for the import export list screen.
 *
 * @version $Rev$ $Date$
 */
public class ListHandler extends BaseImportExportHandler {
    
    public ListHandler() {
        super(LIST_MODE, "/WEB-INF/view/car/list.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String repository = (String) request.getAttribute("repository");
        if(repository == null || repository.equals("")) {
            return INDEX_MODE+BEFORE_ACTION;
        }
        response.setRenderParameter("repository", repository);
        String user = (String) request.getAttribute("repo-user");
        String pass = (String) request.getAttribute("repo-pass");
        String column = (String) request.getAttribute("column");

        if(!isEmpty(user)) response.setRenderParameter("repo-user", user);
        if(!isEmpty(pass)) response.setRenderParameter("repo-pass", pass);
        if(!isEmpty(column)) response.setRenderParameter("column", column);
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        String repository = request.getParameter("repository");
        String user = request.getParameter("repo-user");
        String pass = request.getParameter("repo-pass");
        String column = request.getParameter("column");
        if(!loadFromRepository(request, repository, user, pass)) {
            //todo: loading failed -- do something!
        }
        request.setAttribute("repository", repository);
        request.setAttribute("repouser", user);
        request.setAttribute("repopass", pass);
        request.setAttribute("column", column);
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        return getMode()+BEFORE_ACTION;
    }

    private boolean loadFromRepository(RenderRequest request, String repository, String username, String password) throws IOException, PortletException {
        
        PluginInstaller pluginInstaller = ManagementHelper.getManagementHelper(request).getPluginInstaller();

        // try to reuse the catalog data if it was already downloaded
        PluginListType data = (PluginListType) request.getPortletSession(true).getAttribute(CONFIG_LIST_SESSION_KEY);
        if (data==null) {
            try {
                data = pluginInstaller.listPlugins(new URL(repository), username, password);
            } catch (FailedLoginException e) {
                throw new PortletException("Invalid login for Maven repository '"+repository+"'", e);
            }
        }
        
        if(data == null || data.getPlugin() == null) {
            return false;
        }
        
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
                
                // determine if the plugin is installable
                PluginType holder = PluginInstallerGBean.copy(metadata, artifact);
                try {
                    pluginInstaller.validatePlugin(holder);
                } catch (Exception e) {
                    plugin.setInstallable(false);
                }
                Dependency[] missingPrereqs = pluginInstaller.checkPrerequisites(holder);
                if (missingPrereqs.length > 0) {
                    plugin.setInstallable(false);
                }
                plugins.add(plugin);
            }
        }

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
        
        // save everything in the request
        request.setAttribute("plugins", plugins);
        request.getPortletSession(true).setAttribute(CONFIG_LIST_SESSION_KEY, data);
        return true;
    }
}
