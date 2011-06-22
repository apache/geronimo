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

package org.apache.geronimo.testsuite.security.test;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.PostMethod;
import java.net.HttpURLConnection;
import java.io.IOException;
import java.util.regex.*;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeTest;

public class GenericRealmTest {

    private static String url;
	/**
	 * Test 1  
	 * Test valid user "izumi"(role "manager") in file realm can access protected resources
	 */
	@Test
	public void GenericFileRealmSucessTest()throws Exception{					
			Assert.assertEquals(getHTTPResponseStatus("izumi"), HttpURLConnection.HTTP_OK);			
		}
	
	/**
	 * Test 2  
	 * Test valid user "tom"(role "manager") in SQL realm can access protected resources
	 */
	@Test
	public void GenericSQLRealmSucessTest()throws Exception{			
			Assert.assertEquals(getHTTPResponseStatus("tom"), HttpURLConnection.HTTP_OK);			
		}
	
	/**
	 * Test 3  
	 * Test valid but forbidden user "alan"(role "it") cannot access the protected resources
	 */
	@Test
	public void GenericFailureTest()throws Exception{		
		Assert.assertEquals(getHTTPResponseStatus("alan"), HttpURLConnection.HTTP_FORBIDDEN);
	}
	
	/**
	 * Test 4  
	 * Test invalid user "nobody" cannot access the protected resources
	 */
	@Test
	public void GenericUnauthTest()throws Exception{		
		Assert.assertEquals(getHTTPResponseStatus("nobody"), HttpURLConnection.HTTP_UNAUTHORIZED);
	}
	
	@BeforeTest
	public void setURL(){
	    String contextRoot = System.getProperty("appContext");
        url = "http://localhost:8080/"+contextRoot+"/protect/hello.html";
	}
	
	private int getHTTPResponseStatus(String username) throws Exception {		
		HttpMethodBase httpMethod;
		httpMethod = new PostMethod(url);
		httpMethod.setRequestHeader("SM_USER", username);
		int status = 0;
		
		HttpClient nclient = new HttpClient();
		status = nclient.executeMethod(httpMethod);
		httpMethod.releaseConnection();		
		return status;
	}
}
