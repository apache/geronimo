/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbean.sample.ejbtelephone.test;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.apache.geronimo.testsupport.SeleniumTestSupport;

public class EjbtelephoneTest extends SeleniumTestSupport {
	@Test
	public void inputNothing() {
        String appContextStr = System.getProperty("appContext");
		selenium.open(appContextStr + "/contact.jsf");
//		selenium.open("/webbean-ejb-test");
		selenium.click("form:addNewRecord");
		selenium.waitForPageToLoad("30000");
		Assert.assertEquals(selenium.getText("xpath=/html/body/div/div/ul/li"),
		"Please give a name!");
		Assert.assertEquals(selenium.getText("xpath=/html/body/div/div/ul/li[2]"),
		"Please give a surname!");
		Assert.assertEquals(selenium.getText("xpath=/html/body/div/div/ul/li[3]"),
		"Please give a telephone!");	 	 
		  
	}

	@Test(dependsOnMethods = { "inputNothing" })
	public void saveTelephone() {
		selenium.type("form:text", "gero");
		selenium.type("form:surname", "nimo");
		selenium.type("form:telephone", "12345678");
		selenium.click("form:addNewRecord");
		selenium.waitForPageToLoad("60000");
		Assert.assertEquals(selenium.getText("xpath=/html/body/div/div/ul/li"),
				"Record added");
	}

	@Test(dependsOnMethods = { "saveTelephone" })
	public void showAllTelephone() {
		selenium.click("form:showAllRecords");
		selenium.waitForPageToLoad("30000");
		Assert.assertEquals(
						selenium.getText("xpath=/html/body/div/div/form/div[2]/table/tbody/tr/td[2]"),
						"gero");
		Assert.assertEquals(
						selenium.getText("xpath=/html/body/div/div/form/div[2]/table/tbody/tr/td[3]"),
						"nimo");
		Assert.assertEquals(
						selenium.getText("xpath=/html/body/div/div/form/div[2]/table/tbody/tr/td[4]"),
						"12345678");

	}
}
