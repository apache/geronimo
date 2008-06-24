/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.geronimo.concurrent.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.geronimo.crypto.encoders.Base64;
import org.apache.geronimo.testsupport.TestSupport;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class ConcurrentTest extends TestSupport {

    protected String baseURL = "http://localhost:8080/";
    protected static final int TIMEOUT = 1000 * 60 * 2;
    protected JMXConnector jmxConnector;
    
    @BeforeMethod
    public void setUp() throws Exception {
        System.out.println("setUp");
        Map environment = new HashMap();
        String[] credentials = new String[]{"system", "manager"};
        environment.put(JMXConnector.CREDENTIALS, credentials);

        JMXServiceURL address =
           new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost/JMXConnector");
        this.jmxConnector = JMXConnectorFactory.connect(address, environment);
    }
    
    @AfterMethod
    public void tearDown() throws Exception {
        System.out.println("tearDown");
        jmxConnector.close();
    }
         
    abstract String getServletName();
    
    public URL getTestURL(String servletName, String testName) throws MalformedURLException {
        String warName = System.getProperty("webAppName");
        String servlet = "/" + servletName + "?testName=" + testName;
        URL url = new URL(baseURL + warName + servlet);
        return url;
    }
    
    protected void invokeTest(String testName) throws Exception {
        invokeTest(getServletName(), testName);
    }
    
    protected void invokeTest(String servletName, String testName) throws Exception {
        URL url = getTestURL(servletName, testName);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            String reply = call(conn);
            assertEquals("responseCode", 200, conn.getResponseCode());
        } finally {
            conn.disconnect();
        }
    }
    
    protected void invokeSecureTest(String testName) throws Exception {
        invokeSecureTest("secure/" + getServletName(), testName);
    }
    
    protected void invokeSecureTest(String servletName, String testName) throws Exception {
        URL url = getTestURL(servletName, testName);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();        
        conn.setRequestProperty("Authorization",
                                getAuthorizationValue("system", "manager"));                        
        try {
            String reply = call(conn);
            assertEquals("responseCode", 200, conn.getResponseCode());
        } finally {
            conn.disconnect();
        }
    }
    
    private String getAuthorizationValue(String user, String password) {
        String userPassword = user + ":" + password;
        byte[] encodedUserPassword = Base64.encode(userPassword.getBytes());
        String encodedUserPasswordStr = new String(encodedUserPassword, 0, encodedUserPassword.length);
        return "Basic " + encodedUserPasswordStr;
    }
        
    protected String call(HttpURLConnection conn) throws IOException {        
        conn.setConnectTimeout(30 * 1000);
        conn.setReadTimeout(30 * 1000);
        conn.setUseCaches(false);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "text/plain");

        InputStream is = null;
        
        try {
            is = conn.getInputStream();
        } catch (IOException e) {
            is = conn.getErrorStream();
        }
        
        StringBuffer buf = new StringBuffer();
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            System.out.println(inputLine);
            buf.append(inputLine);
        }
        in.close();
        
        return buf.toString();
    }

    
}
