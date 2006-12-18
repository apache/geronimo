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

import org.apache.cxf.Bus;
import org.apache.cxf.binding.xml.XMLConstants;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.geronimo.webservices.WebServiceContainer;

import java.util.List;
import java.util.ArrayList;

//TODO consider putting most of this in the CXFWebServiceContaInerFactoryGBean
public class CXFWebServiceContainer implements WebServiceContainer {

    private final GeronimoDestination destination;
    private final Bus bus;


    public CXFWebServiceContainer(PortInfo portInfo, Object target, Bus bus) {
        //TODO actually use portInfo
        this.bus = bus;

        List ids = new ArrayList();
        ids.add("http://schemas.xmlsoap.org/wsdl/soap/http");

        DestinationFactoryManager destinationFactoryManager = bus.getExtension(DestinationFactoryManager.class);
        GeronimoDestinationFactory factory = new GeronimoDestinationFactory(bus);
        factory.setTransportIds(ids);
        destinationFactoryManager.registerDestinationFactory("http://cxf.apache.org/transports/http/configuration", factory);
        destinationFactoryManager.registerDestinationFactory("http://www.w3.org/2003/05/soap/bindings/HTTP/", factory);
        destinationFactoryManager.registerDestinationFactory("http://schemas.xmlsoap.org/soap/http", factory);
        destinationFactoryManager.registerDestinationFactory("http://schemas.xmlsoap.org/wsdl/http/", factory);
        destinationFactoryManager.registerDestinationFactory("http://schemas.xmlsoap.org/wsdl/soap/", factory);
        destinationFactoryManager.registerDestinationFactory("http://schemas.xmlsoap.org/wsdl/soap/http", factory);
        destinationFactoryManager.registerDestinationFactory(XMLConstants.NS_XML_FORMAT, factory);
        EndpointImpl publishedEndpoint = publishEndpoint(target);
        destination = (GeronimoDestination) publishedEndpoint.getServer().getDestination();
    }

    public void invoke(Request request, Response response) throws Exception {

            destination.invoke(request, response);
    }


    public void getWsdl(Request request, Response response) throws Exception {
    }

    private  EndpointImpl publishEndpoint(Object target) {

        assert target != null : "null target received";

        EndpointImpl ep = new EndpointImpl(bus, target, (String)null);
        ep.publish("http://nopath");
        return ep;

    }

}
