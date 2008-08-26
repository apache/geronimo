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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.schema.SchemaReference;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cxf.Bus;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.http.WSDLQueryHandler;

public class GeronimoQueryHandler extends WSDLQueryHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GeronimoQueryHandler.class);

    public GeronimoQueryHandler(Bus bus) {
        super(bus);
    }

    protected void updateDefinition(Definition def,
                                    Map<String, Definition> done,
                                    Map<String, SchemaReference> doneSchemas,
                                    String base,
                                    EndpointInfo ei) {
        if (done.get("") == def) {
            QName serviceName = ei.getService().getName();
            String portName = ei.getName().getLocalPart();
            updateServices(serviceName, portName, def, base);
        }
        super.updateDefinition(def, done, doneSchemas, base, ei);
    }

    private void updateServices(QName serviceName, 
                                String portName, 
                                Definition def, 
                                String baseUri) {
        boolean updated = false;
        Map services = def.getServices();
        if (services != null) {
            ArrayList<QName> servicesToRemove = new ArrayList<QName>();
            
            Iterator serviceIterator = services.entrySet().iterator();
            while (serviceIterator.hasNext()) {
                Map.Entry serviceEntry = (Map.Entry) serviceIterator.next();
                QName currServiceName = (QName) serviceEntry.getKey();
                if (currServiceName.equals(serviceName)) {
                    Service service = (Service) serviceEntry.getValue();
                    updatePorts(portName, service, baseUri);
                    updated = true;
                } else {
                    servicesToRemove.add(currServiceName);
                }
            }
            
            for (QName serviceToRemove : servicesToRemove) {
                def.removeService(serviceToRemove);                
            }
        }
        if (!updated) {
            LOG.warn("WSDL '" + serviceName.getLocalPart() + "' service not found.");
        }
    }

    private void updatePorts(String portName, 
                             Service service, 
                             String baseUri) {
        boolean updated = false;
        Map ports = service.getPorts();
        if (ports != null) {
            ArrayList<String> portsToRemove = new ArrayList<String>();
            
            Iterator portIterator = ports.entrySet().iterator();
            while (portIterator.hasNext()) {
                Map.Entry portEntry = (Map.Entry) portIterator.next();
                String currPortName = (String) portEntry.getKey();
                if (currPortName.equals(portName)) {
                    Port port = (Port) portEntry.getValue();
                    updatePortLocation(port, baseUri);
                    updated = true;
                } else {
                    portsToRemove.add(currPortName);
                }
            }
            
            for (String portToRemove : portsToRemove) {
                service.removePort(portToRemove);               
            }
        }
        if (!updated) {
            LOG.warn("WSDL '" + portName + "' port not found.");
        }
    }

    private void updatePortLocation(Port port, 
                                    String baseUri) {
        List<?> exts = port.getExtensibilityElements();
        if (exts != null) {
            for (Object extension : exts) {
                if (extension instanceof SOAP12Address) {
                    ((SOAP12Address)extension).setLocationURI(baseUri);
                } else if (extension instanceof SOAPAddress) {
                    ((SOAPAddress)extension).setLocationURI(baseUri);
                }
            }
        }
    }

}
