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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.jws.WebService;
import javax.xml.ws.WebServiceProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.apache.geronimo.kernel.classloader.JarFileClassLoader;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.openejb.deployment.EjbModule;
import org.apache.geronimo.xbeans.javaee.ServletMappingType;
import org.apache.geronimo.xbeans.javaee.ServletType;
import org.apache.geronimo.xbeans.javaee.WebAppType;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.xbean.finder.ClassFinder;

public abstract class JAXWSServiceBuilder implements WebServiceBuilder {
    private static final Log LOG = LogFactory.getLog(JAXWSServiceBuilder.class);

    protected final Environment defaultEnvironment;

    public JAXWSServiceBuilder(Environment defaultEnvironment) {
        this.defaultEnvironment = defaultEnvironment;
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
        Map<String, PortInfo> map = new HashMap<String, PortInfo>();        
        if (isEJB) {            
            discoverEJBWebServices(module, correctedPortLocations, map);
        } else {          
            discoverPOJOWebServices(module, correctedPortLocations, map);
        }        
        return map;
    }
    
    private void discoverPOJOWebServices(Module module,
                                         Map correctedPortLocations,
                                         Map<String, PortInfo> map) 
        throws DeploymentException {
        ClassLoader classLoader = module.getEarContext().getClassLoader();
        WebAppType webApp = (WebAppType) module.getSpecDD();

        // find web services
        ServletType[] servletTypes = webApp.getServletArray();

        if (webApp.getDomNode().getChildNodes().getLength() == 0) {
            // web.xml not present (empty really), discover annotated
            // classes and update DD
            List<Class> services = discoverWebServices(module.getModuleFile(), false);
            String contextRoot = ((WebModule) module).getContextRoot();
            for (Class service : services) {
                // skip interfaces and such
                if (!JAXWSUtils.isWebService(service)) {
                    continue;
                }

                LOG.debug("Discovered POJO Web Service: " + service.getName());
                
                // add new <servlet/> element
                ServletType servlet = webApp.addNewServlet();
                servlet.addNewServletName().setStringValue(service.getName());
                servlet.addNewServletClass().setStringValue(service.getName());

                // add new <servlet-mapping/> element
                String location = "/" + JAXWSUtils.getServiceName(service);
                ServletMappingType servletMapping = webApp.addNewServletMapping();
                servletMapping.addNewServletName().setStringValue(service.getName());
                servletMapping.addNewUrlPattern().setStringValue(location);

                // map service
                PortInfo portInfo = new PortInfo();
                portInfo.setLocation(contextRoot + location);
                map.put(service.getName(), portInfo);
            }
        } else {
            // web.xml present, examine servlet classes and check for web
            // services
            for (ServletType servletType : servletTypes) {
                String servletName = servletType.getServletName().getStringValue().trim();
                if (servletType.isSetServletClass()) {
                    String servletClassName = servletType.getServletClass().getStringValue().trim();
                    try {
                        Class servletClass = classLoader.loadClass(servletClassName);
                        if (JAXWSUtils.isWebService(servletClass)) {
                            LOG.debug("Found POJO Web Service: " + servletName);
                            PortInfo portInfo = new PortInfo();
                            map.put(servletName, portInfo);
                        }
                    } catch (Exception e) {
                        throw new DeploymentException("Failed to load servlet class "
                                                      + servletClassName, e);
                    }
                }
            }

            // update web service locations
            for (Map.Entry entry : map.entrySet()) {
                String servletName = (String) entry.getKey();
                PortInfo portInfo = (PortInfo) entry.getValue();

                String location = (String) correctedPortLocations.get(servletName);
                if (location != null) {
                    portInfo.setLocation(location);
                }
            }
        }
    }       
                   
    private void discoverEJBWebServices(Module module,
                                        Map correctedPortLocations,
                                        Map<String, PortInfo> map) 
        throws DeploymentException {
        ClassLoader classLoader = module.getEarContext().getClassLoader();
        EjbModule ejbModule = (EjbModule) module;
        for (EnterpriseBeanInfo bean : ejbModule.getEjbJarInfo().enterpriseBeans) {
            if (bean.type != EnterpriseBeanInfo.STATELESS) {
                continue;
            }            
            try {
                Class ejbClass = classLoader.loadClass(bean.ejbClass);
                if (JAXWSUtils.isWebService(ejbClass)) {
                    LOG.debug("Found EJB Web Service: " + bean.ejbName);
                    PortInfo portInfo = new PortInfo();
                    String location = (String) correctedPortLocations.get(bean.ejbName);
                    if (location == null) {
                        // set default location, i.e. /@WebService.serviceName/@WebService.name
                        location = "/" + JAXWSUtils.getServiceName(ejbClass) + "/" + JAXWSUtils.getName(ejbClass);
                    }
                    portInfo.setLocation(location);
                    map.put(bean.ejbName, portInfo);
                }
            } catch (Exception e) {
                throw new DeploymentException("Failed to load ejb class "
                                              + bean.ejbName, e);
            }
        }
    }
    
    /**
     * Returns a list of any classes annotated with @WebService or
     * @WebServiceProvider annotation.
     */
    private List<Class> discoverWebServices(JarFile moduleFile,
                                            boolean isEJB)                                                      
            throws DeploymentException {
        LOG.debug("Discovering web service classes");

        File tmpDir = null;
        List<URL> urlList = new ArrayList<URL>();
        if (isEJB) {
            File jarFile = new File(moduleFile.getName());
            try {
                urlList.add(jarFile.toURL());
            } catch (MalformedURLException e) {
                // this should not happen
                throw new DeploymentException(e);
            }
        } else {
            /*
             * Can't get ClassLoader to load nested Jar files, so
             * unpack the module Jar file and discover all nested Jar files
             * within it and the classes/ directory.
             */
            try {
                tmpDir = DeploymentUtil.createTempDir();
                /*
                 * This is needed becuase DeploymentUtil.unzipToDirectory()
                 * always closes the passed JarFile.
                 */
                JarFile module = new JarFile(moduleFile.getName());
                DeploymentUtil.unzipToDirectory(module, tmpDir);
            } catch (IOException e) {
                if (tmpDir != null) {
                    DeploymentUtil.recursiveDelete(tmpDir);
                }
                throw new DeploymentException("Failed to expand the module archive", e);
            }

            // create URL list
            Enumeration<JarEntry> jarEnum = moduleFile.entries();
            while (jarEnum.hasMoreElements()) {
                JarEntry entry = jarEnum.nextElement();
                String name = entry.getName();
                if (name.equals("WEB-INF/classes/")) {
                    // ensure it is first
                    File classesDir = new File(tmpDir, "WEB-INF/classes/");
                    try {
                        urlList.add(0, classesDir.toURL());
                    } catch (MalformedURLException e) {
                        // this should not happen, ignore
                    }
                } else if (name.startsWith("WEB-INF/lib/")
                        && name.endsWith(".jar")) {
                    File jarFile = new File(tmpDir, name);
                    try {
                        urlList.add(jarFile.toURL());
                    } catch (MalformedURLException e) {
                        // this should not happen, ignore
                    }
                }
            }
        }
        
        URL[] urls = urlList.toArray(new URL[] {});
        JarFileClassLoader tempClassLoader = new JarFileClassLoader(null, urls, this.getClass().getClassLoader());
        ClassFinder classFinder = new ClassFinder(tempClassLoader, urlList);

        List<Class> classes = new ArrayList<Class>();

        classes.addAll(classFinder.findAnnotatedClasses(WebService.class));
        classes.addAll(classFinder.findAnnotatedClasses(WebServiceProvider.class));       

        tempClassLoader.destroy();

        if (tmpDir != null) {
            DeploymentUtil.recursiveDelete(tmpDir);
        }

        return classes;
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
