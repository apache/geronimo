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

import org.apache.geronimo.tomcat.app.MockWebServiceContainer;
import org.apache.geronimo.crypto.encoders.Base64;


/**
 * @version $Rev$ $Date$
 */
public class ContainerTest extends AbstractWebModuleTest {

    public void testWebServiceHandler() throws Exception {

        String contextPath = "/foo/webservice.ws";
        MockWebServiceContainer webServiceInvoker = new MockWebServiceContainer();
        container.addWebService(contextPath, null, webServiceInvoker, null, null, null, null, null, cl);
        HttpURLConnection connection = (HttpURLConnection) new URL(connector.getConnectUrl() + contextPath).openConnection();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
            assertEquals("Hello World", reader.readLine());
        } finally {
            connection.disconnect();
            if (reader != null)
                try {
                    reader.close();
                } catch (Exception e) {
                }
        }
        container.removeWebService(contextPath);
        connection = (HttpURLConnection) new URL(connector.getConnectUrl() + contextPath).openConnection();
        try {
            connection.getInputStream();
            fail();
        } catch (Exception e) {
            // see if we removed the ws.
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, connection.getResponseCode());
            connection.disconnect();
        }
    }

    public void xtestSecureWebServiceHandler() throws Exception {

        setUpSecurityService();

        String contextPath = "/foo/webservice.ws";
        MockWebServiceContainer webServiceInvoker = new MockWebServiceContainer();
        container.addWebService(contextPath, null, webServiceInvoker, "ContextID", realm, securityRealmName, "BASIC", null, cl);

        //Veryify its secured
        HttpURLConnection connection = (HttpURLConnection) new URL(connector.getConnectUrl() + contextPath).openConnection();
        try {
            connection.getInputStream();
            fail();
        } catch (Exception e) {
            assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, connection.getResponseCode());
        } finally {
            connection.disconnect();
        }

        //Authenticate
        connection = (HttpURLConnection) new URL(connector.getConnectUrl() + contextPath).openConnection();
        String authentication = new String(Base64.encode("alan:starcraft".getBytes()));
        connection.setRequestProperty("Authorization", "Basic " + authentication);
        BufferedReader reader = null;
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
            connection.disconnect();
        }
        container.removeWebService(contextPath);
        connection = (HttpURLConnection) new URL(connector.getConnectUrl() + contextPath).openConnection();
        try {
            connection.getInputStream();
            fail();
        } catch (Exception e) {
            // see if we removed the ws.
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, connection.getResponseCode());
            connection.disconnect();
        }

    }


    protected void setUp() throws Exception {
        super.setUp();
        super.init(null);
    }

}
