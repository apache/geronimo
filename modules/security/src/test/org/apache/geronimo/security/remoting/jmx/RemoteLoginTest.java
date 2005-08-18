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

package org.apache.geronimo.security.remoting.jmx;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Properties;
import javax.management.ObjectName;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;

import junit.framework.TestCase;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.jaas.JaasLoginService;
import org.apache.geronimo.security.jaas.LoginModuleGBean;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.security.jaas.GeronimoLoginConfiguration;
import org.apache.geronimo.security.jaas.DirectConfigurationEntry;
import org.apache.geronimo.security.jaas.LoginModuleControlFlag;
import org.apache.geronimo.security.realm.GenericSecurityRealm;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;


/**
 * @version $Rev$ $Date$
 */
public class RemoteLoginTest extends TestCase {
    Kernel kernel;
    ObjectName serverInfo;
    ObjectName loginService;
    protected ObjectName testCE;
    protected ObjectName testRealm;
    ObjectName serverStub;
//    JaasLoginServiceMBean asyncRemoteProxy;
//    JaasLoginServiceMBean saslRemoteProxy;
//    JaasLoginServiceMBean gssapiRemoteProxy;


    public void testLogin() throws Exception {
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            LoginContext context = new LoginContext("FOO", new UsernamePasswordCallback("alan", "starcraft"));

            context.login();
            Subject subject = context.getSubject();

            assertTrue("expected non-null subject", subject != null);
            assertTrue("subject should have one remote principal", subject.getPrincipals(IdentificationPrincipal.class).size() == 1);
            IdentificationPrincipal principal = (IdentificationPrincipal) subject.getPrincipals(IdentificationPrincipal.class).iterator().next();
            assertTrue("id of principal should be non-zero", principal.getId().getSubjectId().longValue() != 0);
            assertEquals("subject should have three principals", 3, subject.getPrincipals().size());
            assertEquals("subject should have no realm principal", 0, subject.getPrincipals(RealmPrincipal.class).size());

            context.logout();
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    public void setUp() throws Exception {
        kernel = KernelFactory.newInstance().createKernel("test.kernel");
        kernel.boot();

        GBeanData gbean;

        // Create all the parts

        serverInfo = new ObjectName("geronimo.system:role=ServerInfo");
        gbean = new GBeanData(serverInfo, BasicServerInfo.GBEAN_INFO);
        gbean.setAttribute("baseDirectory", ".");
        kernel.loadGBean(gbean, ServerInfo.class.getClassLoader());
        kernel.startGBean(serverInfo);

        loginService = new ObjectName("geronimo.security:type=JaasLoginService");
        gbean = new GBeanData(loginService, JaasLoginService.getGBeanInfo());
        gbean.setReferencePatterns("Realms", Collections.singleton(new ObjectName("geronimo.security:type=SecurityRealm,*")));
        gbean.setAttribute("algorithm", "HmacSHA1");
        gbean.setAttribute("password", "secret");
        kernel.loadGBean(gbean, JaasLoginService.class.getClassLoader());

        testCE = new ObjectName("geronimo.security:type=LoginModule,name=properties");
        gbean = new GBeanData(testCE, LoginModuleGBean.getGBeanInfo());
        gbean.setAttribute("loginModuleClass", "org.apache.geronimo.security.realm.providers.PropertiesFileLoginModule");
        gbean.setAttribute("serverSide", new Boolean(true));
        gbean.setAttribute("loginDomainName", "secret");
        Properties props = new Properties();
        props.put("usersURI", new File(new File("."), "src/test-data/data/users.properties").toURI().toString());
        props.put("groupsURI", new File(new File("."), "src/test-data/data/groups.properties").toURI().toString());
        gbean.setAttribute("options", props);
        kernel.loadGBean(gbean, LoginModuleGBean.class.getClassLoader());

        ObjectName testUseName = new ObjectName("geronimo.security:type=LoginModuleUse,name=properties");
        gbean = new GBeanData(testUseName, JaasLoginModuleUse.getGBeanInfo());
        gbean.setAttribute("controlFlag", "REQUIRED");
        gbean.setReferencePattern("LoginModule", testCE);
        kernel.loadGBean(gbean, JaasLoginModuleUse.class.getClassLoader());

        testRealm = new ObjectName("geronimo.security:type=SecurityRealm,realm=properties-realm");
        gbean = new GBeanData(testRealm, GenericSecurityRealm.getGBeanInfo());
        gbean.setAttribute("realmName", "properties-realm");
        gbean.setReferencePattern("LoginModuleConfiguration", testUseName);
        gbean.setReferencePatterns("ServerInfo", Collections.singleton(serverInfo));
        kernel.loadGBean(gbean, GenericSecurityRealm.class.getClassLoader());


        serverStub = new ObjectName("geronimo.remoting:target=JaasLoginServiceRemotingServer");
        gbean = new GBeanData(serverStub, JaasLoginServiceRemotingServer.getGBeanInfo());
        gbean.setAttribute("bindURI", new URI("tcp://0.0.0.0:4242"));
        gbean.setReferencePattern("LoginService", loginService);
        kernel.loadGBean(gbean, JaasLoginServiceRemotingServer.class.getClassLoader());

        kernel.startGBean(loginService);
        kernel.startGBean(testCE);
        kernel.startGBean(testUseName);
        kernel.startGBean(testRealm);
        try {
            kernel.startGBean(serverStub);
        } catch (Throwable t) {
            tearDown();
            throw new RuntimeException(t);
        }

        //set up "Client side" in the same kernel
        ObjectName glc = new ObjectName("geronimo.client:name=GeronimoLoginConfiguration");
        gbean = new GBeanData(glc, GeronimoLoginConfiguration.getGBeanInfo());
        gbean.setReferencePattern("Configurations", new ObjectName("geronimo.security:type=ConfigurationEntry,*"));
        kernel.loadGBean(gbean, GeronimoLoginConfiguration.class.getClassLoader());
        kernel.startGBean(glc);

        //JaasLoginCoordinator client lm
        ObjectName jlc = new ObjectName("geronimo.security:type=JaasLoginCoordinatorLM");
        gbean = new GBeanData(jlc, LoginModuleGBean.getGBeanInfo());
        gbean.setAttribute("loginModuleClass", "org.apache.geronimo.security.jaas.JaasLoginCoordinator");
        gbean.setAttribute("serverSide", new Boolean(false));
        props = new Properties();
        URI connectURI = (URI) kernel.getAttribute(serverStub, "clientConnectURI");
        props.put("host", connectURI.getHost());
        props.put("port", "" + connectURI.getPort());
        props.put("realm", "properties-realm");

        gbean.setAttribute("options", props);
        kernel.loadGBean(gbean, LoginModuleGBean.class.getClassLoader());
        kernel.startGBean(jlc);

        ObjectName dce = new ObjectName("geronimo.security:type=ConfigurationEntry,name=client");
        gbean = new GBeanData(dce, DirectConfigurationEntry.getGBeanInfo());
        gbean.setAttribute("applicationConfigName", "FOO");
        gbean.setAttribute("controlFlag", LoginModuleControlFlag.REQUIRED);
        gbean.setReferencePattern("Module", jlc);
        kernel.loadGBean(gbean, DirectConfigurationEntry.class.getClassLoader());
        kernel.startGBean(dce);

//        connectURI = (URI) kernel.getAttribute(serverStub, "clientConnectURI");
//        asyncRemoteProxy = JaasLoginServiceRemotingClient.create(connectURI.getHost(), connectURI.getPort());
//
//        connectURI = (URI) kernel.getAttribute(serverStub, "clientConnectURI");
//        saslRemoteProxy = JaasLoginServiceRemotingClient.create(connectURI.getHost(), connectURI.getPort());
//
//        connectURI = (URI) kernel.getAttribute(serverStub, "clientConnectURI");
//        gssapiRemoteProxy = JaasLoginServiceRemotingClient.create(connectURI.getHost(), connectURI.getPort());
    }

    protected void tearDown() throws Exception {
        kernel.stopGBean(serverStub);
        kernel.stopGBean(testRealm);
        kernel.stopGBean(testCE);
        kernel.stopGBean(loginService);
        kernel.stopGBean(serverInfo);

        kernel.unloadGBean(loginService);
        kernel.unloadGBean(testCE);
        kernel.unloadGBean(testRealm);
        kernel.unloadGBean(serverStub);
        kernel.unloadGBean(serverInfo);

        kernel.shutdown();
    }

    class UsernamePasswordCallback implements CallbackHandler {
        private final String username;
        private final String password;

        UsernamePasswordCallback(String username, String password) {
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
