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
public class DBManagerTest extends ConsoleTestSupport {
    @Test
    public void testNewDB() throws Exception {
        try {
            login();

            selenium.click("link=DB Manager");
            selenium.waitForPageToLoad("30000");
            selenium.type("createDB", "MyUniqueDB");
            selenium.click("//input[@value = 'Create']");
            selenium.waitForPageToLoad("30000");
            assertTrue(selenium.isTextPresent("MyUniqueDB"));
            selenium.type("sqlStmts", "create table myTable ( id int primary key );");
            selenium.select("useDB", "label=SystemDatabase");
            selenium.select("useDB", "label=MyUniqueDB");
            selenium.click("//input[@value = 'Run SQL']");
            selenium.waitForPageToLoad("30000");
            //selenium.click("link=Application");
            selenium.click("//a[contains(@href, 'db=MyUniqueDB')]");
            selenium.waitForPageToLoad("30000");
            assertTrue(selenium.isTextPresent("MYTABLE"));
            selenium.select("deleteDB", "label=SystemDatabase");
            selenium.select("deleteDB", "label=MyUniqueDB");
            selenium.click("//input[@value = 'Delete']");
            selenium.waitForPageToLoad("30000");
            assertTrue(selenium.getConfirmation().matches("^Are you sure you want to delete this database[\\s\\S]$"));
            selenium.waitForPageToLoad("30000");
            assertTrue(selenium.isTextPresent("Database deleted: MyUniqueDB"));
        } finally {
            logout();
        }
    }
}
