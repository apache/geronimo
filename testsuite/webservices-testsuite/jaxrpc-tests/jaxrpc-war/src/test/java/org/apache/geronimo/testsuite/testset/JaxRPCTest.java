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
package org.apache.geronimo.testsuite.testset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.xml.WSDLReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import org.xml.sax.InputSource;

import org.apache.geronimo.testsupport.TestSupport;
import org.testng.annotations.Test;

public class JaxRPCTest extends TestSupport {

    private String baseURL = "http://localhost:8080/";

    @Test
    public void testInvocation() throws Exception {
        String warName = System.getProperty("webAppName");
        assertNotNull("Web application name not specified", warName);
        
        InputStream requestInput = JaxRPCTest.class.getResourceAsStream("/request1.xml");
        assertNotNull("SOAP request not specified", requestInput);
                
        URL url = new URL(baseURL + warName + "/servlet");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(30 * 1000);
        conn.setReadTimeout(30 * 1000);
        try {
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/xml");

            OutputStream out = conn.getOutputStream();

            byte[] data = new byte[1024];
            int read = 0;
            while ((read = requestInput.read(data, 0, data.length)) != -1) {
                out.write(data, 0, read);
            }

            requestInput.close();

            out.flush();
            out.close();

            boolean found = false;

            // FIXME: Simple test is required for now, as CXF has problems
            BufferedReader in = 
                new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);

                if (found == false &&
                    inputLine.indexOf("Hello foo bar") != -1) {
                    found = true;
                }
            }
            in.close();

            assertTrue("Reply", found);

        } finally {
            conn.disconnect();
        }
    }

    @Test
    public void testWSDL() throws Exception {
        String warName = System.getProperty("webAppName");
        assertNotNull("Web application name not specified", warName);
        
        URL url = new URL(baseURL + warName + "/servlet?wsdl");
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

}
