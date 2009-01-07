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
public class DBManagerTest extends TestSupport {
    @Test
    public void testNewDB() throws Exception {
        selenium.click("link=DB Manager");
        waitForPageLoad();
        selenium.type("createDB", "MyUniqueDB");
        selenium.click("//input[@value = 'Create']");
        waitForPageLoad();
        assertTrue(selenium.isTextPresent("MyUniqueDB"));
        selenium.select("useDB", "label=MyUniqueDB");
        selenium.type("sqlStmts", "create table myTable ( id int primary key );");
        selenium.click("//input[@value = 'Run SQL']");
        waitForPageLoad();
        //selenium.click("link=Application");
        selenium.click("//a[contains(@href, 'db=MyUniqueDB')]");
        waitForPageLoad();
        assertTrue(selenium.isTextPresent("MYTABLE"));
        selenium.select("deleteDB", "label=MyUniqueDB");
        selenium.click("//input[@value = 'Delete']");
        waitForPageLoad();
        assertTrue(selenium.getConfirmation().matches("^Are you sure you want to delete this database[\\s\\S]$"));
        waitForPageLoad();
        assertTrue(selenium.isTextPresent("Database deleted: MyUniqueDB"));
    }
    
    @Test
    public void testRunSQL() throws Exception {
        selenium.click("link=DB Manager");
        waitForPageLoad();
        selenium.select("useDB", "label=SystemDatabase");
        selenium.type("sqlStmts", "select * from SYS.SYSDEPENDS;");
        selenium.click("//input[@value = 'Run SQL']");
        waitForPageLoad();
        assertTrue(selenium.isTextPresent("SQL command(s) executed successfully"));
    }
}
