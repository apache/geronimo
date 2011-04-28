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

package org.apache.geronimo.testsupport;

import org.apache.geronimo.testsupport.TestSupport;

import com.thoughtworks.selenium.Selenium;

import org.openqa.selenium.server.SeleniumServer;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.AfterSuite;

/**
 * Provides support for Selenium test cases.
 *
 * @version $Rev$ $Date$
 */
public class SeleniumTestSupport extends TestSupport
{
    protected static ExtendedSelenium selenium;
    
    protected ExtendedSelenium createSeleniumClient(String url) throws Exception {
        super.setUp();
        
        if (url == null) {
            // url = "http://localhost:" + SeleniumServer.DEFAULT_PORT;
            // post 1.0-beta-1 builds don't define DEFAULT_PORT
            url = "http://localhost:4444";
        }
        
        String browser = System.getProperty("browser", "*firefox");

        log.info("Creating Selenium client for URL: {}, Browser: {}", url, browser);
        
        //ExtendedSelenium selenium = new ExtendedSelenium("localhost", SeleniumServer.DEFAULT_PORT, "*firefox", url);
        ExtendedSelenium selenium = new ExtendedSelenium("localhost", 4444, browser, url);
        
        return selenium;
    }
    
    protected void ensureSeleniumClientInitialized() {
        if (selenium == null) {
            throw new IllegalStateException("Selenium client was not initalized");
        }
    }
    
    @BeforeSuite
    protected synchronized void startSeleniumClient() throws Exception {
        log.info("Starting Selenium client");
        
        selenium = createSeleniumClient("http://localhost:8080/");
        selenium.start();
    }
    
    @AfterSuite
    protected synchronized void stopSeleniumClient() throws Exception {
        ensureSeleniumClientInitialized();
        
        log.info("Stopping Selenium client");
        
        selenium.stop();
    }
    
    protected void waitForPageLoad() throws Exception {
        ensureSeleniumClientInitialized();
        
        selenium.waitForPageToLoad("30000");
    }
}

