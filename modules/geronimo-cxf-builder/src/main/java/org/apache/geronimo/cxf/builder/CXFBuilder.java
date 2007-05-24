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
package org.apache.geronimo.cxf.builder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxws.javaee.HandlerChainsType;
import org.apache.cxf.jaxws.javaee.PortComponentType;
import org.apache.cxf.jaxws.javaee.ServiceImplBeanType;
import org.apache.cxf.jaxws.javaee.WebserviceDescriptionType;
import org.apache.cxf.jaxws.javaee.WebservicesType;
import org.apache.cxf.jaxws.support.JaxWsImplementorInfo;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.cxf.client.CXFServiceReference;
import org.apache.geronimo.cxf.pojo.POJOWebServiceContainerFactoryGBean;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.jaxws.JAXWSUtils;
import org.apache.geronimo.jaxws.PortInfo;
import org.apache.geronimo.jaxws.builder.EndpointInfoBuilder;
import org.apache.geronimo.jaxws.builder.JAXWSServiceBuilder;
import org.apache.geronimo.jaxws.builder.WsdlGenerator;
import org.apache.geronimo.jaxws.client.EndpointInfo;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.xbeans.geronimo.naming.GerServiceRefType;
import org.apache.geronimo.xbeans.javaee.PortComponentRefType;
import org.apache.geronimo.xbeans.javaee.ServiceRefHandlerChainsType;
import org.apache.xmlbeans.XmlOptions;

public class CXFBuilder extends JAXWSServiceBuilder {
    private static final Log LOG = LogFactory.getLog(CXFBuilder.class);

    public CXFBuilder() {
        this(null);
    }

    public CXFBuilder(Environment defaultEnvironment) {
        super(defaultEnvironment);
    }

    protected GBeanInfo getContainerFactoryGBeanInfo() {
        return POJOWebServiceContainerFactoryGBean.GBEAN_INFO;
    }

    protected Map<String, PortInfo> parseWebServiceDescriptor(InputStream in,
                                                              URL wsDDUrl,
                                                              JarFile moduleFile,
                                                              boolean isEJB,
                                                              Map correctedPortLocations)
            throws DeploymentException {

        LOG.debug("Parsing descriptor " + wsDDUrl);

        Map<String, PortInfo> map = null;

        try {
            JAXBContext ctx = JAXBContext.newInstance(WebservicesType.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            Object obj = unmarshaller.unmarshal(new StreamSource(in), WebservicesType.class);

            if (obj instanceof JAXBElement) {
                obj = ((JAXBElement) obj).getValue();
            }

            if (!(obj instanceof WebservicesType)) {
                return map;
            }
            WebservicesType wst = (WebservicesType) obj;

            for (WebserviceDescriptionType desc : wst.getWebserviceDescription()) {
                String wsdlFile = null;
                if (desc.getWsdlFile() != null) {
                    wsdlFile = getString(desc.getWsdlFile().getValue());
                }

                String serviceName = desc.getWebserviceDescriptionName().getValue();

                for (PortComponentType port : desc.getPortComponent()) {

                    PortInfo portInfo = new PortInfo();

                    String serviceLink = null;
                    ServiceImplBeanType beanType = port.getServiceImplBean();
                    if (beanType.getEjbLink() != null) {
                        serviceLink = beanType.getEjbLink().getValue();
                    } else if (beanType.getServletLink().getValue() != null) {
                        serviceLink = beanType.getServletLink().getValue();
                    }
                    portInfo.setServiceLink(serviceLink);

                    if (port.getServiceEndpointInterface() != null) {
                        String sei = port.getServiceEndpointInterface().getValue();
                        portInfo.setServiceEndpointInterfaceName(sei);
                    }

                    String portName = port.getPortComponentName().getValue();
                    portInfo.setPortName(portName);

                    portInfo.setProtocolBinding(port.getProtocolBinding());
                    portInfo.setServiceName(serviceName);
                    portInfo.setWsdlFile(wsdlFile);

                    if (port.getEnableMtom() != null) {
                        portInfo.setEnableMTOM(port.getEnableMtom().isValue());
                    }

                    portInfo.setHandlers(HandlerChainsType.class, port.getHandlerChains());

                    if (port.getWsdlPort() != null) {
                        portInfo.setWsdlPort(port.getWsdlPort().getValue());
                    }

                    if (port.getWsdlService() != null) {
                        portInfo.setWsdlService(port.getWsdlService().getValue());
                    }

                    String location = (String) correctedPortLocations.get(serviceLink);
                    portInfo.setLocation(location);

                    if (map == null) {
                        map = new HashMap<String, PortInfo>();
                    }
                    map.put(serviceLink, portInfo);
                }
            }

            return map;
        } catch (FileNotFoundException e) {
            return Collections.emptyMap();
        } catch (IOException ex) {
            throw new DeploymentException("Unable to read " + wsDDUrl, ex);
        } catch (JAXBException ex) {
            throw new DeploymentException("Unable to parse " + wsDDUrl, ex);
        } catch (Exception ex) {
            throw new DeploymentException("Unknown deployment error", ex);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public Object createService(Class serviceInterface,
                                Class serviceReference,
                                URI wsdlURI,
                                QName serviceQName,
                                Map<Class, PortComponentRefType> portComponentRefMap,
                                ServiceRefHandlerChainsType handlerChains,
                                GerServiceRefType serviceRefType,
                                Module module,
                                ClassLoader cl) throws DeploymentException {     
        EndpointInfoBuilder builder = new EndpointInfoBuilder(serviceInterface,
                serviceRefType, portComponentRefMap, module.getModuleFile(),
                wsdlURI, serviceQName);
        builder.build();

        wsdlURI = builder.getWsdlURI();
        serviceQName = builder.getServiceQName();
        Map<Object, EndpointInfo> seiInfoMap = builder.getEndpointInfo();

        String handlerChainsXML = null;
        try {
            handlerChainsXML = getHandlerChainAsString(handlerChains);
        } catch (IOException e) {
            // this should not happen
            LOG.warn("Failed to serialize handler chains", e);
        }

        String serviceReferenceName = (serviceReference == null) ? null : serviceReference.getName();
        
        return new CXFServiceReference(serviceInterface.getName(), serviceReferenceName,  wsdlURI,
                serviceQName, module.getModuleName(), handlerChainsXML, seiInfoMap);
    }
    
    private static String getHandlerChainAsString(ServiceRefHandlerChainsType handlerChains)
            throws IOException {
        String xml = null;
        if (handlerChains != null) {
            StringWriter w = new StringWriter();
            XmlOptions options = new XmlOptions();
            options.setSaveSyntheticDocumentElement(new QName("http://java.sun.com/xml/ns/javaee", "handler-chains")); 
            handlerChains.save(w, options);
            xml = w.toString();
        }
        return xml;
    }
    
    private static String getString(String in) {
        if (in != null) {
            in = in.trim();
            if (in.length() == 0) {
                return null;
            }
        }
        return in;
    }
    
    /*
    @Override
    protected void initialize(GBeanData targetGBean, Class serviceClass, PortInfo portInfo, Module module) 
        throws DeploymentException {
        if (isWsdlSet(portInfo, serviceClass)) {
            LOG.debug("Service " + portInfo.getServiceName() + " has WSDL.");
            return;
        }        
        LOG.debug("Service " + portInfo.getServiceName() + " does not have WSDL. Generating WSDL...");

        WsdlGenerator generator = new WsdlGenerator();
        generator.setSunSAAJ();
        
        JaxWsImplementorInfo serviceInfo = new JaxWsImplementorInfo(serviceClass);
        
        // set wsdl service
        if (portInfo.getWsdlService() == null) {
            generator.setWsdlService(serviceInfo.getServiceName());
        } else {
            generator.setWsdlService(portInfo.getWsdlService());
        }
        
        // set wsdl port
        if (portInfo.getWsdlPort() == null) {
            generator.setWsdlPort(serviceInfo.getEndpointName());
        } else {
            generator.setWsdlPort(portInfo.getWsdlPort());
        }
                        
        String wsdlFile = generator.generateWsdl(module, serviceClass.getName(), module.getEarContext(), portInfo);
        portInfo.setWsdlFile(wsdlFile);
        
        LOG.debug("Generated " + wsdlFile + " for service " + portInfo.getServiceName()); 
    }   
    
    private boolean isWsdlSet(PortInfo portInfo, Class serviceClass) {
        return (portInfo.getWsdlFile() != null && !portInfo.getWsdlFile().trim().equals(""))
                || JAXWSUtils.containsWsdlLocation(serviceClass, serviceClass.getClassLoader());
    }
    */
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(CXFBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addInterface(WebServiceBuilder.class);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true, true);

        infoBuilder.setConstructor(new String[]{"defaultEnvironment"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
