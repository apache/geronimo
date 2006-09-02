/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.testsuite.console;

import junit.framework.TestCase;

import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.DefaultSelenium;

import org.openqa.selenium.server.SeleniumServer;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class SimpleLoginTest
    extends TestCase
{
    private Selenium selenium;
    
    protected void setUp(String url) throws Exception {
        super.setUp();
        if (url == null) {
            url = "http://localhost:" + SeleniumServer.DEFAULT_PORT;
        }
        selenium = new DefaultSelenium("localhost", SeleniumServer.DEFAULT_PORT, "*firefox", url);
        selenium.start();
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        
        this.setUp("http://localhost:8080/");
    }
    
    protected void tearDown() throws Exception {
        selenium.stop();
    }
    
    public void testLogin() throws Exception {
        selenium.open("/");
        assertEquals("Apache Geronimo", selenium.getTitle());
        
        selenium.click("link=Console");
        selenium.waitForPageToLoad("30000");
        assertEquals("Geronimo Console Login", selenium.getTitle());
        
        selenium.type("j_username", "system");
        selenium.type("j_password", "manager");
        selenium.click("submit");
        selenium.waitForPageToLoad("30000");
        assertEquals("Geronimo Console", selenium.getTitle());
	}
    
    public void testLoginAndLogout() throws Exception {
        selenium.open("/");
        assertEquals("Apache Geronimo", selenium.getTitle());
        
        selenium.click("link=Console");
        selenium.waitForPageToLoad("30000");
        assertEquals("Geronimo Console Login", selenium.getTitle());
        
        selenium.type("j_username", "system");
        selenium.type("j_password", "manager");
        selenium.click("submit");
        selenium.waitForPageToLoad("30000");
        assertEquals("Geronimo Console", selenium.getTitle());
        
        selenium.click("//a[contains(@href, '/console/logout.jsp')]");
        selenium.waitForPageToLoad("30000");
        assertEquals("Geronimo Console Login", selenium.getTitle());
    }
    
    public void testClickSomeLinks() throws Exception {
        selenium.open("/");
        assertEquals("Apache Geronimo", selenium.getTitle());
        
        selenium.click("link=Console");
        selenium.waitForPageToLoad("30000");
        assertEquals("Geronimo Console Login", selenium.getTitle());
        
        selenium.type("j_username", "system");
        selenium.type("j_password", "manager");
        selenium.click("submit");
        selenium.waitForPageToLoad("30000");
        assertEquals("Geronimo Console", selenium.getTitle());
        
        selenium.click("link=Information");
        selenium.waitForPageToLoad("30000");
        assertEquals("Geronimo Console", selenium.getTitle());
        
        selenium.click("link=JVM");
        selenium.waitForPageToLoad("30000");
        assertEquals("Geronimo Console", selenium.getTitle());
        
        selenium.click("link=DB Info");
        selenium.waitForPageToLoad("30000");
        assertEquals("Geronimo Console", selenium.getTitle());
    }
}

