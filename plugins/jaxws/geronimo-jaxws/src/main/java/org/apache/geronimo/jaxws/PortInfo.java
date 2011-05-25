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
package org.apache.geronimo.jaxws;

import java.io.Serializable;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.apache.geronimo.jaxws.feature.AddressingFeatureInfo;
import org.apache.geronimo.jaxws.feature.MTOMFeatureInfo;
import org.apache.geronimo.jaxws.feature.RespectBindingFeatureInfo;
import org.apache.geronimo.jaxws.info.HandlerChainsInfo;

public class PortInfo implements Serializable {

    private String serviceName;

    private String portName;

    private String seiInterfaceName;

    private String wsdlFile;

    private String servletLink;

    private HandlerChainsInfo handlerChainsInfo;

    private String binding;

    private QName wsdlPort;

    private QName wsdlService;

    private String location;

    private MTOMFeatureInfo mtomFeatureInfo;

    private AddressingFeatureInfo addressingFeatureInfo;

    private RespectBindingFeatureInfo respectBindingFeatureInfo;

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

    public String getServiceLink() {
        return servletLink;
    }

    public void setServiceLink(String sl) {
        servletLink = sl;
    }

    public String getWsdlFile() {
        return wsdlFile;
    }

    public void setWsdlFile(String wf) {
        wsdlFile = wf;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String sn) {
        serviceName = sn;
    }

    public void setProtocolBinding(String binding) {
        this.binding = binding;
    }

    public String getProtocolBinding() {
        return binding;
    }

    public QName getWsdlPort() {
        return wsdlPort;
    }

    public void setWsdlPort(QName wsdlPort) {
        this.wsdlPort = wsdlPort;
    }

    public QName getWsdlService() {
        return wsdlService;
    }

    public void setWsdlService(QName wsdlService) {
        this.wsdlService = wsdlService;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public MTOMFeatureInfo getMtomFeatureInfo() {
        return mtomFeatureInfo;
    }

    public void setMtomFeatureInfo(MTOMFeatureInfo mtomFeatureInfo) {
        this.mtomFeatureInfo = mtomFeatureInfo;
    }

    public AddressingFeatureInfo getAddressingFeatureInfo() {
        return addressingFeatureInfo;
    }

    public void setAddressingFeatureInfo(AddressingFeatureInfo addressingFeatureInfo) {
        this.addressingFeatureInfo = addressingFeatureInfo;
    }

    public RespectBindingFeatureInfo getRespectBindingFeatureInfo() {
        return respectBindingFeatureInfo;
    }

    public void setRespectBindingFeatureInfo(RespectBindingFeatureInfo respectBindingFeatureInfo) {
        this.respectBindingFeatureInfo = respectBindingFeatureInfo;
    }

    public HandlerChainsInfo getHandlerChainsInfo() {
        return handlerChainsInfo;
    }

    public void setHandlerChainsInfo(HandlerChainsInfo handlerChainsInfo) {
        this.handlerChainsInfo = handlerChainsInfo;
    }

    @Override
    public String toString() {
        return "PortInfo [serviceName=" + serviceName + ", portName=" + portName + ", seiInterfaceName=" + seiInterfaceName + ", wsdlFile=" + wsdlFile + ", servletLink=" + servletLink
                +  ", binding=" + binding + ", wsdlPort=" + wsdlPort + ", wsdlService=" + wsdlService + ", location=" + location + ", mtomFeatureInfo="
                + mtomFeatureInfo + ", addressingFeatureInfo=" + addressingFeatureInfo + ", respectBindingFeatureInfo=" + respectBindingFeatureInfo + "]";
    }

}
