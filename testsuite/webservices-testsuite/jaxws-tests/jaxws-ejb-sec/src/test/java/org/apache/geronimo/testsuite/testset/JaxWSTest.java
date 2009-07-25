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

import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import org.apache.geronimo.testsupport.TestSupport;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

public class JaxWSTest extends TestSupport {

    private String baseHttpUrl = "http://localhost:8080/";
    private String baseHttpsUrl = "https://localhost:8443/";

    @Test
    public void testInvokeBasic() throws Exception {
        invokeBasic("BeanBasic/ejb");
    }
        
    @Test
    public void testInvokeBasicAllowGet() throws Exception {
        invokeBasic("BeanBasicAllowGet/ejb");
    }
    
    private void invokeBasic(String address) throws Exception {
        URL url = new URL(baseHttpUrl + address);
                
        InputStream requestInput;
        HttpURLConnection conn;
        
        // send request WITHOUT basic authentication info
        requestInput = JaxWSTest.class.getResourceAsStream("/request1.xml");
        assertNotNull("SOAP request not specified", requestInput);
        
        conn = (HttpURLConnection) url.openConnection();
        try {
            String reply = TestUtils.call(requestInput, conn);            
            assertEquals("responseCode", 401, conn.getResponseCode());
        } finally {
            conn.disconnect();
        }
        
        // send request WITH basic authentication info
        requestInput = JaxWSTest.class.getResourceAsStream("/request1.xml");
        assertNotNull("SOAP request not specified", requestInput);
        
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", TestUtils.getAuthorizationValue());
        try {
            checkResponse(requestInput, conn);
        } finally {
            conn.disconnect();
        }
    }
    
    @Test
    public void testInvokeHttps() throws Exception {
        invokeHttps("BeanHttps/ejb");
    }
    
    @Test
    public void testInvokeHttpsAllowGet() throws Exception {
        invokeHttps("BeanHttpsAllowGet/ejb");
    }
    
    private void invokeHttps(String address) throws Exception {
        URL url;
        InputStream requestInput;
        
        // send request over http
        url = new URL(baseHttpUrl + address);
        
        requestInput = JaxWSTest.class.getResourceAsStream("/request1.xml");
        assertNotNull("SOAP request not specified", requestInput);
        
        HttpURLConnection conn1 = (HttpURLConnection) url.openConnection();
        try {
            String reply = TestUtils.call(requestInput, conn1);            
            assertEquals("responseCode", 403, conn1.getResponseCode());
        } finally {
            conn1.disconnect();
        }
        
        // send requests over https        
        String oldTrustStore = System.getProperty("javax.net.ssl.trustStore");
        String oldTrustStorePassword = System.getProperty("javax.net.ssl.trustStorePassword");
        
        File keyStore = new File(TestUtils.getGeronimoHome(), "var/security/keystores/geronimo-default");        
        System.setProperty("javax.net.ssl.trustStore", keyStore.getAbsolutePath());
        System.setProperty("javax.net.ssl.trustStorePassword", "secret");
        
        url = new URL(baseHttpsUrl + address);
        
        requestInput = JaxWSTest.class.getResourceAsStream("/request1.xml");
        assertNotNull("SOAP request not specified", requestInput);
        
        HttpsURLConnection conn2 = (HttpsURLConnection) url.openConnection();
        conn2.setHostnameVerifier(new TestUtils.TestHostnameVerifier());
        try {
            checkResponse(requestInput, conn2);
        } finally {
            TestUtils.unset("javax.net.ssl.trustStore", oldTrustStore);
            TestUtils.unset("javax.net.ssl.trustStorePassword", oldTrustStorePassword);
            
            conn2.disconnect();
        }
    }

    private void checkResponse(InputStream requestInput, HttpURLConnection conn) throws Exception {
        String reply = TestUtils.call(requestInput, conn);
        
        assertEquals("responseCode", 200, conn.getResponseCode());
        String contentType = conn.getHeaderField("Content-Type");
        assertTrue("contentType", contentType.indexOf("text/xml") != -1);
                    
        InputSource is = new InputSource(new StringReader(reply));
        Document doc = TestUtils.parseMessage(is);
        
        Text replyMsg = TestUtils.findText(doc.getDocumentElement(), "Hello foo bar");
        assertTrue("reply message", replyMsg != null);
    }
    
    @Test
    public void testWSDLBasic() throws Exception {
        URL url = new URL(baseHttpUrl + "BeanBasic/ejb?wsdl");
        
        HttpURLConnection conn;
        
        // get wsdl WITHOUT basic authentication info
        conn = (HttpURLConnection) url.openConnection();
        try {
            String reply = TestUtils.call(conn);            
            assertEquals("responseCode", 401, conn.getResponseCode());
        } finally {
            conn.disconnect();
        }
        
        // get wsdl WITH basic authentication info
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", TestUtils.getAuthorizationValue());
        try {
            checkWSDL(conn);
        } finally {
            conn.disconnect();
        }
    }
    
    @Test
    public void testWSDLBasicAllowGet() throws Exception {
        URL url = new URL(baseHttpUrl + "BeanBasicAllowGet/ejb?wsdl");
        
        HttpURLConnection conn;
        
        // get wsdl WITHOUT basic authentication info
        conn = (HttpURLConnection) url.openConnection();
        try {
            checkWSDL(conn);
        } finally {
            conn.disconnect();
        }
    }
    
    @Test
    public void testWSDLHttps() throws Exception {
        URL url;
        
        // get wsdl over http
        url = new URL(baseHttpUrl + "BeanHttps/ejb?wsdl");
                
        HttpURLConnection conn1 = (HttpURLConnection) url.openConnection();
        try {
            String reply = TestUtils.call(conn1);            
            assertEquals("responseCode", 403, conn1.getResponseCode());
        } finally {
            conn1.disconnect();
        }
        
        // get wsdl over https        
        String oldTrustStore = System.getProperty("javax.net.ssl.trustStore");
        String oldTrustStorePassword = System.getProperty("javax.net.ssl.trustStorePassword");
        
        File keyStore = new File(TestUtils.getGeronimoHome(), "var/security/keystores/geronimo-default");        
        System.setProperty("javax.net.ssl.trustStore", keyStore.getAbsolutePath());
        System.setProperty("javax.net.ssl.trustStorePassword", "secret");
        
        url = new URL(baseHttpsUrl + "BeanHttps/ejb?wsdl");
                
        HttpsURLConnection conn2 = (HttpsURLConnection) url.openConnection();
        conn2.setHostnameVerifier(new TestUtils.TestHostnameVerifier());
        try {
            checkWSDL(conn2);
        } finally {
            TestUtils.unset("javax.net.ssl.trustStore", oldTrustStore);
            TestUtils.unset("javax.net.ssl.trustStorePassword", oldTrustStorePassword);
            
            conn2.disconnect();
        }
    }
    
    @Test
    public void testWSDLHttpsAllowGet() throws Exception {
        URL url;
        
        // get wsdl over http
        url = new URL(baseHttpUrl + "BeanHttpsAllowGet/ejb?wsdl");
                
        HttpURLConnection conn1 = (HttpURLConnection) url.openConnection();
        try {
            checkWSDL(conn1);
        } finally {
            conn1.disconnect();
        }
    }
    
    private void checkWSDL(HttpURLConnection conn) throws Exception {
        conn.setConnectTimeout(30 * 1000);
        conn.setReadTimeout(30 * 1000);
        conn.setUseCaches(false);

        WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
        wsdlReader.setFeature("javax.wsdl.importDocuments", false);
        Definition def = wsdlReader.readWSDL(null, new InputSource(conn.getInputStream()));

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
    }
}
