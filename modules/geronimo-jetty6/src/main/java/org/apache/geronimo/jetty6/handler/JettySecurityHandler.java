/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.jetty6.handler;

import java.io.IOException;
import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.PermissionCollection;
import java.security.Principal;

import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebUserDataPermission;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.common.GeronimoSecurityException;
import org.apache.geronimo.security.Callers;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.SubjectId;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.security.util.ConfigurationUtil;
import org.apache.geronimo.jetty6.JAASJettyPrincipal;
import org.apache.geronimo.jetty6.JAASJettyRealm;
import org.apache.geronimo.jetty6.JettyContainer;
import org.mortbay.jetty.HttpException;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;
import org.mortbay.jetty.security.Authenticator;
import org.mortbay.jetty.security.FormAuthenticator;
import org.mortbay.jetty.security.SecurityHandler;

public class JettySecurityHandler extends SecurityHandler {

    private String policyContextID;

    private JAASJettyPrincipal defaultPrincipal;

    private String formLoginPath;

    private PermissionCollection checked;

    private PermissionCollection excludedPermissions;


    private JAASJettyRealm realm;

    public JettySecurityHandler() {
    }

    public boolean hasConstraints() {
        return true;
    }

    public void init(String policyContextID,
            DefaultPrincipal defaultPrincipal, 
            PermissionCollection checkedPermissions,
            PermissionCollection excludedPermissions, 
            ClassLoader classLoader) {
        this.policyContextID = policyContextID;

        this.defaultPrincipal = generateDefaultPrincipal(defaultPrincipal,classLoader);
        this.checked = checkedPermissions;
        this.excludedPermissions = excludedPermissions;

        Authenticator authenticator = getAuthenticator();
        if (authenticator instanceof FormAuthenticator) {
            String formLoginPath = ((FormAuthenticator) authenticator).getLoginPage();
            if (formLoginPath.indexOf('?') > 0) {
                formLoginPath = formLoginPath.substring(0, formLoginPath.indexOf('?'));
            }
            this.formLoginPath = formLoginPath;
        } else {
            formLoginPath = null;
        }

        /**
         * Register our default principal with the ContextManager
         */
        Subject defaultSubject = this.defaultPrincipal.getSubject();
        ContextManager.registerSubject(defaultSubject);
        SubjectId id = ContextManager.getSubjectId(defaultSubject);
        defaultSubject.getPrincipals().add(new IdentificationPrincipal(id));
        this.realm = (JAASJettyRealm)getUserRealm();
        assert realm != null;
    }

    public void doStop(JettyContainer jettyContainer) throws Exception {
        try{
            super.doStop();
        }
        finally {
            Subject defaultSubject = this.defaultPrincipal.getSubject();
            ContextManager.unregisterSubject(defaultSubject);
            jettyContainer.removeRealm(realm.getSecurityRealmName());
        }
    }

    /* ------------------------------------------------------------ */
    /*
     * @see org.mortbay.jetty.security.SecurityHandler#handle(java.lang.String,
     *      javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse, int)
     */
    public void handle(String target, HttpServletRequest request,
            HttpServletResponse response, int dispatch) throws IOException,
            ServletException {
        String old_policy_id = PolicyContext.getContextID();
        Callers oldCallers = ContextManager.getCallers();

        try {
            PolicyContext.setContextID(policyContextID);
            PolicyContext.setHandlerData(request);

            super.handle(target, request, response, dispatch);
        } finally {
            PolicyContext.setContextID(old_policy_id);
            ContextManager.popCallers(oldCallers);
        }
    }

//    public static Subject getCurrentRoleDesignate(String role) {
//        return ((JettySecurityHandler) (WebAppContext.getCurrentWebAppContext()
//                .getSecurityHandler())).getRoleDesignate(role);
//    }
//
//    private Subject getRoleDesignate(String roleName) {
//        return (Subject) roleDesignates.get(roleName);
//    }

    /**
     * Check the security constraints using JACC.
     * 
     * @param pathInContext
     *            path in context
     * @param request
     *            HTTP request
     * @param response
     *            HTTP response
     * @return true if the path in context passes the security check, false if
     *         it fails or a redirection has occured during authentication.
     */
    public boolean checkSecurityConstraints(String pathInContext,
            Request request, Response response) throws HttpException,
            IOException {
        if (formLoginPath != null) {
            String pathToBeTested = (pathInContext.indexOf('?') > 0 ? pathInContext
                    .substring(0, pathInContext.indexOf('?'))
                    : pathInContext);

            if (pathToBeTested.equals(formLoginPath)) {
                return true;
            }
        }

        try {
            String transportType;
            if (request.isSecure()) {
                transportType = "CONFIDENTIAL";
            } else if (request.getConnection().isIntegral(request)) {
                transportType = "INTEGRAL";
            } else {
                transportType = "NONE";
            }
            WebUserDataPermission wudp = new WebUserDataPermission(pathInContext, new String[] { request.getMethod() }, transportType);
            WebResourcePermission webResourcePermission = new WebResourcePermission(request);
            Principal user = obtainUser(pathInContext, request, response, webResourcePermission, wudp);

            if (user == null) {
                return false;
            }
            if (user == SecurityHandler.__NOBODY) {
                return true;
            }

            AccessControlContext acc = ContextManager.getCurrentContext();

            /**
             * JACC v1.0 secion 4.1.1
             */

            acc.checkPermission(wudp);

            /**
             * JACC v1.0 secion 4.1.2
             */
            acc.checkPermission(webResourcePermission);
        } catch (HttpException he) {
            response.sendError(he.getStatus(), he.getReason());
            return false;
        } catch (AccessControlException ace) {
            response.sendError(403);
            return false;
        }
        return true;
    }

    /**
     * Obtain an authenticated user, if one is required. Otherwise return the
     * default principal. <p/> Also set the current caller for JACC security
     * checks for the default principal. This is automatically done by
     * <code>JAASJettyRealm</code>.
     * 
     * @param pathInContext
     *            path in context
     * @param request
     *            HTTP request
     * @param response
     *            HTTP response
     * @return <code>null</code> if there is no authenticated user at the
     *         moment and security checking should not proceed and servlet
     *         handling should also not proceed, e.g. redirect.
     *         <code>SecurityConstraint.__NOBODY</code> if security checking
     *         should not proceed and servlet handling should proceed, e.g.
     *         login page.
     */
    private Principal obtainUser(String pathInContext, Request request,
            Response response, WebResourcePermission resourcePermission,
            WebUserDataPermission dataPermission) throws IOException {
        boolean unauthenticated = !(checked.implies(resourcePermission) || checked.implies(dataPermission));
        boolean forbidden = excludedPermissions.implies(resourcePermission) || excludedPermissions.implies(dataPermission);

        Authenticator authenticator = getAuthenticator();
        if (!unauthenticated && !forbidden) {
            return authenticator.authenticate(realm, pathInContext, request,
                    response);
        } else if (authenticator instanceof FormAuthenticator
                && pathInContext.endsWith(FormAuthenticator.__J_SECURITY_CHECK)) {
            /**
             * This could be a post request to __J_SECURITY_CHECK.
             */
            return authenticator.authenticate(realm, pathInContext, request,
                    response);
        }

        // attempt to access an unprotected resource that is not the
        // j_security_check.
        // if we are logged in, return the logged in principal.
        if (request != null) {
            // null response appears to prevent redirect to login page
            Principal user = authenticator.authenticate(realm, pathInContext,
                    request, null);
            if (user != null) {
                return user;
            }
        }

        /**
         * No authentication is required. Return the defaultPrincipal.
         */
        //TODO use run-as as nextCaller if present
        ContextManager.setCallers(defaultPrincipal.getSubject(), defaultPrincipal.getSubject());
        return defaultPrincipal;
    }

    /**
     * Generate the default principal from the security config.
     * 
     * @param defaultPrincipal
     *            The Geronimo security configuration.
     * @param classLoader
     * @return the default principal
     */
    protected JAASJettyPrincipal generateDefaultPrincipal(
            DefaultPrincipal defaultPrincipal, ClassLoader classLoader)
            throws GeronimoSecurityException {

        if (defaultPrincipal == null) {
            throw new GeronimoSecurityException(
                    "Unable to generate default principal");
        }

        try {
            JAASJettyPrincipal result = new JAASJettyPrincipal("default");
            Subject defaultSubject = ConfigurationUtil.generateDefaultSubject(
                    defaultPrincipal, classLoader);

            result.setSubject(defaultSubject);

            return result;
        } catch (DeploymentException de) {
            throw new GeronimoSecurityException(
                    "Unable to generate default principal", de);
        }
    }

}
