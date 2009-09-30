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
import org.apache.geronimo.testsupport.console.ConsoleTestSupport;

@Test
public class ConsoleRealmTest extends ConsoleTestSupport {
    @Test
    public void testNewUser() throws Exception {
        try {
            login();

            selenium.click("link=Users and Groups");
            selenium.waitForPageToLoad("30000");
            selenium.click("link=Create New User");
            selenium.waitForPageToLoad("30000");
            selenium.type("userId", "myuser");
            selenium.type("password", "myuser");
            selenium.type("confirmpassword", "myuser");
            selenium.click("//input[@value='Add']");
            selenium.waitForPageToLoad("30000");
            assertTrue(selenium.isTextPresent("myuser"));
            selenium.click("//a[@onclick=\"return confirm('Confirm Delete user myuser?');\"]");
            selenium.waitForPageToLoad("30000");
            assertTrue(selenium.getConfirmation().matches("^Confirm Delete user myuser[\\s\\S]$"));
            selenium.waitForPageToLoad("30000");
            assertFalse(selenium.isTextPresent("myuser"));
        } finally {
            logout();
        }
    }

    @Test
    public void testNewGroup() throws Exception {
        try {
            login();
            
            selenium.click("link=Users and Groups");
            selenium.waitForPageToLoad("30000");
            selenium.click("link=Create New Group");
            selenium.waitForPageToLoad("30000");
            selenium.type("group", "mygroup");
            selenium.click("//input[@value='Add']");
            selenium.waitForPageToLoad("30000");
            assertTrue(selenium.isTextPresent("mygroup"));
            selenium.click("//a[@onclick=\"return confirm('Confirm Delete group mygroup?');\"]");
            selenium.waitForPageToLoad("30000");
            assertTrue(selenium.getConfirmation().matches("^Confirm Delete group mygroup[\\s\\S]$"));
            selenium.waitForPageToLoad("30000");
            assertFalse(selenium.isTextPresent("mygroup"));
        } finally {
            logout();
        }
    }
}
