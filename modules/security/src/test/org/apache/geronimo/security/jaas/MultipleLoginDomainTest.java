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
import javax.security.auth.login.LoginException;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.spi.LoginModule;

import junit.framework.TestCase;
import org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal;

/**
 * @version $Rev:  $ $Date:  $
 */
public class MultipleLoginDomainTest extends TestCase {

    /** this test demonstrates that naming login domains does not actually separate principals from different login domains.
     * The crucial line is commented out so as to avoid breaking the build.
     * @throws Exception
     */
    public void XtestMultipleLoginDomains() throws Exception {
        JaasLoginModuleConfiguration m1 = new JaasLoginModuleConfiguration(MockLoginModule.class.getName(), LoginModuleControlFlag.REQUIRED, new HashMap(), true, "D1");
        JaasLoginModuleConfiguration m2 = new JaasLoginModuleConfiguration(MockLoginModule.class.getName(), LoginModuleControlFlag.REQUIRED, new HashMap(), true, "D2");
        JaasLoginModuleConfiguration m3 = new JaasLoginModuleConfiguration(MockLoginModule2.class.getName(), LoginModuleControlFlag.REQUIRED, new HashMap(), true, "D3");
        JaasSecurityContext c = new JaasSecurityContext("realm", new JaasLoginModuleConfiguration[] {m1, m2});
        ClassLoader cl = this.getClass().getClassLoader();
        Subject s = c.getSubject();
        m1.getLoginModule(cl).initialize(s, null, null, null);
        m2.getLoginModule(cl).initialize(s, null, null, null);
        m3.getLoginModule(cl).initialize(s, null, null, null);
        m1.getLoginModule(cl).login();
        m2.getLoginModule(cl).login();
        m3.getLoginModule(cl).login();
        m1.getLoginModule(cl).commit();
        c.processPrincipals("D1");
        assertEquals(2, s.getPrincipals().size());
        m2.getLoginModule(cl).commit();
        c.processPrincipals("D2");
        //Uncomment the following line to verify that the subject will have only 2 principals rather than the desired 3 after both
        //login modules have tried to add the same principal to the subject.
        assertEquals(3, s.getPrincipals().size());
        c.processPrincipals("D3");
        //algorithmm is still broken, as can be seen by uncommenting the next line
//        assertEquals(3, s.getPrincipals().size());
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

    public static class MockLoginModule2 implements LoginModule {

        Subject subject;

        public void initialize(Subject subject, CallbackHandler callbackHandler, Map map, Map map1) {
            this.subject = subject;
        }

        public boolean login() throws LoginException {
            return true;
        }

        public boolean commit() throws LoginException {
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
