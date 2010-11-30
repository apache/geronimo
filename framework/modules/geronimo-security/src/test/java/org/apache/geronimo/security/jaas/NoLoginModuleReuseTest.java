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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import junit.framework.TestCase;
import org.apache.karaf.jaas.boot.ProxyLoginModule;
import org.apache.geronimo.kernel.osgi.MockBundleContext;
import org.apache.geronimo.security.realm.GenericSecurityRealm;
import org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal;
import org.osgi.framework.BundleContext;


/**
 * @version $Rev$ $Date$
 */
public class NoLoginModuleReuseTest extends TestCase {

    private BundleContext bundleContext = new MockBundleContext(getClass().getClassLoader(), "", null, null);

    public void testNoLoginModuleReuse() throws Exception {
        ProxyLoginModule.init(bundleContext);
        doTest(true, "realm1");
        doTest(false, "realm2");
    }

    private void doTest(boolean wrapPrincipals, String realmName) throws ClassNotFoundException, LoginException {
        LoginModuleGBean module = new LoginModuleGBean(MockLoginModule.class.getName(), "foo", true, new HashMap<String, Object>(), "domain", getClass().getClassLoader());
        JaasLoginModuleUse loginModuleUse = new JaasLoginModuleUse(module, null, LoginModuleControlFlag.REQUIRED);
        GenericSecurityRealm realm = new GenericSecurityRealm(realmName,
                loginModuleUse,
                wrapPrincipals,
                true,
                null,
                bundleContext.getBundle(),
                null);
        GeronimoLoginConfiguration loginConfig = new GeronimoLoginConfiguration(Collections.<ConfigurationEntryFactory>singleton(realm), false);
        doLogin(loginConfig, realmName);
        doLogin(loginConfig, realmName);
    }

    private void doLogin(Configuration config, String realm) throws LoginException {
        LoginContext lc = new LoginContext(realm,
                new Subject(),
                new CallbackHandler() {

                    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                    }
                },
                config);
        lc.login();
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
