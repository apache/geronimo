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

package org.apache.geronimo.jetty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.security.SecurityService;
import org.apache.geronimo.security.deploy.AutoMapAssistant;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.security.deploy.Principal;
import org.apache.geronimo.security.deploy.Realm;
import org.apache.geronimo.security.deploy.Role;
import org.apache.geronimo.security.deploy.Security;
import org.apache.geronimo.transaction.OnlineUserTransaction;


/**
 * Tests the JAAC security for Jetty by using both explicit and auto role mapping
 *
 * @version $Rev$ $Date$
 */
public class SecurityTest extends BaseSecurityTest {

    /**
     * Test the explicit map feature.  Only Alan should be able to log in.
     *
     * @throws Exception thrown if an error in the test occurs
     */
    public void testExplicitMapping() throws Exception {
        Security securityConfig = new Security();
        securityConfig.setUseContextHandler(false);

        DefaultPrincipal defaultPrincipal = new DefaultPrincipal();
        defaultPrincipal.setRealmName("demo-properties-realm");
        Principal principal = new Principal();
        principal.setClassName("org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal");
        principal.setPrincipalName("izumi");
        defaultPrincipal.setPrincipal(principal);

        securityConfig.setDefaultPrincipal(defaultPrincipal);

        Role role = new Role();
        role.setRoleName("content-administrator");
        principal = new Principal();
        principal.setClassName("org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal");
        principal.setPrincipalName("it");
        Realm realm = new Realm();
        realm.setRealmName("demo-properties-realm");
        realm.getPrincipals().add(principal);
        role.getRealms().put(realm.getRealmName(), realm);

        securityConfig.getRoleMappings().put(role.getRoleName(), role);

        startWebApp(securityConfig);

        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:5678/test/protected/hello.txt").openConnection();
        connection.setInstanceFollowRedirects(false);
        assertEquals(HttpURLConnection.HTTP_MOVED_TEMP, connection.getResponseCode());

        String cookie = connection.getHeaderField("Set-Cookie");
        cookie = cookie.substring(0, cookie.lastIndexOf(';'));
        String location = connection.getHeaderField("Location");

        connection = (HttpURLConnection) new URL(location).openConnection();
        connection.setInstanceFollowRedirects(false);
        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());

        location = location.substring(0, location.lastIndexOf('/')) + "/j_security_check?j_username=alan&j_password=starcraft";

        connection = (HttpURLConnection) new URL(location).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Cookie", cookie);
        connection.setInstanceFollowRedirects(false);
        assertEquals(HttpURLConnection.HTTP_MOVED_TEMP, connection.getResponseCode());

        connection = (HttpURLConnection) new URL("http://localhost:5678/test/protected/hello.txt").openConnection();
        connection.setRequestProperty("Cookie", cookie);
        connection.setInstanceFollowRedirects(false);
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        assertEquals("Hello World", reader.readLine());
        connection.disconnect();

        connection = (HttpURLConnection) new URL("http://localhost:5678/test/protected/hello.txt").openConnection();
        connection.setInstanceFollowRedirects(false);
        assertEquals(HttpURLConnection.HTTP_MOVED_TEMP, connection.getResponseCode());

        cookie = connection.getHeaderField("Set-Cookie");
        cookie = cookie.substring(0, cookie.lastIndexOf(';'));
        location = connection.getHeaderField("Location");

        connection = (HttpURLConnection) new URL(location).openConnection();
        connection.setInstanceFollowRedirects(false);
        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());

        location = location.substring(0, location.lastIndexOf('/')) + "/j_security_check?j_username=izumi&j_password=violin";

        connection = (HttpURLConnection) new URL(location).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Cookie", cookie);
        connection.setInstanceFollowRedirects(false);
        assertEquals(HttpURLConnection.HTTP_MOVED_TEMP, connection.getResponseCode());

        try {
            connection = (HttpURLConnection) new URL("http://localhost:5678/test/protected/hello.txt").openConnection();
            connection.setRequestProperty("Cookie", cookie);
            connection.setInstanceFollowRedirects(false);
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            fail("Should throw an IOException for HTTP 403 response");
        } catch (IOException e) {
        }

        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, connection.getResponseCode());
        connection.disconnect();

        stopWebApp();
    }

    /**
     * Test the auto map feature.  Only Izumi should be able to log in.
     *
     * @throws Exception thrown if an error in the test occurs
     */
    public void testAutoMapping() throws Exception {
        Security securityConfig = new Security();
        securityConfig.setUseContextHandler(false);

        AutoMapAssistant assistant = new AutoMapAssistant();
        assistant.setSecurityRealm("demo-properties-realm");
        securityConfig.setAssistant(assistant);

        securityConfig.getRoleNames().add("content-administrator");
        securityConfig.getRoleNames().add("auto-administrator");

        SecurityService securityService = null;
        try {
            securityService = (SecurityService) kernel.getProxyManager().createProxy(securityServiceName, SecurityService.class);
            securityConfig.autoGenerate(securityService);

            startWebApp(securityConfig);

            HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:5678/test/protected/hello.txt").openConnection();
            connection.setInstanceFollowRedirects(false);
            assertEquals(HttpURLConnection.HTTP_MOVED_TEMP, connection.getResponseCode());

            String cookie = connection.getHeaderField("Set-Cookie");
            cookie = cookie.substring(0, cookie.lastIndexOf(';'));
            String location = connection.getHeaderField("Location");

            connection = (HttpURLConnection) new URL(location).openConnection();
            connection.setInstanceFollowRedirects(false);
            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());

            location = location.substring(0, location.lastIndexOf('/')) + "/j_security_check?j_username=izumi&j_password=violin";

            connection = (HttpURLConnection) new URL(location).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Cookie", cookie);
            connection.setInstanceFollowRedirects(false);
            assertEquals(HttpURLConnection.HTTP_MOVED_TEMP, connection.getResponseCode());

            connection = (HttpURLConnection) new URL("http://localhost:5678/test/protected/hello.txt").openConnection();
            connection.setRequestProperty("Cookie", cookie);
            connection.setInstanceFollowRedirects(false);
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
            assertEquals("Hello World", reader.readLine());
            connection.disconnect();


            connection = (HttpURLConnection) new URL("http://localhost:5678/test/protected/hello.txt").openConnection();
            connection.setInstanceFollowRedirects(false);
            assertEquals(HttpURLConnection.HTTP_MOVED_TEMP, connection.getResponseCode());

            cookie = connection.getHeaderField("Set-Cookie");
            cookie = cookie.substring(0, cookie.lastIndexOf(';'));
            location = connection.getHeaderField("Location");

            connection = (HttpURLConnection) new URL(location).openConnection();
            connection.setInstanceFollowRedirects(false);
            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());

            location = location.substring(0, location.lastIndexOf('/')) + "/j_security_check?j_username=alan&j_password=starcraft";

            connection = (HttpURLConnection) new URL(location).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Cookie", cookie);
            connection.setInstanceFollowRedirects(false);
            assertEquals(HttpURLConnection.HTTP_MOVED_TEMP, connection.getResponseCode());

            try {
                connection = (HttpURLConnection) new URL("http://localhost:5678/test/protected/hello.txt").openConnection();
                connection.setRequestProperty("Cookie", cookie);
                connection.setInstanceFollowRedirects(false);
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                fail("Should throw an IOException for HTTP 403 response");
            } catch (IOException e) {
            }

            assertEquals(HttpURLConnection.HTTP_FORBIDDEN, connection.getResponseCode());
            connection.disconnect();
            stopWebApp();
        } finally {
            kernel.getProxyManager().destroyProxy(securityService);
        }
    }

    /**
     * Mixed the auto map and the standard explicit map.  Both Alan and Izumi
     * should be able to login.
     *
     * @throws Exception thrown if an error in the test occurs
     */
    public void testMixedMapping() throws Exception {
        Security securityConfig = new Security();
        securityConfig.setUseContextHandler(false);

        AutoMapAssistant assistant = new AutoMapAssistant();
        assistant.setSecurityRealm("demo-properties-realm");
        securityConfig.setAssistant(assistant);

        securityConfig.getRoleNames().add("content-administrator");
        securityConfig.getRoleNames().add("auto-administrator");

        SecurityService securityService = null;
        try {
            securityService = (SecurityService) kernel.getProxyManager().createProxy(securityServiceName, SecurityService.class);
            securityConfig.autoGenerate(securityService);

            DefaultPrincipal defaultPrincipal = new DefaultPrincipal();
            defaultPrincipal.setRealmName("demo-properties-realm");
            Principal principal = new Principal();
            principal.setClassName("org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal");
            principal.setPrincipalName("izumi");
            defaultPrincipal.setPrincipal(principal);

            securityConfig.setDefaultPrincipal(defaultPrincipal);

            Role role = new Role();
            role.setRoleName("content-administrator");
            principal = new Principal();
            principal.setClassName("org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal");
            principal.setPrincipalName("it");
            Realm realm = new Realm();
            realm.setRealmName("demo-properties-realm");
            realm.getPrincipals().add(principal);
            role.getRealms().put(realm.getRealmName(), realm);

            securityConfig.append(role);

            startWebApp(securityConfig);

            HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:5678/test/protected/hello.txt").openConnection();
            connection.setInstanceFollowRedirects(false);
            assertEquals(HttpURLConnection.HTTP_MOVED_TEMP, connection.getResponseCode());

            String cookie = connection.getHeaderField("Set-Cookie");
            cookie = cookie.substring(0, cookie.lastIndexOf(';'));
            String location = connection.getHeaderField("Location");

            connection = (HttpURLConnection) new URL(location).openConnection();
            connection.setInstanceFollowRedirects(false);
            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());

            location = location.substring(0, location.lastIndexOf('/')) + "/j_security_check?j_username=izumi&j_password=violin";

            connection = (HttpURLConnection) new URL(location).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Cookie", cookie);
            connection.setInstanceFollowRedirects(false);
            assertEquals(HttpURLConnection.HTTP_MOVED_TEMP, connection.getResponseCode());

            connection = (HttpURLConnection) new URL("http://localhost:5678/test/protected/hello.txt").openConnection();
            connection.setRequestProperty("Cookie", cookie);
            connection.setInstanceFollowRedirects(false);
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
            assertEquals("Hello World", reader.readLine());
            connection.disconnect();


            connection = (HttpURLConnection) new URL("http://localhost:5678/test/protected/hello.txt").openConnection();
            connection.setInstanceFollowRedirects(false);
            assertEquals(HttpURLConnection.HTTP_MOVED_TEMP, connection.getResponseCode());

            cookie = connection.getHeaderField("Set-Cookie");
            cookie = cookie.substring(0, cookie.lastIndexOf(';'));
            location = connection.getHeaderField("Location");

            connection = (HttpURLConnection) new URL(location).openConnection();
            connection.setInstanceFollowRedirects(false);
            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());

            location = location.substring(0, location.lastIndexOf('/')) + "/j_security_check?j_username=alan&j_password=starcraft";

            connection = (HttpURLConnection) new URL(location).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Cookie", cookie);
            connection.setInstanceFollowRedirects(false);
            assertEquals(HttpURLConnection.HTTP_MOVED_TEMP, connection.getResponseCode());

            connection = (HttpURLConnection) new URL("http://localhost:5678/test/protected/hello.txt").openConnection();
            connection.setRequestProperty("Cookie", cookie);
            connection.setInstanceFollowRedirects(false);
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
            assertEquals("Hello World", reader.readLine());
            connection.disconnect();

            stopWebApp();
        } finally {
            kernel.getProxyManager().destroyProxy(securityService);
        }
    }

    protected void startWebApp(Security securityConfig) throws Exception {
        GBeanMBean app = new GBeanMBean(JettyWebAppJACCContext.GBEAN_INFO);

        app.setAttribute("uri", URI.create("war3/"));
        app.setAttribute("componentContext", null);
        OnlineUserTransaction userTransaction = new OnlineUserTransaction();
        app.setAttribute("userTransaction", userTransaction);
        app.setAttribute("webClassPath", new URI[0]);
        app.setAttribute("contextPriorityClassLoader", Boolean.FALSE);
        app.setAttribute("configurationBaseUrl", Thread.currentThread().getContextClassLoader().getResource("deployables/"));
        app.setAttribute("securityConfig", securityConfig);
        app.setReferencePattern("SecurityService", securityServiceName);
        app.setAttribute("policyContextID", "TEST");

        app.setAttribute("contextPath", "/test");

        app.setReferencePattern("TransactionContextManager", tcmName);
        app.setReferencePattern("TrackedConnectionAssociator", tcaName);
        app.setReferencePatterns("JettyContainer", containerPatterns);

        start(appName, app);
    }

    protected void stopWebApp() throws Exception {
        stop(appName);
    }
}
