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

package org.apache.geronimo.console.webmanager;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.management.geronimo.WebContainer;

/**
 * Basic portlet showing statistics for a web container
 *
 * @version $Rev: 46228 $ $Date: 2004-09-16 21:21:04 -0400 (Thu, 16 Sep 2004) $
 */
public class WebManagerPortlet extends BasePortlet {
    private PortletRequestDispatcher normalView;

    private PortletRequestDispatcher maximizedView;

    private PortletRequestDispatcher helpView;

    public void processAction(ActionRequest actionRequest,
            ActionResponse actionResponse) throws PortletException, IOException {
        try {
            String[] names = PortletManager.getWebManagerNames(actionRequest);  //todo: handle multiple
            if (names != null) {
                String managerName = names[0];  //todo: handle multiple
                String[] containers = PortletManager.getWebContainerNames(actionRequest, managerName);  //todo: handle multiple
                if (containers != null) {
                    String containerName = containers[0];  //todo: handle multiple
                    WebContainer container = PortletManager.getWebContainer(actionRequest, containerName);
                    String server = getWebServerType(container.getClass());
                    String action = actionRequest.getParameter("stats");
                    if (action != null) {
                        boolean stats = action.equals("true");
                        if(server.equals(WEB_SERVER_JETTY)) {
                            setProperty(container, "collectStatistics", stats ? Boolean.TRUE : Boolean.FALSE);
                        }
                        else if (server.equals(WEB_SERVER_TOMCAT)) {
                            //todo:   Any Tomcat specific processing?
                        }
                        else {
                            //todo:   Handle "should not occur" condition
                        }
                    }
                    if (actionRequest.getParameter("resetStats") != null) {
                        if(server.equals(WEB_SERVER_JETTY)) {
                            callOperation(container, "resetStatistics", null);
                        }
                        else if (server.equals(WEB_SERVER_TOMCAT)) {
                            //todo:   Any Tomcat specific processing?
                        }
                        else {
                            //todo:   Handle "should not occur" condition
                        }
                    }
                }
                else {
                    // todo  - Handle "should not occur" error  - message?
                }
            }
            else {
                // todo  - Handle "should not occur" error  - message?
            }
        } catch (Exception e) {
            throw new PortletException(e);
        }
    }

    protected void doView(RenderRequest renderRequest,
            RenderResponse renderResponse) throws IOException, PortletException {
        if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
            return;
        }
        try {
            String[] names = PortletManager.getWebManagerNames(renderRequest);  //todo: handle multiple
            if (names != null) {
                String managerName = names[0];  //todo: handle multiple
                String[] containers = PortletManager.getWebContainerNames(renderRequest, managerName);  //todo: handle multiple
                if (containers != null) {
                    String containerName = containers[0];  //todo: handle multiple
                    WebContainer container = PortletManager.getWebContainer(renderRequest, containerName);
                    String server = getWebServerType(container.getClass());
                    StatisticsHelper helper = null;
                    if(server.equals(WEB_SERVER_JETTY)) {
                        helper = new JettyStatisticsHelper();
                    } else if(server.equals(WEB_SERVER_TOMCAT)) {
                        //todo     - Handle Tomcat logs
                    }
                    else {
                        // todo   - Log error, unknown server
                    }
                    if(helper != null) {
                        helper.gatherStatistics(container, renderRequest);
                    }
                }
                else {
                    // todo  - Handle "should not occur" error  - message?
                }
            }
            else {
                // todo  - Handle "should not occur" error  - message?
            }
        } catch (Exception e) {
            throw new PortletException(e);
        }
        if (WindowState.NORMAL.equals(renderRequest.getWindowState())) {
            normalView.include(renderRequest, renderResponse);
        } else {
            maximizedView.include(renderRequest, renderResponse);
        }
    }

    protected void doHelp(RenderRequest renderRequest,
            RenderResponse renderResponse) throws PortletException, IOException {
        helpView.include(renderRequest, renderResponse);
    }

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);

        normalView = portletConfig.getPortletContext().getRequestDispatcher(
                "/WEB-INF/view/webmanager/normal.jsp");
        maximizedView = portletConfig.getPortletContext().getRequestDispatcher(
                "/WEB-INF/view/webmanager/maximized.jsp");
        helpView = portletConfig.getPortletContext().getRequestDispatcher(
                "/WEB-INF/view/webmanager/help.jsp");
    }

    public void destroy() {
        helpView = null;
        normalView = null;
        maximizedView = null;
        super.destroy();
    }

}
