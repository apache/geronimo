/**
 *   Licensed to the Apache Software Foundation (ASF) under one or more
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

import java.net.URI;

import javax.jms.JMSException;
import javax.resource.ResourceException;
import javax.sql.DataSource;

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.jmx.ManagementContext;
import org.apache.activemq.store.DefaultPersistenceAdapterFactory;
import org.apache.activemq.transport.TransportDisposedIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.naming.ResourceSource;
import org.apache.geronimo.management.geronimo.JMSManager;
import org.apache.geronimo.management.geronimo.NetworkConnector;
import org.apache.geronimo.system.jmx.MBeanServerReference;
import org.apache.geronimo.system.serverinfo.ServerInfo;

/**
 * Default implementation of the ActiveMQ Message Server
 *
 * @version $Rev$ $Date$
 */
public class BrokerServiceGBeanImpl implements GBeanLifecycle, BrokerServiceGBean {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private String brokerName;
    private String brokerUri;
    private BrokerService brokerService;
    private ServerInfo serverInfo;
    private String dataDirectory;
    private ResourceSource<ResourceException> dataSource;
    private ClassLoader classLoader;
    private String objectName;
    private JMSManager manager;
    private boolean useShutdownHook;
    private MBeanServerReference mbeanServerReference;

    public BrokerServiceGBeanImpl() {
    }

    public synchronized BrokerService getBrokerContainer() {
        return brokerService;
    }
    
    public void setMbeanServerReference(MBeanServerReference mbeanServerReference) {
        this.mbeanServerReference = mbeanServerReference;
    }

    public synchronized void doStart() throws Exception {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClassLoader());
        try {
            if (brokerService == null) {
                if (brokerUri != null) {
                    brokerService = BrokerFactory.createBroker(new URI(brokerUri));
                    brokerName = brokerService.getBrokerName();
                }
                else {
                    brokerService = new BrokerService();
                    if (brokerName != null) {
                        brokerService.setBrokerName(brokerName);
                    }
                    else {
                        brokerName = brokerService.getBrokerName();
                    }
                }
            }
            
            // Do not allow creation of another ConnectorServer
            ManagementContext mgmtctx = new ManagementContext(mbeanServerReference != null ? mbeanServerReference.getMBeanServer() : null);
            mgmtctx.setCreateConnector(false);
            brokerService.setManagementContext(mgmtctx);

            // Do not allow the broker to use a shutown hook, the kernel will stop it
            brokerService.setUseShutdownHook(isUseShutdownHook());

            // Setup the persistence adapter to use the right datasource and directory
            DefaultPersistenceAdapterFactory persistenceFactory = (DefaultPersistenceAdapterFactory) brokerService.getPersistenceFactory();
            persistenceFactory.setDataDirectoryFile(serverInfo.resolveServer(dataDirectory));
            persistenceFactory.setDataSource((DataSource) dataSource.$getResource());

            brokerService.start();
        }
        finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    public synchronized void doStop() throws Exception {
        if (brokerService != null) {
            BrokerService temp = brokerService;
            brokerService = null;
            try {
                temp.stop();
            } catch (JMSException ignored) {
                // just a lame exception ActiveMQ likes to throw on shutdown
                if (!(ignored.getCause() instanceof TransportDisposedIOException)) {
                    throw ignored;
                }
            }
        }
    }

    public synchronized void doFail() {
        if (brokerService != null) {
            BrokerService temp = brokerService;
            brokerService = null;
            try {
                temp.stop();
            } catch (JMSException ignored) {
                // just a lame exception ActiveMQ likes to throw on shutdown
                if (!(ignored.getCause() instanceof TransportDisposedIOException)) {
                    log.warn("Caught while closing due to failure: " + ignored, ignored);
                }
            } catch (Exception e) {
                log.warn("Caught while closing due to failure: " + e, e);
            }
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder("ActiveMQ Message Broker", BrokerServiceGBeanImpl.class, "JMSServer");
        infoBuilder.addReference("serverInfo", ServerInfo.class);
        infoBuilder.addReference("mbeanServerReference", MBeanServerReference.class);
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);
        infoBuilder.addAttribute("brokerName", String.class, true);
        infoBuilder.addAttribute("brokerUri", String.class, true);
        infoBuilder.addAttribute("useShutdownHook", Boolean.TYPE, true);
        infoBuilder.addAttribute("dataDirectory", String.class, true);
        infoBuilder.addReference("dataSource", ResourceSource.class);
        infoBuilder.addAttribute("objectName", String.class, false);
        infoBuilder.addReference("manager", JMSManager.class);
        infoBuilder.addInterface(BrokerServiceGBean.class);
        // infoFactory.setConstructor(new String[]{"brokerName, brokerUri"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

	/**
	 * @return Returns the brokerName.
	 */
	public String getBrokerName() {
		return brokerName;
	}

    public String getBrokerUri() {
        return brokerUri;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public void setBrokerUri(String brokerUri) {
        this.brokerUri = brokerUri;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    public String getDataDirectory() {
        return dataDirectory;
    }

    public void setDataDirectory(String dataDir) {
        this.dataDirectory = dataDir;
    }

    public ResourceSource<ResourceException> getDataSource() {
        return dataSource;
    }

    public void setDataSource(ResourceSource<ResourceException> dataSource) {
        this.dataSource = dataSource;
    }

    public String getObjectName() {
        return objectName;
    }

    public boolean isStateManageable() {
        return true;
    }

    public boolean isStatisticsProvider() {
        return false; // todo: return true once stats are integrated
    }

    public boolean isEventProvider() {
        return true;
    }

    public NetworkConnector[] getConnectors() {
        return manager.getConnectorsForContainer(this);
    }

    public NetworkConnector[] getConnectors(String protocol) {
        return manager.getConnectorsForContainer(this, protocol);
    }

    public JMSManager getManager() {
        return manager;
    }

    public void setManager(JMSManager manager) {
        this.manager = manager;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
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

    public boolean isUseShutdownHook() {
        return useShutdownHook;
    }

    public void setUseShutdownHook(final boolean useShutdownHook) {
        this.useShutdownHook = useShutdownHook;
    }
}