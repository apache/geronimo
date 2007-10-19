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
import java.util.Map.Entry;
import java.util.Set;

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
    
    private static final ConnectorType HTTP_BIO = new ConnectorType("Tomcat BIO HTTP Connector");
    private static final ConnectorType HTTPS_BIO = new ConnectorType("Tomcat BIO HTTPS Connector");
    private static final ConnectorType HTTP_NIO = new ConnectorType("Tomcat NIO HTTP Connector");
    private static final ConnectorType HTTPS_NIO = new ConnectorType("Tomcat NIO HTTPS Connector");
    private static final ConnectorType HTTP_APR = new ConnectorType("Tomcat APR HTTP Connector");
    private static final ConnectorType HTTPS_APR = new ConnectorType("Tomcat APR HTTPS Connector");
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
        //******************* HTTP - BIO CONNECTOR
        List<ConnectorAttribute> connectorAttributes = new ArrayList<ConnectorAttribute>();
        addCommonConnectorAttributes(connectorAttributes);
        addHttpConnectorAttributes(connectorAttributes);
        CONNECTOR_ATTRIBUTES.put(HTTP_BIO, connectorAttributes);
        
        //******************* HTTPS - BIO CONNECTOR
        connectorAttributes = new ArrayList<ConnectorAttribute>();
        addCommonConnectorAttributes(connectorAttributes);
        addHttpConnectorAttributes(connectorAttributes);
        addSslConnectorAttributes(connectorAttributes);
        setAttribute(connectorAttributes, "port", 8443); // SSL port
        CONNECTOR_ATTRIBUTES.put(HTTPS_BIO, connectorAttributes);
        
        //******************* HTTP - NIO CONNECTOR
        connectorAttributes = new ArrayList<ConnectorAttribute>();
        addCommonConnectorAttributes(connectorAttributes);
        addHttpConnectorAttributes(connectorAttributes);
        addNioConnectorAttributes(connectorAttributes);
        CONNECTOR_ATTRIBUTES.put(HTTP_NIO, connectorAttributes);
        
        //******************* HTTPS - NIO CONNECTOR
        connectorAttributes = new ArrayList<ConnectorAttribute>();
        addCommonConnectorAttributes(connectorAttributes);
        addHttpConnectorAttributes(connectorAttributes);
        addSslConnectorAttributes(connectorAttributes);
        addNioConnectorAttributes(connectorAttributes);
        setAttribute(connectorAttributes, "port", 8443); // SSL port
        CONNECTOR_ATTRIBUTES.put(HTTPS_NIO, connectorAttributes);
        
        //******************* HTTP - APR CONNECTOR
        connectorAttributes = new ArrayList<ConnectorAttribute>();        
        addCommonConnectorAttributes(connectorAttributes);
        addHttpConnectorAttributes(connectorAttributes);
        addAprConnectorAttributes(connectorAttributes);
        CONNECTOR_ATTRIBUTES.put(HTTP_APR, connectorAttributes);
        
        //******************* HTTPS - APR CONNECTOR
        connectorAttributes = new ArrayList<ConnectorAttribute>();
        addCommonConnectorAttributes(connectorAttributes);
        addHttpConnectorAttributes(connectorAttributes);
        addAprConnectorAttributes(connectorAttributes);
        //APR SSL specific values, different from BIO and NIO SSL because it uses openssl
        connectorAttributes.add(new ConnectorAttribute<String>("sslProtocol", "all", "Protocol which may be used for communicating with clients.", String.class));
        connectorAttributes.add(new ConnectorAttribute<String>("sslCipherSuite", "ALL", "Ciphers which may be used for communicating with clients.", String.class));
        connectorAttributes.add(new ConnectorAttribute<String>("sslCertificateFile", "", "Name of the file that contains the server certificate. The format is PEM-encoded.", String.class, true));
        connectorAttributes.add(new ConnectorAttribute<String>("sslCertificateKeyFile", null, "Name of the file that contains the server private key.", String.class));
        connectorAttributes.add(new ConnectorAttribute<String>("sslPassword", null, "Pass phrase for the encrypted private key.", String.class));
        connectorAttributes.add(new ConnectorAttribute<String>("sslVerifyClient", "none", "Ask client for certificate.", String.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("sslVerifyDepth", 10, "Maximum verification depth for client certificates.", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<String>("sslCACertificateFile", null, "File of concatenated PEM-encoded CA Certificates for Client Auth.", String.class));
        connectorAttributes.add(new ConnectorAttribute<String>("sslCACertificatePath", null, "Directory of PEM-encoded CA Certificates for Client Auth.", String.class));
        connectorAttributes.add(new ConnectorAttribute<String>("sslCertificateChainFile", null, "File of PEM-encoded Server CA Certificates.", String.class));
        connectorAttributes.add(new ConnectorAttribute<String>("sslCARevocationFile", null, "File of concatenated PEM-encoded CA CRLs for Client Auth.", String.class));
        connectorAttributes.add(new ConnectorAttribute<String>("sslCARevocationPath", null, "Directory of PEM-encoded CA CRLs for Client Auth.", String.class));
        setAttribute(connectorAttributes, "port", 8443); // SSL port
        CONNECTOR_ATTRIBUTES.put(HTTPS_APR, connectorAttributes);
        
        //******************* AJP CONNECTOR
        connectorAttributes = new ArrayList<ConnectorAttribute>();
        addCommonConnectorAttributes(connectorAttributes);
        //AJP Attributes, see http://tomcat.apache.org/tomcat-6.0-doc/config/ajp.html
        connectorAttributes.add(new ConnectorAttribute<String>("host", "0.0.0.0", "The host name or IP to bind to. The normal values are 0.0.0.0 (all interfaces) or localhost (local connections only)", String.class, true));
        connectorAttributes.add(new ConnectorAttribute<Integer>("port", 8009, "The network port to bind to.", Integer.class, true));
        connectorAttributes.add(new ConnectorAttribute<Integer>("backlog", 10, "The maximum queue length for incoming connection requests when all possible request processing threads are in use.", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("bufferSize", -1, "Buffer size", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("connectionTimeout", org.apache.coyote.ajp.Constants.DEFAULT_CONNECTION_TIMEOUT, "Connection timeout in milliseconds", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("keepAliveTimeout", org.apache.coyote.ajp.Constants.DEFAULT_CONNECTION_TIMEOUT, "Keep alive timeout in milliseconds", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("maxThreads", 40, "The maximum number of threads this connector should use to handle incoming requests", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("minSpareThreads", 10, "Minimum spare threads", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("maxSpareThreads", 100, "Maximum spare threads", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("tcpNoDelay", true, "If set to true, the TCP_NO_DELAY option will be set on the server socket, which improves performance under most circumstances.", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("tomcatAuthentication", true, "If set to true, the authetication will be done in Geronimo. Otherwise, the authenticated principal will be propagated from the native webaserver and used for authorization in Geronimo.", Boolean.class));
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
        gbeanData.setAttribute("name", uniqueName);
        gbeanData.setReferencePattern(ConnectorGBean.CONNECTOR_CONTAINER_REFERENCE, containerName);
        for (ConnectorAttribute connectorAttribute : connectorAttributes) {
            gbeanData.setAttribute(connectorAttribute.getAttributeName(), connectorAttribute.getValue());
        }
        AbstractNameQuery query = new AbstractNameQuery(ServerInfo.class.getName());
        Set set = kernel.listGBeans(query);
        AbstractName serverInfo = (AbstractName)set.iterator().next();
        gbeanData.setReferencePattern("ServerInfo", serverInfo);

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

    // see http://tomcat.apache.org/tomcat-6.0-doc/config/http.html    
    private static void addCommonConnectorAttributes(List<ConnectorAttribute> connectorAttributes) {
        connectorAttributes.add(new ConnectorAttribute<Boolean>("allowTrace", false, "A boolean value which can be used to enable or disable the TRACE HTTP method.", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("emptySessionPath", false, "If set to true, all paths for session cookies will be set to /. This can be useful for portlet specification implementations, but will greatly affect performance if many applications are accessed on a given server by the client.", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("enableLookups", true, "Set to true if you want calls to request.getRemoteHost() to perform DNS lookups in order to return the actual host name of the remote client. Set to false to skip the DNS lookup and return the IP address in String form instead (thereby improving performance).", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("maxPostSize", 2097152, "The maximum size in bytes of the POST which will be handled by the container FORM URL parameter parsing. The limit can be disabled by setting this attribute to a value less than or equal to 0.", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("maxSavePostSize", 4096, "The maximum size in bytes of the POST which will be saved/buffered by the container during FORM or CLIENT-CERT authentication. For both types of authentication, the POST will be saved/buffered before the user is authenticated. For CLIENT-CERT authentication, the POST is buffered for the duration of the SSL handshake and the buffer emptied when the request is processed. For FORM authentication the POST is saved whilst the user is re-directed to the login form and is retained until the user successfully authenticates or the session associated with the authentication request expires. The limit can be disabled by setting this attribute to -1. Setting the attribute to zero will disable the saving of POST data during authentication.", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<String>("proxyName", null, "If this Connector is being used in a proxy configuration, configure this attribute to specify the server name to be returned for calls to request.getServerName().", String.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("proxyPort", 0, "If this Connector is being used in a proxy configuration, configure this attribute to specify the server port to be returned for calls to request.getServerPort().", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("redirectPort", 8443, "If this Connector is supporting non-SSL requests, and a request is received for which a matching <security-constraint> requires SSL transport, Catalina will automatically redirect the request to the port number specified here.", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<String>("uriEncoding", "ISO-8859-1", "This specifies the character encoding used to decode the URI bytes, after %xx decoding the URL.", String.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("useBodyEncodingForURI", false, "This specifies if the encoding specified in contentType should be used for URI query parameters, instead of using the URIEncoding. This setting is present for compatibility with Tomcat 4.1.x, where the encoding specified in the contentType, or explicitely set using Request.setCharacterEncoding method was also used for the parameters from the URL.", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("useIPVHosts", false, "Set this attribute to true to cause Tomcat to use the IP address that the request was recieved on to determine the Host to send the request to.", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("xpoweredBy", false, "Set this attribute to true to cause Tomcat to advertise support for the Servlet specification using the header recommended in the specification.", Boolean.class));
    }
    
    // see http://tomcat.apache.org/tomcat-6.0-doc/config/http.html
    private static void addHttpConnectorAttributes(List<ConnectorAttribute> connectorAttributes) {
        connectorAttributes.add(new ConnectorAttribute<Integer>("acceptCount", 10, "The maximum queue length for incoming connection requests when all possible request processing threads are in use. Any requests received when the queue is full will be refused.", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<String>("address", "0.0.0.0", "The host name or IP to bind to. The normal values are 0.0.0.0 (all interfaces) or localhost (local connections only).", String.class, true));
        connectorAttributes.add(new ConnectorAttribute<Integer>("bufferSize", 2048, "The size (in bytes) of the buffer to be provided for input streams created by this connector.", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<String>("compressableMimeType", "text/html,text/xml,text/plain", "The value is a comma separated list of MIME types for which HTTP compression may be used.", String.class));
        connectorAttributes.add(new ConnectorAttribute<String>("compression", "off", "The Connector may use HTTP/1.1 GZIP compression in an attempt to save server bandwidth. The acceptable values for the parameter is <i>off</i> (disable compression), <i>on</i> (allow compression, which causes text data to be compressed), <i>force</i> (forces compression in all cases), or a numerical integer value (which is equivalent to <i>on</i>, but specifies the minimum amount of data before the output is compressed). If the content-length is not known and compression is set to <i>on</i> or more aggressive, the output will also be compressed.", String.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("connectionLinger", -1, "The number of milliseconds during which the sockets used by this Connector will linger when they are closed. The default value is -1 (socket linger is disabled).", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("connectionTimeout", 60000, "The number of milliseconds this Connector will wait, after accepting a connection, for the request URI line to be presented.", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<String>("executor", null, "A reference to the name in an Executor element. If this attribute is enabled, and the named executor exists, the connector will use the executor, and all the other thread attributes will be ignored.", String.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("keepAliveTimeout", 60000, "The number of milliseconds this Connector will wait for another HTTP request before closing the connection. ", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("disableUploadTimeout", true, "This flag allows the servlet container to use a different, longer connection timeout while a servlet is being executed, which in the end allows either the servlet a longer amount of time to complete its execution, or a longer timeout during data upload.", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("maxHttpHeaderSize", 4096, "The maximum size of the request and response HTTP header, specified in bytes.", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("maxKeepAliveRequests", 100, "The maximum number of HTTP requests which can be pipelined until the connection is closed by the server. Setting this attribute to 1 will disable HTTP/1.0 keep-alive, as well as HTTP/1.1 keep-alive and pipelining. Setting this to -1 will allow an unlimited amount of pipelined or keep-alive HTTP requests.", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("maxThreads", 40, "The maximum number of request processing threads to be created by this Connector, which therefore determines the maximum number of simultaneous requests that can be handled. If not specified, this attribute is set to 40. If an executor is associated with this connector, this attribute is ignored as the connector will execute tasks using the executor rather than an internal thread pool.", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("minSpareThreads", 10, "Minimum spare threads", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("maxSpareThreads", 100, "Maximum spare threads", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<String>("noCompressionUserAgents", "", "The value is a comma separated list of regular expressions matching user-agents of HTTP clients for which compression should not be used, because these clients, although they do advertise support for the feature, have a broken implementation.", String.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("port", 8080, "The TCP port number on which this Connector  will create a server socket and await incoming connections. Your operating system will allow only one server application to listen to a particular port number on a particular IP address.", Integer.class, true));
        connectorAttributes.add(new ConnectorAttribute<String>("restrictedUserAgents", "", "The value is a comma separated list of regular expressions matching user-agents of HTTP clients for which HTTP/1.1 or HTTP/1.0 keep alive should not be used, even if the clients advertise support for these features. ", String.class));
        connectorAttributes.add(new ConnectorAttribute<String>("server", "", "The Server header for the http response.", String.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("socketBuffer", 9000, "The size (in bytes) of the buffer to be provided for socket output buffering. -1 can be specified to disable the use of a buffer.", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("tcpNoDelay", true, "If set to true, the TCP_NO_DELAY option will be set on the server socket, which improves performance under most circumstances.", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("threadPriority", Thread.NORM_PRIORITY, "The priority of the request processing threads within the JVM.", Integer.class));
    }
    
    // see http://tomcat.apache.org/tomcat-6.0-doc/config/http.html
    private static void addSslConnectorAttributes(List<ConnectorAttribute> connectorAttributes) {
        connectorAttributes.add(new ConnectorAttribute<String>("algorithm", KeyManagerFactory.getDefaultAlgorithm(), "The certificate encoding algorithm to be used.", String.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("clientAuth", false, "Set to true if you want the SSL stack to require a valid certificate chain from the client before accepting a connection. Set to want if you want the SSL stack to request a client Certificate, but not fail if one isn't presented. A false  value (which is the default) will not require a certificate chain unless the client requests a resource protected by a security constraint that uses CLIENT-CERT authentication.", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<String>("keystoreFile", "", "The file that holds the keystore (relative to the Geronimo install dir)", String.class, true));
        connectorAttributes.add(new ConnectorAttribute<String>("keystorePass", null, "Set the password used to access the keystore file. This is also the password used to access the server private key within the keystore (so the two passwords must be set to be the same on the keystore).", String.class));
        connectorAttributes.add(new ConnectorAttribute<String>("keystoreType", "JKS", "Set the keystore type. There is normally no reason not to use the default (JKS).", String.class));
        connectorAttributes.add(new ConnectorAttribute<String>("sslProtocol", "TLS", "Set the HTTPS protocol. This should normally be set to TLS, though some (IBM) JVMs don't work properly with popular browsers unless it is changed to SSL.", String.class));
        connectorAttributes.add(new ConnectorAttribute<String>("ciphers", "", "A comma seperated list of the encryption ciphers that may be used. If not specified, then any available cipher may be used.", String.class));
        connectorAttributes.add(new ConnectorAttribute<String>("keyAlias", null, "The alias used to for the server certificate in the keystore. If not specified the first key read in the keystore will be used.", String.class));
        connectorAttributes.add(new ConnectorAttribute<String>("truststoreFile", null, "The TrustStore file to use to validate client certificates.", String.class));
        connectorAttributes.add(new ConnectorAttribute<String>("truststorePass", null, "The password to access the TrustStore.", String.class));
        connectorAttributes.add(new ConnectorAttribute<String>("truststoreType", null, "Set the truststore type. There is normally no reason not to use the default (JKS).", String.class));
    }
    
    // see http://tomcat.apache.org/tomcat-6.0-doc/config/http.html
    private static void addNioConnectorAttributes(List<ConnectorAttribute> connectorAttributes) {
        connectorAttributes.add(new ConnectorAttribute<Boolean>("useSendfile", true, "Use this attribute to enable or disable sendfile capability.", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("useExecutor", true, "Set to true to use the NIO thread pool executor. The default value is true. If set to false, it uses a thread pool based on a stack for its execution. Generally, using the executor yields a little bit slower performance, but yields a better fairness for processing connections in a high load environment as the traffic gets queued through a FIFO queue. If set to true(default) then the max pool size is the maxThreads attribute and the core pool size is the minSpareThreads. This value is ignored if the executor attribute is present and points to a valid shared thread pool.", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("acceptorThreadCount", 1, "The number of threads to be used to accept connections. Increase this value on a multi CPU machine, although you would never really need more than 2. Also, with a lot of non keep alive connections, you might want to increase this value as well. ", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("pollerThreadCount", 1, "The number of threads to be used to run for the polling events.", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("pollerThreadPriority", Thread.NORM_PRIORITY, "The priority of the poller threads.", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("acceptorThreadPriority", Thread.NORM_PRIORITY, "The priority of the acceptor threads. The threads used to accept new connections.", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("selectorTimeout", 1000, "The time in milliseconds to timeout on a select() for the poller. This value is important, since connection clean up is done on the same thread, so dont set this value to an extremely high one.", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("useComet", true, "Whether to allow comet servlets or not.", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("processCache", 200, "The protocol handler caches Http11NioProcessor objects to speed up performance. This setting dictates how many of these objects get cached. -1 means unlimited, default is 200. Set this value somewhere close to your maxThreads value.", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("socket_directBuffer", false, "Whether to use direct ByteBuffers or java mapped ByteBuffers. Default is false. When you are using direct buffers, make sure you allocate the appropriate amount of memory for the direct memory space. On Sun's JDK that would be something like -XX:MaxDirectMemorySize=256m", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("socket_rxBufSize", 25188, "The socket receive buffer (SO_RCVBUF) size in bytes.", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("socket_txBufSize", 43800, "The socket send buffer (SO_SNDBUF) size in bytes.", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("socket_appReadBufSize", 8192, "Each connection that is opened up in Tomcat get associated with a read and a write ByteBuffer This attribute controls the size of these buffers. By default this read buffer is sized at 8192 bytes. For lower concurrency, you can increase this to buffer more data. For an extreme amount of keep alive connections, decrease this number or increase your heap size.", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("socket_appWriteBufSize", 8192, "Each connection that is opened up in Tomcat get associated with a read and a write ByteBuffer This attribute controls the size of these buffers. By default this write buffer is sized at 8192 bytes. For low concurrency you can increase this to buffer more response data. For an extreme amount of keep alive connections, decrease this number or increase your heap size.", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("socket_bufferPool", 500, "The Nio connector uses a class called NioChannel that holds elements linked to a socket. To reduce garbage collection, the Nio connector caches these channel objects. This value specifies the size of this cache. The default value is 500, and represents that the cache will hold 500 NioChannel objects. Other values are -1. unlimited cache, and 0, no cache.", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("socket_bufferPoolSize", 104857600, "The NioChannel pool can also be size based, not used object based. The size is calculated as follows: <br>NioChannel buffer size = read buffer size + write buffer size <br>SecureNioChannel buffer size = application read buffer size + application write buffer size + network read buffer size + network write buffer size", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("socket_processorCache", 500, "Tomcat will cache SocketProcessor objects to reduce garbage collection. The integer value specifies how many objects to keep in the cache at most. The default is 500. Other values are -1. unlimited cache, and 0, no cache.", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("socket_keyCache", 500, "Tomcat will cache KeyAttachment objects to reduce garbage collection. The integer value specifies how many objects to keep in the cache at most. The default is 500. Other values are -1. unlimited cache, and 0, no cache.", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("socket_eventCache", 500, "Tomcat will cache PollerEvent objects to reduce garbage collection. The integer value specifies how many objects to keep in the cache at most. The default is 500. Other values are -1. unlimited cache, and 0, no cache.", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("socket_tcpNoDelay", false, "Same as the standard setting tcpNoDelay.", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("socket_soKeepAlive", false, "Boolean value for the socket's keep alive setting (SO_KEEPALIVE).", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("socket_ooBInline", true, "Boolean value for the socket OOBINLINE setting.", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("socket_soReuseAddress", true, "Boolean value for the sockets reuse address option (SO_REUSEADDR). ", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("socket_soLingerOn", true, "Boolean value for the sockets so linger option (SO_LINGER). This option is paired with the soLingerTime value.", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("socket_soLingerTime", 25, "Value in seconds for the sockets so linger option (SO_LINGER). This option is paired with the soLinger value.", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("socket_soTimeout", 5000, "Value in milliseconds for the sockets read timeout (SO_TIMEOUT). ", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("socket_soTrafficClass", (0x04 | 0x08 | 0x010), "Value between 0 and 255 for the traffic class on the socket, 0x04 | 0x08 | 0x010", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("socket_performanceConnectionTime", 1, "he first value for the performance settings.", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("socket_performanceLatency", 0, "The second value for the performance settings.", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("socket_performanceBandwidth", 1, "The third value for the performance settings.", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("selectorPool_maxSelectors", 200, "The max selectors to be used in the pool, to reduce selector contention. Use this option when the command line org.apache.tomcat.util.net.NioSelectorShared value is set to false.", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("selectorPool_maxSpareSelectors", -1, "The max spare selectors to be used in the pool, to reduce selector contention. When a selector is returned to the pool, the system can decide to keep it or let it be GC:ed. Use this option when the command line org.apache.tomcat.util.net.NioSelectorShared value is set to false.", Integer.class));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("command_line_options", true, "The following command line options are available for the NIO connector: <i>-Dorg.apache.tomcat.util.net.NioSelectorShared=true|false</i>. Set this value to false if you wish to use a selector for each thread. the property. If you do set it to false, you can control the size of the pool of selectors by using the selectorPool.maxSelectors attribute", Boolean.class));
        connectorAttributes.add(new ConnectorAttribute<Integer>("oomParachute", 1048576, "The NIO connector implements an OutOfMemoryError strategy called parachute. It holds a chunk of data as a byte array. In case of an OOM, this chunk of data is released and the error is reported. This will give the VM enough room to clean up. The oomParachute represent the size in bytes of the parachute(the byte array). The default value is 1024*1024(1MB). Please note, this only works for OOM errors regarding the Java Heap space, and there is absolutely no guarantee that you will be able to recover at all. If you have an OOM outside of the Java Heap, then this parachute trick will not help.", Integer.class));
    }

    // http://tomcat.apache.org/tomcat-6.0-doc/apr.html
    private static void addAprConnectorAttributes(List<ConnectorAttribute> connectorAttributes) {
        connectorAttributes.add(new ConnectorAttribute<Integer>("pollTime", 2000, "Duration of a poll call. Lowering this value will slightly decrease latency of connections being kept alive in some cases, but will use more CPU as more poll calls are being made.", Integer.class, true));
        connectorAttributes.add(new ConnectorAttribute<Integer>("pollerSize", 8192, "Amount of sockets that the poller responsible for polling kept alive connections can hold at a given time. Extra connections will be closed right away.", Integer.class, true));
        connectorAttributes.add(new ConnectorAttribute<Boolean>("useSendfile", true, "Use kernel level sendfile for certain static files.", Boolean.class, true));
        connectorAttributes.add(new ConnectorAttribute<Integer>("sendfileSize", 1024, "Amount of sockets that the poller responsible for sending static files asynchronously can hold at a given time. Extra connections will be closed right away without any data being sent (resulting in a zero length file on the client side). Note that in most cases, sendfile is a call that will return right away (being taken care of synchonously by the kernel), and the sendfile poller will not be used, so the amount of static files which can be sent concurrently is much larger than the specified amount.", Integer.class, true));
    }
       
    private static <T> void setAttribute (List<ConnectorAttribute> connectorAttributes, String attributeName, T value) {
        for (ConnectorAttribute connectorAttribute : connectorAttributes) {
            if (connectorAttribute.getAttributeName().equals(attributeName)) {
                connectorAttribute.setValue(value);
                return;
            }
        }
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

}
