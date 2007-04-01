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

import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.bus.CXFBusFactory;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.tools.common.extensions.soap.SoapAddress;
import org.apache.cxf.tools.util.SOAPBindingUtil;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.wsdl11.ServiceWSDLBuilder;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.apache.geronimo.webservices.saaj.SAAJUniverse;
import org.xmlsoap.schemas.wsdl.http.AddressType;

public abstract class CXFWebServiceContainer implements WebServiceContainer {

    private static final Log LOG = LogFactory.getLog(CXFWebServiceContainer.class);
    
    protected final GeronimoDestination destination;

    protected final Bus bus;

    protected final CXFEndpoint endpoint;

    protected URL configurationBaseUrl;

    public CXFWebServiceContainer(Bus bus,
                                  URL configurationBaseUrl,
                                  Object target) {
        this.bus = bus;
        this.configurationBaseUrl = configurationBaseUrl;

        List ids = new ArrayList();
        ids.add("http://schemas.xmlsoap.org/wsdl/soap/http");
        
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
                "http://schemas.xmlsoap.org/wsdl/soap/http", factory);

        endpoint = publishEndpoint(target);
        destination = (GeronimoDestination) endpoint.getServer().getDestination();
    }

    public void invoke(Request request, Response response) throws Exception {
        if (request.getMethod() == Request.GET) {
            EndpointInfo ei = this.destination.getEndpointInfo();
            response.setContentType("text/html");
            PrintWriter pw = new PrintWriter(response.getOutputStream());
            pw.write("<html><title>Web Service</title><body>");
            pw.write("Hi, this is '" + ei.getService().getName().getLocalPart() + "' web service.");
            pw.write("</body></html>");
            pw.flush();
        } else {
            SAAJUniverse universe = new SAAJUniverse();
            universe.set(SAAJUniverse.SUN);
            try {
                destination.invoke(request, response);
            } finally {
                universe.unset();
            }
        }
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
                URI requestURI = request.getURI();
                URI serviceURI = new URI(requestURI.getScheme(), null, 
                                         requestURI.getHost(), requestURI.getPort(), 
                                         requestURI.getPath(), null, null);
                ExtensibilityElement el = (ExtensibilityElement) exts.get(0);
                if (SOAPBindingUtil.isSOAPAddress(el)) {
                    SoapAddress add = SOAPBindingUtil.getSoapAddress(el);
                    add.setLocationURI(serviceURI.toString());
                }
                if (el instanceof AddressType) {
                    AddressType add = (AddressType) el;
                    add.setLocation(serviceURI.toString());
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

    abstract protected CXFEndpoint publishEndpoint(Object target);
        
    /*
     * Ensure the bus created is unqiue and non-shared. 
     * The very first bus created is set as a default bus which then can
     * be (re)used in other places.
     */
    public static Bus getBus() {
        CXFBusFactory busFactory = new CXFBusFactory();
        Bus bus = busFactory.createBus();
        Bus defaultBus = BusFactory.getDefaultBus(false);
        if (defaultBus == null) {
            BusFactory.setDefaultBus(bus);
            return busFactory.createBus();
        } else if (defaultBus == bus) {
            return busFactory.createBus();
        } else {
            return bus;
        }
    }

}
