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

package org.apache.geronimo.testsuite.console;

import org.testng.annotations.Test;

/**
 * @version $Rev$ $Date$
 */
public class ConsoleRealmTest extends TestSupport {
    @Test
    public void testNewUser() throws Exception {
    	//selenium.click(getNavigationTreeNodeLocation("Security"));
        selenium.click("link=Users and Groups");
        waitForPageLoad();
        selenium.click("link=Create New User");
        waitForPageLoad();
        selenium.type("userId", "myuser");
        selenium.type("password", "myuser");
        selenium.type("confirm-password", "myuser");
        selenium.click("//input[@value='Add']");
        waitForPageLoad();
        //selenium.selectFrame("index=0");
        assertTrue(selenium.isTextPresent("myuser"));
        selenium.click("//a[@onclick=\"return confirm('Confirm Delete user myuser?');\"]");
        waitForPageLoad();
        assertTrue(selenium.getConfirmation().matches("^Confirm Delete user myuser[\\s\\S]$"));
        waitForPageLoad();
        assertFalse(selenium.isTextPresent("myuser"));
        //return to main window
        selenium.selectWindow("null");
    }

    @Test
    public void testNewGroup() throws Exception {
    	//selenium.click(getNavigationTreeNodeLocation("Security"));
        selenium.click("link=Users and Groups");
        waitForPageLoad();
        //selenium.selectFrame("index=0");
        selenium.click("link=Create New Group");
        waitForPageLoad();
        selenium.type("group", "mygroup");
        selenium.click("//input[@value='Add']");
        waitForPageLoad();
        assertTrue(selenium.isTextPresent("mygroup"));
        selenium.click("//a[@onclick=\"return confirm('Confirm Delete group mygroup?');\"]");
        waitForPageLoad();
        assertTrue(selenium.getConfirmation().matches("^Confirm Delete group mygroup[\\s\\S]$"));
        waitForPageLoad();
        assertFalse(selenium.isTextPresent("mygroup"));
        selenium.selectWindow("null");
    }
}
