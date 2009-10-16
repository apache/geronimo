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

import java.util.HashMap;
import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.realm.providers.GeronimoPasswordCredential;
import org.apache.geronimo.security.realm.providers.GeronimoPasswordCredentialLoginModule;

/**
 * @version $Rev$ $Date$
 */
public class LoginGeronimoPasswordCredentialTest extends AbstractLoginModuleTest {
    private String username = "john";
    private String password = "smith";

    protected GBeanData setupTestLoginModule() throws MalformedObjectNameException {
        GBeanData gbean;
        gbean = buildGBeanData("name", "GeronimoPasswordCredentialLoginModule", LoginModuleGBean.class);
        gbean.setAttribute("loginModuleClass", GeronimoPasswordCredentialLoginModule.class.getName());
        Map<String, Object> props = new HashMap<String, Object>();
        gbean.setAttribute("options", props);
        gbean.setAttribute("loginDomainName", "GeronimoPasswordCredential");
        gbean.setAttribute("wrapPrincipals", Boolean.FALSE);
        return gbean;
    }

    public void testLogin() throws Exception {
        LoginContext context = new LoginContext(COMPLEX_REALM, new UsernamePasswordCallback(username, password));

        context.login();
        Subject subject = context.getSubject();

        assertTrue("expected non-null subject", subject != null);
        assertEquals("Principals", 0, subject.getPrincipals().size());
        assertEquals("Private credentials", 1, subject.getPrivateCredentials().size());
        assertEquals("GeronimoPasswordCredential private credentials", 1, subject.getPrivateCredentials(GeronimoPasswordCredential.class).size());
        assertEquals("Public credentials", 0, subject.getPublicCredentials().size());
        GeronimoPasswordCredential pwdCred = (GeronimoPasswordCredential) subject.getPrivateCredentials().toArray()[0];
        assertEquals("Username", username, pwdCred.getUserName());
        assertEquals("Password", password, new String(pwdCred.getPassword()));

        context.logout();

        assertEquals("Private credentials upon logout", 0, subject.getPrivateCredentials().size());
        assertTrue("id of server subject should be null", ContextManager.getSubjectId(subject) == null);
    }

    public void testNullUserLogin() throws Exception {
        LoginContext context = new LoginContext(COMPLEX_REALM, new UsernamePasswordCallback(null, password));

        context.login();
        Subject subject = context.getSubject();

        assertTrue("expected non-null subject", subject != null);
        assertEquals("Principals", 0, subject.getPrincipals().size());
        assertEquals("Private credentials", 0, subject.getPrivateCredentials().size());
        assertEquals("Public credentials", 0, subject.getPublicCredentials().size());

        context.logout();
    }

    public void testBadUserLogin() throws Exception {
        //not relevant
    }

    public void testNullPasswordLogin() throws Exception {
        LoginContext context = new LoginContext(COMPLEX_REALM, new UsernamePasswordCallback(username, null));

        context.login();
        Subject subject = context.getSubject();

        assertTrue("expected non-null subject", subject != null);
        assertEquals("Principals", 0, subject.getPrincipals().size());
        assertEquals("Private credentials", 0, subject.getPrivateCredentials().size());
        assertEquals("Public credentials", 0, subject.getPublicCredentials().size());

        context.logout();
    }

    public void testBadPasswordLogin() throws Exception {
        //not relevant
    }

    public void testNoPrincipalsAddedOnFailure() throws Exception {
        //not relevant
    }

    public void testLogoutWithReadOnlySubject() throws Exception {
        LoginContext context = new LoginContext(COMPLEX_REALM, new UsernamePasswordCallback(username, password));

        context.login();
        Subject subject = context.getSubject();

        assertTrue("expected non-null subject", subject != null);

        subject.setReadOnly();

        try {
            context.logout();
        } catch(Exception e) {
            fail("logout failed");
        }
        GeronimoPasswordCredential pwdCred = (GeronimoPasswordCredential) subject.getPrivateCredentials().toArray()[0];
        assertTrue("credential should have been destroyed", pwdCred.isDestroyed());
    }
}
