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

package org.apache.geronimo.jetty7.deployment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarFile;

import javax.management.ObjectName;
import javax.security.auth.message.module.ServerAuthModule;
import javax.servlet.Servlet;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.components.jaspi.model.AuthModuleType;
import org.apache.geronimo.components.jaspi.model.ConfigProviderType;
import org.apache.geronimo.components.jaspi.model.JaspiXmlUtil;
import org.apache.geronimo.components.jaspi.model.ServerAuthConfigType;
import org.apache.geronimo.components.jaspi.model.ServerAuthContextType;
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
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.deployment.ModuleBuilderExtension;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.deployment.WebModule;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.deployment.annotation.AnnotatedWebApp;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.jetty7.Host;
import org.apache.geronimo.jetty7.JettyDefaultServletHolder;
import org.apache.geronimo.jetty7.JettyFilterHolder;
import org.apache.geronimo.jetty7.JettyFilterMapping;
import org.apache.geronimo.jetty7.JettyJspServletHolder;
import org.apache.geronimo.jetty7.JettyServletHolder;
import org.apache.geronimo.jetty7.JettyWebAppContext;
import org.apache.geronimo.jetty7.security.AuthConfigProviderHandlerFactory;
import org.apache.geronimo.jetty7.security.BuiltInAuthMethod;
import org.apache.geronimo.jetty7.security.JettySecurityHandlerFactory;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.naming.deployment.GBeanResourceEnvironmentBuilder;
import org.apache.geronimo.naming.deployment.ResourceEnvironmentSetter;
import org.apache.geronimo.schema.ElementConverter;
import org.apache.geronimo.schema.NamespaceElementConverter;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.security.jaspi.AuthConfigProviderGBean;
import org.apache.geronimo.security.jaspi.ServerAuthContextGBean;
import org.apache.geronimo.security.jaspi.ServerAuthModuleGBean;
import org.apache.geronimo.security.jaspi.ServerAuthConfigGBean;
import org.apache.geronimo.security.jaas.ConfigurationFactory;
import org.apache.geronimo.web.deployment.GenericToSpecificPlanConverter;
import org.apache.geronimo.web25.deployment.AbstractWebModuleBuilder;
import org.apache.geronimo.xbeans.geronimo.web.jetty.JettyAuthenticationType;
import org.apache.geronimo.xbeans.geronimo.web.jetty.JettyWebAppDocument;
import org.apache.geronimo.xbeans.geronimo.web.jetty.JettyWebAppType;
import org.apache.geronimo.xbeans.geronimo.web.jetty.config.GerJettyDocument;
import org.apache.geronimo.xbeans.javaee.DispatcherType;
import org.apache.geronimo.xbeans.javaee.ErrorPageType;
import org.apache.geronimo.xbeans.javaee.FilterMappingType;
import org.apache.geronimo.xbeans.javaee.FilterType;
import org.apache.geronimo.xbeans.javaee.FormLoginConfigType;
import org.apache.geronimo.xbeans.javaee.JspConfigType;
import org.apache.geronimo.xbeans.javaee.JspPropertyGroupType;
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
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @version $Rev:385659 $ $Date$
 */
public class JettyModuleBuilder extends AbstractWebModuleBuilder implements GBeanLifecycle {
    private static final Logger log = LoggerFactory.getLogger(JettyModuleBuilder.class);
    static final String ROLE_MAPPER_DATA_NAME = "roleMapperDataName";

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
    private final JettyJspServletHolder jspServlet;
    private final Collection defaultServlets;
    private final Collection defaultFilters;
    private final Collection defaultFilterMappings;
    private final GBeanData pojoWebServiceTemplate;

    protected final NamespaceDrivenBuilderCollection clusteringBuilders;

    private final List<String> defaultWelcomeFiles;
    private final Integer defaultSessionTimeoutSeconds;
    private final Map<String, String> defaultLocaleEncodingMappings;
    private final Map<String, String> defaultMimeTypeMappings;

    private static final String JETTY_NAMESPACE = JettyWebAppDocument.type.getDocumentElementName().getNamespaceURI();

    public JettyModuleBuilder(Environment defaultEnvironment,
                              Integer defaultSessionTimeoutSeconds,
                              List<String> defaultWelcomeFiles,
                              AbstractNameQuery jettyContainerName,
                              JettyJspServletHolder jspServlet,
                              Collection defaultServlets,
                              Collection defaultFilters,
                              Collection defaultFilterMappings,
                              Map<String, String> defaultLocaleEncodingMappings,
                              Map<String, String> defaultMimeTypeMappings,
                              Object pojoWebServiceTemplate,
                              Collection<WebServiceBuilder> webServiceBuilder,
                              Collection clusteringBuilders,
                              Collection serviceBuilders,
                              NamingBuilder namingBuilders,
                              Collection<ModuleBuilderExtension> moduleBuilderExtensions,
                              ResourceEnvironmentSetter resourceEnvironmentSetter,
                              Kernel kernel) throws GBeanNotFoundException {
        super(kernel, serviceBuilders, namingBuilders, resourceEnvironmentSetter, webServiceBuilder, moduleBuilderExtensions);
        this.defaultEnvironment = defaultEnvironment;
        this.defaultSessionTimeoutSeconds = (defaultSessionTimeoutSeconds == null) ? 30 * 60 : defaultSessionTimeoutSeconds;
        this.jettyContainerObjectName = jettyContainerName;
        this.jspServlet = jspServlet;
        this.defaultServlets = defaultServlets;
        this.defaultFilters = defaultFilters;
        this.defaultFilterMappings = defaultFilterMappings;
        this.pojoWebServiceTemplate = getGBeanData(kernel, pojoWebServiceTemplate);
        this.clusteringBuilders = new NamespaceDrivenBuilderCollection(clusteringBuilders);//, GerClusteringDocument.type.getDocumentElementName());

        this.defaultWelcomeFiles = defaultWelcomeFiles == null ? new ArrayList<String>() : defaultWelcomeFiles;
        this.defaultLocaleEncodingMappings = defaultLocaleEncodingMappings == null ? new HashMap<String, String>() : defaultLocaleEncodingMappings;
        this.defaultMimeTypeMappings = defaultMimeTypeMappings == null ? new HashMap<String, String>() : defaultMimeTypeMappings;
    }

    public void doStart() throws Exception {
        XmlBeansUtil.registerNamespaceUpdates(NAMESPACE_UPDATES);
        SchemaConversionUtils.registerNamespaceConversions(GERONIMO_SCHEMA_CONVERSIONS);
    }

    public void doStop() {
        XmlBeansUtil.unregisterNamespaceUpdates(NAMESPACE_UPDATES);
        //TODO not yet implemented
//        SchemaConversionUtils.unregisterNamespaceConversions(GERONIMO_SCHEMA_CONVERSIONS);
    }

    public void doFail() {
        doStop();
    }

    private static GBeanData getGBeanData(Kernel kernel, Object template) throws GBeanNotFoundException {
        if (template == null) {
            return null;
        }
        AbstractName templateName = kernel.getAbstractNameFor(template);
        return kernel.getGBeanData(templateName);
    }

    protected Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, boolean standAlone, String contextRoot, AbstractName earName, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        assert moduleFile != null : "moduleFile is null";
        assert targetPath != null : "targetPath is null";
        assert !targetPath.endsWith("/") : "targetPath must not end with a '/'";

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
            if (!moduleFile.getName().endsWith(".war")) {
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
        contextRoot = getContextRoot(jettyWebApp, contextRoot, webApp, standAlone, moduleFile, targetPath);

        EnvironmentType environmentType = jettyWebApp.getEnvironment();
        Environment environment = EnvironmentBuilder.buildEnvironment(environmentType, defaultEnvironment);

        Boolean distributable = webApp.getDistributableArray().length == 1 ? TRUE : FALSE;
        if (TRUE == distributable) {
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

        WebModule module = new WebModule(standAlone, moduleName, environment, moduleFile, targetPath, webApp, jettyWebApp, specDD, contextRoot, JETTY_NAMESPACE, annotatedWebApp);

        for (ModuleBuilderExtension mbe : moduleBuilderExtensions) {
            mbe.createModule(module, plan, moduleFile, targetPath, specDDUrl, environment, contextRoot, earName, naming, idBuilder);
        }
        return module;
    }

    String getContextRoot(JettyWebAppType jettyWebApp, String contextRoot, WebAppType webApp, boolean standAlone, JarFile moduleFile, String targetPath) {
        if (jettyWebApp.isSetContextRoot()) {
            contextRoot = jettyWebApp.getContextRoot();
        } else if (contextRoot == null || contextRoot.trim().equals("")) {
            contextRoot = determineDefaultContextRoot(webApp, standAlone, moduleFile, targetPath);
        }

        contextRoot = contextRoot.trim();
        return contextRoot;
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
                                log.warn(
                                        "Web application " + targetPath + " does not contain a WEB-INF/geronimo-web.xml deployment plan.  This may or may not be a problem, depending on whether you have things like resource references that need to be resolved.  You can also give the deployer a separate deployment plan file on the command line.");
                            }
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

    public void initContext(EARContext earContext, Module module, ClassLoader cl) throws DeploymentException {
        JettyWebAppType gerWebApp = (JettyWebAppType) module.getVendorDD();
        boolean hasSecurityRealmName = gerWebApp.isSetSecurityRealmName();
        basicInitContext(earContext, module, gerWebApp, hasSecurityRealmName);
        for (ModuleBuilderExtension mbe : moduleBuilderExtensions) {
            mbe.initContext(earContext, module, cl);
        }
    }

    public void addGBeans(EARContext earContext, Module module, ClassLoader cl, Collection repository) throws DeploymentException {
        EARContext moduleContext = module.getEarContext();
        AbstractName moduleName = moduleContext.getModuleName();
        WebModule webModule = (WebModule) module;

        WebAppType webApp = (WebAppType) webModule.getSpecDD();
        JettyWebAppType jettyWebApp = (JettyWebAppType) webModule.getVendorDD();
        GBeanData webModuleData = new GBeanData(moduleName, JettyWebAppContext.class);

        configureBasicWebModuleAttributes(webApp, jettyWebApp, moduleContext, earContext, webModule, webModuleData);

        // unsharableResources, applicationManagedSecurityResources
        GBeanResourceEnvironmentBuilder rebuilder = new GBeanResourceEnvironmentBuilder(webModuleData);
        //N.B. use earContext not moduleContext
        //TODO fix this for javaee 5 !!!
        resourceEnvironmentSetter.setResourceEnvironment(rebuilder, webApp.getResourceRefArray(), jettyWebApp.getResourceRefArray());
        try {
            moduleContext.addGBean(webModuleData);

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
            if (webApp.getDisplayNameArray().length > 0) {
                webModuleData.setAttribute("displayName", webApp.getDisplayNameArray()[0].getStringValue());
            }

            // configure context parameters.
            configureContextParams(webApp, webModuleData);

            // configure listeners.
            configureListeners(webApp, webModuleData);

            webModuleData.setAttribute(JettyWebAppContext.GBEAN_ATTR_SESSION_TIMEOUT,
                    (webApp.getSessionConfigArray().length == 1 && webApp.getSessionConfigArray(0).getSessionTimeout() != null) ?
                            webApp.getSessionConfigArray(0).getSessionTimeout().getBigIntegerValue().intValue() * 60 :
                            defaultSessionTimeoutSeconds);

            Boolean distributable = webApp.getDistributableArray().length == 1 ? TRUE : FALSE;
            webModuleData.setAttribute("distributable", distributable);
            if (TRUE == distributable) {
                clusteringBuilders.build(jettyWebApp, earContext, moduleContext);
                if (webModuleData.getReferencePatterns(JettyWebAppContext.GBEAN_REF_SESSION_HANDLER_FACTORY) == null) {
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
            configureLocaleEncodingMappingLists(webApp, webModuleData);

            // configure error pages.
            configureErrorPages(webApp, webModuleData);

            // configure tag libs.
            Set<String> knownServletMappings = new HashSet<String>();
            Map<String, Set<String>> servletMappings = new HashMap<String, Set<String>>();
            if (jspServlet != null) {
                configureTagLibs(module, webApp, webModuleData, servletMappings, knownServletMappings, jspServlet.getServletName());
                GBeanData jspServletData = configureDefaultServlet(jspServlet, earContext, moduleName, knownServletMappings, moduleContext);
                Set<String> jspMappings = (Set<String>) jspServletData.getAttribute("servletMappings");
                jspMappings.addAll(knownServletMappings);
                jspServletData.setAttribute("servletMappings", jspMappings);
            }

            // configure login configs.
            configureAuthentication(module, webApp, jettyWebApp, webModuleData);

            // Make sure that servlet mappings point to available servlets and never add a duplicate pattern.

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
            addServlets(moduleName, webModule, servletTypes, servletMappings, moduleContext);

            if (jettyWebApp.isSetSecurityRealmName()) {
                configureSecurityRealm(earContext, webApp, jettyWebApp, webModuleData);
            }

            //See Jetty-386, GERONIMO-3738
            if (jettyWebApp.getCompactPath()) {
                webModuleData.setAttribute("compactPath", Boolean.TRUE);
            }

            //TODO this may definitely not be the best place for this!
            for (ModuleBuilderExtension mbe : moduleBuilderExtensions) {
                mbe.addGBeans(earContext, module, cl, repository);
            }

            //not truly metadata complete until MBEs have run
            if (!webApp.getMetadataComplete()) {
                webApp.setMetadataComplete(true);
                module.setOriginalSpecDD(module.getSpecDD().toString());
            }
            webModuleData.setAttribute("deploymentDescriptor", module.getOriginalSpecDD());

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

    private void configureSecurityRealm(EARContext earContext, WebAppType webApp, JettyWebAppType jettyWebApp, GBeanData webModuleData) throws DeploymentException {
        AbstractName moduleName = webModuleData.getAbstractName();
        if (earContext.getSecurityConfiguration() == null) {
            throw new DeploymentException(
                    "You have specified a <security-realm-name> for the webapp " + moduleName + " but no <security> configuration (role mapping) is supplied in the Geronimo plan for the web application (or the Geronimo plan for the EAR if the web app is in an EAR)");
        }
        String securityRealmName = jettyWebApp.getSecurityRealmName().trim();
        webModuleData.setAttribute("securityRealmName", securityRealmName);
        webModuleData.setReferencePattern("RunAsSource", (AbstractNameQuery)earContext.getGeneralData().get(ROLE_MAPPER_DATA_NAME));

        /**
         * TODO - go back to commented version when possible.
         */
        String policyContextID = moduleName.toString().replaceAll("[, :]", "_");
        //String policyContextID = webModuleName.getCanonicalName();
        webModuleData.setAttribute("policyContextID", policyContextID);

        ComponentPermissions componentPermissions = buildSpecSecurityConfig(webApp);

        earContext.addSecurityContext(policyContextID, componentPermissions);
    }

    private void addDefaultServletsGBeans(EARContext earContext, EARContext moduleContext, AbstractName moduleName, Set knownServletMappings) throws GBeanNotFoundException, GBeanAlreadyExistsException {
        for (Object defaultServlet : defaultServlets) {
            configureDefaultServlet(defaultServlet, earContext, moduleName, knownServletMappings, moduleContext);
        }
    }

    private GBeanData configureDefaultServlet(Object defaultServlet, EARContext earContext, AbstractName moduleName, Set knownServletMappings, EARContext moduleContext) throws GBeanNotFoundException, GBeanAlreadyExistsException {
        GBeanData servletGBeanData = getGBeanData(kernel, defaultServlet);
        AbstractName defaultServletObjectName = earContext.getNaming().createChildName(moduleName, (String) servletGBeanData.getAttribute("servletName"), NameFactory.SERVLET);
        servletGBeanData.setAbstractName(defaultServletObjectName);
        servletGBeanData.setReferencePattern("JettyServletRegistration", moduleName);
        Set<String> defaultServletMappings = new HashSet<String>((Collection<String>) servletGBeanData.getAttribute("servletMappings"));
        defaultServletMappings.removeAll(knownServletMappings);
        servletGBeanData.setAttribute("servletMappings", defaultServletMappings);
        moduleContext.addGBean(servletGBeanData);
        return servletGBeanData;
    }

    private void addFiltersGBeans(EARContext earContext, EARContext moduleContext, AbstractName moduleName, WebAppType webApp) throws GBeanAlreadyExistsException {
        FilterType[] filterArray = webApp.getFilterArray();
        for (FilterType filterType : filterArray) {
            String filterName = filterType.getFilterName().getStringValue().trim();
            AbstractName filterAbstractName = earContext.getNaming().createChildName(moduleName, filterName, NameFactory.WEB_FILTER);
            GBeanData filterData = new GBeanData(filterAbstractName, JettyFilterHolder.GBEAN_INFO);
            filterData.setAttribute("filterName", filterName);
            filterData.setAttribute("filterClass", filterType.getFilterClass().getStringValue().trim());
            Map<String, String> initParams = new HashMap<String, String>();
            ParamValueType[] initParamArray = filterType.getInitParamArray();
            for (ParamValueType paramValueType : initParamArray) {
                initParams.put(paramValueType.getParamName().getStringValue().trim(), paramValueType.getParamValue().getStringValue().trim());
            }
            filterData.setAttribute("initParams", initParams);
            filterData.setReferencePattern("JettyServletRegistration", moduleName);
            moduleContext.addGBean(filterData);
        }
    }

    private void addFilterMappingsGBeans(EARContext earContext, EARContext moduleContext, AbstractName moduleName, WebAppType webApp, AbstractName previous) throws GBeanAlreadyExistsException {
        FilterMappingType[] filterMappingArray = webApp.getFilterMappingArray();
        for (FilterMappingType filterMappingType : filterMappingArray) {
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
                Set<AbstractName> servletNameSet = new HashSet<AbstractName>();
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
            filterMappingData.setAttribute("requestDispatch", request);
            filterMappingData.setAttribute("forwardDispatch", forward);
            filterMappingData.setAttribute("includeDispatch", include);
            filterMappingData.setAttribute("errorDispatch", error);
            filterMappingData.setReferencePattern("Filter", filterAbstractName);
            moduleContext.addGBean(filterMappingData);
        }
    }

    private AbstractName addDefaultFiltersGBeans(EARContext earContext, EARContext moduleContext, AbstractName moduleName, AbstractName previous) throws GBeanNotFoundException, GBeanAlreadyExistsException {
        for (Object defaultFilter : defaultFilters) {
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


            filterMappingGBeanData.setAttribute("requestDispatch", TRUE);
            filterMappingGBeanData.setAttribute("forwardDispatch", TRUE);
            filterMappingGBeanData.setAttribute("includeDispatch", TRUE);
            filterMappingGBeanData.setAttribute("errorDispatch", FALSE);
            filterMappingGBeanData.setReferencePattern("Filter", defaultFilterAbstractName);
            moduleContext.addGBean(filterMappingGBeanData);
        }
        return previous;
    }

    private Map buildServletMappings(Module module, WebAppType webApp, Map<String, Set<String>> servletMappings, Set<String> knownServletMappings) throws DeploymentException {
        ServletType[] servletTypes = webApp.getServletArray();
        Set<String> knownServlets = new HashSet<String>();
        for (ServletType type : servletTypes) {
            knownServlets.add(type.getServletName().getStringValue().trim());
        }

        ServletMappingType[] servletMappingArray = webApp.getServletMappingArray();
        for (ServletMappingType servletMappingType : servletMappingArray) {
            String servletName = servletMappingType.getServletName().getStringValue().trim();
            if (!knownServlets.contains(servletName)) {
                throw new DeploymentException("Web app " + module.getName() +
                        " contains a servlet mapping that refers to servlet '" + servletName +
                        "' but no such servlet was found!");
            }
            UrlPatternType[] urlPatterns = servletMappingType.getUrlPatternArray();
            addMappingsForServlet(servletName, urlPatterns, knownServletMappings, servletMappings);
        }

        return servletMappings;
    }

    private void addMappingsForServlet(String servletName, UrlPatternType[] urlPatterns, Set<String> knownServletMappings, Map<String, Set<String>> servletMappings) throws DeploymentException {
        for (UrlPatternType patternType : urlPatterns) {
            String urlPattern = patternType.getStringValue().trim();
            if (!urlPattern.startsWith("*") && !urlPattern.startsWith("/")) {
                urlPattern = "/" + urlPattern;
            }
            if (!knownServletMappings.contains(urlPattern)) {
                knownServletMappings.add(urlPattern);
                checkString(urlPattern);
                Set<String> urlsForServlet = servletMappings.get(servletName);
                if (urlsForServlet == null) {
                    urlsForServlet = new HashSet<String>();
                    servletMappings.put(servletName, urlsForServlet);
                }
                urlsForServlet.add(urlPattern);
            }
        }
    }

    private void configureAuthentication(Module module, WebAppType webApp, JettyWebAppType jettyWebApp, GBeanData webModuleData) throws DeploymentException, GBeanAlreadyExistsException {
        EARContext moduleContext = module.getEarContext();
        LoginConfigType[] loginConfigArray = webApp.getLoginConfigArray();
        if (loginConfigArray.length > 1) {
            throw new DeploymentException("Web app " + module.getName() + " cannot have more than one login-config element.  Currently has " + loginConfigArray.length + " login-config elements.");
        }
        JettyAuthenticationType authType = jettyWebApp.getAuthentication();
        if (loginConfigArray.length == 1 || authType != null) {
            AbstractName factoryName = moduleContext.getNaming().createChildName(module.getModuleName(), "securityHandlerFactory", GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
            webModuleData.setReferencePattern("SecurityHandlerFactory", factoryName);


            AbstractName providerName = moduleContext.getNaming().createChildName(module.getModuleName(), "authConfigProvider", GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
            if (authType != null) {
                GBeanData securityFactoryData = new GBeanData(factoryName, AuthConfigProviderHandlerFactory.class);
                securityFactoryData.setAttribute("messageLayer", "HttpServlet");
                Object contextPath = webModuleData.getAttribute("contextPath");
                securityFactoryData.setAttribute("appContext", "server " + contextPath);
                configureConfigurationFactory(jettyWebApp, null, securityFactoryData);
                moduleContext.addGBean(securityFactoryData);
                GBeanData authConfigProviderData = null;
                try {
                    if (authType.isSetConfigProvider()) {
                        authConfigProviderData = new GBeanData(providerName, AuthConfigProviderGBean.class);
                        final XmlCursor xmlCursor = authType.getConfigProvider().newCursor();
                        try {
                            XMLStreamReader reader = new InternWrapper(xmlCursor.newXMLStreamReader());
                            ConfigProviderType configProviderType = JaspiXmlUtil.loadConfigProvider(reader);
                            StringWriter out = new StringWriter();
                            JaspiXmlUtil.writeConfigProvider(configProviderType, out);
                            authConfigProviderData.setAttribute("config", out.toString());
                        } finally {
                            xmlCursor.dispose();
                        }
                    } else if (authType.isSetServerAuthConfig()) {
                        authConfigProviderData = new GBeanData(providerName, ServerAuthConfigGBean.class);
                        final XmlCursor xmlCursor = authType.getServerAuthConfig().newCursor();
                        try {
                            XMLStreamReader reader = new InternWrapper(xmlCursor.newXMLStreamReader());
                            ServerAuthConfigType serverAuthConfigType = JaspiXmlUtil.loadServerAuthConfig(reader);
                            StringWriter out = new StringWriter();
                            JaspiXmlUtil.writeServerAuthConfig(serverAuthConfigType, out);
                            authConfigProviderData.setAttribute("config", out.toString());
                        } finally {
                            xmlCursor.dispose();
                        }
                    } else if (authType.isSetServerAuthContext()) {
                        authConfigProviderData = new GBeanData(providerName, ServerAuthContextGBean.class);
                        final XmlCursor xmlCursor = authType.getServerAuthContext().newCursor();
                        try {
                            XMLStreamReader reader = new InternWrapper(xmlCursor.newXMLStreamReader());
                            ServerAuthContextType serverAuthContextType = JaspiXmlUtil.loadServerAuthContext(reader);
                            StringWriter out = new StringWriter();
                            JaspiXmlUtil.writeServerAuthContext(serverAuthContextType, out);
                            authConfigProviderData.setAttribute("config", out.toString());
                        } finally {
                            xmlCursor.dispose();
                        }
                    } else if (authType.isSetServerAuthModule()) {
                        authConfigProviderData = new GBeanData(providerName, ServerAuthModuleGBean.class);
                        final XmlCursor xmlCursor = authType.getServerAuthModule().newCursor();
                        try {
                            XMLStreamReader reader = new InternWrapper(xmlCursor.newXMLStreamReader());
                            AuthModuleType<ServerAuthModule> authModuleType = JaspiXmlUtil.loadServerAuthModule(reader);
                            StringWriter out = new StringWriter();
                            JaspiXmlUtil.writeServerAuthModule(authModuleType, out);
                            authConfigProviderData.setAttribute("config", out.toString());
                            authConfigProviderData.setAttribute("messageLayer", "Http");
                            authConfigProviderData.setAttribute("appContext", contextPath);
                            //TODO ??
                            authConfigProviderData.setAttribute("authenticationID", contextPath);
                        } finally {
                            xmlCursor.dispose();
                        }
                    }
                } catch (ParserConfigurationException e) {
                    throw new DeploymentException("Could not read auth config", e);
                } catch (IOException e) {
                    throw new DeploymentException("Could not read auth config", e);
                } catch (SAXException e) {
                    throw new DeploymentException("Could not read auth config", e);
                } catch (JAXBException e) {
                    throw new DeploymentException("Could not read auth config", e);
                } catch (XMLStreamException e) {
                    throw new DeploymentException("Could not read auth config", e);
                }
                if (authConfigProviderData != null) {
                    moduleContext.addGBean(authConfigProviderData);
                    securityFactoryData.addDependency(providerName);
                }
                //otherwise rely on pre-configured jaspi
            } else {
                LoginConfigType loginConfig = loginConfigArray[0];
                if (loginConfig.isSetAuthMethod()) {
                    String authMethod = loginConfig.getAuthMethod().getStringValue().trim();
                    BuiltInAuthMethod auth = BuiltInAuthMethod.valueOf(authMethod);
                    GBeanData securityFactoryData = new GBeanData(factoryName, JettySecurityHandlerFactory.class);
                    securityFactoryData.setAttribute("authMethod", auth);
                    configureConfigurationFactory(jettyWebApp, loginConfig, securityFactoryData);

                    moduleContext.addGBean(securityFactoryData);


                    if (auth == BuiltInAuthMethod.BASIC) {
                        securityFactoryData.setAttribute("realmName", loginConfig.getRealmName().getStringValue().trim());
                    } else if (auth == BuiltInAuthMethod.DIGEST) {
                        securityFactoryData.setAttribute("realmName", loginConfig.getRealmName().getStringValue().trim());
                    } else if (auth == BuiltInAuthMethod.FORM) {
                        if (loginConfig.isSetFormLoginConfig()) {
                            FormLoginConfigType formLoginConfig = loginConfig.getFormLoginConfig();
                            securityFactoryData.setAttribute("loginPage", formLoginConfig.getFormLoginPage().getStringValue());
                            securityFactoryData.setAttribute("errorPage", formLoginConfig.getFormErrorPage().getStringValue());
                        }
                    } else if (auth == BuiltInAuthMethod.CLIENTCERT) {
                        //nothing to do
                    } else {
                        throw new DeploymentException("unrecognized auth method, use jaspi to configure: " + authMethod);
                    }

                } else {
                    throw new DeploymentException("No auth method configured and no jaspi configured");
                }
                if (loginConfig.isSetRealmName()) {
                    webModuleData.setAttribute("realmName", loginConfig.getRealmName().getStringValue());
                }
            }
//        } else if (jettyWebApp.isSetSecurityRealmName()) {
//            webModuleData.setAttribute("authenticator", new NonAuthenticator());
        }
    }

    private void configureConfigurationFactory(JettyWebAppType jettyWebApp, LoginConfigType loginConfig, GBeanData securityFactoryData) {
        String securityRealmName;
        if (jettyWebApp.isSetSecurityRealmName()) {
            securityRealmName = jettyWebApp.getSecurityRealmName().trim();
        } else {
            if (loginConfig == null) return;
            securityRealmName = loginConfig.getRealmName().getStringValue().trim();
        }
        AbstractNameQuery configurationFactoryName = new AbstractNameQuery(null, Collections.singletonMap("name", securityRealmName), ConfigurationFactory.class.getName());
        securityFactoryData.setReferencePattern("ConfigurationFactory", configurationFactoryName);
    }

    private void configureTagLibs(Module module, WebAppType webApp, GBeanData webModuleData, Map<String, Set<String>> servletMappings, Set<String> knownServletMappings, String jspServletName) throws DeploymentException {
        JspConfigType[] jspConfigArray = webApp.getJspConfigArray();
        if (jspConfigArray.length > 1) {
            throw new DeploymentException("Web app " + module.getName() + " cannot have more than one jsp-config element.  Currently has " + jspConfigArray.length + " jsp-config elements.");
        }
        Map<String, String> tagLibMap = new HashMap<String, String>();
        for (JspConfigType aJspConfigArray : jspConfigArray) {
            TaglibType[] tagLibArray = aJspConfigArray.getTaglibArray();
            for (TaglibType taglib : tagLibArray) {
                tagLibMap.put(taglib.getTaglibUri().getStringValue().trim(), taglib.getTaglibLocation().getStringValue().trim());
            }
            for (JspPropertyGroupType propertyGroup : aJspConfigArray.getJspPropertyGroupArray()) {
                UrlPatternType[] urlPatterns = propertyGroup.getUrlPatternArray();
                addMappingsForServlet(jspServletName, urlPatterns, knownServletMappings, servletMappings);
            }
        }
        webModuleData.setAttribute("tagLibMap", tagLibMap);
    }

    private void configureErrorPages(WebAppType webApp, GBeanData webModuleData) {
        ErrorPageType[] errorPageArray = webApp.getErrorPageArray();
        Map<String, String> errorPageMap = new HashMap<String, String>();
        for (ErrorPageType errorPageType : errorPageArray) {
            if (errorPageType.isSetErrorCode()) {
                errorPageMap.put(errorPageType.getErrorCode().getStringValue().trim(), errorPageType.getLocation().getStringValue().trim());
            } else {
                errorPageMap.put(errorPageType.getExceptionType().getStringValue().trim(), errorPageType.getLocation().getStringValue().trim());
            }
        }
        webModuleData.setAttribute("errorPages", errorPageMap);
    }

    private void configureLocaleEncodingMappingLists(WebAppType webApp, GBeanData webModuleData) {
        LocaleEncodingMappingListType[] localeEncodingMappingListArray = webApp.getLocaleEncodingMappingListArray();
        Map<String, String> localeEncodingMappingMap = new HashMap<String, String>(defaultLocaleEncodingMappings);
        for (LocaleEncodingMappingListType aLocaleEncodingMappingListArray : localeEncodingMappingListArray) {
            LocaleEncodingMappingType[] localeEncodingMappingArray = aLocaleEncodingMappingListArray.getLocaleEncodingMappingArray();
            for (LocaleEncodingMappingType localeEncodingMapping : localeEncodingMappingArray) {
                localeEncodingMappingMap.put(localeEncodingMapping.getLocale().trim(), localeEncodingMapping.getEncoding().trim());
            }
        }
        webModuleData.setAttribute("localeEncodingMapping", localeEncodingMappingMap);
    }

    private void configureWelcomeFileLists(WebAppType webApp, GBeanData webModuleData) {
        WelcomeFileListType[] welcomeFileArray = webApp.getWelcomeFileListArray();
        List<String> welcomeFiles;
        if (welcomeFileArray.length > 0) {
            welcomeFiles = new ArrayList<String>();
            for (WelcomeFileListType aWelcomeFileArray : welcomeFileArray) {
                String[] welcomeFileListType = aWelcomeFileArray.getWelcomeFileArray();
                for (String welcomeFile : welcomeFileListType) {
                    welcomeFiles.add(welcomeFile.trim());
                }
            }
        } else {
            welcomeFiles = new ArrayList<String>(defaultWelcomeFiles);
        }
        webModuleData.setAttribute("welcomeFiles", welcomeFiles.toArray(new String[welcomeFiles.size()]));
    }

    private void configureMimeMappings(WebAppType webApp, GBeanData webModuleData) {
        MimeMappingType[] mimeMappingArray = webApp.getMimeMappingArray();
        Map<String, String> mimeMappingMap = new HashMap<String, String>(defaultMimeTypeMappings);
        for (MimeMappingType mimeMappingType : mimeMappingArray) {
            mimeMappingMap.put(mimeMappingType.getExtension().getStringValue().trim(), mimeMappingType.getMimeType().getStringValue().trim());
        }
        webModuleData.setAttribute("mimeMap", mimeMappingMap);
    }

    private void configureListeners(WebAppType webApp, GBeanData webModuleData) {
        ListenerType[] listenerArray = webApp.getListenerArray();
        Collection<String> listeners = new ArrayList<String>();
        for (ListenerType listenerType : listenerArray) {
            listeners.add(listenerType.getListenerClass().getStringValue().trim());
        }
        webModuleData.setAttribute("listenerClassNames", listeners);
    }

    private void configureContextParams(WebAppType webApp, GBeanData webModuleData) {
        ParamValueType[] contextParamArray = webApp.getContextParamArray();
        Map<String, String> contextParams = new HashMap<String, String>();
        for (ParamValueType contextParam : contextParamArray) {
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
     * @param moduleContext   an <code>EARContext</code> value
     * @throws DeploymentException if an error occurs
     */
    private void addServlets(AbstractName webModuleName,
                             Module module,
                             ServletType[] servletTypes,
                             Map<String, Set<String>> servletMappings,
                             EARContext moduleContext) throws DeploymentException {

        // this TreeSet will order the ServletTypes based on whether
        // they have a load-on-startup element and what its value is
        TreeSet<ServletType> loadOrder = new TreeSet<ServletType>(new StartupOrderComparator());

        // add all of the servlets to the sorted set
        loadOrder.addAll(Arrays.asList(servletTypes));

        // now that they're sorted, read them in order and add them to
        // the context.  we'll use a GBean reference to enforce the
        // load order.  Each servlet GBean (except the first) has a
        // reference to the previous GBean.  The kernel will ensure
        // that each "previous" GBean is running before it starts any
        // other GBeans that reference it.  See also
        // o.a.g.jetty7.JettyFilterMapping which provided the example
        // of how to do this.
        // http://issues.apache.org/jira/browse/GERONIMO-645
        AbstractName previousServlet = null;
        for (Object aLoadOrder : loadOrder) {
            ServletType servletType = (ServletType) aLoadOrder;
            previousServlet = addServlet(webModuleName, module, previousServlet, servletType, servletMappings, moduleContext);
        }
    }

    /**
     * @param webModuleName   AbstractName of the web module
     * @param module          the web module being added
     * @param previousServlet the servlet to start before this one in init order
     * @param servletType     XMLObject specifying the servlet configuration
     * @param servletMappings Map of servlet name to set of ServletMapping strings for this web app
     * @param moduleContext   deployment context for this module
     * @return AbstractName of servlet gbean added
     * @throws DeploymentException if something goes wrong
     */
    private AbstractName addServlet(AbstractName webModuleName,
                                    Module module,
                                    AbstractName previousServlet,
                                    ServletType servletType,
                                    Map<String, Set<String>> servletMappings,
                                    EARContext moduleContext) throws DeploymentException {
        String servletName = servletType.getServletName().getStringValue().trim();
        AbstractName servletAbstractName = moduleContext.getNaming().createChildName(webModuleName, servletName, NameFactory.SERVLET);
        GBeanData servletData;
        Map<String, String> initParams = new HashMap<String, String>();
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
                for (Object aWebServiceBuilder : webServiceBuilder) {
                    WebServiceBuilder serviceBuilder = (WebServiceBuilder) aWebServiceBuilder;
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
            servletData.setAttribute("servletClass", jspServlet.getServletClassName());
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
        for (ParamValueType paramValueType : initParamArray) {
            initParams.put(paramValueType.getParamName().getStringValue().trim(), paramValueType.getParamValue().getStringValue().trim());
        }
        servletData.setAttribute("initParams", initParams);
        if (servletType.isSetLoadOnStartup()) {
            Integer loadOnStartup = new Integer(servletType.xgetLoadOnStartup().getStringValue());
            servletData.setAttribute("loadOnStartup", loadOnStartup);
        }

        Set mappings = servletMappings.get(servletName);
        servletData.setAttribute("servletMappings", mappings == null ? Collections.EMPTY_SET : mappings);

        //run-as
        if (servletType.isSetRunAs()) {
            String runAsRole = servletType.getRunAs().getRoleName().getStringValue().trim();
            servletData.setAttribute("runAsRole", runAsRole);
        }

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
        infoBuilder.addAttribute("defaultLocaleEncodingMappings", Map.class, true, true);
        infoBuilder.addAttribute("defaultMimeTypeMappings", Map.class, true, true);
        infoBuilder.addAttribute("jettyContainerObjectName", AbstractNameQuery.class, true, true);
        infoBuilder.addReference("JspServlet", JettyJspServletHolder.class, NameFactory.SERVLET_TEMPLATE);
        infoBuilder.addReference("DefaultServlets", JettyDefaultServletHolder.class, NameFactory.SERVLET_TEMPLATE);
        infoBuilder.addReference("DefaultFilters", Object.class);
        infoBuilder.addReference("DefaultFilterMappings", Object.class);
        infoBuilder.addReference("PojoWebServiceTemplate", Object.class, NameFactory.SERVLET_WEB_SERVICE_TEMPLATE);
        infoBuilder.addReference("WebServiceBuilder", WebServiceBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("ClusteringBuilders", NamespaceDrivenBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("ServiceBuilders", NamespaceDrivenBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("NamingBuilders", NamingBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("ModuleBuilderExtensions", ModuleBuilderExtension.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("ResourceEnvironmentSetter", ResourceEnvironmentSetter.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("kernel", Kernel.class, false);
        infoBuilder.addInterface(ModuleBuilder.class);

        infoBuilder.setConstructor(new String[]{
                "defaultEnvironment",
                "defaultSessionTimeoutSeconds",
                "defaultWelcomeFiles",
                "jettyContainerObjectName",
                "JspServlet",
                "DefaultServlets",
                "DefaultFilters",
                "DefaultFilterMappings",
                "defaultLocaleEncodingMappings",
                "defaultMimeTypeMappings",
                "PojoWebServiceTemplate",
                "WebServiceBuilder",
                "ClusteringBuilders",
                "ServiceBuilders",
                "NamingBuilders",
                "ModuleBuilderExtensions",
                "ResourceEnvironmentSetter",
                "kernel"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    static class StartupOrderComparator implements Comparator<ServletType> {
        /**
         * comparator that compares first on the basis of startup order, and then on the lexicographical
         * ordering of servlet name.  Since the servlet names have a uniqueness constraint, this should
         * provide a total ordering consistent with equals.  All servlets with no startup order are after
         * all servlets with a startup order.
         *
         * @param s1 first ServletType object
         * @param s2 second ServletType object
         * @return an int < 0 if o1 precedes o2, 0 if they are equal, and > 0 if o2 preceeds o1.
         */
        public int compare(ServletType s1, ServletType s2) {

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

    private static class InternWrapper implements XMLStreamReader {
        private final XMLStreamReader delegate;

        private InternWrapper(XMLStreamReader delegate) {
            this.delegate = delegate;
        }

        public void close() throws XMLStreamException {
                 delegate.close();
            }

            public int getAttributeCount() {
                return  delegate.getAttributeCount();
            }

            public String getAttributeLocalName(int i) {
                return  delegate.getAttributeLocalName(i);
            }

            public QName getAttributeName(int i) {
                return  delegate.getAttributeName(i);
            }

            public String getAttributeNamespace(int i) {
                return  delegate.getAttributeNamespace(i);
            }

            public String getAttributePrefix(int i) {
                return  delegate.getAttributePrefix(i);
            }

            public String getAttributeType(int i) {
                return  delegate.getAttributeType(i);
            }

            public String getAttributeValue(int i) {
                return  delegate.getAttributeValue(i);
            }

            public String getAttributeValue(String s, String s1) {
                return  delegate.getAttributeValue(s, s1);
            }

            public String getCharacterEncodingScheme() {
                return  delegate.getCharacterEncodingScheme();
            }

            public String getElementText() throws XMLStreamException {
                return  delegate.getElementText();
            }

            public String getEncoding() {
                return  delegate.getEncoding();
            }

            public int getEventType() {
                return  delegate.getEventType();
            }

            public String getLocalName() {
                return  delegate.getLocalName().intern();
            }

            public Location getLocation() {
                return  delegate.getLocation();
            }

            public QName getName() {
                return  delegate.getName();
            }

            public NamespaceContext getNamespaceContext() {
                return  delegate.getNamespaceContext();
            }

            public int getNamespaceCount() {
                return  delegate.getNamespaceCount();
            }

            public String getNamespacePrefix(int i) {
                return  delegate.getNamespacePrefix(i);
            }

            public String getNamespaceURI() {
                return  delegate.getNamespaceURI().intern();
            }

            public String getNamespaceURI(int i) {
                return  delegate.getNamespaceURI(i);
            }

            public String getNamespaceURI(String s) {
                return  delegate.getNamespaceURI(s);
            }

            public String getPIData() {
                return  delegate.getPIData();
            }

            public String getPITarget() {
                return  delegate.getPITarget();
            }

            public String getPrefix() {
                return  delegate.getPrefix();
            }

            public Object getProperty(String s) throws IllegalArgumentException {
                return  delegate.getProperty(s);
            }

            public String getText() {
                return  delegate.getText();
            }

            public char[] getTextCharacters() {
                return  delegate.getTextCharacters();
            }

            public int getTextCharacters(int i, char[] chars, int i1, int i2) throws XMLStreamException {
                return  delegate.getTextCharacters(i, chars, i1, i2);
            }

            public int getTextLength() {
                return  delegate.getTextLength();
            }

            public int getTextStart() {
                return  delegate.getTextStart();
            }

            public String getVersion() {
                return  delegate.getVersion();
            }

            public boolean hasName() {
                return  delegate.hasName();
            }

            public boolean hasNext() throws XMLStreamException {
                return  delegate.hasNext();
            }

            public boolean hasText() {
                return  delegate.hasText();
            }

            public boolean isAttributeSpecified(int i) {
                return  delegate.isAttributeSpecified(i);
            }

            public boolean isCharacters() {
                return  delegate.isCharacters();
            }

            public boolean isEndElement() {
                return  delegate.isEndElement();
            }

            public boolean isStandalone() {
                return  delegate.isStandalone();
            }

            public boolean isStartElement() {
                return  delegate.isStartElement();
            }

            public boolean isWhiteSpace() {
                return  delegate.isWhiteSpace();
            }

            public int next() throws XMLStreamException {
                return  delegate.next();
            }

            public int nextTag() throws XMLStreamException {
                return  delegate.nextTag();
            }

            public void require(int i, String s, String s1) throws XMLStreamException {
                 delegate.require(i, s, s1);
            }

            public boolean standaloneSet() {
                return  delegate.standaloneSet();
            }
    }
}
