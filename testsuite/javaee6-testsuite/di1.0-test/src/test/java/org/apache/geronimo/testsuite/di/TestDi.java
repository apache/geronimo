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

package org.apache.geronimo.testsuite.di;

import org.testng.annotations.Test;

import org.apache.geronimo.testsupport.SeleniumTestSupport;

public class TestDi extends SeleniumTestSupport {

    @Test
    public void testDi() throws Exception {
        String appContextStr = System.getProperty("appContext");
        selenium.open(appContextStr);
        selenium.click("q:q");
        waitForPageLoad();
        assertTrue(selenium.isTextPresent("Congratulations! You have got the only apple!"));
        selenium.click("q:q");
        waitForPageLoad();
        selenium.click("q:q");
        waitForPageLoad();
        assertTrue(selenium.isTextPresent("Sorry, the only apple has been taken by another guy~"));
        selenium.click("q:q");
        waitForPageLoad();
        selenium.click("q:qq");
        waitForPageLoad();
        assertTrue(selenium.isTextPresent("Congratulations! There are enough small apples!"));
    }

}
