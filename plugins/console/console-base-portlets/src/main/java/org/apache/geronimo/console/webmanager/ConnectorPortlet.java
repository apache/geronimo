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

package org.apache.geronimo.console.webmanager;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.crypto.KeystoreUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.proxy.GeronimoManagedBean;
import org.apache.geronimo.management.geronimo.KeystoreException;
import org.apache.geronimo.management.geronimo.KeystoreInstance;
import org.apache.geronimo.management.geronimo.KeystoreManager;
import org.apache.geronimo.management.geronimo.NetworkConnector;
import org.apache.geronimo.management.geronimo.SecureConnector;
import org.apache.geronimo.management.geronimo.WebContainer;
import org.apache.geronimo.management.geronimo.WebManager;
import org.apache.geronimo.management.geronimo.WebManager.ConnectorAttribute;
import org.apache.geronimo.management.geronimo.WebManager.ConnectorType;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A portlet that lets you list, add, remove, start, stop, restart and edit web
 * connectors (currently, either Tomcat or Jetty).
 *
 * @version $Rev$ $Date$
 */
public class ConnectorPortlet extends BasePortlet {
    private static final Logger log = LoggerFactory.getLogger(ConnectorPortlet.class);

    public static final String PARM_CONTAINER_URI = "containerURI";
    public static final String PARM_CONNECTOR_URI = "connectorURI";
    public static final String PARM_MANAGER_URI = "managerURI";
    public static final String PARM_MODE = "mode";
    public static final String PARM_CONNECTOR_TYPE = "connectorType";
    public static final String PARM_CONNECTOR_ATTRIBUTES = "connectorAttributes";
    public static final String PARM_DISPLAY_NAME = "uniqueName";
    public static final String PARM_SERVER = "server";

    private PortletRequestDispatcher normalView;
    private PortletRequestDispatcher maximizedView;
    private PortletRequestDispatcher helpView;
    private PortletRequestDispatcher editConnectorView;

    public void processAction(ActionRequest actionRequest,
                              ActionResponse actionResponse) throws PortletException, IOException {
        String submit = actionRequest.getParameter("submit");
        if ("Cancel".equalsIgnoreCase(submit)) {
            // User clicked on "Cancel" button in add/edit connector page
            actionResponse.setRenderParameter(PARM_MODE, "list");
            return;
        }

        String mode = actionRequest.getParameter(PARM_MODE);
        String managerURI = actionRequest.getParameter(PARM_MANAGER_URI);
        String containerURI = actionRequest.getParameter(PARM_CONTAINER_URI);
        if(managerURI != null) actionResponse.setRenderParameter(PARM_MANAGER_URI, managerURI);
        if(containerURI != null) actionResponse.setRenderParameter(PARM_CONTAINER_URI, containerURI);
        WebContainer webContainer  = null;
        String server = null;
        if(containerURI != null) {
            webContainer = PortletManager.getWebContainer(actionRequest, new AbstractName(URI.create(containerURI)));
            server = getWebServerType(webContainer.getClass());
        } else {
            server = "unknown";
        }
        actionResponse.setRenderParameter(PARM_SERVER, server);
        if(mode.equals("new")) {
            // User selected to add a new connector, need to show criteria portlet
            actionResponse.setRenderParameter(PARM_MODE, "new");
            String connectorType = actionRequest.getParameter(PARM_CONNECTOR_TYPE);
            actionResponse.setRenderParameter(PARM_CONNECTOR_TYPE, connectorType);
        } else if(mode.equals("add")) { // User just submitted the form to add a new connector
            // Create and configure the connector
            WebManager manager = PortletManager.getWebManager(actionRequest, new AbstractName(URI.create(managerURI)));
            ConnectorType connectorType = new ConnectorType(actionRequest.getParameter(PARM_CONNECTOR_TYPE));

            String uniqueName = actionRequest.getParameter(PARM_DISPLAY_NAME);
            actionResponse.setRenderParameter(PARM_DISPLAY_NAME, uniqueName);
            // set the connector attributes from the form post
            List<ConnectorAttribute> connectorAttributes = manager.getConnectorAttributes(connectorType);
            for (ConnectorAttribute attribute : connectorAttributes) {
                String name = attribute.getAttributeName();
                String value = actionRequest.getParameter(name);

                // handle booelan type special
                if (attribute.getAttributeClass().equals(Boolean.class)) {
                    // browser sends value of checked checkbox as "on" or "checked"
                    if ("on".equalsIgnoreCase(value) || "checked".equalsIgnoreCase(value)) {
                        value=Boolean.toString(true);
                    } else {
                        value=Boolean.toString(false);
                    }
                }
                // set the string form of the attribute's value as submitted by the browser
                if (value == null || value.trim().length()<1) {
                    // special case for KeystoreManager gbean
                    if ("trustStore".equals(attribute.getAttributeName())) {
                        attribute.setValue(null);
                    }
                } else {
                    attribute.setStringValue(value.trim());
                }
            }
            // create the connector gbean based on the configuration data
            AbstractName newConnectorName = manager.getConnectorConfiguration(connectorType, connectorAttributes, webContainer, uniqueName);

            // set the keystore properties if its a secure connector
            setKeystoreProperties(actionRequest, newConnectorName);

            // Start the connector
            try {
                GeronimoManagedBean managedBean = PortletManager.getManagedBean(actionRequest, newConnectorName);
                managedBean.startRecursive();
            } catch (Exception e) {
                log.error("Unable to start connector", e); //TODO: get into rendered page
            }

            try {
                manager.updateConnectorConfig(newConnectorName);
            } catch (Exception e) {
                log.error("Unable to update connector in server.xml", e); //TODO: get into rendered page
            }
            actionResponse.setRenderParameter(PARM_MODE, "list");
        } else if(mode.equals("save")) { // User just submitted the form to update a connector
            // Get submitted values
            //todo: lots of validation
            String connectorURI = actionRequest.getParameter(PARM_CONNECTOR_URI);
            // Identify and update the connector
            AbstractName connectorName = new AbstractName(URI.create(connectorURI));
            GBeanData connectorGBeanData=null;
            try {
                connectorGBeanData=PortletManager.getKernel().getGBeanData(connectorName);
            } catch (Exception e) {
                log.error("Unable to get connectorGBeanData by abstractName:"+connectorName.toURI(), e);
            }

            // Store the port info of the connector currently modified
            String toBeUpdatedConnectorName = (String) connectorGBeanData.getAttribute("name");
            Integer toBeUpdatedConnectorPort = (Integer) connectorGBeanData.getAttribute("port");
            
            NetworkConnector connector = PortletManager.getNetworkConnector(actionRequest, connectorName);
            if(connector != null) {
                WebManager manager = PortletManager.getWebManager(actionRequest, new AbstractName(URI.create(managerURI)));
                ConnectorType connectorType = manager.getConnectorType(connectorName);
                

                // set the connector attributes from the form post
                for (ConnectorAttribute attribute : manager.getConnectorAttributes(connectorType)) {
                    String name = attribute.getAttributeName();
                    String value = actionRequest.getParameter(name);

                    // handle booelan type special
                    if (attribute.getAttributeClass().equals(Boolean.class)) {
                        // browser sends value of checked checkbox as "on" or "checked"
                        if ("on".equalsIgnoreCase(value) || "checked".equalsIgnoreCase(value)) {
                            value=Boolean.toString(true);
                        } else {
                            value=Boolean.toString(false);
                        }
                    }
                    // set the string form of the attribute's value as submitted by the browser
                    if (value == null || value.trim().length()<1) {
                        // special case for KeystoreManager gbean
                        if ("trustStore".equals(attribute.getAttributeName())) {

                            connectorGBeanData.setAttribute(name, null);
                        }
                    } else {
                        // set the string value on the ConnectorAttribute so
                        // it can handle type conversion via getValue()
                        try {
                            attribute.setStringValue(value);
                            connectorGBeanData.setAttribute(name, attribute.getValue());
                        } catch (Exception e) {
                            log.error("Unable to set property " + attribute.getAttributeName(), e);
                        }
                    }
                }

                // set the keystore properties if its a secure connector
                setKeystoreProperties(actionRequest, connectorName);
                
                if (actionRequest.getServerPort() != toBeUpdatedConnectorPort.intValue()) {
                    try {
                        Kernel kernel = PortletManager.getKernel();
                        BundleContext bundleContext = kernel.getBundleFor(connectorName).getBundleContext();
                        kernel.stopGBean(connectorName);
                        kernel.unloadGBean(connectorName);
                        kernel.loadGBean(connectorGBeanData, bundleContext);
                        kernel.startGBean(connectorName);
                    } catch (Exception e) {
                        log.error("Unable to reload updated connector:" + connectorName.toURI(), e);
                        actionResponse.setRenderParameter("toBeUpdatedConnectorName", toBeUpdatedConnectorName);
                        addErrorMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.errorMsg25", toBeUpdatedConnectorName));
                    }

                    try {
                        manager.updateConnectorConfig(connectorName);
                    } catch (Exception e) {
                        log.error("Unable to update connector in server.xml", e);
                        addErrorMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.errorMsg26"));
                    }
                } else {
                    actionResponse.setRenderParameter("toBeUpdatedConnectorName", toBeUpdatedConnectorName);
                    addWarningMessage(actionRequest, getLocalizedString(actionRequest, "consolebase.warnMsg09", toBeUpdatedConnectorName));
                }
            }
            actionResponse.setRenderParameter(PARM_MODE, "list");
        } else if(mode.equals("start")) {
            String connectorURI = actionRequest.getParameter(PARM_CONNECTOR_URI);
            // work with the current connector to start it.
            NetworkConnector connector = PortletManager.getNetworkConnector(actionRequest, new AbstractName(URI.create(connectorURI)));
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
            actionResponse.setRenderParameter(PARM_CONNECTOR_URI, connectorURI);
            actionResponse.setRenderParameter(PARM_MODE, "list");
        } else if(mode.equals("stop")) {
            String connectorURI = actionRequest.getParameter(PARM_CONNECTOR_URI);
            // work with the current connector to stop it.
            NetworkConnector connector = PortletManager.getNetworkConnector(actionRequest, new AbstractName(URI.create(connectorURI)));
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
            actionResponse.setRenderParameter(PARM_CONNECTOR_URI, connectorURI);
            actionResponse.setRenderParameter(PARM_MODE, "list");
        } else if(mode.equals("restart")) {
            String connectorURI = actionRequest.getParameter(PARM_CONNECTOR_URI);
            // work with the current connector to restart it.
            NetworkConnector connector = PortletManager.getNetworkConnector(actionRequest, new AbstractName(URI.create(connectorURI)));
            if(connector != null) {
                try {
                    ((GeronimoManagedBean)connector).stop();
                    ((GeronimoManagedBean)connector).start();
                } catch (Exception e) {
                    log.error("Unable to restart connector", e); //todo: get into rendered page somehow?
                }
            } else {
                log.error("Incorrect connector reference"); //Replace this with correct error processing
            }
            actionResponse.setRenderParameter(PARM_CONNECTOR_URI, connectorURI);
            actionResponse.setRenderParameter(PARM_MODE, "list");
        } else if(mode.equals("edit")) {
            String connectorURI = actionRequest.getParameter(PARM_CONNECTOR_URI);
            actionResponse.setRenderParameter(PARM_CONNECTOR_URI, connectorURI);
            actionResponse.setRenderParameter(PARM_MODE, "edit");

        } else if(mode.equals("delete")) { // User chose to delete a connector
            String connectorURI = actionRequest.getParameter(PARM_CONNECTOR_URI);
            PortletManager.getWebManager(actionRequest, new AbstractName(URI.create(managerURI))).removeConnector(new AbstractName(URI.create(connectorURI)));
            actionResponse.setRenderParameter(PARM_MODE, "list");
        }
    }

    protected void doView(RenderRequest renderRequest,
                          RenderResponse renderResponse) throws IOException, PortletException {
        if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
            return;
        }
        String mode = renderRequest.getParameter(PARM_MODE);
        if(mode == null || mode.equals("")) {
            mode = "list";
        }

        if(mode.equals("list")) {
            doList(renderRequest, renderResponse);
        } else {
            String managerURI = renderRequest.getParameter(PARM_MANAGER_URI);
            String containerURI = renderRequest.getParameter(PARM_CONTAINER_URI);
            if(managerURI != null) renderRequest.setAttribute(PARM_MANAGER_URI, managerURI);
            if(containerURI != null) renderRequest.setAttribute(PARM_CONTAINER_URI, containerURI);

            WebContainer container = PortletManager.getWebContainer(renderRequest, new AbstractName(URI.create(containerURI)));
            String server = getWebServerType(container.getClass());
            renderRequest.setAttribute(PARM_SERVER, server);

            if(mode.equals("new")) {
                String connectorType = renderRequest.getParameter(PARM_CONNECTOR_TYPE);
                WebManager webManager = PortletManager.getWebManager(renderRequest, new AbstractName(URI.create(managerURI)));
                ConnectorType type = new ConnectorType(connectorType);
                List<ConnectorAttribute> connectorAttributes = webManager.getConnectorAttributes(type);
                sortConnectorAttributes(connectorAttributes);
                renderRequest.setAttribute(PARM_CONNECTOR_ATTRIBUTES, connectorAttributes);
                renderRequest.setAttribute(PARM_CONNECTOR_TYPE, connectorType);
                renderRequest.setAttribute(PARM_MODE, "add");
                populateEnumAttributes(renderRequest);
                editConnectorView.include(renderRequest, renderResponse);
            } else if(mode.equals("edit")) {
                String connectorURI = renderRequest.getParameter(PARM_CONNECTOR_URI);
                NetworkConnector connector = PortletManager.getNetworkConnector(renderRequest, new AbstractName(URI.create(connectorURI)));
                if(connector == null) {
                    doList(renderRequest, renderResponse);
                } else {
                    AbstractName connectorName = new AbstractName(URI.create(connectorURI));
                    String uniqueName = connectorName.getName().get("name").toString();
                    renderRequest.setAttribute(PARM_DISPLAY_NAME, uniqueName);
                    WebManager webManager = PortletManager.getWebManager(renderRequest, new AbstractName(URI.create(managerURI)));
                    ConnectorType connectorType = webManager.getConnectorType(connectorName);
                    List<ConnectorAttribute> connectorAttributes = webManager.getConnectorAttributes(connectorType);
                    sortConnectorAttributes(connectorAttributes);

                    // populate the connector attributes from the connector
                    for (ConnectorAttribute attribute : connectorAttributes) {
                        try {
                            Object value = getProperty(connector, attribute.getAttributeName());
                            attribute.setValue(value);
                        } catch (IllegalArgumentException e) {
                            log.error("Unable to retrieve value of property " + attribute.getAttributeName(), e);
                        }
                    }

                    renderRequest.setAttribute(PARM_CONNECTOR_ATTRIBUTES, connectorAttributes);
                    renderRequest.setAttribute(PARM_CONNECTOR_URI, connectorURI);
                    // populate any enum type values.  the browser will render them in a
                    // <SELECT> input for the attribute
                    populateEnumAttributes(renderRequest);

                    renderRequest.setAttribute(PARM_MODE, "save");
                    editConnectorView.include(renderRequest, renderResponse);
                }
            }
        }

    }

    // sorts connector attributes alphabetically, required attributes listed first
    private void sortConnectorAttributes(List<ConnectorAttribute> connectorAttributes) {
        Collections.sort(connectorAttributes, new Comparator<ConnectorAttribute>() {
            public int compare(ConnectorAttribute o1, ConnectorAttribute o2) {
                if (o1.isRequired()) {
                    if (o2.isRequired()) {
                        return o1.getAttributeName().compareTo(o2.getAttributeName());
                    }
                    return -1;
                }
                if (o2.isRequired()) {
                    return 1;
                }
                return o1.getAttributeName().compareTo(o2.getAttributeName());
            }
        });
    }

    private void doList(RenderRequest renderRequest, RenderResponse renderResponse) throws PortletException, IOException {
        WebManager[] managers = PortletManager.getWebManagers(renderRequest);
        List<ContainerInfo> all = new ArrayList<ContainerInfo>();
        for (int i = 0; i < managers.length; i++) {
            WebManager manager = managers[i];
            AbstractName webManagerName = PortletManager.getNameFor(renderRequest, manager);

            WebContainer[] containers = (WebContainer[]) manager.getContainers();
            for (int j = 0; j < containers.length; j++) {
                List<ConnectorInfo> beans = new ArrayList<ConnectorInfo>();
                WebContainer container = containers[j];
                AbstractName containerName = PortletManager.getNameFor(renderRequest, container);
                String id;
                if(containers.length == 1) {
                    id = manager.getProductName();
                } else {
                    id = manager.getProductName() + " (" + containerName.getName().get(NameFactory.J2EE_NAME) + ")";
                }
                ContainerInfo result = new ContainerInfo(id, webManagerName.toString(), containerName.toString());

                for (NetworkConnector connector : manager.getConnectorsForContainer(container)) {
                    ConnectorInfo info = new ConnectorInfo();
                    AbstractName connectorName = PortletManager.getNameFor(renderRequest, connector);
                    info.setConnectorURI(connectorName.toString());
                    info.setDescription(PortletManager.getGBeanDescription(renderRequest, connectorName));
                    info.setUniqueName((String)connectorName.getName().get(NameFactory.J2EE_NAME));
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
                result.setConnectorTypes(manager.getConnectorTypes());
                all.add(result);
            }
        }
        if (0 == all.size()) {
            addWarningMessage(renderRequest, "warnMsg08");
        }
        renderRequest.setAttribute("containers", all);
        renderRequest.setAttribute("serverPort", Integer.valueOf(renderRequest.getServerPort()));

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
        private List connectorTypes;
        private List connectors;

        public ContainerInfo(String name, String managerURI, String containerURI) {
            this.name = name;
            this.managerURI = managerURI;
            this.containerURI = containerURI;
        }

        public String getName() {
            return name;
        }

        public List getConnectorTypes() {
            return connectorTypes;
        }

        public void setConnectorTypes(List connectorTypes) {
            this.connectorTypes = connectorTypes;
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
        editConnectorView = pc.getRequestDispatcher("/WEB-INF/view/webmanager/connector/editConnector.jsp");
    }

    public void destroy() {
        normalView = null;
        maximizedView = null;
        helpView = null;
        editConnectorView = null;
        super.destroy();
    }

    public static boolean isValid(String s) {
        return s != null && !s.equals("");
    }

    // stash any 'enum' type values for attributes.  right now this is
    // hardcoded, need to promote these to the ConnectorAttribute apis
    private void populateEnumAttributes(PortletRequest request) {
        HashMap<String,String[]> enumValues = new HashMap<String,String[]>();

        // provide the two possible values for secure protocol - TLS and SSL
        enumValues.put("secureProtocol", new String[] { "TLS", "SSL" }); //jetty
        enumValues.put("sslProtocol", new String[] { "TLS", "SSL" }); //tomcat

        // keystore and truststore types for tomcat
        enumValues.put("keystoreType", KeystoreUtil.keystoreTypes.toArray(new String[0]));
        enumValues.put("truststoreType", KeystoreUtil.keystoreTypes.toArray(new String[0]));

        // provide the three possible values for secure algorithm - Default, SunX509, and IbmX509
        enumValues.put("algorithm", new String[] { "Default", "SunX509", "IbmX509" });

        // provide the possible values for the keystore name
        KeystoreManager mgr = PortletManager.getCurrentServer(request).getKeystoreManager();
        KeystoreInstance[] stores = mgr.getUnlockedKeyStores();
        String[] storeNames = new String[stores.length];
        for (int i = 0; i < storeNames.length; i++) {
            storeNames[i] = stores[i].getKeystoreName();
        }
        enumValues.put("keyStore", storeNames);

        // provide the possible values for the trust store name
        KeystoreInstance[] trusts = mgr.getUnlockedTrustStores();
        String[] trustNames = new String[trusts.length];
        for (int i = 0; i < trustNames.length; i++) {
            trustNames[i] = trusts[i].getKeystoreName();
        }
        enumValues.put("trustStore", trustNames);

        request.setAttribute("geronimoConsoleEnumValues", enumValues);
    }

    // get the special keystore properties from the request and set them on the connector
    // TODO: need a more generic way to handle this
    private void setKeystoreProperties(PortletRequest request, AbstractName connectorName) throws PortletException {
        String containerURI = request.getParameter(PARM_CONTAINER_URI);
        WebContainer container = PortletManager.getWebContainer(request, new AbstractName(URI.create(containerURI)));
        String server = getWebServerType(container.getClass());
        NetworkConnector connector = PortletManager.getNetworkConnector(request, connectorName);

        // return if not a secure connector
        if (!(connector instanceof SecureConnector)) {
            return;
        }

        // right now only jetty supports the KeystoreManager
        if (server.equals(WEB_SERVER_JETTY)) {
            String keyStore = request.getParameter("keyStore");

            // get the unlocked keystore object from the keystore managaer
            // gbean and set its keyalias directly on the connector
            try {
                KeystoreInstance[] keystores = PortletManager.getCurrentServer(request)
                        .getKeystoreManager().getKeystores();

                String[] keys = null;
                for (int i = 0; i < keystores.length; i++) {
                    KeystoreInstance keystore = keystores[i];
                    if (keystore.getKeystoreName().equals(keyStore)) {
                        keys = keystore.getUnlockedKeys(null);
                    }
                }
                if (keys != null && keys.length == 1) {
                    setProperty(connector, "keyAlias", keys[0]);
                } else {
                    throw new PortletException("Cannot handle keystores with anything but 1 unlocked private key");
                }
            } catch (KeystoreException e) {
                throw new PortletException(e);
            }
        }
    }
}
