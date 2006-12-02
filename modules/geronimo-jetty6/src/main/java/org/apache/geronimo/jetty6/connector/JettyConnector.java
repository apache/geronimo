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

package org.apache.geronimo.jetty6.connector;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.jetty6.JettyContainer;
import org.apache.geronimo.jetty6.JettyWebConnector;
import org.mortbay.jetty.AbstractConnector;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.nio.SelectChannelConnector;

/**
 * Base class for GBeans for Jetty network connectors (HTTP, HTTPS, AJP, etc.).
 *
 * @version $Rev$ $Date$
 */
public abstract class JettyConnector implements GBeanLifecycle, JettyWebConnector {
    public final static String CONNECTOR_CONTAINER_REFERENCE = "JettyContainer";
    private final JettyContainer container;
    protected final Connector listener;
    private String connectHost;

    /**
     * Only used to allow declaration as a reference.
     */
    public JettyConnector() {
        container = null;
        listener = null;
    }

    public JettyConnector(JettyContainer container) {
        this.container = container;
        this.listener = null;
    }

    public JettyConnector(JettyContainer container, Connector listener) {
        this.container = container;
        this.listener = listener;
    }

    //TODO: support the jetty6 specific methods
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
        return ((AbstractConnector) listener).getMaxIdleTime();
    }

    public void setMaxIdleTimeMs(int idleTime) {
        ((AbstractConnector) listener).setMaxIdleTime(idleTime);
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
        return ((AbstractConnector) listener).getAcceptQueueSize();
    }

    public void setAcceptQueueSize(int size) {
        ((AbstractConnector) listener).setAcceptQueueSize(size);
    }

    public int getLingerMillis() {
        return (int) ((AbstractConnector) listener).getSoLingerTime();
    }

    public void setLingerMillis(int millis) {
        ((AbstractConnector) listener).setSoLingerTime(millis);
    }

    public boolean isTcpNoDelay() {
        return true;
    }

    public void setTcpNoDelay(boolean enable) {
        throw new UnsupportedOperationException(listener == null ? "No Listener" : listener.getClass().getName());
    }

    public void setMaxThreads(int maxThreads) {
        //TODO: in jetty6 connectors have a number of acceptor threads
        ((AbstractConnector) listener).setAcceptors(maxThreads);
    }

    public int getMaxThreads() {
        //TODO: confirm that this is reasonable
        return ((AbstractConnector) listener).getAcceptors();
    }

    public int getRedirectPort() {
        return listener.getConfidentialPort();
    }

    public InetSocketAddress getListenAddress() {
        try {
            return new InetSocketAddress(InetAddress.getByName(listener.getHost()), listener.getPort());
        } catch (UnknownHostException e) {
            throw new IllegalStateException("InetSocketAddress cannot be determined for host=" + listener.getHost());
        }
    }


    public void setRedirectPort(int port) {
        if (listener instanceof SocketConnector) {
            SocketConnector socketListener = (SocketConnector) listener;
            socketListener.setConfidentialPort(port);
            socketListener.setIntegralPort(port);
            socketListener.setIntegralScheme("https");
            socketListener.setConfidentialScheme("https");
        } else if (listener instanceof SelectChannelConnector) {
            SelectChannelConnector connector = (SelectChannelConnector) listener;
            connector.setConfidentialPort(port);
            connector.setIntegralPort(port);
            connector.setIntegralScheme("https");
            connector.setConfidentialScheme("https");
        }
        /*
                 * don't have one of these yet
                else if(listener instanceof AJP13Listener) {
            AJP13Listener ajpListener = (AJP13Listener) listener;
            ajpListener.setConfidentialPort(port);
            ajpListener.setIntegralPort(port);
            ajpListener.setIntegralScheme("https");
            ajpListener.setConfidentialScheme("https");
        */
        else {
            throw new UnsupportedOperationException(listener == null ? "No Listener" : listener.getClass().getName()); //todo: can this happen?
        }
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
                continue;
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
                continue;
            }
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("Jetty HTTP Connector", JettyConnector.class);
        infoFactory.addReference(CONNECTOR_CONTAINER_REFERENCE, JettyContainer.class, NameFactory.GERONIMO_SERVICE);
        infoFactory.addInterface(JettyWebConnector.class, new String[]{"host", "port", "minThreads", "maxThreads", "bufferSizeBytes", "acceptQueueSize", "lingerMillis", "tcpNoDelay", "redirectPort", "connectUrl", "maxIdleTimeMs"},
                new String[]{"host", "port", "redirectPort", "maxThreads", "minThreads"});
        infoFactory.setConstructor(new String[]{"JettyContainer"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }
}
