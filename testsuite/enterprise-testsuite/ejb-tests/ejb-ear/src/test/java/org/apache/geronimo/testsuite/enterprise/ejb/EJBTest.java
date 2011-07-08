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
package org.apache.geronimo.testsuite.enterprise.ejb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import javax.naming.InitialContext;

import org.apache.geronimo.testsupport.TestSupport;
import org.testng.annotations.Test;

import org.apache.geronimo.test.hello.ejb.HelloRemote;

public class EJBTest extends TestSupport {

    private String baseURL = "http://localhost:8080/ejbtests";

    @Test
    public void testClientInvocation() throws Exception {
        Properties p = new Properties();
    
        p.put("java.naming.factory.initial", 
              "org.apache.openejb.client.RemoteInitialContextFactory");
        p.put("java.naming.provider.url", 
              "127.0.0.1:4201");   
        
        InitialContext ctx = new InitialContext(p);
        
        HelloRemote bean = (HelloRemote)ctx.lookup("/HelloBeanRemote");
        
        String response = bean.sayHi("foo bar");
        
        System.out.println(response);
        
        assertEquals("Hello foo bar", response);

        bean.testTimerService();
    }

    @Test
    public void testInvocation1() throws Exception {
        testInvocation("/servlet1", "Hello foo");
    }

    @Test
    public void testInvocation2() throws Exception {
        testInvocation("/servlet2", "Hello bar");
    }

    private void testInvocation(String servlet, String expectedOutput) throws Exception {
        URL url = new URL(baseURL + servlet);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            String reply = call(conn);
            
            assertEquals("responseCode", 200, conn.getResponseCode());
                        
            assertTrue("expected message", reply.indexOf(expectedOutput) != -1);
            
        } finally {
            conn.disconnect();
        }
    }
    
    private String call(HttpURLConnection conn) throws IOException {        
        conn.setConnectTimeout(30 * 1000);
        conn.setReadTimeout(30 * 1000);
        conn.setUseCaches(false);

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
