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
package org.apache.geronimo.jmxremoting;

import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
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
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import junit.framework.TestCase;

import org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal;

/**
 * 
 * 
 * @version $Rev$ $Date$
 */
public class AuthenticatorTest extends TestCase {
    private static final String CONFIG_NAME = "testConfig";
    private Configuration oldConfiguration;
    private Authenticator authenticator;
    public void testMonitorGroupLogin() throws Exception {
        testFailure("monitor", "monitor");
    }

    public void testLogin() {
        try {
            String[] credentials = new String[]{"system", "manager"};
            Subject s = authenticator.authenticate(credentials);
            Set principals = s.getPrincipals();
            assertTrue(principals.contains(new MockPrincipal("system")));
        } catch (SecurityException e) {
            e.printStackTrace();
            fail();
        }
    }
    
    public void testBadPasswordLogin() throws Exception {   
        testFailure("system", "managerr");
    }
    
    public void testBadUser() throws Exception {  
        testFailure("doesnotexist", "managerr");
    }
    
    public void testNullPasswordLogin() throws Exception {        
        testFailure("system", null);
    }
    
    public void testNullUserLogin() throws Exception {        
        testFailure(null, "manager");
    }
    
    public void testNullCredentialsLogin() throws Exception {        
        testFailure(null, null);
    }
    
    public void testEmptyCredentialsLogin() throws Exception {        
        testFailure("", "");
    }
    
    private void testFailure(String usernane, String password) throws Exception {
        try {
            String[] credentials = new String[]{usernane, password};
            Subject s = authenticator.authenticate(credentials);
            fail("Did not throw expected exception");
        } catch (SecurityException e) {
            // expected
        }
    }
    
    public void testNoCredentialsLogin() {
        try {
            Subject s = authenticator.authenticate(null);
            fail("Did not throw expected exception");
        } catch (Exception e) {
            // expected
        }
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        try {
            oldConfiguration = Configuration.getConfiguration();
        } catch (SecurityException e) {
            oldConfiguration = null;
        }
        Configuration loginConfig = new MockConfiguration();
        Configuration.setConfiguration(loginConfig);

        authenticator = new Authenticator(CONFIG_NAME, getClass().getClassLoader());
    }

    protected void tearDown() throws Exception {
        Configuration.setConfiguration(oldConfiguration);
        super.tearDown();
    }

    private class MockConfiguration extends Configuration {
        public AppConfigurationEntry[] getAppConfigurationEntry(String applicationName) {
            if (!CONFIG_NAME.equals(applicationName)) {
                fail();
            }
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("system", "manager");
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
        private static Map<String, Set<String>> userGroupsMap = new HashMap<String, Set<String>>();
        static {
            Set<String> systemGroupsSet = new HashSet<String>();
            systemGroupsSet.add("admin");
            systemGroupsSet.add("monitor");
            userGroupsMap.put("system", systemGroupsSet);
            Set<String> monitorGroupsSet = new HashSet<String>();
            monitorGroupsSet.add("monitor");
            userGroupsMap.put("monitor", monitorGroupsSet);
        }

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
                    throw new FailedLoginException();
                }
                if (password.equals(new String(passwordCallback.getPassword()))) {
                    return true;
                }
                throw new FailedLoginException();
            } catch (java.io.IOException e) {
                throw new FailedLoginException();
            } catch (UnsupportedCallbackException e) {
                throw new FailedLoginException();
            }
        }

        public boolean commit() throws LoginException {
            subject.getPrincipals().add(new MockPrincipal(username));
            for (String groupName : userGroupsMap.get(username)) {
                subject.getPrincipals().add(
                    new GeronimoGroupPrincipal(groupName));
            }
            return true;
        }

        public boolean logout() throws LoginException {
            return true;
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
