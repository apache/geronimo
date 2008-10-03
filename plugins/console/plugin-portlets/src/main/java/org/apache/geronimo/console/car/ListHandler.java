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
import javax.portlet.WindowState;
import javax.security.auth.login.FailedLoginException;

import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.system.plugin.PluginInstaller;
import org.apache.geronimo.system.plugin.model.PluginListType;

/**
 * Handler for the import export list screen.
 *
 * @version $Rev$ $Date$
 */
public class ListHandler extends AbstractListHandler {
    
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
        
        response.setWindowState(WindowState.MAXIMIZED);
        
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        String repository = request.getParameter("repository");
        String user = request.getParameter("repo-user");
        String pass = request.getParameter("repo-pass");
        String column = request.getParameter("column");
        if(!loadFromRepository(request, repository)) {
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

    private boolean loadFromRepository(RenderRequest request, String repository) throws IOException, PortletException {
        
        PluginInstaller pluginInstaller = ManagementHelper.getManagementHelper(request).getPluginInstaller();

        // try to reuse the catalog data if it was already downloaded
        PluginListType data = getRepoPluginList(request, pluginInstaller, repository);
        
        if(data == null || data.getPlugin() == null) {
            return false;
        }

        listPlugins(request, pluginInstaller, data, true);
        // don't cache plugin list - see GERONIMO-4306
        //request.getPortletSession(true).setAttribute(CONFIG_LIST_SESSION_KEY, data);
        return true;
    }

}
