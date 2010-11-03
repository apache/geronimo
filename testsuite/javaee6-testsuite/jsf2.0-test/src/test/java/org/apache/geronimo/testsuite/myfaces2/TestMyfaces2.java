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
package org.apache.geronimo.testsuite.myfaces2;
 
import org.testng.annotations.Test;
import org.apache.geronimo.testsupport.SeleniumTestSupport;

public class TestMyfaces2 extends SeleniumTestSupport {
	
    @Test
    public void testJSF2() throws Exception {
        selenium.open("/converter-javaee6/");
        waitForPageLoad();
        
        assertTrue(selenium.isTextPresent("Converter-A Facelets and AJAX Example In JSF2.0"));
        assertTrue(selenium.isTextPresent("Please input a number, or else you can't get a reply!"));
        selenium.type("form2:in", "1000");
        selenium.keyUp("form2:in", "0");
        Thread.sleep(5000);
        
        assertTrue(selenium.isTextPresent("<USD>"));
        assertTrue(selenium.isTextPresent("<HKD>"));
        assertTrue(selenium.isTextPresent("<JPY>"));
        assertTrue(selenium.isTextPresent("<EUR>"));
        assertTrue(selenium.isTextPresent("<GBP>"));
        assertTrue(selenium.isTextPresent("146.47"));
        assertTrue(selenium.isTextPresent("1137.82"));
        assertTrue(selenium.isTextPresent("132625.99"));
        assertTrue(selenium.isTextPresent("103.37"));
        assertTrue(selenium.isTextPresent("90.08"));

        selenium.click("form1:button1");
        Thread.sleep(5000);

        assertTrue(selenium.isTextPresent("USD"));
        assertTrue(selenium.isTextPresent("HKD"));
        assertTrue(selenium.isTextPresent("JPY"));
        assertTrue(selenium.isTextPresent("EUR"));
        assertTrue(selenium.isTextPresent("GBP"));
        assertTrue(selenium.isTextPresent("6.8269"));
        assertTrue(selenium.isTextPresent("0.87887"));
        assertTrue(selenium.isTextPresent("0.00754"));
        assertTrue(selenium.isTextPresent("9.6734"));
        assertTrue(selenium.isTextPresent("11.1009"));
    }

}
