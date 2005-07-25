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
package org.apache.geronimo.j2ee.management.geronimo;

import java.net.InetSocketAddress;

/**
 * The common configuration settings for a web container network connector --
 * that is, the protocol and network settings used to connect to the web
 * container (with a variety of tuning arguments as well).
 *
 * http://jakarta.apache.org/tomcat/tomcat-5.5-doc/config/http.html
 * http://mortbay.org/javadoc/org/mortbay/http/SocketListener.html
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public interface WebConnector {
    /**
     * Gets the network port that this connector listens on.
     */
    public int getPort();
    /**
     * Sets the network port that this connector listens on.
     */
    public void setPort(int port);
    /**
     * Gets the hostname/IP that this connector listens on.
     */
    public String getHost();
    /**
     * Sets the hostname/IP that this connector listens on.  This is typically
     * most useful for machines with multiple network cards, but can be used
     * to limit a connector to only listen for connections from the local
     * machine (127.0.0.1).  To listen on all available network interfaces,
     * specify an address of 0.0.0.0.
     */
    public void setHost(String host);
    /**
     * Every connector must specify a property of type InetSocketAddress
     * because we use that to identify the network services to print a list
     * during startup.  However, this can be read-only since the host and port
     * are set separately using setHost and setPort.
     */
    public InetSocketAddress getListenAddress();

    /**
     * Gets the size of the buffer used to handle network data for this
     * connector.
     */
    public int getBufferSizeBytes();
    /**
     * Gets the size of the buffer used to handle network data for this
     * connector.
     */
    public void setBufferSizeBytes(int bytes);
    /**
     * Gets the maximum number of threads used to service connections from
     * this connector.
     */
    public int getMaxThreads();
    /**
     * Sets the maximum number of threads used to service connections from
     * this connector.
     */
    public void setMaxThreads(int threads);
    /**
     * Gets the maximum number of connections that may be queued while all
     * threads are busy.  Any requests received while the queue is full will
     * be rejected.
     */
    public int getAcceptQueueSize();
    /**
     * Sets the maximum number of connections that may be queued while all
     * threads are busy.  Any requests received while the queue is full will
     * be rejected.
     */
    public void setAcceptQueueSize(int size);
    /**
     * Gets the amount of time the socket used by this connector will linger
     * after being closed.  -1 indicates that socket linger is disabled.
     */
    public int getLingerMillis();
    /**
     * Sets the amount of time the socket used by this connector will linger
     * after being closed.  Use -1 to disable socket linger.
     */
    public void setLingerMillis(int millis);
    /**
     * Gets whether the TCP_NODELAY flag is set for the sockets used by this
     * connector.  This usually enhances performance, so it should typically
     * be set.
     */
    public boolean isTcpNoDelay();
    /**
     * Sets whether the TCP_NODELAY flag is set for the sockets used by this
     * connector.  This usually enhances performance, so it should typically
     * be set.
     */
    public void setTcpNoDelay(boolean enable);
    /**
     * Gets the network port to which traffic will be redirected if this
     * connector handles insecure traffic and the request requires a secure
     * connection.  Needless to say, this should point to another connector
     * configured for SSL.
     */
    public int getRedirectPort();
    /**
     * Gets the network port to which traffic will be redirected if this
     * connector handles insecure traffic and the request requires a secure
     * connection.  Needless to say, this should point to another connector
     * configured for SSL.  If no SSL connector is available, any port can
     * be used as they all fail equally well.  :)
     */
    public void setRedirectPort(int port);
}
