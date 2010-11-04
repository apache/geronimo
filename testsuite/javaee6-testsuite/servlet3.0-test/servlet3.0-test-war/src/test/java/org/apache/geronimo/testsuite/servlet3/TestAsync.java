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

package org.apache.geronimo.testsuite.servlet3;

import org.apache.geronimo.testsupport.SeleniumTestSupport;
import org.testng.annotations.Test;

public class TestAsync extends SeleniumTestSupport {

    @Test
    public void testAsyncServlet() throws Exception {
        String appContextStr = System.getProperty("appContext");
        selenium.open(appContextStr);

        selenium.click("link=Test AsyncServlet.");
        waitForPageLoad();

        assertTrue(selenium.isTextPresent("Servlet starts at:"));
        assertTrue(selenium
                .isTextPresent("Task assigned to executor.Servlet finishes at:"));
        assertTrue(selenium.isTextPresent("TaskExecutor starts at:"));
        assertTrue(selenium.isTextPresent("Task finishes."));
        assertTrue(selenium.isTextPresent("TaskExecutor finishes at:"));

        long sfti = Long.parseLong(selenium.getText("xpath=//b[@id='sft']"))/1000;
        long tsti = Long.parseLong(selenium.getText("xpath=//b[@id='tst']"))/1000;
        long tfti = Long.parseLong(selenium.getText("xpath=//b[@id='tft']"))/1000;
        assertTrue(sfti == tsti);
        long intervalTimeMills = tfti - sfti;
        assertTrue(intervalTimeMills > 9L && intervalTimeMills < 12L);
    }}
