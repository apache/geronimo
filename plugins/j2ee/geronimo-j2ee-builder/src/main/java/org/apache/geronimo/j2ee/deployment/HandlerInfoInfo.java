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

package org.apache.geronimo.j2ee.deployment;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

/**
 * @version $Rev$ $Date$
 */
public class HandlerInfoInfo {
    private final Set<String> portNames;
    private final Class<?> handlerClass;
    private final Map<String, String> handlerConfig;
    private final List<QName> soapHeaders;
    private final Set<String> soapRoles;

    public HandlerInfoInfo(Set<String> portNames, Class<?> handlerClass, Map<String, String> handlerConfig, List<QName> soapHeaders, Set<String> soapRoles) {
        this.portNames = portNames;
        this.handlerClass = handlerClass;
        this.handlerConfig = handlerConfig;
        this.soapHeaders = soapHeaders;
        this.soapRoles = soapRoles;
    }

    public Set<String> getPortNames() {
        return portNames;
    }

    public Class<?> getHandlerClass() {
        return handlerClass;
    }

    public Map<String, String> getHandlerConfig() {
        return handlerConfig;
    }

    public List<QName> getSoapHeaders() {
        return soapHeaders;
    }

    public Set<String> getSoapRoles() {
        return soapRoles;
    }
}
