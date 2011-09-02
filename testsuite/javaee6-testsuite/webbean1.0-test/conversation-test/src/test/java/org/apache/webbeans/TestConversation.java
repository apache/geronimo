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
package org.apache.webbeans;

import junit.framework.Assert;

import org.testng.annotations.Test;
import org.apache.geronimo.testsupport.SeleniumTestSupport;


public class TestConversation extends SeleniumTestSupport {

	//test non-conversational context
	@Test
	public void testNonConversation(){
        String appContextStr = System.getProperty("appContext");
		selenium.open(appContextStr+"/buy.jsf");
//		selenium.open("/conversation-test/buy.jsf");	
		selenium.click("form:j_id1807007596_6bb4bff0:1:buy");
		selenium.waitForPageToLoad("30000");		
		Assert.assertEquals(selenium.getText("xpath=/html/body/form/div[4]/table/tbody/tr[2]/td"),"Item-2");
		Assert.assertEquals(selenium.getText("xpath=/html/body/form/div[4]/table/tbody/tr[2]/td[2]"),"3000");
		selenium.click("form:j_id1807007596_6bb4bff0:3:buy");
		selenium.waitForPageToLoad("30000");		
		Assert.assertEquals(selenium.getText("xpath=/html/body/form/div[4]/table/tbody/tr[2]/td"),"Item-4");
		Assert.assertEquals(selenium.getText("xpath=/html/body/form/div[4]/table/tbody/tr[2]/td[2]"),"6000");
		
	}
	//test start of the conversation
	@Test(dependsOnMethods={"testNonConversation"})
	public void testStartShopping(){
		Assert.assertEquals(selenium.getText("xpath=//*[@id=\"form:conversation\"]"),"");
		selenium.click("form:button1");
		selenium.waitForPageToLoad("30000");
		Assert.assertNotSame(selenium.getText("xpath=//*[@id=\"form:conversation\"]"), "");	
	}
	@Test(dependsOnMethods={"testStartShopping"})
	public void buyItemTwo() {		
		selenium.click("form:j_id1807007596_6bb4bff0:1:buy");
		selenium.waitForPageToLoad("30000");		
		Assert.assertEquals(selenium.getText("xpath=/html/body/form/div[4]/table/tbody/tr[2]/td"),"Item-2");
		Assert.assertEquals(selenium.getText("xpath=/html/body/form/div[4]/table/tbody/tr[2]/td[2]"),"3000");
				
	}
    //test that data in the conversation is not lost
	@Test(dependsOnMethods={"buyItemTwo"})
	public void buyItemFour() {
		//buy item-4
		selenium.click("form:j_id1807007596_6bb4bff0:3:buy");
		selenium.waitForPageToLoad("30000");	
		Assert.assertEquals(selenium.getText("xpath=/html/body/form/div[4]/table/tbody/tr[2]/td"),"Item-2");
		Assert.assertEquals(selenium.getText("xpath=/html/body/form/div[4]/table/tbody/tr[2]/td[2]"),"3000");
		Assert.assertEquals(selenium.getText("xpath=/html/body/form/div[4]/table/tbody/tr[3]/td"),"Item-4");
		Assert.assertEquals(selenium.getText("xpath=/html/body/form/div[4]/table/tbody/tr[3]/td[2]"),"6000");
	}
	//test checkout
	@Test(dependsOnMethods={"buyItemFour"})
	public void checkOut(){
		selenium.click("form:button4");
		selenium.waitForPageToLoad("30000");
		Assert.assertNotSame(selenium.getText("xpath=//*[@id=\"form:conversation\"]"),"");
		
	}
}
