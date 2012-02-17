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

import org.apache.catalina.Executor;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.connector.Connector;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.Persistent;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.tomcat.BaseGBean;
import org.apache.geronimo.tomcat.ObjectRetriever;
import org.apache.geronimo.tomcat.TomcatContainer;
import org.apache.geronimo.tomcat.TomcatServerGBean;
import org.apache.tomcat.util.IntrospectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GBean(name="Tomcat Connector")
public abstract class ConnectorGBean extends BaseGBean implements CommonProtocol, GBeanLifecycle, ObjectRetriever, TomcatWebConnector {

    private static final Logger log = LoggerFactory.getLogger(ConnectorGBean.class);

    public final static String CONNECTOR_CONTAINER_REFERENCE = "TomcatContainer";

    protected final ServerInfo serverInfo;

    protected Connector connector;

    protected final TomcatContainer container;

    private String name;

    private boolean wrappedConnector;

    private String tomcatProtocol;

    private Map<String, String> initParams;

    public ConnectorGBean(@ParamAttribute(manageable=false, name = "name") String name,
                        @ParamAttribute(manageable=false, name = "initParams") Map<String, String> _initParams,
                        @ParamAttribute(manageable=false, name = "protocol") String _tomcatProtocol,
                        @ParamReference(name = "TomcatContainer") TomcatContainer container,
                        @ParamReference(name = "ServerInfo") ServerInfo serverInfo,
                        @ParamAttribute(manageable=false, name = "connector") Connector conn)  throws Exception {

        //Relief for new Tomcat-only parameters that may come in the future
        if (_initParams == null){
            initParams = new HashMap<String, String>();

        }
        initParams  = (_initParams == null)?new HashMap<String, String>():_initParams;


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

        tomcatProtocol = validateProtocol(_tomcatProtocol);

        this.name = name;
        this.container = container;
        this.serverInfo = serverInfo;

        // Create the Connector object
        if (conn == null) {

            //create a connector in connector management portlet will reach here.
            this.connector = new Connector(tomcatProtocol);
            for (LifecycleListener listener : TomcatServerGBean.LifecycleListeners) {
                this.connector.addLifecycleListener(listener);
            }
            wrappedConnector = false;
        } else if (conn.getState().equals(LifecycleState.DESTROYED)) {

            //restarting a connector in connector management portlet will reach here.
            this.connector = new Connector(tomcatProtocol);
            for (LifecycleListener listener : TomcatServerGBean.LifecycleListeners) {
                this.connector.addLifecycleListener(listener);
            }
        } else {

          //the connectors defined in server.xml will reach here.
            connector = conn;
            wrappedConnector = true;

        }


        setParameters(connector, initParams);

    }

    public void doFail() {
        //log.warn(name + " connector failed");
        doStop();
    }

    public void doStart() throws LifecycleException {

        if (wrappedConnector && this.connector.getState().equals(LifecycleState.STARTED)) {
            return;
        }

        String executorName=null;
        Executor executor=null;

        if (this.connector.getAttribute("executor") != null) {

            Object value = connector.getAttribute("executor");
            if (value == null)
                executorName=null;

            if (value instanceof String)
                executorName= (String)value;

            if(value instanceof Executor){
                executorName= ((Executor) value).getName();
            }

            executor = TomcatServerGBean.executors.get(executorName);

            if (executor == null) {

                log.warn("No executor found with name:" + executorName+", trying to get default executor with name 'DefaultThreadPool'");
                executor = TomcatServerGBean.executors.get("DefaultThreadPool");
            }


        } else {

            executor = TomcatServerGBean.executors.get("DefaultThreadPool");

            if (executor == null) {

                log.warn("No executor found in service with name: DefaultThreadPool");

            }
        }


        if (executor != null)

        {
            log.info("executor:"+executor.getName()+" found, set it to connector:"+this.getName() );

            try {

                IntrospectionUtils.callMethod1(this.connector.getProtocolHandler(),
                                                "setExecutor",
                                                executor,
                                                java.util.concurrent.Executor.class.getName(),
                                                connector.getClass().getClassLoader());
            } catch (Exception e) {

                log.info("connector:"+this.getName()+"does not support executor set, do nothing");
            }
        }

        container.addConnector(this.connector);

        this.connector.start();

        log.debug("{} connector started", name);

    }

    public void doStop() {
        if (!wrappedConnector) {
            container.removeConnector(connector);
            try {
                connector.stop();
                connector.destroy();
            } catch (LifecycleException e) {
                log.error("fail to stop connector", e);
            }
            log.debug("{} connector stopped", name);
        }
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
    
    @Persistent(manageable=false)
    public void setAllowTrace(boolean allow) {
        connector.setAllowTrace(allow);
    }

    public boolean getAllowTrace() {
        return connector.getAllowTrace();
    }

    public long getAsyncTimeout() {
        return  connector.getAsyncTimeout();
    }

    @Persistent(manageable=false)
    public void setAsyncTimeout(long asyncTimeout) {
        connector.setAsyncTimeout(asyncTimeout);
    }

    @Persistent(manageable=false)
    public void setEnableLookups(boolean enabled) {
        connector.setEnableLookups(enabled);
    }

    public int getMaxPostSize() {
        int value = connector.getMaxPostSize();
        return value == 0 ? 2097152 : value;
    }

    @Persistent(manageable=false)
    public void setMaxPostSize(int bytes) {
        connector.setMaxPostSize(bytes);
    }
    
    public int getMaxParameterCount(){
    	int value = connector.getMaxParameterCount();
    	return value == 0 ? 10000 : value;
    }
    
    @Persistent(manageable=false)
    public void setMaxParameterCount(int count){
    	connector.setMaxParameterCount(count);
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

    @Persistent(manageable=false)
    public void setMaxSavePostSize(int maxSavePostSize) {
        connector.setMaxSavePostSize(maxSavePostSize);
    }

    @Persistent(manageable=false)
    public void setProxyName(String proxyName) {
        if (proxyName.equals(""))
            proxyName = null;
        connector.setProxyName(proxyName);
    }

    @Persistent(manageable=false)
    public void setProxyPort(int port) {
        connector.setProxyPort(port);
    }

    @Persistent(manageable=false)
    public void setRedirectPort(int port) {
        connector.setRedirectPort(port);
    }

    @Persistent(manageable=false)
    public void setScheme(String scheme) {
        connector.setScheme(scheme);
    }

    @Persistent(manageable=false)
    public void setSecure(boolean secure) {
        connector.setSecure(secure);
    }

    public boolean getSslEnabled() {
        Object value = connector.getAttribute("SSLEnabled");
        return value == null ? false : Boolean.valueOf(value.toString());
    }

    @Persistent(manageable=false)
    public void setSslEnabled(boolean sslEnabled) {
        connector.setAttribute("SSLEnabled", sslEnabled);
    }

    @Persistent(manageable=false)
    public void setUriEncoding(String uriEncoding) {
        connector.setURIEncoding(uriEncoding);
    }

    @Persistent(manageable=false)
    public void setUseBodyEncodingForURI(boolean useBodyEncodingForURI) {
        connector.setUseBodyEncodingForURI(useBodyEncodingForURI);
    }

    @Persistent(manageable=false)
    public void setUseIPVHosts(boolean useIPVHosts) {
        connector.setUseIPVHosts(useIPVHosts);
    }

    @Persistent(manageable=false)
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

    public boolean getXpoweredBy() {
        return connector.getXpoweredBy();
    }


}
