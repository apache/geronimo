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
public class DatabasePoolTest extends TestSupport {
    @Test
    public void testNewDBPool() throws Exception {
        String geronimoVersion = System.getProperty("geronimoVersion");
        assertNotNull(geronimoVersion);
        
        selenium.click("link=Datasources");
        waitForPageLoad();
        selenium.click("link=Using the Geronimo database pool wizard");
        waitForPageLoad();
        selenium.type("name", "UniquePool");
        selenium.select("dbtype", "label=Derby embedded");
        selenium.click("//input[@value='Next']");
        waitForPageLoad();
        selenium.addSelection("jars", "label=org.apache.geronimo.configs/system-database/" + geronimoVersion + "/car");
        selenium.type("property-DatabaseName", "SystemDatabase");
        selenium.click("//input[@value='Deploy']");
        waitForPageLoad();
        selenium.isTextPresent("UniquePool");
        selenium.click("//tr[td[1] = 'UniquePool']/td[4]/a[3]");
        waitForPageLoad();
        assertFalse(selenium.isTextPresent("UniquePool"));
    }

    @Test
    public void testRunSQLDS() throws Exception {
        selenium.click("link=Datasources");
        waitForPageLoad();
        selenium.select("useDB", "label=SystemDatasource");
        selenium.type("sqlStmts", "select * from SYS.SYSDEPENDS;");
        selenium.click("//input[@value = 'Run SQL']");
        waitForPageLoad();
        assertTrue(selenium.isTextPresent("SQL command(s) executed successfully"));
    }
    /*
    // cannot test yet. jetty is having problems rending the page
    
    @Test
    public void testDatabasePoolEdit() throws Exception {
        selenium.click("link=Database Pools");
        waitForPageLoad();
        assertTrue(selenium.isTextPresent("running"));
        selenium.click("link=edit");
        waitForPageLoad();
        selenium.type("maxSize", "101");
        selenium.click("//input[@value='Save']");
        waitForPageLoad();
        selenium.click("link=edit");
        waitForPageLoad();
        assertEquals("101", selenium.getValue("maxSize"));
        selenium.type("maxSize", "100");
        selenium.click("//input[@value='Save']");
        waitForPageLoad();
        selenium.click("link=edit");
        waitForPageLoad();
        assertEquals("100", selenium.getValue("maxSize"));
    }*/
}
