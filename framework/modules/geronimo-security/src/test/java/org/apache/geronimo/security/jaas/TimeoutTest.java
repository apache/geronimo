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
public class TimeoutTest extends AbstractTest {

    protected AbstractName testCE;
    protected AbstractName testRealm;
    protected AbstractName clientLM;
    protected AbstractName clientCE;

    public void setUp() throws Exception {
        timeoutTest = true;
        needServerInfo = true;
        needLoginConfiguration = true;
        super.setUp();

        GBeanData gbean;

        // Create all the parts
        ProxyLoginModule.init(bundleContext);
        gbean = buildGBeanData    ("name", "PropertiesLoginModule", LoginModuleGBean.class);
        testCE = gbean.getAbstractName();
        gbean.setAttribute("loginModuleClass", "org.apache.geronimo.security.realm.providers.PropertiesFileLoginModule");
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("usersURI", new File(BASEDIR, "src/test/data/data/users.properties").toURI().toString());
        props.put("groupsURI", new File(BASEDIR, "src/test/data/data/groups.properties").toURI().toString());
        gbean.setAttribute("options", props);
        gbean.setAttribute("loginDomainName", "PropertiesDomain");
        gbean.setAttribute("wrapPrincipals", Boolean.TRUE);
        kernel.loadGBean(gbean, bundleContext);

        gbean = buildGBeanData("name", "PropertiesLoginModuleUse", JaasLoginModuleUse.class);
        AbstractName testUseName = gbean.getAbstractName();
        gbean.setAttribute("controlFlag", LoginModuleControlFlag.REQUIRED);
        gbean.setReferencePattern("LoginModule", testCE);
        kernel.loadGBean(gbean, bundleContext);

        gbean = buildGBeanData("name", "PropertiesSecurityRealm", GenericSecurityRealm.class);
        testRealm = gbean.getAbstractName();
        gbean.setAttribute("realmName", "properties-realm");
        gbean.setReferencePattern("LoginModuleConfiguration", testUseName);
        gbean.setReferencePattern("ServerInfo", serverInfo);
        gbean.setAttribute("global", Boolean.TRUE);
        kernel.loadGBean(gbean, bundleContext);

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

        kernel.shutdown();
    }


    public void testTimeout() throws Exception {

        LoginContext context = ContextManager.login("properties-realm", new AbstractTest.UsernamePasswordCallback("alan", "starcraft"));

        Subject subject = context.getSubject();
        assertTrue("expected non-null client subject", subject != null);
        Set set = subject.getPrincipals(IdentificationPrincipal.class);
        assertEquals("client subject should have one ID principal", set.size(), 1);
        IdentificationPrincipal idp = (IdentificationPrincipal) set.iterator().next();
        subject = ContextManager.getRegisteredSubject(idp.getId());

        assertTrue("expected non-null server subject", subject != null);
        assertTrue("server subject should have one remote principal", subject.getPrincipals(IdentificationPrincipal.class).size() == 1);
        IdentificationPrincipal remote = subject.getPrincipals(IdentificationPrincipal.class).iterator().next();
        assertTrue("server subject should be associated with remote id", ContextManager.getRegisteredSubject(remote.getId()) != null);
        assertEquals("server-side subject should have seven principal", 7, subject.getPrincipals().size());
        assertTrue("server subject should have two realm principal", subject.getPrincipals(RealmPrincipal.class).size() == 2);
        assertTrue("server subject should have two domain principal", subject.getPrincipals(DomainPrincipal.class).size() == 2);

        assertTrue("id of server subject should be non-null", ContextManager.getSubjectId(subject) != null);

//        Thread.sleep(3000); // wait for timeout to kick in
//
//        assertTrue("id of server subject should be non-null", ContextManager.getSubjectId(subject) != null);

//        Thread.sleep(7000); // wait for timeout to kick in
        //TODO figure out if we can time out logins!
//        assertTrue("id of server subject should be null", ContextManager.getSubjectId(subject) == null);
    }
}
