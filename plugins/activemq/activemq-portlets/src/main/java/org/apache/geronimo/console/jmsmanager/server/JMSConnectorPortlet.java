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
package org.apache.geronimo.console.jmsmanager.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.net.URI;

import javax.portlet.PortletRequestDispatcher;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.kernel.proxy.GeronimoManagedBean;
import org.apache.geronimo.management.activemq.ActiveMQConnector;
import org.apache.geronimo.management.geronimo.JMSConnector;
import org.apache.geronimo.management.geronimo.JMSManager;
import org.apache.geronimo.management.geronimo.JMSBroker;
import org.apache.geronimo.management.geronimo.NetworkConnector;
import org.apache.geronimo.gbean.AbstractName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * List, edit, add, remove JMS network connectors
 *
 * @version $Rev$ $Date$
 */
public class JMSConnectorPortlet extends BaseJMSPortlet {
    private static final Logger log = LoggerFactory.getLogger(JMSConnectorPortlet.class);

    private PortletRequestDispatcher normalView;

    private PortletRequestDispatcher maximizedView;

    private PortletRequestDispatcher helpView;

    protected PortletRequestDispatcher editView;

    public void processAction(ActionRequest actionRequest,
                              ActionResponse actionResponse) throws PortletException, IOException {
        try {
            String mode = actionRequest.getParameter("mode");
            String connectorURI = actionRequest.getParameter("connectorURI");
            String brokerURI = actionRequest.getParameter("brokerURI");
            JMSManager manager = PortletManager.getCurrentServer(actionRequest).getJMSManagers()[0];  //todo: handle multiple
            if(mode.equals("new")) {
                // User selected to add a new connector, need to show criteria portlet
                actionResponse.setRenderParameter("mode", "new");
                String protocol = actionRequest.getParameter("protocol");
                actionResponse.setRenderParameter("protocol", protocol);
                actionResponse.setRenderParameter("brokerURI", brokerURI);
            } else if(mode.equals("add")) { // User just submitted the form to add a new connector
                // Get submitted values
                //todo: lots of validation
                String protocol = actionRequest.getParameter("protocol");
                String host = actionRequest.getParameter("host");
                int port = Integer.parseInt(actionRequest.getParameter("port"));
                String name = actionRequest.getParameter("name");
                AbstractName brokerAbstractName = new AbstractName(URI.create(brokerURI));
                // Create and configure the connector
                JMSConnector connector = PortletManager.createJMSConnector(actionRequest, manager, brokerAbstractName, name, protocol, host, port);
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
                // Identify and update the connector
                AbstractName connectorAbstractName = new AbstractName(URI.create(connectorURI));
                JMSConnector connector = (JMSConnector)PortletManager.getManagedBean(actionRequest, connectorAbstractName);
                if(connector != null) {
                    connector.setHost(host);
                    connector.setPort(port);
                }
                actionResponse.setRenderParameter("mode", "list");
            } else if(mode.equals("start")) {
                AbstractName connectorAbstractName = new AbstractName(URI.create(connectorURI));
                try {
                    PortletManager.getManagedBean(actionRequest, connectorAbstractName).startRecursive();
                } catch (Exception e) {
                    throw new PortletException(e);
                }
                actionResponse.setRenderParameter("mode", "list");
            } else if(mode.equals("stop")) {
                AbstractName connectorAbstractName = new AbstractName(URI.create(connectorURI));
                try {
                    PortletManager.getManagedBean(actionRequest, connectorAbstractName).stop();
                } catch (Exception e) {
                    throw new PortletException(e);
                }
                actionResponse.setRenderParameter("mode", "list");
            } else if(mode.equals("edit")) {
                actionResponse.setRenderParameter("connectorURI", connectorURI);
                actionResponse.setRenderParameter("brokerURI", brokerURI);
                actionResponse.setRenderParameter("mode", "edit");
            } else if(mode.equals("delete")) {
                AbstractName connectorAbstractName = new AbstractName(URI.create(connectorURI));
                manager.removeConnector(connectorAbstractName);
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
            //JMSManager manager = PortletManager.getCurrentServer(renderRequest).getJMSManagers()[0];  //todo: handle multiple

            if(mode.equals("new")) {
                String brokerURI = renderRequest.getParameter("brokerURI");
                String protocol = renderRequest.getParameter("protocol");
                renderRequest.setAttribute("protocol", protocol);
                renderRequest.setAttribute("brokerURI", brokerURI);
                renderRequest.setAttribute("brokerName", new AbstractName(URI.create(brokerURI)).getName().get("name").toString());
                renderRequest.setAttribute("mode", "add");
                editView.include(renderRequest, renderResponse);
            } else if(mode.equals("edit")) {
                /*
                String brokerURI = renderRequest.getParameter("brokerURI");
                String connectorURI = renderRequest.getParameter("connectorURI");
                JMSConnector connector = (JMSConnector)PortletManager.getManagedBean(renderRequest, new AbstractName(URI.create(connectorURI)));
                if(connector == null) {
                    doList(renderRequest, manager, renderResponse);
                } else {
                    renderRequest.setAttribute("connectorURI", connectorURI);
                    renderRequest.setAttribute("brokerName", new AbstractName(URI.create(brokerURI)).getName().get("name").toString());
                    renderRequest.setAttribute("connectorName", new AbstractName(URI.create(connectorURI)).getName().get("name").toString());
                    renderRequest.setAttribute("protocol", connector.getProtocol());
                    renderRequest.setAttribute("port", new Integer(connector.getPort()));
                    renderRequest.setAttribute("host", connector.getHost());
                    renderRequest.setAttribute("mode", "save");
                    editView.include(renderRequest, renderResponse);
                }
                */
            } else if(mode.equals("list")) {
                doList(renderRequest, renderResponse);
            }
        } catch (Throwable e) {
            log.error("Unable to render portlet", e);
        }
    }

    private void doList(RenderRequest renderRequest, RenderResponse renderResponse) throws PortletException, IOException {
        List<ConnectorWrapper> connectorWrappers = new ArrayList<ConnectorWrapper>();
        Map<String, BrokerServiceWrapper> brokerServices = getBrokerServices();
        Collection<BrokerServiceWrapper> wrappers = brokerServices.values();
        for (BrokerServiceWrapper wrapper: wrappers) {
            BrokerService brokerService = wrapper.getBrokerService();
            List<TransportConnector> connectors = brokerService.getTransportConnectors();
            for (TransportConnector connector: connectors) {
                String connectorURI = connector.getUri().toString();
                String brokerName = wrapper.getBrokerName();
                String protocol = connector.getUri().getScheme();
                int port = connector.getUri().getPort();
                ConnectorWrapper connectorWrapper = new ConnectorWrapper(connectorURI, brokerName, protocol, port);
                connectorWrappers.add(connectorWrapper);
            }
        }
        //renderRequest.setAttribute("brokers", getBrokerList(renderRequest, manager));
        renderRequest.setAttribute("connectors", connectorWrappers);
        //ArrayList protocols = new ArrayList(Arrays.asList(manager.getSupportedProtocols()));
        //protocols.remove("peer"); // add operation not supported for peer protocol
        //protocols.remove("failover"); // add operation not supported for failover protocol
        //renderRequest.setAttribute("protocols", protocols);

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

    public static boolean isValid(String s) {
        return s != null && !s.equals("");
    }

    public static class ConnectorWrapper {
        
        private String connectorURI;
        private String brokerName;
        private String protocol;
        private int port;
        //private TransportConnector connector;

        public ConnectorWrapper(String connectorURI, String brokerName, String protocol, int port) {
            this.connectorURI = connectorURI;
            this.brokerName = brokerName;
            this.protocol = protocol;
            this.port = port;
        }
        
        public String getConnectorURI() {
            return connectorURI;
        }
        
        public String getBrokerName() {
            return brokerName;
        }

        public String getProtocol() {
            return protocol;
        }
        public int getPort() {
            return port;
        }
    }
}
