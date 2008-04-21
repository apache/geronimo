/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.web25.deployment.security;

import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebUserDataPermission;
import javax.security.jacc.WebRoleRefPermission;

import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.xbeans.javaee.RoleNameType;
import org.apache.geronimo.xbeans.javaee.SecurityConstraintType;
import org.apache.geronimo.xbeans.javaee.UrlPatternType;
import org.apache.geronimo.xbeans.javaee.WebAppType;
import org.apache.geronimo.xbeans.javaee.WebResourceCollectionType;
import org.apache.geronimo.xbeans.javaee.SecurityRoleType;
import org.apache.geronimo.xbeans.javaee.ServletType;
import org.apache.geronimo.xbeans.javaee.SecurityRoleRefType;

/**
 * @version $Rev$ $Date$
 */
public class SpecSecurityBuilder {
    private final Set<String> securityRoles = new HashSet<String>();
    private final Map<String, PermissionCollection> rolePermissions = new HashMap<String, PermissionCollection>();
    private final Map<String, URLPattern> uncheckedPatterns = new HashMap<String, URLPattern>();
    private final Map<UncheckedItem, HTTPMethods> uncheckedResourcePatterns = new HashMap<UncheckedItem, HTTPMethods>();
    private final Map<UncheckedItem, HTTPMethods> uncheckedUserPatterns = new HashMap<UncheckedItem, HTTPMethods>();
    private final Map<String, URLPattern> excludedPatterns = new HashMap<String, URLPattern>();
    private final Map<String, URLPattern> rolesPatterns = new HashMap<String, URLPattern>();
    private final Set<URLPattern> allSet = new HashSet<URLPattern>();   // == allMap.values()
    private final Map<String, URLPattern> allMap = new HashMap<String, URLPattern>();   //uncheckedPatterns union excludedPatterns union rolesPatterns.
//    private boolean useExcluded = false;
    private boolean useExcluded = true;

    public ComponentPermissions buildSpecSecurityConfig(WebAppType webApp) {
        collectRoleNames(webApp.getSecurityRoleArray());
        //role refs
        for (ServletType servletType: webApp.getServletArray()) {
           processRoleRefPermissions(servletType);
        }
        //add the role-ref permissions for unmapped jsps
        addUnmappedJSPPermissions();

        analyzeSecurityConstraints(webApp.getSecurityConstraintArray());
//        if (!useExcluded) {
            removeExcludedDups();
//        }

        return buildComponentPermissions();
    }

    public void analyzeSecurityConstraints(SecurityConstraintType[] securityConstraintArray) {
        for (SecurityConstraintType securityConstraintType : securityConstraintArray) {
            Map<String, URLPattern> currentPatterns;
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
            for (WebResourceCollectionType webResourceCollectionType : webResourceCollectionTypeArray) {
                UrlPatternType[] urlPatternTypeArray = webResourceCollectionType.getUrlPatternArray();
                for (UrlPatternType urlPatternType : urlPatternTypeArray) {
                    String url = urlPatternType.getStringValue().trim();
                    URLPattern pattern = currentPatterns.get(url);
                    if (pattern == null) {
                        pattern = new URLPattern(url);
                        currentPatterns.put(url, pattern);
                    }

                    URLPattern allPattern = allMap.get(url);
                    if (allPattern == null) {
                        allPattern = new URLPattern(url);
                        allSet.add(allPattern);
                        allMap.put(url, allPattern);
                    }

                    String[] httpMethodTypeArray = webResourceCollectionType.getHttpMethodArray();
                    if (httpMethodTypeArray.length == 0) {
                        pattern.addMethod("");
                        allPattern.addMethod("");
                    } else {
                        for (String aHttpMethodTypeArray : httpMethodTypeArray) {
                            String method = (aHttpMethodTypeArray == null ? null : aHttpMethodTypeArray.trim());
                            if (method != null) {
                                pattern.addMethod(method);
                                allPattern.addMethod(method);
                            }
                        }
                    }
                    if (currentPatterns == rolesPatterns) {
                        RoleNameType[] roleNameTypeArray = securityConstraintType.getAuthConstraint().getRoleNameArray();
                        for (RoleNameType roleNameType : roleNameTypeArray) {
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
    }

    public void removeExcludedDups() {
        for (Map.Entry<String, URLPattern> excluded: excludedPatterns.entrySet()) {
            String url = excluded.getKey();
            URLPattern pattern = excluded.getValue();
            removeExcluded(url, pattern, uncheckedPatterns);
            removeExcluded(url, pattern, rolesPatterns);
        }
    }

    private void removeExcluded(String url, URLPattern pattern, Map<String, URLPattern> patterns) {
        URLPattern testPattern = patterns.get(url);
        if (testPattern != null) {
            if (!testPattern.removeMethods(pattern)) {
                patterns.remove(url);
            }
        }
    }

    public ComponentPermissions buildComponentPermissions() {
        PermissionCollection excludedPermissions = new Permissions();
        PermissionCollection uncheckedPermissions = new Permissions();

        if (useExcluded) {
            for (URLPattern pattern : excludedPatterns.values()) {
                String name = pattern.getQualifiedPattern(allSet);
                String actions = pattern.getMethods();

                excludedPermissions.add(new WebResourcePermission(name, actions));
                excludedPermissions.add(new WebUserDataPermission(name, actions));
            }
        }

        for (URLPattern pattern : rolesPatterns.values()) {
            String name = pattern.getQualifiedPattern(allSet);
            String actions = pattern.getMethods();
            WebResourcePermission permission = new WebResourcePermission(name, actions);

            for (String roleName : pattern.getRoles()) {
                addPermissionToRole(roleName, permission);
            }
            HTTPMethods methods = pattern.getHTTPMethods();
            int transportType = pattern.getTransport();

            addOrUpdatePattern(uncheckedUserPatterns, name, methods, transportType);
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

    public void addPermissionToRole(String roleName, Permission permission) {
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

    protected void processRoleRefPermissions(ServletType servletType) {
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
            addPermissionToRole(roleLink, new WebRoleRefPermission(servletName, roleName));
            unmappedRoles.remove(roleName);
        }
        for (String roleName : unmappedRoles) {
            addPermissionToRole(roleName, new WebRoleRefPermission(servletName, roleName));
        }
    }

    protected void addUnmappedJSPPermissions() {
        for (String roleName : securityRoles) {
            addPermissionToRole(roleName, new WebRoleRefPermission("", roleName));
        }
    }

    protected void collectRoleNames(SecurityRoleType[] securityRoles) {
        for (SecurityRoleType securityRole : securityRoles) {
            this.securityRoles.add(securityRole.getRoleName().getStringValue().trim());
        }
    }

}
