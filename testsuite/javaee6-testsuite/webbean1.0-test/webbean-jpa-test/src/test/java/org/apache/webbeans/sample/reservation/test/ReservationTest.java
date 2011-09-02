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

package org.apache.webbeans.sample.reservation.test;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.apache.geronimo.testsupport.SeleniumTestSupport;

public class ReservationTest extends SeleniumTestSupport {
	// If all the admin information are null,then it will give the suggestion
	// message
	@Test
	public void testRequiredMessage() {
		selenium.open("/webbean-jpa-test/login.jsf");
		selenium.click("link=Not a registered! Register here...");
		selenium.waitForPageToLoad("30000");
		selenium.click("form:Register");
		selenium.waitForPageToLoad("30000");
		Assert.assertTrue(selenium.getText("xpath=/html/body/div/ul")
				.indexOf("Password must be minumum 4 and maximum 8 characters!") > -1);
		Assert.assertTrue(selenium.getText("xpath=/html/body/div/ul")
				.indexOf("Name is required!") > -1);
		Assert.assertTrue(selenium.getText("xpath=/html/body/div/ul")
				.indexOf("Surname is required!") > -1);
		Assert.assertTrue(selenium.getText("xpath=/html/body/div/ul")
				.indexOf("Age is required") > -1);
		Assert.assertTrue(selenium.getText("xpath=/html/body/div/ul")
				.indexOf("User name is required and minumum 8 characters!") > -1);
	}

	// Test register as an administrator
	@Test(dependsOnMethods = { "testRequiredMessage" })
	public void testRegisterAdmin() {
		selenium.open("/webbean-jpa-test/login.jsf");
		selenium.click("link=Not a registered! Register here...");
		selenium.waitForPageToLoad("30000");
		selenium.type("form:name", "gero");
		selenium.type("form:surname", "nimo");
		selenium.type("form:age", "1");
		selenium.type("form:userName", "geronimo1");
		selenium.type("form:password", "passw0rd");
		selenium.click("form:adminCheckbox");
		selenium.click("form:Register");
		selenium.waitForPageToLoad("60000");
		Assert.assertEquals(
				selenium.getText("xpath=/html/body/div/ul/li"),
				"User with name : geronimo1 is registered successfully.");
	}

	// test login as an admin
	@Test(dependsOnMethods = { "testRegisterAdmin" })
	public void testLogin() {
		selenium.open("/webbean-jpa-test/login.jsf");
		selenium.type("form:userName", "geronimo1");
		selenium.type("form:password", "passw0rdd");
		selenium.click("form:login");
		selenium.waitForPageToLoad("30000");
		Assert.assertEquals(
		        selenium.getText("xpath=/html/body/div/ul/li"),
						"Login failed!,User name or password is not correct. Try again!");
		selenium.type("form:userName", "geronimo1");
		selenium.type("form:password", "passw0rd");
		selenium.click("form:login");
		selenium.waitForPageToLoad("30000");
		Assert.assertTrue(selenium.getText("xpath=/html/body/div/div[2]").startsWith("Welcome, gero nimo"));
	}

	// The administrator creates HotelA,HotelB,HotelC
	@Test(dependsOnMethods = { "testLogin" })
	public void testDefineNewHotel() {
		selenium.click("link=Define New Hotel");
		selenium.waitForPageToLoad("30000");
		selenium.type("form:name", "HotelA");
		selenium.type("form:star", "3");
		selenium.type("form:city", "NewYork");
		selenium.type("form:country", "USA");
		selenium.click("form:add");
		selenium.waitForPageToLoad("30000");
		Assert.assertEquals(
				selenium.getText("xpath=/html/body/div/ul/li"),
				"Hotel 'HotelA' is successfully created");
		selenium.type("form:name", "HotelB");
		selenium.type("form:star", "4");
		selenium.type("form:city", "ShangHai");
		selenium.type("form:country", "China");
		selenium.click("form:add");
		selenium.waitForPageToLoad("30000");
		Assert.assertEquals(
				selenium.getText("xpath=/html/body/div/ul/li"),
				"Hotel 'HotelB' is successfully created");
		selenium.type("form:name", "HotelC");
		selenium.type("form:star", "5");
		selenium.type("form:city", "London");
		selenium.type("form:country", "England");
		selenium.click("form:add");
		selenium.waitForPageToLoad("30000");
		Assert.assertEquals(
				selenium.getText("xpath=/html/body/div/ul/li"),
				"Hotel 'HotelC' is successfully created");
	}

	@Test(dependsOnMethods = { "testDefineNewHotel" })
	public void testListHotels() {
		selenium.click("link=List Hotels");
		selenium.waitForPageToLoad("30000");
		Assert.assertEquals(selenium
				.getText("xpath=//*[@id=\"form:dt:0:col11\"]"), "HotelA");
		Assert.assertEquals(selenium
				.getText("xpath=//*[@id=\"form:dt:1:col11\"]"), "HotelB");
		Assert.assertEquals(selenium
				.getText("xpath=//*[@id=\"form:dt:2:col11\"]"), "HotelC");

	}


	// The administrator deletes information about HotelB
	@Test(dependsOnMethods = { "testListHotels" })
	public void testDeleteHotel() {
		selenium.click("form:dt:0:deleteHotel");
		selenium.waitForPageToLoad("30000");
		//Assert.assertEquals(selenium.getText("xpath=//*[@id=\"form:dt:1:col11\"]"), "HotelC");
		Assert.assertEquals(selenium.getText("xpath=/html/body/div/ul/li"),"Hotel with name HotelA is succesfully deleted.");
	}

	// The administrator logs out
	@Test(dependsOnMethods = { "testDeleteHotel" })
	public void testAdminLogOut() {
		selenium.click("link=Logout");
		selenium.waitForPageToLoad("30000");
		Assert.assertEquals(
				selenium.getText("xpath=/html/body/div/ul/li"),
				"You have successfully logged out!");
	}

	// Register as a customer
	@Test(dependsOnMethods = { "testAdminLogOut" })
	public void testCustomerRegister() {
		selenium.click("link=Not a registered! Register here...");
		selenium.waitForPageToLoad("30000");
		selenium.type("form:name", "Britney");
		selenium.type("form:surname", "Spears");
		selenium.type("form:age", "1");
		selenium.type("form:userName", "brightday");
		selenium.type("form:password", "passw0rd");
		selenium.click("form:Register");
		selenium.waitForPageToLoad("30000");
		Assert.assertEquals(
				selenium.getText("xpath=/html/body/div/ul/li"),
				"User with name : brightday is registered successfully.");
	}

	// Login as a customer
	@Test(dependsOnMethods = { "testCustomerRegister" })
	public void testCustomerLogin() {
		selenium.type("form:userName", "brightday");
		selenium.type("form:password", "passw0rd");
		selenium.click("form:login");
		selenium.waitForPageToLoad("30000");
		Assert.assertTrue(selenium.getText("xpath=/html/body/div/div[2]").startsWith("Welcome, Britney Spears"));
	}

	// test update customer information
	@Test(dependsOnMethods={"testCustomerLogin"})
	public void testCustomerInfoUpdate() {
		selenium.click("link=Show Reservations");
		selenium.waitForPageToLoad("30000");
		selenium.click("link=Update Personal Information");
		selenium.waitForPageToLoad("30000");
		selenium.type("form:name", "Lily");
		selenium.type("form:surname", "Allen");
		selenium.type("form:age", "5");
		selenium.type("form:userName", "geronimo2");
		selenium.type("form:password", "passw0rdd");
		selenium.click("form:updateInfo");
		selenium.waitForPageToLoad("30000");
		Assert.assertEquals(
				selenium.getText("xpath=/html/body/div/ul/li"),
				"Personal information is succesfully updated.");
	}

	// The customer books a hotel
	
	@Test(dependsOnMethods={"testCustomerInfoUpdate"})
	public void testAddReservation() {
		selenium.click("link=Add Reservations");
		selenium.waitForPageToLoad("30000");
		selenium.type("form:col22date", "31/07/2001");
		selenium.click("link=Add Hotel for Reservation");
		selenium.waitForPageToLoad("30000");
		//selenium.type("form:col22date", "01/01/2002");
		//selenium.click("//a[@onclick=\"return oamSubmitForm('form','form:dt:1:j_id28');\"]");
		//selenium.click("link=Add Hotel for Reservation");
		selenium.click("form:checkouthotel");
		selenium.waitForPageToLoad("30000");
		Assert.assertTrue(selenium.getText("xpath=/html/body/div/ul/li")
				.startsWith("Reservation are completed succesfully."));

	}

	// The customer checks the hotels booked by him.
	@Test(dependsOnMethods = { "testAddReservation" })
	public void testShowReservation() {
		selenium.click("link=Show Reservations");
		selenium.waitForPageToLoad("30000");
		Assert.assertTrue(selenium
				.getText("xpath=//*[@id=\"form:dt:0:col11\"]").startsWith(
						"Hotel"));
		//Assert.assertTrue(selenium.getText("xpath=//*[@id=\"form:dt:1:col11\"]").startsWith("Hotel"));
	}

	// The customer deletes a hotel.
	@Test(dependsOnMethods = { "testShowReservation" })
	public void testDeleteReservation() {
		selenium.click("link=Delete Reservation");
		selenium.waitForPageToLoad("30000");
		Assert.assertEquals(
				selenium.getText("xpath=/html/body/div/ul/li"),
				"Reservation is succesfully delete");

	}

	// The customer logs out and administrator logs in to check reservations
	@Test(dependsOnMethods = { "testDeleteReservation" })
	public void testCheckReservation() {
		selenium.click("link=Logout");
		selenium.waitForPageToLoad("30000");
		selenium.type("form:userName", "geronimo1");
		selenium.type("form:password", "passw0rd");
		selenium.click("form:login");
		selenium.waitForPageToLoad("30000");
		selenium.click("link=Show User Reservations");
		selenium.waitForPageToLoad("30000");
		Assert.assertEquals(selenium
				.getText("xpath=//*[@id=\"form:dt:0:col223\"]"), "geronimo1");
		Assert.assertEquals(selenium
				.getText("xpath=//*[@id=\"form:dt:1:col223\"]"), "geronimo2");
	}

}
