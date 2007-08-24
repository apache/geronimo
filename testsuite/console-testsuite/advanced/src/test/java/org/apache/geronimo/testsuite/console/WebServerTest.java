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
        
        selenium.click("link=Web Server");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=" + container + " BIO HTTP Connector");
        selenium.waitForPageToLoad("30000");
        selenium.type("uniqueName", "uniquename");
        selenium.type("port", "8081");
        selenium.click("submit");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("uniquename"));
        selenium.click("//a[@onclick=\"return confirm('Are you sure you want to delete uniquename?');\"]");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.getConfirmation().matches("^Are you sure you want to delete uniquename[\\s\\S]$"));
        
        logout();
    }
    
    @Test
    public void testEditConnector() throws Exception{
        login();
        
        selenium.click("link=Web Server");
        selenium.waitForPageToLoad("30000");
        
        String TOMCAT = "Tomcat";
        String JETTY = "Jetty";
        
        selenium.click("link=Web Server");
        selenium.waitForPageToLoad("30000");
        String container = JETTY;
        if(selenium.isTextPresent(TOMCAT)) {
            container = TOMCAT;
        }
        
		// assuming there are at least three connectors
		selenium.click("//tr[2]/td[2]/table//tr[4]/td[5]/a[2]");        
		
        selenium.waitForPageToLoad("30000");
        selenium.type("port", "8008");
        selenium.click("submit");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("8008"));
        
		// assuming there are at least three connectors
        selenium.click("//tr[2]/td[2]/table//tr[4]/td[5]/a[2]");
        
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
		// assuming there are at least three connectors and the connector is not being used
        selenium.click("//tr[2]/td[2]/table//tr[4]/td[5]/a[1]");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("stopped"));
        selenium.click("link=start");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isTextPresent("running"));
        
        logout();
    }
}

