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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WSDLUtils {

    private static final Log LOG = LogFactory.getLog(WSDLUtils.class);
    
    public static void trimDefinition(Definition def, 
                                      String serviceName, 
                                      String portName) { 
        Map services = def.getServices();
        if (services != null) {
            ArrayList<QName> servicesToRemove = new ArrayList<QName>(services.size());
            
            Iterator serviceIterator = services.entrySet().iterator();
            while (serviceIterator.hasNext()) {
                Map.Entry serviceEntry = (Map.Entry) serviceIterator.next();
                QName currServiceName = (QName) serviceEntry.getKey();
                if (currServiceName.getLocalPart().equals(serviceName)) {
                    Service service = (Service) serviceEntry.getValue();
                    trimService(service, portName);
                } else {
                    servicesToRemove.add(currServiceName);
                }
            }
            
            for (QName serviceToRemove : servicesToRemove) {
                def.removeService(serviceToRemove);                
            }
        }
    }

    public static void trimService(Service service,
                                   String portName) { 
        Map ports = service.getPorts();
        if (ports != null) {
            ArrayList<String> portsToRemove = new ArrayList<String>(ports.size());
            
            Iterator portIterator = ports.entrySet().iterator();
            while (portIterator.hasNext()) {
                Map.Entry portEntry = (Map.Entry) portIterator.next();
                String currPortName = (String) portEntry.getKey();
                if (!currPortName.equals(portName)) {
                    portsToRemove.add(currPortName);
                }
            }
            
            for (String portToRemove : portsToRemove) {
                service.removePort(portToRemove);               
            }
        }
    }
    
    public static void updateLocations(Definition def, 
                                       String location) {
        Map services = def.getServices();
        if (services != null) {
            Iterator serviceIterator = services.entrySet().iterator();
            while (serviceIterator.hasNext()) {
                Map.Entry serviceEntry = (Map.Entry) serviceIterator.next();
                Service service = (Service) serviceEntry.getValue();                
                updateLocations(service, location);
            }
        }            
    }
    
    public static void updateLocations(Service service,
                                       String location) {
        boolean updated = false;
        Map ports = service.getPorts();
        if (ports != null) {
            Iterator portIterator = ports.entrySet().iterator();
            while (portIterator.hasNext()) {
                Map.Entry portEntry = (Map.Entry) portIterator.next();
                Port port = (Port) portEntry.getValue();
                updateLocations(port, location);
            }
        }
    }
    
    public static void updateLocations(Port port, 
                                       String location) {
        List<?> exts = port.getExtensibilityElements();
        if (exts != null) {
            for (Object extension : exts) {
                if (extension instanceof SOAP12Address) {
                    ((SOAP12Address)extension).setLocationURI(location);
                } else if (extension instanceof SOAPAddress) {
                    ((SOAPAddress)extension).setLocationURI(location);
                } else if (extension instanceof HTTPAddress) {
                    ((HTTPAddress)extension).setLocationURI(location);
                }
            }
        }
    }

}
