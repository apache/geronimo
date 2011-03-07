/*
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
*/

package org.apache.geronimo.testsuite.requirebundle.resolution.tests;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.PostMethod;

import org.testng.Assert;
import org.testng.annotations.Test;


public class ResolutionOptionalServletTest{

	
	/**
	 * Test 1
	 * Test 
	 * Require-Bundle: Resolution=optional in OSGi core spec.
	 * @throws Exception
	 */
	@Test
	public void RequireBundleTest()throws Exception
	{
		String contextroot = System.getProperty("webAppName");
		String root = "http://localhost:8080/"+contextroot;
		int status = 0;
		HttpClient nclient = new HttpClient();
		String url = root+"/checkResolution";
		HttpMethodBase httpMethod;
		httpMethod = new PostMethod(url);
		status = nclient.executeMethod(httpMethod);	
		Assert.assertEquals(status, 200);
		String response = null;
		if(status==200)
		{
			response = new String(httpMethod.getResponseBodyAsString().getBytes("8859_1"));
		}
		Assert.assertTrue(response.contains("Hello! Reexport in Require-bundle attribute is effective since this INFO displays."));
		Assert.assertTrue(response.contains("Succeed to resolve Attr \"resolution=optional\""));	
		httpMethod.releaseConnection();
	}

}

