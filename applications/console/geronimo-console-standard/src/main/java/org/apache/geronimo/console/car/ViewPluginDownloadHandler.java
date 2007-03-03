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
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.security.auth.login.FailedLoginException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.system.plugin.PluginMetadata;
import org.apache.geronimo.system.plugin.PluginList;

/**
 * Handler for the screen that shows you plugin details before you go on and
 * install it.
 *
 * @version $Rev$ $Date$
 */
public class ViewPluginDownloadHandler  extends BaseImportExportHandler {
    private final static Log log = LogFactory.getLog(ViewPluginDownloadHandler.class);

    public ViewPluginDownloadHandler() {
        super(VIEW_FOR_DOWNLOAD_MODE, "/WEB-INF/view/car/viewForDownload.jsp");
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
        PluginMetadata config = null;
        try {
            PluginList list = (PluginList) request.getPortletSession(true).getAttribute(CONFIG_LIST_SESSION_KEY);
            if(list == null) {
                list = PortletManager.getCurrentServer(request).getPluginInstaller().listPlugins(new URL(repo), user, pass);
                request.getPortletSession(true).setAttribute(CONFIG_LIST_SESSION_KEY, list);
            }
            for (int i = 0; i < list.getPlugins().length; i++) {
                PluginMetadata metadata = list.getPlugins()[i];
                if(metadata.getModuleId().toString().equals(configId)) {
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
        request.setAttribute("plugin", config);
        request.setAttribute("repository", repo);
        request.setAttribute("repouser", user);
        request.setAttribute("repopass", pass);
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        return DOWNLOAD_MODE+BEFORE_ACTION;
    }
}
