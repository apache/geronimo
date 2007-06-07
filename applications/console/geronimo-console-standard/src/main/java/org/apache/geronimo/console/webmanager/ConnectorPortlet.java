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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.apache.geronimo.management.geronimo.KeystoreException;
import org.apache.geronimo.management.geronimo.KeystoreInstance;
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
    
    private final static HashMap<String, Object> TOMCAT_DEFAULTS;
    static {
        TOMCAT_DEFAULTS = new HashMap<String, Object>();
        TOMCAT_DEFAULTS.put("allowTrace", Boolean.FALSE);
        TOMCAT_DEFAULTS.put("emptySessionPath", Boolean.FALSE);
        TOMCAT_DEFAULTS.put("enableLookups", Boolean.TRUE);
        TOMCAT_DEFAULTS.put("maxPostSize", 2097152);
        TOMCAT_DEFAULTS.put("maxSavePostSize", 4096);
        TOMCAT_DEFAULTS.put("useBodyEncodingForURI", Boolean.FALSE);
        TOMCAT_DEFAULTS.put("useIPVHosts", Boolean.FALSE);
        TOMCAT_DEFAULTS.put("xpoweredBy", Boolean.FALSE);
        TOMCAT_DEFAULTS.put("acceptCount", 10);
        TOMCAT_DEFAULTS.put("bufferSize", 2048);
        TOMCAT_DEFAULTS.put("compressableMimeType", "text/html,text/xml,text/plain");
        TOMCAT_DEFAULTS.put("compression", "off");
        TOMCAT_DEFAULTS.put("connectionLinger", -1);
        TOMCAT_DEFAULTS.put("connectionTimeout", 60000);
        TOMCAT_DEFAULTS.put("disableUploadTimeout", true);
        TOMCAT_DEFAULTS.put("maxHttpHeaderSize", 4096);
        TOMCAT_DEFAULTS.put("maxKeepAliveRequests", 100);
        TOMCAT_DEFAULTS.put("maxSpareThreads", 50);
        TOMCAT_DEFAULTS.put("minSpareThreads", 4);
        TOMCAT_DEFAULTS.put("noCompressionUserAgents", "");
        TOMCAT_DEFAULTS.put("restrictedUserAgents", "");
        TOMCAT_DEFAULTS.put("socketBuffer", 9000);
        TOMCAT_DEFAULTS.put("strategy", "lf");
        TOMCAT_DEFAULTS.put("tcpNoDelay", true);
        TOMCAT_DEFAULTS.put("threadPriority", Thread.NORM_PRIORITY);
    }

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
            String displayName = actionRequest.getParameter("displayName");
            // Create and configure the connector
            WebConnector connector = PortletManager.createWebConnector(actionRequest, new AbstractName(URI.create(managerURI)), new AbstractName(URI.create(containerURI)), displayName, protocol, host, port);
            connector.setMaxThreads(maxThreads);
            if (server.equals(WEB_SERVER_TOMCAT)) {
                setTomcatAttributes(actionRequest, connector);
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
                String ciphers = actionRequest.getParameter("ciphers");
                boolean clientAuth = isValid(actionRequest.getParameter("clientAuth"));
                SecureConnector secure = (SecureConnector) connector;
                if(isValid(keystoreType)) {
                    secure.setKeystoreType(keystoreType);
                } else {
                    secure.setKeystoreType(null);
                }
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
                                keys = keystore.getUnlockedKeys(null);
                            }
                        }
                        if(keys != null && keys.length == 1) {
                            setProperty(secure, "keyAlias", keys[0]);
                        } else {
                            throw new PortletException("Cannot handle keystores with anything but 1 unlocked private key");
                        }
                    } catch (KeystoreException e) {
                        throw new PortletException(e);
                    }
                    String trustStore = actionRequest.getParameter("unlockTrustStore");
                    // "" is a valid trustStore value, which means the parameter should be cleared
                    setProperty(secure, "trustStore", isValid(trustStore) ? trustStore : null);
                } else if (server.equals(WEB_SERVER_TOMCAT)) {
                    if(isValid(truststoreType)) {setProperty(secure, "truststoreType", truststoreType);}
                    if(isValid(truststoreFile)) {setProperty(secure, "truststoreFileName", truststoreFile);}
                    if(isValid(truststorePass)) {setProperty(secure, "truststorePassword", truststorePass);}
                    setProperty(secure, "ciphers", isValid(ciphers) ? ciphers : "");
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
            String connectorURI = actionRequest.getParameter("connectorURI");
            // Identify and update the connector
            WebConnector connector = PortletManager.getWebConnector(actionRequest, new AbstractName(URI.create(connectorURI)));
            if(connector != null) {
                if(!connector.getHost().equals(host)) connector.setHost(host);
                if(connector.getPort() != port) connector.setPort(port);
                if(connector.getMaxThreads() != maxThreads) connector.setMaxThreads(maxThreads);
                if (server.equals(WEB_SERVER_TOMCAT)) {
                    setTomcatAttributes(actionRequest, connector);
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
                    String ciphers = actionRequest.getParameter("ciphers");
                    boolean clientAuth = isValid(actionRequest.getParameter("clientAuth"));
                    SecureConnector secure = (SecureConnector) connector;
                    String oldVal = secure.getKeystoreType();
                    if(isValid(keystoreType)) {
                        if(!keystoreType.equals(oldVal))
                            secure.setKeystoreType(keystoreType);
                    } else {
                        if(oldVal != null) secure.setKeystoreType(null);
                    }
                    if(isValid(keystoreFile) && !keystoreFile.equals(secure.getKeystoreFileName())) {secure.setKeystoreFileName(keystoreFile);}
                    if(isValid(keystorePass)) {secure.setKeystorePassword(keystorePass);}
                    if(isValid(secureProtocol) && !secureProtocol.equals(secure.getSecureProtocol())) {secure.setSecureProtocol(secureProtocol);}
                    if(isValid(algorithm) && !algorithm.equals(secure.getAlgorithm())) {secure.setAlgorithm(algorithm);}
                    if(clientAuth != secure.isClientAuthRequired()) secure.setClientAuthRequired(clientAuth);
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
                                    keys = keystore.getUnlockedKeys(null);
                                }
                            }
                            if(keys != null && keys.length == 1) {
                                setProperty(secure, "keyAlias", keys[0]);
                            } else {
                                throw new PortletException("Cannot handle keystores with anything but 1 unlocked private key");
                            }
                        } catch (KeystoreException e) {
                            throw new PortletException(e);
                        }
                        // "" is a valid trustStore value, which means the parameter should be cleared
                        setProperty(secure, "trustStore", isValid(trustStore) ? trustStore : null);
                    }
                    else if (server.equals(WEB_SERVER_TOMCAT)) {
                        if(isValid(truststoreType) && !truststoreType.equals(getProperty(secure, "truststoreType"))) {setProperty(secure, "truststoreType", truststoreType);}
                        if(isValid(truststorePass)) {setProperty(secure, "truststorePassword", truststorePass);}
                        if(isValid(truststoreFile) && !truststoreFile.equals(getProperty(secure, "truststoreFileName"))) {setProperty(secure, "truststoreFileName", truststoreFile);}
                        String prevVal = (String)getProperty(secure, "ciphers");
                        if(isValid(ciphers)) {
                            if(!ciphers.equals(prevVal)) setProperty(secure, "ciphers", ciphers);
                        } else {
                            if(prevVal != null) setProperty(secure, "ciphers", null);
                        }
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

    /**
     * This method retrieves Tomcat Connector attributes from the action request and sets the attributes in the connector.
     * @param actionRequest
     * @param connector
     */
    private void setTomcatAttributes(ActionRequest actionRequest, WebConnector connector) {
        boolean prevBoolVal;
        int prevIntVal;
        String prevVal;
        
        boolean allowTrace = isValid(actionRequest.getParameter("allowTrace"));
        prevBoolVal = (Boolean)getProperty(connector, "allowTrace");
        if(allowTrace != prevBoolVal) setProperty(connector, "allowTrace", allowTrace);

        boolean emptySessionPath = isValid(actionRequest.getParameter("emptySessionPath"));
        prevBoolVal = (Boolean)callOperation(connector, "isEmptySessionPath", null);
        if(emptySessionPath != prevBoolVal) setProperty(connector, "emptySessionPath", emptySessionPath);

        boolean enableLookups = isValid(actionRequest.getParameter("enableLookups"));
        prevBoolVal = (Boolean)callOperation(connector, "isHostLookupEnabled", null);
        if(enableLookups != prevBoolVal) setProperty(connector, "hostLookupEnabled", enableLookups);

        String maxPostSize = actionRequest.getParameter("maxPostSize");
        prevIntVal = (Integer)getProperty(connector, "maxPostSize");
        if(isValid(maxPostSize)) {
            int newVal = Integer.parseInt(maxPostSize);
            if(newVal != prevIntVal) setProperty(connector, "maxPostSize", newVal);
        } else {
            if(prevIntVal != (Integer)TOMCAT_DEFAULTS.get("maxPostSize")) setProperty(connector, "maxPostSize", TOMCAT_DEFAULTS.get("maxPostSize"));
        }

        String maxSavePostSize = actionRequest.getParameter("maxSavePostSize");
        prevIntVal = (Integer)getProperty(connector, "maxSavePostSize");
        if(isValid(maxSavePostSize)) {
            int newVal = Integer.parseInt(maxSavePostSize);
            if(newVal != prevIntVal) setProperty(connector, "maxSavePostSize", newVal);
        } else {
            if(prevIntVal != (Integer)TOMCAT_DEFAULTS.get("maxSavePostSize")) setProperty(connector, "maxSavePostSize", TOMCAT_DEFAULTS.get("maxSavePostSize"));
        }

        String proxyName = actionRequest.getParameter("proxyName");
        prevVal = (String)getProperty(connector, "proxyName");
        if(isValid(proxyName)) {
            if(!proxyName.equals(prevVal)) setProperty(connector, "proxyName", proxyName);
        } else {
            if(prevVal != null) setProperty(connector, "proxyName", null);
        }

        String proxyPort = actionRequest.getParameter("proxyPort");
        prevIntVal = (Integer)getProperty(connector, "proxyPort");
        if(isValid(proxyPort)) {
            int newVal = Integer.parseInt(proxyPort);
            if(newVal != prevIntVal) setProperty(connector, "proxyPort", newVal);
        } else {
            if(prevIntVal != 0) setProperty(connector, "proxyPort", 0);
        }
        
        String redirectPort = actionRequest.getParameter("redirectPort");
        prevIntVal = connector.getRedirectPort();
        if(isValid(redirectPort)) {
            int newVal = Integer.parseInt(redirectPort);
            if(newVal != prevIntVal) connector.setRedirectPort(newVal);
        } else {
            if(prevIntVal != 0) connector.setRedirectPort(0);
        }
        
        String URIEncoding = actionRequest.getParameter("URIEncoding");
        prevVal = (String)getProperty(connector, "uriEncoding");
        if(isValid(URIEncoding)) {
            if(!URIEncoding.equals(prevVal)) setProperty(connector, "uriEncoding", URIEncoding);
        } else {
            if(prevVal != null) setProperty(connector, "uriEncoding", null);//FIXME
        }
        
        boolean useBodyEncodingForURI = isValid(actionRequest.getParameter("useBodyEncodingForURI"));
        prevBoolVal = (Boolean)getProperty(connector, "useBodyEncodingForURI");
        if(useBodyEncodingForURI != prevBoolVal) setProperty(connector, "useBodyEncodingForURI", useBodyEncodingForURI);

        boolean useIPVHosts = isValid(actionRequest.getParameter("useIPVHosts"));
        prevBoolVal = (Boolean)getProperty(connector, "useIPVHosts");
        if(useIPVHosts != prevBoolVal) setProperty(connector, "useIPVHosts", useIPVHosts);

        boolean xpoweredBy = isValid(actionRequest.getParameter("xpoweredBy"));
        prevBoolVal = (Boolean)getProperty(connector, "xpoweredBy");
        if(xpoweredBy != prevBoolVal) setProperty(connector, "xpoweredBy", xpoweredBy);

        String acceptCount = actionRequest.getParameter("acceptCount");
        prevIntVal = connector.getAcceptQueueSize();
        if(isValid(acceptCount)) {
            int newVal = Integer.parseInt(acceptCount);
            if(prevIntVal != newVal) connector.setAcceptQueueSize(newVal);
        } else {
            if(prevIntVal != (Integer)TOMCAT_DEFAULTS.get("acceptCount")) connector.setAcceptQueueSize((Integer)TOMCAT_DEFAULTS.get("acceptCount"));
        }

        String bufferSize = actionRequest.getParameter("bufferSize");
        prevIntVal = connector.getBufferSizeBytes();
        if(isValid(bufferSize)) {
            int newVal = Integer.parseInt(bufferSize);
            if(prevIntVal != newVal) connector.setBufferSizeBytes(newVal);
        } else {
            if(prevIntVal != (Integer)TOMCAT_DEFAULTS.get("bufferSize")) connector.setBufferSizeBytes((Integer)TOMCAT_DEFAULTS.get("bufferSize"));
        }

        String compressableMimeType = actionRequest.getParameter("compressableMimeType");
        prevVal = (String)getProperty(connector, "compressableMimeType");
        if(isValid(compressableMimeType)) {
            if(!compressableMimeType.equals(prevVal)) setProperty(connector, "compressableMimeType", compressableMimeType);
        } else {
            if(!TOMCAT_DEFAULTS.get("compressableMimeType").equals(prevVal)) setProperty(connector, "compressableMimeType", TOMCAT_DEFAULTS.get("compressableMimeType"));
        }

        String compression = actionRequest.getParameter("compression");
        prevVal = (String)getProperty(connector, "compression");
        if(isValid(compression)) {
            if(!compression.equals(prevVal)) setProperty(connector, "compression", compression);
        } else {
            if(!TOMCAT_DEFAULTS.get("compression").equals(prevVal)) setProperty(connector, "compression", TOMCAT_DEFAULTS.get("compression"));
        }

        String connectionLinger = actionRequest.getParameter("connectionLinger");
        prevIntVal = connector.getLingerMillis();
        if(isValid(connectionLinger)) {
            int newVal = Integer.parseInt(connectionLinger);
            if(prevIntVal != newVal) connector.setLingerMillis(newVal);
        } else {
            if(prevIntVal != (Integer)TOMCAT_DEFAULTS.get("connectionLinger")) connector.setLingerMillis((Integer)TOMCAT_DEFAULTS.get("connectionLinger"));
        }

        String connectionTimeout = actionRequest.getParameter("connectionTimeout");
        prevIntVal = (Integer)getProperty(connector, "connectionTimeoutMillis");
        if(isValid(connectionTimeout)) {
            int newVal = Integer.parseInt(connectionTimeout);
            if(prevIntVal != newVal) setProperty(connector, "connectionTimeoutMillis", newVal);
        } else {
            if(prevIntVal != (Integer)TOMCAT_DEFAULTS.get("connectionTimeout")) setProperty(connector, "connectionTimeoutMillis", (Integer)TOMCAT_DEFAULTS.get("connectionTimeout"));
        }

        String keepAliveTimeout = actionRequest.getParameter("keepAliveTimeout");
        prevIntVal = (Integer)getProperty(connector, "keepAliveTimeout");
        if(isValid(keepAliveTimeout)) {
            int newVal = Integer.parseInt(keepAliveTimeout);
            if(prevIntVal != newVal) setProperty(connector, "keepAliveTimeout", newVal);
        } else {
            if(prevIntVal != (Integer)getProperty(connector, "connectionTimeoutMillis")) setProperty(connector, "keepAliveTimeout", getProperty(connector, "connectionTimeoutMillis"));
        }

        boolean disableUploadTimeout = isValid(actionRequest.getParameter("disableUploadTimeout"));
        prevBoolVal = !(Boolean)callOperation(connector, "isUploadTimeoutEnabled", null);
        if(disableUploadTimeout != prevBoolVal) setProperty(connector, "uploadTimeoutEnabled", !disableUploadTimeout);

        String maxHttpHeaderSize = actionRequest.getParameter("maxHttpHeaderSize");
        prevIntVal = (Integer)getProperty(connector, "maxHttpHeaderSizeBytes");
        if(isValid(maxHttpHeaderSize)) {
            int newVal = Integer.parseInt(maxHttpHeaderSize);
            if(newVal != prevIntVal) setProperty(connector, "maxHttpHeaderSizeBytes", newVal);
        } else {
            if(prevIntVal != (Integer)TOMCAT_DEFAULTS.get("maxHttpHeaderSize")) setProperty(connector, "maxHttpHeaderSizeBytes", TOMCAT_DEFAULTS.get("maxHttpHeaderSize"));
        }
        
        String maxKeepAliveRequests = actionRequest.getParameter("maxKeepAliveRequests");
        prevIntVal = (Integer)getProperty(connector, "maxKeepAliveRequests");
        if(isValid(maxKeepAliveRequests)) {
            int newVal = Integer.parseInt(maxKeepAliveRequests);
            if(prevIntVal != newVal) setProperty(connector, "maxKeepAliveRequests", newVal);
        } else {
            if(prevIntVal != (Integer)TOMCAT_DEFAULTS.get("maxKeepAliveRequests")) setProperty(connector, "maxKeepAliveRequests", TOMCAT_DEFAULTS.get("maxKeepAliveRequests"));
        }
        
        String maxSpareThreads = actionRequest.getParameter("maxSpareThreads");
        prevIntVal = (Integer)getProperty(connector, "maxSpareThreads");
        if(isValid(maxSpareThreads)) {
            int newVal =  Integer.parseInt(maxSpareThreads);
            if(prevIntVal != newVal) setProperty(connector, "maxSpareThreads", newVal);
        } else {
            if(prevIntVal != (Integer)TOMCAT_DEFAULTS.get("maxSpareThreads")) setProperty(connector, "maxSpareThreads", TOMCAT_DEFAULTS.get("maxSpareThreads"));
        }
        
        String minSpareThreads = actionRequest.getParameter("minSpareThreads");
        prevIntVal = (Integer)getProperty(connector, "minSpareThreads");
        if(isValid(minSpareThreads)) {
            int newVal = new Integer(minSpareThreads);
            if(prevIntVal != newVal) setProperty(connector, "minSpareThreads", newVal);
        } else {
            if(prevIntVal != (Integer)TOMCAT_DEFAULTS.get("minSpareThreads")) setProperty(connector, "minSpareThreads", TOMCAT_DEFAULTS.get("minSpareThreads"));
        }
        
        String noCompressionUserAgents = actionRequest.getParameter("noCompressionUserAgents");
        prevVal = (String)getProperty(connector, "noCompressionUserAgents");
        if(isValid(noCompressionUserAgents)) {
            if(!noCompressionUserAgents.equals(prevVal)) setProperty(connector, "noCompressionUserAgents", noCompressionUserAgents);
        } else {
            if(prevVal != null) setProperty(connector, "noCompressionUserAgents", TOMCAT_DEFAULTS.get("noCompressionUserAgents"));
        }

        String restrictedUserAgents = actionRequest.getParameter("restrictedUserAgents");
        prevVal = (String)getProperty(connector, "restrictedUserAgents");
        if(isValid(restrictedUserAgents)) {
            if(!restrictedUserAgents.equals(prevVal)) setProperty(connector, "restrictedUserAgents", restrictedUserAgents);
        } else {
            if(prevVal != null) setProperty(connector, "restrictedUserAgents", TOMCAT_DEFAULTS.get("restrictedUserAgents"));
        }

        String serverAttribute = actionRequest.getParameter("serverAttribute");
        prevVal = (String)getProperty(connector, "server");
        if(isValid(serverAttribute)) {
            if(!serverAttribute.equals(prevVal)) setProperty(connector, "server", serverAttribute);
        } else {
            if(prevVal != null) setProperty(connector, "server", null);
        }

        String socketBuffer = actionRequest.getParameter("socketBuffer");
        prevIntVal = (Integer)getProperty(connector, "socketBuffer");
        if(isValid(socketBuffer)) {
            int newVal = Integer.parseInt(socketBuffer);
            if(prevIntVal != newVal) setProperty(connector, "socketBuffer", newVal);
        } else {
            if(prevIntVal != (Integer)TOMCAT_DEFAULTS.get("socketBuffer")) setProperty(connector, "socketBuffer", TOMCAT_DEFAULTS.get("socketBuffer"));
        }

        String strategy = actionRequest.getParameter("strategy");
        prevVal = (String)getProperty(connector, "strategy");
        if(isValid(strategy)) {
            if(!strategy.equals(prevVal)) setProperty(connector, "strategy", strategy);
        } else {
            if(prevVal != null) setProperty(connector, "strategy", TOMCAT_DEFAULTS.get("strategy"));
        }

        boolean tcpNoDelay = isValid(actionRequest.getParameter("tcpNoDelay"));
        prevBoolVal = connector.isTcpNoDelay();
        if(tcpNoDelay != prevBoolVal) connector.setTcpNoDelay(tcpNoDelay);
        
        String threadPriority = actionRequest.getParameter("threadPriority");
        prevIntVal = (Integer)getProperty(connector, "threadPriority");
        if(isValid(threadPriority)) {
            int newVal = Integer.parseInt(threadPriority);
            if(prevIntVal != newVal) setProperty(connector, "threadPriority", newVal);
        } else {
            if(prevIntVal != (Integer)TOMCAT_DEFAULTS.get("threadPriority")) setProperty(connector, "threadPriority", TOMCAT_DEFAULTS.get("threadPriority"));
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
                            aliases.put(stores[i].getKeystoreName(), stores[i].getUnlockedKeys(null));
                        } catch (KeystoreException e) {}
                    }
                    renderRequest.setAttribute("unlockedKeys", aliases);
                }
                else if (server.equals(WEB_SERVER_TOMCAT)) {
                    //todo:   Any Tomcat specific processing?
                    for(String key:TOMCAT_DEFAULTS.keySet()) {
                        Object val = TOMCAT_DEFAULTS.get(key);
                        if(!(val instanceof Boolean))
                            renderRequest.setAttribute(key, TOMCAT_DEFAULTS.get(key));
                        else if((Boolean)val) // For boolean, set attribute only if it is true
                            renderRequest.setAttribute(key, TOMCAT_DEFAULTS.get(key));
                    }
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
                                aliases.put(stores[i].getKeystoreName(), stores[i].getUnlockedKeys(null));
                            } catch (KeystoreException e) {}
                        }
                        renderRequest.setAttribute("unlockedKeys", aliases);
                    }
                    else if (server.equals(WEB_SERVER_TOMCAT)) {
                        //todo:   Any Tomcat specific processing?
                        Boolean allowTrace = (Boolean)getProperty(connector, "allowTrace");
                        if(allowTrace) {
                            renderRequest.setAttribute("allowTrace", allowTrace);
                        }

                        Boolean emptySessionPath = (Boolean)callOperation(connector, "isEmptySessionPath", null);
                        if(emptySessionPath) {
                            renderRequest.setAttribute("emptySessionPath", emptySessionPath);
                        }

                        Boolean enableLookups = (Boolean)callOperation(connector, "isHostLookupEnabled", null);
                        if(enableLookups) {
                            renderRequest.setAttribute("enableLookups", enableLookups);
                        }

                        Integer maxPostSize = (Integer)getProperty(connector, "maxPostSize");
                        renderRequest.setAttribute("maxPostSize", maxPostSize);

                        Integer maxSavePostSize = (Integer)getProperty(connector, "maxSavePostSize");
                        renderRequest.setAttribute("maxSavePostSize", maxSavePostSize);

                        String proxyName = (String)getProperty(connector, "proxyName");
                        if(isValid(proxyName)) {
                            renderRequest.setAttribute("proxyName", proxyName);
                        }

                        Integer proxyPort = (Integer)getProperty(connector, "proxyPort");
                        renderRequest.setAttribute("proxyPort", proxyPort);

                        Integer redirectPort = connector.getRedirectPort();
                        renderRequest.setAttribute("redirectPort", redirectPort);

                        String URIEncoding = (String)getProperty(connector, "uriEncoding");
                        if(isValid(URIEncoding)) {
                            renderRequest.setAttribute("URIEncoding", URIEncoding);
                        }

                        Boolean useBodyEncodingForURI = (Boolean)getProperty(connector, "useBodyEncodingForURI");
                        if(useBodyEncodingForURI) {
                            renderRequest.setAttribute("useBodyEncodingForURI", useBodyEncodingForURI);
                        }

                        Boolean useIPVHosts = (Boolean)getProperty(connector, "useIPVHosts");
                        if(useBodyEncodingForURI) {
                            renderRequest.setAttribute("useIPVHosts", useIPVHosts);
                        }

                        Boolean xpoweredBy = (Boolean)getProperty(connector, "xpoweredBy");
                        if(useBodyEncodingForURI) {
                            renderRequest.setAttribute("xpoweredBy", xpoweredBy);
                        }

                        Integer acceptCount = connector.getAcceptQueueSize();
                        renderRequest.setAttribute("acceptCount", acceptCount);

                        Integer bufferSize = connector.getBufferSizeBytes();
                        renderRequest.setAttribute("bufferSize", bufferSize);

                        String compressableMimeType = (String)getProperty(connector, "compressableMimeType");
                        if(isValid(compressableMimeType)) {
                            renderRequest.setAttribute("compressableMimeType", compressableMimeType);
                        }

                        String compression = (String)getProperty(connector, "compression");
                        if(isValid(compression)) {
                            renderRequest.setAttribute("compression", compression);
                        }

                        Integer connectionLinger = connector.getLingerMillis();
                        renderRequest.setAttribute("connectionLinger", connectionLinger);

                        Integer connectionTimeout = (Integer)getProperty(connector, "connectionTimeoutMillis");
                        renderRequest.setAttribute("connectionTimeout", connectionTimeout);

                        Integer keepAliveTimeout = (Integer)getProperty(connector, "keepAliveTimeout");
                        renderRequest.setAttribute("keepAliveTimeout", keepAliveTimeout);

                        Boolean disableUploadTimeout = !(Boolean)callOperation(connector, "isUploadTimeoutEnabled", null);
                        if(disableUploadTimeout) {
                            renderRequest.setAttribute("disableUploadTimeout", disableUploadTimeout);
                        }

                        Integer maxHttpHeaderSize = (Integer)getProperty(connector, "maxHttpHeaderSizeBytes");
                        renderRequest.setAttribute("maxHttpHeaderSize", maxHttpHeaderSize);

                        Integer maxKeepAliveRequests = (Integer)getProperty(connector, "maxKeepAliveRequests");
                        renderRequest.setAttribute("maxKeepAliveRequests", maxKeepAliveRequests);

                        Integer maxSpareThreads = (Integer)getProperty(connector, "maxSpareThreads");
                        renderRequest.setAttribute("maxSpareThreads", maxSpareThreads);

                        Integer minSpareThreads = (Integer)getProperty(connector, "minSpareThreads");
                        renderRequest.setAttribute("minSpareThreads", minSpareThreads);

                        String noCompressionUserAgents = (String)getProperty(connector, "noCompressionUserAgents");
                        if(isValid(noCompressionUserAgents)) {
                            renderRequest.setAttribute("noCompressionUserAgents", noCompressionUserAgents);
                        }

                        String restrictedUserAgents = (String)getProperty(connector, "restrictedUserAgents");
                        if(isValid(restrictedUserAgents)) {
                            renderRequest.setAttribute("restrictedUserAgents", restrictedUserAgents);
                        }
                        
                        String serverAttribute = (String)getProperty(connector, "server");
                        if(isValid(serverAttribute)) {
                            renderRequest.setAttribute("serverAttribute", serverAttribute);
                        }

                        Integer socketBuffer = (Integer)getProperty(connector, "socketBuffer");
                        renderRequest.setAttribute("socketBuffer", socketBuffer);

                        String strategy = (String)getProperty(connector, "strategy");
                        if(isValid(strategy)) {
                            renderRequest.setAttribute("strategy", strategy);
                        }

                        Boolean tcpNoDelay = connector.isTcpNoDelay();
                        if(tcpNoDelay) {
                            renderRequest.setAttribute("tcpNoDelay", tcpNoDelay);
                        }

                        Integer threadPriority = (Integer)getProperty(connector, "threadPriority");
                        renderRequest.setAttribute("threadPriority", threadPriority);
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
                            String ciphers = (String)getProperty(secure, "ciphers");
                            renderRequest.setAttribute("truststoreFile", truststoreFile);
                            renderRequest.setAttribute("truststoreType", truststoreType);
                            renderRequest.setAttribute("ciphers", ciphers);
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
        renderRequest.setAttribute("serverPort", new Integer(renderRequest.getServerPort()));

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
