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
package org.apache.geronimo.jetty.interceptor;

import java.io.IOException;
import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.x500.X500Principal;
import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebRoleRefPermission;
import javax.security.jacc.WebUserDataPermission;

import org.mortbay.http.Authenticator;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.SecurityConstraint;
import org.mortbay.http.UserRealm;
import org.mortbay.jetty.servlet.FormAuthenticator;
import org.mortbay.jetty.servlet.ServletHttpRequest;

import org.apache.geronimo.common.GeronimoSecurityException;
import org.apache.geronimo.jetty.JAASJettyPrincipal;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.PrimaryRealmPrincipal;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.SubjectId;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.security.deploy.DistinguishedName;
import org.apache.geronimo.security.deploy.Realm;
import org.apache.geronimo.security.deploy.Role;
import org.apache.geronimo.security.deploy.Security;
import org.apache.geronimo.security.jacc.RoleMappingConfiguration;
import org.apache.geronimo.security.jacc.RoleMappingConfigurationFactory;
import org.apache.geronimo.security.util.ConfigurationUtil;


/**
 * @version $Rev:  $ $Date:  $
 */
public class SecurityContextBeforeAfter implements BeforeAfter {

    private final BeforeAfter next;
    private final int policyContextIDIndex;
    private final int webAppContextIndex;
    private final String policyContextID;
    private final static ThreadLocal currentWebAppContext = new ThreadLocal();
    private final Map roleDesignates = new HashMap();
    private final JAASJettyPrincipal defaultPrincipal;

    private final String formLoginPath;
    private final PolicyConfigurationFactory factory;
    private final PolicyConfiguration policyConfiguration;

    private final PermissionCollection checked = new Permissions();
    private final PermissionCollection excludedPermissions;
    private final Authenticator authenticator;

    private final UserRealm realm;

    public SecurityContextBeforeAfter(BeforeAfter next,
                                      int policyContextIDIndex,
                                      int webAppContextIndex,
                                      String policyContextID,
                                      Security securityConfig,
                                      Authenticator authenticator,
                                      Set securityRoles,
                                      PermissionCollection uncheckedPermissions,
                                      PermissionCollection excludedPermissions,
                                      Map rolePermissions,
                                      UserRealm realm) throws PolicyContextException, ClassNotFoundException {
        this.next = next;
        this.policyContextIDIndex = policyContextIDIndex;
        this.webAppContextIndex = webAppContextIndex;
        this.policyContextID = policyContextID;

        this.defaultPrincipal = generateDefaultPrincipal(securityConfig);

        if (authenticator instanceof FormAuthenticator) {
            String formLoginPath = ((FormAuthenticator) authenticator).getLoginPage();
            if (formLoginPath.indexOf('?') > 0) {
                formLoginPath = formLoginPath.substring(0, formLoginPath.indexOf('?'));
            }
            this.formLoginPath = formLoginPath;
        } else {
            formLoginPath = null;
        }

        this.authenticator = authenticator;
        /**
         * Register our default principal with the ContextManager
         */
        Subject defaultSubject = defaultPrincipal.getSubject();
        ContextManager.registerSubject(defaultSubject);
        SubjectId id = ContextManager.getSubjectId(defaultSubject);
        defaultSubject.getPrincipals().add(new IdentificationPrincipal(id));

//        log.debug("Default subject " + id + " for JACC policy '" + policyContextID + "' registered.");

        /**
         * Get the JACC policy configuration that's associated with this
         * web application and configure it with the geronimo security
         * configuration.  The work for this is done by the class
         * JettyXMLConfiguration.
         */
        factory = PolicyConfigurationFactory.getPolicyConfigurationFactory();

        policyConfiguration = factory.getPolicyConfiguration(policyContextID, true);
        configure(uncheckedPermissions, excludedPermissions, rolePermissions);
        RoleMappingConfiguration roleMapper = RoleMappingConfigurationFactory.getRoleMappingFactory().getRoleMappingConfiguration(policyContextID, false);
        addRoleMappings(securityRoles, securityConfig, roleMapper);
        policyConfiguration.commit();
        this.excludedPermissions = excludedPermissions;

        Set allRolePermissions = new HashSet();
        for (Iterator iterator = rolePermissions.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Set permissionsForRole = (Set) entry.getValue();
            allRolePermissions.addAll(permissionsForRole);
        }
        for (Iterator iterator = allRolePermissions.iterator(); iterator.hasNext();) {
            Permission permission = (Permission) iterator.next();
            checked.add(permission);
        }

        this.realm = realm;
//        log.info("JettyWebAppJACCContext started with JACC policy '" + policyContextID + "'");
    }

    public void registerServletHolder(Map webRoleRefPermissions) throws PolicyContextException {
        PolicyConfiguration policyConfiguration = factory.getPolicyConfiguration(policyContextID, false);
        for (Iterator iterator = webRoleRefPermissions.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String roleName = (String) entry.getValue();
            WebRoleRefPermission webRoleRefPermission = (WebRoleRefPermission) entry.getKey();
            policyConfiguration.addToRole(roleName, webRoleRefPermission);
        }
        policyConfiguration.commit();

    }

    public void before(Object[] context, HttpRequest httpRequest, HttpResponse httpResponse) {
        context[policyContextIDIndex] = PolicyContext.getContextID();
        context[webAppContextIndex] = getCurrentSecurityInterceptor();

        PolicyContext.setContextID(policyContextID);
        setCurrentSecurityInterceptor(this);

        if (next != null) {
            next.before(context, httpRequest, httpResponse);
        }
    }

    public void after(Object[] context, HttpRequest httpRequest, HttpResponse httpResponse) {
        if (next != null) {
            next.after(context, httpRequest, httpResponse);
        }
        setCurrentSecurityInterceptor((SecurityContextBeforeAfter) context[webAppContextIndex]);
        PolicyContext.setContextID((String) context[policyContextIDIndex]);
    }

    private static void setCurrentSecurityInterceptor(SecurityContextBeforeAfter context) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(ContextManager.SET_CONTEXT);

        currentWebAppContext.set(context);
    }

    private static SecurityContextBeforeAfter getCurrentSecurityInterceptor() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(ContextManager.GET_CONTEXT);

        return (SecurityContextBeforeAfter) currentWebAppContext.get();
    }

    public static Subject getCurrentRoleDesignate(String role) {
        return getCurrentSecurityInterceptor().getRoleDesignate(role);
    }

    private Subject getRoleDesignate(String roleName) {
        return (Subject) roleDesignates.get(roleName);
    }

    private void setRoleDesignate(String roleName, Subject subject) {
        roleDesignates.put(roleName, subject);
    }
    
    //security check methods, delegated from WebAppContext
    
    /**
     * Check the security constraints using JACC.
     *
     * @param pathInContext path in context
     * @param request       HTTP request
     * @param response      HTTP response
     * @return true if the path in context passes the security check,
     *         false if it fails or a redirection has occured during authentication.
     */
    public boolean checkSecurityConstraints(String pathInContext, HttpRequest request, HttpResponse response) throws HttpException, IOException {
        if (formLoginPath != null) {
            String pathToBeTested = (pathInContext.indexOf('?') > 0 ? pathInContext.substring(0, pathInContext.indexOf('?')) : pathInContext);

            if (pathToBeTested.equals(formLoginPath)) {
                return true;
            }
        }

        try {
            Principal user = obtainUser(pathInContext, request, response);

            if (user == null) {
                return false;
            }
            if (user == SecurityConstraint.__NOBODY) {
                return true;
            }

            AccessControlContext acc = ContextManager.getCurrentContext();
            ServletHttpRequest servletHttpRequest = (ServletHttpRequest) request.getWrapper();

            /**
             * JACC v1.0 secion 4.1.1
             */

            String transportType;
            if (request.isConfidential()) {
                transportType = "CONFIDENTIAL";
            } else if (request.isIntegral()) {
                transportType = "INTEGRAL";
            } else {
                transportType = null;
            }
            WebUserDataPermission wudp = new WebUserDataPermission(servletHttpRequest.getServletPath(), new String[]{servletHttpRequest.getMethod()}, transportType);
            acc.checkPermission(wudp);

            /**
             * JACC v1.0 secion 4.1.2
             */
            acc.checkPermission(new WebResourcePermission(servletHttpRequest));
        } catch (HttpException he) {
            response.sendError(he.getCode(), he.getReason());
            return false;
        } catch (AccessControlException ace) {
            response.sendError(HttpResponse.__403_Forbidden);
            return false;
        }
        return true;
    }

    /**
     * Obtain an authenticated user, if one is required.  Otherwise return the
     * default principal.
     * <p/>
     * Also set the current caller for JACC security checks for the default
     * principal.  This is automatically done by <code>JAASJettyRealm</code>.
     *
     * @param pathInContext path in context
     * @param request       HTTP request
     * @param response      HTTP response
     * @return <code>null</code> if there is no authenticated user at the moment
     *         and security checking should not proceed and servlet handling should also
     *         not proceed, e.g. redirect. <code>SecurityConstraint.__NOBODY</code> if
     *         security checking should not proceed and servlet handling should proceed,
     *         e.g. login page.
     */
    private Principal obtainUser(String pathInContext, HttpRequest request, HttpResponse response) throws IOException, IOException {
        ServletHttpRequest servletHttpRequest = (ServletHttpRequest) request.getWrapper();
        WebResourcePermission resourcePermission = new WebResourcePermission(servletHttpRequest);
        WebUserDataPermission dataPermission = new WebUserDataPermission(servletHttpRequest);
        boolean unauthenticated = !(checked.implies(resourcePermission) || checked.implies(dataPermission));
        boolean forbidden = excludedPermissions.implies(resourcePermission) || excludedPermissions.implies(dataPermission);

//       Authenticator authenticator = getAuthenticator();
        Principal user = null;
        if (!unauthenticated && !forbidden) {
            if (realm == null) {
//               log.warn("Realm Not Configured");
                throw new HttpException(HttpResponse.__500_Internal_Server_Error, "Realm Not Configured");
            }


            // Handle pre-authenticated request
            if (authenticator != null) {
                // User authenticator.
                user = authenticator.authenticate(realm, pathInContext, request, response);
            } else {
                // don't know how authenticate
//               log.warn("Mis-configured Authenticator for " + request.getPath());
                throw new HttpException(HttpResponse.__500_Internal_Server_Error, "Mis-configured Authenticator for " + request.getPath());
            }

            return user;
        } else if (authenticator instanceof FormAuthenticator && pathInContext.endsWith(FormAuthenticator.__J_SECURITY_CHECK)) {
            /**
             * This could be a post request to __J_SECURITY_CHECK.
             */
            if (realm == null) {
//               log.warn("Realm Not Configured");
                throw new HttpException(HttpResponse.__500_Internal_Server_Error, "Realm Not Configured");
            }
            return authenticator.authenticate(realm, pathInContext, request, response);
        }

        /**
         * No authentication is required.  Return the defaultPrincipal.
         */
        ContextManager.setCurrentCaller(defaultPrincipal.getSubject());
        return defaultPrincipal;
    }


    /**
     * Generate the default principal from the security config.
     *
     * @param securityConfig The Geronimo security configuration.
     * @return the default principal
     */
    protected JAASJettyPrincipal generateDefaultPrincipal(Security securityConfig) throws GeronimoSecurityException {

        DefaultPrincipal defaultPrincipal = securityConfig.getDefaultPrincipal();
        if (defaultPrincipal == null) {
            throw new GeronimoSecurityException("Unable to generate default principal");
        }

        JAASJettyPrincipal result = new JAASJettyPrincipal("default");
        Subject defaultSubject = new Subject();

        RealmPrincipal realmPrincipal = ConfigurationUtil.generateRealmPrincipal(defaultPrincipal.getPrincipal(), defaultPrincipal.getRealmName());
        if (realmPrincipal == null) {
            throw new GeronimoSecurityException("Unable to create realm principal");
        }
        PrimaryRealmPrincipal primaryRealmPrincipal = ConfigurationUtil.generatePrimaryRealmPrincipal(defaultPrincipal.getPrincipal(), defaultPrincipal.getRealmName());
        if (primaryRealmPrincipal == null) {
            throw new GeronimoSecurityException("Unable to create primary realm principal");
        }

        defaultSubject.getPrincipals().add(realmPrincipal);
        defaultSubject.getPrincipals().add(primaryRealmPrincipal);

        result.setSubject(defaultSubject);

        return result;
    }


    public void addRoleMappings(Set securityRoles, Security security, RoleMappingConfiguration roleMapper) throws PolicyContextException, GeronimoSecurityException {

        for (Iterator roleMappings = security.getRoleMappings().values().iterator(); roleMappings.hasNext();) {
            Role role = (Role) roleMappings.next();
            String roleName = role.getRoleName();
            Set principalSet = new HashSet();

            if (!securityRoles.contains(roleName)) {
                throw new GeronimoSecurityException("Role '" + roleName + "' does not exist in this configuration");
            }

            Subject roleDesignate = new Subject();

            for (Iterator realms = role.getRealms().values().iterator(); realms.hasNext();) {
                Realm realm = (Realm) realms.next();

                for (Iterator principals = realm.getPrincipals().iterator(); principals.hasNext();) {
                    org.apache.geronimo.security.deploy.Principal principal = (org.apache.geronimo.security.deploy.Principal) principals.next();

                    RealmPrincipal realmPrincipal = ConfigurationUtil.generateRealmPrincipal(principal, realm.getRealmName());
                    if (realmPrincipal == null) {
                        throw new GeronimoSecurityException("Unable to create realm principal");
                    }

                    principalSet.add(realmPrincipal);
                    if (principal.isDesignatedRunAs()) {
                        roleDesignate.getPrincipals().add(realmPrincipal);
                    }
                }
            }

            for (Iterator names = role.getDNames().iterator(); names.hasNext();) {
                DistinguishedName dn = (DistinguishedName) names.next();

                X500Principal x500Principal = ConfigurationUtil.generateX500Principal(dn.getName());

                principalSet.add(x500Principal);
                if (dn.isDesignatedRunAs()) {
                    roleDesignate.getPrincipals().add(x500Principal);
                }
            }

            roleMapper.addRoleMapping(roleName, principalSet);

            if (roleDesignate.getPrincipals().size() > 0) {
                setRoleDesignate(roleName, roleDesignate);
            }
        }

        /**
         * Register the role designates with the context manager.
         *
         * THIS MUST BE RUN AFTER JettyXMLConfiguration.configure()
         */
        for (Iterator iter = roleDesignates.keySet().iterator(); iter.hasNext();) {
            String roleName = (String) iter.next();
            Subject roleDesignate = (Subject) roleDesignates.get(roleName);

            ContextManager.registerSubject(roleDesignate);
            SubjectId id = ContextManager.getSubjectId(roleDesignate);
            roleDesignate.getPrincipals().add(new IdentificationPrincipal(id));

//            log.debug("Role designate " + id + " for role '" + roleName + "' for JACC policy '" + policyContextID + "' registered.");
        }

    }

    private void configure(PermissionCollection uncheckedPermissions,
                           PermissionCollection excludedPermissions,
                           Map rolePermissions) throws GeronimoSecurityException {
        try {
            policyConfiguration.addToExcludedPolicy(excludedPermissions);
            policyConfiguration.addToUncheckedPolicy(uncheckedPermissions);
            for (Iterator iterator = rolePermissions.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String roleName = (String) entry.getKey();
                Set permissions = (Set) entry.getValue();
                for (Iterator iterator1 = permissions.iterator(); iterator1.hasNext();) {
                    Permission permission = (Permission) iterator1.next();
                    policyConfiguration.addToRole(roleName, permission);
                }
            }
        } catch (PolicyContextException e) {
            throw new GeronimoSecurityException(e);
        }
    }


    public void stop() throws PolicyContextException {
        for (Iterator iter = roleDesignates.keySet().iterator(); iter.hasNext();) {
            String roleName = (String) iter.next();
            Subject roleDesignate = (Subject) roleDesignates.get(roleName);

            ContextManager.unregisterSubject(roleDesignate);
//            log.debug("Role designate " + ContextManager.getSubjectId(roleDesignate) + " for role '" + roleName + "' for JACC policy '" + policyContextID + "' unregistered.");
        }
        ContextManager.unregisterSubject(defaultPrincipal.getSubject());

        if (policyConfiguration != null) {
            policyConfiguration.delete();
        }


    }
}
