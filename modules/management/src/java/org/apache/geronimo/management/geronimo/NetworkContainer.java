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
 * Base management interface for a server component that has associated
 * network connectors.
 *
 * @version $Rev: 46228 $ $Date: 2004-09-16 21:21:04 -0400 (Thu, 16 Sep 2004) $
 */
public interface NetworkContainer {
    /**
     * Gets the protocols which this container can configure connectors for.
     */
    public String[] getSupportedProtocols();

    /**
     * Removes a connector.  This shuts it down if necessary, and removes it
     * from the server environment.  It must be a connector that this container
     * is responsible for.
     */
    public void removeConnector(String objectName);

    /**
     * Gets the ObjectNames of any existing connectors for the specified
     * protocol.
     *
     * @param protocol A protocol as returned by getSupportedProtocols
     */
    public String[] getConnectors(String protocol);

    /**
     * Gets the ObjectNames of any existing connectors associated with this
     * container.
     */
    public String[] getConnectors();
}
