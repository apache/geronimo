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

package org.apache.geronimo.testsuite.javaee6.tests;

import org.apache.geronimo.testsupport.SeleniumTestSupport;
import org.testng.Assert;
import org.testng.annotations.Test;

public class jtaTest extends SeleniumTestSupport {
    
    @Test
    public void testDerbyTxSucessCommit() throws Exception {
        selenium.open("/jta1.1-test/");
        selenium.click("link=BJAcc");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Return");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=SHAcc");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Return");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=TestJTA");
        selenium.waitForPageToLoad("30000");
        // test if transfer from BJ to SH sucess
        selenium.type("amount", "1");
        selenium.type("flag", "0");
        selenium.click("//input[@value='Submit']");
        selenium.waitForPageToLoad("30000");
        Assert.assertEquals("199", selenium.getText("//*[@id=\"bj\"]"));
        Assert.assertEquals("201", selenium.getText("//*[@id=\"sh\"]"));
        // test if the DoTransfer Servlet can invoked again
        selenium.type("amount", "1");
        selenium.type("flag", "0");
        selenium.click("//input[@value='Submit']");
        selenium.waitForPageToLoad("30000");
        Assert.assertEquals("198", selenium.getText("//*[@id=\"bj\"]"));
        Assert.assertEquals("202", selenium.getText("//*[@id=\"sh\"]"));
    }

    @Test
    public void testDerbyTxRollback() throws Exception {
        selenium.open("/jta1.1-test/");
        selenium.click("link=BJAcc");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Return");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=SHAcc");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=Return");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=TestJTA");
        selenium.waitForPageToLoad("30000");
        // test if transaction fail can occured
        selenium.type("amount", "1");
        selenium.type("flag", "1");// input 1 for transaction rollback
        selenium.click("//input[@value='Submit']");
        selenium.waitForPageToLoad("30000");
        Assert.assertEquals("200", selenium.getText("//*[@id=\"bj\"]"));
        Assert.assertEquals("200", selenium.getText("//*[@id=\"sh\"]"));
        // test if the DoTransfer Servlet can invoked again and do
        // transaction.commit()
        selenium.type("amount", "10");
        selenium.type("flag", "0");
        selenium.click("//input[@value='Submit']");
        selenium.waitForPageToLoad("30000");
        Assert.assertEquals("190", selenium.getText("//*[@id=\"bj\"]"));
        Assert.assertEquals("210", selenium.getText("//*[@id=\"sh\"]"));
    }
    
    @Test
    public void testInjection() throws Exception {
        selenium.open("/jta1.1-test/injection");
        selenium.waitForPageToLoad("30000");
        String text = selenium.getBodyText();
        System.out.println(text);
        assertTrue(text.contains("TransactionSynchronizationRegistry: ok"));
        assertTrue(text.contains("TransactionManager: ok"));
    }
}
