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

/**
 * @version $Rev$ $Date$
 */
public class SimpleLoginTest
    extends ConsoleTestSupport
{
    @Test
    public void testLoginAndLogout() throws Exception {
        login();
        logout();
    }
    
    @Test
    public void testClickSomeLinks() throws Exception {
        try {
            login();
            selenium.click("link=Server Information");
            waitForPageLoad();
//            assertEquals("Geronimo Console", selenium.getTitle());
            
            selenium.open("/console");
            selenium.click("link=Java System Info");
            waitForPageLoad();
//            assertEquals("Geronimo Console", selenium.getTitle());
            selenium.open("/console");
            
            selenium.click("link=DB Info");
            waitForPageLoad();
//            assertEquals("Geronimo Console", selenium.getTitle());
        } finally {
            logout();
        }
    }
}


