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

package org.apache.geronimo.javaee6.jndi.test;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.PostMethod;

import java.util.regex.*;

import org.testng.Assert;
import org.testng.annotations.Test;

public class jndiEJBTest {
	
	@Test
	public void AsynEJBTest()throws Exception
	{
		String VersionPara = System.getProperty("moduleVersion");
		String contextRoot = System.getProperty("appContext");

		HttpClient nclient = new HttpClient();
		String url = "http://localhost:8080/"+contextRoot+"/globalJNDITest?version="+VersionPara;
		HttpMethodBase httpMethod;
		httpMethod = new PostMethod(url);
		
		int status = nclient.executeMethod(httpMethod);
		Assert.assertEquals(200, status);
		
		String result1 = null;
		String result2 = null;
		String result3 = null;
		if(status==200){
			String response = new String(httpMethod.getResponseBodyAsString().getBytes("8859_1"));
			result1 = findRes("global (.)+? at testServlet.",response);
			result2 = findRes("app (.)+? at testServlet.",response);
			result3 = findRes("module (.)+? at testServlet.",response);
		}
		Assert.assertEquals(result1, "global says:hello at testServlet.");
		Assert.assertEquals(result2, "app says:hello at testServlet.");
		Assert.assertEquals(result3, "module says:hello at testServlet.");
		
		httpMethod.releaseConnection();
	}
	
	private String findRes(String regex,String response){
		String res=null;
		Matcher m = Pattern.compile(regex).matcher(response);
		while (m.find()) 
		{
			res = m.group();
		}
		//System.out.println(res);
		return res;
	}
}
