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

import java.util.Map;
import java.io.IOException;
import java.net.URI;

import javax.security.auth.spi.LoginModule;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.openejb.client.ClientSecurity;
import org.apache.openejb.client.ServerMetaData;
import org.apache.geronimo.security.SubjectId;

/**
<<<<<<< .working
 * @version $Rev$ $Date$
=======
 * OpenejbRemoteLoginModule uses the openejb protocol to communicate with the server to be used for ejbs and try to
 * login on that server. If login succeeds an identity token is added to the private credentials of the Subject
 * that can be used on further calls to identify the client.  Note this should only be used on secure networks or
 * with secured communication with openejb, as sniffing the identity token gives you all the permissions of the user you
 * sniffed.
 *
 * This login module checks security credentials so the lifecycle methods must return true to indicate success
 * or throw LoginException to indicate failure.
 *
 * @version $Rev$ $Date$
>>>>>>> .merge-right.r565912
 */
public class OpenejbRemoteLoginModule implements LoginModule {
    private static final String SECURITY_REALM_KEY = "org.apache.geronimo.openejb.OpenejbRemoteLoginModule.RemoteSecurityRealm";
    private static final String SERVER_URI_KEY = "org.apache.geronimo.openejb.OpenejbRemoteLoginModule.ServerURI";

    private Subject subject;
    private CallbackHandler callbackHandler;
    private String securityRealm;
    private URI serverURI;
    private SubjectId identity;
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        securityRealm = (String) options.get(SECURITY_REALM_KEY);
        serverURI = URI.create((String) options.get(SERVER_URI_KEY));
    }

    public boolean login() throws LoginException {
        Callback[] callbacks = new Callback[] {new NameCallback("username"), new PasswordCallback("passsword", false)};
        try {
            callbackHandler.handle(callbacks);
        } catch (IOException e) {
            throw (LoginException)new LoginException("Could not execute callbacks").initCause(e);
        } catch (UnsupportedCallbackException e) {
            throw (LoginException)new LoginException("Could not execute callbacks").initCause(e);
        }
        String userName = ((NameCallback)callbacks[0]).getName();
        String password = new String(((PasswordCallback)callbacks[1]).getPassword());
        identity = (SubjectId) ClientSecurity.directAuthentication(securityRealm, userName, password, new ServerMetaData(serverURI));
        return true;
    }

    public boolean commit() throws LoginException {
        subject.getPrivateCredentials().add(new ServerIdentityToken(serverURI, identity));
        return true;
    }

    public boolean  abort() throws LoginException {
        subject.getPrivateCredentials().remove(identity);
        return true;
    }

    public boolean logout() throws LoginException {
        //TODO what?
        return true;
    }
}
