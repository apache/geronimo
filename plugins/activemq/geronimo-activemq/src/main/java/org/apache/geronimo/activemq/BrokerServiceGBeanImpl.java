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
import java.util.concurrent.atomic.AtomicBoolean;

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
public class BrokerServiceGBeanImpl implements BrokerServiceGBean, GBeanLifecycle {

    private static final Logger log = LoggerFactory.getLogger(BrokerServiceGBeanImpl.class);

    private final String objectName;
    private final BrokerService brokerService;
    private final boolean asyncStartup;
    private final AtomicBoolean active = new AtomicBoolean(false);
    private Thread asyncStartThread;
    private JMSManager manager;

    public BrokerServiceGBeanImpl(@ParamAttribute (name="brokerName") String brokerName,
                                  @ParamAttribute (name="amqBaseDir") URI amqBaseDir, 
                                  @ParamAttribute (name="amqDataDir") String amqDataDir, 
                                  @ParamAttribute (name="amqConfigFile") String amqConfigFile, 
                                  @ParamAttribute (name="useShutdownHook") boolean useShutdownHook,
                                  @ParamAttribute (name="asyncStartup") boolean asyncStartup,
                                  @ParamReference (name="ServerInfo") ServerInfo serverInfo,
                                  @ParamReference (name="MBeanServerReference") MBeanServerReference mbeanServerReference,
                                  @ParamSpecial (type= SpecialAttributeType.objectName) String objectName,
                                  @ParamSpecial (type=SpecialAttributeType.classLoader) ClassLoader classLoader) 
        throws Exception {
        
        this.objectName = objectName;
        this.asyncStartup = asyncStartup;
        URI baseDir = serverInfo.resolveServer(amqBaseDir);
        URI dataDir = baseDir.resolve(amqDataDir);
        URI amqConfigUri = baseDir.resolve(amqConfigFile);
        
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            BrokerFactoryBean brokerFactory = new BrokerFactoryBean(
                    new FileSystemResource(new File(amqConfigUri)));
            //TODO There should be a better way to avoid the concurrent broker creations
            synchronized (BrokerServiceGBeanImpl.class) {
                System.setProperty("activemq.brokerName", brokerName);
                System.setProperty("activemq.home", new File(baseDir).toString());
                System.setProperty("activemq.data", new File(dataDir).toString());
                
                File geronimoHomeURL = serverInfo.resolveServer("./");
                System.setProperty("activemq.geronimo.home.url", geronimoHomeURL.toURI().toURL().toString());
                brokerFactory.afterPropertiesSet();                
            }
            brokerService = brokerFactory.getBroker();
//            brokerService = BrokerFactory.createBroker(new URI(brokerUri));
            
            // Do not allow creation of another ConnectorServer
            ManagementContext mgmtctx = new ManagementContext(mbeanServerReference != null ? mbeanServerReference.getMBeanServer() : null);
            mgmtctx.setCreateConnector(false);
            brokerService.setManagementContext(mgmtctx);

            // Do not allow the broker to use a shutdown hook, the kernel will stop it
            brokerService.setUseShutdownHook(useShutdownHook);
        }
        finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    public synchronized BrokerService getBrokerContainer() {
        return brokerService;
    }

    public String getBrokerName() {
        return brokerService.getBrokerName();
    }

    public synchronized void doStart() throws Exception {
        if (!asyncStartup) {
            brokerService.start();
        } else {
            active.set(true);
            asyncStartThread = new Thread(new Runnable() {
                public void run() {
                    asyncStart();
                }
            }, "AsyncStartThread-" + getBrokerName());
            asyncStartThread.start();
        }
    }

    public synchronized void doStop() throws Exception {
        if (asyncStartup) {
            active.set(false);
        }
        brokerService.stop();
        brokerService.waitUntilStopped();
    }

    public synchronized void doFail() {
        if (asyncStartup) {
            active.set(false);
        }        
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

    private void asyncStart() {
        while (active.get()) {
            try {
                brokerService.start();
                log.info("brokerService started");
                brokerService.waitUntilStopped();
                if (active.get()) {
                    log.warn("brokerService stopped");
                }
            } catch (Exception e) {
                log.warn("brokerService start failed: " + e, e);
            }
        }

    }

    /**
     * Gets the unique name of this object.  The object name must comply with the ObjectName specification
     * in the JMX specification and the restrictions in the J2EEManagementInterface.
     *
     * @return the unique name of this object within the server
     */
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

}