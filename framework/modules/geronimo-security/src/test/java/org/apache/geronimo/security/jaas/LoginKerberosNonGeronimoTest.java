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
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.security.AbstractTest;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.RealmPrincipal;


/**
 * An example of how to setup non-Geronimo login modules when the
 * <code>GeronimoLoginConfiguration</code> has been installed in the JVM.
 *
 * @version $Rev$ $Date$
 * @see org.apache.geronimo.security.jaas.GeronimoLoginConfiguration
 * @see javax.security.auth.login.Configuration
 */
public class LoginKerberosNonGeronimoTest extends AbstractTest {

    protected AbstractName kerberosCE;
    protected AbstractName kerberosLM;

    /**
     * Install the <code>GeronimoLoginConfiguration</code> but setup a non-Geronimo
     * JAAS configuration entry named kerberos-foobar.  This entry does a simple
     * Kerberos login using the ticket cache.
     *
     * @throws Exception
     */
    public void setUp() throws Exception {
        needLoginConfiguration = true;
        super.setUp();

        GBeanData gbean;

        gbean = buildGBeanData("name", "KerberosLoginModule", LoginModuleGBean.class);
        kerberosLM = gbean.getAbstractName();
        gbean.setAttribute("loginModuleClass", "com.sun.security.auth.module.Krb5LoginModule");
        Properties props = new Properties();
        props.put("debug", "true");
        props.put("useTicketCache", "true");
        props.put("doNotPrompt", "true");
        gbean.setAttribute("options", props);
        kernel.loadGBean(gbean, bundleContext);

        gbean = buildGBeanData("name", "kerberosConfigurationEntry", DirectConfigurationEntry.class);
        kerberosCE = gbean.getAbstractName();
        gbean.setAttribute("applicationConfigName", "kerberos-foobar");
        gbean.setAttribute("controlFlag", LoginModuleControlFlag.REQUIRED);
        gbean.setReferencePattern("Module", kerberosLM);
        kernel.loadGBean(gbean, bundleContext);

        kernel.startGBean(loginConfiguration);
        kernel.startGBean(kerberosLM);
        kernel.startGBean(kerberosCE);
    }

    /**
     * Stop and unload the configuration entry.  Restpore the JAAS configuration
     * back to <code>ConfigFile</code>.
     *
     * @throws Exception
     */
    public void tearDown() throws Exception {
        kernel.stopGBean(kerberosCE);
        kernel.stopGBean(kerberosLM);
        kernel.stopGBean(loginConfiguration);

        kernel.unloadGBean(kerberosCE);
        kernel.unloadGBean(kerberosLM);
        kernel.unloadGBean(loginConfiguration);

        super.tearDown();
    }

    /**
     * Perform a vanilla Kerberos login that has nothing to do w/ a Geronimo
     * security realm.  The subject that has been created should not have any
     * realm principals.
     *
     * @throws Exception
     */
    public void testLogin() throws Exception {

        try {
            LoginContext context = new LoginContext("kerberos-foobar");

            context.login();
            Subject subject = context.getSubject();

            assertTrue("expected non-null subject", subject != null);
            assertTrue("id of subject should be null", ContextManager.getSubjectId(subject) == null);
            assertEquals("subject should have one principal", 1, subject.getPrincipals().size());
            assertEquals("subject should have no realm principal", 0, subject.getPrincipals(RealmPrincipal.class).size());

            context.logout();
        } catch (LoginException e) {
            e.printStackTrace();
            // May not have kerberos
        }
    }
}
