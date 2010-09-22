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


package org.apache.geronimo.tomcat.security.authentication.jaspic;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.callback.GroupPrincipalCallback;
import javax.security.auth.message.callback.PasswordValidationCallback;
import javax.security.auth.message.config.ServerAuthConfig;
import javax.security.auth.message.config.ServerAuthContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.tomcat.security.AuthResult;
import org.apache.geronimo.tomcat.security.Authenticator;
import org.apache.geronimo.tomcat.security.IdentityService;
import org.apache.geronimo.tomcat.security.ServerAuthException;
import org.apache.geronimo.tomcat.security.TomcatAuthStatus;
import org.apache.geronimo.tomcat.security.UserIdentity;

import static org.apache.geronimo.tomcat.security.SecurityValve.CACHED_IDENTITY_KEY;

/**
 * @version $Rev$ $Date$
 */
public class JaspicAuthenticator implements Authenticator {
    private static final String MESSAGE_INFO_KEY = "org.apache.geronimo.tomcat.jaspic.message.info";
    public static final String CONTAINER_CACHING_KEY = "org.apache.geronimo.jaspic.servlet.containerCaching";

    private final ServerAuthConfig serverAuthConfig;
    private final Map authProperties;
    private final Subject serviceSubject;
    private final JaspicCallbackHandler callbackHandler;
    private final IdentityService identityService;
    private final boolean containerCaching;

    public JaspicAuthenticator(ServerAuthConfig serverAuthConfig, Map authProperties, Subject serviceSubject, JaspicCallbackHandler callbackHandler, IdentityService identityService) {
        this.serverAuthConfig = serverAuthConfig;
        this.authProperties = authProperties;
        this.serviceSubject = serviceSubject;
        this.callbackHandler = callbackHandler;
        this.identityService = identityService;
        containerCaching = authProperties != null && (authProperties.get(CONTAINER_CACHING_KEY) == null ? false : Boolean.valueOf((String) authProperties.get(CONTAINER_CACHING_KEY)));
    }

    public AuthResult validateRequest(Request request, HttpServletResponse response, boolean isAuthMandatory, UserIdentity cachedIdentity) throws ServerAuthException {
        try {
            MessageInfo messageInfo = new JaspicMessageInfo(request, response, isAuthMandatory);
            if (cachedIdentity != null) {
                messageInfo.getMap().put(CACHED_IDENTITY_KEY, cachedIdentity);
            }
            request.setNote(MESSAGE_INFO_KEY, messageInfo);
            String authContextId = serverAuthConfig.getAuthContextID(messageInfo);
            ServerAuthContext authContext = serverAuthConfig.getAuthContext(authContextId, serviceSubject, authProperties);
            Subject clientSubject = new Subject();

            AuthStatus authStatus = authContext.validateRequest(messageInfo, clientSubject, serviceSubject);
            if (authStatus == AuthStatus.SEND_CONTINUE)
                return new AuthResult(TomcatAuthStatus.SEND_CONTINUE, null, false);
            if (authStatus == AuthStatus.SEND_FAILURE)
                return new AuthResult(TomcatAuthStatus.SEND_FAILURE, null, false);

            if (authStatus == AuthStatus.SUCCESS) {
                Set<UserIdentity> ids = clientSubject.getPrivateCredentials(UserIdentity.class);
                UserIdentity userIdentity;
                if (ids.size() > 0) {
                    userIdentity = ids.iterator().next();
                } else {
                    CallerPrincipalCallback principalCallback = callbackHandler.getThreadCallerPrincipalCallback();
                    if (principalCallback == null) throw new NullPointerException("No CallerPrincipalCallback");
                    Principal principal = principalCallback.getPrincipal();
                    if (principal == null) {
                        String principalName = principalCallback.getName();
                        Set<Principal> principals = principalCallback.getSubject().getPrincipals();
                        for (Principal p : principals) {
                            if (p.getName().equals(principalName)) {
                                principal = p;
                                break;
                            }
                        }
                        if (principal == null) {
                            //TODO not clear what to do here.
                            return new AuthResult(TomcatAuthStatus.SUCCESS, null, false);
                        }
                    }
                    GroupPrincipalCallback groupPrincipalCallback = callbackHandler.getThreadGroupPrincipalCallback();
                    String[] groups = groupPrincipalCallback == null ? null : groupPrincipalCallback.getGroups();
                    userIdentity = identityService.newUserIdentity(clientSubject, principal, groups == null ? Collections.<String>emptyList() : Arrays.asList(groups));
                }
                return new AuthResult(TomcatAuthStatus.SUCCESS, userIdentity, containerCaching);
            }
            if (authStatus == AuthStatus.SEND_SUCCESS) {
                //we are processing a message in a secureResponse dialog.
                return new AuthResult(TomcatAuthStatus.SEND_SUCCESS, null, false);
            }
            //should not happen
            throw new NullPointerException("No AuthStatus returned");
        } catch (AuthException e) {
            throw new ServerAuthException(e);
        }
    }

    public boolean secureResponse(Request request, Response response, AuthResult authResult) throws ServerAuthException {
        JaspicMessageInfo messageInfo = (JaspicMessageInfo)request.getNote(MESSAGE_INFO_KEY);
        if (messageInfo==null) throw new NullPointerException("MessageInfo from request missing: " + request);
        try
        {
            String authContextId = serverAuthConfig.getAuthContextID(messageInfo);
            ServerAuthContext authContext = serverAuthConfig.getAuthContext(authContextId,serviceSubject,authProperties);
            AuthStatus status = authContext.secureResponse(messageInfo,serviceSubject);
            return (AuthStatus.SEND_SUCCESS.equals(status));
        }
        catch (AuthException e)
        {
            throw new ServerAuthException(e);
        }
    }

    public String getAuthType() {
        return "JASPIC";
    }

    @Override
    public AuthResult login(String username, String password, Request request) throws ServletException {
        PasswordValidationCallback passwordValidationCallback = new PasswordValidationCallback(new Subject(), username, password.toCharArray());
        try {
            callbackHandler.handle(new Callback[] {passwordValidationCallback});
            if (passwordValidationCallback.getResult()) {
                UserIdentity userIdentity = passwordValidationCallback.getSubject().getPrivateCredentials(UserIdentity.class).iterator().next();
                return new AuthResult(TomcatAuthStatus.SUCCESS, userIdentity, containerCaching);
            }
            return new AuthResult(TomcatAuthStatus.FAILURE, null, false);
        } catch (UnsupportedCallbackException e) {
            throw new ServletException("internal server error");
        } catch (IOException e) {
            throw new ServletException("Unsuccessful login");
        }
    }

    @Override
    public void logout(Request request) throws ServletException {
        JaspicMessageInfo messageInfo = (JaspicMessageInfo)request.getNote(MESSAGE_INFO_KEY);
        if (messageInfo==null) throw new NullPointerException("MessageInfo from request missing: " + request);
        Subject subject = ContextManager.getCurrentCaller();
        if (subject != null) {
            identityService.associate(null);
            try
            {
                String authContextId = serverAuthConfig.getAuthContextID(messageInfo);
                ServerAuthContext authContext = serverAuthConfig.getAuthContext(authContextId,serviceSubject,authProperties);
                authContext.cleanSubject(messageInfo, subject);
            }
            catch (AuthException e)
            {
                throw new ServletException(e);
            }
        }

    }

}
