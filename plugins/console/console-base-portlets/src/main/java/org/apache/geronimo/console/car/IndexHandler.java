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
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.util.ConfigurationData;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.system.plugin.PluginRepositoryList;

/**
 * Handler for the import export main screen.
 *
 * @version $Rev$ $Date$
 */
public class IndexHandler extends BaseImportExportHandler {
    public IndexHandler() {
        super(INDEX_MODE, "/WEB-INF/view/car/index.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String repo = request.getParameter("repository");
        if(repo != null) response.setRenderParameter("repository", repo);
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        List<PluginRepositoryList> lists = ManagementHelper.getManagementHelper(request).getPluginRepositoryLists();

        // clear out the catalog if it was previously loaded 
        request.getPortletSession(true).removeAttribute(CONFIG_LIST_SESSION_KEY);
        
        List<URL> list = new ArrayList<URL>();
        for (PluginRepositoryList repo: lists) {
            list.addAll(repo.getRepositories());
        }
        ConfigurationData[] configs = PortletManager.getConfigurations(request, null, false);
        request.setAttribute("configurations", configs);
        request.setAttribute("repositories", list);
        String repository = request.getParameter("repository");
        request.setAttribute("repository", repository);
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        request.setAttribute("column", request.getParameter("column"));
        request.setAttribute("repository", request.getParameter("repository"));
        request.setAttribute("repo-user", request.getParameter("username"));
        request.setAttribute("repo-pass", request.getParameter("password"));
        return LIST_MODE+BEFORE_ACTION;
    }
}
