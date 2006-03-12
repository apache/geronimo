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

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.jar.JarFile;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Permission;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.security.jacc.WebRoleRefPermission;
import javax.security.jacc.WebUserDataPermission;
import javax.security.jacc.WebResourcePermission;

import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.xbeans.j2ee.WebAppType;
import org.apache.geronimo.xbeans.j2ee.ServletMappingType;
import org.apache.geronimo.xbeans.j2ee.SecurityConstraintType;
import org.apache.geronimo.xbeans.j2ee.WebResourceCollectionType;
import org.apache.geronimo.xbeans.j2ee.UrlPatternType;
import org.apache.geronimo.xbeans.j2ee.HttpMethodType;
import org.apache.geronimo.xbeans.j2ee.RoleNameType;
import org.apache.geronimo.xbeans.j2ee.SecurityRoleType;
import org.apache.geronimo.xbeans.j2ee.FilterMappingType;
import org.apache.geronimo.xbeans.j2ee.ServletType;
import org.apache.geronimo.xbeans.j2ee.SecurityRoleRefType;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.ConfigurationAlreadyExistsException;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.security.util.URLPattern;

/**
 * @version $Rev: 384686 $ $Date$
 */
public abstract class AbstractWebModuleBuilder implements ModuleBuilder {
    protected static final ObjectName MANAGED_CONNECTION_FACTORY_PATTERN;
    private static final ObjectName ADMIN_OBJECT_PATTERN;
    protected static final ObjectName STATELESS_SESSION_BEAN_PATTERN;
    protected static final ObjectName STATEFUL_SESSION_BEAN_PATTERN;
    protected static final ObjectName ENTITY_BEAN_PATTERN;
    protected final Kernel kernel;

    protected AbstractWebModuleBuilder(Kernel kernel) {
        this.kernel = kernel;
    }

    static {
        try {
            MANAGED_CONNECTION_FACTORY_PATTERN = ObjectName.getInstance("*:j2eeType=" + NameFactory.JCA_MANAGED_CONNECTION_FACTORY +  ",*");
            ADMIN_OBJECT_PATTERN = ObjectName.getInstance("*:j2eeType=" + NameFactory.JCA_ADMIN_OBJECT +  ",*");
            STATELESS_SESSION_BEAN_PATTERN = ObjectName.getInstance("*:j2eeType=" + NameFactory.STATELESS_SESSION_BEAN +  ",*");
            STATEFUL_SESSION_BEAN_PATTERN = ObjectName.getInstance("*:j2eeType=" + NameFactory.STATEFUL_SESSION_BEAN +  ",*");
            ENTITY_BEAN_PATTERN = ObjectName.getInstance("*:j2eeType=" + NameFactory.ENTITY_BEAN +  ",*");
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }

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

    public Module createModule(File plan, JarFile moduleFile) throws DeploymentException {
        return createModule(plan, moduleFile, "war", null, true, null);
    }

    public Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment environment, Object moduleContextInfo) throws DeploymentException {
        return createModule(plan, moduleFile, targetPath, specDDUrl, false, (String) moduleContextInfo);
    }

    protected abstract Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, boolean standAlone, String contextRoot) throws DeploymentException;

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

    public void installModule(JarFile earFile, EARContext earContext, Module module, ConfigurationStore configurationStore, Repository repository) throws DeploymentException {
        EARContext moduleContext;
        if (module.isStandAlone()) {
            moduleContext = earContext;
        } else {
            Environment environment = module.getEnvironment();
            Artifact earConfigId = earContext.getConfigID();
            Artifact configId = new Artifact(earConfigId.getGroupId(), earConfigId.getArtifactId() + "_" + module.getTargetPath(), earConfigId.getVersion(), "car");
            environment.setConfigId(configId);
            File configurationDir = null;
            try {
                configurationDir = configurationStore.createNewConfigurationDir(environment.getConfigId());
            } catch (ConfigurationAlreadyExistsException e) {
                throw new DeploymentException(e);
            }

            // construct the web app deployment context... this is the same class used by the ear context
            try {

                moduleContext = new EARContext(configurationDir,
                        environment,
                        ConfigurationModuleType.WAR,
                        kernel,
                        earContext.getJ2EEApplicationName(),
                        earContext.getTransactionContextManagerObjectName(),
                        earContext.getConnectionTrackerObjectName(),
                        earContext.getTransactedTimerName(),
                        earContext.getNonTransactedTimerName(),
                        earContext.getCORBAGBeanObjectName(),
                        earContext.getRefContext());
            } catch (Exception e) {
                DeploymentUtil.recursiveDelete(configurationDir);
                throw new DeploymentException("Could not create a deployment context for the web app", e);
            }
            //TODO this is extremely fishy
            //Add the ear parent here since it can't be loaded by any config store.
            environment.addDependency(earConfigId, ImportType.ALL);
        }
        module.setEarContext(moduleContext);

        try {
            URI baseDir = URI.create(module.getTargetPath() + "/");

            // add the warfile's content to the configuration
            JarFile warFile = module.getModuleFile();
            Enumeration entries = warFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                URI targetPath = baseDir.resolve(new URI(null, entry.getName(), null));
                if (entry.getName().equals("WEB-INF/web.xml")) {
                    moduleContext.addFile(targetPath, module.getOriginalSpecDD());
                } else if (entry.getName().startsWith("WEB-INF/lib") && entry.getName().endsWith(".jar")) {
                    moduleContext.addInclude(targetPath, warFile, entry);
                } else if (entry.getName().equals("WEB-INF/classes/")) {
                    moduleContext.addInclude(targetPath, warFile, entry);
                } else {
                    moduleContext.addFile(targetPath, warFile, entry);
                }
            }

            // add the manifest classpath entries declared in the war to the class loader
            // we have to explicitly add these since we are unpacking the web module
            // and the url class loader will not pick up a manifest from an unpacked dir
            moduleContext.addManifestClassPath(warFile, URI.create(module.getTargetPath()));

        } catch (IOException e) {
            throw new DeploymentException("Problem deploying war", e);
        } catch (URISyntaxException e) {
            throw new DeploymentException("Could not construct URI for location of war entry", e);
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
