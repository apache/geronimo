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

package org.apache.geronimo.javaee6.asynejb.test;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.PostMethod;

import java.util.regex.*;

import org.testng.Assert;
import org.testng.annotations.Test;

public class AsynEJBTest {
	@Test
	public void AsynEJBTest()throws Exception
	{
		String contextRoot = System.getProperty("appContext");
		HttpClient nclient = new HttpClient();
		String url = "http://localhost:8080/"+contextRoot+"/testServlet";
		HttpMethodBase httpMethod2;
		httpMethod2 = new PostMethod(url);
		int status = nclient.executeMethod(httpMethod2);
		Assert.assertEquals(200, status);
		String result=null;
		if(status==200){
			String response = new String(httpMethod2.getResponseBodyAsString().getBytes("8859_1"));
			Matcher m = Pattern.compile("The notify process(.)+? testServlet.").matcher(response);
			
			while (m.find()) 
			{
				result = m.group();
			}
		}
		Assert.assertEquals(result, "The notify process is undergoing at testServlet.");
		
		httpMethod2.releaseConnection();
}
}
