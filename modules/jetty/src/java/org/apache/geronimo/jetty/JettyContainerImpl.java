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

import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpListener;
import org.mortbay.http.RequestLog;
import org.mortbay.http.UserRealm;
import org.mortbay.jetty.Server;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;

/**
 * @version $Rev$ $Date$
 */
public class JettyContainerImpl implements JettyContainer, GBeanLifecycle {
    private final Server server;

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

    public JettyContainerImpl() {
        server = new JettyServer();
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
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("Jetty Web Container", JettyContainerImpl.class);
        infoFactory.addAttribute("collectStatistics", Boolean.TYPE, true);
        infoFactory.addAttribute("collectStatisticsStarted", Long.TYPE, false);
        infoFactory.addAttribute("connections", Integer.TYPE, false);
        infoFactory.addAttribute("connectionsOpen", Integer.TYPE, false);
        infoFactory.addAttribute("connectionsOpenMax", Integer.TYPE, false);
        infoFactory.addAttribute("connectionsDurationAve", Long.TYPE, false);
        infoFactory.addAttribute("connectionsDurationMax", Long.TYPE, false);
        infoFactory.addAttribute("connectionsRequestsAve", Integer.TYPE, false);
        infoFactory.addAttribute("connectionsRequestsMax", Integer.TYPE, false);
        infoFactory.addAttribute("errors", Integer.TYPE, false);
        infoFactory.addAttribute("requests", Integer.TYPE, false);
        infoFactory.addAttribute("requestsActive", Integer.TYPE, false);
        infoFactory.addAttribute("requestsActiveMax", Integer.TYPE, false);
        infoFactory.addAttribute("requestsDurationAve", Long.TYPE, false);
        infoFactory.addAttribute("requestsDurationMax", Long.TYPE, false);
        infoFactory.addOperation("resetStatistics");

        infoFactory.addAttribute("requestLog", RequestLog.class, false);

        infoFactory.addOperation("addListener", new Class[]{HttpListener.class});
        infoFactory.addOperation("removeListener", new Class[]{HttpListener.class});
        infoFactory.addOperation("addContext", new Class[]{HttpContext.class});
        infoFactory.addOperation("removeContext", new Class[]{HttpContext.class});
        infoFactory.addOperation("addRealm", new Class[]{UserRealm.class});
        infoFactory.addOperation("removeRealm", new Class[]{UserRealm.class});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
