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
package org.apache.geronimo.console.jmsmanager.wizard;

import java.io.IOException;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.MultiPageModel;

/**
 * Handles the page that shows a deployment plan
 *
 * @version $Rev$ $Date$
 */
public class ShowPlanHandler extends AbstractHandler {
    public ShowPlanHandler(BasePortlet portlet) {
        super(SHOW_PLAN_MODE, "/WEB-INF/view/jmswizard/plan.jsp", portlet);
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        JMSResourceData data = (JMSResourceData) model;
        String plan = save(request, response, data, true);
        request.getPortletSession(true).setAttribute("deploymentPlan", plan);
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        JMSResourceData data = (JMSResourceData) model;
        request.setAttribute("deploymentPlan", request.getPortletSession().getAttribute("deploymentPlan"));
        String path = PortletManager.getRepositoryEntry(request, data.getRarURI()).getPath();
        String base = PortletManager.getCurrentServer(request).getServerInfo().getCurrentBaseDirectory();
        if(base != null && path.startsWith(base)) {
            path = path.substring(base.length());
            if(path.startsWith("/")) {
                path = path.substring(1);
            }
        } else {
            int pos = path.lastIndexOf('/');
            path = path.substring(pos+1);
        }
        request.setAttribute("rarURL", path);
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        JMSResourceData data = (JMSResourceData) model;
        request.getPortletSession(true).removeAttribute("deploymentPlan");
        String next = request.getParameter("nextAction");
        if(next.equals(SELECT_DESTINATION_TYPE_MODE)) {
            data.setCurrentDestinationID(data.getAdminObjects().size());
        } else if(next.equals(SELECT_FACTORY_TYPE_MODE)) {
            data.setCurrentFactoryID(data.getConnectionFactories().size());
        }
        return next+BEFORE_ACTION;
    }
}
