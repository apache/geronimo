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


package org.apache.geronimo.tomcat;

import java.security.AccessControlContext;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.auth.message.config.AuthConfigProvider;
import javax.security.auth.message.config.RegistrationListener;
import javax.security.auth.message.config.ServerAuthConfig;
import javax.servlet.ServletContext;
import javax.servlet.SessionCookieConfig;

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.deploy.ErrorPage;
import org.apache.catalina.deploy.WebXml;
import org.apache.catalina.startup.ContextConfig;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.jaas.ConfigurationFactory;
import org.apache.geronimo.tomcat.security.Authenticator;
import org.apache.geronimo.tomcat.security.Authorizer;
import org.apache.geronimo.tomcat.security.IdentityService;
import org.apache.geronimo.tomcat.security.LoginService;
import org.apache.geronimo.tomcat.security.SecurityValve;
import org.apache.geronimo.tomcat.security.UserIdentity;
import org.apache.geronimo.tomcat.security.authentication.BasicAuthenticator;
import org.apache.geronimo.tomcat.security.authentication.ClientCertAuthenticator;
import org.apache.geronimo.tomcat.security.authentication.DigestAuthenticator;
import org.apache.geronimo.tomcat.security.authentication.FormAuthenticator;
import org.apache.geronimo.tomcat.security.authentication.GenericHeaderAuthenticator;
import org.apache.geronimo.tomcat.security.authentication.NoneAuthenticator;
import org.apache.geronimo.tomcat.security.authentication.SpnegoAuthenticator;
import org.apache.geronimo.tomcat.security.authentication.jaspic.JaspicAuthenticator;
import org.apache.geronimo.tomcat.security.authentication.jaspic.JaspicCallbackHandler;
import org.apache.geronimo.tomcat.security.impl.GeronimoIdentityService;
import org.apache.geronimo.tomcat.security.impl.GeronimoLoginService;
import org.apache.geronimo.tomcat.security.jacc.JACCAuthorizer;
import org.apache.geronimo.tomcat.security.jacc.JACCRealm;
import org.apache.geronimo.tomcat.security.jacc.JACCSecurityValve;
import org.apache.geronimo.web.assembler.Assembler;
import org.apache.geronimo.web.info.ErrorPageInfo;
import org.apache.geronimo.web.info.LoginConfigInfo;
import org.apache.geronimo.web.info.SessionConfigInfo;
import org.apache.geronimo.web.info.WebAppInfo;
import org.xml.sax.InputSource;

/**
 * @version $Rev$ $Date$
 */
public abstract class BaseGeronimoContextConfig extends ContextConfig {
    private static final String MESSAGE_LAYER = "HttpServlet";
    private static final String POLICY_CONTEXT_ID_KEY = "javax.security.jacc.PolicyContext";

    private static org.apache.juli.logging.Log log = org.apache.juli.logging.LogFactory.getLog(BaseGeronimoContextConfig.class);

    private final WebAppInfo webAppInfo;

    public BaseGeronimoContextConfig(WebAppInfo webAppInfo) {
        this.webAppInfo = webAppInfo;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void configureStart() {
        ServletContext servletContext = context.getServletContext();
        Assembler assembler = new Assembler();
        assembler.assemble(servletContext, webAppInfo);
        context.setDisplayName(webAppInfo.displayName);
        context.setDistributable(webAppInfo.distributable);
        for (ErrorPageInfo errorPageInfo: webAppInfo.errorPages) {
            ErrorPage errorPage = new ErrorPage();
            errorPage.setLocation(errorPageInfo.location);
            errorPage.setExceptionType(errorPageInfo.exceptionType);
            if (errorPageInfo.errorCode != null) {
                errorPage.setErrorCode(errorPageInfo.errorCode);
            }
            context.addErrorPage(errorPage);
        }

        for (Map.Entry<String, String> localeEncodingMapping: webAppInfo.localeEncodingMappings.entrySet()) {
            context.addLocaleEncodingMappingParameter(localeEncodingMapping.getKey(), localeEncodingMapping.getValue());
        }
        for (Map.Entry<String, String> mimeMapping: webAppInfo.mimeMappings.entrySet()) {
            context.addMimeMapping(mimeMapping.getKey(), mimeMapping.getValue());
        }
        for (String welcomeFile: webAppInfo.welcomeFiles) {
            context.addWelcomeFile(welcomeFile);
        }
        authenticatorConfig(webAppInfo.loginConfig);
        if (webAppInfo.sessionConfig != null) {
            SessionConfigInfo sessionConfig = webAppInfo.sessionConfig;
             if (sessionConfig.sessionTimeoutMinutes != null) {
                context.setSessionTimeout(sessionConfig.sessionTimeoutMinutes);
            }
            if (sessionConfig.sessionTrackingModes != null) {
                servletContext.setSessionTrackingModes(sessionConfig.sessionTrackingModes);
            }
            if (sessionConfig.sessionCookieConfig != null) {
                SessionCookieConfig sessionCookieConfig = servletContext.getSessionCookieConfig();
                if (sessionConfig.sessionCookieConfig.name != null) {
                    sessionCookieConfig.setName(sessionConfig.sessionCookieConfig.name);
                }
                if (sessionConfig.sessionCookieConfig.comment != null) {
                    sessionCookieConfig.setComment(sessionConfig.sessionCookieConfig.comment);
                }
                if (sessionConfig.sessionCookieConfig.domain != null) {
                    sessionCookieConfig.setDomain(sessionConfig.sessionCookieConfig.domain);
                }
                if (sessionConfig.sessionCookieConfig.httpOnly != null) {
                    sessionCookieConfig.setHttpOnly(sessionConfig.sessionCookieConfig.httpOnly);
                }
                if (sessionConfig.sessionCookieConfig.maxAge != null) {
                    sessionCookieConfig.setMaxAge(sessionConfig.sessionCookieConfig.maxAge);
                }
                if (sessionConfig.sessionCookieConfig.path != null) {
                    sessionCookieConfig.setPath(sessionConfig.sessionCookieConfig.path);
                }
                if (sessionConfig.sessionCookieConfig.secure != null) {
                    sessionCookieConfig.setSecure(sessionConfig.sessionCookieConfig.secure);
                }
            }
        }
        context.setConfigured(true);
    }

    protected abstract void authenticatorConfig(LoginConfigInfo loginConfigInfo);

    protected void configureSecurity(StandardContext geronimoContext, String policyContextId, ConfigurationFactory configurationFactory, Subject defaultSubject, String authMethod, String realmName, String loginPage, String errorPage) {
        if (defaultSubject == null) {
            defaultSubject = ContextManager.EMPTY;
        }
        IdentityService identityService = new GeronimoIdentityService(defaultSubject);
        UserIdentity unauthenticatedIdentity = identityService.newUserIdentity(defaultSubject, null, null);
        LoginService loginService = new GeronimoLoginService(configurationFactory, identityService);
        Authenticator authenticator;
        AuthConfigFactory authConfigFactory = AuthConfigFactory.getFactory();
        RegistrationListener listener = new RegistrationListener() {

            public void notify(String layer, String appContext) {
            }
        };
        //?? TODO is context.getPath() the context root?
        String appContext = "server " + geronimoContext.getPath();
        AuthConfigProvider authConfigProvider = authConfigFactory.getConfigProvider(MESSAGE_LAYER, appContext, listener);
        ServerAuthConfig serverAuthConfig = null;
        JaspicCallbackHandler callbackHandler = null;
        if (authConfigProvider != null) {
            callbackHandler = new JaspicCallbackHandler(loginService);
            try {
                serverAuthConfig = authConfigProvider.getServerAuthConfig(MESSAGE_LAYER, appContext, callbackHandler);
            } catch (AuthException e) {
                //TODO log exception?  rethrow????
            }
        }
        if (serverAuthConfig != null) {
            Map authProperties = new HashMap();
            authProperties.put(POLICY_CONTEXT_ID_KEY, policyContextId);
            Subject serviceSubject = new Subject();
            authenticator = new JaspicAuthenticator(serverAuthConfig, authProperties, serviceSubject, callbackHandler, identityService);
        } else if ("BASIC".equalsIgnoreCase(authMethod)) {
            authenticator = new BasicAuthenticator(loginService, realmName, unauthenticatedIdentity);
        } else if ("CLIENT-CERT".equalsIgnoreCase(authMethod)) {
            authenticator = new ClientCertAuthenticator(loginService, unauthenticatedIdentity);
        } else if ("DIGEST".equalsIgnoreCase(authMethod)) {
            authenticator = new DigestAuthenticator(loginService, realmName, unauthenticatedIdentity);
        } else if ("FORM".equalsIgnoreCase(authMethod)) {
            authenticator = new FormAuthenticator(loginService, unauthenticatedIdentity, loginPage, errorPage);
        } else if ("GENERIC".equalsIgnoreCase(authMethod)) {
            authenticator = new GenericHeaderAuthenticator(loginService, unauthenticatedIdentity);
        } else if ("SPNEGO".equalsIgnoreCase(authMethod)) {
            authenticator = new SpnegoAuthenticator(loginService, realmName, unauthenticatedIdentity);  
        } else {
            authenticator = new NoneAuthenticator(unauthenticatedIdentity);
        }

        AccessControlContext defaultAcc = ContextManager.registerSubjectShort(defaultSubject,  null, null);
        Authorizer authorizer = createAuthorizer(defaultAcc);

        SecurityValve securityValve = new JACCSecurityValve(authenticator, authorizer, identityService, policyContextId);

        geronimoContext.addValve(securityValve);
        if (log.isDebugEnabled()) {
            log.debug(sm.getString(
                    "contextConfig.authenticatorConfigured",
                    authMethod));
        }

        geronimoContext.setRealm(new JACCRealm());
    }

    protected Authorizer createAuthorizer(AccessControlContext defaultAcc) {
        return new JACCAuthorizer(defaultAcc);
    }

    @Override
    protected void parseWebXml(InputSource source, WebXml dest, boolean fragment) {
        super.parseWebXml(source, dest, fragment);
        //Let's forbidden Tomcat scanning anything
        dest.setMetadataComplete(true);
    }
}
