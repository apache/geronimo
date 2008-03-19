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

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.system.plugin.PluginInstaller;
import org.apache.geronimo.system.plugin.model.PluginListType;

/**
 * Handler for the import export list screen.
 *
 * @version $Rev$ $Date$
 */
public class AssemblyListHandler extends AbstractListHandler {

    public AssemblyListHandler() {
        super(LIST_SERVER_MODE, "/WEB-INF/view/car/assemblylist.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String column = (String) request.getAttribute("column");
        String relativeServerPath = request.getParameter("relativeServerPath");
        String groupId = request.getParameter("groupId");
        String artifactId = request.getParameter("artifactId");
        String version = request.getParameter("version");
        String format = request.getParameter("format");

        if(!isEmpty(column)) response.setRenderParameter("column", column);
        response.setRenderParameter("relativeServerPath", isEmpty(relativeServerPath) ? "var/temp/assembly" : relativeServerPath);
        if(!isEmpty(groupId)) response.setRenderParameter("groupId", groupId);
        if(!isEmpty(artifactId)) response.setRenderParameter("artifactId", artifactId);
        response.setRenderParameter("version", isEmpty(version) ? "1.0" : version);
        if(!isEmpty(format)) response.setRenderParameter("format", format);
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        String column = request.getParameter("column");
        String relativeServerPath = request.getParameter("relativeServerPath");
        String groupId = request.getParameter("groupId");
        String artifactId = request.getParameter("artifactId");
        String version = request.getParameter("version");
        String format = request.getParameter("format");
        if(!loadFromServer(request)) {
            //todo: loading failed -- do something!
        }
        request.setAttribute("column", column);
        request.setAttribute("relativeServerPath", relativeServerPath);
        request.setAttribute("groupId", groupId);
        request.setAttribute("artifactId", artifactId);
        request.setAttribute("version", version);
        request.setAttribute("format", format);
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        return getMode()+BEFORE_ACTION;
    }

    private boolean loadFromServer(RenderRequest request) throws IOException, PortletException {

        PluginInstaller pluginInstaller = ManagementHelper.getManagementHelper(request).getPluginInstaller();

        // try to reuse the catalog data if it was already downloaded
        PluginListType data = getServerPluginList(request, pluginInstaller);

        if(data == null || data.getPlugin() == null) {
            return false;
        }

        listPlugins(request, pluginInstaller, data, false);
        request.getPortletSession(true).setAttribute(SERVER_CONFIG_LIST_SESSION_KEY, data);
        return true;
    }

}