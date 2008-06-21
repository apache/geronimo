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
package org.apache.geronimo.tomcat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.management.ObjectName;
import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebUserDataPermission;

import org.apache.geronimo.security.deploy.PrincipalInfo;
import org.apache.geronimo.security.deploy.Role;
import org.apache.geronimo.security.deploy.Security;
import org.apache.geronimo.security.deploy.SubjectInfo;
import org.apache.geronimo.security.deployment.GeronimoSecurityBuilderImpl;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.security.credentialstore.CredentialStore;
import org.apache.geronimo.tomcat.util.SecurityHolder;


/**
 * Tests the JACC security for Tomcat
 *
 * @version $Revision$ $Date$
 */
public class JACCSecurityTest extends AbstractWebModuleTest {

    ObjectName appName = null;

    /**
     * Test the explicit map feature.  Only Alan should be able to log in.
     *
     * @throws Exception thrown if an error in the test occurs
     */
    public void testExplicitMapping() throws Exception {

        Security securityConfig = new Security();
        securityConfig.setUseContextHandler(false);

        String securityRealmName = "demo-properties-realm";
        String defaultPrincipalId = "izumi";
        SubjectInfo defaultSubjectInfo = new SubjectInfo(securityRealmName, defaultPrincipalId);
        securityConfig.setDefaultSubjectInfo(defaultSubjectInfo);

        Role role = new Role();
        role.setRoleName("content-administrator");
        PrincipalInfo principalInfo = new PrincipalInfo("org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal", "it");
        role.getPrincipals().add(principalInfo);

        securityConfig.getRoleMappings().put(role.getRoleName(), role);

        Map<String, SubjectInfo> roleDesignates = new HashMap<String, SubjectInfo>();
        Map<String, Set<Principal>> principalRoleMap = new HashMap<String, Set<Principal>>();
        buildPrincipalRoleMap(securityConfig, roleDesignates, principalRoleMap);

        PermissionCollection uncheckedPermissions = new Permissions();
        uncheckedPermissions.add(new WebUserDataPermission("/protected/*", ""));

        PermissionCollection excludedPermissions = new Permissions();
        uncheckedPermissions.add(new WebResourcePermission("/auth/logon.html", ""));
        uncheckedPermissions.add(new WebUserDataPermission("/auth/logon.html", ""));

        Map<String, PermissionCollection> rolePermissions = new HashMap<String, PermissionCollection>();
        PermissionCollection permissions = new Permissions();
        permissions.add(new WebResourcePermission("/protected/*", ""));
        rolePermissions.put("content-administrator", permissions);
        rolePermissions.put("auto-administrator", permissions);

        ComponentPermissions componentPermissions = new ComponentPermissions(excludedPermissions, uncheckedPermissions, rolePermissions);

        startWebApp(roleDesignates, principalRoleMap, componentPermissions,
                defaultSubjectInfo, permissions);

        //Begin the test
        HttpURLConnection connection = (HttpURLConnection) new URL(connector.getConnectUrl() + "/test/protected/hello.txt").openConnection();
        connection.setInstanceFollowRedirects(false);
        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());

        //Be sure we have been given the login page
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        assertEquals("<!-- Login Page -->", reader.readLine());
        reader.close();

        String cookie = connection.getHeaderField("Set-Cookie");
        cookie = cookie.substring(0, cookie.lastIndexOf(';'));
        String location = connector.getConnectUrl() + "/test/protected/j_security_check?j_username=alan&j_password=starcraft";

        connection = (HttpURLConnection) new URL(location).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Referer", connector.getConnectUrl() + "/test/auth/logon.html?param=test");
        connection.setRequestProperty("Cookie", cookie);
        connection.setInstanceFollowRedirects(false);
        assertEquals(HttpURLConnection.HTTP_MOVED_TEMP, connection.getResponseCode());

        connection = (HttpURLConnection) new URL(connector.getConnectUrl() + "/test/protected/hello.txt").openConnection();
        connection.setRequestProperty("Cookie", cookie);
        connection.setInstanceFollowRedirects(false);
        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        assertEquals("Hello World", reader.readLine());
        connection.disconnect();

        //Now lets try it with izumi
        connection = (HttpURLConnection) new URL(connector.getConnectUrl() + "/test/protected/hello.txt").openConnection();
        connection.setInstanceFollowRedirects(false);
        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());

        cookie = connection.getHeaderField("Set-Cookie");
        cookie = cookie.substring(0, cookie.lastIndexOf(';'));

        //Be sure we have been given the login page
        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        assertEquals("<!-- Login Page -->", reader.readLine());
        reader.close();

        location = connector.getConnectUrl() + "/test/protected/j_security_check?j_username=izumi&j_password=violin";

        connection = (HttpURLConnection) new URL(location).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Cookie", cookie);
        connection.setInstanceFollowRedirects(false);
        assertEquals(HttpURLConnection.HTTP_MOVED_TEMP, connection.getResponseCode());

        try {
            connection = (HttpURLConnection) new URL(connector.getConnectUrl() + "/test/protected/hello.txt").openConnection();
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

    protected TomcatWebAppContext startWebApp(
            Map roleDesignates,
            Map principalRoleMap,
            ComponentPermissions componentPermissions,
            SubjectInfo defaultPrincipal,
            PermissionCollection checked) throws Exception {

        SecurityHolder securityHolder = new SecurityHolder();
        securityHolder.setSecurity(true);
        securityHolder.setPolicyContextID(POLICY_CONTEXT_ID);
//        securityHolder.setDefaultSubject(defaultPrincipal);
        securityHolder.setSecurityRealm(securityRealmName);
        CredentialStore credentialStore = null;
        return setUpSecureAppContext(roleDesignates,
                principalRoleMap,
                componentPermissions,
                null,
                securityHolder,
                credentialStore);
    }

    protected void stopWebApp() throws Exception {
    }

    public void buildPrincipalRoleMap(Security security, Map<String, SubjectInfo> roleDesignates, Map<String, Set<Principal>> principalRoleMap) {
        Map roleToPrincipalMap = new HashMap();
        GeronimoSecurityBuilderImpl.buildRolePrincipalMap(security, roleToPrincipalMap, getClass().getClassLoader());
        invertMap(roleToPrincipalMap, principalRoleMap);
    }

    private static Map invertMap(Map<String, Set<Principal>> roleToPrincipalMap, Map principalRoleMapping) {
        for (Iterator roles = roleToPrincipalMap.entrySet().iterator(); roles.hasNext();) {
            Map.Entry entry = (Map.Entry) roles.next();
            String role = (String) entry.getKey();
            Set principals = (Set) entry.getValue();
            for (Iterator iter = principals.iterator(); iter.hasNext();) {
                java.security.Principal principal = (java.security.Principal) iter.next();

                HashSet roleSet = (HashSet) principalRoleMapping.get(principal);
                if (roleSet == null) {
                    roleSet = new HashSet();
                    principalRoleMapping.put(principal, roleSet);
                }
                roleSet.add(role);
            }
        }
        return principalRoleMapping;
    }

    protected void setUp() throws Exception {
        super.setUp();
        super.init("org.apache.geronimo.tomcat.realm.TomcatGeronimoRealm");
        setUpSecurity();
    }

    protected void tearDown() throws Exception {
        tearDownSecurity();
        super.tearDown();
    }

}
