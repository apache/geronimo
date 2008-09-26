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

import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.geronimo.console.MultiPagePortlet;
import org.apache.geronimo.console.MultiPageModel;

/**
 * Portlet for creating a plugin.
 *
 * @version $Rev: 627838 $ $Date: 2008-02-14 13:54:30 -0500 (Thu, 14 Feb 2008) $
 */
public class CreatePluginPortlet extends MultiPagePortlet {
    private PortletRequestDispatcher helpView;
    
    public void init(PortletConfig config) throws PortletException {
        super.init(config);
        addHelper(new CreatePluginIndexHandler(), config);
        addHelper(new ExportConfigHandler(), config);
        addHelper(new ExportHandler(), config);
        helpView = config.getPortletContext().getRequestDispatcher("/WEB-INF/view/car/createPluginHelp.jsp");
    }

    protected String getModelJSPVariableName() {
        return "model";
    }

    protected MultiPageModel getModel(PortletRequest request) {
        return null;
    }
    
    protected void doHelp(RenderRequest renderRequest,
            RenderResponse renderResponse) throws PortletException, IOException {
        helpView.include(renderRequest, renderResponse);
    }
    public void destroy() {
        helpView = null;
        super.destroy();
    }    
}
