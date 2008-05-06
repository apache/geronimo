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


package org.apache.geronimo.openejb;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.security.auth.Destroyable;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.security.SubjectId;
import org.apache.openejb.client.ClientSecurity;
import org.apache.openejb.client.ServerMetaData;

/**
 * OpenejbRemoteLoginModule uses the openejb protocol to communicate with the server to be used for ejbs and try to
 * login on that server. If login succeeds an identity token is added to the private credentials of the Subject
 * that can be used on further calls to identify the client.  Note this should only be used on secure networks or
 * with secured communication with openejb, as sniffing the identity token gives you all the permissions of the user you
 * sniffed.
 * <p/>
 * This login module checks security credentials so the lifecycle methods must return true to indicate success
 * or throw LoginException to indicate failure.
 *
 * @version $Rev$ $Date$
 */
public class OpenejbRemoteLoginModule implements LoginModule {
    private static final Logger log = LoggerFactory.getLogger(OpenejbRemoteLoginModule.class);
    private static final String SECURITY_REALM_KEY = "RemoteSecurityRealm";
    private static final String SECURITY_REALM_KEY_LONG = OpenejbRemoteLoginModule.class.getName() + "." + SECURITY_REALM_KEY;
    private static final String SERVER_URI_KEY = "ServerURI";
    private static final String SERVER_URI_KEY_LONG = OpenejbRemoteLoginModule.class.getName() + "." + SERVER_URI_KEY;
    public final static List<String> supportedOptions = Collections.unmodifiableList(Arrays.asList(SECURITY_REALM_KEY, SERVER_URI_KEY, SECURITY_REALM_KEY_LONG, SERVER_URI_KEY_LONG));

    private Subject subject;
    private CallbackHandler callbackHandler;
    private String securityRealm;
    private URI serverURI;
    private SubjectId identity;
    private boolean loginSucceeded;
    private ServerIdentityToken sit;

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        for (Object option : options.keySet()) {
            if (!supportedOptions.contains(option)) {
                log.warn("Ignoring option: " + option + ". Not supported.");
            }
        }
        securityRealm = (String) options.get(SECURITY_REALM_KEY);
        if (securityRealm == null) {
            securityRealm = (String) options.get(SECURITY_REALM_KEY_LONG);
        }

        String serverURIstring = (String) options.get(SERVER_URI_KEY);
        if (serverURIstring == null) {
            serverURIstring = (String) options.get(SERVER_URI_KEY_LONG);
        }
        serverURI = URI.create(serverURIstring);

    }

    public boolean login() throws LoginException {
        loginSucceeded = false;
        Callback[] callbacks = new Callback[]{new NameCallback("username"), new PasswordCallback("passsword", false)};
        try {
            callbackHandler.handle(callbacks);
        } catch (IOException e) {
            throw (LoginException) new LoginException("Could not execute callbacks").initCause(e);
        } catch (UnsupportedCallbackException e) {
            throw (LoginException) new LoginException("Could not execute callbacks").initCause(e);
        }
        String userName = ((NameCallback) callbacks[0]).getName();
        String password = new String(((PasswordCallback) callbacks[1]).getPassword());
        identity = (SubjectId) ClientSecurity.directAuthentication(securityRealm, userName, password, new ServerMetaData(serverURI));
        loginSucceeded = true;
        return true;
    }

    /*
     * @exception LoginException if login succeeded but commit failed.
     *
     * @return true if login succeeded and commit succeeded, or false if login failed but commit succeeded.
     */
    public boolean commit() throws LoginException {
        if (loginSucceeded) {
            if (identity != null) {
                sit = new ServerIdentityToken(serverURI, identity);
                subject.getPrivateCredentials().add(sit);
            }
        }
        // Clear out the private state
        identity = null;
        return loginSucceeded;
    }

    public boolean abort() throws LoginException {
        if (loginSucceeded) {
            // Clear out the private state
            identity = null;
            sit = null;
        }
        return loginSucceeded;
    }

    public boolean logout() throws LoginException {
        // Clear out the private state
        loginSucceeded = false;
        identity = null;
        if (sit != null) {
            if (!subject.isReadOnly()) {
                subject.getPrivateCredentials().remove(sit);
            } else {
                try {
                    if (sit instanceof Destroyable) {
                        // Try to destroy the credential
                        try {
                            ((Destroyable) sit).destroy();
                        } catch (Exception e) {
                            throw new LoginException();
                        }
                    } else {
                        throw new LoginException();
                    }
                } finally {
                    sit = null;
                }
            }
        }
        sit = null;
        return true;
    }
}
