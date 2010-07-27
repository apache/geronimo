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

package org.apache.geronimo.testsuite.javaee6.tests;

import org.apache.geronimo.testsupport.SeleniumTestSupport;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import java.util.regex.Pattern;

import junit.framework.Assert;

public class EJBTest extends SeleniumTestSupport {
	@Test 
	public void testEJBSingleton() throws Exception {
		selenium.open("/EJB31Lite/");
		selenium.type("NumberValue", "1.1");
		selenium.click("operation");
		selenium.waitForPageToLoad("30000");
		Assert.assertEquals("1.1", selenium.getText("//*[@id=\"result\"]"));
		System.out.println(selenium.getText("result").toString());
		selenium.type("NumberValue", "2.2");
		selenium.click("operation");
		selenium.waitForPageToLoad("30000");
		Assert.assertEquals("3.3", selenium.getText("//*[@id=\"result\"]"));
		System.out.println(selenium.getText("result").toString());
		selenium.type("NumberValue", "0.3");
		selenium.click("//input[@name='operation' and @value='sub']");
		selenium.waitForPageToLoad("30000");
		Assert.assertEquals("3", selenium.getText("//*[@id=\"result\"]"));
		System.out.println(selenium.getText("result").toString());
		selenium.type("NumberValue", "3");
		selenium.click("//input[@name='operation' and @value='sub']");
		selenium.waitForPageToLoad("30000");
		Assert.assertEquals("0", selenium.getText("//*[@id=\"result\"]"));
		System.out.println(selenium.getText("result").toString());
		selenium.type("NumberValue", "5");
		selenium.click("//input[@name='operation' and @value='sub']");
		selenium.waitForPageToLoad("30000");
		Assert.assertEquals("-5", selenium.getText("//*[@id=\"result\"]"));
		System.out.println(selenium.getText("result").toString());
	}
}


