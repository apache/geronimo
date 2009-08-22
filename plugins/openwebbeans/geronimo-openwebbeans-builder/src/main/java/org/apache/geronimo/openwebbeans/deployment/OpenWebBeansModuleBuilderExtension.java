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

package org.apache.geronimo.openwebbeans.deployment;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.j2ee.annotation.Holder;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilderExtension;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.deployment.WebModule;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.xbeans.javaee.FullyQualifiedClassType;
import org.apache.geronimo.xbeans.javaee.ListenerType;
import org.apache.geronimo.xbeans.javaee.ServletType;
import org.apache.geronimo.xbeans.javaee.WebAppType;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

/**
 * @version $Rev $Date
 */
public class OpenWebBeansModuleBuilderExtension implements ModuleBuilderExtension {

    private static final Logger log = LoggerFactory.getLogger(OpenWebBeansModuleBuilderExtension.class);

    private final Environment defaultEnvironment;
    private final NamingBuilder namingBuilders;

    private static final String CONTEXT_LISTENER_NAME = "";

    public OpenWebBeansModuleBuilderExtension(Environment defaultEnvironment, NamingBuilder namingBuilders) {
        this.defaultEnvironment = defaultEnvironment;
        this.namingBuilders = namingBuilders;
    }

    public void createModule(Module module, Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment environment, Object moduleContextInfo, AbstractName earName, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        if (!(module instanceof WebModule)) {
            //not a web module, nothing to do
            return;
        }

        EnvironmentBuilder.mergeEnvironments(environment, defaultEnvironment);
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


        EARContext moduleContext = module.getEarContext();
        Map sharedContext = module.getSharedContext();

        //add the ServletContextListener to the web app context
        GBeanData webAppData = (GBeanData) sharedContext.get(WebModule.WEB_APP_DATA);

        //jetty specific support
        Object value = webAppData.getAttribute("listenerClassNames");
        if (value instanceof Collection && !((Collection) value).contains(CONTEXT_LISTENER_NAME)) {
            ((Collection<String>) value).add(CONTEXT_LISTENER_NAME);
        } else {
            //try to add listener to the web app xml
            ListenerType listenerType = webApp.addNewListener();
            FullyQualifiedClassType className = listenerType.addNewListenerClass();
            className.setStringValue(CONTEXT_LISTENER_NAME);
        }

        AbstractName moduleName = moduleContext.getModuleName();
        Map<NamingBuilder.Key, Object> buildingContext = new HashMap<NamingBuilder.Key, Object>();
        buildingContext.put(NamingBuilder.GBEAN_NAME_KEY, moduleName);

        //use the same jndi context as the web app
        Map compContext = NamingBuilder.JNDI_KEY.get(module.getSharedContext());
        buildingContext.put(NamingBuilder.JNDI_KEY, compContext);

        //use the same holder object as the web app.
        Holder holder = NamingBuilder.INJECTION_KEY.get(sharedContext);
        buildingContext.put(NamingBuilder.INJECTION_KEY, holder);

        XmlObject jettyWebApp = webModule.getVendorDD();

        ClassFinder classFinder = createOpenWebBeansClassFinder(webApp, webModule);
        webModule.setClassFinder(classFinder);

        namingBuilders.buildNaming(webApp, jettyWebApp, webModule, buildingContext);

    }

    protected ClassFinder createOpenWebBeansClassFinder(WebAppType webApp, WebModule webModule) throws DeploymentException {

        List<Class> classes = getManagedClasses(webApp, webModule);
        return new ClassFinder(classes);
    }


    private List<Class> getManagedClasses(WebAppType webApp, WebModule webModule) throws DeploymentException {
        log.debug("getManagedClasses( " + webApp.toString() + "," + '\n' +
                (webModule != null ? webModule.getName() : null) + " ): Entry");

        // Get the classloader from the module's EARContext
        ClassLoader classLoader = webModule.getEarContext().getClassLoader();

        List<Class> classes = new ArrayList<Class>();

        // Look for all META-INF/beans.xml files and process those urls

        // Add the managed bean classes

        log.debug("getManagedClasses() Exit: " + classes.size() + " " + classes.toString());

        // return the managed bean classes
        return classes;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(OpenWebBeansModuleBuilderExtension.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true, true);
        infoBuilder.addReference("NamingBuilders", NamingBuilder.class, NameFactory.MODULE_BUILDER);

        infoBuilder.setConstructor(new String[]{
                "defaultEnvironment",
                "NamingBuilders"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
