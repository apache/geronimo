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

import javax.security.auth.Subject;
import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebUserDataPermission;
import javax.transaction.TransactionManager;
import java.io.IOException;
import java.net.URI;
import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mortbay.http.Authenticator;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.PathMap;
import org.mortbay.http.SecurityConstraint;
import org.mortbay.http.UserRealm;
import org.mortbay.jetty.servlet.FormAuthenticator;
import org.mortbay.jetty.servlet.ServletHttpRequest;
import org.mortbay.util.LazyList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.kernel.config.ConfigurationParent;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.GeronimoSecurityException;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.PrimaryRealmPrincipal;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.SubjectId;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.security.deploy.Security;
import org.apache.geronimo.security.util.ConfigurationUtil;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;
import org.apache.geronimo.transaction.UserTransactionImpl;


/**
 * A class extension to <code>JettyWebAppContext</code> whose purpose is to
 * provide JACC security checks.
 *
 * @version $Revision: 1.3 $ $Date: 2004/07/12 06:07:51 $
 * @see org.mortbay.jetty.servlet.WebApplicationContext#checkSecurityConstraints(java.lang.String, org.mortbay.http.HttpRequest, org.mortbay.http.HttpResponse)
 */
public class JettyWebAppJACCContext extends JettyWebAppContext {

    private static Log log = LogFactory.getLog(JettyWebAppJACCContext.class);

    private final String policyContextID;
    private final Security securityConfig;

    private PolicyConfigurationFactory factory;
    private PolicyConfiguration policyConfiguration;

    private Map roleDesignates = new HashMap();

    private JAASJettyPrincipal defaultPrincipal;

    private PathMap _constraintMap = new PathMap();

    public JettyWebAppJACCContext() {
        this(null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public JettyWebAppJACCContext(ConfigurationParent config,
                                  URI uri,
                                  JettyContainer container,
                                  ReadOnlyContext compContext,
                                  String policyContextID,
                                  Security securityConfig,
                                  Set unshareableResources,
                                  Set applicationManagedSecurityResources,
                                  TransactionManager txManager,
                                  TrackedConnectionAssociator associator,
                                  UserTransactionImpl userTransaction,
                                  ClassLoader classLoader) {
        super(config, uri, container, compContext, unshareableResources, applicationManagedSecurityResources,
              txManager, associator, userTransaction, classLoader);

        this.policyContextID = policyContextID;
        this.securityConfig = securityConfig;

        setConfiguration(new JettyXMLConfiguration(this));

        defaultPrincipal = generateDefaultPrincipal(securityConfig);

        /**
         * We want to use our own web-app handler.
         */
        addHandler(new JettyWebAppHandler());
    }

    public String getPolicyContextID() {
        return policyContextID;
    }

    public Security getSecurityConfig() {
        return securityConfig;
    }

    public Subject getRoleDesignate(String roleName) {
        return (Subject) roleDesignates.get(roleName);
    }

    void setRoleDesignate(String roleName, Subject subject) {
        roleDesignates.put(roleName, subject);
    }

    /**
     * Handler request.
     * Call each HttpHandler until request is handled.
     *
     * @param pathInContext Path in context
     * @param pathParams    Path parameters such as encoded Session ID
     * @param httpRequest
     * @param httpResponse
     * @throws HttpException
     * @throws IOException
     */
    public void handle(String pathInContext,
                       String pathParams,
                       HttpRequest httpRequest,
                       HttpResponse httpResponse)
            throws HttpException, IOException {

        String savedPolicyContextID = PolicyContext.getContextID();
        JettyWebAppJACCContext savedContext = JettyServer.getCurrentWebAppContext();

        try {
            PolicyContext.setContextID(policyContextID);
            JettyServer.setCurrentWebAppContext(this);

            super.handle(pathInContext, pathParams, httpRequest, httpResponse);
        } finally {
            JettyServer.setCurrentWebAppContext(savedContext);
            PolicyContext.setContextID(savedPolicyContextID);
        }
    }

    /**
     * Keep our own copy of security constraints.<p/>
     * <p/>
     * We keep our own copy of security constraints because Jetty's copy is
     * private.  We use these constraints not for any authorization descitions
     * but, to decide whether we should attempt to authenticate the request.
     *
     * @param pathSpec The path spec to which the secuiryt cosntraint applies
     * @param sc       the security constraint
     * @todo Jetty to provide access to this map so we can remove this method
     * @see org.mortbay.http.HttpContext#addSecurityConstraint(java.lang.String, org.mortbay.http.SecurityConstraint)
     */
    public void addSecurityConstraint(String pathSpec, SecurityConstraint sc) {
        super.addSecurityConstraint(pathSpec, sc);

        Object scs = _constraintMap.get(pathSpec);
        scs = LazyList.add(scs, sc);
        _constraintMap.put(pathSpec, scs);

        if (log.isDebugEnabled()) log.debug("added " + sc + " at " + pathSpec);
    }

    /**
     * Check the security constraints using JACC.
     *
     * @param pathInContext path in context
     * @param request       HTTP request
     * @param response      HTTP response
     * @return <code>true</code> if the path in context passes the security
     *         check, <code>false</code> if it fails or a redirection has occured
     *         during authentication.
     * @throws HttpException
     * @throws IOException
     */
    public boolean checkSecurityConstraints(String pathInContext, HttpRequest request, HttpResponse response) throws HttpException, IOException {

        try {
            Principal user = obtainUser(pathInContext, request, response);

            if (user == null) return false;
            if (user == SecurityConstraint.__NOBODY) return true;

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
     * @throws HttpException
     * @throws IOException
     */
    public Principal obtainUser(String pathInContext, HttpRequest request, HttpResponse response) throws HttpException, IOException {

        List scss = _constraintMap.getMatches(pathInContext);
        String pattern = null;
        boolean unauthenticated = false;
        boolean forbidden = false;

        if (scss != null && scss.size() > 0) {

            // for each path match
            // Add only constraints that have the correct method
            // break if the matching pattern changes.  This allows only
            // constraints with matching pattern and method to be combined.
            loop:
            for (int m = 0; m < scss.size(); m++) {
                Map.Entry entry = (Map.Entry) scss.get(m);
                Object scs = entry.getValue();
                String p = (String) entry.getKey();
                for (int c = 0; c < LazyList.size(scs); c++) {
                    SecurityConstraint sc = (SecurityConstraint) LazyList.get(scs, c);
                    if (!sc.forMethod(request.getMethod())) continue;

                    if (pattern != null && !pattern.equals(p)) break loop;
                    pattern = p;

                    // Check the method applies
                    if (!sc.forMethod(request.getMethod())) continue;

                    // Combine auth constraints.
                    if (sc.getAuthenticate()) {
                        if (!sc.isAnyRole()) {
                            List scr = sc.getRoles();
                            if (scr == null || scr.size() == 0) {
                                forbidden = true;
                                break loop;
                            }
                        }
                    } else {
                        unauthenticated = true;
                    }
                }
            }
        } else {
            unauthenticated = true;
        }

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
     * @param securityConfig The Geronimo security configuration.
     * @return the default principal
     * @throws GeronimoSecurityException
     */
    protected JAASJettyPrincipal generateDefaultPrincipal(Security securityConfig) throws GeronimoSecurityException {
        JAASJettyPrincipal result = new JAASJettyPrincipal("default");
        Subject defaultSubject = new Subject();

        DefaultPrincipal principal = securityConfig.getDefaultPrincipal();

        RealmPrincipal realmPrincipal = ConfigurationUtil.generateRealmPrincipal(principal.getPrincipal(), principal.getRealmName());
        if (realmPrincipal == null) throw new GeronimoSecurityException("Unable to create realm principal");
        PrimaryRealmPrincipal primaryRealmPrincipal = ConfigurationUtil.generatePrimaryRealmPrincipal(principal.getPrincipal(), principal.getRealmName());
        if (primaryRealmPrincipal == null) throw new GeronimoSecurityException("Unable to create primary realm principal");

        defaultSubject.getPrincipals().add(realmPrincipal);
        defaultSubject.getPrincipals().add(primaryRealmPrincipal);

        result.setSubject(defaultSubject);

        return result;
    }

    public void doStart() throws WaitingException, Exception {

        super.doStart();

        /**
         * Register our default principal with the ContextManager
         */
        Subject defaultSubject = defaultPrincipal.getSubject();
        ContextManager.registerSubject(defaultSubject);
        SubjectId id = ContextManager.getSubjectId(defaultSubject);
        defaultSubject.getPrincipals().add(new IdentificationPrincipal(id));

        log.debug("Default subject " + id + " for JACC policy '" + ((JettyWebAppJACCContext) getHttpContext()).getPolicyContextID() + "' registered.");

        /**
         * Get the JACC policy configuration that's associated with this
         * web application and configure it with the geronimo security
         * configuration.  The work for this is done by the class
         * JettyXMLConfiguration.
         */
        try {
            factory = PolicyConfigurationFactory.getPolicyConfigurationFactory();

            policyConfiguration = factory.getPolicyConfiguration(policyContextID, true);
            ((JettyXMLConfiguration) this.getConfiguration()).configure(policyConfiguration, securityConfig);
            policyConfiguration.commit();
        } catch (ClassNotFoundException e) {
            // do nothing
        } catch (PolicyContextException e) {
            // do nothing
        } catch (GeronimoSecurityException e) {
            // do nothing
        }

        /**
         * Register the role designates with the context manager.
         *
         * THIS MUST BE RUN AFTER JettyXMLConfiguration.configure()
         */
        Iterator iter = roleDesignates.keySet().iterator();
        while (iter.hasNext()) {
            String roleName = (String) iter.next();
            Subject roleDesignate = (Subject) roleDesignates.get(roleName);

            ContextManager.registerSubject(roleDesignate);
            id = ContextManager.getSubjectId(roleDesignate);
            roleDesignate.getPrincipals().add(new IdentificationPrincipal(id));

            log.debug("Role designate " + id + " for role '" + roleName + "' for JACC policy '" + ((JettyWebAppJACCContext) getHttpContext()).getPolicyContextID() + "' registered.");
        }

        log.info("JettyWebAppJACCContext started with JACC policy '" + policyContextID + "'");
    }

    public void doStop() throws WaitingException, Exception {

        super.doStop();

        /**
         * Unregister the default principal and role designates
         */
        log.debug("Default subject " + ContextManager.getSubjectId(defaultPrincipal.getSubject()) + " for JACC policy " + ((JettyWebAppJACCContext) getHttpContext()).getPolicyContextID() + "' unregistered.");

        ContextManager.unregisterSubject(defaultPrincipal.getSubject());

        Iterator iter = roleDesignates.keySet().iterator();
        while (iter.hasNext()) {
            String roleName = (String) iter.next();
            Subject roleDesignate = (Subject) roleDesignates.get(roleName);

            ContextManager.unregisterSubject(roleDesignate);
            log.debug("Role designate " + ContextManager.getSubjectId(roleDesignate) + " for role '" + roleName + "' for JACC policy '" + ((JettyWebAppJACCContext) getHttpContext()).getPolicyContextID() + "' unregistered.");
        }

        /**
         * Delete the policy configuration for this web application
         */
        if (policyConfiguration != null) policyConfiguration.delete();

        log.info("JettyWebAppJACCContext with JACC policy '" + policyContextID + "' stopped");
    }

    public void doFail() {

        super.doFail();

        try {
            if (policyConfiguration != null) policyConfiguration.delete();
        } catch (PolicyContextException e) {
            // do nothing
        }

        log.info("JettyWebAppJACCContext failed");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory("Jetty JACC WebApplication Context", JettyWebAppJACCContext.class, JettyWebAppContext.GBEAN_INFO);

        infoFactory.addAttribute("securityConfig", Security.class, true);
        infoFactory.addAttribute("policyContextID", String.class, true);

        infoFactory.setConstructor(new String[]{
            "Configuration",
            "URI",
            "JettyContainer",
            "componentContext",
            "policyContextID",
            "securityConfig",
            "unshareableResources",
            "applicationManagedSecurityResources",
            "TransactionManager",
            "TrackedConnectionAssociator",
            "userTransaction",
            "classLoader"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
