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

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import javax.management.ObjectName;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.security.AbstractTest;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.DomainPrincipal;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.jaas.server.JaasLoginService;
import org.apache.geronimo.security.realm.GenericSecurityRealm;
import org.apache.geronimo.security.remoting.jmx.JaasLoginServiceRemotingServer;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;
import org.apache.geronimo.system.serverinfo.ServerInfo;


/**
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class TimeoutTest extends AbstractTest {

    protected ObjectName serverInfo;
    protected ObjectName loginConfiguration;
    protected ObjectName testCE;
    protected ObjectName testRealm;
    protected ObjectName clientLM;
    protected ObjectName clientCE;

    public void setUp() throws Exception {
        kernel = KernelFactory.newInstance().createKernel("test.kernel");
        kernel.boot();

        GBeanData gbean;

        // Create all the parts

        loginService = new ObjectName("geronimo.security:type=JaasLoginService");
        gbean = new GBeanData(loginService, JaasLoginService.getGBeanInfo());
        gbean.setReferencePatterns("Realms", Collections.singleton(new ObjectName("geronimo.security:type=SecurityRealm,*")));
        gbean.setAttribute("expiredLoginScanIntervalMillis", new Integer(50));
        gbean.setAttribute("maxLoginDurationMillis", new Integer(5000));
        gbean.setAttribute("algorithm", "HmacSHA1");
        gbean.setAttribute("password", "secret");
        kernel.loadGBean(gbean, JaasLoginService.class.getClassLoader());


        serverStub = new ObjectName("geronimo.remoting:target=JaasLoginServiceRemotingServer");
        gbean = new GBeanData(serverStub, JaasLoginServiceRemotingServer.getGBeanInfo());
        gbean.setAttribute("protocol", "tcp");
        gbean.setAttribute("host", "0.0.0.0");
        gbean.setAttribute("port", new Integer(4242));
        gbean.setReferencePattern("LoginService", loginService);
        kernel.loadGBean(gbean, JaasLoginServiceRemotingServer.class.getClassLoader());

        kernel.startGBean(loginService);
        kernel.startGBean(serverStub);

        serverInfo = new ObjectName("geronimo.system:role=ServerInfo");
        gbean = new GBeanData(serverInfo, BasicServerInfo.GBEAN_INFO);
        gbean.setAttribute("baseDirectory", ".");
        kernel.loadGBean(gbean, ServerInfo.class.getClassLoader());
        kernel.startGBean(serverInfo);

        loginConfiguration = new ObjectName("geronimo.security:type=LoginConfiguration");
        gbean = new GBeanData(loginConfiguration, GeronimoLoginConfiguration.getGBeanInfo());
        Set configurations = new HashSet();
        configurations.add(new ObjectName("geronimo.security:type=SecurityRealm,*"));
        configurations.add(new ObjectName("geronimo.security:type=ConfigurationEntry,*"));
        gbean.setReferencePatterns("Configurations", configurations);
        kernel.loadGBean(gbean, GeronimoLoginConfiguration.class.getClassLoader());

        testCE = new ObjectName("geronimo.security:type=LoginModule,name=properties");
        gbean = new GBeanData(testCE, LoginModuleGBean.getGBeanInfo());
        gbean.setAttribute("loginModuleClass", "org.apache.geronimo.security.realm.providers.PropertiesFileLoginModule");
        gbean.setAttribute("serverSide", new Boolean(true));
        Properties props = new Properties();
        props.put("usersURI", new File(new File("."), "src/test-data/data/users.properties").toURI().toString());
        props.put("groupsURI", new File(new File("."), "src/test-data/data/groups.properties").toURI().toString());
        gbean.setAttribute("options", props);
        gbean.setAttribute("loginDomainName", "PropertiesDomain");
        gbean.setAttribute("wrapPrincipals", Boolean.TRUE);
        kernel.loadGBean(gbean, LoginModuleGBean.class.getClassLoader());

        ObjectName testUseName = new ObjectName("geronimo.security:type=LoginModuleUse,name=properties");
        gbean = new GBeanData(testUseName, JaasLoginModuleUse.getGBeanInfo());
        gbean.setAttribute("controlFlag", "REQUIRED");
        gbean.setReferencePattern("LoginModule", testCE);
        kernel.loadGBean(gbean, JaasLoginModuleUse.class.getClassLoader());

        testRealm = new ObjectName("geronimo.security:type=SecurityRealm,realm=properties-realm");
        gbean = new GBeanData(testRealm, GenericSecurityRealm.getGBeanInfo());
        gbean.setAttribute("realmName", "properties-realm");
//        props = new Properties();
//        props.setProperty("LoginModule.1.REQUIRED","geronimo.security:type=LoginModule,name=properties");
//        gbean.setAttribute("loginModuleConfiguration", props);
        gbean.setReferencePattern("LoginModuleConfiguration", testUseName);
        gbean.setReferencePatterns("ServerInfo", Collections.singleton(serverInfo));
        gbean.setReferencePattern("LoginService", loginService);
        kernel.loadGBean(gbean, GenericSecurityRealm.class.getClassLoader());

        clientLM = new ObjectName("geronimo.security:type=LoginModule,name=properties-client");
        gbean = new GBeanData(clientLM, LoginModuleGBean.getGBeanInfo());
        gbean.setAttribute("loginModuleClass", "org.apache.geronimo.security.jaas.client.JaasLoginCoordinator");
        gbean.setAttribute("serverSide", new Boolean(false));
        props = new Properties();
        props.put("host", "localhost");
        props.put("port", "4242");
        props.put("realm", "properties-realm");
        gbean.setAttribute("options", props);
        kernel.loadGBean(gbean, LoginModuleGBean.class.getClassLoader());

        clientCE = new ObjectName("geronimo.security:type=ConfigurationEntry,jaasId=properties-client");
        gbean = new GBeanData(clientCE, DirectConfigurationEntry.getGBeanInfo());
        gbean.setAttribute("applicationConfigName", "properties-client");
        gbean.setAttribute("controlFlag", LoginModuleControlFlag.REQUIRED);
        gbean.setReferencePatterns("Module", Collections.singleton(clientLM));
        kernel.loadGBean(gbean, DirectConfigurationEntry.class.getClassLoader());

        kernel.startGBean(loginConfiguration);
        kernel.startGBean(clientLM);
        kernel.startGBean(clientCE);
        kernel.startGBean(testCE);
        kernel.startGBean(testUseName);
        kernel.startGBean(testRealm);
    }

    public void tearDown() throws Exception {
        kernel.stopGBean(testRealm);
        kernel.stopGBean(testCE);
        kernel.stopGBean(clientCE);
        kernel.stopGBean(clientLM);
        kernel.stopGBean(loginConfiguration);
        kernel.stopGBean(serverInfo);

        kernel.unloadGBean(testCE);
        kernel.unloadGBean(testRealm);
        kernel.unloadGBean(clientCE);
        kernel.unloadGBean(clientLM);
        kernel.unloadGBean(loginConfiguration);
        kernel.unloadGBean(serverInfo);

        kernel.stopGBean(serverStub);
        kernel.stopGBean(loginService);

        kernel.unloadGBean(loginService);
        kernel.unloadGBean(serverStub);

        kernel.shutdown();
    }

    public void testNothing() {
    }

    public void testTimeout() throws Exception {

        LoginContext context = new LoginContext("properties-client", new AbstractTest.UsernamePasswordCallback("alan", "starcraft"));

        context.login();
        Subject subject = context.getSubject();
        assertTrue("expected non-null client subject", subject != null);
        Set set = subject.getPrincipals(IdentificationPrincipal.class);
        assertEquals("client subject should have one ID principal", set.size(), 1);
        IdentificationPrincipal idp = (IdentificationPrincipal) set.iterator().next();
        subject = ContextManager.getRegisteredSubject(idp.getId());

        assertTrue("expected non-null server subject", subject != null);
        assertTrue("server subject should have one remote principal", subject.getPrincipals(IdentificationPrincipal.class).size() == 1);
        IdentificationPrincipal remote = (IdentificationPrincipal) subject.getPrincipals(IdentificationPrincipal.class).iterator().next();
        assertTrue("server subject should be associated with remote id", ContextManager.getRegisteredSubject(remote.getId()) != null);
        assertEquals("server-side subject should have seven principal", 7, subject.getPrincipals().size());
        assertTrue("server subject should have two realm principal", subject.getPrincipals(RealmPrincipal.class).size() == 2);
        assertTrue("server subject should have two domain principal", subject.getPrincipals(DomainPrincipal.class).size() == 2);

        assertTrue("id of server subject should be non-null", ContextManager.getSubjectId(subject) != null);

        Thread.sleep(3000); // wait for timeout to kick in

        assertTrue("id of server subject should be non-null", ContextManager.getSubjectId(subject) != null);

        Thread.sleep(7000); // wait for timeout to kick in

        assertTrue("id of server subject should be null", ContextManager.getSubjectId(subject) == null);
    }
}
