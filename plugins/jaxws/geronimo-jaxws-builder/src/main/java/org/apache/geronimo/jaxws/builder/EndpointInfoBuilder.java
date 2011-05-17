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
package org.apache.geronimo.jaxws.builder;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLLocator;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.soap.AddressingFeature;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.jaxws.JAXWSUtils;
import org.apache.geronimo.jaxws.client.EndpointInfo;
import org.apache.geronimo.jaxws.feature.AddressingFeatureInfo;
import org.apache.geronimo.jaxws.feature.MTOMFeatureInfo;
import org.apache.geronimo.jaxws.feature.RespectBindingFeatureInfo;
import org.apache.geronimo.jaxws.feature.WebServiceFeatureInfo;
import org.apache.geronimo.jaxws.wsdl.CatalogJarWSDLLocator;
import org.apache.geronimo.jaxws.wsdl.CatalogWSDLLocator;
import org.apache.geronimo.xbeans.geronimo.naming.GerPortPropertyType;
import org.apache.geronimo.xbeans.geronimo.naming.GerPortType;
import org.apache.geronimo.xbeans.geronimo.naming.GerServiceRefType;
import org.apache.openejb.jee.Addressing;
import org.apache.openejb.jee.PortComponentRef;
import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndpointInfoBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointInfoBuilder.class);

    private Module module;

    private Bundle bundle;

    private URI wsdlURI;

    private QName serviceQName;

    private Class serviceClass;

    private GerServiceRefType serviceRefType;

    private Map<Object, EndpointInfo> portInfoMap = new HashMap<Object, EndpointInfo>();

    private Map<Class<?>, PortComponentRef> portComponentRefMap;

    public EndpointInfoBuilder(Class serviceClass,
                               GerServiceRefType serviceRefType,
                               Map<Class<?>, PortComponentRef> portComponentRefMap,
                               Module module,
                               Bundle bundle,
                               URI wsdlURI,
                               QName serviceQName) {
        this.serviceClass = serviceClass;
        this.serviceRefType = serviceRefType;
        this.portComponentRefMap = portComponentRefMap;
        this.module = module;
        this.bundle = bundle;
        this.wsdlURI = wsdlURI;
        this.serviceQName = serviceQName;
    }

    public URI getWsdlURI() {
        return this.wsdlURI;
    }

    public QName getServiceQName() {
        return this.serviceQName;
    }

    public Map<Object, EndpointInfo> getEndpointInfo() {
        return this.portInfoMap;
    }

    public void build() throws DeploymentException {
        if (serviceRefType != null) {
            String wsdlFile = serviceRefType.getWsdlFile();
            if (wsdlFile != null && !wsdlFile.isEmpty()) {
                try {
                    this.wsdlURI = new URI(serviceRefType.getWsdlFile());
                } catch (URISyntaxException e) {
                    throw new DeploymentException("Illegal WSDL location is specified in deployment plan " + wsdlFile, e);
                }
            }
        }

        if (this.wsdlURI == null) {
            // wsdl was not explicitly specified
            if (javax.xml.ws.Service.class == this.serviceClass) {
                // Generic Service class specified.
                // Service API requires a service qname so create a dummy one
                this.serviceQName = new QName("http://noservice", "noservice");

                if (serviceRefType != null) {
                    for (GerPortType gerPort : serviceRefType.getPortArray()) {
                        String portName = gerPort.getPortName().trim();
                        URL location = getLocation(gerPort);
                        String credentialsName = getCredentialsName(gerPort);
                        List<WebServiceFeatureInfo> webServiceFeatureInfos = getWebServiceFeatureInfos(portName);
                        Map<String, Object> props = getProperties(gerPort);
                        EndpointInfo info = new EndpointInfo(location, credentialsName, props, webServiceFeatureInfos);
                        this.portInfoMap.put(portName, info);
                    }
                }

                return;
            } else {
                // Generated Service class specified.
                // Get the wsdl and service qname from the WebServiceClient annotation
                // of the generated Service class
                WebServiceClient webServiceClient =
                    (WebServiceClient) this.serviceClass.getAnnotation(WebServiceClient.class);
                if (webServiceClient != null) {
                    this.wsdlURI = getWSDLLocation(webServiceClient);
                    this.serviceQName = getServiceQName(webServiceClient);
                }

                // wsdl really shouldn't be null at this point
                if (this.wsdlURI == null) {
                    return;
                }
            }
        }

        Catalog catalog = loadCatalog();

        WSDLLocator wsdlLocator = null;
        if (isURL(this.wsdlURI.toString())) {
            wsdlLocator = new CatalogWSDLLocator(this.wsdlURI.toString(), catalog);
        } else {
            wsdlLocator = new CatalogJarWSDLLocator(this.module.getModuleFile(), this.wsdlURI, catalog);
        }

        Definition definition;
        WSDLFactory wsdlFactory;
        try {
            wsdlFactory = WSDLFactory.newInstance();
        } catch (WSDLException e) {
            throw new DeploymentException("Could not create WSDLFactory", e);
        }
        WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
        wsdlReader.setFeature("javax.wsdl.importDocuments", true);
        wsdlReader.setFeature("javax.wsdl.verbose", false);
        try {
            definition = wsdlReader.readWSDL(wsdlLocator);
        } catch (WSDLException e) {
            throw new DeploymentException("Failed to read wsdl document", e);
        } catch (RuntimeException e) {
            throw new DeploymentException(e.getMessage(), e);
        }

        verifyPortComponentList(definition);

        Map<QName, Service> services = definition.getServices();
        if (services.size() == 0) {
            // partial wsdl, return as is

            if (this.serviceRefType != null && this.serviceRefType.isSetServiceCompletion()) {
                LOG.warn("Service completion is not supported with partial wsdl");
            }
        } else {
            // full wsdl

            if (this.serviceRefType != null && this.serviceRefType.isSetServiceCompletion()) {
                throw new DeploymentException("Full wsdl, but service completion supplied");
            }

            Service service = null;
            if (this.serviceQName != null) {
                service = definition.getService(this.serviceQName);
                if (service == null) {
                    throw new DeploymentException(
                            "No service wsdl for supplied service qname "
                                    + this.serviceQName);
                }
            } else if (services.size() == 1) {
                service = services.values().iterator().next();
                this.serviceQName = service.getQName();
            } else {
                throw new DeploymentException(
                        "No service qname supplied, and there are "
                                + services.size() + " services");
            }

            // organize the extra port info
            Map<String, GerPortType> portMap = new HashMap<String, GerPortType>();
            if (serviceRefType != null) {
                GerPortType[] ports = serviceRefType.getPortArray();
                for (int i = 0; i < ports.length; i++) {
                    GerPortType port = ports[i];
                    String portName = port.getPortName().trim();
                    portMap.put(portName, port);
                }
            }

            Map<String, Port> wsdlPortMap = service.getPorts();
            for (Map.Entry<String, Port> entry : wsdlPortMap.entrySet()) {
                String portName = entry.getKey();
                Port port = entry.getValue();

                GerPortType gerPort = portMap.get(portName);

                URL location = (gerPort == null) ? getAddressLocation(port) : getLocation(gerPort);
                // skip non-soap ports
                if (location == null) {
                    continue;
                }
                String credentialsName = (gerPort == null) ? null : getCredentialsName(gerPort);

                Binding binding = port.getBinding();
                if (binding == null) {
                    throw new DeploymentException("No binding for port: " + portName);
                }

                PortType portType = binding.getPortType();
                if (portType == null) {
                    throw new DeploymentException("No portType for binding: " + binding.getQName());
                }

                Map<String, Object> props = getProperties(gerPort);

                List<WebServiceFeatureInfo> webServiceFeatureInfo = getWebServiceFeatureInfos(portType.getQName());
                EndpointInfo info = new EndpointInfo(location, credentialsName, props, webServiceFeatureInfo);
                this.portInfoMap.put(portName, info);
                // prefer first binding listed in wsdl
                if (!this.portInfoMap.containsKey(portType.getQName())) {
                    this.portInfoMap.put(portType.getQName(), info);
                }
            }
        }
    }

    private QName getServiceQName(WebServiceClient webServiceClient) {
        if (webServiceClient.targetNamespace() != null && webServiceClient.name() != null) {
            return new QName(webServiceClient.targetNamespace(), webServiceClient.name());
        } else {
            return null;
        }
    }

    private URI getWSDLLocation(WebServiceClient webServiceClient) throws DeploymentException {
        String wsdlLocation = webServiceClient.wsdlLocation();
        if (wsdlLocation != null && wsdlLocation.trim().length() > 0) {
            try {
                return new URI(wsdlLocation.trim());
            } catch (URISyntaxException e) {
                throw new DeploymentException(
                        "Invalid wsdl location in annotation: " + wsdlLocation, e);
            }
        }

        return null;
    }

    private Map<String, Object> getProperties(GerPortType port) {
        Map<String, Object> props = new HashMap<String, Object>();
        if (port != null && port.getPropertyArray() != null) {
            for (GerPortPropertyType propertyType : port.getPropertyArray()) {
                props.put(propertyType.getName(), propertyType.getStringValue().trim());
            }
        }
        return props;
    }

    private String getCredentialsName(GerPortType port) {
        String credentialsName = port.getCredentialsName();
        return (credentialsName == null) ? null : credentialsName.trim();
    }

    private URL getLocation(GerPortType port) throws DeploymentException {
        String protocol = port.getProtocol().trim();
        String host = port.getHost().trim();
        int portNum = port.getPort();
        String uri = port.getUri().trim();
        String locationURIString = protocol + "://" + host + ":" + portNum + uri;
        URL location = getURL(locationURIString);
        return location;
    }

    private URL getAddressLocation(Port port) throws DeploymentException {
        SOAPAddress soapAddress =
            (SOAPAddress) getExtensibilityElement(SOAPAddress.class, port.getExtensibilityElements());
        URL location = null;
        if (soapAddress != null) {
            String locationURIString = soapAddress.getLocationURI();
            location = getURL(locationURIString);
        }
        return location;
    }

    private URL getURL(String locationURIString) throws DeploymentException {
        try {
            return new URL(locationURIString);
        } catch (MalformedURLException e) {
            throw new DeploymentException(
                    "Could not construct web service location URL from "
                            + locationURIString, e);
        }
    }

    public static ExtensibilityElement getExtensibilityElement(Class clazz,
                                                               List extensibilityElements) {
        for (Iterator iterator = extensibilityElements.iterator(); iterator
                .hasNext();) {
            ExtensibilityElement extensibilityElement = (ExtensibilityElement) iterator
                    .next();
            if (clazz.isAssignableFrom(extensibilityElement.getClass())) {
                return extensibilityElement;
            }
        }
        return null;
    }

    private List<WebServiceFeatureInfo> getWebServiceFeatureInfos(QName portName) {
        PortComponentRef portComponentRef = getPortComponentRef(portName);
        if (portComponentRef == null) {
            return new ArrayList<WebServiceFeatureInfo>(0);
        }
        return buildWebServiceFeatureInfos(portComponentRef);
    }

    private List<WebServiceFeatureInfo> getWebServiceFeatureInfos(String portName) {
        PortComponentRef portComponentRef = getPortComponentRef(portName);
        if (portComponentRef == null) {
            return new ArrayList<WebServiceFeatureInfo>(0);
        }
        return buildWebServiceFeatureInfos(portComponentRef);
    }

    private List<WebServiceFeatureInfo> buildWebServiceFeatureInfos(PortComponentRef portComponentRef) {
        List<WebServiceFeatureInfo> webServiceFeatureInfos = new ArrayList<WebServiceFeatureInfo>(3);
        Addressing addressing = portComponentRef.getAddressing();
        if (addressing != null) {
            webServiceFeatureInfos.add(new AddressingFeatureInfo(addressing.getEnabled() == null ? true : addressing.getEnabled(), addressing.getRequired() == null ? false : addressing.getRequired(),
                    addressing.getResponses() != null ? AddressingFeature.Responses.valueOf(addressing.getResponses().toString()) : AddressingFeature.Responses.ALL));
        }
        if (portComponentRef.getEnableMtom() != null) {
            webServiceFeatureInfos.add(new MTOMFeatureInfo(portComponentRef.isEnableMtom(), portComponentRef.getMtomThreshold() == null ? 0 : portComponentRef.getMtomThreshold()));
        }
        if (portComponentRef.getRespectBinding() != null && portComponentRef.getRespectBinding().getEnabled() != null) {
            webServiceFeatureInfos.add(new RespectBindingFeatureInfo(portComponentRef.getRespectBinding().getEnabled()));
        }
        return webServiceFeatureInfos;
    }

    private void verifyPortComponentList(Definition wsdl) throws DeploymentException {
        if (this.portComponentRefMap == null) {
            return;
        }
        for (Class sei : this.portComponentRefMap.keySet()) {
            QName portType = JAXWSUtils.getPortType(sei);
            if (portType == null) {
                continue;
            }
            if (wsdl.getPortType(portType) == null) {
                throw new DeploymentException("No portType found in WSDL for SEI: " + sei.getName());
            }
        }
    }

    private PortComponentRef getPortComponentRef(QName portType) {
        if (this.portComponentRefMap == null) {
            return null;
        }
        for (Class sei : this.portComponentRefMap.keySet()) {
            QName seiPortType = JAXWSUtils.getPortType(sei);
            if (seiPortType == null) {
                continue;
            }
            if (portType.equals(seiPortType)) {
                return this.portComponentRefMap.get(sei);
            }
        }
        return null;
    }

    private PortComponentRef getPortComponentRef(String portName) {
        if (this.portComponentRefMap == null) {
            return null;
        }
        for (Class sei : this.portComponentRefMap.keySet()) {
            QName seiPortType = JAXWSUtils.getPortType(sei);
            if (seiPortType == null) {
                continue;
            }
            if (portName.equals(seiPortType.getLocalPart())) {
                return this.portComponentRefMap.get(sei);
            }
        }
        return null;
    }

    private boolean isURL(String name) {
        try {
            new URL(name);
            return true;
        } catch (MalformedURLException e1) {
            return false;
        }
    }

    private Catalog loadCatalog() {
        URL catalogURI = null;
        try {
            catalogURI = getCatalog(JAXWSUtils.DEFAULT_CATALOG_WEB);
            if (catalogURI == null) {
                catalogURI = getCatalog(JAXWSUtils.DEFAULT_CATALOG_EJB);
            }
        } catch (IOException e) {
            LOG.warn("Failed to open OASIS catalog", e);
        }

        CatalogManager catalogManager = new CatalogManager();
        catalogManager.setUseStaticCatalog(false);
        catalogManager.setIgnoreMissingProperties(true);
        CatalogResolver catalogResolver = new CatalogResolver(catalogManager);
        Catalog catalog = catalogResolver.getCatalog();

        if (catalogURI != null) {
            LOG.debug("Found OASIS catalog {} ", catalogURI);
            try {
                catalog.parseCatalog(catalogURI);
            } catch (Exception e) {
                LOG.warn("Failed to read OASIS catalog", e);
            }
        }

        return catalog;
    }

    private URL getCatalog(String name) throws IOException {
        URL catalogURL = this.bundle.getResource(name);
        if (catalogURL == null) {
            File f = this.module.getEarContext().getTargetFile(URI.create(name));
            if (f.exists()) {
                catalogURL = f.toURI().toURL();
            }
        }
        return catalogURL;
    }

}
