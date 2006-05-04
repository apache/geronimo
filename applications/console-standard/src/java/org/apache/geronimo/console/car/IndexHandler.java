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
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
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
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
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
        PluginRepositoryList[] lists = PortletManager.getCurrentServer(request).getPluginRepositoryLists();
        List list = new ArrayList();
        for (int i = 0; i < lists.length; i++) {
            PluginRepositoryList repo = lists[i];
            list.addAll(Arrays.asList(repo.getRepositories()));
        }
        ConfigurationData[] configs = PortletManager.getConfigurations(request, null, false);
        request.setAttribute("configurations", configs);
        request.setAttribute("repositories", list);
        String repository = request.getParameter("repository");
        request.setAttribute("repository", repository);
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        request.setAttribute("repository", request.getParameter("repository"));
        request.setAttribute("repo-user", request.getParameter("username"));
        request.setAttribute("repo-pass", request.getParameter("password"));
        return LIST_MODE+BEFORE_ACTION;
    }
}
