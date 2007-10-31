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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.security.AbstractTest;
import org.apache.geronimo.security.realm.GenericSecurityRealm;


/**
 * This test makes sure that CertificatePropertiesFileLoginModule does not add any principals when login failed.
 * 
 * @version $Rev$ $Date$
 */
public class LoginCertificatePropertiesFileAdvancedTest extends AbstractTest {
    protected AbstractName clientCE;
    protected AbstractName testCE;
    protected AbstractName testRealm;
    protected AbstractName neverFailModule;

    protected X509Certificate badCert;
    String badCertText = "-----BEGIN CERTIFICATE-----\n"
                        +"MIIBqTCCAVMCBgEV+C5ZTjANBgkqhkiG9w0BAQQFADBdMQwwCgYDVQQDEwNCYWQxEDAOBgNVBAsT\n"
                        +"B015IFVuaXQxDzANBgNVBAoTBk15IE9yZzEQMA4GA1UEBxMHTXkgQ2l0eTELMAkGA1UECBMCQVAx\n"
                        +"CzAJBgNVBAYTAklOMB4XDTA3MTAzMTIyMjg0OFoXDTE3MTAyODIyMjg0OFowXTEMMAoGA1UEAxMD\n"
                        +"QmFkMRAwDgYDVQQLEwdNeSBVbml0MQ8wDQYDVQQKEwZNeSBPcmcxEDAOBgNVBAcTB015IENpdHkx\n"
                        +"CzAJBgNVBAgTAkFQMQswCQYDVQQGEwJJTjBcMA0GCSqGSIb3DQEBAQUAA0sAMEgCQQDBoSkFEEJC\n"
                        +"/OtI49Cr8rC9pXpniw8lXG6iUUjEC3VzDuHpqKaCGNdilhVPkRsONsmLySoJDFsZR/w4HrXlwUS1\n"
                        +"AgMBAAEwDQYJKoZIhvcNAQEEBQADQQCe3Xt5J+UoNqpdDN4y6KV4EdQlrMNfqBJnGTE+sdlKV6cN\n"
                        +"PjUnrEF1laqhX4Rx+2u56VBA2SBnEaeADawaXWkD\n"
                        +"-----END CERTIFICATE-----";

    public void setUp() throws Exception {
        needServerInfo = true;
        needLoginConfiguration = true;
        super.setUp();

        GBeanData gbean;

        gbean = buildGBeanData("name", "NeverFailLoginModule", LoginModuleGBean.getGBeanInfo());
        neverFailModule = gbean.getAbstractName();
        gbean.setAttribute("loginModuleClass", "org.apache.geronimo.security.jaas.NeverFailLoginModule");
        gbean.setAttribute("options", null);
        gbean.setAttribute("loginDomainName", "NeverFailDomain");
        gbean.setAttribute("wrapPrincipals", Boolean.TRUE);
        kernel.loadGBean(gbean, LoginModuleGBean.class.getClassLoader());
        kernel.startGBean(neverFailModule);

        gbean = buildGBeanData("name", "CertificatePropertiesLoginModule", LoginModuleGBean.getGBeanInfo());
        testCE = gbean.getAbstractName();
        gbean.setAttribute("loginModuleClass", "org.apache.geronimo.security.realm.providers.CertificatePropertiesFileLoginModule");
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("usersURI", new File(BASEDIR, "src/test/data/data/cert-users.properties").toURI().toString());
        props.put("groupsURI", new File(BASEDIR, "src/test/data/data/groups.properties").toURI().toString());
        gbean.setAttribute("options", props);
        gbean.setAttribute("loginDomainName", "CertProperties");
        gbean.setAttribute("wrapPrincipals", Boolean.TRUE);
        kernel.loadGBean(gbean, LoginModuleGBean.class.getClassLoader());
        kernel.startGBean(testCE);

        gbean = buildGBeanData("name", "CertificatePropertiesLoginModuleUse", JaasLoginModuleUse.getGBeanInfo());
        AbstractName propsUseName = gbean.getAbstractName();
        gbean.setAttribute("controlFlag", LoginModuleControlFlag.OPTIONAL);
        gbean.setReferencePattern("LoginModule", testCE);
        kernel.loadGBean(gbean, JaasLoginModuleUse.class.getClassLoader());
        kernel.startGBean(propsUseName);

        gbean = buildGBeanData("name", "NeverFailLoginModuleUse", JaasLoginModuleUse.getGBeanInfo());
        AbstractName neverFailUseName = gbean.getAbstractName();
        gbean.setAttribute("controlFlag", LoginModuleControlFlag.REQUIRED);
        gbean.setReferencePattern("LoginModule", neverFailModule);
        gbean.setReferencePattern("Next", propsUseName);
        kernel.loadGBean(gbean, JaasLoginModuleUse.class.getClassLoader());
        kernel.startGBean(neverFailUseName);

        gbean = buildGBeanData("name", "CertificatePropertiesSecurityRealm", GenericSecurityRealm.getGBeanInfo());
        testRealm = gbean.getAbstractName();
        gbean.setAttribute("realmName", "cert-properties-realm");
        gbean.setAttribute("wrapPrincipals", Boolean.TRUE);
        gbean.setReferencePattern("LoginModuleConfiguration", neverFailUseName);
        gbean.setReferencePattern("ServerInfo", serverInfo);
        kernel.loadGBean(gbean, GenericSecurityRealm.class.getClassLoader());

        kernel.startGBean(loginConfiguration);
        kernel.startGBean(testRealm);
        
        CertificateFactory certFac = CertificateFactory.getInstance("X.509");
        badCert = (X509Certificate) certFac.generateCertificate(new ByteArrayInputStream(badCertText.getBytes()));
    }

    public void tearDown() throws Exception {
        kernel.stopGBean(testRealm);
        kernel.stopGBean(loginConfiguration);
        kernel.stopGBean(testCE);
        kernel.stopGBean(neverFailModule);
        kernel.stopGBean(serverInfo);

        kernel.unloadGBean(testRealm);
        kernel.unloadGBean(loginConfiguration);
        kernel.unloadGBean(testCE);
        kernel.unloadGBean(neverFailModule);
        kernel.unloadGBean(serverInfo);

        super.tearDown();
    }

    public void testBadCertificate() throws Exception {
        LoginContext context = new LoginContext("cert-properties-realm", new CertCallback(badCert));

        context.login();
        Subject subject = context.getSubject();
        assertTrue("expected non-null subject", subject != null);
        assertEquals("expected zero principals", 0, subject.getPrincipals().size());
        context.logout();
    }
}
