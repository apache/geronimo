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

import java.net.URL;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyContextException;
import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebRoleRefPermission;
import javax.security.jacc.WebUserDataPermission;
import javax.servlet.HttpMethodConstraintElement;
import javax.servlet.Servlet;
import javax.servlet.ServletSecurityElement;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.HttpMethodConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.ServletSecurity.TransportGuarantee;

import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.kernel.util.JarUtils;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.xbeans.javaee6.AuthConstraintType;
import org.apache.geronimo.xbeans.javaee6.RoleNameType;
import org.apache.geronimo.xbeans.javaee6.SecurityConstraintType;
import org.apache.geronimo.xbeans.javaee6.SecurityRoleRefType;
import org.apache.geronimo.xbeans.javaee6.SecurityRoleType;
import org.apache.geronimo.xbeans.javaee6.ServletMappingType;
import org.apache.geronimo.xbeans.javaee6.ServletType;
import org.apache.geronimo.xbeans.javaee6.UrlPatternType;
import org.apache.geronimo.xbeans.javaee6.WebAppDocument;
import org.apache.geronimo.xbeans.javaee6.WebAppType;
import org.apache.geronimo.xbeans.javaee6.WebResourceCollectionType;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
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
    private WebAppType initialWebApp;

    private Bundle bundle;

    private boolean annotationScanRequired;

    private Set<String> urlPatternsConfiguredInDeploymentPlans = new HashSet<String>();

    /**
     *   dynamicSecurityWebApp contains all the servlet security constraints configured by ServletRegistration.Dynamic interface
     */
    private WebAppType dynamicSecurityWebApp;

    /**
     * annotationSecurityWebApp contains all the servlet security constraints configured by ServletConstraint annotation
     */
    private WebAppType annotationSecurityWebApp;

    public SpecSecurityBuilder(WebAppType webApp) {
        this(webApp, null, false);
    }

    public SpecSecurityBuilder(WebAppType initialWebApp, Bundle bundle, boolean annotationScanRequired) {
        this.initialWebApp = initialWebApp;
        if (annotationScanRequired && bundle == null) {
            throw new IllegalArgumentException("Bundle parameter could not be null while annotation scanning is required");
        }
        this.bundle = bundle;
        this.annotationScanRequired = annotationScanRequired;
        initialize();
    }

    public SpecSecurityBuilder(Bundle bundle, boolean annotationScanRequired) {
        this.bundle = bundle;
        this.annotationScanRequired = annotationScanRequired;
        URL specDDUrl = BundleUtils.getEntry(bundle, "WEB-INF/web.xml");
        if (specDDUrl == null) {
            initialWebApp = WebAppType.Factory.newInstance();
        } else {
            try {
                String specDD = JarUtils.readAll(specDDUrl);
                XmlObject parsed = XmlBeansUtil.parse(specDD);
                WebAppDocument webAppDoc = SchemaConversionUtils.convertToServletSchema(parsed);
                initialWebApp = webAppDoc.getWebApp();
            } catch (XmlException e) {
                throw new IllegalArgumentException("Error parsing web.xml for " + bundle.getSymbolicName(), e);
            } catch (Exception e) {
                throw new IllegalArgumentException("Error reading web.xml for " + bundle.getSymbolicName(), e);
            }
        }
        initialize();
    }

    public SpecSecurityBuilder(Bundle bundle, String deploymentDescriptor, boolean annotationScanRequired) {
        this.bundle = bundle;
        this.annotationScanRequired = annotationScanRequired;
        if (deploymentDescriptor == null || deploymentDescriptor.length() == 0) {
            initialWebApp = WebAppType.Factory.newInstance();
        } else {
            try {
                XmlObject parsed = XmlBeansUtil.parse(deploymentDescriptor);
                WebAppDocument webAppDoc = SchemaConversionUtils.convertToServletSchema(parsed);
                initialWebApp = webAppDoc.getWebApp();
            } catch (XmlException e) {
                throw new IllegalArgumentException("Error parsing web.xml for " + bundle.getSymbolicName(), e);
            } catch (Exception e) {
                throw new IllegalArgumentException("Error reading web.xml for " + bundle.getSymbolicName(), e);
            }
        }
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
            dynamicSecurityWebApp = WebAppType.Factory.newInstance();
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

    private void overrideSecurityConstraints(WebAppType webApp, Collection<String> urlPatterns) {
        for (SecurityConstraintType securityConstraint : webApp.getSecurityConstraintArray()) {
            int iCurrentWebResourceCollectionIndex = 0;
            for (WebResourceCollectionType webResourceCollection : securityConstraint.getWebResourceCollectionArray()) {
                Set<String> validateAnnotationUrlPatterns = new HashSet<String>();
                for (UrlPatternType urlPattern : webResourceCollection.getUrlPatternArray()) {
                    if (!urlPatterns.contains(urlPattern.getStringValue())) {
                        validateAnnotationUrlPatterns.add(urlPattern.getStringValue());
                    }
                }
                if (validateAnnotationUrlPatterns.size() == 0) {
                    securityConstraint.removeWebResourceCollection(iCurrentWebResourceCollectionIndex);
                    continue;
                } else if (validateAnnotationUrlPatterns.size() < webResourceCollection.getUrlPatternArray().length) {
                    for (int i = 0, iLoopSize = webResourceCollection.getUrlPatternArray().length; i < iLoopSize; i++) {
                        webResourceCollection.removeUrlPattern(0);
                    }
                    for (String validateAnnotationUrlPattern : validateAnnotationUrlPatterns) {
                        webResourceCollection.addNewUrlPattern().setStringValue(validateAnnotationUrlPattern);
                    }
                }
                iCurrentWebResourceCollectionIndex++;
            }
        }
    }

    public ComponentPermissions buildSpecSecurityConfig() {
        if (dynamicSecurityWebApp != null) {
            for (SecurityConstraintType securityConstraint : dynamicSecurityWebApp.getSecurityConstraintArray()) {
                initialWebApp.addNewSecurityConstraint().set(securityConstraint);
            }
        }
        if (annotationSecurityWebApp != null) {
            for (SecurityConstraintType securityConstraint : annotationSecurityWebApp.getSecurityConstraintArray()) {
                initialWebApp.addNewSecurityConstraint().set(securityConstraint);
            }
        }
        collectRoleNames(initialWebApp.getSecurityRoleArray());
        try {
            for (ServletType servletType : initialWebApp.getServletArray()) {
                processRoleRefPermissions(servletType);
            }
            //add the role-ref permissions for unmapped jsps
            addUnmappedJSPPermissions();
            analyzeSecurityConstraints(initialWebApp.getSecurityConstraintArray());
            removeExcludedDups();
            return buildComponentPermissions();
        } catch (PolicyContextException e) {
            throw new IllegalStateException("Should not happen", e);
        }
    }

    private void analyzeSecurityConstraints(SecurityConstraintType[] securityConstraintArray) {
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
                //Calculate HTTP methods list
                Set<String> httpMethods = new HashSet<String>();
                //While using HTTP omission methods and empty methods (which means all methods) as the configurations, isExcluded value is true
                //While using HTTP methods as the configurations, isExcluded value is false
                boolean isExcludedList = true;
                if (webResourceCollectionType.getHttpMethodArray().length > 0) {
                    isExcludedList = false;
                    for (String httpMethod : webResourceCollectionType.getHttpMethodArray()) {
                        if (httpMethod != null) {
                            httpMethods.add(httpMethod.trim());
                        }
                    }
                } else if (webResourceCollectionType.getHttpMethodOmissionArray().length > 0) {
                    for (String httpMethodOmission : webResourceCollectionType.getHttpMethodOmissionArray()) {
                        if (httpMethodOmission != null) {
                            httpMethods.add(httpMethodOmission.trim());
                        }
                    }
                }
                for (UrlPatternType urlPatternType : webResourceCollectionType.getUrlPatternArray()) {
                    String url = urlPatternType.getStringValue().trim();
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

    /**
     * 1. Scan ServletConstraint annotations to build a map for conflict checking
     * 2. Build a url-pattern
     */
    private void initialize() {
        // Initialize urlPatternsConfiguredInDeploymentPlans map, which contains all the url patterns configured in portable deployment plan
        for (SecurityConstraintType secuirtyConstrait : initialWebApp.getSecurityConstraintArray()) {
            for (WebResourceCollectionType webResourceCollection : secuirtyConstrait.getWebResourceCollectionArray()) {
                for (UrlPatternType urlPattern : webResourceCollection.getUrlPatternArray()) {
                    urlPatternsConfiguredInDeploymentPlans.add(urlPattern.getStringValue());
                }
            }
        }
        //Scan ServletConstraint annotations if required
        if (annotationScanRequired) {
            annotationSecurityWebApp = WebAppType.Factory.newInstance();
            scanServletConstraintAnnotations();
        }
    }

    private void scanServletConstraintAnnotations() {
        try {
            Map<String, Set<String>> servletClassNameUrlPatternsMap = genetateServletClassUrlPatternsMap();
            for (ServletType servlet : initialWebApp.getServletArray()) {
                if (servlet.getServletClass() == null || servlet.getServletClass().getStringValue().isEmpty()) {
                    continue;
                }
                String servletClassName = servlet.getServletClass().getStringValue();
                Class<?> cls = bundle.loadClass(servletClassName);
                if (!Servlet.class.isAssignableFrom(cls)) {
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
     * @return null when emptyRoleSemantic=PERMIT AND rolesAllowed={} AND transportGuarantee=NONE
     */
    private SecurityConstraintType addNewSecurityConstraint(WebAppType webApp, String[] rolesAllowed, TransportGuarantee transportGuarantee, ServletSecurity.EmptyRoleSemantic emptyRoleSemantic) {
        //IF emptyRoleSemantic=PERMIT AND rolesAllowed={} AND transportGuarantee=NONE then
        //  No Constraint
        //END IF
        if (rolesAllowed.length > 0 || transportGuarantee.equals(TransportGuarantee.CONFIDENTIAL) || emptyRoleSemantic.equals(ServletSecurity.EmptyRoleSemantic.DENY)) {
            SecurityConstraintType securityConstraint = webApp.addNewSecurityConstraint();
            if (transportGuarantee.equals(TransportGuarantee.CONFIDENTIAL)) {
                securityConstraint.addNewUserDataConstraint().addNewTransportGuarantee().setStringValue(TransportGuarantee.CONFIDENTIAL.name());
            }
            if (emptyRoleSemantic.equals(ServletSecurity.EmptyRoleSemantic.DENY)) {
                securityConstraint.addNewAuthConstraint();
            } else {
                AuthConstraintType authConstraint = securityConstraint.addNewAuthConstraint();
                for (String roleAllowed : rolesAllowed) {
                    authConstraint.addNewRoleName().setStringValue(roleAllowed);
                }
            }
            return securityConstraint;
        }
        return null;
    }

    private SecurityConstraintType addNewHTTPSecurityConstraint(WebAppType webApp, String[] rolesAllowed, TransportGuarantee transportGuarantee, ServletSecurity.EmptyRoleSemantic emptyRoleSemantic,
            String[] omissionMethods, Collection<String> urlPatterns) {
        SecurityConstraintType securityConstraint = addNewSecurityConstraint(webApp, rolesAllowed, transportGuarantee, emptyRoleSemantic);
        if (omissionMethods.length > 0 || securityConstraint != null) {
            if (securityConstraint == null) {
                securityConstraint = webApp.addNewSecurityConstraint();
            }
            WebResourceCollectionType webResourceCollection = securityConstraint.getWebResourceCollectionArray().length == 0 ? securityConstraint.addNewWebResourceCollection() : securityConstraint
                    .getWebResourceCollectionArray(0);
            for (String omissionMethod : omissionMethods) {
                webResourceCollection.addNewHttpMethodOmission().setStringValue(omissionMethod);
            }
            for (String urlPattern : urlPatterns) {
                webResourceCollection.addNewUrlPattern().setStringValue(urlPattern);
            }
        }
        return securityConstraint;
    }

    private SecurityConstraintType addNewHTTPMethodSecurityConstraint(WebAppType webApp, String[] rolesAllowed, TransportGuarantee transportGuarantee,
            ServletSecurity.EmptyRoleSemantic emptyRoleSemantic, String httpMethod, Collection<String> urlPatterns) {
        SecurityConstraintType securityConstraint = addNewSecurityConstraint(webApp, rolesAllowed, transportGuarantee, emptyRoleSemantic);
        if (securityConstraint == null) {
            securityConstraint = webApp.addNewSecurityConstraint();
        }
        WebResourceCollectionType webResourceCollection = securityConstraint.getWebResourceCollectionArray().length == 0 ? securityConstraint.addNewWebResourceCollection() : securityConstraint
                .getWebResourceCollectionArray(0);
        for (String urlPattern : urlPatterns) {
            webResourceCollection.addNewUrlPattern().setStringValue(urlPattern);
        }
        webResourceCollection.addNewHttpMethod().setStringValue(httpMethod);
        return securityConstraint;
    }

    /**
     * The return map contains the servlet class -> url patterns pairs, which are not configured in the security-constraint elements in the deployment plan.
     * Because the security-constraint configurations in the deployment plan have the highest priority, those constraints configured by annotations should not override them
     * @return
     */
    private Map<String, Set<String>> genetateServletClassUrlPatternsMap() {
        Map<String, Set<String>> servletNameUrlPatternsMap = new HashMap<String, Set<String>>();
        for (ServletMappingType servletMapping : initialWebApp.getServletMappingArray()) {
            String servletName = servletMapping.getServletName().getStringValue();
            Set<String> urlPatterns = servletNameUrlPatternsMap.get(servletName);
            if (urlPatterns == null) {
                urlPatterns = new HashSet<String>();
                servletNameUrlPatternsMap.put(servletName, urlPatterns);
            }
            for (UrlPatternType urlPattern : servletMapping.getUrlPatternArray()) {
                if (!urlPatternsConfiguredInDeploymentPlans.contains(urlPattern.getStringValue())) {
                    urlPatterns.add(urlPattern.getStringValue());
                }
            }
        }
        Map<String, Set<String>> servletClassUrlPatternsMap = new HashMap<String, Set<String>>();
        for (ServletType servlet : initialWebApp.getServletArray()) {
            if (servlet.getServletClass() == null || servlet.getServletClass().getStringValue().isEmpty()) {
                continue;
            }
            String servletClassName = servlet.getServletClass().getStringValue();
            Set<String> urlPatterns = servletClassUrlPatternsMap.get(servlet.getServletClass().getStringValue());
            if (urlPatterns == null) {
                urlPatterns = new HashSet<String>();
                servletClassUrlPatternsMap.put(servletClassName, urlPatterns);
            }
            Set<String> servletMappingUrlPatterns = servletNameUrlPatternsMap.get(servlet.getServletName().getStringValue());
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
