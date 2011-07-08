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
package org.apache.geronimo.jaxws.catalog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.testng.annotations.Test;

public class WebCatalogTest extends CatalogTest {

    protected String getTestServletContext() {
        return "/catalog-war";
    }
       
    @Test
    public void testClient() throws Exception {
        URL url = new URL(baseURL + getTestServletContext() + "/greeter-client");
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try {
            String reply = doGET(connection);
            
            assertEquals("responseCode", 200, connection.getResponseCode());
            assertTrue(reply.indexOf("OK") != -1);
        } finally {
            connection.disconnect();
        }

    }
    
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

}
