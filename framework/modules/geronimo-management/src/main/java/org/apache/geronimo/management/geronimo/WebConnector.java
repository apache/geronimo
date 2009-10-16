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
package org.apache.geronimo.management.geronimo;

/**
 * The common configuration settings for a web container network connector --
 * that is, the protocol and network settings used to connect to the web
 * container (with a variety of tuning arguments as well).
 *
 * http://jakarta.apache.org/tomcat/tomcat-5.5-doc/config/http.html
 * http://mortbay.org/javadoc/org/mortbay/http/SocketListener.html
 *
 * @version $Rev$ $Date$
 */
public interface WebConnector extends NetworkConnector {
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
     * Gets the size of the header buffer used to handle network data for this
     * connector.
     */
    public int getHeaderBufferSizeBytes();
    /**
     * Sets the size of the Header buffer used to handle network data for this
     * connector.
     */
    public void setHeaderBufferSizeBytes(int bytes);
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

    /**
     * Gets a URL used to connect to the web server via this connector.
     * This is not guaranteed to work (for example, if the server is
     * located behind a proxy), but it should give a reasonable value if
     * possible.  The form of the returned String should be
     * http://hostname or http://hostname:port (in other words, suitable
     * for appending a context path).
     */
    public String getConnectUrl();
}
