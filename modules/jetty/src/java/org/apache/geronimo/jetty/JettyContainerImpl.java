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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.GBeanQuery;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.management.impl.Util;
import org.apache.geronimo.jetty.connector.AJP13Connector;
import org.apache.geronimo.jetty.connector.HTTPConnector;
import org.apache.geronimo.jetty.connector.HTTPSConnector;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.webservices.SoapHandler;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpListener;
import org.mortbay.http.RequestLog;
import org.mortbay.http.UserRealm;
import org.mortbay.jetty.Server;

/**
 * @version $Rev$ $Date$
 */
public class JettyContainerImpl implements JettyContainer, SoapHandler, GBeanLifecycle {
    private final static Log log = LogFactory.getLog(JettyContainerImpl.class);
    private final Server server;
    private final Map webServices = new HashMap();
    private Kernel kernel;
    private ObjectName myName;

    public JettyContainerImpl(Kernel kernel, String objectName) {
        server = new JettyServer();
        this.kernel = kernel;
        try {
            myName = ObjectName.getInstance(objectName);
        } catch (MalformedObjectNameException e) {
            log.error(e);
        }
    }

    public void resetStatistics() {
        server.statsReset();
    }

    public void setCollectStatistics(boolean on) {
        server.setStatsOn(on);
    }

    public boolean getCollectStatistics() {
        return server.getStatsOn();
    }

    public long getCollectStatisticsStarted() {
        return server.getStatsOnMs();
    }

    public int getConnections() {
        return server.getConnections();
    }

    public int getConnectionsOpen() {
        return server.getConnectionsOpen();
    }

    public int getConnectionsOpenMax() {
        return server.getConnectionsOpenMax();
    }

    public long getConnectionsDurationAve() {
        return server.getConnectionsDurationAve();
    }

    public long getConnectionsDurationMax() {
        return server.getConnectionsDurationMax();
    }

    public int getConnectionsRequestsAve() {
        return server.getConnectionsRequestsAve();
    }

    public int getConnectionsRequestsMax() {
        return server.getConnectionsRequestsMax();
    }

    public int getErrors() {
        return server.getErrors();
    }

    public int getRequests() {
        return server.getRequests();
    }

    public int getRequestsActive() {
        return server.getRequestsActive();
    }

    public int getRequestsActiveMax() {
        return server.getRequestsActiveMax();
    }

    public long getRequestsDurationAve() {
        return server.getRequestsDurationAve();
    }

    public long getRequestsDurationMax() {
        return server.getRequestsDurationMax();
    }

    public void addListener(HttpListener listener) {
        server.addListener(listener);
    }

    public void removeListener(HttpListener listener) {
        server.removeListener(listener);
    }

    public void addContext(HttpContext context) {
        server.addContext(context);
    }

    public void removeContext(HttpContext context) {
        server.removeContext(context);
    }

    public void addRealm(UserRealm realm) {
        server.addRealm(realm);
    }

    public void removeRealm(UserRealm realm) {
        server.removeRealm(realm.getName());
    }

    public void addWebService(String contextPath, String[] virtualHosts, WebServiceContainer webServiceContainer, String securityRealmName, String realmName, String transportGuarantee, String authMethod, ClassLoader classLoader) throws Exception {
        JettyEJBWebServiceContext webServiceContext = new JettyEJBWebServiceContext(contextPath, webServiceContainer, securityRealmName, realmName, transportGuarantee, authMethod, classLoader);
        webServiceContext.setHosts(virtualHosts);
        addContext(webServiceContext);
        webServiceContext.start();
        webServices.put(contextPath, webServiceContext);
    }

    public void removeWebService(String contextPath) {
        JettyEJBWebServiceContext webServiceContext = (JettyEJBWebServiceContext) webServices.remove(contextPath);
        removeContext(webServiceContext);
    }

    public void setRequestLog(RequestLog log) {
        server.setRequestLog(log);
    }

    /**
     * Gets the protocols which this container can configure connectors for.
     */
    public String[] getSupportedProtocols() {
        return new String[]{PROTOCOL_HTTP, PROTOCOL_HTTPS, PROTOCOL_AJP};
    }

    /**
     * Gets the ObjectNames of any existing connectors for the specified
     * protocol.
     *
     * @param protocol A protocol as returned by getSupportedProtocols
     */
    public String[] getConnectors(String protocol) {
        GBeanQuery query = new GBeanQuery(null, JettyWebConnector.class.getName());
        Set names = kernel.listGBeans(query);
        List result = new ArrayList();
        for (Iterator it = names.iterator(); it.hasNext();) {
            ObjectName name = (ObjectName) it.next();
            try {
                if (kernel.getAttribute(name, "protocol").equals(protocol)) {
                    result.add(name.getCanonicalName());
                }
            } catch (Exception e) {
                log.error("Unable to check the protocol for a connector", e);
            }
        }
        return (String[]) result.toArray(new String[result.size()]);
    }

    /**
     * Gets the ObjectNames of any existing connectors.
     */
    public String[] getConnectors() {
        GBeanQuery query = new GBeanQuery(null, JettyWebConnector.class.getName());
        Set names = kernel.listGBeans(query);
        String[] result = new String[names.size()];
        int i = 0;
        for (Iterator it = names.iterator(); it.hasNext();) {
            ObjectName name = (ObjectName) it.next();
            result[i++] = name.getCanonicalName();
        }
        return result;
    }

    /**
     * Creates a new connector, and returns the ObjectName for it.  Note that
     * the connector may well require further customization before being fully
     * functional (e.g. SSL settings for an HTTPS connector).
     */
    public String addConnector(String uniqueName, String protocol, String host, int port) {
        ObjectName name = getConnectorName(protocol, uniqueName);
        GBeanData connector;
        if (protocol.equals(PROTOCOL_HTTP)) {
            connector = new GBeanData(name, HTTPConnector.GBEAN_INFO);
        } else if (protocol.equals(PROTOCOL_HTTPS)) {
            connector = new GBeanData(name, HTTPSConnector.GBEAN_INFO);
            GBeanQuery query = new GBeanQuery(null, ServerInfo.class.getName());
            Set set = kernel.listGBeans(query);
            connector.setReferencePattern("ServerInfo", (ObjectName) set.iterator().next());
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
        connector.setReferencePattern("JettyContainer", myName);
        ObjectName config = Util.getConfiguration(kernel, myName);
        try {
            kernel.invoke(config, "addGBean", new Object[]{connector, Boolean.FALSE}, new String[]{GBeanData.class.getName(), boolean.class.getName()});
        } catch (Exception e) {
            log.error("Unable to add GBean ", e);
            return null;
        }
        return name.getCanonicalName();
    }

    private ObjectName getConnectorName(String protocol, String uniqueName) {
        Hashtable table = new Hashtable();
        table.put(NameFactory.J2EE_APPLICATION, myName.getKeyProperty(NameFactory.J2EE_APPLICATION));
        table.put(NameFactory.J2EE_SERVER, myName.getKeyProperty(NameFactory.J2EE_SERVER));
        table.put(NameFactory.J2EE_MODULE, myName.getKeyProperty(NameFactory.J2EE_MODULE));
        table.put(NameFactory.J2EE_TYPE, myName.getKeyProperty(NameFactory.J2EE_TYPE));
        table.put(NameFactory.J2EE_NAME, "JettyWebConnector-" + protocol + "-" + uniqueName);
        try {
            return ObjectName.getInstance(myName.getDomain(), table);
        } catch (MalformedObjectNameException e) {
            throw new IllegalStateException("Never should have failed: " + e.getMessage());
        }
    }

    /**
     * Removes a connector.  This shuts it down if necessary, and removes it
     * from the server environment.  It must be a connector that this container
     * is responsible for.
     */
    public void removeConnector(String objectName) {
        ObjectName name = null;
        try {
            name = ObjectName.getInstance(objectName);
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException("Invalid object name '" + objectName + "': " + e.getMessage());
        }
        try {
            GBeanInfo info = kernel.getGBeanInfo(name);
            boolean found = false;
            Set intfs = info.getInterfaces();
            for (Iterator it = intfs.iterator(); it.hasNext();) {
                String intf = (String) it.next();
                if (intf.equals(JettyWebConnector.class.getName())) {
                    found = true;
                }
            }
            if (!found) {
                throw new GBeanNotFoundException(name);
            }
            ObjectName config = Util.getConfiguration(kernel, name);
            kernel.invoke(config, "removeGBean", new Object[]{name}, new String[]{ObjectName.class.getName()});
        } catch (GBeanNotFoundException e) {
            log.warn("No such GBean '" + objectName + "'"); //todo: what if we want to remove a failed GBean?
        } catch (Exception e) {
            log.error(e);
        }
    }

    /* ------------------------------------------------------------ */
    public RequestLog getRequestLog() {
        return server.getRequestLog();
    }

    public void doStart() throws Exception {
        server.start();
    }

    public void doStop() {
        try {
            server.stop(true);
        } catch (InterruptedException e) {
        }
    }

    public void doFail() {
        try {
            server.stop(false);
        } catch (InterruptedException e) {
            // continue
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder("Jetty Web Container", JettyContainerImpl.class);
        infoBuilder.addAttribute("kernel", Kernel.class, false);
        infoBuilder.addAttribute("objectName", String.class, false);
        infoBuilder.addAttribute("collectStatistics", Boolean.TYPE, true);
        infoBuilder.addAttribute("collectStatisticsStarted", Long.TYPE, false);
        infoBuilder.addAttribute("connections", Integer.TYPE, false);
        infoBuilder.addAttribute("connectionsOpen", Integer.TYPE, false);
        infoBuilder.addAttribute("connectionsOpenMax", Integer.TYPE, false);
        infoBuilder.addAttribute("connectionsDurationAve", Long.TYPE, false);
        infoBuilder.addAttribute("connectionsDurationMax", Long.TYPE, false);
        infoBuilder.addAttribute("connectionsRequestsAve", Integer.TYPE, false);
        infoBuilder.addAttribute("connectionsRequestsMax", Integer.TYPE, false);
        infoBuilder.addAttribute("errors", Integer.TYPE, false);
        infoBuilder.addAttribute("requests", Integer.TYPE, false);
        infoBuilder.addAttribute("requestsActive", Integer.TYPE, false);
        infoBuilder.addAttribute("requestsActiveMax", Integer.TYPE, false);
        infoBuilder.addAttribute("requestsDurationAve", Long.TYPE, false);
        infoBuilder.addAttribute("requestsDurationMax", Long.TYPE, false);
        infoBuilder.addOperation("resetStatistics");

        infoBuilder.addAttribute("requestLog", RequestLog.class, false);

        infoBuilder.addOperation("addListener", new Class[]{HttpListener.class});
        infoBuilder.addOperation("removeListener", new Class[]{HttpListener.class});
        infoBuilder.addOperation("addContext", new Class[]{HttpContext.class});
        infoBuilder.addOperation("removeContext", new Class[]{HttpContext.class});
        infoBuilder.addOperation("addRealm", new Class[]{UserRealm.class});
        infoBuilder.addOperation("removeRealm", new Class[]{UserRealm.class});

        infoBuilder.addInterface(SoapHandler.class);
        infoBuilder.addInterface(JettyContainer.class);
        infoBuilder.setConstructor(new String[]{"kernel", "objectName"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
