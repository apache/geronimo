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

import java.util.Properties;
import javax.management.ObjectName;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.security.AbstractTest;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.RealmPrincipal;


/**
 * @version $Rev$ $Date$
 */
public class LoginKerberosTest extends AbstractTest {

    protected ObjectName kerberosRealm;
    protected ObjectName kerberosLM;

    public void setUp() throws Exception {
        super.setUp();

        GBeanMBean gbean = new GBeanMBean("org.apache.geronimo.security.jaas.LoginModuleGBean");
        kerberosLM = new ObjectName("geronimo.security:type=LoginModule,name=TOOLAZYDOGS.COM");
        gbean.setAttribute("loginModuleClass", "com.sun.security.auth.module.Krb5LoginModule");
        gbean.setAttribute("serverSide", new Boolean(true)); // normally not, but in this case, it's treated as server-side
        Properties props = new Properties();
        props.put("debug", "true");
        props.put("useTicketCache", "true");
        props.put("doNotPrompt", "true");
        gbean.setAttribute("options", props);
        kernel.loadGBean(kerberosLM, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.security.realm.GenericSecurityRealm");
        kerberosRealm = new ObjectName("geronimo.security:type=SecurityRealm,realm=TOOLAZYDOGS.COM");
        gbean.setAttribute("realmName", "TOOLAZYDOGS.COM");
        props = new Properties();
        props.setProperty("LoginModule.1.REQUIRED","geronimo.security:type=LoginModule,name=TOOLAZYDOGS.COM");
        gbean.setAttribute("loginModuleConfiguration", props);
        kernel.loadGBean(kerberosRealm, gbean);
        kernel.startGBean(kerberosLM);
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
        try {
            LoginContext context = new LoginContext("kerberos-local");

            context.login();
            Subject subject = context.getSubject();

            assertTrue("expected non-null client-side subject", subject != null);
            subject = ContextManager.getServerSideSubject(subject);

            assertTrue("expected non-null server-side subject", subject != null);
            assertTrue("id of server-side subject should be non-null", ContextManager.getSubjectId(subject) != null);
            assertEquals("server-side subject should have two principals", 2, subject.getPrincipals().size());
            assertEquals("server-side subject should have one realm principal", 1, subject.getPrincipals(RealmPrincipal.class).size());
            RealmPrincipal principal = (RealmPrincipal) subject.getPrincipals(RealmPrincipal.class).iterator().next();
            assertTrue("id of principal should be non-zero", principal.getId() != 0);

            context.logout();

            assertTrue("id of subject should be null", ContextManager.getSubjectId(subject) == null);
        } catch (LoginException e) {
            e.printStackTrace();
            // May not have kerberos
        }
    }
}
