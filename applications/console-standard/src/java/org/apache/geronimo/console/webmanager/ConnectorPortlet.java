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
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import javax.portlet.PortletContext;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.management.geronimo.WebContainer;
import org.apache.geronimo.management.geronimo.WebConnector;
import org.apache.geronimo.management.geronimo.SecureConnector;
import org.apache.geronimo.management.geronimo.WebManager;
import org.apache.geronimo.kernel.proxy.GeronimoManagedBean;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A portlet that lets you list, add, remove, start, stop, and edit web
 * connectors (currently, either Tomcat or Jetty).
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class ConnectorPortlet extends BasePortlet {
    private final static Log log = LogFactory.getLog(ConnectorPortlet.class);

    private PortletRequestDispatcher normalView;

    private PortletRequestDispatcher maximizedView;

    private PortletRequestDispatcher helpView;

    protected PortletRequestDispatcher editHttpView;
    protected PortletRequestDispatcher editHttpsView;

    public void processAction(ActionRequest actionRequest,
            ActionResponse actionResponse) throws PortletException, IOException {
        String mode = actionRequest.getParameter("mode");
        String managerName = actionRequest.getParameter("managerObjectName");
        String containerName = actionRequest.getParameter("containerObjectName");
        if(managerName != null) actionResponse.setRenderParameter("managerObjectName", managerName);
        if(containerName != null) actionResponse.setRenderParameter("containerObjectName", containerName);

        WebContainer container = PortletManager.getWebContainer(actionRequest, containerName);
        String server = getWebServerType(container.getClass());
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
            WebConnector connector = PortletManager.createWebConnector(actionRequest, managerName, containerName, name, protocol, host, port);
            connector.setMaxThreads(maxThreads);
            // todo: more configurable HTTP/Jetty values
            if(server.equals(WEB_SERVER_JETTY)) {
                if(minThreads != null) {
                    setProperty(connector, "minThreads", minThreads);
                }
            }
            else if (server.equals(WEB_SERVER_TOMCAT)) {
                //todo:   Any Tomcat specific processing?
            }
            else {
                //todo:   Handle "should not occur" condition
            }
            if(protocol.equals(WebManager.PROTOCOL_HTTPS)) {
                String keystoreType = actionRequest.getParameter("keystoreType");
                String keystoreFile = actionRequest.getParameter("keystoreFile");
                String privateKeyPass = actionRequest.getParameter("privateKeyPassword");
                String keystorePass = actionRequest.getParameter("keystorePassword");
                String secureProtocol = actionRequest.getParameter("secureProtocol");
                String algorithm = actionRequest.getParameter("algorithm");
                boolean clientAuth = isValid(actionRequest.getParameter("clientAuth"));
                SecureConnector secure = (SecureConnector) connector;
                if(isValid(keystoreType)) {secure.setKeystoreType(keystoreType);}
                if(isValid(keystoreFile)) {secure.setKeystoreFileName(keystoreFile);}
                if(isValid(keystorePass)) {secure.setKeystorePassword(keystorePass);}
                if(isValid(secureProtocol)) {secure.setSecureProtocol(secureProtocol);}
                if(isValid(algorithm)) {secure.setAlgorithm(algorithm);}
                secure.setClientAuthRequired(clientAuth);
                if(server.equals(WEB_SERVER_JETTY)) {
                    if(isValid(privateKeyPass)) {setProperty(secure, "keyPassword", privateKeyPass);}
                }
                else if (server.equals(WEB_SERVER_TOMCAT)) {
                    //todo:   Any Tomcat specific processing?
                }
                else {
                    //todo:   Handle "should not occur" condition
                }
            }
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
            int maxThreads = Integer.parseInt(actionRequest.getParameter("maxThreads"));
            Integer minThreads = getInteger(actionRequest, "minThreads");
            String objectName = actionRequest.getParameter("objectName");
            // Identify and update the connector
            WebConnector connector = null;
            WebConnector all[] = PortletManager.getWebConnectors(actionRequest, managerName);
            for (int i = 0; i < all.length; i++) {
                WebConnector conn = all[i];
                if(((GeronimoManagedBean)conn).getObjectName().equals(objectName)) {
                    connector = conn;
                    break;
                }
            }
            if(connector != null) {
                connector.setHost(host);
                connector.setPort(port);
                connector.setMaxThreads(maxThreads);
                if(server.equals(WEB_SERVER_JETTY)) {
                    if(minThreads != null) {
                        setProperty(connector,"minThreads",minThreads);
                    }
                    else if (server.equals(WEB_SERVER_TOMCAT)) {
                        //todo:   Any Tomcat specific processing?
                    }
                    else {
                        //todo:   Handle "should not occur" condition
                    }
                }
                if(connector instanceof SecureConnector) {
                    String keystoreType = actionRequest.getParameter("keystoreType");
                    String keystoreFile = actionRequest.getParameter("keystoreFile");
                    String privateKeyPass = actionRequest.getParameter("privateKeyPassword");
                    String keystorePass = actionRequest.getParameter("keystorePassword");
                    String secureProtocol = actionRequest.getParameter("secureProtocol");
                    String algorithm = actionRequest.getParameter("algorithm");
                    boolean clientAuth = isValid(actionRequest.getParameter("clientAuth"));
                    SecureConnector secure = (SecureConnector) connector;
                    if(isValid(keystoreType)) {secure.setKeystoreType(keystoreType);}
                    if(isValid(keystoreFile)) {secure.setKeystoreFileName(keystoreFile);}
                    if(isValid(keystorePass)) {secure.setKeystorePassword(keystorePass);}
                    if(isValid(secureProtocol)) {secure.setSecureProtocol(secureProtocol);}
                    if(isValid(algorithm)) {secure.setAlgorithm(algorithm);}
                    secure.setClientAuthRequired(clientAuth);
                    if(server.equals(WEB_SERVER_JETTY)) {
                        if(isValid(privateKeyPass)) {setProperty(secure, "keyPassword", privateKeyPass);}
                    }
                    else if (server.equals(WEB_SERVER_TOMCAT)) {
                        //todo:   Any Tomcat specific processing?
                    }
                    else {
                        //todo:   Handle "should not occur" condition
                    }
                }
            }
            actionResponse.setRenderParameter("mode", "list");
        } else if(mode.equals("start")) {
            String objectName = actionRequest.getParameter("name");
            // work with the current connector to start it.
            WebConnector connector = null;
            WebConnector all[] = PortletManager.getWebConnectors(actionRequest, managerName);
            for (int i = 0; i < all.length; i++) {
                WebConnector conn = all[i];
                if(((GeronimoManagedBean)conn).getObjectName().equals(objectName)) {
                    connector = conn;
                    break;
                }
            }
            if(connector != null) {
                try {
                    ((GeronimoManagedBean)connector).startRecursive();
                } catch (Exception e) {
                    log.error("Unable to start connector", e); //todo: get into rendered page somehow?
                }
            }
            else {
                log.error("Incorrect connector reference"); //Replace this with correct error processing
            }
            actionResponse.setRenderParameter("name", objectName);
            actionResponse.setRenderParameter("mode", "list");
        } else if(mode.equals("stop")) {
            String objectName = actionRequest.getParameter("name");
            // work with the current connector to stop it.
            WebConnector connector = null;
            WebConnector all[] = PortletManager.getWebConnectors(actionRequest, managerName);
            for (int i = 0; i < all.length; i++) {
                WebConnector conn = all[i];
                if(((GeronimoManagedBean)conn).getObjectName().equals(objectName)) {
                    connector = conn;
                    break;
                }
            }
            if(connector != null) {
                try {
                    ((GeronimoManagedBean)connector).stop();
                } catch (Exception e) {
                    log.error("Unable to stop connector", e); //todo: get into rendered page somehow?
                }
            }
            else {
                log.error("Incorrect connector reference"); //Replace this with correct error processing
            }
            actionResponse.setRenderParameter("name", objectName);
            actionResponse.setRenderParameter("mode", "list");
        } else if(mode.equals("edit")) {
            String objectName = actionRequest.getParameter("name");
            actionResponse.setRenderParameter("objectName", objectName);
            actionResponse.setRenderParameter("mode", "edit");

        } else if(mode.equals("delete")) { // User chose to delete a connector
            String objectName = actionRequest.getParameter("name");
            PortletManager.getWebManager(actionRequest, managerName).removeConnector(objectName);
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


        if(mode.equals("list")) {
            doList(renderRequest, renderResponse);
        } else {
            String managerName = renderRequest.getParameter("managerObjectName");
            String containerName = renderRequest.getParameter("containerObjectName");
            if(managerName != null) renderRequest.setAttribute("managerObjectName", managerName);
            if(containerName != null) renderRequest.setAttribute("containerObjectName", containerName);

            WebContainer container = PortletManager.getWebContainer(renderRequest, containerName);
            String server = getWebServerType(container.getClass());
            renderRequest.setAttribute("server", server);

            if(mode.equals("new")) {
                String protocol = renderRequest.getParameter("protocol");
                renderRequest.setAttribute("maxThreads", "50");
                if(server.equals(WEB_SERVER_JETTY)) {
                    renderRequest.setAttribute("minThreads", "10");
                }
                else if (server.equals(WEB_SERVER_TOMCAT)) {
                    //todo:   Any Tomcat specific processing?
                }
                else {
                    //todo:   Handle "should not occur" condition
                }
                renderRequest.setAttribute("protocol", protocol);
                renderRequest.setAttribute("mode", "add");
                if(protocol.equals(WebManager.PROTOCOL_HTTPS)) {
                    editHttpsView.include(renderRequest, renderResponse);
                } else {
                    editHttpView.include(renderRequest, renderResponse);
                }

            } else if(mode.equals("edit")) {
                String objectName = renderRequest.getParameter("objectName");
                WebConnector connector = null;
                WebConnector all[] = PortletManager.getWebConnectors(renderRequest, managerName);
                for (int i = 0; i < all.length; i++) {
                    WebConnector conn = all[i];
                    if(((GeronimoManagedBean)conn).getObjectName().equals(objectName)) {
                        connector = conn;
                        break;
                    }
                }
                if(connector == null) {
                    doList(renderRequest, renderResponse);
                } else {
                    renderRequest.setAttribute("objectName", objectName);
                    renderRequest.setAttribute("port", new Integer(connector.getPort()));
                    renderRequest.setAttribute("host", connector.getHost());
                    int maxThreads = connector.getMaxThreads();
                    renderRequest.setAttribute("maxThreads", Integer.toString(maxThreads));
                    if(server.equals(WEB_SERVER_JETTY)) {
                        int minThreads = ((Number)getProperty(connector, "minThreads")).intValue();
                        renderRequest.setAttribute("minThreads", String.valueOf(minThreads));
                    }
                    else if (server.equals(WEB_SERVER_TOMCAT)) {
                        //todo:   Any Tomcat specific processing?
                    }
                    else {
                        //todo:   Handle "should not occur" condition
                    }
                    renderRequest.setAttribute("mode", "save");

                    if(connector instanceof SecureConnector) {
                        SecureConnector secure = (SecureConnector) connector;
                        renderRequest.setAttribute("keystoreFile",secure.getKeystoreFileName());
                        renderRequest.setAttribute("keystoreType",secure.getKeystoreType());
                        renderRequest.setAttribute("algorithm",secure.getAlgorithm());
                        renderRequest.setAttribute("secureProtocol",secure.getSecureProtocol());
                        if(secure.isClientAuthRequired()) {
                            renderRequest.setAttribute("clientAuth", Boolean.TRUE);
                        }
                    }

                    if(connector.getProtocol().equals(WebManager.PROTOCOL_HTTPS)) {
                        editHttpsView.include(renderRequest, renderResponse);
                    } else {
                        editHttpView.include(renderRequest, renderResponse);
                    }
                }
            }
        }

    }

    private void doList(RenderRequest renderRequest, RenderResponse renderResponse) throws PortletException, IOException {
        WebManager[] managers = PortletManager.getWebManagers(renderRequest);
        List all = new ArrayList();
        for (int i = 0; i < managers.length; i++) {
            WebManager manager = managers[i];
            String webManagerName = ((GeronimoManagedBean)manager).getObjectName();

            String[] containers = PortletManager.getWebContainerNames(renderRequest, webManagerName);
            for (int j = 0; j < containers.length; j++) {
                try {
                    List beans = new ArrayList();
                    String containerName = containers[j];
                    String id;
                    if(containers.length == 1) {
                        id = manager.getProductName();
                    } else {
                        ObjectName containerON = ObjectName.getInstance(containerName);
                        id = manager.getProductName() + " (" + containerON.getKeyProperty(NameFactory.J2EE_NAME) + ")";
                    }
                    ContainerStatus result = new ContainerStatus(id, webManagerName, containerName);

                    WebConnector[] connectors = PortletManager.getWebConnectorsForContainer(renderRequest, webManagerName, containerName);
                    for (int k = 0; k < connectors.length; k++) {
                        WebConnector connector = connectors[k];
                        ConnectorInfo info = new ConnectorInfo();
                            String objectName = ((GeronimoManagedBean)connector).getObjectName();
                            info.setObjectName(objectName);
                            info.setDescription(PortletManager.getGBeanDescription(renderRequest, objectName));
                            try {
                                ObjectName realName = ObjectName.getInstance(objectName);
                                info.setDisplayName(realName.getKeyProperty("name"));
                            } catch (MalformedObjectNameException e) {
                                log.error("Bad object name for web connector", e);
                                info.setDisplayName(info.getDescription());
                            }
                            info.setState(((GeronimoManagedBean)connector).getState());
                            info.setPort(connector.getPort());
                        try {
                            info.setProtocol(connector.getProtocol());
                        } catch (IllegalStateException e) {
                            info.setProtocol("unknown");
                        }
                        beans.add(info);
                    }
                    result.setConnectors(beans);
                    result.setProtocols(manager.getSupportedProtocols());
                    all.add(result);
                } catch (MalformedObjectNameException e) {
                    log.error("Unable to parse ObjectName", e);
                }
            }
        }
        renderRequest.setAttribute("containers", all);

        if (WindowState.NORMAL.equals(renderRequest.getWindowState())) {
            normalView.include(renderRequest, renderResponse);
        } else {
            maximizedView.include(renderRequest, renderResponse);
        }
    }

    public final static class ContainerStatus {
        private String name;
        private String managerObjectName;
        private String containerObjectName;
        private String[] protocols;
        private List connectors;

        public ContainerStatus(String name, String managerObjectName, String containerObjectName) {
            this.name = name;
            this.managerObjectName = managerObjectName;
            this.containerObjectName = containerObjectName;
        }

        public String getName() {
            return name;
        }

        public String[] getProtocols() {
            return protocols;
        }

        public void setProtocols(String[] protocols) {
            this.protocols = protocols;
        }

        public List getConnectors() {
            return connectors;
        }

        public void setConnectors(List connectors) {
            this.connectors = connectors;
        }

        public String getManagerObjectName() {
            return managerObjectName;
        }

        public String getContainerObjectName() {
            return containerObjectName;
        }
    }

    protected void doHelp(RenderRequest renderRequest,
            RenderResponse renderResponse) throws PortletException, IOException {
        helpView.include(renderRequest, renderResponse);
    }

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
        PortletContext pc = portletConfig.getPortletContext();
        normalView = pc.getRequestDispatcher("/WEB-INF/view/webmanager/connector/normal.jsp");
        maximizedView = pc.getRequestDispatcher("/WEB-INF/view/webmanager/connector/maximized.jsp");
        helpView = pc.getRequestDispatcher("/WEB-INF/view/webmanager/connector/help.jsp");
        editHttpView = pc.getRequestDispatcher("/WEB-INF/view/webmanager/connector/editHTTP.jsp");
        editHttpsView = pc.getRequestDispatcher("/WEB-INF/view/webmanager/connector/editHTTPS.jsp");
    }

    public void destroy() {
        normalView = null;
        maximizedView = null;
        helpView = null;
        editHttpsView = null;
        editHttpView = null;
        super.destroy();
    }

    public final static boolean isValid(String s) {
        return s != null && !s.equals("");
    }

}
