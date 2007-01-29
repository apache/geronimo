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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.jws.WebService;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.cxf.jaxws.javaee.PortComponentType;
import org.apache.cxf.jaxws.javaee.ServiceImplBeanType;
import org.apache.cxf.jaxws.javaee.WebserviceDescriptionType;
import org.apache.cxf.jaxws.javaee.WebservicesType;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.deployment.WebModule;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.cxf.PortInfo;
import org.apache.geronimo.cxf.CXFWebServiceContainerFactoryGBean;
import org.apache.geronimo.kernel.classloader.JarFileClassLoader;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.xbean.finder.ClassFinder;

public class CXFBuilder implements WebServiceBuilder {

    private static final Log LOG = LogFactory.getLog(CXFBuilder.class);

    private final Environment defaultEnvironment;

    private static final String KEY = CXFBuilder.class.getName();

    public CXFBuilder(Environment defaultEnvironment) {
        this.defaultEnvironment = defaultEnvironment;
    }

    public void findWebServices(JarFile moduleFile,
                                boolean isEJB,
                                Map servletLocations,
                                Environment environment,
                                Map sharedContext) throws DeploymentException {
        Map portMap = null;
        String path = isEJB ? "META-INF/webservices.xml" : "WEB-INF/webservices.xml";
        try {
            URL wsDDUrl = DeploymentUtil.createJarURL(moduleFile, path);
            InputStream in = wsDDUrl.openStream();
            portMap = parseWebServiceDescriptor(in, wsDDUrl, moduleFile, isEJB, servletLocations);
        } catch (IOException e) {
            // webservices.xml does not exist, search classes for annotations
            portMap = discoverWebServices(moduleFile, isEJB, servletLocations);
        }

        if (portMap != null) {
            EnvironmentBuilder.mergeEnvironments(environment, defaultEnvironment);
            sharedContext.put(KEY, portMap);
        }

    }

    private Map<String, PortInfo> discoverWebServices(JarFile moduleFile,
                                                      boolean isEJB,
                                                      Map correctedPortLocations)
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
                throw new DeploymentException();
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

        Map<String, PortInfo> map = null;
        List<Class> classes = null;
        
        classes = classFinder.findAnnotatedClasses(WebService.class);        
        map = updatePortMap(classes, map, correctedPortLocations);
        classes = classFinder.findAnnotatedClasses(WebServiceProvider.class);
        map = updatePortMap(classes, map, correctedPortLocations);
               
        tempClassLoader.destroy();
        
        if (tmpDir != null) {
            DeploymentUtil.recursiveDelete(tmpDir);
        }
        
        return map;
    }

    private static Map<String, PortInfo> updatePortMap(List<Class> classes,
                                                       Map<String, PortInfo> map,
                                                       Map correctedPortLocations) {
        for (Class clazz : classes) {
            if (isProperWebService(clazz)) {
                LOG.debug("Found web service class: " + clazz.getName());
                if (map == null) {
                    map = new HashMap<String, PortInfo>();
                }
                PortInfo portInfo = new PortInfo();
                String location = (String) correctedPortLocations.get(clazz.getName());
                portInfo.setLocation(location);
                map.put(clazz.getName(), portInfo);
            }
        }
        return map;
    }

    private static boolean isProperWebService(Class clazz) {
        int modifiers = clazz.getModifiers();
        return (Modifier.isPublic(modifiers) && 
                !Modifier.isFinal(modifiers) && 
                !Modifier.isAbstract(modifiers));
    }

    private Map<String, PortInfo> parseWebServiceDescriptor(InputStream in,
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
                obj = ((JAXBElement)obj).getValue();
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

                    portInfo.setHandlers(port.getHandlerChains());

                    if (port.getWsdlPort() != null) {
                        portInfo.setWsdlPort(port.getWsdlPort().getValue());
                    }

                    if (port.getWsdlService() != null) {
                        portInfo.setWsdlService(port.getWsdlService().getValue());
                    }

                    String location = (String)correctedPortLocations.get(serviceLink);
                    portInfo.setLocation(location);

                    if (map == null) {
                        map = new HashMap<String, PortInfo>();
                    }
                    map.put(serviceLink, portInfo);
                }
            }

            return map;
        } catch (FileNotFoundException e) {
            return Collections.EMPTY_MAP;
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

    private static String getString(String in) {
        if (in != null) {
            in = in.trim();
            if (in.length() == 0) {
                return null;
            }
        }
        return in;
    }

    public boolean configurePOJO(GBeanData targetGBean,
                                 String servletName,
                                 Module module,
                                 String seiClassName,
                                 DeploymentContext context)
            throws DeploymentException {
        Map sharedContext = ((WebModule) module).getSharedContext();
        Map portInfoMap = (Map) sharedContext.get(KEY);
        if (portInfoMap == null) {
            // not ours
            return false;
        }
        PortInfo portInfo = (PortInfo) portInfoMap.get(servletName);
        if (portInfo == null) {
            // not ours
            return false;
        }

        Map componentContext = null;
        try {
            GBeanData moduleGBean = context.getGBeanInstance(context.getModuleName());
            componentContext = (Map)moduleGBean.getAttribute("componentContext");
        } catch (GBeanNotFoundException e) {
            LOG.warn("ModuleGBean not found. JNDI resource injection will not work.");
        }

        LOG.info("configuring POJO webservice: " + servletName + " sei: " + seiClassName);

        // verify that the class is loadable
        ClassLoader classLoader = context.getClassLoader();
        loadSEI(seiClassName, classLoader);

        AbstractName containerFactoryName = context.getNaming().createChildName(targetGBean.getAbstractName(), "cxfWebServiceContainerFactory", NameFactory.GERONIMO_SERVICE);
        GBeanData containerFactoryData = new GBeanData(containerFactoryName, CXFWebServiceContainerFactoryGBean.GBEAN_INFO);
        containerFactoryData.setAttribute("portInfo", portInfo);
        containerFactoryData.setAttribute("endpointClassName", seiClassName);
        containerFactoryData.setAttribute("componentContext", componentContext);
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

        return true;
    }

    public boolean configureEJB(GBeanData targetGBean,
                                String ejbName,
                                JarFile moduleFile,
                                Map sharedContext,
                                ClassLoader classLoader)
            throws DeploymentException {
        throw new DeploymentException("configureEJB NYI");
    }

    Class<?> loadSEI(String className, ClassLoader loader) throws DeploymentException {
        try {
            return loader.loadClass(className);
        } catch (ClassNotFoundException ex) {
            throw new DeploymentException("unable to load Service Endpoint Interface: " + className, ex);
        }
    }

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
