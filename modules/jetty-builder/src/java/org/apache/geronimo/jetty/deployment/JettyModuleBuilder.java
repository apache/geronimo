/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.jetty.deployment;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.ArrayList;
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
import java.util.zip.ZipEntry;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebRoleRefPermission;
import javax.security.jacc.WebUserDataPermission;
import javax.servlet.Servlet;
import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.service.ServiceConfigBuilder;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.deployment.xbeans.DependencyType;
import org.apache.geronimo.deployment.xbeans.GbeanType;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.deployment.WebModule;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.jetty.JettyClassLoader;
import org.apache.geronimo.jetty.JettyFilterHolder;
import org.apache.geronimo.jetty.JettyFilterMapping;
import org.apache.geronimo.jetty.JettyServletHolder;
import org.apache.geronimo.jetty.JettyWebAppContext;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.naming.deployment.GBeanResourceEnvironmentBuilder;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.security.deployment.SecurityBuilder;
import org.apache.geronimo.security.deployment.SecurityConfiguration;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.security.util.URLPattern;
import org.apache.geronimo.transaction.context.OnlineUserTransaction;
import org.apache.geronimo.xbeans.geronimo.web.GerWebAppDocument;
import org.apache.geronimo.xbeans.geronimo.web.GerWebAppType;
import org.apache.geronimo.xbeans.j2ee.DispatcherType;
import org.apache.geronimo.xbeans.j2ee.ErrorPageType;
import org.apache.geronimo.xbeans.j2ee.FilterMappingType;
import org.apache.geronimo.xbeans.j2ee.FilterType;
import org.apache.geronimo.xbeans.j2ee.FormLoginConfigType;
import org.apache.geronimo.xbeans.j2ee.HttpMethodType;
import org.apache.geronimo.xbeans.j2ee.JspConfigType;
import org.apache.geronimo.xbeans.j2ee.ListenerType;
import org.apache.geronimo.xbeans.j2ee.LocaleEncodingMappingListType;
import org.apache.geronimo.xbeans.j2ee.LocaleEncodingMappingType;
import org.apache.geronimo.xbeans.j2ee.LoginConfigType;
import org.apache.geronimo.xbeans.j2ee.MimeMappingType;
import org.apache.geronimo.xbeans.j2ee.ParamValueType;
import org.apache.geronimo.xbeans.j2ee.RoleNameType;
import org.apache.geronimo.xbeans.j2ee.SecurityConstraintType;
import org.apache.geronimo.xbeans.j2ee.SecurityRoleRefType;
import org.apache.geronimo.xbeans.j2ee.SecurityRoleType;
import org.apache.geronimo.xbeans.j2ee.ServletMappingType;
import org.apache.geronimo.xbeans.j2ee.ServletType;
import org.apache.geronimo.xbeans.j2ee.TaglibType;
import org.apache.geronimo.xbeans.j2ee.UrlPatternType;
import org.apache.geronimo.xbeans.j2ee.WebAppDocument;
import org.apache.geronimo.xbeans.j2ee.WebAppType;
import org.apache.geronimo.xbeans.j2ee.WebResourceCollectionType;
import org.apache.geronimo.xbeans.j2ee.WelcomeFileListType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.mortbay.http.BasicAuthenticator;
import org.mortbay.http.ClientCertAuthenticator;
import org.mortbay.http.DigestAuthenticator;
import org.mortbay.jetty.servlet.FormAuthenticator;


/**
 * @version $Rev$ $Date$
 */
public class JettyModuleBuilder implements ModuleBuilder {
    private final static Log log = LogFactory.getLog(JettyModuleBuilder.class);
    private final URI defaultParentId;
    private final ObjectName jettyContainerObjectName;
    private final Collection defaultServlets;
    private final Collection defaultFilters;
    private final Collection defaultFilterMappings;
    private final GBeanData pojoWebServiceTemplate;

    private final WebServiceBuilder webServiceBuilder;

    private final List defaultWelcomeFiles;
    private final Integer defaultSessionTimeoutSeconds;

    private final Repository repository;
    private final Kernel kernel;

    public JettyModuleBuilder(URI defaultParentId,
                              Integer defaultSessionTimeoutSeconds,
                              List defaultWelcomeFiles,
                              ObjectName jettyContainerObjectName,
                              Collection defaultServlets,
                              Collection defaultFilters,
                              Collection defaultFilterMappings,
                              Object pojoWebServiceTemplate,
                              WebServiceBuilder webServiceBuilder,
                              Repository repository,
                              Kernel kernel) throws GBeanNotFoundException {
        this.defaultParentId = defaultParentId;
        this.defaultSessionTimeoutSeconds = (defaultSessionTimeoutSeconds == null) ? new Integer(30 * 60) : defaultSessionTimeoutSeconds;
        this.jettyContainerObjectName = jettyContainerObjectName;
        this.defaultServlets = defaultServlets;
        this.defaultFilters = defaultFilters;
        this.defaultFilterMappings = defaultFilterMappings;
        this.pojoWebServiceTemplate = getGBeanData(kernel, pojoWebServiceTemplate);
        this.webServiceBuilder = webServiceBuilder;
        this.repository = repository;
        this.kernel = kernel;

        //todo locale mappings

        this.defaultWelcomeFiles = defaultWelcomeFiles;
    }

    private static GBeanData getGBeanData(Kernel kernel, Object template) throws GBeanNotFoundException {
        if (template == null) {
            return null;
        }
        ObjectName templateName = kernel.getProxyManager().getProxyTarget(template);
        GBeanData templateData = kernel.getGBeanData(templateName);
        return templateData;
    }

    public Module createModule(File plan, JarFile moduleFile) throws DeploymentException {
        return createModule(plan, moduleFile, "war", null, true, null);
    }

    public Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, URI earConfigId, Object moduleContextInfo) throws DeploymentException {
        return createModule(plan, moduleFile, targetPath, specDDUrl, false, (String) moduleContextInfo);
    }

    private Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, boolean standAlone, String contextRoot) throws DeploymentException {
        assert moduleFile != null: "moduleFile is null";
        assert targetPath != null: "targetPath is null";
        assert !targetPath.endsWith("/"): "targetPath must not end with a '/'";

        // parse the spec dd
        String specDD;
        WebAppType webApp;
        try {
            if (specDDUrl == null) {
                specDDUrl = DeploymentUtil.createJarURL(moduleFile, "WEB-INF/web.xml");
            }

            // read in the entire specDD as a string, we need this for getDeploymentDescriptor
            // on the J2ee management object
            specDD = DeploymentUtil.readAll(specDDUrl);
        } catch (Exception e) {
            //no web.xml, not for us
            return null;
        }
        //we found web.xml, if it won't parse that's an error.
        try {
            // parse it
            XmlObject parsed = SchemaConversionUtils.parse(specDD);
            WebAppDocument webAppDoc = SchemaConversionUtils.convertToServletSchema(parsed);
            webApp = webAppDoc.getWebApp();
        } catch (XmlException xmle) {
            throw new DeploymentException("Error parsing web.xml", xmle);
        }
        check(webApp);

        // parse vendor dd
        GerWebAppType jettyWebApp = getJettyWebApp(plan, moduleFile, standAlone, targetPath, webApp);
        if (contextRoot == null) {
            contextRoot = jettyWebApp.getContextRoot();
        }

        Map servletNameToPathMap = buildServletNameToPathMap(webApp, contextRoot);

        //look for a webservices dd
        Map portMap = Collections.EMPTY_MAP;
        try {
            URL wsDDUrl = DeploymentUtil.createJarURL(moduleFile, "WEB-INF/webservices.xml");
            portMap = webServiceBuilder.parseWebServiceDescriptor(wsDDUrl, moduleFile, false, servletNameToPathMap);
        } catch (MalformedURLException e) {
            //no descriptor
        }

        // get the ids from either the application plan or for a stand alone module from the specific deployer
        URI configId = null;
        try {
            configId = new URI(jettyWebApp.getConfigId());
        } catch (URISyntaxException e) {
            throw new DeploymentException("Invalid configId " + jettyWebApp.getConfigId(), e);
        }

        URI parentId = null;
        if (jettyWebApp.isSetParentId()) {
            try {
                parentId = new URI(jettyWebApp.getParentId());
            } catch (URISyntaxException e) {
                throw new DeploymentException("Invalid parentId " + jettyWebApp.getParentId(), e);
            }
        } else {
            parentId = defaultParentId;
        }

        WebModule module = new WebModule(standAlone, configId, parentId, moduleFile, targetPath, webApp, jettyWebApp, specDD, portMap);
        module.setContextRoot(contextRoot);
        return module;
    }

    /**
     * Some servlets will have multiple url patterns.  However, webservice servlets
     * will only have one, which is what this method is intended for.
     *
     * @param webApp
     * @param contextRoot
     * @return
     */
    private Map buildServletNameToPathMap(WebAppType webApp, String contextRoot) {
        contextRoot = "/" + contextRoot;
        Map map = new HashMap();
        ServletMappingType[] servletMappings = webApp.getServletMappingArray();
        for (int j = 0; j < servletMappings.length; j++) {
            ServletMappingType servletMapping = servletMappings[j];
            String servletName = servletMapping.getServletName().getStringValue().trim();
            map.put(servletName, contextRoot + servletMapping.getUrlPattern().getStringValue());
        }
        return map;
    }

    GerWebAppType getJettyWebApp(Object plan, JarFile moduleFile, boolean standAlone, String targetPath, WebAppType webApp) throws DeploymentException {
        GerWebAppType jettyWebApp = null;
        try {
            // load the geronimo-web.xml from either the supplied plan or from the earFile
            try {
                if (plan instanceof XmlObject) {
                    XmlObject object = SchemaConversionUtils.getNestedObject((XmlObject) plan, "web-app");
                    jettyWebApp = TemporaryPlanAdapter.convertJettyElementToWeb(object);
                } else {
                    GerWebAppDocument jettyWebAppdoc = null;
                    if (plan != null) {
                        XmlObject object = SchemaConversionUtils.parse(((File) plan).toURL());
                        jettyWebAppdoc = TemporaryPlanAdapter.convertJettyDocumentToWeb(object);
                    } else {
                        URL path = DeploymentUtil.createJarURL(moduleFile, "WEB-INF/geronimo-web.xml");
                        try {
                            jettyWebAppdoc = GerWebAppDocument.Factory.parse(path);
                        } catch (FileNotFoundException e) {
                            path = DeploymentUtil.createJarURL(moduleFile, "WEB-INF/geronimo-jetty.xml");
                            try {
                                XmlObject object = SchemaConversionUtils.parse(path);
                                if (object != null) {
                                    log.error("Incorrect deployment plan naming: found geronimo-jetty.xml, should be geronimo-web.xml");
                                    jettyWebAppdoc = TemporaryPlanAdapter.convertJettyDocumentToWeb(object);
                                }
                            } catch (FileNotFoundException e1) {
                                log.warn("Web application does not contain a WEB-INF/geronimo-web.xml deployment plan.  This may or may not be a problem, depending on whether you have things like resource references that need to be resolved.  You can also give the deployer a separate deployment plan file on the command line.");
                            }
                        }
                    }
                    if (jettyWebAppdoc != null) {
                        jettyWebApp = jettyWebAppdoc.getWebApp();
                    }
                }
            } catch (IOException e) {
                log.warn(e);
            }

            // if we got one extract and validate it otherwise create a default one
            if (jettyWebApp != null) {
                jettyWebApp = (GerWebAppType) SchemaConversionUtils.convertToGeronimoNamingSchema(jettyWebApp);
                jettyWebApp = (GerWebAppType) SchemaConversionUtils.convertToGeronimoSecuritySchema(jettyWebApp);
                jettyWebApp = (GerWebAppType) SchemaConversionUtils.convertToGeronimoServiceSchema(jettyWebApp);
                SchemaConversionUtils.validateDD(jettyWebApp);
            } else {
                String path;
                if (standAlone) {
                    // default configId is based on the moduleFile name
                    path = new File(moduleFile.getName()).getName();
                } else {
                    // default configId is based on the module uri from the application.xml
                    path = targetPath;
                }
                jettyWebApp = createDefaultPlan(path, webApp);
            }
        } catch (XmlException e) {
            e.printStackTrace();
            throw new DeploymentException("xml problem", e);
        }
        return jettyWebApp;
    }

    private GerWebAppType createDefaultPlan(String path, WebAppType webApp) {
        String id = webApp.getId();
        if (id == null) {
            id = path;
            if (id.endsWith(".war")) {
                id = id.substring(0, id.length() - 4);
            }
            if (id.endsWith("/")) {
                id = id.substring(0, id.length() - 1);
            }
        }

        GerWebAppType jettyWebApp = GerWebAppType.Factory.newInstance();

        // set the parentId, configId and context root
        jettyWebApp.setParentId(defaultParentId.toString());
        if (null != webApp.getId()) {
            id = webApp.getId();
        }
        jettyWebApp.setConfigId(id);
        jettyWebApp.setContextRoot(id);
        return jettyWebApp;
    }

    public void installModule(JarFile earFile, EARContext earContext, Module module) throws DeploymentException {
        try {
            URI baseDir = URI.create(module.getTargetPath() + "/");

            // add the warfile's content to the configuration
            JarFile warFile = module.getModuleFile();
            Enumeration entries = warFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                URI targetPath = baseDir.resolve(new URI(null, entry.getName(), null));
                if (entry.getName().equals("WEB-INF/web.xml")) {
                    earContext.addFile(targetPath, module.getOriginalSpecDD());
                } else {
                    earContext.addFile(targetPath, warFile, entry);
                }
            }

            // add the manifest classpath entries declared in the war to the class loader
            // we have to explicitly add these since we are unpacking the web module
            // and the url class loader will not pick up a manifiest from an unpacked dir
            earContext.addManifestClassPath(warFile, URI.create(module.getTargetPath()));

            // add the dependencies declared in the geronimo-web.xml file
            GerWebAppType jettyWebApp = (GerWebAppType) module.getVendorDD();
            DependencyType[] dependencies = jettyWebApp.getDependencyArray();
            ServiceConfigBuilder.addDependencies(earContext, dependencies, repository);
        } catch (IOException e) {
            throw new DeploymentException("Problem deploying war", e);
        } catch (URISyntaxException e) {
            throw new DeploymentException("Could not construct URI for location of war entry", e);
        }
    }

    public void initContext(EARContext earContext, Module module, ClassLoader cl) {
        // web application do not add anything to the shared context
    }

    public void addGBeans(EARContext earContext, Module module, ClassLoader cl) throws DeploymentException {
        J2eeContext earJ2eeContext = earContext.getJ2eeContext();
        J2eeContext moduleJ2eeContext = J2eeContextImpl.newModuleContextFromApplication(earJ2eeContext, NameFactory.WEB_MODULE, module.getName());
        WebModule webModule = (WebModule) module;

        WebAppType webApp = (WebAppType) webModule.getSpecDD();
        GerWebAppType jettyWebApp = (GerWebAppType) webModule.getVendorDD();

        boolean contextPriorityClassLoader = false;
        if (jettyWebApp != null) {
            contextPriorityClassLoader = Boolean.valueOf(jettyWebApp.getContextPriorityClassloader()).booleanValue();
        }
        // construct the webClassLoader
        ClassLoader webClassLoader = getWebClassLoader(earContext, webModule, cl, contextPriorityClassLoader);

        if (jettyWebApp != null) {
            GbeanType[] gbeans = jettyWebApp.getGbeanArray();
            ServiceConfigBuilder.addGBeans(gbeans, webClassLoader, moduleJ2eeContext, earContext);
        }

        ObjectName webModuleName = null;
        try {
            webModuleName = NameFactory.getModuleName(null, null, null, null, null, moduleJ2eeContext);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Could not construct module name", e);
        }

        UserTransaction userTransaction = new OnlineUserTransaction();
        //this may add to the web classpath with enhanced classes.
        Map compContext = buildComponentContext(earContext, webModule, webApp, jettyWebApp, userTransaction, webClassLoader);

        GBeanData webModuleData = new GBeanData(webModuleName, JettyWebAppContext.GBEAN_INFO);
        try {
            webModuleData.setReferencePattern("J2EEServer", earContext.getServerObjectName());
            if (!earContext.getJ2EEApplicationName().equals("null")) {
                webModuleData.setReferencePattern("J2EEApplication", earContext.getApplicationObjectName());
            }

            webModuleData.setAttribute("deploymentDescriptor", module.getOriginalSpecDD());
            Set securityRoles = collectRoleNames(webApp);
            Map rolePermissions = new HashMap();

            webModuleData.setAttribute("uri", URI.create(module.getTargetPath() + "/"));
            webModuleData.setAttribute("componentContext", compContext);
            webModuleData.setAttribute("userTransaction", userTransaction);
            //classpath may have been augmented with enhanced classes
            webModuleData.setAttribute("webClassPath", webModule.getWebClasspath());
            // unsharableResources, applicationManagedSecurityResources
            GBeanResourceEnvironmentBuilder rebuilder = new GBeanResourceEnvironmentBuilder(webModuleData);
            ENCConfigBuilder.setResourceEnvironment(earContext, webModule.getModuleURI(), rebuilder, webApp.getResourceRefArray(), jettyWebApp.getResourceRefArray());

            webModuleData.setAttribute("contextPath", webModule.getContextRoot());
            webModuleData.setAttribute("contextPriorityClassLoader", Boolean.valueOf(contextPriorityClassLoader));

            webModuleData.setReferencePattern("TransactionContextManager", earContext.getTransactionContextManagerObjectName());
            webModuleData.setReferencePattern("TrackedConnectionAssociator", earContext.getConnectionTrackerObjectName());
            webModuleData.setReferencePattern("JettyContainer", jettyContainerObjectName);
            //stuff that jetty used to do
            if (webApp.getDisplayNameArray().length > 0) {
                webModuleData.setAttribute("displayName", webApp.getDisplayNameArray()[0].getStringValue());
            }

            ParamValueType[] contextParamArray = webApp.getContextParamArray();
            Map contextParams = new HashMap();
            for (int i = 0; i < contextParamArray.length; i++) {
                ParamValueType contextParam = contextParamArray[i];
                contextParams.put(contextParam.getParamName().getStringValue().trim(), contextParam.getParamValue().getStringValue().trim());
            }
            webModuleData.setAttribute("contextParamMap", contextParams);

            ListenerType[] listenerArray = webApp.getListenerArray();
            Collection listeners = new ArrayList();
            for (int i = 0; i < listenerArray.length; i++) {
                ListenerType listenerType = listenerArray[i];
                listeners.add(listenerType.getListenerClass().getStringValue());
            }
            webModuleData.setAttribute("listenerClassNames", listeners);

            webModuleData.setAttribute("distributable", webApp.getDistributableArray().length == 1 ? Boolean.TRUE : Boolean.FALSE);

            webModuleData.setAttribute("sessionTimeoutSeconds",
                    (webApp.getSessionConfigArray().length == 1 && webApp.getSessionConfigArray(0).getSessionTimeout() != null) ?
                    new Integer(webApp.getSessionConfigArray(0).getSessionTimeout().getBigIntegerValue().intValue() * 60) :
                    defaultSessionTimeoutSeconds);

            MimeMappingType[] mimeMappingArray = webApp.getMimeMappingArray();
            Map mimeMappingMap = new HashMap();
            for (int i = 0; i < mimeMappingArray.length; i++) {
                MimeMappingType mimeMappingType = mimeMappingArray[i];
                mimeMappingMap.put(mimeMappingType.getExtension().getStringValue(), mimeMappingType.getMimeType().getStringValue());
            }
            webModuleData.setAttribute("mimeMap", mimeMappingMap);

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

            JspConfigType[] jspConfigArray = webApp.getJspConfigArray();
            if (jspConfigArray.length > 1) {
                throw new DeploymentException("At most one jsp-config element, not " + jspConfigArray.length);
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

            LoginConfigType[] loginConfigArray = webApp.getLoginConfigArray();
            if (loginConfigArray.length > 1) {
                throw new DeploymentException("At most one login-config element, not " + loginConfigArray.length);
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

            }
            earContext.addGBean(webModuleData);

            //never add a duplicate pattern.
            Set knownServletMappings = new HashSet();

            ServletMappingType[] servletMappingArray = webApp.getServletMappingArray();
            Map servletMappings = new HashMap();
            for (int i = 0; i < servletMappingArray.length; i++) {
                ServletMappingType servletMappingType = servletMappingArray[i];
                String servletName = servletMappingType.getServletName().getStringValue().trim();
                String urlPattern = servletMappingType.getUrlPattern().getStringValue();
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

            //"previous" filter mapping for linked list to keep dd's ordering.
            ObjectName previous = null;

            //add default filters
            if (defaultFilters != null) {
                for (Iterator iterator = defaultFilters.iterator(); iterator.hasNext();) {
                    Object defaultFilter = iterator.next();
                    GBeanData filterGBeanData = getGBeanData(kernel, defaultFilter);
                    String filterName = (String) filterGBeanData.getAttribute("filterName");
                    ObjectName defaultFilterObjectName = NameFactory.getWebComponentName(null, null, null, null, filterName, NameFactory.WEB_FILTER, moduleJ2eeContext);
                    filterGBeanData.setName(defaultFilterObjectName);
                    filterGBeanData.setReferencePattern("JettyServletRegistration", webModuleName);
                    earContext.addGBean(filterGBeanData);
                    //add a mapping to /*

                    GBeanData filterMappingGBeanData = new GBeanData(JettyFilterMapping.GBEAN_INFO);
                    filterMappingGBeanData.setReferencePattern("Previous", previous);
                    filterMappingGBeanData.setReferencePattern("JettyServletRegistration", webModuleName);
                    String urlPattern = "/*";
                    filterMappingGBeanData.setAttribute("urlPattern", urlPattern);
                    ObjectName filterMappingName = NameFactory.getWebFilterMappingName(null, null, null, null, filterName, null, urlPattern, moduleJ2eeContext);
                    filterMappingGBeanData.setName(filterMappingName);
                    previous = filterMappingName;


                    filterMappingGBeanData.setAttribute("requestDispatch", Boolean.TRUE);
                    filterMappingGBeanData.setAttribute("forwardDispatch", Boolean.TRUE);
                    filterMappingGBeanData.setAttribute("includeDispatch", Boolean.TRUE);
                    filterMappingGBeanData.setAttribute("errorDispatch", Boolean.FALSE);
                    filterMappingGBeanData.setReferencePattern("Filter", defaultFilterObjectName);
                    earContext.addGBean(filterMappingGBeanData);
                }
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
//                        defaultFilterMappingObjectName = NameFactory.getWebFilterMappingName(null, null, null, null, filterName, null, urlPattern, moduleJ2eeContext);
//                    } else {
//                        Set servletNames = filterMappingGBeanData.getReferencePatterns("Servlet");
//                        if (servletNames == null || servletNames.size() != 1) {
//                            throw new DeploymentException("Exactly one servlet name must be supplied");
//                        }
//                        ObjectName servletObjectName = (ObjectName) servletNames.iterator().next();
//                        String servletName = servletObjectName.getKeyProperty("name");
//                        defaultFilterMappingObjectName = NameFactory.getWebFilterMappingName(null, null, null, null, filterName, servletName, null, moduleJ2eeContext);
//                    }
//                    filterMappingGBeanData.setName(defaultFilterMappingObjectName);
//                    filterMappingGBeanData.setReferencePattern("JettyFilterMappingRegistration", webModuleName);
//                    earContext.addGBean(filterMappingGBeanData);
//                }
//            }

            FilterMappingType[] filterMappingArray = webApp.getFilterMappingArray();
            for (int i = 0; i < filterMappingArray.length; i++) {
                FilterMappingType filterMappingType = filterMappingArray[i];
                String filterName = filterMappingType.getFilterName().getStringValue().trim();
                GBeanData filterMappingData = new GBeanData(JettyFilterMapping.GBEAN_INFO);
                filterMappingData.setReferencePattern("Previous", previous);
                filterMappingData.setReferencePattern("JettyServletRegistration", webModuleName);

                ObjectName filterMappingName = null;
                if (filterMappingType.isSetUrlPattern()) {
                    //do not trim!
                    String urlPattern = filterMappingType.getUrlPattern().getStringValue();
                    filterMappingData.setAttribute("urlPattern", urlPattern);
                    filterMappingName = NameFactory.getWebFilterMappingName(null, null, null, null, filterName, null, urlPattern, moduleJ2eeContext);
                }
                if (filterMappingType.isSetServletName()) {
                    String servletName = filterMappingType.getServletName().getStringValue().trim();
                    ObjectName servletObjectName = NameFactory.getWebComponentName(null, null, null, null, servletName, NameFactory.SERVLET, moduleJ2eeContext);
                    filterMappingData.setReferencePattern("Servlet", servletObjectName);
                    filterMappingName = NameFactory.getWebFilterMappingName(null, null, null, null, filterName, servletName, null, moduleJ2eeContext);
                }
                filterMappingData.setName(filterMappingName);
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
                ObjectName filterObjectName = NameFactory.getWebComponentName(null, null, null, null, filterName, NameFactory.WEB_FILTER, moduleJ2eeContext);
                filterMappingData.setReferencePattern("Filter", filterObjectName);
                earContext.addGBean(filterMappingData);
            }

            FilterType[] filterArray = webApp.getFilterArray();
            for (int i = 0; i < filterArray.length; i++) {
                FilterType filterType = filterArray[i];
                String filterName = filterType.getFilterName().getStringValue().trim();
                ObjectName filterObjectName = NameFactory.getWebComponentName(null, null, null, null, filterName, NameFactory.WEB_FILTER, moduleJ2eeContext);
                GBeanData filterData = new GBeanData(filterObjectName, JettyFilterHolder.GBEAN_INFO);
                filterData.setAttribute("filterName", filterName);
                filterData.setAttribute("filterClass", filterType.getFilterClass().getStringValue().trim());
                Map initParams = new HashMap();
                ParamValueType[] initParamArray = filterType.getInitParamArray();
                for (int j = 0; j < initParamArray.length; j++) {
                    ParamValueType paramValueType = initParamArray[j];
                    initParams.put(paramValueType.getParamName().getStringValue().trim(), paramValueType.getParamValue().getStringValue().trim());
                }
                filterData.setAttribute("initParams", initParams);
                filterData.setReferencePattern("JettyServletRegistration", webModuleName);
                earContext.addGBean(filterData);
            }

            //add default servlets
            if (defaultServlets != null) {
                for (Iterator iterator = defaultServlets.iterator(); iterator.hasNext();) {
                    Object defaultServlet = iterator.next();
                    GBeanData servletGBeanData = getGBeanData(kernel, defaultServlet);
                    ObjectName defaultServletObjectName = NameFactory.getWebComponentName(null, null, null, null, (String) servletGBeanData.getAttribute("servletName"), NameFactory.SERVLET, moduleJ2eeContext);
                    servletGBeanData.setName(defaultServletObjectName);
                    servletGBeanData.setReferencePattern("JettyServletRegistration", webModuleName);
                    Set defaultServletMappings = new HashSet((Collection) servletGBeanData.getAttribute("servletMappings"));
                    defaultServletMappings.removeAll(knownServletMappings);
                    servletGBeanData.setAttribute("servletMappings", defaultServletMappings);
                    earContext.addGBean(servletGBeanData);
                }
            }

            //set up servlet gbeans.
            ServletType[] servletTypes = webApp.getServletArray();
            Map portMap = webModule.getPortMap();

            addServlets(webModuleName, webModule.getModuleFile(), servletTypes, servletMappings, securityRoles, rolePermissions, portMap, webClassLoader, moduleJ2eeContext, earContext);

            if (jettyWebApp.isSetSecurityRealmName()) {
                String securityRealmName = jettyWebApp.getSecurityRealmName().trim();
                webModuleData.setAttribute("securityRealmName", securityRealmName);
//                webModuleData.setAttribute("securityConfig", security);

                /**
                 * TODO - go back to commented version when possible.
                 */
                String policyContextID = webModuleName.getCanonicalName().replaceAll("[, :]", "_");
                //String policyContextID = webModuleName.getCanonicalName();
                webModuleData.setAttribute("policyContextID", policyContextID);
//                webModuleData.setAttribute("securityRoles", securityRoles);

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
                if (jettyWebApp.isSetSecurity()) {
                    SecurityConfiguration securityConfiguration = SecurityBuilder.buildSecurityConfiguration(jettyWebApp.getSecurity());
                    earContext.setSecurityConfiguration(securityConfiguration);
                }
                DefaultPrincipal defaultPrincipal = earContext.getSecurityConfiguration().getDefaultPrincipal();
                webModuleData.setAttribute("defaultPrincipal", defaultPrincipal);

                webModuleData.setReferencePattern("RoleDesignateSource", earContext.getJaccManagerName());
            }
        } catch (DeploymentException de) {
            throw de;
        } catch (Exception e) {
            throw new DeploymentException("Unable to initialize webapp GBean", e);
        }
    }

    private ClassLoader getWebClassLoader(EARContext earContext, WebModule webModule, ClassLoader cl, boolean contextPriorityClassLoader) throws DeploymentException {
        getWebClassPath(earContext, webModule);
        URI[] webClassPath = webModule.getWebClasspath();
        URI baseUri = earContext.getBaseDir().toURI();
        URL baseUrl = null;
        try {
            baseUrl = baseUri.resolve(webModule.getTargetPathURI()).toURL();
        } catch (MalformedURLException e) {
            throw new DeploymentException("Invalid module location: " + webModule.getTargetPathURI() + ", baseUri: " + baseUri);
        }
        URL[] webClassPathURLs = new URL[webClassPath.length];
        for (int i = 0; i < webClassPath.length; i++) {
            URI path = baseUri.resolve(webClassPath[i]);
            try {
                webClassPathURLs[i] = path.toURL();
            } catch (MalformedURLException e) {
                throw new DeploymentException("Invalid web class path element: path=" + path + ", baseUri=" + baseUri);
            }
        }

        ClassLoader webClassLoader = new JettyClassLoader(webClassPathURLs, baseUrl, cl, contextPriorityClassLoader);
        return webClassLoader;
    }


    /**
     * Adds the provided servlets, taking into account the load-on-startup ordering.
     *
     * @param webModuleName     an <code>ObjectName</code> value
     * @param moduleFile        a <code>JarFile</code> value
     * @param servletTypes      a <code>ServletType[]</code> value, contains the <code>servlet</code> entries from <code>web.xml</code>.
     * @param servletMappings   a <code>Map</code> value
     * @param securityRoles     a <code>Set</code> value
     * @param rolePermissions   a <code>Map</code> value
     * @param portMap           a <code>Map</code> value
     * @param webClassLoader    a <code>ClassLoader</code> value
     * @param moduleJ2eeContext a <code>J2eeContext</code> value
     * @param earContext        an <code>EARContext</code> value
     * @throws MalformedObjectNameException if an error occurs
     * @throws DeploymentException          if an error occurs
     */
    private void addServlets(ObjectName webModuleName,
                             JarFile moduleFile,
                             ServletType[] servletTypes,
                             Map servletMappings,
                             Set securityRoles,
                             Map rolePermissions, Map portMap,
                             ClassLoader webClassLoader,
                             J2eeContext moduleJ2eeContext,
                             EARContext earContext) throws MalformedObjectNameException, DeploymentException {

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
        // o.a.g.jetty.JettyFilterMapping which provided the example
        // of how to do this.
        // http://issues.apache.org/jira/browse/GERONIMO-645
        ServletType previousServlet = null;
        for (Iterator servlets = loadOrder.iterator(); servlets.hasNext();) {
            ServletType servletType = (ServletType) servlets.next();
            addServlet(webModuleName, moduleFile, previousServlet, servletType, servletMappings, securityRoles, rolePermissions, portMap, webClassLoader, moduleJ2eeContext, earContext);
            previousServlet = servletType;
        }

        // JACC v1.0 secion B.19
        addUnmappedJSPPermissions(securityRoles, rolePermissions);
    }

    private void addUnmappedJSPPermissions(Set securityRoles, Map rolePermissions) {
        for (Iterator iter = securityRoles.iterator(); iter.hasNext();) {
            String roleName = (String) iter.next();
            addPermissionToRole(roleName, new WebRoleRefPermission("", roleName), rolePermissions);
        }
    }

    private void addServlet(ObjectName webModuleName,
                            JarFile moduleFile,
                            ServletType previousServlet,
                            ServletType servletType,
                            Map servletMappings,
                            Set securityRoles,
                            Map rolePermissions, Map portMap,
                            ClassLoader webClassLoader,
                            J2eeContext moduleJ2eeContext,
                            EARContext earContext) throws MalformedObjectNameException, DeploymentException {
        String servletName = servletType.getServletName().getStringValue().trim();
        ObjectName servletObjectName = NameFactory.getWebComponentName(null, null, null, null, servletName, NameFactory.SERVLET, moduleJ2eeContext);
        GBeanData servletData;
        if (servletType.isSetServletClass()) {
            String servletClassName = servletType.getServletClass().getStringValue().trim();
            Class servletClass = null;
            try {
                servletClass = webClassLoader.loadClass(servletClassName);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Could not load servlet class " + servletClassName, e);
            }
            Class baseServletClass = null;
            try {
                baseServletClass = webClassLoader.loadClass(Servlet.class.getName());
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Could not load javax.servlet.Servlet in web classloader", e);
            }
            if (baseServletClass.isAssignableFrom(servletClass)) {
                servletData = new GBeanData(servletObjectName, JettyServletHolder.GBEAN_INFO);
                servletData.setAttribute("servletClass", servletClassName);
            } else {
                servletData = new GBeanData(pojoWebServiceTemplate);
                servletData.setName(servletObjectName);
                //let the web service builder deal with configuring the gbean with the web service stack
                Object portInfo = portMap.get(servletName);
                if (portInfo == null) {
                    throw new DeploymentException("No web service deployment info for servlet name " + servletName);
                }
                webServiceBuilder.configurePOJO(servletData, moduleFile, portInfo, servletClassName, webClassLoader);
            }
        } else if (servletType.isSetJspFile()) {
            servletData = new GBeanData(servletObjectName, JettyServletHolder.GBEAN_INFO);
            servletData.setAttribute("jspFile", servletType.getJspFile().getStringValue().trim());
            //TODO MAKE THIS CONFIGURABLE!!! Jetty uses the servlet mapping set up from the default-web.xml
            servletData.setAttribute("servletClass", "org.apache.jasper.servlet.JspServlet");
        } else {
            throw new DeploymentException("Neither servlet class nor jsp file is set for " + servletName);
        }

        // link to previous servlet, if there is one, so that we
        // preserve the <load-on-startup> ordering.
        // http://issues.apache.org/jira/browse/GERONIMO-645
        if (null != previousServlet) {
            String name = previousServlet.getServletName().getStringValue().trim();
            ObjectName oName = NameFactory.getWebComponentName(null, null, null, null, name, NameFactory.SERVLET, moduleJ2eeContext);
            servletData.setReferencePattern("Previous", oName);
        }

        //TODO in init param setter, add classpath if jspFile is not null.
        servletData.setReferencePattern("JettyServletRegistration", webModuleName);
        servletData.setAttribute("servletName", servletName);
        Map initParams = new HashMap();
        ParamValueType[] initParamArray = servletType.getInitParamArray();
        for (int j = 0; j < initParamArray.length; j++) {
            ParamValueType paramValueType = initParamArray[j];
            initParams.put(paramValueType.getParamName().getStringValue().trim(), paramValueType.getParamValue().getStringValue().trim());
        }
        servletData.setAttribute("initParams", initParams);
        if (servletType.isSetLoadOnStartup()) {
            Integer loadOnStartup = new Integer(servletType.getLoadOnStartup().getBigIntegerValue().intValue());
            servletData.setAttribute("loadOnStartup", loadOnStartup);
        }

        Set mappings = (Set) servletMappings.get(servletName);
        servletData.setAttribute("servletMappings", mappings == null ? Collections.EMPTY_SET : mappings);


        //WebRoleRefPermissions
        SecurityRoleRefType[] securityRoleRefTypeArray = servletType.getSecurityRoleRefArray();
        Set unmappedRoles = new HashSet(securityRoles);
        for (int j = 0; j < securityRoleRefTypeArray.length; j++) {
            SecurityRoleRefType securityRoleRefType = securityRoleRefTypeArray[j];
            String roleName = securityRoleRefType.getRoleName().getStringValue().trim();
            String roleLink = securityRoleRefType.getRoleLink().getStringValue().trim();
            //jacc 3.1.3.2
            /*   The name of the WebRoleRefPermission must be the servlet-name in whose
            * context the security-role-ref is defined. The actions of the  WebRoleRefPermission
            * must be the value of the role-name (that is the  reference), appearing in the security-role-ref.
            * The deployment tools must  call the addToRole method on the PolicyConfiguration object to add the
            * WebRoleRefPermission object resulting from the translation to the role
            * identified in the role-link appearing in the security-role-ref.
            */
            addPermissionToRole(roleLink, new WebRoleRefPermission(servletName, roleName), rolePermissions);
            unmappedRoles.remove(roleName);
        }
        for (Iterator iterator = unmappedRoles.iterator(); iterator.hasNext();) {
            String roleName = (String) iterator.next();
            addPermissionToRole(roleName, new WebRoleRefPermission(servletName, roleName), rolePermissions);
        }
//        servletData.setAttribute("webRoleRefPermissions", webRoleRefPermissions);

        earContext.addGBean(servletData);
    }

    private ComponentPermissions buildSpecSecurityConfig(WebAppType webApp, Set securityRoles, Map rolePermissions) {
        Map uncheckedPatterns = new HashMap();
        Map uncheckedResourcePatterns = new HashMap();
        Map uncheckedUserPatterns = new HashMap();
        Map excludedPatterns = new HashMap();
        Map rolesPatterns = new HashMap();
        Set allSet = new HashSet();   // == allMap.values()
        Map allMap = new HashMap();   //uncheckedPatterns union excludedPatterns union rolesPatterns.

        SecurityConstraintType[] securityConstraintArray = webApp.getSecurityConstraintArray();
        for (int i = 0; i < securityConstraintArray.length; i++) {
            SecurityConstraintType securityConstraintType = securityConstraintArray[i];
            Map currentPatterns;
            if (securityConstraintType.isSetAuthConstraint()) {
                if (securityConstraintType.getAuthConstraint().getRoleNameArray().length == 0) {
                    currentPatterns = excludedPatterns;
                } else {
                    currentPatterns = rolesPatterns;
                }
            } else {
                currentPatterns = uncheckedPatterns;
            }

            String transport = "";
            if (securityConstraintType.isSetUserDataConstraint()) {
                transport = securityConstraintType.getUserDataConstraint().getTransportGuarantee().getStringValue().trim().toUpperCase();
            }

            WebResourceCollectionType[] webResourceCollectionTypeArray = securityConstraintType.getWebResourceCollectionArray();
            for (int j = 0; j < webResourceCollectionTypeArray.length; j++) {
                WebResourceCollectionType webResourceCollectionType = webResourceCollectionTypeArray[j];
                UrlPatternType[] urlPatternTypeArray = webResourceCollectionType.getUrlPatternArray();
                for (int k = 0; k < urlPatternTypeArray.length; k++) {
                    UrlPatternType urlPatternType = urlPatternTypeArray[k];
                    //presumably, don't trim
                    String url = urlPatternType.getStringValue();
                    URLPattern pattern = (URLPattern) currentPatterns.get(url);
                    if (pattern == null) {
                        pattern = new URLPattern(url);
                        currentPatterns.put(url, pattern);
                    }

                    URLPattern allPattern = (URLPattern) allMap.get(url);
                    if (allPattern == null) {
                        allPattern = new URLPattern(url);
                        allSet.add(allPattern);
                        allMap.put(url, allPattern);
                    }

                    HttpMethodType[] httpMethodTypeArray = webResourceCollectionType.getHttpMethodArray();
                    if (httpMethodTypeArray.length == 0) {
                        pattern.addMethod("");
                        allPattern.addMethod("");
                    } else {
                        for (int l = 0; l < httpMethodTypeArray.length; l++) {
                            HttpMethodType httpMethodType = httpMethodTypeArray[l];
                            //TODO is trim OK?
                            String method = httpMethodType.getStringValue().trim();
                            pattern.addMethod(method);
                            allPattern.addMethod(method);
                        }
                    }
                    if (currentPatterns == rolesPatterns) {
                        RoleNameType[] roleNameTypeArray = securityConstraintType.getAuthConstraint().getRoleNameArray();
                        for (int l = 0; l < roleNameTypeArray.length; l++) {
                            RoleNameType roleNameType = roleNameTypeArray[l];
                            String role = roleNameType.getStringValue().trim();
                            if (role.equals("*")) {
                                pattern.addAllRoles(securityRoles);
                            } else {
                                pattern.addRole(role);
                            }
                        }
                    }

                    pattern.setTransport(transport);
                }
            }
        }

        PermissionCollection excludedPermissions = new Permissions();
        PermissionCollection uncheckedPermissions = new Permissions();

        Iterator iter = excludedPatterns.keySet().iterator();
        while (iter.hasNext()) {
            URLPattern pattern = (URLPattern) excludedPatterns.get(iter.next());
            String name = pattern.getQualifiedPattern(allSet);
            String actions = pattern.getMethods();

            excludedPermissions.add(new WebResourcePermission(name, actions));
            excludedPermissions.add(new WebUserDataPermission(name, actions));
        }

        iter = rolesPatterns.keySet().iterator();
        while (iter.hasNext()) {
            URLPattern pattern = (URLPattern) rolesPatterns.get(iter.next());
            String name = pattern.getQualifiedPattern(allSet);
            String actions = pattern.getMethods();
            WebResourcePermission permission = new WebResourcePermission(name, actions);

            for (Iterator names = pattern.getRoles().iterator(); names.hasNext();) {
                String roleName = (String) names.next();
                addPermissionToRole(roleName, permission, rolePermissions);
            }
        }

        iter = uncheckedPatterns.keySet().iterator();
        while (iter.hasNext()) {
            URLPattern pattern = (URLPattern) uncheckedPatterns.get(iter.next());
            String name = pattern.getQualifiedPattern(allSet);
            String actions = pattern.getMethods();

            addOrUpdatePattern(uncheckedResourcePatterns, name, actions);
        }

        iter = rolesPatterns.keySet().iterator();
        while (iter.hasNext()) {
            URLPattern pattern = (URLPattern) rolesPatterns.get(iter.next());
            String name = pattern.getQualifiedPattern(allSet);
            String actions = pattern.getMethodsWithTransport();

            addOrUpdatePattern(uncheckedUserPatterns, name, actions);
        }

        iter = uncheckedPatterns.keySet().iterator();
        while (iter.hasNext()) {
            URLPattern pattern = (URLPattern) uncheckedPatterns.get(iter.next());
            String name = pattern.getQualifiedPattern(allSet);
            String actions = pattern.getMethodsWithTransport();

            addOrUpdatePattern(uncheckedUserPatterns, name, actions);
        }

        /**
         * A <code>WebResourcePermission</code> and a <code>WebUserDataPermission</code> must be instantiated for
         * each <tt>url-pattern</tt> in the deployment descriptor and the default pattern "/", that is not combined
         * by the <tt>web-resource-collection</tt> elements of the deployment descriptor with ever HTTP method
         * value.  The permission objects must be contructed using the qualified pattern as their name and with
         * actions defined by the subset of the HTTP methods that do not occur in combination with the pattern.
         * The resulting permissions that must be added to the unchecked policy statements by calling the
         * <code>addToUncheckedPolcy</code> method on the <code>PolicyConfiguration</code> object.
         */
        iter = allSet.iterator();
        while (iter.hasNext()) {
            URLPattern pattern = (URLPattern) iter.next();
            String name = pattern.getQualifiedPattern(allSet);
            String actions = pattern.getComplementedMethods();

            if (actions.length() == 0) {
                continue;
            }

            addOrUpdatePattern(uncheckedResourcePatterns, name, actions);
            addOrUpdatePattern(uncheckedUserPatterns, name, actions);
        }

        URLPattern pattern = new URLPattern("/");
        if (!allSet.contains(pattern)) {
            String name = pattern.getQualifiedPattern(allSet);
            String actions = pattern.getComplementedMethods();

            addOrUpdatePattern(uncheckedResourcePatterns, name, actions);
            addOrUpdatePattern(uncheckedUserPatterns, name, actions);
        }

        //Create the uncheckedPermissions for WebResourcePermissions
        iter = uncheckedResourcePatterns.keySet().iterator();
        while (iter.hasNext()) {
            UncheckedItem item = (UncheckedItem) iter.next();
            String actions = (String) uncheckedResourcePatterns.get(item);

            uncheckedPermissions.add(new WebResourcePermission(item.getName(), actions));
        }
        //Create the uncheckedPermissions for WebUserDataPermissions
        iter = uncheckedUserPatterns.keySet().iterator();
        while (iter.hasNext()) {
            UncheckedItem item = (UncheckedItem) iter.next();
            String actions = (String) uncheckedUserPatterns.get(item);

            uncheckedPermissions.add(new WebUserDataPermission(item.getName(), actions));
        }

        ComponentPermissions componentPermissions = new ComponentPermissions(excludedPermissions, uncheckedPermissions, rolePermissions);
        return componentPermissions;

    }

    private void addPermissionToRole(String roleName, Permission permission, Map rolePermissions) {
        PermissionCollection permissionsForRole = (PermissionCollection) rolePermissions.get(roleName);
        if (permissionsForRole == null) {
            permissionsForRole = new Permissions();
            rolePermissions.put(roleName, permissionsForRole);
        }
        permissionsForRole.add(permission);
    }

    private void addOrUpdatePattern(Map patternMap, String name, String actions) {
        UncheckedItem item = new UncheckedItem(name, actions);
        String existingActions = (String) patternMap.get(item);
        if (existingActions != null) {
            patternMap.put(item, actions + "," + existingActions);
            return;
        }

        patternMap.put(item, actions);
    }

    private static Set collectRoleNames(WebAppType webApp) {
        Set roleNames = new HashSet();

        SecurityRoleType[] securityRoles = webApp.getSecurityRoleArray();
        for (int i = 0; i < securityRoles.length; i++) {
            roleNames.add(securityRoles[i].getRoleName().getStringValue().trim());
        }

        return roleNames;
    }

    private static void getWebClassPath(EARContext earContext, WebModule webModule) {
        File baseDir = earContext.getTargetFile(webModule.getTargetPathURI());
        File webInfDir = new File(baseDir, "WEB-INF");

        // check for a classes dir
        File classesDir = new File(webInfDir, "classes");
        if (classesDir.isDirectory()) {
            webModule.addToWebClasspath(webModule.getTargetPathURI().resolve(URI.create("WEB-INF/classes/")));
        }

        // add all of the libs
        File libDir = new File(webInfDir, "lib");
        if (libDir.isDirectory()) {
            File[] libs = libDir.listFiles(new FileFilter() {
                public boolean accept(File file) {
                    return file.isFile() && file.getName().endsWith(".jar");
                }
            });

            if (libs != null) {
                for (int i = 0; i < libs.length; i++) {
                    File lib = libs[i];
                    webModule.addToWebClasspath(webModule.getTargetPathURI().resolve(URI.create("WEB-INF/lib/" + lib.getName())));
                }
            }
        }
    }

    private Map buildComponentContext(EARContext earContext, Module webModule, WebAppType webApp, GerWebAppType jettyWebApp, UserTransaction userTransaction, ClassLoader cl) throws DeploymentException {
        return ENCConfigBuilder.buildComponentContext(earContext,
                earContext,
                webModule,
                userTransaction,
                webApp.getEnvEntryArray(),
                webApp.getEjbRefArray(), jettyWebApp.getEjbRefArray(),
                webApp.getEjbLocalRefArray(), jettyWebApp.getEjbLocalRefArray(),
                webApp.getResourceRefArray(), jettyWebApp.getResourceRefArray(),
                webApp.getResourceEnvRefArray(), jettyWebApp.getResourceEnvRefArray(),
                webApp.getMessageDestinationRefArray(),
                webApp.getServiceRefArray(), jettyWebApp.getServiceRefArray(),
                cl);
    }

    private static void check(WebAppType webApp) throws DeploymentException {
        checkURLPattern(webApp);
        checkMultiplicities(webApp);
    }

    private static void checkURLPattern(WebAppType webApp) throws DeploymentException {

        FilterMappingType[] filterMappings = webApp.getFilterMappingArray();
        for (int i = 0; i < filterMappings.length; i++) {
            if (filterMappings[i].isSetUrlPattern()) {
                checkString(filterMappings[i].getUrlPattern().getStringValue());
            }
        }

        ServletMappingType[] servletMappings = webApp.getServletMappingArray();
        for (int i = 0; i < servletMappings.length; i++) {
            checkString(servletMappings[i].getUrlPattern().getStringValue());
        }

        SecurityConstraintType[] constraints = webApp.getSecurityConstraintArray();
        for (int i = 0; i < constraints.length; i++) {
            WebResourceCollectionType[] collections = constraints[i].getWebResourceCollectionArray();
            for (int j = 0; j < collections.length; j++) {
                UrlPatternType[] patterns = collections[j].getUrlPatternArray();
                for (int k = 0; k < patterns.length; k++) {
                    checkString(patterns[k].getStringValue());
                }
            }
        }
    }

    private static void checkString(String pattern) throws DeploymentException {
        //j2ee_1_4.xsd explicitly requires preserving all whitespace. Do not trim.
        if (pattern.indexOf(0x0D) >= 0) throw new DeploymentException("<url-pattern> must not contain CR(#xD)");
        if (pattern.indexOf(0x0A) >= 0) throw new DeploymentException("<url-pattern> must not contain LF(#xA)");
    }

    private static void checkMultiplicities(WebAppType webApp) throws DeploymentException {
        if (webApp.getSessionConfigArray().length > 1) throw new DeploymentException("Multiple <session-config> elements found");
        if (webApp.getJspConfigArray().length > 1) throw new DeploymentException("Multiple <jsp-config> elements found");
        if (webApp.getLoginConfigArray().length > 1) throw new DeploymentException("Multiple <login-config> elements found");
    }

    class UncheckedItem {
        final static int NA = 0x00;
        final static int INTEGRAL = 0x01;
        final static int CONFIDENTIAL = 0x02;

        private int transportType = NA;
        private String name;

        public UncheckedItem(String name, String actions) {
            setName(name);
            setTransportType(actions);
        }

        public boolean equals(Object o) {
            UncheckedItem item = (UncheckedItem) o;
            return item.getKey().equals(this.getKey());
        }

        public String getKey() {
            return (name + transportType);
        }

        public int hashCode() {
            return getKey().hashCode();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getTransportType() {
            return transportType;
        }

        public void setTransportType(String actions) {
            String[] tokens = actions.split(":", 2);
            if (tokens.length == 2) {
                if (tokens[1].equals("INTEGRAL")) {
                    this.transportType = INTEGRAL;
                } else if (tokens[1].equals("CONFIDENTIAL")) {
                    this.transportType = CONFIDENTIAL;
                }
            }
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(JettyModuleBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("defaultParentId", URI.class, true);
        infoBuilder.addAttribute("defaultSessionTimeoutSeconds", Integer.class, true);
        infoBuilder.addAttribute("defaultWelcomeFiles", List.class, true);
        infoBuilder.addAttribute("jettyContainerObjectName", ObjectName.class, true);
        infoBuilder.addReference("DefaultServlets", Object.class);
        infoBuilder.addReference("DefaultFilters", Object.class);
        infoBuilder.addReference("DefaultFilterMappings", Object.class);
        infoBuilder.addReference("PojoWebServiceTemplate", Object.class);
        infoBuilder.addReference("WebServiceBuilder", WebServiceBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("Repository", Repository.class, NameFactory.GERONIMO_SERVICE);
        infoBuilder.addAttribute("kernel", Kernel.class, false);
        infoBuilder.addInterface(ModuleBuilder.class);

        infoBuilder.setConstructor(new String[]{
            "defaultParentId",
            "defaultSessionTimeoutSeconds",
            "defaultWelcomeFiles",
            "jettyContainerObjectName",
            "DefaultServlets",
            "DefaultFilters",
            "DefaultFilterMappings",
            "PojoWebServiceTemplate",
            "WebServiceBuilder",
            "Repository",
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
            int comp = s1.getLoadOnStartup().getBigIntegerValue().compareTo(s2.getLoadOnStartup().getBigIntegerValue());
            if (comp == 0) {
                return s1.getServletName().getStringValue().trim().compareTo(s2.getServletName().getStringValue().trim());
            }
            return comp;
        }
    }
}
