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
package org.apache.geronimo.tomcat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.net.ssl.KeyManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.EditableConfigurationManager;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.proxy.ProxyManager;
import org.apache.geronimo.management.geronimo.NetworkConnector;
import org.apache.geronimo.management.geronimo.WebAccessLog;
import org.apache.geronimo.management.geronimo.WebConnector;
import org.apache.geronimo.management.geronimo.WebContainer;
import org.apache.geronimo.management.geronimo.WebManager;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.tomcat.connector.AJP13ConnectorGBean;
import org.apache.geronimo.tomcat.connector.ConnectorGBean;
import org.apache.geronimo.tomcat.connector.Http11APRConnectorGBean;
import org.apache.geronimo.tomcat.connector.Http11ConnectorGBean;
import org.apache.geronimo.tomcat.connector.Http11NIOConnectorGBean;
import org.apache.geronimo.tomcat.connector.Https11APRConnectorGBean;
import org.apache.geronimo.tomcat.connector.Https11ConnectorGBean;
import org.apache.geronimo.tomcat.connector.Https11NIOConnectorGBean;
import org.apache.geronimo.tomcat.connector.TomcatWebConnector;

/**
 * Tomcat implementation of the WebManager management API.  Knows how to
 * manipulate other Tomcat objects for management purposes.
 *
 * @version $Rev$ $Date$
 */
public class TomcatManagerImpl implements WebManager {
    private final static Log log = LogFactory.getLog(TomcatManagerImpl.class);
    private final Kernel kernel;
    
    private static final ConnectorType HTTP_APR = new ConnectorType("Tomcat APR HTTP Connector");
    private static final ConnectorType HTTPS_APR = new ConnectorType("Tomcat APR HTTPS Connector");
    private static final ConnectorType HTTP_NIO = new ConnectorType("Tomcat NIO HTTP Connector");
    private static final ConnectorType HTTPS_NIO = new ConnectorType("Tomcat NIO HTTPS Connector");
    private static final ConnectorType HTTP_BIO = new ConnectorType("Tomcat BIO HTTP Connector");
    private static final ConnectorType HTTPS_BIO = new ConnectorType("Tomcat BIO HTTPS Connector");
    private static final ConnectorType AJP = new ConnectorType("Tomcat AJP Connector");
    private static List<ConnectorType> CONNECTOR_TYPES = Arrays.asList(
            HTTP_BIO,
            HTTPS_BIO,
            HTTP_NIO,
            HTTPS_NIO,
            HTTP_APR,
            HTTPS_APR,
            AJP
    );
    
    private static Map<ConnectorType, List<ConnectorAttribute>> CONNECTOR_ATTRIBUTES = new HashMap<ConnectorType, List<ConnectorAttribute>>();

    static {
        List<ConnectorAttribute> connectorAttributes = new ArrayList<ConnectorAttribute>();
        //HTTP Attributes
        connectorAttributes.add(new ConnectorAttribute<String>("host", "0.0.0.0", "Host", String.class, true));
        connectorAttributes.add(new ConnectorAttribute<Integer>("port", 8080, "Port", Integer.class, true));
        connectorAttributes.add(new ConnectorAttribute<Integer>("maxThreads", 40, "Maximum number of threads", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("minSpareThreads", 10, "Minimum spare threads", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("maxSpareThreads", 100, "Maximum spare threads", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("bufferSize", 2048, "Buffer size", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("acceptCount", 10, "acceptCount", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("connectionLinger", -1, "connectionLinger", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("tcpNoDelay", true, "tcpNoDelay", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<String>("compressableMimeType", "text/html,text/xml,text/plain", "compressableMimeType", String.class));
        connectorAttributes.add(new ConnectorAttribute<String>("compression", "off", "compression", String.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("connectionTimeout", 60000, "Connection timeout in milliseconds", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("keepAliveTimeout", 60000, "Keep alive timeout in milliseconds", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("disableUploadTimeout", true, "disableUploadTimeout", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("maxHttpHeaderSize", 4096, "Maximum HTTP header size in bytes", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("maxKeepAliveRequests", 100, "Maximum keep alive requests", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<String>("noCompressionUserAgents", "", "Comma separated list of regular expressions matching user-agents for which compression should not be used", String.class));
        connectorAttributes.add(new ConnectorAttribute<String>("restrictedUserAgents", "", "Comma separated list of regular expressions matching user-agents for which which HTTP/1.1 or HTTP/1.0 keep alive should not be used", String.class));
        connectorAttributes.add(new ConnectorAttribute<String>("restrictedUserAgents", "", "Comma separated list of regular expressions matching user-agents for which which HTTP/1.1 or HTTP/1.0 keep alive should not be used", String.class));
        connectorAttributes.add(new ConnectorAttribute<String>("server", "", "The Server header for the http response.", String.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("socketBuffer", 9000, "The size (in bytes) of the buffer to be provided for socket output buffering", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("threadPriority", Thread.NORM_PRIORITY, "The priority of the request processing threads within the JVM", Integer.class));
        //Common attributes
        connectorAttributes.add(new ConnectorAttribute<Boolean>("allowTrace", false, "Used to enable or disable the TRACE HTTP method.", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("emptySessionPath", false, "emptySessionPath", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("enableLookups", true, "enableLookups", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("maxPostSize", 2097152, "maxPostSize", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("maxSavePostSize", 4096, "maxSavePostSize", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<String>("proxyName", "", "proxyName", String.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("proxyPort", 0, "proxyPort", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("redirectPort", 8443, "redirectPort", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<String>("uriEncoding", "ISO-8859-1", "uriEncoding", String.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("useBodyEncodingForURI", false, "useBodyEncodingForURI", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("useIPVHosts", false, "useIPVHosts", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("xpoweredBy", false, "xpoweredBy", Boolean.class));

        CONNECTOR_ATTRIBUTES.put(HTTP_BIO, connectorAttributes);
        
        connectorAttributes = new ArrayList<ConnectorAttribute>();
        
        //HTTP Attributes
        connectorAttributes.add(new ConnectorAttribute<String>("host", "0.0.0.0", "Host", String.class, true));
        connectorAttributes.add(new ConnectorAttribute<Integer>("port", 8443, "Port", Integer.class, true));
        connectorAttributes.add(new ConnectorAttribute<String>("algorithm", KeyManagerFactory.getDefaultAlgorithm(), "Algorithm", String.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("clientAuth", false, "clientAuth", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<String>("keystoreFile", "", "keystoreFile", String.class));
        connectorAttributes.add(new ConnectorAttribute<String>("keystorePass", "", "keystorePass", String.class));
        connectorAttributes.add(new ConnectorAttribute<String>("keystoreType", "JKS", "keystoreType", String.class));
        connectorAttributes.add(new ConnectorAttribute<String>("sslProtocol", "TLS", "sslProtocol", String.class));
        connectorAttributes.add(new ConnectorAttribute<String>("ciphers", "", "Ciphers", String.class));
        connectorAttributes.add(new ConnectorAttribute<String>("keyAlias", "", "keyAlias", String.class));
        connectorAttributes.add(new ConnectorAttribute<String>("truststoreFile", "", "truststoreFile", String.class));
        connectorAttributes.add(new ConnectorAttribute<String>("truststorePass", "", "truststorePass", String.class));
        connectorAttributes.add(new ConnectorAttribute<String>("truststoreType", "", "truststoreType", String.class));

        connectorAttributes.add(new ConnectorAttribute<Integer>("maxThreads", 40, "Maximum number of threads", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("minSpareThreads", 10, "Minimum spare threads", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("maxSpareThreads", 100, "Maximum spare threads", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("bufferSize", 2048, "Buffer size", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("acceptCount", 10, "acceptCount", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("connectionLinger", -1, "connectionLinger", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("tcpNoDelay", true, "tcpNoDelay", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<String>("compressableMimeType", "text/html,text/xml,text/plain", "compressableMimeType", String.class));
        connectorAttributes.add(new ConnectorAttribute<String>("compression", "off", "compression", String.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("connectionTimeout", 60000, "Connection timeout in milliseconds", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("keepAliveTimeout", 60000, "Keep alive timeout in milliseconds", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("disableUploadTimeout", true, "disableUploadTimeout", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("maxHttpHeaderSize", 4096, "Maximum HTTP header size in bytes", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("maxKeepAliveRequests", 100, "Maximum keep alive requests", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<String>("noCompressionUserAgents", "", "Comma separated list of regular expressions matching user-agents for which compression should not be used", String.class));
        connectorAttributes.add(new ConnectorAttribute<String>("restrictedUserAgents", "", "Comma separated list of regular expressions matching user-agents for which which HTTP/1.1 or HTTP/1.0 keep alive should not be used", String.class));
        connectorAttributes.add(new ConnectorAttribute<String>("restrictedUserAgents", "", "Comma separated list of regular expressions matching user-agents for which which HTTP/1.1 or HTTP/1.0 keep alive should not be used", String.class));
        connectorAttributes.add(new ConnectorAttribute<String>("server", "", "The Server header for the http response.", String.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("socketBuffer", 9000, "The size (in bytes) of the buffer to be provided for socket output buffering", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("threadPriority", Thread.NORM_PRIORITY, "The priority of the request processing threads within the JVM", Integer.class));
        //Common attributes
        connectorAttributes.add(new ConnectorAttribute<Boolean>("allowTrace", false, "Used to enable or disable the TRACE HTTP method.", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("emptySessionPath", false, "emptySessionPath", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("enableLookups", true, "enableLookups", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("maxPostSize", 2097152, "maxPostSize", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("maxSavePostSize", 4096, "maxSavePostSize", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<String>("proxyName", "", "proxyName", String.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("proxyPort", 0, "proxyPort", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("redirectPort", 8443, "redirectPort", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<String>("uriEncoding", "ISO-8859-1", "uriEncoding", String.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("useBodyEncodingForURI", false, "useBodyEncodingForURI", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("useIPVHosts", false, "useIPVHosts", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("xpoweredBy", false, "xpoweredBy", Boolean.class));

        CONNECTOR_ATTRIBUTES.put(HTTPS_BIO, connectorAttributes);
        
        connectorAttributes = new ArrayList<ConnectorAttribute>();
        CONNECTOR_ATTRIBUTES.put(HTTP_NIO, connectorAttributes);
        
        connectorAttributes = new ArrayList<ConnectorAttribute>();
        CONNECTOR_ATTRIBUTES.put(HTTPS_NIO, connectorAttributes);
        
        connectorAttributes = new ArrayList<ConnectorAttribute>();
        CONNECTOR_ATTRIBUTES.put(HTTP_APR, connectorAttributes);
        
        connectorAttributes = new ArrayList<ConnectorAttribute>();
        CONNECTOR_ATTRIBUTES.put(HTTPS_APR, connectorAttributes);
        
        connectorAttributes = new ArrayList<ConnectorAttribute>();
        CONNECTOR_ATTRIBUTES.put(AJP, connectorAttributes);
    }
    
    private static Map<ConnectorType, GBeanInfo> CONNECTOR_GBEAN_INFOS = new HashMap<ConnectorType, GBeanInfo>();

    static {
        CONNECTOR_GBEAN_INFOS.put(HTTP_BIO, Http11ConnectorGBean.GBEAN_INFO);
        CONNECTOR_GBEAN_INFOS.put(HTTPS_BIO, Https11ConnectorGBean.GBEAN_INFO);
        CONNECTOR_GBEAN_INFOS.put(HTTP_NIO, Http11NIOConnectorGBean.GBEAN_INFO);
        CONNECTOR_GBEAN_INFOS.put(HTTPS_NIO, Https11NIOConnectorGBean.GBEAN_INFO);
        CONNECTOR_GBEAN_INFOS.put(HTTP_APR, Http11APRConnectorGBean.GBEAN_INFO);
        CONNECTOR_GBEAN_INFOS.put(HTTPS_APR, Https11APRConnectorGBean.GBEAN_INFO);
        CONNECTOR_GBEAN_INFOS.put(AJP, AJP13ConnectorGBean.GBEAN_INFO);
    }
    
    public TomcatManagerImpl(Kernel kernel) {
        this.kernel = kernel;
    }

    public String getProductName() {
        return "Tomcat";
    }

    /**
     * Creates and returns a new connector.  Note that the connector may well
     * require further customization before being fully functional (e.g. SSL
     * settings for a secure connector).  This may need to be done before
     * starting the resulting connector.
     *
     * @param container    The container to add the connector to
     * @param uniqueName   A name fragment that's unique to this connector
     * @param protocol     The protocol that the connector should use
     * @param host         The host name or IP that the connector should listen on
     * @param port         The port that the connector should listen on
     */
    public WebConnector addConnector(WebContainer container, String uniqueName, String protocol, String host, int port) {
        AbstractName containerName = kernel.getAbstractNameFor(container);
        AbstractName name = kernel.getNaming().createSiblingName(containerName, uniqueName, NameFactory.GERONIMO_SERVICE);
        //Get the server info
        AbstractNameQuery query = new AbstractNameQuery(ServerInfo.class.getName());
        Set set = kernel.listGBeans(query);
        AbstractName serverInfo = (AbstractName)set.iterator().next();
        GBeanData connector;
        if(protocol.equals(PROTOCOL_HTTP)) {
            connector = new GBeanData(name, Http11ConnectorGBean.GBEAN_INFO);
            connector.setReferencePattern("ServerInfo", serverInfo);
        } else if(protocol.equals(PROTOCOL_HTTPS)) {
            connector = new GBeanData(name, Https11ConnectorGBean.GBEAN_INFO);
            connector.setReferencePattern("ServerInfo", serverInfo);
            //todo: default HTTPS settings
        } else if(protocol.equals(PROTOCOL_AJP)) {
            connector = new GBeanData(name, AJP13ConnectorGBean.GBEAN_INFO);
            connector.setReferencePattern("ServerInfo", serverInfo);
        } else {
            throw new IllegalArgumentException("Invalid protocol '"+protocol+"'");
        }
//        connector.setAttribute("protocol", protocol);
        connector.setAttribute("host", host);
        connector.setAttribute("port", new Integer(port));
        connector.setAttribute("maxThreads", new Integer(50));
        connector.setAttribute("acceptCount", new Integer(100));
        connector.setReferencePattern(ConnectorGBean.CONNECTOR_CONTAINER_REFERENCE, containerName);
        connector.setAttribute("name", uniqueName);
        EditableConfigurationManager mgr = ConfigurationUtil.getEditableConfigurationManager(kernel);
        if(mgr != null) {
            try {
                mgr.addGBeanToConfiguration(containerName.getArtifact(), connector, false);
                return (WebConnector) kernel.getProxyManager().createProxy(name, TomcatWebConnector.class.getClassLoader());
            } catch (InvalidConfigException e) {
                log.error("Unable to add GBean", e);
                return null;
            } finally {
                ConfigurationUtil.releaseConfigurationManager(kernel, mgr);
            }
        } else {
            log.warn("The ConfigurationManager in the kernel does not allow editing");
            return null;
        }
    }

    /**
     * Gets the network containers.
     */
    public Object[] getContainers() {
        ProxyManager proxyManager = kernel.getProxyManager();
        AbstractNameQuery query = new AbstractNameQuery(TomcatWebContainer.class.getName());
        Set names = kernel.listGBeans(query);
        TomcatWebContainer[] results = new TomcatWebContainer[names.size()];
        int i=0;
        for (Iterator it = names.iterator(); it.hasNext(); i++) {
            AbstractName name = (AbstractName) it.next();
            results[i] = (TomcatWebContainer) proxyManager.createProxy(name, TomcatWebContainer.class.getClassLoader());
        }
        return results;
    }

    /**
     * Gets the protocols which this container can configure connectors for.
     */
    public String[] getSupportedProtocols() {
        return new String[]{PROTOCOL_HTTP, PROTOCOL_HTTPS, PROTOCOL_AJP};
    }

    /**
     * Removes a connector.  This shuts it down if necessary, and removes it from the server environment.  It must be a
     * connector that uses this network technology.
     * @param connectorName
     */
    public void removeConnector(AbstractName connectorName) {
        try {
            GBeanInfo info = kernel.getGBeanInfo(connectorName);
            boolean found = false;
            Set intfs = info.getInterfaces();
            for (Iterator it = intfs.iterator(); it.hasNext();) {
                String intf = (String) it.next();
                if (intf.equals(TomcatWebConnector.class.getName())) {
                    found = true;
                }
            }
            if (!found) {
                throw new GBeanNotFoundException(connectorName);
            }
            EditableConfigurationManager mgr = ConfigurationUtil.getEditableConfigurationManager(kernel);
            if(mgr != null) {
                try {
                    mgr.removeGBeanFromConfiguration(connectorName.getArtifact(), connectorName);
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
            log.error(e);
        }
    }

    /**
     * Gets the ObjectNames of any existing connectors for this network technology for the specified protocol.
     *
     * @param protocol A protocol as returned by getSupportedProtocols
     */
    public NetworkConnector[] getConnectors(String protocol) {
        if(protocol == null) {
            return getConnectors();
        }
        List result = new ArrayList();
        ProxyManager proxyManager = kernel.getProxyManager();
        AbstractNameQuery query = new AbstractNameQuery(TomcatWebConnector.class.getName());
        Set names = kernel.listGBeans(query);
        for (Iterator it = names.iterator(); it.hasNext();) {
            AbstractName name = (AbstractName) it.next();
            try {
                if (kernel.getAttribute(name, "protocol").equals(protocol)) {
                    result.add(proxyManager.createProxy(name, TomcatWebConnector.class.getClassLoader()));
                }
            } catch (Exception e) {
                log.error("Unable to check the protocol for a connector", e);
            }
        }
        return (TomcatWebConnector[]) result.toArray(new TomcatWebConnector[names.size()]);
    }

    public WebAccessLog getAccessLog(WebContainer container) {
        AbstractNameQuery query = new AbstractNameQuery(TomcatLogManager.class.getName());
        Set names = kernel.listGBeans(query);
        if(names.size() == 0) {
            return null;
        } else if(names.size() > 1) {
            throw new IllegalStateException("Should not be more than one Tomcat access log manager");
        }
        return (WebAccessLog) kernel.getProxyManager().createProxy((AbstractName)names.iterator().next(), TomcatLogManager.class.getClassLoader());
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
        AbstractName name = kernel.getNaming().createSiblingName(containerName, uniqueName, NameFactory.GERONIMO_SERVICE);
        GBeanData gbeanData = new GBeanData(name, gbeanInfo);
        gbeanData.setReferencePattern(ConnectorGBean.CONNECTOR_CONTAINER_REFERENCE, containerName);
        for (ConnectorAttribute connectorAttribute : connectorAttributes) {
            gbeanData.setAttribute(connectorAttribute.getAttributeName(), connectorAttribute.getStringValue());
        }
        EditableConfigurationManager mgr = ConfigurationUtil.getEditableConfigurationManager(kernel);
        if (mgr != null) {
            try {
                mgr.addGBeanToConfiguration(containerName.getArtifact(), gbeanData, false);
            } catch (InvalidConfigException e) {
                log.error("Unable to add GBean", e);
                return null;
            } finally {
                ConfigurationUtil.releaseConfigurationManager(kernel, mgr);
            }
        } else {
            log.warn("The ConfigurationManager in the kernel does not allow editing");
            return null;
        }
        return name;
    }

    /**
     * Gets the ObjectNames of any existing connectors associated with this network technology.
     */
    public NetworkConnector[] getConnectors() {
        ProxyManager proxyManager = kernel.getProxyManager();
        AbstractNameQuery query = new AbstractNameQuery(TomcatWebConnector.class.getName());
        Set names = kernel.listGBeans(query);
        TomcatWebConnector[] results = new TomcatWebConnector[names.size()];
        int i=0;
        for (Iterator it = names.iterator(); it.hasNext(); i++) {
            AbstractName name = (AbstractName) it.next();
            results[i] = (TomcatWebConnector) proxyManager.createProxy(name, TomcatWebConnector.class.getClassLoader());
        }
        return results;
    }

    /**
     * Gets the ObjectNames of any existing connectors for the specified container for the specified protocol.
     *
     * @param protocol A protocol as returned by getSupportedProtocols
     */
    public NetworkConnector[] getConnectorsForContainer(Object container, String protocol) {
        if(protocol == null) {
            return getConnectorsForContainer(container);
        }
        AbstractName containerName = kernel.getAbstractNameFor(container);
        ProxyManager mgr = kernel.getProxyManager();
        try {
            List results = new ArrayList();
            AbstractNameQuery query = new AbstractNameQuery(TomcatWebConnector.class.getName());
            Set set = kernel.listGBeans(query); // all Tomcat connectors
            for (Iterator it = set.iterator(); it.hasNext();) {
                AbstractName name = (AbstractName) it.next(); // a single Tomcat connector
                GBeanData data = kernel.getGBeanData(name);
                ReferencePatterns refs = data.getReferencePatterns(ConnectorGBean.CONNECTOR_CONTAINER_REFERENCE);
                if(containerName.equals(refs.getAbstractName())) {
                    try {
                        String testProtocol = (String) kernel.getAttribute(name, "protocol");
                        if(testProtocol != null && testProtocol.equals(protocol)) {
                            results.add(mgr.createProxy(name, TomcatWebConnector.class.getClassLoader()));
                        }
                    } catch (Exception e) {
                        log.error("Unable to look up protocol for connector '"+name+"'",e);
                    }
                    break;
                }
            }
            return (TomcatWebConnector[]) results.toArray(new TomcatWebConnector[results.size()]);
        } catch (Exception e) {
            throw (IllegalArgumentException)new IllegalArgumentException("Unable to look up connectors for Tomcat container '"+containerName +"': ").initCause(e);
        }
    }

    /**
     * Gets the ObjectNames of any existing connectors for the specified container.
     */
    public NetworkConnector[] getConnectorsForContainer(Object container) {
        AbstractName containerName = kernel.getAbstractNameFor(container);
        ProxyManager mgr = kernel.getProxyManager();
        try {
            List results = new ArrayList();
            AbstractNameQuery query = new AbstractNameQuery(TomcatWebConnector.class.getName());
            Set set = kernel.listGBeans(query); // all Tomcat connectors
            for (Iterator it = set.iterator(); it.hasNext();) {
                AbstractName name = (AbstractName) it.next(); // a single Tomcat connector
                GBeanData data = kernel.getGBeanData(name);
                ReferencePatterns refs = data.getReferencePatterns(ConnectorGBean.CONNECTOR_CONTAINER_REFERENCE);
                if (containerName.equals(refs.getAbstractName())) {
                    results.add(mgr.createProxy(name, TomcatWebConnector.class.getClassLoader()));
                }
            }
            return (TomcatWebConnector[]) results.toArray(new TomcatWebConnector[results.size()]);
        } catch (Exception e) {
            throw (IllegalArgumentException) new IllegalArgumentException("Unable to look up connectors for Tomcat container '"+containerName).initCause(e);
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("Tomcat Web Manager", TomcatManagerImpl.class);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addInterface(WebManager.class);
        infoFactory.setConstructor(new String[] {"kernel"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    public ConnectorType getConnectorType(AbstractName connectorName) {
        ConnectorType connectorType = null; 
        try {
            GBeanInfo info = kernel.getGBeanInfo(connectorName);
            boolean found = false;
            Set intfs = info.getInterfaces();
            for (Iterator it = intfs.iterator(); it.hasNext() && !found;) {
                String intf = (String) it.next();
                if (intf.equals(TomcatWebConnector.class.getName())) {
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
            log.error(e);
        }
            
        return connectorType;
    }
}
