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

package org.apache.geronimo.axis2;

import org.apache.axis2.jaxws.javaee.PortComponentHandlerType;

import java.io.Serializable;
import java.util.List;


public class PortInfo implements Serializable {

    private String serviceName;
    private String portName;
    private String seiInterfaceName;
    private String wsdlFile;
    private String servletLink;

    private List<PortComponentHandlerType> handlers;

    public String getPortName() {
        return portName;
    }

    public void setPortName(String pn) {
        portName = pn;
    }

    public String getServiceEndpointInterfaceName() {
        return seiInterfaceName;
    }

    public void setServiceEndpointInterfaceName(String sei) {
        seiInterfaceName = sei;
    }

    public String getServletLink() {
        return servletLink;
    }

    public void setServletLink(String sl) {
        servletLink = sl;
    }

    public String getWsdlFile() {
        return wsdlFile;
    }

    public void setWsdlFile(String wf) {
        wsdlFile = wf;
    }

    public void setHandlers(List<PortComponentHandlerType> h) {
        handlers = h;
    }

    public List<PortComponentHandlerType> getHandlers() {
        return handlers;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String sn) {
        serviceName = sn;
    }

    public String toString() {
        return "[" + serviceName + ":" + portName + ":" + seiInterfaceName + ":" + wsdlFile + "]";
    }
}
