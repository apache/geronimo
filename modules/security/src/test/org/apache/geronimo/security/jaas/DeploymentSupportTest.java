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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.security.AbstractTest;
import org.apache.geronimo.security.realm.DeploymentSupport;
import org.apache.geronimo.security.realm.SecurityRealm;
import org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal;
import org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal;
import org.apache.geronimo.system.serverinfo.ServerInfo;

/**
 * Unit test for the DeploymentSupport features of security realms.
 *
 * @version $Rev: 105949 $ $Date: 2004-11-20 02:38:55 -0500 (Sat, 20 Nov 2004) $
 */
public class DeploymentSupportTest extends AbstractTest {

    protected ObjectName serverInfo;
    protected ObjectName loginConfiguration;
    protected ObjectName clientLM;
    protected ObjectName clientCE;
    protected ObjectName testCE;
    protected ObjectName testRealm;

    public void setUp() throws Exception {
        super.setUp();

        GBeanMBean gbean;

        gbean = new GBeanMBean(ServerInfo.GBEAN_INFO);
        serverInfo = new ObjectName("geronimo.system:role=ServerInfo");
        gbean.setAttribute("baseDirectory", ".");
        kernel.loadGBean(serverInfo, gbean);
        kernel.startGBean(serverInfo);

        gbean = new GBeanMBean("org.apache.geronimo.security.jaas.GeronimoLoginConfiguration");
        loginConfiguration = new ObjectName("geronimo.security:type=LoginConfiguration");
        kernel.loadGBean(loginConfiguration, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.security.jaas.LoginModuleGBean");
        clientLM = new ObjectName("geronimo.security:type=LoginModule,name=properties-client");
        gbean.setAttribute("loginModuleClass", "org.apache.geronimo.security.jaas.JaasLoginCoordinator");
        gbean.setAttribute("serverSide", new Boolean(false));
        Properties props = new Properties();
        props.put("host", "localhost");
        props.put("port", "4242");
        props.put("realm", "properties-realm");
        gbean.setAttribute("options", props);
        kernel.loadGBean(clientLM, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.security.jaas.DirectConfigurationEntry");
        clientCE = new ObjectName("geronimo.security:type=ConfigurationEntry,jaasId=properties-client");
        gbean.setAttribute("applicationConfigName", "properties-client");
        gbean.setAttribute("controlFlag", LoginModuleControlFlag.REQUIRED);
        gbean.setReferencePatterns("Module", Collections.singleton(clientLM));
        kernel.loadGBean(clientCE, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.security.jaas.LoginModuleGBean");
        testCE = new ObjectName("geronimo.security:type=LoginModule,name=properties");
        gbean.setAttribute("loginModuleClass", "org.apache.geronimo.security.realm.providers.PropertiesFileLoginModule");
        gbean.setAttribute("serverSide", new Boolean(true));
        props = new Properties();
        props.put("usersURI", "src/test-data/data/users.properties");
        props.put("groupsURI", "src/test-data/data/groups.properties");
        gbean.setAttribute("options", props);
        gbean.setAttribute("loginDomainName", "TestProperties");
        kernel.loadGBean(testCE, gbean);

        gbean = new GBeanMBean("org.apache.geronimo.security.realm.GenericSecurityRealm");
        testRealm = new ObjectName("geronimo.security:type=SecurityRealm,realm=properties-realm");
        gbean.setAttribute("realmName", "properties-realm");
        props = new Properties();
        props.setProperty("LoginModule.1.REQUIRED","geronimo.security:type=LoginModule,name=properties");
        gbean.setAttribute("loginModuleConfiguration", props);
        gbean.setReferencePatterns("ServerInfo", Collections.singleton(serverInfo));
        kernel.loadGBean(testRealm, gbean);

        kernel.startGBean(loginConfiguration);
        kernel.startGBean(clientLM);
        kernel.startGBean(clientCE);
        kernel.startGBean(testCE);
        kernel.startGBean(testRealm);
    }

    public void tearDown() throws Exception {
        kernel.stopGBean(testRealm);
        kernel.stopGBean(testCE);
        kernel.stopGBean(clientCE);
        kernel.stopGBean(clientLM);
        kernel.stopGBean(loginConfiguration);
        kernel.stopGBean(serverInfo);

        kernel.unloadGBean(testCE);
        kernel.unloadGBean(testRealm);
        kernel.unloadGBean(clientCE);
        kernel.unloadGBean(clientLM);
        kernel.unloadGBean(loginConfiguration);
        kernel.unloadGBean(serverInfo);

        super.tearDown();
    }

    public void testDeploymentSupport() throws Exception {
        SecurityRealm realm = (SecurityRealm) kernel.getProxyManager().createProxy(testRealm, SecurityRealm.class);
        try {
            String[] domains = realm.getLoginDomains();
            assertEquals(1, domains.length);
            DeploymentSupport deployment = realm.getDeploymentSupport(domains[0]);
            assertNotNull(deployment);
            String[] classes = deployment.getPrincipalClassNames();
            assertEquals(2, classes.length);
            if(classes[0].equals(GeronimoUserPrincipal.class.getName())) {
                assertEquals(GeronimoGroupPrincipal.class.getName(), classes[1]);
            } else if(classes[1].equals(GeronimoUserPrincipal.class.getName())) {
                assertEquals(GeronimoGroupPrincipal.class.getName(), classes[0]);
            } else {
                fail("Unexpected principal class names "+classes[0]+" / "+classes[1]);
            }
            String[] names = deployment.getPrincipalsOfClass(GeronimoUserPrincipal.class.getName());
            assertEquals(5, names.length);
            List list = Arrays.asList(names);
            assertTrue(list.contains("izumi"));
            assertTrue(list.contains("alan"));
            assertTrue(list.contains("george"));
            assertTrue(list.contains("gracie"));
            assertTrue(list.contains("metro"));
            names = deployment.getPrincipalsOfClass(GeronimoGroupPrincipal.class.getName());
            assertEquals(5, names.length);
            list = Arrays.asList(names);
            assertTrue(list.contains("manager"));
            assertTrue(list.contains("it"));
            assertTrue(list.contains("pet"));
            assertTrue(list.contains("dog"));
            assertTrue(list.contains("cat"));
            String[] map = deployment.getAutoMapPrincipalClassNames();
            assertEquals(1, map.length);
            assertEquals(GeronimoGroupPrincipal.class.getName(), map[0]);
        } finally {
            kernel.getProxyManager().destroyProxy(realm);
        }
    }
}
