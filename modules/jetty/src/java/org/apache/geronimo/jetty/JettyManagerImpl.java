/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.jetty;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.jetty.connector.AJP13Connector;
import org.apache.geronimo.jetty.connector.HTTPConnector;
import org.apache.geronimo.jetty.connector.HTTPSConnector;
import org.apache.geronimo.jetty.connector.JettyConnector;
import org.apache.geronimo.jetty.requestlog.JettyLogManager;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.EditableConfigurationManager;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.management.geronimo.WebManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Collections;

/**
 * Jetty implementation of WebManager.  Knows how to manipulate
 * other Jetty objects for management purposes.
 *
 * @version $Rev:386276 $ $Date$
 */
public class JettyManagerImpl implements WebManager {
    private final static Log log = LogFactory.getLog(JettyManagerImpl.class);
    private final Kernel kernel;

    public JettyManagerImpl(Kernel kernel) {
        this.kernel = kernel;
    }

    public String getProductName() {
        return "Jetty";
    }

    /**
     * Creates a new connector, and returns the ObjectName for it.  Note that
     * the connector may well require further customization before being fully
     * functional (e.g. SSL settings for an HTTPS connector).
     */
    public AbstractName addConnector(AbstractName containerName, String uniqueName, String protocol, String host, int port) {
        AbstractName name = kernel.getNaming().createChildName(containerName, "JettyWebConnector-" + protocol + "-" + uniqueName, NameFactory.GERONIMO_SERVICE);
        GBeanData connector;
        if (protocol.equals(PROTOCOL_HTTP)) {
            connector = new GBeanData(name, HTTPConnector.GBEAN_INFO);
        } else if (protocol.equals(PROTOCOL_HTTPS)) {
            connector = new GBeanData(name, HTTPSConnector.GBEAN_INFO);
            AbstractNameQuery query = new AbstractNameQuery(null, Collections.singletonMap(NameFactory.J2EE_TYPE, "ServerInfo"));
            connector.setReferencePattern("ServerInfo", query);
            //todo: default HTTPS settings
        } else if (protocol.equals(PROTOCOL_AJP)) {
            connector = new GBeanData(name, AJP13Connector.GBEAN_INFO);
        } else {
            throw new IllegalArgumentException("Invalid protocol '" + protocol + "'");
        }
        connector.setAttribute("host", host);
        connector.setAttribute("port", new Integer(port));
        connector.setAttribute("minThreads", new Integer(10));
        connector.setAttribute("maxThreads", new Integer(50));
        connector.setReferencePattern(JettyConnector.CONNECTOR_CONTAINER_REFERENCE, containerName);
        EditableConfigurationManager mgr = ConfigurationUtil.getEditableConfigurationManager(kernel);
        if(mgr != null) {
            try {
                mgr.addGBeanToConfiguration(containerName.getArtifact(), connector, false);
                return name;
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

    /**
     * Get a list of containers for this web implementation.
     */
    public AbstractName[] getContainers() {
        AbstractNameQuery query = new AbstractNameQuery(JettyContainer.class.getName());
        Set names = kernel.listGBeans(query);
        return (AbstractName[]) names.toArray(new AbstractName[names.size()]);
    }

    /**
     * Gets the protocols that this web container supports (that you can create
     * connectors for).
     */
    public String[] getSupportedProtocols() {
        return new String[]{PROTOCOL_HTTP, PROTOCOL_HTTPS, PROTOCOL_AJP};
    }

    /**
     * Removes a connector.  This shuts it down if necessary, and removes it
     * from the server environment.  It must be a connector that this container
     * is responsible for.
     * @param connectorName
     */
    public void removeConnector(AbstractName connectorName) {
        try {
            GBeanInfo info = kernel.getGBeanInfo(connectorName);
            boolean found = false;
            Set intfs = info.getInterfaces();
            for (Iterator it = intfs.iterator(); it.hasNext();) {
                String intf = (String) it.next();
                if (intf.equals(JettyWebConnector.class.getName())) {
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
     * Gets the ObjectNames of any existing connectors for the specified
     * protocol.
     *
     * @param protocol A protocol as returned by getSupportedProtocols
     */
    public AbstractName[] getConnectors(String protocol) {
        AbstractNameQuery query = new AbstractNameQuery(JettyWebConnector.class.getName());
        Set names = kernel.listGBeans(query);
        List result = new ArrayList();
        for (Iterator it = names.iterator(); it.hasNext();) {
            AbstractName name = (AbstractName) it.next();
            try {
                if (kernel.getAttribute(name, "protocol").equals(protocol)) {
                    result.add(name);
                }
            } catch (Exception e) {
                log.error("Unable to check the protocol for a connector", e);
            }
        }
        return (AbstractName[]) result.toArray(new AbstractName[result.size()]);
    }

    public AbstractName getAccessLog(String containerObjectName) {
        AbstractNameQuery query = new AbstractNameQuery(JettyLogManager.class.getName());
        Set names = kernel.listGBeans(query);
        if(names.size() == 0) {
            return null;
        } else if(names.size() > 1) {
            throw new IllegalStateException("Should not be more than one Jetty access log manager");
        }
        return (AbstractName)names.iterator().next();
    }

    /**
     * Gets the ObjectNames of any existing connectors.
     */
    public AbstractName[] getConnectors() {
        AbstractNameQuery query = new AbstractNameQuery(JettyWebConnector.class.getName());
        Set names = kernel.listGBeans(query);
        return (AbstractName[]) names.toArray(new AbstractName[names.size()]);
    }

    public AbstractName[] getConnectorsForContainer(AbstractName containerName, String protocol) {
        if(protocol == null) {
            return getConnectorsForContainer(containerName);
        }
        try {
            List results = new ArrayList();
            AbstractNameQuery query = new AbstractNameQuery(JettyWebConnector.class.getName());
            Set set = kernel.listGBeans(query); // all Jetty connectors
            for (Iterator it = set.iterator(); it.hasNext();) {
                AbstractName name = (AbstractName) it.next(); // a single Jetty connector
                GBeanData data = kernel.getGBeanData(name);
                ReferencePatterns refs = data.getReferencePatterns(JettyConnector.CONNECTOR_CONTAINER_REFERENCE);
                //TODO configid need to verify that the refpattern is resolved
                if(containerName.equals(refs.getAbstractName())) {
                    try {
                        String testProtocol = (String) kernel.getAttribute(name, "protocol");
                        if(testProtocol != null && testProtocol.equals(protocol)) {
                            results.add(name);
                        }
                    } catch (Exception e) {
                        log.error("Unable to look up protocol for connector '"+name+"'",e);
                    }
                    break;
                }
            }
            return (AbstractName[]) results.toArray(new AbstractName[results.size()]);
        } catch (Exception e) {
            throw (IllegalArgumentException)new IllegalArgumentException("Unable to look up connectors for Jetty container '"+containerName +"': ").initCause(e);
        }
    }

    public AbstractName[] getConnectorsForContainer(AbstractName containerName) {
        try {
            List results = new ArrayList();
            AbstractNameQuery query = new AbstractNameQuery(JettyWebConnector.class.getName());
            Set set = kernel.listGBeans(query); // all Jetty connectors
            for (Iterator it = set.iterator(); it.hasNext();) {
                AbstractName name = (AbstractName) it.next(); // a single Jetty connector
                GBeanData data = kernel.getGBeanData(name);
                ReferencePatterns refs = data.getReferencePatterns(JettyConnector.CONNECTOR_CONTAINER_REFERENCE);
                //TODO configid need to verify that the refpattern is resolved
                if (containerName.equals(refs.getAbstractName())) {
                    results.add(name);
                }
            }
            return (AbstractName[]) results.toArray(new AbstractName[results.size()]);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to look up connectors for Jetty container '"+containerName +"': "+e);
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("Jetty Web Manager", JettyManagerImpl.class);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addInterface(WebManager.class);
        infoFactory.setConstructor(new String[] {"kernel"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
