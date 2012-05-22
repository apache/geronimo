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

import static java.lang.Boolean.TRUE;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

import javax.servlet.Servlet;

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
import org.apache.geronimo.kernel.lifecycle.LifecycleAdapter;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.util.FileUtils;
import org.apache.geronimo.kernel.util.IOUtils;
import org.apache.geronimo.kernel.util.JarUtils;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.naming.deployment.GBeanResourceEnvironmentBuilder;
import org.apache.geronimo.naming.deployment.ResourceEnvironmentSetter;
import org.apache.geronimo.security.deployment.GeronimoSecurityBuilderImpl;
import org.apache.geronimo.security.jaas.ConfigurationFactory;
import org.apache.geronimo.tomcat.LifecycleListenerGBean;
import org.apache.geronimo.tomcat.ManagerGBean;
import org.apache.geronimo.tomcat.RealmGBean;
import org.apache.geronimo.tomcat.TomcatWebAppContext;
import org.apache.geronimo.tomcat.ValveGBean;
import org.apache.geronimo.tomcat.cluster.CatalinaClusterGBean;
import org.apache.geronimo.tomcat.util.SecurityHolder;
import org.apache.geronimo.web.WebApplicationConstants;
import org.apache.geronimo.web.deployment.GenericToSpecificPlanConverter;
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
import org.apache.geronimo.xbeans.geronimo.web.tomcat.TomcatAuthenticationType;
import org.apache.geronimo.xbeans.geronimo.web.tomcat.TomcatContextType;
import org.apache.geronimo.xbeans.geronimo.web.tomcat.TomcatParameterType;
import org.apache.geronimo.xbeans.geronimo.web.tomcat.TomcatWebAppDocument;
import org.apache.geronimo.xbeans.geronimo.web.tomcat.TomcatWebAppType;
import org.apache.geronimo.xbeans.geronimo.web.tomcat.config.GerTomcatDocument;
import org.apache.openejb.jee.JavaeeSchema;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.WebApp;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;



/**
 * @version $Rev:385659 $ $Date$
 */
@GBean(j2eeType = NameFactory.MODULE_BUILDER)
public class TomcatModuleBuilder extends AbstractWebModuleBuilder implements GBeanLifecycle {

    private static final Logger log = LoggerFactory.getLogger(TomcatModuleBuilder.class);

    private static final String TOMCAT_NAMESPACE = TomcatWebAppDocument.type.getDocumentElementName().getNamespaceURI();
    private static final Map<String, String> NAMESPACE_UPDATES = new HashMap<String, String>();
    private static final Set<String> INGORED_ELEMENT_NAMES = new HashSet<String>();
    private static final Set<String> INGORED_CONTEXT_ATTRIBUTE_NAMES = new HashSet<String>();
   // private static final Map<String, ElementConverter> GERONIMO_SCHEMA_CONVERSIONS = new HashMap<String, ElementConverter>();
    static {
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/web", "http://geronimo.apache.org/xml/ns/j2ee/web-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web-1.1", "http://geronimo.apache.org/xml/ns/j2ee/web-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web-1.2", "http://geronimo.apache.org/xml/ns/j2ee/web-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web-2.0", "http://geronimo.apache.org/xml/ns/j2ee/web-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/web/tomcat", "http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-1.1", "http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-1.2", "http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-2.0", "http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-2.0.1");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/web/tomcat/config", "http://geronimo.apache.org/xml/ns/j2ee/web/tomcat/config-1.0");

        INGORED_ELEMENT_NAMES.add("context-priority-classloader");
        INGORED_ELEMENT_NAMES.add("configId");
        INGORED_ELEMENT_NAMES.add("parentId");

        INGORED_CONTEXT_ATTRIBUTE_NAMES.add("className".toLowerCase());
        INGORED_CONTEXT_ATTRIBUTE_NAMES.add("xmlNamespaceAware".toLowerCase());
        INGORED_CONTEXT_ATTRIBUTE_NAMES.add("processTlds".toLowerCase());
        INGORED_CONTEXT_ATTRIBUTE_NAMES.add("unpackWAR".toLowerCase());
        INGORED_CONTEXT_ATTRIBUTE_NAMES.add("xmlValidation".toLowerCase());
        INGORED_CONTEXT_ATTRIBUTE_NAMES.add("path");
        INGORED_CONTEXT_ATTRIBUTE_NAMES.add("useNaming".toLowerCase());
        INGORED_CONTEXT_ATTRIBUTE_NAMES.add("javaVMs".toLowerCase());
        INGORED_CONTEXT_ATTRIBUTE_NAMES.add("server");
        INGORED_CONTEXT_ATTRIBUTE_NAMES.add("j2EEApplication".toLowerCase());
        INGORED_CONTEXT_ATTRIBUTE_NAMES.add("j2EEServer".toLowerCase());
        INGORED_CONTEXT_ATTRIBUTE_NAMES.add("path");
        INGORED_CONTEXT_ATTRIBUTE_NAMES.add("delegate");
        INGORED_CONTEXT_ATTRIBUTE_NAMES.add("docBase".toLowerCase());
    }

    private final Environment defaultEnvironment;
    private final AbstractNameQuery tomcatContainerName;
    protected final NamespaceDrivenBuilderCollection clusteringBuilders;

    public static final String GBEAN_REF_CLUSTERING_BUILDERS = "ClusteringBuilders";
    private final StandardWebAppInfoFactory webAppInfoFactory;
    private final JspServletInfoProviderListener jspServletInfoProviderListener = new JspServletInfoProviderListener();

    public TomcatModuleBuilder(@ParamAttribute(name = "defaultEnvironment") Environment defaultEnvironment,
            @ParamAttribute(name = "tomcatContainerName") AbstractNameQuery tomcatContainerName,
            @ParamAttribute(name = "defaultWebApp") WebAppInfo defaultWebApp,
            @ParamReference(name="WebServiceBuilder", namingType = NameFactory.MODULE_BUILDER) Collection<WebServiceBuilder> webServiceBuilder,
            @ParamReference(name="ServiceBuilders", namingType = NameFactory.MODULE_BUILDER)Collection<NamespaceDrivenBuilder> serviceBuilders,
            @ParamReference(name="NamingBuilders", namingType = NameFactory.MODULE_BUILDER)NamingBuilder namingBuilders,
            @ParamReference(name= GBEAN_REF_CLUSTERING_BUILDERS, namingType = NameFactory.MODULE_BUILDER)Collection<NamespaceDrivenBuilder> clusteringBuilders,
            @ParamReference(name="ModuleBuilderExtensions", namingType = NameFactory.MODULE_BUILDER)Collection<ModuleBuilderExtension> moduleBuilderExtensions,
            @ParamReference(name="ResourceEnvironmentSetter", namingType = NameFactory.MODULE_BUILDER)ResourceEnvironmentSetter resourceEnvironmentSetter,
            @ParamSpecial(type = SpecialAttributeType.kernel) Kernel kernel,
            @ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext) {
        super(kernel, serviceBuilders, namingBuilders, resourceEnvironmentSetter, webServiceBuilder, moduleBuilderExtensions, bundleContext);
        this.defaultEnvironment = defaultEnvironment;
        this.clusteringBuilders = new NamespaceDrivenBuilderCollection(clusteringBuilders);
        this.tomcatContainerName = tomcatContainerName;
        this.webAppInfoFactory = new StandardWebAppInfoFactory(defaultWebApp, null);

    }

    public void doStart() throws Exception {
        XmlBeansUtil.registerNamespaceUpdates(NAMESPACE_UPDATES);
        kernel.getLifecycleMonitor().addLifecycleListener(jspServletInfoProviderListener, new AbstractNameQuery(JspServletInfoProvider.class.getName()));
    }

    public void doStop() {
        XmlBeansUtil.unregisterNamespaceUpdates(NAMESPACE_UPDATES);
        kernel.getLifecycleMonitor().removeLifecycleListener(jspServletInfoProviderListener);
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
            InputStream in = null;
            try {
                specDD = JarUtils.readAll(specDDUrl);
                in = specDDUrl.openStream();
                webApp = (WebApp) JaxbJavaee.unmarshalJavaee(WebApp.class, in);
            } catch (Exception e) {
                throw new DeploymentException("Error reading web.xml for " + bundle.getSymbolicName(), e);
            } finally {
                IOUtils.close(in);
            }
        }

        String targetPath = ".";
        boolean standAlone = true;

        Deployable deployable = new DeployableBundle(bundle);
        // parse vendor dd
        TomcatWebAppType tomcatWebApp = getTomcatWebApp(null, deployable, standAlone, targetPath, webApp);

        EnvironmentType environmentType = tomcatWebApp.getEnvironment();
        Environment environment = EnvironmentBuilder.buildEnvironment(environmentType, defaultEnvironment);

        if (webApp.getDistributable().size() == 1) {
            clusteringBuilders.buildEnvironment(tomcatWebApp, environment);
        }

        idBuilder.resolve(environment, bundle.getSymbolicName(), "wab");

        AbstractName earName = naming.createRootName(environment.getConfigId(), NameFactory.NULL, NameFactory.J2EE_APPLICATION);
        AbstractName moduleName = naming.createChildName(earName, environment.getConfigId().toString(), NameFactory.WEB_MODULE);

        String name = webApp.getModuleName();
        if (name == null) {
            name = bundle.getSymbolicName();
        }

        WebModule module = new WebModule(standAlone, moduleName, name, environment, deployable, targetPath, webApp, tomcatWebApp, specDD, contextPath, TOMCAT_NAMESPACE, shareJndi(null), null);
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


            // firstly validate the DD xml file, if it is defined by a schema.
            if (identifySpecDDSchemaVersion(specDD) >= 2.4f){
                InputStream in = null;
                try {
                    in = specDDUrl.openStream();
                    JaxbJavaee.validateJavaee(JavaeeSchema.WEB_APP_3_0, in);
                } catch (Exception e) {
                    throw new DeploymentException("Error validate web.xml for " + targetPath, e);
                } finally {
                    IOUtils.close(in);
                }
            }

            // we found web.xml, if it won't parse that's an error.
            InputStream in = null;
            try {
                in = specDDUrl.openStream();
                webApp = (WebApp) JaxbJavaee.unmarshalJavaee(WebApp.class, in);
            } catch (Exception e) {
                // Output the target path in the error to make it clearer to the user which webapp
                // has the problem. The targetPath is used, as moduleFile may have an unhelpful
                // value such as C:\geronimo-1.1\var\temp\geronimo-deploymentUtil22826.tmpdir
                throw new DeploymentException("Error unmarshal web.xml for " + targetPath, e);
            } finally {
                IOUtils.close(in);
            }

        } catch (Exception e) {
            if (e instanceof DeploymentException) {
                throw new DeploymentException(e);
            }

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
        TomcatWebAppType tomcatWebApp = getTomcatWebApp(plan, deployable, standAlone, targetPath, webApp);
        contextRoot = getContextRoot(tomcatWebApp, contextRoot, webApp, standAlone, moduleFile, targetPath);

        EnvironmentType environmentType = tomcatWebApp.getEnvironment();
        Environment environment = EnvironmentBuilder.buildEnvironment(environmentType, defaultEnvironment);

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

        WebModule module = new WebModule(standAlone, moduleName, name, environment, deployable, targetPath, webApp, tomcatWebApp, specDD, contextRoot, TOMCAT_NAMESPACE, shareJndi(parentModule), parentModule);
        for (ModuleBuilderExtension mbe : moduleBuilderExtensions) {
            mbe.createModule(module, plan, moduleFile, targetPath, specDDUrl, environment, contextRoot, earName, naming, idBuilder);
        }
        return module;
    }

    private String getContextRoot(TomcatWebAppType tomcatWebApp, String contextRoot, WebApp webApp, boolean standAlone, JarFile moduleFile, String targetPath) {
        //If we have a context root, override everything
        if (tomcatWebApp.isSetContextRoot()) {
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


    TomcatWebAppType getTomcatWebApp(Object plan, Deployable deployable, boolean standAlone, String targetPath, WebApp webApp) throws DeploymentException {
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
                            path = deployable.getResource("WEB-INF/geronimo-tomcat.xml");
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

            TomcatWebAppType tomcatWebApp;
            if (rawPlan != null) {
                XmlObject webPlan = new GenericToSpecificPlanConverter(GerTomcatDocument.type.getDocumentElementName().getNamespaceURI(),
                        TomcatWebAppDocument.type.getDocumentElementName().getNamespaceURI(), "tomcat").convertToSpecificPlan(rawPlan);
                tomcatWebApp = (TomcatWebAppType) webPlan.changeType(TomcatWebAppType.type);
                XmlBeansUtil.validateDD(tomcatWebApp, INGORED_ELEMENT_NAMES);
            } else {
                tomcatWebApp = createDefaultPlan();
            }
            return tomcatWebApp;
        } catch (XmlException e) {
            throw new DeploymentException("xml problem for web app " + targetPath, e);
        }
    }

    private TomcatWebAppType createDefaultPlan() {
        return TomcatWebAppType.Factory.newInstance();
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
        TomcatWebAppType gerWebApp = (TomcatWebAppType) webModule.getVendorDD();
        boolean hasSecurityRealmName = gerWebApp.isSetSecurityRealmName();
        webModule.getEarContext().getGeneralData().put(WEB_MODULE_HAS_SECURITY_REALM, hasSecurityRealmName);
    }

    public void addGBeans(EARContext earContext, Module module, Bundle bundle, Collection repository) throws DeploymentException {
        EARContext moduleContext = module.getEarContext();
        Bundle webBundle = moduleContext.getDeploymentBundle();
        AbstractName moduleName = module.getModuleName();
        WebModule webModule = (WebModule) module;

        WebApp webApp = webModule.getSpecDD();

        TomcatWebAppType tomcatWebApp = (TomcatWebAppType) webModule.getVendorDD();

        GBeanData webModuleData = new GBeanData(moduleName, TomcatWebAppContext.class);
        configureBasicWebModuleAttributes(webApp, tomcatWebApp, moduleContext, earContext, webModule, webModuleData);
        String contextPath = webModule.getContextRoot();
        if (!contextPath.startsWith("/")) {
            contextPath = "/" + contextPath;
        }
        try {
            module.addGBean(webModuleData);
            Map<String, String> contextAttributes = new HashMap<String, String>();
            webModuleData.setAttribute("contextPath", contextPath);
            // unsharableResources, applicationManagedSecurityResources
            GBeanResourceEnvironmentBuilder rebuilder = new GBeanResourceEnvironmentBuilder(webModuleData);
            //N.B. use earContext not moduleContext
            resourceEnvironmentSetter.setResourceEnvironment(rebuilder, webApp.getResourceRef(), tomcatWebApp.getResourceRefArray());

            if (tomcatWebApp.isSetWebContainer()) {
                AbstractNameQuery webContainerName = ENCConfigBuilder.getGBeanQuery(GBeanInfoBuilder.DEFAULT_J2EE_TYPE, tomcatWebApp.getWebContainer());
                webModuleData.setReferencePattern("Container", webContainerName);
            } else {
                webModuleData.setReferencePattern("Container", tomcatContainerName);
            }

            // Process the Tomcat container-config elements
            if (tomcatWebApp.isSetHost()) {
                String virtualServer = tomcatWebApp.getHost().trim();
                webModuleData.setAttribute("virtualServer", virtualServer);
            }

            if (tomcatWebApp.isSetCrossContext()) {
                contextAttributes.put("crossContext", "true");
            }

            if (tomcatWebApp.isSetWorkDir()) {
                String workDir = tomcatWebApp.getWorkDir();
                contextAttributes.put("workDir", workDir);
            }

            if (tomcatWebApp.isSetDisableCookies()) {
                contextAttributes.put("cookies", "false");
            }

            if (tomcatWebApp.isSetTomcatRealm()) {
                String tomcatRealm = tomcatWebApp.getTomcatRealm().trim();
                AbstractName realmName = earContext.getNaming().createChildName(moduleName, tomcatRealm, RealmGBean.GBEAN_INFO.getJ2eeType());
                webModuleData.setReferencePattern("TomcatRealm", realmName);
            }
            if (tomcatWebApp.isSetValveChain()) {
                String valveChain = tomcatWebApp.getValveChain().trim();
                AbstractName valveName = earContext.getNaming().createChildName(moduleName, valveChain, ValveGBean.J2EE_TYPE);
                webModuleData.setReferencePattern("TomcatValveChain", valveName);
            }

            if (tomcatWebApp.isSetListenerChain()) {
                String listenerChain = tomcatWebApp.getListenerChain().trim();
                AbstractName listenerName = earContext.getNaming().createChildName(moduleName, listenerChain, LifecycleListenerGBean.J2EE_TYPE);
                webModuleData.setReferencePattern("LifecycleListenerChain", listenerName);
            }

            if (tomcatWebApp.isSetCluster()) {
                String cluster = tomcatWebApp.getCluster().trim();
                AbstractName clusterName = earContext.getNaming().createChildName(moduleName, cluster, CatalinaClusterGBean.J2EE_TYPE);
                webModuleData.setReferencePattern("Cluster", clusterName);
            }

            if (tomcatWebApp.isSetManager()) {
                String manager = tomcatWebApp.getManager().trim();
                AbstractName managerName = earContext.getNaming().createChildName(moduleName, manager, ManagerGBean.J2EE_TYPE);
                webModuleData.setReferencePattern(TomcatWebAppContext.GBEAN_REF_MANAGER_RETRIEVER, managerName);
            }

            Boolean distributable = !webApp.getDistributable().isEmpty();
            if (TRUE == distributable) {
                clusteringBuilders.build(tomcatWebApp, earContext, moduleContext);
                if (null == webModuleData.getReferencePatterns(TomcatWebAppContext.GBEAN_REF_CLUSTERED_VALVE_RETRIEVER)) {
                    log.warn("No clustering builders configured: app will not be clustered");
                }
            }

            WebAppInfoBuilder webAppInfoBuilder = new WebAppInfoBuilder(webApp, webAppInfoFactory);
            WebAppInfo webAppInfo = webAppInfoBuilder.build();

            webModuleData.setAttribute("webAppInfo", webAppInfo);

            webModule.getSharedContext().put(WebModule.WEB_APP_INFO, webAppInfoBuilder);

            //Add context attributes and parameters
            if (tomcatWebApp.isSetContext()) {
                TomcatContextType context = tomcatWebApp.getContext();
                NamedNodeMap namedNodeMap = context.getDomNode().getAttributes();
                for (int i = 0; i < namedNodeMap.getLength(); i++) {
                    Node node = namedNodeMap.item(i);
                    String attributeName = node.getNodeName();
                    if (INGORED_CONTEXT_ATTRIBUTE_NAMES.contains(attributeName.toLowerCase())) {
                        if (log.isWarnEnabled()) {
                            log.warn("Context attribute " + attributeName + " in the geronimo-web.xml is ignored, as it is not support or Geronimo has already configured it");
                        }
                        continue;
                    }
                    if (contextAttributes.containsKey(attributeName)) {
                        if (log.isWarnEnabled()) {
                            log.warn("Context attribute " + attributeName
                                    + " on the context element in geronimo-web.xml is ignored, as it has been explicitly configured with other elements in the geronimo-web.xml file");
                        }
                        continue;
                    }
                    contextAttributes.put(node.getNodeName(), node.getNodeValue());
                }
                for (TomcatParameterType parameterType : context.getParameterArray()) {
                    if (webAppInfo.contextParams.containsKey(parameterType.getName()) && !parameterType.getOverride()) {
                        if (log.isWarnEnabled()) {
                            log.warn("Context parameter from geronimo-web.xml is ignored, as a same name context paramter " + parameterType.getName() + " = "
                                    + webAppInfo.contextParams.get(parameterType.getName()) + " in web.xml, configure override with true to make the value take effect.");
                        }
                        continue;
                    }
                    webAppInfo.contextParams.put(parameterType.getName(), parameterType.getValue());
                }
            }
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

            if (tomcatWebApp.isSetSecurityRealmName()) {
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
                if (tomcatWebApp.isSetAuthentication()) {
                    AuthenticationWrapper authType = new TomcatAuthenticationWrapper(tomcatWebApp.getAuthentication());
                    configureLocalJaspicProvider(authType, contextPath, module, webModuleData);
                }
            }

            //Save Deployment Attributes
            Map<String, Object> deploymentAttributes = new HashMap<String, Object>();
            deploymentAttributes.put(WebApplicationConstants.META_COMPLETE, webApp.isMetadataComplete());
            deploymentAttributes.put(WebApplicationConstants.SCHEMA_VERSION, INITIAL_WEB_XML_SCHEMA_VERSION.get(webModule.getEarContext().getGeneralData()));
            deploymentAttributes.put(WebApplicationConstants.ORDERED_LIBS, AbstractWebModuleBuilder.ORDERED_LIBS.get(webModule.getEarContext().getGeneralData()));
            deploymentAttributes.put(WebApplicationConstants.SERVLET_CONTAINER_INITIALIZERS, AbstractWebModuleBuilder.SERVLET_CONTAINER_INITIALIZERS.get(webModule.getEarContext().getGeneralData()));
            webModuleData.setAttribute("deploymentAttributes", deploymentAttributes);

            //listeners added directly to the StandardContext will get loaded by the tomcat classloader, not the app classloader!
            //TODO this may definitely not be the best place for this!
            for (ModuleBuilderExtension mbe : moduleBuilderExtensions) {
                mbe.addGBeans(earContext, module, bundle, repository);
            }
            //commented out code shares OWB context between ejb submodules and this web module.
//            LinkedHashSet<Module<?, ?>> submodules = module.getModules();
//            for (Module<?, ?> subModule: submodules) {
//                if (subModule.getSharedContext().get(SharedOwbContext.class) != null) {
//                    GBeanData data = (GBeanData) subModule.getSharedContext().get(SharedOwbContext.class);
//                    AbstractName name = data.getAbstractName();
//                    webModuleData.setReferencePattern("SharedOwbContext", name);
//                }
//            }

            if(tomcatWebApp.isSetSecurityRealmName()) {
                webModuleData.setReferencePattern("applicationPolicyConfigurationManager", EARContext.JACC_MANAGER_NAME_KEY.get(earContext.getGeneralData()));
            }
            //not truly metadata complete until MBEs have run
            if (INITIAL_WEB_XML_SCHEMA_VERSION.get(module.getEarContext().getGeneralData()) >= 2.5f) {
                webApp.setMetadataComplete(true);
                String specDeploymentPlan = getSpecDDAsString(webModule);
                module.setOriginalSpecDD(specDeploymentPlan);
                earContext.addFile(module.getTargetPathURI().resolve("WEB-INF/web.xml"), specDeploymentPlan);
            }
            webModuleData.setAttribute("deploymentDescriptor", module.getOriginalSpecDD());
            module.flushGBeansToContext();
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

    private static class TomcatAuthenticationWrapper implements AuthenticationWrapper {
        private final TomcatAuthenticationType authType;

        private TomcatAuthenticationWrapper(TomcatAuthenticationType authType) {
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



}
