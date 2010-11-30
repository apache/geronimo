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


package org.apache.geronimo.security.jaas;

import org.apache.karaf.jaas.boot.ProxyLoginModule;
import org.apache.geronimo.security.AbstractTest;
import org.apache.geronimo.security.realm.GenericSecurityRealm;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.AbstractName;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractLoginModuleTest extends AbstractTest {
    protected static final String SIMPLE_REALM = "simple-realm";
    protected static final String COMPLEX_REALM = "complex-realm";
    protected AbstractName clientCE;
    protected AbstractName testCE;
    protected AbstractName testRealm;
    protected AbstractName testRealm2;
    protected AbstractName neverFailModule;

    public void setUp() throws Exception {
        needServerInfo = true;
        needLoginConfiguration = true;
        super.setUp();
        ProxyLoginModule.init(bundleContext);

        GBeanData gbean;

        gbean = setupTestLoginModule();
        testCE = gbean.getAbstractName();
        kernel.loadGBean(gbean, bundleContext);

        gbean = buildGBeanData("name", "PropertiesLoginModuleUse", JaasLoginModuleUse.class);
        AbstractName testUseName = gbean.getAbstractName();
        gbean.setAttribute("controlFlag", LoginModuleControlFlag.REQUIRED);
        gbean.setReferencePattern("LoginModule", testCE);
        kernel.loadGBean(gbean, bundleContext);

        gbean = buildGBeanData("name", "PropertiesSecurityRealm", GenericSecurityRealm.class);
        testRealm = gbean.getAbstractName();
        gbean.setAttribute("realmName", SIMPLE_REALM);
        gbean.setAttribute("wrapPrincipals", Boolean.TRUE);
        gbean.setReferencePattern("LoginModuleConfiguration", testUseName);
        gbean.setReferencePattern("ServerInfo", serverInfo);
        gbean.setAttribute("global", Boolean.TRUE);
        kernel.loadGBean(gbean, bundleContext);

        gbean = buildGBeanData("name", "NeverFailLoginModule", LoginModuleGBean.class);
        neverFailModule = gbean.getAbstractName();
        gbean.setAttribute("loginModuleClass", "org.apache.geronimo.security.jaas.NeverFailLoginModule");
        gbean.setAttribute("options", null);
        gbean.setAttribute("loginDomainName", "NeverFailDomain");
        gbean.setAttribute("wrapPrincipals", Boolean.TRUE);
        kernel.loadGBean(gbean, bundleContext);
        kernel.startGBean(neverFailModule);

        gbean = buildGBeanData("name", "PropertiesLoginModuleUse2", JaasLoginModuleUse.class);
        AbstractName propsUseName = gbean.getAbstractName();
        gbean.setAttribute("controlFlag", LoginModuleControlFlag.OPTIONAL);
        gbean.setReferencePattern("LoginModule", testCE);
        kernel.loadGBean(gbean, bundleContext);
        kernel.startGBean(propsUseName);

        gbean = buildGBeanData("name", "NeverFailLoginModuleUse", JaasLoginModuleUse.class);
        AbstractName neverFailUseName = gbean.getAbstractName();
        gbean.setAttribute("controlFlag", LoginModuleControlFlag.REQUIRED);
        gbean.setReferencePattern("LoginModule", neverFailModule);
        gbean.setReferencePattern("Next", propsUseName);
        kernel.loadGBean(gbean, bundleContext);
        kernel.startGBean(neverFailUseName);

        gbean = buildGBeanData("name", "PropertiesSecurityRealm2", GenericSecurityRealm.class);
        testRealm2 = gbean.getAbstractName();
        gbean.setAttribute("realmName", COMPLEX_REALM);
        gbean.setAttribute("wrapPrincipals", Boolean.TRUE);
        gbean.setReferencePattern("LoginModuleConfiguration", neverFailUseName);
        gbean.setReferencePattern("ServerInfo", serverInfo);
        gbean.setAttribute("global", Boolean.TRUE);
        kernel.loadGBean(gbean, bundleContext);

        kernel.startGBean(loginConfiguration);
        kernel.startGBean(testCE);
        kernel.startGBean(testUseName);
        kernel.startGBean(testRealm);

        kernel.startGBean(neverFailModule);
        kernel.startGBean(neverFailUseName);
        kernel.startGBean(propsUseName);
        kernel.startGBean(testRealm2);
    }

    protected abstract GBeanData setupTestLoginModule() throws Exception;

    public void tearDown() throws Exception {
        kernel.stopGBean(testRealm);
        kernel.stopGBean(testCE);
        kernel.stopGBean(neverFailModule);
        kernel.stopGBean(loginConfiguration);
        kernel.stopGBean(serverInfo);

        kernel.unloadGBean(testCE);
        kernel.unloadGBean(testRealm);
        kernel.unloadGBean(loginConfiguration);
        kernel.unloadGBean(serverInfo);

        super.tearDown();
    }

    public abstract void testLogin() throws Exception;

    public abstract void testNullUserLogin() throws Exception;

    public abstract void testBadUserLogin() throws Exception;

    public abstract void testNullPasswordLogin() throws Exception;

    public abstract void testBadPasswordLogin() throws Exception;

    public abstract void testNoPrincipalsAddedOnFailure() throws Exception;
    
    public abstract void testLogoutWithReadOnlySubject() throws Exception;
}
