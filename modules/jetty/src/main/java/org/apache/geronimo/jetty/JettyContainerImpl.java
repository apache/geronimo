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

import javax.management.j2ee.statistics.Stats;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.management.StatisticsProvider;
import org.apache.geronimo.management.geronimo.NetworkConnector;
import org.apache.geronimo.management.geronimo.WebManager;
import org.apache.geronimo.webservices.SoapHandler;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpListener;
import org.mortbay.http.RequestLog;
import org.mortbay.jetty.Server;

/**
 * @version $Rev$ $Date$
 */
public class JettyContainerImpl implements JettyContainer, SoapHandler, GBeanLifecycle, StatisticsProvider {
    private final Server server;
    private final Map webServices = new HashMap();
    private final String objectName;
    private final WebManager manager;
    private JettyWebContainerStatsImpl stats;
    private final Map realms = new HashMap();

    public JettyContainerImpl(String objectName, WebManager manager) {
        this.objectName = objectName;
        server = new JettyServer();
        stats = new JettyWebContainerStatsImpl();
        this.manager = manager;
    }

    public String getObjectName() {
        return objectName;
    }

    public boolean isStateManageable() {
        return true;
    }

    public boolean isStatisticsProvider() {
        return true;
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

    public void resetStatistics() {
        server.statsReset();
    }

    public void setCollectStatistics(boolean on) {
        server.setStatsOn(on);
        stats.setStatsOn(on);
    }

    public boolean getCollectStatistics() {
        return server.getStatsOn();
    }

    public long getCollectStatisticsStarted() {
        return server.getStatsOnMs();
    }

    public Stats getStats() {
        if (getCollectStatistics()) {

            /* set active request count */
            stats.getTotalRequestCountImpl().setCount(server.getRequests());

            /* set total connection count */
            stats.getTotalConnectionCountImpl().setCount(server.getConnections());

            /* set total error count */
            stats.getTotalErrorCountImpl().setCount(server.getErrors());

            /* set active request range values */
            stats.getActiveRequestCountImpl().setCurrent(server.getRequestsActive());
            stats.getActiveRequestCountImpl().setLowWaterMark(server.getRequestsActiveMin());
            stats.getActiveRequestCountImpl().setHighWaterMark(server.getRequestsActiveMax());

            /* set connection requests range values */
//          stats.getConnectionRequestCountImpl().setCurrent(server.getConnectionsRequestsCurrent());    // temporarily removed until added by jetty
            stats.getConnectionRequestCountImpl().setCurrent(server.getConnectionsOpen());
            stats.getConnectionRequestCountImpl().setLowWaterMark(server.getConnectionsRequestsMin());
            stats.getConnectionRequestCountImpl().setHighWaterMark(server.getConnectionsRequestsMax());

            /* set open connection range values */
            stats.getOpenConnectionCountImpl().setCurrent(server.getConnectionsOpen());
            stats.getOpenConnectionCountImpl().setLowWaterMark(server.getConnectionsOpenMin());
            stats.getOpenConnectionCountImpl().setHighWaterMark(server.getConnectionsOpenMax());

            /* set request duration time values */
            stats.getRequestDurationImpl().setMinTime(server.getRequestsDurationMin());
            stats.getRequestDurationImpl().setMaxTime(server.getRequestsDurationMax());
//          stats.getRequestDurationImpl().setCount(server.getRequestsDurationCount());     // temporarily removed until added by jetty
            stats.getRequestDurationImpl().setCount(stats.getTotalRequestCount().getCount());
            stats.getRequestDurationImpl().setTotalTime(server.getRequestsDurationTotal());

            /* set connection duration Time values */
            stats.getConnectionDurationImpl().setMinTime(server.getConnectionsDurationMin());
            stats.getConnectionDurationImpl().setMaxTime(server.getConnectionsDurationMax());
//          stats.getConnectionDurationImpl().setCount(server.getConnectionsDurationCount());    // temporarily removed until added by jetty
            stats.getConnectionDurationImpl().setCount(stats.getTotalConnectionCount().getCount());
            stats.getConnectionDurationImpl().setTotalTime(server.getConnectionsDurationTotal());

        } else {
            // should probably set the stats object to all zero/null values to avoid unpredicable results
        }
        return stats;
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

    public InternalJAASJettyRealm addRealm(String realmName) {
        InternalJAASJettyRealm realm = (InternalJAASJettyRealm) realms.get(realmName);
        if (realm == null) {
            realm = new InternalJAASJettyRealm(realmName);
            realms.put(realmName, realm);
        } else {
            realm.addUse();
        }
        return realm;
    }

    public void removeRealm(String realmName) {
        InternalJAASJettyRealm realm = (InternalJAASJettyRealm) realms.get(realmName);
        if (realm != null) {
            if (realm.removeUse() == 0){
                realms.remove(realmName);
            }
        }
    }

    public void addWebService(String contextPath, String[] virtualHosts, WebServiceContainer webServiceContainer, String securityRealmName, String realmName, String transportGuarantee, String authMethod, ClassLoader classLoader) throws Exception {
        InternalJAASJettyRealm internalJAASJettyRealm = securityRealmName == null? null:addRealm(securityRealmName);
        JettyEJBWebServiceContext webServiceContext = new JettyEJBWebServiceContext(contextPath, webServiceContainer, internalJAASJettyRealm, realmName, transportGuarantee, authMethod, classLoader);
        webServiceContext.setHosts(virtualHosts);
        addContext(webServiceContext);
        webServiceContext.start();
        webServices.put(contextPath, webServiceContext);
     }

    public void removeWebService(String contextPath) {
        JettyEJBWebServiceContext webServiceContext = (JettyEJBWebServiceContext) webServices.remove(contextPath);
        String securityRealmName = webServiceContext.getSecurityRealmName();
        if (securityRealmName != null) {
            removeRealm(securityRealmName);
        }
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
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic("Jetty Web Container", JettyContainerImpl.class);
        infoBuilder.addAttribute("collectStatistics", Boolean.TYPE, true);
        infoBuilder.addAttribute("collectStatisticsStarted", Long.TYPE, false);
        infoBuilder.addOperation("resetStatistics");

        infoBuilder.addAttribute("requestLog", RequestLog.class, false, false);

        infoBuilder.addOperation("addListener", new Class[]{HttpListener.class});
        infoBuilder.addOperation("removeListener", new Class[]{HttpListener.class});
        infoBuilder.addOperation("addContext", new Class[]{HttpContext.class});
        infoBuilder.addOperation("removeContext", new Class[]{HttpContext.class});
        infoBuilder.addOperation("addRealm", new Class[]{String.class});
        infoBuilder.addOperation("removeRealm", new Class[]{String.class});

        infoBuilder.addAttribute("objectName", String.class, false);
        infoBuilder.addReference("WebManager", WebManager.class);

        infoBuilder.addInterface(SoapHandler.class);
        infoBuilder.addInterface(JettyContainer.class);
        infoBuilder.addInterface(StatisticsProvider.class);
        infoBuilder.setConstructor(new String[]{"objectName", "WebManager"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
