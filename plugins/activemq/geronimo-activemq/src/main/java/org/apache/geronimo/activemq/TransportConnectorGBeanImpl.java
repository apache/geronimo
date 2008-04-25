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

package org.apache.geronimo.activemq;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.activemq.broker.TransportConnector;
import org.apache.geronimo.activemq.ActiveMQConnector;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.GConstructorInfo;

/**
 * Default implementation of the ActiveMQ connector
 *
 * @version $Rev$ $Date$
 */
public class TransportConnectorGBeanImpl implements GBeanLifecycle, ActiveMQConnector {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private TransportConnector transportConnector;
    private BrokerServiceGBean brokerServiceGBean;
    
    private String protocol;
    private String host;
    private int port;
    private String path;
    private String query;
    private String urlAsStarted;
    private ClassLoader classLoader;

    public TransportConnectorGBeanImpl(BrokerServiceGBean brokerServiceGBean, String protocol, String host, int port) {
        this.brokerServiceGBean = brokerServiceGBean;
        this.protocol = protocol;
        this.host = host;
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getUrl() {
        try {
            return new URI(protocol, null, host, port, path, query, null).toString();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Attributes don't form a valid URI: "+protocol+"://"+host+":"+port+"/"+path+"?"+query, e);
        }
    }

    public InetSocketAddress getListenAddress() {
        try {
            return transportConnector.getServer().getSocketAddress();
        } catch (Throwable e) {
            log.debug("Failure to determine ListenAddress: "+e,e);
            return null;
        }
    }

    public synchronized void doStart() throws Exception {
    	ClassLoader old = Thread.currentThread().getContextClassLoader();
    	Thread.currentThread().setContextClassLoader(getClassLoader());
    	try {
	        if (transportConnector == null) {
                urlAsStarted = getUrl();
	            transportConnector = createBrokerConnector(urlAsStarted);
	            transportConnector.start();
	        }
    	} finally {
        	Thread.currentThread().setContextClassLoader(old);
    	}
    }

    public synchronized void doStop() throws Exception {
        if (transportConnector != null) {
            TransportConnector temp = transportConnector;
            transportConnector = null;
            temp.stop();
        }
    }

    public synchronized void doFail() {
        if (transportConnector != null) {
            TransportConnector temp = transportConnector;
            transportConnector = null;
            try {
                temp.stop();
            }
            catch (Exception e) {
                log.info("Caught while closing due to failure: " + e, e);
            }
        }
    }

    protected TransportConnector createBrokerConnector(String url) throws Exception {
        return brokerServiceGBean.getBrokerContainer().addConnector(url);
    }

    public ClassLoader getClassLoader() {
        if( classLoader == null ) {
            classLoader = this.getClass().getClassLoader();
        }
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic("ActiveMQ Transport Connector", TransportConnectorGBeanImpl.class, CONNECTOR_J2EE_TYPE);
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);
        infoBuilder.addAttribute("url", String.class.getName(), false);
        infoBuilder.addReference("brokerService", BrokerServiceGBean.class);
        infoBuilder.addInterface(ActiveMQConnector.class, new String[]{"host","port","protocol","path","query"},
                new String[]{"host","port"});
        infoBuilder.setConstructor(new GConstructorInfo(new String[]{"brokerService", "protocol", "host", "port"}));
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
