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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * ???
 * 
 * @version $Rev$ $Date$
 */
public class SimpleLoginTest extends TestCase {
    private static StartSeleniumDecorator decorator;

    public static Test suite() {
        Test test = new TestSuite(SimpleLoginTest.class);
        decorator = new StartSeleniumDecorator(test);
        return decorator;
    }

    public void testLogin() throws Exception {
        decorator.login();
        assertEquals("Geronimo Console", decorator.getSelenium().getTitle());
        decorator.logout();
    }

    public void testLoginAndLogout() throws Exception {
        decorator.login();
        assertEquals("Geronimo Console", decorator.getSelenium().getTitle());
        decorator.logout();
        decorator.getSelenium().open("/console");
        assertEquals("Geronimo Console Login", decorator.getSelenium().getTitle());
        decorator.getSelenium().close();
    }

    public void testClickSomeLinks() throws Exception {
        decorator.login();
        assertEquals("Geronimo Console", decorator.getSelenium().getTitle());
        decorator.getSelenium().click("link=Information");
        decorator.getSelenium().waitForPageToLoad("30000");
        assertEquals("Geronimo Console", decorator.getSelenium().getTitle());
        decorator.getSelenium().click("link=JVM");
        decorator.getSelenium().waitForPageToLoad("30000");
        assertEquals("Geronimo Console", decorator.getSelenium().getTitle());
        decorator.getSelenium().click("link=DB Info");
        decorator.getSelenium().waitForPageToLoad("30000");
        assertEquals("Geronimo Console", decorator.getSelenium().getTitle());
        decorator.logout();
    }

    public void testDeployDataSource() throws Exception {
        decorator.login();
        assertEquals("Geronimo Console", decorator.getSelenium().getTitle());
        decorator.getSelenium().click("link=Database Pools");
        decorator.getSelenium().waitForPageToLoad("30000");
        decorator.getSelenium().click("link=Using the Geronimo database pool wizard");
        decorator.getSelenium().waitForPageToLoad("30000");
        decorator.getSelenium().type("name", "DefaultDS");
        decorator.getSelenium().select("dbtype", "Derby embedded");
        decorator.getSelenium().click("xpath=//input[@value='Next']");
        // invalid XHTML prevents this from working;
        // <select multiple name="jars" size="10"> is the value that its trying to parse
        //decorator.getSelenium().addSelection("xpath=//select[@name='jars']", "org.apache.derby/derby/10.1.1.0/jar");
        assertEquals("Geronimo Console", decorator.getSelenium().getTitle());
        decorator.logout();
    }
}

