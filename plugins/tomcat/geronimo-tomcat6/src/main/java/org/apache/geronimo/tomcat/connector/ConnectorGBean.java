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
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.connector.Connector;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.tomcat.BaseGBean;
import org.apache.geronimo.tomcat.ObjectRetriever;
import org.apache.geronimo.tomcat.TomcatContainer;
import org.apache.geronimo.tomcat.TomcatServerGBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GBean(name="Tomcat Connector")
public abstract class ConnectorGBean extends BaseGBean implements CommonProtocol, GBeanLifecycle, ObjectRetriever, TomcatWebConnector {

    private static final Logger log = LoggerFactory.getLogger(ConnectorGBean.class);

    public final static String CONNECTOR_CONTAINER_REFERENCE = "TomcatContainer";

    protected final ServerInfo serverInfo;
    
    protected final Connector connector;

    private final TomcatContainer container;

    private String name;
    

	public ConnectorGBean(@ParamAttribute(name = "name") String name,
						  @ParamAttribute(name = "initParams") Map<String, String> initParams,
						  @ParamAttribute(name = "protocol") String tomcatProtocol,
						  @ParamReference(name = "TomcatContainer") TomcatContainer container,
						  @ParamReference(name = "ServerInfo") ServerInfo serverInfo,
						  @ParamAttribute(name = "connector") Connector conn)  throws Exception {
        
        //Relief for new Tomcat-only parameters that may come in the future
        if (initParams == null){
            initParams = new HashMap<String, String>();
            
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
        if (conn == null) {
            this.connector= new Connector(tomcatProtocol);
           
            for(LifecycleListener listener:TomcatServerGBean.LifecycleListeners){
                this.connector.addLifecycleListener(listener);
            }
        }else{
            connector=conn;
        }
        
        setParameters(connector, initParams);

    }

    public void doFail() {
        log.warn(name + " connector failed");
        doStop();
    }

    public void doStart() throws LifecycleException {
        container.addConnector(this.connector);
        log.debug("{} connector started", name);

    }

    public void doStop() {

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


}
