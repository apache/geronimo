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
import javax.security.auth.login.LoginContext;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.security.AbstractTest;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.system.serverinfo.ServerInfo;


/**
 * @version $Rev$ $Date$
 */
public class LoginPropertiesFileTest extends AbstractTest {

    protected ObjectName serverInfo;
    protected ObjectName loginConfiguration;
    protected ObjectName clientLM;
    protected ObjectName clientCE;
    protected ObjectName testCE;
    protected ObjectName testRealm;

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
        Set configurations = new HashSet();
        configurations.add(new ObjectName("geronimo.security:type=SecurityRealm,*"));
        configurations.add(new ObjectName("geronimo.security:type=ConfigurationEntry,*"));
        gbean.setReferencePatterns("Configurations", configurations);
        kernel.loadGBean(loginConfiguration, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.security.jaas.LoginModuleGBean");
        clientLM = new ObjectName("geronimo.security:type=LoginModule,name=properties-client");
        gbean.setAttribute("loginModuleClass", "org.apache.geronimo.security.jaas.JaasLoginCoordinator");
        gbean.setAttribute("serverSide", new Boolean(false));
        Properties props = new Properties();
        props.put("host", "localhost");
        props.put("port", "4242");
        props.put("realm", "properties-realm");
        gbean.setAttribute("options", props);
        kernel.loadGBean(clientLM, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.security.jaas.DirectConfigurationEntry");
        clientCE = new ObjectName("geronimo.security:type=ConfigurationEntry,jaasId=properties-client");
        gbean.setAttribute("applicationConfigName", "properties-client");
        gbean.setAttribute("controlFlag", LoginModuleControlFlag.REQUIRED);
        gbean.setReferencePatterns("Module", Collections.singleton(clientLM));
        kernel.loadGBean(clientCE, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.security.jaas.LoginModuleGBean");
        testCE = new ObjectName("geronimo.security:type=LoginModule,name=properties");
        gbean.setAttribute("loginModuleClass", "org.apache.geronimo.security.realm.providers.PropertiesFileLoginModule");
        gbean.setAttribute("serverSide", new Boolean(true));
        props = new Properties();
        props.put("usersURI", new File(new File("."), "src/test-data/data/users.properties").toURI().toString());
        props.put("groupsURI", new File(new File("."), "src/test-data/data/groups.properties").toURI().toString());
        gbean.setAttribute("options", props);
        gbean.setAttribute("loginDomainName", "TestProperties");
        kernel.loadGBean(testCE, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.security.realm.GenericSecurityRealm");
        testRealm = new ObjectName("geronimo.security:type=SecurityRealm,realm=properties-realm");
        gbean.setAttribute("realmName", "properties-realm");
        props = new Properties();
        props.setProperty("LoginModule.1.REQUIRED", "geronimo.security:type=LoginModule,name=properties");
        gbean.setAttribute("loginModuleConfiguration", props);
        gbean.setReferencePatterns("ServerInfo", Collections.singleton(serverInfo));
        kernel.loadGBean(testRealm, gbean);

        kernel.startGBean(loginConfiguration);
        kernel.startGBean(clientLM);
        kernel.startGBean(clientCE);
        kernel.startGBean(testCE);
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

        super.tearDown();
    }

    public void testLogin() throws Exception {

        LoginContext context = new LoginContext("properties-client", new AbstractTest.UsernamePasswordCallback("alan", "starcraft"));

        context.login();
        Subject subject = context.getSubject();

        assertTrue("expected non-null subject", subject != null);
        assertTrue("subject should have one remote principal", subject.getPrincipals(IdentificationPrincipal.class).size() == 1);
        IdentificationPrincipal remote = (IdentificationPrincipal) subject.getPrincipals(IdentificationPrincipal.class).iterator().next();
        assertTrue("subject should be associated with remote id", ContextManager.getRegisteredSubject(remote.getId()) != null);
        assertEquals("subject should have three principals (" + subject.getPrincipals().size() + ")", 3, subject.getPrincipals().size());
        assertEquals("subject should have no realm principals (" + subject.getPrincipals(RealmPrincipal.class).size() + ")", 0, subject.getPrincipals(RealmPrincipal.class).size());

        subject = ContextManager.getServerSideSubject(subject);

        assertTrue("expected non-null subject", subject != null);
        assertTrue("subject should have one remote principal", subject.getPrincipals(IdentificationPrincipal.class).size() == 1);
        remote = (IdentificationPrincipal) subject.getPrincipals(IdentificationPrincipal.class).iterator().next();
        assertTrue("subject should be associated with remote id", ContextManager.getRegisteredSubject(remote.getId()) != null);
        assertEquals("subject should have five principals (" + subject.getPrincipals().size() + ")", 5, subject.getPrincipals().size());
        assertEquals("subject should have two realm principals (" + subject.getPrincipals(RealmPrincipal.class).size() + ")", 2, subject.getPrincipals(RealmPrincipal.class).size());
        RealmPrincipal principal = (RealmPrincipal) subject.getPrincipals(RealmPrincipal.class).iterator().next();
        assertTrue("id of principal should be non-zero", principal.getId() != 0);

        context.logout();

        assertTrue("id of server subject should be null", ContextManager.getSubjectId(subject) == null);
    }
}
