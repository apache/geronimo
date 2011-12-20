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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import org.apache.karaf.jaas.boot.ProxyLoginModule;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.osgi.MockBundleContext;
import org.apache.geronimo.security.AbstractTest;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.realm.GenericSecurityRealm;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;


/**
 * @version $Rev$ $Date$
 */
public class ConfigurationEntryTest extends AbstractTest {

//    protected Kernel kernel;
    protected AbstractName serverInfo;
    protected AbstractName loginConfiguration;
    protected AbstractName testUPCred;
    protected AbstractName testCE;         //audit lm
    protected AbstractName testProperties; //properties lm
    protected AbstractName testRealm;

    public void test() throws Exception {
        File auditlog = new File(BASEDIR, "target/login-audit.log");

        if (auditlog.exists()) {
            auditlog.delete();
        }

        assertEquals("Audit file wasn't cleared", 0, auditlog.length());
        ProxyLoginModule.init(bundleContext);
        // First try with explicit configuration entry
        LoginContext context = ContextManager.login("properties-realm", new AbstractTest.UsernamePasswordCallback("alan", "starcraft"));

        Subject subject = context.getSubject();
        assertTrue("expected non-null client subject", subject != null);
        Set set = subject.getPrincipals(IdentificationPrincipal.class);
        assertEquals("client subject should have one ID principal", set.size(), 1);
        IdentificationPrincipal idp = (IdentificationPrincipal) set.iterator().next();
        assertEquals(idp.getId(), idp.getId());
        subject = ContextManager.getRegisteredSubject(idp.getId());

        assertTrue("expected non-null server subject", subject != null);
        assertTrue("server subject should have one remote principal", subject.getPrincipals(IdentificationPrincipal.class).size() == 1);
        IdentificationPrincipal remote = subject.getPrincipals(IdentificationPrincipal.class).iterator().next();
        assertTrue("server subject should be associated with remote id", ContextManager.getRegisteredSubject(remote.getId()) != null);
        assertTrue("server subject should have two realm principals (" + subject.getPrincipals(RealmPrincipal.class).size() + ")", subject.getPrincipals(RealmPrincipal.class).size() == 2);
        assertTrue("server subject should have seven principals (" + subject.getPrincipals().size() + ")", subject.getPrincipals().size() == 7);
        assertTrue("server subject should have one private credential (" + subject.getPrivateCredentials().size() + ")", subject.getPrivateCredentials().size() == 1);

        ContextManager.logout(context);

        assertNull(ContextManager.getRegisteredSubject(idp.getId()));

        assertTrue("id of subject should be null", ContextManager.getSubjectId(subject) == null);

        // next try the automatic configuration entry
        context = ContextManager.login("properties-realm", new AbstractTest.UsernamePasswordCallback("alan", "starcraft"));

        subject = context.getSubject();
        assertTrue("expected non-null client subject", subject != null);
        set = subject.getPrincipals(IdentificationPrincipal.class);
        assertEquals("client subject should have one ID principal", set.size(), 1);
        IdentificationPrincipal idp2 = (IdentificationPrincipal) set.iterator().next();
        assertNotSame(idp.getId(), idp2.getId());
        assertEquals(idp2.getId(), idp2.getId());
        assertTrue("server subject should have one remote principal", subject.getPrincipals(IdentificationPrincipal.class).size() == 1);
        remote = subject.getPrincipals(IdentificationPrincipal.class).iterator().next();
        assertTrue("server subject should be associated with remote id", ContextManager.getRegisteredSubject(remote.getId()) != null);
        assertTrue("server subject should have two realm principals (" + subject.getPrincipals(RealmPrincipal.class).size() + ")", subject.getPrincipals(RealmPrincipal.class).size() == 2);
        assertTrue("server subject should have seven principals (" + subject.getPrincipals().size() + ")", subject.getPrincipals().size() == 7);
        assertTrue("server subject should have one private credential (" + subject.getPrivateCredentials().size() + ")", subject.getPrivateCredentials().size() == 1);

        ContextManager.logout(context);

        assertTrue("id of subject should be null", ContextManager.getSubjectId(subject) == null);

        assertTrue("Audit file wasn't written to", auditlog.length() > 0);
    }

    protected void setUp() throws Exception {
//        super.setUp();
        bundleContext = new MockBundleContext(getClass().getClassLoader(), BASEDIR.getAbsolutePath(), null, null);
        kernel = KernelFactory.newInstance(bundleContext).createKernel("test.kernel");
        kernel.boot(bundleContext);

        GBeanData gbean;

        // Create all the parts

        gbean = buildGBeanData("name", "ServerInfo", BasicServerInfo.class);
        serverInfo = gbean.getAbstractName();
        gbean.setAttribute("baseDirectory", BASEDIR.getAbsolutePath());
        kernel.loadGBean(gbean, bundleContext);
        kernel.startGBean(serverInfo);

        gbean = buildGBeanData("new", "LoginConfiguration", GeronimoLoginConfiguration.class);
        loginConfiguration = gbean.getAbstractName();
        gbean.setReferencePattern("Configurations", new AbstractNameQuery(ConfigurationEntryFactory.class.getName()));
        kernel.loadGBean(gbean, bundleContext);

        gbean = buildGBeanData("name", "PropertiesLoginModule", LoginModuleGBean.class);
        testProperties = gbean.getAbstractName();
        gbean.setAttribute("loginModuleClass", "org.apache.geronimo.security.realm.providers.PropertiesFileLoginModule");
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("usersURI", new File(BASEDIR, "src/test/data/data/users.properties").toURI().toString());
        props.put("groupsURI", new File(BASEDIR, "src/test/data/data/groups.properties").toURI().toString());
        gbean.setAttribute("options", props);
        gbean.setAttribute("loginDomainName", "TestProperties");
        gbean.setAttribute("wrapPrincipals", Boolean.TRUE);
        kernel.loadGBean(gbean, bundleContext);

        gbean = buildGBeanData("name", "GeronimoPasswordCredentialLoginModule", LoginModuleGBean.class);
        testUPCred = gbean.getAbstractName();
        gbean.setAttribute("loginModuleClass", "org.apache.geronimo.security.realm.providers.GeronimoPasswordCredentialLoginModule");
        gbean.setAttribute("options", new HashMap<String, Object>());
        kernel.loadGBean(gbean, bundleContext);

        gbean = buildGBeanData    ("name", "AuditLoginModule", LoginModuleGBean.class);
        testCE = gbean.getAbstractName();
        gbean.setAttribute("loginModuleClass", "org.apache.geronimo.security.realm.providers.FileAuditLoginModule");
        props = new HashMap<String, Object>();
        props.put("file", new File(BASEDIR, "target/login-audit.log").getPath());
        gbean.setAttribute("options", props);
        kernel.loadGBean(gbean, bundleContext);

        gbean = buildGBeanData("name", "GeronimoPasswordCredentialLoginModuleUse", JaasLoginModuleUse.class);
        AbstractName testUseName3 = gbean.getAbstractName();
        gbean.setAttribute("controlFlag", LoginModuleControlFlag.REQUIRED);
        gbean.setReferencePattern("LoginModule", testUPCred);
        kernel.loadGBean(gbean, bundleContext);

        gbean = buildGBeanData("name", "AuditLoginModuleUse", JaasLoginModuleUse.class);
        AbstractName testUseName2 = gbean.getAbstractName();
        gbean.setAttribute("controlFlag", LoginModuleControlFlag.REQUIRED);
        gbean.setReferencePattern("LoginModule", testCE);
        gbean.setReferencePattern("Next", testUseName3);
        kernel.loadGBean(gbean, bundleContext);

        gbean = buildGBeanData("name", "PropertiesLoginModuleUse", JaasLoginModuleUse.class);
        AbstractName testUseName1 = gbean.getAbstractName();
        gbean.setAttribute("controlFlag", LoginModuleControlFlag.REQUIRED);
        gbean.setReferencePattern("LoginModule", testProperties);
        gbean.setReferencePattern("Next", testUseName2);
        kernel.loadGBean(gbean, bundleContext);

        gbean = buildGBeanData("name", "PropertiesSecurityRealm", GenericSecurityRealm.class);
        testRealm = gbean.getAbstractName();
        gbean.setAttribute("realmName", "properties-realm");
        gbean.setReferencePattern("LoginModuleConfiguration", testUseName1);
        gbean.setReferencePattern("ServerInfo", serverInfo);
        gbean.setAttribute("global", Boolean.TRUE);
        kernel.loadGBean(gbean, bundleContext);

        kernel.startGBean(loginConfiguration);
        kernel.startGBean(testCE);
        kernel.startGBean(testProperties);
        kernel.startGBean(testUPCred);
        kernel.startGBean(testUseName3);
        kernel.startGBean(testUseName2);
        kernel.startGBean(testUseName1);
        kernel.startGBean(testRealm);
    }

    protected void tearDown() throws Exception {
        kernel.stopGBean(testRealm);
        kernel.stopGBean(testUPCred);
        kernel.stopGBean(testCE);
        kernel.stopGBean(loginConfiguration);
        kernel.stopGBean(serverInfo);

        kernel.unloadGBean(testCE);
        kernel.unloadGBean(testUPCred);
        kernel.unloadGBean(testRealm);
        kernel.unloadGBean(loginConfiguration);
        kernel.unloadGBean(serverInfo);

        kernel.shutdown();
    }

}
