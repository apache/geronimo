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

package org.apache.geronimo.javaee6.jpa20.tests;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.PostMethod;

import org.testng.Assert;
import org.testng.annotations.Test;

public class JPATest {
	private static String root ;
	
	/**
	 * Test 1
	 * Test 
	 * Criteria Query feature.
	 * @throws Exception
	 */
	@Test
	public void addCourseTest()throws Exception
	{
		String contextroot = System.getProperty("appContext");
		root = "http://localhost:8080/"+contextroot;
		
		HttpClient nclient = new HttpClient();
		String url = root+"/CourseAdd?cid=1&cname=course1&classroom=course1&teacher=course1&assistTeacher=course1";
		HttpMethodBase httpMethod;
		httpMethod = new PostMethod(url);
		nclient.executeMethod(httpMethod);
		httpMethod.releaseConnection();
		
		url = root+"/ListQuery?cname=course1";
		HttpMethodBase httpMethod2;
		httpMethod2 = new PostMethod(url);
		int status = nclient.executeMethod(httpMethod2);
		Assert.assertEquals(status, 200);
//		System.out.println("status:" + status);
		String response = null;
		if(status==200)
		{
			response = new String(httpMethod2.getResponseBodyAsString().getBytes("8859_1"));
		}
		Assert.assertTrue(response.contains("course name is :course1 from listQuery."));
	
		httpMethod2.releaseConnection();
	}

	
	/**
	 * Test 2
	 * Test 
	 * @ElementCollection annotation
	 * */
	@Test(dependsOnMethods = { "addCourseTest" })
	public void addCommentTest() throws HttpException, Exception
	{
		HttpClient nclient = new HttpClient();
		String url = root+"/CommentAdd?cid=1";
		HttpMethodBase httpMethod;
		httpMethod = new PostMethod(url);
		nclient.executeMethod(httpMethod);
		httpMethod.releaseConnection();
		
		url = root+"/viewAllComments?cid=1";
		HttpMethodBase httpMethod2;
		httpMethod2 = new PostMethod(url);
		int status = nclient.executeMethod(httpMethod2);
		System.out.println("status:" + status);
		String response = null;
		if(status==200)
		{
			response = new String(httpMethod2.getResponseBodyAsString().getBytes("8859_1"));
		}
		Assert.assertTrue(response.contains("Comment:comment0 from viewAllComments"));
		
		httpMethod2.releaseConnection();	
	}
	
	/**
	 * Test 3
	 * Test 
	 * @Embedded annotation
	 * @Embeddable annotation
	 * @throws Exception 
	 * @throws HttpException 
	 */
	@Test(dependsOnMethods = { "addCourseTest" })
	public void AddStudentTest() throws HttpException, Exception
	{
		HttpClient nclient = new HttpClient();
		String url = root+"/StudentAdd?sid=1&sname=s1&country=country1&city=city1&street=street1&telephone=111111&age=11&score=0";
		HttpMethodBase httpMethod;
		httpMethod = new PostMethod(url);
		nclient.executeMethod(httpMethod);
		httpMethod.releaseConnection();
		
		url = root+"/viewStudents";
		HttpMethodBase httpMethod2;
		httpMethod2 = new PostMethod(url);
		nclient.executeMethod(httpMethod2);
		int status = nclient.executeMethod(httpMethod2);
		Assert.assertEquals(status,200);
//		System.out.println("status:" + status);
		
		String response = null;
		if(status==200)
		{
			response = new String(httpMethod2.getResponseBodyAsString().getBytes("8859_1"));
		}
		Assert.assertTrue(response.contains("Country:country1"));
		Assert.assertTrue(response.contains("City:city1"));
		Assert.assertTrue(response.contains("Street:street1"));
		httpMethod2.releaseConnection();
	}
	
	/**
	 * Test 4
	 * Test
	 * Foreign key function--add one ONE-TO-MANY record
	 * @throws HttpException
	 * @throws Exception
	 */
	
	@Test(dependsOnMethods = { "AddStudentTest" })
	public void SelectCourseTest() throws HttpException, Exception
	{
		HttpClient nclient = new HttpClient();
		String url = root+"/viewSelect_CourseRelation?sid=1";
		HttpMethodBase httpMethod;
		httpMethod = new PostMethod(url);
		int status = nclient.executeMethod(httpMethod);
//		System.out.println("status:" + status);
		String response = null;
		Assert.assertEquals(status, 200);
		if(status==200)
		{
			response = new String(httpMethod.getResponseBodyAsString().getBytes("8859_1"));
		}
		Assert.assertTrue(response.contains("Click Here to Select course1 from selectCourse"));
		httpMethod.releaseConnection();
		
		
		url = root+"/CourseSelect?cid=1&sid=1";
		httpMethod = new PostMethod(url);
		status = nclient.executeMethod(httpMethod);
		Assert.assertEquals(status, 200);
//		System.out.println("status:" + status);
		response = null;
		if(status==200)
		{
			response = new String(httpMethod.getResponseBodyAsString().getBytes("8859_1"));
		}
		Assert.assertTrue(response.contains("Click Here to Unselect course1 from selectCourse"));
		httpMethod.releaseConnection();
	}
	
	/**
	 * Test 5
	 * Test
	 * Foreign key function--delete one ONE-TO-MANY record
	 * @throws HttpException
	 * @throws Exception
	 */
	@Test(dependsOnMethods = { "SelectCourseTest" })
	public void UnselectCourseTest() throws HttpException, Exception
	{
		String result=null;
		String response = null;
		HttpClient nclient = new HttpClient();
		String url = root+"/CourseUnselect?cid=1&sid=1";
		HttpMethodBase httpMethod;
		httpMethod = new PostMethod(url);
		int status = nclient.executeMethod(httpMethod);
//		System.out.println("status:" + status);

		Assert.assertEquals(status, 200);

		if(status==200)
		{
			response = new String(httpMethod.getResponseBodyAsString().getBytes("8859_1"));
		}
		Assert.assertTrue(response.contains("Click Here to Select course1 from selectCourse"));
		httpMethod.releaseConnection();
	}
	
	/**
	 * Test 6
	 * Test
	 * Insert and delete Student Record.
	 * @throws HttpException
	 * @throws Exception
	 */
	@Test
	public void Insert_Del_Stu_Test() throws HttpException, Exception
	{
		String response = null;
		HttpClient nclient = new HttpClient();
		String url = root+"/StudentAdd?sid=2&sname=student2&country=country2&city=city2&street=street2&telephone=222222&age=22&score=0";
		HttpMethodBase httpMethod;
		httpMethod = new PostMethod(url);
		int status = nclient.executeMethod(httpMethod);
//		System.out.println("status:" + status);
		Assert.assertEquals(status, 200);
		httpMethod.releaseConnection();
		
		url = root +"/viewAllStudents";
		HttpMethodBase httpMethod2;
		httpMethod2 = new PostMethod(url);
		status = nclient.executeMethod(httpMethod2);
		Assert.assertEquals(status, 200);
		if(status==200)
		{
			response = new String(httpMethod2.getResponseBodyAsString().getBytes("8859_1"));
		}
		Assert.assertTrue(response.contains("student2"));
		httpMethod2.releaseConnection();
	
	
		url = root +"/StudentDelete?sid=2";
		httpMethod = new PostMethod(url);
		status = nclient.executeMethod(httpMethod);
		Assert.assertEquals(status, 200);
		httpMethod.releaseConnection();
		
		url = root +"/viewAllStudents";
		httpMethod2 = new PostMethod(url);
		status = nclient.executeMethod(httpMethod2);
		Assert.assertEquals(status, 200);
		if(status==200)
		{
			response = new String(httpMethod2.getResponseBodyAsString().getBytes("8859_1"));
		}
		Assert.assertTrue(!response.contains("student2"));
		httpMethod2.releaseConnection();
	}
	
	/**
	 * Test 7
	 * Test
	 * NULLIF in JPQL
	 * @throws HttpException
	 * @throws Exception
	 */
	@Test
	public void NullIf_JPQL_Test() throws HttpException, Exception
	{
		String response = null;
		HttpClient nclient = new HttpClient();
		String url = root+"/StudentAdd?sid=3&sname=student3&country=country3&city=city3&street=street3&telephone=333333&age=33&score=0";
		HttpMethodBase httpMethod;
		httpMethod = new PostMethod(url);
		int status = nclient.executeMethod(httpMethod);
//		System.out.println("status:" + status);
		Assert.assertEquals(status, 200);
		httpMethod.releaseConnection();	
		
		url = root+"/nullIfJPQL?sid=3";
		HttpMethodBase httpMethod2;
		httpMethod2 = new PostMethod(url);
		status = nclient.executeMethod(httpMethod2);
		Assert.assertEquals(status, 200);
		if(status==200)
		{
			response = new String(httpMethod2.getResponseBodyAsString().getBytes("8859_1"));
		}
		Assert.assertTrue(response.contains("Nullif is sucess."));
		httpMethod2.releaseConnection();	
	}

}
