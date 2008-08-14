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

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.geronimo.console.MultiPageModel;

/**
 * Handler for the confirm screen.
 */
public class AssemblyConfirmHandler extends BaseImportExportHandler {
    public AssemblyConfirmHandler() {
        super(ASSEMBLY_CONFIRM_MODE, "/WEB-INF/view/car/assemblyConfirm.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String relativeServerPath = request.getParameter("relativeServerPath");
        response.setRenderParameter("relativeServerPath", relativeServerPath);
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        PortletSession assemblysession = request.getPortletSession(false);
        List<PluginInfoBean> plugins = (List<PluginInfoBean>) assemblysession.getAttribute("plugins");
        String relativeServerPath = request.getParameter("relativeServerPath");
        File deployedPath = new File(System.getProperty("org.apache.geronimo.home.dir"), relativeServerPath);
        String absoluteDeployedPath = deployedPath.getAbsolutePath();
        
        request.setAttribute("plugins", plugins);
        request.setAttribute("absoluteDeployedPath", absoluteDeployedPath);
        request.setAttribute("mode", ASSEMBLY_CONFIRM_MODE + AFTER_ACTION);
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        return INDEX_MODE+BEFORE_ACTION;
    }
}
