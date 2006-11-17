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

import java.util.Set;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * @version $Rev$ $Date$
 */
public class HandlerInfoInfo {
    private final Set portNames;
    private final Class handlerClass;
    private final Map handlerConfig;
    private final QName[] soapHeaders;
    private final Set soapRoles;

    public HandlerInfoInfo(Set portNames, Class handlerClass, Map handlerConfig, QName[] soapHeaders, Set soapRoles) {
        this.portNames = portNames;
        this.handlerClass = handlerClass;
        this.handlerConfig = handlerConfig;
        this.soapHeaders = soapHeaders;
        this.soapRoles = soapRoles;
    }

    public Set getPortNames() {
        return portNames;
    }

    public Class getHandlerClass() {
        return handlerClass;
    }

    public Map getHandlerConfig() {
        return handlerConfig;
    }

    public QName[] getSoapHeaders() {
        return soapHeaders;
    }

    public Set getSoapRoles() {
        return soapRoles;
    }
}
