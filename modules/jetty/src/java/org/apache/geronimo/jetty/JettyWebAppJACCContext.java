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

package org.apache.geronimo.jetty;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Principal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Enumeration;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyContextException;
import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebRoleRefPermission;
import javax.security.jacc.WebUserDataPermission;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.http.Authenticator;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.SecurityConstraint;
import org.mortbay.http.UserRealm;
import org.mortbay.jetty.servlet.FormAuthenticator;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletHttpRequest;

import org.apache.geronimo.common.GeronimoSecurityException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.jetty.interceptor.SecurityContextBeforeAfter;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.PrimaryRealmPrincipal;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.SubjectId;
import org.apache.geronimo.security.deploy.AutoMapAssistant;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.security.deploy.Security;
import org.apache.geronimo.security.jacc.RoleMappingConfiguration;
import org.apache.geronimo.security.realm.SecurityRealm;
import org.apache.geronimo.security.util.ConfigurationUtil;
import org.apache.geronimo.transaction.OnlineUserTransaction;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;
import org.apache.geronimo.transaction.context.TransactionContextManager;


/**
 * A class extension to <code>JettyWebAppContext</code> whose purpose is to
 * provide JACC security checks.
 *
 * @version $Rev$ $Date$
 * @see org.mortbay.jetty.servlet.WebApplicationContext#checkSecurityConstraints(java.lang.String, org.mortbay.http.HttpRequest, org.mortbay.http.HttpResponse)
 */
public class JettyWebAppJACCContext extends JettyWebAppContext {
    private static Log log = LogFactory.getLog(JettyWebAppJACCContext.class);

    private final Kernel kernel;
    private final String policyContextID;
    private final String loginDomainName;
    private final Security securityConfig;
    private final JAASJettyPrincipal defaultPrincipal;

    private PolicyConfigurationFactory factory;
    private PolicyConfiguration policyConfiguration;

    private String formLoginPath;

    private final Set securityRoles;
    private final PermissionCollection excludedPermissions;
    private final PermissionCollection uncheckedPermissions;
    private final Map rolePermissions;

    PermissionCollection checked = new Permissions();

    private final SecurityContextBeforeAfter securityInterceptor;


    public JettyWebAppJACCContext() {
        kernel = null;
        policyContextID = null;
        loginDomainName = null;
        securityConfig = null;
        defaultPrincipal = null;
        this.securityRoles = null;
        this.excludedPermissions = null;
        this.uncheckedPermissions = null;
        this.rolePermissions = null;
        securityInterceptor = null;
    }

    public JettyWebAppJACCContext(URI uri,
                                  ReadOnlyContext componentContext,
                                  OnlineUserTransaction userTransaction,
                                  ClassLoader classLoader,
                                  URI[] webClassPath,
                                  boolean contextPriorityClassLoader,
                                  URL configurationBaseUrl,
                                  Set unshareableResources,
                                  Set applicationManagedSecurityResources,

                                  String displayName,
                                  Map contextParamMap,
                                  Collection listenerClassNames,
                                  boolean distributable,
                                  Map mimeMap,
                                  String[] welcomeFiles,
                                  Map localeEncodingMapping,
                                  Map errorPages,
                                  Authenticator authenticator,
                                  String realmName,
                                  Map tagLibMap,
                                  int sessionTimeoutSeconds,

                                  String policyContextID,
                                  String loginDomainName,
                                  Security securityConfig,
                                  //from jettyxmlconfig
                                  Set securityRoles,
                                  PermissionCollection uncheckedPermissions,
                                  PermissionCollection excludedPermissions,
                                  Map rolePermissions,

                                  TransactionContextManager transactionContextManager,
                                  TrackedConnectionAssociator trackedConnectionAssociator,
                                  JettyContainer jettyContainer,
                                  Kernel kernel) throws Exception, IllegalAccessException, InstantiationException, ClassNotFoundException {

        super(uri,
                componentContext,
                userTransaction,
                classLoader,
                webClassPath,
                contextPriorityClassLoader,
                configurationBaseUrl,
                unshareableResources,
                applicationManagedSecurityResources,

                displayName,
                contextParamMap,
                listenerClassNames,
                distributable,
                mimeMap,
                welcomeFiles,
                localeEncodingMapping,
                errorPages,
                authenticator,
                realmName,
                tagLibMap,
                sessionTimeoutSeconds,

                transactionContextManager,
                trackedConnectionAssociator,
                jettyContainer);

        this.kernel = kernel;
        setRealmName(realmName);
        //set the JAASJettyRealm as our realm.
        JAASJettyRealm realm = new JAASJettyRealm(realmName, loginDomainName);
        setRealm(realm);

        this.policyContextID = policyContextID;
        this.loginDomainName = loginDomainName;
        this.securityConfig = securityConfig;

        this.securityRoles = securityRoles;
        this.uncheckedPermissions = uncheckedPermissions;
        this.excludedPermissions = excludedPermissions;
        this.rolePermissions = rolePermissions;

        this.defaultPrincipal = generateDefaultPrincipal(securityConfig, loginDomainName);

        int index = contextLength;
        this.securityInterceptor = new SecurityContextBeforeAfter(chain, index++, index++, policyContextID);
        contextLength = index;
        chain = securityInterceptor;

        Set p = new HashSet();
        for (Iterator iterator = rolePermissions.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Set permissions = (Set) entry.getValue();
            for (Iterator iterator1 = permissions.iterator(); iterator1.hasNext();) {
                Permission permission = (Permission) iterator1.next();
                p.add(permission);
            }
        }
        for (Iterator iterator = p.iterator(); iterator.hasNext();) {
            Permission permission = (Permission) iterator.next();
            checked.add(permission);
        }

    }

    public void registerServletHolder(ServletHolder servletHolder, String servletName, Set servletMappings, Map webRoleRefPermissions) throws Exception {
        super.registerServletHolder(servletHolder, servletName, servletMappings, webRoleRefPermissions);

        policyConfiguration = factory.getPolicyConfiguration(policyContextID, false);
        for (Iterator iterator = webRoleRefPermissions.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String roleName = (String) entry.getValue();
            WebRoleRefPermission webRoleRefPermission = (WebRoleRefPermission) entry.getKey();
            policyConfiguration.addToRole(roleName, webRoleRefPermission);
        }
        policyConfiguration.commit();
    }

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
            acc.checkPermission(new WebUserDataPermission(servletHttpRequest));

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
    public Principal obtainUser(String pathInContext, HttpRequest request, HttpResponse response) throws HttpException, IOException {
        ServletHttpRequest servletHttpRequest = (ServletHttpRequest) request.getWrapper();
        WebResourcePermission resourcePermission = new WebResourcePermission(servletHttpRequest);
        WebUserDataPermission dataPermission = new WebUserDataPermission(servletHttpRequest);
        boolean unauthenticated = !(checked.implies(resourcePermission) || checked.implies(dataPermission));
        boolean forbidden = excludedPermissions.implies(resourcePermission) || excludedPermissions.implies(dataPermission);

        UserRealm realm = getRealm();
        Authenticator authenticator = getAuthenticator();
        Principal user = null;
        if (!unauthenticated && !forbidden) {
            if (realm == null) {
                log.warn("Realm Not Configured");
                throw new HttpException(HttpResponse.__500_Internal_Server_Error, "Realm Not Configured");
            }


            // Handle pre-authenticated request
            if (authenticator != null) {
                // User authenticator.
                user = authenticator.authenticate(realm, pathInContext, request, response);
            } else {
                // don't know how authenticate
                log.warn("Mis-configured Authenticator for " + request.getPath());
                throw new HttpException(HttpResponse.__500_Internal_Server_Error, "Mis-configured Authenticator for " + request.getPath());
            }

            return user;
        } else if (authenticator instanceof FormAuthenticator && pathInContext.endsWith(FormAuthenticator.__J_SECURITY_CHECK)) {
            /**
             * This could be a post request to __J_SECURITY_CHECK.
             */
            if (realm == null) {
                log.warn("Realm Not Configured");
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
     * @param securityConfig  The Geronimo security configuration.
     * @param loginDomainName
     * @return the default principal
     */
    protected JAASJettyPrincipal generateDefaultPrincipal(Security securityConfig, String loginDomainName) throws GeronimoSecurityException {

        DefaultPrincipal defaultPrincipal = securityConfig.getDefaultPrincipal();
        if (defaultPrincipal == null) {
            AutoMapAssistant config = securityConfig.getAssistant();
            try {
                if (config != null) {
                    Set assistants = kernel.listGBeans(new ObjectName("geronimo.security:type=SecurityRealm,realm=" + config.getSecurityRealm()));
                    if (assistants.size() < 1 || assistants.size() > 1) throw new GeronimoSecurityException("Only one auto mapping assistant should match " + config.getSecurityRealm());

                    org.apache.geronimo.security.realm.AutoMapAssistant assistant = (org.apache.geronimo.security.realm.AutoMapAssistant) assistants.iterator().next();
                    org.apache.geronimo.security.deploy.Principal principal = assistant.obtainDefaultPrincipal();
                    defaultPrincipal = new DefaultPrincipal();
                    defaultPrincipal.setPrincipal(principal);
                    defaultPrincipal.setRealmName(((SecurityRealm) assistant).getRealmName());
                }
            } catch (MalformedObjectNameException e) {
                throw new GeronimoSecurityException("Bad object name geronimo.security:type=SecurityRealm,realm=" + config.getSecurityRealm());
            }

        }
        if (defaultPrincipal == null) throw new GeronimoSecurityException("Unable to generate default principal");

        return generateDefaultPrincipal(securityConfig, defaultPrincipal, loginDomainName);
    }

    protected JAASJettyPrincipal generateDefaultPrincipal(Security securityConfig, DefaultPrincipal defaultPrincipal, String loginDomainName) throws GeronimoSecurityException {
        JAASJettyPrincipal result = new JAASJettyPrincipal("default");
        Subject defaultSubject = new Subject();

        RealmPrincipal realmPrincipal = ConfigurationUtil.generateRealmPrincipal(defaultPrincipal.getPrincipal(), loginDomainName, defaultPrincipal.getRealmName());
        if (realmPrincipal == null) {
            throw new GeronimoSecurityException("Unable to create realm principal");
        }
        PrimaryRealmPrincipal primaryRealmPrincipal = ConfigurationUtil.generatePrimaryRealmPrincipal(defaultPrincipal.getPrincipal(), loginDomainName, defaultPrincipal.getRealmName());
        if (primaryRealmPrincipal == null) {
            throw new GeronimoSecurityException("Unable to create primary realm principal");
        }

        defaultSubject.getPrincipals().add(realmPrincipal);
        defaultSubject.getPrincipals().add(primaryRealmPrincipal);

        result.setSubject(defaultSubject);

        return result;
    }

    public void doStart() throws WaitingException, Exception {
        super.doStart();

        Authenticator authenticator = getAuthenticator();
        if (authenticator instanceof FormAuthenticator) {
            formLoginPath = ((FormAuthenticator) authenticator).getLoginPage();
            if (formLoginPath.indexOf('?') > 0) {
                formLoginPath = formLoginPath.substring(0, formLoginPath.indexOf('?'));
            }
        }

        /**
         * Register our default principal with the ContextManager
         */
        Subject defaultSubject = defaultPrincipal.getSubject();
        ContextManager.registerSubject(defaultSubject);
        SubjectId id = ContextManager.getSubjectId(defaultSubject);
        defaultSubject.getPrincipals().add(new IdentificationPrincipal(id));

        log.debug("Default subject " + id + " for JACC policy '" + policyContextID + "' registered.");

        /**
         * Get the JACC policy configuration that's associated with this
         * web application and configure it with the geronimo security
         * configuration.  The work for this is done by the class
         * JettyXMLConfiguration.
         */
        try {
            factory = PolicyConfigurationFactory.getPolicyConfigurationFactory();

            policyConfiguration = factory.getPolicyConfiguration(policyContextID, true);
            configure();
//            configure(policyConfiguration);
            securityInterceptor.addRoleMappings(securityRoles, loginDomainName, securityConfig, (RoleMappingConfiguration) policyConfiguration);
            policyConfiguration.commit();
        } catch (ClassNotFoundException e) {
            // do nothing
        } catch (PolicyContextException e) {
            // do nothing
        } catch (GeronimoSecurityException e) {
            // do nothing
        }


        log.info("JettyWebAppJACCContext started with JACC policy '" + policyContextID + "'");
    }

    public void doStop() throws WaitingException, Exception {
        super.doStop();

        /**
         * Unregister the default principal and role designates
         */
        log.debug("Default subject " + ContextManager.getSubjectId(defaultPrincipal.getSubject()) + " for JACC policy " + policyContextID + "' unregistered.");

        ContextManager.unregisterSubject(defaultPrincipal.getSubject());

        securityInterceptor.stop();

        /**
         * Delete the policy configuration for this web application
         */
        if (policyConfiguration != null) {
            policyConfiguration.delete();
        }

        log.info("JettyWebAppJACCContext with JACC policy '" + policyContextID + "' stopped");
    }

    public void doFail() {
        super.doFail();

        try {
            if (policyConfiguration != null) {
                policyConfiguration.delete();
            }
        } catch (PolicyContextException e) {
            // do nothing
        }

        log.info("JettyWebAppJACCContext failed");
    }


//from jettyxmlconfig

    private void configure() throws GeronimoSecurityException {
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


    //===============================================================================
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder("Jetty JACC WebApplication Context", JettyWebAppJACCContext.class, JettyWebAppContext.GBEAN_INFO);

        infoBuilder.addAttribute("policyContextID", String.class, true);
        infoBuilder.addAttribute("loginDomainName", String.class, true);
        infoBuilder.addAttribute("securityConfig", Security.class, true);

        infoBuilder.addAttribute("securityRoles", Set.class, true);
        infoBuilder.addAttribute("uncheckedPermissions", PermissionCollection.class, true);
        infoBuilder.addAttribute("excludedPermissions", PermissionCollection.class, true);
        infoBuilder.addAttribute("rolePermissions", Map.class, true);

        infoBuilder.addAttribute("kernel", Kernel.class, false);

        infoBuilder.setConstructor(new String[]{
            "uri",
            "componentContext",
            "userTransaction",
            "classLoader",
            "webClassPath",
            "contextPriorityClassLoader",
            "configurationBaseUrl",
            "unshareableResources",
            "applicationManagedSecurityResources",

            "displayName",
            "contextParamMap",
            "listenerClassNames",
            "distributable",
            "mimeMap",
            "welcomeFiles",
            "localeEncodingMapping",
            "errorPages",
            "authenticator",
            "realmName",
            "tagLibMap",
            "sessionTimeoutSeconds",

            "policyContextID",
            "loginDomainName",
            "securityConfig",

            "securityRoles",
            "uncheckedPermissions",
            "excludedPermissions",
            "rolePermissions",

            "TransactionContextManager",
            "TrackedConnectionAssociator",
            "JettyContainer",
            "kernel",
        });

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
