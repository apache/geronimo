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
 
package org.apache.geronimo.testsuite.el22;

import org.testng.annotations.Test;
import org.apache.geronimo.testsupport.SeleniumTestSupport;

public class TestEL extends SeleniumTestSupport {

    @Test
    public void testMethodWithParameters() throws Exception {
        String appContextStr = System.getProperty("appContext");
        selenium.open(appContextStr);
        selenium.selectFrame("sampleDocumentFrame");
        selenium.type("el:name", "qq");
        selenium.type("el:age", "6");
        selenium.click("el:button");
        waitForPageLoad();
        assertTrue(selenium.isTextPresent("Hello,qq"));
        assertTrue(selenium.isTextPresent("You will be 11 years old after five years."));
    }
}
