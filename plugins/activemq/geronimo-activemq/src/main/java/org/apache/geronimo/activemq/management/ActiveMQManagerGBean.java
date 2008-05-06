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

package org.apache.geronimo.activemq.management;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.geronimo.activemq.ActiveMQBroker;
import org.apache.geronimo.activemq.ActiveMQConnector;
import org.apache.geronimo.activemq.ActiveMQManager;
import org.apache.geronimo.activemq.TransportConnectorGBeanImpl;
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
import org.apache.geronimo.management.geronimo.JMSBroker;
import org.apache.geronimo.management.geronimo.JMSConnector;
import org.apache.geronimo.management.geronimo.NetworkConnector;

/**
 * Implementation of the ActiveMQ management interface.  These are the ActiveMQ
 * management features available at runtime.
 *
 * @version $Rev$ $Date$
 */
public class ActiveMQManagerGBean implements ActiveMQManager {
    private static final Logger log = LoggerFactory.getLogger(ActiveMQManagerGBean.class);
    private Kernel kernel;
    private String objectName;

    public ActiveMQManagerGBean(Kernel kernel, String objectName) {
        this.kernel = kernel;
        this.objectName = objectName;
    }

    public String getProductName() {
        return "ActiveMQ";
    }

    public String getObjectName() {
        return objectName;
    }

    public boolean isEventProvider() {
        return false;
    }

    public boolean isStateManageable() {
        return true;
    }

    public boolean isStatisticsProvider() {
        return false;
    }

    public Object[] getContainers() {
        ProxyManager proxyManager = kernel.getProxyManager();
        AbstractNameQuery query = new AbstractNameQuery(ActiveMQBroker.class.getName());
        Set names = kernel.listGBeans(query);
        ActiveMQBroker[] results = new ActiveMQBroker[names.size()];
        int i=0;
        for (Iterator it = names.iterator(); it.hasNext(); i++) {
            AbstractName name = (AbstractName) it.next();
            results[i] = (ActiveMQBroker) proxyManager.createProxy(name, ActiveMQBroker.class.getClassLoader());
        }
        return results;
    }

    public String[] getSupportedProtocols() {
        // see files in modules/core/src/conf/META-INF/services/org/activemq/transport/server/
        return new String[]{ "tcp", "stomp", "vm", "peer", "udp", "multicast", "failover"};
    }

    public NetworkConnector[] getConnectors() {
        ProxyManager proxyManager = kernel.getProxyManager();
        AbstractNameQuery query = new AbstractNameQuery(ActiveMQConnector.class.getName());
        Set names = kernel.listGBeans(query);
        ActiveMQConnector[] results = new ActiveMQConnector[names.size()];
        int i=0;
        for (Iterator it = names.iterator(); it.hasNext(); i++) {
            AbstractName name = (AbstractName) it.next();
            results[i] = (ActiveMQConnector) proxyManager.createProxy(name, ActiveMQConnector.class.getClassLoader());
        }
        return results;
    }

    public NetworkConnector[] getConnectors(String protocol) {
        if(protocol == null) {
            return getConnectors();
        }
        List result = new ArrayList();
        ProxyManager proxyManager = kernel.getProxyManager();
        AbstractNameQuery query = new AbstractNameQuery(ActiveMQConnector.class.getName());
        Set names = kernel.listGBeans(query);
        for (Iterator it = names.iterator(); it.hasNext();) {
            AbstractName name = (AbstractName) it.next();
            try {
                if (kernel.getAttribute(name, "protocol").equals(protocol)) {
                    result.add(proxyManager.createProxy(name, ActiveMQConnector.class.getClassLoader()));
                }
            } catch (Exception e) {
                log.error("Unable to check the protocol for a connector", e);
            }
        }
        return (ActiveMQConnector[]) result.toArray(new ActiveMQConnector[names.size()]);
    }

    public NetworkConnector[] getConnectorsForContainer(Object broker) {
        AbstractName containerName = kernel.getAbstractNameFor(broker);
        ProxyManager mgr = kernel.getProxyManager();
        try {
            List results = new ArrayList();
            AbstractNameQuery query = new AbstractNameQuery(ActiveMQConnector.class.getName());
            Set set = kernel.listGBeans(query); // all Jetty connectors
            for (Iterator it = set.iterator(); it.hasNext();) {
                AbstractName name = (AbstractName) it.next(); // a single Jetty connector
                GBeanData data = kernel.getGBeanData(name);
                ReferencePatterns refs = data.getReferencePatterns("brokerService");
                if (containerName.equals(refs.getAbstractName())) {
                    results.add(mgr.createProxy(name, ActiveMQConnector.class.getClassLoader()));
                }
            }
            return (ActiveMQConnector[]) results.toArray(new ActiveMQConnector[results.size()]);
        } catch (Exception e) {
            throw (IllegalArgumentException) new IllegalArgumentException("Unable to look up connectors for ActiveMQ broker '"+containerName).initCause(e);
        }
    }

    public NetworkConnector[] getConnectorsForContainer(Object broker, String protocol) {
        if(protocol == null) {
            return getConnectorsForContainer(broker);
        }
        AbstractName containerName = kernel.getAbstractNameFor(broker);
        ProxyManager mgr = kernel.getProxyManager();
        try {
            List results = new ArrayList();
            AbstractNameQuery query = new AbstractNameQuery(ActiveMQConnector.class.getName());
            Set set = kernel.listGBeans(query); // all Jetty connectors
            for (Iterator it = set.iterator(); it.hasNext();) {
                AbstractName name = (AbstractName) it.next(); // a single Jetty connector
                GBeanData data = kernel.getGBeanData(name);
                ReferencePatterns refs = data.getReferencePatterns("brokerService");
                if(containerName.equals(refs.getAbstractName())) {
                    try {
                        String testProtocol = (String) kernel.getAttribute(name, "protocol");
                        if(testProtocol != null && testProtocol.equals(protocol)) {
                            results.add(mgr.createProxy(name, ActiveMQConnector.class.getClassLoader()));
                        }
                    } catch (Exception e) {
                        log.error("Unable to look up protocol for connector '"+name+"'",e);
                    }
                    break;
                }
            }
            return (ActiveMQConnector[]) results.toArray(new ActiveMQConnector[results.size()]);
        } catch (Exception e) {
            throw (IllegalArgumentException)new IllegalArgumentException("Unable to look up connectors for ActiveMQ broker '"+containerName +"': ").initCause(e);
        }
    }

    /**
     * Returns a new JMSConnector.  Note that
     * the connector may well require further customization before being fully
     * functional (e.g. SSL settings for a secure connector).
     */
    public JMSConnector addConnector(JMSBroker broker, String uniqueName, String protocol, String host, int port) {
        AbstractName brokerAbstractName = kernel.getAbstractNameFor(broker);
        AbstractName name = kernel.getNaming().createChildName(brokerAbstractName, uniqueName, NameFactory.GERONIMO_SERVICE);
        GBeanData connector = new GBeanData(name, TransportConnectorGBeanImpl.GBEAN_INFO);
        //todo: if SSL is supported, need to add more properties or use a different GBean?
        connector.setAttribute("protocol", protocol);
        connector.setAttribute("host", host);
        connector.setAttribute("port", new Integer(port));
        connector.setReferencePattern("brokerService", brokerAbstractName);
        EditableConfigurationManager mgr = ConfigurationUtil.getEditableConfigurationManager(kernel);
        if(mgr != null) {
            try {
                mgr.addGBeanToConfiguration(brokerAbstractName.getArtifact(), connector, false);
                return (JMSConnector) kernel.getProxyManager().createProxy(name, ActiveMQConnector.class.getClassLoader());
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

    public void removeConnector(AbstractName connectorName) {
        try {
            GBeanInfo info = kernel.getGBeanInfo(connectorName);
            boolean found = false;
            Set intfs = info.getInterfaces();
            for (Iterator it = intfs.iterator(); it.hasNext();) {
                String intf = (String) it.next();
                if (intf.equals(ActiveMQConnector.class.getName())) {
                    found = true;
                }
            }
            if (!found) {
                throw new GBeanNotFoundException(connectorName);
            }
            EditableConfigurationManager mgr = ConfigurationUtil.getEditableConfigurationManager(kernel);
            if (mgr != null) {
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
            log.error("Failed to remove connector", e);
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("ActiveMQ Manager", ActiveMQManagerGBean.class);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addInterface(ActiveMQManager.class);
        infoFactory.setConstructor(new String[]{"kernel", "objectName"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
