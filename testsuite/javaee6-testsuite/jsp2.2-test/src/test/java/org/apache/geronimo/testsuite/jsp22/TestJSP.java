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

package org.apache.geronimo.testsuite.jsp22;

import org.testng.annotations.Test;
import org.apache.geronimo.testsupport.SeleniumTestSupport;

public class TestJSP extends SeleniumTestSupport {

    @Test
    public void testWithDefaultContentType() throws Exception {
        String appContextStr = System.getProperty("appContext");
        selenium.open(appContextStr);
        selenium.click("link=Page with contentType.");
        waitForPageLoad();
        assertTrue(selenium
                .isTextPresent("Page with contentType uses tag:default-content-type which value is text/xml."));
        assertFalse(selenium.isTextPresent("<html>"));
    }

    // XML page test error in Selenium. This test is excluded at this time.
    @Test
    public void testWithoutDefaultContentType() throws Exception {
        String appContextStr = System.getProperty("appContext");
        selenium.open(appContextStr);
        selenium.click("link=Page without contentType.");
        waitForPageLoad();
        assertTrue(selenium.isTextPresent("<html>"));
        assertTrue(selenium
                .isTextPresent("Page without contentType uses tag: default-content-type which value is text/xml."));
    }

    @Test
    public void testWithErrorNamespace() throws Exception {
        String appContextStr = System.getProperty("appContext");
        selenium.open(appContextStr);
        selenium.click("link=Page with tag: error-on-undeclared-namespace = true.");
        waitForPageLoad();
        assertTrue(selenium
                .isTextPresent("A custom tag was encountered with an undeclared namespace [qq]"));
    }

    @Test
    public void testWithoutErrorNamespace() throws Exception {
        String appContextStr = System.getProperty("appContext");
        selenium.open(appContextStr);
        selenium.click("link=Page with tag: error-on-undeclared-namespace = false.");
        waitForPageLoad();
        assertTrue(selenium.isTextPresent("Page with tag: error-on-undeclared-namespace = false."));
        assertTrue(selenium.isTextPresent("error namespace value"));
    }
}
