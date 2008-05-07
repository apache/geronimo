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
public class SeleniumTestSupport
    extends TestSupport
{
    protected static ExtendedSelenium selenium;
    
    protected ExtendedSelenium createSeleniumClient(String url) throws Exception {
        super.setUp();
        
        if (url == null) {
            url = "http://localhost:" + SeleniumServer.DEFAULT_PORT;
        }
        
        log.info("Creating Selenium client for URL: " + url);
        
        ExtendedSelenium selenium = new ExtendedSelenium(
            "localhost", SeleniumServer.DEFAULT_PORT, "*firefox", url);
        
        return selenium;
    }
    
    @BeforeSuite
    protected void startSeleniumClient() throws Exception {
        log.info("Starting Selenium client");
        
        selenium = createSeleniumClient("http://localhost:8080/");
        selenium.start();
    }
    
    @AfterSuite
    protected void stopSeleniumClient() throws Exception {
        log.info("Stopping Selenium client");
        
        selenium.stop();
    }
    
    protected void waitForLoad() throws Exception {
        selenium.waitForPageToLoad("30000");
    }
    
    /**
     * junit's per class setup.
     * 
    protected void setUp() throws Exception {
        log.info("Starting Selenium client");
        
        selenium = createSeleniumClient("http://localhost:8080/");
        selenium.start();
    }
     */
    
    /**
     * junit's per class teardown.
     * 
    protected void tearDown() throws Exception {
        log.info("Stopping Selenium client");
        
        selenium.stop();
    }
    */
}

