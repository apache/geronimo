/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.tomcat.deployment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.jar.JarFile;

import javax.servlet.Servlet;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.Deployable;
import org.apache.geronimo.deployment.DeployableBundle;
import org.apache.geronimo.deployment.DeployableJarFile;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilderCollection;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.service.plan.EnvironmentType;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilderExtension;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.deployment.WebModule;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.util.FileUtils;
import org.apache.geronimo.kernel.util.JarUtils;
import org.apache.geronimo.naming.deployment.GBeanResourceEnvironmentBuilder;
import org.apache.geronimo.naming.deployment.ResourceEnvironmentSetter;
import org.apache.geronimo.openwebbeans.SharedOwbContext;
import org.apache.geronimo.security.deployment.GeronimoSecurityBuilderImpl;
import org.apache.geronimo.security.jaas.ConfigurationFactory;
import org.apache.geronimo.tomcat.LifecycleListenerGBean;
import org.apache.geronimo.tomcat.ManagerGBean;
import org.apache.geronimo.tomcat.RealmGBean;
import org.apache.geronimo.tomcat.TomcatWebAppContext;
import org.apache.geronimo.tomcat.ValveGBean;
import org.apache.geronimo.tomcat.cluster.CatalinaClusterGBean;
import org.apache.geronimo.tomcat.deployment.model.ContextType;
import org.apache.geronimo.tomcat.deployment.model.JaxbUtil;
import org.apache.geronimo.tomcat.deployment.model.ParameterType;
import org.apache.geronimo.tomcat.deployment.model.TomcatConfigType;
import org.apache.geronimo.tomcat.deployment.model.TomcatObjectFactory;
import org.apache.geronimo.tomcat.deployment.model.TomcatWebAppType;
import org.apache.geronimo.tomcat.util.SecurityHolder;
import org.apache.geronimo.web.WebAttributeName;
import org.apache.geronimo.web.info.ServletInfo;
import org.apache.geronimo.web.info.WebAppInfo;
import org.apache.geronimo.web25.deployment.AbstractWebModuleBuilder;
import org.apache.geronimo.web25.deployment.StandardWebAppInfoFactory;
import org.apache.geronimo.web25.deployment.WebAppInfoBuilder;
import org.apache.geronimo.web25.deployment.WebAppInfoFactory;
import org.apache.geronimo.web25.deployment.model.ObjectFactory;
import org.apache.geronimo.web25.deployment.model.WebAppType;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.WebApp;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Boolean.TRUE;

/**
 * @version $Rev:385659 $ $Date$
 */
@GBean(j2eeType = NameFactory.MODULE_BUILDER)
public class TomcatModuleBuilder extends AbstractWebModuleBuilder implements GBeanLifecycle {

    private static final Logger log = LoggerFactory.getLogger(TomcatModuleBuilder.class);

    private static final String TOMCAT_NAMESPACE = TomcatObjectFactory._Tomcat_QNAME.getNamespaceURI();
////    private static final Map<String, String> NAMESPACE_UPDATES = new HashMap<String, String>();
////    private static final Set<String> INGORED_ELEMENT_NAMES = new HashSet<String>();
////    private static final Set<String> INGORED_CONTEXT_ATTRIBUTE_NAMES = new HashSet<String>();
//    static {
//        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/web", "http://geronimo.apache.org/xml/ns/j2ee/web-2.0.1");
//        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web-1.1", "http://geronimo.apache.org/xml/ns/j2ee/web-2.0.1");
//        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web-1.2", "http://geronimo.apache.org/xml/ns/j2ee/web-2.0.1");
//        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web-2.0", "http://geronimo.apache.org/xml/ns/j2ee/web-2.0.1");
//        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/web/tomcat", "http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-2.0.1");
//        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-1.1", "http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-2.0.1");
//        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-1.2", "http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-2.0.1");
//        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-2.0", "http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-2.0.1");
//        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/web/tomcat/config", "http://geronimo.apache.org/xml/ns/j2ee/web/tomcat/config-1.0");
//
//        INGORED_ELEMENT_NAMES.add("context-priority-classloader");
//        INGORED_ELEMENT_NAMES.add("configId");
//        INGORED_ELEMENT_NAMES.add("parentId");
//
//        INGORED_CONTEXT_ATTRIBUTE_NAMES.add("className".toLowerCase());
//        INGORED_CONTEXT_ATTRIBUTE_NAMES.add("xmlNamespaceAware".toLowerCase());
//        INGORED_CONTEXT_ATTRIBUTE_NAMES.add("processTlds".toLowerCase());
//        INGORED_CONTEXT_ATTRIBUTE_NAMES.add("unpackWAR".toLowerCase());
//        INGORED_CONTEXT_ATTRIBUTE_NAMES.add("xmlValidation".toLowerCase());
//        INGORED_CONTEXT_ATTRIBUTE_NAMES.add("path");
//        INGORED_CONTEXT_ATTRIBUTE_NAMES.add("useNaming".toLowerCase());
//        INGORED_CONTEXT_ATTRIBUTE_NAMES.add("javaVMs".toLowerCase());
//        INGORED_CONTEXT_ATTRIBUTE_NAMES.add("server");
//        INGORED_CONTEXT_ATTRIBUTE_NAMES.add("j2EEApplication".toLowerCase());
//        INGORED_CONTEXT_ATTRIBUTE_NAMES.add("j2EEServer".toLowerCase());
//        INGORED_CONTEXT_ATTRIBUTE_NAMES.add("path");
//        INGORED_CONTEXT_ATTRIBUTE_NAMES.add("delegate");
//        INGORED_CONTEXT_ATTRIBUTE_NAMES.add("docBase".toLowerCase());
//    }

    private final AbstractNameQuery tomcatContainerName;
    protected final NamespaceDrivenBuilderCollection clusteringBuilders;

    public static final String GBEAN_REF_CLUSTERING_BUILDERS = "ClusteringBuilders";
    private final WebAppInfoFactory webAppInfoFactory;

    public TomcatModuleBuilder(
            @ParamAttribute(name = "tomcatContainerName") AbstractNameQuery tomcatContainerName,
            @ParamAttribute(name = "defaultWebApp") WebAppInfo defaultWebApp,
            @ParamAttribute(name = "jspServlet") WebAppInfo jspServlet,
            @ParamReference(name="WebServiceBuilder", namingType = NameFactory.MODULE_BUILDER) Collection<WebServiceBuilder> webServiceBuilder,
            @ParamReference(name="ServiceBuilders", namingType = NameFactory.MODULE_BUILDER)Collection<NamespaceDrivenBuilder> serviceBuilders,
            @ParamReference(name="NamingBuilders", namingType = NameFactory.MODULE_BUILDER)NamingBuilder namingBuilders,
            @ParamReference(name= GBEAN_REF_CLUSTERING_BUILDERS, namingType = NameFactory.MODULE_BUILDER)Collection<NamespaceDrivenBuilder> clusteringBuilders,
            @ParamReference(name="ModuleBuilderExtensions", namingType = NameFactory.MODULE_BUILDER)Collection<ModuleBuilderExtension> moduleBuilderExtensions,
            @ParamReference(name="ResourceEnvironmentSetter", namingType = NameFactory.MODULE_BUILDER)ResourceEnvironmentSetter resourceEnvironmentSetter,
            @ParamSpecial(type = SpecialAttributeType.kernel) Kernel kernel,
            @ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext) {
        super(kernel, serviceBuilders, namingBuilders, resourceEnvironmentSetter, webServiceBuilder, moduleBuilderExtensions, bundleContext);
        this.clusteringBuilders = new NamespaceDrivenBuilderCollection(clusteringBuilders);
        this.tomcatContainerName = tomcatContainerName;
        ServletInfo jspServletInfo;
        if (jspServlet != null) {
            jspServletInfo = jspServlet.servlets.get(0);
        } else {
            jspServletInfo = null;
        }
        this.webAppInfoFactory = new StandardWebAppInfoFactory(defaultWebApp, jspServletInfo);

    }

    public void doStart() throws Exception {
//        XmlBeansUtil.registerNamespaceUpdates(NAMESPACE_UPDATES);
    }

    public void doStop() {
//        XmlBeansUtil.unregisterNamespaceUpdates(NAMESPACE_UPDATES);
    }

    public void doFail() {
        doStop();
    }

    public Module createModule(Bundle bundle, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        if (bundle == null) {
            throw new NullPointerException("bundle is null");
        }
        String contextPath = (String) bundle.getHeaders().get("Web-ContextPath");
        if (contextPath == null) {
            // not for us
            return null;
        }

        String specDD = null;
        WebApp webApp = null;

        URL specDDUrl = BundleUtils.getEntry(bundle, "WEB-INF/web.xml");
        if (specDDUrl == null) {
            webApp = new WebApp();
        } else {
            try {
                specDD = JarUtils.readAll(specDDUrl);

                InputStream in = specDDUrl.openStream();
                try {
                    webApp = (WebApp) JaxbJavaee.unmarshalJavaee(WebApp.class, in);
                } finally {
                    in.close();
                }
//                WebDeploymentValidationUtils.validateWebApp(webApp);
            } catch (Exception e) {
                throw new DeploymentException("Error reading web.xml for " + bundle.getSymbolicName(), e);
            }
        }

        AbstractName earName = null;
        String targetPath = ".";
        boolean standAlone = true;

        Deployable deployable = new DeployableBundle(bundle);
        // parse vendor dd
        WebAppType tomcatWebApp = getTomcatWebApp(null, deployable, standAlone, targetPath, webApp);

        EnvironmentType environmentType = tomcatWebApp.getEnvironment();
        Environment environment = EnvironmentBuilder.buildEnvironment(environmentType);

        if (webApp.getDistributable().size() == 1) {
            clusteringBuilders.buildEnvironment(tomcatWebApp, environment);
        }

        idBuilder.resolve(environment, bundle.getSymbolicName(), "wab");

        AbstractName moduleName;
        if (earName == null) {
            earName = naming.createRootName(environment.getConfigId(), NameFactory.NULL, NameFactory.J2EE_APPLICATION);
            moduleName = naming.createChildName(earName, environment.getConfigId().toString(), NameFactory.WEB_MODULE);
        } else {
            moduleName = naming.createChildName(earName, targetPath, NameFactory.WEB_MODULE);
        }

        String name = webApp.getModuleName();
        if (name == null) {
            name = bundle.getSymbolicName();
        }

        WebModule<WebAppType> module = new WebModule<WebAppType>(standAlone, moduleName, name, environment, deployable, targetPath, webApp, tomcatWebApp, specDD, contextPath, TOMCAT_NAMESPACE, shareJndi(null), null);
        for (ModuleBuilderExtension mbe : moduleBuilderExtensions) {
            mbe.createModule(module, bundle, naming, idBuilder);
        }
        return module;
    }

    protected Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment earEnvironment, String contextRoot, Module parentModule, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        assert moduleFile != null : "moduleFile is null";
        assert targetPath != null : "targetPath is null";
        assert !targetPath.endsWith("/") : "targetPath must not end with a '/'";

        // parse the spec dd
        String specDD = null;
        WebApp webApp = null;
        try {
            if (specDDUrl == null) {
                specDDUrl = JarUtils.createJarURL(moduleFile, "WEB-INF/web.xml");
            }

            // read in the entire specDD as a string, we need this for getDeploymentDescriptor
            // on the J2ee management object
            specDD = JarUtils.readAll(specDDUrl);

            // we found web.xml, if it won't parse that's an error.
            InputStream in = specDDUrl.openStream();
            try {
                webApp = (WebApp) JaxbJavaee.unmarshalJavaee(WebApp.class, in);
            } finally {
                in.close();
            }
//            WebDeploymentValidationUtils.validateWebApp(webApp);
        } catch (JAXBException e) {
            // Output the target path in the error to make it clearer to the user which webapp
            // has the problem.  The targetPath is used, as moduleFile may have an unhelpful
            // value such as C:\geronimo-1.1\var\temp\geronimo-deploymentUtil22826.tmpdir
            throw new DeploymentException("Error parsing web.xml for " + targetPath, e);
        } catch (Exception e) {
            if (!moduleFile.getName().endsWith(".war")) {
                //not for us
                return null;
            }
            //else ignore as jee5 allows optional spec dd for .war's
        }

        if (webApp == null) {
            webApp = new WebApp();
        }

        Deployable deployable = new DeployableJarFile(moduleFile);
        // parse vendor dd
        boolean standAlone = earEnvironment == null;
        WebAppType tomcatWebApp = getTomcatWebApp(plan, deployable, standAlone, targetPath, webApp);
        contextRoot = getContextRoot(tomcatWebApp, contextRoot, webApp, standAlone, moduleFile, targetPath);

        EnvironmentType environmentType = tomcatWebApp.getEnvironment();
        Environment environment = EnvironmentBuilder.buildEnvironment(environmentType);

        if (webApp.getDistributable().size() == 1) {
            clusteringBuilders.buildEnvironment(tomcatWebApp, environment);
        }

        if (!standAlone && COMBINED_BUNDLE) {
            EnvironmentBuilder.mergeEnvironments(earEnvironment, environment);
            environment = earEnvironment;
        }

        // Note: logic elsewhere depends on the default artifact ID being the file name less extension (ConfigIDExtractor)
        String warName = new File(moduleFile.getName()).getName();
        if (warName.lastIndexOf('.') > -1) {
            warName = warName.substring(0, warName.lastIndexOf('.'));
        }
        idBuilder.resolve(environment, warName, "war");

        AbstractName moduleName;
        AbstractName earName;
        if (parentModule == null) {
            earName = naming.createRootName(environment.getConfigId(), NameFactory.NULL, NameFactory.J2EE_APPLICATION);
            moduleName = naming.createChildName(earName, environment.getConfigId().toString(), NameFactory.WEB_MODULE);
        } else {
            earName = parentModule.getModuleName();
            moduleName = naming.createChildName(earName, targetPath, NameFactory.WEB_MODULE);
        }

        String name = webApp.getModuleName();
        if (name == null) {
            if (standAlone) {
                name = FileUtils.removeExtension(new File(moduleFile.getName()).getName(), ".war");
            } else {
                name = FileUtils.removeExtension(targetPath, ".war");
            }
        }

        WebModule<WebAppType> module = new WebModule<WebAppType>(standAlone, moduleName, name, environment, deployable, targetPath, webApp, tomcatWebApp, specDD, contextRoot, TOMCAT_NAMESPACE, shareJndi(parentModule), parentModule);
        for (ModuleBuilderExtension mbe : moduleBuilderExtensions) {
            mbe.createModule(module, plan, moduleFile, targetPath, specDDUrl, environment, contextRoot, earName, naming, idBuilder);
        }
        return module;
    }

    private String getContextRoot(WebAppType tomcatWebApp, String contextRoot, WebApp webApp, boolean standAlone, JarFile moduleFile, String targetPath) {
        //If we have a context root, override everything
        if (tomcatWebApp.getContextRoot() != null) {
            contextRoot = tomcatWebApp.getContextRoot();
        } else if (contextRoot == null || contextRoot.trim().equals("")) {
            //Otherwise if no contextRoot was passed in from the ear, then make up a default
            contextRoot = determineDefaultContextRoot(webApp, standAlone, moduleFile, targetPath);
        }
        contextRoot = contextRoot.trim();
        if (contextRoot.length() > 0) {
            // only force the context root to start with a forward slash
            // if it is not null
            if (!contextRoot.startsWith("/")) {
                //I'm not sure if we should always fix up peculiar context roots.
                contextRoot = "/" + contextRoot;
            }
        }
        return contextRoot;
    }


    WebAppType getTomcatWebApp(Object plan, Deployable deployable, boolean standAlone, String targetPath, WebApp webApp) throws DeploymentException {
        try {
            // load the geronimo-web.xml from either the supplied plan or from the earFile  '
            WebAppType rawPlan = null;
            try {
                if (plan instanceof WebAppType) {
                    rawPlan = (WebAppType) plan;
                } else {
                    if (plan != null) {
                        rawPlan = parse(((File) plan).toURI().toURL());
                    } else {
                        URL path = deployable.getResource("WEB-INF/geronimo-web.xml");
                        if (path == null) {
                            path = deployable.getResource("WEB-INF/geronimo-tomcat.xml");
                        }
                        if (path == null) {
                            log.warn("Web application " + targetPath + " does not contain a WEB-INF/geronimo-web.xml deployment plan.  This may or may not be a problem, depending on whether you have things like resource references that need to be resolved.  You can also give the deployer a separate deployment plan file on the command line.");
                        } else {
                            rawPlan = parse(path);
                        }
                    }
                }
            } catch (IOException e) {
                log.warn("Failed to load geronimo-web.xml", e);
            }

            if (rawPlan == null) {
                rawPlan = createDefaultPlan();
            }

            TomcatConfigType tomcatConfigType;
            if (rawPlan.getContainerConfig() instanceof TomcatConfigType) {
                tomcatConfigType = (TomcatConfigType) rawPlan.getContainerConfig();
            } else {
                tomcatConfigType = new TomcatConfigType();
                rawPlan.setContainerConfig(tomcatConfigType);
            }
            if (rawPlan instanceof TomcatWebAppType) {
                TomcatWebAppType tomcatPlan = (TomcatWebAppType) rawPlan;
                tomcatConfigType.setCluster(tomcatPlan.getCluster());
                tomcatConfigType.setContext(tomcatPlan.getContext());
                tomcatConfigType.setCrossContext(tomcatPlan.getCrossContext());
                tomcatConfigType.setDisableCookies(tomcatPlan.getDisableCookies());
                tomcatConfigType.setHost(tomcatPlan.getHost());
                tomcatConfigType.setListenerChain(tomcatPlan.getListenerChain());
                tomcatConfigType.setManager(tomcatPlan.getManager());
                tomcatConfigType.setTomcatRealm(tomcatPlan.getTomcatRealm());
                tomcatConfigType.setValveChain(tomcatPlan.getValveChain());
            }
            return rawPlan;
        } catch (Exception e) {
            throw new DeploymentException("xml problem for web app " + targetPath, e);
        }
    }

    private WebAppType parse(URL path) throws IOException, XMLStreamException, JAXBException {
        InputStream in = path.openStream();
        try {
            return JaxbUtil.unmarshalTomcatWebApp(in, false);
        } catch (JAXBException e) {
            InputStream in2 = path.openStream();
            try {
                return JaxbUtil.unmarshalWebApp(in2, false);
            } finally {
                in2.close();
            }
        } finally {
            in.close();
        }
    }

    private WebAppType createDefaultPlan() {
        return new ObjectFactory().createWebAppType();
    }

    @Override
    protected void postInitContext(EARContext earContext, WebModule<WebAppType> webModule, Bundle bundle) throws DeploymentException {
        for (ModuleBuilderExtension mbe : moduleBuilderExtensions) {
            mbe.initContext(earContext, webModule, bundle);
        }
        //Process Web Service
        Map<String, String> servletNameToPathMap = buildServletNameToPathMap(webModule.getSpecDD(), webModule.getContextRoot());
        for (WebServiceBuilder serviceBuilder : webServiceBuilder) {
            serviceBuilder.findWebServices(webModule, false, servletNameToPathMap, webModule.getEnvironment(), webModule.getSharedContext());
        }
    }

    @Override
    protected void preInitContext(EARContext earContext, WebModule<WebAppType> webModule, Bundle bundle) throws DeploymentException {
        WebAppType gerWebApp = webModule.getVendorDD();
        boolean hasSecurityRealmName = gerWebApp.getSecurityRealmName() != null;
        webModule.getEarContext().getGeneralData().put(WEB_MODULE_HAS_SECURITY_REALM, hasSecurityRealmName);
    }

    public void addGBeans(EARContext earContext, Module module, Bundle bundle, Collection repository) throws DeploymentException {
        EARContext moduleContext = module.getEarContext();
        Bundle webBundle = moduleContext.getDeploymentBundle();
        AbstractName moduleName = module.getModuleName();
        WebModule<WebAppType> webModule = (WebModule<WebAppType>) module;

        WebApp webApp = webModule.getSpecDD();

        WebAppType tomcatWebApp = webModule.getVendorDD();

        GBeanData webModuleData = new GBeanData(moduleName, TomcatWebAppContext.class);
        configureBasicWebModuleAttributes(webApp, tomcatWebApp, moduleContext, earContext, webModule, webModuleData);
        String contextPath = webModule.getContextRoot();
        if (!contextPath.startsWith("/")) {
            contextPath = "/" + contextPath;
        }
        try {
            moduleContext.addGBean(webModuleData);
            Map<String, String> contextAttributes = new HashMap<String, String>();
            webModuleData.setAttribute("contextPath", contextPath);
            // unsharableResources, applicationManagedSecurityResources
            GBeanResourceEnvironmentBuilder rebuilder = new GBeanResourceEnvironmentBuilder(webModuleData);
            //N.B. use earContext not moduleContext
            resourceEnvironmentSetter.setResourceEnvironment(rebuilder, webApp.getResourceRef(), tomcatWebApp.getResourceRef());

            if (tomcatWebApp.getWebContainer() != null) {
                //TODO osgi filter
//                AbstractNameQuery webContainerName = ENCConfigBuilder.getGBeanQuery(GBeanInfoBuilder.DEFAULT_J2EE_TYPE, tomcatWebApp.getWebContainer());
//                webModuleData.setReferencePattern("Container", webContainerName);
            } else {
                webModuleData.setReferencePattern("Container", tomcatContainerName);
            }

            if (tomcatWebApp.getWorkDir() != null) {
                String workDir = tomcatWebApp.getWorkDir();
                contextAttributes.put("workDir", workDir);
            }

            WebAppInfoBuilder webAppInfoBuilder = new WebAppInfoBuilder(webApp, webAppInfoFactory);
            WebAppInfo webAppInfo = webAppInfoBuilder.build();

            // Process the Tomcat container-config elements
            TomcatConfigType config = (TomcatConfigType) tomcatWebApp.getContainerConfig();
            if (config != null) {
                if (config.getHost() != null) {
                    String virtualServer = config.getHost().trim();
                    webModuleData.setAttribute("virtualServer", virtualServer);
                }

                if (config.getCrossContext() != null) {
                    contextAttributes.put("crossContext", "true");
                }

                if (config.getDisableCookies() != null) {
                    contextAttributes.put("cookies", "false");
                }

                if (config.getTomcatRealm() != null) {
                    String tomcatRealm = config.getTomcatRealm().trim();
                    AbstractName realmName = earContext.getNaming().createChildName(moduleName, tomcatRealm, RealmGBean.GBEAN_INFO.getJ2eeType());
                    webModuleData.setReferencePattern("TomcatRealm", realmName);
                }
                if (config.getValveChain() != null) {
                    String valveChain = config.getValveChain().trim();
                    AbstractName valveName = earContext.getNaming().createChildName(moduleName, valveChain, ValveGBean.J2EE_TYPE);
                    webModuleData.setReferencePattern("TomcatValveChain", valveName);
                }

                if (config.getListenerChain() != null) {
                    String listenerChain = config.getListenerChain().trim();
                    AbstractName listenerName = earContext.getNaming().createChildName(moduleName, listenerChain, LifecycleListenerGBean.J2EE_TYPE);
                    webModuleData.setReferencePattern("LifecycleListenerChain", listenerName);
                }

                if (config.getCluster() != null) {
                    String cluster = config.getCluster().trim();
                    AbstractName clusterName = earContext.getNaming().createChildName(moduleName, cluster, CatalinaClusterGBean.J2EE_TYPE);
                    webModuleData.setReferencePattern("Cluster", clusterName);
                }

                if (config.getManager() != null) {
                    String manager = config.getManager().trim();
                    AbstractName managerName = earContext.getNaming().createChildName(moduleName, manager, ManagerGBean.J2EE_TYPE);
                    webModuleData.setReferencePattern(TomcatWebAppContext.GBEAN_REF_MANAGER_RETRIEVER, managerName);
                }
                //Add context attributes and parameters
                if (config.getContext() != null) {
                    ContextType context = config.getContext();
//                    NamedNodeMap namedNodeMap = context.getDomNode().getAttributes();
//                    for (int i = 0; i < namedNodeMap.getLength(); i++) {
//                        Node node = namedNodeMap.item(i);
//                        String attributeName = node.getNodeName();
//                        if (INGORED_CONTEXT_ATTRIBUTE_NAMES.contains(attributeName.toLowerCase())) {
//                            if (log.isWarnEnabled()) {
//                                log.warn("Context attribute " + attributeName + " in the geronimo-web.xml is ignored, as it is not support or Geronimo has already configured it");
//                            }
//                            continue;
//                        }
//                        if (contextAttributes.containsKey(attributeName)) {
//                            if (log.isWarnEnabled()) {
//                                log.warn("Context attribute " + attributeName
//                                        + " on the context element in geronimo-web.xml is ignored, as it has been explicitly configured with other elements in the geronimo-web.xml file");
//                            }
//                            continue;
//                        }
//                        contextAttributes.put(node.getNodeName(), node.getNodeValue());
//                    }
                    for (ParameterType parameterType : context.getParameter()) {
                        if (webAppInfo.contextParams.containsKey(parameterType.getName()) && !parameterType.isOverride()) {
                            if (log.isWarnEnabled()) {
                                log.warn("Context parameter from geronimo-web.xml is ignored, as a same name context paramter " + parameterType.getName() + " = "
                                        + webAppInfo.contextParams.get(parameterType.getName()) + " in web.xml, configure override with true to make the value take effect.");
                            }
                            continue;
                        }
                        webAppInfo.contextParams.put(parameterType.getName(), parameterType.getValue());
                    }
                }
            }

            Boolean distributable = !webApp.getDistributable().isEmpty();
            if (TRUE == distributable) {
                clusteringBuilders.build(tomcatWebApp, earContext, moduleContext);
                if (null == webModuleData.getReferencePatterns(TomcatWebAppContext.GBEAN_REF_CLUSTERED_VALVE_RETRIEVER)) {
                    log.warn("No clustering builders configured: app will not be clustered");
                }
            }

            webModuleData.setAttribute("webAppInfo", webAppInfo);

            webModule.getSharedContext().put(WebModule.WEB_APP_INFO, webAppInfoBuilder);

            /**
             * The old geronimo-web.xml also support to configure some context attributes individually,
             * let's override them in the contextAttributes
             */

            webModuleData.setAttribute("contextAttributes", contextAttributes);

            //Handle the role permissions and webservices on the servlets.
            Map<String, AbstractName> webServices = new HashMap<String, AbstractName>();
            Class<?> baseServletClass;
            try {
                baseServletClass = webBundle.loadClass(Servlet.class.getName());
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Could not load javax.servlet.Servlet in bundle " + bundle, e);
            }

            for (org.apache.openejb.jee.Servlet servlet : webApp.getServlet()) {
                String servletClassName = servlet.getServletClass();
                if(servletClassName == null || servletClassName.length() == 0) {
                    continue;
                }
                String servletName = servlet.getServletName();
                Class<?> servletClass;
                try {
                    servletClass = webBundle.loadClass(servletClassName);
                } catch (ClassNotFoundException e) {
                    throw new DeploymentException("Could not load servlet class " + servletClassName, e);
                }
                if (!baseServletClass.isAssignableFrom(servletClass)) {
                    //fake servletData
                    AbstractName servletAbstractName = moduleContext.getNaming().createChildName(moduleName, servletName, NameFactory.SERVLET);
                    GBeanData servletData = new GBeanData();
                    servletData.setAbstractName(servletAbstractName);
                    //let the web service builder deal with configuring the gbean with the web service stack
                    //Here we just extract the factory reference
                    boolean configured = false;
                    for (WebServiceBuilder serviceBuilder : webServiceBuilder) {
                        if (serviceBuilder.configurePOJO(servletData, servletName, module, servletClassName, moduleContext)) {
                            configured = true;
                            break;
                        }
                    }
                    if (!configured) {
                        throw new DeploymentException("POJO web service: " + servletName + " not configured by any web service builder");
                    }
                    ReferencePatterns patterns = servletData.getReferencePatterns("WebServiceContainerFactory");
                    AbstractName wsContainerFactoryName = patterns.getAbstractName();
                    webServices.put(servletName, wsContainerFactoryName);
                    //force all the factories to start before the web app that needs them.
                    webModuleData.addDependency(wsContainerFactoryName);
                }
            }


            webModuleData.setAttribute("webServices", webServices);

            if (tomcatWebApp.getSecurityRealmName() != null) {
                if (earContext.getSecurityConfiguration() == null) {
                    throw new DeploymentException("You have specified a <security-realm-name> for the webapp " + moduleName + " but no <security> configuration (role mapping) is supplied in the Geronimo plan for the web application (or the Geronimo plan for the EAR if the web app is in an EAR)");
                }

                SecurityHolder securityHolder = new SecurityHolder();
                String securityRealmName = tomcatWebApp.getSecurityRealmName().trim();

                webModuleData.setReferencePattern("RunAsSource", GeronimoSecurityBuilderImpl.ROLE_MAPPER_DATA_NAME.get(earContext.getGeneralData()));
                webModuleData.setReferencePattern("ConfigurationFactory", new AbstractNameQuery(null, Collections.singletonMap("name", securityRealmName), ConfigurationFactory.class.getName()));

                /**
                 * TODO - go back to commented version when possible.
                 */
                String policyContextID = moduleName.toString().replaceAll("[, :]", "_");
                securityHolder.setPolicyContextID(policyContextID);

                /*
                 * For web applications, we would not calculate permissions in the deployment time, as it is allowed to update in Servlet 3.0 on the initialize step
                ComponentPermissions componentPermissions = buildSpecSecurityConfig(webApp);
                earContext.addSecurityContext(policyContextID, componentPermissions);
                */
                //TODO WTF is this for?
                securityHolder.setSecurity(true);

                webModuleData.setAttribute("securityHolder", securityHolder);
                //local jaspic configuration
                if (tomcatWebApp.getAuthentication() != null) {
                    configureLocalJaspicProvider(tomcatWebApp.getAuthentication(), contextPath, module, webModuleData);
                }
            }

            //Save Deployment Attributes
            Map<String, Object> deploymentAttributes = new HashMap<String, Object>();
            deploymentAttributes.put(WebAttributeName.META_COMPLETE.name(), webApp.isMetadataComplete());
            deploymentAttributes.put(WebAttributeName.SCHEMA_VERSION.name(), INITIAL_WEB_XML_SCHEMA_VERSION.get(earContext.getGeneralData()));
            deploymentAttributes.put(WebAttributeName.ORDERED_LIBS.name(), AbstractWebModuleBuilder.ORDERED_LIBS.get(earContext.getGeneralData()));
            deploymentAttributes.put(WebAttributeName.SERVLET_CONTAINER_INITIALIZERS.name(), AbstractWebModuleBuilder.SERVLET_CONTAINER_INITIALIZERS.get(earContext.getGeneralData()));
            webModuleData.setAttribute("deploymentAttributes", deploymentAttributes);

            //listeners added directly to the StandardContext will get loaded by the tomcat classloader, not the app classloader!
            //TODO this may definitely not be the best place for this!
            for (ModuleBuilderExtension mbe : moduleBuilderExtensions) {
                mbe.addGBeans(earContext, module, bundle, repository);
            }
            LinkedHashSet<Module<?, ?>> submodules = module.getModules();
            for (Module<?, ?> subModule: submodules) {
                if (subModule.getSharedContext().get(SharedOwbContext.class) != null) {
                    GBeanData data = (GBeanData) subModule.getSharedContext().get(SharedOwbContext.class);
                    AbstractName name = data.getAbstractName();
                    webModuleData.setReferencePattern("SharedOwbContext", name);
                }
            }
            if(tomcatWebApp.getSecurityRealmName() != null) {
                webModuleData.setReferencePattern("applicationPolicyConfigurationManager", EARContext.JACC_MANAGER_NAME_KEY.get(earContext.getGeneralData()));
            }
            //not truly metadata complete until MBEs have run
            if (INITIAL_WEB_XML_SCHEMA_VERSION.get(earContext.getGeneralData()) >= 2.5f) {
                webApp.setMetadataComplete(true);
                String specDeploymentPlan = getSpecDDAsString(webModule);
                module.setOriginalSpecDD(specDeploymentPlan);
                earContext.addFile(module.getTargetPathURI().resolve("WEB-INF/web.xml"), specDeploymentPlan);
            }
            webModuleData.setAttribute("deploymentDescriptor", module.getOriginalSpecDD());
            module.addAsChildConfiguration();
        } catch (DeploymentException de) {
            throw de;
        } catch (Exception e) {
            throw new DeploymentException("Unable to initialize GBean for web app " + module.getName(), e);
        }
    }

    public String getSchemaNamespace() {
        return TOMCAT_NAMESPACE;
    }

}
