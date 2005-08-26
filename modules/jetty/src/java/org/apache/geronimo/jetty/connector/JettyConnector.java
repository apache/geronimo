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
import org.mortbay.http.SocketListener;
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
        if(listener instanceof SocketListener) {
            SocketListener socketListener = (SocketListener)listener;
            socketListener.setBufferSize(bytes);
        } else {
            throw new UnsupportedOperationException(); //todo: can this happen?
        }
    }

    public int getAcceptQueueSize() {
        if(listener instanceof SocketListener) {
            SocketListener socketListener = (SocketListener)listener;
            return socketListener.getAcceptQueueSize();
        } else {
            throw new UnsupportedOperationException(); //todo: can this happen?
        }
    }

    public void setAcceptQueueSize(int size) {
        if(listener instanceof SocketListener) {
            SocketListener socketListener = (SocketListener)listener;
            socketListener.setAcceptQueueSize(size);
        } else {
            throw new UnsupportedOperationException(); //todo: can this happen?
        }
    }

    public int getLingerMillis() {
        if(listener instanceof SocketListener) {
            SocketListener socketListener = (SocketListener)listener;
            return socketListener.getLingerTimeSecs()*1000;
        } else {
            throw new UnsupportedOperationException(); //todo: can this happen?
        }
    }

    public void setLingerMillis(int millis) {
        if(listener instanceof SocketListener) {
            SocketListener socketListener = (SocketListener)listener;
            socketListener.setLingerTimeSecs(Math.round((float)millis/1000f));
        } else {
            throw new UnsupportedOperationException(); //todo: can this happen?
        }
    }

    public boolean isTcpNoDelay() {
        if(listener instanceof SocketListener) {
            SocketListener socketListener = (SocketListener)listener;
            return socketListener.getTcpNoDelay();
        } else {
            throw new UnsupportedOperationException(); //todo: can this happen?
        }
    }

    public void setTcpNoDelay(boolean enable) {
        if(listener instanceof SocketListener) {
            SocketListener socketListener = (SocketListener)listener;
            socketListener.setTcpNoDelay(enable);
        } else {
            throw new UnsupportedOperationException(); //todo: can this happen?
        }
    }

    public int getRedirectPort() {
        return listener.getConfidentialPort();
    }

    public void setRedirectPort(int port) {
        if(listener instanceof SocketListener) {
            SocketListener socketListener = (SocketListener)listener;
            socketListener.setConfidentialPort(port);
            socketListener.setIntegralPort(port);
            socketListener.setIntegralScheme("https");
            socketListener.setConfidentialScheme("https");
        } else {
            throw new UnsupportedOperationException(); //todo: can this happen?
        }
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
        infoFactory.addReference("JettyContainer", JettyContainer.class, NameFactory.GERONIMO_SERVICE);
        infoFactory.addInterface(JettyWebConnector.class, new String[]{"host", "port", "minThreads","maxThreads","bufferSizeBytes","acceptQueueSize","lingerMillis","tcpNoDelay","redirectPort",},
                                                          new String[]{"host", "port", "redirectPort"});
        infoFactory.setConstructor(new String[] {"JettyContainer"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }
}
