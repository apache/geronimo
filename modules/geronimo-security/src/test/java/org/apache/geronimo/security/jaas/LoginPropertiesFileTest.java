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

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.security.AbstractTest;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.DomainPrincipal;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.realm.GenericSecurityRealm;


/**
 * @version $Rev$ $Date$
 */
public class LoginPropertiesFileTest extends AbstractTest {
    protected AbstractName clientCE;
    protected AbstractName testCE;
    protected AbstractName testRealm;

    public void setUp() throws Exception {
        needServerInfo = true;
        needLoginConfiguration = true;
        super.setUp();

        GBeanData gbean;

        gbean = buildGBeanData("name", "PropertiesLoginModule", LoginModuleGBean.getGBeanInfo());
        testCE = gbean.getAbstractName();
        gbean.setAttribute("loginModuleClass", "org.apache.geronimo.security.realm.providers.PropertiesFileLoginModule");
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("usersURI", new File(BASEDIR, "src/test/data/data/users.properties").toURI().toString());
        props.put("groupsURI", new File(BASEDIR, "src/test/data/data/groups.properties").toURI().toString());
        gbean.setAttribute("options", props);
        gbean.setAttribute("loginDomainName", "TestProperties");
        gbean.setAttribute("wrapPrincipals", Boolean.TRUE);
        kernel.loadGBean(gbean, LoginModuleGBean.class.getClassLoader());

        gbean = buildGBeanData("name", "PropertiesLoginModuleUse", JaasLoginModuleUse.getGBeanInfo());
        AbstractName testUseName = gbean.getAbstractName();
        gbean.setAttribute("controlFlag", LoginModuleControlFlag.REQUIRED);
        gbean.setReferencePattern("LoginModule", testCE);
        kernel.loadGBean(gbean, JaasLoginModuleUse.class.getClassLoader());

        gbean = buildGBeanData("name", "PropertiesSecurityRealm", GenericSecurityRealm.getGBeanInfo());
        testRealm = gbean.getAbstractName();
        gbean.setAttribute("realmName", "properties-realm");
        gbean.setAttribute("wrapPrincipals", Boolean.TRUE);
        gbean.setReferencePattern("LoginModuleConfiguration", testUseName);
        gbean.setReferencePattern("ServerInfo", serverInfo);
        kernel.loadGBean(gbean, GenericSecurityRealm.class.getClassLoader());

        kernel.startGBean(loginConfiguration);
        kernel.startGBean(testCE);
        kernel.startGBean(testUseName);
        kernel.startGBean(testRealm);
    }

    public void tearDown() throws Exception {
        kernel.stopGBean(testRealm);
        kernel.stopGBean(testCE);
        kernel.stopGBean(loginConfiguration);
        kernel.stopGBean(serverInfo);

        kernel.unloadGBean(testCE);
        kernel.unloadGBean(testRealm);
        kernel.unloadGBean(loginConfiguration);
        kernel.unloadGBean(serverInfo);

        super.tearDown();
    }

    public void testLogin() throws Exception {

        LoginContext context = new LoginContext("properties-realm", new AbstractTest.UsernamePasswordCallback("alan", "starcraft"));

        context.login();
        Subject subject = context.getSubject();

        assertTrue("expected non-null subject", subject != null);
        assertTrue("subject should have one remote principal", subject.getPrincipals(IdentificationPrincipal.class).size() == 1);
        IdentificationPrincipal remote = subject.getPrincipals(IdentificationPrincipal.class).iterator().next();
        assertTrue("subject should be associated with remote id", ContextManager.getRegisteredSubject(remote.getId()) != null);
        assertEquals("subject should have seven principals (" + subject.getPrincipals().size() + ")", 7, subject.getPrincipals().size());
        assertEquals("subject should have 2 realm principals (" + subject.getPrincipals(RealmPrincipal.class).size() + ")", 2, subject.getPrincipals(RealmPrincipal.class).size());
        assertEquals("subject should have 2 domain principals (" + subject.getPrincipals(DomainPrincipal.class).size() + ")", 2, subject.getPrincipals(DomainPrincipal.class).size());

        subject = ContextManager.getServerSideSubject(subject);

        assertTrue("expected non-null subject", subject != null);
        assertTrue("subject should have one remote principal", subject.getPrincipals(IdentificationPrincipal.class).size() == 1);
        remote = subject.getPrincipals(IdentificationPrincipal.class).iterator().next();
        assertTrue("subject should be associated with remote id", ContextManager.getRegisteredSubject(remote.getId()) != null);
        assertEquals("subject should have seven principals (" + subject.getPrincipals().size() + ")", 7, subject.getPrincipals().size());
        assertEquals("subject should have 2 realm principals (" + subject.getPrincipals(RealmPrincipal.class).size() + ")", 2, subject.getPrincipals(RealmPrincipal.class).size());
        assertEquals("subject should have 2 domain principals (" + subject.getPrincipals(DomainPrincipal.class).size() + ")", 2, subject.getPrincipals(DomainPrincipal.class).size());

        context.logout();

        assertTrue("id of server subject should be null", ContextManager.getSubjectId(subject) == null);
    }

    public void testNullUserLogin() throws Exception {
        LoginContext context = new LoginContext("properties-realm", new UsernamePasswordCallback(null, "starcraft"));

        try {
            context.login();
            fail("Should not allow this login with null username");
        } catch (LoginException e) {
        }
    }

    public void testBadUserLogin() throws Exception {
        LoginContext context = new LoginContext("properties-realm", new UsernamePasswordCallback("bad", "starcraft"));

        try {
            context.login();
            fail("Should not allow this login with null username");
        } catch (LoginException e) {
        }
    }

    public void testNullPasswordLogin() throws Exception {
        LoginContext context = new LoginContext("properties-realm", new UsernamePasswordCallback("alan", null));

        try {
            context.login();
            fail("Should not allow this login with null password");
        } catch (LoginException e) {
        }
    }

    public void testBadPasswordLogin() throws Exception {
        LoginContext context = new LoginContext("properties-realm", new UsernamePasswordCallback("alan", "bad"));

        try {
            context.login();
            fail("Should not allow this login with null password");
        } catch (LoginException e) {
        }
    }
}
