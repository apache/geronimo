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
import java.util.ArrayList;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import javax.portlet.PortletContext;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.j2ee.management.geronimo.WebContainer;
import org.apache.geronimo.j2ee.management.geronimo.WebConnector;
import org.apache.geronimo.jetty.JettyContainer;
import org.apache.geronimo.jetty.JettyWebConnector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AJP13ConnectorPortlet extends GenericPortlet {
    private final static Log log = LogFactory.getLog(AJP13ConnectorPortlet.class);

    private PortletRequestDispatcher normalView;

    private PortletRequestDispatcher maximizedView;

    private PortletRequestDispatcher helpView;

    protected PortletRequestDispatcher editHttpView;
    protected PortletRequestDispatcher editHttpsView;

    public void processAction(ActionRequest actionRequest,
            ActionResponse actionResponse) throws PortletException, IOException {
        String mode = actionRequest.getParameter("mode");
        WebContainer container = PortletManager.getCurrentWebContainer(actionRequest);
        String server = "generic";
        if(container instanceof JettyContainer) {
            server = "jetty";
        }
        actionResponse.setRenderParameter("server", server);
        if(mode.equals("new")) {
            // User selected to add a new connector, need to show criteria portlet
            actionResponse.setRenderParameter("mode", "new");
            String protocol = actionRequest.getParameter("protocol");
            actionResponse.setRenderParameter("protocol", protocol);
        } else if(mode.equals("add")) { // User just submitted the form to add a new connector
            // Get submitted values
            //todo: lots of validation
            String protocol = actionRequest.getParameter("protocol");
            String host = actionRequest.getParameter("host");
            int port = Integer.parseInt(actionRequest.getParameter("port"));
            int maxThreads = Integer.parseInt(actionRequest.getParameter("maxThreads"));
            Integer minThreads = getInteger(actionRequest, "minThreads");
            String name = actionRequest.getParameter("name");
            // Create and configure the connector
            WebConnector connector = PortletManager.createWebConnector(actionRequest, name, protocol, host, port);
            connector.setMaxThreads(maxThreads);
            // todo: more configurable HTTP/Jetty values
            if(connector instanceof JettyWebConnector) {
                if(minThreads != null) {
                    ((JettyWebConnector)connector).setMinThreads(minThreads.intValue());
                }
            }
            if(protocol.equals(WebContainer.PROTOCOL_HTTPS)) {
                //todo: HTTPS values
            }

            // Start the connector
            try {
                connector.startRecursive();
            } catch (Exception e) {
                log.error("Unable to start connector", e); //todo: get into rendered page somehow?
            }
            actionResponse.setRenderParameter("mode", "list");
        } else if(mode.equals("edit")) {
            String objectName = actionRequest.getParameter("name");
            actionResponse.setRenderParameter("name", objectName);
            actionResponse.setRenderParameter("mode", "edit");
        } else if(mode.equals("delete")) { // User chose to delete a connector
            String objectName = actionRequest.getParameter("name");
            PortletManager.getCurrentWebContainer(actionRequest).removeConnector(objectName);
            actionResponse.setRenderParameter("mode", "list");
        }
    }

    private Integer getInteger(ActionRequest actionRequest, String key) {
        String value = actionRequest.getParameter(key);
        if(value == null || value.equals("")) {
            return null;
        }
        return new Integer(value);
    }

    protected void doView(RenderRequest renderRequest,
            RenderResponse renderResponse) throws IOException, PortletException {
        if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
            return;
        }
        String mode = renderRequest.getParameter("mode");
        if(mode == null || mode.equals("")) {
            mode = "list";
        }
        renderRequest.setAttribute("server", renderRequest.getParameter("server"));
        WebContainer container = PortletManager.getCurrentWebContainer(renderRequest);
        if(mode.equals("new")) {
            String protocol = renderRequest.getParameter("protocol");
            renderRequest.setAttribute("maxThreads", "50");
            if(container instanceof JettyWebConnector) {
                renderRequest.setAttribute("minThreads", "10");
            }
            renderRequest.setAttribute("protocol", protocol);
            renderRequest.setAttribute("mode", "add");
            if(protocol.equals(WebContainer.PROTOCOL_HTTPS)) {
                editHttpsView.include(renderRequest, renderResponse);
            } else {
                editHttpView.include(renderRequest, renderResponse);
            }
        } else if(mode.equals("edit")) {
            String objectName = renderRequest.getParameter("name");
            WebConnector connector = null;
            WebConnector all[] = PortletManager.getWebConnectors(renderRequest);
            for (int i = 0; i < all.length; i++) {
                WebConnector conn = all[i];
                if(conn.getObjectName().equals(objectName)) {
                    connector = conn;
                    break;
                }
            }
            if(connector == null) {
                doList(renderRequest, container, renderResponse);
            } else {
                renderRequest.setAttribute("protocol", connector.getProtocol());
                renderRequest.setAttribute("port", new Integer(connector.getPort()));
                renderRequest.setAttribute("host", connector.getHost());
                renderRequest.setAttribute("maxThreads", "50");
                if(container instanceof JettyWebConnector) {
                    renderRequest.setAttribute("minThreads", "10");
                }
                renderRequest.setAttribute("mode", "edit");
                if(connector.getProtocol().equals(WebContainer.PROTOCOL_HTTPS)) {
                    editHttpsView.include(renderRequest, renderResponse);
                } else {
                    editHttpView.include(renderRequest, renderResponse);
                }
            }
        } else if(mode.equals("list")) {
            doList(renderRequest, container, renderResponse);
        }
    }

    private void doList(RenderRequest renderRequest, WebContainer container, RenderResponse renderResponse) throws PortletException, IOException {
        List beans = new ArrayList();
        WebConnector[] connectors = PortletManager.getWebConnectors(renderRequest);
        for (int i = 0; i < connectors.length; i++) {
            WebConnector connector = connectors[i];
            ConnectorInfo info = new ConnectorInfo();
            info.setObjectName(connector.getObjectName());
            info.setState(connector.getState());
            info.setPort(connector.getPort());
            info.setProtocol(connector.getProtocol());
            info.setDescription(PortletManager.getGBeanDescription(renderRequest, connector.getObjectName()));
            beans.add(info);
        }
        renderRequest.setAttribute("connectors", beans);
        renderRequest.setAttribute("protocols", container.getSupportedProtocols());

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
        PortletContext pc = portletConfig.getPortletContext();
        normalView = pc.getRequestDispatcher("/WEB-INF/view/webmanager/ajp13/normal.jsp");
        maximizedView = pc.getRequestDispatcher("/WEB-INF/view/webmanager/ajp13/maximized.jsp");
        helpView = pc.getRequestDispatcher("/WEB-INF/view/webmanager/ajp13/help.jsp");
        editHttpView = pc.getRequestDispatcher("/WEB-INF/view/webmanager/editHTTP.jsp");
        editHttpsView = pc.getRequestDispatcher("/WEB-INF/view/webmanager/editHTTP.jsp"); //todo: HTTPS args
    }

    public void destroy() {
        normalView = null;
        maximizedView = null;
        super.destroy();
    }
}
