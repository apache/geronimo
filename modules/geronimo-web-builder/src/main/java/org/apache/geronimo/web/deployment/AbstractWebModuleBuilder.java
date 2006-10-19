/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.web.deployment;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebRoleRefPermission;
import javax.security.jacc.WebUserDataPermission;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilderCollection;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.deployment.xbeans.ServiceDocument;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.security.util.URLPattern;
import org.apache.geronimo.xbeans.j2ee.FilterMappingType;
import org.apache.geronimo.xbeans.j2ee.HttpMethodType;
import org.apache.geronimo.xbeans.j2ee.RoleNameType;
import org.apache.geronimo.xbeans.j2ee.SecurityConstraintType;
import org.apache.geronimo.xbeans.j2ee.SecurityRoleRefType;
import org.apache.geronimo.xbeans.j2ee.SecurityRoleType;
import org.apache.geronimo.xbeans.j2ee.ServletMappingType;
import org.apache.geronimo.xbeans.j2ee.ServletType;
import org.apache.geronimo.xbeans.j2ee.UrlPatternType;
import org.apache.geronimo.xbeans.j2ee.WebAppType;
import org.apache.geronimo.xbeans.j2ee.WebResourceCollectionType;
import org.apache.geronimo.xbeans.j2ee.WebAppDocument;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerSecurityDocument;
import org.apache.geronimo.naming.deployment.ResourceEnvironmentSetter;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlDocumentProperties;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractWebModuleBuilder implements ModuleBuilder {
    private static final Log log = LogFactory.getLog(AbstractWebModuleBuilder.class);
    private static final QName TAGLIB = new QName(SchemaConversionUtils.J2EE_NAMESPACE, "taglib");
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

    protected final NamingBuilder namingBuilders;

    private static final QName SECURITY_QNAME = GerSecurityDocument.type.getDocumentElementName();
    private static final QName SERVICE_QNAME = ServiceDocument.type.getDocumentElementName();

    /**
     * Manifest classpath entries in a war configuration must be resolved relative to the war configuration, not the
     * enclosing ear configuration.  Resolving relative to he war configuration using this offset produces the same
     * effect as URI.create(module.targetPath()).resolve(mcpEntry) executed in the ear configuration.
     */
    private static final URI RELATIVE_MODULE_BASE_URI = URI.create("../");

    protected AbstractWebModuleBuilder(Kernel kernel, Collection securityBuilders, Collection serviceBuilders, NamingBuilder namingBuilders, ResourceEnvironmentSetter resourceEnvironmentSetter) {
        this.kernel = kernel;
        this.securityBuilders = new NamespaceDrivenBuilderCollection(securityBuilders);
        this.serviceBuilders = new NamespaceDrivenBuilderCollection(serviceBuilders);
        this.namingBuilders = namingBuilders;
        this.resourceEnvironmentSetter = resourceEnvironmentSetter;
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

    //TODO configid these need to be converted to ReferencePatterns
    protected Set findGBeanDependencies(EARContext earContext) {
        Set dependencies = new HashSet();
        dependencies.addAll(earContext.listGBeans(MANAGED_CONNECTION_FACTORY_PATTERN));
        dependencies.addAll(earContext.listGBeans(ADMIN_OBJECT_PATTERN));
        dependencies.addAll(earContext.listGBeans(STATELESS_SESSION_BEAN_PATTERN));
        dependencies.addAll(earContext.listGBeans(STATEFUL_SESSION_BEAN_PATTERN));
        dependencies.addAll(earContext.listGBeans(ENTITY_BEAN_PATTERN));
        return dependencies;
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
     * @param webApp
     * @param contextRoot
     * @return map of servlet names to path mapped to them.  Possibly inaccurate except for web services.
     */
    protected Map buildServletNameToPathMap(WebAppType webApp, String contextRoot) {
        contextRoot = "/" + contextRoot;
        Map map = new HashMap();
        ServletMappingType[] servletMappings = webApp.getServletMappingArray();
        for (int j = 0; j < servletMappings.length; j++) {
            ServletMappingType servletMapping = servletMappings[j];
            String servletName = servletMapping.getServletName().getStringValue().trim();
            map.put(servletName, contextRoot + servletMapping.getUrlPattern().getStringValue().trim());
        }
        return map;
    }

    protected String determineDefaultContextRoot(WebAppType webApp, boolean isStandAlone, JarFile moduleFile, String targetPath) {

        if (webApp != null && webApp.getId() != null) {
            return webApp.getId();
        }

        if (isStandAlone) {
            // default configId is based on the moduleFile name
            return trimPath(new File(moduleFile.getName()).getName());
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

            // always add WEB-INF/classes to the classpath regardless of whether
            // any classes exist.  This must be searched BEFORE the WEB-INF/lib jar files,
            // per the servlet specifications.
            moduleContext.getConfiguration().addToClassPath("WEB-INF/classes/");

            // add the warfile's content to the configuration
            JarFile warFile = module.getModuleFile();
            Enumeration entries = warFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                URI targetPath = new URI(null, entry.getName(), null);
                if (entry.getName().equals("WEB-INF/web.xml")) {
                    moduleContext.addFile(targetPath, module.getOriginalSpecDD());
                } else if (entry.getName().startsWith("WEB-INF/lib") && entry.getName().endsWith(".jar")) {
                    moduleContext.addInclude(targetPath, warFile, entry);
                } else {
                    moduleContext.addFile(targetPath, warFile, entry);
                }
            }

            // add the manifest classpath entries declared in the war to the class loader
            // we have to explicitly add these since we are unpacking the web module
            // and the url class loader will not pick up a manifest from an unpacked dir
            moduleContext.addManifestClassPath(warFile, RELATIVE_MODULE_BASE_URI);

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
    }

    protected void addUnmappedJSPPermissions(Set securityRoles, Map rolePermissions) {
        for (Iterator iter = securityRoles.iterator(); iter.hasNext();) {
            String roleName = (String) iter.next();
            addPermissionToRole(roleName, new WebRoleRefPermission("", roleName), rolePermissions);
        }
    }

    protected ComponentPermissions buildSpecSecurityConfig(WebAppType webApp, Set securityRoles, Map rolePermissions) {
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
                    String url = urlPatternType.getStringValue().trim();
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

        return new ComponentPermissions(excludedPermissions, uncheckedPermissions, rolePermissions);

    }

    protected void addPermissionToRole(String roleName, Permission permission, Map rolePermissions) {
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

    protected static Set collectRoleNames(WebAppType webApp) {
        Set roleNames = new HashSet();

        SecurityRoleType[] securityRoles = webApp.getSecurityRoleArray();
        for (int i = 0; i < securityRoles.length; i++) {
            roleNames.add(securityRoles[i].getRoleName().getStringValue().trim());
        }

        return roleNames;
    }

    protected static void check(WebAppType webApp) throws DeploymentException {
        checkURLPattern(webApp);
        checkMultiplicities(webApp);
    }

    private static void checkURLPattern(WebAppType webApp) throws DeploymentException {

        FilterMappingType[] filterMappings = webApp.getFilterMappingArray();
        for (int i = 0; i < filterMappings.length; i++) {
            if (filterMappings[i].isSetUrlPattern()) {
                checkString(filterMappings[i].getUrlPattern().getStringValue().trim());
            }
        }

        ServletMappingType[] servletMappings = webApp.getServletMappingArray();
        for (int i = 0; i < servletMappings.length; i++) {
            checkString(servletMappings[i].getUrlPattern().getStringValue().trim());
        }

        SecurityConstraintType[] constraints = webApp.getSecurityConstraintArray();
        for (int i = 0; i < constraints.length; i++) {
            WebResourceCollectionType[] collections = constraints[i].getWebResourceCollectionArray();
            for (int j = 0; j < collections.length; j++) {
                UrlPatternType[] patterns = collections[j].getUrlPatternArray();
                for (int k = 0; k < patterns.length; k++) {
                    checkString(patterns[k].getStringValue().trim());
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
        if (webApp.getSessionConfigArray().length > 1) throw new DeploymentException("Multiple <session-config> elements found");
        if (webApp.getJspConfigArray().length > 1) throw new DeploymentException("Multiple <jsp-config> elements found");
        if (webApp.getLoginConfigArray().length > 1) throw new DeploymentException("Multiple <login-config> elements found");
    }

    private boolean cleanupConfigurationDir(File configurationDir)
    {
        LinkedList cannotBeDeletedList = new LinkedList();

        if (!DeploymentUtil.recursiveDelete(configurationDir,cannotBeDeletedList)) {
            // Output a message to help user track down file problem
            log.warn("Unable to delete " + cannotBeDeletedList.size() +
                    " files while recursively deleting directory "
                    + configurationDir + LINE_SEP +
                    "The first file that could not be deleted was:" + LINE_SEP + "  "+
                    ( !cannotBeDeletedList.isEmpty() ? cannotBeDeletedList.getFirst() : "") );
            return false;
        }
        return true;
    }

    protected void processRoleRefPermissions(ServletType servletType, Set securityRoles, Map rolePermissions) {
        String servletName = servletType.getServletName().getStringValue().trim();
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
    }

    protected void buildSubstitutionGroups(XmlObject gerWebApp, boolean hasSecurityRealmName, Module module, EARContext earContext) throws DeploymentException {
        XmlObject[] securityElements = XmlBeansUtil.selectSubstitutionGroupElements(SECURITY_QNAME, gerWebApp);
        if (securityElements.length > 0 && !hasSecurityRealmName) {
            throw new DeploymentException("You have supplied a security configuration for web app " + module.getName() + " but no security-realm-name to allow login");
        }
        securityBuilders.build(gerWebApp, earContext, module.getEarContext());
        serviceBuilders.build(gerWebApp, earContext, module.getEarContext());
    }

    protected static WebAppDocument convertToServletSchema(XmlObject xmlObject) throws XmlException {
        if (WebAppDocument.type.equals(xmlObject.schemaType())) {
            XmlBeansUtil.validateDD(xmlObject);
            return (WebAppDocument) xmlObject;
        }
        XmlCursor cursor = xmlObject.newCursor();
        try {
            cursor.toStartDoc();
            cursor.toFirstChild();
            if (SchemaConversionUtils.J2EE_NAMESPACE.equals(cursor.getName().getNamespaceURI())) {
                XmlObject result = xmlObject.changeType(WebAppDocument.type);
                XmlBeansUtil.validateDD(result);
                return (WebAppDocument) result;
            }

            XmlDocumentProperties xmlDocumentProperties = cursor.documentProperties();
            String publicId = xmlDocumentProperties.getDoctypePublicId();
            if ("-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN".equals(publicId) ||
                    "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN".equals(publicId)) {
                XmlCursor moveable = xmlObject.newCursor();
                try {
                    moveable.toStartDoc();
                    moveable.toFirstChild();
                    String schemaLocationURL = "http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd";
                    String version = "2.4";
                    SchemaConversionUtils.convertToSchema(cursor, SchemaConversionUtils.J2EE_NAMESPACE, schemaLocationURL, version);
                    cursor.toStartDoc();
                    cursor.toChild(SchemaConversionUtils.J2EE_NAMESPACE, "web-app");
                    cursor.toFirstChild();
                    SchemaConversionUtils.convertToDescriptionGroup(SchemaConversionUtils.J2EE_NAMESPACE, cursor, moveable);
                    SchemaConversionUtils.convertToJNDIEnvironmentRefsGroup(SchemaConversionUtils.J2EE_NAMESPACE, cursor, moveable);
                    cursor.push();
                    if (cursor.toNextSibling(TAGLIB)) {
                        cursor.toPrevSibling();
                        moveable.toCursor(cursor);
                        cursor.beginElement("jsp-config", SchemaConversionUtils.J2EE_NAMESPACE);
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
                            SchemaConversionUtils.convertToDescriptionGroup(SchemaConversionUtils.J2EE_NAMESPACE, cursor, moveable);
                            while (cursor.toNextSibling(SchemaConversionUtils.J2EE_NAMESPACE, "init-param")) {
                                cursor.push();
                                cursor.toFirstChild();
                                SchemaConversionUtils.convertToDescriptionGroup(SchemaConversionUtils.J2EE_NAMESPACE, cursor, moveable);
                                cursor.pop();
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
}
