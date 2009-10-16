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
 * @version $Rev$ $Date$
 */
public interface EJBManager extends NetworkManager {
    public final static String PROTOCOL_RMI = "RMI";
    public final static String PROTOCOL_IIOP = "IIOP";
    public final static String PROTOCOL_HTTP = "HTTP";
    public final static String PROTOCOL_HTTPS = "HTTPS";
    public final static String PROTOCOL_HTTP_SOAP = "HTTPSOAP";

    /**
     * Creates a new connector, and returns the ObjectName for it.  Note that
     * the connector may well require further customization before being fully
     * functional (e.g. SSL settings for a secure connector).
     */
    public String addConnector(String uniqueName, String protocol, String threadPoolObjectName, String host, int port);
}
