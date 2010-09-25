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

package org.apache.geronimo.testsuite.servlets;

import java.net.HttpURLConnection;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.geronimo.testsupport.SeleniumTestSupport;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ServletsTest extends SeleniumTestSupport {

/**In web.xml, it reads as follows:
 *     <security-constraint>
 *        <web-resource-collection>
 *        	<web-resource-name>resource1</web-resource-name>
 *          <url-pattern>/SampleServlet</url-pattern>
 *          <url-pattern>/SampleServlet3Dynamic</url-pattern>
 *          <http-method-omission>POST</http-method-omission>
 *      </web-resource-collection>
 *      <auth-constraint/>
 *  </security-constraint>
 *  The Test1 and Test2 tests the description above.
 */

	/**
	 * Test1
	 * test <http-method-omission>
	 */
   @Test
    public void test_SampleServlet_POST_Sucess() throws Exception {
        Assert.assertEquals( invoke("/SampleServlet1", "POST", "alan", "starcraft"), HttpURLConnection.HTTP_OK);
    }

	/**
	 * Test2
	 */
    @Test
    public void test_SampleServlet1_GET_Fail() throws Exception {
        Assert.assertEquals(invoke("/SampleServlet1", "GET", "alan", "starcraft"), HttpURLConnection.HTTP_FORBIDDEN);
    }

	/**
	 * Test3
	 * <security-constraint>
        <web-resource-collection>
            <web-resource-name>resource2</web-resource-name>
            <url-pattern>/SampleServlet2</url-pattern>
            <http-method>GET</http-method>
        </web-resource-collection>
        <auth-constraint>
            <role-name>RoleA</role-name>
        </auth-constraint>
      </security-constraint>
	 */
    @Test
    public void test_SampleServlet2_GET_RoleA_Success() throws Exception {
        Assert.assertEquals(invoke("/SampleServlet2" , "GET", "alan" , "starcraft" ) , HttpURLConnection.HTTP_OK);
    }

    /**
	 * Test4
	 */
    @Test
    public void test_SampleServlet2_GET_RoleB_Fail() throws Exception {
        Assert.assertEquals(invoke("/SampleServlet2" , "GET", "george" , "bone" ) , HttpURLConnection.HTTP_FORBIDDEN);
    }

	/**
	 * Test5
	 * @WebServlet("/SampleServlet4")
	 * @ServletSecurity(httpMethodConstraints = {@HttpMethodConstraint(value = "POST", rolesAllowed = "RoleB") })
	 * public class SampleServlet4 extends HttpServlet{
	 */
    @Test
    public void test_SampleServlet4_POST_RoleB_Success() throws Exception {
        Assert.assertEquals(invoke("/SampleServlet4", "POST", "george", "bone"), HttpURLConnection.HTTP_OK);
    }

	/**
	 * Test6
	 */
    @Test
    public void test_SampleServlet4_POST_RoleC_Fail() throws Exception {
        Assert.assertEquals(invoke("/SampleServlet4", "POST", "gracie", "biscuit"), HttpURLConnection.HTTP_FORBIDDEN);
    }

    /**
	 * Test7
	 */
    @Test
    public void test_SampleServlet3_All_Success() throws Exception {
        Assert.assertEquals(invoke("/SampleServlet3", "POST", "unknown", "unknown"), HttpURLConnection.HTTP_OK);
    }

	/**
	 * Test8
	 * URL "/SampleServlet3Dynamic" are set both in web.xml and ServletRegistration.Dynamic
	 * IN web.xml,GET access is forbidden by all users.
	 * In ServletRegistration.Dynamic, GET access is allowled by RoleC
	 * But web.xml content's priority is higher.
	 */
    @Test
    public void test_SampleServlet3Dynamic_GET_RoleC_Fail() throws Exception {
        Assert.assertEquals(invoke("/SampleServlet3Dynamic", "GET", "gracie", "biscuit"), HttpURLConnection.HTTP_FORBIDDEN);
    }


    /**
	 * Test9
	 */
    @Test
    public void test_SampleServlet3Dynamic_POST_RoleAll_Success() throws Exception {
        Assert.assertEquals(invoke("/SampleServlet3Dynamic", "POST", "unknown", "unknown"), HttpURLConnection.HTTP_OK);
    }


    /**
	 * Test10
	 * Test @WebServlet annotation feature in Servlet 3.0
	 */
    @Test
    public void test_annotation_WebServlet() throws Exception{
    	Assert.assertEquals(invoke("/WebServlet1", "POST", "unknown", "unknown"), HttpURLConnection.HTTP_OK);
    }

    /**
	 * Test11
	 * Test @WebServlet annotation feature in Servlet 3.0
	 * Test mapping two url in @WebServlet
	 * (name = "WebServlet",  urlPatterns = {"/WebServlet1",  "/WebServlet2"} )
	 */
    @Test
    public void test_annotation_WebServlet2() throws Exception{
    	Assert.assertEquals(invoke("/WebServlet2", "GET", "unknown", "unknown"), HttpURLConnection.HTTP_OK);
    }



    /**
	 * Test12
	 * In ServletRegistration.Dynamic, GET access is allowled by RoleC
	 */
    @Test
    public void test_TestDynamic_GET_RoleC_Sucess() throws Exception{
    	Assert.assertEquals(invoke("/TestDynamic", "GET", "gracie", "biscuit"), HttpURLConnection.HTTP_OK);
    }

    /**
	 * Test13
	 */
    @Test
    public void test_TestDynamic_GET_RoleB_Fail() throws Exception{
    	Assert.assertEquals(invoke("/TestDynamic", "GET", "george", "bone"), HttpURLConnection.HTTP_FORBIDDEN);
    }

    /**
	 * Test14 RoleA\B\C should succeed
	 */
    @Test
    public void test_Authenticate_Sucess() throws Exception{
    	Assert.assertEquals(invoke("/AuthenticateServlet", "GET", "george", "bone"), HttpURLConnection.HTTP_OK);
    }

    /**
	 * Test15 RoleA\B\C should succeed
	 */
    @Test
    public void test_Login_Logout_Sucess() throws Exception{
		selenium.open("/servlet30/");
		selenium.type("UserName", "george");
		selenium.type("Password", "bone");
		selenium.click("//input[@value='Login']");
		selenium.waitForPageToLoad("30000");
		Assert.assertEquals("false", selenium.getText("//*[@id=\"bli1\"]"));
		Assert.assertEquals("true", selenium.getText("//*[@id=\"ali1\"]"));
		Assert.assertEquals("false", selenium.getText("//*[@id=\"alo1\"]"));
		Assert.assertEquals("null", selenium.getText("//*[@id=\"bli2\"]"));
		Assert.assertEquals("george", selenium.getText("//*[@id=\"ali2\"]"));
		Assert.assertEquals("null", selenium.getText("//*[@id=\"alo2\"]"));
		Assert.assertEquals("null", selenium.getText("//*[@id=\"bli3\"]"));
		Assert.assertEquals("george", selenium.getText("//*[@id=\"ali3\"]"));
		Assert.assertEquals("null", selenium.getText("//*[@id=\"alo3\"]"));
    }

    public void test_ServletSecurityAnnotation() throws Exception {

        Assert.assertEquals(invoke("/SampleServlet5_1", "GET", "alan", "starcraft"), HttpURLConnection.HTTP_OK);
        Assert.assertEquals(invoke("/SampleServlet5_1", "GET", "george", "bone"), HttpURLConnection.HTTP_FORBIDDEN);
        Assert.assertEquals(invoke("/SampleServlet5_1", "POST", "unknown", "unknown"), HttpURLConnection.HTTP_OK);

        Assert.assertEquals(invoke("/SampleServlet5_2", "GET", "unknown", "unknown"), HttpURLConnection.HTTP_OK);
        Assert.assertEquals(invoke("/SampleServlet5_2", "POST", "unknown", "unknown"), HttpURLConnection.HTTP_OK);

        Assert.assertEquals(invoke("/SampleServlet5_3", "GET", "alan", "starcraft"), HttpURLConnection.HTTP_OK);
        Assert.assertEquals(invoke("/SampleServlet5_3", "GET", "george", "bone"), HttpURLConnection.HTTP_FORBIDDEN);
        Assert.assertEquals(invoke("/SampleServlet5_3", "POST", "unknown", "unknown"), HttpURLConnection.HTTP_OK);

        Assert.assertEquals(invoke("/SampleServlet6_1", "POST", "alan", "starcraft"), HttpURLConnection.HTTP_FORBIDDEN);
        Assert.assertEquals(invoke("/SampleServlet6_1", "POST", "george", "bone"), HttpURLConnection.HTTP_OK);
        Assert.assertEquals(invoke("/SampleServlet6_1", "GET", "unknown", "unknown"), HttpURLConnection.HTTP_OK);

        Assert.assertEquals(invoke("/TestDynamic", "GET", "gracie", "biscuit"), HttpURLConnection.HTTP_OK);
        Assert.assertEquals(invoke("/TestDynamic", "GET", "alan", "starcraft"), HttpURLConnection.HTTP_FORBIDDEN);

        Assert.assertEquals(invoke("/TestDynamicAfter", "GET", "gracie", "biscuit"), HttpURLConnection.HTTP_OK);
        Assert.assertEquals(invoke("/TestDynamicAfter", "GET", "alan", "starcraft"), HttpURLConnection.HTTP_FORBIDDEN);

        Assert.assertEquals(invoke("/SampleServlet3Dynamic", "GET", "gracie", "biscuit"), HttpURLConnection.HTTP_FORBIDDEN);

    }

    private int invoke(String address, String methodName, String userName, String password) throws Exception {
        HttpClient client = new HttpClient();
        Credentials defaultcreds = new UsernamePasswordCredentials(userName, password);
        client.getState().setCredentials(AuthScope.ANY, defaultcreds);
        String url = "http://localhost:8080/servlet30" + address;
        HttpMethodBase httpMethod;
        if (methodName.equals("GET")) {
            httpMethod = new GetMethod(url);
        } else {
            httpMethod = new PostMethod(url);
        }
        return client.executeMethod(httpMethod);
    }

}
