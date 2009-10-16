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
package org.apache.geronimo.security.jaas;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.security.auth.spi.LoginModule;
import javax.security.auth.Subject;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.login.LoginException;
import javax.security.auth.callback.CallbackHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConfiguredIdentityNamedUsernamePasswordLoginModule adds a geronimo-specific NamedUsernamePasswordCredential
 * to the subject constructed from the configured username, password, and credential name.  This is useful in
 * supplying fixed credentials to e.g. web service calls.
 *
 * Note that this places passwords to external services in configuration information.  It may be more appropriate
 * to use the GeronimoPropertiesFileMappedPasswordCredentialLoginModule or a run-as subject with a
 * NamedUsernamePasswordCredentialLoginModule although the latter solution may put a credential in a
 * credential store configuration.
 *
 * This login module does not check credentials so it should never be able to cause a login to succeed.
 * Therefore the lifecycle methods must return false to indicate success or throw a LoginException to indicate failure.
 *
 * @version $Rev$ $Date$
 */
public class ConfiguredIdentityNamedUsernamePasswordLoginModule implements LoginModule {
    private static final Logger log = LoggerFactory.getLogger(ConfiguredIdentityNamedUsernamePasswordLoginModule.class);

    public static final String CREDENTIAL_NAME = "org.apache.geronimo.jaas.NamedUsernamePasswordCredential.Name";
    public static final String USER_NAME = "org.apache.geronimo.jaas.NamedUsernamePasswordCredential.Username";
    public static final String PASSWORD = "org.apache.geronimo.jaas.NamedUsernamePasswordCredential.Password";
    public final static List<String> supportedOptions = Collections.unmodifiableList(Arrays.asList(CREDENTIAL_NAME, USER_NAME, PASSWORD));

    private Subject subject;
    private NamedUsernamePasswordCredential namedUsernamePasswordCredential;
    private String name, username, password;

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        this.subject = subject;
        for(Object option: options.keySet()) {
            if(!supportedOptions.contains(option) && !JaasLoginModuleUse.supportedOptions.contains(option)
                    && !WrappingLoginModule.supportedOptions.contains(option)) {
                log.warn("Ignoring option: "+option+". Not supported.");
            }
        }
        name = (String) options.get(CREDENTIAL_NAME);
        username = (String) options.get(USER_NAME);
        password = (String) options.get(PASSWORD);
    }

    public boolean login() throws LoginException {
        return false;
    }

    public boolean commit() throws LoginException {
        if (subject.isReadOnly()) {
            throw new LoginException("Subject is ReadOnly");
        }

        namedUsernamePasswordCredential = new NamedUsernamePasswordCredential(username, password.toCharArray(), name);
        subject.getPrivateCredentials().add(namedUsernamePasswordCredential);

        return false;
    }

    public boolean abort() throws LoginException {
        return logout();
    }

    public boolean logout() throws LoginException {
        if (namedUsernamePasswordCredential == null) {
            return false;
        }

        Set pvtCreds = subject.getPrivateCredentials();
        if (!subject.isReadOnly()) {
            pvtCreds.remove(namedUsernamePasswordCredential);
        }

        try {
            namedUsernamePasswordCredential.destroy();
        } catch (DestroyFailedException e) {
            // do nothing
        }
        namedUsernamePasswordCredential = null;

        return false;
    }
}
