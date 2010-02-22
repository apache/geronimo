/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.activemq.management;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.apache.activemq.broker.TransportConnector;
import org.apache.geronimo.management.activemq.ActiveMQConnector;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
@GBean(name="ActiveMQ Transport Connector")
public class ActiveMQTransportConnector implements ActiveMQConnector {

    private static final Logger log = LoggerFactory.getLogger(ActiveMQTransportConnector.class);

    private final TransportConnector transportConnector;

    public ActiveMQTransportConnector(@ParamAttribute(manageable = false, name = "transportConnector") TransportConnector transportConnector) {
        this.transportConnector = transportConnector;
    }

    // Additional stuff you can add to an ActiveMQ connector URI
    public String getPath() {
        try {
            return transportConnector.getUri().toString();
        } catch (Exception e) {
            log.warn("error", e);
            return "Error: " + e.getMessage();
        }
    }

    public void setPath(String path) {
        throw new RuntimeException("Not implemented");
    }

    //????
    public String getQuery() {
        try {
            return transportConnector.getUri().getQuery();
        } catch (Exception e) {
            log.warn("error", e);
            return "Error: " + e.getMessage();
        }
    }

    public void setQuery(String query) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Gets the network protocol that this connector handles.
     */
    public String getProtocol() {
        try {
            return transportConnector.getUri().getScheme();
        } catch (Exception e) {
            log.warn("error", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Gets the network port that this connector listens on.
     */
    public int getPort() {
        try {
            return transportConnector.getUri().getPort();
        } catch (Exception e) {
            log.warn("error", e);
            return -1;
        }
    }

    /**
     * Sets the network port that this connector listens on.
     */
    public void setPort(int port) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Gets the hostname/IP that this connector listens on.
     */
    public String getHost() {
        try {
            return transportConnector.getUri().getHost();
        } catch (Exception e) {
            log.warn("error", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Sets the hostname/IP that this connector listens on.  This is typically
     * most useful for machines with multiple network cards, but can be used
     * to limit a connector to only listen for connections from the local
     * machine (127.0.0.1).  To listen on all available network interfaces,
     * specify an address of 0.0.0.0.
     */
    public void setHost(String host) throws UnknownHostException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Every connector must specify a property of type InetSocketAddress
     * because we use that to identify the network services to print a list
     * during startup.  However, this can be read-only since the host and port
     * are set separately using setHost and setPort.
     */
    public InetSocketAddress getListenAddress() {
        return new InetSocketAddress(getHost(), getPort());
    }
}
