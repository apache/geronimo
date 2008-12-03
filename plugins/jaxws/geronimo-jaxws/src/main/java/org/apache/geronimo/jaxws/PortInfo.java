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

public class PortInfo implements Serializable {

    private String serviceName;

    private String portName;

    private String seiInterfaceName;

    private String wsdlFile;

    private String servletLink;

    private String handlersAsXML;

    private Boolean mtomEnabled;

    private String binding;
    
    private QName wsdlPort;
    
    private QName wsdlService;
    
    private String location;

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

    public void setEnableMTOM(Boolean mtomEnabled) {
        this.mtomEnabled = mtomEnabled;
    }

    public Boolean isMTOMEnabled() {
        return this.mtomEnabled;
    }

    public void setProtocolBinding(String binding) {
        this.binding = binding;
    }

    public String getProtocolBinding() {
        return binding;
    }

    /*
     * This is a bit tricky here since JAXB generated classes are not serializable, 
     * so serialize the handler chain to XML and pass it as a String. 
     */
    
    public void setHandlers(Class type, Object handlerChain) throws Exception {
        if (handlerChain == null) {
            return;
        }

        JAXBContext ctx = JAXBContext.newInstance(type);
        Marshaller m = ctx.createMarshaller();
        StringWriter writer = new StringWriter();
        /*
         * Since HandlerChainsType is a type, have to wrap it into some element
         */
        JAXBElement element = 
            new JAXBElement(HandlerChainsUtils.HANDLER_CHAINS_QNAME, type, handlerChain);
        m.marshal(element, writer);

        this.handlersAsXML = writer.toString();
    }

    public <T>T getHandlers(Class<T> type) throws Exception {
        return HandlerChainsUtils.toHandlerChains(this.handlersAsXML, type);
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
    
    /*
     * private String serviceName; private String portName; private String
     * seiInterfaceName; private String wsdlFile; private String servletLink;
     */
    public String toString() {
        return "[" + serviceName + ":" + portName + ":" + seiInterfaceName
                + ":" + wsdlFile + "]";
    }

    public String getHandlersAsXML() {
        return handlersAsXML;
    }

    public void setHandlersAsXML(String handlersAsXML) {
        this.handlersAsXML = handlersAsXML;
    }
}
