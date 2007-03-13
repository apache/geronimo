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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
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
import org.apache.geronimo.xbeans.javaee.WebAppType;
import org.apache.xbean.finder.ClassFinder;
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
        ClassFinder classFinder = createMyFacesClassFinder(webModule);
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

    private ClassFinder createMyFacesClassFinder(WebModule webModule) throws DeploymentException {

        List<Class> classes = new ArrayList<Class>();

        // Get the classloader from the module's EARContext
        ClassLoader classLoader = webModule.getEarContext().getClassLoader();

        try {
            URL url = getConfigFileURL(classLoader);

// TODO: Handle JSF 1.1 config files with DTD schema
            XmlObject xml = XmlBeansUtil.parse(url, null);
            FacesConfigDocument fcd = (FacesConfigDocument) XmlBeansUtil.typedCopy(xml, FacesConfigDocument.type);
            FacesConfigType facesConfig = fcd.getFacesConfig();

            // Get all the managed beans from the faces config file
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
        catch (Exception anyException) {
            log.debug("MyFacesModuleBuilderExtension: Exception caught while create classfinder");
        }
        return new ClassFinder(classes);
    }


    private static URL getConfigFileURL(ClassLoader moduleClassLoader) {
        URL url = moduleClassLoader.getResource("META-INF/faces-confg.xml");
        if (url == null) {
            //todo context initialization param javax.faces.CONFIG_FILES
        }
        if (url == null) {
            url = moduleClassLoader.getResource("WEB-INF/faces-confg.xml");
        }
        return url;
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
