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

import org.apache.geronimo.axis2.pojo.POJOWebServiceContainerFactoryGBean;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.Deployable;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.annotation.AnnotationGBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.jaxws.JAXWSUtils;
import org.apache.geronimo.jaxws.PortInfo;
import org.apache.geronimo.jaxws.builder.JAXWSServiceBuilder;
import org.apache.geronimo.jaxws.builder.WARWebServiceFinder;
import org.apache.geronimo.jaxws.builder.wsdl.WsdlGenerator;
import org.apache.geronimo.jaxws.builder.wsdl.WsdlGeneratorOptions;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.xbeans.javaee6.PortComponentType;
import org.apache.geronimo.xbeans.javaee6.ServiceImplBeanType;
import org.apache.geronimo.xbeans.javaee6.WebserviceDescriptionType;
import org.apache.geronimo.xbeans.javaee6.WebservicesDocument;
import org.apache.geronimo.xbeans.javaee6.WebservicesType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class Axis2Builder extends JAXWSServiceBuilder {

    private static final Logger log = LoggerFactory.getLogger(Axis2Builder.class);

    protected Collection<WsdlGenerator> wsdlGenerators;

    private GBeanInfo defaultContainerFactoryGBeanInfo;

    public Axis2Builder(Environment defaultEnviroment, Collection<WsdlGenerator> wsdlGenerators) {
        super(defaultEnviroment);
        this.wsdlGenerators = wsdlGenerators;
        this.webServiceFinder = new WARWebServiceFinder();
        AnnotationGBeanInfoBuilder annotationGBeanInfoBuilder = new AnnotationGBeanInfoBuilder(POJOWebServiceContainerFactoryGBean.class);
        defaultContainerFactoryGBeanInfo = annotationGBeanInfoBuilder.buildGBeanInfo();
    }

    public Axis2Builder(){
        super(null);
    }

    protected GBeanInfo getContainerFactoryGBeanInfo() {
        return defaultContainerFactoryGBeanInfo;
    }

    protected Map<String, PortInfo> parseWebServiceDescriptor(InputStream in,
                                                              URL wsDDUrl,
                                                              Deployable deployable,
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
    protected void initialize(GBeanData targetGBean, Class serviceClass, PortInfo portInfo, Module module, Bundle bundle) throws DeploymentException {
        String serviceName = (portInfo.getServiceName() == null ? serviceClass.getName() : portInfo.getServiceName());
        if (isWsdlSet(portInfo, serviceClass, bundle)) {
            log.debug("Service " + serviceName + " has WSDL.");
            return;
        }

        if (isHTTPBinding(portInfo, serviceClass)) {
            log.debug("Service " + serviceName + " has HTTPBinding.");
            return;
        }

        if (JAXWSUtils.isWebServiceProvider(serviceClass)) {
            throw new DeploymentException("WSDL must be specified for @WebServiceProvider service " + serviceName);
        }

        log.debug("Service " + serviceName + " does not have WSDL. Generating WSDL...");

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

        log.debug("Generated " + wsdlFile + " for service " + serviceName);
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
