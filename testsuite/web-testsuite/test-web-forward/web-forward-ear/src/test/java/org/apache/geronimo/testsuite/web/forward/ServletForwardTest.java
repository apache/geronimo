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

package org.apache.geronimo.testsuite.web.forward;

import org.apache.geronimo.testsupport.SeleniumTestSupport;
import org.testng.annotations.Test;

@Test
public class ServletForwardTest extends SeleniumTestSupport {

    @Test
    public void testServletForward() throws Exception {
        selenium.open("/dispatch1/TestServlet");
        waitForPageLoad();
        System.out.println(selenium.getBodyText());
        assertTrue("Servlet1", selenium.isTextPresent("TestServlet1: 10"));

        selenium.open("/dispatch2/TestServlet");
        waitForPageLoad();
        System.out.println(selenium.getBodyText());
        assertTrue("Servlet2", selenium.isTextPresent("TestServlet2: 20"));

        selenium.open("/dispatch2/TestServlet?mode=forward");
        waitForPageLoad();
        System.out.println(selenium.getBodyText());
        assertTrue("Servlet2->Servlet1 forward", selenium.isTextPresent("TestServlet1: 10"));
        
        
    }
}

