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

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
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
import org.apache.xbean.osgi.bundle.util.BundleResourceFinder;
import org.apache.xbean.osgi.bundle.util.BundleResourceFinder.ResourceFinderCallback;
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
            @ParamReference(name = "NamingBuilders", namingType = NameFactory.MODULE_BUILDER) NamingBuilder namingBuilders) {
        this.defaultEnvironment = defaultEnvironment;
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
    }

    public void initContext(EARContext earContext, Module module, Bundle bundle) throws DeploymentException {
        module.getEarContext().getGeneralData().put(JSF_META_INF_CONFIGURATION_RESOURCES, findMetaInfConfigurationResources(earContext, module, bundle));
        module.getEarContext().getGeneralData().put(JSF_FACELET_CONFIG_RESOURCES, findFaceletConfigResources(earContext, module, bundle));
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

        AbstractName moduleName = module.getModuleName();
        Map<EARContext.Key, Object> buildingContext = new HashMap<EARContext.Key, Object>();
        buildingContext.put(NamingBuilder.GBEAN_NAME_KEY, moduleName);

        //use the same holder object as the web app.
        Holder holder = NamingBuilder.INJECTION_KEY.get(sharedContext);
        buildingContext.put(NamingBuilder.INJECTION_KEY, holder);

        XmlObject jettyWebApp = webModule.getVendorDD();

        //Parse default web application faces configuration file WEB-INF/faces-config.xml
        FacesConfig webAppFacesConfig = getWebAppFacesConfig(webModule);

        //Parse all faces-config.xml files found in META-INF folder
        Set<ConfigurationResource> metaInfConfigurationResources = JSF_META_INF_CONFIGURATION_RESOURCES.get(module.getEarContext().getGeneralData());
        List<FacesConfig> metaInfFacesConfigs = new ArrayList<FacesConfig>(metaInfConfigurationResources.size());
        for (ConfigurationResource configurationResource : metaInfConfigurationResources) {
            FacesConfig facesConfig = configurationResource.getFacesConfig();
            if (facesConfig == null) {
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
                facesConfig = parseConfigFile(url, url.toExternalForm());
            }
            metaInfFacesConfigs.add(facesConfig);
        }

        //Parse all faces-config.xml files found in classloader hierarchy
        List<FacesConfig> classloaderFacesConfigs = new ArrayList<FacesConfig>();
        classloaderFacesConfigs.addAll(metaInfFacesConfigs);
        ServiceReference ref = null;
        try {
            ref = bundle.getBundleContext().getServiceReference(ConfigRegistry.class.getName());
            ConfigRegistry configRegistry = (ConfigRegistry) bundle.getBundleContext().getService(ref);
            classloaderFacesConfigs.addAll(configRegistry.getDependentFacesConfigs(bundle.getBundleId()));
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

        Set<ConfigurationResource> faceletsLibraries = new HashSet<ConfigurationResource>();
        faceletsLibraries.addAll(JSF_FACELET_CONFIG_RESOURCES.get(webModule.getEarContext().getGeneralData()));
        faceletsLibraries.addAll(getContextFaceletsLibraries(webApp, webModule));
        myFacesWebAppContextData.setAttribute("faceletConfigResources", faceletsLibraries);

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
        try {
            moduleContext.addGBean(providerData);
            moduleContext.addGBean(myFacesWebAppContextData);
        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException("Duplicate jsf config gbean in web module", e);
        }

        myFacesWebAppContextData.setReferencePattern("LifecycleProvider", providerName);
        //make the web app start second after the injection machinery
        webAppData.addDependency(providerName);
        webAppData.addDependency(myFacesWebAppContextName);
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
            annotationClassSetMap.put(FacesComponent.class, new HashSet<Class<?>>(bundleAnnotationFinder.findAnnotatedClasses(FacesComponent.class)));
            annotationClassSetMap.put(FacesConverter.class, new HashSet<Class<?>>(bundleAnnotationFinder.findAnnotatedClasses(FacesConverter.class)));
            annotationClassSetMap.put(FacesValidator.class, new HashSet<Class<?>>(bundleAnnotationFinder.findAnnotatedClasses(FacesValidator.class)));
            annotationClassSetMap.put(FacesRenderer.class, new HashSet<Class<?>>(bundleAnnotationFinder.findAnnotatedClasses(FacesRenderer.class)));
            annotationClassSetMap.put(javax.faces.bean.ManagedBean.class, new HashSet<Class<?>>(bundleAnnotationFinder.findAnnotatedClasses(javax.faces.bean.ManagedBean.class)));
            annotationClassSetMap.put(NamedEvent.class, new HashSet<Class<?>>(bundleAnnotationFinder.findAnnotatedClasses(NamedEvent.class)));
            annotationClassSetMap.put(FacesBehavior.class, new HashSet<Class<?>>(bundleAnnotationFinder.findAnnotatedClasses(FacesBehavior.class)));
            annotationClassSetMap.put(FacesBehaviorRenderer.class, new HashSet<Class<?>>(bundleAnnotationFinder.findAnnotatedClasses(FacesBehaviorRenderer.class)));
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
                        //faces-config.xml is a center XML for JSF, we will read it separately.
                        if (configfile.equals("WEB-INF/faces-config.xml")) {
                            continue;
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

    protected List<ConfigurationResource> getContextFaceletsLibraries(WebApp webApp, WebModule webModule) throws DeploymentException {
        String moduleNamePrefix = webModule.isStandAlone() ? "" : webModule.getTargetPath() + "/";
        for (ParamValue paramValue : webApp.getContextParam()) {
            if (paramValue.getParamName().trim().equals("javax.faces.FACELETS_LIBRARIES")) {
                List<ConfigurationResource> faceletsLibraries = new ArrayList<ConfigurationResource>();
                String configFiles = paramValue.getParamValue().trim();
                StringTokenizer st = new StringTokenizer(configFiles, ";", false);
                while (st.hasMoreTokens()) {
                    String faceletsLibrary = st.nextToken().trim();
                    if (!faceletsLibrary.isEmpty()) {
                        if (faceletsLibrary.startsWith("/")) {
                            faceletsLibrary = faceletsLibrary.substring(1);
                        }
                        faceletsLibraries.add(new ConfigurationResource(null, moduleNamePrefix + faceletsLibrary));
                    }
                }
                return faceletsLibraries;
            }
        }
        return Collections.<ConfigurationResource> emptyList();
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
        List<Class<?>> managedBeanClasses = new ArrayList<Class<?>>();
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

    protected Set<ConfigurationResource> findMetaInfConfigurationResources(EARContext earContext, Module module, Bundle bundle) throws DeploymentException {
        final Set<ConfigurationResource> metaInfConfigurationResources = new HashSet<ConfigurationResource>();
        String moduleNamePrefix = module.isStandAlone() ? "" : module.getTargetPath() + "/";
        //1. jar files in the WEB-INF/lib folder
        ServiceReference reference = null;
        try {
            reference = bundle.getBundleContext().getServiceReference(PackageAdmin.class.getName());
            PackageAdmin packageAdmin = (PackageAdmin) bundle.getBundleContext().getService(reference);
            final String libDirectory = moduleNamePrefix + "WEB-INF/lib";
            BundleResourceFinder resourceFinder = new BundleResourceFinder(packageAdmin, bundle, "META-INF/", "faces-config.xml", new ResourceDiscoveryFilter() {

                @Override
                public boolean directoryDiscoveryRequired(String directoryName) {
                    return false;
                }

                @Override
                public boolean rangeDiscoveryRequired(DiscoveryRange discoveryRange) {
                    return discoveryRange == DiscoveryRange.BUNDLE_CLASSPATH;
                }

                @Override
                public boolean zipFileDiscoveryRequired(String zipFileName) {
                    return zipFileName.startsWith(libDirectory) && zipFileName.endsWith(".jar");
                }

            });
            resourceFinder.find(new ResourceFinderCallback() {

                @Override
                public boolean foundInDirectory(Bundle arg0, String arg1, URL arg2) throws Exception {
                    return false;
                }

                @Override
                public boolean foundInJar(Bundle bundle, String zipFileName, ZipEntry zipEntry, InputStream in) throws Exception {
                    String zipEntryName = zipEntry.getName();
                    if ((zipEntryName.endsWith(".faces-config.xml") && zipEntryName.indexOf('/', "META-INF/".length()) == -1) || zipEntryName.equals("META-INF/faces-config.xml")) {
                        ConfigurationResource configurationResource = new ConfigurationResource(zipFileName, zipEntryName);
                        FacesConfig facesConfig = defaultFacesConfigUnmarshaller.getFacesConfig(in, configurationResource.getConfigurationResourceURL(bundle).toExternalForm());
                        configurationResource.setFacesConfig(facesConfig);
                        metaInfConfigurationResources.add(configurationResource);
                    }
                    return true;
                }
            });
        } catch (Exception e) {
            throw new DeploymentException("Fail to scan faces-config.xml configuration files", e);
        } finally {
            if (reference != null) {
                bundle.getBundleContext().ungetService(reference);
            }
        }
        //2 WEB-INF/classes/META-INF folder
        Enumeration<URL> classesEn = bundle.findEntries(moduleNamePrefix + "WEB-INF/classes/META-INF/", "*faces-config.xml", false);
        if (classesEn != null) {
            while (classesEn.hasMoreElements()) {
                String filePath = classesEn.nextElement().getPath();
                if (filePath.endsWith("/faces-config.xml") || filePath.endsWith(".faces-config.xml")) {
                    metaInfConfigurationResources.add(new ConfigurationResource(null, filePath));
                }
            }
        }
        //3  META-INF folder
        Enumeration<URL> metaInfEn = bundle.findEntries(moduleNamePrefix + "META-INF/", "*faces-config.xml", false);
        if (metaInfEn != null) {
            while (metaInfEn.hasMoreElements()) {
                String filePath = metaInfEn.nextElement().getPath();
                if (filePath.endsWith("/faces-config.xml") || filePath.endsWith(".faces-config.xml")) {
                    metaInfConfigurationResources.add(new ConfigurationResource(null, filePath));
                }
            }
        }
        return metaInfConfigurationResources;
    }

    protected Set<ConfigurationResource> findFaceletConfigResources(EARContext earContext, Module module, Bundle bundle) throws DeploymentException {
        final Set<ConfigurationResource> metaInfConfigurationResources = new HashSet<ConfigurationResource>();
        String moduleNamePrefix = module.isStandAlone() ? "" : module.getTargetPath() + "/";
        //1. jar files in the WEB-INF/lib folder
        ServiceReference reference = null;
        try {
            reference = bundle.getBundleContext().getServiceReference(PackageAdmin.class.getName());
            PackageAdmin packageAdmin = (PackageAdmin) bundle.getBundleContext().getService(reference);
            final String libDirectory = moduleNamePrefix + "WEB-INF/lib";
            BundleResourceFinder resourceFinder = new BundleResourceFinder(packageAdmin, bundle, "META-INF/", ".taglib.xml", new ResourceDiscoveryFilter() {

                @Override
                public boolean directoryDiscoveryRequired(String directoryName) {
                    return false;
                }

                @Override
                public boolean rangeDiscoveryRequired(DiscoveryRange discoveryRange) {
                    return discoveryRange == DiscoveryRange.BUNDLE_CLASSPATH;
                }

                @Override
                public boolean zipFileDiscoveryRequired(String zipFileName) {
                    return zipFileName.startsWith(libDirectory) && zipFileName.endsWith(".jar");
                }

            });
            resourceFinder.find(new ResourceFinderCallback() {

                @Override
                public boolean foundInDirectory(Bundle arg0, String arg1, URL arg2) throws Exception {
                    return false;
                }

                @Override
                public boolean foundInJar(Bundle bundle, String zipFileName, ZipEntry zipEntry, InputStream in) throws Exception {
                    String zipEntryName = zipEntry.getName();
                    if (zipEntryName.endsWith(".taglib.xml") && zipEntryName.indexOf('/', "META-INF/".length()) == -1) {
                        ConfigurationResource configurationResource = new ConfigurationResource(zipFileName, zipEntry.getName());
                        metaInfConfigurationResources.add(configurationResource);
                    }
                    return true;
                }
            });
        } catch (Exception e) {
            throw new DeploymentException("Fail to scan .taglib.xml configuration files", e);
        } finally {
            if (reference != null) {
                bundle.getBundleContext().ungetService(reference);
            }
        }
        //2 WEB-INF/classes/META-INF folder
        Enumeration<URL> classesEn = bundle.findEntries(moduleNamePrefix + "WEB-INF/classes/META-INF/", "*.taglib.xml", false);
        if (classesEn != null) {
            while (classesEn.hasMoreElements()) {
                String filePath = classesEn.nextElement().getPath();
                metaInfConfigurationResources.add(new ConfigurationResource(null, filePath));
            }
        }
        //3  META-INF folder
        Enumeration<URL> metaInfEn = bundle.findEntries(moduleNamePrefix + "META-INF/", "*.taglib.xml", false);
        if (metaInfEn != null) {
            while (metaInfEn.hasMoreElements()) {
                String filePath = metaInfEn.nextElement().getPath();
                metaInfConfigurationResources.add(new ConfigurationResource(null, filePath));
            }
        }
        return metaInfConfigurationResources;
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
