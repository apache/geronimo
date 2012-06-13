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

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.AddressingFeature;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.Deployable;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.annotation.Holder;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.deployment.WebModule;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.jaxws.JAXWSEJBApplicationContext;
import org.apache.geronimo.jaxws.JAXWSUtils;
import org.apache.geronimo.jaxws.JAXWSWebApplicationContext;
import org.apache.geronimo.jaxws.PortInfo;
import org.apache.geronimo.jaxws.annotations.AnnotationHolder;
import org.apache.geronimo.jaxws.feature.AddressingFeatureInfo;
import org.apache.geronimo.jaxws.feature.MTOMFeatureInfo;
import org.apache.geronimo.jaxws.feature.RespectBindingFeatureInfo;
import org.apache.geronimo.jaxws.handler.HandlerChainsInfoBuilder;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.util.IOUtils;
import org.apache.openejb.jee.Addressing;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.PortComponent;
import org.apache.openejb.jee.ServiceImplBean;
import org.apache.openejb.jee.WebserviceDescription;
import org.apache.openejb.jee.Webservices;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JAXWSServiceBuilder implements WebServiceBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(JAXWSServiceBuilder.class);

    private HandlerChainsInfoBuilder handlerChainsInfoBuilder = new HandlerChainsInfoBuilder();

    protected final Environment defaultEnvironment;
    protected WebServiceFinder webServiceFinder;

    public JAXWSServiceBuilder(Environment defaultEnvironment) {
        this.defaultEnvironment = defaultEnvironment;
    }

    protected void setWebServiceFinder(WebServiceFinder finder) {
        this.webServiceFinder = finder;
    }

    protected String getKey() {
        return getClass().getName();
    }

    @Override
    public void findWebServices(Module module, boolean isEJB, Map<String, String> servletLocations, Environment environment, Map sharedContext) throws DeploymentException {
        Map<String, PortInfo> serviceLinkPortInfoMap = discoverWebServices(module, isEJB, servletLocations);
        String path = isEJB ? "META-INF/webservices.xml" : "WEB-INF/webservices.xml";
        Deployable deployable = module.getDeployable();
        URL wsDDUrl = deployable.getResource(path);
        if (wsDDUrl != null) {
            InputStream in = null;
            try {
                in = new BufferedInputStream(wsDDUrl.openStream());

                Webservices wst = (Webservices) JaxbJavaee.unmarshalJavaee(Webservices.class, in);
                for (WebserviceDescription desc : wst.getWebserviceDescription()) {
                    String wsdlFile = null;
                    if (desc.getWsdlFile() != null) {
                        wsdlFile = getString(desc.getWsdlFile());
                    }
                    String serviceName = desc.getWebserviceDescriptionName();
                    for (PortComponent port : desc.getPortComponent()) {

                        String serviceLink = null;
                        ServiceImplBean beanType = port.getServiceImplBean();
                        if (beanType.getEjbLink() != null) {
                            serviceLink = beanType.getEjbLink();
                        } else if (beanType.getServletLink() != null) {
                            serviceLink = beanType.getServletLink();
                        }

                        PortInfo portInfo = serviceLinkPortInfoMap.get(serviceLink);
                        if (portInfo == null) {
                            portInfo = new PortInfo();
                            portInfo.setServiceLink(serviceLink);
                            serviceLinkPortInfoMap.put(serviceLink, portInfo);
                        }

                        if (port.getServiceEndpointInterface() != null) {
                            String sei = port.getServiceEndpointInterface();
                            portInfo.setServiceEndpointInterfaceName(sei);
                        }

                        if (port.getPortComponentName() != null) {
                            portInfo.setPortName(port.getPortComponentName());
                        }

                        if (port.getProtocolBinding() != null) {
                            portInfo.setProtocolBinding(port.getProtocolBinding());
                        }

                        portInfo.setServiceName(serviceName);

                        if (wsdlFile != null) {
                            portInfo.setWsdlFile(wsdlFile);
                        }

                        if (port.getHandlerChains() != null) {
                            portInfo.setHandlerChainsInfo(handlerChainsInfoBuilder.build(port.getHandlerChains()));
                        }

                        if (port.getWsdlPort() != null) {
                            portInfo.setWsdlPort(port.getWsdlPort());
                        }

                        if (port.getWsdlService() != null) {
                            portInfo.setWsdlService(port.getWsdlService());
                        }

                        String location = servletLocations.get(serviceLink);
                        portInfo.setLocation(location);

                        Addressing addressing = port.getAddressing();
                        if (addressing != null) {
                            AddressingFeatureInfo addressingFeatureInfo = portInfo.getAddressingFeatureInfo();
                            if (addressingFeatureInfo == null) {
                                addressingFeatureInfo = new AddressingFeatureInfo();
                                portInfo.setAddressingFeatureInfo(addressingFeatureInfo);
                            }
                            if (addressing.getEnabled() != null) {
                                addressingFeatureInfo.setEnabled(addressing.getEnabled());
                            }
                            if (addressing.getRequired() != null) {
                                addressingFeatureInfo.setRequired(addressing.getRequired());
                            }
                            if (addressing.getResponses() != null) {
                                addressingFeatureInfo.setResponses(AddressingFeature.Responses.valueOf(addressing.getResponses().name()));
                            }
                        }

                        if (port.getEnableMtom() != null || port.getMtomThreshold() != null) {
                            MTOMFeatureInfo mtomFeatureInfo = portInfo.getMtomFeatureInfo();
                            if (mtomFeatureInfo == null) {
                                mtomFeatureInfo = new MTOMFeatureInfo();
                                portInfo.setMtomFeatureInfo(mtomFeatureInfo);
                            }
                            if (port.getEnableMtom() != null) {
                                mtomFeatureInfo.setEnabled(port.getEnableMtom());
                            }
                            if (port.getMtomThreshold() != null) {
                                mtomFeatureInfo.setThreshold(port.getMtomThreshold());
                            }
                        }

                        if (port.getRespectBinding() != null && port.getRespectBinding().getEnabled() != null) {
                            RespectBindingFeatureInfo respectBindingFeatureInfo = portInfo.getRespectBindingFeatureInfo();
                            if (respectBindingFeatureInfo == null) {
                                respectBindingFeatureInfo = new RespectBindingFeatureInfo();
                                portInfo.setRespectBindingFeatureInfo(respectBindingFeatureInfo);
                            }
                            respectBindingFeatureInfo.setEnabled(port.getRespectBinding().getEnabled());
                        }
                    }
                }
            } catch (JAXBException e) {
                //we hope it's jax-rpc
                LOG.debug("Descriptor ignored (not a Java EE 5 descriptor)");
            } catch (Exception e) {
                throw new DeploymentException("Failed to parse " + path, e);
            } finally {
                IOUtils.close(in);
            }
        }

        if (serviceLinkPortInfoMap != null && !serviceLinkPortInfoMap.isEmpty()) {
            EnvironmentBuilder.mergeEnvironments(environment, defaultEnvironment);
            sharedContext.put(getKey(), serviceLinkPortInfoMap);
        }
    }

    private String getString(String in) {
        if (in != null) {
            in = in.trim();
            if (in.length() == 0) {
                return null;
            }
        }
        return in;
    }

    private Map<String, PortInfo> discoverWebServices(Module module,
                                                      boolean isEJB,
                                                      Map<String, String> correctedPortLocations)
            throws DeploymentException {
        if (webServiceFinder == null) {
            throw new DeploymentException("WebServiceFinder not configured");
        }
        return webServiceFinder.discoverWebServices(module, correctedPortLocations);
    }

    @Override
    public boolean configurePOJO(GBeanData targetGBean,
                                 String servletName,
                                 Module module,
                                 String servletClassName,
                                 DeploymentContext context)
            throws DeploymentException {
        Map sharedContext = ((WebModule) module).getSharedContext();
        Map<String, PortInfo> portInfoMap = (Map<String, PortInfo>) sharedContext.get(getKey());
        if (portInfoMap == null) {
            // not ours
            return false;
        }
        PortInfo portInfo = portInfoMap.get(servletName);
        if (portInfo == null) {
            // not ours
            return false;
        }

        // verify that the class is loadable and is a JAX-WS web service
        Bundle bundle = context.getDeploymentBundle();
        Class<?> servletClass = loadClass(servletClassName, bundle);
        if (!JAXWSUtils.isWebService(servletClass)) {
            return false;
        }

        Map<String, PortInfo> servletNamePortInfoMap = null;
        AbstractName jaxwsWebApplicationContextName = context.getNaming().createChildName(module.getModuleName(), "JAXWSWebApplicationContext", "JAXWSWebApplicationContext");
        try {
            servletNamePortInfoMap = (Map<String, PortInfo>)(context.getGBeanInstance(jaxwsWebApplicationContextName).getAttribute("servletNamePortInfoMap"));
        } catch (GBeanNotFoundException e) {
            GBeanData jaxwsWebApplicationContextGBeanData = new GBeanData(jaxwsWebApplicationContextName, JAXWSWebApplicationContext.class);
            try {
                context.addGBean(jaxwsWebApplicationContextGBeanData);
            } catch (GBeanAlreadyExistsException e1) {
            }
            servletNamePortInfoMap = new HashMap<String, PortInfo>();
            jaxwsWebApplicationContextGBeanData.setAttribute("servletNamePortInfoMap", servletNamePortInfoMap);
        }
        targetGBean.addDependency(jaxwsWebApplicationContextName);
        servletNamePortInfoMap.put(servletName, portInfo);

        Map componentContext = null;
        Holder moduleHolder = null;
        try {
            //TODO Now we share the same DeploymentContext in the ear package, which means all the gbeans are saved in the one EARContext
            //Might need to update while we have real EAR support
            moduleHolder = (Holder) module.getSharedContext().get(NamingBuilder.INJECTION_KEY);
            GBeanData contextSourceGBean = context.getGBeanInstance(context.getNaming().createChildName(module.getModuleName(), "ContextSource", "ContextSource"));
            componentContext = (Map) contextSourceGBean.getAttribute("componentContext");
        } catch (GBeanNotFoundException e) {
            LOG.warn("ModuleGBean not found. JNDI resource injection will not work.");
        }

        AnnotationHolder serviceHolder =
            (AnnotationHolder)sharedContext.get(WebServiceContextAnnotationHelper.class.getName());
        if (serviceHolder == null) {
            serviceHolder = new AnnotationHolder(moduleHolder);
            sharedContext.put(WebServiceContextAnnotationHelper.class.getName(), serviceHolder);
        }
        WebServiceContextAnnotationHelper.addWebServiceContextInjections(serviceHolder, servletClass);

        String location = portInfo.getLocation();
        LOG.info("Configuring JAX-WS Web Service: " + servletName + " at " + location);

        AbstractName containerFactoryName = context.getNaming().createChildName(targetGBean.getAbstractName(), getContainerFactoryGBeanInfo().getName(), GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
        GBeanData containerFactoryData = new GBeanData(containerFactoryName, getContainerFactoryGBeanInfo());
        containerFactoryData.setAttribute("portInfo", portInfo);
        containerFactoryData.setAttribute("endpointClassName", servletClassName);
        containerFactoryData.setAttribute("componentContext", componentContext);
        containerFactoryData.setAttribute("holder", serviceHolder);
        containerFactoryData.setAttribute("contextRoot", ((WebModule) module).getContextRoot());
        containerFactoryData.setAttribute("catalogName", JAXWSBuilderUtils.normalizeCatalogPath(module, JAXWSUtils.DEFAULT_CATALOG_WEB));
        try {
            context.addGBean(containerFactoryData);
        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException("Could not add web service container factory gbean", e);
        }

        targetGBean.setReferencePattern("WebServiceContainerFactory", containerFactoryName);
        // our web container does not use that property
        targetGBean.setAttribute("pojoClassName", "java.lang.Object");

        if (context instanceof EARContext) {
            containerFactoryData.setReferencePattern("TransactionManager",
                                                     ((EARContext)context).getTransactionManagerName());
        }

        initialize(containerFactoryData, servletClass, portInfo, module, bundle);
        return true;
    }

    protected abstract GBeanInfo getContainerFactoryGBeanInfo();

    @Override
    public boolean configureEJB(GBeanData targetGBean,
                                String ejbName,
                                Module module,
                                Map sharedContext,
                                Bundle bundle)
            throws DeploymentException {
        Map<String, PortInfo> portInfoMap = (Map<String, PortInfo>) sharedContext.get(getKey());
        if (portInfoMap == null) {
            // not ours
            return false;
        }
        PortInfo portInfo = portInfoMap.get(ejbName);
        if (portInfo == null) {
            // not ours
            return false;
        }

        String beanClassName = (String)targetGBean.getAttribute("ejbClass");
        // verify that the class is loadable and is a JAX-WS web service
        Class<?> beanClass = loadClass(beanClassName, bundle);
        if (!JAXWSUtils.isWebService(beanClass)) {
            return false;
        }

        String location = portInfo.getLocation();
        if (location == null) {
            throw new DeploymentException("Endpoint URI for EJB WebService is missing");
        }

        Map<String, PortInfo> ejbNamePortInfoMap = null;
        DeploymentContext context = module.getEarContext();
        AbstractName jaxwsEJBApplicationContextName = context.getNaming().createChildName(module.getModuleName(), "JAXWSEJBApplicationContext", "JAXWSEJBApplicationContext");
        try {
            ejbNamePortInfoMap = (Map<String, PortInfo>)(context.getGBeanInstance(jaxwsEJBApplicationContextName).getAttribute("ejbNamePortInfoMap"));
        } catch (GBeanNotFoundException e) {
            GBeanData jaxwsEJBApplicationContextGBeanData = new GBeanData(jaxwsEJBApplicationContextName, JAXWSEJBApplicationContext.class);
            try {
                context.addGBean(jaxwsEJBApplicationContextGBeanData);
            } catch (GBeanAlreadyExistsException e1) {
            }
            ejbNamePortInfoMap = new HashMap<String, PortInfo>();
            jaxwsEJBApplicationContextGBeanData.setAttribute("ejbNamePortInfoMap", ejbNamePortInfoMap);
        }
        targetGBean.addDependency(jaxwsEJBApplicationContextName);
        ejbNamePortInfoMap.put(ejbName, portInfo);

        LOG.info("Configuring EJB JAX-WS Web Service: " + ejbName + " at " + location);

        targetGBean.setAttribute("portInfo", portInfo);

        targetGBean.setAttribute("catalogName", JAXWSBuilderUtils.normalizeCatalogPath(module, JAXWSUtils.DEFAULT_CATALOG_EJB));

        initialize(targetGBean, beanClass, portInfo, module, bundle);

        return true;
    }

    protected void initialize(GBeanData targetGBean, Class wsClass, PortInfo info, Module module, Bundle bundle) throws DeploymentException {
    }

    Class<?> loadClass(String className, Bundle bundle) throws DeploymentException {
        try {
            return bundle.loadClass(className);
        } catch (ClassNotFoundException ex) {
            throw new DeploymentException("Unable to load Web Service class: " + className, ex);
        }
    }

    protected boolean isWsdlSet(PortInfo portInfo, Class serviceClass, Bundle bundle) {
        return (portInfo.getWsdlFile() != null && !portInfo.getWsdlFile().trim().equals("")) || JAXWSUtils.containsWsdlLocation(serviceClass, bundle);
    }

    protected boolean isHTTPBinding(PortInfo portInfo, Class serviceClass) {
        String bindingURI = "";
        String bindingURIFromAnnot;

        if (portInfo.getProtocolBinding() != null) {
            bindingURI = JAXWSUtils.getBindingURI(portInfo.getProtocolBinding());
        }
        bindingURIFromAnnot = JAXWSUtils.getBindingURIFromAnnot(serviceClass);

        if (bindingURI != null && !bindingURI.trim().equals("")) {
            return bindingURI.equals(HTTPBinding.HTTP_BINDING);
        } else if (bindingURIFromAnnot != null && !bindingURIFromAnnot.trim().equals("")) {
            return bindingURIFromAnnot.equals(HTTPBinding.HTTP_BINDING);
        }

        return false;
    }
}
