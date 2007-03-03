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

/**
 * Repository viewer portlet tests
 *
 * @version $Rev$ $Date$
 */
@Test
public class RepositoryViewPortletTest
    extends ConsoleTestSupport
{
    @Test
    public void testRepositoryViewLink() throws Exception {
        login();
        
        selenium.click("link=Common Libs");
        selenium.waitForPageToLoad("30000");
        assertEquals("Geronimo Console", selenium.getTitle());
        assertEquals("Repository Viewer", selenium.getText(
            "xpath=/html/body/table[@id='rootfragment']/tbody/tr[2]/td/table/tbody/tr[2]/td[4]/table/tbody/tr[1]/td/table/tbody/tr[1]/td/div/table/tbody/tr/td[2]/table/tbody/tr/td[1]/strong"));
        // Test help link
        selenium.click("link=help");
        selenium.waitForPageToLoad("30000");
        selenium.isTextPresent("This page displays the artifacts installed in the server's repository.");
        
        logout();
    }
}
