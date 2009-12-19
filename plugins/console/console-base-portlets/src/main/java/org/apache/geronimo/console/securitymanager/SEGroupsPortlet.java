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

package org.apache.geronimo.console.securitymanager;

import java.io.IOException;
import java.util.Hashtable;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.geronimo.console.securitymanager.util.SERealmGroupHelper;
import org.apache.geronimo.console.securitymanager.util.SERealmUserHelper;

public class SEGroupsPortlet extends AbstractSecurityManagerPortlet {

    protected void doView(RenderRequest renderRequest,
            RenderResponse renderResponse) throws PortletException, IOException {
        if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
            return;
        }

        String errorMessage = renderRequest.getParameter("errorMessage");

        if (errorMessage != null) {
            renderRequest.setAttribute("errorMessage", errorMessage);
            errorView.include(renderRequest, renderResponse);
        } else if (!SERealmGroupHelper.isDefaultLoginModuleAvaiable()) {
            renderRequest.setAttribute("errorMessage", getLocalizedString(renderRequest, "consolebase.errorMsg24"));
            errorView.include(renderRequest, renderResponse);
        } else {
            String currAction = renderRequest.getParameter("currAction");
            String message = renderRequest.getParameter("message");
            renderRequest.setAttribute("message", message);

            try {
                if ("new".equals(currAction)) {
                    renderRequest.setAttribute("users", SERealmUserHelper
                            .getUsers());
                    addMaximizedView.include(renderRequest, renderResponse);
                } else if ("edit".equals(currAction)) {
                    String group = renderRequest.getParameter("group");
                    renderRequest.setAttribute("group", group);
                    renderRequest.setAttribute("users", SERealmUserHelper
                            .getUsers());
                    addMaximizedView.include(renderRequest, renderResponse);
                } else {
                    String[] groups = SERealmGroupHelper.getGroups();
                    Hashtable groupsInfo = new Hashtable();
                    for (int i = 0; i < groups.length; i++) {
                        String currentGroup = groups[i];
                        groupsInfo.put(currentGroup, SERealmGroupHelper
                                .getUsers(currentGroup));
                    }
                    renderRequest.setAttribute("groupsInfo", groupsInfo);

                    if (WindowState.NORMAL.equals(renderRequest
                            .getWindowState())) {
                        normalView.include(renderRequest, renderResponse);
                    } else {
                        maximizedView.include(renderRequest, renderResponse);
                    }
                }
            } catch (Exception e) {
                errorMessage = e.getMessage();
                renderRequest.setAttribute("errorMessage", errorMessage);
                errorView.include(renderRequest, renderResponse);
            }
        }
    }

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
        PortletContext pc = portletConfig.getPortletContext();
        normalView = pc
                .getRequestDispatcher("/WEB-INF/view/securityrealmmanager/se/groups/normal.jsp");
        addNormalView = pc
                .getRequestDispatcher("/WEB-INF/view/securityrealmmanager/se/groups/addnormal.jsp");
        maximizedView = pc
                .getRequestDispatcher("/WEB-INF/view/securityrealmmanager/se/groups/maximized.jsp");
        addMaximizedView = pc
                .getRequestDispatcher("/WEB-INF/view/securityrealmmanager/se/groups/addmaximized.jsp");
        helpView = pc
                .getRequestDispatcher("/WEB-INF/view/securityrealmmanager/se/groups/help.jsp");
        errorView = pc
                .getRequestDispatcher("/WEB-INF/view/securityrealmmanager/se/groups/error.jsp");
    }

    public void processAction(ActionRequest actionRequest,
            ActionResponse actionResponse) throws PortletException, IOException {
        String action = actionRequest.getParameter("action").trim();
        String cancel = actionRequest.getParameter("cancel");
        String currAction = "";
        if (cancel != null) {
            action = "";
        }
        String group = actionRequest.getParameter("group");
        String[] users = actionRequest.getParameterValues("users");

        try {
            if ("delete".equals(action)) {
                SERealmGroupHelper.deleteGroup(group);
            } else if ("update".equals(action)) {
                SERealmGroupHelper.updateGroup(group, users);
            } else if ("add".equals(action)) {
                try {
                    SERealmGroupHelper.addGroup(group, users);
                } catch (Exception e) {
                    addErrorMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.errorMsg07"), e.getMessage());
                }
            } else if ("new".equals(action)) {
                currAction = "new";
            } else if ("edit".equals(action)) {
                currAction = "edit";
            }
            actionResponse.setRenderParameter("currAction", currAction);
            if (group != null) {
                actionResponse.setRenderParameter("group", group);
            }
        } catch (Exception e) {
            addErrorMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.errorMsg06"), e.getMessage());
        }
    }
}