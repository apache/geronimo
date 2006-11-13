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
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.proxy.GeronimoManagedBean;
import org.apache.geronimo.management.geronimo.KeystoreInstance;
import org.apache.geronimo.management.geronimo.KeystoreIsLocked;
import org.apache.geronimo.management.geronimo.KeystoreManager;
import org.apache.geronimo.management.geronimo.SecureConnector;
import org.apache.geronimo.management.geronimo.WebConnector;
import org.apache.geronimo.management.geronimo.WebContainer;
import org.apache.geronimo.management.geronimo.WebManager;

/**
 * A portlet that lets you list, add, remove, start, stop, and edit web
 * connectors (currently, either Tomcat or Jetty).
 *
 * @version $Rev$ $Date$
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
        String submit = actionRequest.getParameter("submit");
        if ("Cancel".equalsIgnoreCase(submit)) {
            // User clicked on "Cancel" button in add/edit connector page
            actionResponse.setRenderParameter("mode", "list");
            return;
        }
        String mode = actionRequest.getParameter("mode");
        String managerURI = actionRequest.getParameter("managerURI");
        String containerURI = actionRequest.getParameter("containerURI");
        if(managerURI != null) actionResponse.setRenderParameter("managerURI", managerURI);
        if(containerURI != null) actionResponse.setRenderParameter("containerURI", containerURI);

        String server;
        if(containerURI != null) {
            WebContainer container = PortletManager.getWebContainer(actionRequest, new AbstractName(URI.create(containerURI)));
            server = getWebServerType(container.getClass());
        } else {
            server = "unknown";
        }
        actionResponse.setRenderParameter("server", server);
        if(mode.equals("new")) {
            // User selected to add a new connector, need to show criteria portlet
            actionResponse.setRenderParameter("mode", "new");
            String protocol = actionRequest.getParameter("protocol");
            String containerDisplayName = actionRequest.getParameter("containerDisplayName");
            actionResponse.setRenderParameter("protocol", protocol);
            actionResponse.setRenderParameter("containerDisplayName", containerDisplayName);
        } else if(mode.equals("add")) { // User just submitted the form to add a new connector
            // Get submitted values
            //todo: lots of validation
            String protocol = actionRequest.getParameter("protocol");
            String host = actionRequest.getParameter("host");
            int port = Integer.parseInt(actionRequest.getParameter("port"));
            int maxThreads = Integer.parseInt(actionRequest.getParameter("maxThreads"));
            Integer minThreads = getInteger(actionRequest, "minThreads");
            String displayName = actionRequest.getParameter("displayName");
            // Create and configure the connector
            WebConnector connector = PortletManager.createWebConnector(actionRequest, new AbstractName(URI.create(managerURI)), new AbstractName(URI.create(containerURI)), displayName, protocol, host, port);
            connector.setMaxThreads(maxThreads);
            // todo: more configurable HTTP/Jetty values
            if(server.equals(WEB_SERVER_JETTY)) {
                if(minThreads != null) {
                    setProperty(connector, "minThreads", minThreads);
                }
            } else if (server.equals(WEB_SERVER_TOMCAT)) {
                //todo:   Any Tomcat specific processing?
            } else {
                //todo:   Handle "should not occur" condition
            }
            if(protocol.equals(WebManager.PROTOCOL_HTTPS)) {
                String keystoreType = actionRequest.getParameter("keystoreType");
                String keystoreFile = actionRequest.getParameter("keystoreFile");
                String privateKeyPass = actionRequest.getParameter("privateKeyPassword");
                String keystorePass = actionRequest.getParameter("keystorePassword");
                String secureProtocol = actionRequest.getParameter("secureProtocol");
                String algorithm = actionRequest.getParameter("algorithm");
                String truststoreType = actionRequest.getParameter("truststoreType");
                String truststoreFile = actionRequest.getParameter("truststoreFile");
                String truststorePass = actionRequest.getParameter("truststorePassword");
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
                    String keyStore = actionRequest.getParameter("unlockKeyStore");
                    setProperty(secure, "keyStore", keyStore);
                    try {
                        KeystoreInstance[] keystores = PortletManager.getCurrentServer(actionRequest).getKeystoreManager().getKeystores();

                        String[] keys = null;
                        for (int i = 0; i < keystores.length; i++) {
                            KeystoreInstance keystore = keystores[i];
                            if(keystore.getKeystoreName().equals(keyStore)) {
                                keys = keystore.getUnlockedKeys();
                            }
                        }
                        if(keys != null && keys.length == 1) {
                            setProperty(secure, "keyAlias", keys[0]);
                        } else {
                            throw new PortletException("Cannot handle keystores with anything but 1 unlocked private key");
                        }
                    } catch (KeystoreIsLocked locked) {
                        throw new PortletException(locked.getMessage());
                    }
                    String trustStore = actionRequest.getParameter("unlockTrustStore");
                    // "" is a valid trustStore value, which means the parameter should be cleared
                    setProperty(secure, "trustStore", isValid(trustStore) ? trustStore : null);
                } else if (server.equals(WEB_SERVER_TOMCAT)) {
                    if(isValid(truststoreType)) {setProperty(secure, "truststoreType", truststoreType);}
                    if(isValid(truststoreFile)) {setProperty(secure, "truststoreFileName", truststoreFile);}
                    if(isValid(truststorePass)) {setProperty(secure, "truststorePassword", truststorePass);}
                } else {
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
            String connectorURI = actionRequest.getParameter("connectorURI");
            // Identify and update the connector
            WebConnector connector = PortletManager.getWebConnector(actionRequest, new AbstractName(URI.create(connectorURI)));
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
                    String truststoreType = actionRequest.getParameter("truststoreType");
                    String truststoreFile = actionRequest.getParameter("truststoreFile");
                    String truststorePass = actionRequest.getParameter("truststorePassword");
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
                        String keyStore = actionRequest.getParameter("unlockKeyStore");
                        String trustStore = actionRequest.getParameter("unlockTrustStore");
                        setProperty(secure, "keyStore", keyStore);
                        try {
                            KeystoreInstance[] keystores = PortletManager.getCurrentServer(actionRequest).getKeystoreManager().getKeystores();

                            String[] keys = null;
                            for (int i = 0; i < keystores.length; i++) {
                                KeystoreInstance keystore = keystores[i];
                                if(keystore.getKeystoreName().equals(keyStore)) {
                                    keys = keystore.getUnlockedKeys();
                                }
                            }
                            if(keys != null && keys.length == 1) {
                                setProperty(secure, "keyAlias", keys[0]);
                            } else {
                                throw new PortletException("Cannot handle keystores with anything but 1 unlocked private key");
                            }
                        } catch (KeystoreIsLocked locked) {
                            throw new PortletException(locked.getMessage());
                        }
                        // "" is a valid trustStore value, which means the parameter should be cleared
                        setProperty(secure, "trustStore", isValid(trustStore) ? trustStore : null);
                    }
                    else if (server.equals(WEB_SERVER_TOMCAT)) {
                        if(isValid(truststoreType)) {setProperty(secure, "truststoreType", truststoreType);}
                        if(isValid(truststorePass)) {setProperty(secure, "truststorePassword", truststorePass);}
                        if(isValid(truststoreFile)) {setProperty(secure, "truststoreFileName", truststoreFile);}
                    }
                    else {
                        //todo:   Handle "should not occur" condition
                    }
                }
            }
            actionResponse.setRenderParameter("mode", "list");
        } else if(mode.equals("start")) {
            String connectorURI = actionRequest.getParameter("connectorURI");
            // work with the current connector to start it.
            WebConnector connector = PortletManager.getWebConnector(actionRequest, new AbstractName(URI.create(connectorURI)));
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
            actionResponse.setRenderParameter("connectorURI", connectorURI);
            actionResponse.setRenderParameter("mode", "list");
        } else if(mode.equals("stop")) {
            String connectorURI = actionRequest.getParameter("connectorURI");
            // work with the current connector to stop it.
            WebConnector connector = PortletManager.getWebConnector(actionRequest, new AbstractName(URI.create(connectorURI)));
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
            actionResponse.setRenderParameter("connectorURI", connectorURI);
            actionResponse.setRenderParameter("mode", "list");
        } else if(mode.equals("edit")) {
            String connectorURI = actionRequest.getParameter("connectorURI");
            actionResponse.setRenderParameter("connectorURI", connectorURI);
            actionResponse.setRenderParameter("mode", "edit");

        } else if(mode.equals("delete")) { // User chose to delete a connector
            String connectorURI = actionRequest.getParameter("connectorURI");
            PortletManager.getWebManager(actionRequest, new AbstractName(URI.create(managerURI))).removeConnector(new AbstractName(URI.create(connectorURI)));
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
            String managerURI = renderRequest.getParameter("managerURI");
            String containerURI = renderRequest.getParameter("containerURI");
            if(managerURI != null) renderRequest.setAttribute("managerURI", managerURI);
            if(containerURI != null) renderRequest.setAttribute("containerURI", containerURI);

            WebContainer container = PortletManager.getWebContainer(renderRequest, new AbstractName(URI.create(containerURI)));
            String server = getWebServerType(container.getClass());
            renderRequest.setAttribute("server", server);

            if(mode.equals("new")) {
                String protocol = renderRequest.getParameter("protocol");
                String containerDisplayName = renderRequest.getParameter("containerDisplayName");
                renderRequest.setAttribute("maxThreads", "50");
                if(server.equals(WEB_SERVER_JETTY)) {
                    renderRequest.setAttribute("minThreads", "10");
                    KeystoreManager mgr = PortletManager.getCurrentServer(renderRequest).getKeystoreManager();
                    KeystoreInstance[] stores = mgr.getUnlockedKeyStores();
                    String[] storeNames = new String[stores.length];
                    for (int i = 0; i < storeNames.length; i++) {
                        storeNames[i] = stores[i].getKeystoreName();
                    }
                    renderRequest.setAttribute("keyStores", storeNames);
                    KeystoreInstance[] trusts = mgr.getUnlockedTrustStores();
                    String[] trustNames = new String[trusts.length];
                    for (int i = 0; i < trustNames.length; i++) {
                        trustNames[i] = trusts[i].getKeystoreName();
                    }
                    renderRequest.setAttribute("trustStores", trustNames);
                    Map aliases = new HashMap();
                    for (int i = 0; i < stores.length; i++) {
                        try {
                            aliases.put(stores[i].getKeystoreName(), stores[i].getUnlockedKeys());
                        } catch (KeystoreIsLocked locked) {}
                    }
                    renderRequest.setAttribute("unlockedKeys", aliases);
                }
                else if (server.equals(WEB_SERVER_TOMCAT)) {
                    //todo:   Any Tomcat specific processing?
                }
                else {
                    //todo:   Handle "should not occur" condition
                }
                renderRequest.setAttribute("protocol", protocol);
                renderRequest.setAttribute("mode", "add");
                renderRequest.setAttribute("containerDisplayName", containerDisplayName);
                if(protocol.equals(WebManager.PROTOCOL_HTTPS)) {
                    editHttpsView.include(renderRequest, renderResponse);
                } else {
                    editHttpView.include(renderRequest, renderResponse);
                }

            } else if(mode.equals("edit")) {
                String connectorURI = renderRequest.getParameter("connectorURI");
                WebConnector connector = PortletManager.getWebConnector(renderRequest, new AbstractName(URI.create(connectorURI)));
                if(connector == null) {
                    doList(renderRequest, renderResponse);
                } else {
                	String displayName = new AbstractName(URI.create(connectorURI)).getName().get("name").toString();
                    renderRequest.setAttribute("displayName", displayName);
                    renderRequest.setAttribute("connectorURI", connectorURI);
                    renderRequest.setAttribute("port", new Integer(connector.getPort()));
                    renderRequest.setAttribute("host", connector.getHost());
                    int maxThreads = connector.getMaxThreads();
                    renderRequest.setAttribute("maxThreads", Integer.toString(maxThreads));
                    if(server.equals(WEB_SERVER_JETTY)) {
                        int minThreads = ((Number)getProperty(connector, "minThreads")).intValue();
                        renderRequest.setAttribute("minThreads", String.valueOf(minThreads));
                        KeystoreManager mgr = PortletManager.getCurrentServer(renderRequest).getKeystoreManager();
                        KeystoreInstance[] stores = mgr.getUnlockedKeyStores();
                        String[] storeNames = new String[stores.length];
                        for (int i = 0; i < storeNames.length; i++) {
                            storeNames[i] = stores[i].getKeystoreName();
                        }
                        renderRequest.setAttribute("keyStores", storeNames);
                        KeystoreInstance[] trusts = mgr.getUnlockedTrustStores();
                        String[] trustNames = new String[trusts.length];
                        for (int i = 0; i < trustNames.length; i++) {
                            trustNames[i] = trusts[i].getKeystoreName();
                        }
                        renderRequest.setAttribute("trustStores", trustNames);
                        Map aliases = new HashMap();
                        for (int i = 0; i < stores.length; i++) {
                            try {
                                aliases.put(stores[i].getKeystoreName(), stores[i].getUnlockedKeys());
                            } catch (KeystoreIsLocked locked) {}
                        }
                        renderRequest.setAttribute("unlockedKeys", aliases);
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
                        if(server.equals(WEB_SERVER_JETTY)) {
                            String keyStore = (String)getProperty(secure, "keyStore");
                            String trustStore = (String)getProperty(secure, "trustStore");
                            renderRequest.setAttribute("unlockKeyStore", keyStore);
                            renderRequest.setAttribute("unlockTrustStore", trustStore);
                        } else if(server.equals(WEB_SERVER_TOMCAT)) {
                            String truststoreFile = (String)getProperty(secure, "truststoreFileName");
                            String truststoreType = (String)getProperty(secure, "truststoreType");
                            renderRequest.setAttribute("truststoreFile", truststoreFile);
                            renderRequest.setAttribute("truststoreType", truststoreType);
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
            AbstractName webManagerName = PortletManager.getNameFor(renderRequest, manager);

            WebContainer[] containers = (WebContainer[]) manager.getContainers();
            for (int j = 0; j < containers.length; j++) {
                List beans = new ArrayList();
                WebContainer container = containers[j];
                AbstractName containerName = PortletManager.getNameFor(renderRequest, container);
                String id;
                if(containers.length == 1) {
                    id = manager.getProductName();
                } else {
                    id = manager.getProductName() + " (" + containerName.getName().get(NameFactory.J2EE_NAME) + ")";
                }
                ContainerInfo result = new ContainerInfo(id, webManagerName.toString(), containerName.toString());

                WebConnector[] connectors = (WebConnector[]) manager.getConnectorsForContainer(container);
                for (int k = 0; k < connectors.length; k++) {
                    WebConnector connector = connectors[k];
                    ConnectorInfo info = new ConnectorInfo();
                    AbstractName connectorName = PortletManager.getNameFor(renderRequest, connector);
                    info.setConnectorURI(connectorName.toString());
                    info.setDescription(PortletManager.getGBeanDescription(renderRequest, connectorName));
                    info.setDisplayName((String)connectorName.getName().get(NameFactory.J2EE_NAME));
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
            }
        }
        renderRequest.setAttribute("containers", all);

        if (WindowState.NORMAL.equals(renderRequest.getWindowState())) {
            normalView.include(renderRequest, renderResponse);
        } else {
            maximizedView.include(renderRequest, renderResponse);
        }
    }

    public final static class ContainerInfo {
        private String name;
        private String managerURI;
        private String containerURI;
        private String[] protocols;
        private List connectors;

        public ContainerInfo(String name, String managerURI, String containerURI) {
            this.name = name;
            this.managerURI = managerURI;
            this.containerURI = containerURI;
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

        public String getManagerURI() {
            return managerURI;
        }

        public String getContainerURI() {
            return containerURI;
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

    public static boolean isValid(String s) {
        return s != null && !s.equals("");
    }

}
