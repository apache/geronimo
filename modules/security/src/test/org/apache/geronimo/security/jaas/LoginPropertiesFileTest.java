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
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;

import java.io.File;
import java.util.HashMap;
import java.util.Collections;

import com.sun.security.auth.login.ConfigFile;

import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.security.AbstractTest;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.system.serverinfo.ServerInfo;


/**
 * @version $Revision: 1.4 $ $Date: 2004/05/28 22:22:40 $
 */
public class LoginPropertiesFileTest extends AbstractTest {
    protected ObjectName serverInfo;
    protected ObjectName propertiesRealm;
    protected ObjectName propertiesCE;

    public void setUp() throws Exception {
        Configuration.setConfiguration(new GeronimoLoginConfiguration());

        super.setUp();

        GBeanMBean gbean;

        gbean = new GBeanMBean(ServerInfo.GBEAN_INFO);
        serverInfo = new ObjectName("geronimo.system:role=ServerInfo");
        gbean.setAttribute("BaseDirectory", ".");
        kernel.loadGBean(serverInfo, gbean);
        kernel.startGBean(serverInfo);

        gbean = new GBeanMBean("org.apache.geronimo.security.realm.providers.PropertiesFileSecurityRealm");
        propertiesRealm = new ObjectName("geronimo.security:type=SecurityRealm,realm=properties-realm");
        gbean.setAttribute("RealmName", "properties-realm");
        gbean.setAttribute("MaxLoginModuleAge", new Long(1 * 1000));
        gbean.setAttribute("UsersURI", (new File(new File("."), "src/test-data/data/users.properties")).toURI());
        gbean.setAttribute("GroupsURI", (new File(new File("."), "src/test-data/data/groups.properties")).toURI());
        gbean.setReferencePatterns("ServerInfo", Collections.singleton(serverInfo));
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
        kernel.stopGBean(serverInfo);
        kernel.unloadGBean(propertiesRealm);
        kernel.unloadGBean(propertiesCE);
        kernel.unloadGBean(serverInfo);

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
