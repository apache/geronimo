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

package org.apache.geronimo.console.jmxmanager;

import java.io.IOException;
import java.io.PrintWriter;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.geronimo.console.BasePortlet;

/**
 * The JMX manager portlet
 */
public class JMXManagerPortlet extends BasePortlet {
    private static final String VIEWJMXSERVER_ACTION = "viewJMXServer";

    private static final String VIEWJMXSERVER_JSP = "/WEB-INF/view/jmxmanager/viewJMXServer.jsp";

    private static final String HELP_JSP = "/WEB-INF/view/jmxmanager/help.jsp";

    private PortletRequestDispatcher viewJMXServerView;

    private PortletRequestDispatcher helpView;

    /**
     * Process an action request
     */
    public void processAction(ActionRequest actionRequest,
            ActionResponse actionResponse) throws PortletException, IOException {
    }

    /**
     * Serve up the view mode
     */
    protected void doView(RenderRequest renderRequest,
            RenderResponse renderResponse) throws IOException, PortletException {
        if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
            return;
        } else if (WindowState.NORMAL.equals(renderRequest.getWindowState())) {
            String action = renderRequest.getParameter("action");
            if (action == null) {
                action = VIEWJMXSERVER_ACTION;
            }
            if (VIEWJMXSERVER_ACTION.equals(action)) {
                viewJMXServerView.include(renderRequest, renderResponse);
            } else {
                renderResponse.setContentType("text/html");
                PrintWriter out = renderResponse.getWriter();
                String errorMsg = "Invalid action message: " + action;
                out.println(errorMsg);
            }
        } else if (WindowState.MAXIMIZED.equals(renderRequest.getWindowState())) {
            renderResponse.setContentType("text/html");
            PrintWriter out = renderResponse.getWriter();
            String errorMsg = "Invalid window state: "
                    + renderRequest.getWindowState();
            out.println(errorMsg);
        }
    }

    /**
     * Serve up the help mode
     */
    protected void doHelp(RenderRequest renderRequest,
            RenderResponse renderResponse) throws PortletException, IOException {
        helpView.include(renderRequest, renderResponse);
    }

    /**
     * Portlet is being placed into service
     */
    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
        PortletContext pc = portletConfig.getPortletContext();
        viewJMXServerView = pc.getRequestDispatcher(VIEWJMXSERVER_JSP);
        helpView = pc.getRequestDispatcher(HELP_JSP);
    }

    /**
     * Portlet is being taken out of service
     */
    public void destroy() {
        viewJMXServerView = null;
        helpView = null;
        super.destroy();
    }
}
