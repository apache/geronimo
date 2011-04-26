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

import java.util.Properties;

import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.security.AbstractTest;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.realm.GenericSecurityRealm;


/**
 * @version $Rev$ $Date$
 */
public class LoginKerberosTest extends AbstractTest {

    protected AbstractName kerberosRealm;
    protected AbstractName kerberosLM;

    public void setUp() throws Exception {
        super.setUp();

        GBeanData gbean = buildGBeanData("name", "KerberosLoginModule", LoginModuleGBean.class);
        kerberosLM = gbean.getAbstractName();
        gbean.setAttribute("loginModuleClass", "com.sun.security.auth.module.Krb5LoginModule");
        Properties props = new Properties();
        props.put("debug", "true");
        props.put("useTicketCache", "true");
        props.put("doNotPrompt", "true");
        gbean.setAttribute("options", props);
        kernel.loadGBean(gbean, bundleContext);

        gbean = buildGBeanData("name", "KerberosLoginModuleUse", JaasLoginModuleUse.class);
        AbstractName testUseName = gbean.getAbstractName();
        gbean.setAttribute("controlFlag", LoginModuleControlFlag.REQUIRED);
        gbean.setReferencePattern("LoginModule", kerberosLM);
        kernel.loadGBean(gbean, bundleContext);

        gbean = buildGBeanData("name", "KerberosSecurityRealm", GenericSecurityRealm.class);
        kerberosRealm = gbean.getAbstractName();
        gbean.setAttribute("realmName", "TOOLAZYDOGS.COM");
        gbean.setReferencePattern("LoginModuleConfiguration", testUseName);
        kernel.loadGBean(gbean, bundleContext);
        kernel.startGBean(kerberosLM);
        kernel.startGBean(testUseName);
        kernel.startGBean(kerberosRealm);
    }

    public void tearDown() throws Exception {
        kernel.stopGBean(kerberosRealm);
        kernel.unloadGBean(kerberosRealm);
        kernel.stopGBean(kerberosLM);
        kernel.unloadGBean(kerberosLM);

        super.tearDown();
    }

    public void testLogin() throws Exception {
        //GERONIMO-3388 consider turning on principal wrapping and expecting a RealmPrincipal and DomainPrincipal.
        try {
            LoginContext context = new LoginContext("TOOLAZYDOGS.COM");

            context.login();
            Subject subject = context.getSubject();

            assertTrue("expected non-null subject", subject != null);
            assertEquals("server-side subject should have two principals", 1, subject.getPrincipals().size());
            assertEquals("server-side subject should have one kerberos principal", 1, subject.getPrincipals(KerberosPrincipal.class).size());

            context.logout();

        } catch (LoginException e) {
            //See GERONIMO-3388.  This seems to be the normal code path.
            e.printStackTrace();
            // May not have kerberos
        }
    }
}
