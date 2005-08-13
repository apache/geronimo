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

package org.apache.geronimo.jetty.connector;

import java.net.UnknownHostException;
import java.net.InetSocketAddress;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.jetty.JettyContainer;
import org.apache.geronimo.jetty.JettyWebConnector;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.mortbay.http.HttpListener;
import org.mortbay.util.ThreadedServer;

/**
 * @version $Rev$ $Date$
 */
public abstract class JettyConnector implements GBeanLifecycle, JettyWebConnector {
    private final JettyContainer container;
    protected final HttpListener listener;

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

    public JettyConnector(JettyContainer container, HttpListener listener) {
        this.container = container;
        this.listener = listener;
    }

    public String getDefaultScheme() {
        return listener.getDefaultScheme();
    }

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


    public void setMinThreads(int minThreads) {
      ((ThreadedServer)listener).setMinThreads(minThreads);
    }

    public int getMinThreads() {
      return ((ThreadedServer)listener).getMinThreads();
    }


    public void setMaxThreads(int maxThreads) {
      ((ThreadedServer)listener).setMaxThreads(maxThreads);
    }

    public int getMaxThreads() {
      return ((ThreadedServer)listener).getMaxThreads();
    }

    public int getThreads() {
      return ((ThreadedServer)listener).getThreads();
    }

    public int getIdlethreads() {
      return ((ThreadedServer)listener).getIdleThreads();
    }
    
    public InetSocketAddress getListenAddress() {
        return new InetSocketAddress(getHost(), getPort());
    }

    public int getBufferSizeBytes() {
        return listener.getBufferSize();
    }

    public void setBufferSizeBytes(int bytes) {
        throw new UnsupportedOperationException(); //todo: is this supported?
    }

    public int getAcceptQueueSize() {
        throw new UnsupportedOperationException(); //todo: where does this come from?
    }

    public void setAcceptQueueSize(int size) {
        throw new UnsupportedOperationException(); //todo: where does this come from?
    }

    public int getLingerMillis() {
        throw new UnsupportedOperationException(); //todo: where does this come from?
    }

    public void setLingerMillis(int millis) {
        throw new UnsupportedOperationException(); //todo: where does this come from?
    }

    public boolean isTcpNoDelay() {
        throw new UnsupportedOperationException(); //todo: where does this come from?
    }

    public void setTcpNoDelay(boolean enable) {
        throw new UnsupportedOperationException(); //todo: where does this come from?
    }

    public int getRedirectPort() {
        return listener.getConfidentialPort();
    }

    public void setRedirectPort(int port) {
        throw new UnsupportedOperationException(); //todo: is this supported?
    }

    public abstract String getProtocol();

    public void doStart() throws Exception {
        container.addListener(listener);
        ((ThreadedServer) listener).open();
        listener.start();
    }

    public void doStop() {
        while (true) {
            try {
                listener.stop();
                container.removeListener(listener);
                return;
            } catch (InterruptedException e) {
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
            } catch (InterruptedException e) {
                continue;
            }
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("Jetty HTTP Connector", JettyConnector.class);
        infoFactory.addAttribute("defaultScheme", String.class, false);
        infoFactory.addAttribute("host", String.class, true);
        infoFactory.addAttribute("port", int.class, true);
        infoFactory.addAttribute("minThreads", int.class, true);
        infoFactory.addAttribute("maxThreads", int.class, true);
        infoFactory.addAttribute("threads", int.class, false);
        infoFactory.addAttribute("idleThreads", int.class, false);
        infoFactory.addAttribute("listenAddress", InetSocketAddress.class, false);
        infoFactory.addReference("JettyContainer", JettyContainer.class, NameFactory.GERONIMO_SERVICE);
        infoFactory.addInterface(JettyWebConnector.class);
        infoFactory.setConstructor(new String[] {"JettyContainer"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }
}
