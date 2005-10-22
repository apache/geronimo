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

import org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal;
import org.apache.geronimo.security.jaas.server.JaasSecuritySession;
import org.apache.geronimo.security.jaas.server.JaasLoginModuleConfiguration;


/**
 * @version $Rev:  $ $Date:  $
 */
public class NoLoginModuleReuseTest extends TestCase {

    public void testNoLoginModuleReuse() throws Exception {
        JaasLoginModuleConfiguration m1 = new JaasLoginModuleConfiguration(MockLoginModule.class.getName(), LoginModuleControlFlag.REQUIRED, new HashMap(), true, "D1", true, MockLoginModule.class.getClassLoader());
        doSecurityContextLogin(m1);
        doSecurityContextLogin(m1);
    }

    private void doSecurityContextLogin(JaasLoginModuleConfiguration m1) throws LoginException {
        JaasSecuritySession c = new JaasSecuritySession("realm", new JaasLoginModuleConfiguration[] {m1}, new HashMap(), this.getClass().getClassLoader());
        Subject s = c.getSubject();
        c.getLoginModule(0).initialize(s, null, null, null);
        c.getLoginModule(0).login();
        c.getLoginModule(0).commit();
    }

    public static class MockLoginModule implements LoginModule {

        private Subject subject;
        private boolean used = false;

        public void initialize(Subject subject, CallbackHandler callbackHandler, Map map, Map map1) {
            this.subject = subject;
        }

        public boolean login() throws LoginException {
            if (used) {
                throw new LoginException("already used");
            }
            used = true;
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

}
