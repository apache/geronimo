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
package org.apache.geronimo.tomcat;

import javax.management.ObjectName;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.catalina.deploy.SecurityCollection;
import org.apache.catalina.deploy.SecurityConstraint;


/**
 * Tests the JAAS security for Tomcat
 *
 * @version $Rev: 111239 $ $Date: 2004-12-08 02:29:11 -0700 (Wed, 08 Dec 2004) $
 */
public class SecurityTest extends AbstractWebModuleTest {

    ObjectName appName = null;
    /**
     * Test the explicit map feature.  Only Alan should be able to log in.
     * TODO uncomment this code and get it working when JACC is enabled.
     * @throws Exception thrown if an error in the test occurs
     */
    /*
    public void xtestExplicitMapping() throws Exception {
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

        Set uncheckedPermissions = new HashSet();
        Set excludedPermissions = new HashSet();
        Map rolePermissions = new HashMap();
        Set securityRoles = new HashSet();
        Map legacySecurityConstraintMap = new HashMap();

        startWebApp(securityConfig, uncheckedPermissions, excludedPermissions, rolePermissions, securityRoles, legacySecurityConstraintMap);

        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:8080/test/protected/hello.txt").openConnection();
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

        connection = (HttpURLConnection) new URL("http://localhost:8080/test/protected/hello.txt").openConnection();
        connection.setRequestProperty("Cookie", cookie);
        connection.setInstanceFollowRedirects(false);
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        assertEquals("Hello World", reader.readLine());
        connection.disconnect();

        connection = (HttpURLConnection) new URL("http://localhost:8080/test/protected/hello.txt").openConnection();
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
            connection = (HttpURLConnection) new URL("http://localhost:8080/test/protected/hello.txt").openConnection();
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
    */

    /**
     * Mixed the auto map and the standard explicit map.  Both Alan and Izumi
     * should be able to login.
     *
     * @throws Exception thrown if an error in the test occurs
     */
    /*
    public void xtestMixedMapping() throws Exception {

        Security securityConfig = new Security();
        securityConfig.setUseContextHandler(false);

        AutoMapAssistant assistant = new AutoMapAssistant();
        assistant.setSecurityRealm("demo-properties-realm");
        securityConfig.setAssistant(assistant);

        securityConfig.getRoleNames().add("content-administrator");
        securityConfig.getRoleNames().add("auto-administrator");

        SecurityService securityService = (SecurityService) kernel.getProxyManager().createProxy(securityServiceName, SecurityService.class);
        try {
            securityConfig.autoGenerate(securityService);
        } finally {
            kernel.getProxyManager().destroyProxy(securityService);
        }

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

        Set uncheckedPermissions = new HashSet();
        Set excludedPermissions = new HashSet();
        Map rolePermissions = new HashMap();
        Set securityRoles = new HashSet();
        Map legacySecurityConstraintMap = new HashMap();

        startWebApp(securityConfig, uncheckedPermissions, excludedPermissions, rolePermissions, securityRoles, legacySecurityConstraintMap);

        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:8080/test/protected/hello.txt").openConnection();
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

        connection = (HttpURLConnection) new URL("http://localhost:8080/test/protected/hello.txt").openConnection();
        connection.setRequestProperty("Cookie", cookie);
        connection.setInstanceFollowRedirects(false);
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        assertEquals("Hello World", reader.readLine());
        connection.disconnect();


        connection = (HttpURLConnection) new URL("http://localhost:8080/test/protected/hello.txt").openConnection();
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

        connection = (HttpURLConnection) new URL("http://localhost:8080/test/protected/hello.txt").openConnection();
        connection.setRequestProperty("Cookie", cookie);
        connection.setInstanceFollowRedirects(false);
        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        assertEquals("Hello World", reader.readLine());
        connection.disconnect();

        stopWebApp();
    }
    */

    public void testNotAuthorized() throws Exception {

        SecurityConstraint[] constraints = new SecurityConstraint[2];

        SecurityConstraint sc = new SecurityConstraint();
        sc.setAuthConstraint(true);
        sc.addAuthRole("content-administrator");
        sc.addAuthRole("auto-administrator");
        SecurityCollection coll = new SecurityCollection("Admin Role");
        coll.addPattern("/protected/*");
        sc.addCollection(coll);
        constraints[0] = sc;

        sc = new SecurityConstraint();
        sc.setAuthConstraint(false);
        coll = new SecurityCollection("NO ACCESS");
        coll.addPattern("/auth/logon.html");
        sc.addCollection(coll);
        constraints[1] = sc;

        String[] securityRoles = new String[2];
        securityRoles[0] = "content-administrator";
        securityRoles[1] = "auto-administrator";

        startWebApp(constraints, securityRoles);

        //Begin the test
        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:8080/securetest/protected/hello.txt").openConnection();
        connection.setInstanceFollowRedirects(false);
        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        //Be sure we have been given the login page
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        assertEquals("<!-- Login Page -->", reader.readLine());
        reader.close();

        String cookie = connection.getHeaderField("Set-Cookie");
        cookie = cookie.substring(0, cookie.lastIndexOf(';'));
        String location = "http://localhost:8080/securetest/protected/j_security_check?j_username=alan&j_password=starcraft";
        connection = (HttpURLConnection) new URL(location).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Cookie", cookie);
        connection.setInstanceFollowRedirects(false);
        assertEquals(HttpURLConnection.HTTP_MOVED_TEMP, connection.getResponseCode());

        // TODO This really should pass.  It will pass when we put in JACC
        location = connection.getHeaderField("Location");
        connection = (HttpURLConnection) new URL(location).openConnection();
        connection.setRequestProperty("Cookie", cookie);
        connection.setInstanceFollowRedirects(true);
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, connection.getResponseCode());
        connection.disconnect();

        stopWebApp();
    }

    public void testBadAuthentication() throws Exception {

        SecurityConstraint[] constraints = new SecurityConstraint[2];

        SecurityConstraint sc = new SecurityConstraint();
        sc.setAuthConstraint(true);
        sc.addAuthRole("content-administrator");
        sc.addAuthRole("auto-administrator");
        SecurityCollection coll = new SecurityCollection("Admin Role");
        coll.addPattern("/protected/*");
        sc.addCollection(coll);
        constraints[0] = sc;

        sc = new SecurityConstraint();
        sc.setAuthConstraint(false);
        coll = new SecurityCollection("NO ACCESS");
        coll.addPattern("/auth/logon.html");
        sc.addCollection(coll);
        constraints[1] = sc;

        String[] securityRoles = new String[2];
        securityRoles[0] = "content-administrator";
        securityRoles[1] = "auto-administrator";

        startWebApp(constraints, securityRoles);

        //Begin the test
        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:8080/securetest/protected/hello.txt").openConnection();
        connection.setInstanceFollowRedirects(false);
        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());

        //Be sure we have been given the login page
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        assertEquals("<!-- Login Page -->", reader.readLine());
        reader.close();

        String cookie = connection.getHeaderField("Set-Cookie");
        cookie = cookie.substring(0, cookie.lastIndexOf(';'));
        String location = "http://localhost:8080/securetest/protected/j_security_check?j_username=alan&j_password=basspassword";

        connection = (HttpURLConnection) new URL(location).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Cookie", cookie);
        connection.setInstanceFollowRedirects(true);

        //Be sure we have been given the login error page
        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());

        location = connection.getHeaderField("Location");
        assertEquals("<!-- Not Authorized -->", reader.readLine());
        reader.close();

        connection.disconnect();

        stopWebApp();
    }

    protected void startWebApp(SecurityConstraint[] securityConstraints, String[] securityRoles) throws Exception {
        appName = setUpSecureAppContext(securityConstraints, securityRoles);
    }

    protected void stopWebApp() throws Exception {
        stop(appName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        setUpSecurity();
    }

    protected void tearDown() throws Exception {
        tearDownSecurity();
        super.tearDown();
    }

}
