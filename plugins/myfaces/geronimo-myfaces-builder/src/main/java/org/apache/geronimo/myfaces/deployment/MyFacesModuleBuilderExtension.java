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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.myfaces.deployment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.faces.component.FacesComponent;
import javax.faces.component.behavior.FacesBehavior;
import javax.faces.context.ExternalContext;
import javax.faces.convert.FacesConverter;
import javax.faces.event.NamedEvent;
import javax.faces.render.FacesBehaviorRenderer;
import javax.faces.render.FacesRenderer;
import javax.faces.validator.FacesValidator;
import javax.faces.webapp.FacesServlet;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.j2ee.annotation.Holder;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilderExtension;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.deployment.WebModule;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.util.IOUtils;
import org.apache.geronimo.kernel.util.JarUtils;
import org.apache.geronimo.myfaces.FacesConfigDigester;
import org.apache.geronimo.myfaces.LifecycleProviderGBean;
import org.apache.geronimo.myfaces.config.resource.ConfigurationResource;
import org.apache.geronimo.myfaces.config.resource.osgi.api.ConfigRegistry;
import org.apache.geronimo.myfaces.info.GeronimoFacesConfigData;
import org.apache.geronimo.myfaces.webapp.GeronimoStartupServletContextListener;
import org.apache.geronimo.myfaces.webapp.MyFacesWebAppContext;
import org.apache.geronimo.web.info.WebAppInfo;
import org.apache.myfaces.config.annotation.AnnotationConfigurator;
import org.apache.myfaces.config.element.FacesConfig;
import org.apache.myfaces.config.element.ManagedBean;
import org.apache.myfaces.spi.FacesConfigurationMerger;
import org.apache.myfaces.spi.FacesConfigurationMergerFactory;
import org.apache.myfaces.spi.FacesConfigurationProviderFactory;
import org.apache.openejb.jee.ParamValue;
import org.apache.openejb.jee.Servlet;
import org.apache.openejb.jee.WebApp;
import org.apache.xbean.finder.BundleAnnotationFinder;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xbean.osgi.bundle.util.BundleClassLoader;
import org.apache.xbean.osgi.bundle.util.DiscoveryRange;
import org.apache.xbean.osgi.bundle.util.ResourceDiscoveryFilter;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @version $Rev $Date
 */

@GBean(j2eeType = NameFactory.MODULE_BUILDER)
public class MyFacesModuleBuilderExtension implements ModuleBuilderExtension {

    private static final Logger log = LoggerFactory.getLogger(MyFacesModuleBuilderExtension.class);

    private final Environment defaultEnvironment;

    private final AbstractNameQuery providerFactoryNameQuery;

    private final NamingBuilder namingBuilders;

    private static final String CONTEXT_LISTENER_NAME = GeronimoStartupServletContextListener.class.getName();

    private static final String FACES_SERVLET_NAME = FacesServlet.class.getName();

    private FacesConfigDigester defaultFacesConfigUnmarshaller = new FacesConfigDigester();

    private FacesConfig standardFacesConfig;

    public static final EARContext.Key<Set<ConfigurationResource>> JSF_META_INF_CONFIGURATION_RESOURCES = new EARContext.Key<Set<ConfigurationResource>>() {

        @Override
        public Set<ConfigurationResource> get(Map<EARContext.Key, Object> context) {
            return (Set<ConfigurationResource>) context.get(this);
        }
    };

    public static final EARContext.Key<Set<ConfigurationResource>> JSF_FACELET_CONFIG_RESOURCES = new EARContext.Key<Set<ConfigurationResource>>() {

        @Override
        public Set<ConfigurationResource> get(Map<EARContext.Key, Object> context) {
            return (Set<ConfigurationResource>) context.get(this);
        }
    };

    public MyFacesModuleBuilderExtension(@ParamAttribute(name = "defaultEnvironment") Environment defaultEnvironment,
            @ParamAttribute(name = "providerFactoryNameQuery") AbstractNameQuery providerFactoryNameQuery,
            @ParamReference(name = "NamingBuilders", namingType = NameFactory.MODULE_BUILDER) NamingBuilder namingBuilders) {
        this.defaultEnvironment = defaultEnvironment;
        this.providerFactoryNameQuery = providerFactoryNameQuery;
        this.namingBuilders = namingBuilders;
        this.standardFacesConfig = getStandardFacesConfig();
    }

    public void createModule(Module module, Bundle bundle, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        mergeEnvironment(module);
    }

    public void createModule(Module module, Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment environment, Object moduleContextInfo, AbstractName earName, Naming naming,
            ModuleIDBuilder idBuilder) throws DeploymentException {
        mergeEnvironment(module);
    }

    private void mergeEnvironment(Module module) {
        if (!(module instanceof WebModule)) {
            //not a web module, nothing to do
            return;
        }
        WebModule webModule = (WebModule) module;
        WebApp webApp = webModule.getSpecDD();
        if (!hasFacesServlet(webApp)) {
            return;
        }
        EnvironmentBuilder.mergeEnvironments(module.getEnvironment(), defaultEnvironment);
    }

    public void installModule(JarFile earFile, EARContext earContext, Module module, Collection configurationStores, ConfigurationStore targetConfigurationStore, Collection repository)
            throws DeploymentException {
        if (!(module instanceof WebModule)) {
            return;
        }
        module.getEarContext().getGeneralData().put(JSF_META_INF_CONFIGURATION_RESOURCES, findMetaInfConfigurationResources(earContext, module));
        module.getEarContext().getGeneralData().put(JSF_FACELET_CONFIG_RESOURCES, findFaceletConfigResources(earContext, module));
    }

    public void initContext(EARContext earContext, Module module, Bundle bundle) throws DeploymentException {
    }

    public void addGBeans(EARContext earContext, Module module, Bundle bundle, Collection repository) throws DeploymentException {
        if (!(module instanceof WebModule)) {
            //not a web module, nothing to do
            return;
        }
        WebModule webModule = (WebModule) module;
        WebApp webApp = webModule.getSpecDD();
        if (!hasFacesServlet(webApp)) {
            return;
        }

        EARContext moduleContext = module.getEarContext();
        Map sharedContext = module.getSharedContext();
        //add the ServletContextListener to the web app context
        GBeanData webAppData = (GBeanData) sharedContext.get(WebModule.WEB_APP_DATA);
        // add myfaces listener
        WebAppInfo webAppInfo = (WebAppInfo) webAppData.getAttribute("webAppInfo");
        if (webAppInfo != null && !webAppInfo.listeners.contains(CONTEXT_LISTENER_NAME)) {
            webAppInfo.listeners.add(CONTEXT_LISTENER_NAME);
        } else {
            Object value = webAppData.getAttribute("listenerClassNames");
            if (value instanceof Collection && !((Collection) value).contains(CONTEXT_LISTENER_NAME)) {
                ((Collection<String>) value).add(CONTEXT_LISTENER_NAME);
            }
        }

        AbstractName moduleName = moduleContext.getModuleName();
        Map<EARContext.Key, Object> buildingContext = new HashMap<EARContext.Key, Object>();
        buildingContext.put(NamingBuilder.GBEAN_NAME_KEY, moduleName);

        //use the same holder object as the web app.
        Holder holder = NamingBuilder.INJECTION_KEY.get(sharedContext);
        buildingContext.put(NamingBuilder.INJECTION_KEY, holder);

        XmlObject jettyWebApp = webModule.getVendorDD();

        //Parse default web application faces configuration file WEB-INF/faces-config.xml
        FacesConfig webAppFacesConfig = getWebAppFacesConfig(webModule);

        //Parse all faces-config.xml files found in META-INF folder
        Set<ConfigurationResource> metaInfConfigurationResources = JSF_META_INF_CONFIGURATION_RESOURCES.get(earContext.getGeneralData());
        List<FacesConfig> metaInfFacesConfigs = new ArrayList<FacesConfig>(metaInfConfigurationResources.size());
        for (ConfigurationResource configurationResource : metaInfConfigurationResources) {
            URL url;
            try {
                url = configurationResource.getConfigurationResourceURL(bundle);
            } catch (MalformedURLException e) {
                throw new DeploymentException("Fail to read the faces Configuration file " + configurationResource.getConfigurationResourcePath()
                        + (configurationResource.getJarFilePath() == null ? "" : " from jar file " + configurationResource.getJarFilePath()), e);
            }
            if (url == null) {
                throw new DeploymentException("Fail to read the faces Configuration file " + configurationResource.getConfigurationResourcePath()
                        + (configurationResource.getJarFilePath() == null ? "" : " from jar file " + configurationResource.getJarFilePath()));
            }
            metaInfFacesConfigs.add(parseConfigFile(url, url.toExternalForm()));
        }

        //Parse all faces-config.xml files found in classloader hierarchy
        List<FacesConfig> classloaderFacesConfigs = new ArrayList<FacesConfig>();
        classloaderFacesConfigs.addAll(metaInfFacesConfigs);
        ServiceReference ref = null;
        try {
            ref = bundle.getBundleContext().getServiceReference(ConfigRegistry.class.getName());
            ConfigRegistry configRegistry = (ConfigRegistry) bundle.getBundleContext().getService(ref);
            for (URL url : configRegistry.getRegisteredConfigUrls()) {
                classloaderFacesConfigs.add(parseConfigFile(url, url.toExternalForm()));
            }
        } finally {
            if (ref != null) {
                bundle.getBundleContext().ungetService(ref);
            }
        }

        //Parse all context faces-config.xml files configured in web.xml file
        List<FacesConfig> contextSpecifiedFacesConfigs = getContextFacesConfigs(webApp, webModule);

        //Scan annotations if required
        FacesConfig annotationsFacesConfig = null;
        if (webAppFacesConfig == null || !Boolean.parseBoolean(webAppFacesConfig.getMetadataComplete())) {
            annotationsFacesConfig = getJSFAnnotationFacesConfig(earContext, webModule, bundle, metaInfConfigurationResources);
        }

        AbstractName myFacesWebAppContextName = moduleContext.getNaming().createChildName(moduleName, "myFacesWebAppContext", "MyFacesWebAppContext");
        GBeanData myFacesWebAppContextData = new GBeanData(myFacesWebAppContextName, MyFacesWebAppContext.class);

        myFacesWebAppContextData.setAttribute("faceletConfigResources", JSF_FACELET_CONFIG_RESOURCES.get(earContext.getGeneralData()));
        ClassLoader deploymentClassLoader = new BundleClassLoader(bundle);
        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(deploymentClassLoader);
            StandaloneExternalContext standaloneExternalContext = new StandaloneExternalContext(deploymentClassLoader);
            FacesConfigurationProviderFactory.setFacesConfigurationProviderFactory(standaloneExternalContext, new GeronimoFacesConfigurationProviderFactory(standardFacesConfig, webAppFacesConfig,
                    annotationsFacesConfig, classloaderFacesConfigs, contextSpecifiedFacesConfigs));
            FacesConfigurationMerger facesConfigurationMerger = FacesConfigurationMergerFactory.getFacesConfigurationMergerFactory(standaloneExternalContext).getFacesConfigurationMerger(
                    standaloneExternalContext);
            myFacesWebAppContextData.setAttribute("facesConfigData", new GeronimoFacesConfigData(facesConfigurationMerger.getFacesConfigData(standaloneExternalContext)));
        } finally {
            Thread.currentThread().setContextClassLoader(oldContextClassLoader);
        }

        List<FacesConfig> namingFacesConfigs = new ArrayList<FacesConfig>();
        if (webAppFacesConfig != null) {
            namingFacesConfigs.add(webAppFacesConfig);
        }
        if (annotationsFacesConfig != null) {
            namingFacesConfigs.add(annotationsFacesConfig);
        }
        namingFacesConfigs.addAll(contextSpecifiedFacesConfigs);
        namingFacesConfigs.addAll(metaInfFacesConfigs);

        ClassFinder classFinder = createMyFacesClassFinder(namingFacesConfigs, bundle);
        webModule.setClassFinder(classFinder);

        namingBuilders.buildNaming(webApp, jettyWebApp, webModule, buildingContext);

        AbstractName providerName = moduleContext.getNaming().createChildName(moduleName, "jsf-lifecycle", "jsf");
        GBeanData providerData = new GBeanData(providerName, LifecycleProviderGBean.class);
        providerData.setAttribute("holder", holder);
        providerData.setReferencePatterns("ContextSource", webAppData.getReferencePatterns("ContextSource"));
        providerData.setReferencePattern("LifecycleProviderFactory", providerFactoryNameQuery);
        try {
            moduleContext.addGBean(providerData);
            moduleContext.addGBean(myFacesWebAppContextData);
        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException("Duplicate jsf config gbean in web module", e);
        }

        //make the web app start second after the injection machinery
        webAppData.addDependency(providerName);

    }

    protected FacesConfig getJSFAnnotationFacesConfig(EARContext earContext, Module module, Bundle bundle, Set<ConfigurationResource> metaInfConfigurationResources) throws DeploymentException {
        ServiceReference reference = bundle.getBundleContext().getServiceReference(PackageAdmin.class.getName());
        try {
            PackageAdmin packageAdmin = (PackageAdmin) bundle.getBundleContext().getService(reference);
            final Set<String> requiredJarFiles = new HashSet<String>();
            for (ConfigurationResource configurationResource : metaInfConfigurationResources) {
                if (configurationResource.getJarFilePath() != null) {
                    requiredJarFiles.add(configurationResource.getJarFilePath());
                }
            }
            final Map<Class<? extends Annotation>, Set<Class<?>>> annotationClassSetMap = new HashMap<Class<? extends Annotation>, Set<Class<?>>>();
            BundleAnnotationFinder bundleAnnotationFinder = new BundleAnnotationFinder(packageAdmin, bundle, new ResourceDiscoveryFilter() {

                @Override
                public boolean directoryDiscoveryRequired(String directory) {
                    //TODO WEB-INF/classes ???
                    return true;
                }

                @Override
                public boolean rangeDiscoveryRequired(DiscoveryRange discoveryRange) {
                    return discoveryRange.equals(DiscoveryRange.BUNDLE_CLASSPATH);
                }

                @Override
                public boolean zipFileDiscoveryRequired(String jarFile) {
                    return requiredJarFiles.contains(jarFile);
                }

            });
            annotationClassSetMap.put(FacesComponent.class, new HashSet(bundleAnnotationFinder.findAnnotatedClasses(FacesComponent.class)));
            annotationClassSetMap.put(FacesConverter.class, new HashSet(bundleAnnotationFinder.findAnnotatedClasses(FacesConverter.class)));
            annotationClassSetMap.put(FacesValidator.class, new HashSet(bundleAnnotationFinder.findAnnotatedClasses(FacesValidator.class)));
            annotationClassSetMap.put(FacesRenderer.class, new HashSet(bundleAnnotationFinder.findAnnotatedClasses(FacesRenderer.class)));
            annotationClassSetMap.put(javax.faces.bean.ManagedBean.class, new HashSet(bundleAnnotationFinder.findAnnotatedClasses(javax.faces.bean.ManagedBean.class)));
            annotationClassSetMap.put(NamedEvent.class, new HashSet(bundleAnnotationFinder.findAnnotatedClasses(NamedEvent.class)));
            annotationClassSetMap.put(FacesBehavior.class, new HashSet(bundleAnnotationFinder.findAnnotatedClasses(FacesBehavior.class)));
            annotationClassSetMap.put(FacesBehaviorRenderer.class, new HashSet(bundleAnnotationFinder.findAnnotatedClasses(FacesBehaviorRenderer.class)));
            return new AnnotationConfigurator() {

                @Override
                public org.apache.myfaces.config.impl.digester.elements.FacesConfig createFacesConfig(ExternalContext externalContext, boolean metaComplete) {
                    return createFacesConfig(annotationClassSetMap);
                }
            }.createFacesConfig(null, false);
        } catch (Exception e) {
            throw new DeploymentException("Fail to scan JSF annotations", e);
        } finally {
            bundle.getBundleContext().ungetService(reference);
        }
    }

    protected FacesConfig getWebAppFacesConfig(WebModule webModule) throws DeploymentException {
        URL url = webModule.getDeployable().getResource("WEB-INF/faces-config.xml");
        if (url != null) {
            return parseConfigFile(url, "/WEB-INF/faces-config.xml");
        }
        return null;
    }

    protected List<FacesConfig> getContextFacesConfigs(WebApp webApp, WebModule webModule) throws DeploymentException {
        for (ParamValue paramValue : webApp.getContextParam()) {
            if (paramValue.getParamName().trim().equals(FacesServlet.CONFIG_FILES_ATTR)) {
                List<FacesConfig> contextFacesConfigs = new ArrayList<FacesConfig>();
                String configFiles = paramValue.getParamValue().trim();
                StringTokenizer st = new StringTokenizer(configFiles, ",", false);
                while (st.hasMoreTokens()) {
                    String configfile = st.nextToken().trim();
                    if (!configfile.equals("")) {
                        if (configfile.startsWith("/")) {
                            configfile = configfile.substring(1);
                        }
                        URL url = webModule.getEarContext().getTargetURL(webModule.resolve(configfile));
                        if (url == null) {
                            throw new DeploymentException("Could not locate config file " + configfile + " configured with " + FacesServlet.CONFIG_FILES_ATTR + " in the web.xml");
                        } else {
                            contextFacesConfigs.add(parseConfigFile(url, configfile));
                        }
                    }
                }
                return contextFacesConfigs;
            }
        }
        return Collections.<FacesConfig> emptyList();
    }

    protected FacesConfig getStandardFacesConfig() {
        try {
            //TODO A better way to find the standard faces configuration file ?
            return parseConfigFile(FacesConfig.class.getClassLoader().getResource("META-INF/standard-faces-config.xml"), "META-INF/standard-faces-config.xml");
        } catch (DeploymentException e) {
            log.warn("Fail to load the standard faces config file META-INF/standard-faces-config.xml", e);
            return null;
        }
    }

    protected ClassFinder createMyFacesClassFinder(List<FacesConfig> facesConfigs, Bundle bundle) throws DeploymentException {
        List<Class> managedBeanClasses = new ArrayList<Class>();
        for (FacesConfig facesConfig : facesConfigs) {
            for (ManagedBean managedBean : facesConfig.getManagedBeans()) {
                String className = managedBean.getManagedBeanClassName().trim();
                Class<?> clas;
                try {
                    clas = bundle.loadClass(className);
                    while (clas != null) {
                        managedBeanClasses.add(clas);
                        clas = clas.getSuperclass();
                    }
                } catch (ClassNotFoundException e) {
                    log.warn("MyFacesModuleBuilderExtension: Could not load managed bean class: " + className);
                }
            }
        }
        return new ClassFinder(managedBeanClasses);
    }

    protected Set<ConfigurationResource> findMetaInfConfigurationResources(EARContext earContext, Module module) throws DeploymentException {
        Set<ConfigurationResource> metaInfConfigurationResources = new HashSet<ConfigurationResource>();
        //1. jar files in the WEB-INF/lib folder
        File libDirectory = new File(earContext.getBaseDir() + File.separator + "WEB-INF" + File.separator + "lib");
        if (libDirectory.exists()) {
            for (File file : libDirectory.listFiles()) {
                if (!file.getName().endsWith(".jar")) {
                    continue;
                }
                try {
                    if (!JarUtils.isJarFile(file)) {
                        continue;
                    }
                } catch (IOException e) {
                    continue;
                }
                ZipInputStream in = null;
                JarFile jarFile = null;
                try {
                    jarFile = new JarFile(file);
                    in = new ZipInputStream(new FileInputStream(file));
                    ZipEntry zipEntry;

                    while ((zipEntry = in.getNextEntry()) != null) {
                        String name = zipEntry.getName();
                        // Scan config files named as faces-config.xml or *.faces-config.xml under META-INF
                        if (name.equals("META-INF/faces-config.xml") || (name.startsWith("META-INF/") && name.endsWith(".faces-config.xml"))) {
                            //TODO Double check the relative jar file path once EAR is really supported
                            metaInfConfigurationResources.add(new ConfigurationResource("WEB-INF/lib/" + file.getName(), name));
                        }
                    }
                } catch (Exception e) {
                    throw new DeploymentException("Can not preprocess myfaces application configuration resources", e);
                } finally {
                    IOUtils.close(in);
                    JarUtils.close(jarFile);
                }
            }
        }
        //2. WEB-INF/classes/META-INF folder
        File webInfClassesDirectory = new File(earContext.getBaseDir() + File.separator + "WEB-INF" + File.separator + "classes" + File.separator + "META-INF");
        if (webInfClassesDirectory.exists() && webInfClassesDirectory.isDirectory()) {
            for (File file : webInfClassesDirectory.listFiles()) {
                if (file.isDirectory()) {
                    continue;
                }
                String fileName = file.getName();
                if (fileName.equals("faces-config.xml") || fileName.endsWith(".faces-config.xml")) {
                    //TODO Double check the relative jar file path once EAR is really supported
                    String filePath = "WEB-INF/classes/META-INF/" + fileName;
                    metaInfConfigurationResources.add(new ConfigurationResource(null, filePath));
                }
            }
        }
        //3. META-INF folder
        File baseDirectory = new File(earContext.getBaseDir() + File.separator + "META-INF");
        if (baseDirectory.exists() && baseDirectory.isDirectory()) {
            for (File file : baseDirectory.listFiles()) {
                if (file.isDirectory()) {
                    continue;
                }
                String fileName = file.getName();
                if (fileName.equals("faces-config.xml") || fileName.endsWith(".faces-config.xml")) {
                    //TODO Double check the relative jar file path once EAR is really supported
                    String filePath = "META-INF/" + fileName;
                    metaInfConfigurationResources.add(new ConfigurationResource(null, filePath));
                }
            }
        }
        return metaInfConfigurationResources;
    }

    protected Set<ConfigurationResource> findFaceletConfigResources(EARContext earContext, Module module) throws DeploymentException {
        Set<ConfigurationResource> faceletConfigResources = new HashSet<ConfigurationResource>();
        //1. jar files in the WEB-INF/lib folder
        File libDirectory = new File(earContext.getBaseDir() + File.separator + "WEB-INF" + File.separator + "lib");
        if (libDirectory.exists()) {
            for (File file : libDirectory.listFiles()) {
                if (!file.getName().endsWith(".jar")) {
                    continue;
                }
                try {
                    if (!JarUtils.isJarFile(file)) {
                        continue;
                    }
                } catch (IOException e) {
                    continue;
                }
                ZipInputStream in = null;
                JarFile jarFile = null;
                try {
                    jarFile = new JarFile(file);
                    in = new ZipInputStream(new FileInputStream(file));
                    ZipEntry zipEntry;

                    while ((zipEntry = in.getNextEntry()) != null) {
                        String name = zipEntry.getName();
                        // Scan config files named as faces-config.xml or *.faces-config.xml under META-INF
                        if (name.startsWith("META-INF/") && name.endsWith(".taglib.xml")) {
                            //TODO Double check the relative jar file path once EAR is really supported
                            faceletConfigResources.add(new ConfigurationResource("WEB-INF/lib/" + file.getName(), name));
                        }
                    }
                } catch (Exception e) {
                    throw new DeploymentException("Can not preprocess myfaces application configuration resources", e);
                } finally {
                    IOUtils.close(in);
                    JarUtils.close(jarFile);
                }
            }
        }
        //2. WEB-INF/classes/META-INF folder
        File webInfClassesDirectory = new File(earContext.getBaseDir() + File.separator + "WEB-INF" + File.separator + "classes" + File.separator + "META-INF");
        if (webInfClassesDirectory.exists() && webInfClassesDirectory.isDirectory()) {
            for (File file : webInfClassesDirectory.listFiles()) {
                if (file.isDirectory()) {
                    continue;
                }
                String fileName = file.getName();
                if (fileName.equals("faces-config.xml") || fileName.endsWith(".taglib.xml")) {
                    //TODO Double check the relative jar file path once EAR is really supported
                    String filePath = "WEB-INF/classes/META-INF/" + fileName;
                    faceletConfigResources.add(new ConfigurationResource(null, filePath));
                }
            }
        }
        //3. META-INF folder
        File baseDirectory = new File(earContext.getBaseDir() + File.separator + "META-INF");
        if (baseDirectory.exists() && baseDirectory.isDirectory()) {
            for (File file : baseDirectory.listFiles()) {
                if (file.isDirectory()) {
                    continue;
                }
                String fileName = file.getName();
                if (fileName.endsWith(".taglib.xml")) {
                    //TODO Double check the relative jar file path once EAR is really supported
                    String filePath = "META-INF/" + fileName;
                    faceletConfigResources.add(new ConfigurationResource(null, filePath));
                }
            }
        }
        return faceletConfigResources;
    }

    private boolean hasFacesServlet(WebApp webApp) {
        for (Servlet servlet : webApp.getServlet()) {
            if (servlet.getServletClass() != null && FACES_SERVLET_NAME.equals(servlet.getServletClass().trim())) {
                return true;
            }
        }
        return false;
    }

    private FacesConfig parseConfigFile(URL url, String systemId) throws DeploymentException {
        if (log.isDebugEnabled()) {
            log.debug("parseConfigFile( " + url.toString() + " ): Entry");
        }
        InputStream in = null;
        try {
            in = url.openStream();
            return defaultFacesConfigUnmarshaller.getFacesConfig(in, systemId);
        } catch (IOException e) {
            throw new DeploymentException("Error reading jsf configuration file " + url, e);
        } catch (SAXException e) {
            throw new DeploymentException("Error reading jsf configuration file " + url, e);
        } finally {
            IOUtils.close(in);
        }
    }
}
