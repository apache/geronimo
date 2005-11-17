/**
 *
 * Copyright 2003-2005 The Apache Software Foundation
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
import java.security.PermissionCollection;
import java.security.Principal;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebUserDataPermission;
import javax.servlet.http.HttpServletRequest;

import org.mortbay.http.Authenticator;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.SecurityConstraint;
import org.mortbay.http.UserRealm;
import org.mortbay.jetty.servlet.FormAuthenticator;
import org.mortbay.jetty.servlet.ServletHttpRequest;

import org.apache.geronimo.common.GeronimoSecurityException;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.jetty.JAASJettyPrincipal;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.SubjectId;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.security.util.ConfigurationUtil;


/**
 * @version $Rev$ $Date$
 */
public class SecurityContextBeforeAfter implements BeforeAfter {

    private final BeforeAfter next;
    private final int policyContextIDIndex;
    private final int webAppContextIndex;
    private final String policyContextID;
    private final static ThreadLocal currentWebAppContext = new ThreadLocal();
    private final Map roleDesignates;
    private final JAASJettyPrincipal defaultPrincipal;

    private final String formLoginPath;

    private final PermissionCollection checked;
    private final PermissionCollection excludedPermissions;
    private final Authenticator authenticator;

    private final UserRealm realm;

    public SecurityContextBeforeAfter(BeforeAfter next,
                                      int policyContextIDIndex,
                                      int webAppContextIndex,
                                      String policyContextID,
                                      DefaultPrincipal defaultPrincipal,
                                      Authenticator authenticator,
                                      PermissionCollection checkedPermissions,
                                      PermissionCollection excludedPermissions,
                                      Map roleDesignates,
                                      UserRealm realm, ClassLoader classLoader)
    {
        this.next = next;
        this.policyContextIDIndex = policyContextIDIndex;
        this.webAppContextIndex = webAppContextIndex;
        this.policyContextID = policyContextID;

        this.defaultPrincipal = generateDefaultPrincipal(defaultPrincipal, classLoader);
        this.roleDesignates = roleDesignates;
        this.checked = checkedPermissions;
        this.excludedPermissions = excludedPermissions;

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
        Subject defaultSubject = this.defaultPrincipal.getSubject();
        ContextManager.registerSubject(defaultSubject);
        SubjectId id = ContextManager.getSubjectId(defaultSubject);
        defaultSubject.getPrincipals().add(new IdentificationPrincipal(id));

//        log.debug("Default subject " + id + " for JACC policy '" + policyContextID + "' registered.");


        this.realm = realm;
//        log.info("JettyWebAppJACCContext started with JACC policy '" + policyContextID + "'");
    }

    public void stop() {
        Subject defaultSubject = this.defaultPrincipal.getSubject();
        ContextManager.unregisterSubject(defaultSubject);
    }

    public void before(Object[] context, HttpRequest httpRequest, HttpResponse httpResponse) {
        context[policyContextIDIndex] = PolicyContext.getContextID();
        context[webAppContextIndex] = getCurrentSecurityInterceptor();

        PolicyContext.setContextID(policyContextID);
        setCurrentSecurityInterceptor(this);

        if (httpRequest != null) {
            ServletHttpRequest request = (ServletHttpRequest) httpRequest.getWrapper();
            PolicyContext.setHandlerData((HttpServletRequest) request);
        }

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
                transportType = "NONE";
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
     * @param defaultPrincipal The Geronimo security configuration.
     * @param classLoader
     * @return the default principal
     */
    protected JAASJettyPrincipal generateDefaultPrincipal(DefaultPrincipal defaultPrincipal, ClassLoader classLoader) throws GeronimoSecurityException {

        if (defaultPrincipal == null) {
            throw new GeronimoSecurityException("Unable to generate default principal");
        }

        try {
            JAASJettyPrincipal result = new JAASJettyPrincipal("default");
            Subject defaultSubject = ConfigurationUtil.generateDefaultSubject(defaultPrincipal, classLoader);

            result.setSubject(defaultSubject);

            return result;
        } catch (DeploymentException de) {
            throw new GeronimoSecurityException("Unable to generate default principal", de);
        }
    }

}
