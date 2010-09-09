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
package org.apache.geronimo.jetty8;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.jetty8.connector.AJP13Connector;
import org.apache.geronimo.jetty8.connector.HTTPBlockingConnector;
import org.apache.geronimo.jetty8.connector.HTTPSSelectChannelConnector;
import org.apache.geronimo.jetty8.connector.HTTPSSocketConnector;
import org.apache.geronimo.jetty8.connector.HTTPSelectChannelConnector;
import org.apache.geronimo.jetty8.connector.HTTPSocketConnector;
import org.apache.geronimo.jetty8.connector.JettyConnector;
import org.apache.geronimo.jetty8.requestlog.JettyLogManager;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.EditableConfigurationManager;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.proxy.ProxyManager;
import org.apache.geronimo.management.geronimo.KeystoreManager;
import org.apache.geronimo.management.geronimo.NetworkConnector;
import org.apache.geronimo.management.geronimo.WebAccessLog;
import org.apache.geronimo.management.geronimo.WebContainer;
import org.apache.geronimo.management.geronimo.WebManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jetty implementation of WebManager.  Knows how to manipulate
 * other Jetty objects for management purposes.
 *
 * @version $Rev:386276 $ $Date$
 */

@GBean
public class JettyManagerImpl implements WebManager {
    private static final Logger log = LoggerFactory.getLogger(JettyManagerImpl.class);

    private static final ConnectorType HTTP_NIO = new ConnectorType(Messages.getString("JettyManagerImpl.0")); //$NON-NLS-1$
    private static final ConnectorType HTTPS_NIO = new ConnectorType(Messages.getString("JettyManagerImpl.1")); //$NON-NLS-1$
    private static final ConnectorType HTTP_BLOCKING_NIO = new ConnectorType(Messages.getString("JettyManagerImpl.2")); //$NON-NLS-1$
    private static final ConnectorType HTTP_BIO = new ConnectorType(Messages.getString("JettyManagerImpl.3")); //$NON-NLS-1$
    private static final ConnectorType HTTPS_BIO = new ConnectorType(Messages.getString("JettyManagerImpl.4")); //$NON-NLS-1$
    private static final ConnectorType AJP_NIO = new ConnectorType(Messages.getString("JettyManagerImpl.5")); //$NON-NLS-1$
    private static List<ConnectorType> CONNECTOR_TYPES = Arrays.asList(
            HTTP_NIO,
            HTTPS_NIO,
            HTTP_BLOCKING_NIO,
            HTTP_BIO,
            HTTPS_BIO,
            AJP_NIO
    );

    private static Map<ConnectorType, List<ConnectorAttribute>> CONNECTOR_ATTRIBUTES = new HashMap<ConnectorType, List<ConnectorAttribute>>();

    static {
        List<ConnectorAttribute> connectorAttributes = new ArrayList<ConnectorAttribute>();
        addCommonConnectorAttributes(connectorAttributes);
        CONNECTOR_ATTRIBUTES.put(HTTP_NIO, connectorAttributes);

        connectorAttributes = new ArrayList<ConnectorAttribute>();
        addCommonConnectorAttributes(connectorAttributes);
        addSslConnectorAttributes(connectorAttributes);
        setAttribute(connectorAttributes, "port", 8443); // SSL port
        CONNECTOR_ATTRIBUTES.put(HTTPS_NIO, connectorAttributes);

        connectorAttributes = new ArrayList<ConnectorAttribute>();
        addCommonConnectorAttributes(connectorAttributes);
        CONNECTOR_ATTRIBUTES.put(HTTP_BIO, connectorAttributes);

        connectorAttributes = new ArrayList<ConnectorAttribute>();
        addCommonConnectorAttributes(connectorAttributes);
        addSslConnectorAttributes(connectorAttributes);
        setAttribute(connectorAttributes, "port", 8443); // SSL port
        CONNECTOR_ATTRIBUTES.put(HTTPS_BIO, connectorAttributes);

        connectorAttributes = new ArrayList<ConnectorAttribute>();
        addCommonConnectorAttributes(connectorAttributes);
        CONNECTOR_ATTRIBUTES.put(HTTP_BLOCKING_NIO, connectorAttributes);

        connectorAttributes = new ArrayList<ConnectorAttribute>();
        addCommonConnectorAttributes(connectorAttributes);
        CONNECTOR_ATTRIBUTES.put(AJP_NIO, connectorAttributes);

    }

    private static Map<ConnectorType, GBeanInfo> CONNECTOR_GBEAN_INFOS = new HashMap<ConnectorType, GBeanInfo>();

    static {
        CONNECTOR_GBEAN_INFOS.put(HTTP_NIO, HTTPSelectChannelConnector.GBEAN_INFO);
        CONNECTOR_GBEAN_INFOS.put(HTTPS_NIO, HTTPSSelectChannelConnector.GBEAN_INFO);
        CONNECTOR_GBEAN_INFOS.put(HTTP_BLOCKING_NIO, HTTPBlockingConnector.GBEAN_INFO);
        CONNECTOR_GBEAN_INFOS.put(HTTP_BIO, HTTPSocketConnector.GBEAN_INFO);
        CONNECTOR_GBEAN_INFOS.put(HTTPS_BIO, HTTPSSocketConnector.GBEAN_INFO);
        CONNECTOR_GBEAN_INFOS.put(AJP_NIO, AJP13Connector.GBEAN_INFO);
    }

    private final Kernel kernel;

    public JettyManagerImpl(@ParamSpecial(type = SpecialAttributeType.kernel)Kernel kernel) {
        this.kernel = kernel;
    }

    public String getProductName() {
        return "Jetty";
    }

    /**
     * Get a list of containers for this web implementation.
     */
    public Object[] getContainers() {
        ProxyManager proxyManager = kernel.getProxyManager();
        AbstractNameQuery query = new AbstractNameQuery(JettyContainer.class.getName());
        Set names = kernel.listGBeans(query);
        JettyContainer[] results = new JettyContainer[names.size()];
        int i = 0;
        for (Iterator it = names.iterator(); it.hasNext(); i++) {
            AbstractName name = (AbstractName) it.next();
            results[i] = (JettyContainer) proxyManager.createProxy(name, JettyContainer.class.getClassLoader());
        }
        return results;
    }

    /**
     * Gets the protocols that this web container supports (that you can create
     * connectors for).
     */
    public String[] getSupportedProtocols() {
        return new String[]{PROTOCOL_HTTP, PROTOCOL_HTTPS, PROTOCOL_AJP};
    }

    /**
     * Removes a connector.  This shuts it down if necessary, and removes it
     * from the server environment.  It must be a connector that this container
     * is responsible for.
     *
     * @param connectorName name of jetty connector to remove
     */
    public void removeConnector(AbstractName connectorName) {
        try {
            GBeanInfo info = kernel.getGBeanInfo(connectorName);
            boolean found = false;
            Set<String> intfs = info.getInterfaces();
            for (String intf : intfs) {
                if (intf.equals(JettyWebConnector.class.getName())) {
                    found = true;
                }
            }
            if (!found) {
                throw new GBeanNotFoundException(connectorName);
            }
            ConfigurationManager mgr = ConfigurationUtil.getConfigurationManager(kernel);
            if (mgr != null && mgr instanceof EditableConfigurationManager) {
                try {
                   ((EditableConfigurationManager)mgr).removeGBeanFromConfiguration(connectorName.getArtifact(), connectorName);
                } catch (InvalidConfigException e) {
                    log.error("Unable to add GBean", e);
                } finally {
                    ConfigurationUtil.releaseConfigurationManager(kernel, mgr);
                }
            } else {
                log.warn("The ConfigurationManager in the kernel does not allow editing");
            }
        } catch (GBeanNotFoundException e) {
            log.warn("No such GBean '" + connectorName + "'"); //todo: what if we want to remove a failed GBean?
        } catch (Exception e) {
            log.error("Failed to remove connector", e);
        }
    }

    /**
     * Gets the ObjectNames of any existing connectors for the specified
     * protocol.
     *
     * @param protocol A protocol as returned by getSupportedProtocols
     */
    public NetworkConnector[] getConnectors(String protocol) {
        if (protocol == null) {
            return getConnectors();
        }
        List<JettyWebConnector> result = new ArrayList<JettyWebConnector>();
        ProxyManager proxyManager = kernel.getProxyManager();
        AbstractNameQuery query = new AbstractNameQuery(JettyWebConnector.class.getName());
        Set<AbstractName> names = kernel.listGBeans(query);
        for (AbstractName name : names) {
            try {
                if (kernel.getAttribute(name, "protocol").equals(protocol)) {
                    result.add((JettyWebConnector) proxyManager.createProxy(name, JettyWebConnector.class.getClassLoader()));
                }
            } catch (Exception e) {
                log.error("Unable to check the protocol for a connector", e);
            }
        }
        return result.toArray(new JettyWebConnector[names.size()]);
    }

    public WebAccessLog getAccessLog(WebContainer container) {
        AbstractNameQuery query = new AbstractNameQuery(JettyLogManager.class.getName());
        Set names = kernel.listGBeans(query);
        if (names.size() == 0) {
            return null;
        } else if (names.size() > 1) {
            throw new IllegalStateException("Should not be more than one Jetty access log manager");
        }
        return (WebAccessLog) kernel.getProxyManager().createProxy((AbstractName) names.iterator().next(), JettyLogManager.class.getClassLoader());
    }

    public List<ConnectorType> getConnectorTypes() {
        return CONNECTOR_TYPES;
    }

    public List<ConnectorAttribute> getConnectorAttributes(ConnectorType connectorType) {
        return ConnectorAttribute.copy(CONNECTOR_ATTRIBUTES.get(connectorType));
    }

    public AbstractName getConnectorConfiguration(ConnectorType connectorType, List<ConnectorAttribute> connectorAttributes, WebContainer container, String uniqueName) {
        GBeanInfo gbeanInfo = CONNECTOR_GBEAN_INFOS.get(connectorType);
        AbstractName containerName = kernel.getAbstractNameFor(container);
        AbstractName name = kernel.getNaming().createSiblingName(containerName, uniqueName, GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
        GBeanData gbeanData = new GBeanData(name, gbeanInfo);
        gbeanData.setReferencePattern(JettyConnector.CONNECTOR_CONTAINER_REFERENCE, containerName);
        for (ConnectorAttribute connectorAttribute : connectorAttributes) {
            Object value = connectorAttribute.getValue();
            if (value != null) {
                gbeanData.setAttribute(connectorAttribute.getAttributeName(), connectorAttribute.getValue());
            }
        }

        // provide a reference to KeystoreManager gbean for HTTPS connectors
        if (connectorType.equals(HTTPS_NIO) || connectorType.equals(HTTPS_BIO)) {
            AbstractNameQuery query = new AbstractNameQuery(KeystoreManager.class.getName());
            gbeanData.setReferencePattern("KeystoreManager", query);
        }

        try {
            ConfigurationManager mgr = ConfigurationUtil.getConfigurationManager(kernel);
            if (mgr != null && mgr instanceof EditableConfigurationManager) {
                ((EditableConfigurationManager)mgr).addGBeanToConfiguration(containerName.getArtifact(), gbeanData, false);
            } else {
                log.warn("The ConfigurationManager in the kernel does not allow editing");
                return null;
            }
        } catch (InvalidConfigException e) {
            log.error("Unable to add GBean", e);
            return null;
        } catch (GBeanNotFoundException e) {
            log.error("Unable to add GBean", e);
            return null;
        }
        return name;
    }

    public ConnectorType getConnectorType(AbstractName connectorName) {
        ConnectorType connectorType = null;
        try {
            GBeanInfo info = kernel.getGBeanInfo(connectorName);
            boolean found = false;
            Set intfs = info.getInterfaces();
            for (Iterator it = intfs.iterator(); it.hasNext() && !found;) {
                String intf = (String) it.next();
                if (intf.equals(JettyWebConnector.class.getName())) {
                    found = true;
                }
            }
            if (!found) {
                throw new GBeanNotFoundException(connectorName);
            }
            String searchingFor = info.getName();
            for (Entry<ConnectorType, GBeanInfo> entry : CONNECTOR_GBEAN_INFOS.entrySet() ) {
                String candidate = entry.getValue().getName();
                if (candidate.equals(searchingFor)) {
                    return entry.getKey();
                }
            }
        } catch (GBeanNotFoundException e) {
            log.warn("No such GBean '" + connectorName + "'");
        } catch (Exception e) {
            log.error("Failed to get connector type", e);
        }

        return connectorType;
    }

    /**
     * Gets the ObjectNames of any existing connectors.
     */
    public NetworkConnector[] getConnectors() {
        ProxyManager proxyManager = kernel.getProxyManager();
        AbstractNameQuery query = new AbstractNameQuery(JettyWebConnector.class.getName());
        Set names = kernel.listGBeans(query);
        JettyWebConnector[] results = new JettyWebConnector[names.size()];
        int i = 0;
        for (Iterator it = names.iterator(); it.hasNext(); i++) {
            AbstractName name = (AbstractName) it.next();
            results[i] = (JettyWebConnector) proxyManager.createProxy(name, JettyWebConnector.class.getClassLoader());
        }
        return results;
    }

    public NetworkConnector[] getConnectorsForContainer(Object container, String protocol) {
        if (protocol == null) {
            return getConnectorsForContainer(container);
        }
        AbstractName containerName = kernel.getAbstractNameFor(container);
        ProxyManager mgr = kernel.getProxyManager();
        try {
            List<JettyWebConnector> results = new ArrayList<JettyWebConnector>();
            AbstractNameQuery query = new AbstractNameQuery(JettyWebConnector.class.getName());
            Set<AbstractName> set = kernel.listGBeans(query); // all Jetty connectors
            for (AbstractName name : set) {
                GBeanData data = kernel.getGBeanData(name);
                ReferencePatterns refs = data.getReferencePatterns(JettyConnector.CONNECTOR_CONTAINER_REFERENCE);
                if (containerName.equals(refs.getAbstractName())) {
                    try {
                        String testProtocol = (String) kernel.getAttribute(name, "protocol");
                        if (testProtocol != null && testProtocol.equals(protocol)) {
                            results.add((JettyWebConnector) mgr.createProxy(name, JettyWebConnector.class.getClassLoader()));
                        }
                    } catch (Exception e) {
                        log.error("Unable to look up protocol for connector '" + name + "'", e);
                    }
                    break;
                }
            }
            return results.toArray(new JettyWebConnector[results.size()]);
        } catch (Exception e) {
            throw (IllegalArgumentException) new IllegalArgumentException("Unable to look up connectors for Jetty container '" + containerName + "': ").initCause(e);
        }
    }

    public NetworkConnector[] getConnectorsForContainer(Object container) {
        AbstractName containerName = kernel.getAbstractNameFor(container);
        ProxyManager mgr = kernel.getProxyManager();
        try {
            List<JettyWebConnector> results = new ArrayList<JettyWebConnector>();
            AbstractNameQuery query = new AbstractNameQuery(JettyWebConnector.class.getName());
            Set<AbstractName> set = kernel.listGBeans(query); // all Jetty connectors
            for (AbstractName name : set) {
                GBeanData data = kernel.getGBeanData(name);
                ReferencePatterns refs = data.getReferencePatterns(JettyConnector.CONNECTOR_CONTAINER_REFERENCE);
                if (containerName.equals(refs.getAbstractName())) {
                    results.add((JettyWebConnector) mgr.createProxy(name, JettyWebConnector.class.getClassLoader()));
                }
            }
            return results.toArray(new JettyWebConnector[results.size()]);
        } catch (Exception e) {
            throw (IllegalArgumentException) new IllegalArgumentException("Unable to look up connectors for Jetty container '" + containerName + "'").initCause(e);
        }
    }

    private static void addCommonConnectorAttributes(List<ConnectorAttribute> connectorAttributes) {
        connectorAttributes.add(new ConnectorAttribute<String>("host", "0.0.0.0", Messages.getString("JettyManagerImpl.30"), String.class, true)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        connectorAttributes.add(new ConnectorAttribute<Integer>("port", 8080, Messages.getString("JettyManagerImpl.32"), Integer.class, true)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("maxThreads", 10, Messages.getString("JettyManagerImpl.34"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("bufferSizeBytes", 8096, Messages.getString("JettyManagerImpl.36"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("headerBufferSizeBytes", 8192, Messages.getString("JettyManagerImpl.57"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("acceptQueueSize", 10, Messages.getString("JettyManagerImpl.38"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<Integer>("lingerMillis", 30000, Messages.getString("JettyManagerImpl.40"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        //connectorAttributes.add(new ConnectorAttribute<Boolean>("tcpNoDelay", false, "If true then setTcpNoDelay(true) is called on accepted sockets.", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("redirectPort", 8443, Messages.getString("JettyManagerImpl.42"), Integer.class)); //$NON-NLS-1$ //$NON-NLS-2$
        //connectorAttributes.add(new ConnectorAttribute<Integer>("maxIdleTimeMs", 30000, " The time in milliseconds that a connection can be idle before being closed.", Integer.class));
    }

    private static void addSslConnectorAttributes(List<ConnectorAttribute> connectorAttributes) {
        //connectorAttributes.add(new ConnectorAttribute<Boolean>("clientAuthRequested", false, "clientAuthRequested", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("clientAuthRequired", false, Messages.getString("JettyManagerImpl.44"), Boolean.class)); //$NON-NLS-1$ //$NON-NLS-2$
        connectorAttributes.add(new ConnectorAttribute<String>("keyStore", "", Messages.getString("JettyManagerImpl.47"), String.class, true)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        connectorAttributes.add(new ConnectorAttribute<String>("trustStore", "", Messages.getString("JettyManagerImpl.50"), String.class)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        //connectorAttributes.add(new ConnectorAttribute<String>("keyAlias", "", "keyAlias", String.class, true));
        connectorAttributes.add(new ConnectorAttribute<String>("secureProtocol", "", Messages.getString("JettyManagerImpl.53"), String.class)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        connectorAttributes.add(new ConnectorAttribute<String>("algorithm", "Default", Messages.getString("JettyManagerImpl.56"), String.class)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    private static <T> void setAttribute (List<ConnectorAttribute> connectorAttributes, String attributeName, T value) {
        for (ConnectorAttribute connectorAttribute : connectorAttributes) {
            if (connectorAttribute.getAttributeName().equals(attributeName)) {
                connectorAttribute.setValue(value);
                return;
            }
        }
    }

    public void updateConnectorConfig(AbstractName connectorName)  throws Exception {
        // do nothing for Jetty, only tomcat needs this to update server.xml file.
    }

}
