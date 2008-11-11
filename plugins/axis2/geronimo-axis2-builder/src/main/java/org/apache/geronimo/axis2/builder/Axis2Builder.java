/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.axis2.builder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

import javax.xml.ws.http.HTTPBinding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.axis2.pojo.POJOWebServiceContainerFactoryGBean;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.jaxws.JAXWSUtils;
import org.apache.geronimo.jaxws.PortInfo;
import org.apache.geronimo.jaxws.builder.JAXWSServiceBuilder;
import org.apache.geronimo.jaxws.builder.WARWebServiceFinder;
import org.apache.geronimo.jaxws.wsdl.WsdlGenerator;
import org.apache.geronimo.jaxws.wsdl.WsdlGeneratorOptions;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.xbeans.javaee.PortComponentType;
import org.apache.geronimo.xbeans.javaee.ServiceImplBeanType;
import org.apache.geronimo.xbeans.javaee.WebserviceDescriptionType;
import org.apache.geronimo.xbeans.javaee.WebservicesDocument;
import org.apache.geronimo.xbeans.javaee.WebservicesType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class Axis2Builder extends JAXWSServiceBuilder {

    private static final Logger log = LoggerFactory.getLogger(Axis2Builder.class);
        
    protected Collection<WsdlGenerator> wsdlGenerators;
    
    public Axis2Builder(Environment defaultEnviroment,
                        Collection<WsdlGenerator> wsdlGenerators) {
        super(defaultEnviroment);
        this.wsdlGenerators = wsdlGenerators;
        this.webServiceFinder = new WARWebServiceFinder();
    }
    
    public Axis2Builder(){
        super(null);
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

        log.debug("Parsing descriptor " + wsDDUrl);

        Map<String, PortInfo> map = null;
        XmlCursor cursor = null;

        try {
            XmlObject xobj = XmlObject.Factory.parse(in);
           
            cursor = xobj.newCursor();
            cursor.toStartDoc();
            cursor.toFirstChild();
            //the checking is needed as we also send JAX-RPC based webservices.xml here
            if ("http://java.sun.com/xml/ns/javaee".equals(cursor.getName().getNamespaceURI())) {
                WebservicesDocument wd = (WebservicesDocument)xobj.changeType(WebservicesDocument.type);
                WebservicesType wst = wd.getWebservices();

                for (WebserviceDescriptionType desc : wst.getWebserviceDescriptionArray()) {
                    String wsdlFile = null;
                    if (desc.getWsdlFile() != null) {
                        wsdlFile = getString(desc.getWsdlFile().getStringValue());
                    }

                    String serviceName = desc.getWebserviceDescriptionName().getStringValue();

                    for (PortComponentType port : desc.getPortComponentArray()) {

                        PortInfo portInfo = new PortInfo();
                        String serviceLink = null;
                        ServiceImplBeanType beanType = port.getServiceImplBean();
                        if (beanType.getEjbLink() != null) {
                            serviceLink = beanType.getEjbLink().getStringValue();
                        } else if (beanType.getServletLink().getStringValue() != null) {
                            serviceLink = beanType.getServletLink().getStringValue();
                        }
                        portInfo.setServiceLink(serviceLink);

                        if (port.getServiceEndpointInterface() != null) {
                            String sei = port.getServiceEndpointInterface().getStringValue();
                            portInfo.setServiceEndpointInterfaceName(sei);
                        }

                        String portName = port.getPortComponentName().getStringValue();
                        portInfo.setPortName(portName);

                        portInfo.setProtocolBinding(port.getProtocolBinding());
                        portInfo.setServiceName(serviceName);
                        portInfo.setWsdlFile(wsdlFile);

                        if (port.getEnableMtom() != null) {
                            portInfo.setEnableMTOM(port.getEnableMtom().getBooleanValue());
                        }

                        if (port.getHandlerChains() != null) {
                            StringBuffer chains = new StringBuffer("<handler-chains xmlns=\"http://java.sun.com/xml/ns/javaee\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
                            chains.append(port.getHandlerChains().xmlText());
                            chains.append("</handler-chains>");
                            portInfo.setHandlersAsXML(chains.toString());
                        }

                        if (port.getWsdlPort() != null) {
                            portInfo.setWsdlPort(port.getWsdlPort().getQNameValue());
                        }

                        if (port.getWsdlService() != null) {
                            portInfo.setWsdlService(port.getWsdlService().getQNameValue());
                        }

                        String location = (String) correctedPortLocations.get(serviceLink);
                        portInfo.setLocation(location);

                        if (map == null) {
                            map = new HashMap<String, PortInfo>();
                        }

                        map.put(serviceLink, portInfo);
                    }
                }
            } else {
                log.debug("Descriptor ignored (not a Java EE 5 descriptor)");
            }
            
            return map;
        } catch (FileNotFoundException e) {
            return Collections.emptyMap();
        } catch (IOException ex) {
            throw new DeploymentException("Unable to read " + wsDDUrl, ex);
        } catch (Exception ex) {
            throw new DeploymentException("Unknown deployment error", ex);
        } finally {
            if (cursor != null) {
                cursor.dispose();
            }
            try {
                in.close();
            } catch (IOException e) {
                // ignore
            }
        }
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
   
    protected WsdlGenerator getWsdlGenerator() throws DeploymentException {
        if (this.wsdlGenerators == null || this.wsdlGenerators.isEmpty()) {
            throw new DeploymentException("Wsdl generator not found");
        } else {
            return this.wsdlGenerators.iterator().next();
        }
    }
    
    @Override
    protected void initialize(GBeanData targetGBean, Class serviceClass, PortInfo portInfo, Module module) 
        throws DeploymentException {
        if (isWsdlSet(portInfo, serviceClass)) {
            log.debug("Service " + portInfo.getServiceName() + " has WSDL.");
            return;
        }
        
        if (isHTTPBinding(portInfo, serviceClass)) {
            log.debug("Service " + portInfo.getServiceName() + " is HTTPBinding.  Only SOAP 1.1 or 1.2 is supported.");
            return;
        }
        
        log.debug("Service " + portInfo.getServiceName() + " does not have WSDL. Generating WSDL...");

        WsdlGenerator wsdlGenerator = getWsdlGenerator();
        
        WsdlGeneratorOptions options = new WsdlGeneratorOptions();
        options.setSAAJ(WsdlGeneratorOptions.SAAJ.Axis2);
        
        // set wsdl service
        if (portInfo.getWsdlService() == null) {
            options.setWsdlService(JAXWSUtils.getServiceQName(serviceClass));
        } else {
            options.setWsdlService(portInfo.getWsdlService());
        }
        
        // set wsdl port
        if (portInfo.getWsdlPort() != null) {
            options.setWsdlPort(portInfo.getWsdlPort());
        }
                
        String wsdlFile = wsdlGenerator.generateWsdl(module, serviceClass.getName(), module.getEarContext(), options);
        portInfo.setWsdlFile(wsdlFile);
        
        log.debug("Generated " + wsdlFile + " for service " + portInfo.getServiceName());        
    }
    
    private boolean isWsdlSet(PortInfo portInfo, Class serviceClass) {
        return (portInfo.getWsdlFile() != null && !portInfo.getWsdlFile().trim().equals(""))
                || JAXWSUtils.containsWsdlLocation(serviceClass, serviceClass.getClassLoader());
    }
    
    private boolean isHTTPBinding(PortInfo portInfo, Class serviceClass) {
        String bindingURI = "";
        String bindingURIFromAnnot;
        
        if (portInfo.getProtocolBinding() != null) {
            bindingURI = JAXWSUtils.getBindingURI(portInfo.getProtocolBinding());
        }        
        bindingURIFromAnnot = JAXWSUtils.getBindingURIFromAnnot(serviceClass, serviceClass.getClassLoader());
        
        if (bindingURI != null && !bindingURI.trim().equals("")) {
            return bindingURI.equals(HTTPBinding.HTTP_BINDING);
        } else if (bindingURIFromAnnot != null && !bindingURIFromAnnot.trim().equals("")) {
            return bindingURIFromAnnot.equals(HTTPBinding.HTTP_BINDING);
        } 
        
        return false;  
    }
        
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(Axis2Builder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addInterface(WebServiceBuilder.class);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true, true);
        infoBuilder.addReference("WsdlGenerator", WsdlGenerator.class, GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
        infoBuilder.setConstructor(new String[]{"defaultEnvironment", "WsdlGenerator"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
