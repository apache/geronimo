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

import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import javax.management.ObjectName;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import junit.framework.TestCase;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.security.jaas.server.JaasLoginService;
import org.apache.geronimo.security.jaas.LoginModuleGBean;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.security.realm.GenericSecurityRealm;
import org.apache.geronimo.security.remoting.jmx.JaasLoginServiceRemotingServer;


/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractTest extends TestCase {
    protected Kernel kernel;
    protected ObjectName loginService;
    protected ObjectName testLoginModule;
    protected ObjectName testRealm;
    protected ObjectName serverStub;
    private static final String REALM_NAME = "test-realm";

    protected void setUp() throws Exception {
        kernel = KernelFactory.newInstance().createKernel("test.kernel");
        kernel.boot();

        GBeanData gbean;

        // Create all the parts

        loginService = new ObjectName("test:name=TestLoginService");
        gbean = new GBeanData(loginService, JaasLoginService.getGBeanInfo());
        gbean.setReferencePatterns("Realms", Collections.singleton(new ObjectName("geronimo.security:type=SecurityRealm,*")));
//        gbean.setAttribute("reclaimPeriod", new Long(10 * 1000));  // todo check other tests to see if ok
        gbean.setAttribute("algorithm", "HmacSHA1");
        gbean.setAttribute("password", "secret");
        kernel.loadGBean(gbean, JaasLoginService.class.getClassLoader());

        testLoginModule = new ObjectName("geronimo.security:type=LoginModule,name=TestModule");
        gbean = new GBeanData(testLoginModule, LoginModuleGBean.getGBeanInfo());
        gbean.setAttribute("loginModuleClass", "org.apache.geronimo.security.bridge.TestLoginModule");
        gbean.setAttribute("serverSide", new Boolean(true));
        gbean.setAttribute("loginDomainName", "TestLoginDomain");
        kernel.loadGBean(gbean, LoginModuleGBean.class.getClassLoader());

        ObjectName testUseName = new ObjectName("geronimo.security:type=LoginModuleUse,name=TestModule");
        gbean = new GBeanData(testUseName, JaasLoginModuleUse.getGBeanInfo());
        gbean.setAttribute("controlFlag", "REQUIRED");
        gbean.setReferencePattern("LoginModule", testLoginModule);
        kernel.loadGBean(gbean, JaasLoginModuleUse.class.getClassLoader());

        testRealm = new ObjectName("geronimo.security:type=SecurityRealm,realm=" + REALM_NAME);
        gbean = new GBeanData(testRealm, GenericSecurityRealm.getGBeanInfo());
        gbean.setAttribute("realmName", REALM_NAME);
        Properties props = new Properties();
//        props.setProperty("LoginModule.1.REQUIRED","geronimo.security:type=LoginModule,name=TestModule");
//        gbean.setAttribute("loginModuleConfiguration", props);
        gbean.setReferencePattern("LoginModuleConfiguration", testUseName);
        gbean.setReferencePattern("LoginService", loginService);
        kernel.loadGBean(gbean, GenericSecurityRealm.class.getClassLoader());

        serverStub = new ObjectName("geronimo.remoting:target=JaasLoginServiceRemotingServer");
        gbean = new GBeanData(serverStub, JaasLoginServiceRemotingServer.getGBeanInfo());
        gbean.setAttribute("protocol", "tcp");
        gbean.setAttribute("host", "0.0.0.0");
        gbean.setAttribute("port", new Integer(4242));
        gbean.setReferencePattern("LoginService", loginService);
        kernel.loadGBean(gbean, JaasLoginServiceRemotingServer.class.getClassLoader());

        kernel.startGBean(loginService);
        kernel.startGBean(testLoginModule);
        kernel.startGBean(testUseName);
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
