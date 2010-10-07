/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.testsuite.security.ejb;

import org.testng.annotations.Test;
import org.apache.geronimo.testsupport.SeleniumTestSupport;
public class SecurityEJBTest extends SeleniumTestSupport {

    private String baseURL = "http://localhost:8080/";
          
    @Test
    public void testEJBSecurityAnnonymous() throws Exception {
        String appContextStr = System.getProperty("appContext");
        selenium.open(baseURL+appContextStr);
        selenium.click("link=Annonymous");
        selenium.waitForPageToLoad("30000");
        try {
            assertTrue(selenium.isTextPresent("1. SecurityBean.permitAllMethod:true"));
            assertTrue(selenium.isTextPresent("2. SecurityBean.rolesAllowedUserMethod:false"));
            assertTrue(selenium.isTextPresent("3. SecurityBean.rolesAllowedAdminMethod:false"));
            assertTrue(selenium.isTextPresent("4. SecurityBean.denyAllMethod:false"));
            assertTrue(selenium.isTextPresent("5. SecurityRunAsBean.permitAllMethod:true::SecurityBean.permitAllMethod:true"));
            assertTrue(selenium.isTextPresent("6. SecurityRunAsBean.rolesAllowedUserMethod:false"));
            assertTrue(selenium.isTextPresent("7. SecurityRunAsBean.rolesAllowedAdminMethod:false"));
            assertTrue(selenium.isTextPresent("8. SecurityRunAsBean.denyAllMethod:false"));
        } finally {
            selenium.click("link=Logout");
        }
    
    }

    @Test
    public void testEJBSecurityAdmin() throws Exception {
        String appContextStr = System.getProperty("appContext");
        selenium.open(baseURL+appContextStr);
        selenium.click("link=Admin");
        selenium.waitForPageToLoad("30000");
        selenium.type("j_username", "george");
        selenium.type("j_password", "bone");
        selenium.click("//input[@value='Login']");
        selenium.waitForPageToLoad("30000");
        try {
            assertTrue(selenium.isTextPresent("1. SecurityBean.permitAllMethod:true"));
            assertTrue(selenium.isTextPresent("2. SecurityBean.rolesAllowedUserMethod:false"));
            assertTrue(selenium.isTextPresent("3. SecurityBean.rolesAllowedAdminMethod:true"));
            assertTrue(selenium.isTextPresent("4. SecurityBean.denyAllMethod:false"));
            assertTrue(selenium.isTextPresent("5. SecurityRunAsBean.permitAllMethod:true::SecurityBean.permitAllMethod:true"));
            assertTrue(selenium.isTextPresent("6. SecurityRunAsBean.rolesAllowedUserMethod:false"));
            assertTrue(selenium.isTextPresent("7. SecurityRunAsBean.rolesAllowedAdminMethod:true::SecurityBean.rolesAllowedAdminMethod:false"));
            assertTrue(selenium.isTextPresent("8. SecurityRunAsBean.denyAllMethod:false"));
        } finally {
            selenium.click("link=Logout");
        }
    }
    @Test
    public void testEJBSecurityUser() throws Exception {
        String appContextStr = System.getProperty("appContext");
        selenium.open(baseURL+appContextStr);
        selenium.click("link=User");
        selenium.waitForPageToLoad("30000");
        selenium.type("j_username", "metro");
        selenium.type("j_password", "mouse");
        selenium.click("//input[@value='Login']");
        selenium.waitForPageToLoad("30000");
        try {
            assertTrue(selenium.isTextPresent("1. SecurityBean.permitAllMethod:true"));
            assertTrue(selenium.isTextPresent("2. SecurityBean.rolesAllowedUserMethod:true"));
            assertTrue(selenium.isTextPresent("3. SecurityBean.rolesAllowedAdminMethod:false"));
            assertTrue(selenium.isTextPresent("4. SecurityBean.denyAllMethod:false"));
            assertTrue(selenium.isTextPresent("5. SecurityRunAsBean.permitAllMethod:true::SecurityBean.permitAllMethod:true"));
            assertTrue(selenium.isTextPresent("6. SecurityRunAsBean.rolesAllowedUserMethod:true::SecurityBean.rolesAllowedUserMethod:true"));
            assertTrue(selenium.isTextPresent("7. SecurityRunAsBean.rolesAllowedAdminMethod:false"));
            assertTrue(selenium.isTextPresent("8. SecurityRunAsBean.denyAllMethod:false"));
        } finally {
            selenium.click("link=Logout");
        }
    }
}
