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

package org.apache.geronimo.jetty6;

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
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.RequestLog;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.handler.RequestLogHandler;

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
    private HandlerCollection handlerCollection = new HandlerCollection();
    private ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection();
    private DefaultHandler defaultHandler = new DefaultHandler();
    private RequestLogHandler requestLogHandler = new RequestLogHandler();

    public JettyContainerImpl(String objectName, WebManager manager) {
        this.objectName = objectName;
        server = new JettyServer();

        //set up the new jetty6 handler structure which is to have a HandlerCollection,
        //each element of which is always tried on each request.
        //The first element of the HandlerCollection is a
        //ContextHandlerCollection, which is itself is a collection
        //of Handlers. It's special property is that only of it's
        //handlers will respond to a request.
        //The second element of the HandlerCollection is a DefaultHandler
        //which is responsible for serving static content or anything not
        //handled by a Handler in the ContextHandlerCollection.
        //The third element is the RequestLogHandler, which requires
        //a RequestLog impl to be set.
        Handler[] handlers = new Handler[3];
        handlers[0] = contextHandlerCollection;
        handlers[1] = defaultHandler;
        handlers[2] = requestLogHandler;
        handlerCollection.setHandlers(handlers);
        server.setHandler(handlerCollection);

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
        //TODO: for jetty6
    }

    public void setCollectStatistics(boolean on) {
        //TODO: for jetty6
    }

    public boolean getCollectStatistics() {
        //TODO: for jetty6
        return false;
    }

    public long getCollectStatisticsStarted() {
        //TODO: for jetty6
        return 0L;
    }

    public Stats getStats() {
        if (getCollectStatistics()) {

            /* set active request count */
//            stats.getTotalRequestCountImpl().setCount(server.getRequests());

            /* set total connection count */
//            stats.getTotalConnectionCountImpl().setCount(server.getConnections());

            /* set total error count */
//            stats.getTotalErrorCountImpl().setCount(server.getErrors());

            /* set active request range values */
//            stats.getActiveRequestCountImpl().setCurrent(server.getRequestsActive());
//            stats.getActiveRequestCountImpl().setLowWaterMark(server.getRequestsActiveMin());
//            stats.getActiveRequestCountImpl().setHighWaterMark(server.getRequestsActiveMax());

            /* set connection requests range values */
//          stats.getConnectionRequestCountImpl().setCurrent(server.getConnectionsRequestsCurrent());    // temporarily removed until added by jetty6
//            stats.getConnectionRequestCountImpl().setCurrent(server.getConnectionsOpen());
//            stats.getConnectionRequestCountImpl().setLowWaterMark(server.getConnectionsRequestsMin());
//            stats.getConnectionRequestCountImpl().setHighWaterMark(server.getConnectionsRequestsMax());

            /* set open connection range values */
//            stats.getOpenConnectionCountImpl().setCurrent(server.getConnectionsOpen());
//            stats.getOpenConnectionCountImpl().setLowWaterMark(server.getConnectionsOpenMin());
//            stats.getOpenConnectionCountImpl().setHighWaterMark(server.getConnectionsOpenMax());

            /* set request duration time values */
//            stats.getRequestDurationImpl().setMinTime(server.getRequestsDurationMin());
//            stats.getRequestDurationImpl().setMaxTime(server.getRequestsDurationMax());
//          stats.getRequestDurationImpl().setCount(server.getRequestsDurationCount());     // temporarily removed until added by jetty6
            stats.getRequestDurationImpl().setCount(stats.getTotalRequestCount().getCount());
//            stats.getRequestDurationImpl().setTotalTime(server.getRequestsDurationTotal());

            /* set connection duration Time values */
//            stats.getConnectionDurationImpl().setMinTime(server.getConnectionsDurationMin());
//            stats.getConnectionDurationImpl().setMaxTime(server.getConnectionsDurationMax());
//          stats.getConnectionDurationImpl().setCount(server.getConnectionsDurationCount());    // temporarily removed until added by jetty6
            stats.getConnectionDurationImpl().setCount(stats.getTotalConnectionCount().getCount());
//            stats.getConnectionDurationImpl().setTotalTime(server.getConnectionsDurationTotal());

        } else {
            // should probably set the stats object to all zero/null values to avoid unpredicable results
        }
        return stats;
    }

    public void addListener(Connector listener) {
        server.addConnector(listener);
    }

    public void removeListener(Connector listener) {
        server.removeConnector(listener);
    }

    public void addContext(ContextHandler context) {
        contextHandlerCollection.addHandler(context);
    }

    public void removeContext(ContextHandler context) {
        contextHandlerCollection.removeHandler(context);
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
            if (realm.removeUse() == 0) {
                realms.remove(realmName);
            }
        }
    }

    public void addWebService(String contextPath, String[] virtualHosts, WebServiceContainer webServiceContainer, String securityRealmName, String realmName, String transportGuarantee, String authMethod, ClassLoader classLoader) throws Exception {
        InternalJAASJettyRealm internalJAASJettyRealm = securityRealmName == null ? null : addRealm(securityRealmName);
        JettyEJBWebServiceContext webServiceContext = new JettyEJBWebServiceContext(contextPath, webServiceContainer, internalJAASJettyRealm, realmName, transportGuarantee, authMethod, classLoader);
        webServiceContext.setVirtualHosts(virtualHosts);
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
        try {
            removeContext(webServiceContext);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public void setRequestLog(RequestLog log) {
        this.requestLogHandler.setRequestLog(log);
    }

    /* ------------------------------------------------------------ */
    public RequestLog getRequestLog() {
        return this.requestLogHandler.getRequestLog();
    }

    public void doStart() throws Exception {
        server.start();
    }

    public void doStop() {
        try {
            server.stop();
        } catch (Exception e) {
        }
    }

    public void doFail() {
        try {
            server.stop();
        } catch (Exception e) {
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

        infoBuilder.addOperation("addListener", new Class[]{Connector.class});
        infoBuilder.addOperation("removeListener", new Class[]{Connector.class});
        infoBuilder.addOperation("addContext", new Class[]{ContextHandler.class});
        infoBuilder.addOperation("removeContext", new Class[]{ContextHandler.class});
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
