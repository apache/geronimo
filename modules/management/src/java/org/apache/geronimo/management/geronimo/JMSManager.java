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
package org.apache.geronimo.management.geronimo;

/**
 * Main entry point for managing a certain JMS server implementation
 *
 * @version $Rev: 46228 $ $Date: 2004-09-16 21:21:04 -0400 (Thu, 16 Sep 2004) $
 */
public interface JMSManager extends NetworkContainer {
    /**
     * Gets the ObjectNames of any JMS servers/brokers running in the current
     * Geronimo instance.
     */
    public String[] getBrokers();

    /**
     * Gets the ObjectNames of any existing connectors for the specified
     * protocol associated with the specified broker.
     *
     * @param protocol A protocol as returned by getSupportedProtocols
     */
    public String[] getBrokerConnectors(String brokerObjectName, String protocol);

    /**
     * Gets the ObjectNames of any existing connectors associated with the
     * specified broker.
     */
    public String[] getBrokerConnectors(String brokerObjectName);

    /**
     * Creates a new connector, and returns the ObjectName for it.  Note that
     * the connector may well require further customization before being fully
     * functional (e.g. SSL settings for a secure connector).
     */
    public String addConnector(String broker, String uniqueName, String protocol, String host, int port);
}
