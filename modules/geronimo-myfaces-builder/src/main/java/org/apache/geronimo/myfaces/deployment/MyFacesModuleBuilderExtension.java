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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.jar.JarFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.annotation.Holder;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilderExtension;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.deployment.WebModule;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.myfaces.LifecycleProviderGBean;
import org.apache.geronimo.xbeans.javaee.FacesConfigDocument;
import org.apache.geronimo.xbeans.javaee.FacesConfigManagedBeanType;
import org.apache.geronimo.xbeans.javaee.FacesConfigType;
import org.apache.geronimo.xbeans.javaee.FullyQualifiedClassType;
import org.apache.geronimo.xbeans.javaee.ParamValueType;
import org.apache.geronimo.xbeans.javaee.WebAppType;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev $Date
 */
public class MyFacesModuleBuilderExtension implements ModuleBuilderExtension {

    private static final Log log = LogFactory.getLog(MyFacesModuleBuilderExtension.class);

    private final Environment defaultEnvironment;
    private final AbstractNameQuery providerFactoryNameQuery;
    private final NamingBuilder namingBuilders;


    public MyFacesModuleBuilderExtension(Environment defaultEnvironment, AbstractNameQuery providerFactoryNameQuery, NamingBuilder namingBuilders) {
        this.defaultEnvironment = defaultEnvironment;
        this.providerFactoryNameQuery = providerFactoryNameQuery;
        this.namingBuilders = namingBuilders;
    }

    public void createModule(Module module, File plan, JarFile moduleFile, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
    }

    public void createModule(Module module, Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment environment, Object moduleContextInfo, AbstractName earName, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
    }

    public void installModule(JarFile earFile, EARContext earContext, Module module, Collection configurationStores, ConfigurationStore targetConfigurationStore, Collection repository) throws DeploymentException {
    }

    public void initContext(EARContext earContext, Module module, ClassLoader cl) throws DeploymentException {
    }

    public void addGBeans(EARContext earContext, Module module, ClassLoader cl, Collection repository) throws DeploymentException {
        if (!(module instanceof WebModule)) {
            //not a web module, nothing to do
            return;
        }
        WebModule webModule = (WebModule) module;
        WebAppType webApp = (WebAppType) webModule.getSpecDD();
        XmlObject jettyWebApp = webModule.getVendorDD();
        ClassFinder classFinder = createMyFacesClassFinder(webApp, webModule); 
        if (classFinder == null) {
            //no jsf config found, nothing to do
            return;
        }

        EARContext moduleContext = module.getEarContext();
        EnvironmentBuilder.mergeEnvironments(moduleContext.getConfiguration().getEnvironment(), defaultEnvironment);

        AbstractName moduleName = moduleContext.getModuleName();
        Map<NamingBuilder.Key, Object> buildingContext = new HashMap<NamingBuilder.Key, Object>();
        buildingContext.put(NamingBuilder.GBEAN_NAME_KEY, moduleName);
        Configuration earConfiguration = earContext.getConfiguration();

        webModule.setClassFinder(classFinder);

        namingBuilders.buildNaming(webApp, jettyWebApp, earConfiguration, earConfiguration, webModule, buildingContext);

        Map compContext = NamingBuilder.JNDI_KEY.get(buildingContext);
        Holder holder = NamingBuilder.INJECTION_KEY.get(buildingContext);

        AbstractName providerName = moduleContext.getNaming().createChildName(moduleName, "jsf-lifecycle", "jsf");
        GBeanData providerData = new GBeanData(providerName, LifecycleProviderGBean.GBEAN_INFO);
        providerData.setAttribute("holder", holder);
        providerData.setAttribute("context", compContext);
        providerData.setReferencePattern("LifecycleProviderFactory", providerFactoryNameQuery);
        try {
            moduleContext.addGBean(providerData);
        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException("Duplicate jsf config gbean in web module", e);
        }

    }


    protected ClassFinder createMyFacesClassFinder(WebAppType webApp, WebModule webModule) throws DeploymentException {

        List<Class> classes = getFacesClasses(webApp, webModule);
        if (classes.isEmpty()) {
            return null;
        } else {
            return new ClassFinder(classes);
        }
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
     * @throws org.apache.geronimo.common.DeploymentException if a faces-config.xml file is located but cannot be parsed.
     */
    private List<Class> getFacesClasses(WebAppType webApp, WebModule webModule) throws DeploymentException {
        log.debug("GetConfigFileURL() Entry");
        // Get the classloader from the module's EARContext
        ClassLoader classLoader = webModule.getEarContext().getClassLoader();

        // 1. META-INF/faces-config.xml
        List<Class> classes = new ArrayList<Class>();
        try {
            URL url = DeploymentUtil.createJarURL(webModule.getModuleFile(), "META-INF/faces-config.xml");
            parseConfigFile(url, classLoader, classes);
        } catch (MalformedURLException mfe) {
            throw new DeploymentException("Could not locate META-INF/faces-config.xml" + mfe.getMessage());
        }

        // 2. WEB-INF/faces-config.xml
        try {
            URL url = DeploymentUtil.createJarURL(webModule.getModuleFile(), "WEB-INF/faces-config.xml");
            parseConfigFile(url, classLoader, classes);
        } catch (MalformedURLException mfe) {
            throw new DeploymentException("Could not locate WEB-INF/faces-config.xml" + mfe.getMessage());
        }

        // 3. javax.faces.CONFIG_FILES
        ParamValueType[] paramValues = webApp.getContextParamArray();
        for (ParamValueType paramValue : paramValues) {
            if (paramValue.getParamName().getStringValue().trim().equals("javax.faces.CONFIG_FILES")) {
                String configFiles = paramValue.getParamValue().getStringValue().trim();
                StringTokenizer st = new StringTokenizer(configFiles, ",", false);
                while (st.hasMoreTokens()) {
                    String configfile = st.nextToken().trim();
                    if (!configfile.equals("")) {
                        if (configfile.startsWith("/")) {
                            configfile = configfile.substring(1);
                        }
                        try {
                            URL url = DeploymentUtil.createJarURL(webModule.getModuleFile(), configfile);
                            parseConfigFile(url, classLoader, classes);
                        } catch (MalformedURLException mfe) {
                            throw new DeploymentException("Could not locate config file " + configfile + ", " + mfe.getMessage());
                        }
                    }
                }
                break;
            }
        }

        log.debug("GetConfigFileURL() Exit: " + classes.size() + " " + classes.toString());
        return classes;
    }

    // TODO: Handle JSF 1.1 config files with DTD schema
    private void parseConfigFile(URL url, ClassLoader classLoader, List<Class> classes) throws DeploymentException {
        try {
            XmlObject xml = XmlBeansUtil.parse(url, null);
            FacesConfigDocument fcd = (FacesConfigDocument) XmlBeansUtil.typedCopy(xml, FacesConfigDocument.type);
            FacesConfigType facesConfig = fcd.getFacesConfig();

            // Get all the managed beans from the faces configuration file
            FacesConfigManagedBeanType[] managedBeans = facesConfig.getManagedBeanArray();
            for (FacesConfigManagedBeanType managedBean : managedBeans) {
                FullyQualifiedClassType cls = managedBean.getManagedBeanClass();
                Class<?> clas;
                try {
                    clas = classLoader.loadClass(cls.getStringValue());
                }
                catch (ClassNotFoundException e) {
                    throw new DeploymentException("MyFacesModuleBuilderExtension: Could not load managed bean class: " + cls.getStringValue());
                }
                classes.add(clas);
            }
        }
        catch (XmlException xmle) {
            throw new DeploymentException("Could not parse alleged faces-config.xml at " + url.toString(), xmle);
        }
        catch (IOException ioe) {
            //config file does not exist
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(MyFacesModuleBuilderExtension.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true, true);
        infoBuilder.addAttribute("providerFactoryNameQuery", AbstractNameQuery.class, true, true);
        infoBuilder.addReference("NamingBuilders", NamingBuilder.class, NameFactory.MODULE_BUILDER);

        infoBuilder.setConstructor(new String[]{
                "defaultEnvironment",
                "providerFactoryNameQuery",
                "NamingBuilders"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }


}
