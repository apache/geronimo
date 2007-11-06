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
import javax.security.auth.login.LoginException;
import javax.management.MalformedObjectNameException;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.security.AbstractTest;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.DomainPrincipal;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.realm.GenericSecurityRealm;


/**
 * Tests CertificatePropertiesFileLoginModule
 * 
 * @version $Rev$ $Date$
 */
public class LoginCertificatePropertiesFileTest extends AbstractLoginModuleTest {
    protected AbstractName clientCE;
    protected AbstractName testCE;
    protected AbstractName testRealm;
    protected X509Certificate cert;
    protected X509Certificate badCert;
    String certText =   "-----BEGIN CERTIFICATE-----\n"
                        +"MIIBpzCCAVECBgEV+CystzANBgkqhkiG9w0BAQQFADBcMQswCQYDVQQDEwJNZTEQMA4GA1UECxMH\n"
                        +"TXkgVW5pdDEPMA0GA1UEChMGTXkgT3JnMRAwDgYDVQQHEwdNeSBDaXR5MQswCQYDVQQIEwJBUDEL\n"
                        +"MAkGA1UEBhMCSU4wHhcNMDcxMDMxMjIyNjU4WhcNMTcxMDI4MjIyNjU4WjBcMQswCQYDVQQDEwJN\n"
                        +"ZTEQMA4GA1UECxMHTXkgVW5pdDEPMA0GA1UEChMGTXkgT3JnMRAwDgYDVQQHEwdNeSBDaXR5MQsw\n"
                        +"CQYDVQQIEwJBUDELMAkGA1UEBhMCSU4wXDANBgkqhkiG9w0BAQEFAANLADBIAkEAlN7IscUYq5U9\n"
                        +"d1TYVJaj5RQJLg39Gz9R9hB0hhOULSHOxeE0utTJvgBQcf+f39FgbGIdriJniyoubtCXGfSpxwID\n"
                        +"AQABMA0GCSqGSIb3DQEBBAUAA0EACQN6ScbxzAjrrQ3Ciy8I7/qsgpQo4Nuhfo5cAU4rvcKnujs6\n"
                        +"uGHAJrHMF/ROGl6kPZvFeoGXk5qjyKs8Kx5MJA==\n"
                        +"-----END CERTIFICATE-----";
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

/*
    public void setUp() throws Exception {
        needServerInfo = true;
        needLoginConfiguration = true;
        super.setUp();

        GBeanData gbean;

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

        gbean = buildGBeanData("name", "CertificatePropertiesLoginModuleUse", JaasLoginModuleUse.getGBeanInfo());
        AbstractName testUseName = gbean.getAbstractName();
        gbean.setAttribute("controlFlag", LoginModuleControlFlag.REQUIRED);
        gbean.setReferencePattern("LoginModule", testCE);
        kernel.loadGBean(gbean, JaasLoginModuleUse.class.getClassLoader());

        gbean = buildGBeanData("name", "CertificatePropertiesSecurityRealm", GenericSecurityRealm.getGBeanInfo());
        testRealm = gbean.getAbstractName();
        gbean.setAttribute("realmName", "cert-properties-realm");
        gbean.setAttribute("wrapPrincipals", Boolean.TRUE);
        gbean.setReferencePattern("LoginModuleConfiguration", testUseName);
        gbean.setReferencePattern("ServerInfo", serverInfo);
        kernel.loadGBean(gbean, GenericSecurityRealm.class.getClassLoader());

        kernel.startGBean(loginConfiguration);
        kernel.startGBean(testCE);
        kernel.startGBean(testUseName);
        kernel.startGBean(testRealm);
        
        CertificateFactory certFac = CertificateFactory.getInstance("X.509");
        cert = (X509Certificate) certFac.generateCertificate(new ByteArrayInputStream(certText.getBytes()));
        badCert = (X509Certificate) certFac.generateCertificate(new ByteArrayInputStream(badCertText.getBytes()));
    }
*/

    protected GBeanData setupTestLoginModule() throws Exception {
        CertificateFactory certFac = CertificateFactory.getInstance("X.509");
        cert = (X509Certificate) certFac.generateCertificate(new ByteArrayInputStream(certText.getBytes()));
        badCert = (X509Certificate) certFac.generateCertificate(new ByteArrayInputStream(badCertText.getBytes()));

        GBeanData gbean = buildGBeanData("name", "CertificatePropertiesLoginModule", LoginModuleGBean.getGBeanInfo());
        testCE = gbean.getAbstractName();
        gbean.setAttribute("loginModuleClass", "org.apache.geronimo.security.realm.providers.CertificatePropertiesFileLoginModule");
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("usersURI", "src/test/data/data/cert-users.properties");
        props.put("groupsURI", "src/test/data/data/groups.properties");
        gbean.setAttribute("options", props);
        gbean.setAttribute("loginDomainName", "CertProperties");
        gbean.setAttribute("wrapPrincipals", Boolean.TRUE);
        return gbean;
    }

    public void testLogin() throws Exception {

        LoginContext context = new LoginContext(SIMPLE_REALM, new CertCallback(cert));

        context.login();
        Subject subject = context.getSubject();

        assertTrue("expected non-null subject", subject != null);
        assertTrue("subject should have no remote principal", subject.getPrincipals(IdentificationPrincipal.class).size() == 0);
        assertEquals("subject should have 9 principals (" + subject.getPrincipals().size() + ")", 9, subject.getPrincipals().size());
        assertEquals("subject should have 3 realm principals (" + subject.getPrincipals(RealmPrincipal.class).size() + ")", 3, subject.getPrincipals(RealmPrincipal.class).size());
        assertEquals("subject should have 3 domain principals (" + subject.getPrincipals(DomainPrincipal.class).size() + ")", 3, subject.getPrincipals(DomainPrincipal.class).size());

        context.logout();

        assertTrue("id of server subject should be null", ContextManager.getSubjectId(subject) == null);
    }

    public void testNullUserLogin() throws Exception {
        LoginContext context = new LoginContext(SIMPLE_REALM, new CertCallback(null));

        try {
            context.login();
            fail("Should not allow this login with null certificate");
        } catch (LoginException e) {
        }
    }

    public void testBadUserLogin() throws Exception {
        LoginContext context = new LoginContext(SIMPLE_REALM, new CertCallback(badCert));

        try {
            context.login();
            fail("Should not allow this login with bad certificate");
        } catch (LoginException e) {
        }
    }

    public void testNullPasswordLogin() throws Exception {
        //not relevant
    }

    public void testBadPasswordLogin() throws Exception {
        //not relevant
    }

    public void testNoPrincipalsAddedOnFailure() throws Exception {
        LoginContext context = new LoginContext(COMPLEX_REALM, new CertCallback(badCert));

        context.login();
        Subject subject = context.getSubject();
        assertTrue("expected non-null subject", subject != null);
        assertEquals("expected zero principals", 0, subject.getPrincipals().size());
        context.logout();
    }

}
