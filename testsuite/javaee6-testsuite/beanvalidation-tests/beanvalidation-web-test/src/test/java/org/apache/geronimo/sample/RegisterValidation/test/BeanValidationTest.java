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

package org.apache.geronimo.sample.RegisterValidation.test;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.apache.geronimo.testsupport.SeleniumTestSupport;

public class BeanValidationTest extends SeleniumTestSupport {


	@Test
	public void rightInput() {
		selenium.open("/beanvalidation-web-test/");
		selenium.selectFrame("registerFrame");
		selenium.type("name", "gero");
		selenium.type("age", "23");
		selenium.type("mail", "geronimo@apache.com");
		selenium.type("birthday", "2012-12-30");
		selenium.type("country", "Earth");
		selenium.type("state", "Califorlia");
		selenium.type("city", "sh");
		selenium.type("salary", "1002");
		selenium.click("submit");
		selenium.waitForPageToLoad("30000");
		Assert.assertEquals(selenium.getBodyText(),"Congratulations,All your information passed validation!Register Successfully!");
	}
    //test of @NotNull
	@Test
	public void testNullName() {
		selenium.open("/beanvalidation-web-test/");
		selenium.selectFrame("registerFrame");
		selenium.type("name", "");
		selenium.type("age", "23");
		selenium.type("mail", "geronimo@apache.com");
		selenium.type("birthday", "2012-12-30");
		selenium.type("country", "Earth");
		selenium.type("state", "Califorlia");
		selenium.type("city", "sh");
		selenium.type("salary", "1200");
		selenium.click("submit");
		selenium.waitForPageToLoad("30000");
		Assert.assertEquals(selenium.getText("xpath=//*[@id=\"errormessages\"]"),"Invalid value:null,Name can not be null!");						
		
								
	}
	// test of @Size
	@Test
	public void testNameLength(){
		selenium.open("/beanvalidation-web-test/");
		selenium.selectFrame("registerFrame");
		selenium.type("name", "geronimo");
		selenium.type("age", "23");
		selenium.type("mail", "geronimo@apache.com");
		selenium.type("birthday", "2012-12-30");
		selenium.type("country", "Earth");
		selenium.type("state", "Califorlia");
		selenium.type("city", "sh");
		selenium.type("salary", "1002");
		selenium.click("submit");
		selenium.waitForPageToLoad("30000");
		Assert.assertEquals(selenium.getText("xpath=//*[@id=\"errormessages\"]"),"Invalid value:geronimo,The length of name should between 1 and 5");
		
	}
	//test of @Min and @Max
	@Test
	public void testAge(){
		selenium.open("/beanvalidation-web-test/");
		selenium.selectFrame("registerFrame");
		selenium.type("name", "gero");
		selenium.type("age", "122");
		selenium.type("mail", "geronimo@apache.com");
		selenium.type("birthday", "2012-12-30");
		selenium.type("country", "Earth");
		selenium.type("state", "Califorlia");
		selenium.type("city", "sh");
		selenium.type("salary", "1002");
		selenium.click("submit");
		selenium.waitForPageToLoad("30000");
		Assert.assertEquals(selenium.getText("xpath=//*[@id=\"errormessages\"]"),"Invalid value:122,Age should be less than 100");			
	}
	//test of @Pattern
	@Test
	public void testMail(){
		selenium.open("/beanvalidation-web-test/");
		selenium.selectFrame("registerFrame");
		selenium.type("name", "gero");
		selenium.type("age", "23");
		selenium.type("mail", "ttt!gmail.com");
		selenium.type("birthday", "2012-12-30");
		selenium.type("country", "Earth");
		selenium.type("state", "Califorlia");
		selenium.type("city", "sh");
		selenium.type("salary", "1002");
		selenium.click("submit");
		selenium.waitForPageToLoad("30000");
		Assert.assertEquals(selenium.getText("xpath=//*[@id=\"errormessages\"]"),"Invalid value:ttt!gmail.com,must match the following regular expression: ^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$");				
	}
	//test of the constraints defined by yourself
	@Test
	public void testBirthday(){
		selenium.open("/beanvalidation-web-test/");
		selenium.selectFrame("registerFrame");
		selenium.type("name", "gero");
		selenium.type("age", "23");
		selenium.type("mail", "geronimo@apache.com");
		selenium.type("birthday", "2012-2-31");
		selenium.type("country", "Earth");
		selenium.type("state", "Califorlia");
		selenium.type("city", "sh");
		selenium.type("salary", "1002");
		selenium.click("submit");
		selenium.waitForPageToLoad("30000");
		Assert.assertEquals(selenium.getText("xpath=//*[@id=\"errormessages\"]"),"Invalid value:2012-2-31,Not a valid date.");		
		
	}
	//test of @Valid
	@Test
	public void testAddress(){
		selenium.open("/beanvalidation-web-test/");
		selenium.selectFrame("registerFrame");
		selenium.type("name", "gero");
		selenium.type("age", "23");
		selenium.type("mail", "geronimo@apache.com");
		selenium.type("birthday", "2012-12-30");
		selenium.type("country", "");
		selenium.type("state", "Califorlia");
		selenium.type("city", "sh");
		selenium.type("salary", "1002");
		selenium.click("submit");
		selenium.waitForPageToLoad("30000");
		Assert.assertEquals(selenium.getText("xpath=//*[@id=\"errormessages\"]"),"Invalid value:null,Country can not be null.");	
		
	}
	//test of @Min.List and groups
	@Test
	public void testSalary(){
		selenium.open("/beanvalidation-web-test/");
		selenium.selectFrame("registerFrame");
		selenium.type("name", "gero");
		selenium.type("age", "23");
		selenium.type("mail", "geronimo@apache.com");
		selenium.type("birthday", "2012-12-30");
		selenium.type("country", "Earth");
		selenium.type("state", "Califorlia");
		selenium.type("city", "sh");
		selenium.type("salary", "100");
		selenium.click("submit");
		selenium.waitForPageToLoad("30000");
		Assert.assertEquals(selenium.getText("xpath=//*[@id=\"errormessages\"]"),"Invalid value:100,Your salary should not be less than 1000");									
	}
	//test of @Min.List and groups
	@Test
	public void testGroup(){
		selenium.open("/beanvalidation-web-test/");
		selenium.selectFrame("registerFrame");
		selenium.type("name", "gero");
		selenium.type("age", "23");
		selenium.type("mail", "geronimo@apache.com");
		selenium.type("birthday", "2012-12-30");
		selenium.type("country", "Earth");
		selenium.type("state", "Califorlia");
		selenium.type("city", "sh");
		selenium.type("salary", "1002");
		selenium.click("submitAsVIP");
		selenium.waitForPageToLoad("30000");
		Assert.assertEquals(selenium.getText("xpath=//*[@id=\"errormessages\"]"),"Invalid value:1002,As a VIP,your salary should not be less than 10000");	
		
	}
}
