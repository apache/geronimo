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
package org.apache.geronimo.cxf;

import javax.naming.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.Bus;
import org.apache.cxf.binding.xml.XMLConstants;
import org.apache.cxf.tools.common.extensions.soap.SoapAddress;
import org.apache.cxf.tools.util.SOAPBindingUtil;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.wsdl11.ServiceWSDLBuilder;

import org.apache.geronimo.webservices.WebServiceContainer;
import org.xmlsoap.schemas.wsdl.http.AddressType;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;

import java.net.URL;
import java.util.List;
import java.util.ArrayList;

// TODO consider putting most of this in the CXFWebServiceContaInerFactoryGBean
public class CXFWebServiceContainer implements WebServiceContainer {

    private static final Log LOG = LogFactory.getLog(CXFWebServiceContainer.class);
    
    private final GeronimoDestination destination;

    private final Bus bus;

    private final CXFEndpoint endpoint;

    private URL configurationBaseUrl;

    public CXFWebServiceContainer(PortInfo portInfo,
                                  Object target,
                                  Bus bus,
                                  Context context,
                                  URL configurationBaseUrl) {
        this.bus = bus;
        this.configurationBaseUrl = configurationBaseUrl;

        List ids = new ArrayList();
        ids.add("http://schemas.xmlsoap.org/wsdl/soap/http");

        bus.setExtension(new JNDIResolver(context), JNDIResolver.class);
        bus.setExtension(portInfo, PortInfo.class);

        DestinationFactoryManager destinationFactoryManager = bus
                .getExtension(DestinationFactoryManager.class);
        GeronimoDestinationFactory factory = new GeronimoDestinationFactory(bus);
        factory.setTransportIds(ids);
        destinationFactoryManager.registerDestinationFactory(
                "http://cxf.apache.org/transports/http/configuration", factory);
        destinationFactoryManager.registerDestinationFactory(
                "http://www.w3.org/2003/05/soap/bindings/HTTP/", factory);
        destinationFactoryManager.registerDestinationFactory(
                "http://schemas.xmlsoap.org/soap/http", factory);
        destinationFactoryManager.registerDestinationFactory(
                "http://schemas.xmlsoap.org/wsdl/http/", factory);
        destinationFactoryManager.registerDestinationFactory(
                "http://schemas.xmlsoap.org/wsdl/soap/", factory);
        destinationFactoryManager.registerDestinationFactory(
                "http://schemas.xmlsoap.org/wsdl/soap/http", factory);
        destinationFactoryManager.registerDestinationFactory(
                XMLConstants.NS_XML_FORMAT, factory);

        endpoint = publishEndpoint(target);
        destination = (GeronimoDestination) endpoint.getServer()
                .getDestination();
    }

    public void invoke(Request request, Response response) throws Exception {
        destination.invoke(request, response);
    }

    public void getWsdl(Request request, Response response) throws Exception {
        WSDLWriter wsdlWriter = WSDLFactory.newInstance().newWSDLWriter();

        EndpointInfo ei = this.destination.getEndpointInfo();

        Definition def = new ServiceWSDLBuilder(ei.getService()).build();
        
        Service service = def.getService(ei.getService().getName());
        Port port = service.getPort(ei.getName().getLocalPart());
        if (port == null) {
            LOG.warn("No WSDL port found for: " + ei.getName());
        } else {
            List<?> exts = port.getExtensibilityElements();
            if (exts != null && exts.size() > 0) {
                ExtensibilityElement el = (ExtensibilityElement) exts.get(0);
                if (SOAPBindingUtil.isSOAPAddress(el)) {
                    SoapAddress add = SOAPBindingUtil.getSoapAddress(el);
                    add.setLocationURI(request.getURI().toString());
                }
                if (el instanceof AddressType) {
                    AddressType add = (AddressType) el;
                    add.setLocation(request.getURI().toString());
                }
            }
        }

        wsdlWriter.writeWSDL(def, response.getOutputStream());
    }

    public void destroy() {
        if (this.endpoint != null) {
            this.endpoint.stop();
        }
    }

    private CXFEndpoint publishEndpoint(Object target) {
        assert target != null : "null target received";

        CXFEndpoint ep = new CXFEndpoint(bus, configurationBaseUrl, target,
                (String) null);
        ep.publish("http://nopath");
        return ep;
    }

}
