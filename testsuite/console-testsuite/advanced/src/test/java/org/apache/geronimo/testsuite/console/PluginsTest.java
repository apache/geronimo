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
 * @version $Rev$ $Date$
 */
public class PluginsTest extends TestSupport {
    @Test
    public void testListPlugins() throws Exception {
        String link = "http://geronimo-server:8080/plugin/maven-repo/";
        String actualLink = "http://localhost:8080/plugin/maven-repo/";
        String updatedLink = "http://geronimo.apache.org/plugins/geronimo-";
        //selenium.click(getNavigationTreeNodeLocation("Applications"));     
        selenium.click("link=Plugins");
        waitForPageLoad();
        //selenium.selectFrame("index=0");
        assertTrue(selenium.isTextPresent(link));
        
        selenium.click("link=Update Repository List");
        waitForPageLoad();
        selenium.isTextPresent(updatedLink);
        
        selenium.click("link=Add repository");
        waitForPageLoad();
        assertTrue(selenium.isTextPresent(link));
                    
        selenium.type("newRepository", actualLink);
        selenium.click("//input[@value='Add repository']");
        waitForPageLoad();

        if (selenium.isTextPresent("Already have an entry for repository " + actualLink)) {
            selenium.click("link=Cancel");
            waitForPageLoad();
        }
        selenium.select("repository", "label=" + actualLink);
        selenium.type("username", "system");
        selenium.type("password", "manager");     
        selenium.click("//input[@value = 'Show plugins in selected repository']");
        waitForPageLoad();
        
        assertTrue(selenium.isTextPresent("Geronimo Assemblies :: Karaf Boilerplate Framework"));
        selenium.click("link=Geronimo Assemblies :: Karaf Boilerplate Framework");
        waitForPageLoad();
        
        selenium.isTextPresent("Geronimo-Versions");
      //return to main window
       selenium.selectWindow("null");
    }
}
