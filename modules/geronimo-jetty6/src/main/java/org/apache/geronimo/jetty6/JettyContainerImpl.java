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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.management.j2ee.statistics.Stats;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.management.StatisticsProvider;
import org.apache.geronimo.management.geronimo.NetworkConnector;
import org.apache.geronimo.management.geronimo.WebManager;
import org.apache.geronimo.system.serverinfo.ServerInfo;
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
import org.mortbay.jetty.handler.StatisticsHandler;
import org.mortbay.jetty.handler.AbstractHandlerContainer;

/**
 * @version $Rev$ $Date$
 */
public class JettyContainerImpl implements JettyContainer, SoapHandler, GBeanLifecycle, StatisticsProvider {
    /**
     * The default value of JETTY_HOME variable
     */
    private static final String DEFAULT_JETTY_HOME = "var/jetty";

    private final Server server;
    private final Map webServices = new HashMap();
    private final String objectName;
    private final WebManager manager;
    private final String jettyHome;
    private final ServerInfo serverInfo;
    private File jettyHomeDir;
    private JettyWebContainerStatsImpl stats;
    private final Map realms = new HashMap();
    // list of handlers
    private StatisticsHandler statsHandler = new StatisticsHandler();  
    private HandlerCollection handlerCollection = new HandlerCollection();
    private ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection();
    private DefaultHandler defaultHandler = new DefaultHandler();
    private RequestLogHandler requestLogHandler = new RequestLogHandler();
    private boolean statsHandlerInPlace = false;

    public JettyContainerImpl(String objectName, WebManager manager, String jettyHome, ServerInfo serverInfo) {
        this.objectName = objectName;
        this.jettyHome = jettyHome;
        this.serverInfo = serverInfo;

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

    public void resetStats() {
        statsHandler.statsReset();
        // Set the start time for each statistic
        long startTime = System.currentTimeMillis();
        stats.getTotalRequestCountImpl().setStartTime(startTime);
        stats.getActiveRequestCountImpl().setStartTime(startTime);
        stats.getRequestDurationAvgImpl().setStartTime(startTime);
        stats.getRequestDurationImpl().setStartTime(startTime);
        stats.getResponses1xxImpl().setStartTime(startTime);
        stats.getResponses2xxImpl().setStartTime(startTime);
        stats.getResponses3xxImpl().setStartTime(startTime);
        stats.getResponses4xxImpl().setStartTime(startTime);
        stats.getResponses5xxImpl().setStartTime(startTime);
        stats.getStatsOnMsImpl().setStartTime(startTime);
    }

    public void setCollectStatistics(boolean on) {
        try {
            if (on) {
                // set the statistics handler if not already done so
                if (!statsHandlerInPlace) {
                    handlerCollection.addHandler(statsHandler);
                    statsHandlerInPlace = true;
                }
                // clear previous data and set start times
                resetStats();
                // start the handler
                statsHandler.start();
            } else {
                statsHandler.stop();
                // hack because stats collection really doesn't really stop when statsHandler.stop() is invoked
                if (statsHandlerInPlace) {
                    handlerCollection.removeHandler(statsHandler);
                    statsHandlerInPlace=false;
                }
            }
            stats.setStatsOn(on);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public boolean getCollectStatistics() {
        return statsHandler.isRunning();
    }

    public long getCollectStatisticsStarted() {
        return statsHandler.getStatsOnMs();
    }

    public Stats getStats() {
        if (getCollectStatistics()) {
            long currentTime = System.currentTimeMillis();
            /* set active request count */
            stats.getTotalRequestCountImpl().setCount((long)statsHandler.getRequests());
            stats.getTotalRequestCountImpl().setLastSampleTime(currentTime);
            
    
            /* set active request range values */
            stats.getActiveRequestCountImpl().setCurrent((long)statsHandler.getRequestsActive());
            stats.getActiveRequestCountImpl().setLowWaterMark((long)statsHandler.getRequestsActiveMin());
            stats.getActiveRequestCountImpl().setHighWaterMark((long)statsHandler.getRequestsActiveMax());
            stats.getActiveRequestCountImpl().setLastSampleTime(currentTime);
    
            /* set request duration average time */
            stats.getRequestDurationAvgImpl().setCount((long)statsHandler.getRequestsDurationAve());     // Normally this would be calculated
            stats.getRequestDurationAvgImpl().setLastSampleTime(currentTime);
    
            /* set request duration time values */
//            stats.getRequestDurationImpl().setCount((long)statsHandler.getRequestsDurationCount());    Not yet supported by Jetty
            stats.getRequestDurationImpl().setMaxTime((long)statsHandler.getRequestsDurationMax());
            stats.getRequestDurationImpl().setMinTime((long)statsHandler.getRequestsDurationMin());
            stats.getRequestDurationImpl().setTotalTime((long)statsHandler.getRequestsDurationTotal());
            stats.getRequestDurationImpl().setLastSampleTime(currentTime);
    
            /* set request count values*/
            stats.getResponses1xxImpl().setCount((long)statsHandler.getResponses1xx());
            stats.getResponses1xxImpl().setLastSampleTime(currentTime);
            
            stats.getResponses2xxImpl().setCount((long)statsHandler.getResponses2xx());
            stats.getResponses2xxImpl().setLastSampleTime(currentTime);
            
            stats.getResponses3xxImpl().setCount((long)statsHandler.getResponses3xx());
            stats.getResponses3xxImpl().setLastSampleTime(currentTime);
            
            stats.getResponses4xxImpl().setCount((long)statsHandler.getResponses4xx());
            stats.getResponses4xxImpl().setLastSampleTime(currentTime);
            
            stats.getResponses5xxImpl().setCount((long)statsHandler.getResponses5xx());
            stats.getResponses5xxImpl().setLastSampleTime(currentTime);
    
            /* set elapsed time for stats collection */
            stats.getStatsOnMsImpl().setCount((long)statsHandler.getStatsOnMs());
            stats.getStatsOnMsImpl().setLastSampleTime(currentTime);
        }
        return stats;
    }

    public void addListener(Connector listener) {
        server.addConnector(listener);
    }

    public void removeListener(Connector listener) {
        server.removeConnector(listener);
    }

    public void addContext(AbstractHandlerContainer context) {
        contextHandlerCollection.addHandler(context);
    }

    public void removeContext(AbstractHandlerContainer context) {
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
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void setRequestLog(RequestLog log) {
        this.requestLogHandler.setRequestLog(log);
    }

    public File resolveToJettyHome(String workDir) {
        if (workDir == null) {
            return null;
        }
        return new File(jettyHomeDir, workDir);
    }

    public RequestLog getRequestLog() {
        return this.requestLogHandler.getRequestLog();
    }

    public void doStart() throws Exception {
        jettyHomeDir = new File(serverInfo.resolveServerPath(jettyHome != null ? jettyHome : DEFAULT_JETTY_HOME));
        if (!jettyHomeDir.exists()) {
            jettyHomeDir.mkdirs();
        }
        // start the server
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
        infoBuilder.addOperation("resetStats");

        infoBuilder.addAttribute("objectName", String.class, false);
        infoBuilder.addReference("WebManager", WebManager.class);

        infoBuilder.addAttribute("jettyHome", String.class, true);
        infoBuilder.addReference("ServerInfo", ServerInfo.class, "GBean");

        infoBuilder.addInterface(SoapHandler.class);
        infoBuilder.addInterface(JettyContainer.class);
        infoBuilder.addInterface(StatisticsProvider.class);
        infoBuilder.setConstructor(new String[]{"objectName", "WebManager", "jettyHome", "ServerInfo"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }


}
