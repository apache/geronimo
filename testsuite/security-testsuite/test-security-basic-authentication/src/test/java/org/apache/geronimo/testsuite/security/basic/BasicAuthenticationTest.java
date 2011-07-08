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
package org.apache.geronimo.testsuite.security.basic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.geronimo.testsupport.TestSupport;
import org.apache.xmlbeans.impl.util.Base64;
import org.testng.annotations.Test;
public class BasicAuthenticationTest extends TestSupport{

    private String baseURL = "http://localhost:8080/";
    private String getAuthorizationValue(String user, String password) {
        String userPassword = user + ":" + password;
        byte[] encodedUserPassword = Base64.encode(userPassword.getBytes());
        String encodedUserPasswordStr = new String(encodedUserPassword, 0, encodedUserPassword.length);
        return "Basic " + encodedUserPasswordStr;
    }
    private String getResponse(URL url,String username,String password) throws IOException{
    	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    	conn.setRequestProperty("Authorization",getAuthorizationValue(username,password));
    	conn.setRequestMethod("GET");
    	conn.setConnectTimeout(30 * 1000);
        conn.setReadTimeout(30 * 1000);
        conn.setUseCaches(false);
        conn.connect();
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
           buf.append(inputLine);
      }
      in.close();
      return buf.toString();
    }
   @Test
    public void testAdminLogin() throws Exception {
	    String appContextStr = System.getProperty("appContext");
    	URL url=new URL(baseURL+appContextStr+"/admin/admin.jsp");
    	String response=getResponse(url,"george","bone");
        assertTrue("admin", response.contains("You are in 'admin' role."));
    }

   @Test
   public void testUserLogin() throws Exception {
	   String appContextStr = System.getProperty("appContext");
	   URL url=new URL(baseURL+appContextStr+"/admin/admin.jsp");
       String response=getResponse(url,"metro","mouse");
       assertTrue("user", response.contains("HTTP Status 403"));
    }
}