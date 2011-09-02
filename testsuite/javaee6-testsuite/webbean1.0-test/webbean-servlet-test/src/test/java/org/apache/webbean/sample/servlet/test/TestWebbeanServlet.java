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

package org.apache.webbean.sample.servlet.test;

import org.testng.annotations.Test;
import org.apache.geronimo.testsupport.SeleniumTestSupport;

public class TestWebbeanServlet extends SeleniumTestSupport {

	@Test
	public void testCallWebbeansInServlet() throws Exception {
		String appContextStr = System.getProperty("appContext");
		selenium.open(appContextStr);
		selenium.click("link=Test calling webbeans from servelet");
		waitForPageLoad();
		selenium.type("name=j_username", "metro");
		selenium.type("name=j_password", "mouse");
		selenium.click("name=submit");
		waitForPageLoad();
		assertTrue(selenium
				.isTextPresent("Injection of Bean Instance into Servlet"));
		assertTrue(selenium.isTextPresent("Caller Principal name injection into DateProvider instance : metro"));
		assertTrue(selenium
				.isTextPresent("Current Date : org.apache.webbeans.samples.tomcat.CurrentDateProvider"));
		assertTrue(selenium
				.isTextPresent("Injection of BeanManager into Servlet"));
		assertTrue(selenium
				.isTextPresent("Injection of @Inject BeanManager into servlet is successfull"));
	}
}
