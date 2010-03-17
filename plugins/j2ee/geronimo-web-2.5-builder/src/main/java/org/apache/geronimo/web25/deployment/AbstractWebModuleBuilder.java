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

package org.apache.geronimo.web25.deployment;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.components.jaspi.model.AuthModuleType;
import org.apache.geronimo.components.jaspi.model.ConfigProviderType;
import org.apache.geronimo.components.jaspi.model.JaspiXmlUtil;
import org.apache.geronimo.components.jaspi.model.ServerAuthConfigType;
import org.apache.geronimo.components.jaspi.model.ServerAuthContextType;
import org.apache.geronimo.deployment.ClassPathList;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.ModuleList;
import org.apache.geronimo.deployment.NamespaceDrivenBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilderCollection;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.annotation.Holder;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.deployment.ModuleBuilderExtension;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.deployment.WebModule;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.deployment.annotation.SecurityAnnotationHelper;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.jndi.JndiKey;
import org.apache.geronimo.j2ee.jndi.JndiScope;
import org.apache.geronimo.j2ee.jndi.WebContextSource;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.kernel.util.FileUtils;
import org.apache.geronimo.naming.deployment.ResourceEnvironmentSetter;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.security.jaspi.AuthConfigProviderGBean;
import org.apache.geronimo.security.jaspi.ServerAuthConfigGBean;
import org.apache.geronimo.security.jaspi.ServerAuthContextGBean;
import org.apache.geronimo.security.jaspi.ServerAuthModuleGBean;
import org.apache.geronimo.web25.deployment.security.AuthenticationWrapper;
import org.apache.geronimo.web25.deployment.security.SpecSecurityBuilder;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerSecurityDocument;
import org.apache.geronimo.xbeans.javaee6.FilterMappingType;
import org.apache.geronimo.xbeans.javaee6.FilterType;
import org.apache.geronimo.xbeans.javaee6.FullyQualifiedClassType;
import org.apache.geronimo.xbeans.javaee6.ListenerType;
import org.apache.geronimo.xbeans.javaee6.SecurityConstraintType;
import org.apache.geronimo.xbeans.javaee6.ServletMappingType;
import org.apache.geronimo.xbeans.javaee6.ServletType;
import org.apache.geronimo.xbeans.javaee6.UrlPatternType;
import org.apache.geronimo.xbeans.javaee6.WebAppDocument;
import org.apache.geronimo.xbeans.javaee6.WebAppType;
import org.apache.geronimo.xbeans.javaee6.WebResourceCollectionType;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlDocumentProperties;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.security.auth.message.module.ServerAuthModule;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractWebModuleBuilder implements ModuleBuilder {
    //are we combining all web apps into one bundle in an ear?
    //TODO eliminate this
    protected static final boolean COMBINED_BUNDLE = true;

    public final static NamingBuilder.Key<GBeanData> DEFAULT_JSP_SERVLET_KEY = new NamingBuilder.Key<GBeanData>() {
        public GBeanData get(Map context) {
            return (GBeanData) context.get(this);
        }

    };
    private static final Logger log = LoggerFactory.getLogger(AbstractWebModuleBuilder.class);

    private static final QName TAGLIB = new QName(SchemaConversionUtils.JAVAEE_NAMESPACE, "taglib");

    private static final String LINE_SEP = System.getProperty("line.separator");

    protected static final AbstractNameQuery MANAGED_CONNECTION_FACTORY_PATTERN;
    private static final AbstractNameQuery ADMIN_OBJECT_PATTERN;
    protected static final AbstractNameQuery STATELESS_SESSION_BEAN_PATTERN;
    protected static final AbstractNameQuery STATEFUL_SESSION_BEAN_PATTERN;
    protected static final AbstractNameQuery ENTITY_BEAN_PATTERN;
    protected final Kernel kernel;
    protected final NamespaceDrivenBuilderCollection serviceBuilders;
    protected final ResourceEnvironmentSetter resourceEnvironmentSetter;
    protected final Collection<WebServiceBuilder> webServiceBuilder;

    protected final NamingBuilder namingBuilders;
    protected final Collection<ModuleBuilderExtension> moduleBuilderExtensions;

    private static final QName SECURITY_QNAME = GerSecurityDocument.type.getDocumentElementName();

    /**
     * Manifest classpath entries in a war configuration must be resolved relative to the war configuration, not the
     * enclosing ear configuration.  Resolving relative to he war configuration using this offset produces the same
     * effect as URI.create(module.targetPath()).resolve(mcpEntry) executed in the ear configuration.
     */
    private static final URI RELATIVE_MODULE_BASE_URI = URI.create("../");

    protected AbstractWebModuleBuilder(Kernel kernel, Collection<NamespaceDrivenBuilder> serviceBuilders, NamingBuilder namingBuilders, ResourceEnvironmentSetter resourceEnvironmentSetter, Collection<WebServiceBuilder> webServiceBuilder, Collection<ModuleBuilderExtension> moduleBuilderExtensions) {
        this.kernel = kernel;
        this.serviceBuilders = new NamespaceDrivenBuilderCollection(serviceBuilders);
        this.namingBuilders = namingBuilders;
        this.resourceEnvironmentSetter = resourceEnvironmentSetter;
        this.webServiceBuilder = webServiceBuilder;
        this.moduleBuilderExtensions = moduleBuilderExtensions == null ? new ArrayList<ModuleBuilderExtension>() : moduleBuilderExtensions;
    }

    static {
        MANAGED_CONNECTION_FACTORY_PATTERN = new AbstractNameQuery(null, Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.JCA_MANAGED_CONNECTION_FACTORY));
        ADMIN_OBJECT_PATTERN = new AbstractNameQuery(null, Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.JCA_ADMIN_OBJECT));
        STATELESS_SESSION_BEAN_PATTERN = new AbstractNameQuery(null, Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.STATELESS_SESSION_BEAN));
        STATEFUL_SESSION_BEAN_PATTERN = new AbstractNameQuery(null, Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.STATEFUL_SESSION_BEAN));
        ENTITY_BEAN_PATTERN = new AbstractNameQuery(null, Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.ENTITY_BEAN));

    }

    public NamingBuilder getNamingBuilders() {
        return namingBuilders;
    }

    protected void addGBeanDependencies(EARContext earContext, GBeanData webModuleData) {
        Configuration earConfiguration = earContext.getConfiguration();
        addDependencies(earContext.findGBeanDatas(earConfiguration, MANAGED_CONNECTION_FACTORY_PATTERN), webModuleData);
        addDependencies(earContext.findGBeanDatas(earConfiguration, ADMIN_OBJECT_PATTERN), webModuleData);
        addDependencies(earContext.findGBeanDatas(earConfiguration, STATELESS_SESSION_BEAN_PATTERN), webModuleData);
        addDependencies(earContext.findGBeanDatas(earConfiguration, STATEFUL_SESSION_BEAN_PATTERN), webModuleData);
        addDependencies(earContext.findGBeanDatas(earConfiguration, ENTITY_BEAN_PATTERN), webModuleData);
    }

    private void addDependencies(LinkedHashSet<GBeanData> dependencies, GBeanData webModuleData) {
        for (GBeanData dependency : dependencies) {
            AbstractName dependencyName = dependency.getAbstractName();
            webModuleData.addDependency(dependencyName);
        }
    }

    public Module createModule(File plan, JarFile moduleFile, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        return createModule(plan, moduleFile, ".", null, null, null, null, naming, idBuilder);
    }

    public Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment environment, Object moduleContextInfo, AbstractName earName, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        return createModule(plan, moduleFile, targetPath, specDDUrl, environment, (String) moduleContextInfo, earName, naming, idBuilder);
    }

    protected abstract Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment earEnvironment, String contextRoot, AbstractName earName, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException;

    /**
     * Some servlets will have multiple url patterns.  However, webservice servlets
     * will only have one, which is what this method is intended for.
     *
     * @param webApp      spec deployment descriptor
     * @param contextRoot context root for web app from application.xml or geronimo plan
     * @return map of servlet names to path mapped to them.  Possibly inaccurate except for web services.
     */
    protected Map<String, String> buildServletNameToPathMap(WebAppType webApp, String contextRoot) {
        if (contextRoot == null) {
            contextRoot = "";
        } else if (!contextRoot.startsWith("/")) {
            contextRoot = "/" + contextRoot;
        }
        Map<String, String> map = new HashMap<String, String>();
        ServletMappingType[] servletMappings = webApp.getServletMappingArray();
        for (ServletMappingType servletMapping : servletMappings) {
            String servletName = servletMapping.getServletName().getStringValue().trim();
            UrlPatternType[] urlPatterns = servletMapping.getUrlPatternArray();

            for (int i = 0; urlPatterns != null && (i < urlPatterns.length); i++) {
                map.put(servletName, contextRoot + urlPatterns[i].getStringValue().trim());
            }
        }
        return map;
    }

    protected String determineDefaultContextRoot(WebAppType webApp, boolean isStandAlone, JarFile moduleFile, String targetPath) {

        if (webApp != null && webApp.getId() != null) {
            return webApp.getId();
        }

        if (isStandAlone) {
            // default configId is based on the moduleFile name
            return "/" + trimPath(new File(moduleFile.getName()).getName());
        }

        // default configId is based on the module uri from the application.xml
        return trimPath(targetPath);
    }

    private String trimPath(String path) {

        if (path == null) {
            return null;
        }

        if (path.endsWith(".war")) {
            path = path.substring(0, path.length() - 4);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        return path;
    }

    public void installModule(JarFile earFile, EARContext earContext, Module module, Collection configurationStores, ConfigurationStore targetConfigurationStore, Collection repositories) throws DeploymentException {
        EARContext moduleContext;
        //TODO GERONIMO-4972 find a way to create working nested bundles.
        if (true || module.isStandAlone()) {
            moduleContext = earContext;
        } else {
            Environment environment = module.getEnvironment();
            Artifact earConfigId = earContext.getConfigID();
            Artifact configId = new Artifact(earConfigId.getGroupId(), earConfigId.getArtifactId() + "_" + module.getTargetPath(), earConfigId.getVersion(), "car");
            environment.setConfigId(configId);
            environment.addDependency(earConfigId, ImportType.ALL);
            File configurationDir = new File(earContext.getBaseDir(), module.getTargetPath());
            configurationDir.mkdirs();

            // construct the web app deployment context... this is the same class used by the ear context
            try {
                File inPlaceConfigurationDir = null;
                if (null != earContext.getInPlaceConfigurationDir()) {
                    inPlaceConfigurationDir = new File(earContext.getInPlaceConfigurationDir(), module.getTargetPath());
                }
                moduleContext = new EARContext(configurationDir,
                        inPlaceConfigurationDir,
                        environment,
                        ConfigurationModuleType.WAR,
                        module.getModuleName(),
                        earContext);
            } catch (DeploymentException e) {
                cleanupConfigurationDir(configurationDir);
                throw e;
            }
        }
        module.setEarContext(moduleContext);
        module.setRootEarContext(earContext);

        try {
            ClassPathList manifestcp = new ClassPathList();
            // add the warfile's content to the configuration
            JarFile warFile = module.getModuleFile();
            Enumeration<JarEntry> entries = warFile.entries();
            List<ZipEntry> libs = new ArrayList<ZipEntry>();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                URI targetPath = module.resolve(entry.getName());
                if (entry.getName().equals("WEB-INF/web.xml")) {
                    moduleContext.addFile(targetPath, module.getOriginalSpecDD());
                } else if (entry.getName().startsWith("WEB-INF/lib") && entry.getName().endsWith(".jar")) {
                    // keep a collection of all libs in the war
                    // libs must be installed after WEB-INF/classes which must be installed after this copy phase
                    libs.add(entry);
                } else {
                    moduleContext.addFile(targetPath, warFile, entry);
                }
            }

            // always add WEB-INF/classes to the classpath regardless of whether
            // any classes exist.  This must be searched BEFORE the WEB-INF/lib jar files,
            // per the servlet specifications.
            moduleContext.addToClassPath(module.resolve("WEB-INF/classes/").getPath());
            manifestcp.add("WEB-INF/classes/");

            // install the libs
            for (ZipEntry entry : libs) {
                URI targetPath = module.resolve(entry.getName());
                moduleContext.addInclude(targetPath, warFile, entry);
                manifestcp.add(entry.getName());
            }

            // add the manifest classpath entries declared in the war to the class loader
            // we have to explicitly add these since we are unpacking the web module
            // and the url class loader will not pick up a manifest from an unpacked dir
            //GERONIMO-4972 this can't be correct for one-bundle deployments.
            moduleContext.addManifestClassPath(warFile, RELATIVE_MODULE_BASE_URI);
            moduleContext.getGeneralData().put(ClassPathList.class, manifestcp);

        } catch (IOException e) {
            throw new DeploymentException("Problem deploying war", e);
        } finally {
            if (!module.isStandAlone()) {
                try {
                    moduleContext.flush();
                } catch (IOException e) {
                    throw new DeploymentException("Problem closing war context", e);
                }
            }
        }
        for (ModuleBuilderExtension mbe : moduleBuilderExtensions) {
            mbe.installModule(earFile, earContext, module, configurationStores, targetConfigurationStore, repositories);
        }
    }

    protected void basicInitContext(EARContext earContext, Module module, XmlObject gerWebApp, boolean hasSecurityRealmName) throws DeploymentException {
        WebModule webModule = (WebModule) module;
        //complete manifest classpath
        EARContext moduleContext = webModule.getEarContext();
        ClassPathList manifestcp = (ClassPathList) moduleContext.getGeneralData().get(ClassPathList.class);
        ModuleList moduleLocations = (ModuleList) webModule.getRootEarContext().getGeneralData().get(ModuleList.class);
        URI baseUri = URI.create(webModule.getTargetPath());
        URI resolutionUri = invertURI(baseUri);
        earContext.getCompleteManifestClassPath(webModule.getDeployable(), baseUri, resolutionUri, manifestcp, moduleLocations);

        WebAppType webApp = (WebAppType) webModule.getSpecDD();
        if ((webApp.getSecurityConstraintArray().length > 0 || webApp.getSecurityRoleArray().length > 0)) {
            if (!hasSecurityRealmName) {
                throw new DeploymentException("web.xml for web app " + webModule.getName() + " includes security elements but Geronimo deployment plan is not provided or does not contain <security-realm-name> element necessary to configure security accordingly.");
            }
        }
        if (hasSecurityRealmName) {
            earContext.setHasSecurity(true);
        }
        //TODO think about how to provide a default security realm name
        XmlObject[] securityElements = XmlBeansUtil.selectSubstitutionGroupElements(SECURITY_QNAME, gerWebApp);
        if (securityElements.length > 0 && !hasSecurityRealmName) {
            throw new DeploymentException("You have supplied a security configuration for web app " + webModule.getName() + " but no security-realm-name to allow login");
        }
        getNamingBuilders().buildEnvironment(webApp, webModule.getVendorDD(), webModule.getEnvironment());
        //this is silly
        getNamingBuilders().initContext(webApp, gerWebApp, webModule);

        Map servletNameToPathMap = buildServletNameToPathMap((WebAppType) webModule.getSpecDD(), webModule.getContextRoot());

        Map sharedContext = webModule.getSharedContext();
        for (Object aWebServiceBuilder : webServiceBuilder) {
            WebServiceBuilder serviceBuilder = (WebServiceBuilder) aWebServiceBuilder;
            serviceBuilder.findWebServices(webModule, false, servletNameToPathMap, webModule.getEnvironment(), sharedContext);
        }
        serviceBuilders.build(gerWebApp, earContext, webModule.getEarContext());
    }

    static URI invertURI(URI baseUri) {
        URI resolutionUri = URI.create(".");
        for (URI test = baseUri; !test.equals(RELATIVE_MODULE_BASE_URI); test = test.resolve(RELATIVE_MODULE_BASE_URI)) {
            resolutionUri = resolutionUri.resolve(RELATIVE_MODULE_BASE_URI);
        }
        return resolutionUri;
    }

    protected String getSpecDDAsString(WebModule module) {
        StringWriter writer = new StringWriter();
        XmlOptions options = new XmlOptions();
        QName webQName = new QName("http://java.sun.com/xml/ns/javaee", "web-app");
        options.setSaveSyntheticDocumentElement(webQName);
        try {
            module.getSpecDD().save(writer, options);
        } catch (IOException e) {
            // ignore
        }
        return writer.toString();
    }

    protected WebAppDocument convertToServletSchema(XmlObject xmlObject) throws XmlException {
        String schemaLocationURL = "http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd";
        String version = "3.0";
        XmlCursor cursor = xmlObject.newCursor();
        try {
            cursor.toStartDoc();
            cursor.toFirstChild();
            String nameSpaceURI = cursor.getName().getNamespaceURI();
            if ("http://java.sun.com/xml/ns/javaee".equals(nameSpaceURI) || "http://java.sun.com/xml/ns/j2ee".equals(nameSpaceURI)) {
                SchemaConversionUtils.convertSchemaVersion(cursor, SchemaConversionUtils.JAVAEE_NAMESPACE, schemaLocationURL, version);
                XmlObject result = xmlObject.changeType(WebAppDocument.type);
                XmlBeansUtil.validateDD(result);
                return (WebAppDocument) result;
            }
            //otherwise assume DTD
            XmlDocumentProperties xmlDocumentProperties = cursor.documentProperties();
            String publicId = xmlDocumentProperties.getDoctypePublicId();
            boolean is22 = "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN".equals(publicId);
            if ("-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN".equals(publicId) ||
                    is22) {
                XmlCursor moveable = xmlObject.newCursor();
                try {
                    moveable.toStartDoc();
                    moveable.toFirstChild();

                    SchemaConversionUtils.convertToSchema(cursor, SchemaConversionUtils.JAVAEE_NAMESPACE, schemaLocationURL, version);
                    cursor.toStartDoc();
                    cursor.toChild(SchemaConversionUtils.JAVAEE_NAMESPACE, "web-app");
                    cursor.toFirstChild();
                    SchemaConversionUtils.convertToDescriptionGroup(SchemaConversionUtils.JAVAEE_NAMESPACE, cursor, moveable);
                    SchemaConversionUtils.convertToJNDIEnvironmentRefsGroup(SchemaConversionUtils.JAVAEE_NAMESPACE, cursor, moveable);
                    cursor.push();
                    if (cursor.toNextSibling(TAGLIB)) {
                        cursor.toPrevSibling();
                        moveable.toCursor(cursor);
                        cursor.beginElement("jsp-config", SchemaConversionUtils.JAVAEE_NAMESPACE);
                        while (moveable.toNextSibling(TAGLIB)) {
                            moveable.moveXml(cursor);
                        }
                    }
                    cursor.pop();
                    do {
                        String name = cursor.getName().getLocalPart();
                        if ("filter".equals(name) || "servlet".equals(name) || "context-param".equals(name)) {
                            cursor.push();
                            cursor.toFirstChild();
                            SchemaConversionUtils.convertToDescriptionGroup(SchemaConversionUtils.JAVAEE_NAMESPACE, cursor, moveable);
                            while (cursor.toNextSibling(SchemaConversionUtils.JAVAEE_NAMESPACE, "init-param")) {
                                cursor.push();
                                cursor.toFirstChild();
                                SchemaConversionUtils.convertToDescriptionGroup(SchemaConversionUtils.JAVAEE_NAMESPACE, cursor, moveable);
                                cursor.pop();
                            }
                            cursor.pop();
                            cursor.push();
                            if (cursor.toChild(SchemaConversionUtils.JAVAEE_NAMESPACE, "jsp-file")) {
                                String jspFile = cursor.getTextValue();
                                if (!jspFile.startsWith("/")) {
                                    if (is22) {
                                        cursor.setTextValue("/" + jspFile);
                                    } else {
                                        throw new XmlException("jsp-file does not start with / and this is not a 2.2 web app: " + jspFile);
                                    }
                                }
                            }
                            cursor.pop();
                        }
                    } while (cursor.toNextSibling());
                } finally {
                    moveable.dispose();
                }
            }
        } finally {
            cursor.dispose();
        }
        XmlObject result = xmlObject.changeType(WebAppDocument.type);
        if (result != null) {
            XmlBeansUtil.validateDD(result);
            return (WebAppDocument) result;
        }
        XmlBeansUtil.validateDD(xmlObject);
        return (WebAppDocument) xmlObject;
    }

    protected ComponentPermissions buildSpecSecurityConfig(WebAppType webApp) {
        SpecSecurityBuilder builder = new SpecSecurityBuilder();
        return builder.buildSpecSecurityConfig(webApp);
    }

    protected void configureLocalJaspicProvider(AuthenticationWrapper authType, String contextPath, Module module, GBeanData securityFactoryData) throws DeploymentException, GBeanAlreadyExistsException {
        EARContext moduleContext = module.getEarContext();
        GBeanData authConfigProviderData = null;
        AbstractName providerName = moduleContext.getNaming().createChildName(module.getModuleName(), "authConfigProvider", GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
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
            securityFactoryData.addDependency(authConfigProviderData.getAbstractName());
        }
    }

    protected static void check(WebAppType webApp) throws DeploymentException {
        checkURLPattern(webApp);
        checkMultiplicities(webApp);
    }

    private static void checkURLPattern(WebAppType webApp) throws DeploymentException {

        FilterMappingType[] filterMappings = webApp.getFilterMappingArray();
        for (FilterMappingType filterMapping : filterMappings) {
            UrlPatternType[] urlPatterns = filterMapping.getUrlPatternArray();
            for (int j = 0; (urlPatterns != null) && (j < urlPatterns.length); j++) {
                checkString(urlPatterns[j].getStringValue().trim());
            }
        }

        ServletMappingType[] servletMappings = webApp.getServletMappingArray();
        for (ServletMappingType servletMapping : servletMappings) {
            UrlPatternType[] urlPatterns = servletMapping.getUrlPatternArray();
            for (int j = 0; (urlPatterns != null) && (j < urlPatterns.length); j++) {
                checkString(urlPatterns[j].getStringValue().trim());
            }
        }

        SecurityConstraintType[] constraints = webApp.getSecurityConstraintArray();
        for (SecurityConstraintType constraint : constraints) {
            WebResourceCollectionType[] collections = constraint.getWebResourceCollectionArray();
            for (WebResourceCollectionType collection : collections) {
                UrlPatternType[] patterns = collection.getUrlPatternArray();
                for (UrlPatternType pattern : patterns) {
                    checkString(pattern.getStringValue().trim());
                }
            }
        }
    }

    protected static void checkString(String pattern) throws DeploymentException {
        //j2ee_1_4.xsd explicitly requires preserving all whitespace. Do not trim.
        if (pattern.indexOf(0x0D) >= 0) throw new DeploymentException("<url-pattern> must not contain CR(#xD)");
        if (pattern.indexOf(0x0A) >= 0) throw new DeploymentException("<url-pattern> must not contain LF(#xA)");
    }

    private static void checkMultiplicities(WebAppType webApp) throws DeploymentException {
        if (webApp.getSessionConfigArray().length > 1)
            throw new DeploymentException("Multiple <session-config> elements found");
        if (webApp.getJspConfigArray().length > 1)
            throw new DeploymentException("Multiple <jsp-config> elements found");
        if (webApp.getLoginConfigArray().length > 1)
            throw new DeploymentException("Multiple <login-config> elements found");
    }

    private boolean cleanupConfigurationDir(File configurationDir) {
        LinkedList<String> cannotBeDeletedList = new LinkedList<String>();

        if (!FileUtils.recursiveDelete(configurationDir, cannotBeDeletedList)) {
            // Output a message to help user track down file problem
            log.warn("Unable to delete " + cannotBeDeletedList.size() +
                    " files while recursively deleting directory "
                    + configurationDir.getAbsolutePath() + LINE_SEP +
                    "The first file that could not be deleted was:" + LINE_SEP + "  " +
                    (!cannotBeDeletedList.isEmpty() ? cannotBeDeletedList.getFirst() : ""));
            return false;
        }
        return true;
    }


    protected ClassFinder createWebAppClassFinder(WebAppType webApp, WebModule webModule) throws DeploymentException {
        // Get the classloader from the module's EARContext
        Bundle bundle = webModule.getEarContext().getDeploymentBundle();
        return createWebAppClassFinder(webApp, bundle);
    }

    public static ClassFinder createWebAppClassFinder(WebAppType webApp, Bundle bundle) throws DeploymentException {
        //------------------------------------------------------------------------------------
        // Find the list of classes from the web.xml we want to search for annotations in
        //------------------------------------------------------------------------------------
        List<Class> classes = new ArrayList<Class>();

        // Get all the servlets from the deployment descriptor
        ServletType[] servlets = webApp.getServletArray();
        for (ServletType servlet : servlets) {
            FullyQualifiedClassType cls = servlet.getServletClass();
            if (cls != null) {                              // Don't try this for JSPs
                Class<?> clas;
                try {
                    clas = bundle.loadClass(cls.getStringValue());
                } catch (ClassNotFoundException e) {
                    throw new DeploymentException("AbstractWebModuleBuilder: Could not load servlet class: " + cls.getStringValue(), e);
                }
                addClass(classes, clas);
            }
        }

        // Get all the listeners from the deployment descriptor
        ListenerType[] listeners = webApp.getListenerArray();
        for (ListenerType listener : listeners) {
            FullyQualifiedClassType cls = listener.getListenerClass();
            Class<?> clas;
            try {
                clas = bundle.loadClass(cls.getStringValue());
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("AbstractWebModuleBuilder: Could not load listener class: " + cls.getStringValue(), e);
            }
            addClass(classes, clas);
        }

        // Get all the filters from the deployment descriptor
        FilterType[] filters = webApp.getFilterArray();
        for (FilterType filter : filters) {
            FullyQualifiedClassType cls = filter.getFilterClass();
            Class<?> clas;
            try {
                clas = bundle.loadClass(cls.getStringValue());
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("AbstractWebModuleBuilder: Could not load filter class: " + cls.getStringValue(), e);
            }
            addClass(classes, clas);
        }

        // see https://issues.apache.org/jira/browse/GERONIMO-3421 .
        // if the user has botched her classloader config (perhaps by
        // not including a jar that her app needs) then ClassFinder
        // will throw NoClassDefFoundError.  we want to indicate that
        // it's the user's error and provide a little context to help
        // her fix it.
        try {
            return new ClassFinder(classes);
        } catch (NoClassDefFoundError e) {
            throw new DeploymentException("Classloader for " + webApp.getId() + "can't find " + e.getMessage(), e);
        }
    }

    private static void addClass(List<Class> classes, Class<?> clas) {
        while (clas != Object.class) {
            classes.add(clas);
            clas = clas.getSuperclass();
        }
    }

    protected void configureBasicWebModuleAttributes(WebAppType webApp, XmlObject vendorPlan, EARContext moduleContext, EARContext earContext, WebModule webModule, GBeanData webModuleData) throws DeploymentException {
        Map<NamingBuilder.Key, Object> buildingContext = new HashMap<NamingBuilder.Key, Object>();
        buildingContext.put(NamingBuilder.GBEAN_NAME_KEY, moduleContext.getModuleName());

        //get partial jndi context from earContext.
        Map<JndiKey, Map<String, Object>> jndiContext = new HashMap<JndiKey, Map<String, Object>>(NamingBuilder.JNDI_KEY.get(earContext.getGeneralData()));
        buildingContext.put(NamingBuilder.JNDI_KEY, jndiContext);

        if (!webApp.getMetadataComplete()) {
            // Create a classfinder and populate it for the naming builder(s). The absence of a
            // classFinder in the module will convey whether metadata-complete is set (or not)
            webModule.setClassFinder(createWebAppClassFinder(webApp, webModule));
            SecurityAnnotationHelper.processAnnotations(webApp, webModule.getClassFinder());
        }
        //N.B. we use the ear context which has all the gbeans we could possibly be looking up from this ear.
        //nope, persistence units can be in the war.
        //This means that you cannot use the default environment of the web builder to add configs that will be searched.
        getNamingBuilders().buildNaming(webApp, vendorPlan, webModule, buildingContext);

        //Combine contexts.  Note this may not work right for jaxws which has a comp/env/WebServiceContext binding
        Map<String, Object> compContext = new HashMap<String, Object>();
        if (jndiContext.get(JndiScope.comp) != null) {
            compContext.putAll(jndiContext.get(JndiScope.comp));
        }
        if (jndiContext.get(JndiScope.module) != null) {
            compContext.putAll(jndiContext.get(JndiScope.module));
        }
        AbstractName contextSourceName = moduleContext.getNaming().createChildName(webModuleData.getAbstractName(), "ContextSource", "ContextSource");
        GBeanData contextSourceData = new GBeanData(contextSourceName, WebContextSource.class);
        contextSourceData.setAttribute("componentContext", compContext);
        contextSourceData.setReferencePattern("ApplicationJndi", (AbstractName)earContext.getGeneralData().get(EARContext.APPLICATION_JNDI_NAME_KEY));
        contextSourceData.setReferencePattern("TransactionManager", moduleContext.getTransactionManagerName());
        try {
            moduleContext.addGBean(contextSourceData);
        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException("ContextSource for this webapp already present:" + webModuleData.getAbstractName(), e);
        }
        webModuleData.setReferencePattern("ContextSource", contextSourceName);

        Holder holder = NamingBuilder.INJECTION_KEY.get(buildingContext);

        webModule.getSharedContext().put(WebModule.WEB_APP_DATA, webModuleData);
        webModule.getSharedContext().put(NamingBuilder.JNDI_KEY, jndiContext);
        webModule.getSharedContext().put(NamingBuilder.INJECTION_KEY, holder);
        if (moduleContext.getServerName() != null) {
            webModuleData.setReferencePattern("J2EEServer", moduleContext.getServerName());
        }
        if (!webModule.isStandAlone()) {
            webModuleData.setReferencePattern("J2EEApplication", earContext.getModuleName());
        }

        webModuleData.setAttribute("holder", holder);

        //Add dependencies on managed connection factories and ejbs in this app
        //This is overkill, but allows for people not using java:comp context (even though we don't support it)
        //and sidesteps the problem of circular references between ejbs.
        if (earContext != moduleContext) {
            addGBeanDependencies(earContext, webModuleData);
        }

        webModuleData.setReferencePattern("TransactionManager", moduleContext.getTransactionManagerName());
        webModuleData.setReferencePattern("TrackedConnectionAssociator", moduleContext.getConnectionTrackerName());
        webModuleData.setAttribute("modulePath", webModule.isStandAlone() || webModule.getEarContext() != webModule.getRootEarContext()? null: webModule.getTargetPath());
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
            return delegate.getAttributeCount();
        }

        public String getAttributeLocalName(int i) {
            return delegate.getAttributeLocalName(i);
        }

        public QName getAttributeName(int i) {
            return delegate.getAttributeName(i);
        }

        public String getAttributeNamespace(int i) {
            return delegate.getAttributeNamespace(i);
        }

        public String getAttributePrefix(int i) {
            return delegate.getAttributePrefix(i);
        }

        public String getAttributeType(int i) {
            return delegate.getAttributeType(i);
        }

        public String getAttributeValue(int i) {
            return delegate.getAttributeValue(i);
        }

        public String getAttributeValue(String s, String s1) {
            return delegate.getAttributeValue(s, s1);
        }

        public String getCharacterEncodingScheme() {
            return delegate.getCharacterEncodingScheme();
        }

        public String getElementText() throws XMLStreamException {
            return delegate.getElementText();
        }

        public String getEncoding() {
            return delegate.getEncoding();
        }

        public int getEventType() {
            return delegate.getEventType();
        }

        public String getLocalName() {
            return delegate.getLocalName().intern();
        }

        public Location getLocation() {
            return delegate.getLocation();
        }

        public QName getName() {
            return delegate.getName();
        }

        public NamespaceContext getNamespaceContext() {
            return delegate.getNamespaceContext();
        }

        public int getNamespaceCount() {
            return delegate.getNamespaceCount();
        }

        public String getNamespacePrefix(int i) {
            return delegate.getNamespacePrefix(i);
        }

        public String getNamespaceURI() {
            return delegate.getNamespaceURI().intern();
        }

        public String getNamespaceURI(int i) {
            return delegate.getNamespaceURI(i);
        }

        public String getNamespaceURI(String s) {
            return delegate.getNamespaceURI(s);
        }

        public String getPIData() {
            return delegate.getPIData();
        }

        public String getPITarget() {
            return delegate.getPITarget();
        }

        public String getPrefix() {
            return delegate.getPrefix();
        }

        public Object getProperty(String s) throws IllegalArgumentException {
            return delegate.getProperty(s);
        }

        public String getText() {
            return delegate.getText();
        }

        public char[] getTextCharacters() {
            return delegate.getTextCharacters();
        }

        public int getTextCharacters(int i, char[] chars, int i1, int i2) throws XMLStreamException {
            return delegate.getTextCharacters(i, chars, i1, i2);
        }

        public int getTextLength() {
            return delegate.getTextLength();
        }

        public int getTextStart() {
            return delegate.getTextStart();
        }

        public String getVersion() {
            return delegate.getVersion();
        }

        public boolean hasName() {
            return delegate.hasName();
        }

        public boolean hasNext() throws XMLStreamException {
            return delegate.hasNext();
        }

        public boolean hasText() {
            return delegate.hasText();
        }

        public boolean isAttributeSpecified(int i) {
            return delegate.isAttributeSpecified(i);
        }

        public boolean isCharacters() {
            return delegate.isCharacters();
        }

        public boolean isEndElement() {
            return delegate.isEndElement();
        }

        public boolean isStandalone() {
            return delegate.isStandalone();
        }

        public boolean isStartElement() {
            return delegate.isStartElement();
        }

        public boolean isWhiteSpace() {
            return delegate.isWhiteSpace();
        }

        public int next() throws XMLStreamException {
            return delegate.next();
        }

        public int nextTag() throws XMLStreamException {
            return delegate.nextTag();
        }

        public void require(int i, String s, String s1) throws XMLStreamException {
            delegate.require(i, s, s1);
        }

        public boolean standaloneSet() {
            return delegate.standaloneSet();
        }
    }

}
