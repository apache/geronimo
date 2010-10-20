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

package org.apache.geronimo.web.security;

import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyContextException;
import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebRoleRefPermission;
import javax.security.jacc.WebUserDataPermission;

import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.web.info.SecurityConstraintInfo;
import org.apache.geronimo.web.info.SecurityRoleRefInfo;
import org.apache.geronimo.web.info.ServletInfo;
import org.apache.geronimo.web.info.WebAppInfo;
import org.apache.geronimo.web.info.WebResourceCollectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class SpecSecurityBuilder {

    private static final Logger logger = LoggerFactory.getLogger(SpecSecurityBuilder.class);

    private final Set<String> securityRoles = new HashSet<String>();

    private final Map<String, URLPattern> uncheckedPatterns = new HashMap<String, URLPattern>();

    private final Map<UncheckedItem, HTTPMethods> uncheckedResourcePatterns = new HashMap<UncheckedItem, HTTPMethods>();

    private final Map<UncheckedItem, HTTPMethods> uncheckedUserPatterns = new HashMap<UncheckedItem, HTTPMethods>();

    private final Map<String, URLPattern> excludedPatterns = new HashMap<String, URLPattern>();

    private final Map<String, Map<String, URLPattern>> rolesPatterns = new HashMap<String, Map<String, URLPattern>>();

    private final Set<URLPattern> allSet = new HashSet<URLPattern>();

    private final Map<String, URLPattern> allMap = new HashMap<String, URLPattern>(); //uncheckedPatterns union excludedPatterns union rolesPatterns.

    private final RecordingPolicyConfiguration policyConfiguration = new RecordingPolicyConfiguration(true);

    private WebAppInfo webAppInfo;

    public SpecSecurityBuilder(WebAppInfo webAppInfo) {
        this.webAppInfo = webAppInfo;
    }

    public ComponentPermissions buildSpecSecurityConfig() {
        securityRoles.addAll(webAppInfo.securityRoles);
        try {
            for (ServletInfo servlet : webAppInfo.servlets) {
                processRoleRefPermissions(servlet);
            }
            //add the role-ref permissions for unmapped jsps
            addUnmappedJSPPermissions();
            analyzeSecurityConstraints(webAppInfo.securityConstraints);
            removeExcludedDups();
            return buildComponentPermissions();
        } catch (PolicyContextException e) {
            throw new IllegalStateException("Should not happen", e);
        }
    }

    private void analyzeSecurityConstraints(List<SecurityConstraintInfo> securityConstraints) {
        for (SecurityConstraintInfo securityConstraint : securityConstraints) {
            Map<String, URLPattern> currentPatterns = null;
            Set<String> roleNames = null;
            if (securityConstraint.authConstraint != null) {
                if (securityConstraint.authConstraint.roleNames.size() == 0) {
                    currentPatterns = excludedPatterns;
                } else {
                    roleNames = new HashSet<String>(securityConstraint.authConstraint.roleNames);
                    if(roleNames.remove("*")) {
                        roleNames.addAll(securityRoles);
                    }
                }
            } else {
                currentPatterns = uncheckedPatterns;
            }
            String transport = securityConstraint.userDataConstraint == null ? "NONE" : securityConstraint.userDataConstraint;
            
            boolean isRolebasedPatten = (currentPatterns == null);
            
            for (WebResourceCollectionInfo webResourceCollection : securityConstraint.webResourceCollections) {
                //Calculate HTTP methods list
                for (String urlPattern : webResourceCollection.urlPatterns) {
                    if (isRolebasedPatten) {
                        for (String roleName : roleNames) {
                            Map<String, URLPattern> currentRolePatterns = rolesPatterns.get(roleName);
                            if (currentRolePatterns == null) {
                                currentRolePatterns = new HashMap<String, URLPattern>();
                                rolesPatterns.put(roleName, currentRolePatterns);
                            }
                            analyzeURLPattern(urlPattern, webResourceCollection.httpMethods, webResourceCollection.omission, transport, currentRolePatterns);
                        }
                    } else {
                        analyzeURLPattern(urlPattern, webResourceCollection.httpMethods, webResourceCollection.omission, transport, currentPatterns);
                    }
                    URLPattern allPattern = allMap.get(urlPattern);
                    if (allPattern == null) {
                        allPattern = new URLPattern(urlPattern, webResourceCollection.httpMethods, webResourceCollection.omission);
                        allSet.add(allPattern);
                        allMap.put(urlPattern, allPattern);
                    } else {
                        allPattern.addMethods(webResourceCollection.httpMethods, webResourceCollection.omission);
                    }

                }
            }
        }
    }

    private void analyzeURLPattern(String urlPattern, Set<String> httpMethods, boolean omission, String transport, Map<String, URLPattern> currentPatterns) {
        URLPattern pattern = currentPatterns.get(urlPattern);
        if (pattern == null) {
            pattern = new URLPattern(urlPattern, httpMethods, omission);
            currentPatterns.put(urlPattern, pattern);
        } else {
            pattern.addMethods(httpMethods, omission);
        }
        pattern.setTransport(transport);
    }

    private void removeExcludedDups() {
        for (Map.Entry<String, URLPattern> excluded : excludedPatterns.entrySet()) {
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

    private ComponentPermissions buildComponentPermissions() throws PolicyContextException {
        for (URLPattern pattern : excludedPatterns.values()) {
            String name = pattern.getQualifiedPattern(allSet);
            String actions = pattern.getMethods();
            policyConfiguration.addToExcludedPolicy(new WebResourcePermission(name, actions));
            policyConfiguration.addToExcludedPolicy(new WebUserDataPermission(name, actions));
        }
        for (Map.Entry<String, Map<String, URLPattern>> entry : rolesPatterns.entrySet()) {
            Set<URLPattern> currentRolePatterns = new HashSet<URLPattern>(entry.getValue().values());
            for (URLPattern pattern : entry.getValue().values()) {
                String name = pattern.getQualifiedPattern(currentRolePatterns);
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
        if (!allMap.containsKey("/")) {
            URLPattern pattern = new URLPattern("/", Collections.<String> emptySet(), false);
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
        return policyConfiguration.getComponentPermissions();
    }

    private void addOrUpdatePattern(Map<UncheckedItem, HTTPMethods> patternMap, String name, HTTPMethods actions, int transportType) {
        UncheckedItem item = new UncheckedItem(name, transportType);
        HTTPMethods existingActions = patternMap.get(item);
        if (existingActions != null) {
            patternMap.put(item, existingActions.add(actions));
        } else {
            patternMap.put(item, new HTTPMethods(actions, false));
        }
    }

    protected void processRoleRefPermissions(ServletInfo servlet) throws PolicyContextException {
        String servletName = servlet.servletName.trim();
        //WebRoleRefPermissions
        Set<String> unmappedRoles = new HashSet<String>(securityRoles);
        for (SecurityRoleRefInfo securityRoleRef : servlet.securityRoleRefs) {
            //jacc 3.1.3.2
            /*   The name of the WebRoleRefPermission must be the servlet-name in whose
            * context the security-role-ref is defined. The actions of the  WebRoleRefPermission
            * must be the value of the role-name (that is the  reference), appearing in the security-role-ref.
            * The deployment tools must  call the addToRole method on the PolicyConfiguration object to add the
            * WebRoleRefPermission object resulting from the translation to the role
            * identified in the role-link appearing in the security-role-ref.
            */
            policyConfiguration.addToRole(securityRoleRef.roleLink, new WebRoleRefPermission(servletName, securityRoleRef.roleName));
            unmappedRoles.remove(securityRoleRef.roleName);
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

    public void clear() {
        securityRoles.clear();
        uncheckedPatterns.clear();
        uncheckedResourcePatterns.clear();
        uncheckedUserPatterns.clear();
        excludedPatterns.clear();
        rolesPatterns.clear();
        allSet.clear();
        allMap.clear();
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
