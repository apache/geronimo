/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.geronimo.tomcat.connector;

import java.util.HashMap;
import java.util.Map;

import javax.management.j2ee.statistics.Stats;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.management.geronimo.NetworkConnector;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.tomcat.BaseGBean;
import org.apache.geronimo.tomcat.ObjectRetriever;
import org.apache.geronimo.tomcat.TomcatContainer;

public abstract class ConnectorGBean extends BaseGBean implements CommonProtocol, GBeanLifecycle, ObjectRetriever, TomcatWebConnector {

    private static final Logger log = LoggerFactory.getLogger(ConnectorGBean.class);

    public final static String CONNECTOR_CONTAINER_REFERENCE = "TomcatContainer";

    protected final ServerInfo serverInfo;
    
    protected final Connector connector;

    private final TomcatContainer container;

    private String name;

    public ConnectorGBean(String name, Map initParams, String tomcatProtocol, TomcatContainer container, ServerInfo serverInfo) throws Exception {
        
        //Relief for new Tomcat-only parameters that may come in the future
        if (initParams == null){
            initParams = new HashMap();
        }

        // Do we really need this?? For Tomcat I don't think so...
        // validateProtocol(protocol);

        if (name == null) {
            throw new IllegalArgumentException("name cannot be null.");
        }

        if (container == null) {
            throw new IllegalArgumentException("container cannot be null.");
        }

        if (serverInfo == null){
            throw new IllegalArgumentException("serverInfo cannot be null.");
        }
        
        tomcatProtocol = validateProtocol(tomcatProtocol);

        this.name = name;
        this.container = container;
        this.serverInfo = serverInfo;

        // Create the Connector object
        connector = new Connector(tomcatProtocol);
        
        setParameters(connector, initParams);

    }

    public void doFail() {
        log.warn(name + " connector failed");
        doStop();
    }

    public void doStart() throws LifecycleException {
        container.addConnector(connector);
        connector.start();
        
        log.debug("{} connector started", name);
    }

    public void doStop() {
        try {
            connector.stop();
        } catch (LifecycleException e) {
            log.error("Failed to stop", e);
        }

        container.removeConnector(connector);

        log.debug("{} connector stopped", name);
    }
    
    /**
     * Ensures that this implementation can handle the requested protocol.
     * @param protocol
     */
    protected String validateProtocol(String tomcatProtocol) { return tomcatProtocol;}
    
    public abstract int getDefaultPort();
    
    public abstract String getGeronimoProtocol();
    
    public abstract Stats getStats();
    
    public abstract void resetStats();
    
    public Object getInternalObject() {
        return connector;
    }

    public String getName() {
        return name;
    }

    public void setAllowTrace(boolean allow) {
        connector.setAllowTrace(allow);
    }

    public boolean getAllowTrace() {
        return connector.getAllowTrace();
    }

    public void setEmptySessionPath(boolean emptySessionPath) {
        connector.setEmptySessionPath(emptySessionPath);
    }

    public void setEnableLookups(boolean enabled) {
        connector.setEnableLookups(enabled);
    }

    public int getMaxPostSize() {
        int value = connector.getMaxPostSize();
        return value == 0 ? 2097152 : value;
    }

    public void setMaxPostSize(int bytes) {
        connector.setMaxPostSize(bytes);
    }

    public String getProtocol() {
        //This is totally wrong on the Geronimo side and needs to be re-thought out.
        //This was done to shoe horn in gerneric Geronimo protocols which should have no relation
        //to the container's scheme.  This whole idea needs rework.
        return getGeronimoProtocol();
    }
    
    public String getTomcatProtocol() {
        return connector.getProtocol();
    }

    public String getProxyName() {
        return connector.getProxyName();
    }

    public int getProxyPort() {
        return connector.getProxyPort();
    }

    public int getRedirectPort() {
        return connector.getRedirectPort();
    }

    public String getScheme() {
        return connector.getScheme();
    }

    public boolean getSecure() {
        return connector.getSecure();
    }

    public String getUriEncoding() {
        return connector.getURIEncoding();
    }

    public boolean getUseBodyEncodingForURI() {
        return connector.getUseBodyEncodingForURI();
    }

    public boolean getUseIPVHosts() {
        return connector.getUseIPVHosts();
    }

    public void setMaxSavePostSize(int maxSavePostSize) {
        connector.setMaxSavePostSize(maxSavePostSize);
    }

    public void setProxyName(String proxyName) {
        if (proxyName.equals(""))
            proxyName = null;
        connector.setProxyName(proxyName);
    }

    public void setProxyPort(int port) {
        connector.setProxyPort(port);
    }

    public void setRedirectPort(int port) {
        connector.setRedirectPort(port);
    }

    public void setScheme(String scheme) {
        connector.setScheme(scheme);
    }

    public void setSecure(boolean secure) {
        connector.setSecure(secure);
    }
    
    public boolean getSslEnabled() {
        Object value = connector.getAttribute("SSLEnabled");
        return value == null ? false : new Boolean(value.toString()).booleanValue();
    }

    public void setSslEnabled(boolean sslEnabled) {
        connector.setAttribute("SSLEnabled", sslEnabled);
    }

    public void setUriEncoding(String uriEncoding) {
        connector.setURIEncoding(uriEncoding);
    }

    public void setUseBodyEncodingForURI(boolean useBodyEncodingForURI) {
        connector.setUseBodyEncodingForURI(useBodyEncodingForURI);
    }

    public void setUseIPVHosts(boolean useIPVHosts) {
        connector.setUseIPVHosts(useIPVHosts);
    }

    public void setXpoweredBy(boolean xpoweredBy) {
        connector.setXpoweredBy(xpoweredBy);
    }

    public boolean getEnableLookups() {
        return connector.getEnableLookups();
    }

    public int getMaxSavePostSize() {
        int value = connector.getMaxSavePostSize();
        return value == 0 ? 4096 : value;
    }

    public boolean getEmptySessionPath() {
        return connector.getEmptySessionPath();
    }

    public boolean getXpoweredBy() {
        return connector.getXpoweredBy();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("Tomcat Connector", ConnectorGBean.class);

        infoFactory.addAttribute("name", String.class, true);
        infoFactory.addAttribute("initParams", Map.class, true);
        infoFactory.addAttribute("protocol", String.class, true);
        infoFactory.addReference(CONNECTOR_CONTAINER_REFERENCE, TomcatContainer.class, NameFactory.GERONIMO_SERVICE);
        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");
        infoFactory.addInterface(ObjectRetriever.class);
        infoFactory.addInterface(TomcatWebConnector.class);
        infoFactory.addInterface(CommonProtocol.class,
                
                new String[]{
                        "allowTrace",
                        "emptySessionPath",
                        "enableLookups",
                        "maxPostSize",
                        "maxSavePostSize",
                        "protocol",
                        "tomcatProtocol",
                        "proxyName",
                        "proxyPort",
                        "redirectPort",
                        "scheme",
                        "secure",
                        "sslEnabled",
                        "uriEncoding",
                        "useBodyEncodingForURI",
                        "useIPVHosts",
                        "xpoweredBy"
                },

                new String[]{
                        "allowTrace",
                        "emptySessionPath",
                        "enableLookups",
                        "maxPostSize",
                        "maxSavePostSize",
                        "protocol",
                        "tomcatProtocol",
                        "proxyName",
                        "proxyPort",
                        "redirectPort",
                        "scheme",
                        "secure",
                        "sslEnabled",
                        "uriEncoding",
                        "useBodyEncodingForURI",
                        "useIPVHosts",
                        "xpoweredBy"
                }
        );
        infoFactory.setConstructor(new String[] { "name", "initParams", "protocol", "TomcatContainer", "ServerInfo" });
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
