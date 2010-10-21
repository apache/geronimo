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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebRoleRefPermission;
import javax.security.jacc.WebUserDataPermission;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilderCollection;
import org.apache.geronimo.deployment.ClassPathList;
import org.apache.geronimo.deployment.ModuleList;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.deployment.xbeans.ServiceDocument;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
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
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.naming.deployment.ResourceEnvironmentSetter;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.security.util.HTTPMethods;
import org.apache.geronimo.security.util.URLPattern;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerSecurityDocument;
import org.apache.geronimo.xbeans.javaee.FilterMappingType;
import org.apache.geronimo.xbeans.javaee.FilterType;
import org.apache.geronimo.xbeans.javaee.FullyQualifiedClassType;
import org.apache.geronimo.xbeans.javaee.ListenerType;
import org.apache.geronimo.xbeans.javaee.RoleNameType;
import org.apache.geronimo.xbeans.javaee.SecurityConstraintType;
import org.apache.geronimo.xbeans.javaee.SecurityRoleRefType;
import org.apache.geronimo.xbeans.javaee.SecurityRoleType;
import org.apache.geronimo.xbeans.javaee.ServletMappingType;
import org.apache.geronimo.xbeans.javaee.ServletType;
import org.apache.geronimo.xbeans.javaee.UrlPatternType;
import org.apache.geronimo.xbeans.javaee.WebAppDocument;
import org.apache.geronimo.xbeans.javaee.WebAppType;
import org.apache.geronimo.xbeans.javaee.WebResourceCollectionType;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlDocumentProperties;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractWebModuleBuilder implements ModuleBuilder {
    private static final Log log = LogFactory.getLog(AbstractWebModuleBuilder.class);

    private static final QName TAGLIB = new QName(SchemaConversionUtils.JAVAEE_NAMESPACE, "taglib");

    private static final String LINE_SEP = System.getProperty("line.separator");

    protected static final AbstractNameQuery MANAGED_CONNECTION_FACTORY_PATTERN;
    private static final AbstractNameQuery ADMIN_OBJECT_PATTERN;
    protected static final AbstractNameQuery STATELESS_SESSION_BEAN_PATTERN;
    protected static final AbstractNameQuery STATEFUL_SESSION_BEAN_PATTERN;
    protected static final AbstractNameQuery ENTITY_BEAN_PATTERN;
    protected final Kernel kernel;
    protected final NamespaceDrivenBuilderCollection securityBuilders;
    protected final NamespaceDrivenBuilderCollection serviceBuilders;
    protected final ResourceEnvironmentSetter resourceEnvironmentSetter;
    protected final Collection<WebServiceBuilder> webServiceBuilder;

    protected final NamingBuilder namingBuilders;
    protected final Collection<ModuleBuilderExtension> moduleBuilderExtensions;

    private static final QName SECURITY_QNAME = GerSecurityDocument.type.getDocumentElementName();
    private static final QName SERVICE_QNAME = ServiceDocument.type.getDocumentElementName();

    /**
     * Manifest classpath entries in a war configuration must be resolved relative to the war configuration, not the
     * enclosing ear configuration.  Resolving relative to he war configuration using this offset produces the same
     * effect as URI.create(module.targetPath()).resolve(mcpEntry) executed in the ear configuration.
     */
    private static final URI RELATIVE_MODULE_BASE_URI = URI.create("../");

    protected AbstractWebModuleBuilder(Kernel kernel, Collection securityBuilders, Collection serviceBuilders, NamingBuilder namingBuilders, ResourceEnvironmentSetter resourceEnvironmentSetter, Collection<WebServiceBuilder> webServiceBuilder, Collection<ModuleBuilderExtension> moduleBuilderExtensions) {
        this.kernel = kernel;
        this.securityBuilders = new NamespaceDrivenBuilderCollection(securityBuilders, SECURITY_QNAME);
        this.serviceBuilders = new NamespaceDrivenBuilderCollection(serviceBuilders, SERVICE_QNAME);
        this.namingBuilders = namingBuilders;
        this.resourceEnvironmentSetter = resourceEnvironmentSetter;
        this.webServiceBuilder = webServiceBuilder;
        this.moduleBuilderExtensions = moduleBuilderExtensions == null? new ArrayList<ModuleBuilderExtension>(): moduleBuilderExtensions;
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
        for (GBeanData dependency: dependencies) {
            AbstractName dependencyName = dependency.getAbstractName();
            webModuleData.addDependency(dependencyName);
        }
    }

    public Module createModule(File plan, JarFile moduleFile, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        return createModule(plan, moduleFile, ".", null, true, null, null, naming, idBuilder);
    }

    public Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment environment, Object moduleContextInfo, AbstractName earName, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        return createModule(plan, moduleFile, targetPath, specDDUrl, false, (String) moduleContextInfo, earName, naming, idBuilder);
    }

    protected abstract Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, boolean standAlone, String contextRoot, AbstractName earName, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException;

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
        if (module.isStandAlone()) {
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
                URI targetPath = new URI(null, entry.getName(), null);
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
            moduleContext.getConfiguration().addToClassPath("WEB-INF/classes/");
            manifestcp.add("WEB-INF/classes/");

            // install the libs
            for (ZipEntry entry : libs) {
                URI targetPath = new URI(null, entry.getName(), null);
                moduleContext.addInclude(targetPath, warFile, entry);
                manifestcp.add(entry.getName());
            }

            // add the manifest classpath entries declared in the war to the class loader
            // we have to explicitly add these since we are unpacking the web module
            // and the url class loader will not pick up a manifest from an unpacked dir
            moduleContext.addManifestClassPath(warFile, RELATIVE_MODULE_BASE_URI);
            moduleContext.getGeneralData().put(ClassPathList.class, manifestcp);

        } catch (IOException e) {
            throw new DeploymentException("Problem deploying war", e);
        } catch (URISyntaxException e) {
            throw new DeploymentException("Could not construct URI for location of war entry", e);
        } finally {
            if (!module.isStandAlone()) {
                try {
                    moduleContext.flush();
                } catch (IOException e) {
                    throw new DeploymentException("Problem closing war context", e);
                }
            }
        }
        for (ModuleBuilderExtension mbe: moduleBuilderExtensions) {
            mbe.installModule(earFile, earContext, module, configurationStores, targetConfigurationStore, repositories);
        }
    }

    protected void basicInitContext(EARContext earContext, Module module, XmlObject gerWebApp, boolean hasSecurityRealmName) throws DeploymentException {
        //complete manifest classpath
        EARContext moduleContext = module.getEarContext();
        ClassPathList manifestcp = (ClassPathList) moduleContext.getGeneralData().get(ClassPathList.class);
        ModuleList moduleLocations = (ModuleList) module.getRootEarContext().getGeneralData().get(ModuleList.class);
        URI baseUri = URI.create(module.getTargetPath());
        URI resolutionUri = invertURI(baseUri);
        earContext.getCompleteManifestClassPath(module.getModuleFile(), baseUri, resolutionUri, manifestcp, moduleLocations);


        WebAppType webApp = (WebAppType) module.getSpecDD();
        if ((webApp.getSecurityConstraintArray().length > 0 || webApp.getSecurityRoleArray().length > 0) &&
                !hasSecurityRealmName) {
            throw new DeploymentException("web.xml for web app " + module.getName() + " includes security elements but Geronimo deployment plan is not provided or does not contain <security-realm-name> element necessary to configure security accordingly.");
        }
        XmlObject[] securityElements = XmlBeansUtil.selectSubstitutionGroupElements(SECURITY_QNAME, gerWebApp);
        if (securityElements.length > 0 && !hasSecurityRealmName) {
            throw new DeploymentException("You have supplied a security configuration for web app " + module.getName() + " but no security-realm-name to allow login");
        }
        getNamingBuilders().buildEnvironment(webApp, module.getVendorDD(), module.getEnvironment());
        //this is silly
        getNamingBuilders().initContext(webApp, gerWebApp, module);

        Map servletNameToPathMap = buildServletNameToPathMap((WebAppType) module.getSpecDD(), ((WebModule) module).getContextRoot());

        Map sharedContext = module.getSharedContext();
        for (Object aWebServiceBuilder : webServiceBuilder) {
            WebServiceBuilder serviceBuilder = (WebServiceBuilder) aWebServiceBuilder;
            serviceBuilder.findWebServices(module, false, servletNameToPathMap, module.getEnvironment(), sharedContext);
        }
        securityBuilders.build(gerWebApp, earContext, module.getEarContext());
        serviceBuilders.build(gerWebApp, earContext, module.getEarContext());
    }

    static URI invertURI(URI baseUri) {
        URI resolutionUri = URI.create(".");
        for (URI test = baseUri; !test.equals(RELATIVE_MODULE_BASE_URI); test = test.resolve(RELATIVE_MODULE_BASE_URI)) {
            resolutionUri = resolutionUri.resolve(RELATIVE_MODULE_BASE_URI);
        }
        return resolutionUri;
    }

    protected WebAppDocument convertToServletSchema(XmlObject xmlObject) throws XmlException {

        String schemaLocationURL = "http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd";
        String version = "2.5";
        XmlCursor cursor = xmlObject.newCursor();
        try {
            cursor.toStartDoc();
            cursor.toFirstChild();
            if ("http://java.sun.com/xml/ns/j2ee".equals(cursor.getName().getNamespaceURI())) {
                SchemaConversionUtils.convertSchemaVersion(cursor, SchemaConversionUtils.JAVAEE_NAMESPACE, schemaLocationURL, version);
                XmlObject result = xmlObject.changeType(WebAppDocument.type);
                XmlBeansUtil.validateDD(result);
                return (WebAppDocument) result;
            }

            if ("http://java.sun.com/xml/ns/javaee".equals(cursor.getName().getNamespaceURI())) {
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
                                if (!jspFile.startsWith("/")){
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


    protected void addUnmappedJSPPermissions(Set<String> securityRoles, Map<String, PermissionCollection> rolePermissions) {
        for (String roleName : securityRoles) {
            addPermissionToRole(roleName, new WebRoleRefPermission("", roleName), rolePermissions);
        }
    }

    protected ComponentPermissions buildSpecSecurityConfig(WebAppType webApp, Set<String> securityRoles, Map<String, PermissionCollection> rolePermissions) {
        Map<String, URLPattern> uncheckedPatterns = new HashMap<String, URLPattern>();
        Map<UncheckedItem, HTTPMethods> uncheckedResourcePatterns = new HashMap<UncheckedItem, HTTPMethods>();
        Map<UncheckedItem, HTTPMethods> uncheckedUserPatterns = new HashMap<UncheckedItem, HTTPMethods>();
        Map<String, URLPattern> excludedPatterns = new HashMap<String, URLPattern>();
        Map<String, Map<String, URLPattern>> rolesPatterns = new HashMap<String, Map<String, URLPattern>>();
        Set<URLPattern> allSet = new HashSet<URLPattern>();   // == allMap.values()
        Map<String, URLPattern> allMap = new HashMap<String, URLPattern>();   //uncheckedPatterns union excludedPatterns union rolesPatterns.

        SecurityConstraintType[] securityConstraintArray = webApp.getSecurityConstraintArray();
        for (SecurityConstraintType securityConstraintType : securityConstraintArray) {
            Map<String, URLPattern> currentPatterns = null;
            Set<String> roleNames = null;
            if (securityConstraintType.isSetAuthConstraint()) {
                if (securityConstraintType.getAuthConstraint().getRoleNameArray().length == 0) {
                    currentPatterns = excludedPatterns;
                } else {
                    roleNames = new HashSet<String>();
                    for (RoleNameType roleName : securityConstraintType.getAuthConstraint().getRoleNameArray()) {
                        roleNames.add(roleName.getStringValue().trim());
                    }
                    if (roleNames.remove("*")) {
                        roleNames.addAll(securityRoles);
                    }
                }
            } else {
                currentPatterns = uncheckedPatterns;
            }

            String transport = "";
            if (securityConstraintType.isSetUserDataConstraint()) {
                transport = securityConstraintType.getUserDataConstraint().getTransportGuarantee().getStringValue().trim().toUpperCase();
            }

            boolean isRolebasedPatten = (currentPatterns == null);            
            
            for (WebResourceCollectionType webResourceCollectionType : securityConstraintType.getWebResourceCollectionArray()) {
                for (UrlPatternType urlPatternType : webResourceCollectionType.getUrlPatternArray()) {
                    String url = urlPatternType.getStringValue().trim();
                    if (isRolebasedPatten) {
                        for (String roleName : roleNames) {
                            Map<String, URLPattern> currentRolePatterns = rolesPatterns.get(roleName);
                            if (currentRolePatterns == null) {
                                currentRolePatterns = new HashMap<String, URLPattern>();
                                rolesPatterns.put(roleName, currentRolePatterns);
                            }
                            analyzeURLPattern(url, webResourceCollectionType.getHttpMethodArray(), transport, currentRolePatterns);
                        }
                    } else {
                        analyzeURLPattern(url, webResourceCollectionType.getHttpMethodArray(), transport, currentPatterns);
                    }
                    URLPattern allPattern = allMap.get(url);
                    if (allPattern == null) {
                        allPattern = new URLPattern(url);
                        allSet.add(allPattern);
                        allMap.put(url, allPattern);
                    }
                    analyzeURLPattern(url, webResourceCollectionType.getHttpMethodArray(), transport, allMap);
                }
            }
        }

        PermissionCollection excludedPermissions = new Permissions();
        PermissionCollection uncheckedPermissions = new Permissions();

        for (URLPattern pattern : excludedPatterns.values()) {
            String name = pattern.getQualifiedPattern(allSet);
            String actions = pattern.getMethods();

            excludedPermissions.add(new WebResourcePermission(name, actions));
            excludedPermissions.add(new WebUserDataPermission(name, actions));
        }

        for (Map.Entry<String, Map<String, URLPattern>> entry : rolesPatterns.entrySet()) {
            Set<URLPattern> currentRolePatterns = new HashSet<URLPattern>(entry.getValue().values());
            for (URLPattern pattern : entry.getValue().values()) {
                String name = pattern.getQualifiedPattern(currentRolePatterns);
                String actions = pattern.getMethods();
                WebResourcePermission permission = new WebResourcePermission(name, actions);
                addPermissionToRole(entry.getKey(), permission, rolePermissions);
                HTTPMethods methods = pattern.getHTTPMethods();
                int transportType = pattern.getTransport();
                addOrUpdatePattern(uncheckedUserPatterns, name, methods, transportType);
            }
        }

        for (URLPattern pattern : uncheckedPatterns.values()) {
            String name = pattern.getQualifiedPattern(allSet);
            HTTPMethods methods = pattern.getHTTPMethods();

            addOrUpdatePattern(uncheckedResourcePatterns, name, methods, URLPattern.NA);

            int transportType = pattern.getTransport();
            addOrUpdatePattern(uncheckedUserPatterns, name, methods, transportType);
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
        for (URLPattern pattern : allSet) {
            String name = pattern.getQualifiedPattern(allSet);
            HTTPMethods methods = pattern.getComplementedHTTPMethods();

            if (methods.isNone()) {
                continue;
            }

            addOrUpdatePattern(uncheckedResourcePatterns, name, methods, URLPattern.NA);
            addOrUpdatePattern(uncheckedUserPatterns, name, methods, URLPattern.NA);
        }

        URLPattern pattern = new URLPattern("/");
        if (!allSet.contains(pattern)) {
            String name = pattern.getQualifiedPattern(allSet);
            HTTPMethods methods = pattern.getComplementedHTTPMethods();

            addOrUpdatePattern(uncheckedResourcePatterns, name, methods, URLPattern.NA);
            addOrUpdatePattern(uncheckedUserPatterns, name, methods, URLPattern.NA);
        }

        //Create the uncheckedPermissions for WebResourcePermissions
        for (UncheckedItem item : uncheckedResourcePatterns.keySet()) {
            HTTPMethods methods = uncheckedResourcePatterns.get(item);
            String actions = URLPattern.getMethodsWithTransport(methods, item.getTransportType());

            uncheckedPermissions.add(new WebResourcePermission(item.getName(), actions));
        }
        //Create the uncheckedPermissions for WebUserDataPermissions
        for (UncheckedItem item : uncheckedUserPatterns.keySet()) {
            HTTPMethods methods = uncheckedUserPatterns.get(item);
            String actions = URLPattern.getMethodsWithTransport(methods, item.getTransportType());

            uncheckedPermissions.add(new WebUserDataPermission(item.getName(), actions));
        }

        return new ComponentPermissions(excludedPermissions, uncheckedPermissions, rolePermissions);

    }

    private void analyzeURLPattern(String urlPattern, String[] httpMethods, String transport, Map<String, URLPattern> currentPatterns) {
        URLPattern pattern = currentPatterns.get(urlPattern);
        if (pattern == null) {
            pattern = new URLPattern(urlPattern);
            currentPatterns.put(urlPattern, pattern);
        }
        if (httpMethods.length == 0) {
            pattern.addMethod("");
        } else {
            for (String httpMethod : httpMethods) {
                if (httpMethod != null) {
                    pattern.addMethod(httpMethod.trim());
                }
            }
        }
        pattern.setTransport(transport);
    }

    protected void addPermissionToRole(String roleName, Permission permission, Map<String, PermissionCollection> rolePermissions) {
        PermissionCollection permissionsForRole = rolePermissions.get(roleName);
        if (permissionsForRole == null) {
            permissionsForRole = new Permissions();
            rolePermissions.put(roleName, permissionsForRole);
        }
        permissionsForRole.add(permission);
    }

    private void addOrUpdatePattern(Map<UncheckedItem, HTTPMethods> patternMap, String name, HTTPMethods actions, int transportType) {
        UncheckedItem item = new UncheckedItem(name, transportType);
        HTTPMethods existingActions = patternMap.get(item);
        if (existingActions != null) {
            patternMap.put(item, existingActions.add(actions));
            return;
        }

        patternMap.put(item, new HTTPMethods(actions, false));
    }

    protected static Set<String> collectRoleNames(WebAppType webApp) {
        Set<String> roleNames = new HashSet<String>();

        SecurityRoleType[] securityRoles = webApp.getSecurityRoleArray();
        for (SecurityRoleType securityRole : securityRoles) {
            roleNames.add(securityRole.getRoleName().getStringValue().trim());
        }

        return roleNames;
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

        if (!DeploymentUtil.recursiveDelete(configurationDir, cannotBeDeletedList)) {
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

    protected void processRoleRefPermissions(ServletType servletType, Set<String> securityRoles, Map<String, PermissionCollection> rolePermissions) {
        String servletName = servletType.getServletName().getStringValue().trim();
        //WebRoleRefPermissions
        SecurityRoleRefType[] securityRoleRefTypeArray = servletType.getSecurityRoleRefArray();
        Set<String> unmappedRoles = new HashSet<String>(securityRoles);
        for (SecurityRoleRefType securityRoleRefType : securityRoleRefTypeArray) {
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
        for (String roleName : unmappedRoles) {
            addPermissionToRole(roleName, new WebRoleRefPermission(servletName, roleName), rolePermissions);
        }
    }

    protected ClassFinder createWebAppClassFinder(WebAppType webApp, WebModule webModule) throws DeploymentException {
        // Get the classloader from the module's EARContext
        ClassLoader classLoader = webModule.getEarContext().getClassLoader();
        return createWebAppClassFinder(webApp, classLoader);
    }

    public static ClassFinder createWebAppClassFinder(WebAppType webApp, ClassLoader classLoader) throws DeploymentException {
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
                    clas = classLoader.loadClass(cls.getStringValue());
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
                clas = classLoader.loadClass(cls.getStringValue());
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
                clas = classLoader.loadClass(cls.getStringValue());
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

        Map compContext = NamingBuilder.JNDI_KEY.get(buildingContext);
        Holder holder = NamingBuilder.INJECTION_KEY.get(buildingContext);

        webModule.getSharedContext().put(WebModule.WEB_APP_DATA, webModuleData);
        webModule.getSharedContext().put(NamingBuilder.JNDI_KEY, compContext);
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

        webModuleData.setAttribute("componentContext", compContext);
        webModuleData.setReferencePattern("TransactionManager", moduleContext.getTransactionManagerName());
        webModuleData.setReferencePattern("TrackedConnectionAssociator", moduleContext.getConnectionTrackerName());
    }

    class UncheckedItem {
        final static int NA = 0x00;
        final static int INTEGRAL = 0x01;
        final static int CONFIDENTIAL = 0x02;

        private int transportType = NA;
        private String name;

        public UncheckedItem(String name, int transportType) {
            setName(name);
            setTransportType(transportType);
        }

        public boolean equals(Object o) {
            UncheckedItem item = (UncheckedItem) o;
            return item.transportType == transportType && item.name.equals(this.name);
        }


        public int hashCode() {
            return name.hashCode() + transportType;
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

        public void setTransportType(int transportType) {
            this.transportType = transportType;
        }
    }
}
