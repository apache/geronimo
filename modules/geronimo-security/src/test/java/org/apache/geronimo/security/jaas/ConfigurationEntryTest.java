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

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.security.AbstractTest;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.realm.GenericSecurityRealm;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.testsupport.TestSupport;


/**
 * @version $Rev$ $Date$
 */
public class ConfigurationEntryTest extends TestSupport {
    
    protected Kernel kernel;
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

        // First try with explicit configuration entry
        LoginContext context = new LoginContext("properties-realm", new AbstractTest.UsernamePasswordCallback("alan", "starcraft"));

        context.login();
        Subject subject = context.getSubject();
        Subject clientSubject = subject;
        assertTrue("expected non-null client subject", subject != null);
        Set set = subject.getPrincipals(IdentificationPrincipal.class);
        assertEquals("client subject should have one ID principal", set.size(), 1);
        IdentificationPrincipal idp = (IdentificationPrincipal) set.iterator().next();
        assertEquals(idp.getId(), idp.getId());
        subject = ContextManager.getRegisteredSubject(idp.getId());

        assertTrue("expected non-null server subject", subject != null);
        assertTrue("server subject should have one remote principal", subject.getPrincipals(IdentificationPrincipal.class).size() == 1);
        IdentificationPrincipal remote = (IdentificationPrincipal) subject.getPrincipals(IdentificationPrincipal.class).iterator().next();
        assertTrue("server subject should be associated with remote id", ContextManager.getRegisteredSubject(remote.getId()) != null);
        assertTrue("server subject should have two realm principals (" + subject.getPrincipals(RealmPrincipal.class).size() + ")", subject.getPrincipals(RealmPrincipal.class).size() == 2);
        assertTrue("server subject should have seven principals (" + subject.getPrincipals().size() + ")", subject.getPrincipals().size() == 7);
        assertTrue("server subject should have one private credential (" + subject.getPrivateCredentials().size() + ")", subject.getPrivateCredentials().size() == 1);

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
        IdentificationPrincipal idp2 = (IdentificationPrincipal) set.iterator().next();
        assertNotSame(idp.getId(), idp2.getId());
        assertEquals(idp2.getId(), idp2.getId());
        subject = ContextManager.getServerSideSubject(subject);

        assertTrue("expected non-null server subject", subject != null);
        assertTrue("server subject should have one remote principal", subject.getPrincipals(IdentificationPrincipal.class).size() == 1);
        remote = (IdentificationPrincipal) subject.getPrincipals(IdentificationPrincipal.class).iterator().next();
        assertTrue("server subject should be associated with remote id", ContextManager.getRegisteredSubject(remote.getId()) != null);
        assertTrue("server subject should have two realm principals (" + subject.getPrincipals(RealmPrincipal.class).size() + ")", subject.getPrincipals(RealmPrincipal.class).size() == 2);
        assertTrue("server subject should have seven principals (" + subject.getPrincipals().size() + ")", subject.getPrincipals().size() == 7);
        assertTrue("server subject should have one private credential (" + subject.getPrivateCredentials().size() + ")", subject.getPrivateCredentials().size() == 1);

        context.logout();

        assertTrue("id of subject should be null", ContextManager.getSubjectId(subject) == null);

        assertTrue("Audit file wasn't written to", auditlog.length() > 0);
    }

    protected void setUp() throws Exception {
        kernel = KernelFactory.newInstance().createKernel("test.kernel");
        kernel.boot();

        GBeanData gbean;

        // Create all the parts

        gbean = buildGBeanData("name", "ServerInfo", BasicServerInfo.GBEAN_INFO);
        serverInfo = gbean.getAbstractName();
        gbean.setAttribute("baseDirectory", ".");
        kernel.loadGBean(gbean, ServerInfo.class.getClassLoader());
        kernel.startGBean(serverInfo);

        gbean = buildGBeanData("new", "LoginConfiguration", GeronimoLoginConfiguration.getGBeanInfo());
        loginConfiguration = gbean.getAbstractName();
        gbean.setReferencePattern("Configurations", new AbstractNameQuery(ConfigurationEntryFactory.class.getName()));
        kernel.loadGBean(gbean, GeronimoLoginConfiguration.class.getClassLoader());

        gbean = buildGBeanData("name", "PropertiesLoginModule", LoginModuleGBean.getGBeanInfo());
        testProperties = gbean.getAbstractName();
        gbean.setAttribute("loginModuleClass", "org.apache.geronimo.security.realm.providers.PropertiesFileLoginModule");
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("usersURI", new File(BASEDIR, "src/test/data/data/users.properties").toURI().toString());
        props.put("groupsURI", new File(BASEDIR, "src/test/data/data/groups.properties").toURI().toString());
        gbean.setAttribute("options", props);
        gbean.setAttribute("loginDomainName", "TestProperties");
        gbean.setAttribute("wrapPrincipals", Boolean.TRUE);
        kernel.loadGBean(gbean, LoginModuleGBean.class.getClassLoader());

        gbean = buildGBeanData("name", "UPCredLoginModule", LoginModuleGBean.getGBeanInfo());
        testUPCred = gbean.getAbstractName();
        gbean.setAttribute("loginModuleClass", "org.apache.geronimo.security.jaas.UPCredentialLoginModule");
        gbean.setAttribute("options", new HashMap<String, Object>());
        kernel.loadGBean(gbean, LoginModuleGBean.class.getClassLoader());

        gbean = buildGBeanData    ("name", "AuditLoginModule", LoginModuleGBean.getGBeanInfo());
        testCE = gbean.getAbstractName();
        gbean.setAttribute("loginModuleClass", "org.apache.geronimo.security.realm.providers.FileAuditLoginModule");
        props = new HashMap<String, Object>();
        props.put("file", new File(BASEDIR, "target/login-audit.log").getPath());
        gbean.setAttribute("options", props);
        kernel.loadGBean(gbean, LoginModuleGBean.class.getClassLoader());

        gbean = buildGBeanData("name", "UPCredLoginModuleUse", JaasLoginModuleUse.getGBeanInfo());
        AbstractName testUseName3 = gbean.getAbstractName();
        gbean.setAttribute("controlFlag", LoginModuleControlFlag.REQUIRED);
        gbean.setReferencePattern("LoginModule", testUPCred);
        kernel.loadGBean(gbean, JaasLoginModuleUse.class.getClassLoader());

        gbean = buildGBeanData("name", "AuditLoginModuleUse", JaasLoginModuleUse.getGBeanInfo());
        AbstractName testUseName2 = gbean.getAbstractName();
        gbean.setAttribute("controlFlag", LoginModuleControlFlag.REQUIRED);
        gbean.setReferencePattern("LoginModule", testCE);
        gbean.setReferencePattern("Next", testUseName3);
        kernel.loadGBean(gbean, JaasLoginModuleUse.class.getClassLoader());

        gbean = buildGBeanData("name", "PropertiesLoginModuleUse", JaasLoginModuleUse.getGBeanInfo());
        AbstractName testUseName1 = gbean.getAbstractName();
        gbean.setAttribute("controlFlag", LoginModuleControlFlag.REQUIRED);
        gbean.setReferencePattern("LoginModule", testProperties);
        gbean.setReferencePattern("Next", testUseName2);
        kernel.loadGBean(gbean, JaasLoginModuleUse.class.getClassLoader());

        gbean = buildGBeanData("name", "PropertiesSecurityRealm", GenericSecurityRealm.getGBeanInfo());
        testRealm = gbean.getAbstractName();
        gbean.setAttribute("realmName", "properties-realm");
        gbean.setReferencePattern("LoginModuleConfiguration", testUseName1);
        gbean.setReferencePattern("ServerInfo", serverInfo);
        kernel.loadGBean(gbean, GenericSecurityRealm.class.getClassLoader());

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

    private GBeanData buildGBeanData(String key, String value, GBeanInfo info) throws MalformedObjectNameException {
          AbstractName abstractName = buildAbstractName(key, value, info);
          return new GBeanData(abstractName, info);
      }

      private AbstractName buildAbstractName(String key, String value, GBeanInfo info) throws MalformedObjectNameException {
          Map names = new HashMap();
          names.put(key, value);
          return new AbstractName(new Artifact("test", "foo", "1", "car"), names, new ObjectName("test:" + key + "=" + value));
      }

}
