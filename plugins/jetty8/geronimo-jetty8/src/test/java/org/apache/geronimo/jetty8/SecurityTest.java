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

package org.apache.geronimo.jetty8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebUserDataPermission;

import org.apache.geronimo.security.deploy.SubjectInfo;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal;


/**
 * Tests the JAAC security for Jetty by using both explicit and auto role mapping
 *
 * @version $Rev$ $Date$
 */
public class SecurityTest extends AbstractWebModuleTest {

    /**
     * Test the explicit map feature.  Only Alan should be able to log in.
     *
     * @throws Exception thrown if an error in the test occurs
     */
    public void testExplicitMapping() throws Exception {
        if (1 == 1) return;

        String securityRealmName = "demo-properties-realm";
        String defaultPrincipalId = "izumi";
        SubjectInfo defaultSubjectInfo = new SubjectInfo(securityRealmName, defaultPrincipalId);

        Map<String, SubjectInfo> roleDesignates = Collections.emptyMap();
        Map<Principal, Set<String>> principalRoleMap = Collections.singletonMap((Principal)new GeronimoGroupPrincipal("it"), Collections.singleton("content-administrator"));

        PermissionCollection uncheckedPermissions = new Permissions();
        uncheckedPermissions.add(new WebUserDataPermission("/protected/*", ""));

        PermissionCollection excludedPermissions = new Permissions();
        uncheckedPermissions.add(new WebResourcePermission("/auth/logon.html", ""));
        uncheckedPermissions.add(new WebUserDataPermission("/auth/logon.html", ""));
//        uncheckedPermissions.add(new WebResourcePermission("/auth/j_security_check", ""));
        uncheckedPermissions.add(new WebUserDataPermission("/auth/j_security_check", ""));

        Map<String, PermissionCollection> rolePermissions = new HashMap<String, PermissionCollection>();
        PermissionCollection permissions = new Permissions();
        permissions.add(new WebResourcePermission("/protected/*", ""));
        rolePermissions.put("content-administrator", permissions);
        rolePermissions.put("auto-administrator", permissions);

        Set<String> securityRoles = new HashSet<String>();
        securityRoles.add("content-administrator");
        securityRoles.add("auto-administrator");

        ComponentPermissions componentPermissions = new ComponentPermissions(excludedPermissions, uncheckedPermissions, rolePermissions);

        startWebApp(roleDesignates, principalRoleMap, componentPermissions, defaultSubjectInfo, permissions, securityRoles);

        HttpURLConnection connection = (HttpURLConnection) new URL(hostURL + "/test/protected/hello.txt").openConnection();
        connection.setInstanceFollowRedirects(false);
//        assertEquals(HttpURLConnection.HTTP_MOVED_TEMP, connection.getResponseCode());

        String cookie = connection.getHeaderField("Set-Cookie");
        cookie = cookie == null? "": cookie.substring(0, cookie.lastIndexOf(';'));
//        String location = connection.getHeaderField("Location");
//
//        connection = (HttpURLConnection) new URL(location).openConnection();
//        connection.setInstanceFollowRedirects(false);
//        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());

        String location = hostURL + "/test/protected/j_security_check?j_username=alan&j_password=starcraft";

        connection = (HttpURLConnection) new URL(location).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Cookie", cookie);
        connection.setInstanceFollowRedirects(false);
        assertEquals(HttpURLConnection.HTTP_MOVED_TEMP, connection.getResponseCode());

        connection = (HttpURLConnection) new URL(hostURL + "/test/protected/hello.txt").openConnection();
        connection.setRequestProperty("Cookie", cookie);
        connection.setInstanceFollowRedirects(false);
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        assertEquals("Hello World", reader.readLine());
        connection.disconnect();

        //make sure that leaving out the session id makes us try to login again.
        connection = (HttpURLConnection) new URL(hostURL + "/test/protected/hello.txt").openConnection();
        connection.setInstanceFollowRedirects(false);
//        connection.setRequestProperty("Cookie", cookie);
//        assertEquals(HttpURLConnection.HTTP_MOVED_TEMP, connection.getResponseCode());

        //new cookie for new session
        cookie = connection.getHeaderField("Set-Cookie");
        cookie = cookie.substring(0, cookie.lastIndexOf(';'));
//        location = connection.getHeaderField("Location");

//        connection = (HttpURLConnection) new URL(location).openConnection();
//        connection.setInstanceFollowRedirects(false);
//        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());

        location = hostURL + "/test/protected/j_security_check?j_username=izumi&j_password=violin";

        connection = (HttpURLConnection) new URL(location).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Cookie", cookie);
        connection.setInstanceFollowRedirects(false);
//        assertEquals(HttpURLConnection.HTTP_MOVED_TEMP, connection.getResponseCode());

        try {
            //izumi is not authorized for /protected/*
            connection = (HttpURLConnection) new URL(hostURL + "/test/protected/hello.txt").openConnection();
            connection.setRequestProperty("Cookie", cookie);
            connection.setInstanceFollowRedirects(false);
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

//            fail("Should throw an IOException for HTTP 403 response");
        } catch (IOException e) {
        }

//        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, connection.getResponseCode());
        connection.disconnect();

        stopWebApp();
    }

    protected void startWebApp(Map<String, SubjectInfo> roleDesignates, Map<Principal, Set<String>> principalRoleMap, ComponentPermissions componentPermissions, SubjectInfo defaultSubjectInfo, PermissionCollection checked, Set securityRoles) throws Exception {
        setUpSecureAppContext(securityRealmName, roleDesignates, principalRoleMap, componentPermissions, defaultSubjectInfo, checked, securityRoles);
    }

    protected void stopWebApp() throws Exception {
//        stop(appName);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        tearDownSecurity();
        super.tearDown();
    }
}
