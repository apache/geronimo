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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.WebModule;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.jaxws.PortInfo;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.classloader.JarFileClassLoader;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.xbean.finder.ClassFinder;

import javax.jws.WebService;
import javax.xml.ws.WebServiceProvider;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public abstract class JAXWSServiceBuilder implements WebServiceBuilder {
    private static final Log LOG = LogFactory.getLog(JAXWSServiceBuilder.class);

    protected final Environment defaultEnvironment;

    public JAXWSServiceBuilder(Environment defaultEnvironment) {
        this.defaultEnvironment = defaultEnvironment;
    }

    protected String getKey() {
        return getClass().getName();
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
            sharedContext.put(getKey(), portMap);
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

    protected abstract Map<String, PortInfo> parseWebServiceDescriptor(InputStream in,
                                                            URL wsDDUrl,
                                                            JarFile moduleFile,
                                                            boolean isEJB,
                                                            Map correctedPortLocations)
            throws DeploymentException;

    public boolean configurePOJO(GBeanData targetGBean,
                                 String servletName,
                                 Module module,
                                 String seiClassName,
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

        Map componentContext = null;
        try {
            GBeanData moduleGBean = context.getGBeanInstance(context.getModuleName());
            componentContext = (Map)moduleGBean.getAttribute("componentContext");
        } catch (GBeanNotFoundException e) {
            LOG.warn("ModuleGBean not found. JNDI resource injection will not work.");
        }

        LOG.info("Configuring POJO Web Service: " + servletName + " sei: " + seiClassName);

        // verify that the class is loadable
        ClassLoader classLoader = context.getClassLoader();
        loadSEI(seiClassName, classLoader);

        AbstractName containerFactoryName = context.getNaming().createChildName(targetGBean.getAbstractName(), getContainerFactoryGBeanInfo().getName(), NameFactory.GERONIMO_SERVICE);
        GBeanData containerFactoryData = new GBeanData(containerFactoryName, getContainerFactoryGBeanInfo());
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

    protected abstract GBeanInfo getContainerFactoryGBeanInfo();

    public boolean configureEJB(GBeanData targetGBean,
                                String ejbName,
                                JarFile moduleFile,
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
        
        String shortEjbName = (String)targetGBean.getAttribute("ejbName");
                
        // FIXME: kind of a hack now, need better solution
        String location = portInfo.getLocation();
        if (location == null) {            
            location = "/" + trimPath(new File(moduleFile.getName()).getName()) + "/" + shortEjbName;
            portInfo.setLocation(location);
        }

        LOG.info("Configuring EJB Web Service: " + ejbName + " at " + location);
        
        targetGBean.setAttribute("portInfo", portInfo);
        
        return true;
    }
    
    private String trimPath(String path) {
        if (path == null) {
            return null;
        }

        if (path.endsWith(".war") || path.endsWith(".jar")) {
            path = path.substring(0, path.length() - 4);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        return path;
    }

    Class<?> loadSEI(String className, ClassLoader loader) throws DeploymentException {
        try {
            return loader.loadClass(className);
        } catch (ClassNotFoundException ex) {
            throw new DeploymentException("unable to load Service Endpoint Interface: " + className, ex);
        }
    }
}
