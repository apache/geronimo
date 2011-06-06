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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WSDLUtils {

    private static final Logger LOG = LoggerFactory.getLogger(WSDLUtils.class);

    public static void trimDefinition(Definition def, Map<QName, Set<String>> servicePortNamesMap) {
        @SuppressWarnings("unchecked")
        Map<QName, Service> services = def.getServices();
        if (services == null) {
            return;
        }
        ArrayList<QName> servicesToRemove = new ArrayList<QName>(services.size());

        for (Map.Entry<QName, Service> serviceEntry : services.entrySet()) {
            QName currServiceName = serviceEntry.getKey();
            if (servicePortNamesMap.containsKey(currServiceName)) {
                Service service = serviceEntry.getValue();
                trimService(service, servicePortNamesMap.get(currServiceName));
            } else {
                servicesToRemove.add(currServiceName);
            }
        }

        for (QName serviceToRemove : servicesToRemove) {
            def.removeService(serviceToRemove);
        }
    }

    public static void trimDefinition(Definition def, String serviceName, String portName) {
        @SuppressWarnings("unchecked")
        Map<QName, Service> services = def.getServices();
        if (services == null) {
            return;
        }
        ArrayList<QName> servicesToRemove = new ArrayList<QName>(services.size());

        for (Map.Entry<QName, Service> serviceEntry : services.entrySet()) {
            QName currServiceName = serviceEntry.getKey();
            if (currServiceName.getLocalPart().equals(serviceName)) {
                Service service = serviceEntry.getValue();
                trimService(service, portName);
            } else {
                servicesToRemove.add(currServiceName);
            }
        }

        for (QName serviceToRemove : servicesToRemove) {
            def.removeService(serviceToRemove);
        }
    }

    public static void trimService(Service service, String portName) {
        trimService(service, Collections.singleton(portName));
    }

    public static void trimService(Service service, Set<String> portName) {
        @SuppressWarnings("unchecked")
        Map<String, Port> ports = service.getPorts();
        if (ports == null) {
            return;
        }
        ArrayList<String> portsToRemove = new ArrayList<String>(ports.size());
        for (Map.Entry<String, Port> portEntry : ports.entrySet()) {
            String currPortName = portEntry.getKey();
            if (!portName.contains(currPortName)) {
                portsToRemove.add(currPortName);
            }
        }
        for (String portToRemove : portsToRemove) {
            service.removePort(portToRemove);
        }
    }

    public static void updateLocations(Definition def, Map<QName, Map<String, String>> servicePortNameLocationMap) {
        @SuppressWarnings("unchecked")
        Map<QName, Service> services = def.getServices();
        if (services == null) {
            return;
        }
        for (Map.Entry<QName, Service> serviceEntry : services.entrySet()) {
            Service service = serviceEntry.getValue();
            Map<String, String> portNameLocationMap = servicePortNameLocationMap.get(service.getQName());
            if (portNameLocationMap != null) {
                updateLocations(service, portNameLocationMap);
            }
        }
    }

    public static void updateLocations(Service service, Map<String, String> portNameLocationsMap) {
        @SuppressWarnings("unchecked")
        Map<String, Port> ports = service.getPorts();
        if (ports == null) {
            return;
        }
        for (Map.Entry<String, Port> portEntry : ports.entrySet()) {
            Port port = portEntry.getValue();
            String location = portNameLocationsMap.get(port.getName());
            if (location != null) {
                updateLocations(port, location);
            }
        }
    }

    public static void updateLocations(Definition def, String location) {
        @SuppressWarnings("unchecked")
        Map<QName, Service> services = def.getServices();
        if (services == null) {
            return;
        }
        for (Map.Entry<QName, Service> serviceEntry : services.entrySet()) {
            Service service = serviceEntry.getValue();
            updateLocations(service, location);
        }
    }

    public static void updateLocations(Service service, String location) {
        @SuppressWarnings("unchecked")
        Map<String, Port> ports = service.getPorts();
        if (ports == null) {
            return;
        }
        for (Map.Entry<String, Port> portEntry : ports.entrySet()) {
            Port port = portEntry.getValue();
            updateLocations(port, location);
        }
    }

    public static void updateLocations(Port port, String location) {
        List<?> exts = port.getExtensibilityElements();
        if (exts == null) {
            return;
        }
        for (Object extension : exts) {
            if (extension instanceof SOAP12Address) {
                ((SOAP12Address) extension).setLocationURI(location);
            } else if (extension instanceof SOAPAddress) {
                ((SOAPAddress) extension).setLocationURI(location);
            } else if (extension instanceof HTTPAddress) {
                ((HTTPAddress) extension).setLocationURI(location);
            }
        }
    }
}
