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

import org.apache.geronimo.gbean.AbstractName;

/**
 * Base management interface for a network technology with associated
 * containers and connectors.  Examples might be Web, EJB, JMS (all
 * of which have the concept of containers and connectors).  The container
 * would be the Web Container, EJB Container, or JMS Broker.  The connectors
 * would be the services that expose those containers over the network, via
 * HTTP, RMI, TCP, etc.
 *
 * @version $Rev$ $Date$
 */
public interface NetworkManager {
    /**
     * Gets the name of the product that this manager manages.
     */
    public String getProductName();

    /**
     * Gets the network containers (web, EJB, JMS, etc.)
     */
    public Object[] getContainers();

    /**
     * Gets the protocols which this container can configure connectors for.
     */
    public String[] getSupportedProtocols();

    /**
     * Removes a connector.  This shuts it down if necessary, and removes it
     * from the server environment.  It must be a connector that uses this
     * network technology.
     * @param connectorName
     */
    public void removeConnector(AbstractName connectorName);

    /**
     * Gets any existing connectors for this network
     * technology for the specified protocol.
     *
     * @param protocol A protocol as returned by getSupportedProtocols
     */
    public NetworkConnector[] getConnectors(String protocol);

    /**
     * Gets any existing connectors associated with this
     * network technology.
     */
    public NetworkConnector[] getConnectors();

    /**
     * Gets the ObjectNames of any existing connectors for the specified
     * container for the specified protocol.
     *
     * @param container The container to get connectors for
     * @param protocol A protocol as returned by getSupportedProtocols
     */
    public NetworkConnector[] getConnectorsForContainer(Object container, String protocol);

    /**
     * Gets the ObjectNames of any existing connectors for the specified
     * container.
     * @param container The container to get connectors for
     */
    public NetworkConnector[] getConnectorsForContainer(Object container);
}
