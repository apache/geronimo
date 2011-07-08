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
import java.util.Iterator;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
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

public class JaxWSTest extends TestSupport {

    private String baseURL = "http://localhost:8080/";

    
    @Test
    public void testInvocation1() throws Exception {
        // service without WSDL
        Document doc = getResponse("/servlet1", "/request3.xml");
        Text replyMsg = findText(doc.getDocumentElement(), "Hello foo bar");
        if (replyMsg == null) {
            // XXX: work-around for CXF, wants <requestType> to be unqualified
            doc = getResponse("/servlet1", "/request1.xml");
            replyMsg = findText(doc.getDocumentElement(), "Hello foo bar");
        }
        assertTrue("reply message", replyMsg != null);
    }

    @Test
    public void testInvocation2() throws Exception { 
        // service with WSDL
        Document doc = getResponse("/servlet2", "/request3.xml");
        Text replyMsg = findText(doc.getDocumentElement(), "Hello foo bar");
        assertTrue("reply message", replyMsg != null);
    }

    private Document getResponse(String servlet, String requestFile) throws Exception {
        String warName = System.getProperty("webAppName");
        assertNotNull("Web application name not specified", warName);
        
        InputStream requestInput = JaxWSTest.class.getResourceAsStream(requestFile);
        assertNotNull("SOAP request not found: " + requestFile, requestInput);
                
        URL url = new URL(baseURL + warName + servlet);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            String reply = call(requestInput, conn);
            
            assertEquals("responseCode", 200, conn.getResponseCode());
            String contentType = conn.getHeaderField("Content-Type");
            assertTrue("contentType", contentType.indexOf("text/xml") != -1);
                        
            InputSource is = new InputSource(new StringReader(reply));
            Document doc = parseMessage(is);
            
            return doc;
            
        } finally {
            conn.disconnect();
        }
    }
    
    @Test
    public void testInvocationFault1() throws Exception {
        testInvocationFault("/servlet1");
    }

    @Test
    public void testInvocationFault2() throws Exception {
        testInvocationFault("/servlet2");
    }
    
    private void testInvocationFault(String servlet) throws Exception {
        String warName = System.getProperty("webAppName");
        assertNotNull("Web application name not specified", warName);
        
        InputStream requestInput = JaxWSTest.class.getResourceAsStream("/request2.xml");
        assertNotNull("SOAP request not specified", requestInput);
                
        URL url = new URL(baseURL + warName + servlet);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            String reply = call(requestInput, conn);

            assertEquals("responseCode", 500, conn.getResponseCode());
            String contentType = conn.getHeaderField("Content-Type");
            assertTrue("contentType", contentType.indexOf("text/xml") != -1);
                        
            InputSource is = new InputSource(new StringReader(reply));
            Document doc = parseMessage(is);
            
            Element faultString = findElement(doc.getDocumentElement(), "faultstring");
            assertTrue("faultString", faultString != null);
            assertEquals("faultString value", "my error", faultString.getFirstChild().getNodeValue());
            
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

    private Element findElement(Element element, String name) {
        NodeList list = element.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node child = (Node)list.item(i);
            if (child instanceof Element) {
                Element childEl = (Element)child;
                if (name.equals(childEl.getLocalName())) {
                    return childEl;
                } else {
                    childEl = findElement(childEl, name);
                    if (childEl != null) {
                        return childEl;
                    }
                }
            }
        }
        return null;
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
    
    private String call(InputStream requestInput, HttpURLConnection conn) throws IOException {        
        conn.setConnectTimeout(30 * 1000);
        conn.setReadTimeout(30 * 1000);
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
    public void testWSDL1() throws Exception {
        testWSDL("/servlet1");
    }

    @Test
    public void testWSDL2() throws Exception { 
        testWSDL("/servlet2");
    }

    private void testWSDL(String servlet) throws Exception {

        String warName = System.getProperty("webAppName");
        assertNotNull("Web application name not specified", warName);
        
        URL url = new URL(baseURL + warName + servlet + "?wsdl");
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

    @Test
    public void testClientInvocation() throws Exception {
        String expected = "Hello Tester";

        String warName = System.getProperty("webAppName");
        assertNotNull(warName);
        URL url = new URL(baseURL + warName + "/JAXWSClient.jsp?name=Tester");
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setConnectTimeout(30 * 1000);
        connection.setReadTimeout(30 * 1000);
        try {
            BufferedReader reader = 
                new BufferedReader(new InputStreamReader(connection.getInputStream()));
            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
            BufferedReader in = 
                new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            boolean found = false;
            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);

                if (found == false &&
                    inputLine.indexOf("WebService returned: " + expected) != -1) {
                    found = true;
                }
            }
            in.close();

            assertTrue("Reply", found);

        } finally {
            connection.disconnect();
        }
    }



}
