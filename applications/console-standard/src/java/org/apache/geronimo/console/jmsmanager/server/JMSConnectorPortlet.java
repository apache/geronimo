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
package org.apache.geronimo.console.jmsmanager.server;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.kernel.proxy.GeronimoManagedBean;
import org.apache.geronimo.management.geronimo.JMSConnector;
import org.apache.geronimo.management.geronimo.JMSManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * List, edit, add, remove JMS network connectors
 *
 * @version $Rev$ $Date$
 */
public class JMSConnectorPortlet extends BaseJMSPortlet {
    private final static Log log = LogFactory.getLog(JMSConnectorPortlet.class);

    private PortletRequestDispatcher normalView;

    private PortletRequestDispatcher maximizedView;

    private PortletRequestDispatcher helpView;

    protected PortletRequestDispatcher editView;

    public void processAction(ActionRequest actionRequest,
                              ActionResponse actionResponse) throws PortletException, IOException {
        try {
            String mode = actionRequest.getParameter("mode");
            String managerName = PortletManager.getJMSManagerNames(actionRequest)[0];  //todo: handle multiple
            String brokerName = actionRequest.getParameter("brokerName");
            if(mode.equals("new")) {
                // User selected to add a new connector, need to show criteria portlet
                actionResponse.setRenderParameter("mode", "new");
                String protocol = actionRequest.getParameter("protocol");
                actionResponse.setRenderParameter("protocol", protocol);
                actionResponse.setRenderParameter("brokerName", brokerName);
            } else if(mode.equals("add")) { // User just submitted the form to add a new connector
                // Get submitted values
                //todo: lots of validation
                String protocol = actionRequest.getParameter("protocol");
                String host = actionRequest.getParameter("host");
                int port = Integer.parseInt(actionRequest.getParameter("port"));
                String name = actionRequest.getParameter("name");
                // Create and configure the connector
                JMSConnector connector = PortletManager.createJMSConnector(actionRequest, managerName, brokerName, name, protocol, host, port);
                // Start the connector
                try {
                    ((GeronimoManagedBean)connector).startRecursive();
                } catch (Exception e) {
                    log.error("Unable to start connector", e); //todo: get into rendered page somehow?
                }
                actionResponse.setRenderParameter("mode", "list");
            } else if(mode.equals("save")) { // User just submitted the form to update a connector
                // Get submitted values
                //todo: lots of validation
                String host = actionRequest.getParameter("host");
                int port = Integer.parseInt(actionRequest.getParameter("port"));
                String objectName = actionRequest.getParameter("objectName");
                // Identify and update the connector
                JMSConnector connector = (JMSConnector)PortletManager.getManagedBean(actionRequest, objectName);
                if(connector != null) {
                    connector.setHost(host);
                    connector.setPort(port);
                }
                actionResponse.setRenderParameter("mode", "list");
            } else if(mode.equals("start")) {
                String objectName = actionRequest.getParameter("objectName");
                try {
                    PortletManager.getManagedBean(actionRequest, objectName).startRecursive();
                } catch (Exception e) {
                    throw new PortletException(e);
                }
                actionResponse.setRenderParameter("mode", "list");
            } else if(mode.equals("stop")) {
                String objectName = actionRequest.getParameter("objectName");
                try {
                    PortletManager.getManagedBean(actionRequest, objectName).stop();
                } catch (Exception e) {
                    throw new PortletException(e);
                }
                actionResponse.setRenderParameter("mode", "list");
            } else if(mode.equals("edit")) {
                String objectName = actionRequest.getParameter("objectName");
                actionResponse.setRenderParameter("objectName", objectName);
                actionResponse.setRenderParameter("mode", "edit");
            } else if(mode.equals("delete")) {
                String objectName = actionRequest.getParameter("objectName");
                PortletManager.getJMSManager(actionRequest, managerName).removeConnector(objectName);
                actionResponse.setRenderParameter("mode", "list");
            }
        } catch (Throwable e) {
            log.error("Unable to process portlet action", e);
            if(e instanceof PortletException) {
                throw (PortletException)e;
            }
        }
    }

    protected void doView(RenderRequest renderRequest,
                          RenderResponse renderResponse) throws IOException, PortletException {
        if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
            return;
        }

        try {
            String mode = renderRequest.getParameter("mode");
            if(mode == null || mode.equals("")) {
                mode = "list";
            }
            String managerName = PortletManager.getJMSManagerNames(renderRequest)[0];  //todo: handle multiple

            if(mode.equals("new")) {
                String brokerName = renderRequest.getParameter("brokerName");
                String protocol = renderRequest.getParameter("protocol");
                renderRequest.setAttribute("protocol", protocol);
                renderRequest.setAttribute("brokerName", brokerName);
                renderRequest.setAttribute("mode", "add");
                editView.include(renderRequest, renderResponse);
            } else if(mode.equals("edit")) {
                String objectName = renderRequest.getParameter("objectName");
                JMSConnector connector = (JMSConnector)PortletManager.getManagedBean(renderRequest, objectName);
                if(connector == null) {
                    doList(renderRequest, managerName, renderResponse);
                } else {
                    renderRequest.setAttribute("objectName", objectName);
                    renderRequest.setAttribute("port", new Integer(connector.getPort()));
                    renderRequest.setAttribute("host", connector.getHost());
                    renderRequest.setAttribute("mode", "save");
                    editView.include(renderRequest, renderResponse);
                }
            } else if(mode.equals("list")) {
                doList(renderRequest, managerName, renderResponse);
            }
        } catch (Throwable e) {
            log.error("Unable to render portlet", e);
        }
    }

    private void doList(RenderRequest renderRequest, String managerName, RenderResponse renderResponse) throws PortletException, IOException {
        List beans = new ArrayList();
        String[] brokerNames = PortletManager.getJMSBrokerNames(renderRequest, managerName);
        for (int i = 0; i < brokerNames.length; i++) {
            String brokerName = brokerNames[i];
            try {
                ObjectName bon = ObjectName.getInstance(brokerName);
                JMSConnector[] connectors = PortletManager.getJMSConnectorsForContainer(renderRequest, managerName, brokerName);
                for (int j = 0; j < connectors.length; j++) {
                    JMSConnector connector = connectors[j];
                    ObjectName conName = ObjectName.getInstance(((GeronimoManagedBean)connector).getObjectName());
                    String connectorName = conName.getKeyProperty("name");
                    ConnectorWrapper info = new ConnectorWrapper(bon.getKeyProperty("name"), bon.getCanonicalName(),
                                                                 connectorName, connector);
                    beans.add(info);
                }
            } catch (MalformedObjectNameException e) {
                log.error("Unable to decode ObjectName", e);
            }
        }
        renderRequest.setAttribute("brokers", getBrokerMap(renderRequest, managerName).entrySet());
        renderRequest.setAttribute("connectors", beans);
        JMSManager mgr = PortletManager.getJMSManager(renderRequest, managerName);
        renderRequest.setAttribute("protocols", mgr.getSupportedProtocols());

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
        normalView = pc.getRequestDispatcher("/WEB-INF/view/jmsmanager/server/connector/normal.jsp");
        maximizedView = pc.getRequestDispatcher("/WEB-INF/view/jmsmanager/server/connector/maximized.jsp");
        helpView = pc.getRequestDispatcher("/WEB-INF/view/jmsmanager/server/connector/help.jsp");
        editView = pc.getRequestDispatcher("/WEB-INF/view/jmsmanager/server/connector/editGeneric.jsp");
    }

    public void destroy() {
        helpView = null;
        editView = null;
        normalView = null;
        maximizedView = null;
        super.destroy();
    }

    public final static boolean isValid(String s) {
        return s != null && !s.equals("");
    }

    public static class ConnectorWrapper {
        private String brokerName;
        private String brokerObjectName;
        private String displayName;
        private JMSConnector connector;

        public ConnectorWrapper(String brokerName, String brokerObjectName, String displayName, JMSConnector connector) {
            this.brokerName = brokerName;
            this.displayName = displayName;
            this.connector = connector;
            this.brokerObjectName = brokerObjectName;
        }

        public String getBrokerName() {
            return brokerName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public JMSConnector getConnector() {
            return connector;
        }

        public String getBrokerObjectName() {
            return brokerObjectName;
        }
    }
}
