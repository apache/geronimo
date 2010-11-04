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

package org.apache.geronimo.testsuite.servlet3;

import org.apache.geronimo.testsupport.SeleniumTestSupport;
import org.testng.annotations.Test;

public class TestWebFragment extends SeleniumTestSupport {
    @Test
    public void testWebFragmentSuccess() throws Exception {
        String appContextStr = System.getProperty("appContext");
        selenium.open(appContextStr);
        selenium.click("link=Test WebFragment.");
        waitForPageLoad();
        selenium.click("QueryAll");
        selenium.waitForPageToLoad("30000");
        selenium.type("ID", "1");
        selenium.type("quantity", "6");
        selenium.click("addToCart");
        waitForPageLoad();
        selenium.click("link=>>Continue shopping!");
        waitForPageLoad();
        selenium.type("ID", "6");
        selenium.type("quantity", "1");
        selenium.click("addToCart");
        waitForPageLoad();
        selenium.click("link=>>Go to pay!");
        waitForPageLoad();
        assertEquals("Servlet Payment", selenium.getTitle());
        assertEquals("Dear Customer,\nYou have to pay 120.",
                selenium.getText("xpath=/html/body/h1[1]"));
        assertEquals(
                "Congratulations!You have successfully finished the payment process.",
                selenium.getText("xpath=/html/body/h1[2]"));
        assertTrue(selenium
                .isElementPresent("link=See message generated from different fragments."));

        selenium.click("link=See message generated from different fragments.");
        waitForPageLoad();
        assertEquals("Servlet WebFragmentMessageRecord", selenium.getTitle());
        assertTrue(selenium
                .isTextPresent("The absolute-ordering of fragments in web.xml is: fragment3,fragment2,fragment1,filter chain responses in this order."));
        assertTrue(selenium.isTextPresent("FilterMessage is: "));
        assertTrue(selenium
                .isTextPresent("This Message is from fragment3 filter.This fragment mainly serves to pay for the items you bought."));
        assertTrue(selenium
                .isTextPresent("This Message is from fragment2 filter.This fragment mainly serves to add items to shopping cart."));
        assertTrue(selenium
                .isTextPresent("This Message is from fragment1 filter.This fragment mainly serves to query all the items."));
        assertTrue(selenium.isTextPresent("The Listener Message is: "));
        assertTrue(selenium
                .isTextPresent("This Message is from fragment3 listener.This fragment mainly serves to pay for the items you bought."));
    }

}
