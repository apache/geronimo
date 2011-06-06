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

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.tools.common.extensions.soap.SoapAddress;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.apache.geronimo.webservices.saaj.SAAJUniverse;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CXFWebServiceContainer implements WebServiceContainer {

    private static final Logger LOG = LoggerFactory.getLogger(CXFWebServiceContainer.class);

    protected final GeronimoDestination destination;

    protected final Bus bus;

    protected final CXFEndpoint endpoint;

    protected final Bundle bundle;

    public CXFWebServiceContainer(Bus bus, Object target, Bundle bundle) {
        this.bus = bus;
        this.bundle = bundle;
        List<String> ids = new ArrayList<String>();
        ids.add("http://schemas.xmlsoap.org/wsdl/soap/");

        DestinationFactoryManager destinationFactoryManager = bus
                .getExtension(DestinationFactoryManager.class);
        GeronimoDestinationFactory factory = new GeronimoDestinationFactory(bus);
        factory.setTransportIds(ids);

        destinationFactoryManager.registerDestinationFactory(
                "http://cxf.apache.org/transports/http/configuration", factory);
        destinationFactoryManager.registerDestinationFactory(
                "http://cxf.apache.org/bindings/xformat", factory);
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

    static String getBaseUri(URI request) {
        return request.getScheme() + "://" + request.getHost() + ":" + request.getPort() + request.getPath();
    }

    public void invoke(Request request, Response response) throws Exception {
        this.endpoint.updateAddress(request.getURI());
        if (request.getMethod() == Request.GET) {
            processGET(request, response);
        } else {
            processPOST(request, response);
        }
    }

    protected void processGET(Request request, Response response) throws Exception {
        if (request.getParameter("wsdl") != null || request.getParameter("WSDL") != null ||
                   request.getParameter("xsd") != null || request.getParameter("XSD") != null) {
            GeronimoQueryHandler queryHandler = new GeronimoQueryHandler(this.bus);
            URI requestURI = request.getURI();
            EndpointInfo ei = this.destination.getEndpointInfo();
            // update service port location on each request
            SoapAddress address = ei.getExtensor(SoapAddress.class);
            address.setLocationURI(getBaseUri(requestURI));
            OutputStream out = response.getOutputStream();
            String baseUri = requestURI.toString();
            response.setContentType("text/xml");
            queryHandler.writeResponse(baseUri, null, ei, out);
        } else if (endpoint.isSOAP11()) {
            EndpointInfo ei = this.destination.getEndpointInfo();
            response.setContentType("text/html");
            PrintWriter pw = new PrintWriter(response.getOutputStream());
            pw.write("<html><title>Web Service</title><body>");
            pw.write("Hi, this is '" + ei.getService().getName().getLocalPart() + "' web service.");
            pw.write("</body></html>");
            pw.flush();
        } else {
            processPOST(request, response);
        }
    }

    protected void processPOST(Request request, Response response) throws Exception {
        SAAJUniverse universe = new SAAJUniverse();
        universe.set(SAAJUniverse.DEFAULT);
        try {
            destination.invoke(request, response);
        } finally {
            universe.unset();
        }
    }

    public void getWsdl(Request request, Response response) throws Exception {
        invoke(request, response);
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
        getDefaultBus();
        return new ExtensionManagerBus();
    }

    /*
     * Ensure the Spring bus is initialized with the CXF module classloader
     * instead of the application classloader.
     */
    public static Bus getDefaultBus() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(CXFEndpoint.class.getClassLoader());
        try {
            return BusFactory.getDefaultBus();
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

}
