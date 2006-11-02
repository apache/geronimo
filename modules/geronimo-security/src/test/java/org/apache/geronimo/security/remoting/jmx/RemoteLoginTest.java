/**
 *
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

package org.apache.geronimo.security.remoting.jmx;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;

import org.apache.geronimo.testsupport.TestSupport;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.jaas.server.JaasLoginService;
import org.apache.geronimo.security.jaas.LoginModuleGBean;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.security.jaas.GeronimoLoginConfiguration;
import org.apache.geronimo.security.jaas.DirectConfigurationEntry;
import org.apache.geronimo.security.jaas.LoginModuleControlFlag;
import org.apache.geronimo.security.jaas.ConfigurationEntryFactory;
import org.apache.geronimo.security.realm.GenericSecurityRealm;
import org.apache.geronimo.security.realm.SecurityRealm;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;


/**
 * @version $Rev$ $Date$
 */
public class RemoteLoginTest extends TestSupport {
    private File basedir = new File(System.getProperty("basedir"));
    
    Kernel kernel;
    AbstractName serverInfo;
    AbstractName loginService;
    AbstractName loginConfig;
    protected AbstractName testCE;
    protected AbstractName testRealm;
    AbstractName serverStub;


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

        gbean = buildGBeanData("role", "ServerInfo", BasicServerInfo.GBEAN_INFO);
        serverInfo = gbean.getAbstractName();
        gbean.setAttribute("baseDirectory", ".");
        kernel.loadGBean(gbean, ServerInfo.class.getClassLoader());
        kernel.startGBean(serverInfo);

        gbean = buildGBeanData("type", "JaasLoginService", JaasLoginService.getGBeanInfo());
        loginService = gbean.getAbstractName();
        gbean.setReferencePattern("Realms", new AbstractNameQuery(SecurityRealm.class.getName()));
        gbean.setAttribute("algorithm", "HmacSHA1");
        gbean.setAttribute("password", "secret");
        kernel.loadGBean(gbean, JaasLoginService.class.getClassLoader());

        gbean = buildGBeanData("name", "PropertiesLoginModule", LoginModuleGBean.getGBeanInfo());
        testCE = gbean.getAbstractName();
        gbean.setAttribute("loginModuleClass", "org.apache.geronimo.security.realm.providers.PropertiesFileLoginModule");
        gbean.setAttribute("serverSide", Boolean.TRUE);
        gbean.setAttribute("loginDomainName", "secret");
        Properties props = new Properties();
        props.put("usersURI", new File(BASEDIR, "src/test/data/data/users.properties").toURI().toString());
        props.put("groupsURI", new File(BASEDIR, "src/test/data/data/groups.properties").toURI().toString());
        gbean.setAttribute("options", props);
        kernel.loadGBean(gbean, LoginModuleGBean.class.getClassLoader());

        gbean = buildGBeanData("name", "PropertiesLoginModuleUse", JaasLoginModuleUse.getGBeanInfo());
        AbstractName testUseName = gbean.getAbstractName();
        gbean.setAttribute("controlFlag", "REQUIRED");
        gbean.setReferencePattern("LoginModule", testCE);
        kernel.loadGBean(gbean, JaasLoginModuleUse.class.getClassLoader());

        gbean = buildGBeanData("name", "PropertiesSecurityRealm", GenericSecurityRealm.getGBeanInfo());
        testRealm = gbean.getAbstractName();
        gbean.setAttribute("realmName", "properties-realm");
        gbean.setReferencePattern("LoginModuleConfiguration", testUseName);
        gbean.setReferencePattern("ServerInfo", serverInfo);
        kernel.loadGBean(gbean, GenericSecurityRealm.class.getClassLoader());


        gbean = buildGBeanData("target", "JaasLoginServiceRemotingServer", JaasLoginServiceRemotingServer.getGBeanInfo());
        serverStub = gbean.getAbstractName();
        gbean.setAttribute("protocol", "tcp");
        gbean.setAttribute("host", "localhost");
        gbean.setAttribute("port", new Integer(4242));
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
        gbean = buildGBeanData("name", "ClientLoginConfiguration", GeronimoLoginConfiguration.getGBeanInfo());
        loginConfig = gbean.getAbstractName();
        gbean.setReferencePattern("Configurations", new AbstractNameQuery(ConfigurationEntryFactory.class.getName()));
        kernel.loadGBean(gbean, GeronimoLoginConfiguration.class.getClassLoader());
        kernel.startGBean(loginConfig);

        //JaasLoginCoordinator client lm
        gbean = buildGBeanData("name", "JaasLoginCoordinatorLM", LoginModuleGBean.getGBeanInfo());
        AbstractName jlc = gbean.getAbstractName();
        gbean.setAttribute("loginModuleClass", "org.apache.geronimo.security.jaas.client.JaasLoginCoordinator");
        gbean.setAttribute("serverSide", new Boolean(false));
        props = new Properties();
        URI connectURI = (URI) kernel.getAttribute(serverStub, "clientConnectURI");
        props.put("host", connectURI.getHost());
        props.put("port", "" + connectURI.getPort());
        props.put("realm", "properties-realm");

        gbean.setAttribute("options", props);
        kernel.loadGBean(gbean, LoginModuleGBean.class.getClassLoader());
        kernel.startGBean(jlc);

        gbean = buildGBeanData("name", "ClientConfigurationEntry", DirectConfigurationEntry.getGBeanInfo());
        AbstractName dce = gbean.getAbstractName();
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
        kernel.unloadGBean(loginConfig);
        kernel.unloadGBean(serverInfo);

        kernel.shutdown();
    }

    private GBeanData buildGBeanData(String key, String value, GBeanInfo info) throws MalformedObjectNameException {
        AbstractName abstractName = buildAbstractName(key, value, info);
        return new GBeanData(abstractName, info);
    }

    private AbstractName buildAbstractName(String key, String value, GBeanInfo info) throws MalformedObjectNameException {
        Map names = new HashMap();
        names.put(key, value);
        return new AbstractName(new Artifact("test", "foo", "1", "car"), names, new ObjectName("test:" + key + "=" + value));
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
