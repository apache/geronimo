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
import java.util.Collection;
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

/**
 * @version $Rev$ $Date$
 */
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
        } else if (!SERealmGroupHelper.isDefaultLoginModuleAvaiable()) {
            renderRequest.setAttribute("errorMessage", getLocalizedString(renderRequest, "consolebase.errorMsg24"));
            errorView.include(renderRequest, renderResponse);
        } else {
            try {
                String[] users = SERealmUserHelper.getUsers();

                Hashtable userInfo = new Hashtable();
                for (int i = 0; i < users.length; i++) {
                    String currentUser = users[i];
                    // We really shouldn't be attaching everyone's password as portlet data!!!
                    // And the current Portlet doesn't use it, so why send it....
                    // userInfo.put(currentUser, SERealmUserHelper.getPassword(currentUser.toString()));
                    userInfo.put(currentUser, currentUser.toString());
                }

                String currAction = renderRequest.getParameter("currAction");
                renderRequest.setAttribute("message", renderRequest.getParameter("message"));

                if ("new".equals(currAction)) {
                    String[] groups = SERealmGroupHelper.getGroups();
                    Hashtable groupsInfo = new Hashtable();
                    for (int i = 0; i < groups.length; i++) {
                        String currentGroup = groups[i];
                        groupsInfo.put(currentGroup, SERealmGroupHelper.getUsers(currentGroup));
                    }
                    renderRequest.setAttribute("groupsInfo", groupsInfo);
                    addMaximizedView.include(renderRequest, renderResponse);
                } else if ("edit".equals(currAction)) {
                    String user = renderRequest.getParameter("user");
                    renderRequest.setAttribute("userID", user);
                    // We really shouldn't be sending the user's password as portlet data!!!
                    // And the current Portlet doesn't use it, so why send it....
                    //renderRequest.setAttribute("password", SERealmUserHelper.getPassword(user));
                    // Current Portlet doesn't use groupsInfo, so why send it 
                    //renderRequest.setAttribute("groupsInfo", groupsInfo);
                    addMaximizedView.include(renderRequest, renderResponse);
                } else {
                    if (WindowState.NORMAL.equals(renderRequest.getWindowState())) {
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
                .getRequestDispatcher("/WEB-INF/view/securityrealmmanager/se/users/error.jsp");
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
        String group = actionRequest.getParameter("group");

        try {
            if ("delete".equals(action)) {
                try {
                    String[] groups = SERealmGroupHelper.getGroups();
                    for (int i = 0; i < groups.length; i++) {
                        String currentGroup = groups[i];
                        if (SERealmGroupHelper.isGroupMember(currentGroup, user)) {
                            Collection list = SERealmGroupHelper.getUsers(currentGroup);
                            list.remove(user);
                            String[] groupUsers = (String[]) list.toArray(new String[0]);
                            SERealmGroupHelper.updateGroup(currentGroup, groupUsers);
                        }
                    }
                    SERealmUserHelper.deleteUser(user);
                } catch (Exception e) {
                    addErrorMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.errorMsg04"), e.getMessage());
                }
            } else if ("update".equals(action)) {
                if(password != null && !password.equals("")) {
                    // Update the password only when it is not blank.
                    SERealmUserHelper.updateUser(user, password);
                }
            } else if ("add".equals(action)) {
                try {
                    SERealmUserHelper.addUser(user, password);
                    if ((group != null) && (!group.equals(""))) {
                        Collection list = SERealmGroupHelper.getUsers(group);
                        list.add(user);
                        String[] groupUsers = (String[]) list.toArray(new String[0]);
                        SERealmGroupHelper.updateGroup(group, groupUsers);
                    }
                } catch (Exception e) {
                    addErrorMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.errorMsg05"), e.getMessage());
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
            addErrorMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.errorMsg06"), e.getMessage());
        }
    }

}
