/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.security.jaas;

import javax.management.ObjectName;
import javax.security.auth.Subject;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;

import java.io.File;
import java.util.HashMap;

import com.sun.security.auth.login.ConfigFile;

import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.security.AbstractTest;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.RealmPrincipal;


/**
 * @version $Revision: 1.1 $ $Date: 2004/02/17 00:05:40 $
 */
public class LoginPropertiesFileTest extends AbstractTest {
    protected ObjectName propertiesRealm;
    protected ObjectName propertiesCE;

    public void setUp() throws Exception {
        Configuration.setConfiguration(new GeronimoLoginConfiguration());

        super.setUp();

        GBeanMBean gbean = new GBeanMBean("org.apache.geronimo.security.realm.providers.PropertiesFileSecurityRealm");
        propertiesRealm = new ObjectName("geronimo.security:type=SecurityRealm,realm=properties-realm");
        gbean.setAttribute("RealmName", "properties-realm");
        gbean.setAttribute("MaxLoginModuleAge", new Long(1 * 1000));
        gbean.setAttribute("UsersURI", (new File(new File("."), "src/test-data/data/users.properties")).toURI());
        gbean.setAttribute("GroupsURI", (new File(new File("."), "src/test-data/data/groups.properties")).toURI());
        kernel.loadGBean(propertiesRealm, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.security.jaas.ConfigurationEntryRealmLocal");
        propertiesCE = new ObjectName("geronimo.security:type=ConfigurationEntry,jaasId=properties");
        gbean.setAttribute("JAASId", "properties");
        gbean.setAttribute("RealmName", "properties-realm");
        gbean.setAttribute("ControlFlag", AppConfigurationEntry.LoginModuleControlFlag.REQUIRED);
        gbean.setAttribute("Options", new HashMap());
        kernel.loadGBean(propertiesCE, gbean);

        kernel.startGBean(propertiesRealm);
        kernel.startGBean(propertiesCE);
    }

    public void tearDown() throws Exception {
        kernel.stopGBean(propertiesCE);
        kernel.stopGBean(propertiesRealm);
        kernel.unloadGBean(propertiesRealm);
        kernel.unloadGBean(propertiesCE);

        super.tearDown();

        Configuration.setConfiguration(new ConfigFile());
    }

    public void testLogin() throws Exception {

        LoginContext context = new LoginContext("properties", new AbstractTest.UsernamePasswordCallback("alan", "starcraft"));

        context.login();
        Subject subject = context.getSubject();

        assertTrue("expected non-null subject", subject != null);
        assertTrue("subject should have one remote principal", subject.getPrincipals(IdentificationPrincipal.class).size() == 1);
        IdentificationPrincipal remote = (IdentificationPrincipal) subject.getPrincipals(IdentificationPrincipal.class).iterator().next();
        assertTrue("subject should be associated with remote id", ContextManager.getRegisteredSubject(remote.getId()) != null);
        assertTrue("subject should have five principals", subject.getPrincipals().size() == 5);
        assertTrue("subject should have two realm principal", subject.getPrincipals(RealmPrincipal.class).size() == 2);
        RealmPrincipal principal = (RealmPrincipal) subject.getPrincipals(RealmPrincipal.class).iterator().next();
        assertTrue("id of principal should be non-zero", principal.getId() != 0);

        context.logout();

        assertTrue("id of subject should be null", ContextManager.getSubjectId(subject) == null);
    }
}
