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
import java.util.Collections;

import javax.security.auth.Subject;

import org.apache.karaf.jaas.boot.ProxyLoginModule;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.osgi.MockBundle;
import org.apache.geronimo.kernel.osgi.MockBundleContext;
import org.apache.geronimo.security.AbstractTest;
import org.apache.geronimo.security.jaas.DirectConfigurationEntry;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.security.jaas.LoginModuleGBean;
import org.apache.geronimo.security.jaas.LoginModuleControlFlag;
import org.apache.geronimo.security.jaas.ConfigurationEntryFactory;
import org.apache.geronimo.security.realm.GenericSecurityRealm;
import org.apache.geronimo.security.realm.providers.PropertiesFileLoginModule;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class SimpleCredentialStoreImplTest extends AbstractTest {
    private Bundle bundle = new MockBundle(getClass().getClassLoader(), "", 1);
    protected AbstractName clientLM;
    protected AbstractName clientCE;
    protected AbstractName testCE;
    protected AbstractName testRealm;
    private GenericSecurityRealm gsr;

    public void setUp() throws Exception {
        ProxyLoginModule.init(new MockBundleContext(bundle));
        needServerInfo = true;
        needLoginConfiguration = false;
        super.setUp();

        Map<String, Object> props = new HashMap<String, Object>();
        props.put("usersURI", new File(BASEDIR, "src/test/data/data/users.properties").toURI().toString());
        props.put("groupsURI", new File(BASEDIR, "src/test/data/data/groups.properties").toURI().toString());
        LoginModuleGBean lm = new LoginModuleGBean(PropertiesFileLoginModule.class.getName(), null, true, props, "TestProperties", getClass().getClassLoader());

        JaasLoginModuleUse lmu = new JaasLoginModuleUse(lm, null, LoginModuleControlFlag.REQUIRED);

        gsr = new GenericSecurityRealm("properties-realm", lmu, false, true, (ServerInfo) kernel.getGBean(serverInfo), bundle, kernel);

    }

    public void tearDown() throws Exception {
        kernel.stopGBean(serverInfo);

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
        CredentialStore credentialStore = new SimpleCredentialStoreImpl(credentials, Collections.<ConfigurationEntryFactory>singleton(gsr), getClass().getClassLoader());
        Subject subject = credentialStore.getSubject("properties-realm", "foo");
        assertNotNull(subject);
    }


}
