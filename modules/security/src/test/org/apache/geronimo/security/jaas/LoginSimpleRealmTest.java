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

import javax.management.ObjectName;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.Properties;

import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.security.AbstractTest;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.system.serverinfo.ServerInfo;


/**
 * @version $Rev$ $Date$
 */
public class LoginSimpleRealmTest extends AbstractTest {

    protected ObjectName serverInfo;
    protected ObjectName loginConfiguration;
    protected ObjectName simpleRealm;
    protected ObjectName simpleCE;

    public void setUp() throws Exception {
        super.setUp();

        GBeanMBean gbean;

        gbean = new GBeanMBean(ServerInfo.GBEAN_INFO);
        serverInfo = new ObjectName("geronimo.system:role=ServerInfo");
        gbean.setAttribute("baseDirectory", ".");
        kernel.loadGBean(serverInfo, gbean);
        kernel.startGBean(serverInfo);

        gbean = new GBeanMBean("org.apache.geronimo.security.jaas.GeronimoLoginConfiguration");
        loginConfiguration = new ObjectName("geronimo.security:type=LoginConfiguration");
        kernel.loadGBean(loginConfiguration, gbean);

        Properties options = new Properties();
        options.put("group", "it");

        gbean = new GBeanMBean("org.apache.geronimo.security.realm.providers.SimpleSecurityRealm");
        simpleRealm = new ObjectName("geronimo.security:type=SecurityRealm,realm=simple-realm");
        gbean.setAttribute("realmName", "simple-realm");
        gbean.setAttribute("loginModuleName", TestLoginModule.class.getName());
        gbean.setAttribute("options", options);
        gbean.setAttribute("maxLoginModuleAge", new Long(24 * 60 * 60 * 1000));
        kernel.loadGBean(simpleRealm, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.security.jaas.ConfigurationEntryRealmLocal");
        simpleCE = new ObjectName("geronimo.security:type=ConfigurationEntry,jaasId=properties");
        gbean.setAttribute("applicationConfigName", "simple");
        gbean.setAttribute("realmName", "simple-realm");
        gbean.setAttribute("controlFlag", LoginModuleControlFlag.REQUIRED);
        gbean.setAttribute("options", new Properties());
        kernel.loadGBean(simpleCE, gbean);

        kernel.startGBean(loginConfiguration);
        kernel.startGBean(simpleRealm);
        kernel.startGBean(simpleCE);
    }

    public void tearDown() throws Exception {
        kernel.stopGBean(simpleCE);
        kernel.stopGBean(simpleRealm);
        kernel.stopGBean(loginConfiguration);
        kernel.stopGBean(serverInfo);

        kernel.unloadGBean(simpleRealm);
        kernel.unloadGBean(simpleCE);
        kernel.unloadGBean(loginConfiguration);
        kernel.unloadGBean(serverInfo);

        super.tearDown();
    }

    public void testLogin() throws Exception {

        LoginContext context = new LoginContext("simple", new UsernamePasswordCallback("alan", "starcraft"));

        context.login();
        Subject subject = context.getSubject();

        assertTrue("expected non-null subject", subject != null);
        assertTrue("subject should have one remote principal", subject.getPrincipals(IdentificationPrincipal.class).size() == 1);
        IdentificationPrincipal remote = (IdentificationPrincipal) subject.getPrincipals(IdentificationPrincipal.class).iterator().next();
        assertTrue("subject should be associated with remote id", ContextManager.getRegisteredSubject(remote.getId()) != null);
        assertTrue("subject should have five principals", subject.getPrincipals().size() == 5);
        assertTrue("subject should have two realm principal", subject.getPrincipals(RealmPrincipal.class).size() == 2);
        RealmPrincipal principal = (RealmPrincipal) subject.getPrincipals(RealmPrincipal.class).iterator().next();
        assertTrue("id of principal should be non-zero", principal.getId() != 0);

        context.logout();

        assertTrue("id of subject should be null", ContextManager.getSubjectId(subject) == null);
    }

    public static class TestLoginModule implements LoginModule {

        private Subject subject;
        private CallbackHandler handler;

        public boolean abort() throws LoginException {
            return true;
        }

        public boolean commit() throws LoginException {
            subject.getPrincipals().add(new TestPrincipal("alan"));
            subject.getPrincipals().add(new TestGroupPrincipal("it"));
            return true;
        }

        public boolean login() throws LoginException {
            Callback[] callbacks = new Callback[2];

            callbacks[0] = new NameCallback("User name");
            callbacks[1] = new PasswordCallback("Password", false);
            try {
                handler.handle(callbacks);
            } catch (IOException ioe) {
                throw (LoginException) new LoginException().initCause(ioe);
            } catch (UnsupportedCallbackException uce) {
                throw (LoginException) new LoginException().initCause(uce);
            }
            String username = ((NameCallback) callbacks[0]).getName();
            assert username != null;
            String password = new String(((PasswordCallback) callbacks[1]).getPassword());
            assert password != null;

            return "alan".equals(username) && "starcraft".equals(password);
        }

        public boolean logout() throws LoginException {
            return true;
        }

        public void initialize(Subject subject, CallbackHandler handler, Map sharedState, Map options) {
            this.subject = subject;
            this.handler = handler;

            if (!options.get("group").equals("it")) throw new IllegalArgumentException("Missing group option");
        }

        public class TestPrincipal implements Principal {

            private final String name;

            public TestPrincipal(String name) {
                this.name = name;
            }

            public String getName() {
                return name;
            }
        }

        public class TestGroupPrincipal implements Principal {

            private final String name;

            public TestGroupPrincipal(String name) {
                this.name = name;
            }

            public String getName() {
                return name;
            }
        }
    }
}
