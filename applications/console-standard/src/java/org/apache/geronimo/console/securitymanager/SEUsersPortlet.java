/**
 *
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.
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

import org.apache.geronimo.console.util.SERealmUserHelper;

public class SEUsersPortlet extends AbstractSecurityManagerPortlet {

    protected void doView(RenderRequest renderRequest,
            RenderResponse renderResponse) throws PortletException, IOException {
        if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
            return;
        }

        String errorMessage = renderRequest.getParameter("errorMessage");

        if (errorMessage != null) {
            renderRequest.setAttribute("errorMessage", errorMessage);
            errorView.include(renderRequest, renderResponse);
        } else {
            try {
                String[] users = SERealmUserHelper.getUsers();

                Hashtable userInfo = new Hashtable();
                for (int i = 0; i < users.length; i++) {
                    String currentUser = users[i];
                    userInfo.put(currentUser, SERealmUserHelper
                            .getPassword(currentUser.toString()));
                }

                String currAction = renderRequest.getParameter("currAction");
                renderRequest.setAttribute("message", renderRequest
                        .getParameter("message"));

                if ("new".equals(currAction) || "edit".equals(currAction)) {
                    if (currAction.equals("edit")) {
                        String user = renderRequest.getParameter("user");
                        renderRequest.setAttribute("userID", user);
                        renderRequest.setAttribute("password",
                                SERealmUserHelper.getPassword(user));
                    }
                    addMaximizedView.include(renderRequest, renderResponse);
                } else {
                    if (WindowState.NORMAL.equals(renderRequest
                            .getWindowState())) {
                        renderRequest.setAttribute("userInfo", userInfo);
                        normalView.include(renderRequest, renderResponse);
                    } else {
                        renderRequest.setAttribute("userInfo", userInfo);
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
                .getRequestDispatcher("/WEB-INF/view/securityrealmmanager/se/users/normal.jsp");
        addNormalView = pc
                .getRequestDispatcher("/WEB-INF/view/securityrealmmanager/se/users/addnormal.jsp");
        maximizedView = pc
                .getRequestDispatcher("/WEB-INF/view/securityrealmmanager/se/users/maximized.jsp");
        addMaximizedView = pc
                .getRequestDispatcher("/WEB-INF/view/securityrealmmanager/se/users/addmaximized.jsp");
        helpView = pc
                .getRequestDispatcher("/WEB-INF/view/securityrealmmanager/se/users/help.jsp");
        errorView = pc
                .getRequestDispatcher("/WEB-INF/view/securityrealmmanager/derby/groups/error.jsp");
    }

    public void processAction(ActionRequest actionRequest,
            ActionResponse actionResponse) throws PortletException, IOException {
        String action = actionRequest.getParameter("action").trim();
        String cancel = actionRequest.getParameter("cancel");
        String currAction = "";
        if (cancel != null) {
            action = "";
        }
        String user = actionRequest.getParameter("userId");
        String password = actionRequest.getParameter("password");

        try {
            if ("delete".equals(action)) {
                SERealmUserHelper.deleteUser(user);
            } else if ("update".equals(action)) {
                SERealmUserHelper.updateUser(user, password);
            } else if ("add".equals(action)) {
                try {
                    SERealmUserHelper.addUser(user, password);
                } catch (Exception e) {
                    actionResponse.setRenderParameter("message",
                            "ERROR: Error in SEUsersPortlet while adding user "+user+". Cause: "+e.getMessage());
                }
            } else if ("new".equals(action)) {
                currAction = "new";
            } else if ("edit".equals(action)) {
                currAction = "edit";
            }
            actionResponse.setRenderParameter("currAction", currAction);
            if (user != null) {
                actionResponse.setRenderParameter("user", user);
            }
        } catch (Exception e) {
            actionResponse.setRenderParameter("message",
                    "Error encountered in SEUsersPortlet. Cause: "
                            + e.getMessage());
        }
    }

}
