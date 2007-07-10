/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.security.credentialstore;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.security.AbstractTest;
import org.apache.geronimo.security.jaas.DirectConfigurationEntry;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.security.jaas.LoginModuleGBean;
import org.apache.geronimo.security.jaas.LoginModuleControlFlag;
import org.apache.geronimo.security.realm.GenericSecurityRealm;

/**
 * @version $Rev$ $Date$
 */
public class SimpleCredentialStoreImplTest extends AbstractTest {
    protected AbstractName clientLM;
    protected AbstractName clientCE;
    protected AbstractName testCE;
    protected AbstractName testRealm;

    public void setUp() throws Exception {
        needServerInfo = true;
        needLoginConfiguration = true;
        super.setUp();

        GBeanData gbean;

        gbean = buildGBeanData("name", "ClientPropertiesLoginModule", LoginModuleGBean.getGBeanInfo());
        clientLM = gbean.getAbstractName();
        gbean.setAttribute("loginModuleClass", "org.apache.geronimo.security.jaas.client.JaasLoginCoordinator");
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("host", "localhost");
        props.put("port", "4242");
        props.put("realm", "properties-realm");
        gbean.setAttribute("options", props);
        kernel.loadGBean(gbean, LoginModuleGBean.class.getClassLoader());

        gbean = buildGBeanData("name", "ClientConfigurationEntry", DirectConfigurationEntry.getGBeanInfo());
        clientCE = gbean.getAbstractName();
        gbean.setAttribute("applicationConfigName", "properties-client");
        gbean.setAttribute("controlFlag", LoginModuleControlFlag.REQUIRED);
        gbean.setReferencePattern("Module", clientLM);
        kernel.loadGBean(gbean, DirectConfigurationEntry.class.getClassLoader());

        gbean = buildGBeanData("name", "PropertiesLoginModule", LoginModuleGBean.getGBeanInfo());
        testCE = gbean.getAbstractName();
        gbean.setAttribute("loginModuleClass", "org.apache.geronimo.security.realm.providers.PropertiesFileLoginModule");
        props = new HashMap<String, Object>();
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
        gbean.setReferencePattern("LoginModuleConfiguration", testUseName);
        gbean.setReferencePattern("ServerInfo", serverInfo);
        kernel.loadGBean(gbean, GenericSecurityRealm.class.getClassLoader());

        kernel.startGBean(loginConfiguration);
        kernel.startGBean(clientLM);
        kernel.startGBean(clientCE);
        kernel.startGBean(testUseName);
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

    public void testCredentialStore() throws Exception {
        Map<String, String> callbackHanders = new HashMap<String, String>();
        callbackHanders.put(NameCallbackHandler.class.getName(), "izumi" );
        callbackHanders.put(PasswordCallbackHandler.class.getName(), "violin");
        Map<String, Map<String, String>> entries = new HashMap<String, Map<String, String>>();
        entries.put("foo", callbackHanders);
        Map<String, Map<String, Map<String, String>>> credentials = new HashMap<String, Map<String, Map<String, String>>>();
        credentials.put("properties-realm", entries);
        CredentialStore credentialStore = new SimpleCredentialStoreImpl(credentials, getClass().getClassLoader());
        Subject subject = credentialStore.getSubject("properties-realm", "foo");
        assertNotNull(subject);
    }


}
