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
package org.apache.geronimo.tomcat.realm;

import java.io.IOException;
import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.Principal;
import java.security.cert.X509Certificate;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebRoleRefPermission;
import javax.security.jacc.WebUserDataPermission;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.realm.JAASRealm;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.jaas.ConfigurationFactory;
import org.apache.geronimo.security.realm.providers.CertificateChainCallbackHandler;
import org.apache.geronimo.security.realm.providers.PasswordCallbackHandler;
import org.apache.geronimo.tomcat.JAASTomcatPrincipal;
import org.apache.geronimo.tomcat.interceptor.PolicyContextBeforeAfter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class adapts the tomcat framework to use JACC security.
 */
public class TomcatGeronimoRealm extends JAASRealm {

    private static final Logger log = LoggerFactory.getLogger(TomcatGeronimoRealm.class);

    private static ThreadLocal<String> currentRequestWrapperName = new ThreadLocal<String>();

    /**
     * Descriptive information about this <code>Realm</code> implementation.
     */
    protected static final String info = "org.apache.geronimo.tomcat.TomcatGeronimoRealm/1.1";

    /**
     * Descriptive information about this <code>Realm</code> implementation.
     */
    protected static final String name = "TomcatGeronimoRealm";

    private final ConfigurationFactory configurationFactory;

    public TomcatGeronimoRealm(ConfigurationFactory configurationFactory) {
        this.configurationFactory = configurationFactory;
    }

    public static String setRequestWrapperName(String requestWrapperName) {
        String old = currentRequestWrapperName.get();
        currentRequestWrapperName.set(requestWrapperName);
        return old;
    }

    /**
     * Enforce any user data constraint required by the security constraint
     * guarding this request URI.  Return <code>true</code> if this constraint
     * was not violated and processing should continue, or <code>false</code>
     * if we have created a response already.
     *
     * @param request     Request we are processing
     * @param response    Response we are creating
     * @param constraints Security constraint being checked
     * @throws IOException if an input/output error occurs
     */
    public boolean hasUserDataPermission(Request request,
                                         Response response,
                                         SecurityConstraint[] constraints)
            throws IOException {

        setSubject(request);
        try {

            AccessControlContext acc = ContextManager.getCurrentContext();

            /**
             * JACC v1.0 secion 4.1.1
             */
            WebUserDataPermission wudp = new WebUserDataPermission(request);
            acc.checkPermission(wudp);

        } catch (AccessControlException ace) {
            response.sendError(Response.SC_FORBIDDEN);
            return false;
        }

        return true;
    }

    /**
     * Perform access control based on the specified authorization constraint.
     * Return <code>true</code> if this constraint is satisfied and processing
     * should continue, or <code>false</code> otherwise.
     *
     * @param request     Request we are processing
     * @param response    Response we are creating
     * @param constraints Security constraints we are enforcing
     * @param context     The Context to which client of this class is attached.
     * @throws java.io.IOException if an input/output error occurs
     */
    public boolean hasResourcePermission(Request request,
                                         Response response,
                                         SecurityConstraint[] constraints,
                                         Context context)
            throws IOException {

        // Specifically allow access to the form login and form error pages
        // and the "j_security_check" action
        LoginConfig config = context.getLoginConfig();
        if ((config != null) &&
                (org.apache.catalina.realm.Constants.FORM_METHOD.equals(config.getAuthMethod()))) {
            String requestURI = request.getDecodedRequestURI();
            String loginPage = context.getPath() + config.getLoginPage();
            if (loginPage.equals(requestURI)) {
                if (log.isDebugEnabled())
                    log.debug(" Allow access to login page " + loginPage);
                return (true);
            }
            String errorPage = context.getPath() + config.getErrorPage();
            if (errorPage.equals(requestURI)) {
                if (log.isDebugEnabled())
                    log.debug(" Allow access to error page " + errorPage);
                return (true);
            }
            if (requestURI.endsWith(org.apache.catalina.realm.Constants.FORM_ACTION)) {
                if (log.isDebugEnabled())
                    log.debug(" Allow access to username/password submission");
                return (true);
            }
        }

        setSubject(request);

        //Set the current wrapper name (Servlet mapping)
        currentRequestWrapperName.set(request.getWrapper().getName());

        try {

            AccessControlContext acc = ContextManager.getCurrentContext();

            /**
             * JACC v1.0 section 4.1.2
             */
            acc.checkPermission(new WebResourcePermission(request));

        } catch (AccessControlException ace) {
            response.sendError(Response.SC_FORBIDDEN);
            return false;
        }

        return true;

    }

    private void setSubject(Request request) {
        Principal principal = request.getUserPrincipal();
        //If we have no principal, then we should use the default.
        if (principal == null) {
            Subject defaultSubject = (Subject) request.getAttribute(PolicyContextBeforeAfter.DEFAULT_SUBJECT);
            ContextManager.setCallers(defaultSubject, defaultSubject);
        } else {
            Subject subject = ((JAASTomcatPrincipal) principal).getSubject();
            ContextManager.setCallers(subject, subject);
        }
    }

    /**
     * Return <code>true</code> if the specified Principal has the specified
     * security role, within the context of this Realm; otherwise return
     * <code>false</code>.
     *
     * @param principal Principal for whom the role is to be checked
     * @param role      Security role to be checked
     */
    public boolean hasRole(Principal principal, String role) {

        String name = currentRequestWrapperName.get();

        /**
         * JACC v1.0 secion B.19
         */
        if (name == null || name.equals("jsp")) {
            name = "";
        }

        AccessControlContext acc = ContextManager.getCurrentContext();

        try {
            /**
             * JACC v1.0 section 4.1.3
             */
            acc.checkPermission(new WebRoleRefPermission(name, role));
        } catch (AccessControlException e) {
            return false;
        }

        return true;
    }

    /**
     * Return the <code>Principal</code> associated with the specified
     * username and credentials, if there is one; otherwise return
     * <code>null</code>.
     * <p/>
     * If there are any errors with the JDBC connection, executing the query or
     * anything we return null (don't authenticate). This event is also logged,
     * and the connection will be closed so that a subsequent request will
     * automatically re-open it.
     *
     * @param username    Username of the <code>Principal</code> to look up
     * @param credentials Password or other credentials to use in authenticating this
     *                    username
     */
    public Principal authenticate(String username, String credentials) {

        char[] cred = credentials == null ? null : credentials.toCharArray();
        CallbackHandler callbackHandler = new PasswordCallbackHandler(username, cred);
        return authenticate(callbackHandler, username);
    }

    public Principal authenticate(X509Certificate[] certs) {
        if (certs == null || certs.length == 0) {
            return null;
        }
        CallbackHandler callbackHandler = new CertificateChainCallbackHandler(certs);
        String principalName = certs[0].getSubjectX500Principal().getName();
        return authenticate(callbackHandler, principalName);
    }

    public Principal authenticate(CallbackHandler callbackHandler, String principalName) {

        // Establish a LoginContext to use for authentication
        try {

            if ((principalName != null) && (!principalName.equals(""))) {
                LoginContext loginContext = null;
                if (log.isDebugEnabled())
                    log.debug(sm.getString("jaasRealm.beginLogin", principalName, configurationFactory.getConfigurationName()));

                // What if the LoginModule is in the container class loader ?
                ClassLoader ocl = null;

                if (isUseContextClassLoader()) {
                    ocl = Thread.currentThread().getContextClassLoader();
                    Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
                }

                try {
                    loginContext = ContextManager.login(configurationFactory.getConfigurationName(), callbackHandler, configurationFactory.getConfiguration());
                } catch (AccountExpiredException e) {
                    if (log.isDebugEnabled())
                        log.debug(sm.getString("jaasRealm.accountExpired", principalName));
                    return (null);
                } catch (CredentialExpiredException e) {
                    if (log.isDebugEnabled())
                        log.debug(sm.getString("jaasRealm.credentialExpired", principalName));
                    return (null);
                } catch (FailedLoginException e) {
                    if (log.isDebugEnabled())
                        log.debug(sm.getString("jaasRealm.failedLogin", principalName));
                    return (null);
                } catch (LoginException e) {
                    log.warn(sm.getString("jaasRealm.loginException", principalName), e);
                    return (null);
                } catch (Throwable e) {
                    log.error(sm.getString("jaasRealm.unexpectedError"), e);
                    return (null);
                } finally {
                    if (isUseContextClassLoader()) {
                        Thread.currentThread().setContextClassLoader(ocl);
                    }
                }

                if (log.isDebugEnabled())
                    log.debug("Login context created " + principalName);

                // Negotiate a login via this LoginContext
                Subject subject = loginContext.getSubject();
                //very iffy -- see if needed for basic auth
                ContextManager.setCallers(subject, subject);

                if (log.isDebugEnabled())
                    log.debug(sm.getString("jaasRealm.loginContextCreated", principalName));

                // Return the appropriate Principal for this authenticated Subject
                JAASTomcatPrincipal jaasPrincipal = new JAASTomcatPrincipal(principalName);
                jaasPrincipal.setSubject(subject);

                return (jaasPrincipal);
            } else {
                if (log.isDebugEnabled())
                    log.debug("Login Failed - null userID");
                return null;
            }

        } catch (Throwable t) {
            log.error("error ", t);
            return null;
        }
    }

    /**
     * Prepare for active use of the public methods of this <code>Component</code>.
     *
     * @throws org.apache.catalina.LifecycleException
     *          if this component detects a fatal error
     *          that prevents it from being started
     */
    public void start() throws LifecycleException {

        // Perform normal superclass initialization
        super.start();
        setUseContextClassLoader(false);
    }

    /**
     * Gracefully shut down active use of the public methods of this <code>Component</code>.
     *
     * @throws LifecycleException if this component detects a fatal error
     *                            that needs to be reported
     */
    public void stop() throws LifecycleException {

        // Perform normal superclass finalization
        super.stop();

    }
}
