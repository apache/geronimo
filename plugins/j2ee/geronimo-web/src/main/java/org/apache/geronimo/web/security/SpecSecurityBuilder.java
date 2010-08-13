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
import java.util.Collection;
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
import javax.servlet.HttpMethodConstraintElement;
import javax.servlet.ServletSecurityElement;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.HttpMethodConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.ServletSecurity.TransportGuarantee;

import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.openejb.jee.AuthConstraint;
import org.apache.openejb.jee.SecurityConstraint;
import org.apache.openejb.jee.SecurityRole;
import org.apache.openejb.jee.SecurityRoleRef;
import org.apache.openejb.jee.Servlet;
import org.apache.openejb.jee.ServletMapping;
import org.apache.openejb.jee.UserDataConstraint;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebResourceCollection;
import org.osgi.framework.Bundle;
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

    private final Map<String, URLPattern> rolesPatterns = new HashMap<String, URLPattern>();

    private final Set<URLPattern> allSet = new HashSet<URLPattern>();

    private final Map<String, URLPattern> allMap = new HashMap<String, URLPattern>(); //uncheckedPatterns union excludedPatterns union rolesPatterns.

    private final RecordingPolicyConfiguration policyConfiguration = new RecordingPolicyConfiguration(true);

    /**
     * webApp is xmlbean object of the initial web.xml ( May be merged all the web-fragment.xml files)
     */
    private WebApp initialWebApp;

    private Bundle bundle;

    private boolean annotationScanRequired;

    private Set<String> urlPatternsConfiguredInDeploymentPlans = new HashSet<String>();

    /**
     *   dynamicSecurityWebApp contains all the servlet security constraints configured by ServletRegistration.Dynamic interface
     */
    private WebApp dynamicSecurityWebApp;

    /**
     * annotationSecurityWebApp contains all the servlet security constraints configured by ServletConstraint annotation
     */
    private WebApp annotationSecurityWebApp;

    public SpecSecurityBuilder(WebApp webApp) {
        this(webApp, null, false);
    }

    public SpecSecurityBuilder(WebApp initialWebApp, Bundle bundle, boolean annotationScanRequired) {
        this.initialWebApp = initialWebApp;
        if (annotationScanRequired && bundle == null) {
            throw new IllegalArgumentException("Bundle parameter could not be null while annotation scanning is required");
        }
        this.bundle = bundle;
        this.annotationScanRequired = annotationScanRequired;
        initialize();
    }

    public void declareRoles(String... roleNames) {
        //Let's go ahead to directly add the roles to the securityRoles set. The set will be used in the collectRoleNames method.
        for (String roleName : roleNames) {
            if (roleName == null || roleName.trim().length() == 0) {
                throw new IllegalArgumentException("RoleName of null value or empty string is not allowed in declareRoles method");
            }
            securityRoles.add(roleName.trim());
        }
    }

    public Set<String> setServletSecurity(ServletSecurityElement constraint, Collection<String> urlPatterns) {
        if (dynamicSecurityWebApp == null) {
            dynamicSecurityWebApp = new WebApp();
        }
        Set<String> uneffectedUrlPatterns = new HashSet<String>();
        for (String urlPattern : urlPatterns) {
            if (urlPatternsConfiguredInDeploymentPlans.contains(urlPattern)) {
                uneffectedUrlPatterns.add(urlPattern);
            }
        }
        Collection<String> effectedUrlPatterns = null;
        if (uneffectedUrlPatterns.size() == 0) {
            effectedUrlPatterns = urlPatterns;
        } else {
            effectedUrlPatterns = new HashSet<String>();
            effectedUrlPatterns.addAll(urlPatterns);
            effectedUrlPatterns.removeAll(uneffectedUrlPatterns);
        }
        //Update SecurityConstraint configured by ServletSecurity annotations if required
        if (annotationSecurityWebApp != null) {
            overrideSecurityConstraints(annotationSecurityWebApp, effectedUrlPatterns);
        }
        //Update SecurityConstraint configured by previous setServletSecurity invocations
        overrideSecurityConstraints(dynamicSecurityWebApp, effectedUrlPatterns);
        //Update Role List
        //Roles that are implicitly declared as a result of their use within the setServletSecurity or setRunAsRole methods of the ServletRegistration interface
        //need not be declared.
        //Set SecurityConstraint
        if (constraint.getHttpMethodConstraints().size() > 0) {
            for (HttpMethodConstraintElement httpMethodConstraint : constraint.getHttpMethodConstraints()) {
                //Generate a security-constraint for each HttpMethodConstraint
                addNewHTTPMethodSecurityConstraint(dynamicSecurityWebApp, httpMethodConstraint.getRolesAllowed(), httpMethodConstraint.getTransportGuarantee(), httpMethodConstraint
                        .getEmptyRoleSemantic(), httpMethodConstraint.getMethodName(), effectedUrlPatterns);
               declareRoles(httpMethodConstraint.getRolesAllowed());
            }
        }
        addNewHTTPSecurityConstraint(dynamicSecurityWebApp, constraint.getRolesAllowed(), constraint.getTransportGuarantee(), constraint.getEmptyRoleSemantic(), constraint.getMethodNames()
                .toArray(new String[0]), effectedUrlPatterns);
        declareRoles(constraint.getRolesAllowed());
        return uneffectedUrlPatterns;
    }

    private void overrideSecurityConstraints(WebApp webApp, Collection<String> urlPatterns) {
        for (SecurityConstraint securityConstraint : webApp.getSecurityConstraint()) {
            int iCurrentWebResourceCollectionIndex = 0;
            for (WebResourceCollection webResourceCollection : securityConstraint.getWebResourceCollection()) {
                Set<String> validateAnnotationUrlPatterns = new HashSet<String>();
                for (String urlPattern : webResourceCollection.getUrlPattern()) {
                    if (!urlPatterns.contains(urlPattern)) {
                        validateAnnotationUrlPatterns.add(urlPattern);
                    }
                }
                if (validateAnnotationUrlPatterns.size() == 0) {
                    securityConstraint.getWebResourceCollection().remove(iCurrentWebResourceCollectionIndex);
                    continue;
                } else if (validateAnnotationUrlPatterns.size() < webResourceCollection.getUrlPattern().size()) {
                    for (int i = 0, iLoopSize = webResourceCollection.getUrlPattern().size(); i < iLoopSize; i++) {
                        webResourceCollection.getUrlPattern().remove(0);
                    }
                    for (String validateAnnotationUrlPattern : validateAnnotationUrlPatterns) {
                        webResourceCollection.getUrlPattern().add(validateAnnotationUrlPattern);
                    }
                }
                iCurrentWebResourceCollectionIndex++;
            }
        }
    }

    public ComponentPermissions buildSpecSecurityConfig() {
        if (dynamicSecurityWebApp != null) {
            for (SecurityConstraint securityConstraint : dynamicSecurityWebApp.getSecurityConstraint()) {
                initialWebApp.getSecurityConstraint().add(securityConstraint);
            }
        }
        if (annotationSecurityWebApp != null) {
            for (SecurityConstraint securityConstraint : annotationSecurityWebApp.getSecurityConstraint()) {
                initialWebApp.getSecurityConstraint().add(securityConstraint);
            }
        }
        collectRoleNames(initialWebApp.getSecurityRole());
        try {
            for (Servlet Servlet : initialWebApp.getServlet()) {
                processRoleRefPermissions(Servlet);
            }
            //add the role-ref permissions for unmapped jsps
            addUnmappedJSPPermissions();
            analyzeSecurityConstraints(initialWebApp.getSecurityConstraint());
            removeExcludedDups();
            return buildComponentPermissions();
        } catch (PolicyContextException e) {
            throw new IllegalStateException("Should not happen", e);
        }
    }

    private void analyzeSecurityConstraints(List<SecurityConstraint> securityConstraintArray) {
        for (SecurityConstraint SecurityConstraint : securityConstraintArray) {
            Map<String, URLPattern> currentPatterns;
            if (SecurityConstraint.getAuthConstraint() != null) {
                if (SecurityConstraint.getAuthConstraint().getRoleName().size() == 0) {
                    currentPatterns = excludedPatterns;
                } else {
                    currentPatterns = rolesPatterns;
                }
            } else {
                currentPatterns = uncheckedPatterns;
            }
            org.apache.openejb.jee.TransportGuarantee transport = org.apache.openejb.jee.TransportGuarantee.NONE;
            if (SecurityConstraint.getUserDataConstraint() != null) {
                transport = SecurityConstraint.getUserDataConstraint().getTransportGuarantee();
            }
            List<WebResourceCollection> WebResourceCollectionArray = SecurityConstraint.getWebResourceCollection();
            for (WebResourceCollection WebResourceCollection : WebResourceCollectionArray) {
                //Calculate HTTP methods list
                Set<String> httpMethods = new HashSet<String>();
                //While using HTTP omission methods and empty methods (which means all methods) as the configurations, isExcluded value is true
                //While using HTTP methods as the configurations, isExcluded value is false
                boolean isExcludedList = true;
                if (WebResourceCollection.getHttpMethod().size() > 0) {
                    isExcludedList = false;
                    for (String httpMethod : WebResourceCollection.getHttpMethod()) {
                        if (httpMethod != null) {
                            httpMethods.add(httpMethod.trim());
                        }
                    }
                } else if (WebResourceCollection.getHttpMethodOmission().size() > 0) {
                    for (String httpMethodOmission : WebResourceCollection.getHttpMethodOmission()) {
                        if (httpMethodOmission != null) {
                            httpMethods.add(httpMethodOmission.trim());
                        }
                    }
                }
                for (String urlPatternType : WebResourceCollection.getUrlPattern()) {
                    String url = urlPatternType.trim();
                    URLPattern pattern = currentPatterns.get(url);
                    if (pattern == null) {
                        pattern = new URLPattern(url, httpMethods, isExcludedList);
                        currentPatterns.put(url, pattern);
                    } else {
                        pattern.addMethods(httpMethods, isExcludedList);
                    }
                    URLPattern allPattern = allMap.get(url);
                    if (allPattern == null) {
                        allPattern = new URLPattern(url, httpMethods, isExcludedList);
                        allSet.add(allPattern);
                        allMap.put(url, allPattern);
                    } else {
                        allPattern.addMethods(httpMethods, isExcludedList);
                    }
                    if (currentPatterns == rolesPatterns) {
                        List<String> roleNameTypeArray = SecurityConstraint.getAuthConstraint().getRoleName();
                        for (String roleNameType : roleNameTypeArray) {
                            String role = roleNameType.trim();
                            if (role.equals("*")) {
                                pattern.addAllRoles(securityRoles);
                            } else {
                                pattern.addRole(role);
                            }
                        }
                    }
                    pattern.setTransport(transport.value());
                }
            }
        }
    }

    private void removeExcludedDups() {
        for (Map.Entry<String, URLPattern> excluded : excludedPatterns.entrySet()) {
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

    private ComponentPermissions buildComponentPermissions() throws PolicyContextException {
        for (URLPattern pattern : excludedPatterns.values()) {
            String name = pattern.getQualifiedPattern(allSet);
            String actions = pattern.getMethods();
            policyConfiguration.addToExcludedPolicy(new WebResourcePermission(name, actions));
            policyConfiguration.addToExcludedPolicy(new WebUserDataPermission(name, actions));
        }
        for (URLPattern pattern : rolesPatterns.values()) {
            String name = pattern.getQualifiedPattern(allSet);
            String actions = pattern.getMethods();
            WebResourcePermission permission = new WebResourcePermission(name, actions);
            for (String roleName : pattern.getRoles()) {
                policyConfiguration.addToRole(roleName, permission);
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
        if (!allMap.containsKey("/")) {
            URLPattern pattern = new URLPattern("/", Collections.EMPTY_SET, false);
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

    protected void processRoleRefPermissions(Servlet Servlet) throws PolicyContextException {
        String servletName = Servlet.getServletName().trim();
        //WebRoleRefPermissions
        List<SecurityRoleRef> SecurityRoleRefArray = Servlet.getSecurityRoleRef();
        Set<String> unmappedRoles = new HashSet<String>(securityRoles);
        for (SecurityRoleRef SecurityRoleRef : SecurityRoleRefArray) {
            String roleName = SecurityRoleRef.getRoleName().trim();
            String roleLink = SecurityRoleRef.getRoleLink().trim();
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

    protected void collectRoleNames(List<SecurityRole> securityRoles) {
        for (SecurityRole securityRole : securityRoles) {
            this.securityRoles.add(securityRole.getRoleName());
        }
    }

    /**
     * 1. Scan ServletConstraint annotations to build a map for conflict checking
     * 2. Build a url-pattern
     */
    private void initialize() {
        // Initialize urlPatternsConfiguredInDeploymentPlans map, which contains all the url patterns configured in portable deployment plan
        for (SecurityConstraint secuirtyConstrait : initialWebApp.getSecurityConstraint()) {
            for (WebResourceCollection webResourceCollection : secuirtyConstrait.getWebResourceCollection()) {
                for (String urlPattern : webResourceCollection.getUrlPattern()) {
                    urlPatternsConfiguredInDeploymentPlans.add(urlPattern);
                }
            }
        }
        //Scan ServletConstraint annotations if required
        if (annotationScanRequired) {
            annotationSecurityWebApp = new WebApp();
            scanServletConstraintAnnotations();
        }
    }

    private void scanServletConstraintAnnotations() {
        try {
            Map<String, Set<String>> servletClassNameUrlPatternsMap = genetateServletClassUrlPatternsMap();
            for (Servlet servlet : initialWebApp.getServlet()) {
                if (servlet.getServletClass() == null || servlet.getServletClass().isEmpty()) {
                    continue;
                }
                String servletClassName = servlet.getServletClass();
                Class<?> cls = bundle.loadClass(servletClassName);
                if (!javax.servlet.Servlet.class.isAssignableFrom(cls)) {
                    continue;
                }
                ServletSecurity servletSecurity = cls.getAnnotation(ServletSecurity.class);
                if (servletSecurity == null) {
                    continue;
                }
                Set<String> urlPatterns = servletClassNameUrlPatternsMap.get(servletClassName);
                if (urlPatterns == null || urlPatterns.isEmpty()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("No url pattern for the servlet class " + servletClassName + " is found in the deployment plan, SecurityConstraint annotation is ignored");
                    }
                    continue;
                }
                HttpConstraint httpConstraint = servletSecurity.value();
                if (servletSecurity.httpMethodConstraints().length > 0) {
                    String[] omissionMethods = new String[servletSecurity.httpMethodConstraints().length];
                    int iIndex = 0;
                    for (HttpMethodConstraint httpMethodConstraint : servletSecurity.httpMethodConstraints()) {
                        //Generate a security-constraint for each HttpMethodConstraint
                        String httpMethod = httpMethodConstraint.value().trim();
                        omissionMethods[iIndex++] = httpMethod;
                        addNewHTTPMethodSecurityConstraint(annotationSecurityWebApp, httpMethodConstraint.rolesAllowed(), httpMethodConstraint.transportGuarantee(), httpMethodConstraint
                                .emptyRoleSemantic(), httpMethod, urlPatterns);
                    }
                    addNewHTTPSecurityConstraint(annotationSecurityWebApp, httpConstraint.rolesAllowed(), httpConstraint.transportGuarantee(), httpConstraint.value(), omissionMethods, urlPatterns);
                } else {
                    addNewHTTPSecurityConstraint(annotationSecurityWebApp, httpConstraint.rolesAllowed(), httpConstraint.transportGuarantee(), httpConstraint.value(), new String[] {}, urlPatterns);
                }
            }
        } catch (ClassNotFoundException e) {
            //Should never occur, as webservice builder  have already checked it.
            logger.error("Fail to load class", e);
        }
    }

    /**
     * Create Security Constraint based on the arguments
     * @param webApp
     * @param rolesAllowed
     * @param transportGuarantee
     * @param emptyRoleSemantic
     * @param force
     * @return null when emptyRoleSemantic=PERMIT AND rolesAllowed={} AND transportGuarantee=NONE
     */
    private SecurityConstraint addNewSecurityConstraint(WebApp webApp, String[] rolesAllowed, TransportGuarantee transportGuarantee, ServletSecurity.EmptyRoleSemantic emptyRoleSemantic, boolean force) {
        //IF emptyRoleSemantic=PERMIT AND rolesAllowed={} AND transportGuarantee=NONE then
        //  No Constraint
        //END IF
        if (force || rolesAllowed.length > 0 || transportGuarantee.equals(TransportGuarantee.CONFIDENTIAL) || emptyRoleSemantic.equals(ServletSecurity.EmptyRoleSemantic.DENY)) {
            SecurityConstraint securityConstraint = new SecurityConstraint();
            WebResourceCollection webResourceCollection = new WebResourceCollection();
            securityConstraint.getWebResourceCollection().add(webResourceCollection);
            webApp.getSecurityConstraint().add(securityConstraint);
            if (transportGuarantee.equals(TransportGuarantee.CONFIDENTIAL)) {
                UserDataConstraint udc = new UserDataConstraint();
                udc.setTransportGuarantee(org.apache.openejb.jee.TransportGuarantee.fromValue(TransportGuarantee.CONFIDENTIAL.name()));
                securityConstraint.setUserDataConstraint(udc);
            }
            if (emptyRoleSemantic.equals(ServletSecurity.EmptyRoleSemantic.DENY)) {
                securityConstraint.setAuthConstraint(new AuthConstraint());
            } else if (rolesAllowed.length > 0) {
                //When rolesAllowed.length == 0 and emptyRoleSemantic.equals(ServletSecurity.EmptyRoleSemantic.PERMIT), no need to create the AuthConstraint object, as it means deny all
                AuthConstraint authConstraint = new AuthConstraint();
                for (String roleAllowed : rolesAllowed) {
                    authConstraint.getRoleName().add(roleAllowed);
                }
                securityConstraint.setAuthConstraint(authConstraint);
            }
            return securityConstraint;
        }
        return null;
    }

    private SecurityConstraint addNewHTTPSecurityConstraint(WebApp webApp, String[] rolesAllowed, TransportGuarantee transportGuarantee, ServletSecurity.EmptyRoleSemantic emptyRoleSemantic,
            String[] omissionMethods, Collection<String> urlPatterns) {
        SecurityConstraint securityConstraint = addNewSecurityConstraint(webApp, rolesAllowed, transportGuarantee, emptyRoleSemantic, omissionMethods.length > 0);
        if (securityConstraint != null) {
            WebResourceCollection webResourceCollection = securityConstraint.getWebResourceCollection().get(0);
            for (String omissionMethod : omissionMethods) {
                webResourceCollection.getHttpMethodOmission().add(omissionMethod);
            }
            webResourceCollection.getUrlPattern().addAll(urlPatterns);
        }
        return securityConstraint;
    }

    private SecurityConstraint addNewHTTPMethodSecurityConstraint(WebApp webApp, String[] rolesAllowed, TransportGuarantee transportGuarantee,
            ServletSecurity.EmptyRoleSemantic emptyRoleSemantic, String httpMethod, Collection<String> urlPatterns) {
        SecurityConstraint securityConstraint = addNewSecurityConstraint(webApp, rolesAllowed, transportGuarantee, emptyRoleSemantic, true);
        WebResourceCollection webResourceCollection = securityConstraint.getWebResourceCollection().get(0);
        webResourceCollection.getUrlPattern().addAll(urlPatterns);
        webResourceCollection.getHttpMethod().add(httpMethod);
        return securityConstraint;
    }

    /**
     * The return map contains the servlet class -> url patterns pairs, which are not configured in the security-constraint elements in the deployment plan.
     * Because the security-constraint configurations in the deployment plan have the highest priority, those constraints configured by annotations should not override them
     * @return
     */
    private Map<String, Set<String>> genetateServletClassUrlPatternsMap() {
        Map<String, Set<String>> servletNameUrlPatternsMap = new HashMap<String, Set<String>>();
        for (ServletMapping servletMapping : initialWebApp.getServletMapping()) {
            String servletName = servletMapping.getServletName();
            Set<String> urlPatterns = servletNameUrlPatternsMap.get(servletName);
            if (urlPatterns == null) {
                urlPatterns = new HashSet<String>();
                servletNameUrlPatternsMap.put(servletName, urlPatterns);
            }
            for (String urlPattern : servletMapping.getUrlPattern()) {
                if (!urlPatternsConfiguredInDeploymentPlans.contains(urlPattern)) {
                    urlPatterns.add(urlPattern);
                }
            }
        }
        Map<String, Set<String>> servletClassUrlPatternsMap = new HashMap<String, Set<String>>();
        for (Servlet servlet : initialWebApp.getServlet()) {
            if (servlet.getServletClass() == null || servlet.getServletClass().isEmpty()) {
                continue;
            }
            String servletClassName = servlet.getServletClass();
            Set<String> urlPatterns = servletClassUrlPatternsMap.get(servlet.getServletClass());
            if (urlPatterns == null) {
                urlPatterns = new HashSet<String>();
                servletClassUrlPatternsMap.put(servletClassName, urlPatterns);
            }
            Set<String> servletMappingUrlPatterns = servletNameUrlPatternsMap.get(servlet.getServletName());
            if (servletMappingUrlPatterns != null) {
                urlPatterns.addAll(servletMappingUrlPatterns);
            }
        }
        return servletClassUrlPatternsMap;
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
        initialWebApp = null;
        bundle = null;
        urlPatternsConfiguredInDeploymentPlans = null;
        dynamicSecurityWebApp = null;
        annotationSecurityWebApp = null;
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
