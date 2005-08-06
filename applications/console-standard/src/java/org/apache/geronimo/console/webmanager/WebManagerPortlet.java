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
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.j2ee.management.geronimo.WebContainer;
import org.apache.geronimo.jetty.JettyContainer;

public class WebManagerPortlet extends GenericPortlet {
    private PortletRequestDispatcher normalView;

    private PortletRequestDispatcher maximizedView;

    private PortletRequestDispatcher helpView;

    public void processAction(ActionRequest actionRequest,
            ActionResponse actionResponse) throws PortletException, IOException {
        try {
            WebContainer container = PortletManager.getCurrentWebContainer(actionRequest);
            String action = actionRequest.getParameter("stats");
            if (action != null) {
                boolean stats = action.equals("true");
                if(container instanceof JettyContainer) {
                    ((JettyContainer)container).setCollectStatistics(stats);
                }
            }
            if (actionRequest.getParameter("resetStats") != null) {
                if(container instanceof JettyContainer) {
                    ((JettyContainer)container).resetStatistics();
                }
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
            WebContainer container = PortletManager.getCurrentWebContainer(renderRequest);
            if(container instanceof JettyContainer) {
                JettyContainer jetty = ((JettyContainer)container);
                boolean statsOn = jetty.getCollectStatistics();
                renderRequest.setAttribute("statsOn", statsOn ? Boolean.TRUE : Boolean.FALSE);
                if (statsOn) {
                    renderRequest.setAttribute("connections", new Integer(jetty.getConnections()));
                    renderRequest.setAttribute("connectionsOpen", new Integer(jetty.getConnectionsOpen()));
                    renderRequest.setAttribute("connectionsOpenMax", new Integer(jetty.getConnectionsOpenMax()));
                    renderRequest.setAttribute("connectionsDurationAve", new Long(jetty.getConnectionsDurationAve()));
                    renderRequest.setAttribute("connectionsDurationMax", new Long(jetty.getConnectionsDurationMax()));
                    renderRequest.setAttribute("connectionsRequestsAve", new Integer(jetty.getConnectionsRequestsAve()));
                    renderRequest.setAttribute("connectionsRequestsMax", new Integer(jetty.getConnectionsRequestsMax()));
                    renderRequest.setAttribute("errors", new Integer(jetty.getErrors()));
                    renderRequest.setAttribute("requests", new Integer(jetty.getRequests()));
                    renderRequest.setAttribute("requestsActive", new Integer(jetty.getRequestsActive()));
                    renderRequest.setAttribute("requestsActiveMax", new Integer(jetty.getRequestsActiveMax()));
                    renderRequest.setAttribute("requestsDurationAve", new Long(jetty.getRequestsDurationAve()));
                    renderRequest.setAttribute("requestsDurationMax", new Long(jetty.getRequestsDurationMax()));
                }
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
        normalView = null;
        maximizedView = null;
        super.destroy();
    }

}
