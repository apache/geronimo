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
import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyContextException;

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
    private final Map<String, URLPattern> uncheckedPatterns = new HashMap<String, URLPattern>();
    private final Map<UncheckedItem, HTTPMethods> uncheckedResourcePatterns = new HashMap<UncheckedItem, HTTPMethods>();
    private final Map<UncheckedItem, HTTPMethods> uncheckedUserPatterns = new HashMap<UncheckedItem, HTTPMethods>();
    private final Map<String, URLPattern> excludedPatterns = new HashMap<String, URLPattern>();
    private final Map<String, Map<String, URLPattern>> rolesPatterns = new HashMap<String, Map<String, URLPattern>>();
    private final Set<URLPattern> allSet = new HashSet<URLPattern>();   // == allMap.values()
    private final Map<String, URLPattern> allMap = new HashMap<String, URLPattern>();   //uncheckedPatterns union excludedPatterns union rolesPatterns.
//    private boolean useExcluded = false;
    private boolean useExcluded = true;

    private final RecordingPolicyConfiguration policyConfiguration = new RecordingPolicyConfiguration(true);

    public ComponentPermissions buildSpecSecurityConfig(WebAppType webApp) {
        collectRoleNames(webApp.getSecurityRoleArray());
        //role refs
        try {
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
        } catch (PolicyContextException e) {
            throw new IllegalStateException("Should not happen", e);
        }
    }

    public void analyzeSecurityConstraints(SecurityConstraintType[] securityConstraintArray) {
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

            WebResourceCollectionType[] webResourceCollectionTypeArray = securityConstraintType.getWebResourceCollectionArray();
            for (WebResourceCollectionType webResourceCollectionType : webResourceCollectionTypeArray) {
                UrlPatternType[] urlPatternTypeArray = webResourceCollectionType.getUrlPatternArray();
                for (UrlPatternType urlPatternType : urlPatternTypeArray) {
                    String url = urlPatternType.getStringValue().trim();
                    if(currentPatterns == null) {
                        for (String roleName : roleNames) {
                            currentPatterns = rolesPatterns.get(roleName);
                            if (currentPatterns == null) {
                                currentPatterns = new HashMap<String, URLPattern>();
                                rolesPatterns.put(roleName, currentPatterns);
                            }
                            analyzeURLPattern(url, webResourceCollectionType.getHttpMethodArray(), transport, currentPatterns);
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

    public void removeExcludedDups() {
        for (Map.Entry<String, URLPattern> excluded: excludedPatterns.entrySet()) {
            String url = excluded.getKey();
            URLPattern pattern = excluded.getValue();
            removeExcluded(url, pattern, uncheckedPatterns);
            for (Map<String, URLPattern> rolePatterns : rolesPatterns.values()) {
                removeExcluded(url, pattern, rolePatterns);
            }
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

    public ComponentPermissions buildComponentPermissions() throws PolicyContextException {

        if (useExcluded) {
            for (URLPattern pattern : excludedPatterns.values()) {
                String name = pattern.getQualifiedPattern(allSet);
                String actions = pattern.getMethods();

                policyConfiguration.addToExcludedPolicy(new WebResourcePermission(name, actions));
                policyConfiguration.addToExcludedPolicy(new WebUserDataPermission(name, actions));
            }
        }
        for (Map.Entry<String, Map<String, URLPattern>> entry : rolesPatterns.entrySet()) {
            for (URLPattern pattern : entry.getValue().values()) {
                String name = pattern.getQualifiedPattern(allSet);
                String actions = pattern.getMethods();
                WebResourcePermission permission = new WebResourcePermission(name, actions);
                policyConfiguration.addToRole(entry.getKey(), permission);
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

            policyConfiguration.addToUncheckedPolicy(new WebResourcePermission(item.getName(), actions));
        }
        //Create the uncheckedPermissions for WebUserDataPermissions
        for (UncheckedItem item : uncheckedUserPatterns.keySet()) {
            HTTPMethods methods = uncheckedUserPatterns.get(item);
            String actions = URLPattern.getMethodsWithTransport(methods, item.getTransportType());

            policyConfiguration.addToUncheckedPolicy(new WebUserDataPermission(item.getName(), actions));
        }
        //System.out.println(policyConfiguration.getAudit());
        return policyConfiguration.getComponentPermissions();
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

    protected void processRoleRefPermissions(ServletType servletType) throws PolicyContextException {
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
            policyConfiguration.addToRole(roleLink, new WebRoleRefPermission(servletName, roleName));
            unmappedRoles.remove(roleName);
        }
        for (String roleName : unmappedRoles) {
            policyConfiguration.addToRole(roleName, new WebRoleRefPermission(servletName, roleName));
        }
    }

    protected void addUnmappedJSPPermissions() throws PolicyContextException {
        for (String roleName : securityRoles) {
            policyConfiguration.addToRole(roleName, new WebRoleRefPermission("", roleName));
        }
    }

    protected void collectRoleNames(SecurityRoleType[] securityRoles) {
        for (SecurityRoleType securityRole : securityRoles) {
            this.securityRoles.add(securityRole.getRoleName().getStringValue().trim());
        }
    }


    private static class RecordingPolicyConfiguration implements PolicyConfiguration {
        private final PermissionCollection excludedPermissions = new Permissions();
        private final PermissionCollection uncheckedPermissions = new Permissions();
        private final Map<String, PermissionCollection> rolePermissions = new HashMap<String, PermissionCollection>();

        private final StringBuilder audit;


        private RecordingPolicyConfiguration(boolean audit) {
            if (audit) {
                this.audit = new StringBuilder();
            } else {
                this.audit = null;
            }
        }

        public String getContextID() throws PolicyContextException {
            return null;
        }

        public void addToRole(String roleName, PermissionCollection permissions) {
            throw new IllegalStateException("not implemented");
        }

        public void addToRole(String roleName, Permission permission) throws PolicyContextException {
            if (audit != null) {
                audit.append("Role: ").append(roleName).append(" -> ").append(permission).append('\n');
            }
            PermissionCollection permissionsForRole = rolePermissions.get(roleName);
            if (permissionsForRole == null) {
                permissionsForRole = new Permissions();
                rolePermissions.put(roleName, permissionsForRole);
            }
            permissionsForRole.add(permission);
        }

        public void addToUncheckedPolicy(PermissionCollection permissions) {
            throw new IllegalStateException("not implemented");
        }

        public void addToUncheckedPolicy(Permission permission) throws PolicyContextException {
            if (audit != null) {
                audit.append("Unchecked -> ").append(permission).append('\n');
            }
            uncheckedPermissions.add(permission);
        }

        public void addToExcludedPolicy(PermissionCollection permissions) {
            throw new IllegalStateException("not implemented");
        }

        public void addToExcludedPolicy(Permission permission) throws PolicyContextException {
            if (audit != null) {
                audit.append("Excluded -> ").append(permission).append('\n');
            }
            excludedPermissions.add(permission);
        }

        public void removeRole(String roleName) throws PolicyContextException {
            throw new IllegalStateException("not implemented");
        }

        public void removeUncheckedPolicy() throws PolicyContextException {
            throw new IllegalStateException("not implemented");
        }

        public void removeExcludedPolicy() throws PolicyContextException {
            throw new IllegalStateException("not implemented");
        }

        public void linkConfiguration(PolicyConfiguration link) throws PolicyContextException {
            throw new IllegalStateException("not implemented");
        }

        public void delete() throws PolicyContextException {
            throw new IllegalStateException("not implemented");
        }

        public void commit() throws PolicyContextException {
            throw new IllegalStateException("not implemented");
        }

        public boolean inService() throws PolicyContextException {
            throw new IllegalStateException("not implemented");
        }

        public ComponentPermissions getComponentPermissions() {
            return new ComponentPermissions(excludedPermissions, uncheckedPermissions, rolePermissions);
        }

        public String getAudit() {
            if (audit == null) {
                return "no audit kept";
            }
            return audit.toString();
        }

    }
}
