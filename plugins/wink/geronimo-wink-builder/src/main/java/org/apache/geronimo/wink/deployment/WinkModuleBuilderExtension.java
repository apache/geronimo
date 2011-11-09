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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

import javax.servlet.http.HttpServlet;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilderExtension;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.deployment.WebModule;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.wink.GeronimoRestServlet;
import org.apache.geronimo.wink.GeronimoWinkDeloymentConfiguration;
import org.apache.openejb.jee.FacesConfig;
import org.apache.openejb.jee.FacesManagedBean;
import org.apache.openejb.jee.ParamValue;
import org.apache.openejb.jee.Servlet;
import org.apache.openejb.jee.ServletMapping;
import org.apache.openejb.jee.WebApp;
import org.apache.wink.server.internal.servlet.RestServlet;
import org.apache.xbean.finder.BundleAssignableClassFinder;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xbean.osgi.bundle.util.BundleClassFinder;
import org.apache.xbean.osgi.bundle.util.ClassDiscoveryFilter;
import org.apache.xbean.osgi.bundle.util.DiscoveryRange;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;
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

    private static final String REST_APPLICATION_AS_SERVLET_NAME = Application.class.getName();

    private static final String REST_SERVLET_NAME = GeronimoRestServlet.class.getName();

    public static final EARContext.Key<Set<Class<? extends Application>>> JAXRS_APPLICATION_SUBCLASSES = new EARContext.Key<Set<Class<? extends Application>>>() {

        @Override
        public Set<Class<? extends Application>> get(Map<EARContext.Key, Object> context) {
            return (Set<Class<? extends Application>>) context.get(this);
        }
    };

    /*
     * public static final EARContext.Key<Set<Resource>> JSF_FACELET_CONFIG_RESOURCES = new
     * EARContext.Key<Set<ConfigurationResource>>() {
     *
     * @Override public Set<ConfigurationResource> get(Map<EARContext.Key, Object> context) { return
     * (Set<ConfigurationResource>) context.get(this); } };
     */

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
            // not a web module, nothing to do
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

    @SuppressWarnings("unchecked")
    public void initContext(EARContext earContext, Module module, Bundle bundle) throws DeploymentException {

        if (!(module instanceof WebModule)) {
            // not a web module, nothing to do
            return;
        }
        WebModule webModule = (WebModule) module;
        WebApp webApp = webModule.getSpecDD();
        ServiceReference reference = bundle.getBundleContext().getServiceReference(PackageAdmin.class.getName());
        Set<Class<? extends Application>> applicationClasses = new HashSet<Class<? extends Application>>();
        try {
            PackageAdmin packageAdmin = (PackageAdmin) bundle.getBundleContext().getService(reference);

            BundleClassFinder bundleClassFinder = new BundleAssignableClassFinder(packageAdmin, bundle, new Class<?>[] { Application.class }, new ClassDiscoveryFilter() {

                @Override
                public boolean directoryDiscoveryRequired(String directory) {
                    return true;
                }

                @Override
                public boolean jarFileDiscoveryRequired(String jarUrl) {
                    return true;
                }

                @Override
                public boolean packageDiscoveryRequired(String packageName) {
                    return true;
                }

                @Override
                public boolean rangeDiscoveryRequired(DiscoveryRange discoveryRange) {
                    return discoveryRange.equals(DiscoveryRange.BUNDLE_CLASSPATH);
                }
            });

            Set<String> classes = bundleClassFinder.find();

            for (String clazz : classes) {
                applicationClasses.add(bundle.loadClass(clazz).asSubclass(Application.class));
            }

        } catch (Exception e) {
            log.warn("Fail to scan javax.ws.rs.core.Application sub classes in application", e);
        } finally {
            bundle.getBundleContext().ungetService(reference);
        }

        // JAX-RS specific code here to initialize the runtime and setup the mapping etc.

        // there's no Application sub classes found
        if (applicationClasses == null || applicationClasses.size() == 0) {
            /*
             * TODO jaxrs 1.1 spec section 2.3.2 If no Application subclass is present the added servlet MUST be named ...
             */
            return;
        }

        /*
         *  jaxrs 1.1 spec section 2.3.2 If an Application subclass is present and there is already a servlet defined that has a servlet initialization
         * ...
         */
        Class<? extends Application> applicationClass;
        for (Servlet servlet : webApp.getServlet()) {
            List<ParamValue> params = servlet.getInitParam();
            for (ParamValue parm : params) {
                if (!parm.getParamName().trim().equals("javax.ws.rs.Application")) {
                    continue;
                }
                for (Class<? extends Application> clazz : applicationClasses) {
                    if (clazz.getName().equalsIgnoreCase(parm.getParamValue().trim())) {
                        applicationClass = clazz;
                        Class<?> servletClass = null;
                        try {
                            servletClass = bundle.loadClass(servlet.getServletClass());
                        } catch (ClassNotFoundException e) {
                            log.warn("failed to load servlet class:" + servlet.getServletClass());
                        }
                        if ((servletClass == null) || !servletClass.isAssignableFrom(HttpServlet.class)) {
                            servlet.setServletClass(REST_SERVLET_NAME);
                        }
                        ParamValue paramDeploymentConfig = new ParamValue();
                        paramDeploymentConfig.setParamName(RestServlet.DEPLOYMENT_CONF_PARAM);
                        paramDeploymentConfig.setParamValue(GeronimoWinkDeloymentConfiguration.class.getName());
                        servlet.getInitParam().add(paramDeploymentConfig);
                        return;
                    }
                }

            }
        }

        /*
         * jaxrs 1.1 spec section 2.3.2 If an Application subclass is present ...
         *
         *
         * TODO It is an error for more than one application to be deployed at the same effective servlet mapping
         */

        applicationClass = applicationClasses.iterator().next();

        Servlet restServletInfo = new Servlet();
        restServletInfo.setServletClass(REST_SERVLET_NAME);
        restServletInfo.setServletName(REST_SERVLET_NAME);

        ParamValue paramApplication = new ParamValue();
        paramApplication.setParamName("javax.ws.rs.Application");
        paramApplication.setParamValue(applicationClass.getName());
        restServletInfo.getInitParam().add(paramApplication);

        ParamValue paramDeploymentConfig = new ParamValue();
        paramDeploymentConfig.setParamName("deploymentConfiguration");
        paramDeploymentConfig.setParamValue(GeronimoWinkDeloymentConfiguration.class.getName());

        restServletInfo.getInitParam().add(paramDeploymentConfig);

        if (applicationClass.isAnnotationPresent(ApplicationPath.class)) {

            ApplicationPath ap = applicationClass.getAnnotation(ApplicationPath.class);

            String mapping = ap.value();

            if (!mapping.startsWith("/") || !mapping.startsWith("*.")) {

                mapping = "/" + mapping;
            }

            if (!mapping.endsWith("/*")) {

                if (mapping.endsWith("/"))
                    mapping = mapping + "*";
                else {
                    mapping = mapping + "/*";
                }
            }

            ServletMapping restServletMapping = new ServletMapping();
            restServletMapping.setServletName(REST_SERVLET_NAME);
            restServletMapping.getUrlPattern().add(mapping);
            webApp.getServletMapping().add(restServletMapping);

        }

        webApp.getServlet().add(restServletInfo);

    }

    public void addGBeans(EARContext earContext, Module module, Bundle bundle, Collection repository) throws DeploymentException {

    }

    protected ClassFinder createWinkClassFinder(List<FacesConfig> facesConfigs, Set<Class<?>> annotatedJAXRSClasses, Bundle bundle) throws DeploymentException {
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
        if (annotatedJAXRSClasses != null) {
            for (Class<?> clas : annotatedJAXRSClasses) {
                while (clas != null) {
                    managedBeanClasses.add(clas);
                    clas = clas.getSuperclass();
                }
            }
        }
        return new ClassFinder(managedBeanClasses);
    }

    private boolean hasRestApplicationAsServlet(WebApp webApp) {
        for (Servlet servlet : webApp.getServlet()) {
            if (servlet.getServletClass() != null && REST_APPLICATION_AS_SERVLET_NAME.equals(servlet.getServletClass().trim())) {
                return true;
            }
        }
        return false;
    }

}
