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
import java.util.LinkedList;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.util.ConfigurationData;
import org.apache.geronimo.console.util.PortletManager;

/**
 * Handler for the create a plugin main screen.
 *
 * @version $Rev: 609072 $ $Date: 2008-01-04 19:47:14 -0500 (Fri, 04 Jan 2008) $
 */
public class CreatePluginIndexHandler extends BaseImportExportHandler {
    public CreatePluginIndexHandler() {
        super(INDEX_MODE, "/WEB-INF/view/car/createPluginIndex.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        response.setWindowState(WindowState.NORMAL);
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        ConfigurationData[] configs = PortletManager.getConfigurations(request, null, false);
        List<ConfigurationData> carModulesList = new LinkedList<ConfigurationData>();
        for (ConfigurationData configurationData : configs) {
            if (configurationData.getConfigID().getType().equalsIgnoreCase("car"))
                carModulesList.add(configurationData);
        }
        request.setAttribute("configurations", carModulesList.toArray(new ConfigurationData[carModulesList.size()]));
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {        
        return LIST_MODE+BEFORE_ACTION;
    }
}
