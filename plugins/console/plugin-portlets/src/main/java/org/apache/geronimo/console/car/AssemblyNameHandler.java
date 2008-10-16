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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.system.plugin.PluginInstaller;
import org.apache.geronimo.system.plugin.PluginInstallerGBean;
import org.apache.geronimo.system.plugin.model.ArtifactType;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.geronimo.system.plugin.model.PluginListType;
import org.apache.geronimo.system.plugin.model.PluginType;

/**
 * Handler for the assembly name screen.
 *
 * @version $Rev$ $Date$
 */
public class AssemblyNameHandler extends BaseImportExportHandler{

    public AssemblyNameHandler() {
        super(ASSEMBLY_NAME_MODE, "/WEB-INF/view/car/assemblyName.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String column = (String) request.getAttribute("column");
        String type = request.getParameter("type");

        if(!isEmpty(column)) response.setRenderParameter("column", column);
        if(!isEmpty(type)) response.setRenderParameter("type", type);
        
        response.setWindowState(WindowState.MAXIMIZED);
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        String column = request.getParameter("column");
        String type = request.getParameter("type");
            
        request.setAttribute("column", column);
        request.setAttribute("type", type);
        request.setAttribute("containsPlugin", containsPlugin(request));
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        return getMode()+BEFORE_ACTION;
    }
    
    //this function checks if server contains any plugins.  If so, ask users to name their server assembly.
    private boolean containsPlugin(RenderRequest request) throws IOException, PortletException {

        PluginInstaller pluginInstaller = ManagementHelper.getManagementHelper(request).getPluginInstaller();

        // try to reuse the catalog data if it was already downloaded
        PluginListType data = getServerPluginList(request, pluginInstaller);

        if(data == null || data.getPlugin() == null) {
            return false;
        }
        
        // don't cache plugin list or application module list - see GERONIMO-4306
        //request.getPortletSession(true).setAttribute(SERVER_CONFIG_LIST_SESSION_KEY, data);

        return true;
    }
}