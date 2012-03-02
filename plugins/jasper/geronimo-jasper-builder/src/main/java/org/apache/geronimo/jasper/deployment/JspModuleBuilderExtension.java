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

package org.apache.geronimo.jasper.deployment;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.Deployable;
import org.apache.geronimo.deployment.DeployableBundle;
import org.apache.geronimo.deployment.DeployableJarFile;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
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
import org.apache.geronimo.jasper.JasperServletContextCustomizer;
import org.apache.geronimo.jasper.TldProvider;
import org.apache.geronimo.jasper.TldRegistry;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.util.IOUtils;
import org.apache.geronimo.web.info.ServletInfo;
import org.apache.geronimo.web.info.WebAppInfo;
import org.apache.geronimo.web25.deployment.AbstractWebModuleBuilder;
import org.apache.geronimo.web25.deployment.JspServletInfoProvider;
import org.apache.geronimo.web25.deployment.WebAppInfoBuilder;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.JspConfig;
import org.apache.openejb.jee.JspPropertyGroup;
import org.apache.openejb.jee.Listener;
import org.apache.openejb.jee.Tag;
import org.apache.openejb.jee.Taglib;
import org.apache.openejb.jee.TldTaglib;
import org.apache.openejb.jee.WebApp;
import org.apache.xbean.finder.AbstractFinder;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This JSP module builder extension is meant to find all the TLD descriptor files associated with a
 * deployable artifact, search those TLD files for listeners, search those listeners for
 * annotations, and ultimately create a ClassFinder using those annoated classes (for later
 * processing by the various naming builders)
 *
 * @version $Rev $Date
 */
@GBean(j2eeType = NameFactory.MODULE_BUILDER)
public class JspModuleBuilderExtension implements ModuleBuilderExtension, JspServletInfoProvider {

    private static final Logger log = LoggerFactory.getLogger(JspModuleBuilderExtension.class);

    private final Environment defaultEnvironment;
    private final NamingBuilder namingBuilders;
    private final Set<String> excludedListenerNames = new HashSet<String>();
    private final ServletInfo defaultJspServletInfo;

    public JspModuleBuilderExtension(@ParamAttribute(name = "defaultEnvironment") Environment defaultEnvironment,
                                     @ParamAttribute(name = "excludedListenerNames") Collection<String> excludedListenerNames,
                                     @ParamAttribute(name = "defaultJspServlet") WebAppInfo defaultJspServlet,
                                     @ParamReference(name = "NamingBuilders", namingType = NameFactory.MODULE_BUILDER) NamingBuilder namingBuilders) {
        this.defaultEnvironment = defaultEnvironment;
        this.namingBuilders = namingBuilders;
        if (excludedListenerNames != null) {
            this.excludedListenerNames.addAll(excludedListenerNames);
        }
        if (defaultJspServlet == null || defaultJspServlet.servlets.size() != 1) {
            throw new IllegalArgumentException("Must supply exactly one default jsp servlet");
        }
        defaultJspServletInfo = defaultJspServlet.servlets.get(0);
    }

    public void createModule(Module module, Bundle bundle, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
    }

    public void createModule(Module module, Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment environment, Object moduleContextInfo, AbstractName earName, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
    }

    public void installModule(JarFile earFile, EARContext earContext, Module module, Collection configurationStores, ConfigurationStore targetConfigurationStore, Collection repository) throws DeploymentException {
    }

    public void initContext(EARContext earContext, Module module, Bundle bundle) throws DeploymentException {
    }

    public void addGBeans(EARContext earContext, Module module, Bundle bundle, Collection repository) throws DeploymentException {
        if (!(module instanceof WebModule)) {
            //not a web module, nothing to do
            return;
        }
        //TODO Only merge if we detect jsps???
        EnvironmentBuilder.mergeEnvironments(module.getEnvironment(), defaultEnvironment);

        WebModule webModule = (WebModule) module;
        WebApp webApp = webModule.getSpecDD();

        EARContext moduleContext = module.getEarContext();
        Map sharedContext = module.getSharedContext();

        GBeanData webAppData = (GBeanData) sharedContext.get(WebModule.WEB_APP_DATA);

        AbstractName moduleName = module.getModuleName();
        Map<EARContext.Key, Object> buildingContext = new HashMap<EARContext.Key, Object>();
        buildingContext.put(NamingBuilder.GBEAN_NAME_KEY, moduleName);


        //use the same holder object as the web app.
        Holder holder = NamingBuilder.INJECTION_KEY.get(sharedContext);
        buildingContext.put(NamingBuilder.INJECTION_KEY, holder);

        XmlObject jettyWebApp = webModule.getVendorDD();

        Set<String> listenerNames = new HashSet<String>();

        Map<String, Bundle> tldLocationBundleMap = getTldFiles(webApp, webModule);
        LinkedHashSet<Class<?>> classes = getListenerClasses(webApp, webModule, tldLocationBundleMap, listenerNames);
        
        AbstractFinder originalClassFinder = webModule.getClassFinder();
        ClassFinder classFinder = new ClassFinder(new ArrayList<Class<?>>(classes));
        webModule.setClassFinder(classFinder);
        namingBuilders.buildNaming(webApp, jettyWebApp, webModule, buildingContext);
        webModule.setClassFinder(originalClassFinder);

        //only try to install it if reference will work.
        //Some users (tomcat?) may have back doors into jasper that make adding this gbean unnecessary.
        GBeanInfo webAppGBeanInfo = webAppData.getGBeanInfo();
        if (webAppGBeanInfo.getReference("ContextCustomizer") != null) {
            AbstractName jspLifecycleName = moduleContext.getNaming().createChildName(moduleName, "jspLifecycleProvider", GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
            GBeanData gbeanData = new GBeanData(jspLifecycleName, JasperServletContextCustomizer.class);
            gbeanData.setAttribute("holder", holder);

            try {
                moduleContext.addGBean(gbeanData);
            } catch (GBeanAlreadyExistsException e) {
                throw new DeploymentException("Duplicate jspLifecycleProvider", e);
            }

            webAppData.setReferencePattern("ContextCustomizer", jspLifecycleName);
        }

        WebAppInfoBuilder webAppInfoBuilder = (WebAppInfoBuilder)sharedContext.get(WebModule.WEB_APP_INFO);
        if (webAppInfoBuilder != null) {
            WebAppInfo webAppInfo = webAppInfoBuilder.getWebAppInfo();
            webAppInfo.listeners.addAll(listenerNames);
            //install default jsp servlet....
            ServletInfo jspServlet = webAppInfoBuilder.copy(defaultJspServletInfo);
            List<JspConfig> jspConfigs = webApp.getJspConfig();
            List<String> jspMappings = new ArrayList<String>();
            for (JspConfig jspConfig : jspConfigs) {
                for (JspPropertyGroup propertyGroup : jspConfig.getJspPropertyGroup()) {
                    WebAppInfoBuilder.normalizeUrlPatterns(propertyGroup.getUrlPattern(), jspMappings);
                }
            }

            jspServlet.servletMappings.addAll(jspMappings);
            for (ServletInfo servletInfo: webAppInfo.servlets) {
                servletInfo.servletMappings.removeAll(jspMappings);
            }
            webAppInfo.servlets.add(jspServlet);
        } else {
            GBeanData jspServletData = AbstractWebModuleBuilder.DEFAULT_JSP_SERVLET_KEY.get(sharedContext);
            if (jspServletData != null) {
                try {
                    moduleContext.addGBean(jspServletData);
                } catch (GBeanAlreadyExistsException e) {
                    throw new DeploymentException("jsp servlet already present", e);
                }
            }
            // add listeners
            Object value = webAppData.getAttribute("listenerClassNames");
            if (value instanceof Collection) {
                ((Collection<String>) value).addAll(listenerNames);
            }
        }
    }

    @Override
    public ServletInfo getJspServletInfo() {
        return defaultJspServletInfo;
    }

    /**
     * getTldFiles(): Find all the TLD files in the web module being deployed
     * <p/>
     * <p>Locations to search for these TLD file(s) (matches the precedence search order for TLD
     * files per the JSP specs):
     * <ol>
     * <li>web.xml <taglib> entries
     * <li>TLD(s) in JAR files in WEB-INF/lib
     * <li>TLD(s) under WEB-INF
     * <li>All TLD files in all META-INF(s)
     * </ol>
     *
     * @param webApp    spec DD for module
     * @param webModule module being deployed
     * @return list of the URL(s) for the TLD files
     * @throws DeploymentException if there's a problem finding a tld file
     */
    private Map<String, Bundle> getTldFiles(WebApp webApp, WebModule webModule) throws DeploymentException {
        if (log.isDebugEnabled()) {
            log.debug("getTldFiles( " + webApp.toString() + "," + webModule.getName() + " ): Entry");
        }

        Map<String, Bundle> tldLocationBundleMap = new LinkedHashMap<String, Bundle>();
        Bundle webBundle = webModule.getEarContext().getDeploymentBundle();

        // 1. web.xml <taglib> entries
        List<JspConfig> jspConfigs = webApp.getJspConfig();
        for (JspConfig jspConfig : jspConfigs) {
            List<Taglib> taglibs = jspConfig.getTaglib();
            for (Taglib taglib : taglibs) {
                String uri = taglib.getTaglibUri().trim();
                String location = taglib.getTaglibLocation().trim();
                if (!location.equals("")) {
                    if (location.startsWith("/")) {
                        location = location.substring(1);
                    }
                    try {
                        URL targetUrl = webModule.getEarContext().getTargetURL(webModule.resolve(createURI(location)));
                        if (targetUrl != null) {
                            tldLocationBundleMap.put(targetUrl.toString(), webBundle);
                        }
                    } catch (URISyntaxException use) {
                        throw new DeploymentException("Could not locate TLD file specified in <taglib>: URI: " + uri + " Location: " + location + " " + use.getMessage(), use);
                    }
                }
            }
        }

        // 2. TLD(s) in JAR files in WEB-INF/lib
        // 3. TLD(s) under WEB-INF
        for (URL tldURL : scanModule(webModule)) {
            tldLocationBundleMap.put(tldURL.toString(), webBundle);
        }

        // 4. All TLD files in all META-INF(s)
        tldLocationBundleMap.putAll(scanGlobalTlds(webModule.getEarContext().getDeploymentBundle()));
        if (log.isDebugEnabled()) {
            log.debug("getTldFiles() Exit: URL[" + tldLocationBundleMap.size() + "]: " + tldLocationBundleMap.toString());
        }
        return tldLocationBundleMap;
    }

    /**
     * scanModule(): Scan the module being deployed for JAR files or TLD files in the WEB-INF
     * directory
     *
     * @param webModule module being deployed
     * @return list of the URL(s) for the TLD files in the module
     * @throws DeploymentException if module cannot be scanned
     */
    private List<URL> scanModule(WebModule webModule) throws DeploymentException {
        Deployable deployable = webModule.getDeployable();
        if (deployable instanceof DeployableJarFile) {
            JarFileTldScanner scanner = new JarFileTldScanner();
            return scanner.scanModule(webModule);
        } else if (deployable instanceof DeployableBundle) {
            BundleTldScanner scanner = new BundleTldScanner();
            return scanner.scanModule(webModule);
        }
        return Collections.emptyList();
    }

    private Map<String, Bundle> scanGlobalTlds(Bundle bundle) throws DeploymentException {
        BundleContext bundleContext = bundle.getBundleContext();
        ServiceReference reference = bundleContext.getServiceReference(TldRegistry.class.getName());
        Map<String, Bundle> tldLocationBundleMap = new HashMap<String, Bundle>();
        if (reference != null) {
            TldRegistry tldRegistry = (TldRegistry) bundleContext.getService(reference);
            for (TldProvider.TldEntry entry : tldRegistry.getDependentTlds(bundle)) {
                URL url = entry.getURL();
                tldLocationBundleMap.put(url.toString(), entry.getBundle());
            }
            bundleContext.ungetService(reference);
        }
        return tldLocationBundleMap;
    }

    private LinkedHashSet<Class<?>> getListenerClasses(WebApp webApp, WebModule webModule, Map<String, Bundle> tldLocationBundleMap, Set<String> listenerNames) throws DeploymentException {
        if (log.isDebugEnabled()) {
            log.debug("getListenerClasses( " + webApp.toString() + "," + '\n' +
                    webModule.getName() + " ): Entry");
        }

        // Get the classloader from the module's EARContext
        LinkedHashSet<Class<?>> classes = new LinkedHashSet<Class<?>>();

        try {
            for (Map.Entry<String, Bundle> entry : tldLocationBundleMap.entrySet()) {
                parseTldFile(new URL(entry.getKey()), entry.getValue(), classes, listenerNames);
            }
        } catch (MalformedURLException e) {
            throw new DeploymentException("Fail to parse the tld files", e);
        }

        if (log.isDebugEnabled()) {
            log.debug("getListenerClasses() Exit: Classes[" + classes.size() + "]: " + classes.toString());
        }
        return classes;
    }

    protected void parseTldFile(URL url, Bundle bundle, LinkedHashSet<Class<?>> classes, Set<String> listenerNames) throws DeploymentException {
        if (log.isDebugEnabled()) {
            log.debug("parseTLDFile( " + url.toString() + " ): Entry");
        }
        try {
            InputStream in = null;
            TldTaglib tl;
            try {
                in = url.openStream();
                tl = (TldTaglib) JaxbJavaee.unmarshalTaglib(TldTaglib.class, in);
            } finally {
                IOUtils.close(in);
            }

            // Get all the listeners from the TLD file
            for (Listener listener : tl.getListener()) {
                String className = listener.getListenerClass();
                if (!excludedListenerNames.contains(className)) {
                    try {
                        Class<?> clas = bundle.loadClass(className);
                        while (clas != null) {
                            classes.add(clas);
                            clas = clas.getSuperclass();
                        }
                        listenerNames.add(className);
                    } catch (ClassNotFoundException e) {
                        log.warn("JspModuleBuilderExtension: Could not load listener class: " + className + " mentioned in TLD file at " + url.toString());
                    }
                }
            }

            // Get all the tags from the TLD file
            for (Tag tag : tl.getTag()) {
                String className = tag.getTagClass();
                try {
                    Class<?> clas = bundle.loadClass(className);
                    while (clas != null) {
                        classes.add(clas);
                        clas = clas.getSuperclass();
                    }
                } catch (ClassNotFoundException e) {
                    log.warn("JspModuleBuilderExtension: Could not load tag class: " + className + " mentioned in TLD file at " + url.toString());
                }
            }
        } catch (Exception ioe) {
            throw new DeploymentException("Could not find TLD file at " + url.toString(), ioe);
        }
        if (log.isDebugEnabled()) {
            log.debug("parseTLDFile(): Exit");
        }
    }

    private URI createURI(String path) throws URISyntaxException {
        path = path.replaceAll(" ", "%20");
        return new URI(path);
    }
}
