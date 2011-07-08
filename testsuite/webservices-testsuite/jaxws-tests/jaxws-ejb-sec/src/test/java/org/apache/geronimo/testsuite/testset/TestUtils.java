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
import java.net.HttpURLConnection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.geronimo.crypto.encoders.Base64;
import org.apache.geronimo.mavenplugins.geronimo.ServerProxy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

public class TestUtils {

    private static final String geronimoHome;
    
    static {
        geronimoHome = initGeronimoHome();
    }

    private static String initGeronimoHome() {
        ServerProxy server = null;
        try {
            server = new ServerProxy("localhost", 1099, "system", "manager");
        } catch (Exception e) {
            throw new RuntimeException("Unable to setup ServerProxy", e);
        }

        String home = server.getGeronimoHome();
        Throwable exception = server.getLastError();

        server.closeConnection();

        if (exception != null) {
            throw new RuntimeException("Failed to get Geronimo home", exception);
        } else {
            return home;
        }
    }

    public static String getGeronimoHome() {
        return geronimoHome;
    }
    
    public static void unset(String property, String value) {
        if (value == null) {
            System.clearProperty(property);        
        } else {
            System.setProperty(property, value);
        }
    }
    
    public static Document parseMessage(InputSource is) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();

        Document doc = db.parse(is);
        return doc;
    }

    public static Element findElement(Element element, String name) {
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
    
    public static Text findText(Element element, String value) {
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
    
    public static String getAuthorizationValue() {
        return getAuthorizationValue("system", "manager");
    }
    
    public static String getAuthorizationValue(String user, String password) {
        String userPassword = user + ":" + password;
        byte[] encodedUserPassword = Base64.encode(userPassword.getBytes());
        String encodedUserPasswordStr = new String(encodedUserPassword, 0, encodedUserPassword.length);
        return "Basic " + encodedUserPasswordStr;
    }

    public static String call(HttpURLConnection conn) throws IOException {
        return call(null, conn);
    }
    
    public static String call(InputStream requestInput, HttpURLConnection conn) throws IOException {        
        conn.setConnectTimeout(30 * 1000);
        conn.setReadTimeout(30 * 1000);
        conn.setUseCaches(false);
        
        if (requestInput != null) {
            conn.setDoOutput(true);
            conn.setDoInput(true);        
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

    public static class TestHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

}
