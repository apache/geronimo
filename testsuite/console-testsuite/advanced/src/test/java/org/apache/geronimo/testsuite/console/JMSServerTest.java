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
public class JMSServerTest extends TestSupport {
    @Test
    public void testNewListener() throws Exception {
        selenium.click("link=JMS Server");
        waitForPageLoad();
        //selenium.click("link=Add new tcp listener");
        //waitForPageLoad();
        //selenium.type("name", "uniquename");
        //selenium.type("host", "0.0.0.0");
        //selenium.type("port", "2097");
        //selenium.click("submit");
        //waitForPageLoad();
        //assertTrue(selenium.isTextPresent("uniquename"));
        //selenium.click("link=delete");
        //selenium.click("//a[@onclick=\"return confirm('Are you sure you want to delete uniquename?');\"]");
        //waitForPageLoad();
        //assertTrue(selenium.getConfirmation().matches("^Are you sure you want to delete uniquename[\\s\\S]$"));
    }
    
    @Test
    public void testStartStopListener() throws Exception {
        selenium.click("link=JMS Server");
        waitForPageLoad();
        //selenium.click("//tr[3]/td[6]/a[1]");
        //waitForPageLoad();
        //assertEquals("stopped", selenium.getText("//tr[3]/td[5]"));
        //selenium.click("//tr[3]/td[6]/a[1]");
        //waitForPageLoad();
        //assertEquals("running", selenium.getText("//tr[3]/td[5]"));
    }
    
    @Test
    public void testEditNetworkListener() throws Exception {
        selenium.click("link=JMS Server");
        waitForPageLoad();
        selenium.isTextPresent("61613");
        //selenium.click("link=edit");
        //waitForPageLoad();
        //selenium.type("port", "6161");
        //selenium.click("submit");
        //waitForPageLoad();
        //assertTrue(selenium.isTextPresent("6161"));
        //selenium.click("link=edit");
        //waitForPageLoad();
        //selenium.type("port", "61612");
        //selenium.click("submit");
        //waitForPageLoad();
        //assertTrue(selenium.isTextPresent("61612"));
    }
}
