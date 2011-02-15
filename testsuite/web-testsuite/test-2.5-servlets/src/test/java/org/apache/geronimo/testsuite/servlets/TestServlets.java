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

package org.apache.geronimo.testsuite.servlets;


import org.apache.geronimo.testsupport.SeleniumTestSupport;
import org.testng.annotations.Test;

public class TestServlets extends SeleniumTestSupport
{
    @Test
    public void testIndexContent() throws Exception {
        selenium.open("/servlet25/SampleServlet");
        waitForPageLoad();
        assertEquals("Sample application with Servlets 2.5", selenium.getTitle());
        assertEquals("Welcome to Servlets 2.5 samples. Sample Servlet!", selenium.getText("xpath=/html/body"));
    }


    @Test
    public void testIndexContent2() throws Exception {
        selenium.open("/servlet25/SampleServlet2");
        waitForPageLoad();
        assertEquals("Sample application with Servlets 2.5", selenium.getTitle());
        assertEquals("Welcome to Servlets 2.5 samples. Another Sample Servlet!", selenium.getText("xpath=/html/body"));

    }
    @Test
    public void testAddress() throws Exception {
        selenium.open("/servlet25/AddressServlet");
        waitForPageLoad();
        assertEquals("Sample application with Servlets 2.5", selenium.getTitle());
        String actual = selenium.getText("xpath=/html/body");
        assertEquals(true, (actual.contains("Address Test Remote Address:127.0.0.1")||actual.contains("Address Test Remote Address:0:0:0:0:0:0:0:1")));
    }
}