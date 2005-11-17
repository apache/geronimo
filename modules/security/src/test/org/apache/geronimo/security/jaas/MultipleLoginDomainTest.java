/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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
package org.apache.geronimo.security.jaas;

import java.util.HashMap;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import junit.framework.TestCase;

import org.apache.geronimo.security.DomainPrincipal;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.jaas.server.JaasLoginModuleConfiguration;
import org.apache.geronimo.security.jaas.server.JaasSecuritySession;
import org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal;


/**
 * @version $Rev$ $Date$
 */
public class MultipleLoginDomainTest extends TestCase {

    public void testDummy() throws Exception {
    }

    /**
     * this test demonstrates that naming login domains does not actually separate principals from different login domains.
     * The crucial line is commented out so as to avoid breaking the build.
     *
     * @throws Exception
     */
    public void testMultipleLoginDomains() throws Exception {
        JaasLoginModuleConfiguration m1 = new JaasLoginModuleConfiguration(MockLoginModule.class.getName(), LoginModuleControlFlag.REQUIRED, new HashMap(), true, "D1", true, MockLoginModule.class.getClassLoader());
        JaasLoginModuleConfiguration m2 = new JaasLoginModuleConfiguration(MockLoginModule.class.getName(), LoginModuleControlFlag.REQUIRED, new HashMap(), true, "D2", true, MockLoginModule.class.getClassLoader());
        JaasLoginModuleConfiguration m3 = new JaasLoginModuleConfiguration(AnotherMockLoginModule.class.getName(), LoginModuleControlFlag.REQUIRED, new HashMap(), false, "D3", false, AnotherMockLoginModule.class.getClassLoader());
        JaasLoginModuleConfiguration m4 = new JaasLoginModuleConfiguration(AnotherMockLoginModule.class.getName(), LoginModuleControlFlag.REQUIRED, new HashMap(), false, "D4", true, AnotherMockLoginModule.class.getClassLoader());
        JaasSecuritySession c = new JaasSecuritySession("realm", new JaasLoginModuleConfiguration[]{m1, m2, m3, m4}, new HashMap(), this.getClass().getClassLoader());
        Subject s = c.getSubject();

        c.getLoginModule(0).initialize(s, null, null, null);
        c.getLoginModule(1).initialize(s, null, null, null);
        c.getLoginModule(2).initialize(s, null, null, null);
        c.getLoginModule(3).initialize(s, null, null, null);
        c.getLoginModule(0).login();
        c.getLoginModule(1).login();
        c.getLoginModule(2).login();
        c.getLoginModule(3).login();
        c.getLoginModule(0).commit();

        assertEquals("Subject should have three principals", 3, s.getPrincipals().size());
        assertEquals("server-side subject should have one realm principal", 1, s.getPrincipals(RealmPrincipal.class).size());
        assertEquals("server-side subject should have one domain principal", 1, s.getPrincipals(DomainPrincipal.class).size());

        c.getLoginModule(1).commit();

        assertEquals("Subject should now have five principals", 5, s.getPrincipals().size());

        c.getLoginModule(2).commit();

        assertEquals("Subject should now have five principals", 6, s.getPrincipals().size());

        c.getLoginModule(3).commit();

        assertEquals("Subject should now have five principals", 8, s.getPrincipals().size());
    }

    public static class MockLoginModule implements LoginModule {

        Subject subject;

        public void initialize(Subject subject, CallbackHandler callbackHandler, Map map, Map map1) {
            this.subject = subject;
        }

        public boolean login() throws LoginException {
            return true;
        }

        public boolean commit() throws LoginException {
            subject.getPrincipals().add(new GeronimoGroupPrincipal("Foo"));
            return true;
        }

        public boolean abort() throws LoginException {
            return false;
        }

        public boolean logout() throws LoginException {
            return false;
        }
    }

    public static class AnotherMockLoginModule implements LoginModule {

        Subject subject;

        public void initialize(Subject subject, CallbackHandler callbackHandler, Map map, Map map1) {
            this.subject = subject;
        }

        public boolean login() throws LoginException {
            return true;
        }

        public boolean commit() throws LoginException {
            subject.getPrincipals().add(new GeronimoGroupPrincipal("Bar"));
            return true;
        }

        public boolean abort() throws LoginException {
            return false;
        }

        public boolean logout() throws LoginException {
            return false;
        }
    }

}
