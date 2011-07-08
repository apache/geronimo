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
package org.apache.geronimo.jaxws.wsa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import org.apache.geronimo.testsupport.TestSupport;
import org.testng.annotations.Test;
import org.xml.sax.InputSource;

public abstract class WSATest extends TestSupport {

    private String baseURL = "http://localhost:8080/";
    
    abstract String getTestServlet();
    
    private String doGET(HttpURLConnection conn) throws IOException {        
        conn.setConnectTimeout(30 * 1000);
        conn.setReadTimeout(30 * 1000);

        InputStream is = null;
        
        try {
            is = conn.getInputStream();
        } catch (IOException e) {
            is = conn.getErrorStream();
        }
        
        StringBuilder buf = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            System.out.println(inputLine);
            buf.append(inputLine);
        }
        in.close();
        
        return buf.toString();
    }
    
    @Test
    public void testPort() throws Exception {
        runTest("testPort");
    }
    
    @Test
    public void testDispatch() throws Exception {
        runTest("testDispatch");
    }
    
    @Test
    public void testReferenceProperties() throws Exception {
        runTest("testReferenceProperties");
    }
    
    @Test
    public void testReferencePropertiesDispatch() throws Exception {
        runTest("testReferencePropertiesDispatch");
    }
    
    @Test
    public void testWSDL() throws Exception {
        runTest("testWSDL");
    }

    protected void runTest(String testName) throws Exception {
        String warName = System.getProperty("webAppName");
        assertNotNull(warName);
        URL url = new URL(baseURL + warName + getTestServlet() + "?test=" + testName);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try {
            String reply = doGET(connection);
            
            assertEquals("responseCode", 200, connection.getResponseCode());
        } finally {
            connection.disconnect();
        }
    }
       
    /*
    @Test
    public void testWSDL() throws Exception {
        String warName = System.getProperty("webAppName");
        assertNotNull("Web application name not specified", warName);
        
        URL url = new URL(baseURL + warName + "calculator?wsdl");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(30 * 1000);
        conn.setReadTimeout(30 * 1000);
        try {
            conn.setUseCaches(false);

            WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
            Definition def = 
                wsdlReader.readWSDL(null, new InputSource(conn.getInputStream()));

            System.out.println("WSDL: " + def);

            assertTrue(def.getPortTypes().size() > 0);

            boolean found = false;

            Iterator iter = def.getPortTypes().entrySet().iterator();
            while (iter.hasNext()) {
                PortType portType = 
                    (PortType)((Map.Entry)iter.next()).getValue();
                
                if (found == false &&
                    portType.getOperation("greetMe", null, null) != null) {
                    found = true;
                }
            }

            assertTrue("Operation not found", found);
            
        } finally {
            conn.disconnect();
        }

    }
    */
}
