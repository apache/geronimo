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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.j2ee.annotation.Holder;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.WebModule;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.jaxws.JAXWSUtils;
import org.apache.geronimo.jaxws.PortInfo;
import org.apache.geronimo.jaxws.annotations.AnnotationHolder;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.repository.Environment;

public abstract class JAXWSServiceBuilder implements WebServiceBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(JAXWSServiceBuilder.class);

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
    
    public void findWebServices(Module module,
                                boolean isEJB,
                                Map servletLocations,
                                Environment environment,
                                Map sharedContext) throws DeploymentException {
        Map portMap = null;
        String path = isEJB ? "META-INF/webservices.xml" : "WEB-INF/webservices.xml";
        JarFile moduleFile = module.getModuleFile();
        try {
            URL wsDDUrl = DeploymentUtil.createJarURL(moduleFile, path);
            InputStream in = wsDDUrl.openStream();
            portMap = parseWebServiceDescriptor(in, wsDDUrl, moduleFile, isEJB, servletLocations);
        } catch (IOException e) {
            // webservices.xml does not exist
            portMap = discoverWebServices(module, isEJB, servletLocations);
        }

        if (portMap != null && !portMap.isEmpty()) {
            EnvironmentBuilder.mergeEnvironments(environment, defaultEnvironment);
            sharedContext.put(getKey(), portMap);
        }

    }

    private Map<String, PortInfo> discoverWebServices(Module module,
                                                      boolean isEJB,
                                                      Map correctedPortLocations)
            throws DeploymentException {
        if (this.webServiceFinder == null) {
            throw new DeploymentException("WebServiceFinder not configured");
        }
        return this.webServiceFinder.discoverWebServices(module, isEJB, correctedPortLocations);
    }
                               
    protected abstract Map<String, PortInfo> parseWebServiceDescriptor(InputStream in,
                                                                       URL wsDDUrl,
                                                                       JarFile moduleFile,
                                                                       boolean isEJB,
                                                                       Map correctedPortLocations)
            throws DeploymentException;

    public boolean configurePOJO(GBeanData targetGBean,
                                 String servletName,
                                 Module module,
                                 String servletClassName,
                                 DeploymentContext context)
            throws DeploymentException {
        Map sharedContext = ((WebModule) module).getSharedContext();
        Map portInfoMap = (Map) sharedContext.get(getKey());
        if (portInfoMap == null) {
            // not ours
            return false;
        }
        PortInfo portInfo = (PortInfo) portInfoMap.get(servletName);
        if (portInfo == null) {
            // not ours
            return false;
        }

        // verify that the class is loadable and is a JAX-WS web service
        ClassLoader classLoader = context.getClassLoader();
        Class servletClass = loadClass(servletClassName, classLoader);
        if (!JAXWSUtils.isWebService(servletClass)) {
            return false;
        }
        
        Map componentContext = null;
        Holder moduleHolder = null;
        try {
            GBeanData moduleGBean = context.getGBeanInstance(context.getModuleName());
            componentContext = (Map)moduleGBean.getAttribute("componentContext");
            moduleHolder = (Holder)moduleGBean.getAttribute("holder");
        } catch (GBeanNotFoundException e) {
            LOG.warn("ModuleGBean not found. JNDI resource injection will not work.");
        }

        AnnotationHolder serviceHolder = 
            (AnnotationHolder)sharedContext.get(WebServiceContextAnnotationHelper.class.getName());
        if (serviceHolder == null) {
            serviceHolder = new AnnotationHolder(moduleHolder);
            WebServiceContextAnnotationHelper.addWebServiceContextInjections(serviceHolder, module.getClassFinder());
            sharedContext.put(WebServiceContextAnnotationHelper.class.getName(), serviceHolder);
        }
        
        String location = portInfo.getLocation();
        LOG.info("Configuring JAX-WS Web Service: " + servletName + " at " + location);

        AbstractName containerFactoryName = context.getNaming().createChildName(targetGBean.getAbstractName(), getContainerFactoryGBeanInfo().getName(), NameFactory.GERONIMO_SERVICE);
        GBeanData containerFactoryData = new GBeanData(containerFactoryName, getContainerFactoryGBeanInfo());
        containerFactoryData.setAttribute("portInfo", portInfo);
        containerFactoryData.setAttribute("endpointClassName", servletClassName);
        containerFactoryData.setAttribute("componentContext", componentContext);
        containerFactoryData.setAttribute("holder", serviceHolder);
        containerFactoryData.setAttribute("contextRoot", ((WebModule) module).getContextRoot());
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
        
        initialize(containerFactoryData, servletClass, portInfo, module);
        
        return true;
    }
        
    protected abstract GBeanInfo getContainerFactoryGBeanInfo();

    public boolean configureEJB(GBeanData targetGBean,
                                String ejbName,
                                Module module,
                                Map sharedContext,
                                ClassLoader classLoader)
            throws DeploymentException {        
        Map portInfoMap = (Map) sharedContext.get(getKey());
        if (portInfoMap == null) {
            // not ours
            return false;
        }
        PortInfo portInfo = (PortInfo) portInfoMap.get(ejbName);
        if (portInfo == null) {
            // not ours
            return false;
        }
       
        String beanClassName = (String)targetGBean.getAttribute("ejbClass");
        // verify that the class is loadable and is a JAX-WS web service
        Class beanClass = loadClass(beanClassName, classLoader);
        if (!JAXWSUtils.isWebService(beanClass)) {
            return false;
        }
        
        String location = portInfo.getLocation();
        if (location == null) {                   
            throw new DeploymentException("Endpoint URI for EJB WebService is missing");
        }

        LOG.info("Configuring EJB JAX-WS Web Service: " + ejbName + " at " + location);
        
        targetGBean.setAttribute("portInfo", portInfo);
        
        initialize(targetGBean, beanClass, portInfo, module);
        
        return true;
    }
    
    protected void initialize(GBeanData targetGBean, Class wsClass, PortInfo info, Module module) 
        throws DeploymentException {
    }
    
    Class<?> loadClass(String className, ClassLoader loader) throws DeploymentException {
        try {
            return loader.loadClass(className);
        } catch (ClassNotFoundException ex) {
            throw new DeploymentException("Unable to load Web Service class: " + className, ex);
        }
    }
}
