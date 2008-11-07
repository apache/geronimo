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

import java.io.File;
import java.net.URI;
import java.util.Properties;

import javax.jms.JMSException;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.jmx.ManagementContext;
import org.apache.activemq.transport.TransportDisposedIOException;
import org.apache.activemq.xbean.BrokerFactoryBean;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.management.geronimo.JMSManager;
import org.apache.geronimo.management.geronimo.NetworkConnector;
import org.apache.geronimo.system.jmx.MBeanServerReference;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

/**
 * Default implementation of the ActiveMQ Message Server
 *
 * @version $Rev$ $Date$
 */
@GBean (j2eeType="JMSServer") 
public class BrokerServiceGBeanImpl implements GBeanLifecycle {

    private static final Logger log = LoggerFactory.getLogger(BrokerServiceGBeanImpl.class);

    private final BrokerService brokerService;
//    private ResourceSource<ResourceException> dataSource;
    private JMSManager manager;

    public BrokerServiceGBeanImpl(@ParamAttribute (name="amqBaseDir") URI amqBaseDir, 
                                  @ParamAttribute (name="amqDataDir") String amqDataDir, 
                                  @ParamAttribute (name="amqConfigFile") String amqConfigFile, 
                                  @ParamAttribute (name="useShutdownHook") boolean useShutdownHook, 
                                  @ParamReference (name="ServerInfo") ServerInfo serverInfo, 
                                  @ParamReference (name="MBeanServerReference") MBeanServerReference mbeanServerReference, 
                                  @ParamSpecial (type=SpecialAttributeType.classLoader) ClassLoader classLoader) 
        throws Exception {
        
        
        URI baseDir = serverInfo.resolveServer(amqBaseDir);
        URI dataDir = baseDir.resolve(amqDataDir);
        URI amqConfigUri = baseDir.resolve(amqConfigFile);
        
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            BrokerFactoryBean brokerFactory = new BrokerFactoryBean(
                    new FileSystemResource(new File(amqConfigUri)));
            System.setProperty("activemq.home", new File(baseDir).toString());
            System.setProperty("activemq.data", new File(dataDir).toString());
            brokerFactory.afterPropertiesSet();
            brokerService = brokerFactory.getBroker();
//            brokerService = BrokerFactory.createBroker(new URI(brokerUri));
            
            // Do not allow creation of another ConnectorServer
            ManagementContext mgmtctx = new ManagementContext(mbeanServerReference != null ? mbeanServerReference.getMBeanServer() : null);
            mgmtctx.setCreateConnector(false);
            brokerService.setManagementContext(mgmtctx);

            // Do not allow the broker to use a shutdown hook, the kernel will stop it
            brokerService.setUseShutdownHook(useShutdownHook);

            // Setup the persistence adapter to use the right datasource and directory
//            DefaultPersistenceAdapterFactory persistenceFactory = (DefaultPersistenceAdapterFactory) brokerService.getPersistenceFactory();
//            persistenceFactory.setDataDirectoryFile(serverInfo.resolveServer(dataDirectory));
//            persistenceFactory.setDataSource((DataSource) dataSource.getResource());

            brokerService.start();
        }
        finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    public synchronized BrokerService getBrokerContainer() {
        return brokerService;
    }

    public synchronized void doStart() throws Exception {
      
    }

    public synchronized void doStop() throws Exception {
        brokerService.stop();
        brokerService.waitUntilStopped();
    }

    public synchronized void doFail() {
            try {
                brokerService.stop();
                brokerService.waitUntilStopped();
            } catch (JMSException ignored) {
                // just a lame exception ActiveMQ likes to throw on shutdown
                if (!(ignored.getCause() instanceof TransportDisposedIOException)) {
                    log.warn("Caught while closing due to failure: " + ignored, ignored);
                }
            } catch (Exception e) {
                log.warn("Caught while closing due to failure: " + e, e);
            }
        }

//    public ResourceSource<ResourceException> getDataSource() {
//        return dataSource;
//    }
//
//    public void setDataSource(ResourceSource<ResourceException> dataSource) {
//        this.dataSource = dataSource;
//    }

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

}