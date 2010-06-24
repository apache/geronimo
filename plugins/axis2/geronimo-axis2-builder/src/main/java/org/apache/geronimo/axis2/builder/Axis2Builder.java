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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;
import org.apache.geronimo.axis2.pojo.POJOWebServiceContainerFactoryGBean;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.Deployable;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.annotation.AnnotationGBeanInfoBuilder;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.jaxws.JAXWSUtils;
import org.apache.geronimo.jaxws.PortInfo;
import org.apache.geronimo.jaxws.builder.JAXWSServiceBuilder;
import org.apache.geronimo.jaxws.builder.WARWebServiceFinder;
import org.apache.geronimo.jaxws.builder.wsdl.WsdlGenerator;
import org.apache.geronimo.jaxws.builder.wsdl.WsdlGeneratorOptions;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.openejb.jee.HandlerChains;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.PortComponent;
import org.apache.openejb.jee.ServiceImplBean;
import org.apache.openejb.jee.WebserviceDescription;
import org.apache.openejb.jee.Webservices;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */

@GBean(j2eeType = NameFactory.MODULE_BUILDER)
public class Axis2Builder extends JAXWSServiceBuilder {
    private static final Logger log = LoggerFactory.getLogger(Axis2Builder.class);

    protected Collection<WsdlGenerator> wsdlGenerators;

    private GBeanInfo defaultContainerFactoryGBeanInfo;

    public Axis2Builder(@ParamAttribute(name = "defaultEnvironment")Environment defaultEnviroment,
                        @ParamReference(name="WsdlGenerator", namingType = GBeanInfoBuilder.DEFAULT_J2EE_TYPE)Collection<WsdlGenerator> wsdlGenerators) {
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

        try {
            //the checking is needed as we also send JAX-RPC based webservices.xml here
//            if ("http://java.sun.com/xml/ns/javaee".equals(cursor.getName().getNamespaceURI())) {
                Webservices wst = (Webservices) JaxbJavaee.unmarshal(Webservices.class, in);

                for (WebserviceDescription desc : wst.getWebserviceDescription()) {
                    String wsdlFile = null;
                    if (desc.getWsdlFile() != null) {
                        wsdlFile = getString(desc.getWsdlFile());
                    }

                    String serviceName = desc.getWebserviceDescriptionName();

                    for (PortComponent port : desc.getPortComponent()) {

                        PortInfo portInfo = new PortInfo();
                        String serviceLink = null;
                        ServiceImplBean beanType = port.getServiceImplBean();
                        if (beanType.getEjbLink() != null) {
                            serviceLink = beanType.getEjbLink();
                        } else if (beanType.getServletLink() != null) {
                            serviceLink = beanType.getServletLink();
                        }
                        portInfo.setServiceLink(serviceLink);

                        if (port.getServiceEndpointInterface() != null) {
                            String sei = port.getServiceEndpointInterface();
                            portInfo.setServiceEndpointInterfaceName(sei);
                        }

                        String portName = port.getPortComponentName();
                        portInfo.setPortName(portName);

                        portInfo.setProtocolBinding(port.getProtocolBinding());
                        portInfo.setServiceName(serviceName);
                        portInfo.setWsdlFile(wsdlFile);

                        portInfo.setEnableMTOM(port.isEnableMtom());

                        if (port.getHandlerChains() != null) {
                            String handlerChains = JaxbJavaee.marshal(HandlerChains.class, port.getHandlerChains());
                            portInfo.setHandlersAsXML(handlerChains);
                        }

                        if (port.getWsdlPort() != null) {
                            portInfo.setWsdlPort(port.getWsdlPort());
                        }

                        if (port.getWsdlService() != null) {
                            portInfo.setWsdlService(port.getWsdlService());
                        }

                        String location = (String) correctedPortLocations.get(serviceLink);
                        portInfo.setLocation(location);

                        if (map == null) {
                            map = new HashMap<String, PortInfo>();
                        }

                        map.put(serviceLink, portInfo);
                    }
                }
//            } else {
//                log.debug("Descriptor ignored (not a Java EE 5 descriptor)");
//            }

            return map;
            
        } catch (JAXBException e) {
            //we hope it's jax-rpc
            log.debug("Descriptor ignored (not a Java EE 5 descriptor)");
            return Collections.emptyMap();
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

}
