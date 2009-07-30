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

import org.apache.geronimo.testsupport.SeleniumTestSupport;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
@Test
public class RunAsTest
        extends SeleniumTestSupport {
    private static final String SERVLET_FOO = "TestServlet principal: foo\n" +
            "TestServlet isUserInRole foo: true\n" +
            "TestServlet isUserInRole bar: false\n" +
            "TestServlet isUserInRole baz: false";
    private static final String SERVLET_BAR = "TestServlet principal: foo\n" +
            "TestServlet isUserInRole foo: false\n" +
            "TestServlet isUserInRole bar: true\n" +
            "TestServlet isUserInRole baz: false";
    private static final String SERVLET_BAZ = "TestServlet principal: foo\n" +
            "TestServlet isUserInRole foo: false\n" +
            "TestServlet isUserInRole bar: false\n" +
            "TestServlet isUserInRole baz: true";
    private static final String EJB_FOO = "\nTest EJB principal: foo\n" +
                "TestSession isCallerInRole foo: true\n" +
                "TestSession isCallerInRole bar: false\n" +
                "TestSession isCallerInRole baz: false\n" +
                "security exception on testAccessBar method\n" +
                "security exception on testAccessBaz method\n";
    private static final String EJB_BAR = "\nsecurity exception on testAccessFoo method\n" +
                "Test EJB principal: bar\n" +
                "TestSession isCallerInRole foo: false\n" +
                "TestSession isCallerInRole bar: true\n" +
                "TestSession isCallerInRole baz: false\n" +
                "security exception on testAccessBaz method\n";
    private static final String EJB_BAZ = "\nsecurity exception on testAccessFoo method\n" +
                "security exception on testAccessBar method\n" +
                "Test EJB principal: baz\n" +
                "TestSession isCallerInRole foo: false\n" +
                "TestSession isCallerInRole bar: false\n" +
                "TestSession isCallerInRole baz: true\n";


    @BeforeSuite
    protected void startSeleniumClient() throws Exception {
        log.info("Starting Selenium client");

        selenium = createSeleniumClient("http://foo:foo@localhost:8080/");
        selenium.start();
    }

    @Test
    public void testServletNoRunAs() throws Exception {
        String path = "/sec/noRunAsServlet";
        testPath(path, SERVLET_FOO + EJB_FOO + SERVLET_FOO);
    }

    @Test
    public void testServletRunAs() throws Exception {
        String path = "/sec/servlet";
        testPath(path, SERVLET_FOO + EJB_BAR + SERVLET_FOO);
    }

    @Test
    public void testInjectionServletRunAs() throws Exception {
        testPath("/sec/injectionServlet", SERVLET_FOO + EJB_BAR + SERVLET_FOO);
    }

    @Test
    public void testJspRunAs() throws Exception {
        testPath("/sec/jsp", (SERVLET_FOO + EJB_BAR + SERVLET_FOO).replace("\n", " "));
    }

    @Test
    public void testForwardServlet() throws Exception {
        String path = "/sec/forwardServlet";
        testPath(path, SERVLET_FOO + "\n" + SERVLET_FOO + EJB_FOO + SERVLET_FOO + "\n" + SERVLET_FOO);
    }
    @Test
    public void testForwardServletToRunAs() throws Exception {
        String path = "/sec/forwardServletToRunAs";
        testPath(path, SERVLET_FOO + "\n" + SERVLET_FOO + EJB_BAR + SERVLET_FOO + "\n" + SERVLET_FOO);
    }
    @Test
    public void testForwardRunAsServlet() throws Exception {
        String path = "/sec/forwardRunAsServlet";
        testPath(path, SERVLET_FOO + "\n" + SERVLET_BAZ + EJB_BAZ + SERVLET_BAZ + "\n" + SERVLET_FOO);
    }
    @Test
    public void testForwardRunAsServletToRunAs() throws Exception {
        String path = "/sec/forwardRunAsServletToRunAs";
        testPath(path, SERVLET_FOO + "\n" + SERVLET_BAZ + EJB_BAR + SERVLET_BAZ + "\n" + SERVLET_FOO);
    }



    private void testPath(String path, String expected) throws Exception {
        selenium.open(path);
        waitForPageLoad();
        System.out.println("----------------------------------------------");
        String result = selenium.getText("xpath=/html/body");
        System.out.println(result);
        assertEquals("expected:\n" + expected + "\n\nresult:\n" + result,expected, result);
    }

}

