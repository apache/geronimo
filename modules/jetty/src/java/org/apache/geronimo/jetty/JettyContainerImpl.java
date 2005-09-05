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

import java.util.HashMap;
import java.util.Map;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
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
    private final Server server;
    private final Map webServices = new HashMap();

    public JettyContainerImpl() {
        server = new JettyServer();
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
        infoBuilder.addAttribute("collectStatistics", Boolean.TYPE, true);
        //todo: Move all statistics methods to a Stats implementation
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
        infoBuilder.setConstructor(new String[]{});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
