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

package org.apache.geronimo.security.jaas;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import javax.management.ObjectName;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import junit.framework.TestCase;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.security.AbstractTest;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.realm.GenericSecurityRealm;
import org.apache.geronimo.security.remoting.jmx.JaasLoginServiceRemotingServer;
import org.apache.geronimo.system.serverinfo.ServerInfo;


/**
 * @version $Rev$ $Date$
 */
public class ConfigurationEntryTest extends TestCase {

    protected Kernel kernel;
    protected ObjectName serverInfo;
    protected ObjectName loginConfiguration;
    protected ObjectName loginService;
    protected ObjectName clientCE;
    protected ObjectName testUPCred;
    protected ObjectName testCE;
    protected ObjectName testRealm;
    protected ObjectName serverStub;

    public void test() throws Exception {
        File log = new File("target/login-audit.log");
        if(log.exists()) {
            log.delete();
        }
        assertEquals("Audit file wasn't cleared", 0, log.length());


        // First try with explicit configuration entry
        LoginContext context = new LoginContext("properties-client", new AbstractTest.UsernamePasswordCallback("alan", "starcraft"));

        context.login();
        Subject subject = context.getSubject();
        Subject clientSubject = subject;
        assertTrue("expected non-null client subject", subject != null);
        Set set = subject.getPrincipals(IdentificationPrincipal.class);
        assertEquals("client subject should have one ID principal", set.size(), 1);
        IdentificationPrincipal idp = (IdentificationPrincipal)set.iterator().next();
        assertEquals(idp.getId(), idp.getId());
        subject = ContextManager.getRegisteredSubject(idp.getId());

        assertTrue("expected non-null server subject", subject != null);
        assertTrue("server subject should have one remote principal", subject.getPrincipals(IdentificationPrincipal.class).size() == 1);
        IdentificationPrincipal remote = (IdentificationPrincipal) subject.getPrincipals(IdentificationPrincipal.class).iterator().next();
        assertTrue("server subject should be associated with remote id", ContextManager.getRegisteredSubject(remote.getId()) != null);
        assertTrue("server subject should have two realm principals ("+subject.getPrincipals(RealmPrincipal.class).size()+")", subject.getPrincipals(RealmPrincipal.class).size() == 2);
        assertTrue("server subject should have five principals ("+subject.getPrincipals().size()+")", subject.getPrincipals().size() == 5);
        assertTrue("server subject should have one private credential ("+subject.getPrivateCredentials().size()+")", subject.getPrivateCredentials().size() == 1);
        RealmPrincipal principal = (RealmPrincipal) subject.getPrincipals(RealmPrincipal.class).iterator().next();
        assertTrue("id of principal should be non-zero", principal.getId() != 0);

        context.logout();

        assertNull(ContextManager.getRegisteredSubject(idp.getId()));
        assertNull(ContextManager.getServerSideSubject(clientSubject));

        assertTrue("id of subject should be null", ContextManager.getSubjectId(subject) == null);

        // next try the automatic configuration entry
        context = new LoginContext("properties-realm", new AbstractTest.UsernamePasswordCallback("alan", "starcraft"));

        context.login();
        subject = context.getSubject();
        assertTrue("expected non-null client subject", subject != null);
        set = subject.getPrincipals(IdentificationPrincipal.class);
        assertEquals("client subject should have one ID principal", set.size(), 1);
        IdentificationPrincipal idp2 = (IdentificationPrincipal)set.iterator().next();
        assertNotSame(idp.getId(), idp2.getId());
        assertEquals(idp2.getId(), idp2.getId());
        subject = ContextManager.getServerSideSubject(subject);

        assertTrue("expected non-null server subject", subject != null);
        assertTrue("server subject should have one remote principal", subject.getPrincipals(IdentificationPrincipal.class).size() == 1);
        remote = (IdentificationPrincipal) subject.getPrincipals(IdentificationPrincipal.class).iterator().next();
        assertTrue("server subject should be associated with remote id", ContextManager.getRegisteredSubject(remote.getId()) != null);
        assertTrue("server subject should have two realm principals ("+subject.getPrincipals(RealmPrincipal.class).size()+")", subject.getPrincipals(RealmPrincipal.class).size() == 2);
        assertTrue("server subject should have five principals ("+subject.getPrincipals().size()+")", subject.getPrincipals().size() == 5);
        assertTrue("server subject should have one private credential ("+subject.getPrivateCredentials().size()+")", subject.getPrivateCredentials().size() == 1);
        principal = (RealmPrincipal) subject.getPrincipals(RealmPrincipal.class).iterator().next();
        assertTrue("id of principal should be non-zero", principal.getId() != 0);

        context.logout();

        assertTrue("id of subject should be null", ContextManager.getSubjectId(subject) == null);

        assertTrue("Audit file wasn't written to", log.length() > 0);
    }

    protected void setUp() throws Exception {
        kernel = new Kernel("test.kernel");
        kernel.boot();

        GBeanData gbean;

        // Create all the parts

        serverInfo = new ObjectName("geronimo.system:role=ServerInfo");
        gbean = new GBeanData(serverInfo, ServerInfo.GBEAN_INFO);
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

        loginService = JaasLoginService.OBJECT_NAME;
        gbean = new GBeanData(loginService, JaasLoginService.getGBeanInfo());
        gbean.setReferencePatterns("Realms", Collections.singleton(new ObjectName("geronimo.security:type=SecurityRealm,*")));
//        gbean.setAttribute("reclaimPeriod", new Long(100));
        gbean.setAttribute("algorithm", "HmacSHA1");
        gbean.setAttribute("password", "secret");
        kernel.loadGBean(gbean, JaasLoginService.class.getClassLoader());

        clientCE = new ObjectName("geronimo.security:type=ConfigurationEntry,jaasId=properties-client");
        gbean = new GBeanData(clientCE, ServerRealmConfigurationEntry.getGBeanInfo());
        gbean.setAttribute("applicationConfigName", "properties-client");
        gbean.setAttribute("realmName", "properties-realm");
        kernel.loadGBean(gbean, ServerRealmConfigurationEntry.class.getClassLoader());

        testCE = new ObjectName("geronimo.security:type=LoginModule,name=properties");
        gbean = new GBeanData(testCE, LoginModuleGBean.getGBeanInfo());
        gbean.setAttribute("loginModuleClass", "org.apache.geronimo.security.realm.providers.PropertiesFileLoginModule");
        gbean.setAttribute("serverSide", new Boolean(true));
        Properties props = new Properties();
        props.put("usersURI", new File(new File("."), "src/test-data/data/users.properties").toURI().toString());
        props.put("groupsURI", new File(new File("."), "src/test-data/data/groups.properties").toURI().toString());
        gbean.setAttribute("options", props);
        gbean.setAttribute("loginDomainName", "TestProperties");
        kernel.loadGBean(gbean, LoginModuleGBean.class.getClassLoader());

        testUPCred = new ObjectName("geronimo.security:type=LoginModule,name=UPCred");
        gbean = new GBeanData(testUPCred, LoginModuleGBean.getGBeanInfo());
        gbean.setAttribute("loginModuleClass", "org.apache.geronimo.security.jaas.UPCredentialLoginModule");
        gbean.setAttribute("serverSide", new Boolean(true));
        gbean.setAttribute("options", new Properties());
        kernel.loadGBean(gbean, LoginModuleGBean.class.getClassLoader());

        testCE = new ObjectName("geronimo.security:type=LoginModule,name=audit");
        gbean = new GBeanData(testCE, LoginModuleGBean.getGBeanInfo());
        gbean.setAttribute("loginModuleClass", "org.apache.geronimo.security.realm.providers.FileAuditLoginModule");
        gbean.setAttribute("serverSide", new Boolean(true));
        props = new Properties();
        props.put("file", "target/login-audit.log");
        gbean.setAttribute("options", props);
        kernel.loadGBean(gbean, LoginModuleGBean.class.getClassLoader());

        testRealm = new ObjectName("geronimo.security:type=SecurityRealm,realm=properties-realm");
        gbean = new GBeanData(testRealm, GenericSecurityRealm.getGBeanInfo());
        gbean.setAttribute("realmName", "properties-realm");
        props = new Properties();
        props.setProperty("LoginModule.3.REQUIRED","geronimo.security:type=LoginModule,name=UPCred");
        props.setProperty("LoginModule.2.REQUIRED","geronimo.security:type=LoginModule,name=audit");
        props.setProperty("LoginModule.1.REQUIRED","geronimo.security:type=LoginModule,name=properties");
        gbean.setAttribute("loginModuleConfiguration", props);
        gbean.setReferencePatterns("ServerInfo", Collections.singleton(serverInfo));
        kernel.loadGBean(gbean, GenericSecurityRealm.class.getClassLoader());

        serverStub = new ObjectName("geronimo.remoting:target=JaasLoginServiceRemotingServer");
        gbean = new GBeanData(serverStub, JaasLoginServiceRemotingServer.getGBeanInfo());
        gbean.setAttribute("bindURI", new URI("tcp://0.0.0.0:4242"));
        gbean.setReferencePattern("LoginService", loginService);
        kernel.loadGBean(gbean, JaasLoginServiceRemotingServer.class.getClassLoader());               

        kernel.startGBean(loginConfiguration);
        kernel.startGBean(loginService);
        kernel.startGBean(clientCE);
        kernel.startGBean(testCE);
        kernel.startGBean(testUPCred);
        kernel.startGBean(testRealm);
        kernel.startGBean(serverStub);
    }

    protected void tearDown() throws Exception {
        kernel.stopGBean(serverStub);
        kernel.stopGBean(testRealm);
        kernel.stopGBean(testUPCred);
        kernel.stopGBean(testCE);
        kernel.stopGBean(clientCE);
        kernel.stopGBean(loginService);
        kernel.stopGBean(loginConfiguration);
        kernel.stopGBean(serverInfo);

        kernel.unloadGBean(loginService);
        kernel.unloadGBean(testCE);
        kernel.unloadGBean(testUPCred);
        kernel.unloadGBean(testRealm);
        kernel.unloadGBean(clientCE);
        kernel.unloadGBean(serverStub);
        kernel.unloadGBean(loginConfiguration);
        kernel.unloadGBean(serverInfo);

        kernel.shutdown();
    }
}
