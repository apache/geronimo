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
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import java.security.PermissionCollection;
import java.security.Permissions;

import javax.management.ObjectName;

import org.apache.geronimo.tomcat.util.SecurityHolder;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.security.credentialstore.CredentialStore;
import org.apache.geronimo.web.info.LoginConfigInfo;
import org.apache.geronimo.web.info.WebAppInfo;

/**
 * Tests the JAAS security for Tomcat
 *
 * @version $Revision$ $Date$
 */
public class JAASSecurityTest extends AbstractWebModuleTest {

    ObjectName appName = null;

    public void testDummy() {}

    public void xtestNotAuthorized() throws Exception {

        startWebApp();

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

    public void xtestBadAuthentication() throws Exception {

        startWebApp();

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
        String location = connector.getConnectUrl() + "/test/protected/j_security_check?j_username=alan&j_password=basspassword";

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

    public void xtestGoodAuthentication() throws Exception {
         startWebApp();

        //Begin the test
        HttpURLConnection connection = (HttpURLConnection) new URL(connector.getConnectUrl() + "/test/protected/hello.txt").openConnection();
        connection.setInstanceFollowRedirects(false);
        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());

        //Be sure we have been given the login page
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            assertEquals("<!-- Login Page -->", reader.readLine());
        } finally {
            if (reader != null)
                try {
                    reader.close();
                    reader = null;
                } catch (Exception e) {
                }
        }

        String cookie = connection.getHeaderField("Set-Cookie");
        cookie = cookie.substring(0, cookie.lastIndexOf(';'));
        String location = connector.getConnectUrl() + "/test/protected/j_security_check?j_username=izumi&j_password=violin";

        connection = (HttpURLConnection) new URL(location).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Referer",connector.getConnectUrl() + "/test/auth/logon.html?param=test");
        connection.setRequestProperty("Cookie", cookie);
        connection.setInstanceFollowRedirects(false);
        assertEquals(HttpURLConnection.HTTP_MOVED_TEMP, connection.getResponseCode());

        connection = (HttpURLConnection) new URL(connector.getConnectUrl() + "/test/protected/hello.txt").openConnection();
        connection.setRequestProperty("Cookie", cookie);
        connection.setInstanceFollowRedirects(false);
        try {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
            assertEquals("Hello World", reader.readLine());
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (Exception e) {
                }
        }
        connection.disconnect();

        stopWebApp();
    }

    protected void startWebApp() throws Exception {
        //Set a context level Realm and ignore the Engine level to test that
        //the override along with a Security Realm Name set overrides the Engine
        Map initParams = new HashMap();
        initParams.put("userClassNames", "org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal");
        initParams.put("roleClassNames", "org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal");

//        RealmGBean realm = new RealmGBean("org.apache.geronimo.tomcat.realm.TomcatJAASRealm", initParams);
//        realm.doStart();

        PermissionCollection excludedPermissions = new Permissions();
        PermissionCollection uncheckedPermissions = new Permissions();
        ComponentPermissions componentPermissions = new ComponentPermissions(excludedPermissions, uncheckedPermissions, new HashMap());
        //Force a new realm name and ignore the application name
        SecurityHolder securityHolder = new SecurityHolder();
        securityHolder.setSecurity(false);//redundant: forces use of TomcatJAASRealm not TomcatGeronimoRealm
        securityHolder.setConfigurationFactory(this.realm);
        CredentialStore credentialStore = null;
        WebAppInfo webAppInfo = new WebAppInfo();
        webAppInfo.loginConfig = new LoginConfigInfo();
        webAppInfo.loginConfig.authMethod = "FORM";
        webAppInfo.loginConfig.realmName = "geronimo-admin";
        webAppInfo.loginConfig.formLoginPage = "/auth/logon.html?param=test";
        webAppInfo.loginConfig.formErrorPage = "/auth/logonError.html?param=test";

        setUpSecureAppContext(new HashMap(),
                new HashMap(),
                componentPermissions,
                null,
                securityHolder,
                webAppInfo);
    }

    protected void stopWebApp() throws Exception {
    }

    protected void setUp() throws Exception {
        super.setUp();
        setUpSecurityService();
        super.init(null);
    }

    protected void tearDown() throws Exception {
        tearDownSecurity();
        super.tearDown();
    }

}
