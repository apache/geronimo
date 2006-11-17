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
package org.apache.geronimo.console.apache.jk;

import java.io.IOException;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.management.geronimo.NetworkConnector;
import org.apache.geronimo.management.geronimo.WebManager;

/**
 * Handler for the screen where you select the webapps to expose through Apache
 *
 * @version $Rev$ $Date$
 */
public class ResultsHandler extends BaseApacheHandler {
    public ResultsHandler() {
        super(RESULTS_MODE, "/WEB-INF/view/apache/jk/results.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        //todo: Add AJP Connector
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel amodel) throws PortletException, IOException {
        ApacheModel model = (ApacheModel) amodel;
        String port = "unknown";
        if(model.getAddAjpPort() != null) {
            port = model.getAddAjpPort().toString();
        } else {
            WebManager[] managers = PortletManager.getWebManagers(request);
            // See if any AJP listeners are defined
            for (int i = 0; i < managers.length; i++) {
                WebManager manager = managers[i];
                NetworkConnector[] connectors = manager.getConnectors(WebManager.PROTOCOL_AJP);
                if(connectors.length > 0) {
                    port = Integer.toString(connectors[0].getPort());
                    break;
                }
            }
        }
        request.setAttribute("ajpPort", port);
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        return getMode()+BEFORE_ACTION;
    }
}
