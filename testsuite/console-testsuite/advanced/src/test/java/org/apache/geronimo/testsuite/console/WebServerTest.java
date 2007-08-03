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
public class WebServerTest extends ConsoleTestSupport {
    @Test
    public void testNewConnector() throws Exception {
        login();

        String TOMCAT = "Tomcat";
        String JETTY = "Jetty";
        
        selenium.click("link=Web Server");
        selenium.waitForPageToLoad("30000");
        String container = JETTY;
        if(selenium.isTextPresent(TOMCAT)) {
            container = TOMCAT;
        }
        selenium.click("link=Add new HTTP listener for " + container);
        selenium.waitForPageToLoad("30000");
        selenium.type("displayName", "uniquename");
        selenium.type("host", "0.0.0.0");
        selenium.type("port", "9405");
        selenium.click("submit");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("uniquename"));
        selenium.click("//a[@onclick=\"return confirm('Are you sure you want to delete uniquename?');\"]");
        selenium.waitForPageToLoad("20000");
        assertTrue(selenium.getConfirmation().matches("^Are you sure you want to delete uniquename[\\s\\S]$"));
        selenium.waitForPageToLoad("30000");
        assertFalse(selenium.isTextPresent("uniquename"));
        
        logout();
    }
    
    @Test
    public void testEditConnector() throws Exception{
        login();
        
        selenium.click("link=Web Server");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=edit");
        selenium.waitForPageToLoad("30000");
        selenium.type("port", "8008");
        selenium.click("submit");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("8008"));
        selenium.click("link=edit");
        selenium.waitForPageToLoad("30000");
        selenium.type("port", "8009");
        selenium.click("submit");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("8009"));
        
        logout();
    }
    
    @Test
    public void testStartStopConnector() throws Exception {
        login();
        
        selenium.click("link=Web Server");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("running"));
        selenium.click("link=stop");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("stopped"));
        selenium.click("link=start");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("running"));
        
        logout();
    }
}

