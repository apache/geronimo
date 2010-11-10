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

import javax.faces.bean.ManagedBean;
import javax.faces.component.FacesComponent;
import javax.faces.component.behavior.FacesBehavior;
import javax.faces.convert.FacesConverter;
import javax.faces.event.NamedEvent;
import javax.faces.render.FacesBehaviorRenderer;
import javax.faces.render.FacesRenderer;
import javax.faces.validator.FacesValidator;
import javax.faces.webapp.FacesServlet;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

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
import org.apache.geronimo.myfaces.LifecycleProviderGBean;
import org.apache.geronimo.myfaces.config.resource.ConfigurationResource;
import org.apache.geronimo.myfaces.webapp.GeronimoStartupServletContextListener;
import org.apache.geronimo.myfaces.webapp.MyFacesWebAppContext;
import org.apache.geronimo.web.info.WebAppInfo;
import org.apache.openejb.jee.FacesConfig;
import org.apache.openejb.jee.FacesManagedBean;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.ParamValue;
import org.apache.openejb.jee.Servlet;
import org.apache.openejb.jee.WebApp;
import org.apache.xbean.finder.BundleAnnotationFinder;
import org.apache.xbean.finder.ClassFinder;
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

    public static final EARContext.Key<Set<ConfigurationResource>> JSF_META_INF_CONFIGURATION_RESOURCES = new EARContext.Key<Set<ConfigurationResource>>() {

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

        FacesConfig defaultWebAppFacesConfig = getDefaultWebAppFacesConfig(webModule);
        Set<ConfigurationResource> metaInfConfigurationResources = JSF_META_INF_CONFIGURATION_RESOURCES.get(earContext.getGeneralData());
        Map<Class<? extends Annotation>, Set<Class>> annotationClassSetMap = null;
        if (!defaultWebAppFacesConfig.isMetadataComplete()) {
            annotationClassSetMap = scanJSFAnnotations(earContext, webModule, bundle, metaInfConfigurationResources);
        }


        AbstractName myFacesWebAppContextName = moduleContext.getNaming().createChildName(moduleName, "myFacesWebAppContext", "MyFacesWebAppContext");
        GBeanData myFacesWebAppContextData = new GBeanData(myFacesWebAppContextName, MyFacesWebAppContext.class);
        myFacesWebAppContextData.setAttribute("annotationClassSetMap", annotationClassSetMap);
        myFacesWebAppContextData.setAttribute("metaInfConfigurationResources", metaInfConfigurationResources);

        List<FacesConfig> facesConfigs = new ArrayList<FacesConfig>();
        facesConfigs.add(defaultWebAppFacesConfig);
        facesConfigs.addAll(getContextFacesConfigs(webApp, webModule));
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
            facesConfigs.add(parseConfigFile(url, bundle));
        }

        ClassFinder classFinder = createMyFacesClassFinder(facesConfigs, annotationClassSetMap != null ? annotationClassSetMap.get(ManagedBean.class) : null, bundle);
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

    protected Map<Class<? extends Annotation>, Set<Class>> scanJSFAnnotations(EARContext earContext, Module module, Bundle bundle, Set<ConfigurationResource> metaInfConfigurationResources) throws DeploymentException {
        ServiceReference reference = bundle.getBundleContext().getServiceReference(PackageAdmin.class.getName());
        try {
            PackageAdmin packageAdmin = (PackageAdmin) bundle.getBundleContext().getService(reference);
            final Set<String> requiredJarFiles = new HashSet<String>();
            for (ConfigurationResource configurationResource : metaInfConfigurationResources) {
                if (configurationResource.getJarFilePath() != null) {
                    requiredJarFiles.add(configurationResource.getJarFilePath());
                }
            }
            Map<Class<? extends Annotation>, Set<Class>> annotationClassSetMap = new HashMap<Class<? extends Annotation>, Set<Class>>();
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
            annotationClassSetMap.put(FacesComponent.class, new HashSet<Class>(bundleAnnotationFinder.findAnnotatedClasses(FacesComponent.class)));
            annotationClassSetMap.put(FacesConverter.class, new HashSet<Class>(bundleAnnotationFinder.findAnnotatedClasses(FacesConverter.class)));
            annotationClassSetMap.put(FacesValidator.class, new HashSet<Class>(bundleAnnotationFinder.findAnnotatedClasses(FacesValidator.class)));
            annotationClassSetMap.put(FacesRenderer.class, new HashSet<Class>(bundleAnnotationFinder.findAnnotatedClasses(FacesRenderer.class)));
            annotationClassSetMap.put(ManagedBean.class, new HashSet<Class>(bundleAnnotationFinder.findAnnotatedClasses(ManagedBean.class)));
            annotationClassSetMap.put(NamedEvent.class, new HashSet<Class>(bundleAnnotationFinder.findAnnotatedClasses(NamedEvent.class)));
            annotationClassSetMap.put(FacesBehavior.class, new HashSet<Class>(bundleAnnotationFinder.findAnnotatedClasses(FacesBehavior.class)));
            annotationClassSetMap.put(FacesBehaviorRenderer.class, new HashSet<Class>(bundleAnnotationFinder.findAnnotatedClasses(FacesBehaviorRenderer.class)));
            return annotationClassSetMap;
        } catch (Exception e) {
            throw new DeploymentException("Fail to scan JSF annotations", e);
        } finally {
            bundle.getBundleContext().ungetService(reference);
        }
    }

    protected FacesConfig getDefaultWebAppFacesConfig(WebModule webModule) throws DeploymentException {
        URL url = webModule.getDeployable().getResource("WEB-INF/faces-config.xml");
        if (url != null) {
            Bundle bundle = webModule.getEarContext().getDeploymentBundle();
            return parseConfigFile(url, bundle);
        } else {
            return new FacesConfig();
        }
    }

    protected List<FacesConfig> getContextFacesConfigs(WebApp webApp, WebModule webModule) throws DeploymentException {
        for (ParamValue paramValue : webApp.getContextParam()) {
            if (paramValue.getParamName().trim().equals(FacesServlet.CONFIG_FILES_ATTR)) {
                List<FacesConfig> contextFacesConfigs = new ArrayList<FacesConfig>();
                String configFiles = paramValue.getParamValue().trim();
                StringTokenizer st = new StringTokenizer(configFiles, ",", false);
                Bundle bundle = webModule.getEarContext().getDeploymentBundle();
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
                            contextFacesConfigs.add(parseConfigFile(url, bundle));
                        }
                    }
                }
                return contextFacesConfigs;
            }
        }
        return Collections.<FacesConfig> emptyList();
    }

    protected ClassFinder createMyFacesClassFinder(List<FacesConfig> facesConfigs, Set<Class> annotatedManagedBeanClasses, Bundle bundle) throws DeploymentException {
        List<Class> managedBeanClasses = new ArrayList<Class>();
        for (FacesConfig facesConfig : facesConfigs) {
            for (FacesManagedBean managedBean : facesConfig.getManagedBean()) {
                String className = managedBean.getManagedBeanClass().trim();
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
        if (annotatedManagedBeanClasses != null) {
            for (Class<?> clas : annotatedManagedBeanClasses) {
                while (clas != null) {
                    managedBeanClasses.add(clas);
                    clas = clas.getSuperclass();
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
                            //TODO Should find a way to avoid the file copying
                            String destination = "META-INF/WEB-INF_lib_" + file.getName() + "/" + name;
                            earContext.addFile(module.resolve(destination), jarFile, zipEntry);
                            metaInfConfigurationResources.add(new ConfigurationResource("WEB-INF/lib/" + file.getName(), name, destination));
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
                String fileName = file.getName();
                if (fileName.equals("faces-config.xml") || fileName.endsWith(".faces-config.xml")) {
                    //TODO Double check the relative jar file path once EAR is really supported
                    String filePath = "WEB-INF/classes/META-INF/" + fileName;
                    metaInfConfigurationResources.add(new ConfigurationResource(null, filePath, filePath));
                }
            }
        }
        //3. META-INF folder
        File baseDirectory = new File(earContext.getBaseDir() + File.separator + "META-INF");
        if (baseDirectory.exists() && baseDirectory.isDirectory()) {
            for (File file : baseDirectory.listFiles()) {
                String fileName = file.getName();
                if (fileName.equals("faces-config.xml") || fileName.endsWith(".faces-config.xml")) {
                    //TODO Double check the relative jar file path once EAR is really supported
                    String filePath = "META-INF/" + fileName;
                    metaInfConfigurationResources.add(new ConfigurationResource(null, filePath, filePath));
                }
            }
        }
        //default WEB-INF/faces-config.xml is handled by myfaces no matter what we do???
//        //4. WEB-INF folder
//        baseDirectory = new File(earContext.getBaseDir() + File.separator + "WEB-INF");
//        if (baseDirectory.exists() && baseDirectory.isDirectory()) {
//            for (File file : baseDirectory.listFiles()) {
//                String fileName = file.getName();
//                if (fileName.equals("faces-config.xml") || fileName.endsWith(".faces-config.xml")) {
//                    //TODO Double check the relative jar file path once EAR is really supported
//                    String filePath = "WEB-INF/" + fileName;
//                    metaInfConfigurationResources.add(new ConfigurationResource(null, filePath, filePath));
//                }
//            }
//        }
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

    private FacesConfig parseConfigFile(URL url, Bundle bundle) throws DeploymentException {
        log.debug("parseConfigFile( " + url.toString() + " ): Entry");
        InputStream in = null;
        try {
            in = url.openStream();
            return (FacesConfig) JaxbJavaee.unmarshalJavaee(FacesConfig.class, in);
        } catch (ParserConfigurationException e) {
            throw new DeploymentException("Could not parse alleged faces-config.xml at " + url.toString(), e);
        } catch (SAXException e) {
            throw new DeploymentException("Could not parse alleged faces-config.xml at " + url.toString(), e);
        } catch (JAXBException e) {
            throw new DeploymentException("Could not parse alleged faces-config.xml at " + url.toString(), e);
        } catch (IOException ioe) {
            throw new DeploymentException("Error reading jsf configuration file " + url, ioe);
        } finally {
            IOUtils.close(in);
        }
    }
}
