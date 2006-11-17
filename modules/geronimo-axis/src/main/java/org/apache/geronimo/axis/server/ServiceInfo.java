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
package org.apache.geronimo.axis.server;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.axis.description.JavaServiceDesc;

/**
 * @version $Rev$ $Date$
 */
public class ServiceInfo implements Serializable {
    private final JavaServiceDesc serviceDesc;
    /** List of javax.xml.rpc.handler.HandlerInfo objects */
    private final List handlerInfos;

    private final Map wsdlMap;

    public ServiceInfo(JavaServiceDesc serviceDesc, List handlerInfos, Map wsdlMap) {
        this.serviceDesc = serviceDesc;
        this.handlerInfos = handlerInfos;
        this.wsdlMap = wsdlMap;
    }

    public JavaServiceDesc getServiceDesc() {
        return serviceDesc;
    }

    /** List of javax.xml.rpc.handler.HandlerInfo objects */
    public List getHandlerInfos() {
        return handlerInfos;
    }

    public Map getWsdlMap() {
        return wsdlMap;
    }

}
