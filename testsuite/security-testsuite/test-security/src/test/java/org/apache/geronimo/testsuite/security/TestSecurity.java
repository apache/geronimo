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

package org.apache.geronimo.testsuite.security;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.geronimo.testsupport.TestSupport;
import org.testng.annotations.Test;

public class TestSecurity extends TestSupport {
    
    @Test
    public void testLogin() throws Exception {
        HttpClient httpClient = new HttpClient();
                
        PostMethod postMethod = login(httpClient, "george", "bone");
        
        int statusCode = httpClient.executeMethod(postMethod);
        assertTrue(statusCode >= 300 || statusCode < 310);
        
        postMethod.releaseConnection();         
        Header locationHeader = postMethod.getResponseHeader("location");
        assertNotNull("Expected location header is not present", locationHeader);

        String response;
        
        response = doGet(httpClient, locationHeader.getValue());
        assertTrue(response.contains("hello world"));
    }
   
    @Test
    public void testBadPasswordLogin() throws Exception {   
        testFailure("george", "bonee");
    }

    @Test
    public void testBadUser() throws Exception {  
        testFailure("doesnotexist", "bonee");
    }
    
    @Test
    public void testNullPasswordLogin() throws Exception {        
        testFailure("george", null);
    }
    
    @Test
    public void testNullUserLogin() throws Exception {        
        testFailure(null, "bone");
    }
    
    @Test
    public void testNullCredentialsLogin() throws Exception {        
        testFailure(null, null);
    }
    
    @Test
    public void testEmptyCredentialsLogin() throws Exception {        
        testFailure("", "");
    }
    
    private void testFailure(String username, String password) throws Exception {
        HttpClient httpClient = new HttpClient();
                
        PostMethod postMethod = login(httpClient, username, password);
        
        assertEquals(200, httpClient.executeMethod(postMethod));
        String response = postMethod.getResponseBodyAsString();
        postMethod.releaseConnection();  
        
        boolean authError = response.contains("Authentication ERROR");              
        assertTrue("Expected authentication error", authError);
    }

    private PostMethod login(HttpClient httpClient, String username, String password) throws Exception {
        String response = doGet(httpClient, "http://localhost:8080/demo/protect/hello.html");
        assertTrue("Expected authentication form", response.contains("FORM Authentication demo"));
        
        PostMethod postMethod = new PostMethod("http://localhost:8080/demo/j_security_check");
        if (username != null) {
            postMethod.addParameter("j_username", username);
        }
        if (password != null) {
            postMethod.addParameter("j_password", password);
        }
        
        return postMethod;        
    }
    
    private String doGet(HttpClient httpClient, String url) throws Exception {
        GetMethod getMethod = new GetMethod(url);
        getMethod.setFollowRedirects(true);
        
        assertEquals(200, httpClient.executeMethod(getMethod));
        String response = getMethod.getResponseBodyAsString();
        getMethod.releaseConnection();
        return response;
    }
}
