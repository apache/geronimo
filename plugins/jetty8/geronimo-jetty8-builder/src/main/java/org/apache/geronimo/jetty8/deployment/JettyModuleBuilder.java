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

package org.apache.geronimo.jetty8.deployment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

import javax.xml.bind.JAXBException;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.Deployable;
import org.apache.geronimo.deployment.DeployableBundle;
import org.apache.geronimo.deployment.DeployableJarFile;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilderCollection;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
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
import org.apache.geronimo.jetty8.Host;
import org.apache.geronimo.jetty8.WebAppContextManager;
import org.apache.geronimo.jetty8.WebAppContextWrapper;
import org.apache.geronimo.jetty8.security.AuthConfigProviderHandlerFactory;
import org.apache.geronimo.jetty8.security.BuiltInAuthMethod;
import org.apache.geronimo.jetty8.security.JettySecurityHandlerFactory;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.lifecycle.LifecycleAdapter;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.util.FileUtils;
import org.apache.geronimo.kernel.util.JarUtils;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.naming.deployment.GBeanResourceEnvironmentBuilder;
import org.apache.geronimo.naming.deployment.ResourceEnvironmentSetter;
import org.apache.geronimo.schema.ElementConverter;
import org.apache.geronimo.schema.NamespaceElementConverter;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.security.deployment.GeronimoSecurityBuilderImpl;
import org.apache.geronimo.security.jaas.ConfigurationFactory;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.web.WebApplicationConstants;
import org.apache.geronimo.web.deployment.GenericToSpecificPlanConverter;
import org.apache.geronimo.web.info.LoginConfigInfo;
import org.apache.geronimo.web.info.ServletInfo;
import org.apache.geronimo.web.info.WebAppInfo;
import org.apache.geronimo.web25.deployment.AbstractWebModuleBuilder;
import org.apache.geronimo.web25.deployment.JspServletInfoProvider;
import org.apache.geronimo.web25.deployment.StandardWebAppInfoFactory;
import org.apache.geronimo.web25.deployment.WebAppInfoBuilder;
import org.apache.geronimo.web25.deployment.security.AuthenticationWrapper;
import org.apache.geronimo.xbeans.geronimo.jaspi.JaspiAuthModuleType;
import org.apache.geronimo.xbeans.geronimo.jaspi.JaspiConfigProviderType;
import org.apache.geronimo.xbeans.geronimo.jaspi.JaspiServerAuthConfigType;
import org.apache.geronimo.xbeans.geronimo.jaspi.JaspiServerAuthContextType;
import org.apache.geronimo.xbeans.geronimo.web.jetty.JettyAuthenticationType;
import org.apache.geronimo.xbeans.geronimo.web.jetty.JettyWebAppDocument;
import org.apache.geronimo.xbeans.geronimo.web.jetty.JettyWebAppType;
import org.apache.geronimo.xbeans.geronimo.web.jetty.config.GerJettyDocument;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.WebApp;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev:385659 $ $Date$
 */

@GBean(j2eeType = NameFactory.MODULE_BUILDER)
public class JettyModuleBuilder extends AbstractWebModuleBuilder implements GBeanLifecycle {
    private static final Logger log = LoggerFactory.getLogger(JettyModuleBuilder.class);

    private static final Map<String, String> NAMESPACE_UPDATES = new HashMap<String, String>();
    private static final Map<String, ElementConverter> GERONIMO_SCHEMA_CONVERSIONS = new HashMap<String, ElementConverter>();
    private static final String JASPI_NAMESPACE = "http://geronimo.apache.org/xml/ns/geronimo-jaspi";

    static {
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/web", "http://geronimo.apache.org/xml/ns/j2ee/web-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web-1.1", "http://geronimo.apache.org/xml/ns/j2ee/web-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web-1.2", "http://geronimo.apache.org/xml/ns/j2ee/web-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web-2.0", "http://geronimo.apache.org/xml/ns/j2ee/web-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/web/jetty", "http://geronimo.apache.org/xml/ns/j2ee/web/jetty-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web/jetty-1.1", "http://geronimo.apache.org/xml/ns/j2ee/web/jetty-2.0.2");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web/jetty-1.2", "http://geronimo.apache.org/xml/ns/j2ee/web/jetty-2.0.2");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web/jetty-2.0", "http://geronimo.apache.org/xml/ns/j2ee/web/jetty-2.0.2");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web/jetty-2.0.1", "http://geronimo.apache.org/xml/ns/j2ee/web/jetty-2.0.2");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/web/jetty/config", "http://geronimo.apache.org/xml/ns/web/jetty/config-1.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/web/jetty/config-1.0", "http://geronimo.apache.org/xml/ns/web/jetty/config-1.0.1");
        GERONIMO_SCHEMA_CONVERSIONS.put("configProvider", new NamespaceElementConverter(JASPI_NAMESPACE));
        GERONIMO_SCHEMA_CONVERSIONS.put("serverAuthConfig", new NamespaceElementConverter(JASPI_NAMESPACE));
        GERONIMO_SCHEMA_CONVERSIONS.put("serverAuthContext", new NamespaceElementConverter(JASPI_NAMESPACE));
        GERONIMO_SCHEMA_CONVERSIONS.put("serverAuthModule", new NamespaceElementConverter(JASPI_NAMESPACE));
    }

    private final Environment defaultEnvironment;
    private final AbstractNameQuery jettyContainerObjectName;
    private final StandardWebAppInfoFactory webAppInfoFactory;

    protected final NamespaceDrivenBuilderCollection clusteringBuilders;

    private final Integer defaultSessionTimeoutMinutes;

    private static final String JETTY_NAMESPACE = JettyWebAppDocument.type.getDocumentElementName().getNamespaceURI();
    private final JspServletInfoProviderListener jspServletInfoProviderListener = new JspServletInfoProviderListener();

    public JettyModuleBuilder(@ParamAttribute(name = "defaultEnvironment") Environment defaultEnvironment,
                              @ParamAttribute(name = "defaultSessionTimeoutSeconds") Integer defaultSessionTimeoutSeconds,
                              @ParamAttribute(name = "jettyContainerObjectName") AbstractNameQuery jettyContainerName,
                              @ParamAttribute(name = "defaultWebApp") WebAppInfo defaultWebApp,
                              @ParamReference(name = "PojoWebServiceTemplate", namingType = NameFactory.SERVLET_WEB_SERVICE_TEMPLATE) Object pojoWebServiceTemplate,
                              @ParamReference(name = "WebServiceBuilder", namingType = NameFactory.MODULE_BUILDER) Collection<WebServiceBuilder> webServiceBuilder,
                              @ParamReference(name = "ClusteringBuilders", namingType = NameFactory.MODULE_BUILDER) Collection<NamespaceDrivenBuilder> clusteringBuilders,
                              @ParamReference(name = "ServiceBuilders", namingType = NameFactory.MODULE_BUILDER) Collection<NamespaceDrivenBuilder> serviceBuilders,
                              @ParamReference(name = "NamingBuilders", namingType = NameFactory.MODULE_BUILDER) NamingBuilder namingBuilders,
                              @ParamReference(name = "ModuleBuilderExtensions", namingType = NameFactory.MODULE_BUILDER) Collection<ModuleBuilderExtension> moduleBuilderExtensions,
                              @ParamReference(name = "ResourceEnvironmentSetter", namingType = NameFactory.MODULE_BUILDER) ResourceEnvironmentSetter resourceEnvironmentSetter,
                              @ParamSpecial(type = SpecialAttributeType.kernel) Kernel kernel,
                              @ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext) throws GBeanNotFoundException, DeploymentException {
        super(kernel, serviceBuilders, namingBuilders, resourceEnvironmentSetter, webServiceBuilder, moduleBuilderExtensions, bundleContext);
        this.defaultEnvironment = defaultEnvironment;
        this.defaultSessionTimeoutMinutes = (defaultSessionTimeoutSeconds == null) ? 30 * 60 : defaultSessionTimeoutSeconds;
        this.jettyContainerObjectName = jettyContainerName;
        this.webAppInfoFactory = new StandardWebAppInfoFactory(defaultWebApp, null);
        this.clusteringBuilders = new NamespaceDrivenBuilderCollection(clusteringBuilders);//, GerClusteringDocument.type.getDocumentElementName());
    }

    public void doStart() throws Exception {
        XmlBeansUtil.registerNamespaceUpdates(NAMESPACE_UPDATES);
        SchemaConversionUtils.registerNamespaceConversions(GERONIMO_SCHEMA_CONVERSIONS);
        kernel.getLifecycleMonitor().addLifecycleListener(jspServletInfoProviderListener, new AbstractNameQuery(JspServletInfoProvider.class.getName()));
    }

    public void doStop() {
        XmlBeansUtil.unregisterNamespaceUpdates(NAMESPACE_UPDATES);
        kernel.getLifecycleMonitor().removeLifecycleListener(jspServletInfoProviderListener);
        //TODO not yet implemented
//        SchemaConversionUtils.unregisterNamespaceConversions(GERONIMO_SCHEMA_CONVERSIONS);
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

        URL specDDUrl;
        try {
            specDDUrl = BundleUtils.getEntry(bundle, "WEB-INF/web.xml");
        } catch (MalformedURLException e) {
            log.warn("MalformedURLException when getting entry:" + "WEB-INF/web.xml" + " from bundle " + bundle.getSymbolicName(), e);
            specDDUrl = null;
        }
        
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
        JettyWebAppType jettyWebApp = getJettyWebApp(null, deployable, standAlone, targetPath, webApp);

        EnvironmentType environmentType = jettyWebApp.getEnvironment();
        Environment environment = EnvironmentBuilder.buildEnvironment(environmentType, defaultEnvironment);

        if (webApp.getDistributable().size() == 1) {
            clusteringBuilders.buildEnvironment(jettyWebApp, environment);
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

        WebModule module = new WebModule(standAlone, moduleName, name, environment, deployable, targetPath, webApp, jettyWebApp, specDD, contextPath, JETTY_NAMESPACE, shareJndi(null), null);
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
        JettyWebAppType jettyWebApp = getJettyWebApp(plan, deployable, standAlone, targetPath, webApp);
        contextRoot = getContextRoot(jettyWebApp, contextRoot, webApp, standAlone, moduleFile, targetPath);

        EnvironmentType environmentType = jettyWebApp.getEnvironment();
        Environment environment = EnvironmentBuilder.buildEnvironment(environmentType, defaultEnvironment);

        if (webApp.getDistributable().size() == 1) {
            clusteringBuilders.buildEnvironment(jettyWebApp, environment);
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

        WebModule module = new WebModule(standAlone, moduleName, name, environment, deployable, targetPath, webApp, jettyWebApp, specDD, contextRoot, JETTY_NAMESPACE, shareJndi(parentModule), parentModule);
        for (ModuleBuilderExtension mbe : moduleBuilderExtensions) {
            mbe.createModule(module, plan, moduleFile, targetPath, specDDUrl, environment, contextRoot, earName, naming, idBuilder);
        }
        return module;
    }

    String getContextRoot(JettyWebAppType jettyWebApp, String contextRoot, WebApp webApp, boolean standAlone, JarFile moduleFile, String targetPath) {
        if (jettyWebApp.isSetContextRoot()) {
            contextRoot = jettyWebApp.getContextRoot();
        } else if (contextRoot == null || contextRoot.trim().equals("")) {
            contextRoot = determineDefaultContextRoot(webApp, standAlone, moduleFile, targetPath);
        }

        contextRoot = contextRoot.trim();
        return contextRoot;
    }

    JettyWebAppType getJettyWebApp(Object plan, Deployable deployable, boolean standAlone, String targetPath, WebApp webApp) throws DeploymentException {
        XmlObject rawPlan = null;
        try {
            // load the geronimo-web.xml from either the supplied plan or from the earFile
            try {
                if (plan instanceof XmlObject) {
                    rawPlan = (XmlObject) plan;
                } else {
                    if (plan != null) {
                        rawPlan = XmlBeansUtil.parse(((File) plan).toURI().toURL(), getClass().getClassLoader());
                    } else {
                        URL path = deployable.getResource("WEB-INF/geronimo-web.xml");
                        if (path == null) {
                            path = deployable.getResource("WEB-INF/geronimo-jetty.xml");
                        }
                        if (path == null) {
                            log.warn("Web application " + targetPath + " does not contain a WEB-INF/geronimo-web.xml deployment plan.  This may or may not be a problem, depending on whether you have things like resource references that need to be resolved.  You can also give the deployer a separate deployment plan file on the command line.");
                        } else {
                            rawPlan = XmlBeansUtil.parse(path, getClass().getClassLoader());
                        }
                    }
                }
            } catch (IOException e) {
                log.warn("Failed to load geronimo-web.xml", e);
            }

            JettyWebAppType jettyWebApp;
            if (rawPlan != null) {
                XmlObject webPlan = new GenericToSpecificPlanConverter(GerJettyDocument.type.getDocumentElementName().getNamespaceURI(),
                        JettyWebAppDocument.type.getDocumentElementName().getNamespaceURI(), "jetty").convertToSpecificPlan(rawPlan);
                jettyWebApp = (JettyWebAppType) webPlan.changeType(JettyWebAppType.type);
                XmlBeansUtil.validateDD(jettyWebApp);
            } else {
                jettyWebApp = createDefaultPlan();
            }
            return jettyWebApp;
        } catch (XmlException e) {
            throw new DeploymentException("xml problem for web app " + targetPath, e);
        }
    }

    private JettyWebAppType createDefaultPlan() {
        return JettyWebAppType.Factory.newInstance();
    }

    @Override
    protected void postInitContext(EARContext earContext, WebModule webModule, Bundle bundle) throws DeploymentException {
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
    protected void preInitContext(EARContext earContext, WebModule webModule, Bundle bundle) throws DeploymentException {
        JettyWebAppType gerWebApp = (JettyWebAppType) webModule.getVendorDD();
        boolean hasSecurityRealmName = gerWebApp.isSetSecurityRealmName();
        webModule.getEarContext().getGeneralData().put(WEB_MODULE_HAS_SECURITY_REALM, hasSecurityRealmName);
    }

    @Override
    public void addGBeans(EARContext earContext, Module module, Bundle bundle, Collection repository) throws DeploymentException {
        EARContext moduleContext = module.getEarContext();
        AbstractName moduleName = module.getModuleName();
        WebModule webModule = (WebModule) module;

        WebApp webApp = webModule.getSpecDD();
        JettyWebAppType jettyWebApp = (JettyWebAppType) webModule.getVendorDD();
        GBeanData webModuleData = new GBeanData(moduleName, WebAppContextWrapper.class);

        configureBasicWebModuleAttributes(webApp, jettyWebApp, moduleContext, earContext, webModule, webModuleData);

        // unsharableResources, applicationManagedSecurityResources
        GBeanResourceEnvironmentBuilder rebuilder = new GBeanResourceEnvironmentBuilder(webModuleData);
        //N.B. use earContext not moduleContext
        //TODO fix this for javaee 5 !!!
        resourceEnvironmentSetter.setResourceEnvironment(rebuilder, webApp.getResourceRef(), jettyWebApp.getResourceRefArray());
        try {
            moduleContext.addGBean(webModuleData);

            // configure WebAppContextManager with right priority so that it starts last
            AbstractName contextManagerName = earContext.getNaming().createChildName(moduleName, "WebAppContextManager", NameFactory.SERVICE_MODULE);
            GBeanData contextManagerGBean = new GBeanData(contextManagerName, WebAppContextManager.class);
            contextManagerGBean.setPriority(100);
            contextManagerGBean.setReferencePattern("webApp", moduleName);
            moduleContext.addGBean(contextManagerGBean);

            // configure hosts and virtual-hosts
            configureHosts(earContext, jettyWebApp, webModuleData);

            String contextPath = webModule.getContextRoot();
            if (contextPath == null) {
                throw new DeploymentException("null contextPath");
            }
            if (!contextPath.startsWith("/")) {
                contextPath = "/" + contextPath;
            }
            webModuleData.setAttribute("contextPath", contextPath);

            if (jettyWebApp.isSetWorkDir()) {
                String workDir = jettyWebApp.getWorkDir();
                webModuleData.setAttribute("workDir", workDir);
            }

            if (jettyWebApp.isSetWebContainer()) {
                AbstractNameQuery webContainerName = ENCConfigBuilder.getGBeanQuery(GBeanInfoBuilder.DEFAULT_J2EE_TYPE, jettyWebApp.getWebContainer());
                webModuleData.setReferencePattern("JettyContainer", webContainerName);
            } else {
                webModuleData.setReferencePattern("JettyContainer", jettyContainerObjectName);
            }
            //stuff that jetty used to do
            webModuleData.setAttribute("displayName", webApp.getDisplayName());

            WebAppInfoBuilder webAppInfoBuilder = new WebAppInfoBuilder(webApp, webAppInfoFactory);
            WebAppInfo webAppInfo = webAppInfoBuilder.build();

            webModuleData.setAttribute("webAppInfo", webAppInfo);

            webModule.getSharedContext().put(WebModule.WEB_APP_INFO, webAppInfoBuilder);

            //TODO merge from default web app
            if (webAppInfo.sessionConfig != null) {
                if (webAppInfo.sessionConfig.sessionTimeoutMinutes == null && defaultSessionTimeoutMinutes != -1) {
                    webAppInfo.sessionConfig.sessionTimeoutMinutes = defaultSessionTimeoutMinutes;
                }
            }

            if (webAppInfo.distributable) {
                clusteringBuilders.build(jettyWebApp, earContext, moduleContext);
                if (webModuleData.getReferencePatterns(WebAppContextWrapper.GBEAN_REF_SESSION_HANDLER_FACTORY) == null) {
                    log.warn("No clustering builders configured: app will not be clustered");
                    configureNoClustering(moduleContext, webModuleData);
                }
            } else {
                configureNoClustering(moduleContext, webModuleData);
            }

            // configure login configs.
            configureAuthentication(module, webAppInfo.loginConfig, jettyWebApp, webModuleData);

            if (jettyWebApp.isSetSecurityRealmName()) {
                configureSecurityRealm(earContext, webApp, jettyWebApp, bundle, webModuleData);
            }

            //See Jetty-386, GERONIMO-3738
            if (jettyWebApp.getCompactPath()) {
                webModuleData.setAttribute("compactPath", Boolean.TRUE);
            }

            /*LinkedHashSet<Module<?, ?>> submodules = module.getModules();
            for (Module<?, ?> subModule: submodules) {
                if (subModule.getSharedContext().get(SharedOwbContext.class) != null) {
                    GBeanData data = (GBeanData) subModule.getSharedContext().get(SharedOwbContext.class);
                    AbstractName name = data.getAbstractName();
                    webModuleData.setReferencePattern("SharedOwbContext", name);
                }
            }*/
            //Save Deployment Attributes
            Map<String, Object> deploymentAttributes = new HashMap<String, Object>();
            deploymentAttributes.put(WebApplicationConstants.META_COMPLETE, webApp.isMetadataComplete());
            deploymentAttributes.put(WebApplicationConstants.SCHEMA_VERSION, INITIAL_WEB_XML_SCHEMA_VERSION.get(webModule.getEarContext().getGeneralData()));
            deploymentAttributes.put(WebApplicationConstants.ORDERED_LIBS, AbstractWebModuleBuilder.ORDERED_LIBS.get(webModule.getEarContext().getGeneralData()));
            deploymentAttributes.put(WebApplicationConstants.SERVLET_CONTAINER_INITIALIZERS, AbstractWebModuleBuilder.SERVLET_CONTAINER_INITIALIZERS.get(webModule.getEarContext().getGeneralData()));
            webModuleData.setAttribute("deploymentAttributes", deploymentAttributes);

            //TODO this may definitely not be the best place for this!
            for (ModuleBuilderExtension mbe : moduleBuilderExtensions) {
                mbe.addGBeans(earContext, module, bundle, repository);
            }

            if (jettyWebApp.isSetSecurityRealmName()) {
                webModuleData.setReferencePattern("applicationPolicyConfigurationManager", EARContext.JACC_MANAGER_NAME_KEY.get(earContext.getGeneralData()));
            }

            //not truly metadata complete until MBEs have run
            if (!webApp.isMetadataComplete()) {
                webApp.setMetadataComplete(true);
                if (INITIAL_WEB_XML_SCHEMA_VERSION.get(webModule.getEarContext().getGeneralData()) >= 2.5f) {
                    String specDeploymentPlan = getSpecDDAsString(webModule);
                    module.setOriginalSpecDD(specDeploymentPlan);
                    earContext.addFile(new URI("./WEB-INF/web.xml"), specDeploymentPlan);
                }
            }
            webModuleData.setAttribute("deploymentDescriptor", module.getOriginalSpecDD());

            module.addAsChildConfiguration();
        } catch (DeploymentException de) {
            throw de;
        } catch (Exception e) {
            throw new DeploymentException("Unable to initialize webapp GBean for " + module.getName(), e);
        }

    }

    private void configureNoClustering(EARContext moduleContext, GBeanData webModuleData) throws GBeanAlreadyExistsException {
//        AbstractName name = moduleContext.getNaming().createChildName(moduleContext.getModuleName(),
//                "DefaultWebApplicationHandlerFactory",
//                NameFactory.GERONIMO_SERVICE);
//        GBeanData beanData = new GBeanData(name, DefaultWebApplicationHandlerFactory.class);
//        webModuleData.setReferencePattern(WebAppContextWrapper.GBEAN_REF_WEB_APPLICATION_HANDLER_FACTORY, name);
//        moduleContext.addGBean(beanData);
    }

    private void configureSecurityRealm(EARContext earContext, WebApp webApp, JettyWebAppType jettyWebApp, Bundle bundle, GBeanData webModuleData) throws DeploymentException {
        AbstractName moduleName = webModuleData.getAbstractName();
        if (earContext.getSecurityConfiguration() == null) {
            throw new DeploymentException(
                    "You have specified a <security-realm-name> for the webapp " + moduleName + " but no <security> configuration (role mapping) is supplied in the Geronimo plan for the web application (or the Geronimo plan for the EAR if the web app is in an EAR)");
        }
        String securityRealmName = jettyWebApp.getSecurityRealmName().trim();
        webModuleData.setAttribute("securityRealmName", securityRealmName);
        webModuleData.setReferencePattern("RunAsSource", GeronimoSecurityBuilderImpl.ROLE_MAPPER_DATA_NAME.get(earContext.getGeneralData()));

        /**
         * TODO - go back to commented version when possible.
         */
        String policyContextID = moduleName.toString().replaceAll("[, :]", "_");
        //String policyContextID = webModuleName.getCanonicalName();
        webModuleData.setAttribute("policyContextID", policyContextID);


        ComponentPermissions componentPermissions = buildSpecSecurityConfig(earContext, webApp, bundle);
        earContext.addSecurityContext(policyContextID, componentPermissions);

    }

    private void configureAuthentication(Module module, LoginConfigInfo loginConfigInfo, JettyWebAppType jettyWebApp, GBeanData webModuleData) throws DeploymentException, GBeanAlreadyExistsException {
        EARContext moduleContext = module.getEarContext();
        JettyAuthenticationType authType = jettyWebApp.getAuthentication();
        if (loginConfigInfo != null || authType != null || jettyWebApp.isSetSecurityRealmName()) {
            AbstractName factoryName = moduleContext.getNaming().createChildName(module.getModuleName(), "securityHandlerFactory", GBeanInfoBuilder.DEFAULT_J2EE_TYPE);


            if (authType != null) {
                GBeanData securityFactoryData = new GBeanData(factoryName, AuthConfigProviderHandlerFactory.class);
                securityFactoryData.setAttribute("messageLayer", MESSAGE_LAYER);
                String contextPath = (String) webModuleData.getAttribute("contextPath");
                securityFactoryData.setAttribute("appContext", "server " + contextPath);
                configureConfigurationFactory(jettyWebApp, null, securityFactoryData);
                moduleContext.addGBean(securityFactoryData);
                configureLocalJaspicProvider(new JettyAuthenticationWrapper(authType), contextPath, module, securityFactoryData);
                webModuleData.setReferencePattern("SecurityHandlerFactory", factoryName);
                //otherwise rely on pre-configured jaspi
            } else {
                if ((loginConfigInfo != null && loginConfigInfo.realmName != null) || jettyWebApp.isSetSecurityRealmName()) {
                    GBeanData securityFactoryData = new GBeanData(factoryName, JettySecurityHandlerFactory.class);
                    configureConfigurationFactory(jettyWebApp, loginConfigInfo, securityFactoryData);
                    BuiltInAuthMethod auth = BuiltInAuthMethod.NONE;
                    if (loginConfigInfo != null) {
                        if (loginConfigInfo.authMethod != null) {
                            String authMethod = loginConfigInfo.authMethod.trim();
                            auth = BuiltInAuthMethod.getValueOf(authMethod);

                            if (auth == BuiltInAuthMethod.BASIC) {
                                securityFactoryData.setAttribute("realmName", loginConfigInfo.realmName);
                            } else if (auth == BuiltInAuthMethod.DIGEST) {
                                securityFactoryData.setAttribute("realmName", loginConfigInfo.realmName);
                            } else if (auth == BuiltInAuthMethod.FORM) {
                                if (loginConfigInfo.formLoginPage != null) {
                                    securityFactoryData.setAttribute("loginPage", loginConfigInfo.formLoginPage);
                                    securityFactoryData.setAttribute("errorPage", loginConfigInfo.formErrorPage);
                                }
                            } else if (auth == BuiltInAuthMethod.CLIENTCERT) {
                                //nothing to do
                            } else {
                                throw new DeploymentException("unrecognized auth method, use jaspi to configure: " + authMethod);
                            }

                        } else {
                            throw new DeploymentException("No auth method configured and no jaspi configured");
                        }
                        if (loginConfigInfo.realmName != null) {
                            webModuleData.setAttribute("realmName", loginConfigInfo.realmName);
                        }
                    }
                    securityFactoryData.setAttribute("authMethod", auth);
                    moduleContext.addGBean(securityFactoryData);
                    webModuleData.setReferencePattern("SecurityHandlerFactory", factoryName);
                } else {
                    log.warn("partial security info but no realm to authenticate against");
                }
            }
        }
    }

    private class JspServletInfoProviderListener extends LifecycleAdapter {

        @Override
        public void running(AbstractName abstractName) {
            try {
                JspServletInfoProvider jspServletInfoProvider = (JspServletInfoProvider) kernel.getGBean(abstractName);
                ServletInfo jspServletInfo = webAppInfoFactory.copy(jspServletInfoProvider.getJspServletInfo());
                jspServletInfo.servletMappings.clear();
                webAppInfoFactory.setJspServletInfo(jspServletInfo);
            } catch (Exception e) {
            }
        }

        @Override
        public void stopped(AbstractName abstractName) {
            webAppInfoFactory.setJspServletInfo(null);
        }
    }

    private static class JettyAuthenticationWrapper implements AuthenticationWrapper {
        private final JettyAuthenticationType authType;

        private JettyAuthenticationWrapper(JettyAuthenticationType authType) {
            this.authType = authType;
        }

        public JaspiConfigProviderType getConfigProvider() {
            return authType.getConfigProvider();
        }

        public boolean isSetConfigProvider() {
            return authType.isSetConfigProvider();
        }

        public JaspiServerAuthConfigType getServerAuthConfig() {
            return authType.getServerAuthConfig();
        }

        public boolean isSetServerAuthConfig() {
            return authType.isSetServerAuthConfig();
        }

        public JaspiServerAuthContextType getServerAuthContext() {
            return authType.getServerAuthContext();
        }

        public boolean isSetServerAuthContext() {
            return authType.isSetServerAuthContext();
        }

        public JaspiAuthModuleType getServerAuthModule() {
            return authType.getServerAuthModule();
        }

        public boolean isSetServerAuthModule() {
            return authType.isSetServerAuthModule();
        }
    }

    private void configureConfigurationFactory(JettyWebAppType jettyWebApp, LoginConfigInfo loginConfigInfo, GBeanData securityFactoryData) {
        String securityRealmName;
        if (jettyWebApp.isSetSecurityRealmName()) {
            securityRealmName = jettyWebApp.getSecurityRealmName().trim();
        } else {
            securityRealmName = loginConfigInfo.realmName;
        }
        AbstractNameQuery configurationFactoryName = new AbstractNameQuery(null, Collections.singletonMap("name", securityRealmName), ConfigurationFactory.class.getName());
        securityFactoryData.setReferencePattern("ConfigurationFactory", configurationFactoryName);
    }

    private void configureHosts(EARContext earContext, JettyWebAppType jettyWebApp, GBeanData webModuleData) throws GBeanAlreadyExistsException {
        String[] hosts = jettyWebApp.getHostArray();
        for (int i = 0; i < hosts.length; i++) {
            hosts[i] = hosts[i].trim();
        }
        String[] virtualHosts = jettyWebApp.getVirtualHostArray();
        for (int i = 0; i < virtualHosts.length; i++) {
            virtualHosts[i] = virtualHosts[i].trim();
        }
        if (hosts.length > 0 || virtualHosts.length > 0) {
            //use name same as module
            AbstractName hostName = earContext.getNaming().createChildName(webModuleData.getAbstractName(), "Host", "Host");
            GBeanData hostData = new GBeanData(hostName, Host.class);
            hostData.setAttribute("hosts", hosts);
            hostData.setAttribute("virtualHosts", virtualHosts);
            earContext.addGBean(hostData);
            webModuleData.setReferencePattern("Host", hostName);
        }
    }

    public String getSchemaNamespace() {
        return JETTY_NAMESPACE;
    }

}
