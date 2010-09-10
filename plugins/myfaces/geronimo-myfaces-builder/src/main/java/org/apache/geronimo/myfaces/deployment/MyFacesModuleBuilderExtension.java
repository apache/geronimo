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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.faces.webapp.FacesServlet;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.Deployable;
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
import org.apache.geronimo.myfaces.LifecycleProviderGBean;
import org.apache.geronimo.web.info.WebAppInfo;
import org.apache.myfaces.webapp.StartupServletContextListener;
import org.apache.openejb.jee.FacesConfig;
import org.apache.openejb.jee.FacesManagedBean;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.ParamValue;
import org.apache.openejb.jee.Servlet;
import org.apache.openejb.jee.WebApp;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.Bundle;
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

    private static final String CONTEXT_LISTENER_NAME = StartupServletContextListener.class.getName();

    private static final String FACES_SERVLET_NAME = FacesServlet.class.getName();

    private static final String SCHEMA_LOCATION_URL = "http://java.sun.com/xml/ns/javaee/web-facesconfig_2_0.xsd";

    private static final String VERSION = "2.0";

    private static final String JAVAX_FACES_CONFIG_FILES = "javax.faces.CONFIG_FILES";

    private static final String JAVAX_FACES_CONFIG_FILES_SEPARATOR = ",";

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
        //For MyFaces uses common Jar Strategy to find the config files, while it is impossible in OSGi environment.
        //Current workaround is to copy all the files in the lib/*.jar files to a temp folder, and add those files to the context parameter in web.xml file
        WebModule webModule = (WebModule) module;
        WebApp webApp = webModule.getSpecDD();
        ParamValue configFilesPV = null;
        for (ParamValue pv : webApp.getContextParam()) {
            if (JAVAX_FACES_CONFIG_FILES.equals(pv.getParamName().trim())) {
                configFilesPV = pv;
                break;
            }
        }
        StringBuilder configFiles = new StringBuilder(configFilesPV != null && configFilesPV.getParamValue() != null ? configFilesPV.getParamValue() : "");
        File libDirectory = new File(earContext.getBaseDir() + File.separator + "WEB-INF" + File.separator + "lib");
        if (!libDirectory.exists()) {
            return;
        }
        for (File jarFile : libDirectory.listFiles()) {
            if (!jarFile.getName().endsWith(".jar")) {
                continue;
            }

            JarFile zipFile;
            try {
                zipFile = new JarFile(jarFile);
            } catch (IOException e) {
                //Just ignore
                continue;
            }

            ZipInputStream in = null;
            try {
                in = new ZipInputStream(new FileInputStream(jarFile));
                ZipEntry zipEntry;

                while ((zipEntry = in.getNextEntry()) != null) {
                    String name = zipEntry.getName();

                    // Scan config files named as faces-config.xml or *.faces-config.xml under META-INF
                    if (name.equals("META-INF/faces-cofig.xml") || (name.startsWith("META-INF/") && name.endsWith(".faces-config.xml"))) {

                        // copy config file
                        String destination = "temp/" + jarFile.getName() + ".tempdir" + "/" + name;
                        earContext.addFile(module.resolve(destination), zipFile, zipEntry);

                        // add new location of config file name to configFiles
                        if (configFiles.length() > 0) {
                            configFiles.append(JAVAX_FACES_CONFIG_FILES_SEPARATOR);
                        }
                        configFiles.append("/").append(destination);
                    }
                }
            } catch (Exception e) {
                throw new DeploymentException("Can not preprocess myfaces application configuration resources", e);
            } finally {
                IOUtils.close(in);
            }
        }

        // write configFiles back to web.xml
        if (configFilesPV == null && configFiles.length() > 0) {
            configFilesPV = new ParamValue();
            configFilesPV.setParamName(JAVAX_FACES_CONFIG_FILES);
            webApp.getContextParam().add(configFilesPV);
        }
        if (configFilesPV != null && configFiles.length() > 0) {
            configFilesPV.setParamValue(configFiles.toString());
        }
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

        ClassFinder classFinder = createMyFacesClassFinder(webApp, webModule);
        webModule.setClassFinder(classFinder);

        namingBuilders.buildNaming(webApp, jettyWebApp, webModule, buildingContext);

        AbstractName providerName = moduleContext.getNaming().createChildName(moduleName, "jsf-lifecycle", "jsf");
        GBeanData providerData = new GBeanData(providerName, LifecycleProviderGBean.class);
        providerData.setAttribute("holder", holder);
        providerData.setReferencePatterns("ContextSource", webAppData.getReferencePatterns("ContextSource"));
        providerData.setReferencePattern("LifecycleProviderFactory", providerFactoryNameQuery);
        try {
            moduleContext.addGBean(providerData);
        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException("Duplicate jsf config gbean in web module", e);
        }

        //make the web app start second after the injection machinery
        webAppData.addDependency(providerName);

    }

    private boolean hasFacesServlet(WebApp webApp) {
        for (Servlet servlet : webApp.getServlet()) {
            if (servlet.getServletClass() != null && FACES_SERVLET_NAME.equals(servlet.getServletClass().trim())) {
                return true;
            }
        }
        return false;
    }

    protected ClassFinder createMyFacesClassFinder(WebApp webApp, WebModule webModule) throws DeploymentException {

        LinkedHashSet<Class> classes = getFacesClasses(webApp, webModule);
        return new ClassFinder(new ArrayList<Class>(classes));
    }

    /**
     * getFacesConfigFileURL()
     * <p/>
     * <p>Locations to search for the MyFaces configuration file(s):
     * <ol>
     * <li>META-INF/faces-config.xml
     * <li>WEB-INF/faces-config.xml
     * <li>javax.faces.CONFIG_FILES -- Context initialization param of Comma separated
     * list of URIs of (additional) faces config files
     * </ol>
     * <p/>
     * <p><strong>Notes:</strong>
     * <ul>
     * </ul>
     *
     * @param webApp    spec DD for module
     * @param webModule module being deployed
     * @return list of all managed bean classes from all faces-config xml files.
     * @throws org.apache.geronimo.common.DeploymentException
     *          if a faces-config.xml file is located but cannot be parsed.
     */
    private LinkedHashSet<Class> getFacesClasses(WebApp webApp, WebModule webModule) throws DeploymentException {
        log.debug("getFacesClasses( " + webApp.toString() + "," + '\n' + (webModule != null ? webModule.getName() : null) + " ): Entry");

        Deployable deployable = webModule.getDeployable();
        Bundle bundle = webModule.getEarContext().getDeploymentBundle();

        // 1. META-INF/faces-config.xml
        LinkedHashSet<Class> classes = new LinkedHashSet<Class>();
        URL url = deployable.getResource("META-INF/faces-config.xml");
        if (url != null) {
            parseConfigFile(url, bundle, classes);
        }

        // 2. WEB-INF/faces-config.xml
        url = deployable.getResource("WEB-INF/faces-config.xml");
        if (url != null) {
            parseConfigFile(url, bundle, classes);
        }

        // 3. javax.faces.CONFIG_FILES
        List<ParamValue> paramValues = webApp.getContextParam();
        for (ParamValue paramValue : paramValues) {
            if (paramValue.getParamName().trim().equals("javax.faces.CONFIG_FILES")) {
                String configFiles = paramValue.getParamValue().trim();
                StringTokenizer st = new StringTokenizer(configFiles, ",", false);
                while (st.hasMoreTokens()) {
                    String configfile = st.nextToken().trim();
                    if (!configfile.equals("")) {
                        if (configfile.startsWith("/")) {
                            configfile = configfile.substring(1);
                        }
                        url = webModule.getEarContext().getTargetURL(webModule.resolve(configfile));
                        if (url == null) {
                            throw new DeploymentException("Could not locate config file " + configfile);
                        } else {
                            parseConfigFile(url, bundle, classes);
                        }
                    }
                }
                break;
            }
        }

        log.debug("getFacesClasses() Exit: " + classes.size() + " " + classes.toString());
        return classes;
    }

    private void parseConfigFile(URL url, Bundle bundle, LinkedHashSet<Class> classes) throws DeploymentException {
        log.debug("parseConfigFile( " + url.toString() + " ): Entry");

        try {
            InputStream in = url.openStream();
            FacesConfig facesConfig = null;
            try {
                facesConfig = (FacesConfig) JaxbJavaee.unmarshalJavaee(FacesConfig.class, in);
            } finally {
                in.close();
            }

            // Get all the managed beans from the faces configuration file
            List<FacesManagedBean> managedBeans = facesConfig.getManagedBean();
            for (FacesManagedBean managedBean : managedBeans) {
                String className = managedBean.getManagedBeanClass().trim();
                Class<?> clas;
                try {
                    clas = bundle.loadClass(className);
                    while (clas != null) {
                        classes.add(clas);
                        clas = clas.getSuperclass();
                    }
                } catch (ClassNotFoundException e) {
                    log.warn("MyFacesModuleBuilderExtension: Could not load managed bean class: " + className + " mentioned in faces-config.xml file at " + url.toString());
                }
            }
        } catch (ParserConfigurationException e) {
            throw new DeploymentException("Could not parse alleged faces-config.xml at " + url.toString(), e);

        } catch (SAXException e) {
            throw new DeploymentException("Could not parse alleged faces-config.xml at " + url.toString(), e);

        } catch (JAXBException e) {
            throw new DeploymentException("Could not parse alleged faces-config.xml at " + url.toString(), e);

        } catch (IOException ioe) {
            throw new DeploymentException("Error reading jsf configuration file " + url, ioe);
        }

        log.debug("parseConfigFile(): Exit");
    }

}
