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

package org.apache.geronimo.security;

import javax.management.ObjectName;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.security.bridge.TestLoginModule;
import org.apache.geronimo.security.jaas.JaasLoginService;


/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractTest extends TestCase {
    protected Kernel kernel;
    protected ObjectName loginService;
    protected ObjectName testLoginModule;
    protected ObjectName testRealm;
    protected ObjectName serverStub;

    protected void setUp() throws Exception {
        kernel = new Kernel("test.kernel", "simple.geronimo.test");
        kernel.boot();

        GBeanMBean gbean;

        // Create all the parts

        gbean = new GBeanMBean(JaasLoginService.class.getName());
        loginService = JaasLoginService.OBJECT_NAME;
        gbean.setReferencePatterns("Realms", Collections.singleton(new ObjectName("geronimo.security:type=SecurityRealm,*")));
//        gbean.setAttribute("reclaimPeriod", new Long(10 * 1000));  // todo check other tests to see if ok
        gbean.setAttribute("algorithm", "HmacSHA1");
        gbean.setAttribute("password", "secret");
        kernel.loadGBean(loginService, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.security.jaas.LoginModuleGBean");
        testLoginModule = new ObjectName("geronimo.security:type=LoginModule,name=TestModule");
        gbean.setAttribute("loginModuleClass", "org.apache.geronimo.security.bridge.TestLoginModule");
        gbean.setAttribute("serverSide", new Boolean(true));
        gbean.setAttribute("loginDomainName", "TestLoginDomain");
        kernel.loadGBean(testLoginModule, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.security.realm.GenericSecurityRealm");
        testRealm = new ObjectName("geronimo.security:type=SecurityRealm,realm="+TestLoginModule.REALM_NAME);
        gbean.setAttribute("realmName", TestLoginModule.REALM_NAME);
        Properties props = new Properties();
        props.setProperty("LoginModule.1.REQUIRED","geronimo.security:type=LoginModule,name=TestModule");
        gbean.setAttribute("loginModuleConfiguration", props);
        kernel.loadGBean(testRealm, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.security.remoting.jmx.JaasLoginServiceRemotingServer");
        gbean.setAttribute("bindURI", new URI("tcp://0.0.0.0:4242"));
        gbean.setReferencePattern("loginService", loginService);
        serverStub = new ObjectName("geronimo.remoting:target=JaasLoginServiceRemotingServer");
        kernel.loadGBean(serverStub, gbean);

        kernel.startGBean(loginService);
        kernel.startGBean(testLoginModule);
        kernel.startGBean(testRealm);
        kernel.startGBean(serverStub);
    }

    protected void tearDown() throws Exception {
        kernel.stopGBean(serverStub);
        kernel.stopGBean(testRealm);
        kernel.stopGBean(loginService);

        kernel.unloadGBean(loginService);
        kernel.unloadGBean(testRealm);
        kernel.unloadGBean(testLoginModule);
        kernel.unloadGBean(serverStub);

        kernel.shutdown();
    }

    public static class UsernamePasswordCallback implements CallbackHandler {
        private final String username;
        private final String password;

        public UsernamePasswordCallback(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (int i = 0; i < callbacks.length; i++) {
                if (callbacks[i] instanceof PasswordCallback) {
                    ((PasswordCallback) callbacks[i]).setPassword(password.toCharArray());
                } else if (callbacks[i] instanceof NameCallback) {
                    ((NameCallback) callbacks[i]).setName(username);
                }
            }
        }
    }
}
