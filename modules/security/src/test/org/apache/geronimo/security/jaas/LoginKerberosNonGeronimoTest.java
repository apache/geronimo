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

import javax.management.ObjectName;
import javax.security.auth.Subject;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import java.util.HashMap;

import com.sun.security.auth.login.ConfigFile;

import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.security.AbstractTest;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.RealmPrincipal;


/**
 * An example of how to setup non-Geronimo login modules when the
 * <code>GeronimoLoginConfiguration</code> has been installed in the JVM.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:58:10 $
 * @see org.apache.geronimo.security.jaas.GeronimoLoginConfiguration
 * @see javax.security.auth.login.Configuration
 */
public class LoginKerberosNonGeronimoTest extends AbstractTest {

    protected ObjectName kerberosCE;

    /**
     * Install the <code>GeronimoLoginConfiguration</code> but setup a non-Geronimo
     * JAAS configuration entry named kerberos-foobar.  This entry does a simple
     * Kerberos login using the ticket cache.
     *
     * @throws Exception
     */
    public void setUp() throws Exception {
        Configuration.setConfiguration(new GeronimoLoginConfiguration());

        super.setUp();

        HashMap options = new HashMap();
        options.put("debug", "true");
        options.put("useTicketCache", "true");
        options.put("doNotPrompt", "true");

        GBeanMBean gbean = new GBeanMBean("org.apache.geronimo.security.jaas.ConfigurationEntryLocal");
        kerberosCE = new ObjectName("geronimo.security:type=ConfigurationEntry,jaasId=kerberos-foobar");
        gbean.setAttribute("JAASId", "kerberos-foobar");
        gbean.setAttribute("LoginModuleName", "com.sun.security.auth.module.Krb5LoginModule");
        gbean.setAttribute("ControlFlag", AppConfigurationEntry.LoginModuleControlFlag.REQUIRED);
        gbean.setAttribute("Options", options);
        kernel.loadGBean(kerberosCE, gbean);

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
        kernel.unloadGBean(kerberosCE);

        super.tearDown();

        Configuration.setConfiguration(new ConfigFile());
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
            // May not have kerberos
        }
    }
}
