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

package org.apache.geronimo.wink.deployment;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

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
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.web.info.WebAppInfo;
import org.apache.openejb.jee.FacesConfig;
import org.apache.openejb.jee.FacesManagedBean;
import org.apache.openejb.jee.Servlet;
import org.apache.openejb.jee.WebApp;
import org.apache.wink.server.internal.servlet.RestServlet;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev $Date
 */

@GBean(j2eeType = NameFactory.MODULE_BUILDER)
public class WinkModuleBuilderExtension implements ModuleBuilderExtension {

    private static final Logger log = LoggerFactory.getLogger(WinkModuleBuilderExtension.class);

    private final Environment defaultEnvironment;

    private final NamingBuilder namingBuilders;

    //private static final String CONTEXT_LISTENER_NAME = GeronimoStartupServletContextListener.class.getName();

    private static final String FACES_SERVLET_NAME = RestServlet.class.getName();

    /*public static final EARContext.Key<Set<ConfigurationResource>> JSF_META_INF_CONFIGURATION_RESOURCES = new EARContext.Key<Set<ConfigurationResource>>() {

        @Override
        public Set<ConfigurationResource> get(Map<EARContext.Key, Object> context) {
            return (Set<ConfigurationResource>) context.get(this);
        }
    };*/

    public WinkModuleBuilderExtension(@ParamAttribute(name = "defaultEnvironment") Environment defaultEnvironment,
            @ParamReference(name = "NamingBuilders", namingType = NameFactory.MODULE_BUILDER) NamingBuilder namingBuilders) {
        this.defaultEnvironment = defaultEnvironment;
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
        if (!hasRestServlet(webApp)) {
            return;
        }

        EnvironmentBuilder.mergeEnvironments(module.getEnvironment(), defaultEnvironment);
    }

    public void installModule(JarFile earFile, EARContext earContext, Module module, Collection configurationStores, ConfigurationStore targetConfigurationStore, Collection repository)
            throws DeploymentException {
        if (!(module instanceof WebModule)) {
            return;
        }
        
        //module.getEarContext().getGeneralData().put(JSF_META_INF_CONFIGURATION_RESOURCES, findMetaInfConfigurationResources(earContext, module));
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
        if (!hasRestServlet(webApp)) {
            return;
        }

        EARContext moduleContext = module.getEarContext();
        Map sharedContext = module.getSharedContext();
        //add the ServletContextListener to the web app context
        GBeanData webAppData = (GBeanData) sharedContext.get(WebModule.WEB_APP_DATA);
        // add myfaces listener
        WebAppInfo webAppInfo = (WebAppInfo) webAppData.getAttribute("webAppInfo");
        

        AbstractName moduleName = moduleContext.getModuleName();
        Map<EARContext.Key, Object> buildingContext = new HashMap<EARContext.Key, Object>();
        buildingContext.put(NamingBuilder.GBEAN_NAME_KEY, moduleName);

        //use the same holder object as the web app.
        Holder holder = NamingBuilder.INJECTION_KEY.get(sharedContext);
        buildingContext.put(NamingBuilder.INJECTION_KEY, holder);

        XmlObject jettyWebApp = webModule.getVendorDD();

    }

   

    protected ClassFinder createWinkClassFinder(List<FacesConfig> facesConfigs, Set<Class> annotatedManagedBeanClasses, Bundle bundle) throws DeploymentException {
        List<Class<?>> managedBeanClasses = new ArrayList<Class<?>>();
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
                    log.warn("WinkModuleBuilderExtension: Could not load managed bean class: " + className);
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


    private boolean hasRestServlet(WebApp webApp) {
        for (Servlet servlet : webApp.getServlet()) {
            if (servlet.getServletClass() != null && FACES_SERVLET_NAME.equals(servlet.getServletClass().trim())) {
                return true;
            }
        }
        return false;
    }

}
