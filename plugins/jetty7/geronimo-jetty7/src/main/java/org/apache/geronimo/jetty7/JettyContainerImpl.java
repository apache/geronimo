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

package org.apache.geronimo.jetty7;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.security.Permission;

import javax.management.j2ee.statistics.Stats;
import javax.security.jacc.WebUserDataPermission;

import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.management.LazyStatisticsProvider;
import org.apache.geronimo.management.geronimo.NetworkConnector;
import org.apache.geronimo.management.geronimo.WebManager;
import org.apache.geronimo.management.geronimo.stats.JettyWebContainerStatsImpl;
import org.apache.geronimo.security.jaas.ConfigurationFactory;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.webservices.SoapHandler;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.apache.geronimo.jetty7.handler.EJBServletHandler;
import org.apache.geronimo.jetty7.handler.EJBWebServiceContext;
import org.apache.geronimo.jetty7.security.JettySecurityHandlerFactory;
import org.apache.geronimo.jetty7.security.BuiltInAuthMethod;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.security.SecurityHandler;

/**
 * @version $Rev$ $Date$
 */
@GBean
public class JettyContainerImpl implements JettyContainer, SoapHandler, GBeanLifecycle, LazyStatisticsProvider {
    /**
     * The default value of JETTY_HOME variable
     */
    private static final String DEFAULT_JETTY_HOME = "var/jetty";

    private final Server server;
    private final Map<String, EJBWebServiceContext> webServices = new HashMap<String, EJBWebServiceContext>();
    private final String objectName;
    private final WebManager manager;
    private final String jettyHome;
    private final ServerInfo serverInfo;
    private File jettyHomeDir;
    private JettyWebContainerStatsImpl stats;
    // list of handlers
    private StatisticsHandler statsHandler = new StatisticsHandler();
    private ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection();
    private RequestLogHandler requestLogHandler = new RequestLogHandler();
    //    private boolean statsHandlerInPlace = false;
    private boolean statsOn = false;

    public JettyContainerImpl(@ParamSpecial(type = SpecialAttributeType.objectName) String objectName,
                              @ParamReference(name = "WebManager") WebManager manager,
                              @ParamAttribute(name = "jettyHome") String jettyHome,
                              @ParamReference(name = "ServerInfo") ServerInfo serverInfo) {
        this.objectName = objectName;
        this.jettyHome = jettyHome;
        this.serverInfo = serverInfo;

        server = new Server();

        //set up the jetty7+ handler structure which is to have a HandlerCollection,
        //each element of which is always tried on each request.
        //The first element of the HandlerCollection is a
        //ContextHandlerCollection, which is itself is a collection
        //of Handlers. It's special property is that only one of it's
        //handlers will respond to a request.
        //The second element of the HandlerCollection is a DefaultHandler
        //which is responsible for serving static content or anything not
        //handled by a Handler in the ContextHandlerCollection.
        //The third element is the RequestLogHandler, which requires
        //a RequestLog impl to be set.
        DefaultHandler defaultHandler = new DefaultHandler();
        Handler[] handlers = {contextHandlerCollection, defaultHandler, requestLogHandler, statsHandler};
        HandlerCollection handlerCollection = new HandlerCollection();
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
        stats.setStartTime();
    }

    public long getCollectStatisticsStarted() {
        return statsHandler.getStatsOnMs();
    }

    public boolean isStatsOn() {
        return statsOn;
    }

    public void setStatsOn(boolean on) {
        try {
            if (on) {
                // set the statistics handler if not already done so
//                if (!statsHandlerInPlace) {
//                    handlerCollection.addHandler(statsHandler);
//                    statsHandlerInPlace = true;
//                }
                // clear previous data and set start time
                resetStats();
                // start the handler
                statsHandler.start();
            } else {
                statsHandler.stop();
                // hack because stats collection really doesn't really stop when statsHandler.stop() is invoked
//                if (statsHandlerInPlace) {
//                    handlerCollection.removeHandler(statsHandler);
//                    statsHandlerInPlace=false;
//                }
            }
            statsOn = on;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Stats getStats() {
        if (isStatsOn()) {
            stats.setLastSampleTime();

            /* set active request range values */
            stats.getActiveRequestCountImpl().setCurrent((long) statsHandler.getRequestsActive());
            stats.getActiveRequestCountImpl().setLowWaterMark((long) statsHandler.getRequestsActiveMin());
            stats.getActiveRequestCountImpl().setHighWaterMark((long) statsHandler.getRequestsActiveMax());

            /* set request duration time values, avg = Totaltime/Count */
            /* set active request count */
            stats.getRequestDurationImpl().setCount((long) statsHandler.getRequests());
            stats.getRequestDurationImpl().setMaxTime((long) statsHandler.getRequestsDurationMax());
            stats.getRequestDurationImpl().setMinTime((long) statsHandler.getRequestsDurationMin());
            stats.getRequestDurationImpl().setTotalTime((long) statsHandler.getRequestsDurationTotal());

            /* set request count values*/
            stats.getResponses1xxImpl().setCount((long) statsHandler.getResponses1xx());
            stats.getResponses2xxImpl().setCount((long) statsHandler.getResponses2xx());
            stats.getResponses3xxImpl().setCount((long) statsHandler.getResponses3xx());
            stats.getResponses4xxImpl().setCount((long) statsHandler.getResponses4xx());
            stats.getResponses5xxImpl().setCount((long) statsHandler.getResponses5xx());

            /* set elapsed time for stats collection */
            stats.getStatsOnMsImpl().setCount((long) statsHandler.getStatsOnMs());
        }
        return stats;
    }

    public void addListener(Connector listener) {
        server.addConnector(listener);
    }

    public void removeListener(Connector listener) {
        server.removeConnector(listener);
    }

    public void addContext(Handler context) {
        contextHandlerCollection.addHandler(context);
    }

    public void removeContext(Handler context) {
        contextHandlerCollection.removeHandler(context);
    }

    public void addWebService(String contextPath,
                              String[] virtualHosts,
                              WebServiceContainer webServiceContainer,
                              ConfigurationFactory configurationFactory,
                              String realmName,
                              String transportGuarantee,
                              String authMethod,
                              String[] protectedMethods, //allowed methods?
                              ClassLoader classLoader) throws Exception {
        SecurityHandler securityHandler = null;
        if (configurationFactory != null) {
            JettySecurityHandlerFactory  factory = new JettySecurityHandlerFactory(BuiltInAuthMethod.valueOf(authMethod), null, null, realmName, configurationFactory);
            Permission permission = new WebUserDataPermission("/*", protectedMethods, transportGuarantee);
            securityHandler = factory.buildEJBSecurityHandler(permission);
        }
        ServletHandler servletHandler = new EJBServletHandler(webServiceContainer);
        EJBWebServiceContext webServiceContext = new EJBWebServiceContext(contextPath, securityHandler, servletHandler, classLoader);
        webServiceContext.setVirtualHosts(virtualHosts);
        addContext(webServiceContext);
        webServiceContext.start();
        webServices.put(contextPath, webServiceContext);
    }

    public void removeWebService(String contextPath) {
        EJBWebServiceContext webServiceContext = webServices.remove(contextPath);
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

}
