/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.webdav;

/**
 * A Connector is a request listener and response broker.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:59:20 $
 */
public interface Connector {
    /**
     * Gets listening port number.
     */
    int getPort();

    /**
     * Sets listening port number.
     */
    void setPort(int ort);

    /**
     * Gets the protocol name.
     */
    String getProtocol();

    /**
     * Sets the protocol name.
     */
    void setProtocol(String protocol);

    /**
     * Gets the network interface name.
     */
    String getInterface();

    /**
     * Sets the network interface name.
     */
    void setInterface(String networkInterface);

    /**
     * Gets the maximum number of connections.
     */
    int getMaxConnections();

    /**
     * Sets the maximum number of connections.
     */
    void setMaxConnections(int maxConnects);

    /**
     * Sets the maximum idle time.
     */
    int getMaxIdleTime();

    /**
     * Gets the maximum idle time.
     */
    void setMaxIdleTime(int maxIdleTime);
}