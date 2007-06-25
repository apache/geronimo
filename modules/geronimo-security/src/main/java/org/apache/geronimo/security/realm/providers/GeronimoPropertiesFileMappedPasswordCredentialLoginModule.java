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


package org.apache.geronimo.security.realm.providers;

import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.GeronimoSecurityException;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.security.jaas.NamedUsernamePasswordCredential;
import org.apache.geronimo.system.serverinfo.ServerInfo;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoPropertiesFileMappedPasswordCredentialLoginModule implements LoginModule {

    private static final Log log = LogFactory.getLog(GeronimoPropertiesFileMappedPasswordCredentialLoginModule.class);
    public final static String CREDENTIALS_URI = "credentialsURI";
    private final static Pattern pattern = Pattern.compile("([^:,=]*):([^:,=]*)=([^:,=]*)");

    private final Set<NamedUsernamePasswordCredential> passwordCredentials = new HashSet<NamedUsernamePasswordCredential>();
    private final Properties credentials = new Properties();

    private Subject subject;
    private CallbackHandler callbackHandler;

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        try {
            ServerInfo serverInfo = (ServerInfo) options.get(JaasLoginModuleUse.SERVERINFO_LM_OPTION);
            final String credentials = (String) options.get(CREDENTIALS_URI);
            if (credentials == null) {
                throw new IllegalArgumentException(CREDENTIALS_URI + " must be provided!");
            }
            URI usersURI = new URI(credentials);
            loadProperties(serverInfo, usersURI);
        } catch (Exception e) {
            log.error("Initialization failed", e);
            throw new IllegalArgumentException("Unable to configure properties file login module: " + e.getMessage(), e);
        }
    }

    private void loadProperties(ServerInfo serverInfo, URI credentialsURI) throws GeronimoSecurityException {
        try {
            URI userFile = serverInfo.resolveServer(credentialsURI);
            InputStream stream = userFile.toURL().openStream();
            credentials.load(stream);
            stream.close();
        } catch (Exception e) {
            log.error("Properties File Login Module - data load failed", e);
            throw new GeronimoSecurityException(e);
        }
    }

    public boolean login() throws LoginException {
        Callback[] callbacks = new Callback[1];
        callbacks[0] = new NameCallback("User name");
        try {
            callbackHandler.handle(callbacks);
        } catch (java.io.IOException e) {
            throw (LoginException) new LoginException("Unlikely IOException").initCause(e);
        } catch (UnsupportedCallbackException e) {
            throw (LoginException) new LoginException("Unlikely UnsupportedCallbackException").initCause(e);
        }
        String userName = ((NameCallback) callbacks[0]).getName();
        String unparsedCredentials = credentials.getProperty(userName);
        if (unparsedCredentials != null) {
            parseCredentials(unparsedCredentials, passwordCredentials);
        }
        return true;
    }

    void parseCredentials(String unparsedCredentials, Set<NamedUsernamePasswordCredential> passwordCredentials) {
        Matcher matcher = pattern.matcher(unparsedCredentials);
        while (matcher.find()) {
            String credentialName = matcher.group(1);
            String credentialUser = matcher.group(2);
            String credentialPassword = matcher.group(3);
            NamedUsernamePasswordCredential credential = new NamedUsernamePasswordCredential(credentialUser, credentialPassword.toCharArray(), credentialName);
            passwordCredentials.add(credential);
        }
    }

    public boolean commit() throws LoginException {
        subject.getPrivateCredentials().addAll(passwordCredentials);
        return true;
    }

    public boolean abort() throws LoginException {
        passwordCredentials.clear();
        return true;
    }

    public boolean logout() throws LoginException {
        passwordCredentials.clear();
        return true;
    }
}
