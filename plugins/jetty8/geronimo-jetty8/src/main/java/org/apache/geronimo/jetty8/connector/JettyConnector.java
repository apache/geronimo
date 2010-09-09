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

package org.apache.geronimo.jetty8.connector;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import javax.management.j2ee.statistics.Stats;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.jetty8.JettyContainer;
import org.apache.geronimo.jetty8.JettyWebConnector;
import org.apache.geronimo.management.LazyStatisticsProvider;
import org.apache.geronimo.management.geronimo.stats.JettyWebConnectorStatsImpl;
import org.apache.geronimo.system.threads.ThreadPool;
import org.eclipse.jetty.server.AbstractConnector;

/**
 * Base class for GBeans for Jetty network connectors (HTTP, HTTPS, AJP, etc.).
 *
 * @version $Rev$ $Date$
 */
public abstract class JettyConnector implements GBeanLifecycle, JettyWebConnector, LazyStatisticsProvider {
    public final static String CONNECTOR_CONTAINER_REFERENCE = "JettyContainer";
    private final JettyContainer container;
    protected final AbstractConnector listener;
    private JettyWebConnectorStatsImpl stats;           // data structure for jsr77 stats
    private String connectHost;

    /**
     * Only used to allow declaration as a reference.
     */
    public JettyConnector() {
        container = null;
        listener = null;
    }

    public JettyConnector(JettyContainer container, ThreadPool threadPool) {
        this.container = container;
        this.listener = null;
    }

    public JettyConnector(JettyContainer container, AbstractConnector listener, ThreadPool threadPool, String name) {
        this.container = container;
        this.listener = listener;
        if (threadPool != null) {
            JettyThreadPool jettyThreadPool = new JettyThreadPool(threadPool, name);
            listener.setThreadPool(jettyThreadPool);
        }
        stats = new JettyWebConnectorStatsImpl();
    }

    //TODO: support the jetty8 specific methods
    public String getHost() {
        return listener.getHost();
    }

    public void setHost(String host) throws UnknownHostException {
        // underlying impl treats null as 0.0.0.0
        listener.setHost(host);
    }

    public int getPort() {
        return listener.getPort();
    }

    public void setPort(int port) {
        listener.setPort(port);
    }
    
    public int getHeaderBufferSizeBytes() {
        return listener.getRequestHeaderSize();
    }
    public void setHeaderBufferSizeBytes(int size) {
        listener.setRequestHeaderSize(size);
    }

    public abstract int getDefaultPort();

    public String getDefaultScheme() {
        return null;
    }

    public String getConnectUrl() {
        if (connectHost == null) {
            String host = getHost();
            if (host == null || host.equals("0.0.0.0")) {
                InetAddress address = null;
                try {
                    address = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    host = "unknown-host";
                }
                if (address != null) {
                    host = address.getHostName();
                    if (host == null || host.equals("")) {
                        host = address.getHostAddress();
                    }
                }
            }
            connectHost = host;
        }
        return getProtocol().toLowerCase() + "://" + connectHost + (getPort() == getDefaultPort() ? "" : ":" + getPort());
    }

    public int getMaxIdleTimeMs() {
        return listener.getMaxIdleTime();
    }

    public void setMaxIdleTimeMs(int idleTime) {
        listener.setMaxIdleTime(idleTime);
    }

    public int getBufferSizeBytes() {
        //TODO return the request buffer size, what about the response and header buffer size?
        return listener.getRequestBufferSize();
    }

    public void setBufferSizeBytes(int bytes) {
        //TODO what about the response and header buffer size?
        listener.setRequestBufferSize(bytes);
    }

    public int getAcceptQueueSize() {
        return listener.getAcceptQueueSize();
    }

    public void setAcceptQueueSize(int size) {
        listener.setAcceptQueueSize(size);
    }

    public int getLingerMillis() {
        return ((AbstractConnector) listener).getSoLingerTime();
    }

    public void setLingerMillis(int millis) {
        listener.setSoLingerTime(millis);
    }

    public boolean isTcpNoDelay() {
        return true;
    }

    public void setTcpNoDelay(boolean enable) {
        throw new UnsupportedOperationException(listener == null ? "No Listener" : listener.getClass().getName());
    }

    public void setMaxThreads(int maxThreads) {
        //TODO: in jetty8 connectors have a number of acceptor threads
        listener.setAcceptors(maxThreads);
    }

    public int getMaxThreads() {
        //TODO: confirm that this is reasonable
        return listener.getAcceptors();
    }

    public int getRedirectPort() {
        return listener.getConfidentialPort();
    }

    public InetSocketAddress getListenAddress() {
        try {
            return new InetSocketAddress(InetAddress.getByName(listener.getHost()), listener.getPort());
        } catch (UnknownHostException e) {
            throw new IllegalStateException("InetSocketAddress cannot be determined for host=" + listener.getHost(), e);
        }
    }

    public void setRedirectPort(int port) {
        throw new UnsupportedOperationException("No redirect port on " + this.getClass().getName());
    }

    public abstract String getProtocol();

    public void doStart() throws Exception {
        container.addListener(listener);
        listener.start();
    }

    public void doStop() {
        while (true) {
            try {
                listener.stop();
                container.removeListener(listener);
                return;
            } catch (Exception e) {
                //try again
            }
        }
    }

    public void doFail() {
        while (true) {
            try {
                listener.stop();
                container.removeListener(listener);
                return;
            } catch (Exception e) {
                //try again
            }
        }
    }
    
    public boolean isStatsOn() {
	return listener.getStatsOn();
    }
    
    public void setStatsOn(boolean on) {
        listener.setStatsOn(on);
        if (on) stats.setStartTime();
    }
    
    /**
     * Gets the statistics collected for this class. 
     * The first call to this method initializes the startTime for
     * all statistics. 
     *
     * @return gets collected for this class
     */
    public Stats getStats() {
        if(isStatsOn()) {
            stats.setLastSampleTime();
            // connections open
            stats.getOpenConnectionCountImpl().setCurrent(listener.getConnectionsOpen());
            stats.getOpenConnectionCountImpl().setHighWaterMark(listener.getConnectionsOpenMax());
            //stats.getOpenConnectionCountImpl().setLowWaterMark(listener.getConnectionsOpenMin());
            // request count
            stats.getRequestCountImpl().setCount(listener.getRequests());
            // connections count and durations
            stats.getConnectionsDurationImpl().setCount(listener.getConnections());
            stats.getConnectionsDurationImpl().setMaxTime(listener.getConnectionsDurationMax());
            //stats.getConnectionsDurationImpl().setMinTime(listener.getConnectionsDurationMin());
            stats.getConnectionsDurationImpl().setTotalTime(listener.getConnectionsDurationTotal());
            // requests per connection (connection requests)
            stats.getConnectionsRequestImpl().setCurrent((long)listener.getConnectionsRequestsMean());
            stats.getConnectionsRequestImpl().setHighWaterMark(listener.getConnectionsRequestsMax());
            //stats.getConnectionsRequestImpl().setLowWaterMark(listener.getConnectionsRequestsMin());
        }
        return stats;
    }
    
    /**
     * Reset the startTime for all statistics
     */
    public void resetStats() {
        listener.statsReset();
        stats.setStartTime(); // sets atartTime for all stats to Now
    }
    
    public boolean isStateManageable() {
        return true;
    }

    public boolean isStatisticsProvider() {
        return true;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("Jetty HTTP Connector", JettyConnector.class);
        infoFactory.addReference(CONNECTOR_CONTAINER_REFERENCE, JettyContainer.class, GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
        infoFactory.addReference("ThreadPool", ThreadPool.class, GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
        // this is needed because the getters/setters are not added automatically
        infoFactory.addOperation("setStatsOn", new Class[] { boolean.class }, "void");
        // removed 'minThreads' from persistent and manageable String[]
        // removed 'tcpNoDelay' from persistent String[]
        // added 'protocol' to persistent and manageable String[]
        infoFactory.addInterface(JettyWebConnector.class, 
                new String[]{"host", "port", "minThreads", "maxThreads", "bufferSizeBytes", "headerBufferSizeBytes", "acceptQueueSize", "lingerMillis", "redirectPort", "maxIdleTimeMs"},
                new String[]{"host", "port", "minThreads", "maxThreads", "bufferSizeBytes", "headerBufferSizeBytes", "acceptQueueSize", "lingerMillis", "protocol", "redirectPort"});
        infoFactory.setConstructor(new String[]{"JettyContainer", "ThreadPool"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }
}
