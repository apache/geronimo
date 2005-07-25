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

/**
 * The common configuration settings for a web container (currently, Tomcat or
 * Jetty).
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public interface WebContainer {
    public final static String PROTOCOL_HTTP = "http";
    public final static String PROTOCOL_HTTPS = "https";
    public final static String PROTOCOL_AJP = "ajp";

    /**
     * Gets the protocols which this container can configure connectors for.
     */
    public String[] getSupportedProtocols();
    
    /**
     * Creates a new connector, and returns the ObjectName for it.  Note that
     * the connector may well require further customization before being fully
     * functional (e.g. SSL settings for an HTTPS connector).
     */
    public String addConnector(String uniqueName, String protocol, String host, int port);

    /**
     * Gets the ObjectNames of any existing connectors for the specified
     * protocol.
     *
     * @param protocol A protocol as returned by getSupportedProtocols
     */
    public String[] getConnectors(String protocol);
}
