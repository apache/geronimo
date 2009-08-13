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
import java.security.Principal;

import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebUserDataPermission;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.geronimo.common.GeronimoSecurityException;
import org.apache.geronimo.jetty6.JAASJettyPrincipal;
import org.apache.geronimo.jetty6.JAASJettyRealm;
import org.apache.geronimo.jetty6.JettyContainer;
import org.apache.geronimo.security.Callers;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.jacc.PolicyContextHandlerHttpServletRequest;
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

    private JAASJettyRealm realm;

    public JettySecurityHandler(Authenticator authenticator,
                                JAASJettyRealm userRealm,
                                String policyContextID,
                                Subject defaultSubject) {
        setAuthenticator(authenticator);
        this.policyContextID = policyContextID;

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
        if (defaultSubject == null) {
            defaultSubject = ContextManager.EMPTY;
        }
        this.defaultPrincipal = generateDefaultPrincipal(defaultSubject);

        setUserRealm(userRealm);
        this.realm = userRealm;
        assert realm != null;
    }

    public boolean hasConstraints() {
        return true;
    }

    public void doStop(JettyContainer jettyContainer) throws Exception {
        super.doStop();
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
        PolicyContext.setContextID(policyContextID);
        HttpServletRequest oldRequest = PolicyContextHandlerHttpServletRequest.pushContextData(request);

        try {

            super.handle(target, request, response, dispatch);
        } finally {
            PolicyContext.setContextID(old_policy_id);
            ContextManager.popCallers(oldCallers);
            PolicyContextHandlerHttpServletRequest.popContextData(oldRequest);
        }
    }


    /**
     * Check the security constraints using JACC.
     *
     * @param pathInContext path in context
     * @param request       HTTP request
     * @param response      HTTP response
     * @return true if the path in context passes the security check, false if
     *         it fails or a redirection has occured during authentication.
     */
    public boolean checkSecurityConstraints(String pathInContext, Request request, Response response) throws IOException {
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

            Authenticator authenticator = getAuthenticator();
            boolean isAuthenticated = false;

            if (authenticator instanceof FormAuthenticator
                    && pathInContext.endsWith(FormAuthenticator.__J_SECURITY_CHECK)) {
                /**
                 * This is a post request to __J_SECURITY_CHECK. Stop now after authentication.
                 * Whether or not authentication succeeded, we return.
                 */
                authenticator.authenticate(realm, pathInContext, request, response);
                return false;
            }
            // attempt to access an unprotected resource that is not the
            // j_security_check.
            // if we are logged in, return the logged in principal.
            if (request != null) {
                // null response appears to prevent redirect to login page
                Principal user = authenticator.authenticate(realm, pathInContext,
                        request, null);
                if (user == null || user == SecurityHandler.__NOBODY) {
                    //TODO use run-as as nextCaller if present
                    ContextManager.setCallers(defaultPrincipal.getSubject(), defaultPrincipal.getSubject());
                    request.setUserPrincipal(new NotChecked());
                } else if (user != null) {
                    isAuthenticated = true;
                }
            }


            AccessControlContext acc = ContextManager.getCurrentContext();

            /**
             * JACC v1.0 section 4.1.1
             */
            WebUserDataPermission wudp = new WebUserDataPermission(pathInContext, new String[]{request.getMethod()}, transportType);
            acc.checkPermission(wudp);

            WebResourcePermission webResourcePermission = new WebResourcePermission(request);
            /**
             * JACC v1.0 section 4.1.2
             */
            if (isAuthenticated) {
                //current user is logged in, this is the actual check
                acc.checkPermission(webResourcePermission);
            } else {
                //user is not logged in: if access denied, try to log them in.
                try {
                    acc.checkPermission(webResourcePermission);
                } catch (AccessControlException e) {
                    //not logged in: try to log them in.
                    Principal user = authenticator.authenticate(realm, pathInContext, request, response);
                    if (user == SecurityHandler.__NOBODY) {
                        return true;
                    }
                    if (user == null) {
                        throw e;
                    }
                }
            }

        } catch (HttpException he) {
            response.sendError(he.getStatus(), he.getReason());
            return false;
        } catch (AccessControlException ace) {
            if (!response.isCommitted()) {
                response.sendError(403);
            }
            return false;
        }
        return true;
    }

    /**
     * Generate the default principal from the security config.
     *
     * @param defaultSubject The default subject.
     * @return the default principal
     * @throws org.apache.geronimo.common.GeronimoSecurityException
     *          if the default principal cannot be constructed
     */
    protected JAASJettyPrincipal generateDefaultPrincipal(Subject defaultSubject)
            throws GeronimoSecurityException {

        if (defaultSubject == null) {
            throw new GeronimoSecurityException(
                    "Unable to generate default principal");
        }

        JAASJettyPrincipal result = new JAASJettyPrincipal("default");

        result.setSubject(defaultSubject);

        return result;
    }

}
