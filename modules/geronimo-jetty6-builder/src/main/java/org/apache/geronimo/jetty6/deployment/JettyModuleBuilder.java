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

package org.apache.geronimo.jetty6.deployment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarFile;

import javax.management.ObjectName;
import javax.servlet.Servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilderCollection;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.annotation.AnnotatedWebApp;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.deployment.WebModule;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.annotation.Injection;
import org.apache.geronimo.j2ee.annotation.Holder;
import org.apache.geronimo.jetty6.Host;
import org.apache.geronimo.jetty6.JettyDefaultServletHolder;
import org.apache.geronimo.jetty6.JettyFilterHolder;
import org.apache.geronimo.jetty6.JettyFilterMapping;
import org.apache.geronimo.jetty6.JettyServletHolder;
import org.apache.geronimo.jetty6.JettyWebAppContext;
import org.apache.geronimo.jetty6.NonAuthenticator;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.naming.deployment.GBeanResourceEnvironmentBuilder;
import org.apache.geronimo.naming.deployment.ResourceEnvironmentSetter;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.security.deployment.SecurityConfiguration;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.web.deployment.GenericToSpecificPlanConverter;
import org.apache.geronimo.web25.deployment.AbstractWebModuleBuilder;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerClusteringDocument;
import org.apache.geronimo.xbeans.geronimo.web.jetty.JettyWebAppDocument;
import org.apache.geronimo.xbeans.geronimo.web.jetty.JettyWebAppType;
import org.apache.geronimo.xbeans.geronimo.web.jetty.config.GerJettyDocument;
import org.apache.geronimo.xbeans.javaee.DispatcherType;
import org.apache.geronimo.xbeans.javaee.ErrorPageType;
import org.apache.geronimo.xbeans.javaee.FilterMappingType;
import org.apache.geronimo.xbeans.javaee.FilterType;
import org.apache.geronimo.xbeans.javaee.FormLoginConfigType;
import org.apache.geronimo.xbeans.javaee.JspConfigType;
import org.apache.geronimo.xbeans.javaee.ListenerType;
import org.apache.geronimo.xbeans.javaee.LocaleEncodingMappingListType;
import org.apache.geronimo.xbeans.javaee.LocaleEncodingMappingType;
import org.apache.geronimo.xbeans.javaee.LoginConfigType;
import org.apache.geronimo.xbeans.javaee.MimeMappingType;
import org.apache.geronimo.xbeans.javaee.ParamValueType;
import org.apache.geronimo.xbeans.javaee.ServletMappingType;
import org.apache.geronimo.xbeans.javaee.ServletType;
import org.apache.geronimo.xbeans.javaee.TaglibType;
import org.apache.geronimo.xbeans.javaee.UrlPatternType;
import org.apache.geronimo.xbeans.javaee.WebAppDocument;
import org.apache.geronimo.xbeans.javaee.WebAppType;
import org.apache.geronimo.xbeans.javaee.WelcomeFileListType;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.mortbay.jetty.security.BasicAuthenticator;
import org.mortbay.jetty.security.ClientCertAuthenticator;
import org.mortbay.jetty.security.DigestAuthenticator;
import org.mortbay.jetty.security.FormAuthenticator;


/**
 * @version $Rev:385659 $ $Date$
 */
public class JettyModuleBuilder extends AbstractWebModuleBuilder {
    private final static Log log = LogFactory.getLog(JettyModuleBuilder.class);
    private final Environment defaultEnvironment;
    private final AbstractNameQuery jettyContainerObjectName;
    private final Collection defaultServlets;
    private final Collection defaultFilters;
    private final Collection defaultFilterMappings;
    private final GBeanData pojoWebServiceTemplate;

    protected final NamespaceDrivenBuilderCollection clusteringBuilders;

    private final List defaultWelcomeFiles;
    private final Integer defaultSessionTimeoutSeconds;

    private static final String JETTY_NAMESPACE = JettyWebAppDocument.type.getDocumentElementName().getNamespaceURI();
    private String jspServletClassName;

    public JettyModuleBuilder(Environment defaultEnvironment,
            Integer defaultSessionTimeoutSeconds,
            List defaultWelcomeFiles,
            AbstractNameQuery jettyContainerName,
            String jspServletClassName, Collection defaultServlets,
            Collection defaultFilters,
            Collection defaultFilterMappings,
            Object pojoWebServiceTemplate,
            Collection webServiceBuilder,
            Collection clusteringBuilders,
            Collection securityBuilders,
            Collection serviceBuilders,
            NamingBuilder namingBuilders,
            ResourceEnvironmentSetter resourceEnvironmentSetter,
            Kernel kernel) throws GBeanNotFoundException {
        super(kernel, securityBuilders, serviceBuilders, namingBuilders, resourceEnvironmentSetter, webServiceBuilder);
        this.defaultEnvironment = defaultEnvironment;
        this.defaultSessionTimeoutSeconds = (defaultSessionTimeoutSeconds == null) ? new Integer(30 * 60) : defaultSessionTimeoutSeconds;
        this.jettyContainerObjectName = jettyContainerName;
        this.jspServletClassName = jspServletClassName;
        this.defaultServlets = defaultServlets;
        this.defaultFilters = defaultFilters;
        this.defaultFilterMappings = defaultFilterMappings;
        this.pojoWebServiceTemplate = getGBeanData(kernel, pojoWebServiceTemplate);
        this.clusteringBuilders = new NamespaceDrivenBuilderCollection(clusteringBuilders, GerClusteringDocument.type.getDocumentElementName());

        //todo locale mappings

        this.defaultWelcomeFiles = defaultWelcomeFiles;
    }

    private static GBeanData getGBeanData(Kernel kernel, Object template) throws GBeanNotFoundException {
        if (template == null) {
            return null;
        }
        AbstractName templateName = kernel.getAbstractNameFor(template);
        return kernel.getGBeanData(templateName);
    }

    protected Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, boolean standAlone, String contextRoot, AbstractName earName, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        assert moduleFile != null: "moduleFile is null";
        assert targetPath != null: "targetPath is null";
        assert !targetPath.endsWith("/"): "targetPath must not end with a '/'";

        // parse the spec dd
        String specDD = null;
        WebAppType webApp = null;
        try {
            if (specDDUrl == null) {
                specDDUrl = DeploymentUtil.createJarURL(moduleFile, "WEB-INF/web.xml");
            }

            // read in the entire specDD as a string, we need this for getDeploymentDescriptor
            // on the J2ee management object
            specDD = DeploymentUtil.readAll(specDDUrl);

            // we found web.xml, if it won't parse that's an error.
            XmlObject parsed = XmlBeansUtil.parse(specDD);
            WebAppDocument webAppDoc = convertToServletSchema(parsed);
            webApp = webAppDoc.getWebApp();
            check(webApp);
        } catch (XmlException e) {
            // Output the target path in the error to make it clearer to the user which webapp
            // has the problem.  The targetPath is used, as moduleFile may have an unhelpful
            // value such as C:\geronimo-1.1\var\temp\geronimo-deploymentUtil22826.tmpdir
            throw new DeploymentException("Error parsing web.xml for " + targetPath, e);
        } catch (Exception e) {
            if(!moduleFile.getName().endsWith(".war")) {
                //not for us
                return null;
            }
            //else ignore as jee5 allows optional spec dd for .war's
        }

        if (webApp == null) {
            webApp = WebAppType.Factory.newInstance();
        }
        
        // parse vendor dd
        JettyWebAppType jettyWebApp = getJettyWebApp(plan, moduleFile, standAlone, targetPath, webApp);
        if (contextRoot == null || contextRoot.trim().equals("")) {
            if (jettyWebApp.isSetContextRoot()) {
                contextRoot = jettyWebApp.getContextRoot();
            } else {
                contextRoot = determineDefaultContextRoot(webApp, standAlone, moduleFile, targetPath);
            }
        }

        contextRoot = contextRoot.trim();

        EnvironmentType environmentType = jettyWebApp.getEnvironment();
        Environment environment = EnvironmentBuilder.buildEnvironment(environmentType, defaultEnvironment);

        Boolean distributable = webApp.getDistributableArray().length == 1 ? Boolean.TRUE : Boolean.FALSE;
        if (Boolean.TRUE == distributable) {
            clusteringBuilders.buildEnvironment(jettyWebApp, environment);
        }

        // Note: logic elsewhere depends on the default artifact ID being the file name less extension (ConfigIDExtractor)
        String warName = new File(moduleFile.getName()).getName();
        if (warName.lastIndexOf('.') > -1) {
            warName = warName.substring(0, warName.lastIndexOf('.'));
        }
        idBuilder.resolve(environment, warName, "war");

        AbstractName moduleName;
        if (earName == null) {
            earName = naming.createRootName(environment.getConfigId(), NameFactory.NULL, NameFactory.J2EE_APPLICATION);
            moduleName = naming.createChildName(earName, environment.getConfigId().toString(), NameFactory.WEB_MODULE);
        } else {
            moduleName = naming.createChildName(earName, targetPath, NameFactory.WEB_MODULE);
        }

        // Create the AnnotatedApp interface for the WebModule
        AnnotatedWebApp annotatedWebApp = new AnnotatedWebApp(webApp);

        return new WebModule(standAlone, moduleName, environment, moduleFile, targetPath, webApp, jettyWebApp, specDD, contextRoot, new HashMap(), JETTY_NAMESPACE, annotatedWebApp);
    }

    JettyWebAppType getJettyWebApp(Object plan, JarFile moduleFile, boolean standAlone, String targetPath, WebAppType webApp) throws DeploymentException {
        XmlObject rawPlan = null;
        try {
            // load the geronimo-web.xml from either the supplied plan or from the earFile
            try {
                if (plan instanceof XmlObject) {
                    rawPlan = (XmlObject) plan;
                } else {
                    if (plan != null) {
                        rawPlan = XmlBeansUtil.parse(((File) plan).toURL(), getClass().getClassLoader());
                    } else {
                        URL path = DeploymentUtil.createJarURL(moduleFile, "WEB-INF/geronimo-web.xml");
                        try {
                            rawPlan = XmlBeansUtil.parse(path, getClass().getClassLoader());
                        } catch (FileNotFoundException e) {
                            path = DeploymentUtil.createJarURL(moduleFile, "WEB-INF/geronimo-jetty.xml");
                            try {
                                rawPlan = XmlBeansUtil.parse(path, getClass().getClassLoader());
                            } catch (FileNotFoundException e1) {
                                log.warn("Web application " + targetPath + " does not contain a WEB-INF/geronimo-web.xml deployment plan.  This may or may not be a problem, depending on whether you have things like resource references that need to be resolved.  You can also give the deployer a separate deployment plan file on the command line.");
                            }
                        }
                    }
                }
            } catch (IOException e) {
                log.warn(e);
            }

            JettyWebAppType jettyWebApp;
            if (rawPlan != null) {
                XmlObject webPlan = new GenericToSpecificPlanConverter(GerJettyDocument.type.getDocumentElementName().getNamespaceURI(),
                        JettyWebAppDocument.type.getDocumentElementName().getNamespaceURI(), "jetty").convertToSpecificPlan(rawPlan);
                jettyWebApp = (JettyWebAppType) webPlan.changeType(JettyWebAppType.type);
                XmlBeansUtil.validateDD(jettyWebApp);
            } else {
                String defaultContextRoot = determineDefaultContextRoot(webApp, standAlone, moduleFile, targetPath);
                jettyWebApp = createDefaultPlan(defaultContextRoot);
            }
            return jettyWebApp;
        } catch (XmlException e) {
            throw new DeploymentException("xml problem for web app " + targetPath, e);
        }
    }

    private JettyWebAppType createDefaultPlan(String contextRoot) {
        JettyWebAppType jettyWebApp = JettyWebAppType.Factory.newInstance();
        jettyWebApp.setContextRoot(contextRoot);
        return jettyWebApp;
    }

    public void initContext(EARContext earContext, Module module, ClassLoader cl) throws DeploymentException {
        WebAppType webApp = (WebAppType) module.getSpecDD();
        JettyWebAppType gerWebApp = (JettyWebAppType) module.getVendorDD();
        boolean hasSecurityRealmName = gerWebApp.isSetSecurityRealmName();
        buildSubstitutionGroups(gerWebApp, hasSecurityRealmName, module, earContext);
    }

    public void addGBeans(EARContext earContext, Module module, ClassLoader cl, Collection repository) throws DeploymentException {
        EARContext moduleContext = module.getEarContext();
        ClassLoader moduleClassLoader = moduleContext.getClassLoader();
        AbstractName moduleName = moduleContext.getModuleName();
        WebModule webModule = (WebModule) module;

        WebAppType webApp = (WebAppType) webModule.getSpecDD();
        JettyWebAppType jettyWebApp = (JettyWebAppType) webModule.getVendorDD();

        //N.B. we use the ear context which has all the gbeans we could possibly be looking up from this ear.
        Map buildingContext = new HashMap();
        buildingContext.put(NamingBuilder.GBEAN_NAME_KEY, moduleName);
        Configuration earConfiguration = earContext.getConfiguration();

        if (!webApp.getMetadataComplete()) {
            // Create a classfinder and populate it for the naming builder(s). The absence of a
            // classFinder in the module will convey whether metadata-complete is set (or not)
            webModule.setClassFinder(createWebAppClassFinder(webApp, webModule));
        }

        getNamingBuilders().buildNaming(webApp, jettyWebApp, earConfiguration, earConfiguration, (Module) webModule, buildingContext);

        if (!webApp.getMetadataComplete()) {
            webApp.setMetadataComplete(true);
            module.setOriginalSpecDD(module.getSpecDD().toString());
        }

        Map compContext = NamingBuilder.JNDI_KEY.get(buildingContext);
        Map<String, List<Injection>> injections = NamingBuilder.INJECTION_KEY.get(buildingContext);
        Map<String, Holder> holders = new HashMap<String, Holder> ();
        //TODO naming builders should return the holder map
        for (Map.Entry<String, List<Injection>> entry: injections.entrySet()) {
            holders.put(entry.getKey(), new Holder(entry.getValue(), null, null));
        }

        GBeanData webModuleData = new GBeanData(moduleName, JettyWebAppContext.GBEAN_INFO);
        try {
            moduleContext.addGBean(webModuleData);
            if (moduleContext.getServerName() != null) {
                webModuleData.setReferencePattern("J2EEServer", moduleContext.getServerName());
            }
            if (!module.isStandAlone()) {
                webModuleData.setReferencePattern("J2EEApplication", earContext.getModuleName());
            }

            webModuleData.setAttribute("injections", holders);

            webModuleData.setAttribute("deploymentDescriptor", module.getOriginalSpecDD());
            Set securityRoles = collectRoleNames(webApp);
            Map rolePermissions = new HashMap();

            // configure hosts and virtual-hosts
            configureHosts(earContext, jettyWebApp, webModuleData);

            //Add dependencies on managed connection factories and ejbs in this app
            //This is overkill, but allows for people not using java:comp context (even though we don't support it)
            //and sidesteps the problem of circular references between ejbs.
            Set dependencies = findGBeanDependencies(earContext);
            webModuleData.addDependencies(dependencies);

            webModuleData.setAttribute("componentContext", compContext);
            //classpath may have been augmented with enhanced classes
//            webModuleData.setAttribute("webClassPath", webModule.getWebClasspath());
            // unsharableResources, applicationManagedSecurityResources
            GBeanResourceEnvironmentBuilder rebuilder = new GBeanResourceEnvironmentBuilder(webModuleData);
            //N.B. use earContext not moduleContext
            //TODO fix this for javaee 5 !!!
            resourceEnvironmentSetter.setResourceEnvironment(rebuilder, webApp.getResourceRefArray(), jettyWebApp.getResourceRefArray());

            String contextPath = webModule.getContextRoot();
            if (contextPath == null) {
                throw new DeploymentException("null contextPath");
            }
            if (!contextPath.startsWith("/")) {
                contextPath = "/" + contextPath;
            }
            webModuleData.setAttribute("contextPath", contextPath);

            webModuleData.setReferencePattern("TransactionManager", moduleContext.getTransactionManagerName());
            webModuleData.setReferencePattern("TrackedConnectionAssociator", moduleContext.getConnectionTrackerName());
            if (jettyWebApp.isSetWebContainer()) {
                AbstractNameQuery webContainerName = ENCConfigBuilder.getGBeanQuery(NameFactory.GERONIMO_SERVICE, jettyWebApp.getWebContainer());
                webModuleData.setReferencePattern("JettyContainer", webContainerName);
            } else {
                webModuleData.setReferencePattern("JettyContainer", jettyContainerObjectName);
            }
            //stuff that jetty6 used to do
            if (webApp.getDisplayNameArray().length > 0) {
                webModuleData.setAttribute("displayName", webApp.getDisplayNameArray()[0].getStringValue());
            }

            // configure context parameters.
            configureContextParams(webApp, webModuleData);

            // configure listeners.
            configureListeners(webApp, webModuleData);

            webModuleData.setAttribute(JettyWebAppContext.GBEAN_ATTR_SESSION_TIMEOUT,
                    (webApp.getSessionConfigArray().length == 1 && webApp.getSessionConfigArray(0).getSessionTimeout() != null) ?
                            new Integer(webApp.getSessionConfigArray(0).getSessionTimeout().getBigIntegerValue().intValue() * 60) :
                            defaultSessionTimeoutSeconds);

            Boolean distributable = webApp.getDistributableArray().length == 1 ? Boolean.TRUE : Boolean.FALSE;
            webModuleData.setAttribute("distributable", distributable);
            if (Boolean.TRUE == distributable) {
                clusteringBuilders.build(jettyWebApp, earContext, moduleContext);
                if (webModuleData.getReferencePatterns(JettyWebAppContext.GBEAN_REF_SESSION_HANDLER_FACTORY) == null)
                {
                    log.warn("No clustering builders configured: app will not be clustered");
                    configureNoClustering(moduleContext, webModuleData);
                }
            } else {
                configureNoClustering(moduleContext, webModuleData);
            }

            // configure mime mappings.
            configureMimeMappings(webApp, webModuleData);

            // configure welcome file lists.
            configureWelcomeFileLists(webApp, webModuleData);

            // configure local encoding mapping lists.
            configureLocalEncodingMappingLists(webApp, webModuleData);

            // configure error pages.
            configureErrorPages(webApp, webModuleData);

            // configure tag libs.
            configureTagLibs(module, webApp, webModuleData);

            // configure login configs.
            configureLoginConfigs(module, webApp, jettyWebApp, webModuleData);

            // Make sure that servlet mappings point to available servlets and never add a duplicate pattern.
            Set knownServletMappings = new HashSet();
            Map servletMappings = new HashMap();

            buildServletMappings(module, webApp, servletMappings, knownServletMappings);

            //"previous" filter mapping for linked list to keep dd's ordering.
            AbstractName previous = null;

            //add default filters
            if (defaultFilters != null) {
                previous = addDefaultFiltersGBeans(earContext, moduleContext, moduleName, previous);
            }

            //add default filtermappings
//            if (defaultFilterMappings != null) {
//                for (Iterator iterator = defaultFilterMappings.iterator(); iterator.hasNext();) {
//                    Object defaultFilterMapping = iterator.next();
//                    GBeanData filterMappingGBeanData = getGBeanData(kernel, defaultFilterMapping);
//                    String filterName = (String) filterMappingGBeanData.getAttribute("filterName");
//                    ObjectName defaultFilterMappingObjectName;
//                    if (filterMappingGBeanData.getAttribute("urlPattern") != null) {
//                        String urlPattern = (String) filterMappingGBeanData.getAttribute("urlPattern");
//                        defaultFilterMappingObjectName = NameFactory.getWebFilterMappingName(null, null, null, null, filterName, null, urlPattern, moduleName);
//                    } else {
//                        Set servletNames = filterMappingGBeanData.getReferencePatterns("Servlet");
//                        if (servletNames == null || servletNames.size() != 1) {
//                            throw new DeploymentException("Exactly one servlet name must be supplied");
//                        }
//                        ObjectName servletObjectName = (ObjectName) servletNames.iterator().next();
//                        String servletName = servletObjectName.getKeyProperty("name");
//                        defaultFilterMappingObjectName = NameFactory.getWebFilterMappingName(null, null, null, null, filterName, servletName, null, moduleName);
//                    }
//                    filterMappingGBeanData.setName(defaultFilterMappingObjectName);
//                    filterMappingGBeanData.setReferencePattern("JettyFilterMappingRegistration", webModuleName);
//                    moduleContext.addGBean(filterMappingGBeanData);
//                }
//            }

            // add filter mapping GBeans.
            addFilterMappingsGBeans(earContext, moduleContext, moduleName, webApp, previous);

            // add filter GBeans.
            addFiltersGBeans(earContext, moduleContext, moduleName, webApp);

            //add default servlets
            if (defaultServlets != null) {
                addDefaultServletsGBeans(earContext, moduleContext, moduleName, knownServletMappings);
            }

            //set up servlet gbeans.

            ServletType[] servletTypes = webApp.getServletArray();
            addServlets(moduleName, webModule, servletTypes, servletMappings, securityRoles, rolePermissions, moduleContext);

            if (jettyWebApp.isSetSecurityRealmName()) {
                configureSecurityRealm(earContext, webApp, jettyWebApp, webModuleData, securityRoles, rolePermissions);
            }
            if (!module.isStandAlone()) {
                ConfigurationData moduleConfigurationData = moduleContext.getConfigurationData();
                earContext.addChildConfiguration(module.getTargetPath(), moduleConfigurationData);
            }
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
//        GBeanData beanData = new GBeanData(name, DefaultWebApplicationHandlerFactory.GBEAN_INFO);
//        webModuleData.setReferencePattern(JettyWebAppContext.GBEAN_REF_WEB_APPLICATION_HANDLER_FACTORY, name);
//        moduleContext.addGBean(beanData);
    }

    private void configureSecurityRealm(EARContext earContext, WebAppType webApp, JettyWebAppType jettyWebApp, GBeanData webModuleData, Set securityRoles, Map rolePermissions) throws DeploymentException {
        AbstractName moduleName = webModuleData.getAbstractName();
        if (earContext.getSecurityConfiguration() == null) {
            throw new DeploymentException("You have specified a <security-realm-name> for the webapp " + moduleName + " but no <security> configuration (role mapping) is supplied in the Geronimo plan for the web application (or the Geronimo plan for the EAR if the web app is in an EAR)");
        }
        String securityRealmName = jettyWebApp.getSecurityRealmName().trim();
        webModuleData.setAttribute("securityRealmName", securityRealmName);

        /**
         * TODO - go back to commented version when possible.
         */
        String policyContextID = moduleName.toString().replaceAll("[, :]", "_");
        //String policyContextID = webModuleName.getCanonicalName();
        webModuleData.setAttribute("policyContextID", policyContextID);

        ComponentPermissions componentPermissions = buildSpecSecurityConfig(webApp, securityRoles, rolePermissions);
        webModuleData.setAttribute("excludedPermissions", componentPermissions.getExcludedPermissions());
        PermissionCollection checkedPermissions = new Permissions();
        for (Iterator iterator = rolePermissions.values().iterator(); iterator.hasNext();) {
            PermissionCollection permissionsForRole = (PermissionCollection) iterator.next();
            for (Enumeration iterator2 = permissionsForRole.elements(); iterator2.hasMoreElements();) {
                Permission permission = (Permission) iterator2.nextElement();
                checkedPermissions.add(permission);
            }
        }
        webModuleData.setAttribute("checkedPermissions", checkedPermissions);

        earContext.addSecurityContext(policyContextID, componentPermissions);
        DefaultPrincipal defaultPrincipal = ((SecurityConfiguration) earContext.getSecurityConfiguration()).getDefaultPrincipal();
        webModuleData.setAttribute("defaultPrincipal", defaultPrincipal);
    }

    private void addDefaultServletsGBeans(EARContext earContext, EARContext moduleContext, AbstractName moduleName, Set knownServletMappings) throws GBeanNotFoundException, GBeanAlreadyExistsException {
        for (Iterator iterator = defaultServlets.iterator(); iterator.hasNext();) {
            Object defaultServlet = iterator.next();
            GBeanData servletGBeanData = getGBeanData(kernel, defaultServlet);
            AbstractName defaultServletObjectName = earContext.getNaming().createChildName(moduleName, (String) servletGBeanData.getAttribute("servletName"), NameFactory.SERVLET);
            servletGBeanData.setAbstractName(defaultServletObjectName);
            servletGBeanData.setReferencePattern("JettyServletRegistration", moduleName);
            Set defaultServletMappings = new HashSet((Collection) servletGBeanData.getAttribute("servletMappings"));
            defaultServletMappings.removeAll(knownServletMappings);
            servletGBeanData.setAttribute("servletMappings", defaultServletMappings);
            moduleContext.addGBean(servletGBeanData);
        }
    }

    private void addFiltersGBeans(EARContext earContext, EARContext moduleContext, AbstractName moduleName, WebAppType webApp) throws GBeanAlreadyExistsException {
        FilterType[] filterArray = webApp.getFilterArray();
        for (int i = 0; i < filterArray.length; i++) {
            FilterType filterType = filterArray[i];
            String filterName = filterType.getFilterName().getStringValue().trim();
            AbstractName filterAbstractName = earContext.getNaming().createChildName(moduleName, filterName, NameFactory.WEB_FILTER);
            GBeanData filterData = new GBeanData(filterAbstractName, JettyFilterHolder.GBEAN_INFO);
            filterData.setAttribute("filterName", filterName);
            filterData.setAttribute("filterClass", filterType.getFilterClass().getStringValue().trim());
            Map initParams = new HashMap();
            ParamValueType[] initParamArray = filterType.getInitParamArray();
            for (int j = 0; j < initParamArray.length; j++) {
                ParamValueType paramValueType = initParamArray[j];
                initParams.put(paramValueType.getParamName().getStringValue().trim(), paramValueType.getParamValue().getStringValue().trim());
            }
            filterData.setAttribute("initParams", initParams);
            filterData.setReferencePattern("JettyServletRegistration", moduleName);
            moduleContext.addGBean(filterData);
        }
    }

    private void addFilterMappingsGBeans(EARContext earContext, EARContext moduleContext, AbstractName moduleName, WebAppType webApp, AbstractName previous) throws GBeanAlreadyExistsException {
        FilterMappingType[] filterMappingArray = webApp.getFilterMappingArray();
        for (int i = 0; i < filterMappingArray.length; i++) {
            FilterMappingType filterMappingType = filterMappingArray[i];
            String filterName = filterMappingType.getFilterName().getStringValue().trim();
            GBeanData filterMappingData = new GBeanData(JettyFilterMapping.GBEAN_INFO);
            if (previous != null) {
                filterMappingData.addDependency(previous);
            }
            filterMappingData.setReferencePattern("JettyServletRegistration", moduleName);
            AbstractName filterAbstractName = earContext.getNaming().createChildName(moduleName, filterName, NameFactory.WEB_FILTER);

            AbstractName filterMappingName = null;
            if (filterMappingType.sizeOfUrlPatternArray() > 0) {
                String[] urlPatterns = new String[filterMappingType.sizeOfUrlPatternArray()];
                for (int j = 0; j < urlPatterns.length; j++) {
                    urlPatterns[j] = filterMappingType.getUrlPatternArray(j).getStringValue().trim();
                }

                filterMappingData.setAttribute("urlPatterns", urlPatterns);
                filterMappingName = earContext.getNaming().createChildName(filterAbstractName, ObjectName.quote(Arrays.deepToString(urlPatterns)), NameFactory.URL_WEB_FILTER_MAPPING);
            }
            if (filterMappingType.sizeOfServletNameArray() > 0) {
                Set servletNameSet = new HashSet();
                for (int j = 0; j < filterMappingType.sizeOfServletNameArray(); j++) {
                    String servletName = filterMappingType.getServletNameArray(j).getStringValue().trim();
                    AbstractName abstractServletName = earContext.getNaming().createChildName(moduleName, servletName, NameFactory.SERVLET);
                    servletNameSet.add(abstractServletName);
                    filterMappingData.addDependency(abstractServletName);
                }

                filterMappingData.setReferencePatterns("Servlets", servletNameSet);
                filterMappingName = earContext.getNaming().createChildName(filterAbstractName, ObjectName.quote(Arrays.deepToString(servletNameSet.toArray())), NameFactory.SERVLET_WEB_FILTER_MAPPING);

            }
            filterMappingData.setAbstractName(filterMappingName);
            previous = filterMappingName;

            boolean request = filterMappingType.getDispatcherArray().length == 0;
            boolean forward = false;
            boolean include = false;
            boolean error = false;
            for (int j = 0; j < filterMappingType.getDispatcherArray().length; j++) {
                DispatcherType dispatcherType = filterMappingType.getDispatcherArray()[j];
                if (dispatcherType.getStringValue().equals("REQUEST")) {
                    request = true;
                } else if (dispatcherType.getStringValue().equals("FORWARD")) {
                    forward = true;
                } else if (dispatcherType.getStringValue().equals("INCLUDE")) {
                    include = true;
                } else if (dispatcherType.getStringValue().equals("ERROR")) {
                    error = true;
                }
            }
            filterMappingData.setAttribute("requestDispatch", Boolean.valueOf(request));
            filterMappingData.setAttribute("forwardDispatch", Boolean.valueOf(forward));
            filterMappingData.setAttribute("includeDispatch", Boolean.valueOf(include));
            filterMappingData.setAttribute("errorDispatch", Boolean.valueOf(error));
            filterMappingData.setReferencePattern("Filter", filterAbstractName);
            moduleContext.addGBean(filterMappingData);
        }
    }

    private AbstractName addDefaultFiltersGBeans(EARContext earContext, EARContext moduleContext, AbstractName moduleName, AbstractName previous) throws GBeanNotFoundException, GBeanAlreadyExistsException {
        for (Iterator iterator = defaultFilters.iterator(); iterator.hasNext();) {
            Object defaultFilter = iterator.next();
            GBeanData filterGBeanData = getGBeanData(kernel, defaultFilter);
            String filterName = (String) filterGBeanData.getAttribute("filterName");
            AbstractName defaultFilterAbstractName = earContext.getNaming().createChildName(moduleName, filterName, NameFactory.WEB_FILTER);
            filterGBeanData.setAbstractName(defaultFilterAbstractName);
            filterGBeanData.setReferencePattern("JettyServletRegistration", moduleName);
            moduleContext.addGBean(filterGBeanData);
            //add a mapping to /*

            GBeanData filterMappingGBeanData = new GBeanData(JettyFilterMapping.GBEAN_INFO);
            if (previous != null) {
                filterMappingGBeanData.addDependency(previous);
            }
            filterMappingGBeanData.setReferencePattern("JettyServletRegistration", moduleName);
            String urlPattern = "/*";
            filterMappingGBeanData.setAttribute("urlPattern", urlPattern);
            AbstractName filterMappingName = earContext.getNaming().createChildName(defaultFilterAbstractName, urlPattern, NameFactory.URL_WEB_FILTER_MAPPING);
            filterMappingGBeanData.setAbstractName(filterMappingName);
            previous = filterMappingName;


            filterMappingGBeanData.setAttribute("requestDispatch", Boolean.TRUE);
            filterMappingGBeanData.setAttribute("forwardDispatch", Boolean.TRUE);
            filterMappingGBeanData.setAttribute("includeDispatch", Boolean.TRUE);
            filterMappingGBeanData.setAttribute("errorDispatch", Boolean.FALSE);
            filterMappingGBeanData.setReferencePattern("Filter", defaultFilterAbstractName);
            moduleContext.addGBean(filterMappingGBeanData);
        }
        return previous;
    }

    private Map buildServletMappings(Module module, WebAppType webApp, Map servletMappings, Set knownServletMappings) throws DeploymentException {
        ServletType[] servletTypes = webApp.getServletArray();
        Set knownServlets = new HashSet();
        for (int i = 0; i < servletTypes.length; i++) {
            ServletType type = servletTypes[i];
            knownServlets.add(type.getServletName().getStringValue().trim());
        }

        ServletMappingType[] servletMappingArray = webApp.getServletMappingArray();
        for (int i = 0; i < servletMappingArray.length; i++) {
            ServletMappingType servletMappingType = servletMappingArray[i];
            String servletName = servletMappingType.getServletName().getStringValue().trim();
            if (!knownServlets.contains(servletName)) {
                throw new DeploymentException("Web app " + module.getName() +
                        " contains a servlet mapping that refers to servlet '" + servletName +
                        "' but no such servlet was found!");
            }
            UrlPatternType[] urlPatterns = servletMappingType.getUrlPatternArray();
            for (int j = 0; j < urlPatterns.length; j++) {
                String urlPattern = urlPatterns[j].getStringValue().trim();
                if (!knownServletMappings.contains(urlPattern)) {
                    knownServletMappings.add(urlPattern);
                    checkString(urlPattern);
                    Set urlsForServlet = (Set) servletMappings.get(servletName);
                    if (urlsForServlet == null) {
                        urlsForServlet = new HashSet();
                        servletMappings.put(servletName, urlsForServlet);
                    }
                    urlsForServlet.add(urlPattern);
                }
            }
        }

        return servletMappings;
    }

    private void configureLoginConfigs(Module module, WebAppType webApp, JettyWebAppType jettyWebApp, GBeanData webModuleData) throws DeploymentException {
        LoginConfigType[] loginConfigArray = webApp.getLoginConfigArray();
        if (loginConfigArray.length > 1) {
            throw new DeploymentException("Web app " + module.getName() + " cannot have more than one login-config element.  Currently has " + loginConfigArray.length + " login-config elements.");
        }
        if (loginConfigArray.length == 1) {
            LoginConfigType loginConfig = loginConfigArray[0];
            if (loginConfig.isSetAuthMethod()) {
                String authMethod = loginConfig.getAuthMethod().getStringValue();
                if ("BASIC".equals(authMethod)) {
                    webModuleData.setAttribute("authenticator", new BasicAuthenticator());
                } else if ("DIGEST".equals(authMethod)) {
                    webModuleData.setAttribute("authenticator", new DigestAuthenticator());
                } else if ("FORM".equals(authMethod)) {

                    FormAuthenticator formAuthenticator = new FormAuthenticator();
                    webModuleData.setAttribute("authenticator", formAuthenticator);
                    if (loginConfig.isSetFormLoginConfig()) {
                        FormLoginConfigType formLoginConfig = loginConfig.getFormLoginConfig();
                        formAuthenticator.setLoginPage(formLoginConfig.getFormLoginPage().getStringValue());
                        formAuthenticator.setErrorPage(formLoginConfig.getFormErrorPage().getStringValue());
                    }
                } else if ("CLIENT-CERT".equals(authMethod)) {
                    webModuleData.setAttribute("authenticator", new ClientCertAuthenticator());
                }
            }
            if (loginConfig.isSetRealmName()) {
                webModuleData.setAttribute("realmName", loginConfig.getRealmName().getStringValue());
            }

        } else if (jettyWebApp.isSetSecurityRealmName()) {
            webModuleData.setAttribute("authenticator", new NonAuthenticator());
        }
    }

    private void configureTagLibs(Module module, WebAppType webApp, GBeanData webModuleData) throws DeploymentException {
        JspConfigType[] jspConfigArray = webApp.getJspConfigArray();
        if (jspConfigArray.length > 1) {
            throw new DeploymentException("Web app " + module.getName() + " cannot have more than one jsp-config element.  Currently has " + jspConfigArray.length + " jsp-config elements.");
        }
        Map tagLibMap = new HashMap();
        for (int i = 0; i < jspConfigArray.length; i++) {
            TaglibType[] tagLibArray = jspConfigArray[i].getTaglibArray();
            for (int j = 0; j < tagLibArray.length; j++) {
                TaglibType taglib = tagLibArray[j];
                tagLibMap.put(taglib.getTaglibUri().getStringValue().trim(), taglib.getTaglibLocation().getStringValue().trim());
            }
        }
        webModuleData.setAttribute("tagLibMap", tagLibMap);
    }

    private void configureErrorPages(WebAppType webApp, GBeanData webModuleData) {
        ErrorPageType[] errorPageArray = webApp.getErrorPageArray();
        Map errorPageMap = new HashMap();
        for (int i = 0; i < errorPageArray.length; i++) {
            ErrorPageType errorPageType = errorPageArray[i];
            if (errorPageType.isSetErrorCode()) {
                errorPageMap.put(errorPageType.getErrorCode().getStringValue(), errorPageType.getLocation().getStringValue());
            } else {
                errorPageMap.put(errorPageType.getExceptionType().getStringValue(), errorPageType.getLocation().getStringValue());
            }
        }
        webModuleData.setAttribute("errorPages", errorPageMap);
    }

    private void configureLocalEncodingMappingLists(WebAppType webApp, GBeanData webModuleData) {
        LocaleEncodingMappingListType[] localeEncodingMappingListArray = webApp.getLocaleEncodingMappingListArray();
        Map localeEncodingMappingMap = new HashMap();
        for (int i = 0; i < localeEncodingMappingListArray.length; i++) {
            LocaleEncodingMappingType[] localeEncodingMappingArray = localeEncodingMappingListArray[i].getLocaleEncodingMappingArray();
            for (int j = 0; j < localeEncodingMappingArray.length; j++) {
                LocaleEncodingMappingType localeEncodingMapping = localeEncodingMappingArray[j];
                localeEncodingMappingMap.put(localeEncodingMapping.getLocale(), localeEncodingMapping.getEncoding());
            }
        }
        webModuleData.setAttribute("localeEncodingMapping", localeEncodingMappingMap);
    }

    private void configureWelcomeFileLists(WebAppType webApp, GBeanData webModuleData) {
        WelcomeFileListType[] welcomeFileArray = webApp.getWelcomeFileListArray();
        List welcomeFiles;
        if (welcomeFileArray.length > 0) {
            welcomeFiles = new ArrayList();
            for (int i = 0; i < welcomeFileArray.length; i++) {
                String[] welcomeFileListType = welcomeFileArray[i].getWelcomeFileArray();
                for (int j = 0; j < welcomeFileListType.length; j++) {
                    String welcomeFile = welcomeFileListType[j].trim();
                    welcomeFiles.add(welcomeFile);
                }
            }
        } else {
            welcomeFiles = new ArrayList(defaultWelcomeFiles);
        }
        webModuleData.setAttribute("welcomeFiles", welcomeFiles.toArray(new String[welcomeFiles.size()]));
    }

    private void configureMimeMappings(WebAppType webApp, GBeanData webModuleData) {
        MimeMappingType[] mimeMappingArray = webApp.getMimeMappingArray();
        Map mimeMappingMap = new HashMap();
        for (int i = 0; i < mimeMappingArray.length; i++) {
            MimeMappingType mimeMappingType = mimeMappingArray[i];
            mimeMappingMap.put(mimeMappingType.getExtension().getStringValue(), mimeMappingType.getMimeType().getStringValue());
        }
        webModuleData.setAttribute("mimeMap", mimeMappingMap);
    }

    private void configureListeners(WebAppType webApp, GBeanData webModuleData) {
        ListenerType[] listenerArray = webApp.getListenerArray();
        Collection listeners = new ArrayList();
        for (int i = 0; i < listenerArray.length; i++) {
            ListenerType listenerType = listenerArray[i];
            listeners.add(listenerType.getListenerClass().getStringValue());
        }
        webModuleData.setAttribute("listenerClassNames", listeners);
    }

    private void configureContextParams(WebAppType webApp, GBeanData webModuleData) {
        ParamValueType[] contextParamArray = webApp.getContextParamArray();
        Map contextParams = new HashMap();
        for (int i = 0; i < contextParamArray.length; i++) {
            ParamValueType contextParam = contextParamArray[i];
            contextParams.put(contextParam.getParamName().getStringValue().trim(), contextParam.getParamValue().getStringValue().trim());
        }
        webModuleData.setAttribute("contextParamMap", contextParams);
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
            GBeanData hostData = new GBeanData(hostName, Host.GBEAN_INFO);
            hostData.setAttribute("hosts", hosts);
            hostData.setAttribute("virtualHosts", virtualHosts);
            earContext.addGBean(hostData);
            webModuleData.setReferencePattern("Host", hostName);
        }
    }

    public String getSchemaNamespace() {
        return JETTY_NAMESPACE;
    }

    /**
     * Adds the provided servlets, taking into account the load-on-startup ordering.
     *
     * @param webModuleName   an <code>ObjectName</code> value
     * @param module          a <code>Module</code> value
     * @param servletTypes    a <code>ServletType[]</code> value, contains the <code>servlet</code> entries from <code>web.xml</code>.
     * @param servletMappings a <code>Map</code> value
     * @param securityRoles   a <code>Set</code> value
     * @param rolePermissions a <code>Map</code> value
     * @param moduleContext      an <code>EARContext</code> value
     * @throws DeploymentException if an error occurs
     */
    private void addServlets(AbstractName webModuleName,
            Module module,
            ServletType[] servletTypes,
            Map servletMappings,
            Set securityRoles,
            Map rolePermissions,
            EARContext moduleContext) throws DeploymentException {

        // this TreeSet will order the ServletTypes based on whether
        // they have a load-on-startup element and what its value is
        TreeSet loadOrder = new TreeSet(new StartupOrderComparator());

        // add all of the servlets to the sorted set
        for (int i = 0; i < servletTypes.length; i++) {
            loadOrder.add(servletTypes[i]);
        }

        // now that they're sorted, read them in order and add them to
        // the context.  we'll use a GBean reference to enforce the
        // load order.  Each servlet GBean (except the first) has a
        // reference to the previous GBean.  The kernel will ensure
        // that each "previous" GBean is running before it starts any
        // other GBeans that reference it.  See also
        // o.a.g.jetty6.JettyFilterMapping which provided the example
        // of how to do this.
        // http://issues.apache.org/jira/browse/GERONIMO-645
        AbstractName previousServlet = null;
        for (Iterator servlets = loadOrder.iterator(); servlets.hasNext();) {
            ServletType servletType = (ServletType) servlets.next();
            previousServlet = addServlet(webModuleName, module, previousServlet, servletType, servletMappings, securityRoles, rolePermissions, moduleContext);
        }

        // JACC v1.0 secion B.19
        addUnmappedJSPPermissions(securityRoles, rolePermissions);
    }

    /**
     * @param webModuleName
     * @param module
     * @param previousServlet
     * @param servletType
     * @param servletMappings
     * @param securityRoles
     * @param rolePermissions
     * @param moduleContext
     * @return AbstractName of servlet gbean added
     * @throws DeploymentException
     */
    private AbstractName addServlet(AbstractName webModuleName,
            Module module,
            AbstractName previousServlet,
            ServletType servletType,
            Map servletMappings,
            Set securityRoles,
            Map rolePermissions,
            EARContext moduleContext) throws DeploymentException {
        String servletName = servletType.getServletName().getStringValue().trim();
        AbstractName servletAbstractName = moduleContext.getNaming().createChildName(webModuleName, servletName, NameFactory.SERVLET);
        GBeanData servletData;
        Map initParams = new HashMap();
        if (servletType.isSetServletClass()) {
            ClassLoader webClassLoader = moduleContext.getClassLoader();
            String servletClassName = servletType.getServletClass().getStringValue().trim();
            Class servletClass;
            try {
                servletClass = webClassLoader.loadClass(servletClassName);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Could not load servlet class " + servletClassName, e); // TODO identify web app in message
            }
            Class baseServletClass;
            try {
                baseServletClass = webClassLoader.loadClass(Servlet.class.getName());
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Could not load javax.servlet.Servlet in web classloader", e); // TODO identify web app in message
            }
            if (baseServletClass.isAssignableFrom(servletClass)) {
                servletData = new GBeanData(servletAbstractName, JettyServletHolder.GBEAN_INFO);
                servletData.setAttribute("servletClass", servletClassName);
            } else {
                servletData = new GBeanData(pojoWebServiceTemplate);
                servletData.setAbstractName(servletAbstractName);
                //let the web service builder deal with configuring the gbean with the web service stack
//                Object portInfo = portMap.get(servletName);
//                if (portInfo == null) {
//                    throw new DeploymentException("No web service deployment info for servlet name " + servletName); // TODO identify web app in message
//                }
                boolean configured = false;
                for (Iterator iterator = webServiceBuilder.iterator(); iterator.hasNext();) {
                    WebServiceBuilder serviceBuilder = (WebServiceBuilder) iterator.next();
                    if (serviceBuilder.configurePOJO(servletData, servletName, module, servletClassName, moduleContext)) {
                        configured = true;
                        break;
                    }
                }
                if (!configured) {
                    throw new DeploymentException("POJO web service: " + servletName + " not configured by any web service builder");
                }
            }
        } else if (servletType.isSetJspFile()) {
            servletData = new GBeanData(servletAbstractName, JettyServletHolder.GBEAN_INFO);
            servletData.setAttribute("jspFile", servletType.getJspFile().getStringValue().trim());
            //TODO MAKE THIS CONFIGURABLE!!! Jetty uses the servlet mapping set up from the default-web.xml
            servletData.setAttribute("servletClass", jspServletClassName);
            initParams.put("development", "false");
        } else {
            throw new DeploymentException("Neither servlet class nor jsp file is set for " + servletName); // TODO identify web app in message
        }

        // link to previous servlet, if there is one, so that we
        // preserve the <load-on-startup> ordering.
        // http://issues.apache.org/jira/browse/GERONIMO-645
        if (null != previousServlet) {
            servletData.addDependency(previousServlet);
        }

        //TODO in init param setter, add classpath if jspFile is not null.
        servletData.setReferencePattern("JettyServletRegistration", webModuleName);
        servletData.setAttribute("servletName", servletName);
        ParamValueType[] initParamArray = servletType.getInitParamArray();
        for (int j = 0; j < initParamArray.length; j++) {
            ParamValueType paramValueType = initParamArray[j];
            initParams.put(paramValueType.getParamName().getStringValue().trim(), paramValueType.getParamValue().getStringValue().trim());
        }
        servletData.setAttribute("initParams", initParams);
        if (servletType.isSetLoadOnStartup()) {
            Integer loadOnStartup = new Integer(servletType.xgetLoadOnStartup().getStringValue());
            servletData.setAttribute("loadOnStartup", loadOnStartup);
        }

        Set mappings = (Set) servletMappings.get(servletName);
        servletData.setAttribute("servletMappings", mappings == null ? Collections.EMPTY_SET : mappings);

        //run-as
        if (servletType.isSetRunAs()) {
            servletData.setAttribute("runAsRole", servletType.getRunAs().getRoleName().getStringValue().trim());
        }

        processRoleRefPermissions(servletType, securityRoles, rolePermissions);

        try {
            moduleContext.addGBean(servletData);
        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException("Could not add servlet gbean to context", e); // TODO identify web app in message
        }
        return servletAbstractName;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(JettyModuleBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true, true);
        infoBuilder.addAttribute("defaultSessionTimeoutSeconds", Integer.class, true, true);
        infoBuilder.addAttribute("defaultWelcomeFiles", List.class, true, true);
        infoBuilder.addAttribute("jettyContainerObjectName", AbstractNameQuery.class, true, true);
        infoBuilder.addAttribute("jspServletClassName", String.class, true, true);
        infoBuilder.addReference("DefaultServlets", JettyDefaultServletHolder.class, NameFactory.SERVLET_TEMPLATE);
        infoBuilder.addReference("DefaultFilters", Object.class);
        infoBuilder.addReference("DefaultFilterMappings", Object.class);
        infoBuilder.addReference("PojoWebServiceTemplate", Object.class, NameFactory.SERVLET_WEB_SERVICE_TEMPLATE);
        infoBuilder.addReference("WebServiceBuilder", WebServiceBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("ClusteringBuilders", NamespaceDrivenBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("SecurityBuilders", NamespaceDrivenBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("ServiceBuilders", NamespaceDrivenBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("NamingBuilders", NamingBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("ResourceEnvironmentSetter", ResourceEnvironmentSetter.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("kernel", Kernel.class, false);
        infoBuilder.addInterface(ModuleBuilder.class);

        infoBuilder.setConstructor(new String[]{
                "defaultEnvironment",
                "defaultSessionTimeoutSeconds",
                "defaultWelcomeFiles",
                "jettyContainerObjectName",
                "jspServletClassName",
                "DefaultServlets",
                "DefaultFilters",
                "DefaultFilterMappings",
                "PojoWebServiceTemplate",
                "WebServiceBuilder",
                "ClusteringBuilders",
                "SecurityBuilders",
                "ServiceBuilders",
                "NamingBuilders",
                "ResourceEnvironmentSetter",
                "kernel"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    static class StartupOrderComparator implements Comparator {
        /**
         * comparator that compares first on the basis of startup order, and then on the lexicographical
         * ordering of servlet name.  Since the servlet names have a uniqueness constraint, this should
         * provide a total ordering consistent with equals.  All servlets with no startup order are after
         * all servlets with a startup order.
         *
         * @param o1 first ServletType object
         * @param o2 second ServletType object
         * @return an int < 0 if o1 precedes o2, 0 if they are equal, and > 0 if o2 preceeds o1.
         */
        public int compare(Object o1, Object o2) {
            ServletType s1 = (ServletType) o1;
            ServletType s2 = (ServletType) o2;

            // load-on-startup is set for neither.  the
            // ordering at this point doesn't matter, but we
            // should return "0" only if the two objects say
            // they are equal
            if (!s1.isSetLoadOnStartup() && !s2.isSetLoadOnStartup()) {
                return s1.equals(s2) ? 0 : s1.getServletName().getStringValue().trim().compareTo(s2.getServletName().getStringValue().trim());
            }

            // load-on-startup is set for one but not the
            // other.  whichever one is set will be "less
            // than", i.e. it will be loaded first
            if (s1.isSetLoadOnStartup() && !s2.isSetLoadOnStartup()) {
                return -1;
            }
            if (!s1.isSetLoadOnStartup() && s2.isSetLoadOnStartup()) {
                return 1;
            }

            // load-on-startup is set for both.  whichever one
            // has a smaller value is "less than"
            int comp = new Integer(s1.xgetLoadOnStartup().getStringValue()).compareTo(new Integer(s2.xgetLoadOnStartup().getStringValue()));
            if (comp == 0) {
                return s1.getServletName().getStringValue().trim().compareTo(s2.getServletName().getStringValue().trim());
            }
            return comp;
        }
    }
}
