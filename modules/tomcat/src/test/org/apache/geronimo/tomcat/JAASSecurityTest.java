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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javax.management.ObjectName;

import org.apache.catalina.deploy.SecurityCollection;
import org.apache.catalina.deploy.SecurityConstraint;


/**
 * Tests the JAAS security for Tomcat
 *
 * @version $Revision$ $Date$
 */
public class JAASSecurityTest extends AbstractWebModuleTest {

    ObjectName appName = null;

    public void testNotAuthorized() throws Exception {

        Set constraints = new HashSet();

        SecurityConstraint sc = new SecurityConstraint();
        sc.setAuthConstraint(true);
        sc.addAuthRole("content-administrator");
        sc.addAuthRole("auto-administrator");
        SecurityCollection coll = new SecurityCollection("Admin Role");
        coll.addPattern("/protected/*");
        sc.addCollection(coll);
        constraints.add(sc);

        sc = new SecurityConstraint();
        sc.setAuthConstraint(false);
        coll = new SecurityCollection("NO ACCESS");
        coll.addPattern("/auth/logon.html");
        sc.addCollection(coll);
        constraints.add(sc);

        Set securityRoles = new HashSet();
        securityRoles.add("content-administrator");
        securityRoles.add("auto-administrator");

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

        location = connection.getHeaderField("Location");
        connection = (HttpURLConnection) new URL(location).openConnection();
        connection.setRequestProperty("Cookie", cookie);
        connection.setInstanceFollowRedirects(true);
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, connection.getResponseCode());
        connection.disconnect();

        stopWebApp();
    }

    public void testBadAuthentication() throws Exception {

        Set constraints = new HashSet();

        SecurityConstraint sc = new SecurityConstraint();
        sc.setAuthConstraint(true);
        sc.addAuthRole("content-administrator");
        sc.addAuthRole("auto-administrator");
        SecurityCollection coll = new SecurityCollection("Admin Role");
        coll.addPattern("/protected/*");
        sc.addCollection(coll);
        constraints.add(sc);

        sc = new SecurityConstraint();
        sc.setAuthConstraint(false);
        coll = new SecurityCollection("NO ACCESS");
        coll.addPattern("/auth/logon.html");
        sc.addCollection(coll);
        constraints.add(sc);

        Set securityRoles = new HashSet();
        securityRoles.add("content-administrator");
        securityRoles.add("auto-administrator");

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

    public void testGoodAuthentication() throws Exception {

        Set constraints = new HashSet();

        SecurityConstraint sc = new SecurityConstraint();
        sc.setAuthConstraint(true);
        sc.addAuthRole("content-administrator");
        sc.addAuthRole("auto-administrator");
        SecurityCollection coll = new SecurityCollection("Admin Role");
        coll.addPattern("/protected/*");
        sc.addCollection(coll);
        constraints.add(sc);

        sc = new SecurityConstraint();
        sc.setAuthConstraint(false);
        coll = new SecurityCollection("NO ACCESS");
        coll.addPattern("/auth/logon.html");
        sc.addCollection(coll);
        constraints.add(sc);

        Set securityRoles = new HashSet();
        securityRoles.add("content-administrator");
        securityRoles.add("auto-administrator");

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
        String location = "http://localhost:8080/securetest/protected/j_security_check?j_username=izumi&j_password=violin";

        connection = (HttpURLConnection) new URL(location).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Cookie", cookie);
        connection.setInstanceFollowRedirects(false);
        assertEquals(HttpURLConnection.HTTP_MOVED_TEMP, connection.getResponseCode());

        connection = (HttpURLConnection) new URL("http://localhost:8080/securetest/protected/hello.txt").openConnection();
        connection.setRequestProperty("Cookie", cookie);
        connection.setInstanceFollowRedirects(false);
        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        assertEquals("Hello World", reader.readLine());
        connection.disconnect();

        stopWebApp();
    }

    protected void startWebApp(Set securityConstraints, Set securityRoles) throws Exception {
        appName = setUpJAASSecureAppContext(securityConstraints, securityRoles);
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
