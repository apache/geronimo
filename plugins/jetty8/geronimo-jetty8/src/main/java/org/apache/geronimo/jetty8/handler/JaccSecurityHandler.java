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
package org.apache.geronimo.jetty8.handler;

import java.io.IOException;
import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.util.Collections;
import java.util.Set;

import javax.security.jacc.PolicyContext;
import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebUserDataPermission;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletSecurityElement;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.geronimo.jetty8.JettyContainer;
import org.apache.geronimo.security.Callers;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.jacc.PolicyContextHandlerHttpServletRequest;
import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.UserIdentity;

public class JaccSecurityHandler extends SecurityHandler {

    private final String policyContextID;

    private final AccessControlContext defaultAcc;

    public JaccSecurityHandler(
            String policyContextID,
            Authenticator authenticator,
            final LoginService loginService,
            IdentityService identityService,
            AccessControlContext defaultAcc) {
        setAuthenticator(authenticator);
        this.policyContextID = policyContextID;
        this.defaultAcc = defaultAcc;

        loginService.setIdentityService(identityService);
        setLoginService(loginService);
        setIdentityService(identityService);
    }


    public void doStop(JettyContainer jettyContainer) throws Exception {
        super.doStop();
    }

    /* ------------------------------------------------------------ */
    /*
     * @see org.eclipse.jetty.security.SecurityHandler#handle(java.lang.String,
     *      javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse, int)
     */
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request,
                       HttpServletResponse response) throws IOException,
            ServletException {
        String old_policy_id = PolicyContext.getContextID();
        Callers oldCallers = ContextManager.getCallers();
        HttpServletRequest oldRequest = PolicyContextHandlerHttpServletRequest.pushContextData(request);
        try {
            PolicyContext.setContextID(policyContextID);


            super.handle(target, baseRequest, request, response);
        } finally {
            PolicyContext.setContextID(old_policy_id);
            ContextManager.popCallers(oldCallers);
            PolicyContextHandlerHttpServletRequest.popContextData(oldRequest);
        }
    }

    @Override
    protected Object prepareConstraintInfo(String pathInContext, Request request) {
        return null;
    }

    @Override
    protected boolean checkUserDataPermissions(String pathInContext, Request request, Response response, Object constraintInfo) throws IOException {
        boolean notIntegral = request.isSecure() || !request.getConnection().isIntegral(request);

        try {
            /**
             * JACC v1.0 section 4.1.1
             */
            WebUserDataPermission wudp;
            if (notIntegral) {
                wudp = new WebUserDataPermission(request);
            } else {
                wudp = new WebUserDataPermission(encodeColons(request), new String[]{request.getMethod()}, "INTEGRAL");
            }
            defaultAcc.checkPermission(wudp);
            return true;
        } catch (AccessControlException e) {
            //TODO redirect to secure port.
            return false;
        }
    }

    private static String encodeColons(HttpServletRequest request) {
        String result = request.getServletPath() + (request.getPathInfo() == null ? "" : request.getPathInfo());

        if (result.indexOf(":") > -1) result = result.replaceAll(":", "%3A");

        return result;
    }

    @Override
    protected boolean isAuthMandatory(Request base_request, Response base_response, Object constraintInfo) {
        return !checkWebResourcePermission(base_request, defaultAcc);
    }

    @Override
    protected boolean checkWebResourcePermissions(String pathInContext, Request request, Response response, Object constraintInfo, UserIdentity userIdentity) throws IOException {
        if (!(userIdentity instanceof GeronimoUserIdentity)){
            //we already checked against default_acc and got false
            return false;
        }
        AccessControlContext acc = ((GeronimoUserIdentity)userIdentity).getAccessControlContext();
        return checkWebResourcePermission(request, acc);
    }

    private boolean checkWebResourcePermission(Request request, AccessControlContext acc) {
        WebResourcePermission webResourcePermission = new WebResourcePermission(request);
        /**
         * JACC v1.0 section 4.1.2
         */
        //user is not logged in: if access denied, try to log them in.
        try {
            acc.checkPermission(webResourcePermission);
            return true;
        } catch (AccessControlException e) {
            return false;
        }
    }

}
