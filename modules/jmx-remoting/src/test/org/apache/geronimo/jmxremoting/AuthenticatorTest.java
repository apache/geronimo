/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.jmxremoting;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import junit.framework.TestCase;

/**
 * 
 * 
 * @version $Revision: 1.2 $ $Date: 2004/06/02 06:47:56 $
 */
public class AuthenticatorTest extends TestCase {
    private static final String CONFIG_NAME = "testConfig";
    private Configuration oldConfiguration;
    private Configuration loginConfig;
    private String[] credentials;
    private Authenticator authenticator;

    public void testAuthenticateWithValidPassword() {
        try {
            Subject s = authenticator.authenticate(credentials);
            Set principals = s.getPrincipals();
            assertTrue(principals.contains(new MockPrincipal("username")));
        } catch (SecurityException e) {
            e.printStackTrace();
            fail();
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        try {
            oldConfiguration = Configuration.getConfiguration();
        } catch (SecurityException e) {
            oldConfiguration = null;
        }
        loginConfig = new MockConfiguration();
        Configuration.setConfiguration(loginConfig);

        credentials = new String[]{"username", "password"};
        authenticator = new Authenticator(CONFIG_NAME);
    }

    protected void tearDown() throws Exception {
        Configuration.setConfiguration(oldConfiguration);
        super.tearDown();
    }

    private class MockConfiguration extends Configuration {
        public AppConfigurationEntry[] getAppConfigurationEntry(String applicationName) {
            if (CONFIG_NAME.equals(applicationName) == false) {
                fail();
            }
            Map map = new HashMap();
            map.put("username", "password");
            AppConfigurationEntry entry = new AppConfigurationEntry(MockModule.class.getName(), AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, map);
            return new AppConfigurationEntry[] {entry};
        }

        public void refresh() {
        }
    }

    public static class MockModule implements LoginModule {
        private Subject subject;
        private CallbackHandler handler;
        private Map options;
        private String username;

        public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
            this.subject = subject;
            this.handler = callbackHandler;
            this.options = options;
        }

        public boolean login() throws LoginException {
            NameCallback nameCallback = new NameCallback("name");
            PasswordCallback passwordCallback = new PasswordCallback("password", false);
            try {
                handler.handle(new Callback[] {nameCallback, passwordCallback});
                username = nameCallback.getName();
                String password = (String) options.get(username);
                if (password == null) {
                    return false;
                }
                return password.equals(new String(passwordCallback.getPassword()));
            } catch (java.io.IOException e) {
                return false;
            } catch (UnsupportedCallbackException e) {
                return false;
            }
        }

        public boolean commit() throws LoginException {
            subject.getPrincipals().add(new MockPrincipal(username));
            return true;
        }

        public boolean logout() throws LoginException {
            return false;
        }

        public boolean abort() throws LoginException {
            return true;
        }
    }

    private static class MockPrincipal implements Principal {
        private final String name;

        public MockPrincipal(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MockPrincipal)) return false;

            final MockPrincipal mockPrincipal = (MockPrincipal) o;

            if (!name.equals(mockPrincipal.name)) return false;

            return true;
        }

        public int hashCode() {
            return name.hashCode();
        }
    }
}
