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

package org.apache.geronimo.testsuite.security;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeSuite;
import org.apache.geronimo.testsupport.SeleniumTestSupport;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
@Test
public class RunAsTest
        extends SeleniumTestSupport
{


    @BeforeSuite
     protected void startSeleniumClient() throws Exception {
        log.info("Starting Selenium client");

        selenium = createSeleniumClient("http://foo:foo@localhost:8080/");
        selenium.start();
    }

    @Test
    public void testServletRunAs() throws Exception {
        String path = "/sec/servlet";
        testPath(path);
    }

    @Test
    public void testInjectionServletRunAs() throws Exception {
        testPath("/sec/injectionServlet");
    }

    private void testPath(String path) throws Exception {
        selenium.open(path);
        waitForPageLoad();
        System.out.println("----------------------------------------------");
        System.out.println(selenium.getText("xpath=/html/body"));
        assertEquals("TestServlet principal: foo\n" +
            "TestServlet isUserInRole foo: true\n" +
            "TestServlet isUserInRole bar: false\n" +
            "Test EJB principal: bar\n" +
            "Correctly received security exception on noAccess method\n" +
            "TestSession isCallerInRole foo: false\n" +
            "TestSession isCallerInRole bar: true\n" +
            "TestServlet isUserInRole foo: true\n" +
            "TestServlet isUserInRole bar: false", selenium.getText("xpath=/html/body"));
    }

    @Test
    public void testJspRunAs() throws Exception {
        selenium.open("/sec/jsp");
        waitForPageLoad();
        System.out.println("----------------------------------------------");
        System.out.println(selenium.getText("xpath=/html/body"));
        assertEquals("TestServlet principal: foo Test EJB principal: bar Correctly received security exception on noAccess method", selenium.getText("xpath=/html/body"));

    }
}

