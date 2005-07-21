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

import javax.management.ObjectName;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.geronimo.console.util.ObjectNameConstants;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;

public class WebManagerPortlet extends GenericPortlet {

    private Kernel kernel;

    private PortletRequestDispatcher normalView;

    private PortletRequestDispatcher maximizedView;

    private PortletRequestDispatcher helpView;

    private ObjectName jettyObjectName;

    public void processAction(ActionRequest actionRequest,
            ActionResponse actionResponse) throws PortletException, IOException {
        try {
            String action = actionRequest.getParameter("stats");
            if (action != null) {
                Boolean stats = Boolean.valueOf(action);
                kernel
                        .setAttribute(jettyObjectName, "collectStatistics",
                                stats);
            }
            if (actionRequest.getParameter("resetStats") != null) {
                kernel.invoke(jettyObjectName, "resetStatistics");
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
            Boolean statsOn = (Boolean) kernel.getAttribute(jettyObjectName,
                    "collectStatistics");
            renderRequest.setAttribute("statsOn", statsOn);
            if (statsOn.booleanValue()) {
                renderRequest.setAttribute("connections", (Integer) kernel
                        .getAttribute(jettyObjectName, "connections"));
                renderRequest.setAttribute("connectionsOpen", (Integer) kernel
                        .getAttribute(jettyObjectName, "connectionsOpen"));
                renderRequest.setAttribute("connectionsOpenMax",
                        (Integer) kernel.getAttribute(jettyObjectName,
                                "connectionsOpenMax"));
                renderRequest.setAttribute("connectionsDurationAve",
                        (Long) kernel.getAttribute(jettyObjectName,
                                "connectionsDurationAve"));
                renderRequest.setAttribute("connectionsDurationMax",
                        (Long) kernel.getAttribute(jettyObjectName,
                                "connectionsDurationMax"));
                renderRequest.setAttribute("connectionsRequestsAve",
                        (Integer) kernel.getAttribute(jettyObjectName,
                                "connectionsRequestsAve"));
                renderRequest.setAttribute("connectionsRequestsMax",
                        (Integer) kernel.getAttribute(jettyObjectName,
                                "connectionsRequestsMax"));
                renderRequest.setAttribute("errors", (Integer) kernel
                        .getAttribute(jettyObjectName, "errors"));
                renderRequest.setAttribute("requests", (Integer) kernel
                        .getAttribute(jettyObjectName, "requests"));
                renderRequest.setAttribute("requestsActive", (Integer) kernel
                        .getAttribute(jettyObjectName, "requestsActive"));
                renderRequest.setAttribute("requestsActiveMax",
                        (Integer) kernel.getAttribute(jettyObjectName,
                                "requestsActiveMax"));
                renderRequest.setAttribute("requestsDurationAve", (Long) kernel
                        .getAttribute(jettyObjectName, "requestsDurationAve"));
                renderRequest.setAttribute("requestsDurationMax", (Long) kernel
                        .getAttribute(jettyObjectName, "requestsDurationMax"));
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
        kernel = KernelRegistry.getSingleKernel();
        try {
            jettyObjectName = new ObjectName(
                    ObjectNameConstants.WEBCONTAINER_OBJECT_NAME);
        } catch (Exception e) {
            throw new AssertionError();
        }

        normalView = portletConfig.getPortletContext().getRequestDispatcher(
                "/WEB-INF/view/webmanager/normal.jsp");
        maximizedView = portletConfig.getPortletContext().getRequestDispatcher(
                "/WEB-INF/view/webmanager/maximized.jsp");
        helpView = portletConfig.getPortletContext().getRequestDispatcher(
                "/WEB-INF/view/webmanager/help.jsp");
    }

    public void destroy() {
        normalView = null;
        maximizedView = null;
        kernel = null;
        super.destroy();
    }

}
