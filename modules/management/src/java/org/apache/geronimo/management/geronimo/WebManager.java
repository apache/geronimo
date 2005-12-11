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
 * Specialization of NetworkManager for web containers.
 *
 * @version $Rev$ $Date$
 */
public interface WebManager extends NetworkManager {
    public final static String PROTOCOL_HTTP = "HTTP";
    public final static String PROTOCOL_HTTPS = "HTTPS";
    public final static String PROTOCOL_AJP = "AJP";

    /**
     * Gets the name of the product that this manager manages.
     */
    public String getProductName(); //todo: move into NetworkManager

    /**
     * Creates a new connector, and returns the ObjectName for it.  Note that
     * the connector may well require further customization before being fully
     * functional (e.g. SSL settings for a secure connector).  This may need
     * to be done before starting the resulting connector.
     *
     * @param containerObjectName The ObjectName of the container that the
     *                            connector should be added to
     * @param uniqueName          A name fragment that's unique to this
     *                            connector
     * @param protocol            The protocol that the connector should use
     * @param host                The host name or IP that the connector should
     *                            listen on
     * @param port                The port that the connector should listen on
     *
     * @return The ObjectName of the new connector.
     */
    public String addConnector(String containerObjectName, String uniqueName, String protocol, String host, int port);

    /**
     * Gets the ObjectName of the WebAccessLog implementation for a web
     * container.  May be null if the access log cannot be managed.
     *
     * @param containerObjectName The ObjectName of the container whose access
     *                            log you're interested in.
     *
     */
    public String getAccessLog(String containerObjectName);
}
