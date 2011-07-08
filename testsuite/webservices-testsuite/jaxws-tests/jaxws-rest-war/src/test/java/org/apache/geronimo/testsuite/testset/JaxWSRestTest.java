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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.geronimo.testsupport.TestSupport;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

public class JaxWSRestTest extends TestSupport {

    private String baseURL = "http://localhost:8080/";

    @Test
    public void testGET() throws Exception {
        String warName = System.getProperty("webAppName");
        assertNotNull("Web application name not specified", warName);
                        
        URL url = new URL(baseURL + warName + "/calculator?num1=10&num2=50");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            String reply = call(conn);
            
            assertEquals("responseCode", 200, conn.getResponseCode());

            InputSource is = new InputSource(new StringReader(reply));
            Document doc = parseMessage(is);
            
            Text replyMsg = findText(doc.getDocumentElement(), "60");
            assertTrue("reply message", replyMsg != null);
            
        } finally {
            conn.disconnect();
        }
    }

    @Test(dependsOnMethods = {"testGET"})
    public void testPOST() throws Exception {
        String warName = System.getProperty("webAppName");
        assertNotNull("Web application name not specified", warName);
        
        InputStream requestInput = JaxWSRestTest.class.getResourceAsStream("/request1.xml");
        assertNotNull("SOAP request not specified", requestInput);
                
        URL url = new URL(baseURL + warName + "/calculator");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            String reply = call(requestInput, conn);
            
            assertEquals("responseCode", 200, conn.getResponseCode());

            InputSource is = new InputSource(new StringReader(reply));
            Document doc = parseMessage(is);
            
            Text replyMsg = findText(doc.getDocumentElement(), "120");
            assertTrue("reply message", replyMsg != null);
            
        } finally {
            conn.disconnect();
        }
    }

    
    private Document parseMessage(InputSource is) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();

        Document doc = db.parse(is);
        return doc;
    }
    
    private Text findText(Element element, String value) {
        NodeList list = element.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node child = (Node)list.item(i);
            if (child instanceof Text) {
                Text text = (Text)child;
                if (text.getData().indexOf(value) != -1) {
                    return text;
                }
            } else if (child instanceof Element) {
                Element childEl = (Element)child;
                Text text = findText(childEl, value);
                if (text != null) {
                    return text;
                }
            }
        }   
        return null;
    }
    
    private String call(HttpURLConnection conn) throws IOException {  
        return call(null, conn);
    }

    private String call(InputStream requestInput, HttpURLConnection conn) throws IOException {  
        conn.setConnectTimeout(30 * 1000);
        conn.setReadTimeout(30 * 1000);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        
        if (requestInput == null) {
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/xml");
            
            conn.connect();
        } else {
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/xml");

            conn.connect();
            
            OutputStream out = conn.getOutputStream();

            byte[] data = new byte[1024];
            int read = 0;
            while ((read = requestInput.read(data, 0, data.length)) != -1) {
                out.write(data, 0, read);
            }
            
            requestInput.close();
            
            out.flush();
            out.close();
        }

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

}
