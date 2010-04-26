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

package org.apache.geronimo.testsuite.myfaces;


import org.apache.geronimo.testsupport.SeleniumTestSupport;
import org.testng.annotations.Test;

public class TestMyFaces extends SeleniumTestSupport {

    @Test
    public void testManagedBean() throws Exception {
        selenium.open("/myfaces");
        waitForPageLoad();
        assertEquals("0.99 Please enter your nameHelloUpdate greeting", selenium.getBodyText());
    }
    
    @Test
    public void testPress() throws Exception {
        selenium.open("/myfaces");
        waitForPageLoad();
        selenium.type("form:input1", "Bart Simpson");
        selenium.click("form:button1");
        waitForPageLoad();
        assertEquals("Hello Bart Simpson. We hope you enjoy Apache MyFaces", selenium.getText("xpath=/html/body/h2"));
    }
    
}
