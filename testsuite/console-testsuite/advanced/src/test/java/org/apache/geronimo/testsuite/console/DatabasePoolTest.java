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

/**
 * @version $Rev$ $Date$
 */
@Test
public class DatabasePoolTest extends ConsoleTestSupport {
    @Test
    public void testNewDBPool() throws Exception {
        try {
            login();
            
            String geronimoVersion = System.getProperty("geronimoVersion");

            selenium.click("link=Database Pools");
            selenium.waitForPageToLoad("30000");
            selenium.click("link=Using the Geronimo database pool wizard");
            selenium.waitForPageToLoad("30000");
            selenium.type("name", "UniquePool");
            selenium.select("dbtype", "label=Derby embedded");
            selenium.click("//input[@value='Next']");
            selenium.waitForPageToLoad("30000");
            selenium.addSelection("jars", "label=org.apache.geronimo.configs/system-database/" + geronimoVersion + "/car");
            selenium.type("property-DatabaseName", "SystemDatabase");
            selenium.click("//input[@value='Deploy']");
            selenium.waitForPageToLoad("30000");
            assertTrue(selenium.isTextPresent("UniquePool"));
            selenium.click("//tr[td[1] = 'UniquePool']/td[4]/a[3]");
            selenium.waitForPageToLoad("30000");
            assertFalse(selenium.isTextPresent("UniquePool"));
        } finally {
            logout();
        }
    }

    @Test
    public void testRunSQLDS() throws Exception {
        try {
            login();
            
            selenium.click("link=Database Pools");
            selenium.waitForPageToLoad("30000");
            selenium.select("useDB", "label=SystemDatasource");
            selenium.type("sqlStmts", "select * from SYS.SYSDEPENDS;");
            selenium.click("//input[@value = 'Run SQL']");
            selenium.waitForPageToLoad("30000");
            assertTrue(selenium.isTextPresent("SQL command(s) executed successfully"));
        } finally {
            logout();
        }
    }

    /*
    // cannot test yet. jetty is having problems rending the page
    
    @Test
    public void testDatabasePoolEdit() throws Exception {
        try {
            login();
            
            selenium.click("link=Database Pools");
            selenium.waitForPageToLoad("30000");
            assertTrue(selenium.isTextPresent("running"));
            selenium.click("link=edit");
            selenium.waitForPageToLoad("30000");
            selenium.type("maxSize", "101");
            selenium.click("//input[@value='Save']");
            selenium.waitForPageToLoad("30000");
            selenium.click("link=edit");
            selenium.waitForPageToLoad("30000");
            assertEquals("101", selenium.getValue("maxSize"));
            selenium.type("maxSize", "100");
            selenium.click("//input[@value='Save']");
            selenium.waitForPageToLoad("30000");
            selenium.click("link=edit");
            selenium.waitForPageToLoad("30000");
            assertEquals("100", selenium.getValue("maxSize"));
        } catch(Exception e) {
        
        } finally {
            logout();
        }
    }*/
}
